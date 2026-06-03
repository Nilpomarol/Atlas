package com.atlas.domain.util

fun String.normalizedAircraftRegistration(): String =
    uppercase().filter { it.isLetterOrDigit() }
