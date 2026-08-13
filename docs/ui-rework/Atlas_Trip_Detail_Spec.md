# Trip Detail — rework specification

Status: **Proposed — awaiting approval**
Created: 2026-08-13
Phase: UI rework Phase 5, second vertical slice

The implementation plan gates this screen: "Reopen Trip Detail only after its
user-facing model and information hierarchy are deliberately specified." This is that
specification.

---

## 1. What this page is

The memory page for one travel experience.

It answers three different questions depending on when it is opened, and it must serve
all three without becoming three screens:

| When | What the user wants |
|---|---|
| Before | Where am I going, in what order, and how do I get there |
| During | Where am I now, what is next |
| After | What did I do, and what did it look like |

**The route is the spine.** It is the only element present in all three modes. Photos
are prominent when they exist and absent when they do not; the route is always there.
Everything else appears only when it has content — the same rule the Trips list uses
for cards.

## 2. What it must not become

- It does not expose the storage model. The words `Excursió`, `Itinerari`, and `Grup`
  never appear, exactly as on the Trips list.
- It is not a form. Editing is available everywhere but never the default state.
- It is not a dashboard. Statistics are context, not the subject.

## 3. Information hierarchy

Top to bottom. Sections with no content are omitted entirely, never shown empty.

### 3.1 Hero — identity

Cover photo full-bleed, with a scrim carrying:

- trip title (serif display);
- date range, flexible precision, via `formatTripPill`;
- status pill;
- back action, and an overflow for trip-level actions.

Without a cover photo the hero collapses to a compact band on paper — the same
"content earns height" rule as the list. It does **not** become a tall empty gradient.

### 3.2 The journey — the route

The heart of the page, and the section that must be got right.

An ordered vertical timeline of everything that happened, in one sequence:

- **Main stops**, numbered, in `sortOrder`.
- **Nested places** indented beneath their parent stop, marked with the `└` idiom
  established on the Trips list. Their optional `sideTripLabel` is shown when present.
- **Flight legs** inline, in **chronological position** — not appended. Visually
  distinct, labelled `VOL`, showing `Origin → Destination`.

Flight legs are **not editable as stops**. They are derived from flights, and the plan
already warns that generated stops must not look manually editable unless they are.
They read as a different kind of row: no drag handle, no edit affordance; tapping opens
the flight, not a stop editor.

**Placement rule.** `ItineraryGeneratedStopService` assigns `sortOrder = 10_000 + index`,
which appends legs after every manual stop (analysis §3.3). Trip Detail must not render
that raw order. It orders the timeline by the best available date — stop date, else
flight departure, else `sortOrder` — so a mid-trip flight sits where it happened. Manual
reordering still wins where the user has expressed one.

### 3.3 Your record — photos

The existing `photoGallery` (`TripPhotoGalleryUiState`), which already walks main stops
and their nested places in narrative order. Grouped by place, opening the shared
full-screen viewer.

Absent entirely when the trip has no photos.

### 3.4 Where — map

The trip route on the shared rework map surface, main route as a line and nested places
branching from their parent, matching how `TripMapPreview` already draws them.

Absent when no stop has coordinates.

### 3.5 Context — the quiet band

Countries touched (flags plus name or count, as on the list), stop and side-trip counts,
day count when dates carry day precision. Never invented: no distance, per the
list's standing rule.

### 3.6 Notes

Trip notes, shown as written, when present.

### 3.7 Story

Entry point to the existing read-only story slideshow, when the trip has enough content
to make one. It already exists at `trips/{tripId}/story` and is reused, not rebuilt.

## 4. Editing — and the gap this closes

Legacy trip detail's side-trip editing was removed when excursions collapsed. **This
screen restores it.** Until it ships, nested places cannot be created anywhere.

Two add actions, phrased as places rather than as record types:

| Action | Result |
|---|---|
| `Afegeix una parada` | a main-route stop, `parentStopId = null` |
| `Afegeix una sortida des d'aquí` (from a stop) | a nested stop, `parentStopId` = that stop |

The user never picks a type in the abstract; they either add to the route, or add a
place visited *from* somewhere they already were. This is the whole point of the model
collapse and it must not be undone by a type picker.

Also required:

- edit and delete any stop, main or nested;
- reorder main stops;
- edit trip title, dates, status, notes;
- set or clear the cover photo;
- delete the trip.

Deleting a stop deletes the places nested under it, and their photos. That cascade lives
in `DeleteTripStopUseCase` and `TripRepositoryImpl.deleteTripStop`; the UI must warn
when the stop being deleted has children.

### 4.1 Nesting depth

One level only. A nested place offers no "add a place from here" action.

## 5. Flights on this page

Flights appear as legs of **this trip**. There is no link or unlink action, no itinerary
picker, and no itinerary name.

**Requirement inherited from the itinerary analysis §5:** the one-itinerary-per-trip
rule is currently enforced only by the UI picker being removed here and by import
repair. Whatever write path attaches flights to a trip in this slice **must carry the
guard in the domain**, so a second attachment cannot be created by any caller.

If attaching flights to a trip requires an itinerary row underneath, that row is created
implicitly and never named. The user attaches flights to a trip.

## 6. States

| State | Behaviour |
|---|---|
| Loading | Skeleton hero and route; no flash of empty state |
| Full | All sections |
| Route only | Hero band, route, context. No photo, map or story sections |
| Title only | Hero band, an invitation to add the first place, nothing else |
| Missing trip | A single card: the trip no longer exists, with a way back |
| Cover file missing | Deterministic gradient, as on the list |

## 7. Data contract

Everything comes from `TripDetailViewModel` / `TripDetailUiState`, which already
carries `trip`, `stops` (main and nested), `countries`, `tripStopPhotoMap`, and
`photoGallery`.

Presentation work this slice needs:

- ordering that interleaves flight legs chronologically rather than by raw `sortOrder`;
- main and nested stop counts, as added for the list;
- flight legs projected for display (endpoints, label, date) without exposing groups.

No schema change. No backup change.

## 8. Out of scope

- Removing the `Itinerary` container — deferred to Phase 6, per the itinerary analysis.
- Stop-level progress within an in-progress trip — product spec §7.2 leaves all stops
  counted; unchanged here.
- Trip creation. The rework still cannot create a trip: `NOU VIATGE` and the `Registra`
  capture choices are inert. That is its own slice and should follow immediately.
