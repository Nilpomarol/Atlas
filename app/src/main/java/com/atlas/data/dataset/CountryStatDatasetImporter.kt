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
        val computedRanks = computeRanks(dataset)
        val rows = dataset.countries.flatMap { country ->
            country.facts.map { it.toEntity(country.iso2, computedRanks) }
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

    private fun CountryStatFactDto.toEntity(
        iso2: String,
        computedRanks: Map<Pair<String, String>, RankInfo>,
    ): CountryStatFactEntity {
        val computed = computedRanks[iso2 to key]
        return CountryStatFactEntity(
            countryIso2 = iso2,
            category = category,
            key = key,
            labelCa = labelCa,
            value = value,
            unit = unit,
            year = year,
            rank = rank ?: computed?.rank,
            rankTotal = rankTotal ?: computed?.rankTotal,
            tier = tier,
            sortOrder = sortOrder,
        )
    }

    /**
     * Some meaningful metrics ship without a rank. Derive one across all countries so
     * they can surface as ranked stats. Listed keys are lower-is-better (rank 1 = the
     * lowest value), matching the app convention that a low rank reads as positive.
     */
    private fun computeRanks(dataset: CountryStatDatasetDto): Map<Pair<String, String>, RankInfo> {
        val result = mutableMapOf<Pair<String, String>, RankInfo>()
        for (key in LOWER_BETTER_RANK_KEYS) {
            val valued = dataset.countries.mapNotNull { country ->
                country.facts.firstOrNull { it.key == key && it.rank == null }
                    ?.let { fact -> parseValue(fact.value)?.let { country.iso2 to it } }
            }
            val total = valued.size
            valued.sortedBy { it.second }.forEachIndexed { index, (iso2, _) ->
                result[iso2 to key] = RankInfo(index + 1, total)
            }
        }
        return result
    }

    private fun parseValue(raw: String): Double? =
        raw.replace(".", "").replace(",", ".").toDoubleOrNull()

    private data class RankInfo(val rank: Int, val rankTotal: Int)

    private companion object {
        const val COUNTRY_STATS_ASSET_PATH = "data/country_stats.json"
        val LOWER_BETTER_RANK_KEYS = setOf("co2_per_capita", "co2_total")
    }
}
