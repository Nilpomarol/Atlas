# Atlas Post-v2.0 Roadmap

## 0. Document Purpose

This document defines the Atlas product roadmap from v3.0 onwards. v2.0 is complete (committed 2026-06-02, polish 2026-06-03).

It should be read alongside:

```text
Atlas_Product_Specification.md   — long-term product vision and domain model
Atlas_Roadmap.md                 — general roadmap philosophy and v1/v2 direction
Atlas_v2.0_Roadmap.md            — v2.0 milestone detail (historical)
```

---

## 1. v3.0 — Flight Foundations

**Theme:** Give flights real data, a real map, and fast input. Complete all data-layer and map work that the v3.1 redesign depends on.

Items are ordered by dependency: the API comes first because it gives realistic test data immediately and informs what the datasets must cover.

### 3.1 Flight API integration

Look up a flight by number + date and pre-fill all fields: airline, origin/destination airports, scheduled times, aircraft type. Imports a clean `FlightEntity` ready for manual review and editing.

- Search flow: enter IATA flight number + date → preview card → confirm import
- Adds three fields to `FlightEntity`: `fetched_from: manual | api` (default: manual), `external_provider` (nullable), `external_id` (nullable)
- Provider: TBD (AeroDataBox via RapidAPI is the recommended candidate)
- Graceful fallback to manual entry when flight is not found

### 3.2 Airlines dataset

- Bundle `assets/data/airlines.json`: IATA code, name, country, logo asset reference
- Import into Room (`AirlineEntity`)
- Resolve airline name and logo from IATA code in flight list cards and flight detail
- Logo assets: SVG or PNG for major airlines; graceful text fallback for unknowns

### 3.3 Aircraft type dataset

- Bundle `assets/data/aircraft_types.json`: ICAO/common code, full name, category, image asset reference
- Import into Room (`AircraftTypeEntity`)
- Resolve aircraft display name and category (narrowbody / widebody / regional / turboprop)
- Representative image per type; fallback for unknown types

### 3.4 Polygon/Canvas flight map

Replace the MapLibre view in FlightDetailScreen with a Canvas-drawn world map:

- Simplified continent outline polygons drawn with Compose Canvas
- Great-circle arc between origin and destination airports
- Airport dot markers (status-coloured) at each endpoint
- Fully offline — no tile server, no SDK dependency

TripMapPreview keeps MapLibre (real geographic context for trip stops is worth the SDK).

### 3.5 UTC / local flight times

Per spec §8.3, extend `FlightEntity` with UTC counterparts:

```text
scheduled_departure_utc_at nullable
scheduled_arrival_utc_at nullable
actual_departure_utc_at nullable
actual_arrival_utc_at nullable
```

Calculate UTC from local time using the airport timezone already stored in `AirportEntity`. Display toggle in flight detail: local / UTC / both.

### 3.6 Auto-suggest location search

Replace the manual search-button flow in trip stop and excursion stop dialogs with debounced live suggestions as the user types. Results appear below the field after a short delay — no button press required.

### 3.7 Country tracking flags on flights

Per spec §8.4, add two toggles to flight detail:

```text
destination_counts_for_country_tracking  (default: true)
origin_counts_for_country_tracking       (default: false)
```

Useful for return flights or historical records where the user wants both countries to count. Update `CountryStateDerivationService` to respect these flags.

---

## 2. v3.1 — Visual Redesign

**Theme:** Redesign the trip and flight screens now that all v3.0 content is in place.

Doing the redesign after v3.0 means it is built around known content: polygon map dimensions, airline logo placement, aircraft image proportions, UTC/local time display. Stop rows in the trip redesign are designed with an explicit photo-slot placeholder (v3.2 will fill it).

- **Flights list + detail** — full redesign using polygon map, airline logo, aircraft image, UTC/local toggle
- **Trips list + detail** — full redesign; stop rows include photo-slot placeholder for v3.2
- **All dialogs** — polish pass; auto-suggest already wired from v3.0

---

## 3. v3.2 — Per-Stop Photos

**Theme:** Attach personal photos to the places you visited.

### 3.2.1 Photo data model

- `StopPhoto` entity: `id`, `stop_id`, `stop_type` (TRIP_STOP | EXCURSION_STOP), `local_uri`, `caption`, `sort_order`, `taken_at`, `created_at`
- `READ_MEDIA_IMAGES` permission
- Store URI references only — local-first, no file copies
- Room migration

### 3.2.2 Photo attachment

- Add / remove / reorder photos on trip stops and excursion stops
- Optional caption per photo
- Uses the Android photo picker API — no custom gallery screen needed

### 3.2.3 Gallery UI in stop rows

The specific display pattern (inline thumbnail strip vs. count chip opening a full-screen viewer) is a design decision to be made during the v3.1 redesign. Implementation in v3.2 follows that decision.

---

## 4. v4.0 — Country Depth

**Theme:** Make country/territory detail pages the emotional core of the app.

### 4.1 Country stats dataset

- Bundle `assets/data/country_stats_summary.json` and `assets/data/country_stats_facts.json`
- Import into Room (`CountryStatsSummaryEntity`, `CountryStatFactEntity`)
- Dataset covers: capital, population, area, GDP, HDI, life expectancy, currency, languages, timezone
- Flexible fact table: category/key/value model supports future stats without a Room migration

### 4.2 Country stats/info page

- New screen accessible from country detail
- Sections: geography, demographics, economy, government, health, education, culture, rights, environment, tourism, transport
- Empty sections hidden automatically
- Facts rendered with label, value, unit, year, and source attribution

### 4.3 Country detail — real map

- Replace dot-grid hero with MapLibre map centered on the country
- Show capital marker
- Country boundary polygon if `country_polygons.json` dataset is available (can be deferred to v7.0)

### 4.4 Country detail enrichment

- Add **related flights** section: solo flights whose origin or destination is in this country
- Improve timeline to consistently include: manual logs, trip stops, excursion stops, generated itinerary stops, solo flights, and itinerary group entries
- Add link to country stats/info page from country detail

### 4.5 Country list improvements

- Filter by type: sovereign states / territories / special regions
- Group by continent or subregion (optional toggle)
- Show flag in list cards

---

## 5. v5.0 — Stats and World Map

**Theme:** Let the user see their travel history at a glance.

### 5.1 Interactive world map

- Full-screen MapLibre map showing all countries/territories coloured by state: visited → green, lived → purple, planned → blue, wished → pink, currently living → orange, never → neutral
- Tap country to open country detail
- Accessible from dashboard and as a dedicated tab or screen

### 5.2 Dashboard world map card

- Mini world map card on dashboard showing visited countries
- Tap to expand to full interactive world map

### 5.3 Global stats dashboard

New dedicated stats screen:

```text
Countries visited / total
Territories visited / total
Continents visited
Countries wished / planned / lived
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

- `inferred`: the app may suggest COMPLETED when the date range is clearly in the past
- `manual`: never override
- Inference is conservative with partial dates
- User can always override inferred status

---

## 6. v6.0 — Photos and Memories

**Theme:** Transform travel records into personal memories.

Per-stop photos (trip stops and excursion stops) are already handled in v3.2. This version adds the remaining photo associations and the story mode that depends on a rich photo library.

### 6.1 Country photos

- `CountryPhoto` association
- Shown in country detail as a personal photo section

### 6.2 Story / slideshow mode

Per spec §13.3, a trip story mode presenting a trip as a visual narrative:

```text
Title slide     — trip name, dates, countries
Map overview    — full route
Flights section — if an itinerary is linked
Stop-by-stop    — location, photos, notes
Excursions      — route, photos, notes
Final summary   — countries visited, distance, stats
```

Entry point from trip detail. Read-only presentation mode. Requires v3.2 photos to be complete and populated for the experience to be meaningful.

---

## 7. v7.0 — Advanced Portability and Maps

**Theme:** Own your data fully and explore it anywhere.

### 7.1 Advanced backup and import

- Schema version migration in import (handle older backup versions gracefully)
- Partial import recovery: report and skip bad records rather than failing the entire import
- Photo URI availability check on import
- Import validation report shown to user before confirming

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

## 8. Future / Unscheduled

```text
Country comparison       — side-by-side country stat comparison
Advanced country groups  — continent/subregion stat summaries
Multi-language support   — Spanish and English UI translations
Route animation          — animated flight paths and trip routes on maps
Trip timeline view       — vertical timeline of all stops/excursions/flights in chronological order
Smart map pre-download   — automatic tile caching for upcoming trips
```

---

## 9. Summary

```
v3.0  Flight foundations : API, airlines+logos, aircraft+images, polygon flight map, UTC times, auto-suggest, country flags
v3.1  Visual redesign    : flights list/detail, trips list/detail, all dialogs
v3.2  Per-stop photos    : stop photo model, photo picker, gallery UI in stop rows
v4.0  Country depth      : stats dataset, stats page, country map, detail improvements, list filters
v5.0  Stats + world map  : interactive world map, global stats dashboard, date inference
v6.0  Photos + memories  : country photos, story/slideshow mode
v7.0  Portability        : offline maps, country polygons, advanced export, optional cloud backup
```

Each version adds one coherent layer. The app remains fully usable at every stage.
