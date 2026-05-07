# Atlas Technical Architecture v1

## 1. Purpose

This document defines the initial technical architecture for Atlas MVP v1.

Atlas is a native Android, local-first travel tracking app focused on:

- country tracking
- flight tracking
- trip tracking
- itinerary groups
- excursions
- flexible dates
- local user data
- OpenStreetMap-based maps/location services
- JSON backup/import

This architecture should support the MVP while leaving room for future features such as advanced stats, photos, flight API integration, cloud backup, and richer maps.

---

## 2. Technical Goals

The architecture should be:

- local-first
- simple enough for a personal project
- maintainable
- testable where it matters
- friendly to incremental development
- compatible with rich future visual features
- not over-engineered too early

Atlas should avoid unnecessary backend complexity. The app should be useful even without a server.

---

## 3. Recommended Tech Stack

## 3.1 Language

```text
Kotlin
```

Reason:

- first-class Android language
- strong type system
- excellent Compose support
- good coroutine support
- good serialization support

---

## 3.2 UI

```text
Jetpack Compose
```

Reason:

- modern Android UI toolkit
- good for visual custom interfaces
- easier state-driven UI
- suitable for dashboards, cards, maps, lists, and future slideshow-style screens

---

## 3.3 Database

```text
Room
```

Reason:

- stable Android local database solution
- built on SQLite
- good integration with Kotlin, Flow, and coroutines
- suitable for structured relational data like trips, flights, countries, and stops

---

## 3.4 Async/state streams

```text
Kotlin Coroutines + Flow
```

Reason:

- works naturally with Room
- useful for reactive UI updates
- good for repositories and use cases

---

## 3.5 Navigation

```text
Navigation Compose
```

Reason:

- standard Compose navigation solution
- suitable for screen-based app structure
- supports arguments for country, trip, flight, itinerary IDs

---

## 3.6 Serialization

```text
kotlinx.serialization
```

Reason:

- good Kotlin support
- useful for bundled static JSON datasets
- useful for JSON backup/import
- avoids reflection-heavy serialization

---

## 3.7 Dependency injection

Recommended initial approach:

```text
Manual dependency container
```

Possible later alternatives:

```text
Hilt
Koin
```

For MVP, a manual dependency container is acceptable because Atlas is a personal app and early architecture should avoid too much ceremony.

Example:

```text
AtlasAppContainer
- database
- repositories
- services
- dataset importers
- backup manager
- location search service
```

If the app grows significantly, migration to Hilt or Koin can be considered.

---

## 3.8 Maps

Preferred direction:

```text
OpenStreetMap-based
```

Possible Android map options to evaluate:

```text
osmdroid
MapLibre
```

MVP should not depend heavily on advanced maps at the beginning. Maps can be integrated after the data model and core flows are stable.

---

## 3.9 Location search/geocoding

Preferred direction:

```text
OpenStreetMap-based geocoding provider
```

Possible options to evaluate:

- Nominatim
- Photon
- Geoapify
- LocationIQ
- OpenCage

Important requirement:

The app should use a `LocationSearchService` abstraction so the provider can be replaced later.

---

## 4. High-Level Architecture

Atlas should use a layered architecture:

```text
UI Layer
↓
Presentation Layer
↓
Domain Layer
↓
Data Layer
↓
Local Database / Static Datasets / External Services
```

Recommended package structure:

```text
com.atlas

  app/
    AtlasApp.kt
    AtlasAppContainer.kt

  ui/
    navigation/
    screens/
    components/
    theme/

  presentation/
    country/
    flight/
    itinerary/
    trip/
    settings/

  domain/
    model/
    usecase/
    service/
    util/

  data/
    local/
      database/
      dao/
      entity/
      mapper/
    repository/
    dataset/
    backup/
    location/
    mapper/

  core/
    date/
    result/
    error/
    constants/
```

---

## 5. Layer Responsibilities

## 5.1 UI layer

Package:

```text
ui/
```

Responsibilities:

- Compose screens
- reusable visual components
- app theme
- navigation host
- UI-only state rendering
- user interaction callbacks

The UI layer should not contain business rules such as:

- country state derivation
- itinerary generated stop logic
- flexible date validation
- backup serialization rules

---

## 5.2 Presentation layer

Package:

```text
presentation/
```

Responsibilities:

- ViewModels
- screen UI state
- event handling
- calling use cases/repositories
- converting domain data into UI-friendly display models

Example:

```text
CountryDetailViewModel
TripDetailViewModel
FlightEditViewModel
ItineraryManagementViewModel
```

The presentation layer can prepare display values such as:

```text
"Visited"
"Planned"
"14-06-2023"
"Tokyo → Kamakura → Yokohama → Tokyo"
```

But it should not own core derivation rules.

---

## 5.3 Domain layer

Package:

```text
domain/
```

Responsibilities:

- core app models
- business rules
- use cases
- state derivation
- flexible date logic
- itinerary group generated stop logic
- validation rules

Important services/use cases:

```text
CountryStateDerivationService
FlexibleDateValidator
FlexibleDateFormatter
ItineraryGeneratedStopService
TripStatusService
FlightCountryTrackingService
JsonBackupUseCase
JsonImportUseCase
```

The domain layer should not know about Room entities directly.

It should use domain models, not database entities.

---

## 5.4 Data layer

Package:

```text
data/
```

Responsibilities:

- Room database
- DAOs
- entities
- repositories
- dataset import
- JSON backup/import implementation
- location search implementation
- mapping between entities and domain models

The data layer knows how data is stored and fetched.

It should expose clean repository APIs to the domain/presentation layers.

---

## 6. Data Flow

Recommended data flow for reading data:

```text
Room DAO
→ Repository
→ Use Case / Domain Service
→ ViewModel
→ Compose UI
```

Recommended data flow for user actions:

```text
Compose UI event
→ ViewModel
→ Use Case / Repository
→ DAO / Service
→ Room update
→ Flow emits new data
→ UI refreshes
```

Example: toggling wished country

```text
User taps wished toggle
→ CountryDetailViewModel.onWishedToggled()
→ UpdateCountryWishedUseCase
→ CountryRepository.updateWished(countryId, wished)
→ CountryUserStateDao.updateWished()
→ Country detail Flow emits updated state
→ UI recomposes
```

---

## 7. Local-First Data Strategy

Atlas user data should be stored in Room.

User data includes:

- country user states
- country logs
- flights
- itineraries
- itinerary groups
- trips
- trip stops
- excursions
- excursion stops
- cached places
- future photo metadata/references

Static bundled data includes:

- countries
- airports
- country stats summary
- country stat facts

Static data should be imported from bundled JSON assets into Room.

Recommended dataset assets:

```text
assets/data/countries.json
assets/data/airports.json
assets/data/country_stats_summary.json
assets/data/country_stats_facts.json
```

---

## 8. Static Dataset Import

## 8.1 Dataset import responsibilities

The app should support importing bundled static datasets into Room.

Dataset import should:

- run on first launch
- detect dataset versions
- avoid duplicating records
- allow future dataset updates
- not overwrite user data

---

## 8.2 Dataset versioning

The database should store metadata for imported datasets.

Example entity:

```text
DatasetMetadataEntity
- key
- version
- imported_at
```

Possible keys:

```text
countries
airports
country_stats_summary
country_stats_facts
```

Example:

```text
key = "countries"
version = "2026.1"
```

---

## 8.3 Static data update rule

Updating static datasets should not delete or overwrite user data.

For example:

- updating `country_stats_facts` should not affect `CountryLog`
- updating `airports` should not affect existing flights unless IDs/codes are stable
- updating `countries` should preserve country IDs/ISO codes

Stable identifiers are important.

Recommended stable country key:

```text
iso2
```

Recommended stable airport key:

```text
iata
```

Where IATA is unavailable, later fallback may use ICAO or a generated stable ID.

---

## 9. Database Architecture

The Room database should contain both static reference tables and user data tables.

Reference/static tables:

```text
CountryEntity
CountryStatsSummaryEntity
CountryStatFactEntity
AirportEntity
DatasetMetadataEntity
```

User data tables:

```text
CountryUserStateEntity
CountryLogEntity
FlightEntity
ItineraryEntity
ItineraryGroupEntity
TripEntity
TripStopEntity
ExcursionEntity
ExcursionStopEntity
PlaceEntity
PhotoEntity
```

Exact table definitions will be specified in:

```text
Atlas_Data_Model_v1.md
```

---

## 10. Domain Models vs Room Entities

Atlas should separate Room entities from domain models.

Example:

```text
CountryEntity
```

is a database representation.

```text
Country
```

is a domain representation.

This allows:

- cleaner business logic
- easier future refactoring
- better separation between persistence and app rules
- more flexible JSON backup models

Mapping should be done in mapper classes/functions.

Example package:

```text
data/local/mapper/
```

Example:

```text
fun CountryEntity.toDomain(): Country
fun Country.toEntity(): CountryEntity
```

---

## 11. Flexible Date Architecture

Flexible dates are central to Atlas and should be implemented carefully from the beginning.

Supported precisions:

```text
YEAR
MONTH
DAY
```

Recommended domain model:

```text
FlexibleDate
- year: Int
- month: Int?
- day: Int?
- precision: DatePrecision
```

Recommended range model:

```text
FlexibleDateRange
- start: FlexibleDate?
- end: FlexibleDate?
- precision: DatePrecision?
```

Rules:

- start can be null
- end can be null
- if both exist, both must share precision
- invalid dates should not be allowed
- date formatting should be centralized

Recommended service:

```text
FlexibleDateValidator
FlexibleDateFormatter
FlexibleDateSorter
```

Room storage can be column-based:

```text
start_year INTEGER NULL
start_month INTEGER NULL
start_day INTEGER NULL
end_year INTEGER NULL
end_month INTEGER NULL
end_day INTEGER NULL
date_precision TEXT NULL
```

This avoids fragile string parsing for sorting/filtering.

---

## 12. Flight Date/Time Architecture

Flights should support detailed date/time fields in the database from the start.

Recommended database fields:

```text
planned_departure_datetime optional
planned_arrival_datetime optional
actual_departure_datetime optional
actual_arrival_datetime optional
```

Important future consideration:

Flights cross time zones.

For MVP, the UI may be simplified, but the model should eventually support:

```text
local departure date/time
local arrival date/time
origin timezone
destination timezone
```

Recommended initial implementation:

- store local date/time strings or epoch values carefully
- keep timezone field optional
- avoid advanced delay/timezone stats until this is fully designed

This should be refined in the data model document.

---

## 13. Country State Derivation

Country states should not be manually stored as final truth.

They should be derived from:

- `CountryUserState`
- `CountryLog`
- trips
- trip stops
- excursions
- excursion stops
- itinerary generated stops
- solo flights

Recommended domain service:

```text
CountryStateDerivationService
```

Responsibilities:

- determine if a country is visited
- determine if a country is planned
- determine if a country is lived
- determine if a country is wished
- determine if a country is currently living
- produce a combined state summary for lists/detail pages

Possible domain result:

```text
CountryTrackingState
- visited: Boolean
- lived: Boolean
- wished: Boolean
- planned: Boolean
- currentlyLiving: Boolean
- neverVisited: Boolean
```

---

## 14. Derived State Caching

For MVP, country states can be computed dynamically.

If performance becomes an issue later, add a cache table.

Possible future cache:

```text
CountryDerivedStateCacheEntity
- country_id
- visited
- lived
- wished
- planned
- currently_living
- updated_at
```

Recommendation for MVP:

```text
Compute dynamically first.
```

Reason:

- simpler
- fewer invalidation bugs
- easier to trust during early development

---

## 15. Itinerary Generated Stop Architecture

Itinerary groups generate main trip stops when an itinerary is linked to a trip.

MVP decision:

```text
Generated stops should be stored as TripStop records.
```

Reason:

- easier to display in trip lists
- allows notes later
- allows photos later
- allows visibility/customization later
- easier JSON backup

Generated stops should have:

```text
source = itinerary_group
itinerary_group_id = ...
```

Core generated fields:

- location
- country
- coordinates
- derived date

should be updated from the itinerary group/flight data.

Editing rule:

- user should not directly edit generated core fields
- user should edit the itinerary group/flights instead

Recommended service:

```text
ItineraryGeneratedStopService
```

Responsibilities:

- determine generated stop location for each group
- create missing generated stops
- update existing generated stops when itinerary changes
- remove generated stops when itinerary is unlinked if appropriate
- preserve user-editable generated stop metadata where applicable

---

## 16. Location Search Architecture

Location search should be abstracted.

Recommended interface:

```text
interface LocationSearchService {
    suspend fun search(query: String): Result<List<LocationSearchResult>>
}
```

Possible model:

```text
LocationSearchResult
- provider
- providerPlaceId
- displayName
- countryCode
- latitude
- longitude
- rawData optional
```

When a result is selected, store it locally as `PlaceEntity`.

Manual fallback should use:

```text
provider = manual
```

Manual fallback minimum:

```text
location name
country
```

Coordinates optional.

---

## 17. Map Architecture

Map functionality should be introduced gradually.

Initial map features can be simple:

- show trip stop markers
- show excursion route markers/lines
- show flight line preview later

Advanced future map features:

- country coloring
- interactive country map
- flight route arcs
- slideshow map transitions
- offline map caching

Map-related code should be isolated.

Recommended package:

```text
ui/components/map/
data/map/
domain/map/
```

Avoid spreading map provider-specific code throughout screens.

---

## 18. JSON Backup/Import Architecture

Backup/import should be JSON only for MVP.

Recommended services:

```text
JsonBackupExporter
JsonBackupImporter
BackupValidator
BackupMigrationService
```

Backup should include:

- backup version
- created timestamp
- dataset version metadata
- user data

Backup should not need to include full static datasets.

Example top-level structure:

```json
{
  "backupVersion": 1,
  "createdAt": "2026-05-07T00:00:00Z",
  "countryDatasetVersion": "2026.1",
  "airportDatasetVersion": "2026.1",
  "countryStatsDatasetVersion": "2026.1",
  "data": {
    "countryUserStates": [],
    "countryLogs": [],
    "flights": [],
    "itineraries": [],
    "itineraryGroups": [],
    "trips": [],
    "tripStops": [],
    "excursions": [],
    "excursionStops": [],
    "places": [],
    "photos": []
  }
}
```

Backup import should validate:

- backup version
- required fields
- referenced country IDs
- referenced airport IDs
- relationship consistency

---

## 19. Error Handling

Use a simple result wrapper for operations that can fail.

Example:

```text
Result<T>
- Success<T>
- Error
```

Or Kotlin’s built-in `Result` where appropriate.

Errors should be user-friendly in the UI.

Examples:

- location search failed
- backup import failed
- invalid date range
- airport not found
- dataset import failed

Domain validation should return meaningful errors rather than crashing.

---

## 20. Testing Strategy

Atlas does not need heavy testing at the start, but important logic should be testable.

Recommended priority tests:

1. Flexible date validation.
2. Flexible date sorting/formatting.
3. Country state derivation.
4. Itinerary generated stop rules.
5. JSON backup/import validation.
6. Currently living country rule.
7. Solo flight country tracking flags.

UI tests can be postponed.

Unit tests should target domain services first.

---

## 21. Recommended Initial Package Structure

```text
com.atlas

  app/
    AtlasApplication.kt
    AtlasAppContainer.kt

  core/
    date/
      FlexibleDate.kt
      FlexibleDateRange.kt
      DatePrecision.kt
      FlexibleDateValidator.kt
      FlexibleDateFormatter.kt
    result/
      AppResult.kt
    error/
      AppError.kt

  data/
    local/
      database/
        AtlasDatabase.kt
      dao/
        CountryDao.kt
        CountryLogDao.kt
        FlightDao.kt
        ItineraryDao.kt
        TripDao.kt
      entity/
        CountryEntity.kt
        CountryUserStateEntity.kt
        CountryLogEntity.kt
        AirportEntity.kt
        FlightEntity.kt
        ItineraryEntity.kt
        ItineraryGroupEntity.kt
        TripEntity.kt
        TripStopEntity.kt
        ExcursionEntity.kt
        ExcursionStopEntity.kt
        PlaceEntity.kt
      mapper/
    dataset/
      DatasetImporter.kt
      CountryDatasetImporter.kt
      AirportDatasetImporter.kt
    backup/
      JsonBackupExporter.kt
      JsonBackupImporter.kt
    location/
      LocationSearchService.kt
      OsmLocationSearchService.kt
      ManualPlaceFactory.kt
    repository/
      CountryRepositoryImpl.kt
      FlightRepositoryImpl.kt
      ItineraryRepositoryImpl.kt
      TripRepositoryImpl.kt

  domain/
    model/
      Country.kt
      CountryTrackingState.kt
      Flight.kt
      Itinerary.kt
      ItineraryGroup.kt
      Trip.kt
      TripStop.kt
      Excursion.kt
      ExcursionStop.kt
      Place.kt
    repository/
      CountryRepository.kt
      FlightRepository.kt
      ItineraryRepository.kt
      TripRepository.kt
    service/
      CountryStateDerivationService.kt
      ItineraryGeneratedStopService.kt
      FlightCountryTrackingService.kt
    usecase/
      SetCurrentlyLivingCountryUseCase.kt
      ToggleWishedCountryUseCase.kt
      AddCountryLogUseCase.kt
      CreateTripUseCase.kt
      CreateFlightUseCase.kt

  presentation/
    country/
      CountryListViewModel.kt
      CountryDetailViewModel.kt
    flight/
      FlightListViewModel.kt
      FlightDetailViewModel.kt
      FlightEditViewModel.kt
    itinerary/
      ItineraryManagementViewModel.kt
    trip/
      TripListViewModel.kt
      TripDetailViewModel.kt
      StopEditViewModel.kt
      ExcursionEditViewModel.kt
    settings/
      BackupViewModel.kt

  ui/
    navigation/
      AtlasNavHost.kt
      Routes.kt
    screens/
      dashboard/
      countries/
      flights/
      itineraries/
      trips/
      settings/
    components/
      date/
      country/
      flight/
      trip/
      map/
    theme/
      Color.kt
      Type.kt
      Theme.kt
```

This structure can be simplified during implementation if needed.

---

## 22. First Implementation Milestones

## Milestone 0: Project foundation

Goals:

- create Android project
- set up Compose
- set up Room
- set up navigation shell
- set up app theme
- create package structure
- create static dataset import foundation

Deliverables:

- empty but runnable app
- main navigation structure
- local database created
- initial country dataset imported

---

## Milestone 1: Country tracking foundation

Goals:

- implement country list
- implement country detail
- implement wished toggle
- implement currently living toggle
- implement manual visit/lived logs
- implement country state derivation

Deliverables:

- countries can be viewed
- country can be marked wished
- one country can be currently living
- visit/lived logs can be added
- derived states appear correctly

---

## Milestone 2: Flexible dates

Goals:

- implement flexible date model
- implement flexible date input UI
- implement validation
- implement formatting
- use in country logs

Deliverables:

- year-only, month-year, full date supported
- valid ranges supported
- invalid mixed-precision ranges blocked

---

## Milestone 3: Flights foundation

Goals:

- import airport dataset
- create solo flights
- list flights
- view flight detail
- support planned/completed/unknown status
- support origin-counts-for-country-tracking toggle

Deliverables:

- solo flights can be created
- destination country derivation works
- optional origin country derivation works

---

## Milestone 4: Itineraries

Goals:

- create itinerary
- create itinerary groups
- add flights to groups
- reorder groups
- derive meaningful group stop
- avoid layovers counting as visits

Deliverables:

- grouped flights work
- layover countries are not counted
- group destination/origin rule works

---

## Milestone 5: Trips, stops, and excursions

Goals:

- create trips
- add main stops through location search
- support manual fallback
- create excursions
- add excursion stops
- link one itinerary to a trip
- generate trip stops from itinerary groups

Deliverables:

- trip detail shows main route
- excursions can be added
- generated itinerary stops appear
- country derivation from trips works

---

## Milestone 6: JSON backup/import

Goals:

- export user data as JSON
- import user data from JSON
- include backup version
- include dataset version metadata
- validate import relationships

Deliverables:

- user can back up and restore personal Atlas data

---

## 23. Key Implementation Decisions Still Open

These should be answered before or during early implementation:

1. osmdroid vs MapLibre for maps.
2. First geocoding provider.
3. Exact Room schema for flexible dates.
4. Exact Room schema for flight date/time with time zones.
5. Whether static countries/airports are inserted on first launch or prepackaged in a database.
6. Initial country dataset source.
7. Initial airport dataset size/source.
8. Exact JSON backup schema.
9. How generated itinerary stops update when source flights change.
10. How photo file references should be stored safely long-term.

---

## 24. Architecture Summary

Atlas should start with a simple but clean local-first Android architecture:

```text
Kotlin
Jetpack Compose
Room
Coroutines + Flow
Navigation Compose
kotlinx.serialization
Manual dependency container
OpenStreetMap-based maps/search
```

The most important architectural decisions are:

- separate UI, domain, and data responsibilities
- keep user data local
- import static datasets separately from user data
- use explicit trip/flight status
- implement flexible dates centrally
- derive country states through a domain service
- store generated itinerary stops as marked trip stops
- abstract location search provider
- use JSON-only backup/import

This architecture should be strong enough for MVP while remaining flexible for the richer visual and statistical features planned later.