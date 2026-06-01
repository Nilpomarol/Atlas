# Atlas MVP Roadmap

## 0. Document Purpose

This document defines the detailed implementation roadmap for the **Atlas MVP**.

The MVP is the first usable version of Atlas. It focuses on:

```text
countries/territories
country tracking
simple trips
trip stops
location search/manual fallback
JSON backup/import
```

Out of scope for this roadmap:

```text
flights
airports
itineraries
itinerary groups
generated itinerary stops
excursions
photos
story mode
advanced stats
cloud sync
```

Related documents:

```text
Atlas_MVP_Specification.md
Atlas_MVP_Data_Model.md
Atlas_Technical_Architecture.md
```

---

## 1. MVP Goal

The MVP goal is:

```text
A local-first native Android travel atlas where the user can track countries/territories and simple trips with ordered stops.
```

At the end of the MVP, the user should be able to:

```text
open the app
view countries/territories
search/filter countries
mark wished
set currently living
add visit/lived logs
create trips
add ordered trip stops
derive country states from logs and trip stops
export/import JSON backup
```

---

## 2. MVP Milestones

```text
Milestone 0 — Project Foundation
Milestone 1 — Country/Territory Tracking
Milestone 2 — Trips Foundation
Milestone 3 — Trip Stops
Milestone 4 — Location Search and Manual Fallback
Milestone 5 — JSON Backup and Import
Milestone 6 — MVP Polish
```

---

## 3. Milestone 0 — Project Foundation

### 3.1 Goal

Create a runnable Android app with local data foundation.

At the end of Milestone 0:

```text
Launch app
→ import bundled countries/territories dataset
→ display countries from Room
→ open country detail page
```

No user tracking features are required yet.

---

### 3.2 Deliverables

```text
Android project
Kotlin + Compose setup
Room setup
Navigation Compose setup
kotlinx.serialization setup
manual dependency container
initial package structure
Atlas theme
CountryEntity
DatasetMetadataEntity
CountryDao
DatasetMetadataDao
countries/territories JSON asset
dataset importer
Country domain model
Country mapper
CountryRepository
country list ViewModel/screen
country detail ViewModel/screen
```

---

### 3.3 Implementation Tasks

```text
1. Create Android project.
2. Configure Gradle dependencies.
3. Create package structure.
4. Create app theme.
5. Create navigation shell.
6. Create manual dependency container.
7. Set up Room database.
8. Implement CountryEntity.
9. Implement DatasetMetadataEntity.
10. Implement CountryDao.
11. Implement DatasetMetadataDao.
12. Add countries.json asset.
13. Add dataset DTOs.
14. Add dataset constants.
15. Implement CountryDatasetImporter.
16. Create Country domain model.
17. Create CountryMapper.
18. Create CountryRepository interface.
19. Create CountryRepositoryImpl.
20. Create CountryListViewModel.
21. Create CountryListScreen.
22. Create CountryDetailViewModel.
23. Create CountryDetailScreen.
24. Trigger dataset import on startup.
25. Verify persistence after restart.
```

---

### 3.4 CountryEntity MVP Fields

Use the current MVP data model.

```text
CountryEntity
- iso2
- iso3 optional
- name_ca
- name_en optional
- type
- parent_iso2 optional
- is_un_member
- is_observer_state
- is_trackable
- continent
- subregion optional
- flag_emoji optional
- flag_asset optional
- latitude optional
- longitude optional
```

Important:

```text
Include country/territory classification from the beginning.
```

---

### 3.5 Acceptance Criteria

```text
[ ] App builds.
[ ] App launches.
[ ] AtlasTheme is applied.
[ ] Navigation works.
[ ] Room database initializes.
[ ] Countries/territories import from JSON.
[ ] Dataset metadata is stored.
[ ] Import is skipped on second launch if version is unchanged.
[ ] Country list displays imported data.
[ ] Country detail opens from country list.
[ ] Country data persists after restart.
```

---

## 4. Milestone 1 — Country/Territory Tracking

### 4.1 Goal

Implement the first real Atlas feature: country/territory tracking.

At the end of Milestone 1, the user can:

```text
mark countries/territories as wished
set exactly one currently living place
add visit logs
add lived logs
see derived country states
search/filter by derived states
```

---

### 4.2 Deliverables

```text
CountryUserStateEntity
CountryLogEntity
DatePrecision enum
CountryLogType enum
FlexibleDate
FlexibleDateRange
FlexibleDateValidator
FlexibleDateFormatter
CountryUserStateDao
CountryLogDao
CountryTrackingState model
CountryStateDerivationService
ToggleWishedCountryUseCase
SetCurrentlyLivingCountryUseCase
AddCountryLogUseCase
country list state badges
country list search/filter
country detail tracking section
add country log flow
```

---

### 4.3 Implementation Tasks

```text
1. Add shared enums.
2. Implement FlexibleDate.
3. Implement FlexibleDateRange.
4. Implement FlexibleDateValidator.
5. Implement FlexibleDateFormatter.
6. Implement CountryUserStateEntity.
7. Implement CountryLogEntity.
8. Add DAOs.
9. Extend CountryRepository.
10. Implement ToggleWishedCountryUseCase.
11. Implement SetCurrentlyLivingCountryUseCase.
12. Implement AddCountryLogUseCase.
13. Implement CountryTrackingState.
14. Implement CountryStateDerivationService v1.
15. Update CountryListViewModel with tracking state.
16. Add search/filter to country list.
17. Add state badges to country list.
18. Update CountryDetailViewModel.
19. Add wished toggle to detail.
20. Add currently living action to detail.
21. Add logs list to detail.
22. Add create log modal/screen.
23. Add delete log action.
24. Test persistence and derivation.
```

---

### 4.4 Derivation Rules

Milestone 1 uses only:

```text
CountryUserState
CountryLog
```

Rules:

```text
wished = CountryUserState.wished
currentlyLiving = CountryUserState.currentlyLiving
lived = currentlyLiving OR has LIVED log
visited = currentlyLiving OR has VISIT log OR has LIVED log
planned = false
neverVisited = !visited && !lived
```

---

### 4.5 Acceptance Criteria

```text
[ ] Wished toggle works.
[ ] Currently living can be set.
[ ] Only one place can be currently living.
[ ] Visit log can be added without date.
[ ] Lived log can be added without date.
[ ] Flexible date ranges validate correctly.
[ ] Visit log makes place visited.
[ ] Lived log makes place lived and visited.
[ ] Currently living implies lived and visited.
[ ] Country list shows state badges.
[ ] Country list search works.
[ ] Country list filters work.
[ ] Country detail updates reactively.
[ ] Data persists after restart.
```

---

## 5. Milestone 2 — Trips Foundation

### 5.1 Goal

Add simple trips without stops yet.

At the end of Milestone 2, the user can:

```text
create trips
edit trips
delete trips
set trip status
set optional flexible date range
add optional notes
view trip list/detail
```

---

### 5.2 Deliverables

```text
TravelStatus enum
TripEntity
TripDao
Trip domain model
TripRepository
CreateTripUseCase
UpdateTripUseCase
DeleteTripUseCase
TripListViewModel
TripDetailViewModel
TripEditViewModel
TripsListScreen
TripDetailScreen
Create/Edit Trip screen or modal
```

---

### 5.3 Implementation Tasks

```text
1. Add TravelStatus: PLANNED, IN_PROGRESS, COMPLETED, UNKNOWN.
2. Implement TripEntity.
3. Implement TripDao.
4. Add Trip to Room database.
5. Create Trip domain model.
6. Create TripMapper.
7. Create TripRepository interface.
8. Create TripRepositoryImpl.
9. Implement CreateTripUseCase.
10. Implement UpdateTripUseCase.
11. Implement DeleteTripUseCase.
12. Add trips navigation routes.
13. Create TripsListScreen.
14. Create TripDetailScreen.
15. Create Create/Edit Trip flow.
16. Add status selector.
17. Reuse flexible date input for trip date range.
18. Add notes field.
19. Add basic validation.
20. Test trip persistence.
```

---

### 5.4 Acceptance Criteria

```text
[ ] User can create trip with only title.
[ ] User can set status.
[ ] User can add optional date range.
[ ] User can add optional notes.
[ ] User can edit trip.
[ ] User can delete trip.
[ ] Trips list displays created trips.
[ ] Trip detail displays selected trip.
[ ] Data persists after restart.
```

---

## 6. Milestone 3 — Trip Stops

### 6.1 Goal

Add ordered trip stops and derive country/territory states from trip stops.

At the end of Milestone 3, the user can:

```text
add stops to trips
reorder stops
edit/delete stops
see trip stops contribute to planned/visited country states
```

---

### 6.2 Deliverables

```text
TripStopEntity
TripStopDao
TripStop domain model
TripStopRepository methods
CreateTripStopUseCase
UpdateTripStopUseCase
DeleteTripStopUseCase
ReorderTripStopsUseCase
Trip stop UI
country derivation from trip stops
related trip stops in country detail
```

---

### 6.3 Implementation Tasks

```text
1. Implement TripStopEntity.
2. Implement TripStopDao.
3. Add TripStop to Room database.
4. Create TripStop domain model.
5. Create TripStopMapper.
6. Extend TripRepository or create TripStopRepository.
7. Implement CreateTripStopUseCase.
8. Implement UpdateTripStopUseCase.
9. Implement DeleteTripStopUseCase.
10. Implement ReorderTripStopsUseCase.
11. Add stop list to TripDetailScreen.
12. Add create/edit stop flow.
13. Add manual location name field.
14. Add country/territory selector.
15. Add optional coordinates.
16. Add optional date range.
17. Add optional notes.
18. Add stop ordering.
19. Extend CountryStateDerivationService.
20. Show related trip stops in country detail.
```

---

### 6.4 Derivation Rules

Add trip stop inputs:

```text
Trip.status = PLANNED -> stops count as planned
Trip.status = IN_PROGRESS -> stops count as visited
Trip.status = COMPLETED -> stops count as visited
Trip.status = UNKNOWN -> stops do not affect country state
```

MVP simplification:

```text
If a trip is in progress, all its stops count as visited.
```

---

### 6.5 Acceptance Criteria

```text
[ ] User can add stop to trip.
[ ] Stop requires location name and country/territory.
[ ] Coordinates are optional.
[ ] User can edit stop.
[ ] User can delete stop.
[ ] User can reorder stops.
[ ] Planned trip stops mark countries as planned.
[ ] Completed trip stops mark countries as visited.
[ ] In-progress trip stops mark countries as visited.
[ ] Unknown trip stops do not affect state.
[ ] Country detail shows related trip stops.
```

---

## 7. Milestone 4 — Location Search and Manual Fallback

### 7.1 Goal

Add online location search while preserving manual fallback.

At the end of Milestone 4:

```text
user can search for a location
select result
create trip stop from result
still create stop manually if search fails
```

---

### 7.2 Deliverables

```text
LocationSearchService interface
LocationSearchResult model
chosen provider implementation
PlaceEntity optional
PlaceDao optional
Place mapper optional
location search UI
manual fallback UI
```

---

### 7.3 Implementation Tasks

```text
1. Decide first geocoding provider.
2. Create LocationSearchService interface.
3. Create LocationSearchResult model.
4. Implement provider service.
5. Add error handling.
6. Decide whether to implement PlaceEntity now.
7. If yes, implement PlaceEntity and PlaceDao.
8. Add search field to stop creation flow.
9. Display search results.
10. Map selected result to country/territory.
11. Create stop from selected result.
12. Keep manual fallback available.
13. Test provider failure.
14. Test manual fallback without coordinates.
```

---

### 7.4 Acceptance Criteria

```text
[ ] User can search for location.
[ ] User can select search result.
[ ] Selected result creates a stop.
[ ] Country/territory is resolved or selected.
[ ] Coordinates are stored when available.
[ ] Manual fallback works if search fails.
[ ] Manual fallback works with no coordinates.
[ ] Search error does not block stop creation.
```

---

## 8. Milestone 5 — JSON Backup and Import

### 8.1 Goal

Add local JSON backup/import for MVP user data.

At the end of Milestone 5:

```text
user can export MVP data
user can import MVP data
backup includes version and dataset metadata
```

---

### 8.2 Deliverables

```text
Backup DTOs
JsonBackupExporter
JsonBackupImporter
BackupValidator
backupVersion = 1
export UI
import UI
replace-all import after confirmation
```

---

### 8.3 Included Data

Backup v1 includes:

```text
countryUserStates
countryLogs
trips
tripStops
places if used
```

Metadata:

```text
backupVersion
createdAt
countryDatasetVersion
```

---

### 8.4 Implementation Tasks

```text
1. Define backup DTOs.
2. Implement export mapper.
3. Implement JsonBackupExporter.
4. Add export action in settings.
5. Implement BackupValidator.
6. Implement import mapper.
7. Implement JsonBackupImporter.
8. Use database transaction for import.
9. Add import file picker/action.
10. Add replace-all confirmation dialog.
11. Validate country references.
12. Validate trip/stop relationships.
13. Test export/import round trip.
14. Test invalid backup.
```

---

### 8.5 Acceptance Criteria

```text
[x] User can export JSON backup.
[x] Backup includes country states.
[x] Backup includes country logs.
[x] Backup includes trips.
[x] Backup includes trip stops.
[x] Backup includes dataset version.
[x] User can import valid backup.
[x] Import restores data correctly.
[x] Invalid backup fails safely.
[x] Import does not partially destroy data on failure.
```

---

## 9. Milestone 6 — MVP Polish

### 9.1 Goal

Make the MVP stable and pleasant enough for real personal use.

---

### 9.2 Deliverables

```text
basic dashboard
empty states
loading states
form validation messages
delete confirmations
Catalan text cleanup
basic visual polish
navigation cleanup
manual test pass
unit test pass for critical logic
```

---

### 9.3 Implementation Tasks

```text
1. Improve dashboard cards.
2. Add empty states.
3. Add loading states.
4. Add form validation messages.
5. Add delete confirmations.
6. Review Catalan UI strings.
7. Review navigation/back behaviour.
8. Add basic unit tests.
9. Test with real travel data.
10. Fix rough UI issues.
11. Verify backup/import after real data entry.
12. Verify app restart persistence.
```

---

### 9.4 Acceptance Criteria

```text
[ ] MVP does not crash in normal flows.
[ ] Forms validate required fields.
[ ] Destructive actions ask for confirmation.
[ ] Empty states are understandable.
[ ] Catalan UI is consistent enough.
[ ] Core derivation tests pass.
[ ] Backup/import works with real data.
[ ] App feels usable as a personal travel tracker.
```

---

## 10. MVP Completion Checklist

MVP is complete when:

```text
[ ] Countries/territories display from local dataset.
[ ] Countries can be searched and filtered.
[ ] Country detail works.
[ ] Wished toggle works.
[ ] Currently living rule works.
[ ] Visit/lived logs work.
[ ] Flexible dates work.
[ ] Country states derive from logs and user state.
[ ] Trips can be created/edited/deleted.
[ ] Trip statuses work.
[ ] Trip stops can be created/edited/deleted/reordered.
[ ] Country states derive from trip stops.
[ ] Location search works.
[ ] Manual fallback works.
[ ] JSON export works.
[ ] JSON import works.
[ ] Basic dashboard exists.
[ ] Data persists after restart.
[ ] No backend/account required.
```

---

## 11. Current Forward Roadmap

This section reflects the current handoff state as of 2026-06-01.

Current baseline:

```text
MVP implementation is functionally complete.
Dashboard pass is complete.
Full 244-entry country/territory dataset is live.
Critical domain and backup unit tests exist.
Next work is final MVP QA and stabilization, not new feature scope.
Visual direction has changed to Warm Editorial Atlas.
```

Roadmap rule:

```text
Do not begin v2.0 feature work until the MVP QA pass is complete and the MVP release gate is accepted.
Do apply the new visual direction to MVP surfaces before release.
```

Visual reference:

```text
Documentation/Atlas (offline).html
Screenshots supplied on 2026-06-01
```

MVP visual target:

```text
Warm Editorial Atlas
warm parchment app background
paper cards with subtle borders
editorial headings and compact metadata
bottom navigation shell
rounded filter/status chips
dotted atlas map placeholders
route-line trip cards
state colors as restrained accents
timeline/route rails for stops
```

Scope note:

```text
The visual references include flights, itineraries, and stats screens.
Those are style guidance for v2.0/later only.
MVP must not add those feature areas before MVP acceptance.
```

### 11.1 Phase A - Baseline Verification

Goal:

```text
Confirm the checked-in project builds and the documented baseline is still true.
```

Tasks:

```text
1. Confirm git status is clean or identify unrelated local changes.
2. Run unit tests with the Gradle wrapper.
3. Build the debug APK.
4. Launch the app on device or emulator.
5. Confirm country dataset import succeeds with version 2026.1.
```

Acceptance gate:

```text
[x] testDebugUnitTest passes.
[x] assembleDebug passes.
[x] App launches without startup crash.
[x] Country list loads the bundled dataset.
[x] Manual smoke check can be performed by the user after a debug build is installed.
```

### 11.2 Phase B - MVP QA Pass

Goal:

```text
Walk the MVP end-to-end using realistic data and record/fix defects.
```

QA order:

```text
1. Country list and filters
2. Country detail and related trip stops
3. Country tracking actions
4. Trip list and trip detail
5. Trip stop add/edit/delete/reorder
6. Location search and manual fallback
7. Dashboard derived counts and recent activity
8. Backup export/import round trip
9. Persistence after app restart
10. Warm Editorial Atlas visual consistency
11. Catalan UI consistency and spacing
12. Country dataset spot check
```

Manual checklist:

```text
Documentation/Atlas_MVP_Manual_QA.md
```

Acceptance gate:

```text
[ ] Search works across Catalan name, English name, ISO2, and ISO3.
[ ] Filters work for visited, wished, planned, lived, currently living, and never visited.
[ ] Currently living clears the previous current country.
[ ] Visit/lived logs update derived state correctly.
[ ] Planned trip stops mark countries planned.
[ ] In-progress/completed trip stops mark countries visited.
[ ] Unknown trip stops do not affect country state.
[ ] Related trip stops are visible from country detail.
[ ] Backup export/import restores user states, logs, trips, stops, order, and derived states.
[ ] Data remains correct after app restart.
[ ] Existing MVP screens follow the Warm Editorial Atlas direction.
[ ] UI text visible in normal flows is Catalan.
```

### 11.3 Phase C - Stabilization

Goal:

```text
Fix only the defects found during MVP QA and keep scope locked.
```

Rules:

```text
Prefer small targeted fixes.
Add or update domain/repository tests when defects touch business rules.
Do not add flights, airports, itineraries, photos, cloud sync, accounts, advanced stats, or v2.0 tables.
Use the visual redesign pass only for existing MVP screens.
```

Visual stabilization tasks:

```text
1. Extract shared Warm Editorial Atlas colors, typography, dimensions, and card/chip styles.
2. Update bottom navigation to match the reference shell.
3. Restyle dashboard/home around the atlas map, summary metrics, next trip, recent memories, and wishlist sections.
4. Restyle country list with grouped continent sections, compact rows, filters, and status chips.
5. Restyle trips list with route-line trip cards and status filters.
6. Restyle trip detail with hero route card, summary facts, route map placeholder, route rail, and linked-itinerary placeholder only if existing data supports it.
7. Restyle country detail within MVP data only: identity, state chips, logs, related trips, and map placeholder.
8. Restyle settings/backup enough to fit the same paper/card system.
9. Verify small-screen text wrapping, chip overflow, and bottom-navigation spacing.
```

Acceptance gate:

```text
[ ] All QA-blocking defects fixed.
[ ] Relevant tests added or updated for behavior fixes.
[ ] testDebugUnitTest passes.
[ ] assembleDebug passes.
[ ] Handoff prompt updated with final MVP status.
```

### 11.3.1 Tasks Left to Finish MVP

The remaining MVP work is:

```text
1. Implement the Warm Editorial Atlas visual pass for existing MVP screens.
2. QA country list search, filters, grouping, and empty states with the 244-entry dataset.
3. QA country detail, including state actions, logs, related trip stops, and optional country fields.
4. QA trip list/detail, trip create/edit/delete, stop create/edit/delete/reorder, and route/map placeholders.
5. QA location search plus manual fallback for stop creation/editing.
6. QA dashboard counts, next trip, recent activity, wishlist, and derived state summaries.
7. QA backup export/import round trip with realistic data.
8. QA persistence after app restart.
9. Complete Catalan copy and validation-message pass.
10. Spot-check country dataset names, territories, flags, continents, and subregions.
11. Fix QA defects only within MVP scope.
12. Run final testDebugUnitTest and assembleDebug.
13. User performs a manual smoke check: launch app, open Inici/Atlas/Viatges/Configuracio, and verify data renders.
14. Update Handoff_Prompt.md with final MVP status and known limitations.
15. Prepare the MVP release-candidate note/build.
```

### 11.4 Phase D - MVP Release Candidate

Goal:

```text
Prepare a usable MVP build for personal use.
```

Tasks:

```text
1. Run the final QA smoke pass.
2. Verify backup export before any destructive import test.
3. Confirm versionName/versionCode strategy for the first internal build.
4. Create a release-candidate note with known limitations.
5. Tag or mark the accepted MVP state if desired.
```

Acceptance gate:

```text
[ ] MVP can be used for real country/trip tracking.
[ ] Backup/import is trusted enough for local-first personal data.
[ ] Known limitations are documented.
[ ] v2.0 can start from a stable MVP baseline.
```

### 11.5 Phase E - v2.0 Readiness

Goal:

```text
Prepare for v2.0 without leaking v2.0 work into MVP.
```

Start only after MVP acceptance.

First v2.0 steps:

```text
1. Re-read Atlas_v2.0_Specification.md, Atlas_v2.0_Data_Model.md, and Atlas_v2.0_Roadmap.md.
2. Choose the initial airport dataset source and import shape.
3. Design the MVP-to-v2 Room migration.
4. Add airport foundation as the first v2.0 vertical slice.
5. Keep layover-safe derivation as the main v2.0 correctness constraint.
```

---

## 12. After MVP

After MVP completion, continue with:

```text
Atlas_v2.0_Roadmap.md
```

v2.0 begins with:

```text
airport dataset
airport search
manual solo flights
flight country derivation
itineraries
itinerary groups
generated trip stops
```
