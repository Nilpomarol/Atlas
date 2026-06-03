package com.atlas.data.api

import com.atlas.domain.model.AircraftApiAircraft
import com.atlas.domain.model.AircraftApiResult
import com.atlas.domain.model.FlightApiPrefill
import com.atlas.domain.model.FlightApiResult
import com.atlas.domain.repository.AircraftApiClient
import com.atlas.domain.repository.FlightApiClient
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class AeroDataBoxClient : FlightApiClient, AircraftApiClient {

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
                            aircraftRegistration = flight.aircraft?.registration,
                            aircraftModeS = flight.aircraft?.modeS,
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

    override suspend fun lookupAircraftByRegistration(
        registration: String,
        apiKey: String,
    ): AircraftApiResult = withContext(Dispatchers.IO) {
        val encodedRegistration = URLEncoder.encode(registration, StandardCharsets.UTF_8.name())
        val url = "$BASE_URL/aircrafts/Reg/$encodedRegistration?withImage=true"
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
                    val dto = json.decodeFromString<AeroDataBoxAircraftDetailsDto>(body)
                    val reg = dto.registration ?: registration
                    AircraftApiResult.Success(
                        AircraftApiAircraft(
                            registration = reg,
                            aeroDataBoxId = dto.id,
                            active = dto.active,
                            serialNumber = dto.serial,
                            hexIcao = dto.hexIcao,
                            airlineName = dto.airlineName,
                            iataType = dto.iataType,
                            iataCodeShort = dto.iataCodeShort,
                            icaoCode = dto.icaoCode,
                            model = dto.model,
                            modelCode = dto.modelCode,
                            numSeats = dto.numSeats,
                            rolloutDate = dto.rolloutDate?.toIsoDate(),
                            firstFlightDate = dto.firstFlightDate?.toIsoDate(),
                            deliveryDate = dto.deliveryDate?.toIsoDate(),
                            registrationDate = dto.registrationDate?.toIsoDate(),
                            typeName = dto.typeName,
                            numEngines = dto.numEngines,
                            engineType = dto.engineType,
                            isFreighter = dto.isFreighter,
                            productionLine = dto.productionLine,
                            ageYears = dto.ageYears,
                            verified = dto.verified,
                            imageUrl = dto.image?.url,
                            imageWebUrl = dto.image?.webUrl,
                            imageAuthor = dto.image?.author,
                            imageTitle = dto.image?.title,
                            imageLicense = dto.image?.license,
                        ),
                    )
                }
                204, 404 -> AircraftApiResult.NotFound
                429 -> AircraftApiResult.RateLimited
                else -> AircraftApiResult.NetworkError("Error del servidor: HTTP $code")
            }
        } catch (_: SerializationException) {
            AircraftApiResult.NetworkError("Resposta invÃ lida del servidor.")
        } catch (_: IOException) {
            AircraftApiResult.NetworkError("No s'ha pogut connectar amb l'API d'aeronaus.")
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

private fun String.toIsoDate(): String = take(10)
