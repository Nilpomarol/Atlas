package com.atlas.ui.components.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.maps.Style.Builder

/** Converts a Compose [Color] to a CSS hex string suitable for MapLibre layer properties. */
fun Color.toHexColor(): String {
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)
    return "#%02X%02X%02X".format(r, g, b)
}

// Free OSM vector tiles — no API key required.
// Swap this URL to use MapTiler, Stadia, or a self-hosted tile server.
const val ATLAS_MAP_STYLE = "https://tiles.openfreemap.org/styles/liberty"

/**
 * Lifecycle-aware MapLibre MapView wrapper for Jetpack Compose.
 * Calls [onMapReady] once the style has loaded and the map is ready to receive sources/layers.
 */
@Composable
fun AtlasMapView(
    modifier: Modifier = Modifier,
    styleUrl: String = ATLAS_MAP_STYLE,
    onMapReady: (MapLibreMap, Style) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
    )

    DisposableEffect(lifecycleOwner) {
        mapView.onCreate(null)
        val state = lifecycleOwner.lifecycle.currentState
        if (state.isAtLeast(Lifecycle.State.STARTED)) mapView.onStart()
        if (state.isAtLeast(Lifecycle.State.RESUMED)) mapView.onResume()

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    LaunchedEffect(mapView, styleUrl) {
        mapView.getMapAsync { map ->
            val styleCallback = Style.OnStyleLoaded { style -> onMapReady(map, style) }
            if (styleUrl.trimStart().startsWith("{")) {
                // Inline JSON style — no network tiles
                map.setStyle(Style.Builder().fromJson(styleUrl), styleCallback)
            } else {
                // Remote style URL
                map.setStyle(styleUrl, styleCallback)
            }
        }
    }
}
