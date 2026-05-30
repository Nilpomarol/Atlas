# Atlas MVP Specification

## 0. Document Purpose

This document defines the **minimum viable product** for Atlas.

The MVP is intentionally smaller than the full Atlas product vision. Its purpose is to define the first version that is genuinely useful, buildable, and coherent without trying to implement every long-term feature.

The full product direction should be maintained separately in:

```text
Atlas_Product_Specification.md
```

Future post-MVP scope should be maintained separately in documents such as:

```text
Atlas_v2.0_Specification.md
```

This document is a **build contract**. If a feature is not explicitly included here, it should be treated as out of scope for MVP.

---

## 1. MVP Goal

The MVP goal is:

```text
A local-first native Android app where the user can track countries/territories they have visited, lived in, wished, or planned through manual logs and simple trips with stops.
```

The MVP should allow the user to:

- view a list of countries and territories
- mark places as wished
- mark one place as currently living
- add manual visit/lived logs
- create simple trips
- add ordered trip stops
- use location search for stops
- use manual fallback when search fails
- derive country/territory states from manual logs and trip stops
- export/import user data as JSON

The MVP should already feel like the foundation of a personal travel atlas, but it should not attempt to implement the full travel archive yet.

---

## 2. MVP Product Philosophy

The MVP should prioritize:

```text
working core loop > complete feature set
simple data entry > detailed travel modelling
local reliability > external integrations
clear country/trip tracking > advanced visualization
future-compatible architecture > premature complexity
```

The MVP should be useful even if the user only records:

```text
Countries visited
Countries wished
Current living country
A few trips
A few trip stops
```

The first version does not need to support flights, itineraries, photo memories, advanced statistics, or story mode.

---

## 3. MVP Technical Direction

Atlas MVP is a native Android app.

Recommended stack:

```text
Kotlin
Jetpack Compose
Room
Navigation Compose
Local JSON assets
Local-first architecture
OpenStreetMap-based location search / maps direction
```

The MVP should be Android-only.

No backend is required.

No account system is required.

No cloud sync is required.

---

## 4. MVP Scope Summary

Included in MVP:

```text
1. Android foundation
2. Local Room database
3. Catalan-first UI strings
4. Countries/territories static dataset
5. Countries/territories list
6. Country/territory detail page
7. Wished toggle
8. Currently living toggle
9. Manual visit/lived logs
10. Simple trips
11. Ordered trip stops
12. Location search for trip stops
13. Manual fallback for trip stops
14. Derived country/territory states
15. Basic dashboard
16. Basic JSON export/import
```

Explicitly not included in MVP:

```text
1. Flights
2. Itineraries
3. Itinerary groups
4. Flight API lookup/import
5. Airline logos
6. Aircraft images/specs
7. Excursions
8. Photos
9. Story/slideshow mode
10. Advanced stats dashboard
11. Full country stats/info dataset
12. Cloud sync
13. Social sharing
14. Multi-language support
15. CSV export
16. Offline maps
17. Advanced country comparison
18. Interactive world map with country polygon coloring
```

---

## 5. MVP Core User Flow

The MVP should support this basic loop:

```text
Open app
→ view dashboard
→ view countries/territories
→ mark wished/currently living
→ add visit/lived logs
→ create trip
→ add stops
→ see country/territory states update
→ export/import backup when needed
```

This loop should work without requiring flights, itineraries, photos, or advanced stats.

---

## 6. MVP Country and Territory Scope

### 6.1 Dataset Direction

The MVP should use a broad **countries and territories** dataset rather than only strict sovereign countries.

Reason:

Atlas is a personal travel atlas. Travel-relevant territories and special regions should be trackable even if they are not sovereign states.

Examples of travel-relevant entities:

```text
Hong Kong
Greenland
Faroe Islands
Puerto Rico
Aruba
Curaçao
French Polynesia
Gibraltar
Guadeloupe
Martinique
```

The MVP does not need perfect political handling, but the data model should support classification from the beginning.

---

### 6.2 Country/Territory Entity

Conceptual model:

```text
Country
- id
- iso2
- iso3 optional
- name_ca
- name_en optional
- type: sovereign_state | dependent_territory | special_region | disputed_or_other
- parent_country_id nullable
- is_un_member: boolean
- is_observer_state: boolean
- is_trackable: boolean
- continent
- subregion optional
- flag_emoji optional
- flag_asset optional
- latitude optional
- longitude optional
```

Implementation note:

The exact Room schema may differ, but the MVP should not reduce countries to only name + flag. The classification fields prevent future migration pain.

---

### 6.3 MVP Country/Territory Counts

The MVP should support at least:

```text
Total tracked places visited
```

If easy, it may also show:

```text
Sovereign countries visited
Territories/special regions visited
```

However, separate counts are not mandatory for the first working version.

---

## 7. MVP Country/Territory States

### 7.1 Supported States

MVP states:

```text
never_visited
wished
planned
visited
lived
currently_living
```

These are not all mutually exclusive.

Examples:

```text
A place can be wished and never visited.
A place can be wished and visited.
A place can be planned and wished.
A place can be lived and visited.
A place can be currently living and lived.
```

---

### 7.2 Display Priority

For display purposes, use this priority:

```text
currently_living
lived
planned
visited
wished
never_visited
```

This priority is only for visual presentation. The underlying state derivation should preserve all applicable states.

Example:

```text
A country can be both wished and visited.
The card may display "visited" as the main state, but the wished marker should still exist.
```

---

### 7.3 Wished

A place is wished if:

```text
CountryUserState.wished = true
```

Wished is a simple manual toggle.

Wished should be included in JSON backup.

---

### 7.4 Currently Living

A place is currently living if:

```text
CountryUserState.currently_living = true
```

Rules:

- only one place can be currently living at a time
- setting a new currently living place unsets the previous one
- currently living implies lived
- currently living implies visited
- currently living should be included in JSON backup

For MVP, setting currently living may automatically create or maintain a lived log, but this can also be implemented as a derived state if simpler.

Recommended MVP behaviour:

```text
When the user marks a country as currently living:
- unset previous currently living country
- set selected country as currently living
- treat it as lived and visited
```

---

### 7.5 Manual Visit and Lived Logs

The user can add manual logs to a country/territory.

Supported log types:

```text
visit
lived
```

Optional fields:

```text
date_range
notes
```

A manual visit log makes the place visited.

A manual lived log makes the place lived and visited.

---

### 7.6 Planned

In the MVP, planned state is derived from planned trips and their stops.

There is no separate manual planned country toggle in MVP.

A place counts as planned if:

```text
There is a planned trip stop in that place.
```

---

### 7.7 Visited

A place counts as visited in MVP if any of the following are true:

1. There is a manual visit log.
2. There is a manual lived log.
3. The place is currently living.
4. There is a completed trip stop in that place.
5. There is an in-progress trip stop in that place.

MVP simplification:

```text
If a trip is in progress, all its stops count as visited.
```

This is not perfectly precise, but it is acceptable for MVP. Stop-level status can be introduced later.

---

### 7.8 Never Visited

A place is never visited if it does not satisfy any visited or lived condition.

A place can still be wished or planned while never visited.

---

## 8. MVP Status Rules

### 8.1 Supported Statuses

Trips should support:

```text
planned
in_progress
completed
unknown
```

Country logs do not need a status.

Trip stops do not need their own status in MVP. They inherit status from the parent trip.

---

### 8.2 Status and Dates

Status is stored explicitly.

Dates may suggest status when enough information exists, but the user can override it.

Recommended conceptual model:

```text
status: planned | in_progress | completed | unknown
status_source: manual | inferred
```

MVP implementation can simplify this if needed.

Minimum acceptable MVP implementation:

```text
Trip has explicit status.
Dates do not automatically override status.
```

Better MVP implementation:

```text
Trip has explicit status.
When dates are entered, the app suggests a status.
The user can accept or ignore the suggestion.
```

Avoid complex automatic status updates in the first MVP.

---

### 8.3 Country Derivation by Trip Status

Trip status affects country state derivation.

Rules:

```text
planned trip → stops count as planned
in_progress trip → stops count as visited
completed trip → stops count as visited
unknown trip → stops do not affect visited/planned counts, or are shown as unknown
```

Recommended MVP rule for `unknown`:

```text
Unknown trip stops appear in the trip but do not affect country state.
```

This prevents accidental false visited/planned states.

---

## 9. MVP Flexible Dates

The MVP should support flexible dates for:

- country logs
- trips
- trip stops, if implemented

Supported precision:

```text
year
month
day
```

Examples:

```text
2023
06-2023
14-06-2023
```

Date ranges are supported.

Rules:

```text
start date may be empty
end date may be empty
both may be present
if both are present, both must use the same precision
```

Valid examples:

```text
2023 → 2024
06-2023 → 09-2023
14-06-2023 → 20-06-2023
```

Invalid example:

```text
2023 → 06-2024
```

Conceptual model:

```text
FlexibleDate
- year
- month nullable
- day nullable
- precision: year | month | day
```

```text
FlexibleDateRange
- start_date nullable
- end_date nullable
- precision: year | month | day
```

Implementation may store this as embedded fields, serialized value objects, or separate columns.

---

## 10. MVP Trips

### 10.1 Trip Purpose

A trip is a travel experience.

Examples:

```text
Japan 2026
Roadtrip País Basc
Amsterdam weekend
Ireland summer trip
```

In MVP, trips are intentionally simple.

A trip can be created with only:

```text
title
```

Optional fields:

```text
status
date_range
notes
```

---

### 10.2 Trip Model

Conceptual model:

```text
Trip
- id
- title
- status: planned | in_progress | completed | unknown
- status_source: manual | inferred optional
- date_range optional
- notes optional
- created_at
- updated_at
```

Minimum implementation:

```text
Trip
- id
- title
- status
- date_range optional
- notes optional
- created_at
- updated_at
```

---

### 10.3 Trip Stops

Main trip stops represent the primary route of the trip.

Example:

```text
Tokyo → Kyoto → Osaka
```

A trip stop should have:

```text
location_name
country_id
sort_order
```

Optional:

```text
latitude
longitude
date_range
notes
```

Conceptual model:

```text
TripStop
- id
- trip_id
- location_name
- country_id
- latitude nullable
- longitude nullable
- date_range optional
- notes optional
- sort_order
- created_at
- updated_at
```

---

### 10.4 Trip Stop Ordering

Trip stops must be ordered.

MVP ordering rules:

```text
New stops are appended to the end of the trip.
User can reorder stops manually.
sort_order stores the route order.
```

Auto-order by date is not required in MVP.

---

### 10.5 Trip Detail Page

MVP trip detail page should include:

- title
- status
- date range
- notes
- list of ordered stops
- add stop button
- edit/delete trip actions
- reorder stops
- simple map preview if coordinates exist

Map preview is recommended but not mandatory for the first internal build.

---

### 10.6 Trips List Page

MVP trips list should include:

- list of trips
- trip title
- status
- date summary if available
- number of stops
- create trip button

Optional filters:

- planned
- in progress
- completed
- unknown

Search is nice to have but not mandatory in the first internal build.

---

## 11. MVP Location Search and Manual Fallback

### 11.1 Location Search

MVP should include online location search for trip stops if feasible.

Preferred flow:

```text
Search location
→ select result
→ store selected place locally
→ create trip stop
```

Selected result should provide:

```text
display name
country/territory
latitude
longitude
provider data if useful
```

The exact geocoding provider remains an implementation decision.

Preferred direction:

```text
OpenStreetMap-based search/geocoding
```

---

### 11.2 Manual Fallback

Manual fallback is required.

If search fails or the desired place cannot be found, the user can create a stop manually.

Manual fallback minimum:

```text
location name
country/territory
```

Coordinates are optional.

Manual fallback should still allow:

- trip stop creation
- country/territory derivation
- JSON backup/import

The stop may simply not appear on a precise map until coordinates are added.

---

### 11.3 Place Cache

MVP may store selected places in a `Place` table.

Recommended if not too costly.

Conceptual model:

```text
Place
- id
- provider
- provider_place_id optional
- display_name
- country_id
- latitude nullable
- longitude nullable
- raw_data optional
- created_at
```

Possible providers:

```text
osm
manual
```

For MVP, places may also be stored directly on `TripStop` without a separate cache if that is simpler.

Recommended decision:

```text
Use a Place table only if it does not slow implementation significantly.
```

---

## 12. MVP Dashboard

The MVP dashboard should be simple.

It should give a quick overview of the user’s travel state.

Recommended cards:

```text
Visited places
Wished places
Planned places
Lived places
Trips
```

Optional cards:

```text
Currently living
Recent trip
Upcoming trip
```

The dashboard does not need advanced charts or animated maps.

---

## 13. MVP Countries/Territories List Page

The countries/territories list page should include:

- list of all trackable places
- flag or simple visual marker
- Catalan name
- state summary
- wished indicator
- search
- filter by state

Required filters:

```text
visited
wished
planned
lived
currently living
never visited
```

Optional filters:

```text
continent
type: country / territory / special region
```

The list does not need complex grouping in the first MVP, but grouping by continent is a good improvement if easy.

---

## 14. MVP Country/Territory Detail Page

The detail page should include:

- name
- flag
- type classification
- continent/subregion
- wished toggle
- currently living toggle
- current derived state summary
- manual visit/lived logs
- add log button
- related trip stops
- basic notes/log notes

Optional:

- simple map location
- basic static information such as capital or population
- timeline view

Full stats/info pages are out of scope for MVP.

---

## 15. MVP JSON Backup and Import

### 15.1 Purpose

JSON backup/import is included in MVP because Atlas is local-first.

The user should be able to preserve and move personal data without a cloud account.

---

### 15.2 Backup Format

Format:

```text
JSON
```

The backup should include user-created data:

```text
country user states
country logs
trips
trip stops
cached places if used
settings relevant to tracking
```

The backup does not need to include bundled static datasets:

```text
countries dataset
country stats dataset
airport dataset
```

However, it should include dataset version metadata.

Example:

```json
{
  "backupVersion": 1,
  "createdAt": "2026-05-29T00:00:00Z",
  "countryDatasetVersion": "2026.1",
  "data": {
    "countryUserStates": [],
    "countryLogs": [],
    "trips": [],
    "tripStops": []
  }
}
```

---

### 15.3 Import Rules

Import should handle:

- matching countries/territories by stable code where possible
- restoring wished/currently living state
- restoring logs
- restoring trips
- restoring trip stops
- preserving sort order
- basic validation

Recommended:

```text
Use stable external identifiers in backup payloads, such as ISO codes, not only internal database IDs.
```

For manually added fallback places without ISO ambiguity, store both:

```text
country_id
country_iso2 if available
country_name_snapshot
```

---

## 16. MVP Local Data Architecture

### 16.1 Data Layers

MVP should separate:

```text
Static reference data
User-created data
Derived state
```

Static reference data:

```text
Country
```

User-created data:

```text
CountryUserState
CountryLog
Trip
TripStop
Place optional
```

Derived state:

```text
CountryStateSummary
```

`CountryStateSummary` can be computed dynamically at first. It does not need to be stored unless performance requires it.

---

### 16.2 Static Dataset Files

MVP static dataset:

```text
assets/data/countries.json
```

Optional MVP static dataset:

```text
assets/data/country_stats_summary.json
```

Not needed in MVP:

```text
assets/data/airports.json
assets/data/country_stats_facts.json
assets/data/airlines.json
assets/data/aircraft_types.json
```

---

### 16.3 Minimum Room Entities

Recommended MVP Room entities:

```text
CountryEntity
CountryUserStateEntity
CountryLogEntity
TripEntity
TripStopEntity
```

Optional:

```text
PlaceEntity
```

Do not create flight/itinerary tables in MVP unless there is a strong reason. They can be added in v2.0.

---

## 17. MVP Navigation Structure

Recommended navigation:

```text
Dashboard
Countries
Country Detail
Trips
Trip Detail
Create/Edit Trip
Create/Edit Stop
Settings / Backup
```

Possible bottom navigation:

```text
Dashboard
Countries
Trips
Settings
```

Flights should not appear as a main tab in MVP.

---

## 18. MVP Screens

### 18.1 Dashboard

Required:

- visited count
- wished count
- planned count
- lived count
- trip count

Optional:

- currently living card
- latest updated trip
- upcoming planned trip

---

### 18.2 Countries/Territories List

Required:

- list all trackable countries/territories
- search
- filter by state
- show main state
- show wished marker
- navigate to detail

---

### 18.3 Country/Territory Detail

Required:

- show country/territory name
- show flag/visual marker
- show state summary
- wished toggle
- currently living toggle
- manual logs list
- add/edit/delete log
- related trip stops list

---

### 18.4 Trips List

Required:

- list trips
- create trip
- show title
- show status
- show date range if available
- show stop count
- navigate to detail

---

### 18.5 Trip Detail

Required:

- show title
- show status
- show date range
- show notes
- show ordered stops
- add/edit/delete stop
- reorder stops
- edit/delete trip

Optional:

- simple map preview

---

### 18.6 Stop Creation/Edit

Required:

- location search
- manual fallback
- select country/territory
- optional date range
- optional notes

Minimum fallback:

```text
location name
country/territory
```

---

### 18.7 Settings / Backup

Required:

- export JSON backup
- import JSON backup

Optional:

- app version
- dataset version
- reset data danger zone

---

## 19. MVP Out of Scope

The following are explicitly out of scope for MVP.

### 19.1 Flights and Itineraries

Out of scope:

- solo flights
- flight list/detail
- airport dataset
- itinerary creation
- itinerary groups
- generated itinerary stops
- flight route maps
- flight API lookup/import
- local/UTC flight time management

Reason:

Flights and itineraries are important, but they introduce a second major modelling domain. They are better suited for v2.0 after the country/trip foundation works.

---

### 19.2 Excursions

Out of scope:

- excursions attached to main stops
- excursion stops
- excursion route maps

Reason:

The MVP can represent trips with main stops only. Excursions can be added later without changing the basic trip concept.

---

### 19.3 Photos and Story Mode

Out of scope:

- trip photos
- stop photos
- slideshow/story mode
- photo memory pages

Reason:

Photos are emotionally important but not required to validate the core tracking model.

---

### 19.4 Advanced Stats

Out of scope:

- advanced dashboard charts
- country comparisons
- full 50–150 field country info pages
- detailed stats facts table UI
- global travel analytics

Reason:

The MVP only needs simple counts.

---

### 19.5 Cloud and Social Features

Out of scope:

- account system
- cloud sync
- social sharing
- collaborative trips
- public profiles

Reason:

Atlas is local-first and personal.

---

### 19.6 Multi-Language Support

Out of scope:

- Spanish UI
- English UI
- language selector

Reason:

The app is Catalan-first. Multi-language support is low priority.

---

## 20. MVP Development Phases

### Phase 0: Project Setup and Decisions

Deliverables:

- Android project
- package structure
- theme baseline
- navigation baseline
- Room setup
- decision on country/territory dataset source
- decision on location search provider

---

### Phase 1: Country/Territory Foundation

Build:

- static countries/territories import
- CountryEntity
- countries list
- country detail
- wished toggle
- currently living toggle
- basic state derivation from user state

Success:

```text
The user can browse countries/territories and mark wished/currently living.
```

---

### Phase 2: Manual Country Logs

Build:

- CountryLogEntity
- add/edit/delete visit log
- add/edit/delete lived log
- flexible date input
- notes
- state derivation from logs

Success:

```text
Manual visit/lived logs update country/territory states correctly.
```

---

### Phase 3: Trips

Build:

- TripEntity
- trips list
- trip detail
- create/edit/delete trip
- trip status
- flexible date range
- notes

Success:

```text
The user can create and manage simple trips.
```

---

### Phase 4: Trip Stops

Build:

- TripStopEntity
- add/edit/delete trip stop
- manual fallback stop creation
- stop ordering
- related trip stops in country detail
- state derivation from trip stops

Success:

```text
Trip stops update planned/visited country states based on trip status.
```

---

### Phase 5: Location Search

Build:

- location search abstraction
- OSM-based search provider or chosen provider
- selected result mapping to country/territory
- manual fallback if search fails
- optional PlaceEntity

Success:

```text
The user can add stops by searching locations, while manual fallback remains available.
```

---

### Phase 6: Backup/Import

Build:

- JSON export
- JSON import
- backup version metadata
- dataset version metadata
- basic validation

Success:

```text
The user can export all MVP personal data and import it again.
```

---

### Phase 7: Polish and Internal Release

Build:

- UI polish
- empty states
- validation messages
- delete confirmations
- basic error handling
- simple dashboard improvements
- internal testing with real travel data

Success:

```text
The MVP feels usable as a personal travel tracker.
```

---

## 21. MVP Success Criteria

Atlas MVP is successful if the user can:

1. Open the app and see a basic dashboard.
2. View all countries/territories.
3. Search/filter countries/territories.
4. Mark countries/territories as wished.
5. Mark one country/territory as currently living.
6. Add manual visit logs.
7. Add manual lived logs.
8. Create a trip with only a title.
9. Set trip status to planned, in progress, completed, or unknown.
10. Add ordered stops to a trip.
11. Create stops through location search.
12. Create stops manually if search fails.
13. See countries/territories automatically marked as planned/visited from trip stops.
14. See countries/territories marked as visited/lived from manual logs.
15. Export personal data as JSON.
16. Import personal data from JSON.
17. Use the app without creating an account or using a backend.

---

## 22. MVP Quality Bar

The MVP does not need to be feature-complete, but it should be stable and pleasant enough to use.

Minimum quality expectations:

```text
No crashes in normal flows
Clear empty states
Basic form validation
No forced unnecessary fields
Consistent Catalan UI text
Reliable local persistence
Reliable JSON export/import
Reasonable navigation
Basic visual polish
```

The MVP should not feel like a prototype where data may be lost.

---

## 23. Risks and Mitigations

### Risk 1: MVP Becomes Too Large

Mitigation:

```text
Do not add flights, itineraries, excursions, photos, or advanced stats before MVP completion.
```

---

### Risk 2: Country/Territory Dataset Becomes a Time Sink

Mitigation:

```text
Start with a practical dataset.
Classify entities enough for future compatibility.
Improve data quality later.
```

---

### Risk 3: Location Search Delays MVP

Mitigation:

```text
Manual fallback is required.
Location search can be added after manual trip stops if needed.
```

---

### Risk 4: Flexible Dates Increase UI Complexity

Mitigation:

```text
Start with a simple date precision selector.
Do not over-automate status inference in MVP.
```

---

### Risk 5: Derived State Logic Gets Confusing

Mitigation:

```text
Centralize derivation in one service/use case.
Write clear unit tests for country state derivation.
```

Recommended derivation inputs:

```text
CountryUserState
CountryLog
Trip
TripStop
```

---

## 24. Recommended MVP Implementation Order

Recommended practical order:

```text
1. Android project foundation
2. Room setup
3. Country dataset import
4. Countries list
5. Country detail
6. Wished/currently living state
7. Manual country logs
8. Trip list/detail
9. Trip creation/editing
10. Manual trip stops
11. Country state derivation from trips
12. Location search
13. JSON export
14. JSON import
15. Polish
```

Important note:

```text
Manual trip stops should come before location search.
```

This ensures the trip system can work even if geocoding takes longer than expected.

---

## 25. Summary

Atlas MVP should establish the smallest useful version of the app:

```text
A local-first Android travel atlas focused on countries/territories and simple trips.
```

The MVP should include:

- countries/territories
- wished/currently living markers
- manual visit/lived logs
- simple trips
- ordered trip stops
- location search with manual fallback
- derived country/territory states
- JSON backup/import

The MVP should exclude:

- flights
- itineraries
- excursions
- photos
- story mode
- advanced stats
- cloud sync
- multi-language support

The goal is to build a strong foundation first. Once this version works and feels useful, v2.0 can add flights, itineraries, itinerary groups, generated stops, flight routes, and richer travel modelling.
