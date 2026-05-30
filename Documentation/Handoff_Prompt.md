# Atlas Handoff Prompt

## PROJECT OVERVIEW & STATUS

* **Project Name/Goal:** Atlas, a native Android local-first personal travel atlas. MVP goal: track countries/territories, wished/visited/lived/currently-living states, manual logs, simple trips, ordered trip stops, location search/manual fallback, derived country states, and JSON backup/import.
* **Current Phase:** MVP development is functionally advanced and now entering MVP completion/polish. Core systems are implemented; remaining work is dashboard MVP pass, full country dataset, QA, and final polish.
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

## KEY DECISIONS & GROUND TRUTHS

* **MVP scope is locked:** countries/territories, country tracking, logs, trips, stops, location search/manual fallback, JSON backup/import, basic dashboard. Flights/airports/itineraries are v2.0.
* **UI direction:** Bold, visually powerful, slight gradients, country-state colors, inspired by country detail mockups. Full UI polish deferred until feature set is stable.
* **Country detail screen direction:** Static placeholder map for now; real map later. Country state colors are centralized in theme/style helpers. Flag should use SVG/image-style rendering where possible, not only emoji/icon fallback.
* **Trip map:** Placeholder map with stops exists; real map/API improvements later.
* **Location search:** Use explicit search, not autocomplete for now. Suggestions/autocomplete deferred. Search uses OSM/Nominatim with manual fallback.
* **Stop modal behavior:** Search-first. API-filled fields are hidden after result selection. Manual fields appear only on no result, manual entry, or edit-details action. Date/notes always visible.
* **Date picker:** Custom flexible date component exists for year, month-year, and day precision. Month picker is 4x3 equal-sized pills. Stop date defaults to trip start date when adding a new stop.
* **Trip detail behavior:** Reorder mode hides edit/delete buttons and shows move controls. Reorder toggle is an icon in the same line as `Parades` and `Afegeix`.
* **Trip editing:** Edit trip action lives inside trip detail, not on list cards.
* **Backup/import:** MVP backup version is `1`; import is replace-all after confirmation. Backup includes country states, country logs, trips, trip stops, dataset metadata.
* **Git status:** Work was committed and pushed to `origin/main`.
  * Commit: `1903335 Build Atlas MVP Android foundation`
  * Branch: `main`
  * Repo: `git@github.com:Nilpomarol/Travel-app-android.git`
  * Git may warn about `C:\Users\nilpo/.config/git/ignore` permission, but it did not block commit/push.

## WHAT HAS BEEN ACCOMPLISHED SO FAR

* Android project foundation restored/built and pushed:
  * Gradle setup, Compose, Room, Navigation, manual DI container.
  * App launches with `AtlasApplication`, `AtlasAppContainer`, Room database, dataset importer.
* Countries/territories foundation:
  * `CountryEntity`, dataset metadata, `countries.json` placeholder/small dataset, importer.
  * Country list/detail screens.
  * Search/filter/state badges exist or are at least partially implemented.
* Country tracking:
  * Wished toggle.
  * Currently living rule.
  * Visit/lived logs with add/edit/delete.
  * Flexible date validation/formatting.
  * Derived country states via `CountryStateDerivationService`.
* Country detail redesign:
  * Bold visual design, state-colored hero/info, quick actions, history list.
  * Static placeholder map.
  * Logs use type colors, not country state color.
  * Country detail split into smaller composables.
* Trips:
  * Trip model/entity/DAO/repository/use cases.
  * Trip list/detail routes and screens.
  * Create/edit/delete trip.
  * Status, flexible dates, notes.
  * Trip list cards now show status badge, date range, stop count, and first -> last stop.
* Trip stops:
  * TripStop entity/DAO/domain/repository/use cases.
  * Add/edit/delete stops.
  * Reorder mode.
  * Manual stop entry and country selector.
  * Optional coordinates.
  * Stop date defaults to trip start date on new stop.
  * Stop modal is search-first with manual fallback.
* Location search:
  * Nominatim search repository and DTO mapper.
  * Explicit `Cerca` search button.
  * Search result selection fills location/country/coordinates.
  * Manual fallback works when search fails/no results.
  * Error handling hardened with typed search exception.
* Trip detail:
  * Map placeholder with stops.
  * Stop cards show `MAPA`/`MANUAL` and coordinates if present.
  * Edit/delete on same line in normal mode.
  * Reorder controls only visible in reorder mode.
  * Edit trip inside detail.
  * Summary shows number of days and number of stops.
* Backup/import:
  * Backup DTOs, mappers, validator, repository, settings screen export/import.
  * Validation now checks duplicate IDs, country refs, trip refs, flexible dates, single currently living, coordinate pairs/ranges.
  * Mapper round-trip test for all trip stop backup fields.
* Tests added/passing:
  * Flexible date validator/formatter.
  * Country state derivation service, including planned/completed/in-progress/unknown trip stop rules.
  * Backup validator.
  * Backup mappers.
  * Nominatim DTO mapper.
* Last verified commands passed:
  ```powershell
  .\gradlew.bat testDebugUnitTest
  .\gradlew.bat assembleDebug
  ```

## CURRENT BOTTLENECKS & OPEN ISSUES

* **Dashboard still needs MVP pass.** It exists, but should become useful with real counts/cards: visited, wished, planned, lived, trips, stops, currently living, recent/planned trips.
* **Full countries/territories dataset not yet included.** Current dataset is small/placeholder. Full dataset should be added after dashboard pass and before final MVP QA.
* **Country dataset QA pending:** flags, Catalan names, accents/search, continents/types, detail screen missing optional fields, filters with real data.
* **Country list final verification pending:** confirm required filters exist and work: visited, wished, planned, lived, currently living, never visited.
* **Country detail final verification pending:** ensure related trip stops are visible and understandable.
* **Real-device backup/import QA pending:** create logs/trips/stops -> export -> import -> confirm restore/order/derived states.
* **Persistence/restart QA pending:** close/reopen app; verify data and derived states remain correct.
* **Catalan/UI consistency pass pending:** button labels, empty states, validation messages, spacing.
* **Known environment gotchas:**
  * Git may require safe-directory config due Windows ownership/SID mismatch.
  * Git may print permission warning for `C:\Users\nilpo/.config/git/ignore`.
  * Use `rg` for search where possible.
  * Preserve user changes; do not revert unasked changes.

## NEXT IMMEDIATE STEPS

1. **Start Dashboard MVP Pass**
   Read `DashboardViewModel.kt`, `DashboardScreen.kt`, and MVP docs. Add real dashboard cards for visited/wished/planned/lived/trips/stops/currently-living and maybe recent/planned trip. Keep it functional, not final redesign.
2. **Then Add Full Country/Territory Dataset**
   Expand `app/src/main/assets/data/countries.json` to full MVP dataset with countries plus travel-relevant territories/special regions. Preserve fields: `type`, `parent_iso2`, `is_un_member`, `is_observer_state`, `is_trackable`, continent/subregion, flags, coordinates.
3. **Then MVP Acceptance/QA Pass**
   Walk through MVP checklist: countries, filters, detail, wished/currently living, logs, trips, stops, location search/manual fallback, derivation, backup/import, restart persistence, Catalan text.

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
PLANNED trip stops -> planned
IN_PROGRESS trip stops -> visited
COMPLETED trip stops -> visited
UNKNOWN trip stops -> no derived country effect
```

Flexible date rules:

```text
YEAR: year only, month/day null
MONTH: year + month, day null
DAY: year + month + day
Ranges with both start/end use same precision
```

## Direction For Next AI Agent

Adopt the above context, inspect the current code before editing, then ask the user for permission to begin the **Dashboard MVP Pass** as the first next step.
