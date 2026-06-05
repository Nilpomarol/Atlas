# Atlas Handoff Prompt

## PROJECT OVERVIEW & STATUS

* **Last updated:** 2026-06-05 (TripList redesign in progress/review; 110m polygon cards restored for performance)
* **v2.0 is complete and committed** (`b3d1896` 2026-06-02, polish `41fa56a` 2026-06-03). All milestones M0–M9 are live.
* **v3.0 is complete and committed.** All 7 milestones are done:
  * M1 (`5e07f15`) — Flight API integration. Room DB v14.
  * M2 (`d1cdff7`, `0cfd5a8`, `16554a8`) — Airlines dataset, logos, autocomplete. Room DB v15.
  * M3–M7 committed together — Aircraft types + tail cache (DB v17), Canvas flight map (DB unchanged), UTC fields + distance (DB v18), Auto-suggest location search, Country tracking flags (DB v19).
* **Geo canvas extended (uncommitted):** `CountryMapHero` and dashboard world map now use `AtlasGeoCanvas` (offline Canvas renderer) instead of MapLibre / `AtlasDottedCanvas`. Country detail highlights the target country polygon in its state color. Dashboard world map colors all tracked countries by state. **Both maps still need visual polish** — the country hero viewport framing, highlight contrast, and marker sizing need tuning; the dashboard world map highlight alpha and overall composition need refinement before they look production-ready.
* **Current phase:** v3.1 — Visual redesign (one screen at a time). FlightList, FlightDetail, and ItineraryDetail are complete. TripList redesign is implemented but still uncommitted and under visual/performance review. Modal layer simplified (see §Modal layer). Next: finish TripList QA, then TripDetail.
* **Project name/goal:** Atlas — a native Android local-first personal travel atlas. Tracks countries/territories, trips, stops, flights, itineraries, excursions, and JSON backup/restore.

---

## TECH STACK & CONSTRAINTS

* Kotlin, Jetpack Compose, Room, Navigation Compose, Coroutines/Flow, kotlinx.serialization, manual DI (`AtlasAppContainer`).
* Android package/application id: `com.atlas`.
* **Catalan-first** visible UI. English for code, class, function names, and comments.
* Local-first. No backend.
* **MapLibre GL Android 11.11.0 is used** (added in M7 — this overrides the old "no MapLibre" constraint). Tile provider: OpenFreeMap liberty style (`https://tiles.openfreemap.org/styles/liberty`). `MapLibre.getInstance()` called in `AtlasApplication`.
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
* **Room DB version: 19.** Migration chain: 1→2→…→18→19. All migrations live in `AtlasDatabase.kt`. SQLite cannot add FK columns via `ALTER TABLE` — those require drop-and-recreate (done for migrations 8→9, 9→10). Migration 13→14 was simple `ALTER TABLE ADD COLUMN`. Migration 14→15 creates the `airlines` table (iata PK, no FK to countries). Migration 15→16 creates the `aircraft_types` table. Migration 16→17 adds aircraft engine metadata, `aircraft_registration`, and the tail-number `aircraft` cache table. Migration 17→18 adds nullable UTC datetime columns and `distance_km` to `flights`. Migration 18→19 adds `destination_counts_for_country_tracking` (default 1) and `origin_counts_for_country_tracking` (default 0) to `flights`.
* **Backup version: 2.** Covers all v2 entities (trips, stops, excursions, flights, itineraries, groups). v1 backups import cleanly via defaults. The three new flight provenance columns (`fetched_from`, `external_provider`, `external_id`) are not yet included in the backup — they are operational metadata.
* **Country dataset:** 244 entries, version `2026.1`. Importer inserts `parent_iso2 = null` entries first to satisfy the self-referencing FK.
* **Airport dataset:** 5,931 airports, version `2026.3`. 141 entries skipped (null id / unknown country / null city).
* **Airline dataset:** 101 major airlines, version `2026.1`. `iata` is the primary key. No FK to countries (country is display metadata only).
* **Aircraft type dataset:** 50 common commercial aircraft, version `2026.3`. `code` is the primary key. Lookup supports ICAO/common code plus normalized aliases/model names. Type rows include `num_engines`, `engine_type`, and curated local `image_asset_ref` paths for aircraft model imagery.
* **Aircraft cache:** exact aircraft rows are keyed by normalized tail number in `aircraft`. AeroDataBox aircraft lookup is cache-first when the cached row already has `imageUrl`; successful cached rows without an image are refreshed when an API key is available, then preserved if refresh fails.

### Flights
* Four datetime fields stored as nullable ISO strings `"YYYY-MM-DDTHH:mm"` (scheduled/actual × departure/arrival). No separate year/month/day columns.
* **Datetime prefill rules are implemented** in `FlightEditorDialog`: scheduled arrival prefills its date from departure; actual departure prefills full datetime from scheduled departure; actual arrival prefills from scheduled arrival.
* Solo flights are flights with `itinerary_group_id = NULL`. Grouped flights appear in both the Flights list and the Itinerary detail.
* Deleting a group sets its flights back to solo (FK `SET NULL`). Deleting an itinerary manually clears group refs before deleting.
* **Flight country derivation is implemented.** Solo flights: COMPLETED → destination visited; PLANNED → destination planned; IN_PROGRESS/UNKNOWN → no effect. Itinerary groups use the layover-safe endpoint rule (see §Critical Reference Logic).
* `FlightDetailScreen` is navigable from the flight list **and** from itinerary detail flight rows. Route: `flights/{flightId}`.
* **FlightDetailScreen is redesigned in v3.1:** solo-flight style identity card, duration/distance/delay strip, larger visual Horari card with Local/UTC toggle, aircraft visual card, larger Dades card, edit/delete in detail. Horari primary values show time only in the display font; the date is a secondary line below, scheduled values are smaller/crossed when actual exists, and arrival shows `+N` day offset when the displayed arrival date differs from departure.
* **FlightDetailScreen hero uses the offline Canvas geo renderer** (Natural Earth polygons, route arc, smaller labeled endpoint markers, and group context airports as ghost nodes).
* **Flight time display rule:** airport-local time is primary by default. Flight detail has a Local/UTC segmented toggle and shows only the selected time mode. Actual times are primary when present; scheduled times appear smaller/crossed as secondary. In FlightDetail Horari rows, the primary text is `HH:mm`, the date is shown below, and arrival uses `+N` notation for later/earlier calendar-day offsets.
* **Flight duration/sorting rule:** `domain/util/FlightTimeCalculations.kt` is the shared UTC-first helper. Duration, delay, layover durations, DAO flight ordering, and first/last-flight fallback ordering prefer derived UTC fields and fall back to local `"YYYY-MM-DDTHH:mm"` strings only when UTC is missing.
* **Timezone data caveat:** UTC fields are derived on flight create/update from origin/destination airport timezones. If an airport timezone is corrected in `airports.json` after flights already exist (for example DOH/Hamad), existing DB airport rows and existing flight UTC fields can remain stale until the airport dataset is reimported and affected flights are edited/re-saved or otherwise recomputed.
* **Flight distance:** `distance_km` is a nullable derived value calculated from origin/destination airport coordinates using great-circle distance. Existing rows migrated to v18 start null until edited/recreated/imported with the new field.
* **Country tracking flags** (added migration 18→19): `destinationCountsForCountryTracking: Boolean = true` and `originCountsForCountryTracking: Boolean = false` on `FlightEntity` and `Flight`. `CountryStateDerivationService` checks these for both solo flights and the derived flight of each itinerary group. No UI yet — all flights use defaults.
* **Flight provenance fields** (added migration 13→14): `fetched_from TEXT NOT NULL DEFAULT 'manual'`, `external_provider TEXT`, `external_id TEXT`. These are on `FlightEntity` and `Flight` domain model; carried through `FlightEditorDraftUiState` as hidden fields; preserved on edit.
* **Status inference:** `inferFlightStatus(scheduledDepartureAt)` in `domain/util/FlightStatusInference.kt` — future→PLANNED, today→IN_PROGRESS, past→COMPLETED, null→null. Called on departure date change (new flights only) and when applying an API result.

### Airlines
* **`flight.airline` stores the IATA code** when a structured airline is used (selected from autocomplete or filled by API). It stores raw free text when the user types without selecting. Logo lookup and name resolution both rely on this being a valid IATA code; graceful fallback to raw text when not found.
* **Logo URL pattern:** `https://pics.avs.io/200/100/{IATA}.png` (uppercase IATA). Loaded via Coil `SubcomposeAsyncImage`. Falls back to a styled IATA monogram (`IataMonogram` composable) on error or offline. Logos shown in `FlightCard` (list) and `FlightMetaCard` (detail).
* **Name resolution:** `AirlineRepository.getAirlineByIata(iata)` returns the full name. ViewModels resolve the stored IATA to a name for display; raw text shown as-is if no match.
* **Autocomplete in `FlightEditorDialog`:** `AirlineSearchField` composable — debounced LIKE search on `iata` and `name` columns, shows logo + name in dropdown. `SearchAirlinesUseCase` wired in all three flight-editing ViewModels (FlightList, FlightDetail, ItineraryDetail).
* **`FlightEditorDraftUiState` airline fields:**
  * `airlineQuery: String` — text shown in the editor field (the resolved name, or whatever the user typed)
  * `airlineIata: String?` — structured IATA code; `null` when user typed free text without selecting a suggestion
  * Saved to DB as: `airlineIata ?: airlineQuery.trim().ifBlank { null }`
  * When opening the editor for an existing flight: stored IATA is resolved to a name via `getAirlineByIata`; if not found, raw text shown as-is
  * When API fills the form: IATA resolved to name → `airlineQuery = name, airlineIata = iata`

### Flight API
* **Provider:** AeroDataBox via RapidAPI (`aerodatabox.p.rapidapi.com`). Endpoint: `GET /flights/number/{number}/{date}`.
* **Key storage:** DataStore Preferences (`atlas_prefs`). Managed via `ApiKeyRepository` / `ApiKeyPreferencesDataSource`. Exposed as `StateFlow<String>` in `SettingsViewModel`. User enters key in Settings → "Integracions" card.
* **Client:** `AeroDataBoxClient` (`data/api/`) implements `FlightApiClient` domain interface. Uses `HttpURLConnection`, same pattern as `NominatimLocationSearchRepository`. Returns `FlightApiResult` sealed class: `Success(FlightApiPrefill)`, `NotFound`, `NoApiKey`, `RateLimited`, `NetworkError(message)`.
* **Editor integration:** see §Modal layer — the two-step `FlightEditorDialog` always shows the search step for new flights. API search is handled in both `FlightListViewModel` and `ItineraryDetailViewModel`.
* **Airport resolution:** `AirportRepository.getAirportByIata(iata)` added (queries unique `iata` index) to resolve API-returned IATA codes to local `Airport` objects.
* **Apply flow:** tapping "Utilitza aquests resultats" resolves airports by IATA, resolves airline IATA to full name, infers status from departure date, pre-fills all available draft fields, and advances to the form step. Provenance set to `fetchedFrom = "api"`, `externalProvider = "aerodatabox"`, `externalId = "{number}/{date}"`.

### Aircraft images
* AeroDataBox aircraft lookup endpoint: `GET https://aerodatabox.p.rapidapi.com/aircrafts/Reg/{registration}?withImage=true`.
* `Aircraft.imageUrl` is used first in FlightDetail when present.
* Curated local aircraft model images live under `assets/aircraft/images/` and are referenced by `aircraft_types.json` via `image_asset_ref`.
* FlightDetail aircraft image fallback order: AeroDataBox registration image → local aircraft model image → patterned placeholder.
* Current aircraft image coverage: all aircraft types in `aircraft_types.json` except `DH8D` (Dash 8 Q400), because no matching image was provided yet.

### Maps
* `AtlasMapView` composable (`ui/components/map/AtlasMapView.kt`) — lifecycle-aware MapLibre wrapper, reused across all map surfaces.
* `TripMapPreview` — MapLibre map; blue main-stop markers, amber generated itinerary-stop markers, purple excursion markers; separate LineLayer for main and excursion routes; camera fits all points.
* `ui/components/geo/*` — reusable offline vector geo foundation. Loads bundled Natural Earth 1:110m admin-0 country polygons (`assets/geo/ne_110m_admin_0_countries.geojson`), fits a Mercator-like projection to route or world viewports, draws graticules, country polygons, great-circle arcs, and markers with Compose Canvas.
  * `AtlasGeoCanvas` — core composable. Key params: `viewport`, `routeSegments`, `markers`, `highlightColorByIso2: Map<String, Color>`. Highlighted countries get a tinted fill + accent stroke in their specified color.
  * `GeoMarker` has `isHollow: Boolean` — hollow markers (white fill + colored stroke ring) are used for capital cities.
  * `FlightRouteGeoMap` — wraps `AtlasGeoCanvas` for the flight detail hero; fits viewport to route, draws solid + dashed context arcs.
* `CountryMapHero` — **uses `AtlasGeoCanvas`** (MapLibre removed). Viewport fits to country center (22°×16° minimum span). Target country highlighted in `style.primary`. Solid marker at country center, hollow marker at capital (when coordinates differ).
* `TripMapPreview` — still uses MapLibre; blue main-stop markers, amber itinerary-stop markers, purple excursion markers.
* `DashboardScreen` world map — **uses `AtlasGeoCanvas`** in world viewport. Countries colored by tracking state (living > lived > visited > planned > wished, mutually exclusive). `DashboardUiState` carries five disjoint iso2 sets; `highlightColorByIso2` is `remember`-keyed on them.
* Natural Earth source: `https://github.com/nvkelso/natural-earth-vector/blob/master/geojson/ne_110m_admin_0_countries.geojson` (public domain dataset).

### Trips & stops
* Location search uses **debounced auto-suggest** (min 3 chars, 400 ms delay) — no explicit search button. Results appear inline as the user types in both the trip stop and excursion stop dialogs.
* Stop modal behavior: search-first; API-filled fields hidden after result selection; manual fields appear only on no result, manual entry, or edit-details. Date/notes always visible.
* `FlexibleDateRangeField` for year/month/day ranges. `DateTimePickerField` for date+time (chained DatePicker → TimePicker → `"YYYY-MM-DDTHH:mm"`).
* Trip stops have `source` (MANUAL / ITINERARY_GROUP), `isVisible`, `displayTitle`. Generated ITINERARY_GROUP stops are read-only from the manual stop editor.
* Reorder mode hides edit/delete and shows move controls. Toggle is an icon on the `Parades` header row.
* Excursions anchor to a trip stop (optional); they render inline near their anchor in the timeline.

### UI
* **Warm Editorial Atlas** visual direction: warm parchment `AtlasBackground`, paper cards with subtle borders (`AtlasOutline`), editorial headings, state colors as restrained accents.
* **Typography:** `AtlasSerif` currently uses Fraunces (`res/font/fraunces_variable.ttf`) for display/headline/title text. `headlineLarge` and `headlineMedium` use Medium; `headlineSmall` and `titleLarge` use SemiBold. `AtlasSans` remains Hanken Grotesk and `AtlasMono` remains Space Mono.
* All screens use shared Atlas theme tokens (`AtlasBackground`, `AtlasSurface`, `AtlasOnSurfaceStrong`, etc.). Local color palettes have been removed from all screens.
* Bottom nav: 5 tabs — Countries, Trips, Vols (Flights), Itineraris (Itineraries), Settings.
* **Overflow menus (⋮)** are styled via `MaterialTheme` override: `surfaceContainer = AtlasSurface`, `shapes.extraSmall = RoundedCornerShape(14.dp)`, `Modifier.width(180.dp)` on the `DropdownMenu`, and a 1dp `Box` divider between items. Do not use raw `DropdownMenu`/`DropdownMenuItem` without this wrapper.
* **Shared `FlightCard` component** (`ui/components/FlightCard.kt`) — used in both the solo-flight list (`FlightListScreen`) and inside each group in `ItineraryDetailScreen`. Takes `FlightListItemUiState + onClick`. Visual rules:
  * Middle section shows **flight duration** (UTC-first via `utcAwareDurationMinutes()`), not distance.
  * **Actual times** are color-coded by delay: `AtlasVisited` green (≤ 0 min), `AtlasDelay` amber (1–44 min), `AtlasError` red (≥ 45 min). Scheduled time shown below with strikethrough when actual exists.
  * **+N day offset** shown as a small muted label to the right of the arrival time when landing is on a later calendar day than departure (e.g. `02:15 +1`). Computed from the displayed datetime pair via `dayOffsetBetween()`.
* **`ItinerarySummaryCard`** (flight list, per-group route rows) follows the same display rules: delay color on actual times, strikethrough on scheduled, +N day offset on arrival. Middle section shows **total group duration** (first departure → last arrival via `groupDurationMinutes()`); layover city names shown below the arrow as context.
* **TripList redesign (uncommitted, in review):** `TripListScreen` now uses dense status filter pills, route-led trip cards, and an opaque footer. `TripListViewModel` exposes ordered visible stop coordinates via `TripStopMapPoint`; cards render coordinate-backed routes with `AtlasGeoCanvas` and fall back to a schematic graticule route when no coordinates exist. MapLibre was tested for list cards but caused scroll lag, so list cards use the offline polygon canvas. The 50m Natural Earth asset was tested and removed; `GeoAssetLoader` is back to the 110m asset for performance. Dates sit in a small top-left pill, state pill is top-right, title is dark ink near the footer. Current user feedback: keep checking visual balance/performance on device before committing.

### Modal layer

* **Itinerary create** — immediate action, no modal. `CreateItineraryUseCase` is called with an empty title; the returned ID is emitted as a `SharedFlow<String>` navigation event; the caller navigates to that itinerary's detail. Both `ItineraryListViewModel` and `FlightListViewModel` follow this pattern.
* **Itinerary edit** — removed entirely. `Itinerary.title` and `notes` are never surfaced in the UI; the route label shown everywhere is computed from flights/groups. `ItineraryEditorDraft` class is gone.
* **Group create** — immediate action, no modal. `CreateItineraryGroupUseCase` is called with `title = null, status = null`.
* **Group edit** — removed entirely. Group status is **derived from flights at display time**: `IN_PROGRESS` > `COMPLETED` > `PLANNED`; `null` when the group is empty or all flights are UNKNOWN. `GroupEditorDraft` class is gone.
* **Flight create/edit — two-step `FlightEditorDialog`:**
  * **Step 1 (search)** — shown when `draft.flightId == null && !draft.showForm`. Contains a flight number field, date picker, "Cerca vol" button, state feedback (searching / found / not found / no key / error), and an "Entrada manual" button that jumps to step 2 blank.
  * **Step 2 (form)** — shown for all edits (`fromFlight` sets `showForm = true`) and after search resolution. Full form: airports, status chips, datetime pickers, airline autocomplete, flight number, aircraft, registration, notes. Back arrow (new flights only) in the title returns to step 1.
  * `FlightEditorDraftUiState.showForm: Boolean` (default `false`) controls the step. `onManualEntryClick` and `onApplyApiResult` set it `true`; `onBackToSearch` sets it `false` and clears `apiSearchState`.
  * **Both `FlightListViewModel` and `ItineraryDetailViewModel`** carry the full API search stack: `lookupFlightUseCase`, `lookupAircraftUseCase`, and handlers `onApiFlightNumberChanged`, `onApiSearchDateChanged`, `onSearchByFlightNumber`, `onApplyApiResult`, `onManualEntryClick`, `onBackToSearch`.
  * `FlightDetailScreen` passes stub `{}` lambdas for the search callbacks — edit always opens directly in the form step.
  * `FlightApiSearchCallbacks` data class is **removed**; all callbacks are individual parameters on `FlightEditorDialog`.
* **Group flight cards** — `ItineraryDetailScreen` uses the shared `FlightCard` component for each flight in a group. There is no edit/delete on the card itself — tapping navigates to `FlightDetailScreen` where edit/delete live. In reorder mode, `Treu` / `Mou →` / `↑ ↓` controls appear above each card (see §ItineraryDetail screen design).
* **Group card overflow** — reduced to "Elimina grup" only (no edit action).
* **Itinerary header overflow** — "Elimina" only.

### ItineraryDetail screen design (v3.1)

**Group-card header polish:** `ItineraryGroupCard` now keeps the small uppercase route label, derived status pill, and group overflow/reorder controls on the top row. The large city route title gets a separate full-width row below, so long routes have room to breathe.

Layout (top to bottom): back + overflow header → **route hero** → stat strip (Grups / Vols / Distància) → groups section → **linked-trip panel**.

**Route hero** (`ItineraryRouteHero`): two-line display — big IATA code label (`buildItineraryCodeLabel`, uses `airport.displayCode()`) in `headlineSmall 27sp`, with a smaller muted city label (`buildItineraryRouteLabel`, uses city-first `shortLabel()`) below. Subtitle hidden when it would duplicate the code label.

**Group cards** (`ItineraryGroupCard`): header row shows small `"CITY · CITY"` uppercase label + big `"City → City"` title (city names via `routeSummary()`) with derived status pill inline. Below the header, each flight is rendered using the shared `FlightCard` component — same card as the solo flight list (airline logo, flight number, status pill, big IATA codes, times with delay color + strikethrough + +N offset, duration in middle, metadata footer). Between consecutive flights, a plain centered muted `Text` shows the layover info (e.g. "Escala · Seattle · 1 h").

**Group card footer (normal mode):** `+ Vol existent` (muted, only when solo flights exist) · `+ Afegeix vol` (primary). `+ Vol existent` opens `SoloFlightPickerDialog` — lists all solo flights (those with `itineraryGroupId == null`) labeled `BCN → LHR · 2025-01-15`. Selecting assigns the flight to the group via `updateFlightUseCase`.

**Reorder mode row** (shown above each flight card when flight reorder is active): left side has `Treu` (red) + `Mou →` (muted, only when itinerary has > 1 group); right side has `↑ ↓` arrows.
- **Treu** → confirmation `AlertDialog` → on confirm, sets `itineraryGroupId = null` (flight becomes solo).
- **Mou →** → `MoveFlightToGroupDialog` — lists all other groups in the itinerary with their city-route label. Selecting moves the flight to the end of that group via `updateFlightUseCase`.

**Linked-trip panel** (`LinkedTripPanel`): when a trip is linked, shows trip title + "Desvincula" button and is tappable to navigate to that trip. When no trip is assigned, shows a CTA card ("Sense viatge assignat" + "Assigna" label) that opens `TripPickerDialog` — a simple AlertDialog listing all available trips as tappable rows. Selecting a trip calls `updateItineraryUseCase` + `syncGeneratedTripStopsForItineraryUseCase`.

**ViewModel state:** `ItineraryDetailUiState` carries `availableTrips: List<Trip>`, `showTripPicker: Boolean`, `flightDraft: FlightEditorDraftUiState`, `originSearchResults`, `destinationSearchResults`, `airlineSearchResults`, `isGroupReorderMode`, `reorderingFlightsGroupId`. The old `itineraryDraft` and `groupDraft` fields are gone.

**`ItineraryGroupRouteUiState`** (used in `FlightListScreen` itinerary summary cards): carries split `scheduledDepartureAt`, `actualDepartureAt`, `scheduledArrivalAt`, `actualArrivalAt` (not conflated), plus pre-computed `departureDelayMinutes`, `arrivalDelayMinutes` (UTC-aware, from `FlightListViewModel`), `groupDurationMinutes` (total first-dep → last-arr), and `layoverCities`. The old conflated `departureAt`/`arrivalAt` and `layoverDurationMinutes` fields are gone.

**`ItineraryDetailUiState`** extended fields: `soloFlights: List<Flight>` (reactive, filtered to `itineraryGroupId == null`), `showSoloFlightPicker: Boolean`, `soloFlightPickerGroupId: String?`, `flightPendingRemoval: Flight?` (non-null = remove-confirmation dialog open), `flightPendingMove: Flight?` (non-null = move-to-group picker open). The ViewModel uses a private `FlightActionState` data class to hold the two pending-flight values and bundles it with `flightDraft` in a nested `combine`.

---

## WHAT EXISTS IN THE CODEBASE

### Domain entities (Room DB v19)
`CountryEntity`, `CountryLogEntity`, `CountryUserStateEntity`, `TripEntity`, `TripStopEntity`, `AirportEntity`, `AirlineEntity`, `AircraftTypeEntity`, `AircraftEntity`, `FlightEntity`, `ItineraryEntity`, `ItineraryGroupEntity`, `ExcursionEntity`, `ExcursionStopEntity`

### Screens and routes
* **Countries:** list, detail (state-colored hero, timeline, map hero), log editor
* **Trips:** list (status filter chips, route cards), detail (map preview, linked itinerary, stops timeline, excursions inline), stop dialog (search + manual), excursion dialog, excursion stop dialog
* **Flights:** redesigned list (status filter chips, solo-flight cards, itinerary cards, airline logos, unified ordering), redesigned detail (hero map, identity/stat/horari/aircraft/dades cards, edit/delete), two-step flight editor dialog
* **Itineraries:** list (create navigates immediately to detail, delete only), detail (groups with ⋮ overflow on both group and flight cards, group/flight reorder, trip linking)
* **Dashboard:** real-data world map (Canvas, state-colored country polygons), stat cards, currently living card, upcoming trip card, recent activity
* **Settings:** backup export/import, API key ("Integracions")

### Key services & use cases
* `CountryStateDerivationService` — centralizes all country state derivation
* `SearchAirportsUseCase` — min 2 chars, debounced
* `SearchAirlinesUseCase` — debounced LIKE search on iata + name, limit 8
* `LookupFlightUseCase` — flight API lookup; checks for API key first
* `LookupAircraftUseCase` — AeroDataBox tail-number lookup; cache-first
* `inferFlightStatus()` — pure utility in `domain/util/FlightStatusInference.kt`
* `FlightTimeCalculations.kt` — UTC-first flight sort/duration/delay/layover helpers with local fallback for incomplete rows. Public functions: `utcAwareSortKey`, `utcAwareDepartureSortKey`, `utcAwareDurationMinutes`, `utcAwareDelayMinutes`, `utcAwareDepartureDelayMinutes`, `utcAwareArrivalDelayMinutes`, `utcAwareLayoverDurationMinutesTo`, `groupDurationMinutes(firstFlight, lastFlight)`, `dayOffsetBetween(departureDatetime, arrivalDatetime)`.
* `FlexibleDateFormatter` / `FlexibleDateRangeDraftField`
* Backup: `AtlasBackupV2`, `BackupMappers`, `BackupValidation`

### New in v3.0 M1
* `data/api/AeroDataBoxClient.kt` + `AeroDataBoxFlightDto.kt`
* `data/preferences/ApiKeyPreferencesDataSource.kt`
* `domain/repository/ApiKeyRepository.kt`, `FlightApiClient.kt`
* `domain/model/FlightApiPrefill.kt`, `FlightApiResult.kt`
* `domain/usecase/flight/LookupFlightUseCase.kt`
* `domain/util/FlightStatusInference.kt`
* `domain/util/FlightTimeCalculations.kt`

### New in v3.0 M2
* `assets/data/airlines.json` — 101 airlines, version 2026.1
* `data/dataset/AirlineDatasetDto.kt` + `AirlineDatasetImporter.kt`
* `data/local/entity/AirlineEntity.kt`, `data/local/dao/AirlineDao.kt` (getByIata + search), `data/local/mapper/AirlineMapper.kt`
* `domain/model/Airline.kt`, `domain/repository/AirlineRepository.kt` (getAirlineByIata + searchAirlines)
* `domain/usecase/airline/SearchAirlinesUseCase.kt`
* `data/repository/AirlineRepositoryImpl.kt`
* `ui/components/AirlineLogo.kt` — `SubcomposeAsyncImage` from avs.io, `IataMonogram` fallback
* `ui/components/AirlineSearchField.kt` — text field + logo+name suggestion dropdown
* `FlightEditorDraftUiState`: `airlineQuery + airlineIata` replacing `airline: String`
* `FlightListViewModel`, `FlightDetailViewModel`, `ItineraryDetailViewModel`: airline search flow, `onAirlineQueryChanged` / `onAirlineSelected`, edit-mode resolution, API-fill resolution
* `FlightCard`: airline logo + resolved name row (separate from date/number)
* `FlightMetaCard`: Companyia row shows logo + name inline

### Tests
Flexible date validator/formatter, country state derivation (including layover cases), UTC-first flight time calculations, backup validator/mappers, Nominatim mapper, airport search use case.

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

v3.0 is fully done. v3.1 modal layer is done. Continue with **v3.1 — Visual redesign of remaining screens**.

### Completed in v3.0
Note: UTC flight fields now drive duration, delay, layover duration, and flight ordering via `FlightTimeCalculations.kt`; local datetime strings are fallback only.
1. ✅ **Flight API integration** — AeroDataBox lookup, DataStore API key, `FlightEditorDialog` search section, status inference. Room DB v14.
2. ✅ **Airlines** — dataset (101 airlines), name resolution, logos via avs.io (Coil), autocomplete in all three flight editors (FlightList, FlightDetail, ItineraryDetail). `airlineQuery`/`airlineIata` draft pattern. Room DB v15.
3. ✅ **Aircraft type dataset + tail cache** — aircraft type dataset (50 aircraft), Room import, engine metadata, flight `aircraft_registration`, AeroDataBox tail-number cache, and flight detail exact-aircraft stats. Room DB v17.
4. ✅ **Offline geo foundation + Canvas flight map** — `FlightDetailScreen` hero uses Compose Canvas with Natural Earth country polygons, fitted projection, softened route arcs, and context markers. `TripMapPreview` keeps MapLibre.
5. ✅ **UTC flight fields + distance flown** — DB v18 adds `scheduled_departure_utc`, `scheduled_arrival_utc`, `actual_departure_utc`, `actual_arrival_utc`, and `distance_km`; repository derives them on create/update; detail shows local time first with UTC secondary and distance metadata.
6. ✅ **Auto-suggest location search** — replaced explicit search-button flow in trip stop and excursion stop dialogs with debounced live suggestions (min 3 chars, 400 ms). Leading icon shows spinner while searching.
7. ✅ **Country tracking flags** — `destination_counts_for_country_tracking` (default true) + `origin_counts_for_country_tracking` (default false) on `FlightEntity` (DB v19). `CountryStateDerivationService` respects both flags for solo flights and itinerary group derived flights.

### Completed in v3.1 (so far)
1. ✅ **FlightList redesign** — status filter chips, solo-flight cards with airline logos, itinerary summary cards with per-group routes, unified date-sorted ordering.
2. ✅ **FlightDetail redesign** — Canvas geo hero, identity/stat/horari/aircraft/dades card stack, Local/UTC toggle, AeroDataBox aircraft image.
3. ✅ **ItineraryDetail redesign** — route hero, stat strip, group cards with derived status, group/flight reorder, linked-trip panel with assign flow.
4. ✅ **Modal layer simplification** — itinerary/group create is immediate (no modal), itinerary/group edit removed, two-step flight editor (search → form) wired in both FlightList and ItineraryDetail.
5. ✅ **Shared FlightCard + visual polish** — `FlightCard` extracted to `ui/components/FlightCard.kt` and reused in both `FlightListScreen` (solo flights) and `ItineraryDetailScreen` (group flights). Card shows duration (not distance) in the middle. Actual times are color-coded by delay (green/amber/red). Scheduled times show with strikethrough when actual exists. Arrival time shows a `+N` day-offset label when landing crosses midnight. `ItinerarySummaryCard` per-group rows follow the same time display rules and show total group duration (not layover wait time). FlightDetail stat strip delay formatted as compact `+1h 44min`.
6. ✅ **Itinerary flight management** — Add existing solo flights to a group (`+ Vol existent` → `SoloFlightPickerDialog`). In reorder mode each flight card shows `Treu` (remove from itinerary, with confirmation) and `Mou →` (move to another group in the same itinerary, via `MoveFlightToGroupDialog`). `ItineraryDetailViewModel` now depends on `FlightRepository` to observe solo flights reactively.

7. ✅ **Flight creation modal redesign** — custom Atlas-styled dialog container, clearer search/detail step indicator, richer API search/result states, compact full-width status chips without UNKNOWN, section-level optional labels, and a cleaned-up Horaris section with full-width date/time rows. Existing two-step behavior and all ViewModel callbacks are preserved.

8. ✅ **Display typography + FlightDetail Horari polish** — `AtlasSerif` now uses Fraunces with moderated display weights; FlightDetail Horari rows show large display-font time, date as supporting text, crossed scheduled values when actual exists, and `+N` day offset on arrival.
9. ✅ **Itinerary group-card header polish** — group route label, derived status pill, and overflow/reorder controls share the top row; the large city route title now gets its own full-width row.
10. 🚧 **TripList redesign** — implemented but not committed. Uses status filter pills, route-led trip cards, coordinate-backed offline `AtlasGeoCanvas` map heroes, schematic fallback for no-coordinate trips, top-left date pill, top-right state pill, dark title, and opaque footer. MapLibre list cards and 50m polygons were tried and backed out for performance; current build uses 110m polygons. Needs final device visual/performance acceptance before commit.

### Next: v3.1 remaining screens
- **TripList QA/commit** — finish device review of the current uncommitted redesign, then commit if accepted
- **TripDetail** — map preview, linked itinerary panel, stops timeline, excursions inline
- **CountryList** — search, state filters, continent groups, compact ticked rows
- **CountryDetail** — state-colored map hero, identity card, info spec-sheet, toggles, derivation timeline
- **Dashboard** — polygon map hero, stat ledger, featured trip card, domain stats, upcoming/recent
- **Settings** — backup, API key, dataset health panels

### Beyond v3.1
- **v3.2** Per-stop photos (trip stops + excursion stops)
- **v4.0** Country depth (stats dataset, stats page, country polygon detail map — `AtlasGeoCanvas` already supports polygon highlight; just needs zoom/pan and a tighter viewport)

Full roadmap: `Documentation/Atlas_Post_v2.0_Roadmap.md`

**Before starting any work:** read the relevant existing files, understand the current structure, and ask if anything is unclear. Do not implement the next item until the previous one is complete and verified on device.
