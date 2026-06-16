package com.atlas.presentation.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.StopType
import com.atlas.domain.repository.CountryRepository
import com.atlas.domain.repository.ExcursionRepository
import com.atlas.domain.repository.ItineraryRepository
import com.atlas.domain.repository.StopPhotoRepository
import com.atlas.domain.repository.TripRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class TripStoryViewModel(
    tripRepository: TripRepository,
    countryRepository: CountryRepository,
    excursionRepository: ExcursionRepository,
    itineraryRepository: ItineraryRepository,
    stopPhotoRepository: StopPhotoRepository,
    tripId: String,
) : ViewModel() {
    private val tripFlow = tripRepository.observeTrip(tripId)
    private val stopsFlow = tripRepository.observeTripStops(tripId)
    private val countriesFlow = countryRepository.observeTrackableCountries()
    private val excursionsFlow = excursionRepository.observeExcursions(tripId)
    private val contentFlow = combine(
        tripFlow,
        stopsFlow,
        countriesFlow,
        excursionsFlow,
    ) { trip, stops, countries, excursions ->
        TripStoryContentData(trip, stops, countries, excursions)
    }

    private val tripStopPhotosFlow = stopsFlow.flatMapLatest { stops ->
        val ids = stops.map { it.id }
        if (ids.isEmpty()) flowOf(emptyMap())
        else stopPhotoRepository.observeByStopIds(ids, StopType.TRIP_STOP)
            .map { photos -> photos.groupBy(StopPhoto::stopId) }
    }

    private val excursionStopPhotosFlow = excursionsFlow.flatMapLatest { excursions ->
        val ids = excursions.flatMap { it.stops }.map { it.id }
        if (ids.isEmpty()) flowOf(emptyMap())
        else stopPhotoRepository.observeByStopIds(ids, StopType.EXCURSION_STOP)
            .map { photos -> photos.groupBy(StopPhoto::stopId) }
    }

    private val itineraryFlow = combine(
        itineraryRepository.observeItineraries(),
        itineraryRepository.observeAllGroups(),
    ) { itineraries, itineraryGroups ->
        TripStoryItineraryData(itineraries, itineraryGroups)
    }

    private val photosFlow = combine(
        tripStopPhotosFlow,
        excursionStopPhotosFlow,
    ) { tripStopPhotos, excursionStopPhotos ->
        TripStoryPhotosData(tripStopPhotos, excursionStopPhotos)
    }

    val uiState: StateFlow<TripStoryUiState> = combine(
        contentFlow,
        itineraryFlow,
        photosFlow,
    ) { content, itineraries, photos ->
        val linkedItinerary = content.trip?.let { currentTrip ->
            itineraries.itineraries.firstOrNull { it.tripId == currentTrip.id }
        }
        buildTripStoryUiState(
            trip = content.trip,
            stops = content.stops,
            countries = content.countries,
            excursions = content.excursions,
            itinerary = linkedItinerary,
            itineraryGroups = itineraries.itineraryGroups,
            tripStopPhotoMap = photos.tripStopPhotos,
            excursionStopPhotoMap = photos.excursionStopPhotos,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TripStoryUiState(),
        )

    class Factory(
        private val tripRepository: TripRepository,
        private val countryRepository: CountryRepository,
        private val excursionRepository: ExcursionRepository,
        private val itineraryRepository: ItineraryRepository,
        private val stopPhotoRepository: StopPhotoRepository,
        private val tripId: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TripStoryViewModel(
                tripRepository = tripRepository,
                countryRepository = countryRepository,
                excursionRepository = excursionRepository,
                itineraryRepository = itineraryRepository,
                stopPhotoRepository = stopPhotoRepository,
                tripId = tripId,
            ) as T
    }
}

private data class TripStoryContentData(
    val trip: com.atlas.domain.model.Trip?,
    val stops: List<com.atlas.domain.model.TripStop>,
    val countries: List<com.atlas.domain.model.Country>,
    val excursions: List<com.atlas.domain.model.Excursion>,
)

private data class TripStoryItineraryData(
    val itineraries: List<com.atlas.domain.model.Itinerary>,
    val itineraryGroups: List<com.atlas.domain.model.ItineraryGroup>,
)

private data class TripStoryPhotosData(
    val tripStopPhotos: Map<String, List<StopPhoto>>,
    val excursionStopPhotos: Map<String, List<StopPhoto>>,
)
