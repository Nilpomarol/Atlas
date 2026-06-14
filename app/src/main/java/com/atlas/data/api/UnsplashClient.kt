package com.atlas.data.api

import com.atlas.domain.repository.ApiLandscapePhoto
import com.atlas.domain.repository.CountryLandscapePhotosApiResult
import com.atlas.domain.repository.CountryPhotoApiClient
import com.atlas.domain.repository.CountryPhotoApiResult
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class UnsplashClient : CountryPhotoApiClient {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun randomPhoto(query: String, apiKey: String): CountryPhotoApiResult =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) return@withContext CountryPhotoApiResult.NoApiKey

            val q = URLEncoder.encode(query, StandardCharsets.UTF_8.name())

            val url = "$BASE_URL/search/photos" +
                    "?query=$q" +
                    "&orientation=portrait" +
                    "&content_filter=high" +
                    "&order_by=relevant" +
                    "&per_page=20"

            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MILLIS
                readTimeout = TIMEOUT_MILLIS
                setRequestProperty("Authorization", "Client-ID $apiKey")
                setRequestProperty("Accept-Version", "v1")
                setRequestProperty("Accept", "application/json")
            }

            try {
                when (val code = connection.responseCode) {
                    200 -> {
                        val body = connection.inputStream.bufferedReader().use { it.readText() }
                        val dto = json.decodeFromString<UnsplashSearchResponseDto>(body)

                        val candidates = dto.results
                            .filter { it.hasUsableImageUrl() && it.isPortrait() }
                            .ifEmpty { dto.results.filter { it.hasUsableImageUrl() } }
                        if (candidates.isEmpty()) return@withContext CountryPhotoApiResult.NotFound
                        // Rotate through the top results: a different one each day, deterministic.
                        val index = (LocalDate.now().toEpochDay() % candidates.size).toInt()
                        val photo = candidates[index]

                        val imageUrl = photo.urls?.regular ?: photo.urls?.full
                        ?: return@withContext CountryPhotoApiResult.NotFound

                        CountryPhotoApiResult.Success(
                            imageUrl = imageUrl,
                            author = photo.user?.name,
                            authorLink = photo.user?.links?.html,
                        )
                    }

                    401, 403 -> CountryPhotoApiResult.NoApiKey
                    404 -> CountryPhotoApiResult.NotFound
                    429 -> CountryPhotoApiResult.RateLimited
                    else -> CountryPhotoApiResult.Error("HTTP $code")
                }
            } catch (_: SerializationException) {
                CountryPhotoApiResult.Error("Resposta invàlida d'Unsplash.")
            } catch (_: IOException) {
                CountryPhotoApiResult.Error("No s'ha pogut connectar amb Unsplash.")
            } finally {
                connection.disconnect()
            }
        }

    override suspend fun landscapePhotos(query: String, apiKey: String, count: Int): CountryLandscapePhotosApiResult =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) return@withContext CountryLandscapePhotosApiResult.NoApiKey

            val q = URLEncoder.encode(query, StandardCharsets.UTF_8.name())

            val url = "$BASE_URL/search/photos" +
                    "?query=$q" +
                    "&orientation=landscape" +
                    "&content_filter=high" +
                    "&order_by=relevant" +
                    "&per_page=$LANDSCAPE_SEARCH_SIZE"

            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MILLIS
                readTimeout = TIMEOUT_MILLIS
                setRequestProperty("Authorization", "Client-ID $apiKey")
                setRequestProperty("Accept-Version", "v1")
                setRequestProperty("Accept", "application/json")
            }

            try {
                when (val code = connection.responseCode) {
                    200 -> {
                        val body = connection.inputStream.bufferedReader().use { it.readText() }
                        val dto = json.decodeFromString<UnsplashSearchResponseDto>(body)

                        val photos = dto.results
                            .filter { it.hasUsableImageUrl() && it.isLandscape() }
                            .ifEmpty { dto.results.filter { it.hasUsableImageUrl() } }
                            .take(count.coerceAtLeast(1))
                            .mapNotNull { photo ->
                                val imageUrl = photo.urls?.regular ?: photo.urls?.full ?: return@mapNotNull null
                                ApiLandscapePhoto(
                                    imageUrl = imageUrl,
                                    author = photo.user?.name,
                                    authorLink = photo.user?.links?.html,
                                )
                            }
                        if (photos.isEmpty()) {
                            CountryLandscapePhotosApiResult.NotFound
                        } else {
                            CountryLandscapePhotosApiResult.Success(photos)
                        }
                    }

                    401, 403 -> CountryLandscapePhotosApiResult.NoApiKey
                    404 -> CountryLandscapePhotosApiResult.NotFound
                    429 -> CountryLandscapePhotosApiResult.RateLimited
                    else -> CountryLandscapePhotosApiResult.Error("HTTP $code")
                }
            } catch (_: SerializationException) {
                CountryLandscapePhotosApiResult.Error("Resposta invàlida d'Unsplash.")
            } catch (_: IOException) {
                CountryLandscapePhotosApiResult.Error("No s'ha pogut connectar amb Unsplash.")
            } finally {
                connection.disconnect()
            }
        }

    private fun UnsplashPhotoDto.hasUsableImageUrl(): Boolean {
        return urls?.regular != null || urls?.full != null
    }

    private fun UnsplashPhotoDto.isPortrait(): Boolean {
        val w = width ?: return true
        val h = height ?: return true
        return h > w
    }

    private fun UnsplashPhotoDto.isLandscape(): Boolean {
        val w = width ?: return true
        val h = height ?: return true
        return w > h
    }

    private companion object {
        const val BASE_URL = "https://api.unsplash.com"
        const val TIMEOUT_MILLIS = 15_000
        const val LANDSCAPE_SEARCH_SIZE = 20
    }
}

@Serializable
private data class UnsplashSearchResponseDto(
    val results: List<UnsplashPhotoDto> = emptyList(),
)

@Serializable
private data class UnsplashPhotoDto(
    val id: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val likes: Int? = null,
    val urls: Urls? = null,
    val user: User? = null,
) {
    @Serializable
    data class Urls(
        val regular: String? = null,
        val full: String? = null,
    )

    @Serializable
    data class User(
        val name: String? = null,
        val links: Links? = null,
    ) {
        @Serializable
        data class Links(
            val html: String? = null,
        )
    }
}
