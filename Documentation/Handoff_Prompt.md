# Atlas Handoff Prompt

## PROJECT OVERVIEW & STATUS

* **Last updated:** 2026-06-03
* **v2.0 is complete and committed** (`b3d1896` 2026-06-02, polish `41fa56a` 2026-06-03). All milestones M0–M9 are live.
* **v3.0 M1 (Flight API) is complete and committed** (`5e07f15` 2026-06-03). Room DB is version `14`.
* **v3.0 M2 (Airlines dataset) is complete and committed** (`d1cdff7` 2026-06-03). Room DB is version `15`.
* **Current phase:** v3.0 — Flight Foundations. Next practical work: Aircraft type dataset (see §Direction below).
* **Project name/goal:** Atlas — a native Android local-first personal travel atlas. Tracks countries/territories, trips, stops, flights, itineraries, excursions, and JSON backup/restore.

---

## TECH STACK & CONSTRAINTS

* Kotlin, Jetpack Compose, Room, Navigation Compose, Coroutines/Flow, kotlinx.serialization, manual DI (`AtlasAppContainer`).
* Android package/application id: `com.atlas`.
* **Catalan-first** visible UI. English for code, class, function names, and comments.
* Local-first. No backend.
* **MapLibre GL Android 11.11.0 is used** (added in M7 — this overrides the old "no MapLibre" constraint). Tile provider: OpenFreeMap liberty style (`https://tiles.openfreemap.org/styles/liberty`). `MapLibre.getInstance()` called in `AtlasApplication`.
* No Hilt / Koin / Retrofit / osmdroid. Manual DI only. Do not add major libraries without explicit agreement.
* Flexible dates support `YEAR`, `MONTH`, `DAY` precision (`MONTH` = year + month, no day).
* Country state derivation must stay centralized in `CountryStateDerivationService`.
* Gradle wrapper on Windows:
  ```powershell
  $env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat testDebugUnitTest
  $env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat assembleDebug
  ```
* ADB path: `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe`
* Branch: `main`. Remote: `git@github.com:Nilpomarol/Travel-app-android.git`

---

## KEY DECISIONS & GROUND TRUTHS

### Data model
* **Room DB version: 15.** Migration chain: 1→2→…→14→15. All migrations live in `AtlasDatabase.kt`. SQLite cannot add FK columns via `ALTER TABLE` — those require drop-and-recreate (done for migrations 8→9, 9→10). Migration 13→14 was simple `ALTER TABLE ADD COLUMN`. Migration 14→15 creates the `airlines` table (iata PK, no FK to countries).
* **Backup version: 2.** Covers all v2 entities (trips, stops, excursions, flights, itineraries, groups). v1 backups import cleanly via defaults. The three new flight provenance columns (`fetched_from`, `external_provider`, `external_id`) are not yet included in the backup — they are operational metadata.
* **Country dataset:** 244 entries, version `2026.1`. Importer inserts `parent_iso2 = null` entries first to satisfy the self-referencing FK.
* **Airport dataset:** 5,931 airports, version `2026.2`. 141 entries skipped (null id / unknown country / null city).

### Flights
* Four datetime fields stored as nullable ISO strings `"YYYY-MM-DDTHH:mm"` (scheduled/actual × departure/arrival). No separate year/month/day columns.
* **Datetime prefill rules are implemented** in `FlightEditorDialog`: scheduled arrival prefills its date from departure; actual departure prefills full datetime from scheduled departure; actual arrival prefills from scheduled arrival.
* Solo flights are flights with `itinerary_group_id = NULL`. Grouped flights appear in both the Flights list and the Itinerary detail.
* Deleting a group sets its flights back to solo (FK `SET NULL`). Deleting an itinerary manually clears group refs before deleting.
* **Flight country derivation is implemented.** Solo flights: COMPLETED → destination visited; PLANNED → destination planned; IN_PROGRESS/UNKNOWN → no effect. Itinerary groups use the layover-safe endpoint rule (see §Critical Reference Logic).
* `FlightDetailScreen` is navigable from the flight list **and** from itinerary detail flight rows. Route: `flights/{flightId}`.
* **FlightDetailScreen hero currently uses MapLibre** (status-colored origin/destination markers, route line, group context airports as ghost nodes). This will be replaced with a Canvas polygon map in v3.0 M4.
* **Flight provenance fields** (added migration 13→14): `fetched_from TEXT NOT NULL DEFAULT 'manual'`, `external_provider TEXT`, `external_id TEXT`. These are on `FlightEntity` and `Flight` domain model; carried through `FlightEditorDraftUiState` as hidden fields; preserved on edit.
* **Status inference:** `inferFlightStatus(scheduledDepartureAt)` in `domain/util/FlightStatusInference.kt` — future→PLANNED, today→IN_PROGRESS, past→COMPLETED, null→null. Called on departure date change (new flights only) and when applying an API result.

### Flight API
* **Provider:** AeroDataBox via RapidAPI (`aerodatabox.p.rapidapi.com`). Endpoint: `GET /flights/number/{number}/{date}`.
* **Key storage:** DataStore Preferences (`atlas_prefs`). Managed via `ApiKeyRepository` / `ApiKeyPreferencesDataSource`. Exposed as `StateFlow<String>` in `SettingsViewModel`. User enters key in Settings → "Integracions" card.
* **Client:** `AeroDataBoxClient` (`data/api/`) implements `FlightApiClient` domain interface. Uses `HttpURLConnection`, same pattern as `NominatimLocationSearchRepository`. Returns `FlightApiResult` sealed class: `Success(FlightApiPrefill)`, `NotFound`, `NoApiKey`, `RateLimited`, `NetworkError(message)`.
* **Editor integration:** `FlightEditorDialog` shows an API search section (flight number + date picker + "Cerca vol" button) for **new flights only** (`flightId == null`). The section is controlled by an optional `FlightApiSearchCallbacks` parameter — passing `null` hides it entirely (used in `ItineraryDetailScreen`).
* **Airport resolution:** `AirportRepository.getAirportByIata(iata)` added (queries unique `iata` index) to resolve API-returned IATA codes to local `Airport` objects.
* **Apply flow:** tapping "Utilitza aquests resultats" resolves airports by IATA, infers status from departure date, and pre-fills all available draft fields. Provenance set to `fetchedFrom = "api"`, `externalProvider = "aerodatabox"`, `externalId = "{number}/{date}"`.

### Maps
* `AtlasMapView` composable (`ui/components/map/AtlasMapView.kt`) — lifecycle-aware MapLibre wrapper, reused across all map surfaces.
* `TripMapPreview` — MapLibre map; blue main-stop markers, amber generated itinerary-stop markers, purple excursion markers; separate LineLayer for main and excursion routes; camera fits all points.
* `CountryMapHero` — blank MapLibre parchment style (inline JSON, no tiles/roads/labels), state-colored marker and hollow capital marker. Ready for polygon layer in v4.0.
* **Planned v3.0 change:** FlightDetailScreen hero switches from MapLibre to a Canvas-drawn world polygon map (simplified continent outlines + great-circle arc). TripMapPreview keeps MapLibre.

### Trips & stops
* Location search is currently **explicit** (user taps a search button). Auto-suggest (debounced live suggestions) is planned for v3.0.
* Stop modal behavior: search-first; API-filled fields hidden after result selection; manual fields appear only on no result, manual entry, or edit-details. Date/notes always visible.
* `FlexibleDateRangeField` for year/month/day ranges. `DateTimePickerField` for date+time (chained DatePicker → TimePicker → `"YYYY-MM-DDTHH:mm"`).
* Trip stops have `source` (MANUAL / ITINERARY_GROUP), `isVisible`, `displayTitle`. Generated ITINERARY_GROUP stops are read-only from the manual stop editor.
* Reorder mode hides edit/delete and shows move controls. Toggle is an icon on the `Parades` header row.
* Excursions anchor to a trip stop (optional); they render inline near their anchor in the timeline.

### UI
* **Warm Editorial Atlas** visual direction: warm parchment `AtlasBackground`, paper cards with subtle borders (`AtlasOutline`), editorial headings, state colors as restrained accents.
* All screens use shared Atlas theme tokens (`AtlasBackground`, `AtlasSurface`, `AtlasOnSurfaceStrong`, etc.). Local color palettes have been removed from all screens.
* Bottom nav: 5 tabs — Countries, Trips, Vols (Flights), Itineraris (Itineraries), Settings.

---

## WHAT EXISTS IN THE CODEBASE

### Domain entities (Room DB v15)
`CountryEntity`, `CountryLogEntity`, `CountryUserStateEntity`, `TripEntity`, `TripStopEntity`, `AirportEntity`, `AirlineEntity`, `FlightEntity`, `ItineraryEntity`, `ItineraryGroupEntity`, `ExcursionEntity`, `ExcursionStopEntity`

### Screens and routes
* **Countries:** list, detail (state-colored hero, timeline, map hero), log editor
* **Trips:** list (status filter chips, route cards), detail (map preview, linked itinerary, stops timeline, excursions inline), stop dialog (search + manual), excursion dialog, excursion stop dialog
* **Flights:** list (status filter chips, flight cards), detail (hero map, identity/times/airline cards, edit/delete), flight editor dialog
* **Itineraries:** list, detail (groups, per-group flight rows, group/flight reorder), group editor dialog
* **Dashboard:** hero card, stat cards, currently living card, upcoming trip card, recent activity
* **Settings:** backup export/import

### Key services & use cases
* `CountryStateDerivationService` — centralizes all country state derivation
* `SearchAirportsUseCase` — min 2 chars, debounced
* `LookupFlightUseCase` — flight API lookup; checks for API key first
* `inferFlightStatus()` — pure utility in `domain/util/FlightStatusInference.kt`
* `FlexibleDateFormatter` / `FlexibleDateRangeDraftField`
* Backup: `AtlasBackupV2`, `BackupMappers`, `BackupValidation`

### New in v3.0 M1
* `data/api/AeroDataBoxClient.kt` + `AeroDataBoxFlightDto.kt`
* `data/preferences/ApiKeyPreferencesDataSource.kt`
* `domain/repository/ApiKeyRepository.kt`, `FlightApiClient.kt`
* `domain/model/FlightApiPrefill.kt`, `FlightApiResult.kt`
* `domain/usecase/flight/LookupFlightUseCase.kt`
* `domain/util/FlightStatusInference.kt`

### New in v3.0 M2
* `assets/data/airlines.json` — 101 airlines, version 2026.1
* `data/dataset/AirlineDatasetDto.kt` + `AirlineDatasetImporter.kt`
* `data/local/entity/AirlineEntity.kt`, `data/local/dao/AirlineDao.kt`, `data/local/mapper/AirlineMapper.kt`
* `domain/model/Airline.kt`, `domain/repository/AirlineRepository.kt`
* `data/repository/AirlineRepositoryImpl.kt`
* `FlightListViewModel` + `FlightDetailViewModel`: `resolvedAirlineName` resolved from IATA; shown in `FlightCard` and `FlightMetaCard`
* `presentation/flight/FlightApiSearchState.kt`

### Tests
Flexible date validator/formatter, country state derivation (including layover cases), backup validator/mappers, Nominatim mapper, airport search use case.

---

## CRITICAL REFERENCE LOGIC

### Country state derivation

```kotlin
wished          = CountryUserState.wished
currentlyLiving = CountryUserState.currentlyLiving
lived           = currentlyLiving OR has LIVED log
visited         = currentlyLiving OR has VISIT/LIVED log
                  OR completed/in-progress trip stop
                  OR COMPLETED solo flight to this country
                  OR COMPLETED itinerary group whose derived endpoint is this country
planned         = planned trip stop
                  OR PLANNED solo flight to this country
                  OR PLANNED itinerary group whose derived endpoint is this country
neverVisited    = !visited && !lived
```

### Itinerary group — layover-safe endpoint rule (implemented in `CountryStateDerivationService`)

```text
if group is NOT the last group in the itinerary:
    derived place = destination of the LAST flight in the group
if group IS the last group:
    derived place = origin of the FIRST flight in the group
```

This ensures Barcelona → Doha → Tokyo counts Japan, not Qatar.

### Flexible date rules

```text
YEAR:  year only; month/day null
MONTH: year + month; day null
DAY:   year + month + day
Ranges: both start and end use the same precision
```

### Country dataset insertion order

```kotlin
// parent_iso2 = null entries first — satisfies the self-referencing FK
dataset.countries
    .sortedBy { if (it.parentIso2 == null) 0 else 1 }
    .map { it.toEntity() }
```

---

## DIRECTION FOR NEXT AI AGENT

v2.0 is fully done. v3.0 M1 and M2 are done. Continue with **v3.0 M3 — Aircraft type dataset**.

### Completed in v3.0
1. ✅ **Flight API integration** — AeroDataBox lookup, DataStore API key, `FlightEditorDialog` search section, status inference. Room DB v14.
2. ✅ **Airlines dataset** — 101 airlines in `assets/data/airlines.json`; `AirlineEntity`/DAO/Repo/Importer; airline name resolved from `flight.airline` IATA in both ViewModels; shown in `FlightCard` and `FlightMetaCard`. Room DB v15.

### Remaining v3.0 work (in order)

3. **Aircraft type dataset** ← start here
   — `assets/data/aircraft_types.json` → `AircraftTypeEntity` → resolve display name + category in `FlightDetailScreen`. Image assets per type.

4. **Polygon/Canvas flight map** — replace MapLibre in `FlightDetailScreen` hero with Compose Canvas: simplified continent outline polygons + great-circle arc between airports. `TripMapPreview` keeps MapLibre.

5. **UTC/local flight times** — extend `FlightEntity` with four UTC counterpart columns; calculate from local time using `AirportEntity.timezone`; display toggle in `FlightDetailScreen`.

6. **Auto-suggest location search** — replace explicit search-button flow in trip stop and excursion stop dialogs with debounced live suggestions as user types.

7. **Country tracking flags** — `destination_counts_for_country_tracking` (default true) + `origin_counts_for_country_tracking` (default false) on `FlightEntity`; update `CountryStateDerivationService`.

After v3.0, the plan is:
- **v3.1** Visual redesign (trips + flights) — after all v3.0 content is in place
- **v3.2** Per-stop photos (trip stops + excursion stops)
- **v4.0** Country depth (stats dataset, stats page, real country map)

Full roadmap: `Documentation/Atlas_Post_v2.0_Roadmap.md`

**Before starting any work:** read the relevant existing files, understand the current structure, and ask if anything is unclear. Do not implement the next item until the previous one is complete and verified on device.
