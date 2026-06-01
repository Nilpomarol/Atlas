# Atlas v2.0 Roadmap

## 0. Document Purpose

This document defines the detailed implementation roadmap for **Atlas v2.0**.

v2.0 is the first major version after the MVP. It adds:

```text
airports
manual solo flights
itineraries
itinerary groups
layover-safe country/territory derivation
generated trip stops
flight/itinerary route visualization
backup/import v2
```

Related documents:

```text
Atlas_v2.0_Specification.md
Atlas_v2.0_Data_Model.md
Atlas_Technical_Architecture.md
```

---

## 1. v2.0 Goal

The goal of v2.0 is:

```text
Make flights and itineraries first-class features in Atlas without incorrectly counting layovers as visited countries.
```

At the end of v2.0, the user should be able to:

```text
search/select airports
create solo flights
create itineraries
group flights into meaningful legs
link itinerary to a trip
generate trip stops from itinerary groups
see country states derived from flights/itineraries
view basic route maps
export/import v2 data
```

---

## 2. v2.0 Milestones

```text
Milestone 0 — Airport Foundation
Milestone 1 — Solo Flights
Milestone 2 — Itineraries and Groups
Milestone 3 — Layover-Safe Country Derivation
Milestone 4 — Link Itineraries to Trips
Milestone 5 — Generated Trip Stops
Milestone 6 — Flight and Itinerary Maps
Milestone 7 — Backup/Import v2
Milestone 8 — v2.0 Polish
```

---

## 3. Milestone 0 — Airport Foundation

### 3.1 Goal

Add airport reference data and airport search.

At the end of this milestone:

```text
user can search and select airports from a local dataset
```

---

### 3.2 Deliverables

```text
AirportEntity
AirportDao
Airport domain model
AirportRepository
airport dataset asset
AirportDatasetImporter
airport search UI/component
airport dataset metadata
Room migration from MVP
```

---

### 3.3 Implementation Tasks

```text
1. Choose initial airport dataset source.
2. Decide dataset size: IATA commercial/passenger airports recommended.
3. Add AirportEntity.
4. Add AirportDao.
5. Add AirportEntity to Room database.
6. Add Room migration from MVP.
7. Add airports.json asset.
8. Add AirportDatasetDto.
9. Add AirportMapper.
10. Add AirportDatasetImporter.
11. Add airport dataset metadata key.
12. Create Airport domain model.
13. Create AirportRepository.
14. Create airport search use case.
15. Create airport search UI component.
16. Test search by IATA.
17. Test search by city.
18. Test search by country/territory.
```

---

### 3.4 Acceptance Criteria

```text
[ ] Airport dataset imports.
[ ] Dataset metadata is stored.
[ ] Airport search works by IATA.
[ ] Airport search works by city/name.
[ ] Airport country references resolve.
[ ] Room migration preserves MVP data.
[ ] App launches with existing MVP database.
```

Current implementation note:

```text
2026-06-01:
- Added AirportEntity, AirportDao, Room v7 migration, airport domain model, AirportRepository, search use case, AirportDatasetImporter, dataset metadata key, and a curated seed airports.json.
- Seed dataset is intentionally small and should be replaced or expanded before v2.0 release.
- Airport search UI/component is still pending.
```

---

## 4. Milestone 1 — Solo Flights

### 4.1 Goal

Add manual solo flight tracking.

At the end of this milestone:

```text
user can create, edit, delete, list, and view solo flights
```

---

### 4.2 Deliverables

```text
FlightEntity
FlightDao
Flight domain model
FlightRepository
CreateFlightUseCase
UpdateFlightUseCase
DeleteFlightUseCase
FlightListViewModel
FlightDetailViewModel
FlightEditViewModel
FlightsListScreen
FlightDetailScreen
Create/Edit Flight flow
```

---

### 4.3 Implementation Tasks

```text
1. Add FlightEntity.
2. Add FlightDao.
3. Add FlightEntity to Room database.
4. Add Room migration if separate from airport migration.
5. Create Flight domain model.
6. Create FlightMapper.
7. Create FlightRepository.
8. Implement CreateFlightUseCase.
9. Implement UpdateFlightUseCase.
10. Implement DeleteFlightUseCase.
11. Add flight navigation routes.
12. Create FlightsListScreen.
13. Create FlightDetailScreen.
14. Create Create/Edit Flight screen.
15. Add origin airport selector.
16. Add destination airport selector.
17. Add flight status selector.
18. Add optional planned/actual date-time fields.
19. Add optional airline/flight number/aircraft/notes.
20. Add country tracking flags for solo flights.
21. Test flight persistence.
```

---

### 4.4 Flight Status Rules

```text
PLANNED -> destination can count as planned
IN_PROGRESS -> destination does not count as visited yet
COMPLETED -> destination can count as visited
UNKNOWN -> no country derivation by default
```

---

### 4.5 Acceptance Criteria

```text
[ ] User can create solo flight.
[ ] Origin and destination airports are required.
[ ] Flight status is required.
[ ] Optional details can be saved.
[ ] Flights list displays created flights.
[ ] Flight detail displays selected flight.
[ ] User can edit flight.
[ ] User can delete flight.
[ ] Data persists after restart.
```

---

## 5. Milestone 2 — Itineraries and Groups

### 5.1 Goal

Allow flights to be grouped into meaningful itinerary legs.

At the end of this milestone:

```text
user can create itineraries, create groups, and add ordered flights inside groups
```

---

### 5.2 Deliverables

```text
ItineraryEntity
ItineraryGroupEntity
ItineraryDao
ItineraryGroupDao
Itinerary domain model
ItineraryGroup domain model
ItineraryRepository
ItineraryListViewModel
ItineraryDetailViewModel
ItineraryListScreen
ItineraryDetailScreen
group creation/edit/delete
group reordering
flight assignment to group
flight reordering inside group
```

---

### 5.3 Implementation Tasks

```text
1. Add ItineraryEntity.
2. Add ItineraryGroupEntity.
3. Add DAOs.
4. Add entities to Room database.
5. Create domain models.
6. Create mappers.
7. Create ItineraryRepository.
8. Add itinerary navigation.
9. Create ItineraryListScreen.
10. Create ItineraryDetailScreen.
11. Add create/edit itinerary flow.
12. Add create/edit/delete group flow.
13. Add group sort_order.
14. Add grouped flight creation.
15. Add flight sort_order inside group.
16. Add reorder groups UI.
17. Add reorder flights inside group UI.
18. Add validation for empty groups.
```

---

### 5.4 Acceptance Criteria

```text
[ ] User can create itinerary.
[ ] User can create itinerary groups.
[ ] User can add flights inside groups.
[ ] User can reorder groups.
[ ] User can reorder flights inside groups.
[ ] Group order persists.
[ ] Flight order persists.
[ ] Itinerary detail clearly shows groups and flights.
```

---

## 6. Milestone 3 — Layover-Safe Country Derivation

### 6.1 Goal

Ensure grouped flights do not incorrectly count layovers.

At the end of this milestone:

```text
Barcelona → Doha → Tokyo counts Japan, not Qatar
```

---

### 6.2 Deliverables

```text
FlightCountryTrackingService
ItineraryGroupDerivationService
CountryStateDerivationService v2
unit tests for grouped flight derivation
country detail source explanation
```

---

### 6.3 Derivation Rule

For each itinerary group:

```text
if group is not last group:
    derived place = destination airport of last flight in group

if group is last group:
    derived place = origin airport of first flight in group
```

Examples:

```text
Barcelona → Doha → Tokyo
Derived: Japan

Osaka → Doha → Barcelona
Derived: Japan
```

---

### 6.4 Implementation Tasks

```text
1. Implement solo flight country derivation.
2. Implement itinerary group derived place logic.
3. Add status handling for itinerary groups.
4. Decide group status behaviour when null.
5. Extend CountryStateDerivationService.
6. Ensure grouped flight segments do not directly count.
7. Add country detail source entries for solo flights.
8. Add country detail source entries for itineraries.
9. Write unit tests for layover cases.
10. Test planned/completed/unknown behaviour.
```

---

### 6.5 Acceptance Criteria

```text
[ ] Solo completed flight marks destination visited.
[ ] Solo planned flight marks destination planned.
[ ] Solo unknown flight does not affect country state.
[ ] Grouped layover airport country is not counted.
[ ] Group derived destination is counted correctly.
[ ] Return group uses origin of first flight.
[ ] Country detail explains flight/itinerary-derived states.
```

---

## 7. Milestone 4 — Link Itineraries to Trips

### 7.1 Goal

Allow one itinerary to be linked to one trip.

At the end of this milestone:

```text
a trip can show its linked itinerary and related flights
```

---

### 7.2 Deliverables

```text
LinkItineraryToTripUseCase
UnlinkItineraryFromTripUseCase
trip-itinerary uniqueness validation
trip detail itinerary section
itinerary detail linked trip section
```

---

### 7.3 Rules

```text
A trip can have zero or one itinerary.
An itinerary can be linked to zero or one trip.
```

---

### 7.4 Implementation Tasks

```text
1. Add trip_id nullable unique to ItineraryEntity if not already present.
2. Add link itinerary to trip use case.
3. Add unlink itinerary use case.
4. Add validation for trip already having itinerary.
5. Add link action from trip detail.
6. Add link action from itinerary detail.
7. Show linked itinerary on trip detail.
8. Show linked trip on itinerary detail.
9. Test uniqueness rules.
```

---

### 7.5 Acceptance Criteria

```text
[ ] User can link itinerary to trip.
[ ] User can unlink itinerary.
[ ] A trip cannot have two linked itineraries.
[ ] An itinerary cannot link to two trips.
[ ] Trip detail shows linked itinerary.
[ ] Itinerary detail shows linked trip.
```

---

## 8. Milestone 5 — Generated Trip Stops

### 8.1 Goal

Generate meaningful trip stops from linked itinerary groups.

At the end of this milestone:

```text
linked itinerary groups create generated stops in the trip route
```

---

### 8.2 Deliverables

```text
TripStopEntity migration
TripStop.source
TripStop.itinerary_group_id
TripStop.is_visible
TripStop.display_title optional
ItineraryGeneratedStopService
generated stop preview
trip detail generated stops
generated stop sync logic
```

---

### 8.3 Implementation Tasks

```text
1. Add TripStop.source.
2. Add TripStop.itinerary_group_id.
3. Add TripStop.is_visible.
4. Add TripStop.display_title optional.
5. Migrate existing stops to source = MANUAL.
6. Implement ItineraryGeneratedStopService.
7. Generate stops when itinerary is linked.
8. Update generated stops when group/flights change.
9. Remove or handle generated stops when itinerary unlinked.
10. Preserve notes/display title when regenerating.
11. Show generated stops in trip detail.
12. Clearly label generated stops.
13. Prevent direct editing of generated core fields.
14. Add generated stop preview in itinerary detail.
15. Write generated stop unit tests.
```

---

### 8.4 Acceptance Criteria

```text
[ ] Existing manual stops remain valid after migration.
[ ] Linked itinerary creates generated stops.
[ ] Generated stops use correct group rule.
[ ] Generated stops appear in trip detail.
[ ] Generated stops are visually identified.
[ ] User cannot directly edit generated core location fields.
[ ] Updating flights updates generated stops.
[ ] Unlink behaviour is clear and safe.
```

---

## 9. Milestone 6 — Flight and Itinerary Maps

### 9.1 Goal

Add basic route visualization.

At the end of this milestone:

```text
user can visually understand flight and itinerary routes
```

---

### 9.2 Deliverables

```text
flight route preview
itinerary route preview
trip map with generated stops
basic map route models
map UI components
```

---

### 9.3 Implementation Tasks

```text
1. Choose or confirm map library.
2. Create map abstraction/models if needed.
3. Add airport markers.
4. Draw simple line between origin/destination.
5. Add flight route preview to flight detail.
6. Add itinerary route preview to itinerary detail.
7. Add generated stops to trip map.
8. Handle missing coordinates gracefully.
9. Test with multi-segment itinerary.
```

---

### 9.4 Acceptance Criteria

```text
[ ] Flight detail shows route preview.
[ ] Itinerary detail shows route preview.
[ ] Trip map can include generated stops.
[ ] Missing coordinates do not crash map.
[ ] Basic route visualization is understandable.
```

---

## 10. Milestone 7 — Backup/Import v2

### 10.1 Goal

Extend JSON backup/import to include v2.0 data.

---

### 10.2 Deliverables

```text
backupVersion = 2
airportDatasetVersion metadata
flight backup DTOs
itinerary backup DTOs
itinerary group backup DTOs
backup v2 exporter
backup v2 importer
backup migration/validation
```

---

### 10.3 Included Data

Backup v2 includes MVP data plus:

```text
flights
itineraries
itineraryGroups
generated stops as tripStops
```

Metadata:

```text
countryDatasetVersion
airportDatasetVersion
```

---

### 10.4 Implementation Tasks

```text
1. Define backup v2 schema.
2. Add airport dataset metadata to backup.
3. Add flight export.
4. Add itinerary export.
5. Add itinerary group export.
6. Add flight import.
7. Add itinerary import.
8. Add itinerary group import.
9. Validate airport references.
10. Validate itinerary/group/flight relationships.
11. Validate generated stops.
12. Test v1 backup import compatibility.
13. Test v2 export/import round trip.
```

---

### 10.5 Acceptance Criteria

```text
[ ] v2 backup exports flights.
[ ] v2 backup exports itineraries.
[ ] v2 backup exports groups.
[ ] v2 backup includes airport dataset version.
[ ] v2 import restores flights.
[ ] v2 import restores itineraries/groups.
[ ] v1 backups still import or fail with clear compatible message.
[ ] Invalid airport references fail safely.
```

---

## 11. Milestone 8 — v2.0 Polish

### 11.1 Goal

Make the flight/itinerary expansion stable and usable.

---

### 11.2 Deliverables

```text
empty states
validation messages
delete confirmations
clear labels for solo/grouped flights
clear labels for generated stops
basic flight stats
timeline polish
Catalan text cleanup
unit tests
manual test pass
```

---

### 11.3 Implementation Tasks

```text
1. Add empty states for flights.
2. Add empty states for itineraries.
3. Add validation messages.
4. Add delete confirmations.
5. Add grouped/solo labels.
6. Add generated stop labels.
7. Add basic flight stats.
8. Polish country detail timeline entries.
9. Review Catalan UI text.
10. Add/finish unit tests.
11. Test with real itinerary examples.
12. Verify migration from MVP database.
13. Verify backup/import with real data.
```

---

### 11.4 Acceptance Criteria

```text
[ ] v2.0 does not lose MVP data.
[ ] Flight flows are stable.
[ ] Itinerary flows are stable.
[ ] Layovers are not counted as visits.
[ ] Generated stops are understandable.
[ ] Backup/import v2 works.
[ ] UI labels are clear.
[ ] Core derivation tests pass.
```

---

## 12. v2.0 Completion Checklist

v2.0 is complete when:

```text
[ ] Airports import from local dataset.
[ ] Airport search works.
[ ] Solo flights can be created/edited/deleted.
[ ] Flight list/detail works.
[ ] Solo flight country derivation works.
[ ] Itineraries can be created/edited/deleted.
[ ] Itinerary groups can be created/edited/deleted.
[ ] Flights can be added to groups.
[ ] Groups and flights can be reordered.
[ ] Layover-safe derivation works.
[ ] Itineraries can be linked to trips.
[ ] Generated trip stops work.
[ ] Trip detail shows generated stops.
[ ] Country detail explains flight/itinerary sources.
[ ] Basic route maps work.
[ ] Backup/import v2 works.
[ ] MVP data survives migration.
```

---

## 13. After v2.0

Possible next directions:

```text
excursions and excursion stops
richer maps
travel stats dashboard
country stats summary
photos
story/slideshow mode
flight API lookup
airline logos
aircraft data
```

Recommended decision:

```text
Choose the next version based on actual use after v2.0.
```
