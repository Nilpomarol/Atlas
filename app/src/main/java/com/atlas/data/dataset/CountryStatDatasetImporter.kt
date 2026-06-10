package com.atlas.data.dataset

import android.content.Context
import androidx.room.withTransaction
import com.atlas.core.constants.DatasetConstants
import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.local.entity.CountryStatFactEntity
import com.atlas.data.local.entity.DatasetMetadataEntity
import java.time.Instant
import kotlinx.serialization.json.Json

class CountryStatDatasetImporter(
    private val context: Context,
    private val database: AtlasDatabase,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun importIfNeeded() {
        val existingMetadata = database.datasetMetadataDao()
            .getByKey(DatasetConstants.COUNTRY_STATS_KEY)

        if (existingMetadata?.version == DatasetConstants.COUNTRY_STATS_VERSION) {
            return
        }

        val dataset = loadDataset()
        val rows = dataset.countries.flatMap { country ->
            country.facts.map { it.toEntity(country.iso2) }
        }

        database.withTransaction {
            // Full replace: the fact set changes shape between versions, so a
            // plain upsert could leave stale rows for removed keys behind.
            database.countryStatFactDao().clear()
            database.countryStatFactDao().upsertAll(rows)
            database.datasetMetadataDao().upsert(
                DatasetMetadataEntity(
                    key = DatasetConstants.COUNTRY_STATS_KEY,
                    version = dataset.version,
                    importedAt = Instant.now().toString(),
                ),
            )
        }
    }

    private fun loadDataset(): CountryStatDatasetDto {
        val rawJson = context.assets
            .open(COUNTRY_STATS_ASSET_PATH)
            .bufferedReader()
            .use { it.readText() }

        return json.decodeFromString(CountryStatDatasetDto.serializer(), rawJson)
    }

    private fun CountryStatFactDto.toEntity(iso2: String): CountryStatFactEntity = CountryStatFactEntity(
        countryIso2 = iso2,
        category = category,
        key = key,
        labelCa = labelCa,
        value = value,
        unit = unit,
        year = year,
        rank = rank,
        rankTotal = rankTotal,
        tier = tier,
        sortOrder = sortOrder,
    )

    private companion object {
        const val COUNTRY_STATS_ASSET_PATH = "data/country_stats.json"
    }
}
