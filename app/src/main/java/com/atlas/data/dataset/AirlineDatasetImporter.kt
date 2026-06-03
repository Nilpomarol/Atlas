package com.atlas.data.dataset

import android.content.Context
import androidx.room.withTransaction
import com.atlas.core.constants.DatasetConstants
import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.local.entity.AirlineEntity
import com.atlas.data.local.entity.DatasetMetadataEntity
import java.time.Instant
import kotlinx.serialization.json.Json

class AirlineDatasetImporter(
    private val context: Context,
    private val database: AtlasDatabase,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun importIfNeeded() {
        val existingMetadata = database.datasetMetadataDao()
            .getByKey(DatasetConstants.AIRLINES_KEY)

        if (existingMetadata?.version == DatasetConstants.AIRLINES_VERSION) {
            return
        }

        val dataset = loadDataset()

        database.withTransaction {
            database.airlineDao().upsertAll(dataset.airlines.map { it.toEntity() })
            database.datasetMetadataDao().upsert(
                DatasetMetadataEntity(
                    key = DatasetConstants.AIRLINES_KEY,
                    version = dataset.version,
                    importedAt = Instant.now().toString(),
                ),
            )
        }
    }

    private fun loadDataset(): AirlineDatasetDto {
        val rawJson = context.assets
            .open(AIRLINES_ASSET_PATH)
            .bufferedReader()
            .use { it.readText() }

        return json.decodeFromString(AirlineDatasetDto.serializer(), rawJson)
    }

    private fun AirlineDto.toEntity(): AirlineEntity = AirlineEntity(
        iata = iata,
        icao = icao,
        name = name,
        countryIso2 = countryIso2,
    )

    private companion object {
        const val AIRLINES_ASSET_PATH = "data/airlines.json"
    }
}
