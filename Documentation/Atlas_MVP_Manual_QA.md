# Atlas MVP Manual QA

Use this checklist after a passing debug build. Keep the test data small and realistic so failures are easy to isolate.

## 1. Launch and Dataset

```text
[ ] App launches without crashing.
[ ] Home/dashboard opens.
[ ] Atlas tab loads countries and territories.
[ ] Spain, Iceland, Japan, Morocco, Greenland, Hong Kong, and Peru are present.
[ ] Search works by Catalan name, English/common name, ISO2, and ISO3 where visible.
```

## 2. Country Tracking

```text
[ ] Open a country detail page from the Atlas list.
[ ] Toggle wishlist on and off.
[ ] Toggle currently living on.
[ ] Toggle currently living on for a second country and confirm the first country is cleared.
[ ] Add a visit log with a year-only date.
[ ] Add a lived log with a month-year range.
[ ] Edit a log title/date.
[ ] Delete a log after confirmation.
[ ] Derived state pills on the country detail reflect the visible data.
```

## 3. Trips and Stops

```text
[ ] Create a planned trip.
[ ] Add at least three ordered stops.
[ ] Add one stop through location search.
[ ] Add one stop manually without coordinates.
[ ] Edit a stop.
[ ] Reorder stops.
[ ] Delete a stop.
[ ] Open a country used by a stop and confirm the related trip appears in history.
[ ] Tap the trip from country history and confirm back returns to the country detail.
```

## 4. Derived Country State

```text
[ ] Planned trip stop marks the country as planned.
[ ] In-progress trip stop marks the country as visited.
[ ] Completed trip stop marks the country as visited.
[ ] Unknown trip stop does not mark the country as planned or visited.
[ ] Dashboard country counts update after trip/status changes.
```

## 5. Backup and Import

```text
[ ] Export a JSON backup from Settings.
[ ] Confirm the file is created and non-empty.
[ ] Make a visible data change, such as adding a wishlist country or trip stop.
[ ] Import the exported backup.
[ ] Confirm the replacement dialog shows counts for states, logs, trips, and stops.
[ ] Confirm import restores the previous data.
[ ] Try importing an unrelated/invalid JSON file and confirm current data remains intact.
```

## 6. Persistence

```text
[ ] Close and relaunch the app.
[ ] Country states remain correct.
[ ] Logs remain present.
[ ] Trips and stop order remain present.
[ ] Backup/import actions still work after relaunch.
```

## 7. MVP Visual and Language Pass

```text
[ ] Dashboard follows the Warm Editorial Atlas direction.
[ ] Country list follows the Warm Editorial Atlas direction.
[ ] Country detail follows the Warm Editorial Atlas direction.
[ ] Trip list/detail are usable, even if final trip polish is deferred.
[ ] Settings/backup is readable and consistent enough for MVP.
[ ] Normal visible UI text is Catalan.
[ ] No obvious text overlap on the target phone size.
```

