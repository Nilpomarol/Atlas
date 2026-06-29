# Atlas Technical Architecture

## Purpose

This document defines the architecture currently implemented by Atlas. For current
release facts and documentation status, read `docs/README.md` first.

## Platform and Stack

- Native Android application.
- Kotlin.
- Jetpack Compose and Material 3.
- Navigation Compose.
- Room with exported schemas and explicit migrations.
- Coroutines and Flow.
- `kotlinx.serialization`.
- Manual dependency injection through `AtlasAppContainer`.
- DataStore Preferences.
- WorkManager for constrained periodic backup work.
- MapLibre for the remaining tile-backed map surface.
- Compose Canvas geo components for offline world, country, and route visuals.
- Coil for raster and SVG image loading.
- Telephoto over Coil for full-screen photo zoom/pan and large-image rendering.

Atlas does not use Hilt, Koin, Retrofit, osmdroid, or a backend.

Atlas is released. Architecture changes must preserve existing user data, explicit
Room migrations, backup/import compatibility, and stable dataset identifiers.

## Layered Architecture

```text
UI -> Presentation -> Domain -> Data -> Room / Static Datasets / External Services
```

### UI

Contains Compose screens, reusable components, theme, navigation, map/geo rendering, and interaction callbacks.

UI may render display state and hold ephemeral visual state. It must not own persistence, API calls, validation, country derivation, flexible-date rules, or travel status rules.

### Presentation

Contains ViewModels, immutable UI state, event handling, Flow combination, and domain-to-display mapping.

Presentation may call use cases and repository interfaces. It must expose loading, empty, error, partial, and normal states where relevant. It must not expose Room entities or external DTOs.

### Domain

Contains domain models, repository interfaces, use cases, validators, derivation services, and shared travel rules.

Important centralized logic:

- `CountryStateDerivationService`
- flexible-date validation and formatting
- flight status inference
- UTC-first flight calculations
- itinerary-generated stop derivation
- travel status refresh policy

Domain does not depend on Compose, Room entities, API DTOs, or provider SDKs.

### Data

Contains Room database code, DAOs, entities, migrations, bundled-dataset importers, backup implementation, API clients, DataStore sources, mappers, and repository implementations.

Data maps storage and external representations into domain models before returning them upward.

## Package Responsibilities

```text
com.atlas.app             application setup and AtlasAppContainer
com.atlas.core            shared constants and primitives
com.atlas.ui              Compose UI, theme, navigation, map components
com.atlas.presentation    ViewModels and UI state
com.atlas.domain          models, interfaces, use cases, rules
com.atlas.data            Room, datasets, APIs, preferences, repositories
```

Nested `AGENTS.md` files provide the enforceable rules for each package.

## Dependency Construction

`AtlasAppContainer` constructs:

- Room database and DAOs;
- data sources and API clients;
- repository implementations;
- use cases and services;
- dataset importers.

Manual DI is the current project rule. New framework-based DI requires explicit approval and a demonstrated need.

## Reactive Data Flow

Read flow:

```text
Room/API cache -> Repository Flow -> ViewModel combine/map -> immutable UI state -> Compose
```

Write flow:

```text
UI event -> ViewModel -> Use case/repository -> Room/file/DataStore update -> Flow refresh
```

Long-running imports, file operations, and network work run off the main thread.

## Persistence

Room database version is 24. Every schema change requires:

- an explicit migration;
- registration in `AtlasAppContainer`;
- an exported Room schema;
- preservation of existing user data;
- backup/import review when persisted user data changes.

Static/reference tables and user-created tables are separate. Dataset refreshes may replace versioned static rows but must not overwrite personal travel records.

## Datasets

Bundled JSON datasets are parsed with `kotlinx.serialization` and imported through dedicated importers. `dataset_metadata` records installed versions.

Current datasets include countries, airports, airlines, aircraft types, and country stats. Import behavior must remain deterministic and idempotent for an unchanged version.

## External Services

External integrations are optional enhancements:

- AeroDataBox flight and aircraft lookup;
- Nominatim location search;
- Unsplash country photos;
- OpenFreeMap tiles through MapLibre;
- remote airline logos and country flags through Coil.

Provider-specific DTOs and failures stay in the data layer. Missing keys, network errors, malformed data, empty results, and rate limits must degrade gracefully without blocking local personal data.

## Map Architecture

- `AtlasGeoCanvas` is the reusable offline vector renderer.
- `FlightRouteGeoMap`, `CountryMapHero`, and `DashboardMapHero` build on offline geo data.
- `AtlasMapView` isolates MapLibre lifecycle/provider details.
- `TripMapPreview` is the current MapLibre consumer.

Screens should depend on reusable map components, not directly on provider APIs.

## Image Architecture

Coil is the only image-loading library.

- Bundled/local images and app-private photo files load through Coil.
- SVG flags use Coil's SVG decoder.
- Telephoto does not load images independently; it provides zoom/pan and
  sub-sampling around Coil-backed full-screen photos.
- Remote content always has a local visual fallback.
- Country photo refresh preserves the old cached file on failure.

## Backup and Portability

Backup payloads are versioned and use `kotlinx.serialization`. Format v3 is stored
inside a `.atlasbackup` ZIP with referenced user photos. Import validates supported
versions, supplies defaults for older compatible data, and still accepts legacy v1/v2
JSON files.

Replaceable external photo and currency caches remain outside backups.

Android Auto Backup is disabled. Personal data moves only through Atlas's explicit
`.atlasbackup` export/import and opt-in cloud backup flow, avoiding partial platform
restores that can separate Room metadata from photo files.

Optional cloud backup uses the Storage Access Framework rather than a provider SDK.
The user grants persistent access to a selected document-provider folder, and
WorkManager writes the normal `.atlasbackup` archive every 30 days on an unmetered
network. The worker runs as foreground data sync for large photo archives and retains
the three newest automatic backups. No Atlas server, Google OAuth flow, or cloud sync
model is introduced.

## Released Compatibility Rules

- Do not rewrite or remove historical migrations.
- Do not break imports from legacy v1/v2 JSON backups or v3 `.atlasbackup` files.
- Do not change persisted field meanings without a migration and compatibility plan.
- Do not change stable identifiers for bundled datasets unless existing user data can
  still reconnect safely.
- Prefer additive schema changes and derived presentation projections.

## Planned Feature Architecture Notes

### Country Stats Scope

The planned stats-scope preference should be implemented as a stats filter over the
existing country dataset and derived country states.

- Model the scope as a domain-level enum or value object.
- Keep exact scope membership explicit and covered by tests.
- Store the selected scope as user preference state, likely DataStore, unless a later
  product decision requires backup portability for this setting.
- Apply the scope in stats use cases/ViewModels, not in Room DAOs that feed the
  general country list.
- Do not change `CountryStateDerivationService`; it should continue deriving states
  across all Atlas countries and territories.
- No Room migration or backup-format change is expected.

### Quick Trip Creation

The planned quick-trip flow should create ordinary records through a narrower capture
path.

- Add a use case/repository transaction for creating a trip and its first stop
  together.
- Reuse the existing `Trip` and `TripStop` models, flexible-date utilities, location
  fields, and country identifiers.
- Derive compact-card eligibility in presentation from the trip structure, initially
  `exactly one stop`.
- Do not add a persisted trip type for the first implementation.
- If a future explicit display override is needed, treat it as released data: add a
  migration, exported schema, backup compatibility review, and tests.

## Validation Strategy

- Domain, mapping, ViewModel, and utility changes: focused unit tests.
- Room entities, migrations, UI, resources, or wiring: `assembleDebug`.
- Cross-layer changes: unit tests plus build where practical.
- Visual changes: manual device or screenshot review after a successful build.

