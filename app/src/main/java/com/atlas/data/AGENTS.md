# Atlas Data Layer Instructions

## Scope

This package contains:

- Room database
- DAOs
- entities
- migrations
- dataset importers
- API clients
- repository implementations
- mappers between storage/API and domain models
- backup/import implementation details

## Core data principles

- Separate static/reference data from user-created data.
- Updating bundled datasets must not overwrite personal user data.
- Prefer stable external identifiers in static/reference data.
- Keep Room entities and API DTOs out of UI and domain APIs.
- Map data-layer models into domain models before exposing them upward.
- Handle missing/partial/malformed external data defensively.
- Return explicit result/error states where appropriate.

## Static/reference data

Examples:

- countries/territories
- country stats facts
- airports
- airlines
- aircraft types
- dataset metadata

Rules:

- Use dataset metadata/versioning for imported bundled data.
- Make imports idempotent.
- Preserve stable keys used by user data.
- Avoid foreign keys when the current implementation deliberately avoids them for import-order independence.

## User-created data

Examples:

- country user state
- country logs
- trips
- trip stops
- excursions
- excursion stops
- flights
- itineraries
- itinerary groups
- photo references

Rules:

- Do not silently discard user-created data.
- Backup/import compatibility matters.
- Existing backups should import cleanly unless a migration rule explicitly says otherwise.
- Deletions should follow current product behavior and FK semantics.

## Room and migrations

- Keep migrations explicit.
- Never change an existing entity/table without checking the migration chain.
- SQLite cannot add FK constraints to existing columns via simple `ALTER TABLE`; use drop-and-recreate when required.
- For flexible fact-like datasets, prefer additive rows over schema churn where the spec supports it.
- Index common query keys where needed.

## Country stats v4.0 rules

- `country_stat_facts` uses composite key `country_iso2 + category + key`.
- `country_iso2` is the join key.
- Values are preformatted for Catalan display where the dataset contract says so.
- The ViewModel/UI groups facts by category.
- Empty sections are hidden by UI/presentation logic.
- v4.1 should add rows without requiring code changes when possible.

## External services

- External services must be replaceable and non-essential to core personal data access.
- Do not put API keys in source files.
- API clients should handle:
  - no API key
  - HTTP error status
  - rate limiting
  - network failure
  - empty result
  - malformed JSON
  - partial data
- Avoid adding Retrofit unless explicitly approved; current clients use lightweight direct HTTP patterns.

## Image data

- Coil is the app image loading library.
- Do not add another image-loading dependency.
- Prefer local curated assets or cached references when available.
- Remote images must have graceful fallback UI.

## Validation

For data-layer changes, consider:

- unit tests for mappers and domain-facing behavior
- migration safety
- dataset import idempotence
- backup/import compatibility
- null/missing field coverage
