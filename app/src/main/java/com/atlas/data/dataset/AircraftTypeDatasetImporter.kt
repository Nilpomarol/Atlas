package com.atlas.data.dataset

import android.content.Context
import androidx.room.withTransaction
import com.atlas.core.constants.DatasetConstants
import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.local.entity.AircraftTypeEntity
import com.atlas.data.local.entity.DatasetMetadataEntity
import com.atlas.data.util.normalizedAircraftToken
import java.time.Instant
import kotlinx.serialization.json.Json

class AircraftTypeDatasetImporter(
    private val context: Context,
    private val database: AtlasDatabase,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun importIfNeeded() {
        val existingMetadata = database.datasetMetadataDao()
            .getByKey(DatasetConstants.AIRCRAFT_TYPES_KEY)

        if (existingMetadata?.version == DatasetConstants.AIRCRAFT_TYPES_VERSION) {
            return
        }

        val dataset = loadDataset()

        database.withTransaction {
            database.aircraftTypeDao().upsertAll(dataset.aircraftTypes.map { it.toEntity() })
            database.datasetMetadataDao().upsert(
                DatasetMetadataEntity(
                    key = DatasetConstants.AIRCRAFT_TYPES_KEY,
                    version = dataset.version,
                    importedAt = Instant.now().toString(),
                ),
            )
        }
    }

    private fun loadDataset(): AircraftTypeDatasetDto {
        val rawJson = context.assets
            .open(AIRCRAFT_TYPES_ASSET_PATH)
            .bufferedReader()
            .use { it.readText() }

        return json.decodeFromString(AircraftTypeDatasetDto.serializer(), rawJson)
    }

    private fun AircraftTypeDto.toEntity(): AircraftTypeEntity {
        val tokens = buildSet {
            add(code)
            add(displayName)
            add("$manufacturer $model")
            aliases.forEach { add(it) }
        }.map { it.normalizedAircraftToken() }
            .filter { it.isNotBlank() }
            .joinToString(separator = "|", prefix = "|", postfix = "|")

        return AircraftTypeEntity(
            code = code.normalizedAircraftToken(),
            manufacturer = manufacturer,
            model = model,
            displayName = displayName,
            category = category,
            numEngines = numEngines,
            engineType = engineType,
            imageAssetRef = imageAssetRef,
            normalizedSearchTokens = tokens,
        )
    }

    private companion object {
        const val AIRCRAFT_TYPES_ASSET_PATH = "data/aircraft_types.json"
    }
}
