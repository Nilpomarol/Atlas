# Atlas Current Roadmap

## Purpose

This roadmap starts from the current implemented baseline. Completed release plans are intentionally not repeated here; `docs/Handoff_Prompt.md` records operational status.

## Completed Baseline

- v2.0: countries, trips, stops, flights, itineraries, backup/import.
- v3.0: flight API integration, airline and aircraft datasets, UTC fields, route maps, location suggestions, and country-tracking flags.
- v3.1: Cartographer's Ink visual redesign across primary screens.
- v3.2: stop photos, trip cover photos, complete stats surfaces, timeline, and navigation polish.
- v4.0 M1: country facts dataset and flexible `country_stat_facts` table.
- v4.0 M2 implementation: Country Info screen and optional cached country hero photos.

## Active: v4.0 Country Depth

### M2 Polish

- Review Country Info hierarchy, spacing, density, colors, and default-open sections.
- Verify long Catalan labels and small-screen behavior.
- Keep empty sections hidden.
- Preserve the current fact schema, importer, and presentation/domain boundaries.
- Consider section quick navigation and hero signature chips only if device review shows clear value.

### M3 Country Detail Enrichment

- Add related flights where useful.
- Improve country timeline completeness and consistency.
- Keep Country Info entry clear and visually integrated.
- Reuse `CountryStateDerivationService`; do not duplicate derivation in UI.

### M4 Country List Improvements

- Add useful type and continent/subregion filtering.
- Preserve fast scanning, flag fallback, state colors, and Catalan labels.

## Later Directions

### Maps and Statistics

- Continue improving world-map exploration using the existing offline geo foundation.
- Add interaction only where it improves personal travel understanding.
- Keep MapLibre isolated to surfaces that benefit from tile-backed context.

### Photos and Memories

- Expand trip and country memories without making photo completeness mandatory.
- Keep personal media local-first.
- Treat any cloud backup or sync as optional.

### Portability

- Improve backup validation and recovery.
- Consider selective export formats.
- Preserve old backup compatibility and stable identifiers.

### Optional Notifications

A future arrival reminder may prompt for actual flight times. It would require WorkManager and notification permission, so it remains a separate explicitly approved feature.

## Roadmap Rules

- Build only the current milestone's schema.
- Prefer small reviewable changes.
- Do not introduce a backend for core use.
- Do not add major libraries without explicit approval.
- Preserve local ownership and incomplete-data workflows.
- Update `docs/Handoff_Prompt.md` whenever implemented status changes.

