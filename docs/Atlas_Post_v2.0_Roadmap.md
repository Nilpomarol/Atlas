# Atlas Current Roadmap

## Purpose

This roadmap starts from the implemented and device-verified baseline. It defines
the selected next product direction and its milestone boundaries. Exact operational
status remains in `docs/Handoff_Prompt.md`.

## Completed Baseline

- v2.0: countries, trips, stops, flights, itineraries, and backup/import.
- v3.0: flight API integration, airline and aircraft datasets, UTC fields, route
  maps, location suggestions, and country-tracking flags.
- v3.1: Cartographer's Ink visual redesign across primary screens.
- v3.2: stop photos, trip cover photos, complete stats surfaces, timeline, and
  navigation polish.
- v4.0: country facts, Country Info, country-detail enrichment, country-list
  sorting/filtering, and final device polish.
- Backup and portability milestone: photo-inclusive `.atlasbackup` format v3,
  legacy JSON compatibility, explicit recovery, optional Drive-compatible cloud
  backup, progress and result feedback, and three-file automatic retention.

## Selected Direction: v5 Photos and Memories

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

Status: initial read-only implementation added on 2026-06-16. Automated checks pass
and the debug APK installs/launches on device; story-specific visual review remains
pending. Implementation contract: `docs/Atlas_v5.0_Photo_Memories_M5_Spec.md`.

### Goal

Generate a read-only narrative from existing trip data. Story mode is a presentation
layer, not a second trip editor.

### Initial Format

A vertically scrolling Compose story containing only sections with useful data:

1. trip title, cover, dates, and country summary;
2. route overview;
3. outbound flight or itinerary context;
4. stop-by-stop sections with photos and notes;
5. excursion sections;
6. return travel;
7. compact trip statistics and closing summary.

### Rules

- Story order comes from existing trip, stop, excursion, itinerary, and flight order.
- Empty sections are omitted.
- Approximate dates remain approximate.
- Story mode must work without photos.
- No separate story database, manual slide editor, music, video rendering, or social
  sharing in the initial milestone.

### Data Impact

- No schema change should be required after M4.
- Story mode consumes domain projections built for galleries, timelines, maps, and
  related travel records.

## M6: Final Photo UI Polish

### Goal

Do a final user-facing polish pass across the v5 photo-memory surfaces after M1-M5 are
implemented and device-reviewed.

### Scope

- Review spacing, carousel gutters, card density, and text hierarchy across trip
  records, country memories, viewer overlays, and trip story.
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
7. Finish with M6 UI polish after story mode is device-reviewed.

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
