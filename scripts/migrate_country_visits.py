#!/usr/bin/env python3
"""
Inserts country_visits from an old backup JSON into the Atlas app's Room database.
Only country_logs rows are written — nothing else is read, deleted, or modified.

Usage:
    python migrate_country_visits.py <path_to_old_backup.json>

Requirements:
    - Python 3.6+
    - Android device connected via ADB
    - Atlas installed as a debuggable build (development install)
"""

import json
import os
import sqlite3
import subprocess
import sys
import tempfile

PACKAGE = "com.atlas"
DB_NAME = "databases/atlas.db"

TYPE_MAP = {
    "visited": "VISIT",
    "lived": "LIVED",
}


def parse_date(date_str):
    """Parse 'YYYY', 'YYYY-MM', or 'YYYY-MM-DD' into (year, month, day, precision)."""
    if not date_str:
        return None, None, None, None
    parts = date_str.split("-")
    if len(parts) == 1:
        return int(parts[0]), None, None, "YEAR"
    elif len(parts) == 2:
        return int(parts[0]), int(parts[1]), None, "MONTH"
    else:
        return int(parts[0]), int(parts[1]), int(parts[2]), "DAY"


def convert_visit(visit):
    start_year, start_month, start_day, start_precision = parse_date(visit.get("date_start"))
    end_year, end_month, end_day, end_precision = parse_date(visit.get("date_end"))
    precision = start_precision or end_precision  # use whichever end is non-null

    log_type = TYPE_MAP.get(visit["type"])
    if not log_type:
        raise ValueError(f"Unknown visit type: {visit['type']!r}")

    created_at = visit["created_at"]
    return {
        "id": visit["id"],
        "country_iso2": visit["country_code"],
        "type": log_type,
        "start_year": start_year,
        "start_month": start_month,
        "start_day": start_day,
        "end_year": end_year,
        "end_month": end_month,
        "end_day": end_day,
        "date_precision": precision,
        "notes": visit.get("notes"),
        "created_at": created_at,
        "updated_at": created_at,
    }


def adb_capture(args):
    result = subprocess.run(["adb"] + args, capture_output=True)
    if result.returncode != 0:
        raise RuntimeError(f"adb error: {result.stderr.decode().strip()}")
    return result.stdout


def adb_run(args):
    result = subprocess.run(["adb"] + args)
    if result.returncode != 0:
        raise RuntimeError(f"adb command failed: {' '.join(args)}")


def main():
    if len(sys.argv) < 2:
        print("Usage: python migrate_country_visits.py <backup_json_path>")
        sys.exit(1)

    backup_path = sys.argv[1]
    with open(backup_path, encoding="utf-8") as f:
        backup = json.load(f)

    visits = backup["data"]["data"]["country_visits"]
    print(f"Found {len(visits)} country_visits to migrate.")
    logs = [convert_visit(v) for v in visits]

    # Stop the app so Room releases the database
    print("Stopping app...")
    adb_run(["shell", "am", "force-stop", PACKAGE])

    # Pull database into a temp directory (include WAL so Python sqlite3 can merge it)
    tmp_dir = tempfile.mkdtemp()
    db_local = os.path.join(tmp_dir, "atlas.db")
    wal_local = os.path.join(tmp_dir, "atlas.db-wal")
    shm_local = os.path.join(tmp_dir, "atlas.db-shm")

    print("Pulling database...")
    db_data = adb_capture(["exec-out", f"run-as {PACKAGE} cat {DB_NAME}"])
    with open(db_local, "wb") as f:
        f.write(db_data)
    print(f"  atlas.db: {len(db_data):,} bytes")

    for suffix, local in [("-wal", wal_local), ("-shm", shm_local)]:
        try:
            data = adb_capture(["exec-out", f"run-as {PACKAGE} cat {DB_NAME}{suffix}"])
            if data:
                with open(local, "wb") as f:
                    f.write(data)
                print(f"  atlas.db{suffix}: {len(data):,} bytes")
        except Exception:
            pass  # WAL/SHM may not exist if app was idle

    # Insert records
    conn = sqlite3.connect(db_local)
    cur = conn.cursor()

    inserted = 0
    skipped = 0
    for log in logs:
        cur.execute(
            """
            INSERT OR IGNORE INTO country_logs
                (id, country_iso2, type,
                 start_year, start_month, start_day,
                 end_year, end_month, end_day,
                 date_precision, notes, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                log["id"], log["country_iso2"], log["type"],
                log["start_year"], log["start_month"], log["start_day"],
                log["end_year"], log["end_month"], log["end_day"],
                log["date_precision"], log["notes"],
                log["created_at"], log["updated_at"],
            ),
        )
        if cur.rowcount:
            inserted += 1
            print(f"  + {log['country_iso2']:4}  {log['type']:7}  {log.get('start_year', '?')}")
        else:
            skipped += 1
            print(f"  ~ {log['country_iso2']:4}  {log['type']:7}  (already exists, skipped)")

    conn.commit()
    # Switch to rollback journal so we can push a single clean .db file
    conn.execute("PRAGMA journal_mode=DELETE")
    conn.close()

    print(f"\nInserted: {inserted}  |  Skipped (already existed): {skipped}")

    # Push modified DB back
    print("Pushing database back to device...")
    sdcard_tmp = "/data/local/tmp/atlas_migrate_tmp.db"
    adb_run(["push", db_local, sdcard_tmp])
    adb_run(["shell", f"run-as {PACKAGE} cp {sdcard_tmp} {DB_NAME}"])
    adb_run(["shell", f"rm {sdcard_tmp}"])

    # Remove stale WAL/SHM from device so Room starts clean
    for suffix in ["-wal", "-shm"]:
        subprocess.run(["adb", "shell", f"run-as {PACKAGE} rm -f {DB_NAME}{suffix}"])

    # Cleanup temp files
    for path in [db_local, wal_local, shm_local]:
        if os.path.exists(path):
            os.unlink(path)
    os.rmdir(tmp_dir)

    print("Done. Reopen the app.")


if __name__ == "__main__":
    main()
