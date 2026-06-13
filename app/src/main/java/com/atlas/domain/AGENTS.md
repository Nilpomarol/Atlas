# Atlas Domain Layer Instructions

## Scope

This package contains:

- domain models
- repository interfaces
- use cases
- validation
- derivation services
- status logic
- flexible date logic
- conceptual backup/import rules

The domain layer owns business truth. It must not depend directly on Room entities, API DTOs, Android UI, or provider-specific SDKs.

## Business rules to keep centralized

- Country/territory state derivation.
- Flexible date validation, formatting, and sorting.
- Status inference and status source behavior.
- Currently living exclusivity.
- Flight country tracking behavior.
- Itinerary generated stop derivation.
- Backup/import conceptual validation.

Do not duplicate these rules in Compose screens, DAOs, or ad-hoc ViewModel code.

## Country state derivation

Country/territory states are derived from user data and explicit user markers.

Inputs may include:

- `CountryUserState`
- `CountryLog`
- `Trip`
- `TripStop`
- `ExcursionStop`
- solo flights
- itinerary groups
- generated itinerary stops

Rules:

- Derived state is not source of truth.
- Use `CountryStateDerivationService` for derivation.
- Do not reimplement derivation in SQL or ViewModels unless a future optimization explicitly introduces a cache.

## Flexible dates

Atlas supports approximate travel dates:

- year only
- month + year
- full day

Rules:

- Flexible dates are structured data, not only display strings.
- Date ranges must preserve precision.
- If both start and end are present, both should use the same precision.
- Display must preserve the original precision.

## Status rules

Trips and flights use explicit status:

- planned
- in_progress
- completed
- unknown

Rules:

- Status is source of truth.
- Dates may suggest status.
- Manual status should not be unexpectedly overridden.
- Inference should be conservative when dates are incomplete.

## Flight rules

- Flights can exist solo or inside itinerary groups.
- Solo flights count destination country/territory by default when completed.
- Origin counts only when explicitly enabled.
- In-progress flights should not automatically mark destination as visited before arrival.
- Local and UTC time logic should use shared utilities.
- Sorting/duration/delay logic should prefer UTC-derived fields when available.

## Domain model rules

- Keep models expressive but not overengineered.
- Prefer sealed result/error types for operations that can fail.
- Avoid leaking persistence details into model names.
- Keep optional data optional; Atlas must support incomplete records.

## Validation

For domain changes:

- Add or update unit tests where correctness matters.
- Test edge cases for incomplete dates, null fields, unknown statuses, missing airports, and partial API data.
- Prefer deterministic pure functions for domain rules.
