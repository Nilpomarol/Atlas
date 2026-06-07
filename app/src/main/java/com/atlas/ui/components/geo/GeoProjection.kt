package com.atlas.ui.components.geo

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

private const val MaxMercatorLatitude = 85.05112878
private const val DegreesToRadians = PI / 180.0
private const val RadiansToDegrees = 180.0 / PI

internal data class GeoProjection(
    val centerLongitude: Double,
    private val centerX: Double,
    private val centerY: Double,
    private val scale: Double,
    private val drawSize: Size,
) {
    fun project(coordinate: GeoCoordinate): Offset {
        val longitude = normalizeLongitudeAround(coordinate.longitude, centerLongitude)
        val x = longitude * DegreesToRadians
        val y = mercatorY(coordinate.latitude)
        return Offset(
            x = (drawSize.width / 2f + ((x - centerX) * scale).toFloat()),
            y = (drawSize.height / 2f - ((y - centerY) * scale).toFloat()),
        )
    }
}

internal fun buildProjection(
    viewport: GeoViewport,
    drawSize: Size,
    paddingPx: Float,
): GeoProjection {
    val projectedBounds = when (viewport) {
        is GeoViewport.World -> ProjectedBounds(
            minX = -PI,
            maxX = PI,
            minY = mercatorY(viewport.minLatitudeDeg.coerceIn(-MaxMercatorLatitude, MaxMercatorLatitude)),
            maxY = mercatorY(viewport.maxLatitudeDeg.coerceIn(-MaxMercatorLatitude, MaxMercatorLatitude)),
            centerLongitude = 0.0,
        )

        is GeoViewport.FitPoints -> projectedBoundsForPoints(viewport)
    }

    val availableWidth = max(1f, drawSize.width - paddingPx * 2f).toDouble()
    val availableHeight = max(1f, drawSize.height - paddingPx * 2f).toDouble()
    val spanX = max(0.0001, projectedBounds.maxX - projectedBounds.minX)
    val spanY = max(0.0001, projectedBounds.maxY - projectedBounds.minY)
    val scale = min(availableWidth / spanX, availableHeight / spanY)

    return GeoProjection(
        centerLongitude = projectedBounds.centerLongitude,
        centerX = (projectedBounds.minX + projectedBounds.maxX) / 2.0,
        centerY = (projectedBounds.minY + projectedBounds.maxY) / 2.0,
        scale = scale,
        drawSize = drawSize,
    )
}

private data class ProjectedBounds(
    val minX: Double,
    val maxX: Double,
    val minY: Double,
    val maxY: Double,
    val centerLongitude: Double,
)

private fun projectedBoundsForPoints(viewport: GeoViewport.FitPoints): ProjectedBounds {
    val points = viewport.points.takeIf { it.isNotEmpty() }
        ?: return ProjectedBounds(-PI, PI, mercatorY(-MaxMercatorLatitude), mercatorY(MaxMercatorLatitude), 0.0)
    val unwrappedLongitudes = points.unwrappedLongitudes()
    var minLongitude = unwrappedLongitudes.minOrNull() ?: -180.0
    var maxLongitude = unwrappedLongitudes.maxOrNull() ?: 180.0
    val centerLongitude = (minLongitude + maxLongitude) / 2.0

    val minLongitudeSpan = viewport.minLongitudeSpanDegrees.coerceAtLeast(0.5)
    if ((maxLongitude - minLongitude) < minLongitudeSpan) {
        val center = (minLongitude + maxLongitude) / 2.0
        minLongitude = center - minLongitudeSpan / 2.0
        maxLongitude = center + minLongitudeSpan / 2.0
    }

    var minLatitude = points.minOf { it.latitude }.coerceIn(-MaxMercatorLatitude, MaxMercatorLatitude)
    var maxLatitude = points.maxOf { it.latitude }.coerceIn(-MaxMercatorLatitude, MaxMercatorLatitude)
    val minLatitudeSpan = viewport.minLatitudeSpanDegrees.coerceAtLeast(0.5)
    if ((maxLatitude - minLatitude) < minLatitudeSpan) {
        val center = ((minLatitude + maxLatitude) / 2.0).coerceIn(-MaxMercatorLatitude, MaxMercatorLatitude)
        minLatitude = (center - minLatitudeSpan / 2.0).coerceAtLeast(-MaxMercatorLatitude)
        maxLatitude = (center + minLatitudeSpan / 2.0).coerceAtMost(MaxMercatorLatitude)
    }

    val marginX = (maxLongitude - minLongitude) * 0.18
    val marginLatitude = (maxLatitude - minLatitude) * 0.22
    minLongitude -= marginX
    maxLongitude += marginX
    minLatitude = (minLatitude - marginLatitude).coerceAtLeast(-MaxMercatorLatitude)
    maxLatitude = (maxLatitude + marginLatitude).coerceAtMost(MaxMercatorLatitude)

    return ProjectedBounds(
        minX = minLongitude * DegreesToRadians,
        maxX = maxLongitude * DegreesToRadians,
        minY = mercatorY(minLatitude),
        maxY = mercatorY(maxLatitude),
        centerLongitude = normalizeLongitude(centerLongitude),
    )
}

internal fun normalizeLongitudeAround(longitude: Double, centerLongitude: Double): Double {
    var value = longitude
    while (value - centerLongitude > 180.0) value -= 360.0
    while (value - centerLongitude < -180.0) value += 360.0
    return value
}

internal fun greatCirclePoints(
    from: GeoCoordinate,
    to: GeoCoordinate,
    steps: Int = 64,
): List<GeoCoordinate> {
    val lat1 = from.latitude * DegreesToRadians
    val lon1 = from.longitude * DegreesToRadians
    val lat2 = to.latitude * DegreesToRadians
    val lon2 = to.longitude * DegreesToRadians
    val start = SphericalPoint.fromRadians(lat1, lon1)
    val end = SphericalPoint.fromRadians(lat2, lon2)
    val dot = (start.x * end.x + start.y * end.y + start.z * end.z).coerceIn(-1.0, 1.0)
    val omega = kotlin.math.acos(dot)
    if (omega < 0.000001) return listOf(from, to)

    val sinOmega = sin(omega)
    return (0..steps).map { index ->
        val t = index.toDouble() / steps.toDouble()
        val a = sin((1.0 - t) * omega) / sinOmega
        val b = sin(t * omega) / sinOmega
        val x = a * start.x + b * end.x
        val y = a * start.y + b * end.y
        val z = a * start.z + b * end.z
        val normalizer = sqrt(x.pow(2) + y.pow(2) + z.pow(2)).coerceAtLeast(0.000001)
        val latitude = atan((z / normalizer) / sqrt((x / normalizer).pow(2) + (y / normalizer).pow(2))) * RadiansToDegrees
        val longitude = kotlin.math.atan2(y, x) * RadiansToDegrees
        GeoCoordinate(latitude = latitude, longitude = normalizeLongitude(longitude))
    }
}

internal fun flightRouteCurvePoints(
    from: GeoCoordinate,
    to: GeoCoordinate,
    steps: Int = 64,
    greatCircleWeight: Double = 0.38,
): List<GeoCoordinate> {
    val directToLongitude = normalizeLongitudeAround(to.longitude, from.longitude)
    val greatCircle = greatCirclePoints(from, to, steps)
    return greatCircle.mapIndexed { index, greatCirclePoint ->
        val t = index.toDouble() / steps.toDouble()
        val directLatitude = from.latitude + (to.latitude - from.latitude) * t
        val directLongitude = from.longitude + (directToLongitude - from.longitude) * t
        val greatCircleLongitude = normalizeLongitudeAround(greatCirclePoint.longitude, directLongitude)
        GeoCoordinate(
            latitude = directLatitude + (greatCirclePoint.latitude - directLatitude) * greatCircleWeight,
            longitude = normalizeLongitude(directLongitude + (greatCircleLongitude - directLongitude) * greatCircleWeight),
        )
    }
}

private data class SphericalPoint(
    val x: Double,
    val y: Double,
    val z: Double,
) {
    companion object {
        fun fromRadians(latitude: Double, longitude: Double): SphericalPoint {
            val cosLatitude = cos(latitude)
            return SphericalPoint(
                x = cosLatitude * cos(longitude),
                y = cosLatitude * sin(longitude),
                z = sin(latitude),
            )
        }
    }
}

private fun mercatorY(latitude: Double): Double {
    val clamped = latitude.coerceIn(-MaxMercatorLatitude, MaxMercatorLatitude)
    return ln(tan(PI / 4.0 + (clamped * DegreesToRadians) / 2.0))
}

private fun List<GeoCoordinate>.unwrappedLongitudes(): List<Double> {
    val result = mutableListOf<Double>()
    forEachIndexed { index, point ->
        val longitude = normalizeLongitude(point.longitude)
        if (index == 0) {
            result += longitude
        } else {
            result += normalizeLongitudeAround(longitude, result.last())
        }
    }
    return result
}

private fun normalizeLongitude(longitude: Double): Double {
    var value = longitude
    while (value > 180.0) value -= 360.0
    while (value < -180.0) value += 360.0
    return value
}

internal fun isAntimeridianJump(previous: GeoCoordinate, current: GeoCoordinate, centerLongitude: Double): Boolean {
    val previousLongitude = normalizeLongitudeAround(previous.longitude, centerLongitude)
    val currentLongitude = normalizeLongitudeAround(current.longitude, centerLongitude)
    return abs(currentLongitude - previousLongitude) > 180.0
}
