# Atlas UI Rework Foundation

Status: **Approved foundation; implementation active**
Started: 2026-08-11

## Visual approval artifacts

High-fidelity image concepts are the visual source of truth during the
exploration phase. They establish atmosphere, hierarchy, composition, imagery,
and the relationship between maps and floating editorial surfaces before those
decisions are constrained by implementation.

Current concept:

- `docs/ui-rework/atlas-foundation-concept-v1.png` — Home idle, Home with an
  active trip, and Countries tracking.
- `docs/ui-rework/atlas-home-map-canvas-v2.png` — current Home structural
  direction: one edge-to-edge interactive map canvas with a disciplined stack
  of independent floating information cards. This supersedes bottom-sheet and
  split map/feed Home explorations.

Figma is optional supporting documentation. Its current structural scaffold is
not an approved visual specification and must not be used as a pixel reference.
Once a concept direction is approved, its reusable rules are extracted into
tokens, component contracts, responsive layouts, and Compose implementation.

## Purpose

This document defines the first product and visual foundation for the Atlas UI
rework. It is intentionally limited to the app shell, Home, Countries, and the
shared capture entry point. Production implementation is authorized and must
follow `docs/Atlas_UI_Rework_Implementation_Plan.md`.

The rework is a new UI implementation under a separate package and navigation
entry point. The current UI must not be used as a design, layout, navigation,
component, information-hierarchy, or interaction reference. Existing domain,
data, backup/import, and compatible presentation behavior remain reusable.

## Product thesis

Atlas is a personal travel record whose primary purpose is to show where the
user has been, where they are going, and the journeys and memories that explain
that history.

The rework should make tracking immediately visible, make the map a primary
interaction surface, and let users record travel without first understanding
Atlas's internal entities.

## Locked v1 foundation scope

The first concept pass covers:

- The primary app shell and five destinations.
- A persistent context-aware capture action.
- Home in idle, active-trip, and active-flight states.
- Countries list and tracking-first country detail.
- Purpose-specific map modes built on one shared map foundation.
- The initial modern editorial visual language.
- Offline and optional-network behavior visible in these surfaces.

The first concept pass does not redesign:

- Trip detail, stops, excursions, or the photo ownership model.
- Flight or itinerary detail.
- The complete Stats/Progress experience.
- Backup/import behavior or persisted data structures.
- The country-image acquisition pipeline.

## Primary navigation

The working baseline retains direct access to the app's main collections:

1. `Inici`
2. `Països`
3. `Viatges`
4. `Vols`
5. `Progrés`

Settings is reached from the account/app control in the top chrome. Itineraries
remain part of Flights. Timeline/Journal remains a cross-domain view reachable
from Home or Progress rather than replacing the direct collection destinations.

The capture action is not a sixth destination. It is an elevated action that
can appear above the bottom navigation or in equivalent context-appropriate
chrome.

## Unified capture model

The user-facing creation vocabulary is:

- `Registra una visita`
- `Crea un viatge`
- `Afegeix un vol`
- `Registra que hi has viscut`
- `Afegeix records`

The entry surface receives a `CaptureContext` conceptually containing relevant
values such as country ISO2, trip ID, stop ID, itinerary ID, selected map
coordinate, suggested date, and source screen.

Examples:

- Opened from a country: that country is preselected.
- Opened from an active trip: the trip and current date are suggested.
- Opened from a selected map point: the coordinate and resolved country are
  preselected.
- Opened globally: no assumptions beyond safe, reversible suggestions.

The UI presents one understandable travel-recording system. Existing country
logs, trips, stops, flights, and other entities may remain distinct internally.

## Home context modes

### Idle

- Interactive tracking map is the dominant surface.
- Tracking summary floats over or immediately overlaps the map.
- The next planned journey and recent meaningful record are secondary.
- The capture action remains prominent.

### Active trip

- The active journey becomes the main editorial subject.
- The map focuses on the journey route rather than every global layer.
- Progress, current/next place, and quick capture are prioritized.
- Global tracking remains reachable but visually secondary.

### Active flight

- The current flight enriches an active journey when they are related.
- A solo active flight becomes the main current-travel subject when no trip is
  active.
- Route, origin, destination, timing, status, and destination tracking evidence
  are prioritized.

The presentation layer must resolve overlaps explicitly instead of selecting
the first in-progress record without explanation.

## Map model

Atlas may have multiple maps, but they must share one interaction, styling, and
layer foundation.

| Surface | Default map content |
| --- | --- |
| Home idle | Country tracking states |
| Home active trip | Active route, relevant places, current/next context |
| Home active flight | Active flight route and endpoints |
| Countries | Tracking distribution and country selection |
| Country detail | Selected country and related recorded places |
| Trip detail, later | Only that journey's route, places, and memories |
| Flight detail, later | Only that flight route |

Every mode defines which layers are visible by default. The user should never
receive every country, trip, flight, and memory layer simultaneously.

### Renderer strategy

The global Atlas overview and detailed geographic maps have different camera
requirements and therefore do not have to use the same renderer:

- Home and Countries use the native Compose `AtlasWorldMap`. It draws the
  bundled country geometry on a full-screen sea canvas, fits the complete
  non-Antarctic world width on compact screens, and owns world-level pan, zoom,
  selection, tracking colour, route, and marker layers.
- Detailed country, trip, stop, and route maps may continue to use MapLibre
  behind the shared map boundary when a conventional geographic viewport is
  appropriate.

Screens consume shared map modes, camera state, layer models, styling tokens,
and selection events. Renderer-specific behavior must remain inside the map
package. The Home canvas must never be resized by its cards or navigation;
those elements are overlays above the full-screen gesture surface.

The shared interaction language should eventually include pan, zoom, select,
focus, filter/layer selection, a compact selection sheet, and contextual
capture. Offline fallback must preserve country geometry, tracking color, and
user-created routes even when detailed basemap tiles are unavailable.

## Countries

### Country list

- Remains a primary destination.
- Leads with overall tracking progress and search/filter controls.
- Rows make personal state more prominent than reference statistics.
- Sorting and detailed metrics remain available without dominating the default.

### Country detail

- Leads with the user's relationship to the country.
- Explains tracking evidence, for example a visit derived from a journey or
  completed flight.
- Offers country-context capture without asking the user to understand the
  distinction between a country log and trip stop.
- Uses photography when available and a deliberate map/editorial fallback when
  it is not.

## Visual direction

Working description: **Modern editorial travel atlas**.

The references establish the following direction:

- Edge-to-edge cartography or photography as a screen canvas.
- Warm opaque panels floating above or overlapping the visual canvas.
- High-contrast editorial display type paired with clean sans-serif UI text.
- Restrained vermilion/orange interaction accents.
- Deep blue/teal map structure and tracking colors with stable meaning.
- Fine borders, controlled shadows, and generous but efficient spacing.
- Compact instrument-like metadata without turning screens into dashboards.
- Custom visible styling over accessible platform behavior.
- Subtle texture only; avoid vintage parchment or decorative overload.

The approved composition uses immersive visual canvases with a disciplined
stack of independent, purposeful floating cards. It is specified directly by
the approved concepts, not by comparison with legacy screens.

## Initial semantic token families

Exact values remain provisional until visual prototype review.

- Canvas: map, photography, fallback artwork.
- Surface: primary ivory, raised ivory, selected/active surface.
- Ink: strong, normal, muted, inverse.
- Accent: primary action, active travel, focus.
- Tracking: visited, lived, currently living, planned, wished.
- Structure: border, divider, shadow, scrim.
- Type: display, title, body, label, data/code.
- Space: 4-point scale with named screen, section, panel, and control spacing.
- Shape: control, panel, floating panel, full/pill.
- Motion: selection, map focus, panel reveal, context transition.

## External dependency boundary

Core recording, tracking, manual location entry, and personal-data access must
work without credentials and without a remote service.

Remote imagery, geocoding, flight lookup, currency rates, detailed map tiles,
and airline marks are enhancements or integrations. The initial prototype may
represent imagery, but it must also show a designed no-image/offline state.

Country imagery can later move to a build-time acquisition pipeline that stores
optimized local assets plus source, author, and licence metadata. Runtime URLs
alone are not considered a durable replacement.

## Prototype review matrix

The concept must be reviewed with:

- New/empty user.
- Established user with many visited countries.
- Idle Home.
- Active trip.
- Active solo flight.
- Active flight associated with an active trip.
- Country visited only through a manual record.
- Country state derived from a trip or flight.
- Missing image and no network.
- Missing coordinates.
- Long Catalan labels on a compact Android screen.

## Implementation gate

The new app shell and Home vertical slice were implemented and accepted on
2026-08-11. The current `com.atlas.ui.rework` Home implementation is the
approved foundation for subsequent destinations. Each replacement screen still
requires its own approved hierarchy and states. The rework must establish:

- A convincing primary hierarchy for Home and Countries.
- An acceptable placement and behavior for the capture action.
- Clear map defaults for idle and active travel.
- Tracking prominence and understandable evidence.
- A cohesive modern editorial language that does not resemble stock Android.
- A credible no-image/offline state.

The next implementation milestone is Countries. It must reuse the accepted
rework foundation and begin with a read-only hierarchy/state analysis. Trip
detail remains on the existing implementation until its product model is
deliberately reopened.
