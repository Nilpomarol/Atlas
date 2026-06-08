package com.atlas.domain.usecase.photo

import com.atlas.domain.model.StopPhoto
import com.atlas.domain.repository.TripRepository

class SetTripCoverPhotoUseCase(private val tripRepository: TripRepository) {
    suspend operator fun invoke(tripId: String, photo: StopPhoto?) {
        tripRepository.setCoverPhoto(tripId, photo?.filename)
    }
}
