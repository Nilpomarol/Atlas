# Atlas Trip Model Rework Spec

Status: **Approved and implemented** (model, migration, backup, presentation)
Created: 2026-08-13
Belongs to: UI rework programme, precursor to Phase 5 (Trips)

Implementation notes where the build diverged from the original proposal are marked
**Decision** below.

## 1. Why this exists

The Phase 5 Trips slice was blocked by a modelling problem, not a layout problem.
Recording one travel memory currently requires the user to understand seven record
types across four levels of nesting:

```text
Trip → TripStop → Excursion → ExcursionStop
Itinerary → ItineraryGroup → Flight   (plus solo Flight)
```

Three of those types exist to express one idea: *a place you were, which hangs off
another place you were*. This spec collapses that idea into a single relationship.

The rework implementation plan requires the trip model and information hierarchy to be
"deliberately specified" before Trip Detail is rebuilt. This is that specification.

## 2. Problems being fixed

### 2.1 The quick-trip flag contradicts the product spec, and is buggy

`docs/Atlas_Product_Specification.md` §7.1.1 states:

> Do not introduce a persisted `quick trip` type unless future design needs explicit
> manual compact/full display control.

The implementation nonetheless persists `trips.is_quick_trip` (added in migration
24 → 25). The spec's intended rule was **derived** — "trips with exactly one stop *may
appear* as compact cards".

This produces a live defect: a trip created through quick-create keeps
`is_quick_trip = true` after the user adds a second and third stop, so a genuine
multi-stop trip renders as a compact card forever. The derived rule self-corrects; the
stored flag cannot. `TripListItemUiState` currently carries both signals
(`stopCount` and `isQuickTrip`), so presentation has two competing truths.

**Decision: remove the persisted flag.** Compactness is a presentation concern derived
from trip content. There is no user-facing "trip type".

### 2.2 Excursions ask the user to model instead of remember

An excursion is a titled container, anchored to a main stop, holding its own stops.
To file a place, the user must answer "is this a main stop, or an excursion off a main
stop?" — a data-modelling question with no stable answer for most journeys.

The answer also changes nothing that matters: excursion stops and trip stops count
**identically** for country/territory tracking (product spec §7.6). The app demands a
decision it does not use.

Worse, the concept does not hold its own definition: `excursions.anchor_trip_stop_id`
is nullable, so an excursion can float free of any stop. The spec defines an excursion
as "a side route attached to a main trip stop" — an unanchored one is, by that
definition, not an excursion. It is a second list of stops with a title.

**Decision: collapse excursions into a parent/child relationship between stops.**

### 2.3 Two hierarchies record the same journey

A flight can be a solo `Flight`, or a `Flight` inside an `ItineraryGroup` inside an
`Itinerary` linked to a trip — which then *generates* trip stops that appear in the
route as pseudo-stops carrying `source`, `is_visible`, and `display_title`.

This spec does **not** resolve the itinerary hierarchy (see §8, deferred). It preserves
current generated-stop behaviour unchanged. It is recorded here because it is the next
largest source of the same confusion.

## 3. Target model

### 3.1 Domain

```kotlin
data class Trip(
    val id: String,
    val title: String,
    val status: TravelStatus,
    val dateRange: FlexibleDateRange?,
    val notes: String?,
    val coverPhotoFilename: String? = null,
)   // isQuickTrip removed

data class TripStop(
    val id: String,
    val tripId: String,
    val parentStopId: String?,     // NEW: null = main route; set = nested place
    val sideTripLabel: String?,    // NEW: optional grouping label
    val locationName: String,
    val countryIso2: String,
    val latitude: Double?,
    val longitude: Double?,
    val dateRange: FlexibleDateRange?,
    val notes: String?,
    val sortOrder: Int,
    val source: TripStopSource = TripStopSource.MANUAL,
    val itineraryGroupId: String? = null,
    val isVisible: Boolean = true,
    val displayTitle: String? = null,
)
```

`Excursion` and `ExcursionStop` are deleted, along with their entities, DAO, mappers,
use cases, and repository surface.

### 3.2 Rules

- A stop with `parentStopId == null` is on the main route.
- A stop with `parentStopId` set is a place visited *from* its parent.
- Nesting is **one level only**. A child may not itself have children. Enforced in
  domain validation, not by the schema.
- A child's parent must belong to the same trip.
- `sortOrder` is scoped to siblings — main stops order among themselves, children order
  among themselves under a shared parent.
- Country/territory derivation treats parent and child stops identically. This
  preserves today's behaviour exactly.

### 3.3 About `sideTripLabel`

`excursions.title` is `NOT NULL` and user-typed. It cannot be dropped without
discarding user data, which the data-layer rules forbid. Folding it into stop `notes`
would mix a label into prose irreversibly.

A nullable label column is the clean relocation: it preserves every existing title,
costs one column, and introduces no new entity. The UI ignores it when absent, and may
group consecutive siblings that share one when present.

This is a deliberate trade and the one part of this spec most open to challenge. The
alternative — accept the loss of excursion titles — should be an explicit user
decision, not a silent migration outcome.

## 4. Migration 25 → 26

The migration is safe because **excursion-stop IDs are preserved verbatim**. Photos
reference stops by `stop_photos.stop_id` + `stop_type`; keeping IDs means no photo row
is re-pointed and no photo file moves.

Ordered steps:

1. `ALTER TABLE trip_stops ADD COLUMN parent_stop_id TEXT` (nullable).
2. `ALTER TABLE trip_stops ADD COLUMN side_trip_label TEXT` (nullable).
3. Add index on `parent_stop_id`.
4. Copy `excursion_stops` into `trip_stops`, preserving `id`:
   - `trip_id` ← `excursions.trip_id`
   - `parent_stop_id` ← `excursions.anchor_trip_stop_id`
   - `side_trip_label` ← `excursions.title`
   - `notes` ← `excursion_stops.notes` (excursion `notes` appended when non-empty)
   - `sort_order` ← `(excursions.sort_order * 100) + excursion_stops.sort_order`,
     which preserves both inter-excursion and intra-excursion order among siblings
   - `source` ← `MANUAL`, `is_visible` ← `1`, timestamps carried across
5. Unanchored excursions (`anchor_trip_stop_id IS NULL`) become **main route stops**
   (`parent_stop_id` stays null), appended after existing main stops, retaining
   `side_trip_label` so nothing is lost.
6. `UPDATE stop_photos SET stop_type = 'TRIP_STOP' WHERE stop_type = 'EXCURSION_STOP'`.
7. `DROP TABLE excursion_stops; DROP TABLE excursions;`
8. Recreate `trips` without `is_quick_trip` using the drop-and-recreate pattern
   (SQLite cannot reliably `DROP COLUMN` across supported Android versions), copying
   every remaining column.

**Decision — no foreign key on `parent_stop_id`.** The original proposal declared a
self-referencing FK with `ON DELETE CASCADE`. The implementation does not, for three
reasons: SQLite cannot add a foreign key via `ALTER TABLE`, so it would have forced a
full rebuild of `trip_stops` with a self-reference across a table rename (a known
SQLite footgun); `stop_photos` already references stops with no FK, so this matches
house precedent; and it keeps imports independent of row order, as the data-layer
rules prefer.

The cascade is therefore enforced in code:

- `TripRepositoryImpl.deleteTripStop` deletes children in the same transaction.
- `DeleteTripStopUseCase` deletes the children's photos before deleting the parent,
  so photo files cannot leak with nothing pointing at them.

Any future write path that deletes stops must preserve both steps.

### 4.1 Test coverage — and its gap

Covered by JVM unit tests:

- `LegacyExcursionCollapseTest` asserts the collapse semantics — anchored nesting with
  preserved ids, unanchored promotion to main route, sort-order monotonicity, notes
  attached once to the first stop, childless excursions folded into their anchor, and
  orphaned excursion stops dropped.
- `TripPhotoGalleryUiStateTest`, `TripStoryUiStateTest`, `CountryMemoriesUiStateTest`
  cover the narrative walk over nested stops.
- `CountryStateDerivationServiceTest` covers a nested stop marking a country visited.
- `BackupSchemaCompatibilityTest` covers v1 archives still decoding into v4.

**Not covered:** the SQL of migration 25 → 26 itself. The project has no `androidTest`
source set and no `room-testing` dependency, and a Room `MigrationTestHelper` test
needs both plus a device or emulator. `LegacyExcursionCollapseTest` mirrors the
migration's semantics but exercises the Kotlin import path, not the SQL.

Until that gap is closed, migration 25 → 26 must be verified on a device against a
real database before release:

- a trip with an anchored excursion of two stops nests correctly, in order, with label;
- an unanchored excursion becomes main-route stops;
- photos previously attached to excursion stops still resolve;
- trips survive the `is_quick_trip` table rebuild with all other fields intact.

## 5. Backup

Collapsing the model changes the export shape, so the backup format moves to **v4**.

- **Export (v4)**: `tripStops` entries gain `parentStopId` and `sideTripLabel`. The
  `excursions` and `excursionStops` arrays are removed.
- **Import v1 / v2 / v3**: must continue to work. The importer applies the same
  collapse logic as §4, preserving IDs, and maps photo rows with
  `stopType = EXCURSION_STOP` to `TRIP_STOP`.
- **Validation**: excursion-specific referential checks in `BackupValidation` are
  replaced by parent-stop checks (parent exists, belongs to the same trip, is itself
  a main stop).

Backup compatibility tests must cover a v3 archive containing anchored and unanchored
excursions with photos.

## 6. Presentation and UI consequences

- `TripListItemUiState.isQuickTrip` is removed. Card richness derives from content:
  cover photo present, stop count, route length.
- One creation flow. The user is never asked to choose a trip type up front — they
  give a title, optionally when, optionally one place. Structure emerges as places are
  added.
- Nested places render as indentation under their parent. The words "excursió",
  "itinerari", and "grup" do not appear in the trip UI.
- `CountryStateDerivationService` stops reading excursion stops as a separate source
  and reads trip stops only. No behavioural change — both already counted the same.

## 7. Delivery slices

Each slice is independently reviewable and leaves the app building.

Slices 1–4 landed together: removing two entities referenced by 41 production files
leaves no intermediate state where Kotlin compiles, so splitting them was not possible.

1. **Schema + migration** — done. Entities, migration 25 → 26, exported schema 26.
2. **Domain + data** — done. Unified `TripStop`, excursion use cases/repository/DAO
   deleted, derivation service simplified, cascade enforced in the repository.
3. **Backup v4** — done. Export shape, v1–v3 import collapse, validation, tests.
4. **Legacy UI** — done, as a stub. See the decision below.
5. **Rework Trips list** — next.
6. **Rework Trip Detail** — immediately after the list, to close the gap below.

**Decision — legacy side-trip editing is stubbed, not rewired.** Legacy trip detail
carried 21 excursion CRUD handlers and a full excursion-stop draft flow. Rewiring them
to nested stops meant significant work in a screen Phase 6 deletes, so they were
removed instead. Consequences today:

- Nested stops still **display** everywhere — trip timeline, map (as side-trip lines
  back to their parent), story slides, photo galleries, country memories, stats.
- Nested stops can no longer be **created or edited** in the legacy UI.
- The manual "show as quick trip" toggle is gone, since compactness is now derived.

This is a real capability gap in the currently shipping UI. It closes when the rework
Trip Detail lands, which is why that moved ahead of the rest of Phase 5.

## 8. Deliberately deferred

- **Itinerary hierarchy.** `Itinerary.tripId` is nullable and `ItineraryGroup.status`
  duplicates status alongside `Trip.status` and `Flight.status`. A trip marked
  completed containing a planned flight is representable and meaningless. This
  deserves its own spec and migration.
- **Stop-level progress.** Product spec §7.2 notes that an in-progress trip counts all
  stops as visited. Unchanged here.
- **Date coherence.** Trip and stop date ranges remain mutually unconstrained. A
  future change may warn when a stop falls outside its trip range, but must not block
  — Atlas does not require complete data.

## 9. Compatibility statement

This change alters persisted user data and therefore requires an explicit product
decision, which was given on 2026-08-13. It must not break:

- Room migration history for released users.
- Import of v1 / v2 JSON and v3 `.atlasbackup` archives.
- Existing photo files or their attachment to stops.
- Stable identifiers — every migrated stop keeps its original ID.
- Centralized country-state derivation results.
