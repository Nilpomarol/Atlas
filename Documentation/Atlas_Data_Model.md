# Atlas Data Model

## 0. Document Purpose

This document defines the **long-term conceptual data model** for Atlas.

It describes the data architecture needed by the complete app vision, not only the MVP. Version-specific implementation models should be kept in separate documents:

```text
Atlas_MVP_Data_Model.md
Atlas_v2.0_Data_Model.md
```

This document is the reference for long-term modelling decisions, entity relationships, stable identifiers, backup strategy, and future expansion.

---

## 1. Data Model Principles

### 1.1 Separate static data from user data

Atlas must separate reference/static data from user-created data.

Static/reference data:

```text
Country
CountryStatsSummary
CountryStatFact
Airport
Airline future
AircraftType future
DatasetMetadata
```

User-created data:

```text
CountryUserState
CountryLog
Trip
TripStop
Excursion
ExcursionStop
Flight
Itinerary
ItineraryGroup
Place
Photo future
```

Reasons:

- bundled datasets can be updated without overwriting personal data
- JSON backups can focus on user data
- country statistics can grow independently
- user tracking state remains clean
- future migrations become safer

---

### 1.2 Stable external identifiers matter

Static reference data should use stable external identifiers where possible.

Recommended stable keys:

```text
Country/territory: ISO 3166-1 alpha-2 code when available
Airport: IATA code when available
Airline future: IATA or ICAO code
```

Internal database IDs can exist, but backups and imports should prefer stable identifiers so user data can reconnect after reinstalling the app or updating datasets.

---

### 1.3 Derived states are not source of truth

Country/territory states should be derived from user data.

Examples of derived states:

```text
visited
planned
lived
currently_living
wished
never_visited
```

Inputs:

```text
CountryUserState
CountryLog
Trip
TripStop
ExcursionStop
Flight
ItineraryGroup
Generated itinerary stops
```

A derived-state cache may be added later for performance, but it should not become the source of truth.

---

### 1.4 Flexible dates are structured data

Atlas supports incomplete dates:

```text
2023
06-2023
14-06-2023
```

Flexible dates should not be stored only as display strings. They should be stored structurally so the app can validate, sort, filter, and format them correctly.

---

### 1.5 Status is explicit but can be inferred

Trips and flights use explicit status:

```text
planned
in_progress
completed
unknown
```

Dates may suggest a status, but the stored status remains editable by the user.

Recommended model:

```text
status
status_source: manual | inferred
```

---

### 1.6 Build only the schema needed per version

The complete Atlas model is large. Individual releases should implement only the tables needed by that release.

Recommended strategy:

```text
MVP: countries/territories + logs + trips + stops
v2.0: airports + flights + itineraries + generated stops
Later: excursions + photos + airlines + aircraft + advanced stats
```

---

## 2. Shared Types

### 2.1 DatePrecision

```text
YEAR
MONTH
DAY
```

---

### 2.2 TravelStatus

```text
PLANNED
IN_PROGRESS
COMPLETED
UNKNOWN
```

Used by:

```text
Trip
Flight
ItineraryGroup optional
TripStop optional future
Excursion optional future
ExcursionStop optional future
```

---

### 2.3 StatusSource

```text
MANUAL
INFERRED
```

Used when the app supports date-based status suggestions.

---

### 2.4 CountryEntityType

```text
SOVEREIGN_STATE
DEPENDENT_TERRITORY
SPECIAL_REGION
DISPUTED_OR_OTHER
```

---

### 2.5 CountryLogType

```text
VISIT
LIVED
```

---

### 2.6 TripStopSource

```text
MANUAL
ITINERARY_GROUP
```

---

### 2.7 PlaceProvider

```text
OSM
MANUAL
OTHER
```

---

### 2.8 CountryStatValueType

```text
TEXT
INTEGER
DECIMAL
PERCENTAGE
CURRENCY
BOOLEAN
LIST
URL
```

---

## 3. Flexible Date Model

### 3.1 FlexibleDate

Conceptual model:

```text
FlexibleDate
- year: Int
- month: Int?
- day: Int?
- precision: DatePrecision
```

Examples:

```text
year = 2023
month = null
day = null
precision = YEAR
```

```text
year = 2023
month = 6
day = null
precision = MONTH
```

```text
year = 2023
month = 6
day = 14
precision = DAY
```

---

### 3.2 FlexibleDateRange

Conceptual model:

```text
FlexibleDateRange
- start: FlexibleDate?
- end: FlexibleDate?
- precision: DatePrecision?
```

Rules:

```text
start can be null
end can be null
both can be null if parent record allows no date
if both are present, both must use the same precision
```

---

### 3.3 Recommended Room columns

For entities with flexible ranges:

```text
start_year INTEGER NULL
start_month INTEGER NULL
start_day INTEGER NULL

end_year INTEGER NULL
end_month INTEGER NULL
end_day INTEGER NULL

date_precision TEXT NULL
```

Used by:

```text
CountryLog
Trip
TripStop
Excursion
ExcursionStop
```

Sorting approximation:

```text
YEAR  -> YYYY-01-01
MONTH -> YYYY-MM-01
DAY   -> YYYY-MM-DD
```

Display must preserve the original precision.

---

## 4. Static Reference Entities

### 4.1 CountryEntity

Purpose:

Stores country and territory identity/reference data.

It does not store user-specific tracking state or detailed statistics.

```text
CountryEntity
- iso2: String PRIMARY KEY
- iso3: String NULL UNIQUE
- name_ca: String NOT NULL
- name_en: String NULL
- type: String NOT NULL
- parent_iso2: String NULL REFERENCES CountryEntity(iso2)
- is_un_member: Boolean NOT NULL DEFAULT false
- is_observer_state: Boolean NOT NULL DEFAULT false
- is_trackable: Boolean NOT NULL DEFAULT true
- continent: String NOT NULL
- subregion: String NULL
- flag_emoji: String NULL
- flag_asset: String NULL
- latitude: Double NULL
- longitude: Double NULL
```

Notes:

- `iso2` is the preferred stable key.
- Some disputed or special entities may require custom IDs if no official ISO code exists.
- `parent_iso2` supports territories or special regions linked to a sovereign state.

---

### 4.2 CountryStatsSummaryEntity

Purpose:

Stores frequently used structured country/territory stats.

```text
CountryStatsSummaryEntity
- country_iso2: String PRIMARY KEY REFERENCES CountryEntity(iso2)
- capital: String NULL
- population: Long NULL
- area_km2: Double NULL
- gdp_nominal: Double NULL
- gdp_per_capita: Double NULL
- hdi: Double NULL
- life_expectancy: Double NULL
- currency: String NULL
- languages: String NULL
- timezone_summary: String NULL
- updated_at: String NULL
- dataset_version: String NOT NULL
```

This table should remain small and stable.

---

### 4.3 CountryStatFactEntity

Purpose:

Stores flexible detailed country/stat facts.

```text
CountryStatFactEntity
- id: String PRIMARY KEY
- country_iso2: String NOT NULL REFERENCES CountryEntity(iso2)
- category: String NOT NULL
- key: String NOT NULL
- label_ca: String NOT NULL

- value_text: String NULL
- value_number: Double NULL
- value_boolean: Boolean NULL
- value_type: String NOT NULL

- unit: String NULL
- year: Int NULL
- source_name: String NULL
- source_url: String NULL
- display_order: Int NOT NULL

- updated_at: String NULL
- dataset_version: String NOT NULL
```

Recommended unique constraint:

```text
UNIQUE(country_iso2, key, year)
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

---

### 4.4 AirportEntity

Purpose:

Stores airport reference data for flights.

```text
AirportEntity
- id: String PRIMARY KEY
- iata: String NULL UNIQUE
- icao: String NULL
- name: String NOT NULL
- city: String NOT NULL
- country_iso2: String NOT NULL REFERENCES CountryEntity(iso2)
- latitude: Double NOT NULL
- longitude: Double NOT NULL
- timezone: String NULL
```

ID recommendation:

```text
Use IATA when available.
Use ICAO or stable generated ID when IATA is unavailable.
```

---

### 4.5 DatasetMetadataEntity

Purpose:

Tracks imported static dataset versions.

```text
DatasetMetadataEntity
- key: String PRIMARY KEY
- version: String NOT NULL
- imported_at: String NOT NULL
```

Example keys:

```text
countries
airports
country_stats_summary
country_stats_facts
airlines future
aircraft_types future
```

---

## 5. User Country Tracking Entities

### 5.1 CountryUserStateEntity

Purpose:

Stores simple manual user state for a country/territory.

```text
CountryUserStateEntity
- country_iso2: String PRIMARY KEY REFERENCES CountryEntity(iso2)
- wished: Boolean NOT NULL DEFAULT false
- currently_living: Boolean NOT NULL DEFAULT false
- updated_at: String NOT NULL
```

Rule:

Only one country/territory can have:

```text
currently_living = true
```

Enforce this in a use case rather than relying only on SQLite constraints.

---

### 5.2 CountryLogEntity

Purpose:

Stores manual visit/lived logs.

```text
CountryLogEntity
- id: String PRIMARY KEY
- country_iso2: String NOT NULL REFERENCES CountryEntity(iso2)
- type: String NOT NULL

- start_year: Int NULL
- start_month: Int NULL
- start_day: Int NULL
- end_year: Int NULL
- end_month: Int NULL
- end_day: Int NULL
- date_precision: String NULL

- notes: String NULL

- created_at: String NOT NULL
- updated_at: String NOT NULL
```

A `LIVED` log implies visited.

---

## 6. Trips and Stops

### 6.1 TripEntity

Purpose:

Stores a travel experience.

```text
TripEntity
- id: String PRIMARY KEY
- title: String NOT NULL
- status: String NOT NULL
- status_source: String NULL

- start_year: Int NULL
- start_month: Int NULL
- start_day: Int NULL
- end_year: Int NULL
- end_month: Int NULL
- end_day: Int NULL
- date_precision: String NULL

- notes: String NULL

- created_at: String NOT NULL
- updated_at: String NOT NULL
```

Minimum required user field:

```text
title
```

---

### 6.2 TripStopEntity

Purpose:

Stores main stops in a trip.

```text
TripStopEntity
- id: String PRIMARY KEY
- trip_id: String NOT NULL REFERENCES TripEntity(id)

- source: String NOT NULL DEFAULT MANUAL
- itinerary_group_id: String NULL REFERENCES ItineraryGroupEntity(id)

- place_id: String NULL REFERENCES PlaceEntity(id)

- location_name: String NOT NULL
- country_iso2: String NOT NULL REFERENCES CountryEntity(iso2)
- latitude: Double NULL
- longitude: Double NULL

- status: String NULL

- start_year: Int NULL
- start_month: Int NULL
- start_day: Int NULL
- end_year: Int NULL
- end_month: Int NULL
- end_day: Int NULL
- date_precision: String NULL

- notes: String NULL
- sort_order: Int NOT NULL
- is_visible: Boolean NOT NULL DEFAULT true

- created_at: String NOT NULL
- updated_at: String NOT NULL
```

Notes:

- `source = MANUAL` for normal stops.
- `source = ITINERARY_GROUP` for generated stops from v2.0 onward.
- In the MVP implementation, `source` and `itinerary_group_id` can be omitted and added later.

---

## 7. Places

### 7.1 PlaceEntity

Purpose:

Stores selected searched places or manual fallback places.

```text
PlaceEntity
- id: String PRIMARY KEY
- provider: String NOT NULL
- provider_place_id: String NULL
- display_name: String NOT NULL
- country_iso2: String NOT NULL REFERENCES CountryEntity(iso2)
- latitude: Double NULL
- longitude: Double NULL
- raw_data: String NULL
- created_at: String NOT NULL
```

Manual fallback:

```text
provider = MANUAL
display_name = user-entered name
country_iso2 = selected country/territory
latitude = null
longitude = null
```

Trip stops may duplicate location fields even when linked to a `PlaceEntity` to preserve historical data.

---

## 8. Flights and Itineraries

### 8.1 FlightEntity

Purpose:

Stores one flight segment.

```text
FlightEntity
- id: String PRIMARY KEY

- origin_airport_id: String NOT NULL REFERENCES AirportEntity(id)
- destination_airport_id: String NOT NULL REFERENCES AirportEntity(id)

- status: String NOT NULL
- status_source: String NULL

- planned_departure_local_datetime: String NULL
- planned_departure_utc_datetime: String NULL
- planned_arrival_local_datetime: String NULL
- planned_arrival_utc_datetime: String NULL

- actual_departure_local_datetime: String NULL
- actual_departure_utc_datetime: String NULL
- actual_arrival_local_datetime: String NULL
- actual_arrival_utc_datetime: String NULL

- airline: String NULL
- flight_number: String NULL
- aircraft_type: String NULL
- notes: String NULL

- itinerary_group_id: String NULL REFERENCES ItineraryGroupEntity(id)

- destination_counts_for_country_tracking: Boolean NOT NULL DEFAULT true
- origin_counts_for_country_tracking: Boolean NOT NULL DEFAULT false

- sort_order: Int NULL

- fetched_from: String NOT NULL DEFAULT MANUAL
- external_provider: String NULL
- external_id: String NULL

- created_at: String NOT NULL
- updated_at: String NOT NULL
```

For solo flights:

```text
itinerary_group_id = null
```

For grouped flights:

```text
itinerary_group_id != null
```

Grouped flights should not directly count layover countries. Country derivation should come from itinerary groups.

---

### 8.2 ItineraryEntity

Purpose:

Stores a collection of related flights.

```text
ItineraryEntity
- id: String PRIMARY KEY
- title: String NOT NULL
- trip_id: String NULL UNIQUE REFERENCES TripEntity(id)
- notes: String NULL
- created_at: String NOT NULL
- updated_at: String NOT NULL
```

Rule:

```text
A trip can have zero or one itinerary.
An itinerary can be linked to zero or one trip.
```

---

### 8.3 ItineraryGroupEntity

Purpose:

Stores a meaningful leg inside an itinerary.

```text
ItineraryGroupEntity
- id: String PRIMARY KEY
- itinerary_id: String NOT NULL REFERENCES ItineraryEntity(id)
- title: String NULL
- status: String NULL
- sort_order: Int NOT NULL
- created_at: String NOT NULL
- updated_at: String NOT NULL
```

Generated stop rule:

```text
Not last group -> destination airport of the last flight in the group
Last group -> origin airport of the first flight in the group
```

---

## 9. Excursions

### 9.1 ExcursionEntity

Purpose:

Stores a side route attached to a main trip stop.

```text
ExcursionEntity
- id: String PRIMARY KEY
- trip_id: String NOT NULL REFERENCES TripEntity(id)
- anchor_trip_stop_id: String NOT NULL REFERENCES TripStopEntity(id)

- title: String NULL
- status: String NULL

- start_year: Int NULL
- start_month: Int NULL
- start_day: Int NULL
- end_year: Int NULL
- end_month: Int NULL
- end_day: Int NULL
- date_precision: String NULL

- notes: String NULL
- sort_order: Int NOT NULL

- created_at: String NOT NULL
- updated_at: String NOT NULL
```

---

### 9.2 ExcursionStopEntity

Purpose:

Stores ordered stops inside an excursion.

```text
ExcursionStopEntity
- id: String PRIMARY KEY
- excursion_id: String NOT NULL REFERENCES ExcursionEntity(id)

- place_id: String NULL REFERENCES PlaceEntity(id)

- location_name: String NOT NULL
- country_iso2: String NOT NULL REFERENCES CountryEntity(iso2)
- latitude: Double NULL
- longitude: Double NULL

- status: String NULL

- start_year: Int NULL
- start_month: Int NULL
- start_day: Int NULL
- end_year: Int NULL
- end_month: Int NULL
- end_day: Int NULL
- date_precision: String NULL

- notes: String NULL
- sort_order: Int NOT NULL

- created_at: String NOT NULL
- updated_at: String NOT NULL
```

---

## 10. Future Photos

### 10.1 PhotoEntity

```text
PhotoEntity
- id: String PRIMARY KEY
- local_uri: String NOT NULL
- caption: String NULL
- taken_at: String NULL
- created_at: String NOT NULL
- updated_at: String NOT NULL
```

Photo join tables:

```text
TripPhotoEntity(trip_id, photo_id, sort_order)
TripStopPhotoEntity(trip_stop_id, photo_id, sort_order)
ExcursionPhotoEntity(excursion_id, photo_id, sort_order)
ExcursionStopPhotoEntity(excursion_stop_id, photo_id, sort_order)
CountryPhotoEntity(country_iso2, photo_id, sort_order) optional future
```

Photo handling requires care because Android URI permissions may change.

---

## 11. Country/Territory Derivation Inputs

### 11.1 Wished

From:

```text
CountryUserStateEntity.wished
```

---

### 11.2 Currently living

From:

```text
CountryUserStateEntity.currently_living
```

---

### 11.3 Lived

From:

```text
CountryLogEntity.type = LIVED
CountryUserStateEntity.currently_living = true
```

---

### 11.4 Visited

From:

```text
CountryLogEntity.type = VISIT
CountryLogEntity.type = LIVED
CountryUserStateEntity.currently_living = true
TripStopEntity where parent Trip.status = COMPLETED
TripStopEntity where parent Trip.status = IN_PROGRESS
ExcursionStopEntity where parent Trip.status = COMPLETED
ExcursionStopEntity where parent Trip.status = IN_PROGRESS
Solo FlightEntity where status = COMPLETED and destination country matches
Solo FlightEntity where status = COMPLETED and origin country matches and origin_counts_for_country_tracking = true
ItineraryGroup-derived places where status/trip status counts as visited
```

---

### 11.5 Planned

From:

```text
TripStopEntity where parent Trip.status = PLANNED
ExcursionStopEntity where parent Trip.status = PLANNED
Solo FlightEntity where status = PLANNED and destination country matches
Solo FlightEntity where status = PLANNED and origin country matches and origin_counts_for_country_tracking = true
ItineraryGroup-derived places where status/trip status counts as planned
```

---

### 11.6 Unknown

Recommended rule:

```text
UNKNOWN does not count as visited or planned by default.
```

---

## 12. JSON Backup Strategy

Backups should include user-created data, not bundled static datasets.

Include:

```text
CountryUserStateEntity
CountryLogEntity
TripEntity
TripStopEntity
PlaceEntity
FlightEntity
ItineraryEntity
ItineraryGroupEntity
ExcursionEntity
ExcursionStopEntity
PhotoEntity future
photo join tables future
```

Exclude by default:

```text
CountryEntity
AirportEntity
CountryStatsSummaryEntity
CountryStatFactEntity
DatasetMetadataEntity
```

But include metadata:

```text
countryDatasetVersion
airportDatasetVersion
countryStatsDatasetVersion
```

Use stable references in backup payloads:

```text
country_iso2
airport_iata or airport_id
```

---

## 13. Recommended Indexes

```text
CountryUserStateEntity(country_iso2)
CountryLogEntity(country_iso2)

CountryStatsSummaryEntity(country_iso2)
CountryStatFactEntity(country_iso2)
CountryStatFactEntity(country_iso2, category)
CountryStatFactEntity(country_iso2, key)

AirportEntity(iata)
AirportEntity(country_iso2)
AirportEntity(city)

TripEntity(status)
TripStopEntity(trip_id)
TripStopEntity(country_iso2)
TripStopEntity(itinerary_group_id)
TripStopEntity(source)

PlaceEntity(country_iso2)
PlaceEntity(provider, provider_place_id)

FlightEntity(origin_airport_id)
FlightEntity(destination_airport_id)
FlightEntity(itinerary_group_id)
FlightEntity(status)

ItineraryEntity(trip_id)
ItineraryGroupEntity(itinerary_id)

ExcursionEntity(trip_id)
ExcursionEntity(anchor_trip_stop_id)
ExcursionStopEntity(excursion_id)
ExcursionStopEntity(country_iso2)
```

---

## 14. Version Implementation Mapping

### MVP

```text
CountryEntity
DatasetMetadataEntity
CountryUserStateEntity
CountryLogEntity
TripEntity
TripStopEntity
PlaceEntity optional
```

### v2.0

Adds:

```text
AirportEntity
FlightEntity
ItineraryEntity
ItineraryGroupEntity
TripStop.source
TripStop.itinerary_group_id
airport dataset metadata
backup version 2
```

### Later versions

Adds:

```text
ExcursionEntity
ExcursionStopEntity
PhotoEntity
photo join tables
CountryStatsSummaryEntity
CountryStatFactEntity
AirlineEntity
AircraftTypeEntity
```

---

## 15. Summary

The full Atlas data model should support:

- broad country/territory tracking
- stable static datasets
- local-first user records
- flexible dates
- explicit status
- simple trips and stops
- future flights and itineraries
- layover-safe generated stops
- future excursions
- future photos
- future country stats
- JSON backup/import

Each app version should implement only the necessary subset of this model, while keeping stable identifiers and migration paths ready for future expansion.
