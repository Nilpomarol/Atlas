# Atlas — Photo-Inclusive Backup Spec

Last updated: 2026-06-14.

## Purpose

Make Atlas backups include the user's photos so a backup/restore is a complete,
self-contained copy of personal travel data. This is the next milestone after
v4.0 and the first concrete piece of the roadmap's **Portability** direction.

This document is the source of truth for the work. Update
`docs/Handoff_Prompt.md` when implemented status changes.

## Implementation Status

Implemented on 2026-06-14 with the `.atlasbackup` extension.

- Backup schema v3 includes stop-photo rows and trip cover filenames.
- Exports stream a ZIP containing `atlas-backup.json` and referenced user photos.
- Imports accept both `.atlasbackup` ZIP files and legacy v1/v2 JSON files.
- ZIP entries, filenames, duplicates, counts, and expanded sizes are validated.
- Missing photo files remove their rows and clear affected trip covers.
- Photo-directory replacement is rollback-capable if the Room transaction fails.
- Automated unit tests and the debug build pass. Manual device QA remains.

## Current State (baseline)

- `BackupRepository` is string-based: `exportBackupJson(): String`,
  `previewImport(json)`, `importBackupJson(json)`.
- `SettingsRoute` writes the JSON to a SAF `CreateDocument("application/json")`
  document (`atlas-backup-DATE.json`) and reads back via `OpenDocument`.
- Backup format version is **2**; v1 imports through defaults.
- **Photos are not in the backup at all** — not the binaries *and not the rows*:
  - `AtlasBackupDataV2` has no `stopPhotos` field.
  - The trip backup mapper does not carry `cover_photo_filename`.
- User photos live at `filesDir/photos/<uuid>.jpg` (downscaled to ≤1920 px,
  JPEG-80 on capture). `stop_photos` rows reference those filenames; a trip cover
  photo references one of the same filenames.
- External photo caches live elsewhere: `filesDir/country_photos` (portrait,
  Country Info) and `filesDir/country_landscape_photos` (detail hero), plus the
  `currency_rates` cache. These are re-fetchable.

## Goals

- A backup is a complete, restorable copy of all user-created data, including
  photos and trip cover references.
- Old JSON backups still import (no photos).
- No new third-party dependencies; local-first; old-backup compatibility and
  stable identifiers preserved.

## Non-Goals

- Backing up re-fetchable caches (country portrait/landscape photos, currency
  rates). They regenerate from the network; including them would bloat the file
  and risk restoring stale data.
- Cloud synchronization remains a separate non-goal. Opt-in periodic cloud backup
  was implemented later as a transport for the same `.atlasbackup` archive.

## Design

### Container: ZIP archive

Switch the export from a single JSON text document to a ZIP archive built with
`java.util.zip` (JDK built-in — no new dependency). Base64-in-JSON is rejected:
it inflates size ~33 % and forces the whole payload into memory.

Archive layout:

```text
atlas-backup.json     payload (schema v3)
photos/<uuid>.jpg     one entry per referenced user photo, streamed
```

### What is included / excluded

- Include: `stop_photos` rows + their binary files from `filesDir/photos`. Trip
  cover photos come along for free (a cover references one of those filenames).
- Exclude: country portrait photos, country landscape photos, currency rates.

### Schema: backup v3 (back-compatible)

- Add `stopPhotos: List<StopPhotoBackup>` to the backup data
  (`id, stopId, stopType, filename, sortOrder, createdAt`).
- Add `coverPhotoFilename` to the trip backup model.
- Bump `BACKUP_VERSION` to 3. v1/v2 JSON still imports (defaults, no photos).

### Export flow

1. Repo builds the JSON payload (now including `stop_photos` rows).
2. Repo writes a ZIP to the provided `OutputStream`: the json entry first, then
   streams each photo file that is actually referenced by a row (orphan files in
   `filesDir/photos` are skipped).
3. `SettingsRoute`: `CreateDocument("application/zip")`, filename
   `atlas-backup-DATE.atlasbackup`.

### Import flow (preserves preview → confirm UX)

1. UI copies the chosen document to a temp file in `cacheDir` (so it can be read
   for both preview and confirm).
2. Detect container by magic bytes (`PK\x03\x04`): ZIP → parse
   `atlas-backup.json` and stage `photos/*` into a temp dir; plain JSON → the
   existing path, no photos (legacy).
3. Preview reports counts, now including photos.
4. On confirm, stage photo files first. During the Room transaction, replace the
   structured rows and swap the staged directory into `filesDir/photos`. If the
   transaction or swap fails, restore the previous photo directory before
   returning the error. This avoids committed rows pointing at a failed media
   replacement.

### Interface change (data layer; no Android types leak)

```kotlin
interface BackupRepository {
    suspend fun exportBackup(output: OutputStream)
    suspend fun previewImport(file: File): BackupImportPreview
    suspend fun importBackup(file: File): BackupImportPreview
}
```

- UI stages the SAF stream to a temp `File` and passes that in; SAF/`Uri`
  handling stays in `SettingsRoute`.
- `BackupRepositoryImpl` gains the photos directory (inject `Context` or the
  `File`, consistent with the other repositories).
- `BackupImportPreview` gains a photo count.

### Edge cases

- Photo row whose file is missing from the archive → skip the row and clear any
  cover reference, so there are no dangling/broken images.
- Import is a full replace: existing `filesDir/photos` are cleared and restored
  from the archive, via temp-staging so an aborted import preserves current
  media.
- Stream throughout; never hold the whole archive in memory.
- Photo filenames are UUIDs, so no collisions.

## Resolved Decision

- **File extension:** `.atlasbackup`. The file remains a standard ZIP archive and
  can still be inspected with ZIP tools.

## Implementation Phases (complete)

1. **Schema + rows:** v3 models/mappers (`stopPhotos`, trip cover filename),
   include `stop_photos` in export/import row logic, version bump. Unit-tested.
2. **Zip container:** stream-based `exportBackup`/`importBackup` with
   legacy-JSON fallback; interface change to streams/`File`; wire `SettingsRoute`
   to zip output and temp-file staging.
3. **Restore + edge cases:** photo extraction into `filesDir/photos`,
   missing-file handling, preview photo counts, device QA.

## Validation

- Unit tests: v3 round-trip mappers; zip read/write; legacy-JSON detection;
  missing-photo-file handling.
- `assembleDebug` for the wiring/UI.
- Manual device QA: export a backup with photos, wipe/reinstall, import, confirm
  photos and trip covers are restored; import an old `.json` backup successfully.
