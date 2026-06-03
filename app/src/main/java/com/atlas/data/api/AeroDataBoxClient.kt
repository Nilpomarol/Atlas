package com.atlas.data.api

import com.atlas.domain.model.FlightApiPrefill
import com.atlas.domain.model.FlightApiResult
import com.atlas.domain.repository.FlightApiClient
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class AeroDataBoxClient : FlightApiClient {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun lookup(
        flightNumber: String,
        date: String,
        apiKey: String,
    ): FlightApiResult = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/flights/number/$flightNumber/$date"
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = TIMEOUT_MILLIS
            readTimeout = TIMEOUT_MILLIS
            setRequestProperty("X-RapidAPI-Key", apiKey)
            setRequestProperty("X-RapidAPI-Host", RAPID_API_HOST)
            setRequestProperty("Accept", "application/json")
        }

        try {
            when (val code = connection.responseCode) {
                200 -> {
                    val body = connection.inputStream.bufferedReader().use { it.readText() }
                    val flights = json.decodeFromString<List<AeroDataBoxFlightDto>>(body)
                    val flight = flights.firstOrNull()
                        ?: return@withContext FlightApiResult.NotFound

                    FlightApiResult.Success(
                        FlightApiPrefill(
                            flightNumber = flight.number,
                            airlineIata = flight.airline?.iata,
                            airlineName = flight.airline?.name,
                            originIata = flight.departure?.airport?.iata,
                            destinationIata = flight.arrival?.airport?.iata,
                            scheduledDepartureAt = parseAeroDataBoxLocalTime(
                                flight.departure?.scheduledTime?.local,
                            ),
                            scheduledArrivalAt = parseAeroDataBoxLocalTime(
                                flight.arrival?.scheduledTime?.local,
                            ),
                            aircraftModel = flight.aircraft?.model,
                        ),
                    )
                }
                404 -> FlightApiResult.NotFound
                429 -> FlightApiResult.RateLimited
                else -> FlightApiResult.NetworkError("Error del servidor: HTTP $code")
            }
        } catch (_: SerializationException) {
            FlightApiResult.NetworkError("Resposta invàlida del servidor.")
        } catch (_: IOException) {
            FlightApiResult.NetworkError("No s'ha pogut connectar amb l'API de vols.")
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val BASE_URL = "https://aerodatabox.p.rapidapi.com"
        const val RAPID_API_HOST = "aerodatabox.p.rapidapi.com"
        const val TIMEOUT_MILLIS = 10_000
    }
}
