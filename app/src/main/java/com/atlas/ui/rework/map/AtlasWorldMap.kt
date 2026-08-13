package com.atlas.ui.rework.map

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.atlas.ui.rework.foundation.AtlasReworkTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.MultiPolygon
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@Immutable
data class AtlasWorldMapCountries(
    val living: Set<String> = emptySet(),
    val lived: Set<String> = emptySet(),
    val visited: Set<String> = emptySet(),
    val planned: Set<String> = emptySet(),
    val wished: Set<String> = emptySet(),
)

private const val CountriesAsset = "geo/ne_110m_admin_0_countries.geojson"
// Higher-resolution borders for the detail locator map (smoother coastlines when framed).
private const val DetailCountriesAsset = "geo/ne_50m_admin_0_countries.geojson"
private const val NorthernLatitude = 83.0
private const val SouthernLatitude = -60.0
private const val MinimumCameraScale = 1f
private const val MaximumCameraScale = 8f

/** Geometry is static bundled reference data; retain it across destination changes. */
@Volatile
private var cachedWorldGeometry: WorldGeometry? = null

/** Separate cache for the higher-resolution detail-locator geometry. */
@Volatile
private var cachedDetailGeometry: WorldGeometry? = null

private val northernMercatorY = projectLatitude(NorthernLatitude)
private val southernMercatorY = projectLatitude(SouthernLatitude)

internal val AtlasWorldLandAspectRatio: Float
    get() = (southernMercatorY - northernMercatorY).toFloat()

@Composable
fun AtlasWorldMap(
    countries: AtlasWorldMapCountries,
    initialLandTopPx: Float,
    selectedCountryIso2: String? = null,
    cameraResetRequest: Int = 0,
    modifier: Modifier = Modifier,
    onCountrySelected: (String) -> Unit = {},
    onSelectionCleared: () -> Unit = {},
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val colors = AtlasReworkTheme.colors
    val currentSelectionHandler by rememberUpdatedState(onCountrySelected)
    val currentClearSelectionHandler by rememberUpdatedState(onSelectionCleared)
    val sideMarginPx = with(density) { 8.dp.toPx() }
    val minimumVisiblePx = with(density) { 48.dp.toPx() }
    val borderWidthPx = with(density) { 0.5.dp.toPx() }
    val grainLight = Color(0xFFF3EAD4)
    val grainSepia = Color(0xFF3A2E18)

    var sourceGeometry by remember { mutableStateOf(cachedWorldGeometry) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var cameraScale by remember { mutableFloatStateOf(MinimumCameraScale) }
    var cameraOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(Unit) {
        if (sourceGeometry == null) {
            sourceGeometry = withContext(Dispatchers.IO) {
                cachedWorldGeometry ?: context.assets.open(CountriesAsset).bufferedReader().use { reader ->
                    parseWorldGeometry(FeatureCollection.fromJson(reader.readText())).also { cachedWorldGeometry = it }
                }
            }
        }
    }

    LaunchedEffect(cameraResetRequest) {
        if (cameraResetRequest == 0) return@LaunchedEffect
        val initialScale = cameraScale
        val initialOffset = cameraOffset
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        ) { progress, _ ->
            cameraScale = initialScale + (MinimumCameraScale - initialScale) * progress
            cameraOffset = initialOffset * (1f - progress)
        }
    }

    val geometry = remember(sourceGeometry, viewportSize, initialLandTopPx, sideMarginPx) {
        sourceGeometry?.takeIf { viewportSize.width > 0 }?.toViewportGeometry(
            viewportWidth = viewportSize.width.toFloat(),
            landTop = initialLandTopPx,
            sideMargin = sideMarginPx,
        )
    }
    val grain = remember { createSeaGrain() }

    Canvas(
        modifier = modifier
            .background(colors.mapWater)
            .onSizeChanged { viewportSize = it }
            .pointerInput(geometry) {
                detectTransformGestures { centroid, pan, gestureZoom, _ ->
                    val mapGeometry = geometry ?: return@detectTransformGestures
                    val oldScale = cameraScale
                    val newScale = (oldScale * gestureZoom).coerceIn(MinimumCameraScale, MaximumCameraScale)
                    val scaleChange = newScale / oldScale
                    val proposedOffset = cameraOffset * scaleChange + centroid * (1f - scaleChange) + pan
                    cameraScale = newScale
                    cameraOffset = clampCameraOffset(
                        proposed = proposedOffset,
                        cameraScale = newScale,
                        landBounds = mapGeometry.landBounds,
                        viewportSize = viewportSize,
                        minimumVisible = minimumVisiblePx,
                    )
                }
            }
            .pointerInput(geometry) {
                detectTapGestures { tap ->
                    val mapGeometry = geometry ?: return@detectTapGestures
                    val pointInBaseMap = (tap - cameraOffset) / cameraScale
                    mapGeometry.countries
                        .firstOrNull { country -> country.contains(pointInBaseMap) }
                        ?.iso2
                        ?.let(currentSelectionHandler)
                        ?: currentClearSelectionHandler()
                }
            },
    ) {
        drawRect(colors.mapWater)
        drawRect(
            brush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0f to colors.mapVignette.copy(alpha = 0.10f),
                    0.14f to Color.Transparent,
                    0.70f to Color.Transparent,
                    1f to colors.mapVignette.copy(alpha = 0.16f),
                ),
            ),
        )

        val mapGeometry = geometry ?: return@Canvas
        translate(left = cameraOffset.x, top = cameraOffset.y) {
            scale(scale = cameraScale, pivot = Offset.Zero) {
                mapGeometry.countries.forEach { country ->
                    drawPath(
                        path = country.path,
                        color = countryColor(country.iso2, countries, colors.mapLand, colors.living, colors.lived, colors.visited, colors.planned, colors.wished),
                    )
                }
                selectedCountryIso2?.let { selectedIso2 ->
                    mapGeometry.countries.firstOrNull { it.iso2 == selectedIso2 }?.let { country ->
                        drawPath(
                            path = country.path,
                            color = colors.accent,
                            style = Stroke(width = 2.4f * borderWidthPx / cameraScale),
                        )
                    }
                }
                mapGeometry.countries.forEach { country ->
                    drawPath(
                        path = country.path,
                        color = colors.mapBorder,
                        style = Stroke(width = borderWidthPx / cameraScale),
                    )
                }
            }
        }

        grain.forEach { mark ->
            drawCircle(
                color = if (mark.light) grainLight else grainSepia,
                alpha = mark.alpha,
                radius = mark.radius,
                center = Offset(mark.x * size.width, mark.y * size.height),
            )
        }
    }
}

private fun countryColor(
    iso2: String?,
    countries: AtlasWorldMapCountries,
    default: Color,
    living: Color,
    lived: Color,
    visited: Color,
    planned: Color,
    wished: Color,
): Color {
    if (iso2 == null) return default
    val relationshipColor = when (iso2) {
        in countries.living -> living
        in countries.lived -> lived
        in countries.visited -> visited
        in countries.planned -> planned
        in countries.wished -> wished
        else -> default
    }
    return if (relationshipColor == default) default else lerp(default, relationshipColor, 0.66f)
}

private data class WorldGeometry(val countries: List<NormalizedCountry>)

private data class NormalizedCountry(
    val iso2: String?,
    val polygons: List<List<List<Offset>>>,
)

private data class ViewportGeometry(
    val countries: List<ViewportCountry>,
    val landBounds: Rect,
)

private data class ViewportCountry(
    val iso2: String?,
    val path: Path,
    val polygons: List<List<List<Offset>>>,
    val bounds: Rect,
) {
    fun contains(point: Offset): Boolean {
        if (!bounds.contains(point)) return false
        return polygons.any { polygon ->
            val outerRing = polygon.firstOrNull() ?: return@any false
            pointInRing(point, outerRing) && polygon.drop(1).none { hole -> pointInRing(point, hole) }
        }
    }
}

private fun parseWorldGeometry(collection: FeatureCollection): WorldGeometry {
    return WorldGeometry(
        countries = collection.features().orEmpty().mapNotNull(::parseCountry),
    )
}

private fun parseCountry(feature: Feature): NormalizedCountry? {
    if (feature.getStringProperty("CONTINENT") == "Antarctica") return null
    val iso2 = feature.getStringProperty("ISO_A2")?.takeUnless { it == "-99" }
        ?: feature.getStringProperty("ISO_A2_EH")?.takeUnless { it == "-99" }
    val polygons: List<List<List<Point>>> = when (val geometry = feature.geometry()) {
        is Polygon -> listOf(geometry.coordinates())
        is MultiPolygon -> geometry.coordinates()
        else -> return null
    }
    return NormalizedCountry(
        iso2 = iso2,
        polygons = polygons.map { polygon ->
            polygon.map { ring ->
                ring.map { point ->
                    Offset(
                        x = ((point.longitude() + 180.0) / 360.0).toFloat(),
                        y = projectLatitude(point.latitude()).toFloat(),
                    )
                }
            }
        },
    )
}

private fun WorldGeometry.toViewportGeometry(
    viewportWidth: Float,
    landTop: Float,
    sideMargin: Float,
): ViewportGeometry {
    val worldWidth = (viewportWidth - sideMargin * 2f).coerceAtLeast(1f)
    fun Offset.toViewportPoint(): Offset = Offset(
        x = sideMargin + x * worldWidth,
        y = landTop + (y - northernMercatorY.toFloat()) * worldWidth,
    )

    val viewportCountries = countries.map { country ->
        val viewportPolygons = country.polygons.map { polygon ->
            polygon.map { ring -> ring.map { point -> point.toViewportPoint() } }
        }
        val path = Path().apply {
            fillType = PathFillType.EvenOdd
            viewportPolygons.forEach { polygon ->
                polygon.forEach { ring ->
                    ring.firstOrNull()?.let { first ->
                        moveTo(first.x, first.y)
                        ring.drop(1).forEach { point -> lineTo(point.x, point.y) }
                        close()
                    }
                }
            }
        }
        val points = viewportPolygons.flatten().flatten()
        val bounds = Rect(
            left = points.minOf { it.x },
            top = points.minOf { it.y },
            right = points.maxOf { it.x },
            bottom = points.maxOf { it.y },
        )
        ViewportCountry(country.iso2, path, viewportPolygons, bounds)
    }

    return ViewportGeometry(
        countries = viewportCountries,
        landBounds = Rect(
            left = sideMargin,
            top = landTop,
            right = viewportWidth - sideMargin,
            bottom = landTop + AtlasWorldLandAspectRatio * worldWidth,
        ),
    )
}

private fun clampCameraOffset(
    proposed: Offset,
    cameraScale: Float,
    landBounds: Rect,
    viewportSize: IntSize,
    minimumVisible: Float,
): Offset {
    val minimumX = minimumVisible - cameraScale * landBounds.right
    val maximumX = viewportSize.width - minimumVisible - cameraScale * landBounds.left
    val minimumY = minimumVisible - cameraScale * landBounds.bottom
    val maximumY = viewportSize.height - minimumVisible - cameraScale * landBounds.top
    return Offset(
        x = proposed.x.coerceIn(min(minimumX, maximumX), max(minimumX, maximumX)),
        y = proposed.y.coerceIn(min(minimumY, maximumY), max(minimumY, maximumY)),
    )
}

private fun pointInRing(point: Offset, ring: List<Offset>): Boolean {
    if (ring.size < 3) return false
    var inside = false
    var previous = ring.last()
    ring.forEach { current ->
        val crosses = (current.y > point.y) != (previous.y > point.y)
        if (crosses) {
            val intersectionX = (previous.x - current.x) * (point.y - current.y) /
                (previous.y - current.y) + current.x
            if (point.x < intersectionX) inside = !inside
        }
        previous = current
    }
    return inside
}

private fun projectLatitude(latitude: Double): Double {
    val constrained = latitude.coerceIn(-85.05112878, 85.05112878)
    val radians = Math.toRadians(constrained)
    return 0.5 - ln((1.0 + sin(radians)) / (1.0 - sin(radians))) / (4.0 * PI)
}

private data class GrainMark(
    val x: Float,
    val y: Float,
    val radius: Float,
    val alpha: Float,
    val light: Boolean,
)

private fun createSeaGrain(): List<GrainMark> {
    val random = Random(8142)
    return List(1500) {
        GrainMark(
            x = random.nextFloat(),
            y = random.nextFloat(),
            radius = random.nextFloat() * 1.4f + 0.5f,
            alpha = random.nextFloat() * 0.09f + 0.055f,
            light = random.nextBoolean(),
        )
    }
}

/**
 * A static, non-interactive locator map framed on a single country, with its capital
 * pinned and labelled. Reuses the shared world geometry parse/cache and projection.
 * The focus country is filled in its relationship colour; neighbours provide context.
 */
@Composable
fun CountryLocatorMap(
    iso2: String,
    countries: AtlasWorldMapCountries,
    capitalLatitude: Double?,
    capitalLongitude: Double?,
    capitalName: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val colors = AtlasReworkTheme.colors
    val borderWidthPx = with(density) { 0.6.dp.toPx() }
    val framePaddingPx = with(density) { 34.dp.toPx() }

    var sourceGeometry by remember { mutableStateOf(cachedDetailGeometry) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(Unit) {
        if (sourceGeometry == null) {
            sourceGeometry = withContext(Dispatchers.IO) {
                cachedDetailGeometry ?: context.assets.open(DetailCountriesAsset).bufferedReader().use { reader ->
                    parseWorldGeometry(FeatureCollection.fromJson(reader.readText())).also { cachedDetailGeometry = it }
                }
            }
        }
    }

    val geometry = remember(sourceGeometry, viewportSize) {
        sourceGeometry?.takeIf { viewportSize.width > 0 && viewportSize.height > 0 }?.toViewportGeometry(
            viewportWidth = viewportSize.width.toFloat(),
            landTop = 0f,
            sideMargin = 0f,
        )
    }

    val focus = remember(geometry, iso2, capitalLatitude, capitalLongitude, viewportSize, framePaddingPx) {
        val mapGeometry = geometry ?: return@remember null
        val worldWidth = viewportSize.width.toFloat()
        val capitalBase = if (capitalLatitude != null && capitalLongitude != null) {
            Offset(
                x = ((capitalLongitude + 180.0) / 360.0).toFloat() * worldWidth,
                y = (projectLatitude(capitalLatitude).toFloat() - northernMercatorY.toFloat()) * worldWidth,
            )
        } else {
            null
        }
        val country = mapGeometry.countries.firstOrNull { it.iso2 == iso2 }
        // Small territories are absent from the low-res world geometry; fall back to a
        // regional window centred on the capital so the pin and neighbours still show.
        val bounds = when {
            country != null -> country.focusBounds(capitalBase)
            capitalBase != null -> {
                val half = worldWidth * 0.05f
                Rect(capitalBase.x - half, capitalBase.y - half, capitalBase.x + half, capitalBase.y + half)
            }
            else -> return@remember null
        }
        computeLocatorFocus(bounds, capitalBase, viewportSize, framePaddingPx)
    }

    Box(
        modifier
            .background(colors.mapWater)
            .onSizeChanged { viewportSize = it },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(colors.mapWater)
            val mapGeometry = geometry ?: return@Canvas
            val locator = focus ?: return@Canvas
            translate(left = locator.offset.x, top = locator.offset.y) {
                scale(scale = locator.scale, pivot = Offset.Zero) {
                    mapGeometry.countries.forEach { country ->
                        val isFocus = country.iso2 == iso2
                        drawPath(
                            path = country.path,
                            color = if (isFocus) {
                                countryColor(country.iso2, countries, colors.mapLand, colors.living, colors.lived, colors.visited, colors.planned, colors.wished)
                            } else {
                                colors.mapLand.copy(alpha = 0.55f)
                            },
                        )
                    }
                    mapGeometry.countries.forEach { country ->
                        drawPath(
                            path = country.path,
                            color = colors.mapBorder.copy(alpha = 0.7f),
                            style = Stroke(width = borderWidthPx / locator.scale),
                        )
                    }
                    mapGeometry.countries.firstOrNull { it.iso2 == iso2 }?.let { country ->
                        drawPath(
                            path = country.path,
                            color = colors.accent,
                            style = Stroke(width = 2.2f * borderWidthPx / locator.scale),
                        )
                    }
                }
            }
        }

        val capitalScreen = focus?.capitalScreen
        if (capitalScreen != null) {
            CapitalMarker(
                screen = capitalScreen,
                name = capitalName,
                colors = colors,
            )
        }
    }
}

@Composable
private fun BoxScope.CapitalMarker(
    screen: Offset,
    name: String?,
    colors: com.atlas.ui.rework.foundation.ReworkColors,
) {
    val density = LocalDensity.current
    val dotSize = 12.dp
    val halfDotPx = with(density) { (dotSize / 2).toPx() }
    Row(
        modifier = Modifier
            .offset { IntOffset((screen.x - halfDotPx).roundToInt(), (screen.y - halfDotPx).roundToInt()) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(dotSize)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .padding(2.5.dp)
                .clip(CircleShape)
                .background(colors.accent),
        )
        if (!name.isNullOrBlank()) {
            Spacer(Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(7.dp),
                color = colors.surfaceStrong,
                contentColor = colors.ink,
                border = BorderStroke(1.dp, colors.border),
                shadowElevation = 3.dp,
            ) {
                Text(
                    text = name,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    style = AtlasReworkTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.ink,
                )
            }
        }
    }
}

private data class LocatorFocus(val scale: Float, val offset: Offset, val capitalScreen: Offset?)

/** Bounds of the polygon containing [capitalBase] (the mainland), else the full country bounds. */
private fun ViewportCountry.focusBounds(capitalBase: Offset?): Rect {
    if (capitalBase != null) {
        val polygon = polygons.firstOrNull { polygon ->
            val outerRing = polygon.firstOrNull() ?: return@firstOrNull false
            pointInRing(capitalBase, outerRing)
        }
        if (polygon != null) {
            val points = polygon.flatten()
            if (points.isNotEmpty()) {
                return Rect(
                    left = points.minOf { it.x },
                    top = points.minOf { it.y },
                    right = points.maxOf { it.x },
                    bottom = points.maxOf { it.y },
                )
            }
        }
    }
    return bounds
}

private fun computeLocatorFocus(
    bounds: Rect,
    capitalBase: Offset?,
    viewportSize: IntSize,
    padding: Float,
): LocatorFocus {
    val availableWidth = (viewportSize.width - padding * 2f).coerceAtLeast(1f)
    val availableHeight = (viewportSize.height - padding * 2f).coerceAtLeast(1f)
    val boundsWidth = bounds.width.coerceAtLeast(0.001f)
    val boundsHeight = bounds.height.coerceAtLeast(0.001f)
    // Cap the zoom so tiny countries keep surrounding context instead of filling the panel.
    val scale = min(availableWidth / boundsWidth, availableHeight / boundsHeight).coerceIn(1f, 48f)
    val center = bounds.center
    val offset = Offset(
        x = viewportSize.width / 2f - center.x * scale,
        y = viewportSize.height / 2f - center.y * scale,
    )
    val capitalScreen = capitalBase?.let { Offset(it.x * scale + offset.x, it.y * scale + offset.y) }
    return LocatorFocus(scale = scale, offset = offset, capitalScreen = capitalScreen)
}
