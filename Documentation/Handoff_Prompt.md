# Atlas Handoff Prompt

## PROJECT OVERVIEW & STATUS

* **Last updated:** 2026-06-09 (v3.2 complete — per-stop photos + cover photos, DB v21; navbar fix; Stats Resum ✅ + Cronologia ✅ + Mapa ✅ + Països ✅ + Viatges ✅ + Vols ✅ + Insígnies ✅; list page headers standardized ✅; auto-status update ✅ already implemented)
* **v2.0 is complete and committed** (`b3d1896` 2026-06-02, polish `41fa56a` 2026-06-03). All milestones M0–M9 are live.
* **v3.0 is complete and committed.** All 7 milestones are done:
  * M1 (`5e07f15`) — Flight API integration. Room DB v14.
  * M2 (`d1cdff7`, `0cfd5a8`, `16554a8`) — Airlines dataset, logos, autocomplete. Room DB v15.
  * M3–M7 committed together — Aircraft types + tail cache (DB v17), Canvas flight map (DB unchanged), UTC fields + distance (DB v18), Auto-suggest location search, Country tracking flags (DB v19).
* **v3.1 is complete and committed.** All screens redesigned.
* **Current phase:** v3.2 **complete** (per-stop photos + cover photos, DB v21). Next phase is v4.0 (Country depth / Stats).
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
* **Room DB version: 21.** Migration chain: 1→2→…→19→20→21. All migrations live in `AtlasDatabase.kt`. SQLite cannot add FK columns via `ALTER TABLE` — those require drop-and-recreate (done for migrations 8→9, 9→10). Migration 13→14 was simple `ALTER TABLE ADD COLUMN`. Migration 14→15 creates the `airlines` table (iata PK, no FK to countries). Migration 15→16 creates the `aircraft_types` table. Migration 16→17 adds aircraft engine metadata, `aircraft_registration`, and the tail-number `aircraft` cache table. Migration 17→18 adds nullable UTC datetime columns and `distance_km` to `flights`. Migration 18→19 adds `destination_counts_for_country_tracking` (default 1) and `origin_counts_for_country_tracking` (default 0) to `flights`. Migration 19→20 creates `stop_photos` table with indices on `(stop_id, stop_type)` and `sort_order`. Migration 20→21 adds `cover_photo_filename TEXT` (nullable) to `trips`.
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
* `CountryFlag` (`ui/components/CountryFlag.kt`) — loads flag SVG from `https://flagcdn.com/{iso2_lower}.svg` via Coil + `SvgDecoder.Factory()`; falls back to `FlagIso2Fallback` (ISO2 text). Used in `CountryListScreen` and `CountryIdentityHeader`.
* `ui/components/geo/*` — reusable offline vector geo foundation. Loads bundled Natural Earth 1:110m admin-0 country polygons (`assets/geo/ne_110m_admin_0_countries.geojson`), fits a Mercator-like projection to route or world viewports, draws graticules, country polygons, great-circle arcs, and markers with Compose Canvas.
  * `AtlasGeoCanvas` — core composable. Key params: `viewport`, `routeSegments`, `markers`, `highlightColorByIso2: Map<String, Color>`, `mapPaddingDp: Float = 18f`. Highlighted countries get a tinted fill + accent stroke in their specified color.
  * `GeoViewport.World(minLatitudeDeg, maxLatitudeDeg)` — data class (not object); defaults to full Mercator world. Dashboard uses `World(minLatitudeDeg = -57.0, maxLatitudeDeg = 76.0)` to hide Antarctica.
  * `GeoMarker` has `isHollow: Boolean` and `label: String?` — hollow markers (white fill + colored stroke ring) used for capital cities; labeled markers show a rounded-rect tag above the dot.
  * `FlightRouteGeoMap` — wraps `AtlasGeoCanvas` for the flight detail hero; fits viewport to route, draws solid + dashed context arcs.
* `CountryMapHero` — **uses `AtlasGeoCanvas`** (MapLibre removed). Viewport centers on capital (falls back to country center). Target country highlighted in `style.primary`. Single solid marker at capital position with `label = country.capitalNameCa`.
* `TripMapPreview` — still uses MapLibre; blue main-stop markers, amber itinerary-stop markers, purple excursion markers.
* `DashboardScreen` world map — **uses `AtlasGeoCanvas`** full-width inside `DashboardMapHero`, `aspectRatio(2.05f)`, viewport `GeoViewport.World(minLatitudeDeg = -45.0, maxLatitudeDeg = 72.0)`, `mapPaddingDp = 6f`. Countries are solid-filled by tracking state, markers/labels are hidden, and solid highlights use a stronger neutral border so country boundaries remain visible.
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
* **TripList redesign:** `TripListScreen` uses dense status filter pills, route-led trip cards, and an opaque footer. `TripListViewModel` exposes ordered visible stop coordinates via `TripStopMapPoint`; cards render coordinate-backed routes with `AtlasGeoCanvas` and fall back to a schematic graticule route when no coordinates exist. The 50m Natural Earth asset was tested and removed; `GeoAssetLoader` is back to the 110m asset for performance.

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

### Domain entities (Room DB v21)
`CountryEntity`, `CountryLogEntity`, `CountryUserStateEntity`, `TripEntity` (+ `cover_photo_filename`), `TripStopEntity`, `AirportEntity`, `AirlineEntity`, `AircraftTypeEntity`, `AircraftEntity`, `FlightEntity`, `ItineraryEntity`, `ItineraryGroupEntity`, `ExcursionEntity`, `ExcursionStopEntity`, `StopPhotoEntity`

### Screens and routes
* **Countries:** list, detail (state-colored hero, timeline, map hero), log editor
* **Trips:** list (status filter chips, route cards), detail (map preview, linked itinerary, stops timeline, excursions inline), stop dialog (search + manual), excursion dialog, excursion stop dialog
* **Flights:** redesigned list (status filter chips, solo-flight cards, itinerary cards, airline logos, unified ordering), redesigned detail (hero map, identity/stat/horari/aircraft/dades cards, edit/delete), two-step flight editor dialog
* **Itineraries:** list (create navigates immediately to detail, delete only), detail (groups with ⋮ overflow on both group and flight cards, group/flight reorder, trip linking)
* **Dashboard:** ✅ v3.1 redesign complete — see §Dashboard design below
* **Settings:** backup export/import, API key ("Integracions")
* **Stats:** placeholder screen at route `"stats"` — shows "Estadístiques / Pròximament"

### Key services & use cases
* `CountryStateDerivationService` — centralizes all country state derivation
* `SearchAirportsUseCase` — min 2 chars, debounced
* `SearchAirlinesUseCase` — debounced LIKE search on iata + name, limit 8
* `LookupFlightUseCase` — flight API lookup; checks for API key first
* `LookupAircraftUseCase` — AeroDataBox tail-number lookup; cache-first
* `inferFlightStatus()` — pure utility in `domain/util/FlightStatusInference.kt`
* `FlightTimeCalculations.kt` — UTC-first flight sort/duration/delay/layover helpers with local fallback for incomplete rows. Public functions: `utcAwareSortKey`, `utcAwareDepartureSortKey`, `utcAwareDurationMinutes`, `utcAwareDelayMinutes`, `utcAwareDepartureDelayMinutes`, `utcAwareArrivalDelayMinutes`, `utcAwareLayoverDurationMinutesTo`, `groupDurationMinutes(firstFlight, lastFlight)`, `dayOffsetBetween(departureDatetime, arrivalDatetime)`.
* `FlexibleDateFormatter` — `format(date/range/LocalDate/LocalDateTime)` + `formatTripPill(range?)`: shared trip date pill formatter with YEAR/MONTH/DAY precision and smart range compression; used by `TripListViewModel` and `DashboardViewModel`
* `FlexibleDateRangeDraftField`
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

### New in v3.2
* `domain/model/StopType.kt` — `enum class StopType { TRIP_STOP, EXCURSION_STOP }`
* `domain/model/StopPhoto.kt` — domain model: `id, stopId, stopType, filename, sortOrder, createdAt`
* `data/local/entity/StopPhotoEntity.kt` — Room `@Entity(tableName = "stop_photos")`, indices on `(stop_id, stop_type)` and `sort_order`; no FK constraints (cross-table cascade handled manually)
* `data/local/dao/StopPhotoDao.kt` — `observeByStop`, `observeByStopIds`, `insert`, `delete`, `getByStop`, `deleteAllForStop`, `countByStop`, `getMaxSortOrder`
* `data/local/mapper/StopPhotoMapper.kt` — `StopPhotoEntity.toDomain()` / `StopPhoto.toEntity()`
* `domain/repository/StopPhotoRepository.kt` — interface: `observeByStop`, `observeByStopIds`, `addPhotos`, `deletePhoto`, `deleteAllForStop`, `countByStop`
* `data/repository/StopPhotoRepositoryImpl.kt` — copies URI → `filesDir/photos/<uuid>.jpg`, max 1920px / JPEG 80%; cascade delete deletes files then DB rows; all disk I/O on `Dispatchers.IO`
* `domain/usecase/photo/AddStopPhotosUseCase.kt` — enforces 25-photo cap (`MAX_PHOTOS = 25`)
* `domain/usecase/photo/DeleteStopPhotoUseCase.kt` — thin wrapper; deletes file + DB row
* `ui/components/StopPhotoThumbnails.kt` — 46dp thumbnail widget: 1 photo fills area; 2+ shows 2×2 grid; overflow `+N` on 4th cell
* `ui/components/StopDetailModal.kt` — `ModalBottomSheet` with header, info section, 3-column `LazyVerticalGrid` photo grid, `AddPhotoCell`, delete confirmation `AlertDialog`; internally shows `StopPhotoViewer`
* `StopPhotoViewer` (inside `StopDetailModal.kt`) — full-screen `Dialog` + `HorizontalPager`, back + delete controls, star/outline-star toggle to set/unset trip cover photo, auto-dismiss when all photos deleted
* Updated `DeleteTripStopUseCase` + `DeleteExcursionStopUseCase` — call `stopPhotoRepository.deleteAllForStop()` before deleting the stop row
* Updated `AtlasDatabase` — DB v20, `MIGRATION_19_20`, `stopPhotoDao()`
* Updated `AtlasAppContainer` — wires `StopPhotoRepositoryImpl`, `AddStopPhotosUseCase`, `DeleteStopPhotoUseCase`
* Updated `TripDetailViewModel` — `tripStopPhotosFlow` + `excursionStopPhotosFlow` via `flatMapLatest`; `photosData` intermediate combine; `onAddPhotos` / `onDeletePhoto` actions; `tripStopPhotoMap` + `excursionStopPhotoMap` in `TripDetailUiState`
* Updated `TripDetailScreen` — tappable stop cards (`Surface(onClick)`), `StopPhotoThumbnails` replacing `StopThumbnail` when photos exist, `StopDetailModal` for both stop types

**Photo file storage:** `context.filesDir/photos/<uuid>.jpg` — app-private. **Excluded from JSON backup.**

**Cascade delete:** `StopPhotoRepository.deleteAllForStop()` called by both delete-stop use cases before removing the stop row.

**`observeByStopIds` empty-list guard:** repository returns `flowOf(emptyList())` when `stopIds.isEmpty()` (DAO IN query fails on empty list).

**EXIF orientation (added post-v3.2):** `StopPhotoRepositoryImpl.compressAndSave()` opens the URI stream twice — once for `android.media.ExifInterface` to read orientation, once for `BitmapFactory.decodeStream`. Applies a `Matrix` rotation/flip before compress. Available API 24+, no new dependency needed (minSdk = 26).

### New in v3.2 (cover photos — DB v21, committed `a50ef77`)
* `domain/usecase/photo/SetTripCoverPhotoUseCase.kt` — calls `tripRepository.setCoverPhoto(tripId, photo?.filename)`; pass `null` to unset
* `TripEntity` gains `@ColumnInfo(name = "cover_photo_filename") val coverPhotoFilename: String? = null`; `Trip` domain model and `TripMapper` updated accordingly
* `TripDao` — `setCoverPhoto(tripId, filename?)` and `clearCoverPhotoByFilename(filename)` (sets column to NULL on all trips matching that filename)
* `TripRepository` / `TripRepositoryImpl` — two new methods delegating to dao
* `StopPhotoRepositoryImpl` — calls `tripDao.clearCoverPhotoByFilename(filename)` in both `deletePhoto()` and `deleteAllForStop()` so deleting a photo auto-unsets it as cover
* `TripEditorDraftUiState` — carries `coverPhotoFilename`; `fromTrip()` preserves it so trip edits via `@Upsert` never clobber the cover
* `TripListViewModel` — `TripListItemUiState` gains `coverPhotoFilename` and `datePillText`; `FlexibleDateFormatter` injected; `toListItem()` populates both
* `TripDetailViewModel` — `SetTripCoverPhotoUseCase` injected; `onSetCoverPhoto(photo?)` action added
* `StopDetailModal` — `PhotoGridCell` shows a filled star badge (16dp, `AtlasPrimary`) on the cover photo cell; `StopPhotoViewer` top bar has star/outline-star toggle button (`AtlasPrimary` / `Color.White`)
* `TripDetailScreen` — `onSetCoverPhoto` param wired through
* `TripListScreen` — card image area height 124dp → 160dp; shows `AsyncImage` (ContentScale.Crop) when `coverPhotoFilename != null`, else `TripCardMap`; conditional gradient scrim (transparent → black 55%, `fillMaxHeight(0.4f)`) only when photo is set; title color `Color.White` with photo, `AtlasOnSurfaceStrong` without
* `FlexibleDateFormatter` — `formatTripPill(range: FlexibleDateRange?): String?` added; handles YEAR/MONTH/DAY precision with smart range compression (shared by trip list and dashboard)
* `DashboardViewModel` — `DashboardTripUiState` gains `coverPhotoFilename: String? = null`; populated from `trip.coverPhotoFilename` in `toDashboardTrip()`
* `DashboardTripCards` — `RecentTripCard` shows `AsyncImage` when `coverPhotoFilename != null`, else `TripCardMap`; `AtlasPill` replaced by `TripStatePill` (solid foreground bg + white dot + white text, matching trip list style)
* `AtlasDatabase` — DB v21, `MIGRATION_20_21`
* `AtlasAppContainer` — `MIGRATION_20_21` registered; `SetTripCoverPhotoUseCase` wired; `StopPhotoRepositoryImpl` now receives `tripDao`

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

v3.0 and v3.1 are fully done. All screens redesigned.

### Completed in v3.0
Note: UTC flight fields now drive duration, delay, layover duration, and flight ordering via `FlightTimeCalculations.kt`; local datetime strings are fallback only.
1. ✅ **Flight API integration** — AeroDataBox lookup, DataStore API key, `FlightEditorDialog` search section, status inference. Room DB v14.
2. ✅ **Airlines** — dataset (101 airlines), name resolution, logos via avs.io (Coil), autocomplete in all three flight editors (FlightList, FlightDetail, ItineraryDetail). `airlineQuery`/`airlineIata` draft pattern. Room DB v15.
3. ✅ **Aircraft type dataset + tail cache** — aircraft type dataset (50 aircraft), Room import, engine metadata, flight `aircraft_registration`, AeroDataBox tail-number cache, and flight detail exact-aircraft stats. Room DB v17.
4. ✅ **Offline geo foundation + Canvas flight map** — `FlightDetailScreen` hero uses Compose Canvas with Natural Earth country polygons, fitted projection, softened route arcs, and context markers. `TripMapPreview` keeps MapLibre.
5. ✅ **UTC flight fields + distance flown** — DB v18 adds `scheduled_departure_utc`, `scheduled_arrival_utc`, `actual_departure_utc`, `actual_arrival_utc`, and `distance_km`; repository derives them on create/update; detail shows local time first with UTC secondary and distance metadata.
6. ✅ **Auto-suggest location search** — replaced explicit search-button flow in trip stop and excursion stop dialogs with debounced live suggestions (min 3 chars, 400 ms). Leading icon shows spinner while searching.
7. ✅ **Country tracking flags** — `destination_counts_for_country_tracking` (default true) + `origin_counts_for_country_tracking` (default false) on `FlightEntity` (DB v19). `CountryStateDerivationService` respects both flags for solo flights and itinerary group derived flights.

### Completed in v3.1
1. ✅ **FlightList redesign** — status filter chips, solo-flight cards with airline logos, itinerary summary cards with per-group routes, unified date-sorted ordering.
2. ✅ **FlightDetail redesign** — Canvas geo hero, identity/stat/horari/aircraft/dades card stack, Local/UTC toggle, AeroDataBox aircraft image.
3. ✅ **ItineraryDetail redesign** — route hero, stat strip, group cards with derived status, group/flight reorder, linked-trip panel with assign flow.
4. ✅ **Modal layer simplification** — itinerary/group create is immediate (no modal), itinerary/group edit removed, two-step flight editor (search → form) wired in both FlightList and ItineraryDetail.
5. ✅ **Shared FlightCard + visual polish** — `FlightCard` extracted to `ui/components/FlightCard.kt` and reused in both `FlightListScreen` and `ItineraryDetailScreen`. Delay color-coding, strikethrough scheduled times, +N day-offset label.
6. ✅ **Itinerary flight management** — Add existing solo flights to a group (`+ Vol existent` → `SoloFlightPickerDialog`). Reorder mode: `Treu` (remove from itinerary) + `Mou →` (move to another group).
7. ✅ **Flight creation modal redesign** — custom Atlas-styled dialog container, clearer search/detail step indicator, richer API search/result states, cleaned-up Horaris section.
8. ✅ **Display typography + FlightDetail Horari polish** — `AtlasSerif` now uses Fraunces; FlightDetail Horari rows show large display-font time with +N day offset.
9. ✅ **Itinerary group-card header polish** — group route label, derived status pill, and overflow/reorder controls share the top row.
10. ✅ **TripList redesign** — status filter pills, `AtlasGeoCanvas` trip card heroes, schematic fallback, date pill top-left, state pill top-right.
11. ✅ **TripDetail redesign** — floating top bar, info card, MapLibre preview with fullscreen expand, linked itinerary panel, numbered timeline with excursions inline.
12. ✅ **CountryDetail redesign** — `AtlasGeoCanvas` map hero with capital marker + label, SVG flag header, unified history timeline (trips + flights + logs), living flow with start-date prompt.
13. ✅ **Dashboard redesign** — final map/hero, upcoming cards, recent trips, and recent flights polish is complete.
14. ✅ **Settings redesign** — back-arrow header, `AtlasSectionLabel` dividers, editorial card headers (dark icon square + title + subtitle), backup/API key/dataset health cards. Settings moved from bottom nav to push-nav via dashboard logo tap. Stats tab replaces Settings in bottom nav (`BarChart` icon, route `"stats"`). `DatasetMetadataDao.getAll()` added; `SettingsViewModel` loads installed dataset versions into `SettingsUiState.datasetVersions`. `DashboardScreen.kt` split into 6 files: `DashboardScreen`, `DashboardMapHero`, `DashboardStatsCard`, `DashboardTripCards`, `DashboardFlightCards`, `DashboardShared`.

### Dashboard design (✅ complete)

**Layout top-to-bottom:**
1. **Page header** — "El teu atlas" in `headlineSmall` (Fraunces) + atlas logo icon (top-right, 34dp circle).
2. **World map** — full-width `AtlasGeoCanvas`, `aspectRatio(2.05f)`, no card/border. Viewport: `GeoViewport.World(minLatitudeDeg = -45.0, maxLatitudeDeg = 72.0)`, `mapPaddingDp = 6f`. Countries are solid-filled by tracking state; no country dots or labels on the dashboard map. Solid country fills use visible neutral borders.
3. **Hero stats** — `AtlasCard` below the map: visited count `/total` and world percentage as equal primary metrics, plus a compact 4-column KPI row for Visitats/Viscuts/Plans/Desitjats.
4. **In-progress trip card** — `Surface(onClick)` using TripList card style: `AtlasGeoCanvas` map area (124dp, trip coordinates) with date pill + state pill overlaid, Fraunces title at bottom, route + stop count in footer. Section title "En curs". Hidden when no IN_PROGRESS trip.
5. **Stats card** — `AtlasCard` with "Estadístiques" section title + "Veure tot →" link navigating to `"stats"` route. 3 rows × 2 stats (Fraunces `headlineSmall` for values): Viatges/Vols · Km volats/Aeroports · Dies viatjats/Durada mitj. — no title on the card itself.
6. **Upcoming trips** — "Propers viatges" section title + "Tots els viatges →" (switches to Trips tab). Vertical list of 2–3 PLANNED trips: left map preview, state pill, Fraunces title, date below title, route text, compact day count.
7. **Upcoming flights** — "Propers vols" section title + "Tots els vols →" (switches to Flights tab). Vertical list of 2–3 PLANNED/IN_PROGRESS flights; real flight rows render through the shared `FlightCard`.
8. **Recent trips** — "Viatges recents" section title. Horizontal scroll (224dp cards): map preview top (122dp) — replaced by `AsyncImage` (ContentScale.Crop) when the trip has a cover photo; compact date pill top-left, solid `TripStatePill` top-right (foreground bg + white dot + white text, same style as trip list), Fraunces `headlineSmall` title and country/route text in footer. Multi-month date pills omit the start year (`ABR. - FEBR. 2025`, `DES. - GEN. 2025`); year precision keeps both years (`2025 - 2026`).
9. **Recent flights** — "Vols recents" section title. Horizontal scroll (196dp compact cards): airline logo/fallback + state pill, Fraunces IATA route row, flight number/airline line, date line.

**Navigation:** All cards are clickable — trip cards → `trips/{tripId}`, solo flight cards → `flights/{flightId}`, itinerary group cards → `itineraries/{itineraryId}`. Section links switch tabs using `popUpTo(startDestinationId) + launchSingleTop` (no `saveState`/`restoreState` — tab taps always land on the tab root, not a previously saved sub-stack).

**Key UiState additions in `DashboardUiState`:**
- `worldPercentage: Float`, `hoursFlown: Double`, `uniqueAirportCount: Int`, `uniqueAirlineCount: Int`, `daysTraveled: Int`, `avgTripLengthDays: Double?`
- `highlightedCountryMarkers: List<DashboardCountryMarker>` — still carried in state, but dashboard map currently hides markers/labels.
- `DashboardTripUiState.tripId: String` — for navigation
- `DashboardFlightUiState.flightId: String?` / `itineraryId: String?` — for navigation; `originCode`, `destinationCode`, `airlineIata`, `flightNumber` for card display

**Stats page status:** `StatsRoute` / `StatsScreen` at route `"stats"` is no longer a plain "Pròximament" placeholder. All six stats tabs are complete: Resum, Cronologia, Mapa, Països, Viatges, Vols, and Insígnies (formerly Rècords).

**List page headers standardized (✅ committed `576d1c4`):** `TripListHeader`, `FlightListHeader`, and `ItineraryListHeader` now share a consistent layout — `horizontal=20dp, vertical=16dp` padding, `10dp` row spacing, `RoundedCornerShape(13dp)` button shape, `AtlasNavy/AtlasSurface` primary CTA, and `AtlasFilterPill` status pills (on Trips and Flights). `TripStatusFilterChip` in `TripListScreen` is a private wrapper around `AtlasFilterPill` and was left in place.

**Stats page header static (✅ committed `3a7470d`, tab pill margin `5cdc95b`):** `StatsHeader` and `StatsTabRow` are hoisted outside the scroll column — they stay fixed while tab content scrolls. The duplicated `if/else` (which had header+tabrow in both the Map and non-Map branches) was collapsed into a single `Column { StatsHeader → StatsTabRow → if Map / else scrollable Column }`. Header padding aligned to `16dp` vertical, subtitle changed to `labelMedium`, vertical alignment changed to `CenterVertically`. `StatsTabRow` has `8dp` bottom padding for breathing room.

### Settings design (✅ complete)

Settings is now **push-nav only** — reached by tapping the atlas logo in the dashboard header. System back returns to dashboard. No back button was added to Settings; Android back gesture handles it (the Settings redesign task added a back arrow `IconButton` in the page header).

**Layout top-to-bottom:**
1. **Page header** — `IconButton(ArrowBack)` + "Configuració" `headlineSmall`.
2. **Còpia de seguretat** section — `AtlasSectionLabel` + card: `Backup` icon header, subtitle "Format JSON · v2", description, Exporta/Importa buttons.
3. **Integracions** section — `AtlasSectionLabel` + card: `VpnKey` icon header, subtitle "AeroDataBox · RapidAPI", description, key field with show/hide toggle, Desa/Elimina buttons, "Clau configurada ✓" indicator.
4. **Estat de les dades** section — `AtlasSectionLabel` + `DatasetVersionsCard`: `Layers` icon header, "VERSIONS INSTAL·LADES" subtitle, green-dot rows (name + version). Hidden until `datasetVersions` loads. Versions read from `dataset_metadata` table via `DatasetMetadataDao.getAll()`.

**Card header pattern (`CardHeader` composable):** 40dp rounded-square (10dp) with `AtlasNavy` background + white icon (22dp) · bold `titleMedium` title · `labelSmall` muted uppercase subtitle.

### One-time data tooling
* `scripts/migrate_country_visits.py` — migrated 49 country visit logs from the old app's backup JSON directly into `atlas.db` via ADB (non-destructive INSERT OR IGNORE). Already run on 2026-06-05. Safe to re-run (idempotent).

### ✅ Completed in v3.2 — Per-stop photos + cover photos (DB v21, build verified 2026-06-08)

Both trip stops and excursion stops have full photo support. Any stop photo can be designated as the trip cover photo; it replaces the map in both the trip list cards and the dashboard recent trips cards.

Key implementation notes:
- No `StopDetailViewModel` was created; photo state is folded into `TripDetailViewModel` to stay consistent with the manual DI architecture (no Hilt).
- `combine` hit the 5-flow typed overload limit — resolved by first combining the two photo flows into a `photosData` intermediate, then combining that with the 4 existing flows.
- `observeByStopIds` with an empty list is guarded in the repository layer.
- `TripEditorDraftUiState` carries `coverPhotoFilename` so the `@Upsert` pattern on trip edit never clobbers the cover.
- Deleting a cover photo (directly or via stop cascade) auto-clears the trip's `cover_photo_filename` via `clearCoverPhotoByFilename()` in `StopPhotoRepositoryImpl`.
- EXIF orientation is corrected on import: URI opened twice (once for `ExifInterface`, once for `BitmapFactory`), then a `Matrix` rotation/flip is applied before JPEG compress.

### ✅ Completed post-v3.2 — Navbar root navigation (committed `85680a4`)

Bottom nav `onClick` no longer uses `saveState`/`restoreState`. Every tab tap navigates fresh to the tab's root screen. `launchSingleTop = true` is kept to prevent duplicate root entries when tapping the already-selected tab.

---

### Next items before v4.0 (agreed order)

#### 1. Auto-status update for trips and flights ✅ (already implemented)

`AtlasApplication.onCreate()` calls `container.refreshTravelStatusesOnStartup()`, which launches `UpdateCurrentTravelStatusesUseCase` on `Dispatchers.IO`. Room `Flow` propagates any changes to the UI automatically. No further work needed.

#### 2. Stats page from existing data ✅ (Resum + Cronologia + Mapa + Països tabs complete)

The `StatsRoute` / `StatsScreen` at route `"stats"` is no longer a placeholder. Resum, Cronologia, Mapa, and Països tabs are all complete. Remaining tabs (Viatges, Vols, Rècords) have placeholder content that may need device QA; revisit after auto-status update if gaps are found.

**Initial implementation added 2026-06-08:**
- Added `StatsViewModel` under `presentation/stats`, using only existing repository flows: countries/user states/logs, trips/stops, excursions, flights, itinerary groups, airports, and stop photos.
- Replaced the placeholder with a read-only tabbed stats surface: `Resum`, `Mapa`, `Països`, `Viatges`, `Vols`, and `Rècords`.
- No DB migration, no new data tables, no write actions, and no navigation changes were added.

**Stats polish pass (✅ Resum tab complete; ✅ Mapa tab complete):**

_Earlier polish (same session, not re-listed in detail):_
- Header simplified; tabs compact non-scrolling pills; `Mapa` tab pan/zoom; `Països`/`Viatges`/`Vols`/`Rècords` tabs populated.

**✅ Resum tab — complete**

_Badge system (`StatsViewModel.kt`):_
- `BadgeTier` enum: `BRONZE, PLATA, OR, PLATI`.
- `StatsBadge` data class: `nextGoal: String?`, `tier: BadgeTier?`, `progress: Float?`. Progress is 0–1 fraction between thresholds for tiered badges; `1f`/`0f` for binary.
- Helpers: `tieredBadge(title, value, thresholds, detailText, nextGoalText)` and `binaryBadge(title, unlocked, goal)`. 14 tiered + 14 binary badges (28 total).
- Continent names via `toCatalanContinent()` extension applied in `buildContinentStats`.
- `buildYearStats` country-count logic fixed: trip/excursion stops use `stop.dateRange?.primaryYear()` first, falling back to trip year. Standalone flights count destination only (not origin). Itinerary groups count only the last flight's destination — layover airports are excluded. `itineraryGroups: List<ItineraryGroup>` added as parameter.

_WorldHero card (`StatsScreen.kt`):_
- Single `Row`: `RingMetric` donut left; `Column(weight(1f), End)` right.
- Right column: split visited count (`AtlasVisited` number + muted `/total` + "països visitats") above; `CountryStat` row below (3 × `weight(1f)`, centered number + label, right-aligned).

_NextMilestonesCard (`StatsScreen.kt`):_
- Replaces `IdentityCard`. Shows up to 3 upcoming milestones (tiered with next goal, then locked binary).
- `titleLarge` bold title; progress text `"${detail} → ${nextGoal}"`; 5dp progress bar (tier color at 18% alpha + solid fill).
- Plata color: `Color(0xFF7B96AF)` (steel blue).

_Ritme anual (`StatsScreen.kt`):_
- `VerticalStatBar` fills width via `fillMaxWidth()` + `weight(1f)` per bar in each year column.
- Year column spacing: `spacedBy(14.dp)`. Divider line (1dp, 15% alpha) between chart and legend.
- "Veure cronologia →" link at top-right of the card (see Timeline below).

_Other Resum polish:_
- `ThickProgress`/`StackedProgressBar`: 16dp height, `RoundedCornerShape(4.dp)`.
- `MetricMosaic`: 2-col × 3-row; "Dies" → "Dies de ruta".
- `CompactMetric`: icon + value top, label bottom.

**✅ Cronologia (Timeline) — complete**

New full-screen push-nav screen accessible via "Veure cronologia →" in the Ritme anual card.

_New files:_
- `presentation/timeline/TimelineViewModel.kt` — combines trips, standalone itineraries (`tripId == null`), and country logs; groups items by year (newest first). Itinerary route labels use the same deduplication logic as `buildItineraryCodeLabel`/`buildItineraryRouteLabel` in `ItineraryDetailScreen`: iterates sorted groups, adds origin of first flight + destination of last flight per group, skips already-seen airports. Produces `codeLabel` (IATA codes) and `cityLabel` (city names; omitted if identical to code label).
- `presentation/timeline/TimelineRoute.kt` — wires ViewModel from app container.
- `ui/screens/timeline/TimelineScreen.kt` — `LazyColumn` with sticky year headers. Trip items: left accent strip (status color) + cover photo + title + status pill. Itinerary items: same accent strip pattern, `codeLabel` bold + `cityLabel` muted + date. Log items: flag emoji + country name + log type chip.

_Modified files:_
- `AtlasNavHost.kt`: `"timeline"` composable route added; `StatsRoute` receives `onTimelineClick`.
- `StatsRoute.kt`: accepts `onTimelineClick: () -> Unit`, passes to `StatsScreen`.
- `StatsScreen.kt`: `onTimelineClick` threaded to `SummaryTab` → `YearPulseChart`.

**✅ Països tab — complete (2026-06-09)**

_`CountryFlag.kt`:_
- Added `contentScale: ContentScale = ContentScale.Crop` parameter (backward-compatible default).

_`StatsScreen.kt` — new and redesigned composables:_
- `flagContentScale(iso2)` — pure helper: returns `ContentScale.Fit` for `CH`, `VA`, `NP` (non-rectangular / square flags); `ContentScale.Crop` for all others, so regular flags fill their card fully.
- `CountriesHeroCard` — top KPI card: `headlineLarge` visited count, world %, 10dp `StackedProgressBar`, 4 mini stats (`CountriesHeroStat`).
- `CountryFlagGrid` — 2-row synchronized horizontal scroll; both rows share a single `rememberScrollState()` instance so scrolling either row moves both.
- `FlagStampCard` — 120dp wide, `aspectRatio(3:2)`, full-card flag with `flagContentScale`. Gradient scrim (`Color.Transparent → AtlasNavy 85%`, 34dp tall) overlaid at the bottom. Country name uses `labelMedium` Bold coloured in the state accent colour (no separate state dot or label text).
- `ContinentProgressCard` — compact mono style: `labelMedium` (SpaceMono Bold) for continent name and visited/total counts, `labelSmall` for percentage, 16dp `StackedProgressBar`.
- `RankedCountryCard` / `CountryRankItem` — mirrors `TopAirlineBars` layout exactly: normal `AtlasCard` padding, `Column(spacedBy(12.dp))`, each item is `Row { 48×32dp flag + Column(spacedBy(5dp)) { [name · count row] + ThickProgress } }`. Bar colour follows the country's tracking state accent.

_`StatsViewModel.kt` — activity ranking and data pipeline:_
- `FlightData` now includes `itineraries: List<Itinerary>` (loaded via `observeItineraries()` added to the 3-flow `flightData` combine, making it 4 flows).
- `standaloneItineraryGroups` — computed in the ViewModel body by filtering `itineraryGroups` to exclude groups whose parent `Itinerary` has a `tripId`. This prevents double-counting: if a flight is part of a trip, the trip itself already represents that activity.
- `buildCountryRanks` activity counting rules (excursion stops removed entirely):
  - **Logs** → +1 per log
  - **Trips** → +1 per unique country per trip (multiple stops in the same country within one trip count as 1, not N)
  - **Solo flights** (no `itineraryGroupId`) → +1 per endpoint country
  - **Standalone itinerary groups** (itinerary `tripId == null`) → +1 for group origin + group destination only; intermediate layover airports skipped
- Caption in card: `"Registres · viatges · vols (sense escales)"`.
- `buildCountryRecords` extended with 3 new records: `Continent complet` (first fully covered continent), `País més viscut` (≥2 lived logs), `País amb més parades` (≥2 trip stops).

**✅ Mapa tab — complete**

_`StatsMapCanvas.kt` (new file) + `StatsMapCanvas` composable:_
- No scroll conflict — Map tab uses a separate non-scrolling `Column` branch; other tabs scroll normally.
- Fullscreen `Canvas` with Mercator projection-based zoom (not `graphicsLayer`), so marker dot size stays fixed regardless of zoom level. Max zoom 20×.
- `GeoProjection` gained `projectZoomed()` (zoom baked into projection) and `unproject()` (inverse Mercator for tap hit-testing). Both are `internal` and accessible within `:app`.
- 50m Natural Earth GeoJSON (`ne_50m_admin_0_countries.geojson`, 3 MB) loaded asynchronously and cached via `AtlasGeoAssetLoader.loadCountries50m()`.
- **Six drawable layers:** country state fills (visited/lived/living/planned/wished), completed flight arcs (solid, alpha=1), planned flight arcs (dashed, alpha=0.9), airport dots, trip stop markers (status-colored), excursion stop markers (hollow).
- `MapLayerFilters` data class with 6 booleans; `MapFilterOverlay` is a collapsible bottom-right pill that expands into a 2-column × 3-row filter grid.
- **Fixed-size markers** — drawn in `DrawScope` in dp, not affected by `userScale`.
- **Tap-to-identify** — priority order: trip stops → excursion stops → airport dots → route midpoints → country polygon (ray-casting). Shows a floating label tooltip; tooltip dismisses on tap-elsewhere or on any pan/zoom gesture.
- **Starting position** — `LaunchedEffect(countries, projection)` runs once when both GeoJSON and canvas projection are ready; finds the living country's largest polygon ring, computes bounding-box centroid, and pans+zooms to 5× centred on that point. Falls back to world view if no living country.
- **Reset button** — top-right `Surface` pill with `Icons.Filled.Refresh`; restores the personalised initial zoom/pan (not hardcoded world view).

**✅ Viatges tab — complete (2026-06-09)**

_`StatsScreen.kt` — redesigned composables:_
- `TripMonthChart` — added `clip(RoundedCornerShape(4.dp))` to each bar box, matching the `ThickProgress` style from the first Viatges card.
- `smoothLinePath(points: List<Offset>): Path` — file-level private helper; converts a list of screen-space points to a smooth Catmull-Rom cubic Bézier `Path` (each segment uses the 1/6 control-point formula).
- `TripYearChart` — replaced bar chart with a Canvas-based smooth line graph. Fills gap years between first and last active year with zeros so the line is continuous. Draws: soft area fill (AtlasPrimary 10% alpha), 2dp smooth line stroke, solid dots + hollow centre on active years, trip count labels above each dot, year labels below. Canvas uses `fillMaxWidth()` with point spacing derived from `(size.width - sidePad * 2) / (data.size - 1)` — always stretches edge-to-edge, no horizontal scroll. Single-year fallback shows a plain row with the year + trip count.
- `TripSeasonCard` — redesigned from 4-column layout to vertical list (4 rows). Each row: emoji (16sp) + full season name (`labelMedium`, muted, `weight(1f)`) + trip count (bold, tinted if > 0) + `ThickProgress`. Season-specific colours defined inline by index: Primavera `#8FD4A0` (soft green), Estiu `#F5C04A` (warm amber), Tardor `#D4845A` (terracotta), Hivern `#7BB8E8` (icy blue). Both the count number and the progress bar use the season colour.

_`StatsViewModel.kt`:_
- `buildTripRecords` — added "Primer viatge" as a 6th record (previously 5, which left an orphan row in the 2-column `RecordGrid`). Finds the trip with the earliest `dateRange.start.year` across all trips; shows the year as value and trip title as detail. No-op if no trip has a start date.

_Imports added to `StatsScreen.kt`:_ `androidx.compose.ui.graphics.Path`, `androidx.compose.ui.text.TextStyle`, `androidx.compose.ui.text.drawText`, `androidx.compose.ui.text.rememberTextMeasurer`.

**✅ Vols tab — complete (2026-06-09)**

_`GeoModels.kt`:_
- `GeoRouteSegment` gained two backward-compatible fields: `strokeWidthDp: Float = 2.2f` and `showGlow: Boolean = true`.

_`AtlasGeoCanvas.kt`:_
- `drawRouteSegment` respects `showGlow` (conditionally skips the soft outer glow draw call) and uses `segment.strokeWidthDp` for the main stroke width.

_`StatsScreen.kt` — new and redesigned composables:_
- `FlightMapCanvas` — flight arches use `strokeWidthDp = 1.2f, showGlow = false` (thin solid lines, no glow).
- `FlightHeroPanel` — moon distance replaced the hidden `labelSmall` line with a divider + emoji + `headlineSmall` value (`AtlasGold`) + label row.
- `TopAirlineBars` — airline logos enlarged from 48×28 dp to 64×36 dp.
- `TopAircraftCard` — single card with 150 dp hero image (`ContentScale.Fit`), gradient overlay showing model name + count in `headlineSmall`, per-aircraft rows with `ThickProgress`. Aircraft rows collapse name + category into one line (`"Airbus A320 · Fuselatge estret"`).
- `DelayDistributionCard` — count and percentage split into two separate fixed-width `Text`s (26 dp + 36 dp) for stable column alignment.
- `NightDayCard` — bar titles use inline `labelMedium` + `FontWeight.Bold` rows instead of `ProgressBarRow`'s `labelSmall`.
- `FlightYearChart` — Canvas smooth Catmull-Rom line chart (same pattern as `TripYearChart`), using `AtlasPlanned` color.
- `FlightScopeCard` — intercontinental/continental bars + short/medium/long-haul breakdown separated by a divider.
- `TopDelayChart` — top-5 delays as horizontal `ThickProgress` bars (`AtlasError` color) + route + delay duration label.
- `TopRoutesChart` — top-5 routes as horizontal bars + count × + distance label.
- `TopAirportsChart` — top-10 airports with country flag + IATA + city + count × + `ThickProgress`.
- `FlightsTab` — "Retards destacats", "Rutes principals", "Aeroports principals" are three separate sections using the new chart composables.

_`StatsViewModel.kt` additions:_
- `MOON_DISTANCE_KM = 384_400.0` constant; `moonLoops` derived field in `StatsUiState`.
- `nightFlightCount`, `dayFlightCount` (flights by hour of departure).
- `shortHaulCount`, `mediumHaulCount`, `longHaulCount` (by `distanceKm` thresholds: <1500, 1500–4000, >4000).
- `StatsTopDelay(route, delayMinutes, delayLabel)` data class + `topDelayStats: List<StatsTopDelay>` in `StatsUiState`.
- `buildTopDelayStats` — top-5 delayed flights sorted by delay.
- `StatsAirportRank` gained `countryIso2: String`; `buildTopAirports` populates it.
- `buildFlightRecords` — added "Aeroport principal" as 6th record (top airport IATA + city) for even pairing in `RecordGrid`.

_Aircraft duplicate fix (`AircraftTypeRepositoryImpl.kt`, committed `ce19f8c`):_
- After the two standard lookups fail, tries stripping a single leading manufacturer letter when the next character is a digit (e.g. `"B777300ER"` → `"777300ER"`). Covers the Boeing `B-prefix` shorthand convention.
- `resolveTopAircraft` in `StatsViewModel` now re-groups resolved entries by `displayName` and merges duplicates (summing `count` + `distanceKm`), so different raw spellings that resolve to the same model appear as one row.

**✅ Insígnies tab — complete (2026-06-09)**

_The `Records` tab was renamed `Badges("Insígnies")` in the `StatsTab` enum._

_`StatsScreen.kt` — new and redesigned composables:_
- `BadgesTab` — replaces `RecordsTab`. Shows `BadgeCompletionCard` + full `BadgeGrid`.
- `BadgeCompletionCard` — `headlineLarge` unlocked count + `headlineSmall` total, `ThickProgress` bar (`AtlasGold`), tier breakdown row (colored dot + "Bronze · N", "Plata · N", "Or · N", "Platí · N").
- `BadgeCard` — fixed 136 dp height removed; cards wrap content. `Arrangement.SpaceBetween` replaced with `Arrangement.spacedBy(8.dp)`. A 5 dp thin progress bar added at the bottom of every card (tier color, opacity 55% when locked, 100% when unlocked).

_`StatsViewModel.kt` — badge additions:_
- New tiered badges (3): "Models d'aeronau" [5,10,20,35], "Vols nocturns" [1,5,10,20], "Rutes úniques" [5,15,30,50].
- New binary badges (5): "Volta al món" (earthLoops ≥ 1.0), "Tots els continents" (≥7 continents), "Ultrallarg" (flight ≥ 6 000 km), "Matiner" (flight departing 00h–04h), "Grand Tour" (completed trip ≥ 14 days).
- Total badge count: **28** (14 tiered + 14 binary).
- `buildBadges` receives 7 new parameters: `uniqueRouteCount`, `aircraftTypeCount`, `nightFlightCount`, `hasUltraLongFlight`, `hasEarlyMorningFlight`, `hasLongTrip`, and the existing `visitedContinents` now drives the "Tots els continents" binary badge.

#### 3. v4.0 — Country depth / Stats dataset

See §v4.0 milestones below. Start only after the two items above are complete.

---

### Deferred: flight arrival notification

A future feature (v4.x or v5.x) to prompt the user to enter actual arrival times after a flight lands. **This is intentionally different from auto-status update** — auto-status silently moves state; the notification actively asks the user to input data.

Requires: WorkManager (new dependency), `POST_NOTIFICATIONS` permission (Android 13+), schedule/cancel job lifecycle tied to flight create/edit/delete. Should be built as a standalone feature once auto-status is in place, since auto-status is a prerequisite (the notification can check whether the flight is already COMPLETED before firing).

---

## v3.2 SPEC — Per-stop photos (✅ IMPLEMENTED)

### User experience

**Stop cards in TripDetail timeline (and excursion inline cards):**
- The existing "logo/coordinates" area of a stop card is replaced by up to 4 photo thumbnails when photos exist. If no photos, the current layout is unchanged.
- The entire stop card becomes tappable, opening the **Stop Detail Modal**.

**Stop Detail Modal** (full-height bottom sheet or dialog):
- **Header:** stop title + location name.
- **Info section:** dates, notes — read-only display. Edit button opens the existing stop editor dialog. Delete button (with confirmation) deletes the stop + all its photos.
- **"Afegeix fotos" button** — opens Android photo picker (multi-select, up to remaining capacity). No camera capture.
- **Full photo grid** — lazy 3-column grid of all photos for this stop, in insertion order.
- Tapping a photo opens the **Full-screen viewer**.

**Full-screen viewer:**
- Horizontal pager (swipe between photos).
- Trash icon deletes the current photo (confirmation snackbar / undo optional).
- Back closes the viewer and returns to the modal.

**Photo limit:** 25 per stop. The "Afegeix fotos" button is disabled / shows a message when the limit is reached.

**Excursion stops** are treated identically to trip stops throughout.

---

### Data model — DB v20

New table, simple `CREATE TABLE` migration:

```sql
CREATE TABLE stop_photos (
    id          TEXT PRIMARY KEY,   -- UUID
    stop_id     TEXT NOT NULL,
    stop_type   TEXT NOT NULL,      -- 'TRIP_STOP' | 'EXCURSION_STOP'
    filename    TEXT NOT NULL,      -- e.g. "a3f2...uuid.jpg" in filesDir/photos/
    sort_order  INTEGER NOT NULL,   -- insertion order, 0-based
    created_at  TEXT NOT NULL       -- ISO instant string
)
```

No FK to stop tables (Room can't enforce cross-table FKs with different stop types). Cascade-delete is handled in the repository layer when a stop is deleted.

**File storage:** `context.filesDir/photos/<uuid>.jpg` — app-private internal storage. **Compress on copy:** resize to max 1920px longest side, ~80% JPEG quality. A typical 5 MB phone photo becomes ~300–500 KB. Files are deleted from disk when the photo row is deleted or the parent stop is deleted.

**Backup:** Photos are **excluded from JSON backup** (document in backup spec). Future cloud sync (e.g. Cloudflare R2) is the long-term path for cross-device photo access; deferred to v4.x.

---

### New layers

| Layer | Details |
|---|---|
| `StopPhotoEntity` | `id, stop_id, stop_type, filename, sort_order, created_at` |
| `StopPhotoDao` | `observeByStop(stopId, stopType): Flow<List<StopPhotoEntity>>`, `insert`, `delete(id)`, `deleteAllForStop(stopId, stopType)` |
| `StopPhoto` domain model | mirrors entity |
| `StopPhotoRepository` (interface + impl) | `addPhotos(stopId, stopType, uris): List<StopPhoto>` — copies + compresses each URI into `filesDir/photos/`, inserts rows; `deletePhoto(photo)` — deletes file + row; `deleteAllForStop(stopId, stopType)` — cascade on stop delete |
| `AddStopPhotosUseCase` | wraps repository add, enforces 25-photo limit |
| `DeleteStopPhotoUseCase` | wraps repository delete |
| `StopDetailViewModel` | owns: full photo list, stop info for read display, `showAddPhotos`, `pendingDelete`. Reuses existing edit/delete use cases for the stop itself. |
| `StopDetailModal` composable | bottom sheet with info + grid; shared for both stop types |
| `StopPhotoGrid` composable | lazy 3-column grid, `+` button when < 25 photos |
| `StopPhotoViewer` composable | full-screen pager with delete |
| `StopPhotoThumbnails` composable | up to 4 thumbs on the stop card |

**`TripDetailUiState` stop rows** gain:
- `previewPhotos: List<StopPhoto>` — first 4, for the card thumbnails
- `photoCount: Int` — total, for the "N more" label if > 4

**Cascade delete:** `StopPhotoRepository.deleteAllForStop()` is called by `DeleteTripStopUseCase` and `DeleteExcursionStopUseCase` before deleting the stop row. File deletion + DB row deletion both happen in the repository.

**Image loading:** Coil is already in the project. Local file photos load via `AsyncImage(model = File(filesDir, "photos/${photo.filename}"))`.

**Compression:** Use Android's `BitmapFactory` + `Bitmap.compress()` — no new libraries needed.

---

### Open questions before starting
- None. Design is fully agreed. Start with DB migration + entity/DAO, then repository, then ViewModel, then UI bottom-up.

### Beyond v3.2 → v4.0+
- **v4.0** Country depth (stats dataset, full stats page, country polygon detail map — `AtlasGeoCanvas` already supports polygon highlight; just needs zoom/pan and a tighter viewport). Stats placeholder screen already exists at route `"stats"`.
- **Future:** Cloud photo sync (Cloudflare R2 or similar) to enable cross-device photo access without bundling photos into the JSON backup.

Full roadmap: `Documentation/Atlas_Post_v2.0_Roadmap.md`

**Before starting any work:** read the relevant existing files, understand the current structure, and ask if anything is unclear. Do not implement the next item until the previous one is complete and verified on device.
