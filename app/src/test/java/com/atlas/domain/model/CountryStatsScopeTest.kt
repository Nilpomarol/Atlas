package com.atlas.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CountryStatsScopeTest {
    @Test
    fun un195IncludesMembersAndOnlyRequestedObserverStates() {
        assertTrue(CountryStatsScope.UN_195.includes(country("FR", isUnMember = true)))
        assertTrue(CountryStatsScope.UN_195.includes(country("PS", isObserverState = true)))
        assertTrue(CountryStatsScope.UN_195.includes(country("VA", isObserverState = true)))

        assertFalse(CountryStatsScope.UN_195.includes(country("XK", isObserverState = true)))
        assertFalse(CountryStatsScope.UN_195.includes(country("TW")))
    }

    @Test
    fun un197AddsKosovoAndTaiwan() {
        assertTrue(CountryStatsScope.UN_PLUS_KOSOVO_TAIWAN_197.includes(country("XK")))
        assertTrue(CountryStatsScope.UN_PLUS_KOSOVO_TAIWAN_197.includes(country("TW")))
        assertTrue(CountryStatsScope.UN_PLUS_KOSOVO_TAIWAN_197.includes(country("PS")))
        assertTrue(CountryStatsScope.UN_PLUS_KOSOVO_TAIWAN_197.includes(country("VA")))
    }

    @Test
    fun allAtlasIncludesAnyTrackableCountryOrTerritory() {
        assertTrue(
            CountryStatsScope.ALL_ATLAS.includes(
                country("GL", type = CountryType.DEPENDENT_TERRITORY),
            ),
        )
        assertFalse(CountryStatsScope.ALL_ATLAS.includes(country("ZZ", isTrackable = false)))
    }

    @Test
    fun unknownStoredValueFallsBackToAllAtlas() {
        assertSame(CountryStatsScope.ALL_ATLAS, CountryStatsScope.fromStorageKey(null))
        assertSame(CountryStatsScope.ALL_ATLAS, CountryStatsScope.fromStorageKey("old_value"))
    }
}

private fun country(
    iso2: String,
    type: CountryType = CountryType.SOVEREIGN_STATE,
    isUnMember: Boolean = false,
    isObserverState: Boolean = false,
    isTrackable: Boolean = true,
): Country =
    Country(
        iso2 = iso2,
        iso3 = null,
        nameCa = iso2,
        nameEn = iso2,
        type = type,
        parentIso2 = null,
        isUnMember = isUnMember,
        isObserverState = isObserverState,
        isTrackable = isTrackable,
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
