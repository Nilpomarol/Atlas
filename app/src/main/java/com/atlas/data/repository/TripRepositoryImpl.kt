package com.atlas.data.repository

import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.local.entity.TripEntity
import com.atlas.data.local.entity.TripStopEntity
import com.atlas.data.local.mapper.toDomain
import com.atlas.data.local.mapper.toEntity
import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.model.GeneratedTripStopDraft
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import com.atlas.domain.model.TripStopSource
import com.atlas.domain.repository.TripRepository
import androidx.room.withTransaction
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TripRepositoryImpl(
    private val database: AtlasDatabase,
) : TripRepository {
    private val tripDao = database.tripDao()
    private val tripStopDao = database.tripStopDao()
    private val itineraryDao = database.itineraryDao()

    override fun observeTrips(): Flow<List<Trip>> =
        tripDao.observeTrips().map { trips ->
            trips.map { it.toDomain() }
        }

    override fun observeTrip(id: String): Flow<Trip?> =
        tripDao.observeTrip(id).map { it?.toDomain() }

    override fun observeTripStops(): Flow<List<TripStop>> =
        tripStopDao.observeAll().map { stops ->
            stops.map { it.toDomain() }
        }

    override fun observeTripStops(tripId: String): Flow<List<TripStop>> =
        tripStopDao.observeByTripId(tripId).map { stops ->
            stops.map { it.toDomain() }
        }

    override suspend fun getTripsForStatusRefresh(): List<Trip> =
        tripDao.getForStatusRefresh().map { it.toDomain() }

    override suspend fun createTrip(
        title: String,
        status: TravelStatus,
        dateRange: FlexibleDateRange?,
        notes: String?,
    ) {
        val now = Instant.now().toString()
        tripDao.upsert(
            TripEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                status = status.name,
                startYear = dateRange?.start?.year,
                startMonth = dateRange?.start?.month,
                startDay = dateRange?.start?.day,
                endYear = dateRange?.end?.year,
                endMonth = dateRange?.end?.month,
                endDay = dateRange?.end?.day,
                datePrecision = dateRange?.precision?.name,
                notes = notes,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun createTripWithInitialStop(
        title: String,
        status: TravelStatus,
        dateRange: FlexibleDateRange?,
        notes: String?,
        locationName: String,
        countryIso2: String,
        latitude: Double?,
        longitude: Double?,
        stopNotes: String?,
    ): String {
        val now = Instant.now().toString()
        val tripId = UUID.randomUUID().toString()
        database.withTransaction {
            tripDao.upsert(
                TripEntity(
                    id = tripId,
                    title = title,
                    status = status.name,
                    startYear = dateRange?.start?.year,
                    startMonth = dateRange?.start?.month,
                    startDay = dateRange?.start?.day,
                    endYear = dateRange?.end?.year,
                    endMonth = dateRange?.end?.month,
                    endDay = dateRange?.end?.day,
                    datePrecision = dateRange?.precision?.name,
                    notes = notes,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            tripStopDao.upsert(
                TripStopEntity(
                    id = UUID.randomUUID().toString(),
                    tripId = tripId,
                    locationName = locationName,
                    countryIso2 = countryIso2,
                    latitude = latitude,
                    longitude = longitude,
                    startYear = dateRange?.start?.year,
                    startMonth = dateRange?.start?.month,
                    startDay = dateRange?.start?.day,
                    endYear = dateRange?.end?.year,
                    endMonth = dateRange?.end?.month,
                    endDay = dateRange?.end?.day,
                    datePrecision = dateRange?.precision?.name,
                    notes = stopNotes,
                    sortOrder = 0,
                    source = TripStopSource.MANUAL.name,
                    itineraryGroupId = null,
                    isVisible = true,
                    displayTitle = null,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }
        return tripId
    }

    override suspend fun updateTrip(trip: Trip) {
        val now = Instant.now().toString()
        tripDao.upsert(
            TripEntity(
                id = trip.id,
                title = trip.title,
                status = trip.status.name,
                startYear = trip.dateRange?.start?.year,
                startMonth = trip.dateRange?.start?.month,
                startDay = trip.dateRange?.start?.day,
                endYear = trip.dateRange?.end?.year,
                endMonth = trip.dateRange?.end?.month,
                endDay = trip.dateRange?.end?.day,
                datePrecision = trip.dateRange?.precision?.name,
                notes = trip.notes,
                createdAt = now,
                updatedAt = now,
                coverPhotoFilename = trip.coverPhotoFilename,
            ),
        )
    }

    override suspend fun updateTripStatus(id: String, status: TravelStatus, updatedAt: String) {
        tripDao.updateStatus(id = id, status = status.name, updatedAt = updatedAt)
    }

    override suspend fun setCoverPhoto(tripId: String, filename: String?) {
        tripDao.setCoverPhoto(tripId, filename)
    }

    override suspend fun clearCoverPhotoByFilename(filename: String) {
        tripDao.clearCoverPhotoByFilename(filename)
    }

    override suspend fun deleteTrip(trip: Trip) = database.withTransaction {
        // A linked itinerary outlives its trip: `itineraries.trip_id` has no foreign key,
        // so it must be detached here. Leaving it dangling strands the itinerary and makes
        // the next backup unimportable.
        itineraryDao.clearTripLink(tripId = trip.id, updatedAt = Instant.now().toString())
        tripDao.delete(
            TripEntity(
                id = trip.id,
                title = trip.title,
                status = trip.status.name,
                startYear = trip.dateRange?.start?.year,
                startMonth = trip.dateRange?.start?.month,
                startDay = trip.dateRange?.start?.day,
                endYear = trip.dateRange?.end?.year,
                endMonth = trip.dateRange?.end?.month,
                endDay = trip.dateRange?.end?.day,
                datePrecision = trip.dateRange?.precision?.name,
                notes = trip.notes,
                createdAt = "",
                updatedAt = "",
                coverPhotoFilename = trip.coverPhotoFilename,
            ),
        )
    }

    override suspend fun createTripStop(
        tripId: String,
        locationName: String,
        countryIso2: String,
        latitude: Double?,
        longitude: Double?,
        dateRange: FlexibleDateRange?,
        notes: String?,
    ) {
        val now = Instant.now().toString()
        tripStopDao.upsert(
            TripStopEntity(
                id = UUID.randomUUID().toString(),
                tripId = tripId,
                locationName = locationName,
                countryIso2 = countryIso2,
                latitude = latitude,
                longitude = longitude,
                startYear = dateRange?.start?.year,
                startMonth = dateRange?.start?.month,
                startDay = dateRange?.start?.day,
                endYear = dateRange?.end?.year,
                endMonth = dateRange?.end?.month,
                endDay = dateRange?.end?.day,
                datePrecision = dateRange?.precision?.name,
                notes = notes,
                sortOrder = tripStopDao.getMaxSortOrder(tripId) + 1,
                source = TripStopSource.MANUAL.name,
                itineraryGroupId = null,
                isVisible = true,
                displayTitle = null,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun updateTripStop(stop: TripStop) {
        tripStopDao.updateFields(
            id = stop.id,
            locationName = stop.locationName,
            countryIso2 = stop.countryIso2,
            latitude = stop.latitude,
            longitude = stop.longitude,
            startYear = stop.dateRange?.start?.year,
            startMonth = stop.dateRange?.start?.month,
            startDay = stop.dateRange?.start?.day,
            endYear = stop.dateRange?.end?.year,
            endMonth = stop.dateRange?.end?.month,
            endDay = stop.dateRange?.end?.day,
            datePrecision = stop.dateRange?.precision?.name,
            notes = stop.notes,
            sortOrder = stop.sortOrder,
            updatedAt = Instant.now().toString(),
        )
    }

    override suspend fun reorderTripStops(stops: List<TripStop>) {
        database.withTransaction {
            val now = Instant.now().toString()
            stops.forEachIndexed { index, stop ->
                tripStopDao.updateSortOrder(
                    id = stop.id,
                    sortOrder = index,
                    updatedAt = now,
                )
            }
        }
    }

    override suspend fun deleteTripStop(stop: TripStop) {
        // `parent_stop_id` carries no foreign key, so the cascade is enforced here.
        database.withTransaction {
            tripStopDao.deleteChildren(stop.id)
            tripStopDao.delete(stop.toEntity(createdAt = "", updatedAt = ""))
        }
    }

    override suspend fun childStopIds(parentStopId: String): List<String> =
        tripStopDao.getChildStopIds(parentStopId)

    override suspend fun replaceGeneratedItineraryGroupStops(
        tripId: String,
        groupIds: List<String>,
        stops: List<GeneratedTripStopDraft>,
    ) {
        if (groupIds.isEmpty()) return

        database.withTransaction {
            val now = Instant.now().toString()
            val existingSortOrders = tripStopDao.getGeneratedForGroups(groupIds)
                .mapNotNull { existing ->
                    existing.itineraryGroupId?.let { groupId -> groupId to existing.sortOrder }
                }
                .toMap()
            tripStopDao.deleteGeneratedForGroups(groupIds)
            tripStopDao.upsertAll(
                stops.map { stop ->
                    TripStop(
                        id = "itinerary-group-${stop.itineraryGroupId}",
                        tripId = tripId,
                        locationName = stop.locationName,
                        countryIso2 = stop.countryIso2,
                        latitude = stop.latitude,
                        longitude = stop.longitude,
                        dateRange = null,
                        notes = null,
                        sortOrder = existingSortOrders[stop.itineraryGroupId] ?: stop.sortOrder,
                        source = TripStopSource.ITINERARY_GROUP,
                        itineraryGroupId = stop.itineraryGroupId,
                        isVisible = true,
                        displayTitle = stop.displayTitle,
                    ).toEntity(createdAt = now, updatedAt = now)
                },
            )
        }
    }

    override suspend fun deleteGeneratedItineraryGroupStops(groupIds: List<String>) {
        if (groupIds.isEmpty()) return
        tripStopDao.deleteGeneratedForGroups(groupIds)
    }
}
