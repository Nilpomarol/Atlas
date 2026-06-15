# Atlas v5.0 Photo Memories - M3 Country Memories

## Purpose

M3 brings personal stop photos into country detail so country reference information
and the user's own travel history meet on one surface.

## Scope

- Add an `Els teus records` section to country detail.
- Include photos from trip stops and excursion stops whose `countryIso2` matches the
  open country.
- Group memories into one card per trip, with location labels retained on each photo.
- Reuse the M1 narrative ordering within each trip.
- Open the exact tapped photo in the shared M2 full-screen viewer.
- Navigate from a memory group or viewer item to the owning trip.
- Hide the complete section when there are no matching personal photos.
- Let the user collapse or expand the cards with a per-country preference that
  persists across app restarts.

Not included:

- Room or backup format changes.
- New photo creation, deletion, or cover controls from country detail.
- Photo captions, favorites, albums, or metadata.
- External country hero or information photos in personal memories.

## Projection Contract

The country projection is derived in the presentation layer from:

- existing trips and trip stops;
- existing excursions and excursion stops;
- existing bulk stop-photo repository flows.

No country identifier is added to `StopPhoto`, and no second photo relationship is
persisted. ISO2 ownership continues to come from the owning stop.

Trips are ordered by start date descending, followed by title and stable ID. Each trip
produces at most one country-memory card. Within that card, M3 delegates ordering to
the tested M1 trip gallery projection, keeps only stop groups whose ISO2 matches the
open country, and flattens their photos into one carousel. Photos retain their owning
location, stop type, date, and excursion context, remain ordered by `sortOrder` and
stable ID, and appear exactly once.

The roadmap's proposed country-specific repository method is intentionally not added.
`StopPhotoRepository.observeByStopIds` already supplies the required reactive data;
adding another method would duplicate relationship logic outside the presentation
projection without improving persistence or query safety.

## UI Contract

- The section uses the established country-detail spacing, Atlas cards, typography,
  colors, and the M1 photo tile.
- Each card shows the owning trip, trip date where available, distinct matching
  locations, and photo count.
- Each photo keeps a compact location label and its complete stop or excursion
  context in the viewer.
- Tapping a card opens the owning trip.
- Tapping a photo opens that exact photo in the shared viewer.
- The country viewer is read-only and offers navigation to the owning trip.
- The section is absent rather than replaced with an empty card when no memories
  exist.
- `AMAGA` collapses the trip cards while leaving the section heading and `MOSTRA`
  action discoverable. The choice is stored per ISO2 in the existing preferences
  DataStore and defaults to visible.

## Data Safety

M3 adds no migration, backup change, photo file operation, or default personal data.
Dataset refreshes cannot affect photo ownership because relationships remain anchored
to user-created trip and excursion stops.

## Acceptance Criteria

- Trip-stop and excursion-stop photos resolve to the correct country.
- Each matching photo appears once and only once.
- Trip and in-trip ordering are deterministic.
- Multiple matching stops from one trip produce one card.
- The exact tapped photo opens in the shared viewer.
- Memory navigation opens the owning trip.
- Countries without personal photos show no memories section.
- Collapsed/expanded state survives app restarts independently for each country.
- External cached country photos never enter the projection.
