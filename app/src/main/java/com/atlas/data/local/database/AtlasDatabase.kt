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
import com.atlas.data.local.dao.TripDao
import com.atlas.data.local.dao.TripStopDao
import com.atlas.data.local.entity.AirportEntity
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
    ],
    version = 7,
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
    }
}
