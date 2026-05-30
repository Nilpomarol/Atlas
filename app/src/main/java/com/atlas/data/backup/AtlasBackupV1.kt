package com.atlas.data.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AtlasBackupV1(
    val backupVersion: Int,
    val createdAt: String,
    val countryDatasetVersion: String?,
    val data: AtlasBackupDataV1,
)

@Serializable
data class AtlasBackupDataV1(
    val countryUserStates: List<BackupCountryUserStateV1> = emptyList(),
    val countryLogs: List<BackupCountryLogV1> = emptyList(),
    val trips: List<BackupTripV1> = emptyList(),
    val tripStops: List<BackupTripStopV1> = emptyList(),
    val places: List<String> = emptyList(),
)

@Serializable
data class BackupCountryUserStateV1(
    @SerialName("country_iso2")
    val countryIso2: String,
    val wished: Boolean,
    val currentlyLiving: Boolean,
    val updatedAt: String,
)

@Serializable
data class BackupCountryLogV1(
    val id: String,
    @SerialName("country_iso2")
    val countryIso2: String,
    val type: String,
    val startYear: Int? = null,
    val startMonth: Int? = null,
    val startDay: Int? = null,
    val endYear: Int? = null,
    val endMonth: Int? = null,
    val endDay: Int? = null,
    val datePrecision: String? = null,
    val notes: String? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class BackupTripV1(
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
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class BackupTripStopV1(
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
    val createdAt: String,
    val updatedAt: String,
)
