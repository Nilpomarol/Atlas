# Atlas Presentation Layer Instructions

## Scope

This package contains:

- ViewModels
- UI state models
- screen event handlers
- mapping from domain models to display-ready UI state
- loading/error/empty state orchestration

## Responsibilities

Presentation may:

- call use cases or repositories
- combine flows
- expose immutable UI state
- handle one-off UI events
- format display values when the formatting is UI-specific
- resolve display names/logos/images through appropriate use cases or repositories

Presentation must not:

- own core business rules
- duplicate country state derivation
- duplicate flexible date validation
- directly expose Room entities or API DTOs
- contain Compose UI code
- hide important failure states from the UI

## UI state rules

Every non-trivial screen state should account for:

- loading
- empty
- error
- partial data
- normal content
- edit/delete/import confirmation flows where relevant

For UI polish tasks, make sure ViewModel changes do not turn visual polish into hidden behavior changes.

## Catalan display

- User-facing strings exposed through UI state should be Catalan unless they are raw user-provided values, airport/airline official names, codes, or technical integration labels.
- Prefer domain/core formatting utilities for dates, statuses, and known domain labels.

## Validation

For ViewModel changes:

- Test flow transformations and event handling where practical.
- Run unit tests if available.
- Avoid broad rewrites just to support a visual polish change.
