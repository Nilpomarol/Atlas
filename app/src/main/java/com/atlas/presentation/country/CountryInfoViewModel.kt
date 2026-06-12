package com.atlas.presentation.country

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryPhoto
import com.atlas.domain.model.CountryStatFact
import com.atlas.domain.repository.CountryPhotoRepository
import com.atlas.domain.repository.CountryRepository
import com.atlas.domain.repository.CountryStatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Country info screen. Reads the flexible stat facts for a country and classifies
 * each one into a render type so the screen can draw the right visual. Raw facts
 * keep 16 granular categories; the screen groups them into 12 display sections.
 */
class CountryInfoViewModel(
    countryRepository: CountryRepository,
    countryStatRepository: CountryStatRepository,
    private val countryPhotoRepository: CountryPhotoRepository,
    iso2: String,
) : ViewModel() {

    val uiState: StateFlow<CountryInfoUiState> = combine(
        countryRepository.observeCountry(iso2),
        countryStatRepository.observeByCountry(iso2),
        countryPhotoRepository.observePhoto(iso2),
    ) { country, facts, photo ->
        buildUiState(country, facts, photo)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CountryInfoUiState(isLoading = true),
    )

    init {
        viewModelScope.launch {
            val country = countryRepository.observeCountry(iso2).filterNotNull().first()
            countryPhotoRepository.refreshIfStale(iso2, country.nameEn ?: country.nameCa)
        }
    }

    private fun buildUiState(
        country: Country?,
        facts: List<CountryStatFact>,
        photo: CountryPhoto?,
    ): CountryInfoUiState {
        if (country == null) {
            return CountryInfoUiState(isLoading = facts.isEmpty())
        }
        val views = facts.map { it.toView() }
        return CountryInfoUiState(
            country = country,
            photo = photo,
            sections = buildSections(views),
            highlights = buildHighlights(views),
            kpis = buildKpis(views),
            isLoading = false,
        )
    }

    private fun CountryStatFact.toView(): CountryFactView {
        val type = when {
            value.startsWith("[") -> FactRenderType.BREAKDOWN
            key in MEMBERSHIP_KEYS -> FactRenderType.MEMBERSHIP
            category == CAT_DRETS -> FactRenderType.STATUS
            rank != null -> FactRenderType.RANKED_NUMERIC
            unit == "%" -> FactRenderType.PERCENT
            else -> FactRenderType.TEXT
        }
        return CountryFactView(
            category = category,
            key = key,
            label = labelCa,
            value = value,
            unit = unit,
            year = year,
            rank = rank,
            rankTotal = rankTotal,
            tier = tier,
            sortOrder = sortOrder,
            renderType = type,
            breakdown = if (type == FactRenderType.BREAKDOWN) parseBreakdown(value) else null,
        )
    }

    private fun parseBreakdown(raw: String): List<BreakdownSlice> = try {
        json.decodeFromString(ListSerializer(BreakdownEntry.serializer()), raw)
            .map { BreakdownSlice(it.name, it.pct) }
    } catch (e: Exception) {
        emptyList()
    }

    private fun buildSections(views: List<CountryFactView>): List<CountryInfoSection> {
        val grouped = views.groupBy { SECTION_FOR_CATEGORY[it.category] ?: it.category }
        return SECTION_ORDER.mapNotNull { sectionKey ->
            val facts = grouped[sectionKey]?.sortedWith(
                compareBy({ CATEGORY_ORDER.indexOf(it.category) }, { it.sortOrder }),
            ) ?: return@mapNotNull null
            val items = buildSectionItems(facts)
            if (items.isEmpty()) null
            else CountryInfoSection(
                key = sectionKey,
                title = SECTION_TITLE[sectionKey] ?: sectionKey,
                items = items,
            )
        }
    }

    /** Collapse composition triplets (age, GDP sectors) and the membership set into
     *  their own visual items; everything else stays an individual fact, in order. */
    private fun buildSectionItems(facts: List<CountryFactView>): List<SectionItem> {
        val byKey = facts.associateBy { it.key }
        val consumed = mutableSetOf<String>()
        val items = mutableListOf<SectionItem>()
        for (fact in facts) {
            if (fact.key in consumed) continue
            val comp = COMPOSITION_GROUPS.firstOrNull {
                fact.key in it.keys && it.keys.all(byKey::containsKey)
            }
            if (comp != null) {
                items += SectionItem.CompositionItem(
                    title = comp.title,
                    segments = comp.keys.mapIndexed { i, k ->
                        val v = byKey.getValue(k)
                        CompositionSegment(comp.labels[i], v.value.toCaDouble(), v.value + "%")
                    },
                )
                consumed += comp.keys
                continue
            }
            if (fact.key in MEMBERSHIP_KEYS) {
                items += SectionItem.MembershipItem(
                    MEMBERSHIP_ORDER.mapNotNull { (k, label) ->
                        byKey[k]?.let { Membership(label, it.value == "Sí") }
                    },
                )
                consumed += MEMBERSHIP_KEYS
                continue
            }
            items += SectionItem.FactItem(fact)
        }
        return items
    }

    private fun String.toCaDouble(): Double =
        replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0

    /** The country's most exceptional world rankings (top ~12% among ≥30 ranked peers). */
    private fun buildHighlights(views: List<CountryFactView>): List<CountryHighlight> =
        views.asSequence()
            .filter { it.rank != null && (it.rankTotal ?: 0) >= MIN_RANK_PEERS }
            .sortedBy { it.rank!!.toDouble() / it.rankTotal!! }
            .filter { it.rank!!.toDouble() / it.rankTotal!! <= HIGHLIGHT_PERCENTILE }
            .take(MAX_HIGHLIGHTS)
            .map {
                CountryHighlight(
                    rank = it.rank!!,
                    rankTotal = it.rankTotal!!,
                    label = it.label,
                    valueText = it.value + (it.unit?.let { u -> " $u" } ?: ""),
                    tier = it.tier,
                )
            }
            .toList()

    private fun buildKpis(views: List<CountryFactView>): List<CountryKpi> {
        val byKey = views.associateBy { it.key }
        return KPI_KEYS.mapNotNull { key ->
            byKey[key]?.let {
                CountryKpi(it.label, it.value, it.unit, it.rank, it.rankTotal, it.tier)
            }
        }.take(MAX_KPIS)
    }

    @Serializable
    private data class BreakdownEntry(val name: String, val pct: Double)

    class Factory(
        private val countryRepository: CountryRepository,
        private val countryStatRepository: CountryStatRepository,
        private val countryPhotoRepository: CountryPhotoRepository,
        private val iso2: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CountryInfoViewModel(countryRepository, countryStatRepository, countryPhotoRepository, iso2) as T
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }

        const val CAT_DRETS = "drets"
        val MEMBERSHIP_KEYS = setOf("un_member", "eu_member", "schengen", "nato_member", "oecd_member")
        val MEMBERSHIP_ORDER = listOf(
            "un_member" to "ONU",
            "eu_member" to "UE",
            "schengen" to "Schengen",
            "nato_member" to "OTAN",
            "oecd_member" to "OCDE",
        )
        val COMPOSITION_GROUPS = listOf(
            CompositionGroup(
                title = "Estructura d'edat",
                keys = listOf("age_0_14", "age_15_64", "age_65_plus"),
                labels = listOf("0–14", "15–64", "65+"),
            ),
            CompositionGroup(
                title = "Sectors econòmics",
                keys = listOf("agri_gdp", "industry_gdp", "services_gdp"),
                labels = listOf("Agricultura", "Indústria", "Serveis"),
            ),
        )
        val KPI_KEYS = listOf("population", "gdp_per_capita", "life_expectancy", "hdi", "area", "gini")

        const val MIN_RANK_PEERS = 30
        const val HIGHLIGHT_PERCENTILE = 0.12
        const val MAX_HIGHLIGHTS = 4
        const val MAX_KPIS = 4

        // 16 granular categories → 12 display sections.
        val SECTION_FOR_CATEGORY = mapOf(
            "identitat" to "identitat",
            "geografia" to "geo_medi",
            "mediambient" to "geo_medi",
            "demografia" to "demografia",
            "salut" to "salut",
            "economia" to "economia",
            "finances" to "finances",
            "desigualtat" to "finances",
            "desenvolupament" to "desenvolupament",
            "educacio" to "desenvolupament",
            "infraestructura" to "infraestructura",
            "governanca" to "governanca",
            "cultura" to "cultura",
            "turisme" to "cultura",
            "drets" to "drets",
            "practic" to "practic",
        )
        val SECTION_ORDER = listOf(
            "identitat", "geo_medi", "demografia", "salut", "economia", "finances",
            "desenvolupament", "infraestructura", "governanca", "cultura", "drets", "practic",
        )
        val SECTION_TITLE = mapOf(
            "identitat" to "Identitat",
            "geo_medi" to "Geografia i medi ambient",
            "demografia" to "Demografia",
            "salut" to "Salut",
            "economia" to "Economia",
            "finances" to "Finances i desigualtat",
            "desenvolupament" to "Desenvolupament i educació",
            "infraestructura" to "Infraestructura i tecnologia",
            "governanca" to "Governança i política",
            "cultura" to "Cultura i turisme",
            "drets" to "Drets i societat",
            "practic" to "Pràctic per viatjar",
        )
        // Raw category order — used to sort facts within a merged section.
        val CATEGORY_ORDER = listOf(
            "identitat", "geografia", "mediambient", "demografia", "salut", "economia",
            "finances", "desigualtat", "desenvolupament", "educacio", "infraestructura",
            "governanca", "cultura", "turisme", "drets", "practic",
        )
    }
}

enum class FactRenderType { RANKED_NUMERIC, PERCENT, BREAKDOWN, STATUS, MEMBERSHIP, TEXT }

data class BreakdownSlice(val name: String, val pct: Double)

data class CountryFactView(
    val category: String,
    val key: String,
    val label: String,
    val value: String,
    val unit: String?,
    val year: Int?,
    val rank: Int?,
    val rankTotal: Int?,
    val tier: String?,
    val sortOrder: Int,
    val renderType: FactRenderType,
    val breakdown: List<BreakdownSlice>?,
)

data class CountryInfoSection(
    val key: String,
    val title: String,
    val items: List<SectionItem>,
)

sealed interface SectionItem {
    data class FactItem(val fact: CountryFactView) : SectionItem
    data class CompositionItem(val title: String, val segments: List<CompositionSegment>) : SectionItem
    data class MembershipItem(val memberships: List<Membership>) : SectionItem
}

data class CompositionSegment(val label: String, val pct: Double, val valueText: String)

data class Membership(val label: String, val isMember: Boolean)

data class CompositionGroup(
    val title: String,
    val keys: List<String>,
    val labels: List<String>,
)

data class CountryHighlight(
    val rank: Int,
    val rankTotal: Int,
    val label: String,
    val valueText: String,
    val tier: String?,
)

data class CountryKpi(
    val label: String,
    val value: String,
    val unit: String?,
    val rank: Int?,
    val rankTotal: Int?,
    val tier: String?,
)

data class CountryInfoUiState(
    val country: Country? = null,
    val photo: CountryPhoto? = null,
    val sections: List<CountryInfoSection> = emptyList(),
    val highlights: List<CountryHighlight> = emptyList(),
    val kpis: List<CountryKpi> = emptyList(),
    val isLoading: Boolean = true,
)
