package com.atlas.ui.components.geo

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object AtlasGeoAssetLoader {
    private const val CountriesAssetPath = "geo/ne_110m_admin_0_countries.geojson"
    private const val Countries50mAssetPath = "geo/ne_50m_admin_0_countries.geojson"
    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var cachedCountries: GeoFeatureCollection? = null

    @Volatile
    private var cachedCountries50m: GeoFeatureCollection? = null

    suspend fun loadCountries(context: Context): GeoFeatureCollection {
        cachedCountries?.let { return it }
        return withContext(Dispatchers.IO) {
            synchronized(this@AtlasGeoAssetLoader) {
                cachedCountries ?: parseCountries(context.assets.open(CountriesAssetPath).bufferedReader().use { it.readText() })
                    .also { cachedCountries = it }
            }
        }
    }

    suspend fun loadCountries50m(context: Context): GeoFeatureCollection {
        cachedCountries50m?.let { return it }
        return withContext(Dispatchers.IO) {
            cachedCountries50m ?: try {
                parseCountries(context.assets.open(Countries50mAssetPath).bufferedReader().use { it.readText() })
                    .also { cachedCountries50m = it }
            } catch (_: Exception) {
                loadCountries(context)
            }
        }
    }

    private fun parseCountries(rawJson: String): GeoFeatureCollection {
        val root = json.parseToJsonElement(rawJson).jsonObject
        val countries = root["features"]
            ?.jsonArray
            .orEmpty()
            .mapNotNull { it.jsonObject.toCountryFeatureOrNull() }
        return GeoFeatureCollection(countries = countries)
    }

    private fun JsonObject.toCountryFeatureOrNull(): GeoCountryFeature? {
        val properties = this["properties"]?.jsonObject ?: return null
        val geometry = this["geometry"]?.jsonObject ?: return null
        val polygons = geometry.toPolygons()
        if (polygons.isEmpty()) return null

        return GeoCountryFeature(
            iso2 = properties.stringOrNull("ISO_A2").takeValidCode()
                ?: properties.stringOrNull("ISO_A2_EH").takeValidCode(),
            iso3 = properties.stringOrNull("ISO_A3").takeValidCode()
                ?: properties.stringOrNull("ADM0_A3").takeValidCode(),
            name = properties.stringOrNull("NAME_LONG")
                ?: properties.stringOrNull("ADMIN")
                ?: properties.stringOrNull("NAME")
                ?: "Unknown",
            continent = properties.stringOrNull("CONTINENT"),
            polygons = polygons,
        )
    }

    private fun JsonObject.toPolygons(): List<GeoPolygon> {
        val type = stringOrNull("type") ?: return emptyList()
        val coordinates = this["coordinates"]?.jsonArray ?: return emptyList()
        return when (type) {
            "Polygon" -> listOfNotNull(coordinates.toPolygonOrNull())
            "MultiPolygon" -> coordinates.mapNotNull { it.jsonArray.toPolygonOrNull() }
            else -> emptyList()
        }
    }

    private fun JsonArray.toPolygonOrNull(): GeoPolygon? {
        val rings = mapNotNull { it.jsonArray.toRingOrNull() }
        return rings.takeIf { it.isNotEmpty() }?.let { GeoPolygon(it) }
    }

    private fun JsonArray.toRingOrNull(): GeoRing? {
        val points = mapNotNull { it.toCoordinateOrNull() }
        return points.takeIf { it.size >= 3 }?.let { GeoRing(it) }
    }

    private fun JsonElement.toCoordinateOrNull(): GeoCoordinate? {
        val position = jsonArray
        val longitude = position.getOrNull(0)?.jsonPrimitive?.doubleOrNull ?: return null
        val latitude = position.getOrNull(1)?.jsonPrimitive?.doubleOrNull ?: return null
        return GeoCoordinate(latitude = latitude, longitude = longitude)
    }

    private fun JsonObject.stringOrNull(key: String): String? =
        this[key]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }

    private fun String?.takeValidCode(): String? =
        this?.takeIf { it != "-99" }?.uppercase()
}
