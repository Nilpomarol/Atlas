# Atlas v2.0 Data Model

## 0. Document Purpose

This document defines the **data model additions and changes for Atlas v2.0**.

Atlas v2.0 builds on the MVP data model by adding:

```text
airports
manual solo flights
itineraries
itinerary groups
flights inside groups
layover-safe country/territory derivation
generated trip stops
expanded backup/import
basic flight statistics support
```

This document assumes the MVP already contains:

```text
CountryEntity
DatasetMetadataEntity
CountryUserStateEntity
CountryLogEntity
TripEntity
TripStopEntity
PlaceEntity optional
```

---

## 1. v2.0 Data Model Goals

Atlas v2.0 should make flights and itineraries first-class features while preserving MVP data.

Goals:

```text
create manual solo flights
search/select airports from a local dataset
group flights into itineraries
distinguish layovers from meaningful destinations
link one itinerary to one trip
generate trip stops from itinerary groups
derive country/territory states from flights and itinerary groups
extend JSON backup/import
```

Out of scope for v2.0 data model:

```text
flight API import implementation
live flight status
airline logos
aircraft image/spec datasets
photos
excursions
advanced country stat facts UI
cloud sync
```

The schema may include small future-compatible fields for API import, but the feature itself is not part of v2.0.

---

## 2. New Shared Types

### 2.1 FlightFetchSource

```text
MANUAL
API
OTHER
```

v2.0 should mostly use:

```text
MANUAL
```

`API` exists only for future compatibility.

---

### 2.2 TripStopSource

v2.0 introduces generated stops.

```text
MANUAL
ITINERARY_GROUP
```

---

## 3. AirportEntity

## 3.1 Purpose

Stores airport reference data for flight creation, search, route visualization, and timezone handling.

## 3.2 Fields

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

## 3.3 ID Strategy

Recommended:

```text
id = iata
```

when IATA is available.

For airports without IATA:

```text
id = icao
```

or a stable generated ID.

Recommended v2.0 dataset:

```text
commercial/passenger airports with IATA codes
```

This is enough for most personal flight tracking.

## 3.4 Required Indexes

```text
AirportEntity(iata)
AirportEntity(icao)
AirportEntity(city)
AirportEntity(country_iso2)
AirportEntity(name)
```

---

## 4. FlightEntity

## 4.1 Purpose

Stores one flight segment.

A flight can be:

```text
solo flight
flight inside an itinerary group
```

## 4.2 Fields

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

## 4.3 Status Values

```text
PLANNED
IN_PROGRESS
COMPLETED
UNKNOWN
```

Rules for solo flights:

```text
COMPLETED -> destination may count as visited
PLANNED -> destination may count as planned
IN_PROGRESS -> destination should not count as visited until completed
UNKNOWN -> no country derivation by default
```

Rules for grouped flights:

```text
Grouped flights do not directly derive country states.
Itinerary groups derive meaningful destination/origin places.
```

## 4.4 Local and UTC Date/Time

v2.0 should model both local and UTC time fields.

Reason:

```text
Flights cross timezones and date boundaries.
```

User-facing display should prioritize:

```text
origin local departure time
destination local arrival time
```

UTC is mainly for:

```text
sorting
duration calculation
future API compatibility
```

## 4.5 Required Indexes

```text
FlightEntity(origin_airport_id)
FlightEntity(destination_airport_id)
FlightEntity(itinerary_group_id)
FlightEntity(status)
FlightEntity(flight_number)
FlightEntity(airline)
```

---

## 5. ItineraryEntity

## 5.1 Purpose

Stores a collection of related flights.

An itinerary can exist independently or be linked to one trip.

## 5.2 Fields

```text
ItineraryEntity
- id: String PRIMARY KEY
- title: String NOT NULL
- trip_id: String NULL UNIQUE REFERENCES TripEntity(id)
- notes: String NULL
- created_at: String NOT NULL
- updated_at: String NOT NULL
```

## 5.3 Rules

```text
A trip can have zero or one itinerary.
An itinerary can be linked to zero or one trip.
```

This is enforced by:

```text
trip_id UNIQUE
```

If a trip is deleted, recommended behaviour:

```text
unlink itinerary rather than automatically deleting it
```

This avoids losing flight records unexpectedly.

## 5.4 Required Indexes

```text
ItineraryEntity(trip_id)
ItineraryEntity(title)
```

---

## 6. ItineraryGroupEntity

## 6.1 Purpose

Stores a meaningful travel leg inside an itinerary.

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

Groups prevent layover countries from being counted as visits.

## 6.2 Fields

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

## 6.3 Group Status

Minimum v2.0 implementation:

```text
Group has no required status.
Country derivation uses parent trip status if linked, or flight statuses if unlinked.
```

Better v2.0 implementation:

```text
Group has status: PLANNED | IN_PROGRESS | COMPLETED | UNKNOWN
```

Recommended:

```text
Include status nullable from the start.
```

If null:

```text
derive group status from flights or linked trip.
```

## 6.4 Required Indexes

```text
ItineraryGroupEntity(itinerary_id)
ItineraryGroupEntity(sort_order)
ItineraryGroupEntity(status)
```

---

## 7. TripStopEntity Changes

v2.0 modifies the MVP `TripStopEntity` to support generated stops.

## 7.1 New Fields

Add:

```text
source: String NOT NULL DEFAULT MANUAL
itinerary_group_id: String NULL REFERENCES ItineraryGroupEntity(id)
is_visible: Boolean NOT NULL DEFAULT true
```

Optional but useful:

```text
display_title: String NULL
```

## 7.2 Updated TripStopEntity

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

- start_year: Int NULL
- start_month: Int NULL
- start_day: Int NULL
- end_year: Int NULL
- end_month: Int NULL
- end_day: Int NULL
- date_precision: String NULL

- notes: String NULL
- display_title: String NULL
- sort_order: Int NOT NULL
- is_visible: Boolean NOT NULL DEFAULT true

- created_at: String NOT NULL
- updated_at: String NOT NULL
```

## 7.3 Source Rules

```text
MANUAL -> user-created stop
ITINERARY_GROUP -> generated from itinerary group
```

If:

```text
source = ITINERARY_GROUP
```

then these fields are generated and should not be directly edited:

```text
location_name
country_iso2
latitude
longitude
date fields if derived from flights
```

Editable generated-stop fields:

```text
notes
display_title
sort_order maybe
is_visible
```

## 7.4 Unique Constraint

Recommended:

```text
UNIQUE(trip_id, itinerary_group_id)
```

for generated stops, if supported cleanly.

Alternative:

Enforce in `ItineraryGeneratedStopService`.

---

## 8. Generated Itinerary Stop Rules

## 8.1 Purpose

Generated stops connect itinerary groups to trip routes.

They allow a linked itinerary to automatically produce meaningful trip stops while avoiding layovers.

## 8.2 Rule

For each itinerary group linked to a trip:

```text
if group is not last group:
    generated stop = destination airport of last flight in group

if group is last group:
    generated stop = origin airport of first flight in group
```

Example:

```text
Group 1:
Barcelona → Doha
Doha → Tokyo

Generated stop:
Tokyo / Japan
```

```text
Group 2:
Osaka → Doha
Doha → Barcelona

Generated stop:
Osaka / Japan
```

## 8.3 Generated Fields

Generated from airport/flight data:

```text
location_name
country_iso2
latitude
longitude
date fields if derivable
```

## 8.4 Update Behaviour

When itinerary data changes, generated stops must be synchronized.

`ItineraryGeneratedStopService` should:

```text
create missing generated stops
update generated core fields when flights/groups change
remove generated stops when itinerary is unlinked if appropriate
preserve user-editable fields like notes/display_title/is_visible
```

Recommended unlink behaviour:

```text
Ask user whether to remove generated stops or convert them to manual stops.
```

Simpler v2.0 behaviour:

```text
Remove generated stops when itinerary is unlinked.
```

---

## 9. Country/Territory Derivation v2.0

v2.0 expands country derivation beyond MVP logs and trip stops.

## 9.1 Solo Flight Visited

A country/territory counts as visited if:

```text
Solo Flight.status = COMPLETED
AND destination_counts_for_country_tracking = true
AND destination airport country matches
```

Also:

```text
Solo Flight.status = COMPLETED
AND origin_counts_for_country_tracking = true
AND origin airport country matches
```

## 9.2 Solo Flight Planned

A country/territory counts as planned if:

```text
Solo Flight.status = PLANNED
AND destination_counts_for_country_tracking = true
AND destination airport country matches
```

Also:

```text
Solo Flight.status = PLANNED
AND origin_counts_for_country_tracking = true
AND origin airport country matches
```

## 9.3 Solo Flight In Progress

Recommended rule:

```text
IN_PROGRESS solo flight destination does not count as visited until completed.
```

Optional UI state:

```text
show as currently flying / ongoing
```

but do not mark destination as visited.

## 9.4 Grouped Flights

Grouped flights should not directly count countries.

Reason:

```text
layovers should not count as visits
```

Country derivation comes from:

```text
itinerary group derived place
generated trip stop if linked to a trip
```

## 9.5 Itinerary Without Trip

If an itinerary is not linked to a trip, derive country/territory state from each itinerary group.

Derived place:

```text
not last group -> destination of last flight in group
last group -> origin of first flight in group
```

Status source:

```text
group status if set
otherwise derived from grouped flight statuses
```

Rules:

```text
COMPLETED -> derived place counts as visited
PLANNED -> derived place counts as planned
IN_PROGRESS -> derived place may count as visited only if product rule allows; recommended conservative handling
UNKNOWN -> no derivation
```

Recommended v2.0 simplification:

```text
Use group status if present.
If group status is null, use UNKNOWN for derivation.
```

This avoids ambiguous derivation from mixed flight statuses.

## 9.6 Itinerary Linked to Trip

If itinerary is linked to a trip:

```text
generated trip stops are created
country derivation can come from generated TripStopEntity and parent Trip.status
```

Recommended v2.0 rule:

```text
linked itinerary generated stops follow parent trip status
```

This keeps trip and itinerary behaviour understandable.

---

## 10. JSON Backup v2

## 10.1 Backup Version

v2.0 should use:

```text
backupVersion = 2
```

## 10.2 Included Data

Include MVP data:

```text
countryUserStates
countryLogs
trips
tripStops
places if used
```

Add v2.0 data:

```text
flights
itineraries
itineraryGroups
```

If generated stops are stored as `TripStopEntity`, they are included in `tripStops`.

## 10.3 Metadata

Add:

```text
airportDatasetVersion
```

Example:

```json
{
  "backupVersion": 2,
  "createdAt": "2026-05-29T00:00:00Z",
  "countryDatasetVersion": "2026.1",
  "airportDatasetVersion": "2026.1",
  "data": {
    "countryUserStates": [],
    "countryLogs": [],
    "trips": [],
    "tripStops": [],
    "places": [],
    "flights": [],
    "itineraries": [],
    "itineraryGroups": []
  }
}
```

## 10.4 Stable References

Flights should reference airports by stable IDs.

Recommended:

```text
origin_airport_id
destination_airport_id
origin_iata snapshot
destination_iata snapshot
```

If the current airport dataset cannot resolve an airport during import, import should fail safely or mark the flight as unresolved.

Minimum behaviour:

```text
fail safely with a clear message
```

Future behaviour:

```text
allow user to remap missing airports
```

---

## 11. Room Migration From MVP

## 11.1 New Tables

Add:

```text
AirportEntity
FlightEntity
ItineraryEntity
ItineraryGroupEntity
```

## 11.2 Modified Tables

Modify:

```text
TripStopEntity
```

Add columns:

```text
source TEXT NOT NULL DEFAULT 'MANUAL'
itinerary_group_id TEXT NULL
is_visible INTEGER NOT NULL DEFAULT 1
display_title TEXT NULL
```

Add index:

```text
TripStopEntity(itinerary_group_id)
TripStopEntity(source)
```

## 11.3 Dataset Metadata

Add or update:

```text
DatasetMetadataEntity key = airports
```

## 11.4 Migration Principle

```text
No MVP user data should be deleted.
Existing TripStopEntity rows become source = MANUAL.
```

---

## 12. Delete Behaviour

## 12.1 Airport

Airports are static data.

Do not delete in normal app usage.

---

## 12.2 Flight

Deleting a solo flight removes its country derivation contribution.

Deleting a grouped flight may affect itinerary group generated stop logic.

---

## 12.3 Itinerary

If itinerary is deleted:

Recommended options:

```text
delete groups and grouped flights
remove generated trip stops
```

or:

```text
ask user whether grouped flights should be converted to solo flights
```

Simpler v2.0 behaviour:

```text
delete itinerary groups, grouped flights, and generated stops
```

Only use this if clearly confirmed by the user.

---

## 12.4 Itinerary Group

If a group is deleted:

```text
delete or move its flights
remove/update generated stop
recompute group positions
```

Simpler v2.0 behaviour:

```text
delete group flights and generated stop after confirmation
```

---

## 12.5 Trip with Linked Itinerary

If a trip is deleted:

Recommended:

```text
unlink itinerary
delete manual/generated trip stops
keep itinerary and flights
```

This prevents accidental loss of flight records.

---

## 13. Recommended Indexes

New indexes:

```text
AirportEntity(iata)
AirportEntity(icao)
AirportEntity(city)
AirportEntity(country_iso2)

FlightEntity(origin_airport_id)
FlightEntity(destination_airport_id)
FlightEntity(itinerary_group_id)
FlightEntity(status)
FlightEntity(flight_number)
FlightEntity(airline)

ItineraryEntity(trip_id)
ItineraryEntity(title)

ItineraryGroupEntity(itinerary_id)
ItineraryGroupEntity(sort_order)
ItineraryGroupEntity(status)

TripStopEntity(source)
TripStopEntity(itinerary_group_id)
```

---

## 14. Relationship Summary

```text
Country 1 -> many Airports
Airport 1 -> many Flights as origin
Airport 1 -> many Flights as destination

Itinerary 1 -> many ItineraryGroups
ItineraryGroup 1 -> many Flights
ItineraryGroup 1 -> 0/1 generated TripStop

Trip 1 -> 0/1 Itinerary
Trip 1 -> many TripStops

TripStop may be manual or generated from ItineraryGroup
```

---

## 15. v2.0 Domain Services

v2.0 should introduce or expand these services:

```text
AirportSearchService
FlightCountryTrackingService
ItineraryGeneratedStopService
ItineraryGroupDerivationService
CountryStateDerivationService
JsonBackupV2Exporter
JsonBackupV2Importer
```

Most important:

```text
ItineraryGeneratedStopService
CountryStateDerivationService
```

These two services prevent layover-counting bugs.

---

## 16. Test Cases

Important data-model/unit test cases:

```text
Solo completed Barcelona -> Tokyo marks Japan visited.
Solo planned Barcelona -> Tokyo marks Japan planned.
Solo unknown Barcelona -> Tokyo does not affect Japan.
Solo in-progress Barcelona -> Tokyo does not mark Japan visited.

Barcelona -> Doha -> Tokyo as one itinerary group marks Japan, not Qatar.
Osaka -> Doha -> Barcelona as return group marks Japan, not Spain.
Linked itinerary creates generated stops.
Unlinked itinerary derives country state from groups.
Deleting itinerary removes or updates generated stops according to chosen rule.
Existing MVP trip stops migrate to source = MANUAL.
Backup v2 restores flights and itineraries.
```

---

## 17. Implementation Order

Recommended:

```text
1. AirportEntity and dataset metadata
2. airport dataset import
3. airport search
4. FlightEntity
5. solo flight CRUD
6. solo flight country derivation
7. ItineraryEntity
8. ItineraryGroupEntity
9. grouped flights
10. itinerary group derivation
11. TripStopEntity migration for generated stops
12. generated stop service
13. link itinerary to trip
14. country detail timeline integration
15. JSON backup v2
16. JSON import v2
```

---

## 18. Summary

Atlas v2.0 data model adds the air-travel layer to the MVP.

It adds:

```text
AirportEntity
FlightEntity
ItineraryEntity
ItineraryGroupEntity
generated TripStop support
backupVersion 2
airport dataset metadata
flight/itinerary country derivation
```

It should not add:

```text
flight API implementation
airline dataset
aircraft dataset
photos
excursions
advanced stats facts
```

The main modelling objective is:

```text
Represent real flight journeys without counting layovers as visited countries.
```

If that works correctly, v2.0 has achieved its data-model purpose.
