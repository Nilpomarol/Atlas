package com.atlas.presentation.country

/**
 * Compact, Catalan-formatted magnitude for large stat values so they never overflow:
 * k (milers), M (milions), B (mil milions), T (bilions). Values below 1000 or that are
 * not parseable numbers are returned untouched, so decimals (HDI) and small numbers keep
 * their original formatting.
 *
 * Shared by the country detail KPI tiles and the country-list sort metric.
 */
internal fun compactStatValue(raw: String): String {
    val parsed = raw.replace(".", "").replace(",", ".").toDoubleOrNull() ?: return raw
    val magnitude = kotlin.math.abs(parsed)
    if (magnitude < 1000) return raw
    val (scaled, suffix) = when {
        magnitude >= 1e12 -> parsed / 1e12 to "T"
        magnitude >= 1e9 -> parsed / 1e9 to "B"
        magnitude >= 1e6 -> parsed / 1e6 to "M"
        else -> parsed / 1e3 to "k"
    }
    val rounded = (scaled * 10).toLong() / 10.0
    val text = if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
    return text.replace(".", ",") + suffix
}
