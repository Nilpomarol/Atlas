package com.atlas.domain.usecase.photo

import android.net.Uri
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.StopType
import com.atlas.domain.repository.StopPhotoRepository

class AddStopPhotosUseCase(private val stopPhotoRepository: StopPhotoRepository) {
    suspend operator fun invoke(stopId: String, stopType: StopType, uris: List<Uri>): List<StopPhoto> {
        val current = stopPhotoRepository.countByStop(stopId, stopType)
        val remaining = (MAX_PHOTOS - current).coerceAtLeast(0)
        if (remaining == 0 || uris.isEmpty()) return emptyList()
        return stopPhotoRepository.addPhotos(stopId, stopType, uris.take(remaining))
    }

    companion object {
        const val MAX_PHOTOS = 25
    }
}
