# Atlas Handoff Prompt

## PROJECT OVERVIEW & STATUS

* **Project Name/Goal:** Atlas, a native Android local-first personal travel atlas. MVP goal: track countries/territories, wished/visited/lived/currently-living states, manual logs, simple trips, ordered trip stops, location search/manual fallback, derived country states, and JSON backup/import.
* **Current Phase:** MVP is functionally complete. Dashboard pass done, full country dataset live. Entering final MVP QA pass.
* **Core Tech Stack / Constraints:**
  * Kotlin, Jetpack Compose, Room, Navigation Compose, Coroutines/Flow, kotlinx.serialization, manual dependency container.
  * Android package/application id: `com.atlas`.
  * Catalan-first visible UI. English for code/class/function names and comments.
  * MVP only. Do not add flights, airports, itineraries, photos, cloud sync, accounts, advanced stats, or v2.0 tables unless explicitly requested.
  * Local-first. No backend.
  * Do not add Hilt/Koin/Retrofit/MapLibre/osmdroid or major libraries unless user explicitly agrees.
  * Flexible dates support `YEAR`, `MONTH`, `DAY`; `MONTH` means year + month only, no day.
  * Country state derivation must stay centralized in `CountryStateDerivationService`.
  * Use Gradle wrapper. On Windows:
    ```powershell
    $env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat testDebugUnitTest
    $env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat assembleDebug
    ```
  * ADB path: `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe`

## KEY DECISIONS & GROUND TRUTHS

* **MVP scope is locked:** countries/territories, country tracking, logs, trips, stops, location search/manual fallback, JSON backup/import, basic dashboard. Flights/airports/itineraries are v2.0.
* **UI direction:** Bold, visually powerful, slight gradients, country-state colors, inspired by country detail mockups. Full UI polish deferred until feature set is stable.
* **Country detail screen direction:** Static placeholder map for now; real map later. Country state colors are centralized in theme/style helpers. Flag should use SVG/image-style rendering where possible.
* **Trip map:** Placeholder map with stops exists; real map/API improvements later.
* **Location search:** Use explicit search, not autocomplete. Suggestions/autocomplete deferred. Search uses OSM/Nominatim with manual fallback.
* **Stop modal behavior:** Search-first. API-filled fields are hidden after result selection. Manual fields appear only on no result, manual entry, or edit-details action. Date/notes always visible.
* **Date picker:** Custom flexible date component exists for year, month-year, and day precision. Month picker is 4x3 equal-sized pills. Stop date defaults to trip start date when adding a new stop.
* **Trip detail behavior:** Reorder mode hides edit/delete buttons and shows move controls. Reorder toggle is an icon in the same line as `Parades` and `Afegeix`.
* **Trip editing:** Edit trip action lives inside trip detail, not on list cards.
* **Backup/import:** MVP backup version is `1`; import is replace-all after confirmation. Backup includes country states, country logs, trips, trip stops, dataset metadata.
* **Country dataset:** Full 244-entry dataset sourced from RestCountries API. Version `2026.1`. Importer must sort `parent_iso2 = null` entries first to satisfy Room's enforced self-referencing FK constraint on `countries.parent_iso2`.
* **Git status:** All work committed and pushed.
  * Commit: `997051e Add full country dataset and dashboard MVP pass`
  * Branch: `main`
  * Repo: `git@github.com:Nilpomarol/Travel-app-android.git`

## WHAT HAS BEEN ACCOMPLISHED SO FAR

* Android project foundation:
  * Gradle setup, Compose, Room, Navigation, manual DI container (`AtlasAppContainer`).
  * App launches with `AtlasApplication`, Room database, dataset importer.
* Countries/territories foundation:
  * `CountryEntity`, dataset metadata, full `countries.json` (244 entries: 195 sovereign states, 47 dependent territories, 2 special regions — Kosovo XK, Taiwan TW).
  * Fields: iso2, iso3, name_ca, name_en, type, parent_iso2, is_un_member, is_observer_state, is_trackable, continent, subregion, flag_emoji, latitude, longitude.
  * Country list/detail screens. Search/filter/state badges.
* Country tracking:
  * Wished toggle. Currently living rule (only one at a time).
  * Visit/lived logs with add/edit/delete. Flexible date validation/formatting.
  * Derived country states via `CountryStateDerivationService`.
* Country detail redesign:
  * Bold visual design, state-colored hero/info, quick actions, history list.
  * Static placeholder map. Logs use type colors. Split into smaller composables.
* Trips:
  * Trip model/entity/DAO/repository/use cases.
  * Trip list/detail routes and screens. Create/edit/delete trip.
  * Status, flexible dates, notes.
  * Trip list cards show status badge, date range, stop count, and first → last stop.
* Trip stops:
  * TripStop entity/DAO/domain/repository/use cases.
  * Add/edit/delete stops. Reorder mode. Manual stop entry and country selector.
  * Optional coordinates. Stop date defaults to trip start date on new stop.
  * Stop modal is search-first with manual fallback.
* Location search:
  * Nominatim search repository and DTO mapper.
  * Explicit `Cerca` search button. Search result selection fills location/country/coordinates.
  * Manual fallback works when search fails/no results.
  * Error handling hardened with typed search exception.
* Trip detail:
  * Map placeholder with stops. Stop cards show `MAPA`/`MANUAL` and coordinates if present.
  * Edit/delete on same line in normal mode. Reorder controls only in reorder mode.
  * Edit trip inside detail. Summary shows number of days and stops.
* Backup/import:
  * Backup DTOs, mappers, validator, repository, settings screen export/import.
  * Validation checks duplicate IDs, country refs, trip refs, flexible dates, single currently living, coordinate pairs/ranges.
  * Mapper round-trip test for all trip stop backup fields.
* Dashboard MVP pass:
  * `DashboardViewModel` derives: visitedCount, wishedCount, plannedCount, livedCount, tripCount, stopCount, trackableCountryCount, currentlyLivingCountryName, upcomingTrip, recentItems.
  * `DashboardScreen`: hero card, 3 rows of stat cards (visited/wished, planned/lived, trips/stops), currently living card (only when set), upcoming trip card, recent activity with per-type icons (green globe=visit, purple house=lived, blue map=trip).
  * `DashboardItemIcon` enum: VISIT, LIVED, TRIP — carried on each `DashboardRecentItemUiState`.
* Full country dataset:
  * Replaced placeholder (5 entries) with 244-entry dataset fetched from RestCountries API.
  * Catalan names supplied from knowledge (API has no Catalan translations).
  * `COUNTRIES_VERSION` bumped to `"2026.1"` in `DatasetConstants`.
  * Importer fixed: sorts `parent_iso2 = null` entries first to avoid `SQLiteConstraintException: FOREIGN KEY constraint failed` — Room enforces the self-referencing FK on `countries.parent_iso2` during `@Upsert` within a `withTransaction` block.
* Tests added/passing:
  * Flexible date validator/formatter.
  * Country state derivation service (planned/completed/in-progress/unknown trip stop rules).
  * Backup validator. Backup mappers. Nominatim DTO mapper.
* Last verified commands passed:
  ```powershell
  .\gradlew.bat testDebugUnitTest
  .\gradlew.bat assembleDebug
  ```
  App launches cleanly on device, no crash.

## CURRENT BOTTLENECKS & OPEN ISSUES

* **MVP QA pass pending** — walk the full MVP checklist end-to-end:
  * Country list: confirm all filters work with real data (visited, wished, planned, lived, currently living, never visited).
  * Country detail: verify related trip stops are visible and understandable; check optional fields (flag rendering, subregion, type label).
  * Catalan/UI consistency: button labels, empty states, validation messages, spacing.
  * Backup/import round-trip on device: create logs/trips/stops → export → import → confirm restore/order/derived states.
  * Persistence/restart QA: close/reopen app; verify data and derived states remain correct.
  * Wished/currently living toggle behavior: currently living clears previous correctly.
* **Country data QA pending:** Catalan names for obscure territories may need correction (they were supplied from knowledge, not a translation API). Flag emoji rendering on device for all 244 entries. Continent/subregion accuracy.
* **Country list final verification pending:** confirm continent/type optional filters if present; grouping by continent is a nice improvement if easy.
* **Country detail related trip stops section** — verify it renders correctly with real data now that 244 countries are present.
* **Known environment gotchas:**
  * Git may require safe-directory config due to Windows ownership/SID mismatch.
  * Git may print permission warning for `C:\Users\nilpo/.config/git/ignore`.
  * Use `rg` / Grep tool for search where possible.
  * Preserve user changes; do not revert unasked changes.
  * Room enforces FK constraints inside `withTransaction` — always insert parent rows before child rows when doing bulk upserts on tables with self-referencing or cross-table FKs.

## NEXT IMMEDIATE STEPS

1. **MVP QA Pass — Country List & Filters**
   Open `CountryListScreen.kt` and `CountryListViewModel.kt`. Verify all required filters are present and work with 244 real countries: visited, wished, planned, lived, currently living, never visited. Check search works. Confirm empty states.
2. **MVP QA Pass — Country Detail**
   Check related trip stops section renders, type/subregion labels display correctly, flag emoji renders, optional fields (capital, coordinates) handled gracefully.
3. **MVP QA Pass — Backup/Import Round-Trip**
   Use settings screen to export, then import. Confirm all user data (logs, trips, stops, wished, currently living) restores correctly and derived states are correct after import.
4. **Catalan/UI Consistency Pass**
   Scan all screens for inconsistent button labels, missing empty states, validation messages, and spacing issues.
5. **Update Handoff Prompt** after QA pass is complete.

## Critical Reference Logic

Country state derivation rules must remain centralized and equivalent to:

```kotlin
wished = CountryUserState.wished
currentlyLiving = CountryUserState.currentlyLiving
lived = currentlyLiving OR has LIVED log
visited = currentlyLiving OR has VISIT log OR has LIVED log OR completed/in-progress trip stop
planned = planned trip stop
neverVisited = !visited && !lived
```

Trip stop status rules:

```text
PLANNED trip stops → planned
IN_PROGRESS trip stops → visited
COMPLETED trip stops → visited
UNKNOWN trip stops → no derived country effect
```

Flexible date rules:

```text
YEAR: year only, month/day null
MONTH: year + month, day null
DAY: year + month + day
Ranges with both start/end use same precision
```

Country dataset importer insertion order rule:

```kotlin
// Must sort nulls-first to avoid FOREIGN KEY constraint on countries.parent_iso2
dataset.countries
    .sortedBy { if (it.parentIso2 == null) 0 else 1 }
    .map { it.toEntity() }
```

## Direction For Next AI Agent

Adopt the above context, inspect the current code before editing, then ask the user for permission to begin the **MVP QA Pass** starting with the country list filters.
