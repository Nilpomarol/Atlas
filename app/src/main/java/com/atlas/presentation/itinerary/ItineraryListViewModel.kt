package com.atlas.presentation.itinerary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.domain.model.Itinerary
import com.atlas.domain.repository.ItineraryRepository
import com.atlas.domain.usecase.itinerary.CreateItineraryUseCase
import com.atlas.domain.usecase.itinerary.DeleteItineraryUseCase
import com.atlas.domain.usecase.itinerary.UpdateItineraryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ItineraryListViewModel(
    itineraryRepository: ItineraryRepository,
    private val createItineraryUseCase: CreateItineraryUseCase,
    private val updateItineraryUseCase: UpdateItineraryUseCase,
    private val deleteItineraryUseCase: DeleteItineraryUseCase,
) : ViewModel() {

    private val draft = MutableStateFlow(ItineraryEditorDraft())

    val uiState: StateFlow<ItineraryListUiState> = combine(
        itineraryRepository.observeItineraries(),
        draft,
    ) { itineraries, draft ->
        ItineraryListUiState(itineraries = itineraries, draft = draft)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ItineraryListUiState(),
    )

    fun onCreateItineraryClick() {
        draft.update { ItineraryEditorDraft(isOpen = true) }
    }

    fun onEditItineraryClick(itinerary: Itinerary) {
        draft.update {
            ItineraryEditorDraft(
                isOpen = true,
                itineraryId = itinerary.id,
                title = itinerary.title,
                notes = itinerary.notes ?: "",
            )
        }
    }

    fun onDismissDraft() {
        draft.update { ItineraryEditorDraft() }
    }

    fun onTitleChanged(title: String) {
        draft.update { it.copy(title = title, validationError = null) }
    }

    fun onNotesChanged(notes: String) {
        draft.update { it.copy(notes = notes) }
    }

    fun onSaveDraft() {
        val d = draft.value
        val title = d.title.trim()
        if (title.isBlank()) {
            draft.update { it.copy(validationError = "El títol és obligatori.") }
            return
        }
        viewModelScope.launch {
            if (d.itineraryId == null) {
                createItineraryUseCase(title = title, notes = d.notes)
            } else {
                val existingItinerary = uiState.value.itineraries.firstOrNull { it.id == d.itineraryId }
                updateItineraryUseCase(
                    Itinerary(
                        id = d.itineraryId,
                        title = title,
                        tripId = existingItinerary?.tripId,
                        notes = d.notes.trim().ifBlank { null },
                    ),
                )
            }
            onDismissDraft()
        }
    }

    fun onDeleteItinerary(itinerary: Itinerary) {
        viewModelScope.launch { deleteItineraryUseCase(itinerary) }
    }

    class Factory(
        private val itineraryRepository: ItineraryRepository,
        private val createItineraryUseCase: CreateItineraryUseCase,
        private val updateItineraryUseCase: UpdateItineraryUseCase,
        private val deleteItineraryUseCase: DeleteItineraryUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ItineraryListViewModel(
            itineraryRepository = itineraryRepository,
            createItineraryUseCase = createItineraryUseCase,
            updateItineraryUseCase = updateItineraryUseCase,
            deleteItineraryUseCase = deleteItineraryUseCase,
        ) as T
    }
}

data class ItineraryListUiState(
    val itineraries: List<Itinerary> = emptyList(),
    val draft: ItineraryEditorDraft = ItineraryEditorDraft(),
)

data class ItineraryEditorDraft(
    val isOpen: Boolean = false,
    val itineraryId: String? = null,
    val title: String = "",
    val notes: String = "",
    val validationError: String? = null,
)
