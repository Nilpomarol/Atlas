package com.atlas.domain.repository

class LocationSearchException(
    override val message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
