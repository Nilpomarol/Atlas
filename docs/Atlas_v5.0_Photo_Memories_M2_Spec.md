# Atlas v5.0 Photo Memories — M2 Full-Screen Viewer

## Purpose

M2 provides one reusable full-screen viewer for stop photos and the ordered trip
gallery sequence. It preserves travel context while keeping all mutations routed
through the existing photo use cases.

## Scope

- Open the exact image tapped in the trip gallery.
- Swipe through the M1 narrative sequence.
- Preserve the selected photo by stable photo ID across recomposition.
- Show position, location, stop/excursion context, and optional date.
- Support pinch zoom, double-tap zoom, pan, and flings through Telephoto.
- Open the owning trip stop or excursion stop from the viewer.
- Set or clear the trip cover.
- Delete with confirmation and select a valid neighboring photo.
- Reuse the same viewer from stop detail.

## Architecture

- `TripPhotoGalleryUiState.viewerItems` flattens the tested M1 groups without
  introducing a second ordering rule.
- Viewer selection uses `StopPhoto.id`; file paths and serialized photo objects are
  not navigation state.
- `PhotoViewerDialog` is a small pager/overlay component that receives reactive
  viewer items.
- Image gestures and large-image rendering are delegated to Telephoto 0.13.0
  (`zoomable-image-coil`). Atlas does not own custom zoom math or transformed image
  rendering.
- Telephoto is pinned to the stable Kotlin 1.9 / Coil 2 compatible line so M2 does
  not force a Kotlin, Compose, or Coil upgrade.
- Cover changes continue through `SetTripCoverPhotoUseCase`.
- Deletion continues through `DeleteStopPhotoUseCase`.

## Data Safety

M2 adds no Room migration, backup-format change, file move, or new media directory.
The viewer does not write photo state directly.

## Acceptance Criteria

- Viewer order exactly matches gallery order.
- The tapped image opens first.
- Cover and delete actions update the reactive gallery.
- Deleting chooses the next item, then the previous item, or closes when empty.
- Missing files show a stable error state.
- Stop detail and trip gallery use the same viewer implementation.
- Device review confirms paging, zoom, pan, rotation, and system-inset behavior.
