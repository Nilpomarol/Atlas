package com.atlas.ui.screens.country

import androidx.compose.ui.graphics.Color
import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.CountryTrackingState
import com.atlas.domain.model.CountryType
import com.atlas.domain.model.TravelStatus
import com.atlas.ui.theme.AtlasLiving
import com.atlas.ui.theme.AtlasLivingContainer
import com.atlas.ui.theme.AtlasLivingSecondary
import com.atlas.ui.theme.AtlasLived
import com.atlas.ui.theme.AtlasLivedContainer
import com.atlas.ui.theme.AtlasPending
import com.atlas.ui.theme.AtlasPendingContainer
import com.atlas.ui.theme.AtlasPendingSecondary
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasPlannedContainer
import com.atlas.ui.theme.AtlasPlannedSecondary
import com.atlas.ui.theme.AtlasVisited
import com.atlas.ui.theme.AtlasVisitedContainer
import com.atlas.ui.theme.AtlasVisitedSecondary
import com.atlas.ui.theme.AtlasWished
import com.atlas.ui.theme.AtlasWishedContainer

data class CountryDetailStyle(
    val primary: Color,
    val secondary: Color,
    val primaryLight: Color,
)

fun CountryTrackingState.toStyle(): CountryDetailStyle = when {
    currentlyLiving -> CountryDetailStyle(AtlasLiving, AtlasLivingSecondary, AtlasLivingContainer)
    lived -> CountryDetailStyle(AtlasLived, AtlasLivingSecondary, AtlasLivedContainer)
    visited -> CountryDetailStyle(AtlasVisited, AtlasVisitedSecondary, AtlasVisitedContainer)
    planned -> CountryDetailStyle(AtlasPlanned, AtlasPlannedSecondary, AtlasPlannedContainer)
    wished -> CountryDetailStyle(AtlasWished, Color(0xFFB45309), AtlasWishedContainer)
    else -> CountryDetailStyle(AtlasPending, AtlasPendingSecondary, AtlasPendingContainer)
}

fun CountryTrackingState.toStateLabel(): String = when {
    currentlyLiving -> "VIVINT"
    lived -> "VISCUT"
    visited -> "VISITAT"
    planned -> "PLANEJAT"
    wished -> "DESITJAT"
    else -> "PENDENT"
}

fun CountryLogType.toCatalanLabel(): String = when (this) {
    CountryLogType.VISIT -> "Visita"
    CountryLogType.LIVED -> "Viscut"
}

fun CountryLogType.toAddDialogTitle(): String = when (this) {
    CountryLogType.VISIT -> "Afegeix visita"
    CountryLogType.LIVED -> "Afegeix viscut"
}

fun TravelStatus.toCatalanLabel(): String = when (this) {
    TravelStatus.PLANNED -> "Planificat"
    TravelStatus.IN_PROGRESS -> "En curs"
    TravelStatus.COMPLETED -> "Completat"
    TravelStatus.UNKNOWN -> "Desconegut"
}

fun CountryType.toCountryDetailCatalanLabel(): String = when (this) {
    CountryType.SOVEREIGN_STATE -> "Estat sobirà"
    CountryType.DEPENDENT_TERRITORY -> "Territori dependent"
    CountryType.SPECIAL_REGION -> "Regió especial"
    CountryType.DISPUTED_OR_OTHER -> "Disputat o altre"
}

fun String.toCatalanContinent(): String = when (this) {
    "Africa" -> "Àfrica"
    "Asia" -> "Àsia"
    "Europe" -> "Europa"
    "North America" -> "Amèrica del Nord"
    "South America" -> "Amèrica del Sud"
    "Oceania" -> "Oceania"
    "Antarctica" -> "Antàrtida"
    else -> this
}
