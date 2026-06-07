package com.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.atlas.data.local.entity.DatasetMetadataEntity

@Dao
interface DatasetMetadataDao {
    @Query("SELECT * FROM dataset_metadata WHERE `key` = :key")
    suspend fun getByKey(key: String): DatasetMetadataEntity?

    @Query("SELECT * FROM dataset_metadata")
    suspend fun getAll(): List<DatasetMetadataEntity>

    @Upsert
    suspend fun upsert(metadata: DatasetMetadataEntity)
}
