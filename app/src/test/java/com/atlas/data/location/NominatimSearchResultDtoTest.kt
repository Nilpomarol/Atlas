package com.atlas.data.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NominatimSearchResultDtoTest {
    @Test
    fun mapsValidResultToDomain() {
        val result = NominatimSearchResultDto(
            placeId = 123,
            osmType = "node",
            osmId = 456,
            displayName = "Barcelona Sants, Barcelona, Catalunya, Espanya",
            name = "Barcelona Sants",
            lat = "41.3791",
            lon = "2.1400",
            address = mapOf("country_code" to "es"),
        ).toDomain()

        requireNotNull(result)
        assertEquals("node-456", result.id)
        assertEquals("Barcelona Sants", result.name)
        assertEquals("ES", result.countryIso2)
        assertEquals(41.3791, result.latitude, 0.0001)
        assertEquals(2.1400, result.longitude, 0.0001)
    }

    @Test
    fun fallsBackToDisplayNamePrefixWhenNameMissing() {
        val result = NominatimSearchResultDto(
            placeId = 123,
            displayName = "Andorra la Vella, Andorra",
            name = null,
            lat = "42.5063",
            lon = "1.5218",
        ).toDomain()

        requireNotNull(result)
        assertEquals("Andorra la Vella", result.name)
        assertEquals("123", result.id)
    }

    @Test
    fun rejectsInvalidCoordinates() {
        val result = NominatimSearchResultDto(
            displayName = "Invalid",
            lat = "not-a-number",
            lon = "2.0",
        ).toDomain()

        assertNull(result)
    }
}
