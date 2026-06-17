# Atlas v5.0 Photo Memories - M5 Trip Story

## Purpose

M5 turns existing trip records into a read-only travel story. It is a browsing
surface, not a second editor, export format, or social sharing flow.

## Scope

- Add a `Relat` entry point from trip detail.
- Add a `trips/{tripId}/story` route.
- Generate a full-screen slideshow from existing trip, stop, excursion, itinerary,
  note, map, and photo data.
- Reuse the existing stop-photo files.
- Keep all story content reactive and derived from existing repositories.

Not included:

- Room or backup format changes.
- Editable story blocks.
- Captions, favorites, photo metadata, video, music, export, or sharing.
- A custom image zoom/pan surface inside story mode.

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

- The story screen is a full-screen `HorizontalPager` slideshow, not a vertical
  article or editor. User dragging is disabled.
- The story route opts out of the app scaffold bottom navigation and scaffold padding
  so the slideshow owns the full app window.
- Slides include trip title, route map, stop intro slides, itinerary-derived flight
  slides, excursion intro slides, photo slides, and a generated summary.
- The route slide reuses `TripMapPreview` with existing stop/excursion data and must
  still be useful without coordinates.
- Stop, flight, and excursion names stay visible on intro and photo slides so the user
  always knows where a memory belongs.
- Itinerary-derived stops are labeled `VOL` in story mode, not `PARADA`.
- The slideshow provides normal controls: back/close, previous, play/pause, next,
  progress bars, and current/total count.
- Tapping the left half goes to the previous slide; tapping the right half goes to the
  next slide. Photo taps are slide navigation, not photo-viewer entry points.
- Auto-play advances through the slide deck and pauses at the end; pressing play at
  the end restarts from the first slide.
- Empty photo sets are allowed; the story should still be useful.

## Data Safety

M5 adds no schema, migration, backup version, file operation, or user-created story
records.

## Acceptance Criteria

- The story appears from trip detail and returns with back navigation.
- A trip with no photos still shows title, route, stops, notes, and summary.
- The user can tap left/right to move between slides.
- Auto-play can be paused/resumed and does not loop unexpectedly at the end.
- Anchored and unanchored excursions appear in deterministic order.
- Automated unit tests cover the generated story ordering.
