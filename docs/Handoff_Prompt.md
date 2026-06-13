# Atlas Handoff

## Purpose

This is the current operational source of truth for Atlas. It records what is implemented now, the active phase, and constraints that must be preserved.

When this document conflicts with a long-term specification or roadmap, follow this document and the code currently in the repository.

Last updated: 2026-06-12.

## Current Status

- Atlas is a native Android, local-first personal travel atlas.
- v2.0, v3.0, v3.1, and v3.2 are complete.
- The current phase is v4.0 Country Depth and UI polish.
- v4.0 M1 is complete: country facts dataset, flexible fact table, importer, repository, and Room DB v22.
- v4.0 M2 is complete and committed: Country Info screen, country photo cache, Unsplash integration, and Room DB v23.
- Country Info is being redesigned section by section (committed): full-bleed photo hero, highlights/KPI shelf, dissolved identity (facts redistributed), and a visual geography section with an offline neighbors map.
- The active task remains Country Info redesign/polish. Do not treat this phase as complete yet.
- v4.0 M3 country-detail enrichment and M4 country-list filtering follow only after Country Info visual review is accepted.

## Stack and Constraints

- Kotlin production code.
- Jetpack Compose UI.
- Room local persistence.
- Navigation Compose.
- Coroutines and Flow.
- `kotlinx.serialization` for datasets and backup JSON.
- Manual dependency injection through `AtlasAppContainer`.
- MapLibre GL Android 11.11.0 where tile-backed geographic context is needed.
- Offline Compose Canvas geo rendering for reusable world/country/flight surfaces.
- Coil 2.7.0 for all image loading, including SVG flags.
- DataStore Preferences for API keys and preferences.
- No Hilt, Koin, Retrofit, osmdroid, backend requirement, or additional image loader unless explicitly approved.

Visible UI is Catalan-first. Code, identifiers, comments, and technical documentation are English.

## Architecture

```text
UI -> Presentation -> Domain -> Data -> Room / Static Datasets / External Services
```

- UI renders state and emits user events.
- Presentation owns ViewModels, display-ready UI state, flow combination, and screen events.
- Domain owns models, validation, derivation, status rules, and use cases.
- Data owns Room, DAOs, entities, migrations, DTOs, importers, API clients, mappers, and repository implementations.
- Room entities and external DTOs do not cross into presentation or UI.
- Map-provider and external-provider details stay behind wrappers or interfaces.

## Persistence Ground Truth

Room database version: **23**.

The migration chain is explicit and registered in `AtlasAppContainer`.

Recent migrations:

- `18 -> 19`: flight country-tracking flags.
- `19 -> 20`: `stop_photos`.
- `20 -> 21`: trip `cover_photo_filename`.
- `21 -> 22`: `country_stat_facts`.
- `22 -> 23`: `country_photos`.

Current Room entities:

- Static/reference: countries, dataset metadata, airports, airlines, aircraft types, aircraft cache, country stat facts.
- User-created: country user states, country logs, trips, trip stops, excursions, excursion stops, flights, itineraries, itinerary groups, stop photos.
- External cache: country photos.

Backup format version is 2. Existing v1 backups import through defaults. Photo binaries and external photo cache files are not embedded in JSON backup.

## Dataset Ground Truth

- Countries: 244 entries, dataset version `2026.2`.
- Airports: 5,931 imported entries, dataset version `2026.3`.
- Airlines: 101 entries, dataset version `2026.1`.
- Aircraft types: 50 entries, dataset version `2026.3`.
- Country stats: 244 countries, about 24,000 facts, 16 raw categories, dataset version `2026.2`. The importer derives ranks at import for selected unranked keys (currently `co2_per_capita` and `co2_total`, lower-is-better).

Static/reference imports are versioned and must not overwrite user-created travel data.

## Country Rules

- Country/territory identity uses ISO2 where available.
- Country tracking state is derived, not stored as a single source-of-truth state.
- `CountryStateDerivationService` is the only place that combines wished, lived, currently living, trips, stops, excursions, solo flights, and itinerary-derived visits.
- Currently living is exclusive.
- Completed solo flights count destination by default; origin counts only when explicitly enabled.
- In-progress and unknown flights do not prematurely mark destination as visited.
- Itinerary groups use the layover-safe endpoint rule already implemented by `CountryStateDerivationService`.

## Flexible Dates and Status

- Flexible dates support `YEAR`, `MONTH`, and `DAY` precision.
- Date formatting, validation, and ordering stay in shared domain/core utilities.
- A range preserves precision and uses matching start/end precision when both endpoints exist.
- Travel status is explicit source data. Date inference is conservative and must not unexpectedly overwrite manual status.
- Flight duration, delay, layovers, and ordering prefer UTC-derived values and fall back to local datetime values only when necessary.

## Country Stats and Country Info

`country_stat_facts` uses composite primary key:

```text
country_iso2 + category + key
```

Each fact carries:

```text
label_ca, value, unit, year, rank, rank_total, tier, sort_order
```

The importer reads `assets/data/country_stats.json`, replaces the static fact dataset when its version changes, and keeps user data separate.

`CountryInfoViewModel`:

- combines country, country facts, cached country photo, and the trackable country list;
- maps facts into display render types;
- parses religion and ethnicity distributions;
- groups 16 raw categories into 12 display sections;
- redistributes the former identity facts into the section matching what each fact is (state symbols to governance, anthem/codes/demonym to culture, capital to geography); the identity section no longer exists;
- resolves the Catalan `borders` names into ISO2 + coordinates for the neighbors map;
- hides empty sections;
- builds highlights (colored by world standing) and KPI items;
- keeps rendering decisions out of the data layer.

`CountryInfoScreen` is split into focused files under `ui/screens/countryinfo/` (scaffold, hero, shelf, section card + fact-row dispatcher, fact rows, identity fact rows, geography section, shared components, style). It provides:

- a full-bleed photo hero with a single bottom scrim, shared `BackPill`, and an SVG flag beside the name (navy fallback when no photo);
- a highlights shelf with fixed-size medal cards colored by world standing and a compact k/M/B/T number formatter;
- tier-colored KPI tiles;
- collapsible section cards with a fact-row dispatcher that renders some facts by key (coat of arms as an SVG emblem, government type simplified, codes in monospace);
- a custom geography section: area headline with rank bar, highest point with an Everest comparison bar, land-use stacked bar, a color-coded environment report card grouped into Energia/Emissions/Entorn, and an offline `AtlasGeoCanvas` neighbors map with label-only ISO3 tags and country pills;
- ranked, percent, distribution, composition, status, membership, and text presentations;
- explicit loading and empty states;
- only the first information section expanded by default;
- shared `AtlasCard`, `AtlasSectionLabel`, and `AtlasSectionTitle` primitives where applicable.

Country photo behavior:

- Unsplash key is stored in DataStore and configured under Settings > Integracions.
- Remote photos are optional and never required to access personal data.
- Cached files live under `filesDir/country_photos`.
- Refresh is lazy after 24 hours.
- Failed refresh preserves the previous cached photo.
- Search requests portrait orientation and no longer appends `landscape` to the query.
- Search chooses a deterministic daily candidate from usable portrait results.
- Portrait cache filenames use the `_portrait.jpg` suffix so existing landscape caches refresh once.
- The Country Info photo hero is full-bleed `560.dp` with a single bottom dark scrim and white caption; the no-photo navy fallback remains `184.dp`.

## UI Ground Truth

- Visual direction: Warm Editorial Atlas / Cartographer's Ink.
- Display typeface: Fraunces through `AtlasSerif`.
- UI/body typeface: Hanken Grotesk through `AtlasSans`.
- Instrument/data typeface: Space Mono through `AtlasMono`.
- Use shared Atlas colors, typography, shapes, cards, chips, menus, and spacing patterns.
- Preserve one clear subject per screen and keep secondary metadata quieter.
- Do not introduce broad redesigns during polish.

Implemented primary surfaces include dashboard, countries, country detail, Country Info, trips, trip detail, flights, flight detail, itineraries, itinerary detail, timeline, settings, and the full stats experience.

Map usage:

- `AtlasGeoCanvas`: reusable offline vector geo foundation.
- `CountryMapHero`: offline Canvas geo.
- `FlightRouteGeoMap`: offline Canvas geo.
- `DashboardMapHero`: offline Canvas geo.
- `TripMapPreview`: MapLibre.

## Active Work

Current priority:

Country Info is being redesigned section by section. Hero, highlights/KPI shelf,
identity (dissolved), geography, governance, demography, health, and economia are
done and committed. Each redesigned section is a custom composable special-cased
in `SectionCard` by section key.

The standalone finances section was removed: fiscal/trade/investment facts route
into economia and inequality (`desigualtat`) routes into desenvolupament (via
`SECTION_FOR_CATEGORY`).

1. Continue the section-by-section redesign — remaining: desenvolupament (now also
   holds inequality), infraestructura, cultura, drets, practic. Keep each section
   visual, hierarchical, and color-driven rather than a flat list.
2. Preserve current behavior, Room schema, presentation/domain boundaries; keep
   the dataset importer idempotent and version-gated.
3. After Country Info redesign is accepted, continue v4.0 M3 country-detail
   enrichment and M4 country-list filtering.

Completed in the current pass (committed):

- split `CountryInfoScreen` into focused files;
- full-bleed hero with bottom scrim, shared `BackPill`, and SVG flag;
- highlights/KPI shelf: fixed-size cards, medal colors by world standing, k/M/B/T
  compact number formatter, tier-colored KPI tiles;
- dissolved identity, redistributing facts and moving rich rendering (coat-of-arms
  emblem, simplified government, mono codes) to the fact-row level;
- geography: Everest comparison, land-use bar, grouped environment report card,
  and an offline neighbors map (`GeoMarker.labelOnly`);
- governance: state header, democracy/press/corruption index report card, defense
  block, and an even membership grid;
- demography: population headline, age-structure bar, ethnic donut, and a Dinamica
  block with a birth-vs-death natural-balance visual;
- health: life-expectancy headline with gender split, mortality report card, basic
  access meters, health-system rows, and risk-factor meters;
- economia: GDP headline + per-capita, sectors composition bar, rated macro
  indicators, and the merged finances blocks (trade, public finance, investment);
- CO2 ranks derived at import (`co2_per_capita`, `co2_total`), dataset `2026.2`;
- `assembleDebug` passes after each step.

The health section is a first pass and may still change (thresholds, grouping,
gender-gap visual). Section quality thresholds throughout are opinionated and
easy to retune.

Remaining visual review:

- device review of the full-bleed hero scrim and flag across phone sizes;
- device review of the geography neighbors-map framing (small vs many-neighbor
  countries), ISO3 tag collisions, and the environment meters;
- verify CO2 ranks do not produce trivial highlights for near-zero emitters;
- ethnic-group names render in English (dataset); decide whether to localize;
- device review of long Catalan labels and narrow layouts.

## Validation

For Kotlin changes, use the smallest relevant check:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat testDebugUnitTest
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat assembleDebug
```

UI polish still requires manual device or screenshot review for spacing, text wrapping, and state variants.
