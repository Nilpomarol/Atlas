package com.atlas.data.location

import com.atlas.domain.model.LocationSearchResult
import com.atlas.domain.repository.LocationSearchException
import com.atlas.domain.repository.LocationSearchRepository
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class NominatimLocationSearchRepository(
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val userAgent: String = DEFAULT_USER_AGENT,
) : LocationSearchRepository {
    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val rateLimitMutex = Mutex()
    private var lastRequestAtMillis: Long = 0L

    override suspend fun search(query: String): List<LocationSearchResult> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.length < MIN_QUERY_LENGTH) return emptyList()

        enforceRateLimit()

        return withContext(Dispatchers.IO) {
            val encodedQuery = URLEncoder.encode(normalizedQuery, Charsets.UTF_8.name())
            val requestUrl = "$baseUrl/search?q=$encodedQuery&format=jsonv2&addressdetails=1&limit=5"
            val connection = (URL(requestUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MILLIS
                readTimeout = TIMEOUT_MILLIS
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("Accept-Language", "ca,en")
            }

            try {
                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    throw LocationSearchException("La cerca de llocs no està disponible ara mateix.")
                }

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                json.decodeFromString<List<NominatimSearchResultDto>>(response)
                    .mapNotNull { it.toDomain() }
            } catch (error: LocationSearchException) {
                throw error
            } catch (error: IOException) {
                throw LocationSearchException("No s'ha pogut connectar amb el proveïdor de cerca.", error)
            } catch (error: SerializationException) {
                throw LocationSearchException("La resposta del proveïdor de cerca no és vàlida.", error)
            } finally {
                connection.disconnect()
            }
        }
    }

    private suspend fun enforceRateLimit() {
        rateLimitMutex.withLock {
            val now = System.currentTimeMillis()
            val elapsed = now - lastRequestAtMillis
            if (elapsed in 0 until MIN_REQUEST_INTERVAL_MILLIS) {
                delay(MIN_REQUEST_INTERVAL_MILLIS - elapsed)
            }
            lastRequestAtMillis = System.currentTimeMillis()
        }
    }

    private companion object {
        const val DEFAULT_BASE_URL = "https://nominatim.openstreetmap.org"
        const val DEFAULT_USER_AGENT = "AtlasAndroid/0.1 (com.atlas; personal travel atlas)"
        const val MIN_QUERY_LENGTH = 3
        const val MIN_REQUEST_INTERVAL_MILLIS = 1_100L
        const val TIMEOUT_MILLIS = 10_000
    }
}
