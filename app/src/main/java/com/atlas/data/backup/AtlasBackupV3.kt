package com.atlas.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class AtlasBackupV3(
    val backupVersion: Int,
    val createdAt: String,
    val countryDatasetVersion: String? = null,
    val airportDatasetVersion: String? = null,
    val data: AtlasBackupDataV3,
)

@Serializable
data class AtlasBackupDataV3(
    val countryUserStates: List<BackupCountryUserStateV1> = emptyList(),
    val countryLogs: List<BackupCountryLogV1> = emptyList(),
    val trips: List<BackupTripV3> = emptyList(),
    val tripStops: List<BackupTripStopV2> = emptyList(),
    val places: List<String> = emptyList(),
    val flights: List<BackupFlightV2> = emptyList(),
    val itineraries: List<BackupItineraryV2> = emptyList(),
    val itineraryGroups: List<BackupItineraryGroupV2> = emptyList(),
    val excursions: List<BackupExcursionV2> = emptyList(),
    val excursionStops: List<BackupExcursionStopV2> = emptyList(),
    val stopPhotos: List<StopPhotoBackup> = emptyList(),
)

@Serializable
data class BackupTripV3(
    val id: String,
    val title: String,
    val status: String,
    val startYear: Int? = null,
    val startMonth: Int? = null,
    val startDay: Int? = null,
    val endYear: Int? = null,
    val endMonth: Int? = null,
    val endDay: Int? = null,
    val datePrecision: String? = null,
    val notes: String? = null,
    val coverPhotoFilename: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class StopPhotoBackup(
    val id: String,
    val stopId: String,
    val stopType: String,
    val filename: String,
    val sortOrder: Int,
    val createdAt: String,
)
