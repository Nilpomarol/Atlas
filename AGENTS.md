# AGENTS.md

## Project

Atlas is a native Android, local-first personal travel atlas.
It tracks countries/territories, wished/visited/lived/currently-living states, simple trips, and ordered trip stops.
Future versions add flights, itineraries, generated stops, maps, stats, photos, and story mode.
Prefer incremental, maintainable changes over broad unfinished systems.

## Source of Truth

Read the relevant documents before non-trivial work.
General: Atlas_Product_Specification.md, Atlas_Roadmap.md, Atlas_Data_Model.md, Atlas_Technical_Architecture.md.
MVP: Atlas_MVP_Specification.md, Atlas_MVP_Data_Model.md, Atlas_MVP_Roadmap.md.
v2.0: Atlas_v2.0_Specification.md, Atlas_v2.0_Data_Model.md, Atlas_v2.0_Roadmap.md.
Priority order: current user request, AGENTS.md, version-specific docs, general docs, existing code conventions.
If documents conflict, prefer the version-specific document for the active task.

## Current Scope

Assume the current target is MVP unless the user explicitly says otherwise.
MVP includes Android foundation, Compose, Room, Navigation, manual dependency container, and Catalan-first UI.
MVP includes countries/territories, country list/detail, wished, currently living, visit/lived logs, simple trips, ordered stops, location search/manual fallback, derived states, JSON backup/import, and basic dashboard.
MVP excludes flights, airports, itineraries, itinerary groups, generated itinerary stops, excursions, photos, story mode, advanced stats, cloud sync, and multi-language support.
Do not implement v2.0 or later features during MVP work unless explicitly requested.

## v2.0 Scope

v2.0 begins after MVP completion.
v2.0 includes airport dataset/search, manual solo flights, flight list/detail, itineraries, itinerary groups, flights inside groups, layover-safe derivation, linking itinerary to trip, generated trip stops, route visualization, and backup/import v2.
v2.0 excludes flight API import, live flight status, airline logos, aircraft images/specs, photos, story mode, and cloud sync unless requested.

## Tech Stack

Use Kotlin, Jetpack Compose, Room, Coroutines + Flow, Navigation Compose, kotlinx.serialization, and a manual dependency container.
Do not add Hilt, Koin, Retrofit, MapLibre, osmdroid, or other major libraries unless the current milestone requires it or the user asks.
If adding a dependency, keep it narrow and explain why it is needed.

## Architecture

Use a pragmatic layered architecture: UI -> Presentation -> Domain -> Data -> Room/static datasets/external services.
Recommended top-level packages: app, core, data, domain, presentation, ui.
Keep business rules out of Compose screens.
Compose screens should render state and send events.
ViewModels prepare UI state and call use cases/repositories.
Domain owns validation, derivation, and business rules.
Data owns Room, DAOs, entities, repositories, dataset import, backup, and provider implementations.
Do not expose Room entities directly to UI.

## MVP Data Model

MVP core entities: CountryEntity, DatasetMetadataEntity, CountryUserStateEntity, CountryLogEntity, TripEntity, TripStopEntity.
PlaceEntity is optional in MVP.
Do not add AirportEntity, FlightEntity, ItineraryEntity, ItineraryGroupEntity, ExcursionEntity, ExcursionStopEntity, PhotoEntity, CountryStatsSummaryEntity, or CountryStatFactEntity during MVP unless requested.
Use country_iso2 for country/territory references.
Use UUID/string IDs for user-created records.
Keep static reference data separate from user data.
Do not store final country states as source of truth.

## Countries and Territories

Atlas tracks countries and travel-relevant territories/special regions.
CountryEntity must support type, parent_iso2, is_un_member, is_observer_state, and is_trackable.
Valid entity types: SOVEREIGN_STATE, DEPENDENT_TERRITORY, SPECIAL_REGION, DISPUTED_OR_OTHER.
Do not reduce CountryEntity to only name and flag.

## Status Rules

Use statuses: PLANNED, IN_PROGRESS, COMPLETED, UNKNOWN.
MVP trip derivation: PLANNED stops count as planned.
MVP trip derivation: IN_PROGRESS stops count as visited.
MVP trip derivation: COMPLETED stops count as visited.
MVP trip derivation: UNKNOWN stops do not affect country state by default.
Status is the source of truth.
Dates may suggest status later, but must not unexpectedly override manually chosen status.

## Flexible Dates

Support year-only, month-year, and full-date precision.
Use structured models, not display strings.
FlexibleDate has year, nullable month, nullable day, and precision.
FlexibleDateRange has nullable start, nullable end, and nullable precision.
YEAR precision requires month and day to be null.
MONTH precision requires month and year and day to be null.
DAY precision requires month and day.
Date ranges with both start and end must use the same precision.
Keep validation and formatting centralized.

## Country State Derivation

Centralize country/territory derivation in CountryStateDerivationService.
MVP inputs are CountryUserState, CountryLog, Trip, and TripStop.
wished = CountryUserState.wished.
currentlyLiving = CountryUserState.currentlyLiving.
lived = currentlyLiving OR has LIVED log.
visited = currentlyLiving OR has VISIT log OR has LIVED log OR completed/in-progress trip stop.
planned = planned trip stop.
neverVisited = !visited && !lived.
Do not duplicate derivation logic in ViewModels, composables, or ad-hoc SQL.

## UI and Language

The app UI is Catalan-first.
Use Catalan for visible labels, buttons, validation messages, empty states, and screen titles.
Use English for code, class names, function names, variables, comments, and technical documentation inside code.
Use string resources for user-visible text when practical.

## Compose Guidelines

Prefer small composables, state hoisting, immutable UI state, event lambdas, Material 3 components, and previews for important reusable components.
Avoid business logic inside composables, direct DAO access from UI, scattered route strings, hardcoded colors everywhere, and large monolithic screens.
Preferred pattern: Route composable obtains ViewModel and collects state.
Preferred pattern: Screen composable receives state and event callbacks.

## Room and Repositories

Use mappers for DTO -> Entity, Entity -> Domain, and Domain -> Entity when needed.
Repositories should hide DAO details.
ViewModels should depend on use cases or repositories, not DAOs.
Use Flow for observable data.
Use transactions for multi-step writes such as set-currently-living, backup import, delete trip with stops, and v2.0 generated-stop sync.

## Backup and Import

Backup/import is core because Atlas is local-first.
MVP backup uses backupVersion = 1.
MVP backup includes countryDatasetVersion, countryUserStates, countryLogs, trips, tripStops, and places if used.
v2.0 backup uses backupVersion = 2 and adds airportDatasetVersion, flights, itineraries, and itineraryGroups.
Validate before modifying data.
Import inside a database transaction.
MVP import strategy is replace-all after explicit confirmation.

## Testing Priorities

Prioritize domain tests over UI tests.
MVP tests: FlexibleDateValidator, FlexibleDateFormatter, CountryStateDerivationService, SetCurrentlyLivingCountryUseCase, trip stop ordering, and JSON backup/import validation.
v2.0 tests: FlightCountryTrackingService, ItineraryGeneratedStopService, ItineraryGroupDerivationService, MVP-to-v2 migration, and backupVersion 2 import/export.
Important cases: only one currently living country, visit log marks visited, lived log marks lived and visited, planned stop marks planned, completed/in-progress stop marks visited, unknown stop has no effect.
Important v2.0 case: Barcelona -> Doha -> Tokyo counts Japan, not Qatar.

## Commands

Use the Gradle wrapper when available.
macOS/Linux: ./gradlew assembleDebug, ./gradlew test, ./gradlew lint.
Windows PowerShell: .\gradlew.bat assembleDebug, .\gradlew.bat test, .\gradlew.bat lint.
Before finishing, run the most relevant practical command.
Do not claim tests passed unless they were actually run.
If commands fail because the project is not set up, report that clearly.

## Implementation Discipline

Make the smallest coherent change.
Respect the current milestone.
Do not add unused future tables/classes.
Do not implement future features early.
Avoid unrelated refactors and formatting churn.
Follow existing style.
Keep code comments in English.
Update relevant documentation if implementation changes scope, architecture, data model, or roadmap decisions.

## Safety Rules

Do not run destructive commands unless explicitly requested.
Avoid rm -rf, git reset --hard, git clean -fd, deleting migrations, deleting user data, or rewriting large areas without approval.
Do not commit secrets, API keys, signing configs, keystores, or local machine paths.
Do not add backend/cloud dependencies unless explicitly requested.

## Reporting Work

When summarizing work, include what changed, files changed, tests or commands run, anything not completed, and follow-up needed.
Default assumptions: current target is MVP, UI is Catalan, code is English, data is local-first, no backend, no account system, no flights until v2.0, and no overengineering.
