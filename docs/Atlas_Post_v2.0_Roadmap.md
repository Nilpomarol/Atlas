# Atlas Implemented Roadmap and Future Directions

## Purpose

This roadmap starts from the implemented and device-verified baseline. It records
completed milestone history and later directions. Exact current release facts live in
`docs/README.md`.

## Completed Baseline

- v2.0: countries, trips, stops, flights, itineraries, and backup/import.
- v3.0: flight API integration, airline and aircraft datasets, UTC fields, route
  maps, location suggestions, and country-tracking flags.
- v3.1: Cartographer's Ink visual redesign across primary screens.
- v3.2: stop photos, trip cover photos, complete stats surfaces, timeline, and
  navigation polish.
- v4.0: country facts, Country Info, country-detail enrichment, country-list
  sorting/filtering, and final device polish.
- v5 Photos and Memories: trip gallery, shared full-screen viewer, country memories,
  generated trip story, and final UI polish. Optional photo metadata remains
  intentionally deferred.
- Backup and portability milestone: photo-inclusive `.atlasbackup` format v3,
  legacy JSON compatibility, explicit recovery, optional Drive-compatible cloud
  backup, progress and result feedback, and three-file automatic retention.

## Completed Direction: v5 Photos and Memories

### Product Goal

Turn the travel records Atlas already stores into a better way to revisit trips and
places. v5 should make trips and countries feel like personal memory surfaces without
turning photo completeness into a requirement or introducing a social/sharing model.

The first three milestones deliberately reuse the current `StopPhotoEntity`, stop
ordering, trip relationships, country identifiers, and `.atlasbackup` v3 format.
Photo metadata and schema changes are deferred until the browsing experience proves
useful.

### Product Principles

- Personal meaning over public sharing.
- Browsing and revisiting over additional data entry.
- Useful with zero, one, or many photos.
- Photos remain app-private and local-first.
- Existing stop and excursion structure provides photo context.
- No duplicate photo ownership model.
- No backend, account, public gallery, or mandatory cloud dependency.
- Prefer derived projections over stored duplicate relationships.

## M1: Trip Gallery

Status: implemented on 2026-06-14; automated checks passed and the gallery was
confirmed on device during the M2 review. Implementation contract:
`docs/Atlas_v5.0_Photo_Memories_M1_Spec.md`.

### Goal

Create a coherent trip-level gallery from photos already attached to trip stops and
excursion stops.

### User Experience

- Add a Photos/Memories section to trip detail.
- Group photos by their owning trip stop or excursion stop.
- Preserve the trip's narrative order:
  1. trip-stop order;
  2. photos within each trip stop by `sort_order`;
  3. excursions anchored to the relevant trip context;
  4. excursion-stop order and photo order.
- Show location name, optional date context, and whether a group belongs to an
  excursion.
- Mark the trip cover clearly within the ordered sequence when one exists; do not
  move it ahead of earlier trip memories.
- Provide a deliberate empty state without suggesting photos are required.
- Keep photo add/delete actions attached to their existing stop editors or detail
  surfaces during M1.

### Architecture

- Add a domain/presentation projection for grouped trip photos; do not expose Room
  entities to UI.
- Reuse the existing reactive trip-stop and excursion-stop photo observations in
  trip detail; no repository expansion is needed for M1.
- Resolve display grouping and narrative ordering in a pure presentation projection.
- Reuse Coil and existing Atlas cards, typography, spacing, and image treatment.

### Data Impact

- No Room migration.
- No backup-format change.
- No file copying or new media directory.

### Acceptance Criteria

- A trip gallery updates reactively when photos are added, deleted, or reordered.
- Trip-stop and excursion-stop photos appear exactly once.
- Ordering is deterministic.
- Missing local files degrade to a stable placeholder without crashing.
- Trips without photos retain a useful, visually balanced detail page.

## M2: Full-Screen Photo Viewer

Status: reimplemented cleanly on 2026-06-15; automated checks passed, the APK
installs/launches on the connected device, and paging/zoom/pan were confirmed on
device. Rotation and system-inset edge review remain pending.
Implementation contract: `docs/Atlas_v5.0_Photo_Memories_M2_Spec.md`.

### Goal

Provide an immersive but practical way to browse a trip's photos while preserving
their travel context.

### User Experience

- Open from any trip-gallery image at the selected position.
- Swipe horizontally through the ordered trip photo sequence.
- Show `current / total`, location name, excursion context, and optional date.
- Support zoom and pan through one focused image component. The initial custom
  Compose gesture/rendering implementation proved unreliable and was removed.
- Provide navigation to the owning stop or excursion stop.
- Allow setting or clearing the trip cover.
- Allow deletion with confirmation and immediate viewer/gallery consistency.
- Return to the same gallery position when the viewer closes.

### Architecture

- The viewer receives stable photo IDs and resolves current state reactively.
- Cover selection continues through `SetTripCoverPhotoUseCase`.
- Deletion continues through `DeleteStopPhotoUseCase` so rows, files, and cover
  cleanup remain centralized.
- Navigation arguments must use stable IDs, not serialized photo objects or file paths.
- Use Telephoto only for image zoom/pan/rendering. Keep paging, context, cover,
  deletion, and source navigation in Atlas.

### Data Impact

- No Room migration.
- No backup-format change.

### Acceptance Criteria

- Viewer order matches the gallery order.
- Rotation/recomposition does not reset the selected photo unexpectedly.
- Deleting the current photo selects a valid neighbor or closes an empty viewer.
- Deleting a cover photo clears or updates the trip cover correctly.
- Large images remain responsive and do not require loading the whole trip into
  bitmap memory.

## M3: Country Memories

Status: implemented on 2026-06-15; automated checks pass. The initial M3 APK
installed/launched on device; the one-card-per-trip and persistent-collapse
refinement awaits device visual review. Implementation contract:
`docs/Atlas_v5.0_Photo_Memories_M3_Spec.md`.

### Goal

Bring personal photos into country detail so general country information and personal
travel history meet on the same surface.

### User Experience

- Add an "Els teus records" section to country detail.
- Derive memories from trip stops and excursion stops whose `countryIso2` matches the
  country.
- Use one card per trip, with one ordered carousel across all matching trip and
  excursion stops. Keep the location visible per photo.
- Open the same full-screen viewer used by trip galleries.
- Provide navigation from a memory back to its trip or owning stop.
- Hide the section when the country has no personal photos.
- Allow the user to collapse/expand the cards and persist that choice per country.
- Keep external country hero photos visually and semantically separate from personal
  memories.

### Architecture

- Derive a country-scoped presentation projection from the existing bulk
  `StopPhotoRepository.observeByStopIds` flows. A dedicated repository method was
  unnecessary because stop ownership and ISO2 context already exist in the observed
  trip and excursion models.
- Reuse ISO2 and current trip/stop relationships; do not store a second country ID on
  `StopPhotoEntity`.
- Reuse the M1 photo tile and M2 viewer rather than creating a country-only media
  system.
- Persist only the per-country visibility preference in the existing DataStore; photo
  grouping remains a derived presentation projection.

### Data Impact

- No Room migration.
- No backup-format change.

### Acceptance Criteria

- Trip-stop and excursion-stop photos resolve to the correct country.
- A photo appears once per relevant country memory section.
- Dataset refreshes cannot break personal-photo relationships.
- External cached country photos never appear as personal memories.

## M4: Optional Photo Metadata

Status: deferred on 2026-06-16. M1-M3 device QA passed, but there is still no
demonstrated need strong enough to justify a Room and backup migration. Revisit only
after story mode or day-to-day photo browsing exposes a concrete metadata gap.

### Entry Gate

Do not start M4 until M1–M3 have passed device review and there is a demonstrated need
for metadata while browsing. The exact fields must be confirmed before changing the
schema.

### Candidate Scope

- Optional caption.
- Optional taken date with flexible precision where appropriate.
- Optional favorite marker.
- Explicit photo ordering remains supported.

Rotation correction should first be handled through image metadata/rendering where
possible rather than persisted as a new field.

### Likely Data Impact

- Room DB v25.
- New immutable migration from v24 to v25.
- `StopPhotoEntity` gains only approved optional fields.
- Backup format v4 carries the new metadata.
- v1–v3 imports remain supported through defaults.
- Cloud backup continues transporting the same standard Atlas archive.

### Required Validation

- Migration test preserving existing photo rows.
- Backup v4 round-trip and v1–v3 compatibility tests.
- Caption/date/favorite editing tests.
- Device review for long Catalan captions, approximate dates, and keyboard behavior.

## M5: Generated Trip Story

Status: read-only slideshow implementation added on 2026-06-16 and polished through
M6 on 2026-06-17. Implementation contract:
`docs/Atlas_v5.0_Photo_Memories_M5_Spec.md`.

### Goal

Generate a read-only narrative from existing trip data. Story mode is a presentation
layer, not a second trip editor.

### Initial Format

A full-screen Compose slideshow containing only derived travel data:

1. trip title, dates, days, stops, countries, and photo count;
2. route map overview with country/stop context;
3. stop intro slides with notes and photo counts;
4. itinerary-derived flight slides labeled `VOL`;
5. photo slides with visible stop/flight/excursion context;
6. anchored excursion slides immediately after their owning stop;
7. unanchored or missing-anchor excursions after all trip stops;
8. compact trip statistics and closing summary.

### Rules

- Story order comes from existing trip, stop, excursion, itinerary, and flight order.
- Empty sections are omitted.
- Approximate dates remain approximate.
- The user can tap left/right, use previous/next controls, and pause/resume auto-play.
  Swipe gestures are disabled so auto-play and manual movement always settle on a full
  slide.
- Story mode hides the normal app bottom navigation and bypasses scaffold padding.
- Story mode must work without photos.
- No separate story database, manual slide editor, music, video rendering, or social
  sharing in the initial milestone.

### Data Impact

- No schema change should be required after M4.
- Story mode consumes domain projections built for galleries, timelines, maps, and
  related travel records.

## M6: Final Photo UI Polish

Status: implemented on 2026-06-17 as a visual-only pass. `testDebugUnitTest`,
`assembleDebug`, and connected-device install/launch smoke pass. No schema, backup,
repository, or photo-file behavior changed.

### Goal

Do a final user-facing polish pass across the v5 photo-memory surfaces after M1-M5 are
implemented and device-reviewed.

### Scope

- Review spacing, carousel gutters, card density, and text hierarchy across trip
  records, country memories, viewer overlays, and trip story.
- Use compact, consistent record/memory card headers and balanced LazyRow gutters so
  partially visible photos keep the same card margin.
- Keep full-screen viewer controls separate from the photo context and group position,
  title, context, and date in the bottom overlay.
- Give story slides safer narrow-screen spacing, weighted statistics, long-text
  overflow limits, and a softer caption gradient over photo slides.
- Confirm Catalan labels, empty states, and action wording are consistent.
- Verify photo-empty, photo-heavy, narrow-screen, and long-title cases.
- Keep this polish visual-only unless a small bug is found during review.

### Data Impact

- No Room migration.
- No backup-format change.
- No new feature scope.

## Explicit Non-Goals for v5

- Public profiles, comments, likes, or shared social galleries.
- Automatic photo upload independent of Atlas backups.
- Device-wide photo-library indexing.
- Face recognition, object recognition, or AI captioning.
- Video support.
- Photo editing beyond viewing and existing add/delete/cover actions.
- Duplicate photo files for trip, country, gallery, or story ownership.
- A generic notes or scrapbook system disconnected from travel structure.

## Cross-Milestone Validation

- Run focused unit tests for ordering and trip/country photo projections.
- Run `testDebugUnitTest` and `assembleDebug` for every milestone.
- Device-test small, large, and photo-empty trips.
- Verify trip-stop and excursion-stop deletion behavior.
- Verify gallery/viewer state after photo deletion and cover changes.
- Re-run `.atlasbackup` export/import after any photo schema or file-handling change.
- Review memory use with a large photo trip.
- Review narrow-screen text wrapping and Catalan labels.

## Delivery Order

1. Write the M1 implementation spec after inspecting current trip-detail and photo
   repository APIs.
2. Implement and device-review M1 before introducing the viewer.
3. Reuse the validated trip sequence for M2.
4. Reuse M1/M2 components and projections for M3.
5. Defer M4 unless a concrete metadata need appears.
6. Build M5 from existing data with no schema change.
7. Finish with M6 UI polish after story mode is device-reviewed. Completed on
   2026-06-17.

## Next Planned Work

### N1: Country Stats Scope Preference

Status: planned.

#### Goal

Give the user control over the denominator used by country-based statistics without
changing the underlying country/territory dataset or hiding travel records.

#### Scope Options

- `UN 195`: countries counted by the user-facing UN scope.
- `UN + Kosovo + Taiwan 197`: the UN 195 scope plus Kosovo and Taiwan.
- `UN + territories`: every country and territory available in Atlas. This matches
  the current broad Atlas tracking model.

The implementation must define the exact ISO2 membership of each scope in code and
tests. Do not infer the 195/197 lists from labels at runtime.

#### Product Rules

- The preference affects country-based stats only.
- Country list, country detail, trips, stops, excursions, flights, itineraries,
  search, maps used for travel records, and backups must continue to support all
  Atlas countries and territories.
- Existing users should not lose data or see records disappear when switching scope.
- Stats should make excluded visited territories understandable, for example with a
  secondary count outside the selected scope.
- The default should preserve current released behavior unless a separate migration
  decision is made.

#### Architecture Direction

- Prefer a small domain enum such as `CountryStatsScope`.
- Store the selected scope as a preference, likely in DataStore, not Room.
- Filter only the country set consumed by stats calculations and presentation.
- Keep country state derivation centralized in `CountryStateDerivationService`;
  scope selection should not change how visited/planned/lived states are derived.
- Add focused tests for scope membership, denominators, percentages, continent
  breakdowns, and out-of-scope visited places.

#### Data Impact

- Expected: no Room migration and no backup-format change.
- If the preference is stored in DataStore, it remains local UI/settings state and
  must not affect existing backup/import compatibility.

### N2: Quick Trip Creation and Compact Cards

Status: planned.

#### Goal

Reduce friction for short/simple travel memories by letting the user create a normal
trip and its first stop in one quick flow.

#### Product Decision

Quick trips are not a new trip type. They are normal trips created through a faster
capture path and displayed compactly when their structure is simple.

#### Initial Quick-Creation Fields

- Trip name.
- Flexible trip dates.
- One location using the same location/country/coordinate model as a trip stop.

The flow should create:

1. a normal `Trip`;
2. one normal `TripStop` attached to that trip.

#### Compact Presentation Rules

- In the trip list and dashboard, a trip with exactly one stop should render as a
  compact quick-trip card.
- The compact card is a presentation choice, not proof of a different persisted data
  type.
- Opening the card should still lead to the normal trip detail surface.
- Adding more stops or richer trip structure should naturally make the trip behave
  like a full trip.

#### Architecture Direction

- Prefer a new use case/repository transaction that creates the trip and first stop
  together.
- Reuse existing trip, stop, date, country, coordinate, map, photo, stats, and backup
  models.
- Do not add a `trip_kind`, `quick_trip`, or similar persisted field for the first
  implementation unless compact-card control proves impossible without it.
- If an explicit persisted override is introduced later, it requires a Room migration,
  exported schema update, backup review, and compatibility tests.
- Photos should not be required for quick creation. If photos are added later, attach
  them through the existing stop-photo system after the stop exists.

#### Data Impact

- Expected: no new Room entity, no trip-type column, and no backup-format change.
- Quick-created records must export/import as ordinary trips and trip stops.

## Later Directions

### Maps and Statistics

- Continue improving world-map exploration using the existing offline geo foundation.
- Add interaction only where it improves personal travel understanding.
- Keep MapLibre isolated to surfaces that benefit from tile-backed context.

### Portability

- Consider selective export formats only if a concrete use case appears.
- Preserve old backup compatibility and stable identifiers.

### Optional Notifications

A future arrival reminder may prompt for actual flight times. Existing WorkManager and
notification infrastructure can support it, but it remains separate from v5 and needs
an explicit product decision.

## Roadmap Rules

- Build only the current milestone's schema.
- Prefer small, reviewable changes.
- Do not introduce a backend for core use.
- Do not add major libraries without explicit approval.
- Preserve local ownership and incomplete-data workflows.
- Keep business ordering and relationship rules out of Compose.
- Update `docs/Handoff_Prompt.md` whenever implementation status changes.
