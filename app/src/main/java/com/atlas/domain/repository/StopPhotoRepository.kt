package com.atlas.domain.repository

import android.net.Uri
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.StopType
import kotlinx.coroutines.flow.Flow

interface StopPhotoRepository {
    fun observeByStop(stopId: String, stopType: StopType): Flow<List<StopPhoto>>
    fun observeByStopIds(stopIds: List<String>, stopType: StopType): Flow<List<StopPhoto>>
    suspend fun addPhotos(stopId: String, stopType: StopType, uris: List<Uri>): List<StopPhoto>
    suspend fun deletePhoto(photo: StopPhoto)
    suspend fun deleteAllForStop(stopId: String, stopType: StopType)
    suspend fun countByStop(stopId: String, stopType: StopType): Int
}
