package com.atlas.data.local.mapper

import com.atlas.data.local.entity.CountryLandscapePhotoEntity
import com.atlas.domain.model.CountryLandscapePhoto
import com.atlas.domain.model.CountryLandscapePhotos
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Serialized form of a single cached landscape photo, stored inside [photos_json]. */
@Serializable
data class StoredLandscapePhoto(
    val filename: String,
    val author: String? = null,
    val authorLink: String? = null,
)

private val photosJsonCodec = Json { ignoreUnknownKeys = true }

fun CountryLandscapePhotoEntity.toDomain(): CountryLandscapePhotos {
    val stored = runCatching {
        photosJsonCodec.decodeFromString<List<StoredLandscapePhoto>>(photosJson)
    }.getOrDefault(emptyList())
    return CountryLandscapePhotos(
        countryIso2 = countryIso2,
        photos = stored.map { CountryLandscapePhoto(it.filename, it.author, it.authorLink) },
        fetchedAt = fetchedAt,
    )
}

fun encodeLandscapePhotos(photos: List<StoredLandscapePhoto>): String =
    photosJsonCodec.encodeToString(photos)
