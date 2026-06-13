package com.atlas.ui.screens.countryinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atlas.presentation.country.SectionItem
import com.atlas.ui.components.AtlasSectionLabel

@Composable
internal fun PracticalSection(items: List<SectionItem>) {
    val byKey = items.filterIsInstance<SectionItem.FactItem>().associate { it.fact.key to it.fact }

    val codes = listOfNotNull(
        byKey["calling_code"]?.let { KeyFact("Prefix telefònic", it.value, mono = true) },
        byKey["tld"]?.let { KeyFact("Domini d'internet", it.value, mono = true) },
    )
    val conventions = listOfNotNull(
        byKey["timezones"]?.let { KeyFact("Fus horari", it.value, mono = true) },
        byKey["driving_side"]?.let { KeyFact("Circulació", it.value) },
        byKey["start_of_week"]?.let { KeyFact("Inici de la setmana", it.value) },
    )

    if (codes.isEmpty() && conventions.isEmpty()) return

    Column(
        Modifier.padding(top = 6.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (codes.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AtlasSectionLabel("Codis")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    codes.forEach { KeyFactCell(it) }
                    if (codes.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        if (conventions.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AtlasSectionLabel("Costums i horaris")
                conventions.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { KeyFactCell(it) }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
