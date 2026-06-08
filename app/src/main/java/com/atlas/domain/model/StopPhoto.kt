package com.atlas.domain.model

data class StopPhoto(
    val id: String,
    val stopId: String,
    val stopType: StopType,
    val filename: String,
    val sortOrder: Int,
    val createdAt: String,
)
