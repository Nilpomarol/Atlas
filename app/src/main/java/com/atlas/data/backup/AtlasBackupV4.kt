package com.atlas.data.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Backup format v4 — excursions collapsed into nested trip stops.
 *
 * The legacy `excursions` / `excursionStops` arrays are still declared so that v1–v3
 * archives keep parsing (the JSON reader rejects unknown keys). They are always empty
 * in a v4 export, and the importer folds any legacy content into [tripStops].
 */
@Serializable
data class AtlasBackupV4(
    val backupVersion: Int,
    val createdAt: String,
    val countryDatasetVersion: String? = null,
    val airportDatasetVersion: String? = null,
    val data: AtlasBackupDataV4,
)

@Serializable
data class AtlasBackupDataV4(
    val countryUserStates: List<BackupCountryUserStateV1> = emptyList(),
    val countryLogs: List<BackupCountryLogV1> = emptyList(),
    val trips: List<BackupTripV3> = emptyList(),
    val tripStops: List<BackupTripStopV4> = emptyList(),
    val places: List<String> = emptyList(),
    val flights: List<BackupFlightV2> = emptyList(),
    val itineraries: List<BackupItineraryV2> = emptyList(),
    val itineraryGroups: List<BackupItineraryGroupV2> = emptyList(),
    /** Legacy, read-only. Folded into [tripStops] on import; empty on export. */
    val excursions: List<BackupExcursionV2> = emptyList(),
    /** Legacy, read-only. Folded into [tripStops] on import; empty on export. */
    val excursionStops: List<BackupExcursionStopV2> = emptyList(),
    val stopPhotos: List<StopPhotoBackup> = emptyList(),
)

@Serializable
data class BackupTripStopV4(
    val id: String,
    val tripId: String,
    val parentStopId: String? = null,
    val sideTripLabel: String? = null,
    val locationName: String,
    // v1/v2/v3 archives spell this key in snake_case; keep reading it.
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
    val source: String = "MANUAL",
    val itineraryGroupId: String? = null,
    val isVisible: Boolean = true,
    val displayTitle: String? = null,
    val createdAt: String,
    val updatedAt: String,
)
