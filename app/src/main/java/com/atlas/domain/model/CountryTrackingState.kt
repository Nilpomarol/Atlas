package com.atlas.domain.model

data class CountryTrackingState(
    val wished: Boolean,
    val currentlyLiving: Boolean,
    val lived: Boolean,
    val visited: Boolean,
    val planned: Boolean,
    val neverVisited: Boolean,
) {
    companion object {
        val Empty = CountryTrackingState(
            wished = false,
            currentlyLiving = false,
            lived = false,
            visited = false,
            planned = false,
            neverVisited = true,
        )
    }
}
