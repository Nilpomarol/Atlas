# Atlas Product Specification

## 0. Document Purpose

This document defines the long-term product specification for **Atlas**, a native Android travel tracking app.

It describes what the full application should become. It is not the source of truth for current implementation status or the current Room schema.

Use:

- `docs/Handoff_Prompt.md` for implemented behavior and active work;
- `docs/Atlas_Technical_Architecture.md` for current architecture;
- `docs/Atlas_Data_Model.md` for the implemented conceptual data model;
- this document for long-term product direction.

---

## 1. Product Vision

Atlas is a native Android app for personal travel tracking.

It combines:

- country and territory tracking
- trip tracking
- flight tracking
- itinerary tracking
- travel timelines
- maps
- statistics
- personal notes
- future photo-based memories

The app is designed primarily as a personal project, optimized for the owner’s preferences rather than for broad public release. The goal is to make travel tracking easy, flexible, visual, and meaningful without forcing complete data entry.

Atlas should feel closer to a **personal travel atlas** than to a productivity tool.

It should answer questions such as:

```text
Where have I been?
Where have I lived?
Where do I want to go?
Which trips have I made?
Which flights have I taken?
Which countries or territories have I visited through trips, flights, or manual logs?
What memories, notes, maps, and stats are attached to each place?
```

The emotional value of the app is as important as the data value. Atlas should not only store travel records; it should help the user revisit, understand, and visualize their travel history.

---

## 2. Product Identity

Atlas is not primarily:

- a travel booking app
- a social travel app
- a productivity planner
- a generic notes app
- a replacement for Google Maps
- a commercial flight tracker

Atlas is primarily:

- a personal travel memory system
- a local-first travel database
- a visual atlas of visited, lived, wished, and planned places
- a structured record of trips, flights, stops, and excursions
- a long-term personal archive

The app should prioritize:

```text
personal meaning > public sharing
flexibility > strict completeness
visual clarity > dense data entry
local ownership > cloud dependency
structured travel memory > generic planning
```

---

## 3. Core Product Principles

These principles override lower-level implementation decisions.

---

### 3.1 Native Android First

Atlas is a native Android app.

The first and foreseeable version targets Android only. No iOS, web, or cross-platform requirement exists.

Recommended technical direction:

- Kotlin
- Jetpack Compose
- Room database
- Local-first architecture
- OpenStreetMap-based map/search direction

A future desktop, web, or multiplatform version is not impossible, but it should not influence early architecture unless the cost is very low.

---

### 3.2 Local-First User Data

User-created travel data should be stored locally on the device.

This includes:

- country user states
- country logs
- wished countries
- currently living country
- trips
- trip stops
- excursions
- excursion stops
- flights
- itineraries
- itinerary groups
- notes
- photo references
- cached selected places
- backup metadata

External services may be used for lookup or enrichment, such as:

- location search
- geocoding
- map tiles
- optional future flight lookup
- optional future country/stat dataset updates

However, the user’s personal travel data should not depend on an online backend.

Local-first does **not** mean the entire app must work fully offline. Online location search and map tiles are acceptable. The important rule is that personal records remain owned by the user and usable without a mandatory account or backend.

---

### 3.3 Free or Extremely Cheap External Services

Atlas should avoid external dependencies unless they provide clear value.

If external services are used, they should be:

- free
- extremely cheap
- replaceable
- non-essential to core personal data access

Preferred direction:

```text
OpenStreetMap-based ecosystem for maps and location-related functionality.
```

Atlas should avoid mandatory paid APIs for core functionality.

---

### 3.4 Non-Obligatory Data Entry

Atlas should never force the user to provide more information than is technically required for a record to exist.

Examples:

- Trips can be created with only a title.
- Trips can have zero stops.
- Flights can be created with only origin and destination.
- Country visits can be created without a date.
- Country lived logs can be created without an end date.
- Stops should normally use location search, but manual fallback should exist.
- Notes are always optional.
- Dates are optional unless a specific feature requires them.
- Photos are optional.
- Coordinates are optional for manually created fallback places.

The app should support both quick capture and detailed records.

---

### 3.5 Flexible Dates

Travel memories are often approximate. Atlas should support incomplete dates from the beginning.

Supported date precisions:

```text
year only:       2023
month and year: 06-2023
full date:      14-06-2023
```

Date ranges are supported.

If a date range has both start and end dates, both dates should use the same precision.

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

Flexible dates should be used by:

- country logs
- trips
- trip stops
- excursions
- excursion stops
- future photo metadata where appropriate

Flights may require more precise date/time modelling because of local and UTC times.

---

### 3.6 Explicit Status With Smart Date Assistance

Trips and flights should store explicit status because dates are optional and may be incomplete.

Supported statuses:

```text
planned
in_progress
completed
unknown
```

Status is the source of truth.

Dates may suggest a status when enough information is provided, but the user should be able to override it.

Recommended conceptual model:

```text
status: planned | in_progress | completed | unknown
status_source: manual | inferred
```

Rules:

- If `status_source = inferred`, the app may update status automatically when date information changes.
- If `status_source = manual`, the app should not override the user’s chosen status.
- Users can always manually change status.
- Automatic inference should be conservative when dates are incomplete.

Examples:

```text
Trip date: 14-06-2024 → 20-06-2024
Current date: 2026
Suggested status: completed
```

```text
Trip date: 2026
Current date: May 2026
Suggested status: unknown or planned
```

```text
Trip date: 06-2026
Current date: May 2026
Suggested status: planned
```

This preserves flexibility while allowing helpful automation.

---

### 3.7 Visual Impact

Atlas should be visually impactful.

Important visual areas:

- main dashboard
- world/country map
- country detail pages
- country stats/info pages
- trip map
- excursion route maps
- flight routes
- timelines
- travel statistics
- future slideshow/story mode

The app should feel polished, personal, and exploratory.

The goal is not only to manage data, but to make the user want to open the app and browse their travel history.

---

### 3.8 Catalan-First Language

The app UI should be in Catalan.

Static data direction:

- Country and territory names in Catalan.
- Cities in common Catalan form where available.
- English or original names are acceptable fallbacks for cities.
- Airports should use official/common names.
- Spanish and English translations are not part of the initial product direction and are very low priority.

This does not prevent internal code, database names, comments, and documentation from being written in English.

---

## 4. Country and Territory Model Decision

Atlas should use a broad **countries and territories** dataset rather than limiting itself only to sovereign states.

The app is a personal travel atlas, not a diplomatic registry. Some travel-relevant places are not sovereign countries but still feel distinct from a travel perspective.

Examples:

- Hong Kong
- Greenland
- Faroe Islands
- Puerto Rico
- Aruba
- Curaçao
- French Polynesia
- Gibraltar
- Guadeloupe
- Martinique

Recommended base direction:

```text
Use an ISO 3166-1-style countries-and-territories dataset.
```

Each entity should be classified.

Conceptual model:

```text
CountryOrTerritory
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

The UI should support separate statistics:

```text
Sovereign countries visited
Territories/special regions visited
Total tracked places visited
```

Recommended default:

```text
Track countries + territories.
Show strict country counts separately from total tracked entities.
```

This avoids future regret. If Atlas starts with a wider classified dataset, the app can always filter down. If it starts with only strict sovereign countries, adding territories later would affect stats, maps, airport mapping, backup compatibility, and user expectations.

---

## 5. Main Product Domains

Atlas has three primary domains:

```text
Countries and territories
Trips
Flights and itineraries
```

These domains are connected.

A country or territory can become visited, planned, lived, wished, or currently living through different kinds of user data:

- manual country logs
- trip stops
- excursion stops
- generated itinerary stops
- solo flights
- itinerary groups
- currently living state

The app should avoid duplicate manual work. When possible, country state should be derived from structured travel records.

---

## 6. Country and Territory Tracking

### 6.1 Purpose

Country and territory tracking is the central overview layer of Atlas.

It should allow the user to see:

- places never visited
- wished places
- planned places
- visited places
- places where the user has lived
- the currently living place
- related trips
- related flights
- related notes
- related stats
- related timeline entries

---

### 6.2 Country/Territory States

Supported states:

```text
never_visited
wished
planned
visited
lived
currently_living
```

These are not mutually exclusive.

Examples:

```text
A country can be wished and never visited.
A country can be wished and visited.
A country can be planned and wished.
A country can be lived and visited.
A country can be currently living and lived.
```

For display purposes, the app may define a visual priority.

Recommended visual priority:

```text
currently_living
lived
in_progress / currently visiting
planned
visited
wished
never_visited
```

However, the underlying model should preserve all applicable states.

---

### 6.3 Wished

A place is wished if:

```text
CountryUserState.wished = true
```

Wished is a personal marker.

It can coexist with:

- never visited
- planned
- visited
- lived
- currently living

---

### 6.4 Visited

A place counts as visited if any of the following are true:

1. There is a manual visit log.
2. There is a manual lived log.
3. The place is currently living.
4. There is a completed trip stop in that place.
5. There is an in-progress trip stop in that place.
6. There is a completed excursion stop in that place.
7. There is an in-progress excursion stop in that place.
8. There is a completed generated itinerary stop in that place.
9. There is an in-progress generated itinerary stop in that place.
10. There is a completed solo flight whose destination is in that place.
11. There is a completed solo flight whose origin is in that place and `origin_counts_for_country_tracking = true`.
12. There is an itinerary group whose derived destination/origin place counts as visited according to itinerary country derivation rules.

In general:

```text
in_progress trip-related presence counts as visited.
in_progress flight destination does not necessarily count as visited until completion.
```

---

### 6.5 Planned

A place counts as planned if any of the following are true:

1. There is a planned trip stop in that place.
2. There is a planned excursion stop in that place.
3. There is a planned generated itinerary stop in that place.
4. There is a planned solo flight whose destination is in that place.
5. There is a planned solo flight whose origin is in that place and `origin_counts_for_country_tracking = true`.
6. There is a planned itinerary group whose derived destination/origin place counts as planned according to itinerary country derivation rules.

There is no required manual planned country marker. Planned state is primarily derived from future trips and flights.

A future feature may optionally add a manual planned marker, but it is not required for the core product.

---

### 6.6 Lived

A place counts as lived if:

1. There is a manual lived log.
2. The place is currently living.
3. The place was previously set as currently living and a lived history entry was created or maintained.

Lived implies visited.

---

### 6.7 Currently Living

A place is currently living if:

```text
CountryUserState.currently_living = true
```

Rules:

- only one place can be currently living at a time
- setting a new currently living place unsets the previous one
- setting currently living creates or maintains a lived log/history entry
- currently living implies lived
- currently living implies visited

---

### 6.8 Never Visited

A place is never visited if it does not satisfy any visited or lived condition.

A place can be:

```text
never visited + wished
never visited + planned
never visited + wished + planned
```

---

### 6.9 Country Detail Page

A country/territory detail page should eventually include:

- header
- flag
- name
- type classification
- parent country, if applicable
- continent/subregion
- map
- wished toggle
- currently living toggle
- manual visit/lived logs
- basic stats
- detailed stats/info sections
- related trips
- related trip stops
- related excursions
- related flights
- related itineraries
- combined timeline
- notes or personal memories
- future photo references

The detail page should be the main place where structured data and emotional travel memory meet.

---

## 7. Trips

### 7.1 Purpose

A trip is a travel experience.

Trips are the main way to record meaningful travel beyond individual countries or flights.

Examples:

```text
Japan 2026
Roadtrip País Basc
Interrail Europe
Amsterdam weekend
Summer in Ireland
```

A trip can be minimal or detailed.

Minimum required field:

```text
title
```

Optional fields:

- status
- flexible date range
- notes
- main stops
- excursions
- linked itinerary
- map
- photos
- future slideshow/story mode data

---

### 7.2 Trip Status

Supported trip statuses:

```text
planned
in_progress
completed
unknown
```

Status affects country/territory derivation.

Basic rule:

```text
completed trip → stops count as visited
in_progress trip → stops count as visited in the simplified model
planned trip → stops count as planned
unknown trip → country derivation may be unknown or disabled
```

Nuance:

If a trip is `in_progress`, not all future stops may have actually been visited yet. A future version may support stop-level progress.

Recommended simplified rule:

```text
If a trip is in progress, all its stops count as visited unless stop-level progress exists.
```

Recommended future precise rule:

```text
Trip status and stop status can differ.
Stop-level status determines country derivation when available.
```

---

### 7.3 Main Trip Stops

Main trip stops represent the primary route of the trip.

Example:

```text
Tokyo → Kyoto → Osaka
```

Main trip stops can be:

- manually created
- generated from itinerary groups

Trip stops should support:

- ordered route position
- location name
- country/territory
- coordinates when available
- optional date range
- optional notes
- future photos
- visibility in maps/timelines/story mode

---

### 7.4 Stop Ordering

Trips should support ordered stops.

Ordering can be:

- manual reordering
- automatic ordering from dates, when enough information exists
- generated ordering from itinerary groups

If there is a conflict between manual order and date order, manual order should usually win unless the user chooses auto-order.

Recommended conceptual fields:

```text
sort_order
order_source: manual | date | itinerary | generated
```

---

### 7.5 Excursions

An excursion is a side route attached to a main trip stop.

Example:

```text
Main stop: Tokyo

Excursion: Kamakura and Yokohama
- Kamakura
- Yokohama
```

This allows Atlas to represent:

```text
Tokyo → Kamakura → Yokohama → Tokyo
```

without treating Kamakura and Yokohama as unrelated main route stops.

An excursion belongs to:

- one trip
- one anchor main trip stop

Excursions should support:

- optional title
- optional date range
- optional notes
- ordered excursion stops
- route map
- future photos
- future story/slideshow sections

---

### 7.6 Excursion Stops

An excursion stop is a location visited as part of an excursion.

Excursion stops count for:

- country/territory tracking
- trip map
- trip timeline
- country detail timeline
- future slideshow/story mode

---

### 7.7 Trip Detail Page

A trip detail page should eventually include:

- title
- status
- date range
- notes
- main route stops
- generated itinerary stops
- excursions
- linked itinerary summary
- route map
- related flights
- country/territory summary
- timeline
- future photos
- future slideshow/story mode entry point

The trip page should feel like the memory page for a travel experience.

---

## 8. Flights

### 8.1 Purpose

A flight is an individual air travel segment from one airport to another.

Flights can exist as:

- solo flights
- flights inside itinerary groups

Minimum required fields:

```text
origin airport
destination airport
```

Optional fields:

- status
- planned departure/arrival
- actual departure/arrival
- airline
- flight number
- aircraft type
- notes
- itinerary group relation
- API/external provider metadata
- country tracking flags

---

### 8.2 Flight Status

Supported flight statuses:

```text
planned
in_progress
completed
unknown
```

Country derivation differs between trips and flights.

Recommended rule:

```text
completed flight → may count destination as visited
planned flight → may count destination as planned
in_progress flight → destination should not necessarily count as visited until arrival
unknown flight → country derivation may be disabled or conservative
```

Rationale:

If the user is physically on a flight to Japan, Japan has not necessarily been visited yet. The destination should normally count as visited only once the flight is completed.

---

### 8.3 Local and UTC Times

Flights require correct time modelling.

The app should distinguish:

- local departure time
- local arrival time
- UTC departure time
- UTC arrival time
- origin airport timezone
- destination airport timezone

This is important because flights cross timezones and date boundaries.

Recommended conceptual fields:

```text
planned_departure_local_datetime
planned_departure_utc_datetime
planned_arrival_local_datetime
planned_arrival_utc_datetime

actual_departure_local_datetime
actual_departure_utc_datetime
actual_arrival_local_datetime
actual_arrival_utc_datetime
```

The UI can expose this simply, but the model should avoid ambiguity.

---

### 8.4 Solo Flight Country Derivation

Solo flights count the destination country/territory by default.

Example:

```text
Barcelona → Tokyo
```

Japan counts.

Solo flights may also count the origin if explicitly enabled.

Flags:

```text
destination_counts_for_country_tracking = true
origin_counts_for_country_tracking = false
```

Example:

```text
Tokyo → Barcelona
origin_counts_for_country_tracking = true
```

Japan also counts.

This is useful for return flights or historical records where the user wants to count the origin country.

---

### 8.5 Flight API Preparation

Atlas may later support flight lookup/import from an external API.

The core model should prepare for this, but API integration should not be required for the base product to work.

Recommended fields:

```text
fetched_from: manual | api | other
external_provider optional
external_id optional
external_raw_data optional
```

Manual creation should always remain possible.

Future API functionality may include:

- lookup by flight number and date
- import departure/arrival times
- import airline
- import aircraft type
- update status
- detect delays
- enrich flight details

However, Atlas should not become dependent on a paid or unreliable flight API for core usage.

---

### 8.6 Flight Detail Page

A flight detail page should eventually include:

- origin airport
- destination airport
- status
- planned departure/arrival
- actual departure/arrival
- local and UTC time representation
- airline
- flight number
- aircraft type
- notes
- linked itinerary/group
- linked trip, if applicable
- country tracking options
- route visualization
- future airline logo
- future aircraft image/spec information

---

## 9. Itineraries

### 9.1 Purpose

An itinerary is a collection of related flights.

Itineraries are primarily used to group flights that belong together.

Examples:

```text
Japan 2026 flights
Summer roadtrip flights
Return home from Amsterdam
```

An itinerary can exist independently or be linked to one trip.

Product rule:

```text
A trip can have zero or one linked itinerary.
```

---

### 9.2 Itinerary Groups

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

Without groups, every segment could incorrectly mark layover countries as visited.

---

### 9.3 Layover Rule

Layovers do not count as country/territory visits by default.

Example:

```text
Barcelona → Doha → Tokyo
```

If Doha is only a connection, Qatar should not count as visited.

Country derivation should come from itinerary groups, not every flight segment.

---

### 9.4 Grouped Flight Country Derivation

For flights inside itinerary groups, country/territory derivation should come from the itinerary group generated stop logic.

Rule:

```text
Not the last group → destination of the last flight in the group
Last group → origin of the first flight in the group
```

Example:

```text
Group 1:
Barcelona → Doha
Doha → Tokyo

Derived place:
Tokyo / Japan
```

```text
Group 2:
Osaka → Doha
Doha → Barcelona

Derived place:
Osaka / Japan
```

This avoids creating a final generated stop for the home airport.

---

### 9.5 Itineraries Linked to Trips

For each itinerary group linked to a trip, Atlas should generate one main trip stop.

Generated itinerary stops should appear in the trip but should not have their core location directly edited.

Core generated fields:

- location
- country/territory
- coordinates
- derived date, if applicable

To change these, the user should edit the itinerary group or flights.

Potentially editable generated-stop fields:

- display title
- notes
- future photos
- visibility in trip
- visibility in map/timeline/story mode

---

### 9.6 Itineraries Without Trips

If an itinerary is not linked to a trip, country/territory derivation should still work using the same group logic.

Instead of creating trip stops, the itinerary directly contributes to country/territory status derivation.

Example:

```text
Group 1:
Barcelona → Doha
Doha → Tokyo

Derived visited/planned place:
Japan
```

```text
Group 2:
Shanghai → Doha
Doha → Barcelona

Derived visited/planned place:
China
```

The derivation status depends on the itinerary group/flight status.

---

## 10. Location Search and Places

### 10.1 Purpose

Atlas should support location search for trip stops and excursion stops.

Preferred flow:

1. Search location online.
2. Select result.
3. Store selected place locally.
4. Create stop from selected place.

If search fails, the app should provide manual fallback.

---

### 10.2 Manual Fallback

Manual fallback minimum:

```text
location name + country/territory
```

Coordinates are optional.

Manual fallback should not block record creation.

Example:

```text
Location name: Small village near Kyoto
Country: Japan
Coordinates: empty
```

The place can still be used for country tracking, even if it cannot be precisely mapped.

---

### 10.3 Place Cache

Selected search results should be stored locally.

Purpose:

- avoid losing selected external data
- allow records to remain stable if provider data changes
- support offline viewing of already selected places
- support deduplication
- support future provider replacement

Possible providers:

```text
osm
manual
future_provider
```

Potential deduplication strategies:

- provider ID
- coordinates
- normalized name + country
- user-assisted merge

This remains an implementation decision.

---

## 11. Maps

### 11.1 Direction

Preferred map direction:

```text
OpenStreetMap-based
```

Initial map usage:

- display trip stops
- display excursion routes
- display flight route lines
- display country/territory overview
- display country detail maps

Advanced future map usage:

- interactive world map
- colored visited/planned/wished countries
- route animation
- trip story mode
- local map caching
- richer styling
- country polygon interactions

---

### 11.2 Map Philosophy

Maps should be visual and meaningful, but they should not block core record creation.

If a place has coordinates, it can appear on a map.

If a place does not have coordinates, it can still exist and contribute to country/territory tracking.

---

## 12. Statistics and Country Information

### 12.1 Purpose

Atlas should eventually include both personal travel statistics and general country/territory information.

Personal stats examples:

- countries visited
- territories visited
- total tracked places visited
- countries wished
- countries planned
- countries lived
- flights taken
- trips completed
- trips planned
- distance flown
- most visited countries
- trips by year
- flights by year
- continents visited

Country information examples:

- capital
- population
- area
- GDP
- GDP per capita
- HDI
- life expectancy
- currency
- languages
- timezones
- government type
- geography
- health
- education
- culture
- rights
- environment
- tourism
- transport

---

### 12.2 Country Stats Dataset Architecture

Country-related data should be split into separate layers.

Layer 1:

```text
Country identity/reference data
```

Layer 2:

```text
User country tracking data
```

Layer 3:

```text
Country statistics/info dataset
```

This separation is important because user travel data and static/semi-static country data evolve differently.

---

### 12.3 Country Stat Facts

A flexible fact table should support detailed country info pages without requiring schema migrations for every new field.

Implemented model:

```text
CountryStatFact
- country_iso2
- category
- key
- label_ca
- value
- unit nullable
- year nullable
- rank nullable
- rank_total nullable
- tier nullable
- sort_order
```

Primary key:

```text
country_iso2 + category + key
```

The bundled dataset may preformat Catalan display values. Distribution facts may use a JSON-array string in `value`, which presentation maps into display slices.

---

## 13. Photos and Memories

### 13.1 Purpose

Photos should support the emotional/memory side of Atlas.

Currently implemented:

- app-private photos on trip stops and excursion stops;
- optional trip cover photo selection;
- optional cached country hero photos from an external provider.

Photos should be local-first.

---

### 13.2 Current Photo Model

```text
StopPhoto
- id
- stop_id
- stop_type: TRIP_STOP | EXCURSION_STOP
- filename
- sort_order
- created_at
```

```text
CountryPhotoCache
- country_iso2
- filename
- source_url
- author
- author_link
- fetched_at
```

Stop-photo files live in app-private storage. Country photos are replaceable external cache data, not personal source data.

---

### 13.3 Story / Slideshow Mode

A future story mode could transform trips into visual narratives.

Possible elements:

- trip title slide
- route map
- timeline
- stop-by-stop sections
- excursion sections
- photos
- notes
- country facts
- flight route intro/outro
- statistics summary

This should be treated as a future visual feature, not a core data requirement.

---

## 14. Backup and Import

### 14.1 Backup Philosophy

Atlas should support JSON backup/import for user-created data.

The backup system should protect user ownership and make the app safe to use long-term without cloud dependency.

Backup format:

```text
JSON
```

CSV export may be added later but is not central to the product.

---

### 14.2 Data Included in Backup

Backups should include user-created data:

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
- photo references
- notes
- user settings related to tracking

Backups do not need to include bundled static reference datasets by default:

- country identity dataset
- country stats dataset
- airport dataset

However, backups should include dataset version metadata so imports can be validated.

Example:

```json
{
  "backupVersion": 1,
  "createdAt": "2026-05-29T00:00:00Z",
  "countryDatasetVersion": "2026.1",
  "airportDatasetVersion": "2026.1",
  "countryStatsDatasetVersion": "2026.1",
  "data": {}
}
```

---

### 14.3 Import Principles

Import should be designed carefully.

Important concerns:

- schema migrations
- dataset version compatibility
- missing countries/territories
- missing airports
- duplicate records
- changed static dataset IDs
- photo URI availability
- partial import failures

Recommended approach:

```text
Use stable external identifiers such as ISO codes and IATA codes in backup payloads, not only internal database IDs.
```

---

## 15. Static Dataset Files

Recommended bundled dataset files:

```text
assets/data/countries.json
assets/data/airports.json
assets/data/airlines.json
assets/data/aircraft_types.json
assets/data/country_stats.json
```

Future possible datasets:

```text
assets/data/country_polygons.json
assets/data/map_styles.json
```

---

### 15.1 countries.json

Contains country and territory identity/reference data.

Should include:

- stable ID
- ISO codes where available
- Catalan name
- English fallback name
- type classification
- parent country if applicable
- continent
- subregion
- flag
- approximate coordinates
- trackable flag

---

### 15.2 country_stats.json

Contains detailed flexible stat facts.

Example:

```json
{
  "countryIso2": "JP",
  "category": "demografia",
  "key": "population",
  "labelCa": "Població",
  "value": "124.500.000",
  "unit": "hab.",
  "year": 2024,
  "rank": 12,
  "rankTotal": 195,
  "tier": "Alt",
  "sortOrder": 10
}
```

Example:

```json
{
  "countryIso2": "JP",
  "category": "governanca",
  "key": "government_type",
  "labelCa": "Tipus de govern",
  "value": "Monarquia constitucional parlamentària",
  "year": 2024,
  "sortOrder": 30
}
```

---

### 15.3 airports.json

Contains airport reference data.

Should include:

- IATA code
- ICAO code if available
- airport name
- city
- country/territory ID
- latitude
- longitude
- timezone

Airport names should use official/common names.

---

## 16. Data Model Draft

This section describes the conceptual long-term model. Exact Room implementation may differ.

---

### 16.1 Country

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

Purpose:

- stable country/territory identity
- used across countries, trips, flights, airports, maps, and stats
- should not contain user logs or large country stats dataset

---

### 16.2 CountryUserState

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

### 16.3 CountryLog

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

### 16.4 CountryStatFact

```text
CountryStatFact
- country_iso2
- category
- key
- label_ca
- value
- unit nullable
- year nullable
- rank nullable
- rank_total nullable
- tier nullable
- sort_order
```

---

### 16.5 Airport

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

---

### 16.6 Flight

```text
Flight
- id
- origin_airport_id
- destination_airport_id
- status: planned | in_progress | completed | unknown
- status_source: manual | inferred

- planned_departure_local_datetime optional
- planned_departure_utc_datetime optional
- planned_arrival_local_datetime optional
- planned_arrival_utc_datetime optional

- actual_departure_local_datetime optional
- actual_departure_utc_datetime optional
- actual_arrival_local_datetime optional
- actual_arrival_utc_datetime optional

- airline optional
- flight_number optional
- aircraft_type optional
- notes optional

- itinerary_group_id nullable

- destination_counts_for_country_tracking: boolean default true
- origin_counts_for_country_tracking: boolean default false

- fetched_from: manual | api | other
- external_provider optional
- external_id optional
- external_raw_data optional

- created_at
- updated_at
```

---

### 16.7 Itinerary

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

### 16.8 ItineraryGroup

```text
ItineraryGroup
- id
- itinerary_id
- title optional
- status optional
- sort_order
- created_at
- updated_at
```

---

### 16.9 Trip

```text
Trip
- id
- title
- status: planned | in_progress | completed | unknown
- status_source: manual | inferred
- date_range optional
- notes optional
- cover_photo_filename optional
- created_at
- updated_at
```

---

### 16.10 TripStop

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

- status optional
- date_range optional
- notes optional
- sort_order
- order_source: manual | date | itinerary | generated

- created_at
- updated_at
```

If `source = itinerary_group`, core location fields are generated.

---

### 16.11 Excursion

```text
Excursion
- id
- trip_id
- anchor_trip_stop_id
- title optional
- status optional
- date_range optional
- notes optional
- sort_order
- created_at
- updated_at
```

---

### 16.12 ExcursionStop

```text
ExcursionStop
- id
- excursion_id

- location_name
- country_id
- latitude nullable
- longitude nullable

- status optional
- date_range optional
- notes optional
- sort_order

- created_at
- updated_at
```

---

### 16.13 Place

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

### 16.14 StopPhoto

```text
StopPhoto
- id
- stop_id
- stop_type: TRIP_STOP | EXCURSION_STOP
- filename
- sort_order
- created_at
```

```text
CountryPhotoCache
- country_iso2
- filename
- source_url
- author
- author_link
- fetched_at
```

---

## 17. Main Screens

This is the long-term screen catalogue, not the MVP screen list.

---

### 17.1 Main Dashboard

Should show high-level overview.

Potential cards:

- world map summary
- countries/territories visited
- wished places
- planned places
- lived places
- trips summary
- flights summary
- upcoming planned trips/flights
- current/in-progress trip
- recent memories
- stats highlights

---

### 17.2 Countries/Territories List

Features:

- list countries and territories
- group by continent/subregion
- search
- filter by state:
  - visited
  - lived
  - wished
  - planned
  - currently living
  - never visited
- filter by type:
  - sovereign countries
  - territories
  - special regions
- show flag, name, state summary, latest relevant log/event

---

### 17.3 Country/Territory Detail

Features:

- country header
- flag
- type classification
- map
- wished toggle
- currently living toggle
- manual visit/lived log list
- basic stats
- combined timeline of related events:
  - manual logs
  - trip stops
  - excursion stops
  - generated itinerary stops
  - solo flights
  - itineraries without trip
- related trips
- related flights
- country stats/info page link

---

### 17.4 Country Stats/Info Page

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
- tourism
- transport

This page should use the separated stats dataset.

---

### 17.5 Trips List

Features:

- list trips
- filter by status:
  - planned
  - in progress
  - completed
  - unknown
- search trips
- create trip button
- highlight current/ongoing trip
- show date summary
- show route/country summary

---

### 17.6 Trip Detail

Features:

- title
- status
- date range
- notes
- main route stops
- generated itinerary stops
- excursions
- linked itinerary summary
- map preview
- related flights
- countries/territories summary
- timeline
- photos/story mode entry point

---

### 17.7 Stop Creation Flow

Preferred flow:

1. Search location online.
2. Select result.
3. Store selected place locally.
4. Create stop.

Fallback flow:

1. Manual location name.
2. Select country/territory.
3. Optional coordinates.
4. Create stop.

---

### 17.8 Excursion Management Flow

Features:

- create excursion from main stop
- add ordered excursion stops
- optional title
- optional date range
- optional notes
- map route preview where coordinates exist

---

### 17.9 Flights List

Features:

- list all flights
- group flights by itinerary where applicable
- show solo flights separately
- filter by:
  - planned
  - in progress
  - completed
  - unknown
- search by:
  - airport
  - city
  - country
  - airline
  - flight number
- highlight current/ongoing flight if date/time supports it

---

### 17.10 Flight Detail

Features:

- origin
- destination
- status
- planned/actual date/time fields
- local and UTC time handling
- airline
- flight number
- aircraft type
- notes
- itinerary/group relationship
- trip relationship, if applicable
- country tracking toggles for solo flights
- route visualization
- future airline/aircraft visual enrichment

---

### 17.11 Itinerary Management

Features:

- create/edit itinerary
- create/edit itinerary groups
- add flights to groups
- remove flights
- reorder groups
- reorder flights inside groups
- show generated stop preview if linked to trip
- derive country tracking from groups
- link/unlink itinerary to trip

---

### 17.12 Stats Dashboard

Future full stats dashboard may include:

- total countries visited
- total territories visited
- total tracked entities visited
- continents visited
- countries wished/planned/lived
- trips by year
- flights by year
- distance flown
- top destinations
- recent travel
- map-based statistics

---

### 17.13 Story / Slideshow Mode

Future screen for presenting a trip as a visual story.

Potential structure:

- title screen
- map overview
- itinerary/flights section
- stop-by-stop memories
- excursion sections
- photos
- notes
- final trip summary

---

## 18. Feature Catalogue

### 18.1 Core Features

- local-first data storage
- country/territory dataset
- country/territory list
- country/territory detail
- wished toggle
- currently living toggle
- visit/lived logs
- trips
- trip stops
- excursions
- excursion stops
- location search
- manual location fallback
- derived country/territory states
- JSON backup/import

---

### 18.2 Advanced Travel Features

- flights
- solo flights
- itineraries
- itinerary groups
- layover-safe country derivation
- generated trip stops from itinerary groups
- local/UTC flight time handling
- flight route visualization
- optional flight API lookup/import

---

### 18.3 Visual Features

- dashboard cards
- country map
- trip maps
- excursion route maps
- flight route lines
- country timeline
- trip timeline
- stats dashboard
- story/slideshow mode
- photo-based memories

---

### 18.4 Dataset Features

- country identity dataset
- country stats summary dataset
- flexible country stat facts dataset
- airport dataset
- future airline dataset
- future aircraft type dataset
- future map polygon dataset

---

### 18.5 Backup and Portability Features

- JSON export
- JSON import
- backup versioning
- dataset version metadata
- future advanced import/export
- future optional cloud backup

---

## 19. Release Sequencing Direction

This section is only a broad sequencing suggestion. Dedicated version documents should define exact scope.

---

### 19.1 MVP Direction

The MVP should be much smaller than the full product.

Recommended MVP theme:

```text
A local-first Android app for tracking countries/territories and basic trips with stops.
```

Likely MVP focus:

- Android foundation
- Room
- Catalan UI
- country/territory dataset
- country list/detail
- wished/currently living
- manual visit/lived logs
- simple trips
- manual trip stops
- location search + manual fallback
- derived states from logs and trip stops
- JSON backup/import

---

### 19.2 v2.0 Direction

Recommended v2.0 theme:

```text
Flights and itineraries become first-class citizens.
```

Likely v2.0 focus:

- manual solo flights
- itineraries
- itinerary groups
- flights inside groups
- layover-safe country derivation
- generated trip stops from itinerary groups
- better trip maps
- flight route visualization
- basic stats dashboard

---

### 19.3 Later Versions

Possible later additions:

- flight API lookup/import
- airline logos
- aircraft images/specs
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

## 20. Open Product and Implementation Questions

The main product direction is defined, but several implementation-level questions remain.

1. Which specific OSM-based map library should be used on Android?
2. Which location search/geocoding provider should be used first?
3. How large should the initial airport dataset be?
4. Which countries/territories source should seed the first dataset?
5. How many country stats should be bundled initially?
6. Should generated itinerary stops be physically stored or computed dynamically?
7. How should flexible dates be represented internally in Room?
8. How should status inference from dates work with partial dates?
9. How should JSON backup handle future schema migrations?
10. Should cached places be deduplicated by provider ID, coordinates, or name/country?
11. How should country state derivation be cached for performance?
12. What is the first visual design direction for the app?
13. How should map rendering handle territories and disputed places?
14. Should stop-level status exist from the beginning or only later?
15. Should country/territory stats use only bundled data or allow update packages?

---

## 21. Long-Term Success Criteria

Atlas is successful as a full product if it allows the user to:

1. Track countries and territories visited, wished, planned, lived, and currently living.
2. Record trips with flexible levels of detail.
3. Add main trip stops and side excursions.
4. Track flights manually.
5. Group flights into itineraries.
6. Avoid counting layovers as visited places.
7. Automatically derive visited/planned states from trips, stops, flights, and itineraries.
8. View travel history visually through maps and timelines.
9. Explore country/territory detail pages with personal history and general stats.
10. Preserve all personal travel data locally.
11. Export/import personal travel data safely.
12. Use the app without being forced into complete data entry.
13. Revisit trips as meaningful memories, not only database records.
14. Expand into photos, statistics, richer maps, and story mode without redesigning the core model.

---

## 22. Summary

Atlas should become a local-first personal travel atlas for Android.

The most important long-term product decisions are:

- use a broad countries-and-territories dataset
- separate static reference data from user tracking data
- separate country stats datasets from core country identity
- support flexible dates
- store explicit status while allowing smart date-based inference
- count in-progress trips as visited in the simplified model
- be careful with in-progress flights because destination arrival matters
- model trips, stops, excursions, flights, itineraries, and itinerary groups as connected but distinct concepts
- prevent layovers from incorrectly counting as visits
- prepare for photos, stats, maps, and story mode without forcing them into the first MVP
- keep personal data local-first
- use JSON backup/import for portability
- rely on free or replaceable external services where possible

This document defines the full product direction. The MVP and v2.0 documents should translate this direction into smaller, buildable scopes.
