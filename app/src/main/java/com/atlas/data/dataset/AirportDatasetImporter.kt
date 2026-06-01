package com.atlas.data.dataset

import android.content.Context
import androidx.room.withTransaction
import com.atlas.core.constants.DatasetConstants
import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.local.entity.AirportEntity
import com.atlas.data.local.entity.DatasetMetadataEntity
import java.time.Instant
import kotlinx.serialization.json.Json

class AirportDatasetImporter(
    private val context: Context,
    private val database: AtlasDatabase,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun importIfNeeded() {
        val existingMetadata = database.datasetMetadataDao()
            .getByKey(DatasetConstants.AIRPORTS_KEY)

        if (existingMetadata?.version == DatasetConstants.AIRPORTS_VERSION) {
            return
        }

        val dataset = loadDataset()

        database.withTransaction {
            database.airportDao().upsertAll(dataset.airports.map { it.toEntity() })
            database.datasetMetadataDao().upsert(
                DatasetMetadataEntity(
                    key = DatasetConstants.AIRPORTS_KEY,
                    version = dataset.version,
                    importedAt = Instant.now().toString(),
                ),
            )
        }
    }

    private fun loadDataset(): AirportDatasetDto {
        val rawJson = context.assets
            .open(AIRPORTS_ASSET_PATH)
            .bufferedReader()
            .use { it.readText() }

        return json.decodeFromString(AirportDatasetDto.serializer(), rawJson)
    }

    private fun AirportDto.toEntity(): AirportEntity = AirportEntity(
        id = id,
        iata = iata,
        icao = icao,
        name = name,
        city = city,
        countryIso2 = countryIso2,
        latitude = latitude,
        longitude = longitude,
        timezone = timezone,
    )

    private companion object {
        const val AIRPORTS_ASSET_PATH = "data/airports.json"
    }
}
