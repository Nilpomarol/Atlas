# Atlas Milestone 0–1 Task List

## 1. Purpose

This document defines the first concrete implementation tasks for Atlas.

It covers:

- Milestone 0: project foundation
- Milestone 1: country tracking foundation

The goal is to reach the first usable vertical slice of the app:

```text
Open app
→ view countries
→ open country detail
→ mark wished
→ set currently living
→ add visit/lived logs
→ see derived country states
```

This gives Atlas a working base before adding flights, itineraries, trips, maps, and backup/import.

---

# 2. Development Strategy

Atlas should be built incrementally.

Do not try to implement all MVP features at once.

The first development slice should prove that the following foundations work:

- Android project structure
- Jetpack Compose UI
- Room database
- static dataset import
- local user data persistence
- flexible dates
- country state derivation
- basic navigation
- simple local-first architecture

---

# 3. Milestone 0: Project Foundation

## 3.1 Goal

Create a runnable Android app with the technical foundation needed for future Atlas features.

At the end of Milestone 0, the app should:

- compile and run
- show a basic navigation shell
- initialize Room
- load a small country dataset
- have the package structure ready
- have basic domain/data/ui separation

---

## 3.2 Milestone 0 Deliverables

```text
Runnable Android app
Compose theme
Navigation shell
Room database setup
Manual dependency container
Initial package structure
Static country dataset importer
Small bundled countries.json
Dataset metadata table
Basic country repository
Basic country list screen using real local data
```

---

# 4. Milestone 0 Task Breakdown

---

## 4.1 Create Android project

### Task

Create the Android project.

Recommended configuration:

```text
Language: Kotlin
UI: Jetpack Compose
Minimum SDK: choose reasonably modern Android version
Package name: com.atlas or your preferred package
```

### Acceptance criteria

- app builds successfully
- app runs on emulator/device
- default screen appears
- Compose is enabled

---

## 4.2 Add core dependencies

### Task

Add project dependencies.

Recommended dependencies:

```text
Jetpack Compose
Navigation Compose
Room
Room KTX
Kotlin Coroutines
kotlinx.serialization
Lifecycle ViewModel Compose
```

Optional for later:

```text
osmdroid or MapLibre
image loading library
testing libraries
```

Do not add map dependencies yet unless needed immediately.

### Acceptance criteria

- project syncs successfully
- dependencies compile
- no unused complex framework added too early

---

## 4.3 Create package structure

### Task

Create initial package structure.

Recommended:

```text
com.atlas

  app/
  core/
    date/
    result/
    error/

  data/
    local/
      database/
      dao/
      entity/
      mapper/
    dataset/
    repository/

  domain/
    model/
    repository/
    service/
    usecase/

  presentation/
    country/

  ui/
    navigation/
    screens/
      dashboard/
      countries/
    components/
    theme/
```

### Acceptance criteria

- package structure exists
- code is placed according to architecture
- no business logic in UI packages

---

## 4.4 Create app theme

### Task

Create initial Atlas theme.

The theme does not need final visual polish, but should establish:

- color scheme
- typography
- spacing style
- light/dark support if easy

### Suggested visual direction

Atlas should feel:

```text
clean
map-inspired
visual
personal
modern
warm
```

Possible early color ideas:

```text
deep blue / slate
map green
warm sand
accent orange
```

### Acceptance criteria

- app uses custom `AtlasTheme`
- basic colors defined
- typography defined
- screens use theme rather than hardcoded styling everywhere

---

## 4.5 Create navigation shell

### Task

Create the basic navigation structure.

Initial routes:

```text
dashboard
countries
country_detail/{countryIso2}
```

Future routes can be added later.

### Acceptance criteria

- app starts on dashboard or countries screen
- can navigate from country list to country detail
- back navigation works
- routes are centralized

---

## 4.6 Create manual dependency container

### Task

Create `AtlasAppContainer`.

It should provide:

```text
AtlasDatabase
CountryRepository
DatasetImporter
```

Later it will provide:

```text
FlightRepository
TripRepository
LocationSearchService
BackupManager
```

### Acceptance criteria

- app has one dependency container
- ViewModels can access repositories
- database is initialized once

---

## 4.7 Set up Room database

### Task

Create initial Room database.

Initial entities:

```text
CountryEntity
CountryUserStateEntity
CountryLogEntity
CountryStatsSummaryEntity
CountryStatFactEntity
DatasetMetadataEntity
```

For Milestone 0, only `CountryEntity` and `DatasetMetadataEntity` must be fully used.

### Acceptance criteria

- Room database compiles
- database can be opened
- entities are registered
- DAOs are created
- app does not crash on startup

---

## 4.8 Create CountryEntity

### Task

Implement `CountryEntity`.

Fields:

```text
iso2: String PRIMARY KEY
iso3: String UNIQUE NOT NULL
name_ca: String NOT NULL
name_en: String NULL
continent: String NOT NULL
subregion: String NULL
flag_emoji: String NULL
flag_asset: String NULL
latitude: Double NULL
longitude: Double NULL
```

### Acceptance criteria

- entity compiles
- primary key is `iso2`
- DAO can insert/query countries

---

## 4.9 Create DatasetMetadataEntity

### Task

Implement `DatasetMetadataEntity`.

Fields:

```text
key: String PRIMARY KEY
version: String NOT NULL
imported_at: String NOT NULL
```

### Acceptance criteria

- entity compiles
- DAO can get/set metadata by key

---

## 4.10 Create CountryDao

### Task

Create DAO for country queries.

Initial methods:

```text
getAllCountries(): Flow<List<CountryEntity>>
getCountryByIso2(iso2: String): Flow<CountryEntity?>
insertCountries(countries: List<CountryEntity>)
countCountries(): Int
```

### Acceptance criteria

- DAO compiles
- country list can be observed as Flow
- countries can be inserted from dataset importer

---

## 4.11 Create DatasetMetadataDao

### Task

Create DAO for dataset metadata.

Methods:

```text
getMetadata(key: String): DatasetMetadataEntity?
upsertMetadata(metadata: DatasetMetadataEntity)
```

### Acceptance criteria

- metadata can be read/written
- dataset importer can use it

---

## 4.12 Create initial countries.json

### Task

Create bundled asset:

```text
assets/data/countries.json
```

For the first implementation, include a small test set.

Example countries:

```text
Spain
France
Japan
United States
Italy
Germany
Portugal
United Kingdom
Morocco
Mexico
```

Use Catalan names.

Example fields:

```json
[
  {
    "iso2": "JP",
    "iso3": "JPN",
    "nameCa": "Japó",
    "nameEn": "Japan",
    "continent": "Àsia",
    "subregion": "Àsia oriental",
    "flagEmoji": "🇯🇵",
    "latitude": 36.2048,
    "longitude": 138.2529
  }
]
```

### Acceptance criteria

- JSON file exists
- JSON is valid
- data can be parsed with kotlinx.serialization
- country names are in Catalan

---

## 4.13 Create country dataset DTO

### Task

Create serialization DTO for countries.

Example:

```text
CountryDatasetDto
- iso2
- iso3
- nameCa
- nameEn
- continent
- subregion
- flagEmoji
- flagAsset
- latitude
- longitude
```

### Acceptance criteria

- DTO parses bundled JSON
- DTO maps to `CountryEntity`

---

## 4.14 Create CountryDatasetImporter

### Task

Create importer that reads `countries.json` and inserts countries into Room.

Behavior:

- check dataset metadata
- if countries dataset not imported, import it
- write metadata after successful import

Initial version can be simple.

Dataset version:

```text
2026.1
```

### Acceptance criteria

- countries import on first launch
- repeated launches do not duplicate countries
- metadata is stored after import
- country list displays imported countries

---

## 4.15 Create Country domain model

### Task

Create domain model:

```text
Country
- iso2
- iso3
- nameCa
- nameEn
- continent
- subregion
- flagEmoji
- flagAsset
- latitude
- longitude
```

### Acceptance criteria

- domain model does not depend on Room
- mapper exists between entity and domain model

---

## 4.16 Create CountryRepository

### Task

Create domain repository interface:

```text
CountryRepository
- observeCountries(): Flow<List<Country>>
- observeCountry(iso2: String): Flow<Country?>
```

Create data implementation:

```text
CountryRepositoryImpl
```

### Acceptance criteria

- ViewModel uses repository, not DAO directly
- country list screen receives domain models

---

## 4.17 Create basic countries screen

### Task

Create a simple countries list screen.

Initial display:

```text
flag
country name
continent
```

### Acceptance criteria

- screen observes Room data
- countries appear after import
- loading/empty states handled simply
- clicking country navigates to detail page

---

## 4.18 Create basic country detail screen

### Task

Create country detail page.

Initial display:

```text
flag
country name
continent
subregion
coordinates if available
```

### Acceptance criteria

- accepts `countryIso2` navigation argument
- loads country from repository
- displays country data
- handles missing country gracefully

---

# 5. Milestone 0 Completion Criteria

Milestone 0 is complete when:

```text
App runs
Room works
countries.json imports
country list shows imported countries
country detail opens
basic architecture is in place
```

At this point Atlas has a real local data foundation.

---

# 6. Milestone 1: Country Tracking Foundation

## 6.1 Goal

Implement the first useful Atlas functionality:

```text
country tracking
```

At the end of Milestone 1, the user should be able to:

- mark countries as wished
- set exactly one country as currently living
- add manual visit logs
- add manual lived logs
- view derived country states
- see country states on the country list and detail page

---

## 6.2 Milestone 1 Deliverables

```text
CountryUserStateEntity
CountryLogEntity
FlexibleDate model
FlexibleDateRange model
Flexible date validation
Flexible date formatting
Country user state DAO
Country log DAO
Country state derivation service
Wished toggle
Currently living toggle
Add country log flow
Country state display
Country list filters/search basic version
```

---

# 7. Milestone 1 Task Breakdown

---

## 7.1 Implement shared enums

### Task

Create enums:

```text
DatePrecision
TravelStatus
CountryLogType
```

For Milestone 1, only these are immediately needed:

```text
DatePrecision
CountryLogType
```

### Acceptance criteria

- enums exist in core/domain package
- database mapping strategy decided, likely stored as String

---

## 7.2 Implement FlexibleDate

### Task

Create domain model:

```text
FlexibleDate
- year: Int
- month: Int?
- day: Int?
- precision: DatePrecision
```

### Acceptance criteria

- supports year-only date
- supports month-year date
- supports full date
- invalid combinations are rejected by validator

---

## 7.3 Implement FlexibleDateRange

### Task

Create domain model:

```text
FlexibleDateRange
- start: FlexibleDate?
- end: FlexibleDate?
- precision: DatePrecision?
```

### Acceptance criteria

- start-only range supported
- end-only range supported
- start+end range supported
- empty range supported where parent field is optional
- mixed precision range rejected

---

## 7.4 Implement FlexibleDateValidator

### Task

Create validator for flexible dates and ranges.

Validation rules:

```text
YEAR: month and day must be null
MONTH: month required, day must be null
DAY: month and day required
month must be 1–12
day must be valid for year/month
range start/end must share precision
```

### Acceptance criteria

- valid dates pass
- invalid dates fail
- mixed precision ranges fail
- day/month validity checked

---

## 7.5 Implement FlexibleDateFormatter

### Task

Create Catalan-friendly display formatter.

Initial output can be simple:

```text
2023
06-2023
14-06-2023
2023 – 2024
06-2023 – 09-2023
14-06-2023 – 20-06-2023
```

Later this can become more polished.

### Acceptance criteria

- all supported precisions display correctly
- null/empty date displays as no date or equivalent Catalan label
- date ranges display correctly

---

## 7.6 Implement CountryUserStateEntity

### Task

Create entity:

```text
CountryUserStateEntity
- country_iso2: String PRIMARY KEY
- wished: Boolean NOT NULL DEFAULT false
- currently_living: Boolean NOT NULL DEFAULT false
- updated_at: String NOT NULL
```

### Acceptance criteria

- entity compiles
- foreign key to country
- DAO can read/update state

---

## 7.7 Implement CountryLogEntity

### Task

Create entity:

```text
CountryLogEntity
- id: String PRIMARY KEY
- country_iso2: String NOT NULL
- type: String NOT NULL
- start_year: Int NULL
- start_month: Int NULL
- start_day: Int NULL
- end_year: Int NULL
- end_month: Int NULL
- end_day: Int NULL
- date_precision: String NULL
- notes: String NULL
- created_at: String NOT NULL
- updated_at: String NOT NULL
```

### Acceptance criteria

- entity compiles
- foreign key to country
- supports flexible date range columns
- DAO can insert/query logs

---

## 7.8 Create CountryUserStateDao

### Task

DAO methods:

```text
observeState(countryIso2: String): Flow<CountryUserStateEntity?>
observeAllStates(): Flow<List<CountryUserStateEntity>>
upsertState(state: CountryUserStateEntity)
setWished(countryIso2: String, wished: Boolean, updatedAt: String)
clearCurrentlyLivingForAll(updatedAt: String)
setCurrentlyLiving(countryIso2: String, updatedAt: String)
```

### Acceptance criteria

- wished can be updated
- currently living can be set
- all previous currently living states can be cleared

---

## 7.9 Create CountryLogDao

### Task

DAO methods:

```text
observeLogsForCountry(countryIso2: String): Flow<List<CountryLogEntity>>
observeAllLogs(): Flow<List<CountryLogEntity>>
insertLog(log: CountryLogEntity)
updateLog(log: CountryLogEntity)
deleteLog(logId: String)
```

### Acceptance criteria

- logs can be inserted
- logs can be observed by country
- logs can be deleted
- logs are ordered reasonably, newest first or by date

---

## 7.10 Extend CountryRepository

### Task

Add methods:

```text
observeCountryUserState(countryIso2: String): Flow<CountryUserState?>
observeCountryLogs(countryIso2: String): Flow<List<CountryLog>>
setWished(countryIso2: String, wished: Boolean)
setCurrentlyLiving(countryIso2: String)
addCountryLog(countryIso2: String, type: CountryLogType, dateRange: FlexibleDateRange?, notes: String?)
deleteCountryLog(logId: String)
```

### Acceptance criteria

- repository hides DAO/entity details
- domain models returned to ViewModels
- repository handles basic mapping

---

## 7.11 Implement SetCurrentlyLivingCountryUseCase

### Task

Create use case that enforces currently living rule.

Behavior:

1. clear currently living from all countries
2. set selected country currently living
3. create or maintain a lived log for the country

### Lived log behavior

Recommended simple MVP behavior:

- if the country has no lived log, create one with no date and note such as null
- if it already has a lived log, do not create another automatically

### Acceptance criteria

- only one country is currently living
- selected country becomes currently living
- selected country has at least one lived log
- previous country is no longer currently living

---

## 7.12 Implement ToggleWishedCountryUseCase

### Task

Create use case:

```text
ToggleWishedCountryUseCase
```

Behavior:

- set wished true/false for country
- create CountryUserState if missing

### Acceptance criteria

- wished state toggles correctly
- works even if CountryUserState row did not previously exist

---

## 7.13 Implement AddCountryLogUseCase

### Task

Create use case:

```text
AddCountryLogUseCase
```

Inputs:

```text
countryIso2
type: VISIT | LIVED
dateRange optional
notes optional
```

Behavior:

- validate flexible date range
- create log with ID and timestamps
- insert log

### Acceptance criteria

- valid log is saved
- invalid date range is rejected
- notes optional
- date optional

---

## 7.14 Implement CountryTrackingState domain model

### Task

Create model:

```text
CountryTrackingState
- visited: Boolean
- lived: Boolean
- wished: Boolean
- planned: Boolean
- currentlyLiving: Boolean
- neverVisited: Boolean
```

For Milestone 1:

```text
planned = false
```

because trips/flights are not implemented yet.

### Acceptance criteria

- model exists
- can represent multiple simultaneous states
- neverVisited is derived correctly

---

## 7.15 Implement CountryStateDerivationService v1

### Task

Implement first version using only:

```text
CountryUserState
CountryLog
```

Rules:

```text
wished = CountryUserState.wished
currentlyLiving = CountryUserState.currentlyLiving
lived = has lived log OR currentlyLiving
visited = has visit log OR lived OR currentlyLiving
planned = false for now
neverVisited = !visited && !lived
```

### Acceptance criteria

- wished countries show wished
- lived logs imply lived and visited
- visit logs imply visited
- currently living implies lived and visited
- never visited computed correctly

---

## 7.16 Create country list UI state

### Task

Create ViewModel/UI state for country list.

Should include:

```text
countries with tracking state
search query
selected filters
loading state
```

Initial filters:

```text
all
visited
lived
wished
currently living
never visited
```

Planned filter can be added but will show none until trips/flights exist.

### Acceptance criteria

- country list shows state badges
- search by country name works
- filters work using derived state

---

## 7.17 Update countries list screen

### Task

Add:

- state badges
- search bar
- filters
- wished/visited/lived indicators

Initial badge examples:

```text
Visitat
Viscut
Desitjat
Vivint-hi
No visitat
```

Catalan wording can be refined later.

### Acceptance criteria

- country list displays derived states
- search filters country list
- state filters country list
- UI remains simple and usable

---

## 7.18 Create country detail UI state

### Task

Country detail ViewModel should provide:

```text
country
user state
logs
derived tracking state
```

### Acceptance criteria

- country detail observes changes
- toggling wished updates immediately
- setting currently living updates immediately
- adding logs updates state immediately

---

## 7.19 Update country detail screen

### Task

Add:

- wished toggle
- currently living action/toggle
- state badges
- manual logs section
- add visit/lived log button

### Acceptance criteria

- user can mark country wished
- user can set country currently living
- logs are visible
- derived states are visible
- UI updates reactively

---

## 7.20 Create add country log flow

### Task

Create simple modal/screen for adding a country log.

Fields:

```text
type: visit | lived
date range optional
notes optional
```

For date input, MVP can initially use simple fields:

```text
precision selector
start year/month/day
end year/month/day
```

This can be improved later.

### Acceptance criteria

- visit log can be added without date
- lived log can be added without date
- date range can be added
- notes can be added
- invalid mixed precision range is blocked

---

## 7.21 Add basic country stats summary display

### Task

If `CountryStatsSummaryEntity` exists and test data is available, show simple stats on country detail.

Example:

```text
Capital
Population
Area
Currency
```

This is optional for Milestone 1 but useful.

### Acceptance criteria

- country detail can show summary stats if present
- missing stats do not break UI

---

# 8. Milestone 1 Completion Criteria

Milestone 1 is complete when:

```text
Countries can be listed
Countries can be searched
Countries can be filtered by derived state
Country detail shows state
Wished toggle works
Currently living rule works
Visit logs can be added
Lived logs can be added
Flexible dates work for logs
Country states derive correctly from user data
Data persists after app restart
```

At this point Atlas has its first complete functional vertical slice.

---

# 9. Recommended Implementation Order

Implement in this order:

```text
1. Project setup
2. Room setup
3. Country static import
4. Basic country list/detail
5. Flexible date model
6. Country user state
7. Country logs
8. Wished/currently living use cases
9. Country state derivation
10. Country list state badges/search/filter
11. Country detail log creation
```

---

# 10. Suggested First Test Cases

## 10.1 Flexible date tests

```text
2023 is valid YEAR
06-2023 is valid MONTH
14-06-2023 is valid DAY
month 13 is invalid
31-02-2023 is invalid
2023 → 2024 is valid
2023 → 06-2024 is invalid
```

---

## 10.2 Currently living tests

```text
Setting Spain currently living sets Spain true
Setting Germany currently living unsets Spain
Currently living creates lived log if none exists
Currently living implies lived
Currently living implies visited
```

---

## 10.3 Country state derivation tests

```text
No logs/state → neverVisited true
Wished only → wished true, neverVisited true
Visit log → visited true
Lived log → lived true, visited true
Currently living → currentlyLiving true, lived true, visited true
```

---

# 11. Suggested Sample Countries Dataset

Initial `countries.json` can include:

```text
ES - Espanya
FR - França
JP - Japó
US - Estats Units
IT - Itàlia
DE - Alemanya
PT - Portugal
GB - Regne Unit
MA - Marroc
MX - Mèxic
```

This is enough to test:

- continents
- flags
- country list
- country detail
- user states
- logs

Later replace/expand with full world dataset.

---

# 12. Risks and Notes

## 12.1 Flexible date UI may take time

The date model is important, but the first UI can be basic.

Do not over-polish the date picker early.

---

## 12.2 Country state derivation will expand later

Milestone 1 only derives from country logs and user state.

Later milestones will add:

- solo flights
- trips
- trip stops
- excursions
- itinerary generated stops

Design `CountryStateDerivationService` so it can be expanded.

---

## 12.3 Static dataset update logic can be simple initially

For Milestone 0, only first import is needed.

More complex update/diff behavior can come later.

---

## 12.4 UI wording can be refined later

Use Catalan labels from the start, but do not block development on perfect wording.

Initial labels can be improved later.

---

# 13. After Milestone 1

Once Milestone 1 is complete, continue with:

```text
Atlas_Milestone_2_Flights_Task_List.md
```

Milestone 2 will implement:

- airport dataset
- solo flights
- flight status
- detailed flight fields
- country derivation from solo flights