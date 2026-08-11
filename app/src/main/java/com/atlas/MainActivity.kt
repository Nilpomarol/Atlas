package com.atlas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import com.atlas.app.AtlasApplication
import com.atlas.ui.rework.app.AtlasReworkApp
import com.atlas.ui.navigation.AtlasNavHost
import com.atlas.ui.theme.AtlasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )

        setContent {
            if (USE_REWORK_UI) {
                AtlasReworkApp(container = (application as AtlasApplication).container)
            } else {
                AtlasTheme {
                    AtlasNavHost()
                }
            }
        }
    }

    private companion object {
        // Temporary development bridge. The legacy graph remains intact until rework cutover.
        const val USE_REWORK_UI = true
    }
}
