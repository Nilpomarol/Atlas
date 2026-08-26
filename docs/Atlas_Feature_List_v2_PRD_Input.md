# Atlas — Feature List (input for v2 PRD)

Capability-level list: what the user can do or see, not which fields/screens implement it. For use as raw material when drafting the next version's PRD.

## Countries & Territories
- Comprehensive world coverage, including territories and disputed/special regions, not just sovereign states
- Browse and search all countries/territories, sortable by key indicators
- Mark a country as wished (want to go)
- Mark a country as currently lived in, with history of past places lived
- Log a visit or a period of living somewhere, independent of trip data
- Automatic status derivation for every country (visited, wished, planned, lived, currently living) from actual travel activity
- Country detail page combining personal tracking info with curated reference information about the place
- Deep curated country knowledge base (geography, demographics, economy, health, culture, practical travel info, etc.) with global comparison/ranking
- Visual/photographic representation of each country
- Currency reference/conversion for a country
- Geographic context (location, neighboring countries) for a country
- Personal memories (photos) automatically surfaced per country from past trips there

## Trips
- Record a trip with minimal effort (just a name), with all detail optional
- Track trip progress state (upcoming, ongoing, completed)
- Flexible trip timing, supporting partial/approximate dates
- Attach free-form notes and a cover image to a trip
- Build a trip as an ordered sequence of stops/places visited
- Represent side excursions within a stop without them being full trips
- Reorder a trip's stops
- Fast-path creation for simple single-destination trips
- Browse and search trips, with visual emphasis on the current one
- Consolidated trip overview: places, related flights, countries touched, chronology, photos
- Visual map of a trip's route
- Chronological view of everything that happened during a trip
- Photo gallery for a trip, organized by the order places were visited
- Auto-generated shareable/recap presentation of a trip (route, stops, flights, photos, highlights)

## Flights
- Record a flight with minimal effort (just origin and destination), with all detail optional
- Track flight status (upcoming, ongoing, completed)
- Track both local and universal flight times, planned and actual
- Attach airline, flight number, and aircraft information to a flight
- Look up flight details automatically instead of entering them manually
- Look up aircraft details from its registration
- Automatic derived insights per flight (duration, delay, distance covered)
- Control over whether a flight's endpoints count toward country-visited tracking
- Browse and search flights, distinguishing standalone flights from those part of a larger journey
- Visual representation of a flight's route

## Itineraries
- Group related flights together as a single journey, optionally tied to a trip
- Organize a journey into logical legs/segments
- Reorder flights and legs within a journey
- Correct handling of layovers so connections don't get miscounted as separate country visits
- Automatic creation of trip stops from a linked journey's flight legs

## Location Search & Places
- Look up a place online when adding it to a trip
- Enter a place manually when lookup isn't available or wanted
- Reliable offline reuse of previously chosen places

## Maps
- Visual world map as a home/overview element
- Visual map of a country and its surroundings
- Visual map of a trip's route
- Visual representation of flight routes
- Map-based view of overall travel activity with selectable layers (flights, stops, visited/planned countries)
- Fully offline map rendering, not dependent on network tiles

## Statistics
- Personal travel statistics dashboard across countries, trips, and flights
- Choice of which reference country set is used when calculating coverage stats
- Progress/achievement recognition for travel milestones
- Visualization of travel activity over time

## Photos & Memories
- Attach photos to specific places within a trip
- Manage photos (add, remove, rotate, reorder)
- Set a trip's cover photo
- Immersive full-screen photo viewing with gesture navigation through a trip's photos
- Browse a trip's photos organized by place
- Automatically surfaced photo-based memories per country
- Auto-generated visual recap/story of a trip

## Notes
- Attach free-form personal notes to trips, places within a trip, and country visit/living records

## Timeline
- Unified chronological history across all trips, places, and flights

## Backup & Import
- Export and import all personal data and photos as a single portable file
- Compatibility with data exported by older versions of the app
- Preview and validation before committing an import
- Optional automatic backup to the user's own cloud storage
- Control over automatic backup (retention, pause/resume, run now, disconnect) with status feedback
- Safe compatibility checking between app versions and backup data

## Settings / Integrations
- Manage credentials for optional external lookups (flights, photos)
- Choose the reference country set used for stats
- Configure backup and cloud backup behavior
- Visibility into current data/dataset version

## Cross-cutting behavior
- Flexible/approximate dates supported everywhere, not just exact dates
- Consistent status model (upcoming/ongoing/completed/unknown) with manual override over automatic inference
- Catalan-first user experience
- Local-first data ownership; external services are optional enrichment, never required

---

## Planned but not yet built (as of current codebase)
- Manually marking a country as "planned" independent of trips/flights
- Progress/status tracking at the individual stop level, not just the trip level
- Warnings when a stop's dates fall outside its trip's date range
- Richer automatic flight status updates (e.g. live delay detection)
- Visual airline/aircraft branding and specs
- More interactive world map (country coloring, route animation, offline map caching)
- Exporting data in spreadsheet-friendly format
- Optional notifications tied to flight times
- Optional richer photo metadata (captions, taken-date, favorites)

## Legacy concepts not to carry forward as-is
- Treating "quick trip" as a stored data type rather than a simple creation shortcut
- Modeling side excursions as a separate entity type rather than nested places
- Relying on OS-level backup instead of the app's own explicit backup
