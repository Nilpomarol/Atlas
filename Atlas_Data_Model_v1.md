# Atlas Data Model v1

## 1. Purpose

This document defines the initial data model for Atlas MVP v1.

Atlas is a local-first Android app using Room/SQLite. The data model must support:

- country tracking
- country logs
- country states
- large country stats dataset
- flights
- itineraries
- itinerary groups
- trips
- main stops
- excursions
- excursion stops
- flexible dates
- JSON backup/import
- future photo support

This document is conceptual but should be close enough to guide Room entity implementation.

---

## 2. Data Model Principles

## 2.1 Separate static data from user data

Static/reference data and user-created data must be separate.

Static/reference data:

```text
Country
Airport
CountryStatsSummary
CountryStatFact
DatasetMetadata
```

User data:

```text
CountryUserState
CountryLog
Flight
Itinerary
ItineraryGroup
Trip
TripStop
Excursion
ExcursionStop
Place
Photo
```

Reason:

- static data can be updated without overwriting user data
- JSON backup can focus on user data
- country statistics can grow independently
- user tracking state stays clean

---

## 2.2 Stable identifiers matter

Static data should use stable external identifiers where possible.

Recommended stable keys:

```text
Country: ISO 3166-1 alpha-2 code
Airport: IATA code when available
```

Internal database IDs can exist, but user data should be able to reconnect after backup/import using stable identifiers where possible.

---

## 2.3 Derived states should not be stored as source of truth

Country states like visited/planned/lived should be derived from:

- country user state
- country logs
- trips
- stops
- excursions
- itinerary generated stops
- solo flights

Do not store final country states as permanent truth in MVP.

A cache may be added later if needed.

---

## 2.4 Dates are flexible

Atlas supports incomplete dates.

Examples:

```text
2023
06-2023
14-06-2023
```

Flexible dates should not be stored only as formatted strings.

They should be stored in structured fields to allow sorting/filtering.

---

## 2.5 Flights need detailed fields from the start

The UI can start simple, but the data model should support:

- planned departure
- planned arrival
- actual departure
- actual arrival
- time zones later

---

## 2.6 Generated itinerary stops are stored

Trip stops generated from itinerary groups should be stored as `TripStop` rows with:

```text
source = itinerary_group
itinerary_group_id = ...
```

This allows later notes/photos/visibility while preserving the link to source flights.

---

## 3. Shared Types and Enums

## 3.1 DatePrecision

```text
YEAR
MONTH
DAY
```

Used by flexible dates and flexible date ranges.

---

## 3.2 TravelStatus

Used by trips and flights.

```text
PLANNED
COMPLETED
UNKNOWN
```

Rules:

- `COMPLETED` with no date can count as visited.
- `PLANNED` with no date can count as planned.
- `UNKNOWN` does not automatically count as planned or completed unless another rule says so.

---

## 3.3 CountryLogType

```text
VISIT
LIVED
```

---

## 3.4 TripStopSource

```text
MANUAL
ITINERARY_GROUP
```

---

## 3.5 PlaceProvider

```text
OSM
MANUAL
OTHER
```

Possible future values can be added.

---

## 3.6 CountryStatValueType

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

## 4. Flexible Date Storage

## 4.1 FlexibleDate conceptual model

```text
FlexibleDate
- year: Int
- month: Int?
- day: Int?
- precision: DatePrecision
```

Valid examples:

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

## 4.2 FlexibleDateRange conceptual model

```text
FlexibleDateRange
- start: FlexibleDate?
- end: FlexibleDate?
- precision: DatePrecision?
```

Rules:

- start can be null.
- end can be null.
- both can be null if the parent record allows no date.
- if start and end both exist, both must use the same precision.

---

## 4.3 Recommended Room column pattern

For entities with flexible date ranges:

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

---

## 4.4 Sorting flexible dates

Recommended sort approximation:

```text
YEAR precision  -> YYYY-01-01
MONTH precision -> YYYY-MM-01
DAY precision   -> YYYY-MM-DD
```

This is only for sorting/filtering. Display must preserve the original precision.

---

## 5. Static Reference Tables

---

# 5.1 CountryEntity

## Purpose

Stores stable country identity/reference data.

Does not store:

- visited
- planned
- wished
- lived
- country logs
- detailed 50–150 field country stats

## Fields

```text
CountryEntity
- iso2: String PRIMARY KEY
- iso3: String UNIQUE NOT NULL
- name_ca: String NOT NULL
- name_en: String NULL
- continent: String NOT NULL
- subregion: String NULL
- flag_emoji: String NULL
- flag_asset: String NULL
- latitude: Double NULL
- longitude: Double NULL
```

## Notes

Recommended primary key:

```text
iso2
```

Example:

```text
iso2 = "JP"
name_ca = "Japó"
```

---

# 5.2 CountryStatsSummaryEntity

## Purpose

Stores frequently used structured country stats.

Used for:

- country cards
- country detail header
- common stats display
- quick comparisons later

## Fields

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
- calling_code: String NULL
- timezone_summary: String NULL
- updated_at: String NULL
- dataset_version: String NOT NULL
```

## Notes

Keep this table small and stable.

Do not add every possible country stat here.

---

# 5.3 CountryStatFactEntity

## Purpose

Stores flexible detailed country info/stat facts.

This table supports the future country info page with 50–150 fields per country.

## Fields

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

## Recommended unique constraint

```text
UNIQUE(country_iso2, key, year)
```

If a fact can appear multiple times in the same year, include another discriminator later.

## Example categories

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

# 5.4 AirportEntity

## Purpose

Stores airport reference data.

Used by flights and flight creation/search.

## Fields

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

## ID recommendation

Use:

```text
iata
```

when available.

For airports without IATA:

```text
icao
```

or a stable generated ID.

For MVP, it is acceptable to include only airports with IATA codes if that simplifies the dataset.

---

# 5.5 DatasetMetadataEntity

## Purpose

Tracks imported static dataset versions.

## Fields

```text
DatasetMetadataEntity
- key: String PRIMARY KEY
- version: String NOT NULL
- imported_at: String NOT NULL
```

## Example keys

```text
countries
airports
country_stats_summary
country_stats_facts
```

---

## 6. User Country Tracking Tables

---

# 6.1 CountryUserStateEntity

## Purpose

Stores simple manual booleans per country.

## Fields

```text
CountryUserStateEntity
- country_iso2: String PRIMARY KEY REFERENCES CountryEntity(iso2)
- wished: Boolean NOT NULL DEFAULT false
- currently_living: Boolean NOT NULL DEFAULT false
- updated_at: String NOT NULL
```

## Constraint

Only one country should have:

```text
currently_living = true
```

SQLite cannot easily enforce this in a portable Room-friendly way, so enforce in a use case:

```text
SetCurrentlyLivingCountryUseCase
```

## Rule

When a country is set as currently living:

1. unset all other currently living countries
2. create or maintain a lived `CountryLog`
3. currently living implies lived
4. lived implies visited

---

# 6.2 CountryLogEntity

## Purpose

Stores manual country logs.

Supported MVP types:

```text
VISIT
LIVED
```

## Fields

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

## Notes

A lived log can be manually created or created/maintained when setting currently living.

---

## 7. Place and Location Tables

---

# 7.1 PlaceEntity

## Purpose

Stores selected searched places or manual fallback places.

Used by:

- trip stops
- excursion stops
- possible future reuse

## Fields

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

## Provider examples

```text
OSM
MANUAL
OTHER
```

## Notes

If location search fails, manual fallback should create a `PlaceEntity` with:

```text
provider = MANUAL
display_name = user-entered name
country_iso2 = selected country
latitude = null
longitude = null
```

---

## 8. Flight and Itinerary Tables

---

# 8.1 FlightEntity

## Purpose

Stores a single flight segment.

A flight can be:

- solo
- inside an itinerary group

## Fields

```text
FlightEntity
- id: String PRIMARY KEY

- origin_airport_id: String NOT NULL REFERENCES AirportEntity(id)
- destination_airport_id: String NOT NULL REFERENCES AirportEntity(id)

- status: String NOT NULL

- planned_departure_local_datetime: String NULL
- planned_departure_timezone: String NULL
- planned_arrival_local_datetime: String NULL
- planned_arrival_timezone: String NULL

- actual_departure_local_datetime: String NULL
- actual_departure_timezone: String NULL
- actual_arrival_local_datetime: String NULL
- actual_arrival_timezone: String NULL

- airline: String NULL
- flight_number: String NULL
- aircraft_type: String NULL
- notes: String NULL

- itinerary_group_id: String NULL REFERENCES ItineraryGroupEntity(id)

- destination_counts_for_country_tracking: Boolean NOT NULL DEFAULT true
- origin_counts_for_country_tracking: Boolean NOT NULL DEFAULT false

- sort_order: Int NULL

- created_at: String NOT NULL
- updated_at: String NOT NULL
```

## Notes

For solo flights:

```text
itinerary_group_id = null
```

For grouped flights:

```text
itinerary_group_id != null
```

Country derivation for grouped flights should come from itinerary groups/generated stops, not individual flight segments.

---

# 8.2 ItineraryEntity

## Purpose

Stores a collection of related flights.

Can be linked to one trip.

## Fields

```text
ItineraryEntity
- id: String PRIMARY KEY
- title: String NOT NULL
- trip_id: String NULL UNIQUE REFERENCES TripEntity(id)
- notes: String NULL
- created_at: String NOT NULL
- updated_at: String NOT NULL
```

## Rule

A trip can have zero or one itinerary.

This is enforced by:

```text
trip_id UNIQUE
```

---

# 8.3 ItineraryGroupEntity

## Purpose

Stores a meaningful leg/group inside an itinerary.

Used to distinguish layovers from meaningful destinations.

## Fields

```text
ItineraryGroupEntity
- id: String PRIMARY KEY
- itinerary_id: String NOT NULL REFERENCES ItineraryEntity(id)
- title: String NULL
- sort_order: Int NOT NULL
- created_at: String NOT NULL
- updated_at: String NOT NULL
```

## Generated stop rule

For each itinerary group linked to a trip:

| Group position | Generated stop location |
|---|---|
| Not last group | Destination of the last flight in the group |
| Last group | Origin of the first flight in the group |

---

## 9. Trip Tables

---

# 9.1 TripEntity

## Purpose

Stores a trip.

Minimum required user field:

```text
title
```

## Fields

```text
TripEntity
- id: String PRIMARY KEY
- title: String NOT NULL
- status: String NOT NULL

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

## Status

```text
PLANNED
COMPLETED
UNKNOWN
```

---

# 9.2 TripStopEntity

## Purpose

Stores main stops in a trip.

Trip stops can be:

- manual
- generated from itinerary groups

## Fields

```text
TripStopEntity
- id: String PRIMARY KEY
- trip_id: String NOT NULL REFERENCES TripEntity(id)

- source: String NOT NULL
- itinerary_group_id: String NULL REFERENCES ItineraryGroupEntity(id)

- place_id: String NULL REFERENCES PlaceEntity(id)

- location_name: String NOT NULL
- country_iso2: String NOT NULL REFERENCES CountryEntity(iso2)
- latitude: Double NULL
- longitude: Double NULL

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

## Source values

```text
MANUAL
ITINERARY_GROUP
```

## Generated stop rule

If:

```text
source = ITINERARY_GROUP
```

then these fields are generated from itinerary data:

```text
location_name
country_iso2
latitude
longitude
date fields if derivable
```

They should not be edited directly by the user.

Potentially editable fields:

```text
notes
is_visible
future display title
future photos
```

---

# 9.3 ExcursionEntity

## Purpose

Stores a side route attached to a main trip stop.

Example:

```text
Main stop: Tokyo
Excursion: Kamakura and Yokohama
```

## Fields

```text
ExcursionEntity
- id: String PRIMARY KEY
- trip_id: String NOT NULL REFERENCES TripEntity(id)
- anchor_trip_stop_id: String NOT NULL REFERENCES TripStopEntity(id)

- title: String NULL

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

## Notes

An excursion belongs to one trip and one anchor main trip stop.

---

# 9.4 ExcursionStopEntity

## Purpose

Stores ordered stops inside an excursion.

Example:

```text
Excursion from Tokyo:
1. Kamakura
2. Yokohama
```

## Fields

```text
ExcursionStopEntity
- id: String PRIMARY KEY
- excursion_id: String NOT NULL REFERENCES ExcursionEntity(id)

- place_id: String NULL REFERENCES PlaceEntity(id)

- location_name: String NOT NULL
- country_iso2: String NOT NULL REFERENCES CountryEntity(iso2)
- latitude: Double NULL
- longitude: Double NULL

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

## Notes

Excursion stops count for country tracking like main trip stops.

---

## 10. Future Photo Tables

Photos should be prepared in the data model, even if the MVP UI does not fully support them.

---

# 10.1 PhotoEntity

## Purpose

Stores metadata for local photos.

## Fields

```text
PhotoEntity
- id: String PRIMARY KEY
- local_uri: String NOT NULL
- caption: String NULL
- taken_at: String NULL
- created_at: String NOT NULL
- updated_at: String NOT NULL
```

## Notes

`local_uri` should refer to a local Android URI/path.

Long-term handling needs care because Android file permissions and document URIs can change.

---

# 10.2 TripPhotoEntity

```text
TripPhotoEntity
- trip_id: String NOT NULL REFERENCES TripEntity(id)
- photo_id: String NOT NULL REFERENCES PhotoEntity(id)
- sort_order: Int NOT NULL

PRIMARY KEY(trip_id, photo_id)
```

---

# 10.3 TripStopPhotoEntity

```text
TripStopPhotoEntity
- trip_stop_id: String NOT NULL REFERENCES TripStopEntity(id)
- photo_id: String NOT NULL REFERENCES PhotoEntity(id)
- sort_order: Int NOT NULL

PRIMARY KEY(trip_stop_id, photo_id)
```

---

# 10.4 ExcursionPhotoEntity

```text
ExcursionPhotoEntity
- excursion_id: String NOT NULL REFERENCES ExcursionEntity(id)
- photo_id: String NOT NULL REFERENCES PhotoEntity(id)
- sort_order: Int NOT NULL

PRIMARY KEY(excursion_id, photo_id)
```

---

# 10.5 ExcursionStopPhotoEntity

```text
ExcursionStopPhotoEntity
- excursion_stop_id: String NOT NULL REFERENCES ExcursionStopEntity(id)
- photo_id: String NOT NULL REFERENCES PhotoEntity(id)
- sort_order: Int NOT NULL

PRIMARY KEY(excursion_stop_id, photo_id)
```

---

## 11. Recommended Indexes

Indexes should be added for commonly queried relationships.

```text
CountryLogEntity(country_iso2)
CountryUserStateEntity(country_iso2)

CountryStatsSummaryEntity(country_iso2)
CountryStatFactEntity(country_iso2)
CountryStatFactEntity(country_iso2, category)
CountryStatFactEntity(country_iso2, key)

AirportEntity(iata)
AirportEntity(country_iso2)
AirportEntity(city)

FlightEntity(origin_airport_id)
FlightEntity(destination_airport_id)
FlightEntity(itinerary_group_id)
FlightEntity(status)

ItineraryEntity(trip_id)
ItineraryGroupEntity(itinerary_id)

TripEntity(status)

TripStopEntity(trip_id)
TripStopEntity(country_iso2)
TripStopEntity(itinerary_group_id)
TripStopEntity(source)

ExcursionEntity(trip_id)
ExcursionEntity(anchor_trip_stop_id)

ExcursionStopEntity(excursion_id)
ExcursionStopEntity(country_iso2)

PlaceEntity(country_iso2)
PlaceEntity(provider, provider_place_id)
```

---

## 12. Relationship Summary

```text
Country 1 → 0/1 CountryUserState
Country 1 → many CountryLogs
Country 1 → 0/1 CountryStatsSummary
Country 1 → many CountryStatFacts
Country 1 → many Airports
Country 1 → many Places
Country 1 → many TripStops
Country 1 → many ExcursionStops

Airport 1 → many origin Flights
Airport 1 → many destination Flights

Itinerary 1 → many ItineraryGroups
ItineraryGroup 1 → many Flights
ItineraryGroup 1 → 0/1 generated TripStop

Trip 1 → 0/1 Itinerary
Trip 1 → many TripStops
Trip 1 → many Excursions

TripStop 1 → many Excursions as anchor
Excursion 1 → many ExcursionStops

Photo many-to-many Trip
Photo many-to-many TripStop
Photo many-to-many Excursion
Photo many-to-many ExcursionStop
```

---

## 13. Country Derivation Inputs

Country state derivation should read from the following tables.

## 13.1 Wished

From:

```text
CountryUserStateEntity.wished
```

---

## 13.2 Currently living

From:

```text
CountryUserStateEntity.currently_living
```

---

## 13.3 Lived

From:

```text
CountryLogEntity.type = LIVED
CountryUserStateEntity.currently_living = true
```

---

## 13.4 Visited

From:

```text
CountryLogEntity.type = VISIT
CountryLogEntity.type = LIVED
CountryUserStateEntity.currently_living = true
TripStopEntity where parent Trip.status = COMPLETED
ExcursionStopEntity where parent Trip.status = COMPLETED
Solo FlightEntity where status = COMPLETED and destination country matches
Solo FlightEntity where status = COMPLETED and origin country matches and origin_counts_for_country_tracking = true
```

Grouped flights should not directly count countries. Their effect should come through generated trip stops.

---

## 13.5 Planned

From:

```text
TripStopEntity where parent Trip.status = PLANNED
ExcursionStopEntity where parent Trip.status = PLANNED
Solo FlightEntity where status = PLANNED and destination country matches
Solo FlightEntity where status = PLANNED and origin country matches and origin_counts_for_country_tracking = true
```

There is no manual planned country marker.

---

## 14. Generated Itinerary Stop Data Rules

Generated itinerary stops are stored in:

```text
TripStopEntity
```

with:

```text
source = ITINERARY_GROUP
itinerary_group_id = ...
```

## 14.1 Rule for generated location

For each itinerary group linked to a trip:

| Group position | Generated stop location |
|---|---|
| Not last group | Destination airport of the last flight in the group |
| Last group | Origin airport of the first flight in the group |

## 14.2 Generated fields

Generated from airport/flight data:

```text
location_name
country_iso2
latitude
longitude
date fields if derivable
```

## 14.3 User-editable generated fields

Allowed:

```text
notes
is_visible
future photos
future custom display title
```

Not allowed directly:

```text
country_iso2
latitude
longitude
location_name
```

To change those, edit the itinerary group/flights.

---

## 15. JSON Backup Considerations

JSON backup should include user data, not static datasets.

Include:

```text
CountryUserStateEntity
CountryLogEntity
FlightEntity
ItineraryEntity
ItineraryGroupEntity
TripEntity
TripStopEntity
ExcursionEntity
ExcursionStopEntity
PlaceEntity
PhotoEntity
photo join tables
```

Usually exclude:

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

---

## 16. JSON Backup Identity Strategy

Because backup/import may happen after static datasets are updated, user data should reference static data using stable IDs.

Use:

```text
country_iso2
airport_id
```

For airports, if possible, use IATA code as `airport_id`.

This makes backup portable across app installs.

---

## 17. Delete Behavior Recommendations

Room foreign key behavior should be chosen carefully.

Recommended:

## Countries

Do not allow deleting countries in normal app usage.

Static countries are reference data.

---

## Airports

Do not allow deleting airports in normal app usage.

Static airports are reference data.

---

## Trips

If a trip is deleted, delete:

```text
TripStops
Excursions
ExcursionStops
Trip photo links
```

If the trip has a linked itinerary, decide in implementation whether to:

- unlink itinerary, or
- ask user whether to delete itinerary too

Recommended default:

```text
unlink itinerary, do not delete itinerary automatically
```

---

## Itineraries

If itinerary is deleted, delete:

```text
ItineraryGroups
```

For flights in deleted groups, either:

- delete them, or
- convert to solo flights

Recommended behavior:

Ask the user.

MVP implementation can choose delete-with-itinerary if simpler, but this should be explicit.

---

## Itinerary groups

If an itinerary group is deleted:

- grouped flights should be deleted or moved
- generated trip stop should be removed or updated

Recommended behavior:

Ask the user or delete group flights together.

---

## Places

Do not delete places automatically if referenced by stops.

Unused places can be cleaned later.

---

## Photos

Deleting photo records should not necessarily delete the physical image file unless the app copied it into app-owned storage.

Photo handling is future work.

---

## 18. Open Data Model Questions

These should be resolved before implementation or during Milestone 0.

1. Should airports without IATA be included in MVP?
2. Should `FlightEntity.airline` be plain text or reference an `AirlineEntity` later?
3. Should `aircraft_type` be plain text or reference an `AircraftTypeEntity` later?
4. Should generated itinerary stops have a custom display title from the start?
5. Should `TripStopEntity` and `ExcursionStopEntity` store duplicated location fields if they already reference `PlaceEntity`?
6. Should date/time fields be ISO strings, epoch millis, or a structured local date/time model?
7. Should imported static datasets be updated by replacing rows or diffing rows?
8. Should JSON import merge with existing data or replace all user data?
9. Should photos be copied into app-owned storage or referenced from original Android URIs?
10. Should `UNKNOWN` trips/flights affect country derivation at all?

---

## 19. Recommendations for Open Questions

## 19.1 Airports without IATA

For MVP:

```text
Include only IATA airports if easier.
```

Reason:

- simpler search
- simpler flight entry
- enough for most commercial flights

---

## 19.2 Airline and aircraft data

For MVP:

```text
airline = plain text
aircraft_type = plain text
```

Later, add:

```text
AirlineEntity
AircraftTypeEntity
```

if needed.

---

## 19.3 Custom display title for generated stops

Recommended:

```text
Add generated_display_title nullable later if needed.
```

For MVP, use airport city/name.

---

## 19.4 Duplicated location fields on stops

Recommended:

```text
Keep duplicated location fields on TripStop and ExcursionStop.
```

Reason:

- preserves the stop even if place cache changes
- easier backup/import
- generated itinerary stops may not come from Place
- manual fallback is simple

`place_id` can remain optional.

---

## 19.5 Flight date/time storage

Recommended initial approach:

```text
ISO-8601 local date-time string + optional timezone string
```

Example:

```text
planned_departure_local_datetime = "2026-06-14T09:30"
planned_departure_timezone = "Europe/Madrid"
```

This is easier than epoch-only because flight times are usually displayed in local airport time.

---

## 19.6 JSON import behavior

Recommended MVP behavior:

```text
Replace all user data after confirmation.
```

Reason:

- simpler
- avoids merge conflicts
- safer for first version

Later, add merge/import options.

---

## 19.7 UNKNOWN status derivation

Recommended:

```text
UNKNOWN does not count as planned or visited by default.
```

If the user wants it to count, they should set status to:

```text
PLANNED
```

or:

```text
COMPLETED
```

---

## 20. Implementation Priority

Recommended order:

1. Implement shared enums and flexible date model.
2. Implement static country tables.
3. Implement country user state and logs.
4. Implement country state derivation.
5. Implement airport table.
6. Implement flight table.
7. Implement itinerary and itinerary group tables.
8. Implement trip/stop/excursion tables.
9. Implement generated stop service.
10. Implement JSON backup/import models.

---

## 21. Summary

Atlas Data Model v1 should be built around these key decisions:

- separate country identity from user state and country stats
- store detailed country stats in flexible fact table
- use structured flexible date columns
- use explicit trip/flight status
- store generated itinerary stops as marked trip stops
- use excursions for side routes
- prepare photo metadata for future UI
- use JSON backup for user data only
- keep country states derived, not manually stored

This data model is designed to be practical for MVP while supporting the richer long-term Atlas vision.