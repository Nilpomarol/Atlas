package com.atlas.data.repository

import androidx.room.withTransaction
import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.local.entity.CountryLogEntity
import com.atlas.data.local.entity.CountryUserStateEntity
import com.atlas.data.local.mapper.toDomain
import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryLog
import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.CountryUserState
import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.repository.CountryRepository
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CountryRepositoryImpl(
    private val database: AtlasDatabase,
) : CountryRepository {
    private val countryDao = database.countryDao()
    private val countryLogDao = database.countryLogDao()
    private val countryUserStateDao = database.countryUserStateDao()

    override fun observeTrackableCountries(): Flow<List<Country>> =
        countryDao.observeTrackableCountries().map { countries ->
            countries.map { it.toDomain() }
        }

    override fun observeCountry(iso2: String): Flow<Country?> =
        countryDao.observeCountry(iso2).map { it?.toDomain() }

    override fun observeCountryLogs(): Flow<List<CountryLog>> =
        countryLogDao.observeAll().map { logs ->
            logs.map { it.toDomain() }
        }

    override fun observeCountryLogs(countryIso2: String): Flow<List<CountryLog>> =
        countryLogDao.observeByCountryIso2(countryIso2).map { logs ->
            logs.map { it.toDomain() }
        }

    override fun observeUserStates(): Flow<List<CountryUserState>> =
        countryUserStateDao.observeAll().map { userStates ->
            userStates.map { it.toDomain() }
        }

    override fun observeUserState(countryIso2: String): Flow<CountryUserState?> =
        countryUserStateDao.observeByCountryIso2(countryIso2).map { it?.toDomain() }

    override suspend fun addCountryLog(
        countryIso2: String,
        type: CountryLogType,
        dateRange: FlexibleDateRange?,
        notes: String?,
    ) {
        val now = Instant.now().toString()
        countryLogDao.upsert(
            CountryLogEntity(
                id = UUID.randomUUID().toString(),
                countryIso2 = countryIso2,
                type = type.name,
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

    override suspend fun updateCountryLog(log: CountryLog) {
        val now = Instant.now().toString()
        countryLogDao.upsert(
            CountryLogEntity(
                id = log.id,
                countryIso2 = log.countryIso2,
                type = log.type.name,
                startYear = log.dateRange?.start?.year,
                startMonth = log.dateRange?.start?.month,
                startDay = log.dateRange?.start?.day,
                endYear = log.dateRange?.end?.year,
                endMonth = log.dateRange?.end?.month,
                endDay = log.dateRange?.end?.day,
                datePrecision = log.dateRange?.precision?.name,
                notes = log.notes,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun deleteCountryLog(log: CountryLog) {
        countryLogDao.delete(
            CountryLogEntity(
                id = log.id,
                countryIso2 = log.countryIso2,
                type = log.type.name,
                startYear = log.dateRange?.start?.year,
                startMonth = log.dateRange?.start?.month,
                startDay = log.dateRange?.start?.day,
                endYear = log.dateRange?.end?.year,
                endMonth = log.dateRange?.end?.month,
                endDay = log.dateRange?.end?.day,
                datePrecision = log.dateRange?.precision?.name,
                notes = log.notes,
                createdAt = "",
                updatedAt = "",
            ),
        )
    }

    override suspend fun setWished(
        countryIso2: String,
        wished: Boolean,
    ) {
        val existing = countryUserStateDao.getByCountryIso2(countryIso2)
        countryUserStateDao.upsert(
            CountryUserStateEntity(
                countryIso2 = countryIso2,
                wished = wished,
                currentlyLiving = existing?.currentlyLiving ?: false,
                updatedAt = Instant.now().toString(),
            ),
        )
    }

    override suspend fun setCurrentlyLiving(countryIso2: String) {
        database.withTransaction {
            val now = Instant.now().toString()
            val existing = countryUserStateDao.getByCountryIso2(countryIso2)
            countryUserStateDao.clearCurrentlyLiving(updatedAt = now)
            countryUserStateDao.upsert(
                CountryUserStateEntity(
                    countryIso2 = countryIso2,
                    wished = existing?.wished ?: false,
                    currentlyLiving = true,
                    updatedAt = now,
                ),
            )
        }
    }
}
