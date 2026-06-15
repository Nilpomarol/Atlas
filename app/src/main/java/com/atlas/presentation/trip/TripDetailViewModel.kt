package com.atlas.presentation.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.atlas.domain.model.Country
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.Excursion
import com.atlas.domain.model.ExcursionStop
import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.model.Itinerary
import com.atlas.domain.model.LocationSearchResult
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.StopType
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import com.atlas.domain.model.TripStopSource
import com.atlas.domain.repository.CountryRepository
import com.atlas.domain.repository.ExcursionRepository
import com.atlas.domain.repository.ItineraryRepository
import com.atlas.domain.repository.StopPhotoRepository
import com.atlas.domain.repository.TripMapPreferencesRepository
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.usecase.photo.AddStopPhotosUseCase
import com.atlas.domain.usecase.photo.DeleteStopPhotoUseCase
import com.atlas.domain.usecase.photo.SetTripCoverPhotoUseCase
import com.atlas.domain.usecase.itinerary.UpdateItineraryUseCase
import com.atlas.domain.usecase.itinerary.RemoveGeneratedTripStopsForItineraryUseCase
import com.atlas.domain.usecase.itinerary.SyncGeneratedTripStopsForItineraryUseCase
import com.atlas.domain.usecase.excursion.CreateExcursionStopUseCase
import com.atlas.domain.usecase.excursion.CreateExcursionUseCase
import com.atlas.domain.usecase.excursion.DeleteExcursionStopUseCase
import com.atlas.domain.usecase.excursion.DeleteExcursionUseCase
import com.atlas.domain.usecase.excursion.ReorderExcursionStopsUseCase
import com.atlas.domain.usecase.excursion.ReorderExcursionsUseCase
import com.atlas.domain.usecase.excursion.UpdateExcursionStopUseCase
import com.atlas.domain.usecase.trip.CreateTripStopUseCase
import com.atlas.domain.usecase.trip.DeleteTripUseCase
import com.atlas.domain.usecase.trip.DeleteTripStopUseCase
import com.atlas.domain.usecase.trip.ReorderTripStopsUseCase
import com.atlas.domain.usecase.trip.UpdateTripStopUseCase
import com.atlas.domain.usecase.trip.UpdateTripUseCase
import com.atlas.domain.usecase.location.SearchLocationsUseCase
import com.atlas.domain.validation.FlexibleDateValidator
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.presentation.date.FlexibleDateRangeDraftUiState
import com.atlas.presentation.date.updateField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TripDetailViewModel(
    tripRepository: TripRepository,
    countryRepository: CountryRepository,
    itineraryRepository: ItineraryRepository,
    excursionRepository: ExcursionRepository,
    private val deleteTripUseCase: DeleteTripUseCase,
    private val updateTripUseCase: UpdateTripUseCase,
    private val updateItineraryUseCase: UpdateItineraryUseCase,
    private val syncGeneratedTripStopsForItineraryUseCase: SyncGeneratedTripStopsForItineraryUseCase,
    private val removeGeneratedTripStopsForItineraryUseCase: RemoveGeneratedTripStopsForItineraryUseCase,
    private val createTripStopUseCase: CreateTripStopUseCase,
    private val updateTripStopUseCase: UpdateTripStopUseCase,
    private val reorderTripStopsUseCase: ReorderTripStopsUseCase,
    private val deleteTripStopUseCase: DeleteTripStopUseCase,
    private val searchLocationsUseCase: SearchLocationsUseCase,
    private val createExcursionUseCase: CreateExcursionUseCase,
    private val deleteExcursionUseCase: DeleteExcursionUseCase,
    private val reorderExcursionsUseCase: ReorderExcursionsUseCase,
    private val createExcursionStopUseCase: CreateExcursionStopUseCase,
    private val updateExcursionStopUseCase: UpdateExcursionStopUseCase,
    private val deleteExcursionStopUseCase: DeleteExcursionStopUseCase,
    private val reorderExcursionStopsUseCase: ReorderExcursionStopsUseCase,
    private val flexibleDateValidator: FlexibleDateValidator,
    private val tripMapPreferencesRepository: TripMapPreferencesRepository,
    private val stopPhotoRepository: StopPhotoRepository,
    private val addStopPhotosUseCase: AddStopPhotosUseCase,
    private val deleteStopPhotoUseCase: DeleteStopPhotoUseCase,
    private val setTripCoverPhotoUseCase: SetTripCoverPhotoUseCase,
    private val tripId: String,
) : ViewModel() {
    private val stopDraft = MutableStateFlow(TripStopDraftUiState())
    private val tripDraft = MutableStateFlow(TripEditorDraftUiState())
    private var stopLocationSearchJob: Job? = null
    private var excursionStopLocationSearchJob: Job? = null
    private val excursionStopDraft = MutableStateFlow(ExcursionStopDraftUiState())
    private val isItineraryPickerOpen = MutableStateFlow(false)

    private val tripContentData = combine(
        tripRepository.observeTrip(tripId),
        tripRepository.observeTripStops(tripId),
        countryRepository.observeTrackableCountries(),
        excursionRepository.observeExcursions(tripId),
    ) { trip, stops, countries, excursions ->
        TripContentData(trip, stops, countries, excursions)
    }

    private val draftData = combine(
        stopDraft,
        tripDraft,
        excursionStopDraft,
        isItineraryPickerOpen,
    ) { stopDraft, tripDraft, excursionStopDraft, isItineraryPickerOpen ->
        TripDraftData(stopDraft, tripDraft, excursionStopDraft, isItineraryPickerOpen)
    }

    @Suppress("UNCHECKED_CAST")
    private val tripStopPhotosFlow = tripRepository.observeTripStops(tripId)
        .flatMapLatest { stops ->
            val ids = stops.map { it.id }
            if (ids.isEmpty()) flowOf(emptyMap())
            else stopPhotoRepository.observeByStopIds(ids, StopType.TRIP_STOP)
                .map { photos -> photos.groupBy { it.stopId } }
        }

    @Suppress("UNCHECKED_CAST")
    private val excursionStopPhotosFlow = excursionRepository.observeExcursions(tripId)
        .flatMapLatest { excursions ->
            val ids = excursions.flatMap { it.stops }.map { it.id }
            if (ids.isEmpty()) flowOf(emptyMap())
            else stopPhotoRepository.observeByStopIds(ids, StopType.EXCURSION_STOP)
                .map { photos -> photos.groupBy { it.stopId } }
        }

    private val photosData = combine(
        tripStopPhotosFlow,
        excursionStopPhotosFlow,
    ) { tripStopPhotos, excursionStopPhotos ->
        PhotosData(tripStopPhotos, excursionStopPhotos)
    }

    val uiState: StateFlow<TripDetailUiState> = combine(
        tripContentData,
        itineraryRepository.observeItineraries(),
        draftData,
        tripMapPreferencesRepository.observeGeneratedStopsVisible(tripId),
        photosData,
    ) { content, itineraries, drafts, generatedStopsVisibleOnMap, photos ->
        val linkedItinerary = itineraries.firstOrNull { it.tripId == tripId }
        TripDetailUiState(
            trip = content.trip,
            stops = content.stops,
            countries = content.countries,
            excursions = content.excursions,
            linkedItinerary = linkedItinerary,
            availableItineraries = itineraries.filter { it.tripId == null },
            stopDraft = drafts.stopDraft,
            tripDraft = drafts.tripDraft,
            excursionStopDraft = drafts.excursionStopDraft,
            isItineraryPickerOpen = drafts.isItineraryPickerOpen,
            generatedStopsVisibleOnMap = generatedStopsVisibleOnMap,
            tripStopPhotoMap = photos.tripStopPhotos,
            excursionStopPhotoMap = photos.excursionStopPhotos,
            photoGallery = buildTripPhotoGalleryUiState(
                stops = content.stops,
                excursions = content.excursions,
                tripStopPhotoMap = photos.tripStopPhotos,
                excursionStopPhotoMap = photos.excursionStopPhotos,
            ),
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TripDetailUiState(),
        )

    fun onAddStopClick() {
        val firstCountryIso2 = uiState.value.countries.firstOrNull()?.iso2.orEmpty()
        val dateRange = uiState.value.trip?.dateRange?.start?.let { startDate ->
            FlexibleDateRangeDraftUiState.fromDateRange(
                FlexibleDateRange(
                    start = startDate,
                    end = null,
                    precision = uiState.value.trip?.dateRange?.precision ?: startDate.precision,
                ),
            )
        } ?: FlexibleDateRangeDraftUiState()

        stopDraft.update {
            TripStopDraftUiState(
                isOpen = true,
                countryIso2 = firstCountryIso2,
                dateRange = dateRange,
            )
        }
    }

    fun onDismissStopDraft() {
        stopLocationSearchJob?.cancel()
        stopDraft.update { TripStopDraftUiState() }
    }

    fun onEditTripClick() {
        val trip = uiState.value.trip ?: return
        tripDraft.update { TripEditorDraftUiState.fromTrip(trip) }
    }

    fun onDismissTripDraft() {
        tripDraft.update { TripEditorDraftUiState() }
    }

    fun onOpenItineraryPicker() {
        isItineraryPickerOpen.value = true
    }

    fun onDismissItineraryPicker() {
        isItineraryPickerOpen.value = false
    }

    fun onLinkItinerary(itinerary: Itinerary) {
        val trip = uiState.value.trip ?: return
        viewModelScope.launch {
            updateItineraryUseCase(itinerary.copy(tripId = trip.id))
            syncGeneratedTripStopsForItineraryUseCase(itineraryId = itinerary.id, tripId = trip.id)
            onDismissItineraryPicker()
        }
    }

    fun onUnlinkItinerary() {
        val itinerary = uiState.value.linkedItinerary ?: return
        viewModelScope.launch {
            removeGeneratedTripStopsForItineraryUseCase(itinerary.id)
            updateItineraryUseCase(itinerary.copy(tripId = null))
        }
    }

    fun onTripTitleChanged(title: String) {
        tripDraft.update { it.copy(title = title, validationError = null) }
    }

    fun onTripStatusChanged(status: com.atlas.domain.model.TravelStatus) {
        tripDraft.update { it.copy(status = status) }
    }

    fun onTripDatePrecisionChanged(precision: DatePrecision) {
        tripDraft.update {
            it.copy(
                dateRange = it.dateRange.copy(precision = precision),
                validationError = null,
            )
        }
    }

    fun onTripDateFieldChanged(
        field: FlexibleDateRangeDraftField,
        value: String,
    ) {
        tripDraft.update {
            it.copy(
                dateRange = it.dateRange.updateField(field, value),
                validationError = null,
            )
        }
    }

    fun onTripNotesChanged(notes: String) {
        tripDraft.update { it.copy(notes = notes) }
    }

    fun onSaveTripDraft() {
        val draft = tripDraft.value
        val tripId = draft.tripId ?: return
        val title = draft.title.trim()
        val dateRange = draft.dateRange.toDateRange()

        if (title.isBlank()) {
            tripDraft.update { it.copy(validationError = "El títol és obligatori.") }
            return
        }

        if (dateRange == null && draft.dateRange.hasAnyInput()) {
            tripDraft.update { it.copy(validationError = "Revisa la data del viatge: falta algun camp o el format no és vàlid.") }
            return
        }

        if (dateRange != null && !flexibleDateValidator.isValid(dateRange)) {
            tripDraft.update { it.copy(validationError = "Revisa la data del viatge: el rang o la precisió no són vàlids.") }
            return
        }

        viewModelScope.launch {
            updateTripUseCase(
                Trip(
                    id = tripId,
                    title = title,
                    status = draft.status,
                    dateRange = dateRange,
                    notes = draft.notes,
                    coverPhotoFilename = draft.coverPhotoFilename,
                ),
            )
            onDismissTripDraft()
        }
    }

    fun onEditStop(stop: TripStop) {
        stopDraft.update { TripStopDraftUiState.fromStop(stop) }
    }

    fun onStopLocationNameChanged(locationName: String) {
        stopDraft.update { it.copy(locationName = locationName, validationError = null) }
    }

    fun onLocationSearchQueryChanged(query: String) {
        stopDraft.update {
            it.copy(
                locationSearchQuery = query,
                locationSearchError = null,
                locationSearchResults = emptyList(),
                isSearchingLocation = false,
            )
        }
        stopLocationSearchJob?.cancel()
        if (query.trim().length < 3) return
        stopLocationSearchJob = viewModelScope.launch {
            delay(400)
            val trimmed = query.trim()
            stopDraft.update { it.copy(isSearchingLocation = true) }
            runCatching { searchLocationsUseCase(trimmed) }
                .onSuccess { results ->
                    stopDraft.update {
                        it.copy(
                            isSearchingLocation = false,
                            locationSearchResults = results,
                            lastLocationSearchQuery = trimmed,
                            isManualEntryVisible = results.isEmpty(),
                            locationSearchError = if (results.isEmpty()) {
                                "No s'ha trobat cap lloc. Pots afegir-lo manualment."
                            } else {
                                null
                            },
                        )
                    }
                }
                .onFailure { error ->
                    stopDraft.update {
                        it.copy(
                            isSearchingLocation = false,
                            locationSearchError = error.message ?: "La cerca no està disponible ara mateix.",
                        )
                    }
                }
        }
    }

    fun onUseManualStopEntryClick() {
        stopDraft.update {
            it.copy(
                isManualEntryVisible = true,
                locationSearchResults = emptyList(),
                locationSearchError = null,
                validationError = null,
            )
        }
    }

    fun onLocationSearchResultSelected(result: LocationSearchResult) {
        val supportedCountryIso2 = result.countryIso2
            ?.takeIf { iso2 -> uiState.value.countries.any { it.iso2 == iso2 } }

        stopDraft.update {
            it.copy(
                locationName = result.name,
                countryIso2 = supportedCountryIso2 ?: it.countryIso2,
                latitude = result.latitude.toString(),
                longitude = result.longitude.toString(),
                locationSearchQuery = result.name,
                locationSearchResults = emptyList(),
                isManualEntryVisible = supportedCountryIso2 == null,
                locationSearchError = if (supportedCountryIso2 == null) {
                    "S'han omplert nom i coordenades. Revisa el país manualment."
                } else {
                    null
                },
                validationError = null,
            )
        }
    }

    fun onStopCountryChanged(countryIso2: String) {
        stopDraft.update { it.copy(countryIso2 = countryIso2, validationError = null) }
    }

    fun onStopLatitudeChanged(latitude: String) {
        stopDraft.update { it.copy(latitude = latitude, validationError = null) }
    }

    fun onStopLongitudeChanged(longitude: String) {
        stopDraft.update { it.copy(longitude = longitude, validationError = null) }
    }

    fun onStopDatePrecisionChanged(precision: DatePrecision) {
        stopDraft.update {
            it.copy(
                dateRange = it.dateRange.copy(precision = precision),
                validationError = null,
            )
        }
    }

    fun onStopDateFieldChanged(
        field: FlexibleDateRangeDraftField,
        value: String,
    ) {
        stopDraft.update {
            it.copy(
                dateRange = it.dateRange.updateField(field, value),
                validationError = null,
            )
        }
    }

    fun onStopNotesChanged(notes: String) {
        stopDraft.update { it.copy(notes = notes) }
    }

    fun onSaveStopDraft() {
        val trip = uiState.value.trip ?: return
        val draft = stopDraft.value
        val locationName = draft.locationName.trim()
        val dateRange = draft.dateRange.toDateRange()
        val latitude = draft.latitude.trim().ifBlank { null }?.toDoubleOrNull()
        val longitude = draft.longitude.trim().ifBlank { null }?.toDoubleOrNull()

        if (locationName.isBlank()) {
            stopDraft.update { it.copy(validationError = "El nom del lloc és obligatori.") }
            return
        }

        if (draft.countryIso2.isBlank()) {
            stopDraft.update { it.copy(validationError = "Cal seleccionar un país o territori.") }
            return
        }

        if ((draft.latitude.isNotBlank() && latitude == null) || (draft.longitude.isNotBlank() && longitude == null)) {
            stopDraft.update { it.copy(validationError = "Les coordenades han de ser números vàlids.") }
            return
        }

        if ((draft.latitude.isBlank() && draft.longitude.isNotBlank()) || (draft.latitude.isNotBlank() && draft.longitude.isBlank())) {
            stopDraft.update { it.copy(validationError = "Informa latitud i longitud, o deixa totes dues buides.") }
            return
        }

        if (latitude != null && latitude !in -90.0..90.0) {
            stopDraft.update { it.copy(validationError = "La latitud ha d'estar entre -90 i 90.") }
            return
        }

        if (longitude != null && longitude !in -180.0..180.0) {
            stopDraft.update { it.copy(validationError = "La longitud ha d'estar entre -180 i 180.") }
            return
        }

        if (dateRange == null && draft.dateRange.hasAnyInput()) {
            stopDraft.update { it.copy(validationError = "Revisa la data de la parada: falta algun camp o el format no és vàlid.") }
            return
        }

        if (dateRange != null && !flexibleDateValidator.isValid(dateRange)) {
            stopDraft.update { it.copy(validationError = "Revisa la data de la parada: el rang o la precisió no són vàlids.") }
            return
        }

        viewModelScope.launch {
            if (draft.stopId == null) {
                createTripStopUseCase(
                    tripId = trip.id,
                    locationName = locationName,
                    countryIso2 = draft.countryIso2,
                    latitude = latitude,
                    longitude = longitude,
                    dateRange = dateRange,
                    notes = draft.notes,
                )
            } else {
                updateTripStopUseCase(
                    TripStop(
                        id = draft.stopId,
                        tripId = trip.id,
                        locationName = locationName,
                        countryIso2 = draft.countryIso2,
                        latitude = latitude,
                        longitude = longitude,
                        dateRange = dateRange,
                        notes = draft.notes,
                        sortOrder = draft.sortOrder,
                    ),
                )
            }
            onDismissStopDraft()
        }
    }

    fun onDeleteStop(stop: TripStop) {
        viewModelScope.launch {
            deleteTripStopUseCase(stop)
        }
    }

    fun onAddExcursionClick(anchorTripStopId: String? = null) {
        val trip = uiState.value.trip ?: return

        viewModelScope.launch {
            createExcursionUseCase(
                tripId = trip.id,
                anchorTripStopId = anchorTripStopId,
                title = DEFAULT_EXCURSION_TITLE,
                notes = null,
            )
        }
    }

    fun onDeleteExcursion(excursion: Excursion) {
        viewModelScope.launch { deleteExcursionUseCase(excursion) }
    }

    fun onMoveExcursionUp(excursion: Excursion) {
        moveExcursion(excursion, -1)
    }

    fun onMoveExcursionDown(excursion: Excursion) {
        moveExcursion(excursion, 1)
    }

    fun onAddExcursionStopClick(excursionId: String) {
        val firstCountryIso2 = uiState.value.countries.firstOrNull()?.iso2.orEmpty()
        excursionStopDraft.update {
            ExcursionStopDraftUiState(
                isOpen = true,
                excursionId = excursionId,
                countryIso2 = firstCountryIso2,
            )
        }
    }

    fun onEditExcursionStop(stop: ExcursionStop) {
        excursionStopDraft.update { ExcursionStopDraftUiState.fromStop(stop) }
    }

    fun onDismissExcursionStopDraft() {
        excursionStopLocationSearchJob?.cancel()
        excursionStopDraft.update { ExcursionStopDraftUiState() }
    }

    fun onExcursionStopLocationSearchQueryChanged(query: String) {
        excursionStopDraft.update {
            it.copy(
                locationSearchQuery = query,
                locationSearchError = null,
                locationSearchResults = emptyList(),
                isSearchingLocation = false,
            )
        }
        excursionStopLocationSearchJob?.cancel()
        if (query.trim().length < 3) return
        excursionStopLocationSearchJob = viewModelScope.launch {
            delay(400)
            val trimmed = query.trim()
            excursionStopDraft.update { it.copy(isSearchingLocation = true) }
            runCatching { searchLocationsUseCase(trimmed) }
                .onSuccess { results ->
                    excursionStopDraft.update {
                        it.copy(
                            isSearchingLocation = false,
                            locationSearchResults = results,
                            lastLocationSearchQuery = trimmed,
                            isManualEntryVisible = results.isEmpty(),
                            locationSearchError = if (results.isEmpty()) {
                                "No s'ha trobat cap lloc. Pots afegir-lo manualment."
                            } else {
                                null
                            },
                        )
                    }
                }
                .onFailure { error ->
                    excursionStopDraft.update {
                        it.copy(
                            isSearchingLocation = false,
                            locationSearchError = error.message ?: "La cerca no està disponible ara mateix.",
                        )
                    }
                }
        }
    }

    fun onExcursionStopLocationSearchResultSelected(result: LocationSearchResult) {
        val supportedCountryIso2 = result.countryIso2
            ?.takeIf { iso2 -> uiState.value.countries.any { it.iso2 == iso2 } }

        excursionStopDraft.update {
            it.copy(
                locationName = result.name,
                countryIso2 = supportedCountryIso2 ?: it.countryIso2,
                latitude = result.latitude.toString(),
                longitude = result.longitude.toString(),
                locationSearchQuery = result.name,
                locationSearchResults = emptyList(),
                isManualEntryVisible = supportedCountryIso2 == null,
                locationSearchError = if (supportedCountryIso2 == null) {
                    "S'han omplert nom i coordenades. Revisa el pais manualment."
                } else {
                    null
                },
                validationError = null,
            )
        }
    }

    fun onUseManualExcursionStopEntryClick() {
        excursionStopDraft.update {
            it.copy(
                isManualEntryVisible = true,
                locationSearchResults = emptyList(),
                locationSearchError = null,
                validationError = null,
            )
        }
    }

    fun onExcursionStopLocationNameChanged(locationName: String) {
        excursionStopDraft.update { it.copy(locationName = locationName, validationError = null) }
    }

    fun onExcursionStopCountryChanged(countryIso2: String) {
        excursionStopDraft.update { it.copy(countryIso2 = countryIso2, validationError = null) }
    }

    fun onExcursionStopLatitudeChanged(latitude: String) {
        excursionStopDraft.update { it.copy(latitude = latitude, validationError = null) }
    }

    fun onExcursionStopLongitudeChanged(longitude: String) {
        excursionStopDraft.update { it.copy(longitude = longitude, validationError = null) }
    }

    fun onExcursionStopDatePrecisionChanged(precision: DatePrecision) {
        excursionStopDraft.update {
            it.copy(
                dateRange = it.dateRange.copy(precision = precision),
                validationError = null,
            )
        }
    }

    fun onExcursionStopDateFieldChanged(
        field: FlexibleDateRangeDraftField,
        value: String,
    ) {
        excursionStopDraft.update {
            it.copy(
                dateRange = it.dateRange.updateField(field, value),
                validationError = null,
            )
        }
    }

    fun onExcursionStopNotesChanged(notes: String) {
        excursionStopDraft.update { it.copy(notes = notes) }
    }

    fun onSaveExcursionStopDraft() {
        val draft = excursionStopDraft.value
        val locationName = draft.locationName.trim()
        val latitude = draft.latitude.trim().ifBlank { null }?.toDoubleOrNull()
        val longitude = draft.longitude.trim().ifBlank { null }?.toDoubleOrNull()
        val dateRange = draft.dateRange.toDateRange()

        if (locationName.isBlank()) {
            excursionStopDraft.update { it.copy(validationError = "El nom del lloc es obligatori.") }
            return
        }
        if (draft.countryIso2.isBlank()) {
            excursionStopDraft.update { it.copy(validationError = "Cal seleccionar un pais o territori.") }
            return
        }
        if ((draft.latitude.isNotBlank() && latitude == null) || (draft.longitude.isNotBlank() && longitude == null)) {
            excursionStopDraft.update { it.copy(validationError = "Les coordenades han de ser numeros valids.") }
            return
        }
        if ((draft.latitude.isBlank() && draft.longitude.isNotBlank()) || (draft.latitude.isNotBlank() && draft.longitude.isBlank())) {
            excursionStopDraft.update { it.copy(validationError = "Informa latitud i longitud, o deixa totes dues buides.") }
            return
        }
        if (dateRange == null && draft.dateRange.hasAnyInput()) {
            excursionStopDraft.update { it.copy(validationError = "Revisa la data de la parada: falta algun camp o el format no es valid.") }
            return
        }
        if (dateRange != null && !flexibleDateValidator.isValid(dateRange)) {
            excursionStopDraft.update { it.copy(validationError = "Revisa la data de la parada: el rang o la precisio no son valids.") }
            return
        }

        viewModelScope.launch {
            if (draft.stopId == null) {
                createExcursionStopUseCase(
                    excursionId = draft.excursionId,
                    locationName = locationName,
                    countryIso2 = draft.countryIso2,
                    latitude = latitude,
                    longitude = longitude,
                    dateRange = dateRange,
                    notes = draft.notes,
                )
            } else {
                updateExcursionStopUseCase(
                    ExcursionStop(
                        id = draft.stopId,
                        excursionId = draft.excursionId,
                        locationName = locationName,
                        countryIso2 = draft.countryIso2,
                        latitude = latitude,
                        longitude = longitude,
                        dateRange = dateRange,
                        notes = draft.notes,
                        sortOrder = draft.sortOrder,
                    ),
                )
            }
            onDismissExcursionStopDraft()
        }
    }

    fun onDeleteExcursionStop(stop: ExcursionStop) {
        viewModelScope.launch { deleteExcursionStopUseCase(stop) }
    }

    fun onMoveExcursionStopUp(excursionId: String, stop: ExcursionStop) {
        moveExcursionStop(excursionId, stop, -1)
    }

    fun onMoveExcursionStopDown(excursionId: String, stop: ExcursionStop) {
        moveExcursionStop(excursionId, stop, 1)
    }

    fun onMoveStopUp(stop: TripStop) {
        moveStop(stop = stop, offset = -1)
    }

    fun onMoveStopDown(stop: TripStop) {
        moveStop(stop = stop, offset = 1)
    }

    fun onDeleteTrip() {
        val trip = uiState.value.trip ?: return
        viewModelScope.launch {
            deleteTripUseCase(trip)
        }
    }

    fun onAddPhotos(stopId: String, stopType: StopType, uris: List<Uri>) {
        viewModelScope.launch {
            addStopPhotosUseCase(stopId, stopType, uris)
        }
    }

    fun onDeletePhoto(photo: StopPhoto) {
        viewModelScope.launch {
            deleteStopPhotoUseCase(photo)
        }
    }

    fun onSetCoverPhoto(photo: StopPhoto?) {
        viewModelScope.launch {
            setTripCoverPhotoUseCase(tripId, photo)
        }
    }

    fun onGeneratedStopsVisibleOnMapChanged(isVisible: Boolean) {
        viewModelScope.launch {
            tripMapPreferencesRepository.setGeneratedStopsVisible(
                tripId = tripId,
                isVisible = isVisible,
            )
        }
    }

    private fun moveStop(
        stop: TripStop,
        offset: Int,
    ) {
        if (stop.source != TripStopSource.MANUAL) return

        val stops = uiState.value.stops.toMutableList()
        val fromIndex = stops.indexOfFirst { it.id == stop.id }
        val toIndex = fromIndex + offset
        if (fromIndex !in stops.indices || toIndex !in stops.indices) return

        val moved = stops.removeAt(fromIndex)
        stops.add(toIndex, moved)

        viewModelScope.launch {
            reorderTripStopsUseCase(stops)
        }
    }

    private fun moveExcursion(
        excursion: Excursion,
        offset: Int,
    ) {
        val excursions = uiState.value.excursions.toMutableList()
        val fromIndex = excursions.indexOfFirst { it.id == excursion.id }
        val toIndex = fromIndex + offset
        if (fromIndex !in excursions.indices || toIndex !in excursions.indices) return

        val moved = excursions.removeAt(fromIndex)
        excursions.add(toIndex, moved)

        viewModelScope.launch {
            reorderExcursionsUseCase(excursions)
        }
    }

    private fun moveExcursionStop(
        excursionId: String,
        stop: ExcursionStop,
        offset: Int,
    ) {
        val stops = uiState.value.excursions
            .firstOrNull { it.id == excursionId }
            ?.stops
            ?.toMutableList() ?: return
        val fromIndex = stops.indexOfFirst { it.id == stop.id }
        val toIndex = fromIndex + offset
        if (fromIndex !in stops.indices || toIndex !in stops.indices) return

        val moved = stops.removeAt(fromIndex)
        stops.add(toIndex, moved)

        viewModelScope.launch {
            reorderExcursionStopsUseCase(stops)
        }
    }

    class Factory(
        private val tripRepository: TripRepository,
        private val countryRepository: CountryRepository,
        private val deleteTripUseCase: DeleteTripUseCase,
        private val updateTripUseCase: UpdateTripUseCase,
        private val itineraryRepository: ItineraryRepository,
        private val excursionRepository: ExcursionRepository,
        private val updateItineraryUseCase: UpdateItineraryUseCase,
        private val syncGeneratedTripStopsForItineraryUseCase: SyncGeneratedTripStopsForItineraryUseCase,
        private val removeGeneratedTripStopsForItineraryUseCase: RemoveGeneratedTripStopsForItineraryUseCase,
        private val createTripStopUseCase: CreateTripStopUseCase,
        private val updateTripStopUseCase: UpdateTripStopUseCase,
        private val reorderTripStopsUseCase: ReorderTripStopsUseCase,
        private val deleteTripStopUseCase: DeleteTripStopUseCase,
        private val searchLocationsUseCase: SearchLocationsUseCase,
        private val createExcursionUseCase: CreateExcursionUseCase,
        private val deleteExcursionUseCase: DeleteExcursionUseCase,
        private val reorderExcursionsUseCase: ReorderExcursionsUseCase,
        private val createExcursionStopUseCase: CreateExcursionStopUseCase,
        private val updateExcursionStopUseCase: UpdateExcursionStopUseCase,
        private val deleteExcursionStopUseCase: DeleteExcursionStopUseCase,
        private val reorderExcursionStopsUseCase: ReorderExcursionStopsUseCase,
        private val flexibleDateValidator: FlexibleDateValidator,
        private val tripMapPreferencesRepository: TripMapPreferencesRepository,
        private val stopPhotoRepository: StopPhotoRepository,
        private val addStopPhotosUseCase: AddStopPhotosUseCase,
        private val deleteStopPhotoUseCase: DeleteStopPhotoUseCase,
        private val setTripCoverPhotoUseCase: SetTripCoverPhotoUseCase,
        private val tripId: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TripDetailViewModel(
                tripRepository = tripRepository,
                countryRepository = countryRepository,
                excursionRepository = excursionRepository,
                deleteTripUseCase = deleteTripUseCase,
                updateTripUseCase = updateTripUseCase,
                itineraryRepository = itineraryRepository,
                updateItineraryUseCase = updateItineraryUseCase,
                syncGeneratedTripStopsForItineraryUseCase = syncGeneratedTripStopsForItineraryUseCase,
                removeGeneratedTripStopsForItineraryUseCase = removeGeneratedTripStopsForItineraryUseCase,
                createTripStopUseCase = createTripStopUseCase,
                updateTripStopUseCase = updateTripStopUseCase,
                reorderTripStopsUseCase = reorderTripStopsUseCase,
                deleteTripStopUseCase = deleteTripStopUseCase,
                searchLocationsUseCase = searchLocationsUseCase,
                createExcursionUseCase = createExcursionUseCase,
                deleteExcursionUseCase = deleteExcursionUseCase,
                reorderExcursionsUseCase = reorderExcursionsUseCase,
                createExcursionStopUseCase = createExcursionStopUseCase,
                updateExcursionStopUseCase = updateExcursionStopUseCase,
                deleteExcursionStopUseCase = deleteExcursionStopUseCase,
                reorderExcursionStopsUseCase = reorderExcursionStopsUseCase,
                flexibleDateValidator = flexibleDateValidator,
                tripMapPreferencesRepository = tripMapPreferencesRepository,
                stopPhotoRepository = stopPhotoRepository,
                addStopPhotosUseCase = addStopPhotosUseCase,
                deleteStopPhotoUseCase = deleteStopPhotoUseCase,
                setTripCoverPhotoUseCase = setTripCoverPhotoUseCase,
                tripId = tripId,
            ) as T
        }
    }

    private companion object {
        const val DEFAULT_EXCURSION_TITLE = "Excursió"
    }
}

data class TripDetailUiState(
    val trip: Trip? = null,
    val stops: List<TripStop> = emptyList(),
    val countries: List<Country> = emptyList(),
    val excursions: List<Excursion> = emptyList(),
    val linkedItinerary: Itinerary? = null,
    val availableItineraries: List<Itinerary> = emptyList(),
    val stopDraft: TripStopDraftUiState = TripStopDraftUiState(),
    val tripDraft: TripEditorDraftUiState = TripEditorDraftUiState(),
    val excursionStopDraft: ExcursionStopDraftUiState = ExcursionStopDraftUiState(),
    val isItineraryPickerOpen: Boolean = false,
    val generatedStopsVisibleOnMap: Boolean = true,
    val tripStopPhotoMap: Map<String, List<StopPhoto>> = emptyMap(),
    val excursionStopPhotoMap: Map<String, List<StopPhoto>> = emptyMap(),
    val photoGallery: TripPhotoGalleryUiState = TripPhotoGalleryUiState(),
)

private data class TripContentData(
    val trip: Trip?,
    val stops: List<TripStop>,
    val countries: List<Country>,
    val excursions: List<Excursion>,
)

private data class PhotosData(
    val tripStopPhotos: Map<String, List<StopPhoto>>,
    val excursionStopPhotos: Map<String, List<StopPhoto>>,
)

private data class TripDraftData(
    val stopDraft: TripStopDraftUiState,
    val tripDraft: TripEditorDraftUiState,
    val excursionStopDraft: ExcursionStopDraftUiState,
    val isItineraryPickerOpen: Boolean,
)

data class ExcursionStopDraftUiState(
    val isOpen: Boolean = false,
    val stopId: String? = null,
    val excursionId: String = "",
    val locationName: String = "",
    val countryIso2: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val dateRange: FlexibleDateRangeDraftUiState = FlexibleDateRangeDraftUiState(),
    val locationSearchQuery: String = "",
    val locationSearchResults: List<LocationSearchResult> = emptyList(),
    val isSearchingLocation: Boolean = false,
    val isManualEntryVisible: Boolean = false,
    val locationSearchError: String? = null,
    val lastLocationSearchQuery: String? = null,
    val notes: String = "",
    val sortOrder: Int = 0,
    val validationError: String? = null,
) {
    companion object {
        fun fromStop(stop: ExcursionStop): ExcursionStopDraftUiState = ExcursionStopDraftUiState(
            isOpen = true,
            stopId = stop.id,
            excursionId = stop.excursionId,
            locationName = stop.locationName,
            countryIso2 = stop.countryIso2,
            latitude = stop.latitude?.toString().orEmpty(),
            longitude = stop.longitude?.toString().orEmpty(),
            dateRange = FlexibleDateRangeDraftUiState.fromDateRange(stop.dateRange),
            locationSearchQuery = stop.locationName,
            isManualEntryVisible = stop.latitude == null || stop.longitude == null,
            notes = stop.notes.orEmpty(),
            sortOrder = stop.sortOrder,
        )
    }
}
