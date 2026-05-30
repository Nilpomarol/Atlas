# Atlas Roadmap

## 0. Document Purpose

This document defines the **general long-term roadmap** for Atlas.

It explains how the product should evolve over time, from the first MVP to later versions. It is intentionally higher-level than the detailed MVP and v2.0 roadmaps.

Detailed implementation plans are defined separately:

```text
Atlas_MVP_Roadmap.md
Atlas_v2.0_Roadmap.md
```

Related reference documents:

```text
Atlas_Product_Specification.md
Atlas_MVP_Specification.md
Atlas_v2.0_Specification.md

Atlas_Data_Model.md
Atlas_MVP_Data_Model.md
Atlas_v2.0_Data_Model.md

Atlas_Technical_Architecture.md
```

---

## 1. Roadmap Philosophy

Atlas should be built incrementally.

The app should not try to implement the full long-term vision at once. Each version should add one coherent layer of value while keeping the app usable and stable.

Roadmap principles:

```text
Build a useful local-first foundation first.
Avoid adding unused data models too early.
Prefer working vertical slices over broad incomplete systems.
Keep MVP small.
Move flights and itineraries to v2.0.
Move photos, story mode, advanced stats, and cloud features later.
```

Atlas should evolve in product layers:

```text
MVP: country/territory tracking + simple trips
v2.0: flights + itineraries + generated stops
Later: excursions + richer maps + stats + memories/photos
Far future: story mode, cloud backup, advanced datasets
```

---

## 2. Version Overview

Recommended version structure:

```text
MVP / v1.0
- local-first foundation
- countries/territories
- wished/currently living
- manual visit/lived logs
- simple trips
- ordered trip stops
- location search/manual fallback
- JSON backup/import

v2.0
- airports
- manual solo flights
- itineraries
- itinerary groups
- layover-safe country derivation
- generated trip stops from itinerary groups
- route visualization
- backup/import v2

v3.0 candidates
- excursions
- richer trip maps
- richer country timelines
- basic personal travel stats
- country stats summary
- better dashboard

Later versions
- photos
- story/slideshow mode
- advanced country stats/info pages
- airline logos
- aircraft images/specs
- advanced flight enrichment
- interactive world map
- cloud backup/sync optional
```

---

## 3. MVP / v1.0 Direction

The MVP should establish Atlas as a useful personal travel atlas without flights.

MVP theme:

```text
A local-first Android app for tracking countries/territories and simple trips.
```

Core MVP value:

```text
The user can track where they have been, where they have lived, what they wish to visit, and basic trips with stops.
```

MVP includes:

```text
Android project foundation
Room database
Jetpack Compose UI
Navigation
Catalan-first UI
countries/territories static dataset
country/territory list
country/territory detail
wished toggle
currently living toggle
manual visit/lived logs
simple trips
ordered trip stops
location search
manual fallback for stops
derived country/territory states
JSON backup/import
basic dashboard
```

MVP excludes:

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
multi-language support
```

MVP success definition:

```text
Atlas is already useful for personal travel tracking even before flights exist.
```

---

## 4. v2.0 Direction

v2.0 should make air travel part of Atlas.

v2.0 theme:

```text
Flights and itineraries become first-class citizens.
```

Core v2.0 value:

```text
The user can record flights and model real travel itineraries without incorrectly counting layovers as visited countries.
```

v2.0 includes:

```text
airport static dataset
airport search
manual solo flights
flight list/detail
flight status
local/UTC flight time modelling
itinerary creation
itinerary groups
flights inside groups
group reordering
flight reordering inside groups
layover-safe country derivation
link one itinerary to one trip
generated trip stops
flight route visualization
itinerary route visualization
trip maps with generated stops
country detail timeline with flight/itinerary entries
JSON backup/import v2
basic flight statistics
```

v2.0 excludes:

```text
flight API import
live flight status
airline logos
aircraft images/specs
photos
story mode
cloud sync
advanced country comparison
```

v2.0 success definition:

```text
Atlas understands flights and itineraries well enough to represent real trips without false country visits from layovers.
```

---

## 5. v3.0 Candidate Direction

v3.0 is not fixed yet. It should be chosen after using the MVP and v2.0.

Good v3.0 candidates:

```text
excursions
better trip maps
better country timeline
personal travel stats dashboard
country stats summary
richer dashboard
trip timeline view
basic visual polish pass
```

Recommended v3.0 theme:

```text
Richer travel memories and better visual exploration.
```

Possible v3.0 option A:

```text
Excursions and trip detail richness
```

Includes:

```text
excursions attached to trip stops
excursion stops
excursion route maps
country derivation from excursion stops
trip timeline improvements
```

Possible v3.0 option B:

```text
Stats and dashboard
```

Includes:

```text
personal travel stats
visited/wished/planned/lived charts
trips by year
flights by year
basic distance flown
country stats summary cards
```

Possible v3.0 option C:

```text
Visual map improvement
```

Includes:

```text
better trip maps
flight arcs
map styling
country detail maps
continent/country visual summaries
```

Recommended decision rule:

```text
Choose v3.0 based on which part feels most painful or most exciting after actually using MVP + v2.0.
```

---

## 6. Later Feature Groups

### 6.1 Photos and Memories

Possible features:

```text
trip photos
trip stop photos
excursion photos
photo captions
photo sorting
photo memory sections
```

Reason to delay:

```text
photo handling on Android has storage and URI-permission complexity
```

---

### 6.2 Story / Slideshow Mode

Possible features:

```text
trip story mode
map intro
stop-by-stop narrative
photos and notes
flight route intro/outro
final trip summary
```

Reason to delay:

```text
requires solid trips, stops, maps, photos, and visual design first
```

---

### 6.3 Advanced Country Info

Possible features:

```text
country stats summary
detailed country fact pages
geography/demographics/economy/government/health/education/culture sections
country comparison
```

Reason to delay:

```text
requires dataset sourcing, maintenance, and UI design work
```

---

### 6.4 Advanced Flights

Possible features:

```text
flight API lookup
flight number/date search
live status
delays
airline logos
aircraft images/specs
airline dataset
aircraft type dataset
```

Reason to delay:

```text
depends on external APIs and introduces data quality/provider problems
```

---

### 6.5 Cloud / Sync

Possible features:

```text
optional cloud backup
cross-device sync
encrypted backups
account system
```

Reason to delay:

```text
conflicts with local-first simplicity and adds backend complexity
```

---

## 7. Priority Map

### Must Build First

```text
local database
country/territory dataset
country list/detail
wished/currently living
manual logs
trips
trip stops
backup/import
```

### Important Next

```text
airports
flights
itineraries
itinerary groups
generated stops
route visualization
```

### Valuable Later

```text
excursions
personal stats
better maps
country stats summary
trip timeline
```

### Nice But Not Urgent

```text
photos
story mode
airline logos
aircraft images
flight API import
cloud backup
multi-language support
```

---

## 8. Dependency Logic

Some features should come before others.

```text
Countries/territories before trips
Trips before trip stops
Trip stops before location search polish
Trip stops before country derivation from trips
Backup after core user data exists

Airports before flights
Solo flights before itineraries
Itineraries before generated trip stops
Generated stops before advanced trip maps
Flights/itineraries before flight statistics

Excursions after main trip stops
Photos after trips/stops are stable
Story mode after photos/maps/trip timelines are stable
Cloud backup after local backup/import is reliable
```

---

## 9. Risk Management

### Risk: Overbuilding the MVP

Mitigation:

```text
Keep flights, itineraries, excursions, photos, and advanced stats out of MVP.
```

### Risk: Documentation drift

Mitigation:

```text
Keep product specs, data models, architecture, and roadmaps aligned by version.
```

### Risk: Dataset complexity

Mitigation:

```text
Start with practical datasets and improve them later.
```

### Risk: External APIs slow development

Mitigation:

```text
Avoid mandatory APIs in MVP and v2.0.
```

### Risk: Derived country states become buggy

Mitigation:

```text
Centralize derivation logic and test it.
```

---

## 10. Summary Roadmap

```text
MVP / v1.0
Build the local-first country/territory + simple trip foundation.

v2.0
Add flights, airports, itineraries, itinerary groups, generated stops, and flight route visualization.

v3.0
Choose between excursions, richer maps, stats/dashboard, or country timeline improvements.

Later
Add photos, story mode, advanced country stats, advanced flight enrichment, and optional cloud backup.
```

Atlas should grow by adding coherent layers, not by implementing every possible feature at once.
