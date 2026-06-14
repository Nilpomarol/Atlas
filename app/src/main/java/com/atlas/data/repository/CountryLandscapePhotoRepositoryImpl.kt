package com.atlas.data.repository

import android.content.Context
import com.atlas.data.local.dao.CountryLandscapePhotoDao
import com.atlas.data.local.entity.CountryLandscapePhotoEntity
import com.atlas.data.local.mapper.StoredLandscapePhoto
import com.atlas.data.local.mapper.encodeLandscapePhotos
import com.atlas.data.local.mapper.toDomain
import com.atlas.domain.model.CountryLandscapePhotos
import com.atlas.domain.repository.ApiKeyRepository
import com.atlas.domain.repository.CountryLandscapePhotoRepository
import com.atlas.domain.repository.CountryLandscapePhotosApiResult
import com.atlas.domain.repository.CountryPhotoApiClient
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CountryLandscapePhotoRepositoryImpl(
    private val context: Context,
    private val dao: CountryLandscapePhotoDao,
    private val apiClient: CountryPhotoApiClient,
    private val apiKeyRepository: ApiKeyRepository,
) : CountryLandscapePhotoRepository {

    override fun observePhotos(iso2: String): Flow<CountryLandscapePhotos?> =
        dao.observe(iso2).map { it?.toDomain() }

    override suspend fun refreshIfStale(iso2: String, query: String) = withContext(Dispatchers.IO) {
        val apiKey = apiKeyRepository.observeUnsplashKey().first()
        if (apiKey.isBlank()) return@withContext

        val dir = File(context.filesDir, PHOTOS_DIR).apply { mkdirs() }
        val existing = dao.getByIso2(iso2)
        if (existing != null && isFresh(existing.fetchedAt) && cachedFilesExist(dir, existing)) {
            return@withContext
        }

        when (val result = apiClient.landscapePhotos(query, apiKey, MAX_PHOTOS)) {
            is CountryLandscapePhotosApiResult.Success -> {
                val stored = mutableListOf<StoredLandscapePhoto>()
                result.photos.forEachIndexed { index, photo ->
                    val filename = "${iso2}_landscape_$index.jpg"
                    val tmp = File(dir, "$filename.tmp")
                    if (!download(photo.imageUrl, tmp)) return@forEachIndexed // skip this one
                    val file = File(dir, filename)
                    file.delete()
                    if (!tmp.renameTo(file)) {
                        tmp.delete()
                        return@forEachIndexed
                    }
                    stored += StoredLandscapePhoto(
                        filename = filename,
                        author = photo.author,
                        authorLink = photo.authorLink,
                    )
                }
                // Only replace the cache if at least one photo downloaded successfully.
                if (stored.isEmpty()) return@withContext

                // Remove any now-unused files from a previous, larger cached set.
                existing?.toDomain()?.photos
                    ?.map { it.filename }
                    ?.filter { old -> stored.none { it.filename == old } }
                    ?.forEach { File(dir, it).delete() }

                dao.upsert(
                    CountryLandscapePhotoEntity(
                        countryIso2 = iso2,
                        photosJson = encodeLandscapePhotos(stored),
                        fetchedAt = Instant.now().toString(),
                    ),
                )
            }
            // NoApiKey / NotFound / RateLimited / Error → keep the existing photos.
            else -> Unit
        }
    }

    private fun cachedFilesExist(dir: File, entity: CountryLandscapePhotoEntity): Boolean {
        val photos = entity.toDomain().photos
        return photos.isNotEmpty() && photos.all { File(dir, it.filename).exists() }
    }

    private fun isFresh(fetchedAt: String): Boolean = try {
        Duration.between(Instant.parse(fetchedAt), Instant.now()) < MAX_AGE
    } catch (e: Exception) {
        false
    }

    private fun download(url: String, dest: File): Boolean = try {
        URL(url).openStream().use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        }
        dest.length() > 0
    } catch (e: Exception) {
        dest.delete()
        false
    }

    private companion object {
        const val PHOTOS_DIR = "country_landscape_photos"
        const val MAX_PHOTOS = 5
        val MAX_AGE: Duration = Duration.ofDays(30)
    }
}
