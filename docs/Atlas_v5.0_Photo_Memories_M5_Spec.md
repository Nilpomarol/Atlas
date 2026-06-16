# Atlas v5.0 Photo Memories - M5 Trip Story

## Purpose

M5 turns existing trip records into a read-only travel story. It is a browsing
surface, not a second editor, export format, or social sharing flow.

## Scope

- Add a `Relat` entry point from trip detail.
- Add a `trips/{tripId}/story` route.
- Generate a vertical story from existing trip, stop, excursion, itinerary, note, and
  photo data.
- Reuse the existing stop-photo files and the shared full-screen viewer.
- Keep all story content reactive and derived from existing repositories.

Not included:

- Room or backup format changes.
- Editable story blocks.
- Captions, favorites, photo metadata, video, music, export, or sharing.
- A custom slideshow engine.

## Projection Contract

The story projection is presentation-only. It uses:

- the trip record;
- ordered trip stops;
- ordered anchored, unanchored, and missing-anchor excursions;
- the linked itinerary and itinerary groups when present;
- existing stop-photo flows.

Stop and excursion ordering follows the same narrative rules as the trip gallery:
trip stops by `sortOrder` then ID, anchored excursions after their owning stop,
unanchored or missing-anchor excursions after all trip stops, and photos by
`sortOrder` then ID.

## UI Contract

- The story screen uses the Warm Editorial Atlas card style.
- The first screen area summarizes title, date, days, stops, countries, and photos.
- Route overview uses existing stop/country data and must work without coordinates.
- Stop cards show date/country context, notes, and photos.
- Excursions appear in the relevant stop card when anchored, or in a separate section
  when unanchored.
- Tapping a photo opens the shared viewer in read-only mode.
- Empty photo sets are allowed; the story should still be useful.

## Data Safety

M5 adds no schema, migration, backup version, file operation, or user-created story
records.

## Acceptance Criteria

- The story appears from trip detail and returns with back navigation.
- A trip with no photos still shows title, route, stops, notes, and summary.
- Photos open at the exact tapped item in the shared viewer.
- Anchored and unanchored excursions appear in deterministic order.
- Automated unit tests cover the generated story ordering.
