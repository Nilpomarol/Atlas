# Atlas Data Model

## Purpose

This document describes the data model currently implemented by Atlas. It is conceptual and intentionally omits field-by-field duplication that belongs in Kotlin entities and exported Room schemas.

Read `docs/Handoff_Prompt.md` first for the current database version and active phase.

## Core Principles

- Separate static/reference data from user-created data.
- Use stable external identifiers where available.
- Keep derived country state out of persistence.
- Preserve incomplete records and flexible dates.
- Keep Room entities, API DTOs, and domain models separate.
- Add only schema needed by the current version.
- Preserve backup and migration compatibility.

## Current Database

Room database version: **23**.

### Static and Reference Data

- `CountryEntity`: ISO2 identity, localized names, classification, geography, and parent territory relationship.
- `AirportEntity`: stable airport identity, IATA/ICAO codes, location, coordinates, timezone, and country.
- `AirlineEntity`: IATA-keyed airline display metadata.
- `AircraftTypeEntity`: stable aircraft type code, aliases, engine metadata, and curated image reference.
- `DatasetMetadataEntity`: installed bundled-dataset versions.
- `CountryStatFactEntity`: flexible country facts.

Static dataset updates may replace these reference rows according to importer rules. They must not overwrite user-created records.

### External Caches

- `AircraftEntity`: tail-number cache for aircraft lookup results.
- `CountryPhotoEntity`: one cached country photo record per ISO2, including file/source/author metadata and fetch time.

Caches are replaceable and are not the source of truth for personal travel history.

### User-Created Data

- `CountryUserStateEntity`: explicit wished/currently-living markers and related user state.
- `CountryLogEntity`: personal lived/visited country history with flexible dates.
- `TripEntity`: title, status, flexible date range, notes, and optional cover photo filename.
- `TripStopEntity`: ordered trip stops with location, coordinates, dates, notes, and source.
- `ExcursionEntity`: trip-linked side journeys.
- `ExcursionStopEntity`: ordered stops belonging to an excursion.
- `FlightEntity`: solo or itinerary-group flight, route, status, local/UTC times, airline/aircraft data, derived distance, provenance, and country-tracking flags.
- `ItineraryEntity`: optional trip-linked flight itinerary.
- `ItineraryGroupEntity`: ordered group of flights within an itinerary.
- `StopPhotoEntity`: app-private photo file reference associated with either a trip stop or excursion stop.

## Stable Identifiers

- Countries and territories: ISO2 where available.
- Airports: stored stable ID, with IATA preferred for display and external matching.
- Airlines: IATA.
- Aircraft types: curated stable code and aliases.
- User-created records: generated local IDs.

Backups and importers should reconnect reference data through stable external identifiers where possible.

## Country Facts

`CountryStatFactEntity` uses:

```text
PRIMARY KEY (country_iso2, category, key)
```

Fields:

```text
country_iso2
category
key
label_ca
value
unit?
year?
rank?
rank_total?
tier?
sort_order
```

There is no foreign key to `countries`; this preserves dataset import-order independence. `country_iso2` is the join key.

The fact table is intentionally flexible. Adding a fact normally requires a dataset row and version bump, not a Room migration.

Distribution values such as religion and ethnicity may be stored as JSON-array strings in `value`; presentation parses them into display models.

## Country Photo Cache

`CountryPhotoEntity` is keyed by ISO2 and stores:

```text
country_iso2
filename
source_url
author
author_link
fetched_at
```

The image file lives under app-private storage. Refresh is lazy and failure preserves the previous file and row.

## Flexible Dates

Atlas supports:

- year precision;
- month precision;
- full-day precision.

Flexible dates remain structured domain data. Display preserves original precision. Ranges use compatible precision when both endpoints exist.

## Travel Status

Trips and flights use explicit status:

```text
planned
in_progress
completed
unknown
```

Dates may suggest status, but stored/manual status is not casually overridden.

## Country State Derivation

Country tracking state is computed by `CountryStateDerivationService` from:

- explicit wished/currently-living state;
- country logs;
- trips and trip stops;
- excursion stops;
- solo flights;
- itinerary groups and generated itinerary stops.

The result is not stored as a single source-of-truth column.

## Relationships and Deletion

- Trips own trip stops and excursions according to current repository/foreign-key behavior.
- Excursions own excursion stops.
- Itineraries own ordered itinerary groups.
- Flights may be solo or reference an itinerary group.
- Deleting an itinerary group returns its flights to solo according to existing behavior.
- Stop photos use `(stop_id, stop_type)` rather than cross-table foreign keys; repository/use-case deletion removes rows and files.

Any change to relationship or deletion behavior requires migration and backup review.

## Backup

Current backup format version: **3**.

The `.atlasbackup` ZIP contains versioned JSON plus referenced user photo files.
Legacy v1/v2 JSON imports remain supported through defaults.

Optional cloud backup configuration and status are DataStore preferences. They are not
part of the Room schema or the exported personal-data payload. Cloud backups reuse the
same v3 archive format and do not introduce a second data model.

The backup currently excludes:

- bundled static datasets;
- replaceable API caches;
- cached country photo binaries.

Backup changes must not depend on the installed version of a replaceable static dataset when a stable identifier can be used instead.

## Migration Rules

- Migrations are explicit and sequential.
- Existing migration objects are immutable historical behavior.
- SQLite table rebuilds are used when constraints cannot be added safely with `ALTER TABLE`.
- Entity changes require a new database version, migration, exported schema, and wiring update.
- Flexible fact additions should use dataset rows instead of schema expansion.

