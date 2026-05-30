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
[ ] User can export JSON backup.
[ ] Backup includes country states.
[ ] Backup includes country logs.
[ ] Backup includes trips.
[ ] Backup includes trip stops.
[ ] Backup includes dataset version.
[ ] User can import valid backup.
[ ] Import restores data correctly.
[ ] Invalid backup fails safely.
[ ] Import does not partially destroy data on failure.
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

## 11. After MVP

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
