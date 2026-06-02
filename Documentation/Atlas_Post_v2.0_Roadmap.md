# Atlas Post-v2.0 Roadmap

## 0. Document Purpose

This document defines the Atlas product roadmap from v3.0 onwards, after v2.0 is complete.

It should be read alongside:

```text
Atlas_Product_Specification.md   — long-term product vision and domain model
Atlas_Roadmap.md                 — general roadmap philosophy and v1/v2 direction
Atlas_v2.0_Roadmap.md            — v2.0 milestone detail
```

---

## 1. v2.0 Completion Checklist

Before v3.0 begins, the following v2.0 work must be finished:

| Milestone | Task | Status |
|-----------|------|--------|
| M7c | Map UX pass — canvas consistency across TripMapPreview and FlightDetailScreen, remove PROVISIONAL badge | ⬜ |
| M8 | **Real map SDK** — MapLibre replaces all Canvas route previews (trip map, flight detail hero, country detail hero) | ⬜ |
| M9 | **Backup v2** — extend JSON backup/import to include flights, itineraries, itinerary groups, excursions, excursion stops | ⬜ |
| M10 | **Visual polish pass** — apply Warm Editorial Atlas style to trip list/detail, flights list/detail, itinerary list/detail | ⬜ |

**Blocking decision before M8:** choose map SDK. Recommended: MapLibre GL Android (open source, OSM-based, no API key, offline-capable). This decision is required before any v3.0+ map work.

---

## 2. v3.0 — Data Enrichment

**Theme:** Make flights richer with real airline, aircraft, and enrichment data.

This version adds the datasets and UI enrichment that turn raw flight records into meaningful travel entries. No external API required for the core dataset work.

### 3.1 Airlines dataset

- Bundle `assets/data/airlines.json` with IATA code, name, country, and logo asset reference
- Import into Room (`AirlineEntity`)
- Resolve airline name from IATA code in flight list cards and flight detail

### 3.2 Airline logos

- Add logo assets (SVG or PNG) for major airlines
- Display airline logo on flight detail screen and flight list cards
- Graceful fallback when no logo available

### 3.3 Aircraft type dataset

- Bundle `assets/data/aircraft_types.json` with ICAO/common code, full name, category
- Import into Room (`AircraftTypeEntity`)
- Resolve aircraft display name from stored aircraft string in flight detail
- Show aircraft category (narrowbody / widebody / regional / turboprop)

### 3.4 Aircraft images

- Add representative aircraft images for major types
- Display on flight detail screen
- Graceful fallback for unknown types

### 3.5 Flight API preparation fields

Per spec §8.5, add to `FlightEntity` without requiring live API yet:

```text
fetched_from: manual | api | other   (default: manual)
external_provider nullable
external_id nullable
```

These fields allow future API import without a schema migration.

### 3.6 Country tracking flags on flights

Per spec §8.4, add toggles to flight detail:

```text
destination_counts_for_country_tracking  (default: true)
origin_counts_for_country_tracking       (default: false)
```

Useful for return flights or historical records where the user wants both countries to count.
Update `CountryStateDerivationService` to respect these flags.

---

## 3. v4.0 — Country Depth

**Theme:** Make country/territory detail pages the emotional core of the app.

### 4.1 Country stats dataset

- Bundle `assets/data/country_stats_summary.json` and `assets/data/country_stats_facts.json`
- Import into Room (`CountryStatsSummaryEntity`, `CountryStatFactEntity`)
- Dataset covers: capital, population, area, GDP, HDI, life expectancy, currency, languages, timezone
- Flexible fact table supports future stat additions without Room migrations (category/key/value model per spec §12.4)

### 4.2 Country stats/info page

- New screen accessible from country detail
- Sections: geography, demographics, economy, government, health, education, culture, rights, environment, tourism, transport
- Facts rendered per category with label, value, unit, year, and source attribution
- Empty sections hidden automatically

### 4.3 Country detail — real map

- Replace dot grid hero with live MapLibre map centered on the country
- Show capital marker
- Show country boundary polygon if `country_polygons.json` dataset available (can be deferred)

### 4.4 Country detail enrichment

- Add **related flights** section: solo flights whose origin or destination is in this country
- Improve timeline to consistently include: manual logs, trip stops, excursion stops, generated itinerary stops, solo flights, and itinerary group entries
- Add link to country stats/info page from country detail

### 4.5 Country list improvements

- Filter by type: sovereign states / territories / special regions
- Group by continent or subregion (optional toggle)
- Show flag in list cards

---

## 4. v5.0 — Stats and World Map

**Theme:** Let the user see their travel history at a glance.

### 5.1 Interactive world map

- Full-screen MapLibre map showing all countries/territories colored by state:
  - visited → green
  - lived → purple
  - planned → blue
  - wished → pink
  - currently living → orange
  - never visited → neutral
- Tap country to open country detail
- Accessible from dashboard and as a dedicated tab or screen

### 5.2 Dashboard world map card

- Mini world map card on dashboard showing visited countries
- Tap to expand to full interactive world map

### 5.3 Global stats dashboard

New dedicated stats screen including:

```text
Countries visited / total
Territories visited / total
Continents visited
Countries wished
Countries planned
Countries lived
Trips completed / planned
Flights completed / planned
Distance flown (km)
Most visited countries
Trips by year (bar chart)
Flights by year (bar chart)
Top origin airports
Top destination airports
```

### 5.4 Status inference from dates

Per spec §3.6, add `status_source: manual | inferred` to trips and flights.

Rules:
- If `status_source = inferred`, the app may update status when dates clearly indicate completion (past completed date range → suggest COMPLETED)
- If `status_source = manual`, never override
- Inference should be conservative with partial dates
- User can always override inferred status manually

### 5.5 UTC/local flight times

Per spec §8.3, extend `FlightEntity` with UTC counterparts:

```text
scheduled_departure_utc_at nullable
scheduled_arrival_utc_at nullable
actual_departure_utc_at nullable
actual_arrival_utc_at nullable
```

Display both local and UTC times in flight detail. Calculate UTC from local time using airport timezone data already stored in `AirportEntity`.

---

## 5. v6.0 — Photos and Memories

**Theme:** Transform travel records into personal memories.

### 6.1 Photo model and storage

- `PhotoEntity`: id, local_uri, caption, taken_at, created_at
- Join tables: `TripPhoto`, `TripStopPhoto`, `ExcursionPhoto`, `ExcursionStopPhoto`, `CountryPhoto`
- `READ_MEDIA_IMAGES` permission
- Local-first: store URI references, not file copies
- Room migrations for new tables

### 6.2 Photo attachment — trips and stops

- Add/remove photos from trip detail
- Add/remove photos from trip stop detail
- Add/remove photos from excursion detail and excursion stop detail
- Optional caption per photo
- Ordered photo grid

### 6.3 Country photos

- Optional `CountryPhoto` association
- Shown in country detail as a personal photo section

### 6.4 Story / slideshow mode

Per spec §13.3, a trip story mode that presents a trip as a visual narrative:

```text
Title slide — trip name, dates, countries
Map overview — full route
Itinerary/flights section — if linked
Stop-by-stop sections — location, photos, notes
Excursion sections — route, photos
Final summary — countries visited, distance, stats
```

Entry point from trip detail. Read-only presentation mode.

---

## 6. v7.0 — Advanced Portability and Maps

**Theme:** Own your data fully and explore it anywhere.

### 7.1 Advanced backup and import

- Schema version migration in import (handle older backup versions gracefully)
- Partial import recovery (report and skip bad records rather than failing entire import)
- Photo URI availability check on import
- Import validation report shown to user before confirming
- Backup includes new v3/v4/v5/v6 data: airlines, aircraft, photos, stat dataset versions

### 7.2 Offline map caching

- Cache map tiles for regions containing saved trip stops and upcoming trips
- MapLibre offline API integration
- User-initiated download with size estimate
- Manage cached regions in settings

### 7.3 Country polygons

- Bundle `assets/data/country_polygons.json` for accurate country boundary rendering
- Show country polygon on country detail map
- Use polygons to shade visited/planned/wished countries on the world map

### 7.4 Advanced export

- CSV export per domain (trips, flights, countries)
- Selective JSON export (choose which data domains to include)

### 7.5 Optional cloud backup

- Google Drive integration, opt-in only
- Local-first remains the default
- Encrypted backup option
- No mandatory account system

---

## 7. Future / Unscheduled

Features from the product spec with no assigned version yet:

```text
Flight API lookup — lookup by flight number + date, import times/airline/aircraft, live status, delays
Country comparison — side-by-side country stat comparison
Advanced country grouping — continent/subregion stats summaries
Multi-language support — Spanish and English UI translations
Interactive route animation — animated flight paths and trip routes on maps
Trip timeline view — vertical timeline of all stops/excursions/flights in chronological order
Local map caching improvements — smart region pre-download
```

---

## 8. Summary

```
v2.0  (finishing)   Map SDK, backup v2, visual polish
v3.0                Data enrichment: airlines, logos, aircraft, API prep fields, country tracking flags
v4.0                Country depth: stats dataset, stats page, country map, country detail improvements
v5.0                Stats and world map: interactive map, global stats dashboard, date inference, UTC times
v6.0                Photos and memories: photo attachment, country photos, story/slideshow mode
v7.0                Advanced portability: offline maps, country polygons, advanced export, cloud backup
```

Each version adds one coherent layer. The app remains fully usable at every stage.
