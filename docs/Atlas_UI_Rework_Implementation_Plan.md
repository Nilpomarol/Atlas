# Atlas UI Rework Implementation Plan

Status: **Active and mandatory**
Effective: 2026-08-11

## 1. Directive

Atlas is implementing a new UI as a separate presentation implementation.

All agents and contributors **MUST** build the reworked UI independently from
the legacy screens. The current UI must not be used as a visual, layout,
navigation, component, information-hierarchy, or interaction reference.

This is not a gradual reskin of the existing pages. It is a new UI system and
new app shell connected to the existing Atlas product, domain, and data layers.

The legacy UI may remain temporarily compilable while replacement screens are
built. It is not an acceptance target and visual parity with it is not required.

Normative words such as **MUST**, **MUST NOT**, **SHOULD**, and **SHOULD NOT**
are requirements for Codex, Claude, and human contributors.

## 2. Source-of-truth precedence for UI work

For rework UI decisions, use this order:

1. This implementation plan.
2. `docs/Atlas_UI_Rework_Foundation.md`.
3. Approved image concepts under `docs/ui-rework/`.
4. Product and domain specifications.
5. Existing presentation, domain, data, backup, and dataset behavior.

Legacy Compose screens, legacy navigation, legacy UI components, legacy screen
previews, and the old design-system HTML are **not UI design sources** for the
rework. Do not inspect them to decide what the new UI should look like or how it
should be structured.

Existing non-UI code remains authoritative for persisted data, backup/import,
domain rules, derivations, repository behavior, and compatibility contracts.

If an approved concept conflicts with existing backend capabilities, report the
gap and introduce the smallest presentation/domain adaptation needed. Do not
silently redesign the concept to resemble the legacy screen.

## 3. Required architecture

The new UI begins under a separate package boundary:

```text
com.atlas.ui.rework/
  app/              new app shell and top-level scaffold
  navigation/       new destinations and navigation host
  foundation/       tokens, typography, shape, elevation, motion
  components/       reusable rework components
  map/              shared map canvas, layers, controls, overlays
  capture/          global context-aware capture surface
  screens/          replacement screens by feature
```

Use `presentation/rework/` only for genuinely new UI-state coordinators or
adapters. Reuse existing ViewModels and presentation state when their contracts
fit the new UI without leaking legacy screen assumptions.

The first entry points SHOULD be named clearly, for example
`AtlasReworkApp` and `AtlasReworkNavHost`. Do not replace the contents of the
legacy navigation host with the new graph.

The new UI continues to respect:

```text
UI -> Presentation -> Domain -> Data -> Room / datasets / optional services
```

Do not create a parallel database, repository stack, backup format, or domain
model merely for the rework.

## 4. Legacy UI isolation rules

Agents **MUST NOT**:

- Implement the rework by editing legacy screens in place.
- Copy a legacy screen and treat it as the starting layout.
- Preserve legacy navigation or information architecture for parity.
- Reuse a legacy visual component merely because it already exists.
- Use legacy screenshots or previews as a comparison target.
- Add new rework behavior to both UI implementations.
- spend time polishing legacy pages during the rework.

An existing UI component may be adopted only after an explicit review shows
that its semantics and API fit the approved rework. In that case, move or adapt
the capability into the rework foundation; do not let the new UI depend on a
legacy screen package accidentally.

Changes to legacy UI are limited to:

- A temporary development switch or entry-point bridge.
- Build fixes caused by shared non-UI changes.
- Removing a legacy screen after its replacement is accepted.

Parallel UI implementations are temporary migration infrastructure. The target
state is one reworked UI with the replaced legacy UI deleted.

## 5. Approved Home composition

The current Home structural reference is:

- `docs/ui-rework/atlas-home-map-canvas-v2.png`

The implementation contract is:

- One edge-to-edge interactive map is the persistent screen canvas.
- Home uses the Compose-based `AtlasWorldMap` overview renderer rather than a
  bounded MapLibre viewport. This permits a fit-width world at the initial
  camera while retaining a full-screen pan and zoom surface.
- Antarctica is omitted from the Home overview geometry.
- Information appears as a disciplined vertical stack of independent floating
  cards above the map.
- Visible map space separates the cards.
- Home does not use a bottom sheet or a continuous lower content background.
- Tracking is prominent on the canvas and in the card hierarchy.
- The global capture action is separate from navigation.
- Primary navigation contains exactly `Inici`, `Països`, `Viatges`, `Vols`,
  and `Progrés` at this stage.
- Idle, active-trip, and active-flight modes change map layers and card content
  without abandoning the shared composition.

Generated imagery establishes composition and visual intent, not literal text,
geography, measurements, or business truth. Use real Atlas data contracts.

## 6. Implementation sequence

### Phase 1 - Rework foundation

- Create the separate package and new app/navigation entry points.
- Define new semantic tokens from the approved visual direction.
- Build the edge-to-edge map scaffold.
- Build and validate the fit-width `AtlasWorldMap` renderer independently from
  Home composition before adding cards.
- Build reusable floating-card, navigation, map-control, and global-capture
  primitives.
- Define compact-screen, system-inset, accessibility, and offline behavior.

### Phase 2 - Home idle vertical slice

- Render the approved Home composition with real Atlas data.
- Maintain debug-only Home showcase scenarios for empty, established,
  active-trip, active-flight, selected-country, and achievement states. These
  scenarios replace presentation state in memory only and must never seed Room
  or affect backup/import behavior. The release catalog remains empty.
- Support empty, populated, loading, partial-data, and offline states.
- Preserve map pan/zoom/select behavior behind overlays.
- Validate bottom navigation and global capture interaction.

### Phase 3 - Context-aware Home

- Add active-trip mode.
- Add active-flight mode.
- Resolve overlapping active records explicitly in presentation logic.
- Keep the map canvas and floating-card composition stable across modes.

### Accepted milestone - Home foundation (2026-08-11)

Phases 1 through 3 are implemented and accepted as the foundation for the
remaining rework. Agents must treat the current code under
`com.atlas.ui.rework` as the authoritative implementation, not recreate Home
from the earlier concept images or legacy screens.

The accepted Home foundation includes:

- The separate rework app shell, navigation graph, theme, shared floating-card
  primitive, wordmark, map controls, and centralized capture surface.
- A full-screen interactive `AtlasWorldMap` that fits the non-Antarctic world
  width, supports pan, zoom, country selection, sea-tap deselection, and an
  animated reset action.
- A scrollable floating-card region whose resting boundary follows the bottom
  of the fitted world map and does not resize the map canvas.
- A stable baseline of map/tracking summary, next-trip state, recent record
  when available, and travel archive.
- Additive context cards above that baseline for active flights, active trips,
  achievements, and selected countries. Context must not replace or reorder
  the stable baseline.
- A merged editorial tracking grid whose colored figures report living,
  lived, visited, planned, wished, and unrecorded territory counts.
- Editorial map washes, stronger country outlines, subtle sea grain, and a
  clipped engraved texture for registered countries.
- A compact rectangular `Registra` action above the bottom-right navigation.
- Debug-only scenarios for empty, established, active-trip, active-flight,
  selected-country, and achievement review; release builds expose none.

Home is now frozen as an accepted milestone. Do not redesign or broadly polish
it while implementing another destination. Change it only for an explicit user
request, a verified regression, or a shared-foundation adjustment required by
an approved replacement screen.

The next implementation task is Phase 4 - Countries. Begin it with a read-only
analysis and propose the Countries list, tracking states, country-detail
hierarchy, and contextual capture behavior before writing the screen. Reuse the
accepted rework theme, map, cards, navigation, and capture foundations. Do not
use legacy Countries screens as layout references.

### Phase 4 - Countries

- Build the new Countries list and country detail without using the old pages as
  layout references.
- Reuse the new map, tracking, card, capture, and navigation foundations.

### Phase 5 - Remaining destinations

- Replace Trips, Flights, Progress, capture flows, and secondary screens in
  reviewable vertical slices.
- Reopen Trip Detail only after its user-facing model and information hierarchy
  are deliberately specified.

### Phase 6 - Cutover and removal

- Make the rework graph the only production entry point.
- Verify backup/import and essential user journeys.
- Delete replaced legacy screens, components, previews, and navigation code.
- Remove the temporary UI switch or bridge.

## 7. Change and review discipline

Each implementation task MUST state that it belongs to the UI rework and name
the phase it advances.

Each pull request or agent handoff MUST report:

- New rework files created.
- Existing non-UI contracts reused.
- Any presentation/domain adapters introduced and why.
- Legacy UI files touched, with explicit justification.
- Visual/device states checked.
- Build/tests run.
- Remaining migration or deletion work.

A task is not complete if it only approximates the concept with stock Material
components or introduces one-off styling that bypasses the new foundation.

## 8. Compatibility boundary

The UI may break during development, and test-device app data may be cleared.
However, the rework **MUST NOT** break:

- Room migration history for released users.
- Backup/export/import compatibility.
- Stable identifiers and serialized formats.
- Local-first access to core user data.
- Centralized country-state derivation and flexible-date rules.

Data/schema changes require their own explicit product and migration decision;
they are not implied by a UI redesign.
