package com.atlas.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ExcursionWithItems(
    @Embedded val excursion: ExcursionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "excursion_id",
    )
    val stops: List<ExcursionStopEntity>,
)
