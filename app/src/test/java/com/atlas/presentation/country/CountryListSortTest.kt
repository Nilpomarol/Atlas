package com.atlas.presentation.country

import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryTrackingState
import com.atlas.domain.model.CountryType
import org.junit.Assert.assertEquals
import org.junit.Test

class CountryListSortTest {

    private fun country(iso2: String, name: String) = Country(
        iso2 = iso2,
        iso3 = null,
        nameCa = name,
        nameEn = null,
        type = CountryType.SOVEREIGN_STATE,
        parentIso2 = null,
        isUnMember = true,
        isObserverState = false,
        isTrackable = true,
        continent = "Europe",
        subregion = null,
        flagEmoji = null,
        flagAsset = null,
        latitude = null,
        longitude = null,
        capitalNameCa = null,
        capitalNameEn = null,
        capitalLatitude = null,
        capitalLongitude = null,
    )

    private fun row(iso2: String, name: String) =
        CountryListItemUiState(country(iso2, name), CountryTrackingState.Empty)

    private val rows = listOf(
        row("ZW", "Zimbàbue"),
        row("AD", "Andorra"),
        row("FR", "França"),
    )

    private fun v(numeric: Double) = CountrySortValue(numeric, numeric.toString())

    // FR intentionally has no stat values, so it should sort last under any stat sort.
    private val values = mapOf(
        "ZW" to mapOf("population" to v(16_000_000.0), "area" to v(390_000.0), "gdp" to v(2.0e10), "hdi" to v(0.55)),
        "AD" to mapOf("population" to v(80_000.0), "area" to v(468.0), "gdp" to v(3.0e9), "hdi" to v(0.86)),
    )

    @Test
    fun nameSortAscendingIsAlphabetical() {
        val result = sortCountryRows(rows, CountrySort.Name, ascending = true, values).map { it.country.iso2 }
        assertEquals(listOf("AD", "FR", "ZW"), result)
    }

    @Test
    fun nameSortDescendingIsReverseAlphabetical() {
        val result = sortCountryRows(rows, CountrySort.Name, ascending = false, values).map { it.country.iso2 }
        assertEquals(listOf("ZW", "FR", "AD"), result)
    }

    @Test
    fun populationSortDescendingWithMissingLast() {
        val result = sortCountryRows(rows, CountrySort.Population, ascending = false, values).map { it.country.iso2 }
        assertEquals(listOf("ZW", "AD", "FR"), result)
    }

    @Test
    fun populationSortAscendingKeepsMissingLast() {
        val result = sortCountryRows(rows, CountrySort.Population, ascending = true, values).map { it.country.iso2 }
        assertEquals(listOf("AD", "ZW", "FR"), result)
    }

    @Test
    fun gdpSortDescendingWithMissingLast() {
        val result = sortCountryRows(rows, CountrySort.Gdp, ascending = false, values).map { it.country.iso2 }
        assertEquals(listOf("ZW", "AD", "FR"), result)
    }

    @Test
    fun hdiSortDescendingWithMissingLast() {
        val result = sortCountryRows(rows, CountrySort.Hdi, ascending = false, values).map { it.country.iso2 }
        assertEquals(listOf("AD", "ZW", "FR"), result)
    }

    @Test
    fun foldAccentsStripsCatalanDiacritics() {
        assertEquals("franca", "França".foldAccents())
        assertEquals("austria", "Àustria".foldAccents())
        assertEquals("aland", "Åland".foldAccents())
    }
}
