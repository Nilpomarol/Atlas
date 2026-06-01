package com.atlas.domain.usecase.airport

import com.atlas.domain.model.Airport
import com.atlas.domain.repository.AirportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SearchAirportsUseCaseTest {
    @Test
    fun shortQueryReturnsEmptyResultsWithoutRepositorySearch() = runBlocking {
        val repository = FakeAirportRepository()
        val useCase = SearchAirportsUseCase(repository)

        val result = useCase("B").first()

        assertEquals(emptyList<Airport>(), result)
        assertFalse(repository.wasSearched)
    }

    @Test
    fun trimsQueryBeforeSearching() = runBlocking {
        val repository = FakeAirportRepository()
        val useCase = SearchAirportsUseCase(repository)

        val result = useCase(" BCN ").first()

        assertEquals("BCN", repository.lastQuery)
        assertEquals(listOf(testAirport), result)
    }

    private class FakeAirportRepository : AirportRepository {
        var wasSearched = false
        var lastQuery: String? = null

        override fun searchAirports(query: String): Flow<List<Airport>> {
            wasSearched = true
            lastQuery = query
            return flowOf(listOf(testAirport))
        }
    }

    private companion object {
        val testAirport = Airport(
            id = "BCN",
            iata = "BCN",
            icao = "LEBL",
            name = "Barcelona-El Prat Airport",
            city = "Barcelona",
            countryIso2 = "ES",
            latitude = 41.2974,
            longitude = 2.0833,
            timezone = "Europe/Madrid",
        )
    }
}
