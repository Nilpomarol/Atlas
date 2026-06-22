package com.atlas.presentation.itinerary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.domain.model.Itinerary
import com.atlas.domain.repository.AirportRepository
import com.atlas.domain.repository.ItineraryRepository
import com.atlas.domain.usecase.itinerary.CreateItineraryUseCase
import com.atlas.domain.usecase.itinerary.DeleteItineraryUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ItineraryListViewModel(
    itineraryRepository: ItineraryRepository,
    airportRepository: AirportRepository,
    private val createItineraryUseCase: CreateItineraryUseCase,
    private val deleteItineraryUseCase: DeleteItineraryUseCase,
) : ViewModel() {

    private val _navigationEvents = MutableSharedFlow<String>()
    val navigationEvents: SharedFlow<String> = _navigationEvents.asSharedFlow()

    val uiState: StateFlow<ItineraryListUiState> = combine(
        itineraryRepository.observeItineraries(),
        itineraryRepository.observeAllGroups(),
        airportRepository.observeAirports(),
    ) { itineraries, groups, airports ->
        val groupsByItinerary = groups.groupBy { it.itineraryId }
        ItineraryListUiState(
            itineraries = itineraries,
            itineraryTitles = itineraries.associate { itinerary ->
                itinerary.id to itineraryCodeLabel(groupsByItinerary[itinerary.id].orEmpty(), airports)
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ItineraryListUiState(),
    )

    fun onCreateItineraryClick() {
        viewModelScope.launch {
            val newId = createItineraryUseCase(title = "", notes = null)
            _navigationEvents.emit(newId)
        }
    }

    fun onDeleteItinerary(itinerary: Itinerary) {
        viewModelScope.launch { deleteItineraryUseCase(itinerary) }
    }

    class Factory(
        private val itineraryRepository: ItineraryRepository,
        private val airportRepository: AirportRepository,
        private val createItineraryUseCase: CreateItineraryUseCase,
        private val deleteItineraryUseCase: DeleteItineraryUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ItineraryListViewModel(
            itineraryRepository = itineraryRepository,
            airportRepository = airportRepository,
            createItineraryUseCase = createItineraryUseCase,
            deleteItineraryUseCase = deleteItineraryUseCase,
        ) as T
    }
}

data class ItineraryListUiState(
    val itineraries: List<Itinerary> = emptyList(),
    val itineraryTitles: Map<String, String> = emptyMap(),
)
