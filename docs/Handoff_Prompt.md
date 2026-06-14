# Atlas Handoff

## Purpose

This is the current operational source of truth for Atlas. It records what is implemented now, the active phase, and constraints that must be preserved.

When this document conflicts with a long-term specification or roadmap, follow this document and the code currently in the repository.

Last updated: 2026-06-14.

## Current Status

- Atlas is a native Android, local-first personal travel atlas.
- v2.0, v3.0, v3.1, and v3.2 are complete.
- The current phase is v4.0 Country Depth and UI polish.
- v4.0 M1 is complete: country facts dataset, flexible fact table, importer, repository, and Room DB v22.
- v4.0 M2 is complete and committed: Country Info screen, country photo cache, Unsplash integration, and Room DB v23.
- Country Info redesign is complete and committed: full-bleed photo hero, highlights/KPI shelf, dissolved identity (facts redistributed), visual geography section with offline neighbors map, and all section cards (incl. the final drets/practic polish).
- v4.0 M3 is complete and committed: country-detail enrichment — landscape photo hero with identity overlay and state pills, headline KPI tiles, compact offline map card, and a currency converter card. Room DB v24.
- v4.0 M4 is complete and committed: country-list sorting (name/population/area/GDP/HDI) with ascending/descending, the sorted metric shown per row, accent-insensitive search and name sort, a continent-grouping on/off toggle, and state-colored filter pills.
- v4.0 is feature-complete. The outstanding work is manual device QA across M3/M4 (none of it has been verified on a device yet) and any polish that surfaces from it.

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

Room database version: **24**.

The migration chain is explicit and registered in `AtlasAppContainer`.

Recent migrations:

- `18 -> 19`: flight country-tracking flags.
- `19 -> 20`: `stop_photos`.
- `20 -> 21`: trip `cover_photo_filename`.
- `21 -> 22`: `country_stat_facts`.
- `22 -> 23`: `country_photos`.
- `23 -> 24`: `country_landscape_photos` and `currency_rates`.

Current Room entities:

- Static/reference: countries, dataset metadata, airports, airlines, aircraft types, aircraft cache, country stat facts.
- User-created: country user states, country logs, trips, trip stops, excursions, excursion stops, flights, itineraries, itinerary groups, stop photos.
- External cache: country photos (portrait, Country Info), country landscape photos (country detail hero), currency rates.

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
- `CountryMapCard`: offline Canvas geo on the country detail screen (replaced `CountryMapHero`).
- `FlightRouteGeoMap`: offline Canvas geo.
- `DashboardMapHero`: offline Canvas geo.
- `TripMapPreview`: MapLibre.

## Active Work

v4.0 is feature-complete. The only outstanding work is **manual device QA** of the
M3 and M4 changes (so far verified only by `assembleDebug` and unit tests), plus any
polish that QA surfaces.

### Country Info (complete, committed)

All sections have a custom visual redesign, each special-cased in `SectionCard` by
section key: hero, highlights/KPI shelf, geography, governance, demography, health,
economia, desenvolupament, infraestructura, cultura, drets, practic. Identity was
dissolved into the others; the standalone finances section was removed (fiscal/trade/
investment route into economia, `desigualtat` into desenvolupament via
`SECTION_FOR_CATEGORY`). CO2 ranks are derived at import (`co2_per_capita`,
`co2_total`), dataset `2026.2`.

### M3 — country detail enrichment (complete, committed)

The country detail screen (`ui/screens/country/`) was restructured:

- `CountryDetailHero`: landscape photo hero carrying identity on the image (name,
  capital · subregion, flag) with prominent solid state pills; navy fallback when no
  photo. Replaced the old map-based hero.
- `CountryStatsCard`: 2x2 tier-colored KPI tiles (population, area, GDP/cap, HDI) with
  a filled "Veure detalls" link into Country Info.
- `CountryMapCard`: compact offline `AtlasGeoCanvas` map.
- `CountryCurrencyCard`: compact converter (type-to-convert, swap direction, prefilled
  with 1) showing the currency name, live EUR rate, and "Actualitzat … · BCE" caption.
- `CountryQuickActions` gained an "El teu seguiment" section title.
- Removed `CountryIdentityHeader`, `CountryInfoCard`, `CountryMapHero`.
- Data: `country_landscape_photos` cache (rotating set of up to 5 landscape photos,
  ~30-day refresh) via a landscape `UnsplashClient` method; `currency_rates` cache
  (frankfurter.app, keyless, 12h refresh); `CountryCurrencyCodeMap` (domain) maps ISO2
  → ISO 4217 because the dataset stores currency names, not codes.

### M4 — country list sorting and filter polish (complete, committed)

- Sort by name, population, area, GDP (total), or HDI, with an ascending/descending
  toggle; the active stat value is shown on each row. `sortCountryRows` is a pure,
  unit-tested function; values come from `CountryStatRepository.observeByKeys`.
- Accent/diacritic-insensitive search and name sorting (`String.foldAccents`).
- Continent grouping is now a user toggle in the sort menu; ungrouped renders a flat,
  globally-sorted list.
- Filter pills: removed "currently living" and "never visited"; the remaining pills
  (Tots, Visitats, Desitjats, Planejats, Viscuts) fill one line and take their state
  color when selected.
- `compactStatValue` extracted to shared `presentation/country/StatFormatting.kt`
  (used by detail KPIs and the list metric).

### Remaining device QA

- M3: landscape hero scrim/identity/state-pills across phone sizes and long names;
  the currency converter (keyboard, swap, prefilled value, long converted amounts);
  KPI tiles with long Catalan labels; offline behaviour (photo + currency cards must
  hide gracefully when nothing is cached); a real Unsplash key for the landscape fetch.
- M4: the five state-colored pills fitting one line on narrow screens; the per-row sort
  metric not crowding long names; the grouping on/off toggle; accent-insensitive search.
- Country Info (still pending device review from earlier): neighbors-map framing
  (small vs many-neighbor countries), ISO3 tag collisions, CO2 ranks not producing
  trivial highlights for near-zero emitters, and whether to localize English
  ethnic-group names.

## Validation

For Kotlin changes, use the smallest relevant check:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat testDebugUnitTest
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat assembleDebug
```

UI polish still requires manual device or screenshot review for spacing, text wrapping, and state variants.
