package com.atlas.data.backup

import com.atlas.data.local.entity.CountryLogEntity
import com.atlas.data.local.entity.CountryUserStateEntity
import com.atlas.data.local.entity.ExcursionEntity
import com.atlas.data.local.entity.ExcursionStopEntity
import com.atlas.data.local.entity.FlightEntity
import com.atlas.data.local.entity.ItineraryEntity
import com.atlas.data.local.entity.ItineraryGroupEntity
import com.atlas.data.local.entity.StopPhotoEntity
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

fun TripEntity.toBackupV3(): BackupTripV3 =
    BackupTripV3(
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
        coverPhotoFilename = coverPhotoFilename,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun BackupTripV3.toEntity(): TripEntity =
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
        coverPhotoFilename = coverPhotoFilename,
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

// ── v2 mappers ────────────────────────────────────────────────────────────────

fun TripStopEntity.toBackupV2(): BackupTripStopV2 =
    BackupTripStopV2(
        id = id, tripId = tripId, locationName = locationName, countryIso2 = countryIso2,
        latitude = latitude, longitude = longitude,
        startYear = startYear, startMonth = startMonth, startDay = startDay,
        endYear = endYear, endMonth = endMonth, endDay = endDay,
        datePrecision = datePrecision, notes = notes, sortOrder = sortOrder,
        source = source, itineraryGroupId = itineraryGroupId,
        isVisible = isVisible, displayTitle = displayTitle,
        createdAt = createdAt, updatedAt = updatedAt,
    )

fun BackupTripStopV2.toEntity(): TripStopEntity =
    TripStopEntity(
        id = id, tripId = tripId, locationName = locationName, countryIso2 = countryIso2,
        latitude = latitude, longitude = longitude,
        startYear = startYear, startMonth = startMonth, startDay = startDay,
        endYear = endYear, endMonth = endMonth, endDay = endDay,
        datePrecision = datePrecision, notes = notes, sortOrder = sortOrder,
        source = source, itineraryGroupId = itineraryGroupId,
        isVisible = isVisible, displayTitle = displayTitle,
        createdAt = createdAt, updatedAt = updatedAt,
    )

fun FlightEntity.toBackupV2(): BackupFlightV2 =
    BackupFlightV2(
        id = id, originAirportId = originAirportId, destinationAirportId = destinationAirportId,
        status = status, scheduledDepartureAt = scheduledDepartureAt,
        scheduledArrivalAt = scheduledArrivalAt, actualDepartureAt = actualDepartureAt,
        actualArrivalAt = actualArrivalAt, airline = airline, flightNumber = flightNumber,
        scheduledDepartureUtc = scheduledDepartureUtc, scheduledArrivalUtc = scheduledArrivalUtc,
        actualDepartureUtc = actualDepartureUtc, actualArrivalUtc = actualArrivalUtc,
        distanceKm = distanceKm,
        aircraft = aircraft, aircraftRegistration = aircraftRegistration,
        notes = notes, itineraryGroupId = itineraryGroupId,
        sortOrder = sortOrder, createdAt = createdAt, updatedAt = updatedAt,
    )

fun BackupFlightV2.toEntity(): FlightEntity =
    FlightEntity(
        id = id, originAirportId = originAirportId, destinationAirportId = destinationAirportId,
        status = status, scheduledDepartureAt = scheduledDepartureAt,
        scheduledArrivalAt = scheduledArrivalAt, actualDepartureAt = actualDepartureAt,
        actualArrivalAt = actualArrivalAt, airline = airline, flightNumber = flightNumber,
        scheduledDepartureUtc = scheduledDepartureUtc, scheduledArrivalUtc = scheduledArrivalUtc,
        actualDepartureUtc = actualDepartureUtc, actualArrivalUtc = actualArrivalUtc,
        distanceKm = distanceKm,
        aircraft = aircraft, aircraftRegistration = aircraftRegistration,
        notes = notes, itineraryGroupId = itineraryGroupId,
        sortOrder = sortOrder, createdAt = createdAt, updatedAt = updatedAt,
    )

fun ItineraryEntity.toBackupV2(): BackupItineraryV2 =
    BackupItineraryV2(
        id = id, title = title, tripId = tripId, notes = notes,
        createdAt = createdAt, updatedAt = updatedAt,
    )

fun BackupItineraryV2.toEntity(): ItineraryEntity =
    ItineraryEntity(
        id = id, title = title.orEmpty(), tripId = tripId, notes = notes,
        createdAt = createdAt, updatedAt = updatedAt,
    )

fun ItineraryGroupEntity.toBackupV2(): BackupItineraryGroupV2 =
    BackupItineraryGroupV2(
        id = id, itineraryId = itineraryId, title = title, status = status,
        sortOrder = sortOrder, createdAt = createdAt, updatedAt = updatedAt,
    )

fun BackupItineraryGroupV2.toEntity(): ItineraryGroupEntity =
    ItineraryGroupEntity(
        id = id, itineraryId = itineraryId, title = title, status = status,
        sortOrder = sortOrder, createdAt = createdAt, updatedAt = updatedAt,
    )

fun ExcursionEntity.toBackupV2(): BackupExcursionV2 =
    BackupExcursionV2(
        id = id, tripId = tripId, anchorTripStopId = anchorTripStopId,
        title = title, notes = notes, sortOrder = sortOrder,
        createdAt = createdAt, updatedAt = updatedAt,
    )

fun BackupExcursionV2.toEntity(): ExcursionEntity =
    ExcursionEntity(
        id = id, tripId = tripId, anchorTripStopId = anchorTripStopId,
        title = title, notes = notes, sortOrder = sortOrder,
        createdAt = createdAt, updatedAt = updatedAt,
    )

fun ExcursionStopEntity.toBackupV2(): BackupExcursionStopV2 =
    BackupExcursionStopV2(
        id = id, excursionId = excursionId, locationName = locationName, countryIso2 = countryIso2,
        latitude = latitude, longitude = longitude,
        startYear = startYear, startMonth = startMonth, startDay = startDay,
        endYear = endYear, endMonth = endMonth, endDay = endDay,
        datePrecision = datePrecision, notes = notes, sortOrder = sortOrder,
        createdAt = createdAt, updatedAt = updatedAt,
    )

fun BackupExcursionStopV2.toEntity(): ExcursionStopEntity =
    ExcursionStopEntity(
        id = id, excursionId = excursionId, locationName = locationName, countryIso2 = countryIso2,
        latitude = latitude, longitude = longitude,
        startYear = startYear, startMonth = startMonth, startDay = startDay,
        endYear = endYear, endMonth = endMonth, endDay = endDay,
        datePrecision = datePrecision, notes = notes, sortOrder = sortOrder,
        createdAt = createdAt, updatedAt = updatedAt,
    )

fun StopPhotoEntity.toBackupV3(): StopPhotoBackup =
    StopPhotoBackup(
        id = id,
        stopId = stopId,
        stopType = stopType,
        filename = filename,
        sortOrder = sortOrder,
        createdAt = createdAt,
    )

fun StopPhotoBackup.toEntity(): StopPhotoEntity =
    StopPhotoEntity(
        id = id,
        stopId = stopId,
        stopType = stopType,
        filename = filename,
        sortOrder = sortOrder,
        createdAt = createdAt,
    )
