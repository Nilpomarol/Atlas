package com.atlas.data.repository

import android.content.Context
import com.atlas.data.local.dao.CountryPhotoDao
import com.atlas.data.local.entity.CountryPhotoEntity
import com.atlas.data.local.mapper.toDomain
import com.atlas.domain.model.CountryPhoto
import com.atlas.domain.repository.ApiKeyRepository
import com.atlas.domain.repository.CountryPhotoApiClient
import com.atlas.domain.repository.CountryPhotoApiResult
import com.atlas.domain.repository.CountryPhotoRepository
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

class CountryPhotoRepositoryImpl(
    private val context: Context,
    private val dao: CountryPhotoDao,
    private val apiClient: CountryPhotoApiClient,
    private val apiKeyRepository: ApiKeyRepository,
) : CountryPhotoRepository {

    override fun observePhoto(iso2: String): Flow<CountryPhoto?> =
        dao.observe(iso2).map { it?.toDomain() }

    override suspend fun refreshIfStale(iso2: String, query: String) = withContext(Dispatchers.IO) {
        val apiKey = apiKeyRepository.observeUnsplashKey().first()
        if (apiKey.isBlank()) return@withContext

        val dir = File(context.filesDir, PHOTOS_DIR).apply { mkdirs() }
        val filename = "${iso2}_portrait.jpg"
        val file = File(dir, filename)
        val existing = dao.getByIso2(iso2)
        if (existing != null && file.exists() && isFresh(existing.fetchedAt)) return@withContext

        when (val result = apiClient.randomPhoto(query, apiKey)) {
            is CountryPhotoApiResult.Success -> {
                val tmp = File(dir, "$filename.tmp")
                if (!download(result.imageUrl, tmp)) return@withContext // keep old photo
                file.delete()
                if (!tmp.renameTo(file)) {
                    tmp.delete()
                    return@withContext
                }
                existing?.filename
                    ?.takeIf { it != filename }
                    ?.let { File(dir, it).delete() }
                dao.upsert(
                    CountryPhotoEntity(
                        countryIso2 = iso2,
                        filename = filename,
                        sourceUrl = result.imageUrl,
                        author = result.author,
                        authorLink = result.authorLink,
                        fetchedAt = Instant.now().toString(),
                    ),
                )
            }
            // NoApiKey / NotFound / RateLimited / Error → keep the existing photo.
            else -> Unit
        }
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
        const val PHOTOS_DIR = "country_photos"
        val MAX_AGE: Duration = Duration.ofHours(24)
    }
}
