package com.atlas.domain.usecase.photo

import com.atlas.domain.model.StopPhoto
import com.atlas.domain.repository.StopPhotoRepository

/** Rotates a stored stop photo 90° clockwise to correct its orientation. */
class RotateStopPhotoUseCase(private val stopPhotoRepository: StopPhotoRepository) {
    suspend operator fun invoke(photo: StopPhoto, degrees: Int = 90) {
        stopPhotoRepository.rotatePhoto(photo, degrees)
    }
}
