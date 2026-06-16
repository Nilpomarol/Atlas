package com.atlas.ui.screens.trip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.atlas.presentation.trip.PhotoViewerItemUiState
import com.atlas.presentation.trip.TripStorySlideUiState
import com.atlas.presentation.trip.TripStoryUiState
import com.atlas.ui.components.PhotoViewerDialog
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val AUTO_PLAY_DELAY_MS = 4_500L

@Composable
fun TripStoryScreen(
    uiState: TripStoryUiState,
    onBackClick: () -> Unit,
) {
    var selectedPhotoId by rememberSaveable { mutableStateOf<String?>(null) }
    var autoPlay by rememberSaveable { mutableStateOf(true) }
    val slides = uiState.slides

    if (uiState.trip == null || slides.isEmpty()) {
        LoadingStory(onBackClick = onBackClick)
        return
    }

    val pagerState = rememberPagerState { slides.size }
    val scope = rememberCoroutineScope()

    LaunchedEffect(slides.size) {
        if (pagerState.currentPage > slides.lastIndex) {
            pagerState.scrollToPage(slides.lastIndex)
        }
    }

    LaunchedEffect(autoPlay, pagerState.currentPage, slides.size) {
        if (!autoPlay || slides.size < 2) return@LaunchedEffect
        delay(AUTO_PLAY_DELAY_MS)
        val current = pagerState.currentPage
        if (current < slides.lastIndex) {
            pagerState.animateScrollToPage(current + 1)
        } else {
            autoPlay = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        HorizontalPager(
            state = pagerState,
            key = { page -> slides[page].id },
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            StorySlide(
                slide = slides[page],
                uiState = uiState,
                onPhotoClick = { item ->
                    autoPlay = false
                    selectedPhotoId = item.photo.id
                },
            )
        }

        StoryTopControls(
            currentPage = pagerState.currentPage,
            total = slides.size,
            onBackClick = onBackClick,
        )

        StoryBottomControls(
            autoPlay = autoPlay,
            canGoBack = pagerState.currentPage > 0,
            canGoForward = pagerState.currentPage < slides.lastIndex,
            onPrevious = {
                autoPlay = false
                val target = (pagerState.currentPage - 1).coerceAtLeast(0)
                scope.launch { pagerState.animateScrollToPage(target) }
            },
            onNext = {
                autoPlay = false
                val target = (pagerState.currentPage + 1).coerceAtMost(slides.lastIndex)
                scope.launch { pagerState.animateScrollToPage(target) }
            },
            onPlayPause = {
                if (!autoPlay && pagerState.currentPage == slides.lastIndex) {
                    scope.launch { pagerState.animateScrollToPage(0) }
                }
                autoPlay = !autoPlay
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    selectedPhotoId?.let { photoId ->
        PhotoViewerDialog(
            items = uiState.viewerItems,
            initialPhotoId = photoId,
            coverPhotoFilename = uiState.trip?.coverPhotoFilename,
            onDismiss = { selectedPhotoId = null },
        )
    }
}

@Composable
private fun LoadingStory(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Carregant el relat...",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.72f),
        )
        StoryBackButton(
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart),
        )
    }
}

@Composable
private fun StorySlide(
    slide: TripStorySlideUiState,
    uiState: TripStoryUiState,
    onPhotoClick: (PhotoViewerItemUiState) -> Unit,
) {
    when (slide) {
        is TripStorySlideUiState.Title -> TitleSlide(slide)
        is TripStorySlideUiState.Route -> RouteSlide(slide, uiState)
        is TripStorySlideUiState.Place -> PlaceSlide(slide)
        is TripStorySlideUiState.Photo -> PhotoSlide(slide, onPhotoClick)
        is TripStorySlideUiState.Summary -> SummarySlide(slide)
    }
}

@Composable
private fun TitleSlide(slide: TripStorySlideUiState.Title) {
    CenterSlide {
        Text(
            text = "RELAT DEL VIATGE",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AtlasPrimary,
        )
        Text(
            text = slide.title,
            style = MaterialTheme.typography.displaySmall.copy(lineHeight = 42.sp),
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        slide.dateText?.let {
            Text(
                text = it.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.72f),
            )
        }
        StoryStats(
            dayCountText = slide.dayCountText,
            stopCount = slide.stopCount,
            countryCount = slide.countryCount,
            photoCount = slide.photoCount,
        )
    }
}

@Composable
private fun RouteSlide(
    slide: TripStorySlideUiState.Route,
    uiState: TripStoryUiState,
) {
    val mapHeight = (LocalConfiguration.current.screenHeightDp * 0.52f).dp
    CenterSlide(contentMaxWidth = true) {
        Text(
            text = "RUTA",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AtlasPrimary,
        )
        TripMapPreview(
            stops = uiState.mapStops,
            excursions = uiState.mapExcursions,
            mapHeight = mapHeight,
            gesturesEnabled = false,
            showFooter = false,
            generatedStopsVisible = true,
        )
        Text(
            text = slide.routeText ?: "Encara no hi ha parades en aquest viatge.",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        if (slide.countryNames.isNotEmpty()) {
            Text(
                text = slide.countryNames.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PlaceSlide(slide: TripStorySlideUiState.Place) {
    CenterSlide {
        Text(
            text = slide.eyebrow,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AtlasPrimary,
        )
        Text(
            text = slide.title,
            style = MaterialTheme.typography.displaySmall.copy(lineHeight = 42.sp),
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        slide.contextText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
            )
        }
        slide.notes?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.76f),
                textAlign = TextAlign.Center,
            )
        }
        if (slide.photoCount > 0) {
            Text(
                text = "${slide.photoCount} ${if (slide.photoCount == 1) "foto" else "fotos"}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.64f),
            )
        }
    }
}

@Composable
private fun PhotoSlide(
    slide: TripStorySlideUiState.Photo,
    onPhotoClick: (PhotoViewerItemUiState) -> Unit,
) {
    val context = LocalContext.current
    val file = remember(slide.item.photo.filename) {
        File(context.filesDir, "photos/${slide.item.photo.filename}")
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onPhotoClick(slide.item) },
    ) {
        SubcomposeAsyncImage(
            model = file,
            contentDescription = "Foto de ${slide.item.title}",
            modifier = Modifier.fillMaxSize(),
        ) {
            when (painter.state) {
                is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent(
                    contentScale = ContentScale.Crop,
                )
                else -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AtlasBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No s'ha pogut carregar la foto",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.72f),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.46f))
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 118.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = slide.sectionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AtlasPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = slide.item.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = listOfNotNull(slide.item.contextLabel, slide.item.dateText)
                        .joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.76f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SummarySlide(slide: TripStorySlideUiState.Summary) {
    CenterSlide {
        Text(
            text = "RESUM",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AtlasPrimary,
        )
        Text(
            text = slide.title,
            style = MaterialTheme.typography.displaySmall.copy(lineHeight = 42.sp),
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        StoryStats(
            dayCountText = slide.dayCountText,
            stopCount = slide.stopCount,
            countryCount = slide.countryCount,
            photoCount = slide.photoCount,
        )
        Text(
            text = "Relat generat automàticament amb les dades actuals del viatge.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.72f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CenterSlide(
    contentMaxWidth: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 22.dp)
                .then(if (contentMaxWidth) Modifier.fillMaxWidth() else Modifier),
            shape = RoundedCornerShape(26.dp),
            color = AtlasSurface.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content,
            )
        }
    }
}

@Composable
private fun StoryStats(
    dayCountText: String,
    stopCount: Int,
    countryCount: Int,
    photoCount: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StoryStat(value = dayCountText, label = "DIES")
        StoryStat(value = stopCount.toString(), label = "PARADES")
        StoryStat(value = countryCount.toString(), label = "PAÏSOS")
        StoryStat(value = photoCount.toString(), label = "FOTOS")
    }
}

@Composable
private fun StoryStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = Color.White,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.58f),
        )
    }
}

@Composable
private fun StoryTopControls(
    currentPage: Int,
    total: Int,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StoryProgress(currentPage = currentPage, total = total)
        Row(verticalAlignment = Alignment.CenterVertically) {
            StoryBackButton(onBackClick = onBackClick)
            Spacer(Modifier.weight(1f))
            Text(
                text = "${currentPage + 1} / $total",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
private fun StoryBackButton(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(42.dp),
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Torna",
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun StoryProgress(currentPage: Int, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (index <= currentPage) Color.White.copy(alpha = 0.86f)
                        else Color.White.copy(alpha = 0.22f),
                    ),
            )
        }
    }
}

@Composable
private fun StoryBottomControls(
    autoPlay: Boolean,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StoryControlButton(
            enabled = canGoBack,
            onClick = onPrevious,
        ) {
            Icon(Icons.Filled.SkipPrevious, contentDescription = "Anterior", tint = Color.White)
        }
        StoryControlButton(onClick = onPlayPause) {
            Icon(
                imageVector = if (autoPlay) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (autoPlay) "Pausa" else "Reprodueix",
                tint = Color.White,
            )
        }
        StoryControlButton(
            enabled = canGoForward,
            onClick = onNext,
        ) {
            Icon(Icons.Filled.SkipNext, contentDescription = "Següent", tint = Color.White)
        }
    }
}

@Composable
private fun StoryControlButton(
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.size(50.dp),
        shape = CircleShape,
        color = if (enabled) Color.Black.copy(alpha = 0.54f) else Color.Black.copy(alpha = 0.24f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = if (enabled) 0.18f else 0.08f)),
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
        ) {
            content()
        }
    }
}
