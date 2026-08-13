package com.atlas.presentation.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.atlas.domain.model.Country
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.model.Itinerary
import com.atlas.domain.model.LocationSearchResult
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.StopType
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import com.atlas.domain.repository.AirportRepository
import com.atlas.domain.repository.CountryRepository
import com.atlas.domain.repository.ItineraryRepository
import com.atlas.domain.repository.StopPhotoRepository
import com.atlas.domain.repository.TripMapPreferencesRepository
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.usecase.photo.AddStopPhotosUseCase
import com.atlas.domain.usecase.photo.DeleteStopPhotoUseCase
import com.atlas.domain.usecase.photo.RotateStopPhotoUseCase
import com.atlas.domain.usecase.photo.SetTripCoverPhotoUseCase
import com.atlas.domain.usecase.itinerary.UpdateItineraryUseCase
import com.atlas.domain.usecase.itinerary.RemoveGeneratedTripStopsForItineraryUseCase
import com.atlas.domain.usecase.itinerary.SyncGeneratedTripStopsForItineraryUseCase
import com.atlas.domain.usecase.trip.CreateTripStopUseCase
import com.atlas.domain.usecase.trip.DeleteTripUseCase
import com.atlas.domain.usecase.trip.DeleteTripStopUseCase
import com.atlas.domain.usecase.trip.ReorderTripStopsUseCase
import com.atlas.domain.usecase.trip.UpdateTripStopUseCase
import com.atlas.domain.usecase.trip.UpdateTripUseCase
import com.atlas.domain.usecase.location.SearchLocationsUseCase
import com.atlas.domain.validation.FlexibleDateValidator
import com.atlas.presentation.itinerary.itineraryCodeLabel
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
    airportRepository: AirportRepository,
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
    private val flexibleDateValidator: FlexibleDateValidator,
    private val tripMapPreferencesRepository: TripMapPreferencesRepository,
    private val stopPhotoRepository: StopPhotoRepository,
    private val addStopPhotosUseCase: AddStopPhotosUseCase,
    private val deleteStopPhotoUseCase: DeleteStopPhotoUseCase,
    private val rotateStopPhotoUseCase: RotateStopPhotoUseCase,
    private val setTripCoverPhotoUseCase: SetTripCoverPhotoUseCase,
    private val tripId: String,
) : ViewModel() {
    private val stopDraft = MutableStateFlow(TripStopDraftUiState())
    private val tripDraft = MutableStateFlow(TripEditorDraftUiState())
    private var stopLocationSearchJob: Job? = null
    private val isItineraryPickerOpen = MutableStateFlow(false)

    private val tripContentData = combine(
        tripRepository.observeTrip(tripId),
        tripRepository.observeTripStops(tripId),
        countryRepository.observeTrackableCountries(),
    ) { trip, stops, countries ->
        TripContentData(trip, stops, countries)
    }

    private val draftData = combine(
        stopDraft,
        tripDraft,
        isItineraryPickerOpen,
    ) { stopDraft, tripDraft, isItineraryPickerOpen ->
        TripDraftData(stopDraft, tripDraft, isItineraryPickerOpen)
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

    private val photosData = tripStopPhotosFlow

    // Itineraries plus their route-derived display titles (see [itineraryCodeLabel]).
    private val itineraryData = combine(
        itineraryRepository.observeItineraries(),
        itineraryRepository.observeAllGroups(),
        airportRepository.observeAirports(),
    ) { itineraries, groups, airports ->
        val groupsByItinerary = groups.groupBy { it.itineraryId }
        val titles = itineraries.associate { itinerary ->
            itinerary.id to itineraryCodeLabel(groupsByItinerary[itinerary.id].orEmpty(), airports)
        }
        ItineraryData(
            itineraries = itineraries,
            titles = titles,
            groups = groups,
            airportsById = airports.associateBy { it.id },
        )
    }

    val uiState: StateFlow<TripDetailUiState> = combine(
        tripContentData,
        itineraryData,
        draftData,
        tripMapPreferencesRepository.observeGeneratedStopsVisible(tripId),
        photosData,
    ) { content, itineraryData, drafts, generatedStopsVisibleOnMap, photos ->
        val linkedItinerary = itineraryData.itineraries.firstOrNull { it.tripId == tripId }
        TripDetailUiState(
            trip = content.trip,
            stops = content.stops,
            countries = content.countries,
            linkedItinerary = linkedItinerary,
            availableItineraries = itineraryData.itineraries.filter { it.tripId == null },
            itineraryTitles = itineraryData.titles,
            stopDraft = drafts.stopDraft,
            tripDraft = drafts.tripDraft,
            isItineraryPickerOpen = drafts.isItineraryPickerOpen,
            generatedStopsVisibleOnMap = generatedStopsVisibleOnMap,
            tripStopPhotoMap = photos,
            photoGallery = buildTripPhotoGalleryUiState(
                stops = content.stops,
                stopPhotoMap = photos,
            ),
            timeline = buildTripTimeline(
                stops = content.stops,
                groups = itineraryData.groups,
                airportsById = itineraryData.airportsById,
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

    /**
     * Adds a place visited *from* [parentStopId]. The user picks a place, never a record
     * type — nesting comes from where the action was taken.
     */
    fun onAddSideTripClick(parentStopId: String) {
        val parent = uiState.value.stops.firstOrNull { it.id == parentStopId } ?: return
        stopDraft.update {
            TripStopDraftUiState(
                isOpen = true,
                parentStopId = parent.id,
                parentStopName = parent.displayTitle?.takeIf(String::isNotBlank) ?: parent.locationName,
                countryIso2 = parent.countryIso2,
                dateRange = FlexibleDateRangeDraftUiState.fromDateRange(parent.dateRange),
            )
        }
    }

    fun onSideTripLabelChanged(label: String) {
        stopDraft.update { it.copy(sideTripLabel = label, validationError = null) }
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
        val parentName = stop.parentStopId
            ?.let { parentId -> uiState.value.stops.firstOrNull { it.id == parentId } }
            ?.let { it.displayTitle?.takeIf(String::isNotBlank) ?: it.locationName }
        stopDraft.update { TripStopDraftUiState.fromStop(stop, parentStopName = parentName) }
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
                    parentStopId = draft.parentStopId,
                    sideTripLabel = draft.sideTripLabel,
                    locationName = locationName,
                    countryIso2 = draft.countryIso2,
                    latitude = latitude,
                    longitude = longitude,
                    dateRange = dateRange,
                    notes = draft.notes,
                )
            } else {
                // Rebuild from the stored stop rather than a bare TripStop: the draft does
                // not carry source, itineraryGroupId, isVisible or displayTitle, and losing
                // parentStopId here would silently un-nest the place being edited.
                val existing = uiState.value.stops.firstOrNull { it.id == draft.stopId }
                updateTripStopUseCase(
                    (existing ?: TripStop(
                        id = draft.stopId,
                        tripId = trip.id,
                        locationName = locationName,
                        countryIso2 = draft.countryIso2,
                        latitude = latitude,
                        longitude = longitude,
                        dateRange = dateRange,
                        notes = draft.notes,
                        sortOrder = draft.sortOrder,
                    )).copy(
                        locationName = locationName,
                        countryIso2 = draft.countryIso2,
                        latitude = latitude,
                        longitude = longitude,
                        dateRange = dateRange,
                        notes = draft.notes,
                        sortOrder = draft.sortOrder,
                        sideTripLabel = draft.sideTripLabel.trim().ifBlank { null },
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

    fun onRotatePhoto(photo: StopPhoto) {
        viewModelScope.launch {
            rotateStopPhotoUseCase(photo)
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

    class Factory(
        private val tripRepository: TripRepository,
        private val countryRepository: CountryRepository,
        private val deleteTripUseCase: DeleteTripUseCase,
        private val updateTripUseCase: UpdateTripUseCase,
        private val itineraryRepository: ItineraryRepository,
        private val airportRepository: AirportRepository,
        private val updateItineraryUseCase: UpdateItineraryUseCase,
        private val syncGeneratedTripStopsForItineraryUseCase: SyncGeneratedTripStopsForItineraryUseCase,
        private val removeGeneratedTripStopsForItineraryUseCase: RemoveGeneratedTripStopsForItineraryUseCase,
        private val createTripStopUseCase: CreateTripStopUseCase,
        private val updateTripStopUseCase: UpdateTripStopUseCase,
        private val reorderTripStopsUseCase: ReorderTripStopsUseCase,
        private val deleteTripStopUseCase: DeleteTripStopUseCase,
        private val searchLocationsUseCase: SearchLocationsUseCase,
        private val flexibleDateValidator: FlexibleDateValidator,
        private val tripMapPreferencesRepository: TripMapPreferencesRepository,
        private val stopPhotoRepository: StopPhotoRepository,
        private val addStopPhotosUseCase: AddStopPhotosUseCase,
        private val deleteStopPhotoUseCase: DeleteStopPhotoUseCase,
        private val rotateStopPhotoUseCase: RotateStopPhotoUseCase,
        private val setTripCoverPhotoUseCase: SetTripCoverPhotoUseCase,
        private val tripId: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TripDetailViewModel(
                tripRepository = tripRepository,
                countryRepository = countryRepository,
                airportRepository = airportRepository,
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
                flexibleDateValidator = flexibleDateValidator,
                tripMapPreferencesRepository = tripMapPreferencesRepository,
                stopPhotoRepository = stopPhotoRepository,
                addStopPhotosUseCase = addStopPhotosUseCase,
                deleteStopPhotoUseCase = deleteStopPhotoUseCase,
                rotateStopPhotoUseCase = rotateStopPhotoUseCase,
                setTripCoverPhotoUseCase = setTripCoverPhotoUseCase,
                tripId = tripId,
            ) as T
        }
    }

    private companion object {
    }
}

data class TripDetailUiState(
    val trip: Trip? = null,
    val stops: List<TripStop> = emptyList(),
    val countries: List<Country> = emptyList(),
    val linkedItinerary: Itinerary? = null,
    val availableItineraries: List<Itinerary> = emptyList(),
    val itineraryTitles: Map<String, String> = emptyMap(),
    val stopDraft: TripStopDraftUiState = TripStopDraftUiState(),
    val tripDraft: TripEditorDraftUiState = TripEditorDraftUiState(),
    val isItineraryPickerOpen: Boolean = false,
    val generatedStopsVisibleOnMap: Boolean = true,
    val tripStopPhotoMap: Map<String, List<StopPhoto>> = emptyMap(),
    val photoGallery: TripPhotoGalleryUiState = TripPhotoGalleryUiState(),
    val timeline: List<TripTimelineEntry> = emptyList(),
)

private data class TripContentData(
    val trip: Trip?,
    val stops: List<TripStop>,
    val countries: List<Country>,
)


private data class ItineraryData(
    val itineraries: List<Itinerary>,
    val titles: Map<String, String>,
    val groups: List<com.atlas.domain.model.ItineraryGroup> = emptyList(),
    val airportsById: Map<String, com.atlas.domain.model.Airport> = emptyMap(),
)

private data class TripDraftData(
    val stopDraft: TripStopDraftUiState,
    val tripDraft: TripEditorDraftUiState,
    val isItineraryPickerOpen: Boolean,
)
