package com.atlas.data.api

import com.atlas.domain.repository.CurrencyRateApiClient
import com.atlas.domain.repository.CurrencyRateApiResult
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Fetches EUR-based exchange rates from frankfurter.app, an ECB-backed free service
 * that needs no API key. Uses the same lightweight direct-HTTP pattern as the other
 * Atlas API clients; no Retrofit dependency.
 */
class FrankfurterClient : CurrencyRateApiClient {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetchRate(currencyCode: String): CurrencyRateApiResult =
        withContext(Dispatchers.IO) {
            val code = currencyCode.trim().uppercase()
            if (code.isBlank() || code == "EUR") return@withContext CurrencyRateApiResult.NotFound

            val to = URLEncoder.encode(code, StandardCharsets.UTF_8.name())
            val url = "$BASE_URL/latest?from=EUR&to=$to"

            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MILLIS
                readTimeout = TIMEOUT_MILLIS
                setRequestProperty("Accept", "application/json")
            }

            try {
                when (val status = connection.responseCode) {
                    200 -> {
                        val body = connection.inputStream.bufferedReader().use { it.readText() }
                        val dto = json.decodeFromString<FrankfurterResponseDto>(body)
                        val rate = dto.rates[code]
                        if (rate != null && rate > 0) {
                            CurrencyRateApiResult.Success(rate)
                        } else {
                            CurrencyRateApiResult.NotFound
                        }
                    }

                    404, 422 -> CurrencyRateApiResult.NotFound
                    else -> CurrencyRateApiResult.Error("HTTP $status")
                }
            } catch (_: SerializationException) {
                CurrencyRateApiResult.Error("Resposta invàlida del servei de canvi.")
            } catch (_: IOException) {
                CurrencyRateApiResult.Error("No s'ha pogut connectar amb el servei de canvi.")
            } finally {
                connection.disconnect()
            }
        }

    private companion object {
        const val BASE_URL = "https://api.frankfurter.app"
        const val TIMEOUT_MILLIS = 15_000
    }
}

@Serializable
private data class FrankfurterResponseDto(
    val base: String? = null,
    val date: String? = null,
    val rates: Map<String, Double> = emptyMap(),
)
