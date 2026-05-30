package com.atlas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardPlaceholderScreen() {
    PlaceholderScreen(
        title = "Inici",
        body = "El resum del teu atlas apareixerà aquí.",
    )
}

@Composable
fun TripsPlaceholderScreen() {
    PlaceholderScreen(
        title = "Viatges",
        body = "Els viatges simples arribaran després de la base de països.",
    )
}

@Composable
fun SettingsPlaceholderScreen() {
    PlaceholderScreen(
        title = "Configuració",
        body = "Les còpies de seguretat JSON viuran en aquesta secció.",
    )
}

@Composable
private fun PlaceholderScreen(
    title: String,
    body: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = body,
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
