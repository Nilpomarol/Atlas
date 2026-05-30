package com.atlas.data.dataset

import android.content.Context
import androidx.room.withTransaction
import com.atlas.core.constants.DatasetConstants
import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.local.entity.CountryEntity
import com.atlas.data.local.entity.DatasetMetadataEntity
import java.time.Instant
import kotlinx.serialization.json.Json

class CountryDatasetImporter(
    private val context: Context,
    private val database: AtlasDatabase,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun importIfNeeded() {
        val existingMetadata = database.datasetMetadataDao()
            .getByKey(DatasetConstants.COUNTRIES_KEY)

        if (existingMetadata?.version == DatasetConstants.COUNTRIES_VERSION) {
            return
        }

        val dataset = loadDataset()

        database.withTransaction {
            database.countryDao().upsertAll(
                dataset.countries.map { it.toEntity() },
            )
            database.datasetMetadataDao().upsert(
                DatasetMetadataEntity(
                    key = DatasetConstants.COUNTRIES_KEY,
                    version = dataset.version,
                    importedAt = Instant.now().toString(),
                ),
            )
        }
    }

    private fun loadDataset(): CountryDatasetDto {
        val rawJson = context.assets
            .open(COUNTRIES_ASSET_PATH)
            .bufferedReader()
            .use { it.readText() }

        return json.decodeFromString(CountryDatasetDto.serializer(), rawJson)
    }

    private fun CountryDto.toEntity(): CountryEntity = CountryEntity(
        iso2 = iso2,
        iso3 = iso3,
        nameCa = nameCa,
        nameEn = nameEn,
        type = type,
        parentIso2 = parentIso2,
        isUnMember = isUnMember,
        isObserverState = isObserverState,
        isTrackable = isTrackable,
        continent = continent,
        subregion = subregion,
        flagEmoji = flagEmoji,
        flagAsset = flagAsset,
        latitude = latitude,
        longitude = longitude,
    )

    private companion object {
        const val COUNTRIES_ASSET_PATH = "data/countries.json"
    }
}
