# Atlas MVP Data Model

## 0. Document Purpose

This document defines the **data model for the Atlas MVP**.

The MVP is intentionally smaller than the full Atlas product. It focuses on:

```text
countries/territories
wished/currently living state
manual visit/lived logs
simple trips
ordered trip stops
location search/manual fallback
derived country/territory states
JSON backup/import
```

This document should be treated as the schema guide for the first implementation.

Out of scope for this MVP data model:

```text
flights
airports
itineraries
itinerary groups
generated itinerary stops
excursions
photos
advanced country stat facts
airlines
aircraft
```

Those are covered in later data-model documents.

---

## 1. MVP Data Model Principles

### 1.1 Keep the schema small

The MVP should only implement tables needed by the first usable version.

Required MVP entities:

```text
CountryEntity
DatasetMetadataEntity
CountryUserStateEntity
CountryLogEntity
TripEntity
TripStopEntity
```

Optional MVP entity:

```text
PlaceEntity
```

Do not implement unused v2.0 tables just because they exist in the long-term model.

---

### 1.2 Make the small schema future-aware

The MVP schema should still preserve decisions that would be painful to change later.

Keep from the beginning:

```text
country/territory classification
stable country IDs
flexible date structure
explicit trip status with in_progress
trip stop sort order
optional coordinates
dataset metadata
JSON backup versioning
```

---

### 1.3 Derived country states are computed

The MVP should not store visited/planned/lived as permanent truth.

Derived states are computed from:

```text
CountryUserStateEntity
CountryLogEntity
TripEntity
TripStopEntity
```

A cache can be added later if performance requires it.

---

## 2. MVP Shared Types

### 2.1 DatePrecision

```text
YEAR
MONTH
DAY
```

---

### 2.2 TravelStatus

Used by `TripEntity`.

```text
PLANNED
IN_PROGRESS
COMPLETED
UNKNOWN
```

Rules:

```text
PLANNED trip stops count as planned
IN_PROGRESS trip stops count as visited
COMPLETED trip stops count as visited
UNKNOWN trip stops do not affect derived country state by default
```

---

### 2.3 StatusSource

Optional for MVP.

```text
MANUAL
INFERRED
```

Minimum MVP implementation can omit this and store only explicit status.

Recommended MVP implementation:

```text
Trip.status_source nullable
```

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

### 2.6 PlaceProvider

Only needed if `PlaceEntity` is implemented.

```text
OSM
MANUAL
OTHER
```

---

## 3. Flexible Dates

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
2023
06-2023
14-06-2023
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
if both are present, both must use the same precision
```

---

### 3.3 Room column pattern

For MVP entities with date ranges:

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
CountryLogEntity
TripEntity
TripStopEntity optional
```

Sorting approximation:

```text
YEAR  -> YYYY-01-01
MONTH -> YYYY-MM-01
DAY   -> YYYY-MM-DD
```

Display must preserve precision.

---

## 4. Static Reference Tables

## 4.1 CountryEntity

Purpose:

Stores country/territory identity data.

It does not store:

```text
wished
visited
planned
lived
currently living
logs
trip stops
```

Fields:

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

- `iso2` is the main stable key.
- For special entities without ISO codes, use a stable custom ID if needed.
- The MVP should include both sovereign countries and travel-relevant territories/special regions.
- `is_trackable = false` can be used for entities that exist in the dataset but should not appear in normal tracking.

---

## 4.2 DatasetMetadataEntity

Purpose:

Tracks imported static dataset versions.

Fields:

```text
DatasetMetadataEntity
- key: String PRIMARY KEY
- version: String NOT NULL
- imported_at: String NOT NULL
```

MVP keys:

```text
countries
```

Optional future/MVP key if Place provider metadata is versioned:

```text
geocoding_provider
```

---

## 5. User Country Tracking Tables

## 5.1 CountryUserStateEntity

Purpose:

Stores simple per-country/per-territory user flags.

Fields:

```text
CountryUserStateEntity
- country_iso2: String PRIMARY KEY REFERENCES CountryEntity(iso2)
- wished: Boolean NOT NULL DEFAULT false
- currently_living: Boolean NOT NULL DEFAULT false
- updated_at: String NOT NULL
```

Rules:

```text
Only one row can have currently_living = true.
currently_living implies lived.
lived implies visited.
```

Implementation recommendation:

Enforce currently living through a use case:

```text
SetCurrentlyLivingCountryUseCase
```

When setting a new currently living country:

```text
unset previous currently living country
set selected country currently_living = true
update timestamp
```

Optional:

```text
create or maintain a LIVED CountryLog
```

For MVP, it is acceptable for lived/visited to be derived directly from `currently_living`.

---

## 5.2 CountryLogEntity

Purpose:

Stores manual country/territory logs.

Fields:

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

Valid `type` values:

```text
VISIT
LIVED
```

Rules:

```text
VISIT makes the country/territory visited.
LIVED makes the country/territory lived and visited.
```

---

## 6. Trip Tables

## 6.1 TripEntity

Purpose:

Stores a simple travel experience.

Minimum required user field:

```text
title
```

Fields:

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

Required status values:

```text
PLANNED
IN_PROGRESS
COMPLETED
UNKNOWN
```

Minimum MVP status behaviour:

```text
User manually chooses status.
Dates do not automatically override status.
```

Optional better MVP behaviour:

```text
Dates can suggest status.
User accepts or ignores the suggestion.
```

---

## 6.2 TripStopEntity

Purpose:

Stores ordered main stops in a trip.

Fields:

```text
TripStopEntity
- id: String PRIMARY KEY
- trip_id: String NOT NULL REFERENCES TripEntity(id)

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

Required MVP behaviour:

```text
new stops are appended to the end of the trip
user can reorder stops manually
sort_order stores route order
```

Notes:

- `place_id` is optional because stops duplicate their location data.
- Coordinates can be null for manual fallback stops.
- Trip stops inherit status from the parent trip in MVP.
- Do not add itinerary-specific fields in MVP unless you want to prepare migration early.

Recommended MVP decision:

```text
Do not include source or itinerary_group_id yet.
Add them in v2.0 migration.
```

---

## 7. Optional Place Table

## 7.1 PlaceEntity

Purpose:

Stores selected searched places or manual fallback places.

This table is optional in MVP. If it slows implementation, store location fields directly on `TripStopEntity`.

Fields:

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

Provider values:

```text
OSM
MANUAL
OTHER
```

Manual fallback example:

```text
provider = MANUAL
display_name = "Small village near Kyoto"
country_iso2 = "JP"
latitude = null
longitude = null
```

Recommended MVP approach:

```text
Implement PlaceEntity only if location search is implemented early.
Otherwise, start with direct TripStop fields and add PlaceEntity later.
```

---

## 8. Derived Country/Territory State

## 8.1 Derived State Model

Domain result:

```text
CountryTrackingState
- wished: Boolean
- currently_living: Boolean
- lived: Boolean
- visited: Boolean
- planned: Boolean
- never_visited: Boolean
```

`never_visited` is computed as:

```text
visited == false && lived == false
```

A country/territory can be wished and never visited at the same time.

---

## 8.2 Wished

From:

```text
CountryUserStateEntity.wished = true
```

---

## 8.3 Currently Living

From:

```text
CountryUserStateEntity.currently_living = true
```

---

## 8.4 Lived

True if:

```text
CountryUserStateEntity.currently_living = true
OR CountryLogEntity.type = LIVED
```

---

## 8.5 Visited

True if:

```text
CountryLogEntity.type = VISIT
OR CountryLogEntity.type = LIVED
OR CountryUserStateEntity.currently_living = true
OR TripStopEntity.country_iso2 matches country AND parent Trip.status = COMPLETED
OR TripStopEntity.country_iso2 matches country AND parent Trip.status = IN_PROGRESS
```

MVP simplification:

```text
If a trip is in progress, all its stops count as visited.
```

---

## 8.6 Planned

True if:

```text
TripStopEntity.country_iso2 matches country AND parent Trip.status = PLANNED
```

There is no manual planned country toggle in MVP.

---

## 8.7 Unknown Trip Stops

Recommended MVP rule:

```text
Trip stops whose parent trip has status UNKNOWN do not affect country state.
```

They should still appear in the trip.

---

## 9. JSON Backup v1

## 9.1 Backup Scope

MVP backup includes user-created data:

```text
CountryUserStateEntity
CountryLogEntity
TripEntity
TripStopEntity
PlaceEntity if used
```

MVP backup excludes static datasets:

```text
CountryEntity
DatasetMetadataEntity
```

But it includes dataset metadata values.

---

## 9.2 Backup Top-Level Structure

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
    "tripStops": [],
    "places": []
  }
}
```

If `PlaceEntity` is not implemented, omit `places`.

---

## 9.3 Backup Identity Strategy

Use stable country identifiers.

Country references should include:

```text
country_iso2
country_name_snapshot optional
```

Do not rely only on internal database IDs.

Trip and log IDs can be UUIDs generated by the app.

---

## 9.4 Import Behaviour

Recommended MVP import behaviour:

```text
replace all user data after confirmation
```

Reason:

- simpler
- avoids merge conflicts
- easier to test
- safer for the first version

Import should validate:

```text
backupVersion
required fields
country_iso2 exists in current dataset
trip references exist
trip stop references exist
```

If validation fails, the app should show a clear error and avoid partial destructive import.

---

## 10. Delete Behaviour

### 10.1 Countries

Countries/territories are static reference data.

Normal app usage should not delete them.

---

### 10.2 CountryUserState

If wished is false and currently_living is false, the row may be deleted or kept.

Recommended:

```text
Keep row simple; either approach is acceptable.
```

---

### 10.3 CountryLog

Deleting a log removes its contribution to derived state.

If it was the only visit/lived evidence, the country state should update accordingly.

---

### 10.4 Trip

Deleting a trip should delete its trip stops.

Recommended foreign key behaviour:

```text
TripEntity delete cascades to TripStopEntity
```

---

### 10.5 TripStop

Deleting a stop removes its contribution to planned/visited derivation.

---

### 10.6 Place

If `PlaceEntity` is used:

```text
Do not delete places automatically if referenced by stops.
Unused places can be cleaned later.
```

---

## 11. Recommended Indexes

```text
CountryEntity(name_ca)
CountryEntity(continent)
CountryEntity(type)
CountryEntity(is_trackable)

CountryUserStateEntity(country_iso2)
CountryUserStateEntity(wished)
CountryUserStateEntity(currently_living)

CountryLogEntity(country_iso2)
CountryLogEntity(type)

TripEntity(status)
TripEntity(title)

TripStopEntity(trip_id)
TripStopEntity(country_iso2)
TripStopEntity(sort_order)

PlaceEntity(country_iso2)
PlaceEntity(provider, provider_place_id)
```

---

## 12. MVP Relationships

```text
Country 1 -> 0/1 CountryUserState
Country 1 -> many CountryLogs
Country 1 -> many TripStops

Trip 1 -> many TripStops

Place 1 -> many TripStops optional
```

---

## 13. Minimum Room Entities

Required:

```text
CountryEntity
DatasetMetadataEntity
CountryUserStateEntity
CountryLogEntity
TripEntity
TripStopEntity
```

Optional:

```text
PlaceEntity
```

Do not implement for MVP:

```text
AirportEntity
FlightEntity
ItineraryEntity
ItineraryGroupEntity
ExcursionEntity
ExcursionStopEntity
PhotoEntity
CountryStatsSummaryEntity
CountryStatFactEntity
```

---

## 14. MVP Migration Readiness

The MVP schema should be ready for v2.0 migrations.

Expected v2.0 additions:

```text
AirportEntity
FlightEntity
ItineraryEntity
ItineraryGroupEntity
TripStopEntity.source
TripStopEntity.itinerary_group_id
backupVersion = 2
airportDatasetVersion
```

To make this easy:

```text
use UUID/string IDs for user entities
use stable country_iso2 references
keep TripStopEntity clean and ordered
avoid hardcoding assumptions that trips never have generated stops
```

---

## 15. Implementation Order

Recommended order:

```text
1. shared enums and flexible date model
2. CountryEntity and country dataset import
3. DatasetMetadataEntity
4. CountryUserStateEntity
5. CountryLogEntity
6. Country state derivation service
7. TripEntity
8. TripStopEntity
9. PlaceEntity if used
10. JSON backup/export models
11. JSON import validation
```

---

## 16. Summary

The MVP data model should be intentionally small.

It should include:

```text
countries/territories
dataset metadata
country user state
country logs
trips
trip stops
optional places
```

It should not include:

```text
flights
airports
itineraries
excursions
photos
advanced stats
```

The MVP schema should still be future-aware by supporting:

```text
broad country/territory classification
flexible dates
explicit trip status with in_progress
stable identifiers
ordered trip stops
JSON backup metadata
```

This gives Atlas a clean first implementation without locking the app out of v2.0 expansion.
