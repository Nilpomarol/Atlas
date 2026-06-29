# Atlas Backup, Export, and Import Contract

Last updated: 2026-06-24.

## Purpose

Atlas is a released, local-first app. Backup and restore must protect the user's
personal travel history, including app-private photos, without requiring a backend or
platform auto-restore.

This document describes the current implemented contract. It is not a proposal.

## Current Implementation

- Backup format version: **3**.
- Export file extension: `.atlasbackup`.
- Container: standard ZIP archive.
- Payload entry: `atlas-backup.json`.
- Photo entries: `photos/<uuid>.jpg`.
- Legacy imports: v1/v2 JSON backups remain supported.
- Android Auto Backup is disabled; explicit Atlas backup/import is the supported
  portability path.
- Optional cloud backup writes the same `.atlasbackup` archive through the Android
  Storage Access Framework. It does not introduce a second data model or a cloud sync
  protocol.

Implemented code lives in:

- `data/repository/BackupRepositoryImpl.kt`
- `data/backup/AtlasBackupV1.kt`
- `data/backup/AtlasBackupV2.kt`
- `data/backup/AtlasBackupV3.kt`
- `data/backup/BackupArchive.kt`
- `data/backup/BackupValidation.kt`
- `data/backup/BackupMappers.kt`
- `data/backup/BackupPhotoSanitizer.kt`

## Archive Layout

```text
atlas-backup.json
photos/<uuid>.jpg
```

The archive is streamed with JDK ZIP APIs. Photo binaries are not base64-encoded into
JSON.

## Backup v3 Payload

`atlas-backup.json` uses `AtlasBackupV3`:

```text
backupVersion
createdAt
countryDatasetVersion
airportDatasetVersion
data
```

`data` currently includes:

- country user states
- country logs
- trips, including `coverPhotoFilename`
- trip stops
- flights
- itineraries
- itinerary groups
- excursions
- excursion stops
- stop photos

`stopPhotos` rows carry:

```text
id
stopId
stopType
filename
sortOrder
createdAt
```

## Included Data

Backups include user-created travel data and referenced user photo files:

- wished/currently-living country state
- country visit/lived logs
- trips, trip stops, excursions, and excursion stops
- flights and itineraries
- stop-photo rows
- referenced files from `filesDir/photos`
- trip cover filename references, when valid

## Excluded Data

Backups intentionally exclude replaceable or static data:

- bundled countries, airports, airlines, aircraft types, and country facts datasets
- dataset rows installed from assets
- country portrait photo cache
- country landscape photo cache
- currency-rate cache
- API keys and integration preferences
- cloud backup settings/status

Reference data is reinstalled from bundled assets. Replaceable caches can be
refreshed. Personal travel records use stable identifiers where possible so dataset
updates do not overwrite user-created history.

## Export Rules

1. Build the v3 JSON payload from current Room rows.
2. Include only stop-photo rows whose referenced files exist.
3. Clear invalid trip cover references through the same sanitization logic.
4. Write `atlas-backup.json`.
5. Stream each referenced photo file under `photos/`.

The current UI exports with MIME type `application/zip` and the filename pattern:

```text
atlas-backup-DATE.atlasbackup
```

## Import Rules

1. The UI copies the selected SAF document to a temporary file.
2. `BackupArchive` detects ZIP vs legacy JSON by magic bytes.
3. ZIP archives must contain exactly one `atlas-backup.json` payload and only valid
   `photos/<uuid>.jpg` photo entries.
4. JSON is decoded with known backup models and validated.
5. v1/v2 JSON imports are accepted through default values; they contain no photo
   files.
6. v3 imports validate relationships, enum values, date ranges, country ISO2 values,
   photo filename shape, duplicate IDs, and archive size limits.
7. Photo rows whose files are absent from the archive are sanitized out, and affected
   cover references are cleared.
8. Import is a full replace of user-created rows.
9. Photo files are staged before the Room transaction. If the transaction or photo
   directory swap fails, the previous photo directory is restored.

## Safety Limits

The archive reader rejects invalid or risky inputs, including:

- duplicate ZIP entries
- unknown ZIP entries
- absolute paths, backslashes, or `..` path traversal
- non-UUID photo filenames
- non-JPG photo filenames
- duplicate photo filenames
- excessive JSON, photo, total expanded size, or photo-entry count
- malformed JSON
- unsupported backup versions

## Optional Cloud Backup

Cloud backup is an opt-in transport for the same v3 archive:

- the user chooses a document-provider folder;
- Atlas persists folder access through SAF;
- WorkManager can run monthly backups on an unmetered network with battery/storage
  constraints;
- automatic retention keeps the newest three automatic backups;
- manual exports are not deleted by automatic retention.

No Google Drive SDK, account model, backend, or continuous sync protocol is part of
the current implementation.

## Compatibility Rules

- Do not break v1/v2 JSON import compatibility.
- Do not break v3 `.atlasbackup` import compatibility.
- A future backup v4 must preserve defaults for older imports and keep v3 files
  importable.
- Adding persisted user data requires a backup review and round-trip tests.
- Changing photo ownership, filenames, or directory behavior requires explicit
  migration and restore testing.
- Do not include replaceable caches unless there is a concrete product reason and a
  compatibility plan.

## Validation Expectations

For backup-related changes, run or update focused tests for:

- schema compatibility;
- v3 mapper round-trips;
- v1/v2 legacy imports;
- ZIP archive validation;
- missing-photo sanitization;
- photo directory rollback behavior;
- cloud backup retention logic, when affected.

Also run the smallest relevant Android build/check and manually review restore flows
when file handling changes.
