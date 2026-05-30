# Atlas Technical Architecture

## 0. Document Purpose

This document defines the technical architecture for **Atlas**, a local-first native Android travel tracking app.

It aligns with the current project document structure:

```text
Atlas_Product_Specification.md
Atlas_MVP_Specification.md
Atlas_v2.0_Specification.md

Atlas_Data_Model.md
Atlas_MVP_Data_Model.md
Atlas_v2.0_Data_Model.md
```

This is a **version-aware architecture document**:

```text
MVP  -> countries/territories, logs, simple trips, trip stops, JSON backup/import
v2.0 -> airports, flights, itineraries, generated stops, expanded backup/import
Later -> excursions, photos, advanced stats, richer maps, story mode
```

The goal is to keep the architecture clean enough for long-term growth while avoiding over-engineering the MVP.

---

## 1. Architecture Goals

Atlas should be:

```text
local-first
native Android
simple enough for a personal project
incrementally buildable
testable where correctness matters
safe for long-term personal data
friendly to future maps, stats, flights, photos, and story mode
```

The app should avoid backend complexity unless a future feature clearly requires it.

Atlas should work without:

```text
accounts
cloud sync
paid backend services
mandatory online APIs
```

External services may be used for:

```text
location search
map tiles
future flight lookup
future dataset updates
```

but personal user data should remain stored locally.

---

## 2. Recommended Tech Stack

### 2.1 Language

```text
Kotlin
```

Reasons:

- first-class Android language
- strong type system
- good Compose support
- good coroutine support
- good serialization support

---

### 2.2 UI

```text
Jetpack Compose
```

Reasons:

- modern Android UI toolkit
- state-driven UI
- good for lists, cards, dashboards, forms, maps, and visual travel pages
- suitable for future story/slideshow-style screens

---

### 2.3 Database

```text
Room
```

Reasons:

- stable Android SQLite abstraction
- good Flow/coroutine integration
- suitable for relational data such as countries, trips, stops, flights, and itineraries
- supports migrations between MVP and v2.0

---

### 2.4 Async and Reactive State

```text
Kotlin Coroutines + Flow
```

Uses:

- Room observable queries
- repository streams
- ViewModel state
- background imports/exports
- location search calls

---

### 2.5 Navigation

```text
Navigation Compose
```

Uses:

- dashboard
- countries list/detail
- trips list/detail
- settings/backup
- later flights/itineraries

---

### 2.6 Serialization

```text
kotlinx.serialization
```

Uses:

- bundled JSON datasets
- JSON backup/export
- JSON import
- future backup migrations

---

### 2.7 Dependency Injection

Recommended initial approach:

```text
Manual dependency container
```

Example:

```text
AtlasAppContainer
- database
- DAOs
- repositories
- use cases
- services
- dataset importers
- backup manager
- location search service
```

Reasons:

- less ceremony for a personal project
- easier to understand while learning Android
- good enough for MVP and early v2.0

Possible future alternatives:

```text
Hilt
Koin
```

Migration to a DI framework can happen later if object creation becomes noisy.

---

### 2.8 Maps

Preferred direction:

```text
OpenStreetMap-based
```

Candidates to evaluate:

```text
osmdroid
MapLibre
```

Architecture rule:

```text
Map-provider-specific code should be isolated.
```

Do not let screens directly depend on a specific map provider’s API unless the wrapper is deliberately small.

---

### 2.9 Location Search / Geocoding

Preferred direction:

```text
OpenStreetMap-based geocoding
```

Possible providers to evaluate:

```text
Nominatim
Photon
Geoapify
LocationIQ
OpenCage
```

Atlas should use an abstraction:

```kotlin
interface LocationSearchService {
    suspend fun search(query: String): AppResult<List<LocationSearchResult>>
}
```

This keeps the provider replaceable.

---

## 3. High-Level Architecture

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
Room / Static Datasets / External Services
```

The goal is not to create excessive ceremony, but to prevent business rules from leaking into UI screens or database classes.

---

## 4. Layer Responsibilities

## 4.1 UI Layer

Package:

```text
ui/
```

Responsibilities:

```text
Compose screens
reusable UI components
theme
navigation host
visual state rendering
user interaction callbacks
```

The UI layer should not own rules such as:

```text
country state derivation
flexible date validation
currently living exclusivity
JSON backup/import rules
itinerary generated stop logic
```

Example components:

```text
CountryStateChip
FlexibleDateField
TripStatusSelector
TripStopList
BackupImportDialog
```

---

## 4.2 Presentation Layer

Package:

```text
presentation/
```

Responsibilities:

```text
ViewModels
screen state
event handling
calling use cases
mapping domain models to UI state
loading/error/empty states
```

MVP examples:

```text
DashboardViewModel
CountryListViewModel
CountryDetailViewModel
TripListViewModel
TripDetailViewModel
StopEditViewModel
BackupViewModel
```

v2.0 additions:

```text
FlightListViewModel
FlightDetailViewModel
FlightEditViewModel
ItineraryListViewModel
ItineraryDetailViewModel
```

Presentation may format display values, but it should not own domain truth.

Acceptable:

```text
display "Visited" instead of VISITED
format "14-06-2023"
build UI state for a country card
```

Not acceptable:

```text
deciding whether a country is visited
deciding whether a layover counts
validating flexible date precision rules
```

---

## 4.3 Domain Layer

Package:

```text
domain/
```

Responsibilities:

```text
domain models
business rules
use cases
validation
derivation logic
status logic
backup/import rules at a conceptual level
```

Important MVP domain services/use cases:

```text
CountryStateDerivationService
FlexibleDateValidator
FlexibleDateFormatter
FlexibleDateSorter
SetCurrentlyLivingCountryUseCase
ToggleWishedCountryUseCase
AddCountryLogUseCase
CreateTripUseCase
UpdateTripStatusUseCase
CreateTripStopUseCase
ReorderTripStopsUseCase
JsonBackupExportUseCase
JsonBackupImportUseCase
```

v2.0 additions:

```text
AirportSearchService
CreateFlightUseCase
FlightCountryTrackingService
CreateItineraryUseCase
ItineraryGeneratedStopService
ItineraryGroupDerivationService
LinkItineraryToTripUseCase
```

The domain layer should not depend directly on Room entities. It should use domain models such as:

```text
Country
CountryTrackingState
Trip
TripStop
FlexibleDateRange
```

---

## 4.4 Data Layer

Package:

```text
data/
```

Responsibilities:

```text
Room database
DAOs
entities
repository implementations
dataset import
JSON backup/import implementation
location search implementation
mapping between entities and domain models
```

The data layer knows how information is stored or fetched. It exposes clean repository APIs to the domain and presentation layers.

MVP examples:

```text
CountryRepositoryImpl
TripRepositoryImpl
BackupRepositoryImpl
LocationRepositoryImpl
```

v2.0 additions:

```text
AirportRepositoryImpl
FlightRepositoryImpl
ItineraryRepositoryImpl
```

---

## 5. Recommended Package Structure

Recommended initial structure:

```text
com.atlas

  app/
    AtlasApplication.kt
    AtlasAppContainer.kt

  core/
    date/
    result/
    error/
    constants/
    dispatcher/

  data/
    local/
      database/
      dao/
      entity/
      mapper/
      migration/
    dataset/
    backup/
    location/
    repository/

  domain/
    model/
    repository/
    service/
    usecase/
    validation/

  presentation/
    dashboard/
    country/
    trip/
    settings/

  ui/
    navigation/
    screens/
    components/
    theme/
```

v2.0 can add:

```text
presentation/
  flight/
  itinerary/

ui/screens/
  flights/
  itineraries/

data/repository/
  FlightRepositoryImpl.kt
  ItineraryRepositoryImpl.kt
  AirportRepositoryImpl.kt

domain/service/
  FlightCountryTrackingService.kt
  ItineraryGeneratedStopService.kt
```

Avoid creating all v2.0 packages during MVP unless they are actually used.

---

## 6. Data Flow

### 6.1 Read Flow

Recommended:

```text
Room DAO
→ Repository
→ Use Case / Domain Service
→ ViewModel
→ Compose UI
```

Example:

```text
CountryDao.observeCountries()
→ CountryRepository.observeCountries()
→ GetCountryListUseCase
→ CountryListViewModel
→ CountryListScreen
```

---

### 6.2 Write Flow

Recommended:

```text
Compose UI event
→ ViewModel
→ Use Case
→ Repository
→ DAO / Service
→ Room update
→ Flow emits updated data
→ UI recomposes
```

Example: toggling wished

```text
User taps wished toggle
→ CountryDetailViewModel.onWishedToggled()
→ ToggleWishedCountryUseCase
→ CountryRepository.updateWished()
→ CountryUserStateDao.upsert()
→ Country detail Flow emits updated state
→ UI recomposes
```

---

## 7. Local-First Data Strategy

Atlas stores user data locally in Room.

MVP user data:

```text
country user states
country logs
trips
trip stops
places optional
```

v2.0 user data:

```text
flights
itineraries
itinerary groups
generated trip stops
```

Future user data:

```text
excursions
excursion stops
photos
photo associations
```

Static bundled data:

```text
countries/territories
airports from v2.0
country stats future
airlines future
aircraft types future
```

Static data should be imported from bundled JSON assets or a prepackaged database.

---

## 8. Static Dataset Import Architecture

Dataset import should:

```text
run on first launch or app setup
detect dataset version
avoid duplicating records
preserve user data
support future updates
```

MVP required dataset:

```text
assets/data/countries.json
```

MVP metadata:

```text
DatasetMetadataEntity(key = "countries")
```

v2.0 adds:

```text
assets/data/airports.json
DatasetMetadataEntity(key = "airports")
```

Possible later datasets:

```text
country_stats_summary.json
country_stats_facts.json
airlines.json
aircraft_types.json
country_polygons.json
```

Updating static datasets must not overwrite user data.

Examples:

```text
updating countries must preserve country ISO codes used by user logs
updating airports must preserve airport IDs used by flights
updating country stats must not affect user country tracking
```

Stable identifiers are mandatory.

Recommended keys:

```text
Country/territory: iso2 or stable custom ID
Airport: IATA where possible
```

---

## 9. Room Database Architecture

The database should be versioned by app release.

### 9.1 MVP Tables

Required MVP tables:

```text
CountryEntity
DatasetMetadataEntity
CountryUserStateEntity
CountryLogEntity
TripEntity
TripStopEntity
```

Optional MVP table:

```text
PlaceEntity
```

### 9.2 v2.0 Tables

v2.0 adds:

```text
AirportEntity
FlightEntity
ItineraryEntity
ItineraryGroupEntity
```

v2.0 modifies:

```text
TripStopEntity
```

with fields such as:

```text
source
itinerary_group_id
is_visible
display_title optional
```

### 9.3 Later Tables

Later versions may add:

```text
ExcursionEntity
ExcursionStopEntity
PhotoEntity
photo join tables
CountryStatsSummaryEntity
CountryStatFactEntity
AirlineEntity
AircraftTypeEntity
```

---

## 10. Domain Models vs Room Entities

Atlas should separate persistence models from domain models.

Example:

```text
CountryEntity
```

is a Room database entity.

```text
Country
```

is a domain model.

Benefits:

```text
business logic is cleaner
database schema can change without rewriting domain logic
backup models can differ from database entities
UI models remain simpler
testing is easier
```

Mapping should be centralized.

Recommended package:

```text
data/local/mapper/
```

Example:

```kotlin
fun CountryEntity.toDomain(): Country
fun Country.toEntity(): CountryEntity
```

Do not expose Room entities directly to the UI.

---

## 11. Flexible Date Architecture

Flexible dates are central to Atlas.

Supported precisions:

```text
YEAR
MONTH
DAY
```

Recommended domain model:

```kotlin
data class FlexibleDate(
    val year: Int,
    val month: Int?,
    val day: Int?,
    val precision: DatePrecision,
)
```

Recommended range model:

```kotlin
data class FlexibleDateRange(
    val start: FlexibleDate?,
    val end: FlexibleDate?,
    val precision: DatePrecision?,
)
```

Rules:

```text
start can be null
end can be null
if both exist, both must share precision
invalid calendar dates are not allowed
display must preserve precision
sorting may use approximation
```

Recommended services:

```text
FlexibleDateValidator
FlexibleDateFormatter
FlexibleDateSorter
```

Room storage should use structured columns:

```text
start_year
start_month
start_day
end_year
end_month
end_day
date_precision
```

Do not store flexible dates only as formatted strings.

---

## 12. Status Architecture

Supported travel statuses:

```text
PLANNED
IN_PROGRESS
COMPLETED
UNKNOWN
```

MVP:

```text
Trip.status
```

v2.0:

```text
Flight.status
ItineraryGroup.status optional
```

Recommended status-source model:

```text
status_source: MANUAL | INFERRED
```

MVP minimum:

```text
manual status only
```

Future improvement:

```text
dates suggest status
user accepts or overrides
```

Rule:

```text
Status is source of truth.
Dates may assist but should not unexpectedly override manual status.
```

---

## 13. Country/Territory State Derivation Architecture

Country/territory state derivation should be centralized.

Recommended service:

```text
CountryStateDerivationService
```

Possible output:

```kotlin
data class CountryTrackingState(
    val wished: Boolean,
    val currentlyLiving: Boolean,
    val lived: Boolean,
    val visited: Boolean,
    val planned: Boolean,
    val neverVisited: Boolean,
)
```

MVP inputs:

```text
CountryUserState
CountryLog
Trip
TripStop
```

v2.0 additional inputs:

```text
Solo Flight
ItineraryGroup
Generated TripStop
```

Future inputs:

```text
ExcursionStop
```

Rules should not be duplicated in ViewModels or SQL queries unless optimized later.

---

## 14. Derived State Caching

MVP recommendation:

```text
compute dynamically first
```

Reason:

```text
simpler
fewer invalidation bugs
easier to test
```

Future cache table if needed:

```text
CountryDerivedStateCacheEntity
- country_iso2
- wished
- currently_living
- lived
- visited
- planned
- updated_at
```

Only introduce this if performance becomes a real issue.

---

## 15. Location Search Architecture

Location search should be abstracted behind an interface.

Recommended model:

```kotlin
data class LocationSearchResult(
    val provider: PlaceProvider,
    val providerPlaceId: String?,
    val displayName: String,
    val countryCode: String?,
    val latitude: Double?,
    val longitude: Double?,
    val rawData: String?,
)
```

Interface:

```kotlin
interface LocationSearchService {
    suspend fun search(query: String): AppResult<List<LocationSearchResult>>
}
```

Manual fallback should exist independently of online search.

Manual fallback minimum:

```text
location name
country/territory
```

If `PlaceEntity` is used, manual fallback creates:

```text
provider = MANUAL
latitude = null
longitude = null
```

Important:

```text
Location search failure must not block trip stop creation.
```

---

## 16. Map Architecture

Maps should be introduced gradually.

MVP map support:

```text
optional simple trip stop preview
optional country location preview
```

v2.0 map support:

```text
flight route preview
itinerary route preview
trip map with generated stops
```

Future map support:

```text
interactive world map
country polygon coloring
flight arcs
offline map caching
story mode map transitions
```

Architecture rule:

```text
Map provider code should be isolated.
```

Suggested packages:

```text
ui/components/map/
domain/map/
data/map/
```

Potential abstraction:

```kotlin
data class MapMarker(...)
data class MapRoute(...)
data class MapCameraState(...)
```

Screens should pass simple map models rather than depending directly on provider-specific APIs wherever practical.

---

## 17. JSON Backup and Import Architecture

Backup/import is core because Atlas is local-first.

Recommended services:

```text
JsonBackupExporter
JsonBackupImporter
BackupValidator
BackupMigrationService
```

### 17.1 MVP Backup

MVP backup includes:

```text
backupVersion = 1
countryDatasetVersion
countryUserStates
countryLogs
trips
tripStops
places optional
```

### 17.2 v2.0 Backup

v2.0 backup adds:

```text
backupVersion = 2
airportDatasetVersion
flights
itineraries
itineraryGroups
generated stops as tripStops if stored
```

### 17.3 Import Strategy

MVP recommended import strategy:

```text
replace all user data after confirmation
```

Reason:

```text
simpler
avoids merge conflicts
safer for first version
```

Future import options:

```text
merge
selective import
conflict resolution
airport remapping
country remapping
```

### 17.4 Backup Validation

Backup import should validate:

```text
backup version
required fields
country references
airport references in v2.0
trip references
stop references
itinerary/group references
dataset compatibility
```

Failed import should not partially destroy user data.

Recommended approach:

```text
validate first
then import in a database transaction
```

---

## 18. Error Handling

Use a simple application result type.

Example:

```kotlin
sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>
}
```

Possible error categories:

```text
ValidationError
DatabaseError
DatasetImportError
LocationSearchError
BackupExportError
BackupImportError
NotFoundError
UnknownError
```

User-facing messages should be clear and non-technical.

Examples:

```text
The date range is invalid.
The backup file could not be imported.
The selected country no longer exists in the current dataset.
Location search failed. You can add the stop manually.
```

---

## 19. Validation Architecture

Validation should live in domain services/use cases, not directly in UI.

Important validations:

```text
flexible date precision rules
currently living exclusivity
trip title not blank
trip stop country required
trip stop location name required
backup schema validity
airport exists for flight in v2.0
itinerary group contains ordered flights in v2.0
```

UI should show validation errors, not define the business rule.

---

## 20. Testing Strategy

Atlas does not need heavy UI testing at first, but correctness-critical logic should be unit tested.

### 20.1 MVP Test Priorities

```text
FlexibleDateValidator
FlexibleDateFormatter
CountryStateDerivationService
SetCurrentlyLivingCountryUseCase
Trip stop ordering
JSON backup export/import
Backup validation
```

Important MVP cases:

```text
currently living implies lived and visited
only one country can be currently living
visit log marks country visited
lived log marks country lived and visited
planned trip stop marks country planned
completed trip stop marks country visited
in-progress trip stop marks country visited
unknown trip stop does not affect country state
```

### 20.2 v2.0 Test Priorities

```text
FlightCountryTrackingService
ItineraryGeneratedStopService
ItineraryGroupDerivationService
Room migration from MVP to v2.0
JSON backup v2 import/export
```

Important v2.0 cases:

```text
solo completed Barcelona -> Tokyo marks Japan visited
solo planned Barcelona -> Tokyo marks Japan planned
solo unknown does not affect country state
in-progress flight does not mark destination visited
Barcelona -> Doha -> Tokyo group marks Japan, not Qatar
Osaka -> Doha -> Barcelona return group marks Japan, not Spain
existing MVP trip stops migrate to source = MANUAL
```

---

## 21. MVP Architecture Scope

The MVP should implement only:

```text
dashboard
countries/territories
country detail
country user state
country logs
simple trips
trip stops
location search/manual fallback
JSON backup/import
settings
```

MVP packages should focus on:

```text
presentation/dashboard
presentation/country
presentation/trip
presentation/settings

domain/service/CountryStateDerivationService
domain/usecase/country
domain/usecase/trip
domain/usecase/backup

data/repository/CountryRepositoryImpl
data/repository/TripRepositoryImpl
data/backup
data/dataset
data/location
```

Do not build unused flight/itinerary repositories during MVP.

---

## 22. v2.0 Architecture Additions

v2.0 adds:

```text
AirportRepository
FlightRepository
ItineraryRepository
AirportSearchService
FlightCountryTrackingService
ItineraryGeneratedStopService
ItineraryGroupDerivationService
Room migration
Backup version 2
flight/itinerary screens
route visualization
```

v2.0 packages:

```text
presentation/flight/
presentation/itinerary/

ui/screens/flights/
ui/screens/itineraries/

domain/usecase/flight/
domain/usecase/itinerary/

data/repository/FlightRepositoryImpl.kt
data/repository/ItineraryRepositoryImpl.kt
data/repository/AirportRepositoryImpl.kt
```

v2.0 should extend MVP, not require rewriting countries/trips.

---

## 23. Recommended MVP Implementation Milestones

### Milestone 0: Project Foundation

Build:

```text
Android project
Compose setup
Room setup
Navigation shell
theme baseline
manual dependency container
basic package structure
```

Success:

```text
App runs with empty navigation and database initialized.
```

---

### Milestone 1: Dataset Foundation

Build:

```text
countries/territories JSON asset
DatasetMetadataEntity
CountryEntity
dataset importer
first-launch import
```

Success:

```text
Countries/territories are loaded from local data.
```

---

### Milestone 2: Country Tracking

Build:

```text
countries list
country detail
CountryUserStateEntity
wished toggle
currently living toggle
CountryStateDerivationService basic version
```

Success:

```text
User can browse countries and mark wished/currently living.
```

---

### Milestone 3: Country Logs and Flexible Dates

Build:

```text
CountryLogEntity
visit/lived log creation
flexible date model
flexible date validation
flexible date formatting
notes
```

Success:

```text
Manual logs correctly affect country state.
```

---

### Milestone 4: Trips

Build:

```text
TripEntity
trips list
trip detail
create/edit/delete trip
trip status
trip date range
notes
```

Success:

```text
User can create simple trips.
```

---

### Milestone 5: Trip Stops

Build:

```text
TripStopEntity
manual stop creation
stop ordering
stop editing/deleting
country derivation from trip status
related stops on country detail
```

Success:

```text
Trip stops affect planned/visited country state correctly.
```

---

### Milestone 6: Location Search

Build:

```text
LocationSearchService abstraction
chosen provider implementation
manual fallback
optional PlaceEntity
```

Success:

```text
User can add stops through search or manual fallback.
```

---

### Milestone 7: Backup/Import MVP

Build:

```text
backupVersion 1
JSON export
JSON import
validation
replace-all import after confirmation
dataset version metadata
```

Success:

```text
MVP user data can be exported and restored safely.
```

---

### Milestone 8: MVP Polish

Build:

```text
empty states
validation messages
delete confirmations
basic dashboard
Catalan strings
basic visual polish
```

Success:

```text
MVP is stable and pleasant enough for personal use.
```

---

## 24. v2.0 Implementation Milestones

### Milestone 9: Airport Foundation

Build:

```text
AirportEntity
airport dataset import
airport search
airport selection UI
```

---

### Milestone 10: Solo Flights

Build:

```text
FlightEntity
flight list
flight detail
create/edit/delete solo flights
solo flight country derivation
```

---

### Milestone 11: Itineraries

Build:

```text
ItineraryEntity
ItineraryGroupEntity
grouped flights
group ordering
flight ordering inside groups
```

---

### Milestone 12: Generated Stops

Build:

```text
TripStopEntity migration
source = MANUAL / ITINERARY_GROUP
ItineraryGeneratedStopService
link itinerary to trip
generated stop preview
trip detail integration
```

---

### Milestone 13: v2.0 Maps and Timelines

Build:

```text
flight route preview
itinerary route preview
trip map with generated stops
country detail timeline with flight/itinerary entries
```

---

### Milestone 14: Backup/Import v2

Build:

```text
backupVersion 2
export flights
export itineraries
export itinerary groups
import v2 data
airport dataset validation
```

---

## 25. Migration Strategy

Room migrations should be explicit.

### MVP to v2.0

Add tables:

```text
AirportEntity
FlightEntity
ItineraryEntity
ItineraryGroupEntity
```

Modify:

```text
TripStopEntity
```

Add:

```text
source TEXT NOT NULL DEFAULT 'MANUAL'
itinerary_group_id TEXT NULL
is_visible INTEGER NOT NULL DEFAULT 1
display_title TEXT NULL
```

Migration principle:

```text
Existing MVP data remains valid.
Existing trip stops become source = MANUAL.
```

---

## 26. Performance Strategy

Start simple.

MVP:

```text
compute derived states dynamically
use Room indexes for common queries
avoid premature caching
```

Potential later optimizations:

```text
derived country state cache
precomputed dashboard stats
paged lists if datasets grow
map marker clustering
background dataset import
```

Only optimize after real performance issues appear.

---

## 27. Security and Privacy

Atlas is local-first and personal.

Privacy principles:

```text
no account required
no backend required
no personal data uploaded by default
location search only sends search queries to provider
JSON backups are user-controlled files
```

Future cloud backup would require a separate architecture decision.

Potential future improvement:

```text
encrypted backup export
```

Not required for MVP.

---

## 28. Build Configuration

Recommended minimum setup:

```text
single Android app module
Kotlin
Compose
Room
kotlinx.serialization
Coroutines
Navigation Compose
```

A multi-module architecture is not recommended at the start.

Reason:

```text
extra Gradle complexity is not worth it for MVP
```

Possible future modules:

```text
core
data
domain
feature-countries
feature-trips
feature-flights
```

Only split modules if the codebase becomes large enough to justify it.

---

## 29. Key Open Technical Decisions

These should be resolved during implementation planning:

```text
1. osmdroid vs MapLibre for maps
2. first geocoding provider
3. exact country/territory dataset source
4. whether static datasets are JSON-imported or prepackaged Room DB
5. whether PlaceEntity is included in MVP or added later
6. exact flexible date UI component
7. backup import replace-only vs merge
8. airport dataset source for v2.0
9. flight local/UTC conversion strategy
10. generated stop unlink behaviour
```

Recommended answers for MVP:

```text
manual dependency container
JSON-imported country dataset
dynamic country state derivation
replace-all backup import
manual trip stops before location search
no flights until v2.0
```

---

## 30. Architecture Summary

Atlas should use a clean but pragmatic local-first Android architecture:

```text
Kotlin
Jetpack Compose
Room
Coroutines + Flow
Navigation Compose
kotlinx.serialization
manual dependency container
OpenStreetMap-based maps/search
```

The key architectural rules are:

```text
keep user data local
separate static data from user data
separate Room entities from domain models
centralize flexible date logic
centralize country state derivation
abstract location search
use JSON backup/import
build only the schema needed for each version
add flights/itineraries in v2.0 through migrations
avoid backend/cloud complexity during MVP
```

The architecture should make the MVP easy to build while leaving a clear path for v2.0 and the full Atlas product.
