package com.atlas.ui.rework.foundation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.R

@Immutable
data class ReworkColors(
    val mapWater: Color = Color(0xFFABC4BF),
    val mapLand: Color = Color(0xFFE9E2D2),
    val mapBorder: Color = Color(0xFF5F7F7A),
    val surface: Color = Color(0xFFF8F2E7),
    val surfaceStrong: Color = Color(0xFFFFFBF2),
    val ink: Color = Color(0xFF182C2A),
    val inkMuted: Color = Color(0xFF62706C),
    val accent: Color = Color(0xFFD85B37),
    val visited: Color = Color(0xFF168D72),
    val lived: Color = Color(0xFF26799D),
    val living: Color = Color(0xFFCE5231),
    val planned: Color = Color(0xFFD9A11D),
    val wished: Color = Color(0xFF7A549C),
    val border: Color = Color(0x33213632),
    val shadow: Color = Color(0x3D102825),
)

@Immutable
data class ReworkTypography(
    val display: TextStyle,
    val title: TextStyle,
    val body: TextStyle,
    val label: TextStyle,
    val data: TextStyle,
)

@Immutable
data class ReworkDimensions(
    val screenPadding: androidx.compose.ui.unit.Dp = 16.dp,
    val cardPadding: androidx.compose.ui.unit.Dp = 16.dp,
    val cardGap: androidx.compose.ui.unit.Dp = 10.dp,
    val cardRadius: androidx.compose.ui.unit.Dp = 14.dp,
    val controlRadius: androidx.compose.ui.unit.Dp = 14.dp,
    val navigationHeight: androidx.compose.ui.unit.Dp = 70.dp,
)

private val EditorialSerif = FontFamily(
    Font(R.font.fraunces_variable, FontWeight.Normal),
    Font(R.font.fraunces_variable, FontWeight.Medium),
    Font(R.font.fraunces_variable, FontWeight.SemiBold),
    Font(R.font.fraunces_variable, FontWeight.Bold),
    Font(R.font.fraunces_variable, FontWeight.ExtraBold),
)

private val EditorialSans = FontFamily(
    Font(R.font.hanken_grotesk_variable, FontWeight.Normal),
    Font(R.font.hanken_grotesk_variable, FontWeight.Medium),
    Font(R.font.hanken_grotesk_variable, FontWeight.SemiBold),
)

private val InstrumentMono = FontFamily(
    Font(R.font.space_mono_regular, FontWeight.Normal),
    Font(R.font.space_mono_bold, FontWeight.Bold),
)

val LocalReworkColors = staticCompositionLocalOf { ReworkColors() }
val LocalReworkTypography = staticCompositionLocalOf {
    ReworkTypography(
        display = TextStyle(fontFamily = EditorialSerif, fontSize = 32.sp, lineHeight = 34.sp, fontWeight = FontWeight.ExtraBold),
        title = TextStyle(fontFamily = EditorialSerif, fontSize = 21.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold),
        body = TextStyle(fontFamily = EditorialSans, fontSize = 15.sp, lineHeight = 20.sp),
        label = TextStyle(fontFamily = EditorialSans, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp),
        data = TextStyle(fontFamily = InstrumentMono, fontSize = 12.sp, lineHeight = 16.sp),
    )
}
val LocalReworkDimensions = staticCompositionLocalOf { ReworkDimensions() }

object AtlasReworkTheme {
    val colors: ReworkColors @Composable get() = LocalReworkColors.current
    val typography: ReworkTypography @Composable get() = LocalReworkTypography.current
    val dimensions: ReworkDimensions @Composable get() = LocalReworkDimensions.current
}

@Composable
fun AtlasReworkTheme(content: @Composable () -> Unit) {
    val colors = ReworkColors()
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = colors.accent,
            surface = colors.surface,
            onSurface = colors.ink,
            background = colors.mapWater,
            onBackground = colors.ink,
        ),
        content = content,
    )
}
