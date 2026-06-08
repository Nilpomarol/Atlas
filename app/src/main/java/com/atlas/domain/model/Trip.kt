package com.atlas.domain.model

data class Trip(
    val id: String,
    val title: String,
    val status: TravelStatus,
    val dateRange: FlexibleDateRange?,
    val notes: String?,
    val coverPhotoFilename: String? = null,
)
