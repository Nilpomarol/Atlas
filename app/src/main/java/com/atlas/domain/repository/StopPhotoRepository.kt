package com.atlas.domain.repository

import android.net.Uri
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.StopType
import kotlinx.coroutines.flow.Flow

interface StopPhotoRepository {
    fun observeByStop(stopId: String, stopType: StopType): Flow<List<StopPhoto>>
    fun observeByStopIds(stopIds: List<String>, stopType: StopType): Flow<List<StopPhoto>>
    suspend fun addPhotos(stopId: String, stopType: StopType, uris: List<Uri>): List<StopPhoto>

    /**
     * Rotates a stored photo by [degrees] clockwise, re-saving it under a fresh filename so
     * image-loader caches see new content. Updates the photo row and any trip cover reference.
     * Returns the updated photo, or the original unchanged if the file could not be processed.
     */
    suspend fun rotatePhoto(photo: StopPhoto, degrees: Int): StopPhoto

    suspend fun deletePhoto(photo: StopPhoto)
    suspend fun deleteAllForStop(stopId: String, stopType: StopType)
    suspend fun countByStop(stopId: String, stopType: StopType): Int
}
