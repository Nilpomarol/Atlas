package com.atlas.ui.rework.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.ImageView
import com.atlas.R
import com.atlas.ui.rework.foundation.AtlasReworkTheme

@Composable
fun AtlasWordmark(modifier: Modifier = Modifier) {
    val colors = AtlasReworkTheme.colors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    setImageResource(R.mipmap.ic_launcher)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    contentDescription = null
                }
            },
            modifier = Modifier.size(44.dp),
        )
        Text(
            text = "ATLAS",
            style = AtlasReworkTheme.typography.title.copy(
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.1.sp,
                shadow = Shadow(
                    color = colors.surfaceStrong.copy(alpha = 0.9f),
                    offset = androidx.compose.ui.geometry.Offset.Zero,
                    blurRadius = 8f,
                ),
            ),
            color = colors.ink,
        )
    }
}
