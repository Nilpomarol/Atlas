package com.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.atlas.data.local.entity.CurrencyRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyRateDao {
    @Query("SELECT * FROM currency_rates WHERE currency_code = :code LIMIT 1")
    fun observe(code: String): Flow<CurrencyRateEntity?>

    @Query("SELECT * FROM currency_rates WHERE currency_code = :code LIMIT 1")
    suspend fun getByCode(code: String): CurrencyRateEntity?

    @Upsert
    suspend fun upsert(rate: CurrencyRateEntity)
}
