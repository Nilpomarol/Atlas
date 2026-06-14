package com.atlas.data.repository

import com.atlas.data.local.dao.CurrencyRateDao
import com.atlas.data.local.entity.CurrencyRateEntity
import com.atlas.data.local.mapper.toDomain
import com.atlas.domain.model.CurrencyRate
import com.atlas.domain.repository.CurrencyRateApiClient
import com.atlas.domain.repository.CurrencyRateApiResult
import com.atlas.domain.repository.CurrencyRateRepository
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CurrencyRateRepositoryImpl(
    private val dao: CurrencyRateDao,
    private val apiClient: CurrencyRateApiClient,
) : CurrencyRateRepository {

    override fun observeRate(code: String): Flow<CurrencyRate?> =
        dao.observe(code.uppercase()).map { it?.toDomain() }

    override suspend fun refreshIfStale(code: String) = withContext(Dispatchers.IO) {
        val normalized = code.trim().uppercase()
        if (normalized.isBlank() || normalized == "EUR") return@withContext

        val existing = dao.getByCode(normalized)
        if (existing != null && isFresh(existing.fetchedAt)) return@withContext

        when (val result = apiClient.fetchRate(normalized)) {
            is CurrencyRateApiResult.Success -> {
                dao.upsert(
                    CurrencyRateEntity(
                        currencyCode = normalized,
                        eurRate = result.rate,
                        fetchedAt = Instant.now().toString(),
                    ),
                )
            }
            // NotFound / Error → keep any existing cached rate.
            else -> Unit
        }
    }

    private fun isFresh(fetchedAt: String): Boolean = try {
        Duration.between(Instant.parse(fetchedAt), Instant.now()) < MAX_AGE
    } catch (e: Exception) {
        false
    }

    private companion object {
        val MAX_AGE: Duration = Duration.ofHours(12)
    }
}
