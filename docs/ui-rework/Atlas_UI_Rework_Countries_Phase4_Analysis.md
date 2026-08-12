# Atlas UI Rework — Countries Phase 4 Analysis

Status: proposed, before implementation  
Date: 2026-08-11

This is the required read-only analysis for Phase 4 of the active UI rework.
It describes the proposed Countries hierarchy and state behavior. It does not
use legacy Country screens as a layout or interaction reference.

## Reused product contracts

The existing presentation and domain contracts already provide the required
behavior:

- `CountryListViewModel` provides the full trackable country collection,
  accent-insensitive search, state filters, optional reference-stat ordering,
  and a `CountryTrackingState` derived by `CountryStateDerivationService`.
- `CountryDetailViewModel` provides the selected country, all derived tracking
  state, manual logs, trip and excursion summaries, flight/itinerary evidence,
  optional cached landscape image, country facts, and country memories.
- `AtlasWorldMap` provides the accepted rework map language: offline bundled
  geometry, tracking colours, pan/zoom, country selection, and a deliberate
  no-network baseline.
- `CaptureContext(countryIso2)` already supports country-aware capture
  suggestions. The capture surface should expose that context as the country
  name rather than a raw ISO code once this destination is connected.

Consequently, Countries needs new rework composition and small presentation
adapters only. It does **not** need a Room migration, a backup change, a new
country-state derivation path, or a parallel repository.

## Proposed Countries list hierarchy

1. **Tracking canvas.** The destination opens on the shared edge-to-edge world
   map, coloured by personal tracking state. A compact header identifies
   `PAÏSOS` and places the tracked/total count above the map.
2. **Progress card.** An opaque floating card overlaps the lower map edge and
   gives the primary relationship count (visited, with lived and current-living
   visible in the state legend). It is the entry point to a state filter, not a
   statistics dashboard.
3. **Search and state filters.** A persistent search field is followed by a
   horizontally scrollable state filter row: all, visited, planned, wished, and
   lived. Filter counts make the current scope understandable. Search matches
   Catalan/English country names and ISO identifiers through the existing
   ViewModel contract.
4. **Country archive.** Results are a vertically scrolling editorial list. A
   row gives the flag, Catalan name, personal relationship first, and a single
   quiet secondary fact only when the user selects a reference-stat sort.
   Default order is Catalan name, not a ranking.
5. **Secondary ordering.** A compact sort control exposes name, population,
   area, GDP, and HDI with direction. It remains available but is visually
   subordinate to tracking and search.

Selecting either a map country or a country row opens the country detail. A
map selection first uses a small floating country preview with the relation and
an `Obre` action; it does not force a navigation change while the user explores
the map.

## Tracking-state presentation

`CountryTrackingState` is multi-valued: a country can be both visited and
planned, or lived and wished. The UI must therefore never reduce it to one
exclusive status.

- The map keeps the established visual precedence in `AtlasWorldMap`:
  currently living, lived, visited, planned, wished, then unrecorded.
- List rows show the highest-priority relationship as the leading colour and
  show any additional meaningful states as compact text/chips. This is a
  presentation summary only; it does not alter the derived model.
- The detail shows every active relationship in a compact state ledger.
- Countries outside a selected filter remain in the map and data set; filters
  affect only the displayed list results.

## Proposed country detail hierarchy

1. **Personal relationship.** A full-width visual canvas leads with the
   country name, flag, and a state ledger (`Has visitat`, `Hi has viscut`,
   `Hi vius`, `El tens planejat`, `El vols visitar`) appropriate to the derived
   state. A cached landscape image is used when available; otherwise this is a
   deliberate selected-country map canvas, never an empty image placeholder.
2. **Evidence card.** Immediately below, a floating `LA TEVA PETJADA` card
   explains *why* the state exists. It groups manual visits, journeys/stops,
   excursions, and completed flight or itinerary endpoints into readable
   records. It explicitly retains evidence when state is derived rather than
   manually entered.
3. **Contextual capture.** A clear action, `Registra en aquest país`, opens
   the global capture surface with the selected ISO2. Choices should use the
   country name in their confirmation/context copy and include the foundation
   vocabulary for visit, trip, flight, living, and memories as those capture
   flows are connected.
4. **Place map.** A focused map shows the selected country and the user's
   recorded places/routes where coordinates exist. It keeps an offline geometry
   fallback. This requires a reusable rework `CountryMapCanvas` map mode rather
   than trying to coerce the world overview into a detail viewport.
5. **Country context.** Personal memories follow when present. Reference facts
   (headline KPIs and deeper country information) follow the personal record
   and are visually quieter. Optional remote imagery, currency, and other
   integrations must remain non-blocking.

The proposed initial vertical slice includes the first three levels, with the
map canvas fallback and a compact personal evidence list. The existing richer
facts, memories, and currency content can be connected incrementally after the
tracking-first detail is accepted; it should not delay the list/detail
replacement.

## Required states and safeguards

- **New user:** the map is uncoloured except for base land; the progress card
  explains that every country can become part of the archive; search and full
  list remain useful.
- **Established user:** all tracking colours and multi-state labels remain
  legible; a long results list keeps the map and controls discoverable.
- **Map selection:** selection has a visible preview and can be cleared by a
  sea tap; it never silently changes a country record.
- **No cached image/offline:** the detail uses the selected-country map
  fallback; country records and capture remain available.
- **No location coordinates:** evidence still renders and the map describes
  the missing geographic detail without hiding the record.
- **Long Catalan labels/compact screens:** state labels wrap or abbreviate
  safely; controls retain accessible touch targets.
- **Missing or deleted country route:** show a stable recovery state with a
  back action, rather than a blank detail canvas.

## Implementation boundary

The next coding slice should create:

- `ui/rework/screens/countries/` compositions for the list, selected-map
  preview, and tracking-first detail;
- rework navigation routes for list and `country/{iso2}` detail;
- thin route adapters that instantiate the existing `CountryListViewModel` and
  `CountryDetailViewModel` with the existing app-container dependencies;
- a reusable focused-country map mode in `ui/rework/map/` if the first detail
  slice cannot adequately represent the selected country through the shared
  map boundary.

Legacy UI files remain untouched. The detail route and capture actions must
not mutate country state until their existing use-case-backed flows are
deliberately connected.
