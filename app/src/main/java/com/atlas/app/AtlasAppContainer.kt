package com.atlas.app

import android.content.Context
import androidx.room.Room
import com.atlas.data.dataset.CountryDatasetImporter
import com.atlas.data.location.NominatimLocationSearchRepository
import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.repository.CountryRepositoryImpl
import com.atlas.data.repository.BackupRepositoryImpl
import com.atlas.data.repository.TripRepositoryImpl
import com.atlas.domain.repository.BackupRepository
import com.atlas.domain.repository.CountryRepository
import com.atlas.domain.repository.LocationSearchRepository
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.service.CountryStateDerivationService
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.domain.usecase.country.AddCountryLogUseCase
import com.atlas.domain.usecase.country.DeleteCountryLogUseCase
import com.atlas.domain.usecase.country.SetCurrentlyLivingCountryUseCase
import com.atlas.domain.usecase.country.ToggleWishedCountryUseCase
import com.atlas.domain.usecase.country.UpdateCountryLogUseCase
import com.atlas.domain.usecase.location.SearchLocationsUseCase
import com.atlas.domain.usecase.trip.CreateTripUseCase
import com.atlas.domain.usecase.trip.CreateTripStopUseCase
import com.atlas.domain.usecase.trip.DeleteTripUseCase
import com.atlas.domain.usecase.trip.DeleteTripStopUseCase
import com.atlas.domain.usecase.trip.ReorderTripStopsUseCase
import com.atlas.domain.usecase.trip.UpdateTripStopUseCase
import com.atlas.domain.usecase.trip.UpdateTripUseCase
import com.atlas.domain.validation.FlexibleDateValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AtlasAppContainer(context: Context) {
    private val applicationContext = context.applicationContext
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: AtlasDatabase = Room.databaseBuilder(
        context = applicationContext,
        klass = AtlasDatabase::class.java,
        name = DATABASE_NAME,
    )
        .addMigrations(AtlasDatabase.MIGRATION_1_2)
        .addMigrations(AtlasDatabase.MIGRATION_2_3)
        .addMigrations(AtlasDatabase.MIGRATION_3_4)
        .addMigrations(AtlasDatabase.MIGRATION_4_5)
        .addMigrations(AtlasDatabase.MIGRATION_5_6)
        .build()

    private val countryDatasetImporter = CountryDatasetImporter(
        context = applicationContext,
        database = database,
    )

    val countryRepository: CountryRepository = CountryRepositoryImpl(
        database = database,
    )

    val tripRepository: TripRepository = TripRepositoryImpl(
        database = database,
    )

    val backupRepository: BackupRepository = BackupRepositoryImpl(
        database = database,
    )

    val locationSearchRepository: LocationSearchRepository = NominatimLocationSearchRepository()

    val countryStateDerivationService = CountryStateDerivationService()
    val flexibleDateValidator = FlexibleDateValidator()
    val flexibleDateFormatter = FlexibleDateFormatter()

    val toggleWishedCountryUseCase = ToggleWishedCountryUseCase(
        countryRepository = countryRepository,
    )

    val setCurrentlyLivingCountryUseCase = SetCurrentlyLivingCountryUseCase(
        countryRepository = countryRepository,
    )

    val addCountryLogUseCase = AddCountryLogUseCase(
        countryRepository = countryRepository,
    )

    val deleteCountryLogUseCase = DeleteCountryLogUseCase(
        countryRepository = countryRepository,
    )

    val updateCountryLogUseCase = UpdateCountryLogUseCase(
        countryRepository = countryRepository,
    )

    val createTripUseCase = CreateTripUseCase(
        tripRepository = tripRepository,
    )

    val updateTripUseCase = UpdateTripUseCase(
        tripRepository = tripRepository,
    )

    val deleteTripUseCase = DeleteTripUseCase(
        tripRepository = tripRepository,
    )

    val createTripStopUseCase = CreateTripStopUseCase(
        tripRepository = tripRepository,
    )

    val deleteTripStopUseCase = DeleteTripStopUseCase(
        tripRepository = tripRepository,
    )

    val updateTripStopUseCase = UpdateTripStopUseCase(
        tripRepository = tripRepository,
    )

    val reorderTripStopsUseCase = ReorderTripStopsUseCase(
        tripRepository = tripRepository,
    )

    val searchLocationsUseCase = SearchLocationsUseCase(
        locationSearchRepository = locationSearchRepository,
    )

    fun importInitialData() {
        applicationScope.launch {
            countryDatasetImporter.importIfNeeded()
        }
    }

    companion object {
        private const val DATABASE_NAME = "atlas.db"
    }
}
