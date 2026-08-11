# Atlas UI Instructions

## Active rework mode — mandatory

The active UI task is a new implementation, not a polish pass. Read and follow
`docs/Atlas_UI_Rework_Implementation_Plan.md` before making UI changes.

New UI work **MUST** be created under `com.atlas.ui.rework`. The legacy UI in
this package remains temporary executable code only and must not be consulted
as a visual, layout, navigation, component, information-hierarchy, or
interaction reference.

Do not edit legacy screens in place to implement the rework. Do not copy them as
starting points. Do not preserve their structure for parity. Reuse underlying
presentation/domain/data behavior, not their UI decisions.

The legacy-specific guidance later in this file applies only to maintenance of
legacy screens when a task explicitly requests it. It does not constrain the
new rework visual system.

## Scope

This package contains Atlas UI code:

- Compose screens
- reusable UI components
- theme tokens
- navigation host
- map/geo visual components
- visual state rendering
- user interaction callbacks

The UI layer must render state and emit events. It must not own domain truth.

## Visual direction

Atlas should feel like a warm printed travel atlas that keeps growing over time.

Core direction:

- Warm Editorial Atlas / Cartographer's Ink.
- Paper and ink, with real contrast.
- Subtle map/graticule texture where appropriate.
- One clear subject per screen.
- Data as decoration: routes, timelines, ranks, duration, distance, aircraft, maps, and facts should carry visual interest.
- Monospace is the instrument layer: codes, labels, coordinates, dates, ranks, and compact metadata.
- Color always has stable meaning.
- Screens should feel personal, visual, calm, and premium without becoming overloaded.

## Current app theme ground truths

This section describes the legacy theme. It is not the rework theme contract.
The rework must define its own semantic foundation from the approved concepts.
Fonts or tokens may be adopted only through an explicit rework decision.

- Use shared Atlas theme tokens.
- Do not recreate local screen palettes.
- Use the existing Atlas colors such as `AtlasBackground`, `AtlasSurface`, `AtlasOutline`, `AtlasOnSurfaceStrong`, and state/accent tokens.
- Current display font in the app is `AtlasSerif` backed by Fraunces.
- `AtlasSans` is Hanken Grotesk.
- `AtlasMono` is Space Mono.
- The design reference uses a warm paper/ink system with vermilion primary, navy structure, gold accent, and stable state colors.

## Compose rules

- Keep composables small and reusable.
- Prefer extracting repeated patterns into components.
- Avoid business logic inside composables.
- Hoist state when parent screens need to control it.
- Keep previews where useful and cheap.
- Do not introduce UI dependencies without explicit justification.
- Preserve behavior during polish unless the task explicitly asks for behavior changes.
- Keep component APIs stable unless changing the API clearly reduces duplication or fixes design inconsistency.
- Avoid excessive nested layouts when a simpler structure works.

## Layout and spacing rules

Check every UI polish change for:

- consistent screen horizontal padding
- consistent section spacing
- consistent card internal padding
- icon/text vertical alignment
- text baseline alignment
- touch target size
- compactness without visual crowding
- long text handling
- missing/null data handling
- empty/loading/error states
- dark mode behavior if applicable
- small screen behavior

Avoid one-off spacing values unless the design intent is clear.

## Component rules

For rework code, first check the new `ui.rework` foundation and approved
external libraries. Legacy components are not candidates for automatic reuse;
adopt one only after explicitly verifying that its semantics and API match the
rework plan.

Common component families:

- cards
- ledgers/stat rows
- field rows
- section headers
- chips
- map heroes
- timeline rows
- route/flight rows
- dropdown/overflow menus

Do not create another visual variant for the same semantic role unless there is a clear reason.

## Specific UI rules

### Country screens

- Country detail pages should feel like the emotional core of the app.
- Country Info should make dense facts browseable, not spreadsheet-like.
- Hide empty country info sections/fields automatically.
- Ranks, tiers, years, and source/context metadata should be visually secondary.
- Use Catalan labels and user-facing text.

### Flight screens

- Prioritize route, time, airline, aircraft, status, duration, distance, and delay.
- Airport-local time is primary unless a UTC mode is selected.
- Actual times are primary when present; scheduled times are secondary.
- Keep route readability higher than decorative density.

### Trip and itinerary screens

- Trip pages should feel like memory pages.
- Make status, dates, route progression, stops, generated itinerary stops, and excursions easy to scan.
- Generated itinerary stops should not look manually editable unless they are editable.

### Dialogs and forms

- Keep forms light and non-intimidating.
- Prefer progressive disclosure for optional details.
- Preserve quick capture.
- Required fields should be visually clear without making optional data feel like an error.
- Date/time fields must clearly distinguish scheduled vs actual and departure vs arrival.

### Overflow menus

Use the shared styled overflow menu wrapper. Do not use raw `DropdownMenu` / `DropdownMenuItem` without the Atlas styling wrapper.

## Map and geo UI rules

- `AtlasGeoCanvas` is the reusable offline vector geo foundation.
- `CountryMapHero` uses `AtlasGeoCanvas`.
- `FlightRouteGeoMap` uses the offline Canvas geo renderer.
- `DashboardMapHero` uses `AtlasGeoCanvas` world view.
- `TripMapPreview` still uses MapLibre.
- Keep provider-specific map code isolated.

## UI polish completion checklist

This checklist applies to legacy maintenance. Rework completion and review are
defined by `docs/Atlas_UI_Rework_Implementation_Plan.md`.

Before finishing a UI task, verify:

- The main subject of the screen/card is clear.
- Secondary data is quieter than primary data.
- Typography usage matches semantic role.
- Monospace is used for labels/data/codes, not long prose.
- Color is meaningful and consistent.
- Empty/loading/error/partial-data states are considered.
- The layout survives long Catalan text.
- The change does not create a new visual language.

## Final response for UI work

Summarize:

- Screens/components changed.
- Visual behavior changed.
- Components reused or introduced.
- Manual screenshot/device review still needed.
- Checks run.
