package com.atlas.data.repository

import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.local.entity.ItineraryEntity
import com.atlas.data.local.entity.ItineraryGroupEntity
import com.atlas.data.local.mapper.toDomain
import com.atlas.domain.model.Itinerary
import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.repository.ItineraryRepository
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ItineraryRepositoryImpl(
    database: AtlasDatabase,
) : ItineraryRepository {
    private val itineraryDao = database.itineraryDao()
    private val flightDao = database.flightDao()
    private val tripStopDao = database.tripStopDao()

    override fun observeItineraries(): Flow<List<Itinerary>> =
        itineraryDao.observeAll().map { it.map { entity -> entity.toDomain() } }

    override fun observeItinerary(id: String): Flow<Itinerary?> =
        itineraryDao.observeById(id).map { it?.toDomain() }

    override fun observeAllGroups(): Flow<List<ItineraryGroup>> =
        itineraryDao.observeAllGroupsWithFlights().map { it.map { relation -> relation.toDomain() } }

    override fun observeGroups(itineraryId: String): Flow<List<ItineraryGroup>> =
        itineraryDao.observeGroupsWithFlights(itineraryId).map { it.map { relation -> relation.toDomain() } }

    override suspend fun getItinerary(id: String): Itinerary? =
        itineraryDao.getById(id)?.toDomain()

    override suspend fun getGroups(itineraryId: String): List<ItineraryGroup> =
        itineraryDao.getGroupsWithFlights(itineraryId).map { it.toDomain() }

    override suspend fun createItinerary(title: String, notes: String?): String {
        val id = UUID.randomUUID().toString()
        val now = Instant.now().toString()
        itineraryDao.upsertItinerary(
            ItineraryEntity(
                id = id,
                title = title,
                tripId = null,
                notes = notes,
                createdAt = now,
                updatedAt = now,
            ),
        )
        return id
    }

    override suspend fun updateItinerary(itinerary: Itinerary) {
        val now = Instant.now().toString()
        itineraryDao.upsertItinerary(
            ItineraryEntity(
                id = itinerary.id,
                title = itinerary.title,
                tripId = itinerary.tripId,
                notes = itinerary.notes,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun deleteItinerary(itinerary: Itinerary) {
        val groupIds = itineraryDao.getGroupIds(itinerary.id)
        if (groupIds.isNotEmpty()) {
            tripStopDao.deleteGeneratedForGroups(groupIds)
        }
        flightDao.clearGroupsForItinerary(itinerary.id)
        itineraryDao.deleteItinerary(
            ItineraryEntity(
                id = itinerary.id,
                title = itinerary.title,
                tripId = itinerary.tripId,
                notes = itinerary.notes,
                createdAt = "",
                updatedAt = "",
            ),
        )
    }

    override suspend fun createGroup(
        itineraryId: String,
        title: String?,
        status: TravelStatus?,
        sortOrder: Int,
    ): String {
        val id = UUID.randomUUID().toString()
        val now = Instant.now().toString()
        itineraryDao.upsertGroup(
            ItineraryGroupEntity(
                id = id,
                itineraryId = itineraryId,
                title = title,
                status = status?.name,
                sortOrder = sortOrder,
                createdAt = now,
                updatedAt = now,
            ),
        )
        return id
    }

    override suspend fun updateGroup(group: ItineraryGroup) {
        val now = Instant.now().toString()
        itineraryDao.upsertGroup(
            ItineraryGroupEntity(
                id = group.id,
                itineraryId = group.itineraryId,
                title = group.title,
                status = group.status?.name,
                sortOrder = group.sortOrder,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun deleteGroup(group: ItineraryGroup) {
        tripStopDao.deleteGeneratedForGroups(listOf(group.id))
        flightDao.clearGroup(group.id)
        itineraryDao.deleteGroup(
            ItineraryGroupEntity(
                id = group.id,
                itineraryId = group.itineraryId,
                title = group.title,
                status = group.status?.name,
                sortOrder = group.sortOrder,
                createdAt = "",
                updatedAt = "",
            ),
        )
    }

    override suspend fun reorderGroups(groups: List<ItineraryGroup>) {
        val now = Instant.now().toString()
        groups.forEachIndexed { index, group ->
            itineraryDao.updateGroupSortOrder(id = group.id, sortOrder = index, updatedAt = now)
        }
    }
}
