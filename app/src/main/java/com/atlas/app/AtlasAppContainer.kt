package com.atlas.app

import android.content.Context
import androidx.room.Room
import com.atlas.data.api.AeroDataBoxClient
import com.atlas.data.dataset.AirlineDatasetImporter
import com.atlas.data.dataset.AirportDatasetImporter
import com.atlas.data.dataset.CountryDatasetImporter
import com.atlas.data.location.NominatimLocationSearchRepository
import com.atlas.data.preferences.ApiKeyPreferencesDataSource
import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.repository.AirlineRepositoryImpl
import com.atlas.data.repository.AirportRepositoryImpl
import com.atlas.data.repository.CountryRepositoryImpl
import com.atlas.data.repository.BackupRepositoryImpl
import com.atlas.data.repository.ExcursionRepositoryImpl
import com.atlas.data.repository.FlightRepositoryImpl
import com.atlas.data.repository.ItineraryRepositoryImpl
import com.atlas.data.repository.TripRepositoryImpl
import com.atlas.domain.repository.AirlineRepository
import com.atlas.domain.repository.ApiKeyRepository
import com.atlas.domain.repository.AirportRepository
import com.atlas.domain.repository.BackupRepository
import com.atlas.domain.repository.CountryRepository
import com.atlas.domain.repository.FlightApiClient
import com.atlas.domain.repository.ExcursionRepository
import com.atlas.domain.repository.FlightRepository
import com.atlas.domain.repository.ItineraryRepository
import com.atlas.domain.repository.LocationSearchRepository
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.service.CountryStateDerivationService
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.domain.service.ItineraryGeneratedStopService
import com.atlas.domain.usecase.airline.SearchAirlinesUseCase
import com.atlas.domain.usecase.country.AddCountryLogUseCase
import com.atlas.domain.usecase.country.DeleteCountryLogUseCase
import com.atlas.domain.usecase.country.SetCurrentlyLivingCountryUseCase
import com.atlas.domain.usecase.country.ToggleWishedCountryUseCase
import com.atlas.domain.usecase.country.UpdateCountryLogUseCase
import com.atlas.domain.usecase.airport.SearchAirportsUseCase
import com.atlas.domain.usecase.excursion.CreateExcursionStopUseCase
import com.atlas.domain.usecase.excursion.CreateExcursionUseCase
import com.atlas.domain.usecase.excursion.DeleteExcursionStopUseCase
import com.atlas.domain.usecase.excursion.DeleteExcursionUseCase
import com.atlas.domain.usecase.excursion.ReorderExcursionStopsUseCase
import com.atlas.domain.usecase.excursion.ReorderExcursionsUseCase
import com.atlas.domain.usecase.excursion.UpdateExcursionStopUseCase
import com.atlas.domain.usecase.excursion.UpdateExcursionUseCase
import com.atlas.domain.usecase.flight.CreateFlightUseCase
import com.atlas.domain.usecase.flight.DeleteFlightUseCase
import com.atlas.domain.usecase.flight.LookupFlightUseCase
import com.atlas.domain.usecase.flight.UpdateFlightUseCase
import com.atlas.domain.usecase.itinerary.CreateItineraryGroupUseCase
import com.atlas.domain.usecase.itinerary.CreateItineraryUseCase
import com.atlas.domain.usecase.itinerary.DeleteItineraryGroupUseCase
import com.atlas.domain.usecase.itinerary.DeleteItineraryUseCase
import com.atlas.domain.usecase.itinerary.ReorderGroupFlightsUseCase
import com.atlas.domain.usecase.itinerary.ReorderItineraryGroupsUseCase
import com.atlas.domain.usecase.itinerary.UpdateItineraryGroupUseCase
import com.atlas.domain.usecase.itinerary.UpdateItineraryUseCase
import com.atlas.domain.usecase.itinerary.RemoveGeneratedTripStopsForItineraryUseCase
import com.atlas.domain.usecase.itinerary.SyncGeneratedTripStopsForItineraryUseCase
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
        .addMigrations(AtlasDatabase.MIGRATION_6_7)
        .addMigrations(AtlasDatabase.MIGRATION_7_8)
        .addMigrations(AtlasDatabase.MIGRATION_8_9)
        .addMigrations(AtlasDatabase.MIGRATION_9_10)
        .addMigrations(AtlasDatabase.MIGRATION_10_11)
        .addMigrations(AtlasDatabase.MIGRATION_11_12)
        .addMigrations(AtlasDatabase.MIGRATION_12_13)
        .addMigrations(AtlasDatabase.MIGRATION_13_14)
        .addMigrations(AtlasDatabase.MIGRATION_14_15)
        .build()

    private val countryDatasetImporter = CountryDatasetImporter(
        context = applicationContext,
        database = database,
    )

    private val airportDatasetImporter = AirportDatasetImporter(
        context = applicationContext,
        database = database,
    )

    private val airlineDatasetImporter = AirlineDatasetImporter(
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

    val airportRepository: AirportRepository = AirportRepositoryImpl(
        database = database,
    )

    val airlineRepository: AirlineRepository = AirlineRepositoryImpl(
        database = database,
    )

    val flightRepository: FlightRepository = FlightRepositoryImpl(
        database = database,
    )

    val itineraryRepository: ItineraryRepository = ItineraryRepositoryImpl(
        database = database,
    )

    val excursionRepository: ExcursionRepository = ExcursionRepositoryImpl(
        database = database,
    )

    val locationSearchRepository: LocationSearchRepository = NominatimLocationSearchRepository()

    val apiKeyRepository: ApiKeyRepository = ApiKeyPreferencesDataSource(applicationContext)
    val flightApiClient: FlightApiClient = AeroDataBoxClient()

    val countryStateDerivationService = CountryStateDerivationService()
    val itineraryGeneratedStopService = ItineraryGeneratedStopService()
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

    val searchAirportsUseCase = SearchAirportsUseCase(
        airportRepository = airportRepository,
    )

    val searchAirlinesUseCase = SearchAirlinesUseCase(
        airlineRepository = airlineRepository,
    )

    val createFlightUseCase = CreateFlightUseCase(
        flightRepository = flightRepository,
    )

    val updateFlightUseCase = UpdateFlightUseCase(
        flightRepository = flightRepository,
    )

    val deleteFlightUseCase = DeleteFlightUseCase(
        flightRepository = flightRepository,
    )

    val lookupFlightUseCase = LookupFlightUseCase(
        flightApiClient = flightApiClient,
        apiKeyRepository = apiKeyRepository,
    )

    val createItineraryUseCase = CreateItineraryUseCase(
        itineraryRepository = itineraryRepository,
    )

    val updateItineraryUseCase = UpdateItineraryUseCase(
        itineraryRepository = itineraryRepository,
    )

    val syncGeneratedTripStopsForItineraryUseCase = SyncGeneratedTripStopsForItineraryUseCase(
        itineraryRepository = itineraryRepository,
        tripRepository = tripRepository,
        airportRepository = airportRepository,
        itineraryGeneratedStopService = itineraryGeneratedStopService,
    )

    val removeGeneratedTripStopsForItineraryUseCase = RemoveGeneratedTripStopsForItineraryUseCase(
        itineraryRepository = itineraryRepository,
        tripRepository = tripRepository,
    )

    val deleteItineraryUseCase = DeleteItineraryUseCase(
        itineraryRepository = itineraryRepository,
    )

    val createItineraryGroupUseCase = CreateItineraryGroupUseCase(
        itineraryRepository = itineraryRepository,
    )

    val updateItineraryGroupUseCase = UpdateItineraryGroupUseCase(
        itineraryRepository = itineraryRepository,
    )

    val deleteItineraryGroupUseCase = DeleteItineraryGroupUseCase(
        itineraryRepository = itineraryRepository,
    )

    val reorderItineraryGroupsUseCase = ReorderItineraryGroupsUseCase(
        itineraryRepository = itineraryRepository,
    )

    val reorderGroupFlightsUseCase = ReorderGroupFlightsUseCase(
        flightRepository = flightRepository,
    )

    val createExcursionUseCase = CreateExcursionUseCase(
        excursionRepository = excursionRepository,
    )

    val updateExcursionUseCase = UpdateExcursionUseCase(
        excursionRepository = excursionRepository,
    )

    val deleteExcursionUseCase = DeleteExcursionUseCase(
        excursionRepository = excursionRepository,
    )

    val reorderExcursionsUseCase = ReorderExcursionsUseCase(
        excursionRepository = excursionRepository,
    )

    val createExcursionStopUseCase = CreateExcursionStopUseCase(
        excursionRepository = excursionRepository,
    )

    val updateExcursionStopUseCase = UpdateExcursionStopUseCase(
        excursionRepository = excursionRepository,
    )

    val deleteExcursionStopUseCase = DeleteExcursionStopUseCase(
        excursionRepository = excursionRepository,
    )

    val reorderExcursionStopsUseCase = ReorderExcursionStopsUseCase(
        excursionRepository = excursionRepository,
    )

    fun importInitialData() {
        applicationScope.launch {
            countryDatasetImporter.importIfNeeded()
            airportDatasetImporter.importIfNeeded()
            airlineDatasetImporter.importIfNeeded()
        }
    }

    companion object {
        private const val DATABASE_NAME = "atlas.db"
    }
}
