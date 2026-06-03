# Atlas Handoff Prompt

## PROJECT OVERVIEW & STATUS

* **Last updated:** 2026-06-03
* **v2.0 is complete and committed** (`b3d1896` 2026-06-02, polish `41fa56a` 2026-06-03). All milestones M0–M9 are live. Room DB is version `13`.
* **Current phase:** v3.0 — Flight Foundations. Next practical work: Flight API integration (see §Direction below).
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
* **Room DB version: 13.** Migration chain: 1→2→3→4→5→6→7→8→9→10→11→12→13. All migrations live in `AtlasDatabase.kt`. SQLite cannot add FK columns via `ALTER TABLE` — those require drop-and-recreate (done for migrations 8→9, 9→10).
* **Backup version: 2.** Covers all v2 entities (trips, stops, excursions, flights, itineraries, groups). v1 backups import cleanly via defaults.
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

### Domain entities (Room DB v13)
`CountryEntity`, `CountryLogEntity`, `CountryUserStateEntity`, `TripEntity`, `TripStopEntity`, `AirportEntity`, `FlightEntity`, `ItineraryEntity`, `ItineraryGroupEntity`, `ExcursionEntity`, `ExcursionStopEntity`

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
* `FlexibleDateFormatter` / `FlexibleDateRangeDraftField`
* Backup: `AtlasBackupV2`, `BackupMappers`, `BackupValidation`

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

v2.0 is fully done. You are starting **v3.0 — Flight Foundations**. Work through the items in this order (each is a prerequisite for the next visible result):

1. **Flight API integration** ← start here
   - Flight number + date → pre-fill airline, airports, times, aircraft
   - Add to `FlightEntity`: `fetched_from TEXT NOT NULL DEFAULT 'manual'`, `external_provider TEXT`, `external_id TEXT`
   - Room migration 13→14
   - Provider: **TBD** — ask the user which API to use before starting (AeroDataBox via RapidAPI is the likely choice)
   - Graceful fallback to manual entry when not found

2. **Airlines dataset** — `assets/data/airlines.json` → `AirlineEntity` → resolve in flight list + detail + logo display

3. **Aircraft type dataset** — `assets/data/aircraft_types.json` → `AircraftTypeEntity` → resolve name, category, image in flight detail

4. **Polygon/Canvas flight map** — replace MapLibre in `FlightDetailScreen` hero with Compose Canvas: simplified continent outline polygons + great-circle arc between airports. `TripMapPreview` keeps MapLibre.

5. **UTC/local flight times** — add UTC counterpart fields to `FlightEntity`, calculate from airport timezone, display toggle in flight detail

6. **Auto-suggest location search** — replace explicit search-button flow in trip stop and excursion stop dialogs with debounced live suggestions

7. **Country tracking flags** — `destination_counts_for_country_tracking` (default true) + `origin_counts_for_country_tracking` (default false) on flights; update `CountryStateDerivationService`

After v3.0, the plan is:
- **v3.1** Visual redesign (trips + flights) — do this after all v3.0 content is in place
- **v3.2** Per-stop photos (trip stops + excursion stops)
- **v4.0** Country depth (stats dataset, stats page, real country map)

Full roadmap: `Documentation/Atlas_Post_v2.0_Roadmap.md`

**Before starting any work:** read the relevant existing files, understand the current structure, and ask if anything is unclear. Do not implement the next item until the previous one is complete and verified on device.
