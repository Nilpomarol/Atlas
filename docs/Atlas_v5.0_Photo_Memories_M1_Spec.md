# Atlas v5.0 Photo Memories — M1 Trip Gallery

## Purpose

M1 adds a read-only, trip-scoped photo gallery to the existing trip detail page.
It turns photos already attached to trip stops and excursion stops into a coherent
memory view without changing how photos are stored, created, deleted, backed up,
or restored.

## Scope

Included:

- A `Records` section on trip detail.
- Photos grouped by their trip stop or excursion stop.
- Deterministic narrative ordering.
- Location, date, country code, and excursion context where available.
- A visual marker for the trip cover photo.
- A deliberate empty state.
- Opening the existing stop detail surface when a gallery group or photo is tapped.
- Pure projection tests for ordering and de-duplication behavior.

Not included:

- Database or Room changes.
- Backup format changes.
- New photo creation or deletion paths.
- A trip-wide full-screen viewer.
- Captions, favorites, albums, or photo editing.

## Existing Ground Truth

- `StopPhoto` is the persisted photo record.
- Photo files remain under the app-owned `files/photos/` directory.
- Trip detail already observes photos for every trip stop and excursion stop.
- Stop detail remains the owner of photo add, delete, and cover actions.
- Backup format v3 already includes the current photo records and photo files.

## Projection Contract

The gallery is derived in the presentation layer from ordered trip stops, ordered
excursions and excursion stops, and the two existing stop-photo maps. No gallery
state is persisted.

Narrative order:

1. Trip stops by `sortOrder`, then stable ID.
2. Photos within each stop by `sortOrder`, then stable ID.
3. Excursions anchored to a trip stop immediately after that stop, ordered by
   excursion `sortOrder`, then stable ID.
4. Excursion stops by `sortOrder`, then stable ID.
5. Unanchored excursions, and excursions with a missing anchor, after all trip
   stops using the same deterministic excursion ordering.

Groups without photos are omitted. Every referenced photo appears in exactly one
group.

## UI Contract

- The section follows the existing Warm Editorial Atlas / Cartographer's Ink
  tokens and card components.
- The section title is `Records`.
- Each group shows a compact context label, location title, optional date, country
  code, photo count, and horizontally scrollable photo tiles.
- The trip cover receives a restrained `PORTADA` marker.
- Missing or unreadable photo files show a stable placeholder instead of collapsing
  layout.
- The empty state explains that photos are added from a stop; it does not introduce
  a second creation workflow.
- Tapping a group or tile opens the existing stop detail modal for that source.

## Data Safety

M1 is read-only with respect to photo persistence. It must not write or migrate
database records, move or delete photo files, alter backup serialization, or
introduce default user data.

## Acceptance Criteria

- The gallery updates reactively when existing stop photo flows change.
- Group and photo ordering is deterministic.
- Anchored, unanchored, and missing-anchor excursion photos are represented once.
- A trip with no photos has a balanced, actionable empty state.
- A missing photo file leaves a visible placeholder.
- Existing stop photo add, delete, cover, backup, and restore behavior is unchanged.
