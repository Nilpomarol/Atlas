package com.atlas.data.backup

import com.atlas.data.local.entity.CountryLogEntity
import com.atlas.data.local.entity.CountryUserStateEntity
import com.atlas.data.local.entity.TripEntity
import com.atlas.data.local.entity.TripStopEntity

fun CountryUserStateEntity.toBackupV1(): BackupCountryUserStateV1 =
    BackupCountryUserStateV1(
        countryIso2 = countryIso2,
        wished = wished,
        currentlyLiving = currentlyLiving,
        updatedAt = updatedAt,
    )

fun BackupCountryUserStateV1.toEntity(): CountryUserStateEntity =
    CountryUserStateEntity(
        countryIso2 = countryIso2,
        wished = wished,
        currentlyLiving = currentlyLiving,
        updatedAt = updatedAt,
    )

fun CountryLogEntity.toBackupV1(): BackupCountryLogV1 =
    BackupCountryLogV1(
        id = id,
        countryIso2 = countryIso2,
        type = type,
        startYear = startYear,
        startMonth = startMonth,
        startDay = startDay,
        endYear = endYear,
        endMonth = endMonth,
        endDay = endDay,
        datePrecision = datePrecision,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun BackupCountryLogV1.toEntity(): CountryLogEntity =
    CountryLogEntity(
        id = id,
        countryIso2 = countryIso2,
        type = type,
        startYear = startYear,
        startMonth = startMonth,
        startDay = startDay,
        endYear = endYear,
        endMonth = endMonth,
        endDay = endDay,
        datePrecision = datePrecision,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun TripEntity.toBackupV1(): BackupTripV1 =
    BackupTripV1(
        id = id,
        title = title,
        status = status,
        startYear = startYear,
        startMonth = startMonth,
        startDay = startDay,
        endYear = endYear,
        endMonth = endMonth,
        endDay = endDay,
        datePrecision = datePrecision,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun BackupTripV1.toEntity(): TripEntity =
    TripEntity(
        id = id,
        title = title,
        status = status,
        startYear = startYear,
        startMonth = startMonth,
        startDay = startDay,
        endYear = endYear,
        endMonth = endMonth,
        endDay = endDay,
        datePrecision = datePrecision,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun TripStopEntity.toBackupV1(): BackupTripStopV1 =
    BackupTripStopV1(
        id = id,
        tripId = tripId,
        locationName = locationName,
        countryIso2 = countryIso2,
        latitude = latitude,
        longitude = longitude,
        startYear = startYear,
        startMonth = startMonth,
        startDay = startDay,
        endYear = endYear,
        endMonth = endMonth,
        endDay = endDay,
        datePrecision = datePrecision,
        notes = notes,
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun BackupTripStopV1.toEntity(): TripStopEntity =
    TripStopEntity(
        id = id,
        tripId = tripId,
        locationName = locationName,
        countryIso2 = countryIso2,
        latitude = latitude,
        longitude = longitude,
        startYear = startYear,
        startMonth = startMonth,
        startDay = startDay,
        endYear = endYear,
        endMonth = endMonth,
        endDay = endDay,
        datePrecision = datePrecision,
        notes = notes,
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
