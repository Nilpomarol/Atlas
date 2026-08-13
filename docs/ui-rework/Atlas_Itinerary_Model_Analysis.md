# Itinerary model — read-only analysis

Status: **Analysis complete, recommendation pending decision**
Created: 2026-08-13
Purpose: unblock the rework Trip Detail spec, which cannot be written until it is
settled how flights attach to a trip.

---

## 1. How flights reach a trip today

```text
Trip ──(0..1, nullable trip_id on itineraries)── Itinerary
                                                    │
                                                    └── ItineraryGroup (a "leg")
                                                            └── Flight
Flight (solo, itinerary_group_id = null)
```

Plus a derived side effect: each group generates a pseudo-stop into the trip's route,
stored as a real `trip_stops` row with `source = ITINERARY_GROUP`, a deterministic id
`itinerary-group-<groupId>`, and a `display_title`.

Creation flow in practice:

1. Itineraries are always created **standalone** from the itinerary list —
   `createItineraryUseCase(title = "", notes = null)`, `trip_id` null.
2. They are linked to a trip afterwards, from either side.
3. Linking triggers `SyncGeneratedTripStopsForItineraryUseCase`, which writes the
   generated stops.

## 2. What each layer is actually for

**Groups earn their place.** A group is the only way to express a layover. Solo
flights cannot: `Barcelona → Doha → Tokyo` recorded as two solo flights marks Qatar
visited, because a completed solo flight counts its destination by default. The group's
endpoint rule (`ItineraryGeneratedStopService.selectedAirportIdForGroup`, mirrored in
`CountryStateDerivationService.deriveItineraryGroupCountries`) is what suppresses the
layover. That is real, load-bearing function.

**The `Itinerary` container does not.** Its entire user-facing content is a title and
optional notes. Its only structural jobs are to hold groups and to carry the single
nullable `trip_id`. The product spec already limits a trip to zero or one itinerary
(§9.1), which means an itinerary linked to a trip is simply "this trip's flights".

**Standalone itineraries are functional, not decorative.** Country derivation runs over
`observeAllGroups()` regardless of trip link, so a standalone itinerary's flights count
for tracking. A standalone itinerary is therefore "grouped flights not tied to a
recorded trip" — the only way to record a layover-correct journey without creating a
trip.

## 3. Defects found

### 3.1 A trip can hold more than one itinerary — reachable from the UI

`ItineraryDetailViewModel` offers **every** trip when linking:

```kotlin
availableTrips = trips        // no filter for trips that already have an itinerary
```

`onLinkTrip` writes `itinerary.copy(tripId = trip.id)` with no check. The trip side is
guarded (`TripDetailViewModel` offers only itineraries with a null `tripId`), but the
itinerary side is not, and nothing in the schema enforces uniqueness.

Consequences when it happens:

- both itineraries generate stops into the same trip, and
  `replaceGeneratedItineraryGroupStops` only replaces stops for the group ids it is
  given, so legs from both coexist in the route;
- `TripDetailViewModel` resolves the link with `firstOrNull`, so the trip page shows
  one itinerary and silently hides the other;
- the trip's route therefore contains flight legs the trip page cannot explain.

The spec rule "a trip can have zero or one linked itinerary" is unenforced at every
level: no unique index, no domain validation, no UI guard on the itinerary side.

### 3.2 `ItineraryGroup.status` is read three different ways

The column is nullable and its meaning is not agreed:

| Reader | Behaviour |
|---|---|
| `CountryStateDerivationService.effectiveStatus` | stored value wins, falls back to flights |
| `DashboardViewModel` | ignores the stored value, derives from flights |
| `FlightListViewModel` | merges the stored value **and** flight statuses |

So one group can report different statuses on different screens. This is the same
"status in three places" problem as `Trip.status` / `ItineraryGroup.status` /
`Flight.status`, but here it is a single field disagreeing with itself.

### 3.3 Generated flight legs are initially placed after every manual stop

`ItineraryGeneratedStopService` assigns `sortOrder = 10_000 + index`. A flight taken
mid-trip therefore appears at the end of the route on first generation. It is
reorderable afterwards — `replaceGeneratedItineraryGroupStops` preserves an existing
`sort_order` across regeneration — but the default placement is chronologically wrong
whenever a trip does not simply end with a flight home.

### 3.4 Fixed already

Itineraries stranded by a deleted trip, which also made the next backup unimportable.
Fixed at the delete path, in migration 25 → 26, and in backup import. See
`docs/README.md`.

## 4. Options

### Option A — keep the model, fix the defects

Guard the itinerary-side link, agree one meaning for group status, improve initial
sort placement. No migration, no backup change.

### Option B — collapse `Itinerary`, keep groups

Move `trip_id` onto the group, delete the `itineraries` table. Groups become legs that
optionally belong to a trip; standalone itineraries become legs with no trip.

- Removes the dangling-link and multiple-itinerary defect classes structurally.
- Costs a migration, backup v5, and rework across the flights and itinerary screens.
- Loses the itinerary title and notes unless relocated, the same lossy problem
  `side_trip_label` solved for excursions.

### Option C — collapse groups too

Flights attach to a trip directly with a layover flag per flight. Rejected: it would
re-derive the endpoint rule per flight and lose the clean expression of a leg.

## 5. Recommendation

**Option A now; revisit Option B at Phase 6.**

The itinerary layer differs from excursions in the one respect that made excursions
worth a migration. Excursions forced the user to *classify* every place — a modelling
decision, made repeatedly, that changed nothing. The itinerary container asks nothing
of the user in the reworked UI, because **the rework has no itinerary destination at
all**: primary navigation is fixed at `Inici · Països · Viatges · Vols · Progrés`. The
concept is already invisible there.

So the itinerary is a maintenance burden, not a comprehension burden. That makes it
worth removing eventually, but not worth a second released-data migration immediately
after the trip-model collapse — especially since the screens that render it are legacy
and Phase 6 deletes them. Removing the table is far cheaper once nothing renders it.

The correctness defects in §3.1 and §3.2 are worth fixing now regardless of which
option is eventually taken; neither needs a schema change.

## 6. Consequence for the Trip Detail spec

Trip Detail treats flights as **legs of this trip**, never as an itinerary:

- the word `Itinerari` does not appear, matching the existing vocabulary ban on
  `Excursió` in `docs/ui-rework/trips-list-image-brief.md`;
- linking and unlinking an itinerary is not a user action in the rework — a trip's
  flights are simply its flights, and the container is created implicitly if the data
  layer still needs one;
- generated legs appear in the route in chronological position, not appended;
- a leg shows its endpoints and suppresses layovers, which is the group's real job.

This is the same approach taken for nested places: the UI does not expose the model's
structure, which keeps the storage question open without leaking it to the user.
