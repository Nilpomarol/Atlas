package com.atlas.data.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AtlasBackupV2(
    val backupVersion: Int,
    val createdAt: String,
    val countryDatasetVersion: String? = null,
    val airportDatasetVersion: String? = null,
    val data: AtlasBackupDataV2,
)

@Serializable
data class AtlasBackupDataV2(
    val countryUserStates: List<BackupCountryUserStateV1> = emptyList(),
    val countryLogs: List<BackupCountryLogV1> = emptyList(),
    val trips: List<BackupTripV1> = emptyList(),
    // TripStop DTO updated with v2 source/group fields; v1 backups get safe defaults.
    val tripStops: List<BackupTripStopV2> = emptyList(),
    val places: List<String> = emptyList(),
    // v2 additions
    val flights: List<BackupFlightV2> = emptyList(),
    val itineraries: List<BackupItineraryV2> = emptyList(),
    val itineraryGroups: List<BackupItineraryGroupV2> = emptyList(),
    val excursions: List<BackupExcursionV2> = emptyList(),
    val excursionStops: List<BackupExcursionStopV2> = emptyList(),
)

// ── TripStop v2 (extends v1 with source/group fields) ────────────────────────

@Serializable
data class BackupTripStopV2(
    val id: String,
    val tripId: String,
    val locationName: String,
    @SerialName("country_iso2")
    val countryIso2: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val startYear: Int? = null,
    val startMonth: Int? = null,
    val startDay: Int? = null,
    val endYear: Int? = null,
    val endMonth: Int? = null,
    val endDay: Int? = null,
    val datePrecision: String? = null,
    val notes: String? = null,
    val sortOrder: Int,
    // v2 fields — absent in v1 JSON, safe defaults applied automatically
    val source: String = "MANUAL",
    val itineraryGroupId: String? = null,
    val isVisible: Boolean = true,
    val displayTitle: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

// ── Flight ────────────────────────────────────────────────────────────────────

@Serializable
data class BackupFlightV2(
    val id: String,
    val originAirportId: String,
    val destinationAirportId: String,
    val status: String,
    val scheduledDepartureAt: String? = null,
    val scheduledArrivalAt: String? = null,
    val actualDepartureAt: String? = null,
    val actualArrivalAt: String? = null,
    val airline: String? = null,
    val flightNumber: String? = null,
    val aircraft: String? = null,
    val notes: String? = null,
    val itineraryGroupId: String? = null,
    val sortOrder: Int? = null,
    val createdAt: String,
    val updatedAt: String,
)

// ── Itinerary ─────────────────────────────────────────────────────────────────

@Serializable
data class BackupItineraryV2(
    val id: String,
    val title: String,
    val tripId: String? = null,
    val notes: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

// ── ItineraryGroup ────────────────────────────────────────────────────────────

@Serializable
data class BackupItineraryGroupV2(
    val id: String,
    val itineraryId: String,
    val title: String? = null,
    val status: String? = null,
    val sortOrder: Int,
    val createdAt: String,
    val updatedAt: String,
)

// ── Excursion ─────────────────────────────────────────────────────────────────

@Serializable
data class BackupExcursionV2(
    val id: String,
    val tripId: String,
    val anchorTripStopId: String? = null,
    val title: String,
    val notes: String? = null,
    val sortOrder: Int,
    val createdAt: String,
    val updatedAt: String,
)

// ── ExcursionStop ─────────────────────────────────────────────────────────────

@Serializable
data class BackupExcursionStopV2(
    val id: String,
    val excursionId: String,
    val locationName: String,
    @SerialName("country_iso2")
    val countryIso2: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val startYear: Int? = null,
    val startMonth: Int? = null,
    val startDay: Int? = null,
    val endYear: Int? = null,
    val endMonth: Int? = null,
    val endDay: Int? = null,
    val datePrecision: String? = null,
    val notes: String? = null,
    val sortOrder: Int,
    val createdAt: String,
    val updatedAt: String,
)
