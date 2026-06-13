# Atlas Android Module Instructions

## Scope

This package contains the Android application implementation for Atlas.

Follow the root `AGENTS.md` first, then these Android-specific rules.

## Stack

- Kotlin
- Jetpack Compose
- Room
- Navigation Compose
- Coroutines + Flow
- kotlinx.serialization
- Manual dependency injection with `AtlasAppContainer`
- MapLibre where map tiles/geographic SDK rendering is required
- Coil for async image loading

## Package boundaries

Expected package responsibilities:

```text
app/             application setup and manual DI
core/            shared primitives, result/error/date utilities
ui/              Compose UI, reusable components, theme, navigation
presentation/    ViewModels, UI state, screen event handling
domain/          domain models, use cases, validation, derivation logic
data/            Room, DAOs, entities, mappers, datasets, API clients, repository impls
```

Do not collapse these layers for convenience.

## Android implementation rules

- Keep ViewModels free of Android UI details except normal lifecycle/state patterns.
- Prefer immutable UI state models.
- Prefer Flow-based observable data from repositories and Room.
- Avoid long-running work on the main thread.
- Keep API clients replaceable behind domain/data interfaces.
- Keep DataStore/API key handling out of UI composables.
- Do not add new Gradle dependencies without explaining the reason.
- Do not introduce Hilt, Koin, Retrofit, or another image-loading library without explicit approval.

## UI routing

- For UI-specific work, follow `ui/AGENTS.md`.
- For data/API/Room/importer work, follow `data/AGENTS.md`.
- For business rules and domain models, follow `domain/AGENTS.md`.
- For ViewModel and UI state work, follow `presentation/AGENTS.md` if present.

## Validation

For Android code changes:

- Prefer `testDebugUnitTest` when changing domain, data mapping, ViewModels, or utility logic.
- Prefer `assembleDebug` when changing UI, resources, Gradle config, database entities, migrations, or assets.
- If a change affects both logic and UI, run both when practical.
