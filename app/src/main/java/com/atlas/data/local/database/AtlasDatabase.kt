package com.atlas.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.atlas.data.local.dao.CountryDao
import com.atlas.data.local.dao.CountryLogDao
import com.atlas.data.local.dao.CountryUserStateDao
import com.atlas.data.local.dao.DatasetMetadataDao
import com.atlas.data.local.dao.AirportDao
import com.atlas.data.local.dao.ExcursionDao
import com.atlas.data.local.dao.FlightDao
import com.atlas.data.local.dao.ItineraryDao
import com.atlas.data.local.dao.TripDao
import com.atlas.data.local.dao.TripStopDao
import com.atlas.data.local.entity.AirportEntity
import com.atlas.data.local.entity.ExcursionEntity
import com.atlas.data.local.entity.ExcursionStopEntity
import com.atlas.data.local.entity.FlightEntity
import com.atlas.data.local.entity.ItineraryEntity
import com.atlas.data.local.entity.ItineraryGroupEntity
import com.atlas.data.local.entity.CountryEntity
import com.atlas.data.local.entity.CountryLogEntity
import com.atlas.data.local.entity.CountryUserStateEntity
import com.atlas.data.local.entity.DatasetMetadataEntity
import com.atlas.data.local.entity.TripEntity
import com.atlas.data.local.entity.TripStopEntity

@Database(
    entities = [
        CountryEntity::class,
        DatasetMetadataEntity::class,
        CountryUserStateEntity::class,
        CountryLogEntity::class,
        TripEntity::class,
        TripStopEntity::class,
        AirportEntity::class,
        FlightEntity::class,
        ItineraryEntity::class,
        ItineraryGroupEntity::class,
        ExcursionEntity::class,
        ExcursionStopEntity::class,
    ],
    version = 14,
    exportSchema = true,
)
abstract class AtlasDatabase : RoomDatabase() {
    abstract fun countryDao(): CountryDao
    abstract fun countryLogDao(): CountryLogDao
    abstract fun countryUserStateDao(): CountryUserStateDao
    abstract fun datasetMetadataDao(): DatasetMetadataDao
    abstract fun tripDao(): TripDao
    abstract fun tripStopDao(): TripStopDao
    abstract fun airportDao(): AirportDao
    abstract fun flightDao(): FlightDao
    abstract fun itineraryDao(): ItineraryDao
    abstract fun excursionDao(): ExcursionDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `country_user_states` (
                        `country_iso2` TEXT NOT NULL,
                        `wished` INTEGER NOT NULL,
                        `currently_living` INTEGER NOT NULL,
                        `updated_at` TEXT NOT NULL,
                        PRIMARY KEY(`country_iso2`),
                        FOREIGN KEY(`country_iso2`) REFERENCES `countries`(`iso2`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_country_user_states_wished` ON `country_user_states` (`wished`)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_country_user_states_currently_living` ON `country_user_states` (`currently_living`)",
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `country_logs` (
                        `id` TEXT NOT NULL,
                        `country_iso2` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `start_year` INTEGER,
                        `start_month` INTEGER,
                        `start_day` INTEGER,
                        `end_year` INTEGER,
                        `end_month` INTEGER,
                        `end_day` INTEGER,
                        `date_precision` TEXT,
                        `notes` TEXT,
                        `created_at` TEXT NOT NULL,
                        `updated_at` TEXT NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`country_iso2`) REFERENCES `countries`(`iso2`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_country_logs_country_iso2` ON `country_logs` (`country_iso2`)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_country_logs_type` ON `country_logs` (`type`)",
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `trips` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `start_year` INTEGER,
                        `start_month` INTEGER,
                        `start_day` INTEGER,
                        `end_year` INTEGER,
                        `end_month` INTEGER,
                        `end_day` INTEGER,
                        `date_precision` TEXT,
                        `notes` TEXT,
                        `created_at` TEXT NOT NULL,
                        `updated_at` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_trips_status` ON `trips` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_trips_title` ON `trips` (`title`)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `trip_stops` (
                        `id` TEXT NOT NULL,
                        `trip_id` TEXT NOT NULL,
                        `location_name` TEXT NOT NULL,
                        `country_iso2` TEXT NOT NULL,
                        `latitude` REAL,
                        `longitude` REAL,
                        `start_year` INTEGER,
                        `start_month` INTEGER,
                        `start_day` INTEGER,
                        `end_year` INTEGER,
                        `end_month` INTEGER,
                        `end_day` INTEGER,
                        `date_precision` TEXT,
                        `notes` TEXT,
                        `sort_order` INTEGER NOT NULL,
                        `created_at` TEXT NOT NULL,
                        `updated_at` TEXT NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`trip_id`) REFERENCES `trips`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`country_iso2`) REFERENCES `countries`(`iso2`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_trip_stops_trip_id` ON `trip_stops` (`trip_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_trip_stops_country_iso2` ON `trip_stops` (`country_iso2`)")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_trip_stops_trip_id_sort_order` ON `trip_stops` (`trip_id`, `sort_order`)",
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `countries` ADD COLUMN `capital_name_ca` TEXT")
                db.execSQL("ALTER TABLE `countries` ADD COLUMN `capital_name_en` TEXT")
                db.execSQL("ALTER TABLE `countries` ADD COLUMN `capital_latitude` REAL")
                db.execSQL("ALTER TABLE `countries` ADD COLUMN `capital_longitude` REAL")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `airports` (
                        `id` TEXT NOT NULL,
                        `iata` TEXT,
                        `icao` TEXT,
                        `name` TEXT NOT NULL,
                        `city` TEXT NOT NULL,
                        `country_iso2` TEXT NOT NULL,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `timezone` TEXT,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`country_iso2`) REFERENCES `countries`(`iso2`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_airports_iata` ON `airports` (`iata`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_airports_icao` ON `airports` (`icao`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_airports_name` ON `airports` (`name`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_airports_city` ON `airports` (`city`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_airports_country_iso2` ON `airports` (`country_iso2`)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `flights` (
                        `id` TEXT NOT NULL,
                        `origin_airport_id` TEXT NOT NULL,
                        `destination_airport_id` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `scheduled_departure_at` TEXT,
                        `scheduled_arrival_at` TEXT,
                        `actual_departure_at` TEXT,
                        `actual_arrival_at` TEXT,
                        `airline` TEXT,
                        `flight_number` TEXT,
                        `aircraft` TEXT,
                        `notes` TEXT,
                        `created_at` TEXT NOT NULL,
                        `updated_at` TEXT NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`origin_airport_id`) REFERENCES `airports`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT,
                        FOREIGN KEY(`destination_airport_id`) REFERENCES `airports`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_flights_origin_airport_id` ON `flights` (`origin_airport_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_flights_destination_airport_id` ON `flights` (`destination_airport_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_flights_status` ON `flights` (`status`)")
            }
        }

        // Replaces the short-lived v8 schema (year/month/day columns) with
        // datetime strings for all four flight time fields.
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `flights`")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `flights` (
                        `id` TEXT NOT NULL,
                        `origin_airport_id` TEXT NOT NULL,
                        `destination_airport_id` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `scheduled_departure_at` TEXT,
                        `scheduled_arrival_at` TEXT,
                        `actual_departure_at` TEXT,
                        `actual_arrival_at` TEXT,
                        `airline` TEXT,
                        `flight_number` TEXT,
                        `aircraft` TEXT,
                        `notes` TEXT,
                        `created_at` TEXT NOT NULL,
                        `updated_at` TEXT NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`origin_airport_id`) REFERENCES `airports`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT,
                        FOREIGN KEY(`destination_airport_id`) REFERENCES `airports`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_flights_origin_airport_id` ON `flights` (`origin_airport_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_flights_destination_airport_id` ON `flights` (`destination_airport_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_flights_status` ON `flights` (`status`)")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create itineraries table first (needed for itinerary_groups FK)
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `itineraries` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `trip_id` TEXT,
                        `notes` TEXT,
                        `created_at` TEXT NOT NULL,
                        `updated_at` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_itineraries_trip_id` ON `itineraries` (`trip_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_itineraries_title` ON `itineraries` (`title`)")

                // Create itinerary_groups table (needed for flights FK)
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `itinerary_groups` (
                        `id` TEXT NOT NULL,
                        `itinerary_id` TEXT NOT NULL,
                        `title` TEXT,
                        `status` TEXT,
                        `sort_order` INTEGER NOT NULL,
                        `created_at` TEXT NOT NULL,
                        `updated_at` TEXT NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`itinerary_id`) REFERENCES `itineraries`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_itinerary_groups_itinerary_id` ON `itinerary_groups` (`itinerary_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_itinerary_groups_sort_order` ON `itinerary_groups` (`sort_order`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_itinerary_groups_status` ON `itinerary_groups` (`status`)")

                // Recreate flights with the new itinerary_group_id FK + sort_order column.
                // ALTER TABLE cannot add FK constraints in SQLite, so we drop and recreate.
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `flights_new` (
                        `id` TEXT NOT NULL,
                        `origin_airport_id` TEXT NOT NULL,
                        `destination_airport_id` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `scheduled_departure_at` TEXT,
                        `scheduled_arrival_at` TEXT,
                        `actual_departure_at` TEXT,
                        `actual_arrival_at` TEXT,
                        `airline` TEXT,
                        `flight_number` TEXT,
                        `aircraft` TEXT,
                        `notes` TEXT,
                        `itinerary_group_id` TEXT,
                        `sort_order` INTEGER,
                        `created_at` TEXT NOT NULL,
                        `updated_at` TEXT NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`origin_airport_id`) REFERENCES `airports`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT,
                        FOREIGN KEY(`destination_airport_id`) REFERENCES `airports`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT,
                        FOREIGN KEY(`itinerary_group_id`) REFERENCES `itinerary_groups`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO `flights_new`
                        (id, origin_airport_id, destination_airport_id, status,
                         scheduled_departure_at, scheduled_arrival_at,
                         actual_departure_at, actual_arrival_at,
                         airline, flight_number, aircraft, notes,
                         itinerary_group_id, sort_order, created_at, updated_at)
                    SELECT id, origin_airport_id, destination_airport_id, status,
                           scheduled_departure_at, scheduled_arrival_at,
                           actual_departure_at, actual_arrival_at,
                           airline, flight_number, aircraft, notes,
                           NULL, NULL, created_at, updated_at
                    FROM `flights`
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE `flights`")
                db.execSQL("ALTER TABLE `flights_new` RENAME TO `flights`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_flights_origin_airport_id` ON `flights` (`origin_airport_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_flights_destination_airport_id` ON `flights` (`destination_airport_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_flights_status` ON `flights` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_flights_itinerary_group_id` ON `flights` (`itinerary_group_id`)")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `trip_stops` ADD COLUMN `source` TEXT NOT NULL DEFAULT 'MANUAL'")
                db.execSQL("ALTER TABLE `trip_stops` ADD COLUMN `itinerary_group_id` TEXT")
                db.execSQL("ALTER TABLE `trip_stops` ADD COLUMN `is_visible` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `trip_stops` ADD COLUMN `display_title` TEXT")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_trip_stops_itinerary_group_id` ON `trip_stops` (`itinerary_group_id`)",
                )
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `excursions` (
                        `id` TEXT NOT NULL,
                        `trip_id` TEXT NOT NULL,
                        `anchor_trip_stop_id` TEXT,
                        `title` TEXT NOT NULL,
                        `notes` TEXT,
                        `sort_order` INTEGER NOT NULL,
                        `created_at` TEXT NOT NULL,
                        `updated_at` TEXT NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`trip_id`) REFERENCES `trips`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`anchor_trip_stop_id`) REFERENCES `trip_stops`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_excursions_trip_id` ON `excursions` (`trip_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_excursions_anchor_trip_stop_id` ON `excursions` (`anchor_trip_stop_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_excursions_trip_id_sort_order` ON `excursions` (`trip_id`, `sort_order`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `excursion_stops` (
                        `id` TEXT NOT NULL,
                        `excursion_id` TEXT NOT NULL,
                        `location_name` TEXT NOT NULL,
                        `country_iso2` TEXT NOT NULL,
                        `latitude` REAL,
                        `longitude` REAL,
                        `start_year` INTEGER,
                        `start_month` INTEGER,
                        `start_day` INTEGER,
                        `end_year` INTEGER,
                        `end_month` INTEGER,
                        `end_day` INTEGER,
                        `date_precision` TEXT,
                        `notes` TEXT,
                        `sort_order` INTEGER NOT NULL,
                        `created_at` TEXT NOT NULL,
                        `updated_at` TEXT NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`excursion_id`) REFERENCES `excursions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`country_iso2`) REFERENCES `countries`(`iso2`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_excursion_stops_excursion_id` ON `excursion_stops` (`excursion_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_excursion_stops_country_iso2` ON `excursion_stops` (`country_iso2`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_excursion_stops_excursion_id_sort_order` ON `excursion_stops` (`excursion_id`, `sort_order`)")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val stopColumns = db.query("PRAGMA table_info(`excursion_stops`)").use { cursor ->
                    buildSet {
                        val nameIndex = cursor.getColumnIndex("name")
                        while (cursor.moveToNext()) {
                            add(cursor.getString(nameIndex))
                        }
                    }
                }
                val hasStopDates = "start_year" in stopColumns &&
                    "start_month" in stopColumns &&
                    "start_day" in stopColumns &&
                    "end_year" in stopColumns &&
                    "end_month" in stopColumns &&
                    "end_day" in stopColumns &&
                    "date_precision" in stopColumns

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `excursions_new` (
                        `id` TEXT NOT NULL,
                        `trip_id` TEXT NOT NULL,
                        `anchor_trip_stop_id` TEXT,
                        `title` TEXT NOT NULL,
                        `notes` TEXT,
                        `sort_order` INTEGER NOT NULL,
                        `created_at` TEXT NOT NULL,
                        `updated_at` TEXT NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`trip_id`) REFERENCES `trips`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`anchor_trip_stop_id`) REFERENCES `trip_stops`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO `excursions_new` (
                        `id`,
                        `trip_id`,
                        `anchor_trip_stop_id`,
                        `title`,
                        `notes`,
                        `sort_order`,
                        `created_at`,
                        `updated_at`
                    )
                    SELECT
                        `id`,
                        `trip_id`,
                        `anchor_trip_stop_id`,
                        `title`,
                        `notes`,
                        `sort_order`,
                        `created_at`,
                        `updated_at`
                    FROM `excursions`
                    """.trimIndent(),
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `excursion_stops_new` (
                        `id` TEXT NOT NULL,
                        `excursion_id` TEXT NOT NULL,
                        `location_name` TEXT NOT NULL,
                        `country_iso2` TEXT NOT NULL,
                        `latitude` REAL,
                        `longitude` REAL,
                        `start_year` INTEGER,
                        `start_month` INTEGER,
                        `start_day` INTEGER,
                        `end_year` INTEGER,
                        `end_month` INTEGER,
                        `end_day` INTEGER,
                        `date_precision` TEXT,
                        `notes` TEXT,
                        `sort_order` INTEGER NOT NULL,
                        `created_at` TEXT NOT NULL,
                        `updated_at` TEXT NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`excursion_id`) REFERENCES `excursions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`country_iso2`) REFERENCES `countries`(`iso2`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                    """.trimIndent(),
                )
                if (hasStopDates) {
                    db.execSQL(
                        """
                        INSERT INTO `excursion_stops_new` (
                            `id`,
                            `excursion_id`,
                            `location_name`,
                            `country_iso2`,
                            `latitude`,
                            `longitude`,
                            `start_year`,
                            `start_month`,
                            `start_day`,
                            `end_year`,
                            `end_month`,
                            `end_day`,
                            `date_precision`,
                            `notes`,
                            `sort_order`,
                            `created_at`,
                            `updated_at`
                        )
                        SELECT
                            `id`,
                            `excursion_id`,
                            `location_name`,
                            `country_iso2`,
                            `latitude`,
                            `longitude`,
                            `start_year`,
                            `start_month`,
                            `start_day`,
                            `end_year`,
                            `end_month`,
                            `end_day`,
                            `date_precision`,
                            `notes`,
                            `sort_order`,
                            `created_at`,
                            `updated_at`
                        FROM `excursion_stops`
                        """.trimIndent(),
                    )
                } else {
                    db.execSQL(
                        """
                        INSERT INTO `excursion_stops_new` (
                            `id`,
                            `excursion_id`,
                            `location_name`,
                            `country_iso2`,
                            `latitude`,
                            `longitude`,
                            `start_year`,
                            `start_month`,
                            `start_day`,
                            `end_year`,
                            `end_month`,
                            `end_day`,
                            `date_precision`,
                            `notes`,
                            `sort_order`,
                            `created_at`,
                            `updated_at`
                        )
                        SELECT
                            `id`,
                            `excursion_id`,
                            `location_name`,
                            `country_iso2`,
                            `latitude`,
                            `longitude`,
                            NULL,
                            NULL,
                            NULL,
                            NULL,
                            NULL,
                            NULL,
                            NULL,
                            `notes`,
                            `sort_order`,
                            `created_at`,
                            `updated_at`
                        FROM `excursion_stops`
                        """.trimIndent(),
                    )
                }

                db.execSQL("DROP TABLE `excursion_stops`")
                db.execSQL("DROP TABLE `excursions`")
                db.execSQL("ALTER TABLE `excursions_new` RENAME TO `excursions`")
                db.execSQL("ALTER TABLE `excursion_stops_new` RENAME TO `excursion_stops`")

                db.execSQL("CREATE INDEX IF NOT EXISTS `index_excursions_trip_id` ON `excursions` (`trip_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_excursions_anchor_trip_stop_id` ON `excursions` (`anchor_trip_stop_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_excursions_trip_id_sort_order` ON `excursions` (`trip_id`, `sort_order`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_excursion_stops_excursion_id` ON `excursion_stops` (`excursion_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_excursion_stops_country_iso2` ON `excursion_stops` (`country_iso2`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_excursion_stops_excursion_id_sort_order` ON `excursion_stops` (`excursion_id`, `sort_order`)")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add flight provenance columns — no FK constraints, so plain ALTER TABLE is safe
                db.execSQL("ALTER TABLE `flights` ADD COLUMN `fetched_from` TEXT NOT NULL DEFAULT 'manual'")
                db.execSQL("ALTER TABLE `flights` ADD COLUMN `external_provider` TEXT")
                db.execSQL("ALTER TABLE `flights` ADD COLUMN `external_id` TEXT")
            }
        }
    }
}
