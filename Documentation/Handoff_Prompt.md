# Atlas Handoff Prompt

## PROJECT OVERVIEW & STATUS

* **Last updated:** 2026-06-04 (v3.1 FlightList + FlightDetail redesign complete)
* **v2.0 is complete and committed** (`b3d1896` 2026-06-02, polish `41fa56a` 2026-06-03). All milestones M0â€“M9 are live.
* **v3.0 is complete and committed.** All 7 milestones are done:
  * M1 (`5e07f15`) â€” Flight API integration. Room DB v14.
  * M2 (`d1cdff7`, `0cfd5a8`, `16554a8`) â€” Airlines dataset, logos, autocomplete. Room DB v15.
  * M3â€“M7 committed together â€” Aircraft types + tail cache (DB v17), Canvas flight map (DB unchanged), UTC fields + distance (DB v18), Auto-suggest location search, Country tracking flags (DB v19).
* **Geo canvas extended (uncommitted):** `CountryMapHero` and dashboard world map now use `AtlasGeoCanvas` (offline Canvas renderer) instead of MapLibre / `AtlasDottedCanvas`. Country detail highlights the target country polygon in its state color. Dashboard world map colors all tracked countries by state. **Both maps still need visual polish** â€” the country hero viewport framing, highlight contrast, and marker sizing need tuning; the dashboard world map highlight alpha and overall composition need refinement before they look production-ready.
* **Current phase:** v3.1 - Visual redesign (one screen at a time). FlightList and FlightDetail are complete; choose the next screen before implementing more. See Direction below.
* **Project name/goal:** Atlas â€” a native Android local-first personal travel atlas. Tracks countries/territories, trips, stops, flights, itineraries, excursions, and JSON backup/restore.

---

## TECH STACK & CONSTRAINTS

* Kotlin, Jetpack Compose, Room, Navigation Compose, Coroutines/Flow, kotlinx.serialization, manual DI (`AtlasAppContainer`).
* Android package/application id: `com.atlas`.
* **Catalan-first** visible UI. English for code, class, function names, and comments.
* Local-first. No backend.
* **MapLibre GL Android 11.11.0 is used** (added in M7 â€” this overrides the old "no MapLibre" constraint). Tile provider: OpenFreeMap liberty style (`https://tiles.openfreemap.org/styles/liberty`). `MapLibre.getInstance()` called in `AtlasApplication`.
* **Coil 2.7.0** (`coil-compose`) added in M2 for async image loading + disk caching (airline logos). This is the only image-loading library; do not add another.
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
* **Room DB version: 19.** Migration chain: 1â†’2â†’â€¦â†’18â†’19. All migrations live in `AtlasDatabase.kt`. SQLite cannot add FK columns via `ALTER TABLE` â€” those require drop-and-recreate (done for migrations 8â†’9, 9â†’10). Migration 13â†’14 was simple `ALTER TABLE ADD COLUMN`. Migration 14â†’15 creates the `airlines` table (iata PK, no FK to countries). Migration 15â†’16 creates the `aircraft_types` table. Migration 16â†’17 adds aircraft engine metadata, `aircraft_registration`, and the tail-number `aircraft` cache table. Migration 17â†’18 adds nullable UTC datetime columns and `distance_km` to `flights`. Migration 18â†’19 adds `destination_counts_for_country_tracking` (default 1) and `origin_counts_for_country_tracking` (default 0) to `flights`.
* **Backup version: 2.** Covers all v2 entities (trips, stops, excursions, flights, itineraries, groups). v1 backups import cleanly via defaults. The three new flight provenance columns (`fetched_from`, `external_provider`, `external_id`) are not yet included in the backup â€” they are operational metadata.
* **Country dataset:** 244 entries, version `2026.1`. Importer inserts `parent_iso2 = null` entries first to satisfy the self-referencing FK.
* **Airport dataset:** 5,931 airports, version `2026.2`. 141 entries skipped (null id / unknown country / null city).
* **Airline dataset:** 101 major airlines, version `2026.1`. `iata` is the primary key. No FK to countries (country is display metadata only).
* **Aircraft type dataset:** 50 common commercial aircraft, version `2026.3`. `code` is the primary key. Lookup supports ICAO/common code plus normalized aliases/model names. Type rows include `num_engines`, `engine_type`, and curated local `image_asset_ref` paths for aircraft model imagery.
* **Aircraft cache:** exact aircraft rows are keyed by normalized tail number in `aircraft`. AeroDataBox aircraft lookup is cache-first when the cached row already has `imageUrl`; successful cached rows without an image are refreshed when an API key is available, then preserved if refresh fails.

### Flights
* Four datetime fields stored as nullable ISO strings `"YYYY-MM-DDTHH:mm"` (scheduled/actual Ã— departure/arrival). No separate year/month/day columns.
* **Datetime prefill rules are implemented** in `FlightEditorDialog`: scheduled arrival prefills its date from departure; actual departure prefills full datetime from scheduled departure; actual arrival prefills from scheduled arrival.
* Solo flights are flights with `itinerary_group_id = NULL`. Grouped flights appear in both the Flights list and the Itinerary detail.
* Deleting a group sets its flights back to solo (FK `SET NULL`). Deleting an itinerary manually clears group refs before deleting.
* **Flight country derivation is implemented.** Solo flights: COMPLETED â†’ destination visited; PLANNED â†’ destination planned; IN_PROGRESS/UNKNOWN â†’ no effect. Itinerary groups use the layover-safe endpoint rule (see Â§Critical Reference Logic).
* `FlightDetailScreen` is navigable from the flight list **and** from itinerary detail flight rows. Route: `flights/{flightId}`.
* **FlightDetailScreen is redesigned in v3.1:** solo-flight style identity card, duration/distance/delay strip, larger visual Horari card with Local/UTC toggle, aircraft visual card, larger Dades card, and edit/delete kept in detail only.
* **FlightDetailScreen hero uses the offline Canvas geo renderer** (Natural Earth polygons, route arc, smaller labeled endpoint markers, and group context airports as ghost nodes).
* **Flight time display rule:** airport-local time is primary by default. Flight detail has a Local/UTC segmented toggle and shows only the selected time mode. Actual times are primary when present; scheduled times appear smaller/crossed as secondary.
* **Flight distance:** `distance_km` is a nullable derived value calculated from origin/destination airport coordinates using great-circle distance. Existing rows migrated to v18 start null until edited/recreated/imported with the new field.
* **Country tracking flags** (added migration 18â†’19): `destinationCountsForCountryTracking: Boolean = true` and `originCountsForCountryTracking: Boolean = false` on `FlightEntity` and `Flight`. `CountryStateDerivationService` checks these for both solo flights and the derived flight of each itinerary group. No UI yet â€” all flights use defaults.
* **Flight provenance fields** (added migration 13â†’14): `fetched_from TEXT NOT NULL DEFAULT 'manual'`, `external_provider TEXT`, `external_id TEXT`. These are on `FlightEntity` and `Flight` domain model; carried through `FlightEditorDraftUiState` as hidden fields; preserved on edit.
* **Status inference:** `inferFlightStatus(scheduledDepartureAt)` in `domain/util/FlightStatusInference.kt` â€” futureâ†’PLANNED, todayâ†’IN_PROGRESS, pastâ†’COMPLETED, nullâ†’null. Called on departure date change (new flights only) and when applying an API result.

### Airlines
* **`flight.airline` stores the IATA code** when a structured airline is used (selected from autocomplete or filled by API). It stores raw free text when the user types without selecting. Logo lookup and name resolution both rely on this being a valid IATA code; graceful fallback to raw text when not found.
* **Logo URL pattern:** `https://pics.avs.io/200/100/{IATA}.png` (uppercase IATA). Loaded via Coil `SubcomposeAsyncImage`. Falls back to a styled IATA monogram (`IataMonogram` composable) on error or offline. Logos shown in `FlightCard` (list) and `FlightMetaCard` (detail).
* **Name resolution:** `AirlineRepository.getAirlineByIata(iata)` returns the full name. ViewModels resolve the stored IATA to a name for display; raw text shown as-is if no match.
* **Autocomplete in `FlightEditorDialog`:** `AirlineSearchField` composable â€” debounced LIKE search on `iata` and `name` columns, shows logo + name in dropdown. `SearchAirlinesUseCase` wired in all three flight-editing ViewModels (FlightList, FlightDetail, ItineraryDetail).
* **`FlightEditorDraftUiState` airline fields:**
  * `airlineQuery: String` â€” text shown in the editor field (the resolved name, or whatever the user typed)
  * `airlineIata: String?` â€” structured IATA code; `null` when user typed free text without selecting a suggestion
  * Saved to DB as: `airlineIata ?: airlineQuery.trim().ifBlank { null }`
  * When opening the editor for an existing flight: stored IATA is resolved to a name via `getAirlineByIata`; if not found, raw text shown as-is
  * When API fills the form: IATA resolved to name â†’ `airlineQuery = name, airlineIata = iata`

### Flight API
* **Provider:** AeroDataBox via RapidAPI (`aerodatabox.p.rapidapi.com`). Endpoint: `GET /flights/number/{number}/{date}`.
* **Key storage:** DataStore Preferences (`atlas_prefs`). Managed via `ApiKeyRepository` / `ApiKeyPreferencesDataSource`. Exposed as `StateFlow<String>` in `SettingsViewModel`. User enters key in Settings â†’ "Integracions" card.
* **Client:** `AeroDataBoxClient` (`data/api/`) implements `FlightApiClient` domain interface. Uses `HttpURLConnection`, same pattern as `NominatimLocationSearchRepository`. Returns `FlightApiResult` sealed class: `Success(FlightApiPrefill)`, `NotFound`, `NoApiKey`, `RateLimited`, `NetworkError(message)`.
* **Editor integration:** `FlightEditorDialog` shows an API search section (flight number + date picker + "Cerca vol" button) for **new flights only** (`flightId == null`). The section is controlled by an optional `FlightApiSearchCallbacks` parameter â€” passing `null` hides it entirely (used in `ItineraryDetailScreen`).
* **Airport resolution:** `AirportRepository.getAirportByIata(iata)` added (queries unique `iata` index) to resolve API-returned IATA codes to local `Airport` objects.
* **Apply flow:** tapping "Utilitza aquests resultats" resolves airports by IATA, resolves airline IATA to full name, infers status from departure date, and pre-fills all available draft fields. Provenance set to `fetchedFrom = "api"`, `externalProvider = "aerodatabox"`, `externalId = "{number}/{date}"`.

### Aircraft images
* AeroDataBox aircraft lookup endpoint: `GET https://aerodatabox.p.rapidapi.com/aircrafts/Reg/{registration}?withImage=true`.
* `Aircraft.imageUrl` is used first in FlightDetail when present.
* Curated local aircraft model images live under `assets/aircraft/images/` and are referenced by `aircraft_types.json` via `image_asset_ref`.
* FlightDetail aircraft image fallback order: AeroDataBox registration image -> local aircraft model image -> patterned placeholder.
* Current aircraft image coverage: all aircraft types in `aircraft_types.json` except `DH8D` (Dash 8 Q400), because no matching image was provided yet.

### Maps
* `AtlasMapView` composable (`ui/components/map/AtlasMapView.kt`) â€” lifecycle-aware MapLibre wrapper, reused across all map surfaces.
* `TripMapPreview` â€” MapLibre map; blue main-stop markers, amber generated itinerary-stop markers, purple excursion markers; separate LineLayer for main and excursion routes; camera fits all points.
* `ui/components/geo/*` â€” reusable offline vector geo foundation. Loads bundled Natural Earth 1:110m admin-0 country polygons (`assets/geo/ne_110m_admin_0_countries.geojson`), fits a Mercator-like projection to route or world viewports, draws graticules, country polygons, great-circle arcs, and markers with Compose Canvas.
  * `AtlasGeoCanvas` â€” core composable. Key params: `viewport`, `routeSegments`, `markers`, `highlightColorByIso2: Map<String, Color>`. Highlighted countries get a tinted fill + accent stroke in their specified color.
  * `GeoMarker` has `isHollow: Boolean` â€” hollow markers (white fill + colored stroke ring) are used for capital cities.
  * `FlightRouteGeoMap` â€” wraps `AtlasGeoCanvas` for the flight detail hero; fits viewport to route, draws solid + dashed context arcs.
* `CountryMapHero` â€” **uses `AtlasGeoCanvas`** (MapLibre removed). Viewport fits to country center (22Â°Ã—16Â° minimum span). Target country highlighted in `style.primary`. Solid marker at country center, hollow marker at capital (when coordinates differ).
* `TripMapPreview` â€” still uses MapLibre; blue main-stop markers, amber itinerary-stop markers, purple excursion markers.
* `DashboardScreen` world map â€” **uses `AtlasGeoCanvas`** in world viewport. Countries colored by tracking state (living > lived > visited > planned > wished, mutually exclusive). `DashboardUiState` carries five disjoint iso2 sets; `highlightColorByIso2` is `remember`-keyed on them.
* Natural Earth source: `https://github.com/nvkelso/natural-earth-vector/blob/master/geojson/ne_110m_admin_0_countries.geojson` (public domain dataset).

### Trips & stops
* Location search uses **debounced auto-suggest** (min 3 chars, 400 ms delay) â€” no explicit search button. Results appear inline as the user types in both the trip stop and excursion stop dialogs.
* Stop modal behavior: search-first; API-filled fields hidden after result selection; manual fields appear only on no result, manual entry, or edit-details. Date/notes always visible.
* `FlexibleDateRangeField` for year/month/day ranges. `DateTimePickerField` for date+time (chained DatePicker â†’ TimePicker â†’ `"YYYY-MM-DDTHH:mm"`).
* Trip stops have `source` (MANUAL / ITINERARY_GROUP), `isVisible`, `displayTitle`. Generated ITINERARY_GROUP stops are read-only from the manual stop editor.
* Reorder mode hides edit/delete and shows move controls. Toggle is an icon on the `Parades` header row.
* Excursions anchor to a trip stop (optional); they render inline near their anchor in the timeline.

### UI
* **Warm Editorial Atlas** visual direction: warm parchment `AtlasBackground`, paper cards with subtle borders (`AtlasOutline`), editorial headings, state colors as restrained accents.
* All screens use shared Atlas theme tokens (`AtlasBackground`, `AtlasSurface`, `AtlasOnSurfaceStrong`, etc.). Local color palettes have been removed from all screens.
* Bottom nav: 5 tabs â€” Countries, Trips, Vols (Flights), Itineraris (Itineraries), Settings.

---

## WHAT EXISTS IN THE CODEBASE

### Domain entities (Room DB v19)
`CountryEntity`, `CountryLogEntity`, `CountryUserStateEntity`, `TripEntity`, `TripStopEntity`, `AirportEntity`, `AirlineEntity`, `AircraftTypeEntity`, `AircraftEntity`, `FlightEntity`, `ItineraryEntity`, `ItineraryGroupEntity`, `ExcursionEntity`, `ExcursionStopEntity`

### Screens and routes
* **Countries:** list, detail (state-colored hero, timeline, map hero), log editor
* **Trips:** list (status filter chips, route cards), detail (map preview, linked itinerary, stops timeline, excursions inline), stop dialog (search + manual), excursion dialog, excursion stop dialog
* **Flights:** redesigned list (status filter chips, solo-flight cards, itinerary cards, airline logos, unified ordering), redesigned detail (hero map, identity/stat/horari/aircraft/dades cards, edit/delete), flight editor dialog (with airline autocomplete)
* **Itineraries:** list, detail (groups, per-group flight rows, group/flight reorder), group editor dialog (with airline autocomplete)
* **Dashboard:** real-data world map (Canvas, state-colored country polygons), stat cards, currently living card, upcoming trip card, recent activity
* **Settings:** backup export/import, API key ("Integracions")

### Key services & use cases
* `CountryStateDerivationService` â€” centralizes all country state derivation
* `SearchAirportsUseCase` â€” min 2 chars, debounced
* `SearchAirlinesUseCase` â€” debounced LIKE search on iata + name, limit 8
* `LookupFlightUseCase` â€” flight API lookup; checks for API key first
* `inferFlightStatus()` â€” pure utility in `domain/util/FlightStatusInference.kt`
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
* `assets/data/airlines.json` â€” 101 airlines, version 2026.1
* `data/dataset/AirlineDatasetDto.kt` + `AirlineDatasetImporter.kt`
* `data/local/entity/AirlineEntity.kt`, `data/local/dao/AirlineDao.kt` (getByIata + search), `data/local/mapper/AirlineMapper.kt`
* `domain/model/Airline.kt`, `domain/repository/AirlineRepository.kt` (getAirlineByIata + searchAirlines)
* `domain/usecase/airline/SearchAirlinesUseCase.kt`
* `data/repository/AirlineRepositoryImpl.kt`
* `ui/components/AirlineLogo.kt` â€” `SubcomposeAsyncImage` from avs.io, `IataMonogram` fallback
* `ui/components/AirlineSearchField.kt` â€” text field + logo+name suggestion dropdown
* `FlightEditorDraftUiState`: `airlineQuery + airlineIata` replacing `airline: String`
* `FlightListViewModel`, `FlightDetailViewModel`, `ItineraryDetailViewModel`: airline search flow, `onAirlineQueryChanged` / `onAirlineSelected`, edit-mode resolution, API-fill resolution
* `FlightCard`: airline logo + resolved name row (separate from date/number)
* `FlightMetaCard`: Companyia row shows logo + name inline

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

### Itinerary group â€” layover-safe endpoint rule (implemented in `CountryStateDerivationService`)

```text
if group is NOT the last group in the itinerary:
    derived place = destination of the LAST flight in the group
if group IS the last group:
    derived place = origin of the FIRST flight in the group
```

This ensures Barcelona â†’ Doha â†’ Tokyo counts Japan, not Qatar.

### Flexible date rules

```text
YEAR:  year only; month/day null
MONTH: year + month; day null
DAY:   year + month + day
Ranges: both start and end use the same precision
```

### Country dataset insertion order

```kotlin
// parent_iso2 = null entries first â€” satisfies the self-referencing FK
dataset.countries
    .sortedBy { if (it.parentIso2 == null) 0 else 1 }
    .map { it.toEntity() }
```

---

## DIRECTION FOR NEXT AI AGENT

v3.0 is fully done. Continue with **v3.1 â€” Visual redesign (trips + flights)**.

### Completed in v3.0
1. âœ… **Flight API integration** â€” AeroDataBox lookup, DataStore API key, `FlightEditorDialog` search section, status inference. Room DB v14.
2. âœ… **Airlines** â€” dataset (101 airlines), name resolution, logos via avs.io (Coil), autocomplete in all three flight editors (FlightList, FlightDetail, ItineraryDetail). `airlineQuery`/`airlineIata` draft pattern. Room DB v15.
3. âœ… **Aircraft type dataset + tail cache** â€” aircraft type dataset (50 aircraft), Room import, engine metadata, flight `aircraft_registration`, AeroDataBox tail-number cache, and flight detail exact-aircraft stats. Room DB v17.
4. âœ… **Offline geo foundation + Canvas flight map** â€” `FlightDetailScreen` hero uses Compose Canvas with Natural Earth country polygons, fitted projection, softened route arcs, and context markers. `TripMapPreview` keeps MapLibre.
5. âœ… **UTC flight fields + distance flown** â€” DB v18 adds `scheduled_departure_utc`, `scheduled_arrival_utc`, `actual_departure_utc`, `actual_arrival_utc`, and `distance_km`; repository derives them on create/update; detail shows local time first with UTC secondary and distance metadata.
6. âœ… **Auto-suggest location search** â€” replaced explicit search-button flow in trip stop and excursion stop dialogs with debounced live suggestions (min 3 chars, 400 ms). Leading icon shows spinner while searching.
7. âœ… **Country tracking flags** â€” `destination_counts_for_country_tracking` (default true) + `origin_counts_for_country_tracking` (default false) on `FlightEntity` (DB v19). `CountryStateDerivationService` respects both flags for solo flights and itinerary group derived flights.

### Next: v3.1 and beyond

- **v3.1** Visual redesign, one screen at a time. **FlightList is complete**: redesigned solo-flight cards, redesigned itinerary cards, unified ordering, no edit/delete on list cards, app-wide date format `31 gen. 2026`, status filters without `Desconegut`, non-scrolling full-width filters, selected filter colors by state. **FlightDetail is complete**: redesigned identity/stat/horari/aircraft/dades cards, Local/UTC toggle, delay treatment, offline hero map cleanup, aircraft registration images, and curated local model-image fallback dataset. Ask the user which screen is next before implementing.
- **v3.2** Per-stop photos (trip stops + excursion stops)
- **v4.0** Country depth (stats dataset, stats page, country polygon detail map â€” `AtlasGeoCanvas` already supports polygon highlight; just needs zoom/pan and a tighter viewport)

Full roadmap: `Documentation/Atlas_Post_v2.0_Roadmap.md`

**Before starting any work:** read the relevant existing files, understand the current structure, and ask if anything is unclear. Do not implement the next item until the previous one is complete and verified on device.
