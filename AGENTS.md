# Atlas — Root Agent Instructions

## Purpose

This repository contains Atlas, a native Android local-first personal travel atlas.

Atlas tracks countries and territories, trips, stops, flights, itineraries, excursions, country information, maps, travel statistics, notes, and future photo-based memories.

The app is a personal travel atlas, not a productivity tool, social network, booking app, or generic notes app.

## Source-of-truth documents

Use these documents before making product, architecture, data, or UI decisions:

- `docs/README.md` — documentation map, current release facts, and historical/current doc status.
- `docs/Handoff_Prompt.md` — development handoff snapshot with useful recent decisions; not the long-term canonical doc when it conflicts with active docs or code.
- `docs/Atlas_Product_Specification.md` — long-term product vision and domain behavior.
- `docs/Atlas_Technical_Architecture.md` — architecture principles and layer responsibilities.
- `docs/Atlas_Data_Model.md` — long-term conceptual data model.
- `docs/Atlas_v4.0_Country_Stats_Spec.md` — v4.0 country info/stat facts contract.
- `docs/Atlas - Design System.html` — legacy visual reference only; do not use
  for the UI rework.
- `docs/Atlas_UI_Rework_Implementation_Plan.md` — mandatory UI rework
  implementation directive.
- `docs/Atlas_UI_Rework_Foundation.md` — approved rework product and visual
  foundation.

## Active UI rework directive

Atlas is currently implementing a completely new UI in parallel with the
legacy UI. For any UI, navigation, Compose screen, UI component, theme, map
presentation, or UI-facing presentation task, follow
`docs/Atlas_UI_Rework_Implementation_Plan.md` before all legacy UI guidance.

The existing UI is **not** a design or implementation reference for the
rework. Do not edit legacy pages in place, copy their layouts, preserve their
navigation for parity, or reuse their visual components by default. Build the
new implementation under `com.atlas.ui.rework` and connect it to existing
presentation/domain/data behavior.

Current code remains authoritative for non-UI behavior: Room, migrations,
backup/import, datasets, repositories, domain rules, derivations, and stable
identifiers. When the general source precedence below says current code wins,
that does not make legacy UI code authoritative for rework UI decisions.

When these documents conflict, prefer the current implementation and active release docs:

1. Current code, exported Room schemas, bundled assets, and backup implementation
2. `docs/README.md`
3. Technical architecture, data model, and backup docs
4. Product specification
5. Version-specific implementation specs
6. Development handoff and older roadmap/history documents

If a conflict affects implementation, report it before changing code.

## Product principles

Atlas should prioritize:

- personal meaning over public sharing
- flexibility over strict completeness
- visual clarity over dense data entry
- local ownership over cloud dependency
- structured travel memory over generic planning

Do not make the app stricter than necessary. The user should be able to create useful records with incomplete data.

## Technical constraints

- Kotlin only for Android production code.
- Native Android first.
- Jetpack Compose for UI.
- Room for local persistence.
- Navigation Compose for navigation.
- Coroutines and Flow for async/reactive state.
- `kotlinx.serialization` for JSON datasets and backups.
- Manual dependency injection through `AtlasAppContainer`.
- Local-first. No backend requirement.
- No Hilt, Koin, Retrofit, or osmdroid unless explicitly approved.
- MapLibre is already used where appropriate; do not remove it without a specific reason.
- Coil is the app image-loading library; do not add another image loading library.
- Do not introduce major dependencies without explaining the value and tradeoff.
- Do not commit API keys, secrets, tokens, or local machine paths.

## Language rules

- Visible app UI is Catalan-first.
- Code, class names, function names, comments, placeholder identifiers, and technical documentation should be in English.
- Do not expose raw API names or technical provider details to the user unless the screen is explicitly a settings/debug/integration screen.

## Architecture boundaries

Respect the layered architecture:

```text
UI → Presentation → Domain → Data → Room / Static Datasets / External Services
```

Rules:

- UI renders state and emits user events.
- ViewModels coordinate state and call use cases/repositories.
- Domain owns business rules and validation.
- Data owns Room, DTOs, mappers, importers, API clients, and repositories.
- Room entities and external DTOs must not leak into UI.
- Business rules must not be duplicated in Compose screens.
- Keep country state derivation centralized in `CountryStateDerivationService`.
- Keep flexible date logic centralized in domain/core date utilities.
- Keep map-provider-specific details isolated behind reusable components/wrappers.

## Data rules

- Separate static/reference data from user-created data.
- Static datasets can be updated without overwriting user data.
- Use stable external identifiers where possible:
  - countries/territories: ISO2 when available
  - airports: IATA when available
  - airlines: IATA when available
  - aircraft types: ICAO/common code when available
- User data backup/import must remain safe and stable across dataset updates.
- Derived states are not source of truth unless a future cache is deliberately introduced.
- Build only the schema required for the current version.

## Current release priorities

Atlas has completed v5 Photos and Memories, with optional photo metadata deliberately deferred.
The app is released, so preserve existing user data, migrations, and backup/import compatibility.

For UI work, the active UI rework supersedes the older polish and roadmap
priorities below. For non-UI work, continue to prioritize:

- Small, reviewable changes.
- Next planned tasks: country stats scope preference and quick trip creation/compact
  cards, as documented in `docs/Atlas_Post_v2.0_Roadmap.md`.
- Current country, trip, flight, itinerary, photo, backup, and stats behavior.
- Consistency with the active UI rework documents and approved image concepts.
- Preserving current behavior unless explicitly asked to change it.
- Additive or migration-safe feature work.

## Validation

When changing Kotlin code, run the smallest relevant check available. On Windows, the known commands are:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat testDebugUnitTest
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat assembleDebug
```

If checks cannot be run, state that clearly and explain why.

## Change discipline

- Make the smallest defensible change.
- Avoid broad rewrites during ordinary polish. The approved UI rework is a
  deliberate replacement and follows its own phased plan.
- Do not change public component APIs unless needed.
- Do not rename files, packages, models, or database fields for style-only reasons.
- Keep migrations explicit and safe.
- Preserve existing data compatibility.
- Avoid speculative future-version abstractions.

## Final response format

After each task, summarize:

- Files changed.
- Behavior changed.
- Visual changes, if any.
- Checks run.
- Risks or manual review needed.
