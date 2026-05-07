# Atlas Milestone 0 Implementation Checklist

## 1. Purpose

This document is the direct implementation checklist for **Milestone 0: Project Foundation**.

The goal of Milestone 0 is to create a runnable Android app with:

- Kotlin
- Jetpack Compose
- Room
- Navigation Compose
- kotlinx.serialization
- manual dependency container
- bundled country dataset import
- countries list screen
- country detail screen

At the end of this milestone, Atlas should be able to:

```text
Launch app
→ import bundled countries.json
→ display countries from Room
→ open a country detail page
```

No country tracking user features are required yet. Those begin in Milestone 1.

---

# 2. Milestone 0 Outcome

Milestone 0 is complete when:

```text
App builds successfully
App launches successfully
Room database initializes
Bundled countries.json imports on first launch
Dataset metadata is stored
Countries list screen shows imported countries
Country detail screen opens for selected country
Country data survives app restart
```

---

# 3. Recommended Implementation Order

Implement in this order:

```text
1. Create Android project
2. Configure Gradle dependencies
3. Create package structure
4. Add Compose theme
5. Add navigation shell
6. Add manual dependency container
7. Add Room database
8. Add CountryEntity
9. Add DatasetMetadataEntity
10. Add DAOs
11. Add countries.json
12. Add dataset DTOs
13. Add country dataset importer
14. Add domain models and mappers
15. Add repository
16. Add country list ViewModel
17. Add country list screen
18. Add country detail ViewModel
19. Add country detail screen
20. Verify persistence after restart
```

---

# 4. Project Setup

## 4.1 Create Android project

Recommended configuration:

```text
Project name: Atlas
Package name: com.atlas
Language: Kotlin
UI: Jetpack Compose
Minimum SDK: 26 or higher
Target SDK: latest stable
```

Recommended package namespace:

```text
com.atlas
```

If you prefer a personal namespace, use that consistently.

---

## 4.2 Gradle dependencies

Add dependencies for:

```text
Jetpack Compose
Navigation Compose
Room
Room KTX
Kotlin Symbol Processing / KSP
Kotlin Coroutines
Lifecycle ViewModel Compose
kotlinx.serialization
```

Optional later, not needed in Milestone 0:

```text
osmdroid
MapLibre
image loading library
```

Do not add map/photo dependencies yet unless necessary.

---

# 5. Package Structure

Create the initial package structure:

```text
com.atlas

  app/

  core/
    error/
    result/

  data/
    local/
      database/
      dao/
      entity/
      mapper/
    dataset/
      dto/
    repository/

  domain/
    model/
    repository/

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

Milestone 0 does not need every future package yet, but creating the structure early helps consistency.

---

# 6. Files to Create

## 6.1 App files

```text
app/AtlasApplication.kt
app/AtlasAppContainer.kt
```

---

## 6.2 Core files

```text
core/result/AppResult.kt
core/error/AppError.kt
```

These can be minimal or postponed if not needed immediately.

---

## 6.3 Database files

```text
data/local/database/AtlasDatabase.kt
```

---

## 6.4 Entity files

```text
data/local/entity/CountryEntity.kt
data/local/entity/DatasetMetadataEntity.kt
```

Optional to create now but not fully use yet:

```text
data/local/entity/CountryUserStateEntity.kt
data/local/entity/CountryLogEntity.kt
data/local/entity/CountryStatsSummaryEntity.kt
data/local/entity/CountryStatFactEntity.kt
```

For strict Milestone 0, only these are required:

```text
CountryEntity
DatasetMetadataEntity
```

---

## 6.5 DAO files

```text
data/local/dao/CountryDao.kt
data/local/dao/DatasetMetadataDao.kt
```

---

## 6.6 Dataset files

```text
data/dataset/dto/CountryDatasetDto.kt
data/dataset/CountryDatasetImporter.kt
data/dataset/DatasetConstants.kt
```

Asset file:

```text
app/src/main/assets/data/countries.json
```

---

## 6.7 Mapper files

```text
data/local/mapper/CountryMapper.kt
```

---

## 6.8 Repository files

```text
domain/repository/CountryRepository.kt
data/repository/CountryRepositoryImpl.kt
```

---

## 6.9 Domain model files

```text
domain/model/Country.kt
```

---

## 6.10 Presentation files

```text
presentation/country/CountryListViewModel.kt
presentation/country/CountryDetailViewModel.kt
```

---

## 6.11 Navigation files

```text
ui/navigation/Routes.kt
ui/navigation/AtlasNavHost.kt
```

---

## 6.12 UI screen files

```text
ui/screens/dashboard/DashboardScreen.kt
ui/screens/countries/CountryListScreen.kt
ui/screens/countries/CountryDetailScreen.kt
```

---

## 6.13 Theme files

```text
ui/theme/Color.kt
ui/theme/Type.kt
ui/theme/Theme.kt
```

Usually Android Studio creates these if using a Compose template.

---

# 7. File Responsibilities

## 7.1 AtlasApplication.kt

### Purpose

Application class that initializes the app container.

### Responsibilities

```text
Create AtlasAppContainer
Expose appContainer to the app
```

### Example behavior

```text
onCreate()
→ appContainer = AtlasAppContainer(applicationContext)
```

### Acceptance criteria

- application class is registered in AndroidManifest
- app container is available from activities/viewmodels as needed

---

## 7.2 AtlasAppContainer.kt

### Purpose

Manual dependency container.

### Responsibilities

Create and expose:

```text
AtlasDatabase
CountryDao
DatasetMetadataDao
CountryDatasetImporter
CountryRepository
```

### Future responsibilities

Later this will also expose:

```text
FlightRepository
TripRepository
LocationSearchService
BackupManager
```

### Acceptance criteria

- database initialized once
- repository instances are available
- no duplicate database instances created casually

---

## 7.3 AtlasDatabase.kt

### Purpose

Room database definition.

### Milestone 0 entities

```text
CountryEntity
DatasetMetadataEntity
```

### Milestone 0 DAOs

```text
CountryDao
DatasetMetadataDao
```

### Suggested database version

```text
version = 1
```

### Acceptance criteria

- Room compiles
- DAOs exposed
- app can open database

---

## 7.4 CountryEntity.kt

### Purpose

Room entity for static country identity data.

### Fields

```text
iso2: String PRIMARY KEY
iso3: String
nameCa: String
nameEn: String?
continent: String
subregion: String?
flagEmoji: String?
flagAsset: String?
latitude: Double?
longitude: Double?
```

### Table name

```text
countries
```

### Acceptance criteria

- primary key is `iso2`
- DAO can insert/query countries
- field names are consistent with dataset mapper

---

## 7.5 DatasetMetadataEntity.kt

### Purpose

Tracks imported static dataset versions.

### Fields

```text
key: String PRIMARY KEY
version: String
importedAt: String
```

### Table name

```text
dataset_metadata
```

### Acceptance criteria

- metadata can be inserted/read
- country importer uses this to avoid duplicate imports

---

## 7.6 CountryDao.kt

### Purpose

Country database access.

### Required methods

```text
observeAllCountries(): Flow<List<CountryEntity>>

observeCountryByIso2(iso2: String): Flow<CountryEntity?>

getCountryCount(): Int

insertCountries(countries: List<CountryEntity>)
```

Optional useful methods:

```text
getCountryByIso2Now(iso2: String): CountryEntity?
```

### Acceptance criteria

- country list screen can observe all countries
- country detail can observe one country
- dataset importer can insert countries

---

## 7.7 DatasetMetadataDao.kt

### Purpose

Dataset metadata database access.

### Required methods

```text
getMetadata(key: String): DatasetMetadataEntity?

upsertMetadata(metadata: DatasetMetadataEntity)
```

### Acceptance criteria

- importer can check if countries dataset was already imported
- importer can write imported version

---

## 7.8 countries.json

### Purpose

Bundled initial country dataset.

### Location

```text
app/src/main/assets/data/countries.json
```

### Initial sample content

Start with a small dataset:

```json
[
  {
    "iso2": "ES",
    "iso3": "ESP",
    "nameCa": "Espanya",
    "nameEn": "Spain",
    "continent": "Europa",
    "subregion": "Europa meridional",
    "flagEmoji": "🇪🇸",
    "latitude": 40.4637,
    "longitude": -3.7492
  },
  {
    "iso2": "FR",
    "iso3": "FRA",
    "nameCa": "França",
    "nameEn": "France",
    "continent": "Europa",
    "subregion": "Europa occidental",
    "flagEmoji": "🇫🇷",
    "latitude": 46.2276,
    "longitude": 2.2137
  },
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
  },
  {
    "iso2": "US",
    "iso3": "USA",
    "nameCa": "Estats Units",
    "nameEn": "United States",
    "continent": "Amèrica del Nord",
    "subregion": "Amèrica del Nord",
    "flagEmoji": "🇺🇸",
    "latitude": 37.0902,
    "longitude": -95.7129
  },
  {
    "iso2": "IT",
    "iso3": "ITA",
    "nameCa": "Itàlia",
    "nameEn": "Italy",
    "continent": "Europa",
    "subregion": "Europa meridional",
    "flagEmoji": "🇮🇹",
    "latitude": 41.8719,
    "longitude": 12.5674
  },
  {
    "iso2": "DE",
    "iso3": "DEU",
    "nameCa": "Alemanya",
    "nameEn": "Germany",
    "continent": "Europa",
    "subregion": "Europa occidental",
    "flagEmoji": "🇩🇪",
    "latitude": 51.1657,
    "longitude": 10.4515
  },
  {
    "iso2": "PT",
    "iso3": "PRT",
    "nameCa": "Portugal",
    "nameEn": "Portugal",
    "continent": "Europa",
    "subregion": "Europa meridional",
    "flagEmoji": "🇵🇹",
    "latitude": 39.3999,
    "longitude": -8.2245
  },
  {
    "iso2": "GB",
    "iso3": "GBR",
    "nameCa": "Regne Unit",
    "nameEn": "United Kingdom",
    "continent": "Europa",
    "subregion": "Europa septentrional",
    "flagEmoji": "🇬🇧",
    "latitude": 55.3781,
    "longitude": -3.4360
  },
  {
    "iso2": "MA",
    "iso3": "MAR",
    "nameCa": "Marroc",
    "nameEn": "Morocco",
    "continent": "Àfrica",
    "subregion": "Àfrica del Nord",
    "flagEmoji": "🇲🇦",
    "latitude": 31.7917,
    "longitude": -7.0926
  },
  {
    "iso2": "MX",
    "iso3": "MEX",
    "nameCa": "Mèxic",
    "nameEn": "Mexico",
    "continent": "Amèrica del Nord",
    "subregion": "Amèrica Central",
    "flagEmoji": "🇲🇽",
    "latitude": 23.6345,
    "longitude": -102.5528
  }
]
```

### Acceptance criteria

- file exists in assets
- JSON parses successfully
- imported countries display in UI

---

## 7.9 CountryDatasetDto.kt

### Purpose

DTO used to parse `countries.json`.

### Fields

```text
iso2: String
iso3: String
nameCa: String
nameEn: String?
continent: String
subregion: String?
flagEmoji: String?
flagAsset: String?
latitude: Double?
longitude: Double?
```

### Acceptance criteria

- uses kotlinx.serialization
- maps to `CountryEntity`

---

## 7.10 DatasetConstants.kt

### Purpose

Central place for dataset keys and versions.

### Suggested constants

```text
COUNTRIES_DATASET_KEY = "countries"
COUNTRIES_DATASET_VERSION = "2026.1"
COUNTRIES_ASSET_PATH = "data/countries.json"
```

### Acceptance criteria

- importer does not hardcode strings repeatedly
- dataset version can be changed later

---

## 7.11 CountryDatasetImporter.kt

### Purpose

Reads bundled country JSON and imports it into Room.

### Responsibilities

```text
Check dataset metadata
Read countries.json from assets
Parse JSON
Map DTOs to CountryEntity
Insert countries
Write dataset metadata
```

### Initial import behavior

For Milestone 0:

```text
If metadata for countries does not exist, import.
If metadata exists with same version, skip.
```

Future behavior can handle updates.

### Acceptance criteria

- first launch imports countries
- second launch skips import
- no duplicate countries
- import failure does not crash app silently; log or expose error

---

## 7.12 Country.kt

### Purpose

Domain country model.

### Fields

```text
iso2
iso3
nameCa
nameEn
continent
subregion
flagEmoji
flagAsset
latitude
longitude
```

### Acceptance criteria

- no Room annotations
- no dependency on data layer
- used by presentation/UI

---

## 7.13 CountryMapper.kt

### Purpose

Maps between data and domain models.

### Required functions

```text
CountryEntity.toDomain(): Country
CountryDatasetDto.toEntity(): CountryEntity
```

Optional:

```text
List<CountryEntity>.toDomain()
```

### Acceptance criteria

- repository returns domain model
- dataset importer maps DTO to entity

---

## 7.14 CountryRepository.kt

### Purpose

Domain-facing repository interface.

### Required methods

```text
observeCountries(): Flow<List<Country>>
observeCountry(iso2: String): Flow<Country?>
```

### Acceptance criteria

- presentation layer depends on this interface
- no DAO exposed to ViewModel

---

## 7.15 CountryRepositoryImpl.kt

### Purpose

Data-layer implementation of `CountryRepository`.

### Responsibilities

```text
Call CountryDao
Map entities to domain models
Return Flows
```

### Acceptance criteria

- country list ViewModel can observe countries
- country detail ViewModel can observe selected country

---

## 7.16 CountryListViewModel.kt

### Purpose

ViewModel for countries list.

### Responsibilities

```text
Observe countries from repository
Expose CountryListUiState
Handle loading/empty state
```

### Suggested UI state

```text
CountryListUiState
- isLoading: Boolean
- countries: List<Country>
- errorMessage: String?
```

Search/filters are Milestone 1, not required yet.

### Acceptance criteria

- screen can observe UI state
- countries appear from repository
- empty state works

---

## 7.17 CountryDetailViewModel.kt

### Purpose

ViewModel for country detail.

### Responsibilities

```text
Observe country by iso2
Expose CountryDetailUiState
Handle missing country
```

### Suggested UI state

```text
CountryDetailUiState
- isLoading: Boolean
- country: Country?
- errorMessage: String?
```

### Acceptance criteria

- detail screen receives country data
- invalid ISO2 shows simple error/missing state

---

## 7.18 Routes.kt

### Purpose

Central route definitions.

### Initial routes

```text
Dashboard = "dashboard"
Countries = "countries"
CountryDetail = "country_detail/{countryIso2}"
```

### Helper

Create function:

```text
countryDetail(countryIso2: String): String
```

### Acceptance criteria

- routes are not hardcoded throughout UI
- navigation argument name is consistent

---

## 7.19 AtlasNavHost.kt

### Purpose

Navigation graph.

### Initial screens

```text
DashboardScreen
CountryListScreen
CountryDetailScreen
```

### Acceptance criteria

- app starts on dashboard or country list
- country list navigates to detail
- detail receives ISO2 argument
- back navigation works

---

## 7.20 DashboardScreen.kt

### Purpose

Temporary or simple dashboard.

For Milestone 0, it can be minimal.

Options:

```text
Show app title and button to countries
```

or start directly on country list.

### Acceptance criteria

- no empty default template screen
- user can reach country list

---

## 7.21 CountryListScreen.kt

### Purpose

Display imported countries.

### Initial UI

Each row/card should show:

```text
flag emoji
nameCa
continent
```

Optional:

```text
subregion
```

### Interaction

```text
Tap country → navigate to country detail
```

### Acceptance criteria

- shows countries from Room
- country rows are clickable
- handles loading/empty states

---

## 7.22 CountryDetailScreen.kt

### Purpose

Display country detail.

### Initial UI

Show:

```text
flag emoji
nameCa
nameEn if available
continent
subregion
iso2 / iso3
latitude / longitude if available
```

### Acceptance criteria

- displays selected country
- handles missing country
- back navigation works

---

# 8. Activity Integration

Depending on project template, likely file:

```text
MainActivity.kt
```

### Responsibilities

```text
Set content
Apply AtlasTheme
Provide app container/repositories to ViewModels
Host AtlasNavHost
Trigger dataset import
```

---

## 8.1 Dataset import trigger

For Milestone 0, simplest approach:

```text
On app startup, launch coroutine and call CountryDatasetImporter.importIfNeeded()
```

Better later:

```text
Splash/init ViewModel
```

### Acceptance criteria

- import runs before or while countries screen loads
- country list updates after import
- app does not block forever

---

# 9. ViewModel Dependency Strategy

Since Milestone 0 uses a manual container, choose one simple approach.

## Option A: custom ViewModel factory

Create factories that receive repositories.

Pros:

```text
Clean
Explicit dependencies
No DI framework
```

Cons:

```text
Some boilerplate
```

## Option B: access app container from Activity and pass lambdas/factories

Acceptable for early MVP.

### Recommendation

Use simple custom ViewModel factories for now.

---

# 10. Milestone 0 Manual Test Plan

After implementation, test:

## 10.1 First launch

Expected:

```text
App opens
Countries import
Country list shows sample countries
```

---

## 10.2 Restart app

Expected:

```text
Countries still appear
Dataset does not duplicate
Metadata exists
```

---

## 10.3 Country detail

Steps:

```text
Tap Japó
```

Expected:

```text
Country detail shows JP/JPN/Japó/Japan/Àsia
```

---

## 10.4 Missing country route

If possible, navigate to:

```text
country_detail/XX
```

Expected:

```text
Missing country state shown
No crash
```

---

# 11. Milestone 0 Acceptance Checklist

Milestone 0 can be marked complete when all are true:

```text
[ ] App builds
[ ] App launches
[ ] AtlasTheme applied
[ ] Navigation shell works
[ ] Room database initializes
[ ] CountryEntity implemented
[ ] DatasetMetadataEntity implemented
[ ] CountryDao implemented
[ ] DatasetMetadataDao implemented
[ ] countries.json exists
[ ] CountryDatasetDto parses JSON
[ ] CountryDatasetImporter imports countries
[ ] Dataset metadata written
[ ] Import is skipped on second launch
[ ] CountryRepository returns countries
[ ] CountryListViewModel exposes countries
[ ] CountryListScreen displays countries
[ ] CountryDetailViewModel loads country by ISO2
[ ] CountryDetailScreen displays selected country
[ ] Data persists after app restart
```

---

# 12. What Not to Implement in Milestone 0

Do not implement yet:

```text
wished toggle
currently living
country logs
flexible date UI
country state derivation
flights
airports
itineraries
trips
stops
excursions
maps
location search
JSON backup
photos
full country stats page
```

These come later.

---

# 13. Next After Milestone 0

After Milestone 0 is complete, begin Milestone 1:

```text
Country tracking foundation
```

Milestone 1 will add:

```text
CountryUserStateEntity
CountryLogEntity
FlexibleDate
FlexibleDateRange
Wished toggle
Currently living rule
Visit/lived logs
Country state derivation
Country filters/search
```
````](#)
