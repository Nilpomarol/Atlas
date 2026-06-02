package com.atlas.domain.repository

import com.atlas.domain.model.Itinerary
import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.model.TravelStatus
import kotlinx.coroutines.flow.Flow

interface ItineraryRepository {
    fun observeItineraries(): Flow<List<Itinerary>>
    fun observeItinerary(id: String): Flow<Itinerary?>
    fun observeAllGroups(): Flow<List<ItineraryGroup>>
    fun observeGroups(itineraryId: String): Flow<List<ItineraryGroup>>
    suspend fun getItinerary(id: String): Itinerary?
    suspend fun getGroups(itineraryId: String): List<ItineraryGroup>

    suspend fun createItinerary(title: String, notes: String?): String
    suspend fun updateItinerary(itinerary: Itinerary)
    suspend fun deleteItinerary(itinerary: Itinerary)

    suspend fun createGroup(itineraryId: String, title: String?, status: TravelStatus?, sortOrder: Int): String
    suspend fun updateGroup(group: ItineraryGroup)
    suspend fun deleteGroup(group: ItineraryGroup)
    suspend fun reorderGroups(groups: List<ItineraryGroup>)
}
