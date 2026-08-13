# Atlas Documentation Index

Last updated: 2026-08-13.

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
- Room database version: **26**.
- Backup format version: **4**.
- Backup container: `.atlasbackup` ZIP with `atlas-backup.json` plus referenced
  user photo files under `photos/`.
- Current bundled dataset versions:
  - countries: `2026.2`
  - airports: `2026.3`
  - airlines: `2026.1`
  - aircraft types: `2026.3`
  - country stats: `2026.3`

### Resolved: persisted quick-trip flag

Migration 24 → 25 added `trips.is_quick_trip`, contradicting
`docs/Atlas_Product_Specification.md` §7.1.1, which requires quick-trip presentation to
be **derived** (a trip with exactly one stop), not stored. The stored flag also never
cleared, so a trip kept rendering compactly after the user added more stops.

Migration 25 → 26 removes the column. Compactness is derived again, and there is no
user-facing trip type.

### Trip model: excursions collapsed into nested stops

Migration 25 → 26 also removes the `excursions` and `excursion_stops` tables. A place
visited from another place is now an ordinary trip stop with `parent_stop_id` set, plus
an optional `side_trip_label`. Excursion-stop ids were preserved through the migration,
so photos stayed attached.

`parent_stop_id` deliberately carries **no foreign key**, matching `stop_photos`. The
parent/child cascade is enforced in `TripRepositoryImpl.deleteTripStop` and
`DeleteTripStopUseCase`; any new stop-deleting write path must preserve it.

See `docs/Atlas_Trip_Model_Rework_Spec.md`, including its device-verification checklist
for the migration.

## Next Planned Work

### Active UI Rework Program

The primary UI initiative is a separate, new implementation governed by:

1. `docs/Atlas_UI_Rework_Implementation_Plan.md`
2. `docs/Atlas_UI_Rework_Foundation.md`
3. Approved concepts under `docs/ui-rework/`

For UI decisions, these documents supersede the legacy screens, legacy
navigation, the old design-system reference, and the UI polish checklist. The
legacy UI is not a design or layout reference. Existing code remains the source
of truth for data, domain behavior, backup/import, and compatibility.

The rework foundation, app shell, Home idle vertical slice, and context-aware
Home are implemented and accepted as of 2026-08-11. Phase 4 **Countries** (list and
country detail) is implemented as of 2026-08-13. The current `com.atlas.ui.rework`
Home and Countries code is authoritative and should not be redesigned while
implementing another destination.

The next phase is **Phase 5 — Trips**, which is blocked on a data-model change rather
than a layout decision. Recording one travel memory currently spans seven record types
across four levels of nesting, and asks the user to classify a place as a main stop or
an excursion — a distinction that changes nothing for country tracking.

`docs/Atlas_Trip_Model_Rework_Spec.md` is the approved direction: collapse excursions
into a parent/child relationship between trip stops, remove the persisted quick-trip
flag, and rebuild Trips on the resulting model. It is a released-data change with its
own migration (25 → 26) and backup format bump (v4), delivered in reviewable slices.

### Other planned product work

- Country stats scope preference: let the user choose whether country-based stats use
  the UN 195 list, UN + Kosovo + Taiwan 197 list, or all Atlas countries and
  territories. This should affect stats only; country lists, country detail, trips,
  flights, search, and stored user records should continue to use the full Atlas
  country/territory dataset.
- Quick trip creation: implemented, but superseded in direction. The rework removes the
  full-versus-quick split from the user's mental model entirely — there is one creation
  flow and one trip type, and card compactness is derived from trip content rather than
  chosen at creation. See `docs/Atlas_Trip_Model_Rework_Spec.md`.

## Active Documentation

Read these for current implementation decisions:

1. `docs/README.md` - documentation map and current release facts.
2. `docs/Atlas_UI_Rework_Implementation_Plan.md` - mandatory plan for the new UI.
3. `docs/Atlas_UI_Rework_Foundation.md` - rework product and visual foundation.
4. `docs/Atlas_Trip_Model_Rework_Spec.md` - approved trip/stop model collapse, migration 25 → 26, and backup v4.
5. `docs/Atlas_Technical_Architecture.md` - implemented architecture and layer boundaries.
6. `docs/Atlas_Data_Model.md` - current conceptual data model, Room version, migration rules, and backup model.
7. `docs/Atlas_Photo_Backup_Spec.md` - current backup/export/import contract.
8. `docs/Atlas_Product_Specification.md` - product identity and long-term domain behavior.
9. `docs/Atlas_Post_v2.0_Roadmap.md` - completed milestone history and later directions.

`docs/ui-polish-checklist.md` and `docs/Atlas - Design System.html` apply only
to explicitly requested legacy maintenance. They are not rework references.

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
