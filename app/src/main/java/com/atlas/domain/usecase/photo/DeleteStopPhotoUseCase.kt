package com.atlas.domain.usecase.photo

import com.atlas.domain.model.StopPhoto
import com.atlas.domain.repository.StopPhotoRepository

class DeleteStopPhotoUseCase(private val stopPhotoRepository: StopPhotoRepository) {
    suspend operator fun invoke(photo: StopPhoto) {
        stopPhotoRepository.deletePhoto(photo)
    }
}
