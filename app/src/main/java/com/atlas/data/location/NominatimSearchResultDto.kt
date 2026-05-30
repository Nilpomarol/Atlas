package com.atlas.data.location

import com.atlas.domain.model.LocationSearchResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NominatimSearchResultDto(
    @SerialName("place_id")
    val placeId: Long? = null,
    @SerialName("osm_type")
    val osmType: String? = null,
    @SerialName("osm_id")
    val osmId: Long? = null,
    @SerialName("display_name")
    val displayName: String,
    val name: String? = null,
    val lat: String,
    val lon: String,
    val address: Map<String, String> = emptyMap(),
) {
    fun toDomain(): LocationSearchResult? {
        val latitude = lat.toDoubleOrNull() ?: return null
        val longitude = lon.toDoubleOrNull() ?: return null
        val resultId = when {
            osmType != null && osmId != null -> "$osmType-$osmId"
            placeId != null -> placeId.toString()
            else -> displayName
        }

        return LocationSearchResult(
            id = resultId,
            name = name?.takeIf { it.isNotBlank() } ?: displayName.substringBefore(","),
            displayName = displayName,
            countryIso2 = address["country_code"]?.uppercase(),
            latitude = latitude,
            longitude = longitude,
        )
    }
}
