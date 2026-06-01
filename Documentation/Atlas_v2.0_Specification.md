# Atlas v2.0 Specification

## 0. Document Purpose

This document defines the scope for **Atlas v2.0**, the first major version after the MVP.

The MVP establishes the core local-first country/territory and simple trip tracking foundation. Atlas v2.0 expands that foundation by making **flights and itineraries first-class parts of the app**.

This document is not the full product specification. The long-term product direction should remain in:

```text
Atlas_Product_Specification.md
```

The MVP build scope should remain in:

```text
Atlas_MVP_Specification.md
```

This document defines the next coherent product step after MVP, not the final version of Atlas.

---

## 1. v2.0 Goal

The goal of Atlas v2.0 is:

```text
Turn Atlas from a country/trip tracker into a fuller travel atlas by adding flights, itineraries, itinerary groups, generated trip stops, excursions, flight route visualization, and richer country/trip derivation.
```

After v2.0, the user should be able to:

- create manual solo flights
- create itineraries
- group flights into meaningful itinerary legs
- avoid counting layovers as visited countries
- link an itinerary to a trip
- generate trip stops from itinerary groups
- add excursions attached to trips or main trip stops
- add ordered stops inside excursions
- derive country/territory states from flights and itineraries
- see flight routes visually
- see better country/trip timelines
- get a more complete travel history from structured data

The key product upgrade is that Atlas should start understanding **air travel structure**, not just trip stops.

---

## 2. Relationship With MVP

Atlas v2.0 assumes the MVP already includes:

```text
- Android foundation
- Room database
- Catalan UI
- countries/territories dataset
- countries/territories list
- country/territory detail page
- wished toggle
- currently living toggle
- manual visit/lived logs
- simple trips
- ordered trip stops
- location search/manual fallback
- derived country/territory states from logs and trip stops
- JSON backup/import
```

v2.0 should build on this foundation rather than rewriting it.

Existing MVP concepts should remain valid:

- local-first data
- flexible dates
- explicit status
- countries and territories, not only sovereign states
- non-obligatory data entry
- JSON portability
- Catalan-first UI

---

## 3. v2.0 Product Theme

The theme of v2.0 is:

```text
Flights and itineraries become first-class citizens.
```

v2.0 should focus on depth in one new domain rather than expanding in every direction.

Main additions:

```text
Flights
Itineraries
Itinerary groups
Airport dataset
Flight-derived country/territory states
Generated trip stops from itinerary groups
Flight route visualization
Better country timelines
Better trip maps
Basic travel stats expansion
```

v2.0 should still avoid becoming the final complete Atlas product.

---

## 4. Included in v2.0

Atlas v2.0 includes:

```text
1. Airport static dataset
2. Manual solo flight creation
3. Flight list
4. Flight detail page
5. Flight status
6. Local and UTC flight time modelling
7. Itinerary creation
8. Itinerary groups
9. Flights inside itinerary groups
10. Reordering groups and flights
11. Layover-safe country/territory derivation
12. Linking one itinerary to one trip
13. Generated trip stops from itinerary groups
14. Excursions attached to trips or main trip stops
15. Ordered excursion stops
16. Flight route visualization
17. Better trip map using manual, generated, and excursion stops
18. Better country/territory timeline
19. Expanded JSON backup/import for new entities
20. Basic travel statistics involving flights
```

---

## 5. Explicitly Out of Scope for v2.0

The following are not included in v2.0:

```text
1. Flight API lookup/import
2. Live flight status updates
3. Airline logos
4. Aircraft images
5. Aircraft specifications dataset
6. Full airline dataset
7. Photos
8. Story/slideshow mode
9. Advanced country comparison
10. Cloud sync
11. Social sharing
12. Multi-language support
13. Complex offline maps
14. Advanced airport search beyond the local dataset
15. Full 50–150 field country info UI
16. Automatic boarding pass parsing
17. Calendar integration
18. Collaborative trips
```

Important rule:

```text
v2.0 may prepare the data model for future API-enriched flights, but it should not implement flight API import yet.
```

Manual creation must remain the core interaction.

---

## 6. v2.0 Core User Flow

The main v2.0 user flow is:

```text
Open app
→ go to Flights
→ create solo flight or itinerary
→ add flights
→ group itinerary flights into meaningful legs
→ optionally link itinerary to a trip
→ see generated trip stops
→ see countries/territories update correctly
→ view route maps and timelines
→ export/import backup including flights and itineraries
```

The app should make flights useful even if the user does not link them to trips.

---

## 7. Flights

### 7.1 Flight Purpose

A flight is an individual air travel segment from one airport to another.

Examples:

```text
Barcelona → Doha
Doha → Tokyo
Osaka → Doha
Doha → Barcelona
```

A flight can exist as:

```text
solo flight
flight inside an itinerary group
```

Minimum required fields:

```text
origin airport
destination airport
status
```

Optional fields:

```text
planned departure time
planned arrival time
actual departure time
actual arrival time
airline
flight number
aircraft type
notes
country tracking flags
```

---

### 7.2 Flight Status

Supported flight statuses:

```text
planned
in_progress
completed
unknown
```

Rules:

```text
completed solo flight → destination may count as visited
planned solo flight → destination may count as planned
in_progress solo flight → destination should not count as visited until completed
unknown solo flight → should not affect country/territory state by default
```

For grouped itinerary flights, country derivation should come from itinerary groups, not individual flight segments.

---

### 7.3 Flight Status and Dates

Status remains explicit.

Dates and times may suggest status, but they should not override manual status without user consent.

Recommended conceptual fields:

```text
status
status_source: manual | inferred
```

Minimum v2.0 rule:

```text
The user manually sets flight status.
```

Better v2.0 rule:

```text
The app may suggest a status based on dates/times, but the user remains in control.
```

---

### 7.4 Local and UTC Time Modelling

v2.0 should model flight times correctly.

Flights cross timezones and may cross date boundaries, so local-only time storage can become ambiguous.

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

The UI does not need to expose UTC aggressively.

User-facing display should prioritize:

```text
origin local departure time
destination local arrival time
```

UTC should exist mainly for reliable sorting, duration calculation, and future API compatibility.

---

### 7.5 Solo Flight Country/Territory Derivation

Solo flights count the destination country/territory by default.

Default flags:

```text
destination_counts_for_country_tracking = true
origin_counts_for_country_tracking = false
```

Example:

```text
Flight: Barcelona → Tokyo
Status: completed
Destination counts: true

Result:
Japan counts as visited.
```

The origin can optionally count if enabled.

Example:

```text
Flight: Tokyo → Barcelona
Status: completed
Origin counts: true

Result:
Japan also counts as visited.
```

This allows the user to decide how return flights and historical entries should affect country tracking.

---

### 7.6 Flight Data Model

Conceptual model:

```text
Flight
- id
- origin_airport_id
- destination_airport_id
- status: planned | in_progress | completed | unknown
- status_source: manual | inferred optional

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

- created_at
- updated_at
```

For v2.0:

```text
fetched_from should usually be manual.
external_provider and external_id exist only for future compatibility.
```

---

## 8. Airports

### 8.1 Airport Dataset

v2.0 requires an airport static dataset.

The dataset should support manual flight creation and route visualization.

Conceptual model:

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

Required fields for v2.0:

```text
iata
name
city
country_id
latitude
longitude
```

Recommended field:

```text
timezone
```

Timezone is important for local/UTC time handling.

---

### 8.2 Airport Search

The user should be able to search airports when creating a flight.

Search should support:

```text
IATA code
airport name
city
country/territory
```

Examples:

```text
BCN
Barcelona
El Prat
Japan
Tokyo
HND
NRT
```

The MVP location search provider is not enough for airports. Airports should come from a local static dataset.

---

### 8.3 Airport Dataset Size

The initial v2.0 dataset does not need every tiny airport in the world.

Recommended approach:

```text
Start with commercial/passenger airports with IATA codes.
```

This is enough for personal flight tracking and keeps search manageable.

Future versions may include:

```text
regional airports
heliports
closed airports
airline-specific metadata
```

---

## 9. Itineraries

### 9.1 Itinerary Purpose

An itinerary is a collection of related flights.

Examples:

```text
Japan 2026 flights
Amsterdam weekend flights
Ireland summer flights
Return from Japan
```

An itinerary can exist:

```text
independently
linked to one trip
```

Rule:

```text
A trip can have zero or one linked itinerary.
An itinerary can be linked to zero or one trip.
```

---

### 9.2 Itinerary Model

Conceptual model:

```text
Itinerary
- id
- title
- trip_id nullable unique
- notes optional
- created_at
- updated_at
```

`trip_id` should be unique to enforce one itinerary per trip.

---

### 9.3 Itinerary Groups

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

Groups are required because they distinguish real destinations from layovers.

Without groups, a simple segment list could incorrectly mark layover countries as visited.

---

### 9.4 Itinerary Group Model

Conceptual model:

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

Status may be:

```text
planned
in_progress
completed
unknown
```

However, v2.0 may derive group status from the flights inside the group if that is simpler.

Minimum v2.0 implementation:

```text
Group has title and sort order.
Flights inside group have status.
```

Better v2.0 implementation:

```text
Group has its own status for clearer country derivation.
```

---

### 9.5 Reordering

v2.0 should support:

```text
reorder itinerary groups
reorder flights inside a group
```

This matters because generated stop logic depends on group order and flight order.

Rules:

```text
Group order determines travel leg sequence.
Flight order inside group determines first origin and final destination.
```

---

## 10. Layover-Safe Country/Territory Derivation

### 10.1 Layover Rule

Layovers do not count as visited countries/territories by default.

Example:

```text
Barcelona → Doha → Tokyo
```

If Doha is only a connection, Qatar should not count as visited.

This is one of the most important reasons for itinerary groups.

---

### 10.2 Group Derivation Rule

For flights inside itinerary groups, country/territory derivation comes from the group, not from every flight segment.

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

This avoids generating a final stop for the home airport.

---

### 10.3 Itinerary Without Trip

If an itinerary is not linked to a trip, the derived place from each group should still affect country/territory state.

Example:

```text
Itinerary: China trip flights

Group 1:
Barcelona → Doha
Doha → Shanghai

Derived place:
China
```

If the group/itinerary is completed, China counts as visited.

If planned, China counts as planned.

If unknown, it should not affect state by default.

---

### 10.4 Itinerary Linked to Trip

If an itinerary is linked to a trip, each group should generate one main trip stop.

Generated stops should appear in the trip route together with manual trip stops.

Generated stop rule:

```text
Not the last group → destination of the last flight in the group
Last group → origin of the first flight in the group
```

Generated stops should inherit or derive status from:

```text
the linked trip status
the itinerary group status
the flights inside the group
```

Recommended v2.0 simplification:

```text
If linked to a trip, generated stops use the parent trip status for country derivation.
```

More precise future rule:

```text
Generated stop status can be derived from group/flight status independently.
```

---

## 11. Generated Trip Stops

### 11.1 Purpose

Generated trip stops connect the flights domain with the trips domain.

Example:

```text
Trip: Japan 2026
Itinerary: Japan 2026 flights

Group 1 generated stop:
Tokyo

Group 2 generated stop:
Osaka
```

This allows Atlas to automatically create meaningful route stops from flights without counting layovers.

---

### 11.2 Generated Stop Edit Rules

Generated itinerary stops should appear in the trip but should not have core generated fields directly edited.

Core generated fields:

```text
location
country/territory
coordinates
derived date/time
```

To change those, the user should edit the itinerary group or flights.

Editable fields:

```text
display title
notes
visibility in trip
visibility in map
```

Optional for v2.0:

```text
custom display title
custom notes
```

Visibility controls can be deferred if necessary.

---

### 11.3 Store or Compute Generated Stops

Open implementation question:

```text
Should generated stops be physically stored or computed dynamically?
```

Recommended v2.0 approach:

```text
Store generated stops as TripStop records with source = itinerary_group.
```

Reason:

- easier to display in trip route
- easier to sort with manual stops
- easier to include in backup
- easier to attach notes later
- easier to debug

But core generated fields should be regenerated or validated when itinerary data changes.

Conceptual field:

```text
TripStop.source: manual | itinerary_group
TripStop.itinerary_group_id nullable
```

---

## 12. Trip Changes in v2.0

The MVP trip system should be extended, not replaced.

v2.0 trip additions:

```text
link itinerary to trip
show generated itinerary stops
mix manual stops and generated stops
show related flights
show route map with generated stops
show trip country/territory summary
```

Trip detail should include:

```text
manual route stops
generated itinerary stops
linked itinerary summary
related flights
map preview
country/territory summary
```

Trip stop ordering must handle both manual and generated stops.

---

## 12.1 Excursions

Excursions are included in v2.0 as secondary routes inside a trip.

Primary use case:

```text
Trip: Japan 2026
Main stop: Kyoto
Excursion: Nara and Uji day loop
Excursion stops: Kyoto -> Nara -> Uji -> Kyoto
```

Recommended model:

```text
Excursion belongs to a Trip.
Excursion may optionally be anchored to a main TripStop.
Excursion has ordered ExcursionStops.
```

Country tracking rule for v2.0:

```text
Excursion stops can contribute to country/territory state using the parent trip status.
Layover-safe flight rules remain separate from excursion rules.
```

Trip detail should show excursions near their anchor stop when present, and in a dedicated excursions section otherwise.

---

## 13. Maps and Visualizations

### 13.1 Flight Route Visualization

v2.0 should add basic flight route visualization.

At minimum:

```text
draw route line between origin and destination airports
show origin and destination markers
```

For itineraries:

```text
show all flight segments
visually distinguish groups if possible
```

Advanced curved great-circle rendering is nice, but not mandatory.

A simple line is acceptable for v2.0 if technically easier.

---

### 13.2 Trip Map Improvements

Trip maps should support:

```text
manual trip stops
generated itinerary stops
basic route lines
available coordinates
```

If some stops lack coordinates, the app should still show the rest.

Missing coordinates should not block trip display.

---

### 13.3 Country/Territory Map

v2.0 may improve country map visualization, but full polygon coloring is not required.

Optional v2.0 improvements:

```text
show approximate country/territory location
show related stops/flights on country detail
show visited/planned/wished counts visually
```

Out of scope:

```text
interactive world map with full country polygon coloring
```

---

## 14. Country/Territory Detail Improvements

v2.0 should improve the country/territory detail page by adding flight and itinerary-related entries.

The combined timeline should include:

```text
manual visit/lived logs
trip stops
generated itinerary stops
excursions
solo flights
itineraries without trip
```

For each related item, the page should show enough context:

```text
source type
trip or itinerary name
date/time if available
status
notes if relevant
```

Example entries:

```text
Visited via trip stop: Tokyo — Japan 2026
Visited via itinerary group: Shanghai — China flights
Visited via solo flight: Barcelona → Tokyo
Manual visit log: 2023
```

This page becomes the best place to understand why a country/territory has a given state.

---

## 15. Flight and Itinerary Screens

### 15.1 Flights List

Required features:

```text
list all flights
show origin and destination
show date/time if available
show status
show solo/grouped indicator
filter by status
search by airport/city/country/airline/flight number
```

Grouped flights should show their itinerary context.

---

### 15.2 Flight Detail

Required features:

```text
origin airport
destination airport
status
planned departure/arrival
actual departure/arrival
airline optional
flight number optional
aircraft type optional
notes optional
linked itinerary/group if applicable
country tracking toggles for solo flights
route preview
```

Country tracking toggles should only be shown for solo flights or under an advanced menu.

---

### 15.3 Itinerary List

Required features:

```text
list itineraries
show title
show linked trip if any
show number of groups
show number of flights
show status summary
create itinerary
```

---

### 15.4 Itinerary Detail / Management

Required features:

```text
edit itinerary title/notes
link or unlink trip
create/edit/delete itinerary groups
reorder groups
add/edit/delete flights inside groups
reorder flights inside groups
show generated stop preview
show itinerary route preview
```

This is the most important new screen in v2.0.

---

### 15.5 Link Itinerary to Trip Flow

The user should be able to:

```text
create itinerary independently
link itinerary to existing trip
create itinerary from inside a trip
unlink itinerary from trip
```

Rules:

```text
one trip can have at most one linked itinerary
one itinerary can be linked to at most one trip
```

If a trip already has an itinerary, the app should prevent linking another one unless the existing link is removed.

---

## 16. Basic Stats Expansion

v2.0 should add basic flight-related statistics.

Required:

```text
total flights
completed flights
planned flights
countries/territories visited via flights
countries/territories planned via flights
itineraries count
```

Optional:

```text
total distance flown
flights by year
most used airports
most visited destination airports
flights by airline
```

Distance calculation is optional because it requires reliable coordinates and geodesic calculation, but it is a good v2.0 stretch feature.

---

## 17. JSON Backup and Import v2.0

v2.0 must extend JSON backup/import to include new data.

New backup entities:

```text
airports dataset version metadata
flights
itineraries
itinerary groups
generated itinerary stops if physically stored
```

Backups do not need to include the full airport static dataset.

They should include:

```text
airportDatasetVersion
```

Flight backup payloads should use stable airport identifiers.

Recommended:

```text
origin_iata
destination_iata
```

Do not rely only on internal database IDs.

Example structure:

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
    "flights": [],
    "itineraries": [],
    "itineraryGroups": []
  }
}
```

Import should handle:

```text
missing airports
unknown IATA codes
changed airport dataset versions
orphaned itinerary groups
orphaned flights
duplicate records
trip-itinerary uniqueness conflicts
```

Minimum acceptable behaviour:

```text
Fail safely with a clear error message if required airport or country references cannot be resolved.
```

---

## 18. Database Migration From MVP

v2.0 requires a Room migration from the MVP schema.

New tables likely include:

```text
AirportEntity
FlightEntity
ItineraryEntity
ItineraryGroupEntity
```

Existing tables likely modified:

```text
TripEntity
TripStopEntity
```

Potential `TripEntity` change:

```text
No required change if Itinerary has trip_id.
```

Potential `TripStopEntity` changes:

```text
source: manual | itinerary_group
itinerary_group_id nullable
```

Migration principle:

```text
Existing MVP user data must remain intact.
```

No existing trip or country data should be deleted or reinterpreted unexpectedly.

---

## 19. v2.0 Navigation

Recommended navigation after v2.0:

```text
Dashboard
Countries
Trips
Flights
Settings
```

Flights section may include:

```text
Flights list
Flight detail
Itineraries list
Itinerary detail
Airport search
```

The app may expose Flights and Itineraries as separate tabs or combine them under one Travel/Air Travel section.

Recommended simple approach:

```text
Main tab: Flights
Inside Flights:
- Solo flights
- Itineraries
```

---

## 20. v2.0 Development Phases

### Phase 0: Technical Decisions

Decide:

```text
airport dataset source
airport dataset size
timezone handling strategy
flight time storage format
generated stops storage strategy
map route rendering approach
```

---

### Phase 1: Airport Foundation

Build:

```text
AirportEntity
airport dataset import
airport search
airport selection UI
```

Success:

```text
The user can search and select airports by IATA, name, city, or country.
```

---

### Phase 2: Solo Flights

Build:

```text
FlightEntity
create/edit/delete solo flight
flight list
flight detail
flight status
country derivation from solo flights
```

Success:

```text
A completed solo flight can mark its destination country/territory as visited.
```

---

### Phase 3: Itineraries and Groups

Build:

```text
ItineraryEntity
ItineraryGroupEntity
create/edit/delete itinerary
create/edit/delete groups
add flights to groups
reorder groups
reorder flights inside groups
```

Success:

```text
The user can represent Barcelona → Doha → Tokyo as one outbound group without counting Doha as visited.
```

---

### Phase 4: Itinerary Country Derivation

Build:

```text
group-derived country/territory logic
planned/completed derivation
itineraries without trips affect country state
country detail timeline entries
```

Success:

```text
Countries/territories are derived from itinerary groups, not layover segments.
```

---

### Phase 5: Link Itineraries to Trips

Build:

```text
link itinerary to trip
unlink itinerary
generated trip stops
generated stop preview
trip detail integration
manual + generated stop display
```

Success:

```text
A linked itinerary generates meaningful trip stops and appears correctly in the trip route.
```

---

### Phase 6: Maps

Build:

```text
solo flight route preview
itinerary route preview
trip map with generated stops
basic route lines
```

Success:

```text
Flights and itinerary routes are visually understandable.
```

---

### Phase 7: Backup/Import v2.0

Build:

```text
backup version 2
export flights
export itineraries
export itinerary groups
import flights
import itineraries
import itinerary groups
airport dataset version validation
```

Success:

```text
All v2.0 personal data can be exported and restored.
```

---

### Phase 8: Polish

Build:

```text
empty states
validation
delete confirmations
error handling
UI polish
timeline clarity
basic flight stats
```

Success:

```text
v2.0 feels like a stable expansion, not an experimental add-on.
```

---

## 21. v2.0 Success Criteria

Atlas v2.0 is successful if the user can:

1. Search and select airports from a local airport dataset.
2. Create a manual solo flight.
3. View flights in a flight list.
4. Open a flight detail page.
5. Mark flights as planned, in progress, completed, or unknown.
6. See completed solo flights affect country/territory visited state.
7. Create an itinerary.
8. Create itinerary groups.
9. Add flights inside itinerary groups.
10. Reorder itinerary groups.
11. Reorder flights inside groups.
12. Avoid counting layover countries as visited.
13. Link one itinerary to one trip.
14. See generated trip stops from itinerary groups.
15. See generated stops in the trip detail page.
16. See country/territory detail pages explain flight/itinerary-derived states.
17. View basic flight route visualization.
18. Export/import v2.0 data as JSON.
19. Keep all MVP data intact after upgrading.

---

## 22. v2.0 Quality Bar

Minimum quality expectations:

```text
No data loss from MVP migration
Stable airport search
Correct country derivation
No layover false positives
Clear itinerary group UI
Reliable flight creation/editing
Reliable generated stop logic
Reliable JSON backup/import
Basic route visualization
Clear empty states
Consistent Catalan UI
```

The most important quality requirement is correctness of derivation.

If the app incorrectly marks layovers as visited, v2.0 has failed its main purpose.

---

## 23. Risks and Mitigations

### Risk 1: Flight API Temptation

Mitigation:

```text
Do not implement API lookup/import in v2.0.
Prepare fields only.
Manual flight creation first.
```

---

### Risk 2: Airport Dataset Complexity

Mitigation:

```text
Start with IATA commercial/passenger airports.
Use stable IATA codes in backups.
Improve dataset later.
```

---

### Risk 3: Timezone Complexity

Mitigation:

```text
Store enough timezone information from the airport dataset.
Keep UI simple.
Use UTC internally for sorting and duration when possible.
```

---

### Risk 4: Itinerary UI Becomes Too Complex

Mitigation:

```text
Design around groups first.
Keep each group as a simple ordered list of flights.
Avoid advanced timeline editing in v2.0.
```

---

### Risk 5: Generated Stops Become Confusing

Mitigation:

```text
Clearly label generated stops.
Do not allow direct editing of generated location fields.
Provide a generated stop preview in itinerary detail.
```

---

### Risk 6: Country Derivation Bugs

Mitigation:

```text
Centralize derivation logic.
Write unit tests for solo flights, grouped flights, linked itineraries, and unlinked itineraries.
```

Important test cases:

```text
Barcelona → Doha → Tokyo should count Japan, not Qatar.
Osaka → Doha → Barcelona as return group should count Japan, not Spain.
Solo Barcelona → Tokyo should count Japan.
Planned Barcelona → Tokyo should mark Japan as planned.
Unknown flight should not affect country state.
```

---

## 24. Recommended v2.0 Implementation Order

Recommended practical order:

```text
1. Airport dataset import
2. Airport search
3. Solo flight creation
4. Flight list/detail
5. Solo flight country derivation
6. Itinerary creation
7. Itinerary groups
8. Flights inside groups
9. Group reordering
10. Group-based country derivation
11. Link itinerary to trip
12. Generated trip stops
13. Trip detail integration
14. Country detail timeline integration
15. Route visualization
16. JSON backup/import v2.0
17. Basic flight stats
18. Polish
```

Important note:

```text
Build solo flights before itineraries.
Build itineraries before generated trip stops.
Build generated stops before advanced maps.
```

---

## 25. Summary

Atlas v2.0 should make flights and itineraries first-class features.

The version should add:

```text
- airport dataset
- manual solo flights
- flight list/detail
- itinerary creation
- itinerary groups
- flights inside groups
- layover-safe derivation
- itinerary-to-trip linking
- generated trip stops
- basic route visualization
- expanded backup/import
- basic flight statistics
```

The version should not add:

```text
- flight API import
- airline logos
- aircraft images/specs
- photos
- story mode
- cloud sync
- advanced country comparison
```

The purpose of v2.0 is to transform Atlas from a simple country/trip tracker into a structured travel atlas that understands air travel while remaining local-first, flexible, and buildable.
