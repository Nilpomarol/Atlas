# Atlas MVP v1 Specification

## 1. Product Vision

Atlas is a native Android app for personal travel tracking. It combines country tracking, flight tracking, and trip tracking into one local-first, visually rich travel memory app.

The app is designed primarily as a personal project, optimized for the owner’s preferences rather than for broad public release. The goal is to make tracking travels easy, flexible, visual, and meaningful without forcing complete data entry.

Atlas should feel closer to a personal travel atlas than to a productivity tool.

---

## 2. Project Principles

These principles override lower-level implementation decisions.

### 2.1 Native Android only

Atlas is an Android app.

The first and foreseeable version targets Android only. No iOS, web, or cross-platform requirement exists for MVP v1.

Recommended technical direction:

- Kotlin
- Jetpack Compose
- Room database
- Local-first architecture
- OpenStreetMap-based map/search direction

---

### 2.2 Local-first user data

User data should be stored locally on the device.

This includes:

- country logs
- wished countries
- currently living country
- flights
- itineraries
- trips
- stops
- excursions
- notes
- future photo references

External services may be used for lookup or enrichment, such as:

- location search
- map tiles
- future flight API lookup

However, the user’s personal travel data should not depend on an online backend.

Local-first does **not** mean the entire app must work fully offline. Online location search is acceptable.

---

### 2.3 Free or extremely cheap external services

External services should be avoided unless they provide clear value.

If used, they should be:

- free, or
- extremely cheap, or
- replaceable with another provider later.

Preferred direction:

- OpenStreetMap ecosystem for maps/location-related functionality.
- Avoid mandatory paid APIs in MVP.

---

### 2.4 Non-obligatory data entry

Atlas should never force the user to provide more information than is technically required for a record to exist.

Examples:

- Trips can be created with only a title.
- Trips can have zero stops.
- Flights can be created with only origin and destination.
- Country visits can be created without a date.
- Stops should normally use location search, but manual fallback should exist if search fails.
- Notes are always optional.
- Dates are optional unless required by a specific future feature.

---

### 2.5 Flexible dates

Dates can be incomplete.

Supported precisions:

- year only: `2023`
- month and year: `06-2023`
- full date: `14-06-2023`

Date ranges are supported.

If a date range has both start and end dates, both must use the same precision.

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

---

### 2.6 Visual impact

Atlas should be visually impactful.

Important visual areas:

- main dashboard
- country map
- trip map
- flight routes
- country stats pages
- future trip slideshow/story mode

The MVP does not need to implement every visual feature, but the architecture should support them.

---

### 2.7 Catalan-first language

The app UI should be in Catalan.

Static data direction:

- Country names in Catalan.
- Cities in common Catalan form where available.
- Airports in official/common names.

Spanish and English translations are not part of MVP.

---

## 3. MVP Scope

MVP v1 should provide a usable local-first travel tracker covering the three core domains:

1. Countries
2. Flights
3. Trips

The MVP should include the foundational data model for future expansion, even when the initial UI is simplified.

---

## 4. Included in MVP v1

### 4.1 Countries

MVP should include:

- static country list
- country detail page
- manual country visit logs
- manual country lived logs
- wished country toggle
- currently living country toggle
- derived country states
- basic country stats
- prepared structure for detailed country info dataset

Country states:

```text
never visited
visited
lived
wished
planned
currently living
```

---

### 4.2 Flights

MVP should include:

- manual solo flight creation
- manual itinerary creation
- itinerary groups
- flights inside itinerary groups
- flight list
- flight detail page
- airport static dataset
- country tracking derivation from solo flights and itinerary groups
- detailed database fields for planned/actual flight times, even if UI starts simpler

---

### 4.3 Trips

MVP should include:

- create trip with title only
- trip status
- optional date range
- optional notes
- main trip stops
- location search for stops
- manual fallback for stop location if search fails
- excursions attached to main stops
- excursion stops
- one optional itinerary linked to a trip
- generated trip stops from itinerary groups
- trip list
- trip detail page

---

### 4.4 Location search

MVP should include online location search.

Selected search results should be stored locally.

If search fails, the app should show a manual fallback.

Manual fallback minimum:

```text
location name + country
```

Coordinates are expected from search results, but optional in fallback mode.

---

### 4.5 JSON backup/import

MVP should use JSON for backup/import.

CSV export is not part of MVP.

The JSON backup should focus on user-created data, not necessarily bundled static reference datasets.

---

### 4.6 Photo data preparation

The full photo UI is not required in MVP v1, but the data model should be prepared for photos.

Future photo support should be possible for:

- trips
- trip stops
- excursions
- excursion stops

---

## 5. Explicitly Out of Scope for MVP v1

The following are not required in MVP v1:

- flight API automatic import
- airline logos
- aircraft images
- advanced global stats page
- full trip slideshow/story mode
- cloud sync
- social sharing
- multi-language support
- CSV export
- complex offline maps
- advanced country comparison tools
- full 50–150 field country info UI for every country

The data model should still prepare for some of these where reasonable.

---

## 6. Core Concepts

## 6.1 Country

A country is a static reference entity representing a recognized country or territory tracked by the app.

Country identity/reference data should be separate from:

- user tracking state
- manual logs
- large country stats/info dataset

---

## 6.2 Country user state

Country user state stores simple personal booleans for a country.

Examples:

- wished
- currently living

This is personal user data and should be included in JSON backup.

---

## 6.3 Country log

A country log is a manual personal entry attached to a country.

Supported MVP log types:

```text
visit
lived
```

Logs can have:

- optional date range
- optional notes

---

## 6.4 Flight

A flight is an individual air travel segment from one airport to another.

A flight can exist as:

- a solo flight
- part of an itinerary group

Minimum required fields:

```text
origin airport
destination airport
```

---

## 6.5 Itinerary

An itinerary is a collection of related flights.

Itineraries are primarily used to group multiple flights that belong together.

An itinerary can exist independently or be linked to one trip.

MVP rule:

```text
A trip can have zero or one itinerary.
```

---

## 6.6 Itinerary group

An itinerary group represents a meaningful travel leg inside an itinerary.

Example:

```text
Itinerary: Japan 2026

Group 1: Outbound
- Barcelona → Doha
- Doha → Tokyo

Group 2: Return
- Osaka → Doha
- Doha → Barcelona
```

Groups are important because they distinguish meaningful destinations from layovers.

---

## 6.7 Trip

A trip is a travel experience.

Minimum required field:

```text
title
```

A trip may have:

- status
- date range
- notes
- main stops
- excursions
- one linked itinerary

---

## 6.8 Trip stop

A trip stop is a main location in the trip route.

Trip stops can be:

- manually created
- generated from itinerary groups

Generated itinerary stops should appear in the trip but should not have their core location directly edited.

---

## 6.9 Excursion

An excursion is a side route attached to a main trip stop.

Example:

```text
Main stop: Tokyo

Excursion: Kamakura and Yokohama
- Kamakura
- Yokohama
```

This allows the app to represent routes such as:

```text
Tokyo → Kamakura → Yokohama → Tokyo
```

rather than treating Kamakura and Yokohama as unrelated child stops.

---

## 6.10 Excursion stop

An excursion stop is a location visited as part of an excursion.

Excursion stops count like normal stops for country tracking.

---

## 7. Status Rules

Trips and flights should have explicit status because dates are optional.

Supported statuses:

```text
planned
completed
unknown
```

### 7.1 Why explicit status is required

If a trip or flight has no date, the app cannot know whether it is past, future, or uncertain.

Example:

```text
Trip: Portugal
Date: empty
```

Without a status, the app cannot determine whether Portugal should count as visited or planned.

Therefore:

```text
Trip: Portugal
Status: completed
Date: empty
```

Portugal counts as visited.

```text
Trip: Iceland
Status: planned
Date: empty
```

Iceland counts as planned.

---

## 8. Date Rules

### 8.1 FlexibleDate

Conceptual model:

```text
FlexibleDate
- year
- month nullable
- day nullable
- precision: year | month | day
```

Examples:

```text
2023
06-2023
14-06-2023
```

---

### 8.2 FlexibleDateRange

Conceptual model:

```text
FlexibleDateRange
- start_date nullable
- end_date nullable
- precision: year | month | day
```

Rules:

- start date can be empty
- end date can be empty
- both can be present
- if both are present, both must use the same precision

---

## 9. Country State Rules

Country states are derived from manual user data, trips, flights, and itinerary groups.

---

### 9.1 Visited

A country counts as visited if any of the following are true:

1. There is a manual visit log.
2. There is a manual lived log.
3. The country is currently living.
4. There is a completed trip stop in that country.
5. There is a completed excursion stop in that country.
6. There is a completed generated itinerary stop in that country.
7. There is a completed solo flight whose destination is in that country.
8. There is a completed solo flight whose origin is in that country and `origin_counts_for_country_tracking = true`.

---

### 9.2 Planned

A country counts as planned if any of the following are true:

1. There is a planned trip stop in that country.
2. There is a planned excursion stop in that country.
3. There is a planned generated itinerary stop in that country.
4. There is a planned solo flight whose destination is in that country.
5. There is a planned solo flight whose origin is in that country and `origin_counts_for_country_tracking = true`.

There is no manual planned country marker in MVP.

---

### 9.3 Wished

A country is wished if:

```text
CountryUserState.wished = true
```

Wished can coexist with:

- visited
- planned
- lived
- currently living

---

### 9.4 Lived

A country counts as lived if:

1. There is a manual lived log.
2. The country is currently living.
3. The country was previously set as currently living and a lived history entry was created or maintained.

Lived implies visited.

---

### 9.5 Currently living

A country is currently living if:

```text
CountryUserState.currently_living = true
```

Rules:

- only one country can be currently living at a time
- setting a new currently living country unsets the previous one
- setting currently living creates or maintains a lived log/history entry
- currently living implies lived
- currently living implies visited

---

### 9.6 Never visited

A country is never visited if it does not satisfy any visited or lived condition.

A country can be never visited and wished.

A country can be never visited and planned.

---

## 10. Flight and Itinerary Rules

### 10.1 Layovers

Layovers do not count as country visits.

Example:

```text
Barcelona → Doha → Tokyo
```

If Doha is only a connection, Qatar does not count as visited.

This is why itinerary groups are important.

---

### 10.2 Solo flight country derivation

Solo flights count the destination country by default.

Example:

```text
Barcelona → Tokyo
```

Japan counts.

Solo flights have an optional flag:

```text
origin_counts_for_country_tracking
```

Example:

```text
Tokyo → Barcelona
origin_counts_for_country_tracking = true
```

Japan also counts.

Default values:

```text
destination_counts_for_country_tracking = true
origin_counts_for_country_tracking = false
```

---

### 10.3 Grouped flight country derivation

For flights inside itinerary groups, country derivation should come from the itinerary group generated stop, not from every flight segment.

This prevents layovers from incorrectly counting as visited.

---

### 10.4 Itinerary group generated stop rule

For each itinerary group linked to a trip, generate one main trip stop.

Rule:

| Group position | Generated stop location |
|---|---|
| Not the last group | Destination of the last flight in the group |
| Last group | Origin of the first flight in the group |

Example:

```text
Group 1:
Barcelona → Doha
Doha → Tokyo

Generated stop:
Tokyo
```

```text
Group 2:
Osaka → Doha
Doha → Barcelona

Generated stop:
Osaka
```

This avoids creating a final generated stop for the home airport.

---

## 11. Trip and Stop Rules

### 11.1 Trip minimum data

A trip can be created with only:

```text
title
```

Everything else is optional.

---

### 11.2 Trip status

Trips have status:

```text
planned
completed
unknown
```

Status affects country derivation.

---

### 11.3 Main trip stops

Main trip stops represent the primary route of the trip.

Example:

```text
Tokyo → Kyoto → Osaka
```

Main stops can be:

- manual
- generated from itinerary groups

---

### 11.4 Excursions

Excursions represent side routes attached to main stops.

Example:

```text
Main stop: Tokyo

Excursion:
Tokyo → Kamakura → Yokohama → Tokyo
```

An excursion belongs to:

- one trip
- one anchor main trip stop

---

### 11.5 Excursion stops

Excursion stops belong to excursions.

They count for:

- country tracking
- trip map
- trip timeline
- future slideshow/story mode

---

### 11.6 Generated itinerary stops

Generated itinerary stops are created from itinerary groups.

Core generated fields should not be directly editable:

- location
- country
- coordinates
- derived date

To change these, the user should edit the itinerary group or flights.

Potentially editable fields:

- display title
- notes
- future photos
- visibility in trip

---

## 12. Data Model Draft

This section describes the conceptual MVP data model. Exact Room implementation may differ.

---

## 12.1 Country

```text
Country
- id
- iso2
- iso3
- name_ca
- name_en optional
- continent
- subregion optional
- flag_emoji optional
- flag_asset optional
- latitude optional
- longitude optional
```

Purpose:

- stable country identity/reference data
- used across countries, trips, flights, airports, maps, and stats

This should not contain user logs or the full 50–150 field country stats dataset.

---

## 12.2 CountryUserState

```text
CountryUserState
- country_id
- wished: boolean
- currently_living: boolean
- updated_at
```

Constraint:

```text
Only one country can have currently_living = true.
```

---

## 12.3 CountryLog

```text
CountryLog
- id
- country_id
- type: visit | lived
- date_range optional
- notes optional
- created_at
- updated_at
```

---

## 12.4 CountryStatsSummary

A small structured dataset for frequently used country stats.

```text
CountryStatsSummary
- country_id
- capital optional
- population optional
- area_km2 optional
- gdp_nominal optional
- gdp_per_capita optional
- hdi optional
- life_expectancy optional
- currency optional
- calling_code optional
- timezone_summary optional
- updated_at
- dataset_version
```

Purpose:

- power top-level country cards
- quick display on country pages
- frequently used comparisons

---

## 12.5 CountryStatFact

Flexible detailed country statistics dataset.

Used for the future detailed country info page with 50–150 fields.

```text
CountryStatFact
- id
- country_id
- category
- key
- label_ca
- value_text nullable
- value_number nullable
- value_boolean nullable
- value_type: text | integer | decimal | percentage | currency | boolean | list | url
- unit nullable
- year nullable
- source_name nullable
- source_url nullable
- display_order
- updated_at
- dataset_version
```

Suggested categories:

```text
geography
demographics
economy
government
health
education
culture
rights
environment
tourism
transport
```

This flexible model avoids database migrations every time a new country stat is added.

---

## 12.6 Airport

```text
Airport
- id
- iata
- icao optional
- name
- city
- country_id
- latitude
- longitude
- timezone optional
```

Airport names should use official/common names.

---

## 12.7 Flight

```text
Flight
- id
- origin_airport_id
- destination_airport_id
- status: planned | completed | unknown

- planned_departure_datetime optional
- planned_arrival_datetime optional
- actual_departure_datetime optional
- actual_arrival_datetime optional

- airline optional
- flight_number optional
- aircraft_type optional
- notes optional

- itinerary_group_id nullable

- destination_counts_for_country_tracking: boolean default true
- origin_counts_for_country_tracking: boolean default false

- created_at
- updated_at
```

The UI can initially expose fewer fields, but the database should prepare for detailed flight tracking.

---

## 12.8 Itinerary

```text
Itinerary
- id
- title
- trip_id nullable unique
- notes optional
- created_at
- updated_at
```

`trip_id` is unique to enforce:

```text
one trip → maximum one itinerary
```

---

## 12.9 ItineraryGroup

```text
ItineraryGroup
- id
- itinerary_id
- title optional
- sort_order
- created_at
- updated_at
```

---

## 12.10 Trip

```text
Trip
- id
- title
- status: planned | completed | unknown
- date_range optional
- notes optional
- created_at
- updated_at
```

---

## 12.11 TripStop

Main trip stops.

```text
TripStop
- id
- trip_id
- source: manual | itinerary_group
- itinerary_group_id nullable

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

If `source = itinerary_group`, core location fields are generated.

---

## 12.12 Excursion

```text
Excursion
- id
- trip_id
- anchor_trip_stop_id
- title optional
- date_range optional
- notes optional
- sort_order
- created_at
- updated_at
```

---

## 12.13 ExcursionStop

```text
ExcursionStop
- id
- excursion_id

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

## 12.14 Place

Cached selected location from search or manual fallback.

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
future_provider
```

---

## 12.15 Future photo-related models

Photos do not need full MVP UI support, but the data model should prepare for them.

Possible future models:

```text
Photo
- id
- local_uri
- caption optional
- taken_at optional
- created_at
- updated_at
```

Associations:

```text
TripPhoto
- trip_id
- photo_id
- sort_order
```

```text
TripStopPhoto
- trip_stop_id
- photo_id
- sort_order
```

```text
ExcursionPhoto
- excursion_id
- photo_id
- sort_order
```

```text
ExcursionStopPhoto
- excursion_stop_id
- photo_id
- sort_order
```

Photos should be local-first.

---

## 13. Country Data Architecture

Country-related data should be split into separate layers.

### 13.1 Layer 1: Country identity/reference data

```text
Country
```

Stable basic information.

Examples:

- ISO codes
- Catalan name
- continent
- coordinates
- flag

---

### 13.2 Layer 2: User country tracking data

```text
CountryUserState
CountryLog
```

Personal user data.

Examples:

- wished
- currently living
- manual visits
- manual lived logs

This must be included in JSON backup.

---

### 13.3 Layer 3: Country statistics/info dataset

```text
CountryStatsSummary
CountryStatFact
```

Static or semi-static dataset for country info pages.

Examples:

- population
- GDP
- HDI
- education stats
- rights indicators
- health stats
- cultural facts
- geography facts

This should be updateable without affecting user data.

---

## 14. Static Dataset Files

Recommended bundled dataset files:

```text
assets/data/countries.json
assets/data/country_stats_summary.json
assets/data/country_stats_facts.json
assets/data/airports.json
```

---

### 14.1 countries.json

Contains country identity/reference data.

---

### 14.2 country_stats_summary.json

Contains frequently used structured country stats.

---

### 14.3 country_stats_facts.json

Contains detailed flexible stat facts.

Example:

```json
{
  "countryIso2": "JP",
  "category": "demographics",
  "key": "population",
  "labelCa": "Població",
  "valueNumber": 124500000,
  "valueType": "integer",
  "unit": "people",
  "year": 2024,
  "sourceName": "World Bank",
  "sourceUrl": "https://data.worldbank.org/",
  "displayOrder": 1
}
```

Example:

```json
{
  "countryIso2": "JP",
  "category": "government",
  "key": "government_type",
  "labelCa": "Tipus de govern",
  "valueText": "Monarquia constitucional parlamentària",
  "valueType": "text",
  "year": 2024,
  "displayOrder": 3
}
```

---

### 14.4 airports.json

Contains airport reference data.

---

## 15. JSON Backup and Import

MVP backup/import format:

```text
JSON only
```

The JSON backup should include user-created data:

- country user states
- country logs
- flights
- itineraries
- itinerary groups
- trips
- trip stops
- excursions
- excursion stops
- cached places if useful
- future photo references

The JSON backup does not need to include full bundled static datasets, such as:

- country identity dataset
- country stats dataset
- airport dataset

However, it should include dataset version metadata.

Example:

```json
{
  "backupVersion": 1,
  "createdAt": "2026-05-07T00:00:00Z",
  "countryDatasetVersion": "2026.1",
  "airportDatasetVersion": "2026.1",
  "countryStatsDatasetVersion": "2026.1",
  "data": {}
}
```

---

## 16. External Services Policy

Atlas should minimize external dependencies.

Allowed MVP external service areas:

- map tiles
- location search/geocoding

Preferred direction:

```text
OpenStreetMap-based ecosystem
```

Location search may require internet.

Selected results should be saved locally.

If search fails, manual fallback should appear.

---

## 17. Screen List for MVP

## 17.1 Main dashboard

Should show high-level overview.

Potential cards:

- countries summary
- flights summary
- trips summary
- upcoming planned items

MVP version can be simple.

---

## 17.2 Countries list page

Features:

- list countries by continent
- search countries
- filter by state:
  - visited
  - lived
  - wished
  - planned
  - currently living
  - never visited
- show flag, name, state summary, latest relevant log/event

---

## 17.3 Country detail page

Features:

- country header
- flag
- name
- basic stats
- wished toggle
- currently living toggle
- manual visit/lived log list
- combined timeline of related events:
  - manual logs
  - trip stops
  - excursion stops
  - generated itinerary stops
  - solo flights
- entry detail modal or screen

---

## 17.4 Country stats/info page

MVP can be simple but should use the separated stats dataset.

Sections may include:

- geography
- demographics
- economy
- government
- health
- education
- culture
- rights
- environment

Full visual treatment can come later.

---

## 17.5 Flights list page

Features:

- list all flights
- group flights by itinerary where applicable
- show solo flights separately
- filter by:
  - planned
  - completed
  - unknown
- search by:
  - airport
  - city
  - country
  - airline
  - flight number

---

## 17.6 Flight detail page

Features:

- origin
- destination
- status
- planned/actual date/time fields
- airline
- flight number
- aircraft type
- notes
- itinerary/group relationship if applicable
- country tracking toggles for solo flights where relevant

---

## 17.7 Itinerary management page

Features:

- create/edit itinerary
- create/edit itinerary groups
- add flights to groups
- reorder groups
- reorder flights inside groups
- show generated stop preview if linked to trip

---

## 17.8 Trips list page

Features:

- list trips
- filter by:
  - planned
  - completed
  - unknown
- highlight current/ongoing trip later if dates support it
- search trips
- create trip button

---

## 17.9 Trip detail page

Features:

- title
- status
- date range
- notes
- main route stops
- generated itinerary stops
- excursions
- linked itinerary summary
- map preview where coordinates exist

---

## 17.10 Stop creation flow

Preferred flow:

1. Search location online.
2. Select result.
3. Store selected place locally.
4. Create stop.

If search fails:

1. Show manual fallback.
2. Require:
   - location name
   - country
3. Coordinates optional.

---

## 17.11 Excursion management flow

Features:

- create excursion from a main stop
- add ordered excursion stops
- optional title
- optional date range
- optional notes
- map route preview where coordinates exist

---

## 18. Maps Direction

Preferred map direction:

```text
OpenStreetMap-based
```

MVP map requirements can be limited.

Initial map usage:

- display trip stops
- display excursion routes
- display flight route lines later
- display country overview later

Advanced country polygon coloring can be implemented after core MVP if needed.

---

## 19. Development Phases

## Phase 0: Specification and technical decisions

Deliverables:

- this MVP specification
- technical architecture decision
- initial data model
- dataset format decisions
- screen flow sketches

---

## Phase 1: Android foundation

Build:

- Kotlin Android project
- Jetpack Compose setup
- Room database
- Navigation
- app theme
- Catalan strings
- local dataset import mechanism

---

## Phase 2: Countries foundation

Build:

- country static dataset import
- countries list
- country detail
- wished toggle
- currently living toggle
- manual visit/lived logs
- country state derivation service

---

## Phase 3: Flights and itineraries

Build:

- airport static dataset import
- manual solo flight creation
- flight list/detail
- itinerary creation
- itinerary groups
- flights inside groups
- generated stop preview logic

---

## Phase 4: Trips, stops, and excursions

Build:

- trip list/detail
- trip creation
- location search abstraction
- manual location fallback
- main stops
- excursions
- excursion stops
- link itinerary to trip
- generated trip stops from itinerary groups

---

## Phase 5: Maps and visual improvements

Build:

- trip map
- excursion route rendering
- flight route rendering
- country map exploration

---

## Phase 6: Backup/import

Build:

- JSON export
- JSON import
- backup versioning
- dataset version metadata

---

## 20. Future Enhancements

Possible post-MVP features:

- flight API lookup
- aircraft images
- airline logos
- trip photo UI
- full slideshow/story mode
- advanced country info pages
- global stats dashboard
- country comparison
- interactive world map with country coloring
- advanced import/export
- optional cloud backup
- richer map styling
- local map caching
- advanced airport/airline/aircraft datasets

---

## 21. Open Implementation Questions

The main product decisions are now defined, but these implementation-level questions remain:

1. Which specific OSM-based map library should be used on Android?
2. Which location search/geocoding provider should be used first?
3. How large should the initial airport dataset be?
4. How many country stats should be bundled in the first version?
5. Should generated itinerary stops be physically stored or computed dynamically?
6. How should flexible dates be represented internally in Room?
7. How should JSON backup handle future schema migrations?
8. Should cached places be deduplicated by provider ID, coordinates, or name/country?
9. How should country state derivation be cached for performance?
10. What is the first visual design direction for the app?

---

## 22. MVP Success Criteria

Atlas MVP v1 is successful if the user can:

1. View all countries.
2. Mark countries as wished.
3. Mark one country as currently living.
4. Add manual visit/lived logs.
5. Create solo flights.
6. Create itineraries with groups and flights.
7. Create trips with only a title.
8. Add main trip stops using location search.
9. Use manual stop fallback if search fails.
10. Create excursions from main stops.
11. Link one itinerary to a trip.
12. See generated trip stops from itinerary groups.
13. See countries automatically marked as visited/planned based on trips and flights.
14. Store all personal data locally.
15. Export/import personal data as JSON.

---

## 23. Summary

Atlas MVP v1 should establish the core local-first travel tracking foundation.

The most important architectural decisions are:

- separate static country identity data from user tracking data
- separate large country stats dataset from the core country object
- use explicit status for trips and flights
- support flexible dates from the start
- include itinerary groups in MVP
- use excursions for side routes
- prepare for photos in the data model
- use JSON backup/import
- use OpenStreetMap-based direction for maps/location functionality

This specification should guide the first implementation phase while leaving room for the richer visual and statistical features planned for future versions.