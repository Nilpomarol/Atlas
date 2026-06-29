# Atlas Documentation Index

Last updated: 2026-06-24.

This repository is a released Android project. The source of truth is the current
code, exported Room schemas, bundled assets, and backup implementation. Documentation
must describe the implementation as it exists, not the other way around.

## Current Released State

- Native Android app written in Kotlin with Jetpack Compose.
- Local-first personal travel atlas: countries/territories, trips, stops, excursions,
  flights, itineraries, country information, stats, notes, photos, memories, and
  explicit backup/restore.
- v2.0 through v5 Photos and Memories are complete. Optional photo metadata is
  intentionally deferred.
- Room database version: **24**.
- Backup format version: **3**.
- Backup container: `.atlasbackup` ZIP with `atlas-backup.json` plus referenced
  user photo files under `photos/`.
- Current bundled dataset versions:
  - countries: `2026.2`
  - airports: `2026.3`
  - airlines: `2026.1`
  - aircraft types: `2026.3`
  - country stats: `2026.3`

## Next Planned Work

- Country stats scope preference: let the user choose whether country-based stats use
  the UN 195 list, UN + Kosovo + Taiwan 197 list, or all Atlas countries and
  territories. This should affect stats only; country lists, country detail, trips,
  flights, search, and stored user records should continue to use the full Atlas
  country/territory dataset.
- Quick trip creation: add a faster way to create a normal trip with name, dates, and
  one location using the same location model as a trip stop. Trips with exactly one
  stop should render as compact quick-trip cards in the trip list and dashboard, but
  they should remain normal trips underneath.

## Active Documentation

Read these for current implementation decisions:

1. `docs/README.md` - documentation map and current release facts.
2. `docs/Atlas_Technical_Architecture.md` - implemented architecture and layer boundaries.
3. `docs/Atlas_Data_Model.md` - current conceptual data model, Room version, migration rules, and backup model.
4. `docs/Atlas_Photo_Backup_Spec.md` - current backup/export/import contract.
5. `docs/Atlas_Product_Specification.md` - product identity and long-term domain behavior.
6. `docs/Atlas_Post_v2.0_Roadmap.md` - completed milestone history and later directions.
7. `docs/ui-polish-checklist.md` and `docs/Atlas - Design System.html` - visual review references.

## Implementation-History Documents

These are useful for understanding why features were built, but they are not the
first source for current release state:

- `docs/Handoff_Prompt.md` - development handoff snapshot with detailed milestone
  notes. Use it as evidence, not as the canonical long-term docs.
- `docs/Atlas_v4.0_Country_Stats_Spec.md` - implemented v4 country facts contract.
- `docs/Atlas_v5.0_Photo_Memories_M1_Spec.md`
- `docs/Atlas_v5.0_Photo_Memories_M2_Spec.md`
- `docs/Atlas_v5.0_Photo_Memories_M3_Spec.md`
- `docs/Atlas_v5.0_Photo_Memories_M5_Spec.md`
- `docs/Country_Info_Enrichment_Backlog.md`

## Historical / Deprecated Notes

- `memory/` contains old agent memory notes. Treat them as historical only.
- Release or milestone docs may mention their original phase. If they conflict with
  the active docs, current code and active docs win.

## Safe Future Development Rules

- Do not break existing user data.
- Do not remove, rewrite, or skip existing Room migrations.
- Do not break backup/import compatibility with v1/v2 JSON or v3 `.atlasbackup`.
- Do not casually change serialized formats, persisted fields, stable identifiers, or
  bundled asset contracts.
- New persisted user data requires a migration, exported schema, backup review, and
  compatibility tests.
- Prefer additive or migration-safe changes.
- Prefer derived projections over duplicated stored relationships.
- Keep optional external services non-blocking so local personal data remains usable.
