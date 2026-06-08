package com.atlas.domain.repository

import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.model.GeneratedTripStopDraft
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    fun observeTrips(): Flow<List<Trip>>
    fun observeTrip(id: String): Flow<Trip?>
    fun observeTripStops(): Flow<List<TripStop>>
    fun observeTripStops(tripId: String): Flow<List<TripStop>>

    suspend fun createTrip(
        title: String,
        status: TravelStatus,
        dateRange: FlexibleDateRange?,
        notes: String?,
    )

    suspend fun updateTrip(trip: Trip)
    suspend fun deleteTrip(trip: Trip)
    suspend fun setCoverPhoto(tripId: String, filename: String?)
    suspend fun clearCoverPhotoByFilename(filename: String)

    suspend fun createTripStop(
        tripId: String,
        locationName: String,
        countryIso2: String,
        latitude: Double?,
        longitude: Double?,
        dateRange: FlexibleDateRange?,
        notes: String?,
    )

    suspend fun updateTripStop(stop: TripStop)
    suspend fun reorderTripStops(stops: List<TripStop>)
    suspend fun deleteTripStop(stop: TripStop)
    suspend fun replaceGeneratedItineraryGroupStops(
        tripId: String,
        groupIds: List<String>,
        stops: List<GeneratedTripStopDraft>,
    )
    suspend fun deleteGeneratedItineraryGroupStops(groupIds: List<String>)
}
