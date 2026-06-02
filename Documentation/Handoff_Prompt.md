# Atlas Handoff Prompt

## PROJECT OVERVIEW & STATUS

* **Current Update (2026-06-02):** MVP is complete and accepted. v2.0 Milestones 0-6 are implemented and verified with unit tests/debug build. Milestone 7 route visualization is in progress: trip maps (Canvas) include normal/itinerary/excursion stops; itinerary detail has airport route preview; **Flight Detail screen** added with individual Canvas route preview and group context airports. Room DB is version `13`. Flight datetime prefill rules implemented. Remaining Milestone 7: excursion route preview polish, final map UX pass.

* **Project Name/Goal:** Atlas, a native Android local-first personal travel atlas. MVP goal: track countries/territories, wished/visited/lived/currently-living states, manual logs, simple trips, ordered trip stops, location search/manual fallback, derived country states, and JSON backup/import.
* **Current Phase:** MVP complete and accepted. v2.0 in progress — Milestones 0–6 complete and verified on device. Milestone 7 (Route Visualization) in progress.
* **Core Tech Stack / Constraints:**
  * Kotlin, Jetpack Compose, Room, Navigation Compose, Coroutines/Flow, kotlinx.serialization, manual dependency container.
  * Android package/application id: `com.atlas`.
  * Catalan-first visible UI. English for code/class/function names and comments.
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

* **MVP scope is locked:** countries/territories, country tracking, logs, trips, stops, location search/manual fallback, JSON backup/import, basic dashboard.
* **v2.0 scope (in progress):** airports, solo flights, itineraries, itinerary groups, layover-safe country derivation, generated trip stops, excursions.
* **UI direction:** Warm Editorial Atlas — warm parchment background, paper cards with subtle borders, editorial headings, rounded filter chips, state colors as restrained accents. Applied to dashboard and countries pages. Trip page polish deferred until excursions (Milestone 6) is implemented.
* **Country detail screen direction:** Static placeholder map for now. Country state colors centralized in theme/style helpers.
* **Trip map:** Placeholder map with stops; real map later.
* **Location search:** Explicit search, not autocomplete. Uses OSM/Nominatim with manual fallback.
* **Stop modal behavior:** Search-first. API-filled fields hidden after result selection. Manual fields appear only on no result, manual entry, or edit-details action. Date/notes always visible.
* **Date picker:** Custom `FlexibleDateRangeField` exists for year/month/day precision ranges. `DateTimePickerField` exists for date+time (chained DatePicker → TimePicker). Stop date defaults to trip start date when adding a new stop.
* **Flight datetimes:** All four flight datetime fields (scheduled departure, scheduled arrival, actual departure, actual arrival) are stored as nullable ISO strings `"YYYY-MM-DDTHH:mm"`. No separate year/month/day columns.
* **Flight datetime prefill rules (not yet implemented):**
  * Scheduled arrival should prefill its date with the same date as scheduled departure when opened.
  * Actual departure should prefill with the scheduled departure datetime when opened.
  * Actual arrival should prefill with the scheduled arrival datetime when opened.
* **Flight country derivation:** Deferred to Milestone 3 (Layover-Safe Country Derivation). No country state effect from flights or itinerary groups yet.
* **Itinerary groups and solo flights:** Flights have nullable `itinerary_group_id` and `sort_order`. When `itinerary_group_id` is NULL, the flight is a solo flight. Grouped flights appear in both the Flights list and the Itinerary detail. Deleting a group sets its flights back to solo (FK `SET NULL`). Deleting an itinerary manually clears group refs from flights before deleting the itinerary.
* **Trip detail behavior:** Reorder mode hides edit/delete and shows move controls. Reorder toggle is an icon on the same line as `Parades` and `Afegeix`.
* **Trip editing:** Edit trip action lives inside trip detail, not on list cards.
* **Backup/import:** MVP backup version is `1`. Import is replace-all after confirmation. Does not yet include flights or itineraries (v2.0 backup upgrade pending — Milestone 8).
* **Country dataset:** Full 244-entry dataset sourced from RestCountries API. Version `2026.1`. Importer sorts `parent_iso2 = null` entries first to satisfy Room's self-referencing FK constraint.
* **Room DB version:** `10`. Migration chain: 1→2→3→4→5→6→7→8→9→10. All migrations live in `AtlasDatabase.kt`.
* **Migration note:** SQLite `ALTER TABLE ADD COLUMN` cannot add FK constraints. When a column with a FK is added to an existing table, the table must be dropped and recreated (copy data via INSERT INTO ... SELECT). This was necessary for migration 8→9 (flights recreated) and 9→10 (flights recreated again to add `itinerary_group_id` FK).
* **Git status:** Work in progress, not yet committed.
  * Last committed: `fee3a8f Move excursions into v2 scope`
  * Branch: `main`
  * Repo: `git@github.com:Nilpomarol/Travel-app-android.git`

## WHAT HAS BEEN ACCOMPLISHED SO FAR

### MVP (complete)
* Android project foundation: Gradle, Compose, Room, Navigation, manual DI (`AtlasAppContainer`).
* Countries/territories: `CountryEntity`, 244-entry dataset, country list/detail screens, search/filter/state badges.
* Country tracking: wished toggle, currently living (one at a time), visit/lived logs, flexible dates, `CountryStateDerivationService`.
* Country detail redesign: Warm Editorial Atlas visual direction, state-colored hero, quick actions, history list, placeholder map.
* Trips: create/edit/delete, status, flexible dates, notes. List cards show status badge, date range, stop count, first→last stop.
* Trip stops: add/edit/delete/reorder, search-first modal with Nominatim + manual fallback, optional coordinates, flexible dates.
* Backup/import: DTOs, mappers, validator, settings screen. Validates duplicate IDs, country/trip refs, flexible dates, coordinate pairs.
* Dashboard: hero card, stat cards (visited/wished, planned/lived, trips/stops), currently living card, upcoming trip card, recent activity.
* Tests: flexible date validator/formatter, country state derivation, backup validator/mappers, Nominatim mapper, airport search use case.
* Warm Editorial Atlas visual pass applied to dashboard and country pages.
* MVP QA passed. App tested on device.

### v2.0 Milestone 0 — Airport Foundation (complete)
* `AirportEntity`, `AirportDao` (search + `getById`), Room migration 6→7, schema 7.json.
* `Airport` domain model, `AirportMapper`, `AirportRepository` (search + getById), `AirportRepositoryImpl`.
* `SearchAirportsUseCase` (min 2 chars, debounced in ViewModel).
* `AirportDatasetImporter` wired into startup after country import. `AirportDto.city` is nullable; `toEntityOrNull()` skips any entry with a blank city so a bad record can never crash the app.
* Full airport dataset: 5,931 airports sourced from `Travel-app-2.0/Atlas/worker/src/data/airports.json`, converted and written as UTF-8 no-BOM. Version `2026.2`. 141 entries skipped (null id, null/unknown country, or null city).
* `AirportSearchField` reusable composable (search field + inline dropdown, used in flight editor).

### v2.0 Milestone 1 — Solo Flights (complete, not yet committed)
* `FlightEntity`: origin/destination airport FKs, status, four datetime strings (scheduled_departure_at, scheduled_arrival_at, actual_departure_at, actual_arrival_at), airline, flight_number, aircraft, notes, created_at, updated_at.
* `FlightDao`: observe all (ordered by scheduled_departure_at DESC), observe by id, upsert, upsertAll, delete, updateSortOrder, clearGroup, clearGroupsForItinerary.
* Room migration 7→8, 8→9. Schema 9 JSON auto-generated at build time.
* `Flight` domain model, `FlightMapper`, `FlightRepository`, `FlightRepositoryImpl`.
* `CreateFlightUseCase`, `UpdateFlightUseCase`, `DeleteFlightUseCase`.
* `FlightListViewModel`: manages list + editor draft, airport search with debounce, resolves IATA labels by joining with airport repository. `FlightListItemUiState` carries flight + resolved originLabel + destinationLabel.
* `FlightEditorDraftUiState`: flat datetime strings for all four fields, airport objects for origin/destination, all optional metadata. Also carries hidden `itineraryGroupId`/`sortOrder` fields preserved through edits.
* `DateTimePickerField` composable: tappable card → `DatePickerDialog` (confirm = "Següent") → 24h `TimePickerDialog` → saves as `"YYYY-MM-DDTHH:mm"`. Displays as `"1 Jun. 2024  ·  14:30"`.
* `FlightListScreen`: status filter chips, flight cards (route label, status pill, date+airline+number meta), delete confirmation dialog.
* `FlightEditorDialog`: airport search fields, status chips, four `DateTimePickerField` pickers, optional text fields.
* `FlightListRoute`, wired into `AtlasNavHost`. `Flights` ("Vols") tab added to bottom nav with `Icons.Filled.Flight`.
* Room DB version history: 7→8 created flights, 8→9 recreated flights (datetime column rename), 9→10 recreated flights (FK for itinerary_group_id).
* All unit tests passing. `assembleDebug` passing. App tested on device — launches cleanly.
* **Pending UX polish (not yet implemented):** datetime prefill rules in `FlightEditorDialog`.

### v2.0 Milestone 2 — Itineraries and Groups (complete, not yet committed)
* `ItineraryEntity`: id, title, trip_id (nullable, unique index), notes, created_at, updated_at.
* `ItineraryGroupEntity`: id, itinerary_id (FK CASCADE to itineraries), title (nullable), status (nullable), sort_order, created_at, updated_at.
* `ItineraryRelations`: `ItineraryGroupWithFlights` Room `@Relation` helper used for the detail query.
* `ItineraryDao`: observeAll, observeById, observeGroupsWithFlights (via `@Transaction` + `@Relation`), upsertItinerary, deleteItinerary, upsertGroup, upsertGroups, deleteGroup, getGroupIds, updateGroupSortOrder.
* `FlightEntity` updated: added `itinerary_group_id` (nullable FK → `itinerary_groups`, `SET_NULL`) and `sort_order` (nullable int). Room migration 9→10 drops and recreates `flights` table to add the FK constraint.
* `Itinerary` and `ItineraryGroup` domain models. `ItineraryGroup` includes `flights: List<Flight>` (sorted by `sort_order`).
* `Flight` domain model updated with `itineraryGroupId` and `sortOrder`.
* `ItineraryMapper` (entity → domain, including nested flight sorting). `FlightMapper` updated for new fields.
* `ItineraryRepository` interface + `ItineraryRepositoryImpl`. Delete operations manually clear flight group refs before deletion (since SQLite FK enforcement is not enabled).
* `FlightRepository` updated with `reorderFlightsInGroup`. `FlightRepositoryImpl` updated throughout.
* Use cases: `CreateItineraryUseCase`, `UpdateItineraryUseCase`, `DeleteItineraryUseCase`, `CreateItineraryGroupUseCase`, `UpdateItineraryGroupUseCase`, `DeleteItineraryGroupUseCase`, `ReorderItineraryGroupsUseCase`, `ReorderGroupFlightsUseCase`.
* `ItineraryListViewModel` + `ItineraryDetailViewModel`. Detail VM uses nested `combine` calls to stay within the 5-flow typed limit.
* `ItineraryListRoute`, `ItineraryDetailRoute`.
* `ItineraryListScreen`: list with create/edit/delete, `ItineraryEditorDialog`.
* `ItineraryDetailScreen`: group cards with per-group flight rows, group reorder toggle (up/down arrows), per-group flight reorder toggle, `GroupEditorDialog` (title + optional status chip), reuses `FlightEditorDialog` for grouped flight creation/editing.
* `Itineraries` ("Itineraris") tab added to bottom nav with `Icons.Filled.FlightTakeoff`. Sub-route `itineraries/{itineraryId}`.
* `AtlasAppContainer` wired with all new use cases and `ItineraryRepositoryImpl`.
* Room DB version 10. Schema 10.json auto-generated. All unit tests passing. App tested on device.

### v2.0 Milestone 3 - Layover-Safe Country Derivation (complete, not yet committed)
* `CountryStateDerivationService` accepts flights, itinerary groups, and airport country maps.
* Solo flights derive country state by destination: `COMPLETED` = visited, `PLANNED` = planned, `IN_PROGRESS`/`UNKNOWN` = no effect.
* Grouped flights do not directly derive state. Itinerary groups derive from the selected endpoint so Barcelona -> Doha -> Tokyo counts Japan, not Qatar.
* Country list, country detail, and dashboard include flight/itinerary-derived state. Country detail only shows unlinked itinerary air-travel history until itinerary-trip linking owns the trip context.

### v2.0 Milestone 4 - Link Itineraries To Trips (complete, not yet committed)
* Trip detail can link an unassigned itinerary, show the linked itinerary, open it, and unlink it.
* Itinerary detail shows the linked trip, can open it, and can unlink it.
* Editing itinerary title/notes preserves `tripId`.

### v2.0 Milestone 5 - Generated Trip Stops From Itineraries (complete, not yet committed)
* `TripStop` now supports `source`, `itineraryGroupId`, `isVisible`, and `displayTitle`; existing manual stops migrate as `MANUAL`.
* Room DB version is `11` with migration 10 -> 11 and schema 11.json.
* Linked itinerary groups generate `ITINERARY_GROUP` trip stops using the layover-safe endpoint rule.
* Generated stops refresh after linked itinerary group/flight edits and are removed on unlink/group delete/itinerary delete.
* Generated stops appear in trip detail with an `ITINERARI` badge and are not directly editable/deletable from manual stop controls.

### v2.0 Milestone 6 - Excursions (complete, not yet committed)
* Added `ExcursionEntity` and `ExcursionStopEntity`, Room migrations 11 -> 12 and 12 -> 13, and schema 13.json.
* Migration 12 -> 13 corrects the excursion date ownership so flexible dates live on `ExcursionStop`, not `Excursion`.
* Added `Excursion`, `ExcursionStop`, DAO, mapper, repository, and create/update/delete/reorder use cases.
* Trip detail can create/edit/delete/reorder excursions and create/edit/delete/reorder ordered excursion stops.
* Excursions can optionally anchor to a main trip stop and render inline in the trip stops timeline near their anchor.
* Excursion stop creation uses the same OpenStreetMap location search/manual fallback flow as normal trip stops, and excursion stops support optional flexible dates.
* Excursion and excursion-stop move controls are only shown while trip detail reorder mode is active.
* Country derivation now includes excursion stops using parent trip status.
* Country detail timeline can show excursion entries for matching countries.

### v2.0 Milestone 7 - Route Visualization / Maps (in progress, not yet committed)
* No map SDK for this slice; all previews use Compose Canvas.
* `TripMapPreview` includes normal trip stops, generated itinerary stops, and excursion stops/routes when coordinates exist. Uses warm parchment `AtlasBackground` + dot grid canvas.
* `RouteMapPreview` / `RouteMapPoint` shared component for airport-based routes (dark hero style); available but not currently used in any screen (itinerary detail route preview was removed — see below).
* **Flight Detail screen** (`FlightDetailScreen` / `FlightDetailViewModel` / `FlightDetailRoute`): full detail view for an individual flight, accessible by tapping a flight card in the list **or** a flight row in itinerary detail. Route `flights/{flightId}`. The itinerary detail route preview card was removed — the flight detail screen is now the canonical place to see a flight's route.
  * Hero: parchment background + dot grid + Canvas route drawn in status color (origin node → destination node, solid main line).
  * Group context: if the flight is part of an itinerary group, the prev flight's origin and/or next flight's destination are shown as smaller ghost nodes connected by dashed lines.
  * Cards below hero (country-detail style offset): identity (IATA codes, city names, status pill, group position), scheduled/actual dates, airline/number/aircraft, notes.
  * Edit (reuses `FlightEditorDialog`) and delete (with confirmation).
  * Flight datetime prefill rules implemented in `FlightEditorDialog`: scheduled arrival prefills date from departure; actual departure prefills full datetime from scheduled departure; actual arrival prefills from scheduled arrival.
* **M7 complete.** M7c map UX pass done: PROVISIONAL badge removed, card wrapper uses theme tokens, unused `RouteMapPreview.kt` deleted.
* **M8 Real map SDK complete (not yet committed).** MapLibre GL Android `11.11.0` (Maven Central, no API key) integrated. Tile provider: OpenFreeMap liberty style (`https://tiles.openfreemap.org/styles/liberty`) — free, no registration. `MapLibre.getInstance()` called in `AtlasApplication`. Reusable `AtlasMapView` composable (`ui/components/map/AtlasMapView.kt`) handles lifecycle.
  * `TripMapPreview` — real map with blue main-stop markers, amber generated-stop markers, purple excursion markers; separate LineLayer for main route and excursion routes; camera fits all points.
  * `FlightDetailScreen` hero — real map with status-colored origin/destination markers, solid route line; context airports (prev/next group flight) shown as smaller markers with dashed connector lines.
  * `CountryMapHero` — blank MapLibre style (inline JSON, parchment `#F4EFE6` background, no tiles/roads/labels) with state-colored circle marker and hollow capital marker. Ready for polygon layer in v7.0 without any wiring change.
  * Flight camera fits only origin→destination; context airports may be partially off-screen.
* **M9 Backup v2 complete (not yet committed).** JSON backup/import now covers all v2 data. Backup version bumped to `2`; v1 backups still import cleanly (new fields default to empty). Changes: new `AtlasBackupV2` DTO with `BackupTripStopV2` (adds source/groupId/visible/displayTitle), `BackupFlightV2`, `BackupItineraryV2`, `BackupItineraryGroupV2`, `BackupExcursionV2`, `BackupExcursionStopV2`. `BackupValidator` accepts versions 1 or 2, validates referential integrity across all entities. Import transaction inserts in FK-safe order (itineraries → groups → flights; trips → stops → excursions → excursion stops). Settings screen preview dialog shows flight/itinerary/excursion counts. Airport dataset version included in export metadata. 7 new mapper round-trip tests + 7 new validator tests all passing.
* Next: **M10 Visual polish pass** — trip, flights, itineraries pages get warm editorial atlas treatment.

## Critical Reference Logic

Country state derivation rules (centralized in `CountryStateDerivationService`):

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

Flight country derivation: **not yet implemented** — deferred to Milestone 3.

Itinerary group derivation rule (for Milestone 3):
```text
if group is not last group:
    derived place = destination airport of last flight in group

if group is last group:
    derived place = origin airport of first flight in group
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

**Current override:** Milestones 0–6 are complete. Milestone 7 is in progress (see above). Next practical work: excursion route preview polish and final map UX pass, then M8 Backup v2. Keep generated itinerary stops read-only from the manual stop editor.

Adopt the above context, inspect the current code before editing, then begin **v2.0 Milestone 3 — Layover-Safe Country Derivation** per `Atlas_v2.0_Roadmap.md`.

Key rules for Milestone 3:
1. **Solo flights**: COMPLETED → destination visited; PLANNED → destination planned; IN_PROGRESS/UNKNOWN → no effect.
2. **Grouped flights**: do not directly derive country state. Derive from the itinerary group using the group derivation rule above.
3. **Group status**: use group's own `status` field if set; if null, derive from grouped flight statuses (or treat as UNKNOWN per the simplified v2.0 rule).
4. **Extend `CountryStateDerivationService`** to accept solo flights and itinerary groups as additional inputs alongside the existing logs and trip stops.
5. Add unit tests for the layover cases (Barcelona→Doha→Tokyo counts Japan, not Qatar).
6. Do not implement Milestone 4 (Link to Trips) in the same session.
