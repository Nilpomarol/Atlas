package com.atlas.data.util

fun String.normalizedAircraftToken(): String =
    uppercase().filter { it.isLetterOrDigit() }
