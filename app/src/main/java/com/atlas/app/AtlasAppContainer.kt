package com.atlas.app

import android.content.Context
import androidx.room.Room
import com.atlas.data.api.AeroDataBoxClient
import com.atlas.data.api.FrankfurterClient
import com.atlas.data.api.UnsplashClient
import com.atlas.data.dataset.AircraftTypeDatasetImporter
import com.atlas.data.dataset.AirlineDatasetImporter
import com.atlas.data.dataset.AirportDatasetImporter
import com.atlas.data.dataset.CountryDatasetImporter
import com.atlas.data.dataset.CountryStatDatasetImporter
import com.atlas.data.location.NominatimLocationSearchRepository
import com.atlas.data.preferences.ApiKeyPreferencesDataSource
import com.atlas.data.preferences.CloudBackupPreferencesDataSource
import com.atlas.data.preferences.CountryMemoriesPreferencesDataSource
import com.atlas.data.preferences.CountryStatsScopePreferencesDataSource
import com.atlas.data.preferences.TripMapPreferencesDataSource
import com.atlas.data.backup.CloudBackupExporter
import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.repository.StopPhotoRepositoryImpl
import com.atlas.data.repository.AircraftTypeRepositoryImpl
import com.atlas.data.repository.AircraftRepositoryImpl
import com.atlas.data.repository.AirlineRepositoryImpl
import com.atlas.data.repository.AirportRepositoryImpl
import com.atlas.data.repository.CountryLandscapePhotoRepositoryImpl
import com.atlas.data.repository.CountryPhotoRepositoryImpl
import com.atlas.data.repository.CountryRepositoryImpl
import com.atlas.data.repository.CountryStatRepositoryImpl
import com.atlas.data.repository.CurrencyRateRepositoryImpl
import com.atlas.data.repository.BackupRepositoryImpl
import com.atlas.data.repository.FlightRepositoryImpl
import com.atlas.data.repository.ItineraryRepositoryImpl
import com.atlas.data.repository.TripRepositoryImpl
import com.atlas.domain.repository.StopPhotoRepository
import com.atlas.domain.repository.AircraftTypeRepository
import com.atlas.domain.repository.AircraftApiClient
import com.atlas.domain.repository.AircraftRepository
import com.atlas.domain.repository.AirlineRepository
import com.atlas.domain.repository.ApiKeyRepository
import com.atlas.domain.repository.AirportRepository
import com.atlas.domain.repository.BackupRepository
import com.atlas.domain.repository.CloudBackupPreferencesRepository
import com.atlas.domain.repository.CloudBackupScheduler
import com.atlas.domain.repository.CountryMemoriesPreferencesRepository
import com.atlas.domain.repository.CountryLandscapePhotoRepository
import com.atlas.domain.repository.CountryPhotoRepository
import com.atlas.domain.repository.CountryRepository
import com.atlas.domain.repository.CountryStatRepository
import com.atlas.domain.repository.CountryStatsScopePreferencesRepository
import com.atlas.domain.repository.CurrencyRateRepository
import com.atlas.domain.repository.FlightApiClient
import com.atlas.domain.repository.FlightRepository
import com.atlas.domain.repository.ItineraryRepository
import com.atlas.domain.repository.LocationSearchRepository
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.repository.TripMapPreferencesRepository
import com.atlas.domain.service.CountryStateDerivationService
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.domain.service.ItineraryGeneratedStopService
import com.atlas.domain.usecase.airline.SearchAirlinesUseCase
import com.atlas.domain.usecase.aircraft.LookupAircraftUseCase
import com.atlas.domain.usecase.country.AddCountryLogUseCase
import com.atlas.domain.usecase.country.DeleteCountryLogUseCase
import com.atlas.domain.usecase.country.SetCurrentlyLivingCountryUseCase
import com.atlas.domain.usecase.country.ToggleWishedCountryUseCase
import com.atlas.domain.usecase.country.UpdateCountryLogUseCase
import com.atlas.domain.usecase.airport.SearchAirportsUseCase
import com.atlas.domain.usecase.photo.AddStopPhotosUseCase
import com.atlas.domain.usecase.photo.DeleteStopPhotoUseCase
import com.atlas.domain.usecase.photo.RotateStopPhotoUseCase
import com.atlas.domain.usecase.photo.SetTripCoverPhotoUseCase
import com.atlas.domain.usecase.status.UpdateCurrentTravelStatusesUseCase
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
import com.atlas.domain.usecase.trip.CreateTripWithFirstStopUseCase
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
        .addMigrations(AtlasDatabase.MIGRATION_15_16)
        .addMigrations(AtlasDatabase.MIGRATION_16_17)
        .addMigrations(AtlasDatabase.MIGRATION_17_18)
        .addMigrations(AtlasDatabase.MIGRATION_18_19)
        .addMigrations(AtlasDatabase.MIGRATION_19_20)
        .addMigrations(AtlasDatabase.MIGRATION_20_21)
        .addMigrations(AtlasDatabase.MIGRATION_21_22)
        .addMigrations(AtlasDatabase.MIGRATION_22_23)
        .addMigrations(AtlasDatabase.MIGRATION_23_24)
        .addMigrations(AtlasDatabase.MIGRATION_24_25)
        .addMigrations(AtlasDatabase.MIGRATION_25_26)
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

    private val aircraftTypeDatasetImporter = AircraftTypeDatasetImporter(
        context = applicationContext,
        database = database,
    )

    private val countryStatDatasetImporter = CountryStatDatasetImporter(
        context = applicationContext,
        database = database,
    )

    val countryRepository: CountryRepository = CountryRepositoryImpl(
        database = database,
    )

    val countryStatRepository: CountryStatRepository = CountryStatRepositoryImpl(
        database = database,
    )

    val tripRepository: TripRepository = TripRepositoryImpl(
        database = database,
    )

    val backupRepository: BackupRepository = BackupRepositoryImpl(
        database = database,
        photosDirectory = java.io.File(applicationContext.filesDir, "photos"),
    )
    val cloudBackupPreferencesRepository: CloudBackupPreferencesRepository =
        CloudBackupPreferencesDataSource(applicationContext)
    val cloudBackupExporter = CloudBackupExporter(
        context = applicationContext,
        backupRepository = backupRepository,
        preferencesRepository = cloudBackupPreferencesRepository,
    )
    val cloudBackupScheduler: CloudBackupScheduler =
        WorkManagerCloudBackupScheduler(applicationContext)

    val airportRepository: AirportRepository = AirportRepositoryImpl(
        database = database,
    )

    val airlineRepository: AirlineRepository = AirlineRepositoryImpl(
        database = database,
    )

    val aircraftTypeRepository: AircraftTypeRepository = AircraftTypeRepositoryImpl(
        database = database,
    )

    val aircraftRepository: AircraftRepository = AircraftRepositoryImpl(
        database = database,
    )

    val flightRepository: FlightRepository = FlightRepositoryImpl(
        database = database,
    )

    val itineraryRepository: ItineraryRepository = ItineraryRepositoryImpl(
        database = database,
    )

    val stopPhotoRepository: StopPhotoRepository = StopPhotoRepositoryImpl(
        context = applicationContext,
        dao = database.stopPhotoDao(),
        tripDao = database.tripDao(),
    )

    val locationSearchRepository: LocationSearchRepository = NominatimLocationSearchRepository()

    val apiKeyRepository: ApiKeyRepository = ApiKeyPreferencesDataSource(applicationContext)
    private val unsplashClient = UnsplashClient()
    val countryPhotoRepository: CountryPhotoRepository = CountryPhotoRepositoryImpl(
        context = applicationContext,
        dao = database.countryPhotoDao(),
        apiClient = unsplashClient,
        apiKeyRepository = apiKeyRepository,
    )
    val countryLandscapePhotoRepository: CountryLandscapePhotoRepository = CountryLandscapePhotoRepositoryImpl(
        context = applicationContext,
        dao = database.countryLandscapePhotoDao(),
        apiClient = unsplashClient,
        apiKeyRepository = apiKeyRepository,
    )
    private val frankfurterClient = FrankfurterClient()
    val currencyRateRepository: CurrencyRateRepository = CurrencyRateRepositoryImpl(
        dao = database.currencyRateDao(),
        apiClient = frankfurterClient,
    )
    val tripMapPreferencesRepository: TripMapPreferencesRepository =
        TripMapPreferencesDataSource(applicationContext)
    val countryMemoriesPreferencesRepository: CountryMemoriesPreferencesRepository =
        CountryMemoriesPreferencesDataSource(applicationContext)
    val countryStatsScopePreferencesRepository: CountryStatsScopePreferencesRepository =
        CountryStatsScopePreferencesDataSource(applicationContext)
    private val aeroDataBoxClient = AeroDataBoxClient()
    val flightApiClient: FlightApiClient = aeroDataBoxClient
    val aircraftApiClient: AircraftApiClient = aeroDataBoxClient

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

    val createTripWithFirstStopUseCase = CreateTripWithFirstStopUseCase(
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
        stopPhotoRepository = stopPhotoRepository,
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

    val lookupAircraftUseCase = LookupAircraftUseCase(
        aircraftRepository = aircraftRepository,
        aircraftApiClient = aircraftApiClient,
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









    val addStopPhotosUseCase = AddStopPhotosUseCase(
        stopPhotoRepository = stopPhotoRepository,
    )

    val deleteStopPhotoUseCase = DeleteStopPhotoUseCase(
        stopPhotoRepository = stopPhotoRepository,
    )

    val rotateStopPhotoUseCase = RotateStopPhotoUseCase(
        stopPhotoRepository = stopPhotoRepository,
    )

    val setTripCoverPhotoUseCase = SetTripCoverPhotoUseCase(
        tripRepository = tripRepository,
    )

    val updateCurrentTravelStatusesUseCase = UpdateCurrentTravelStatusesUseCase(
        tripRepository = tripRepository,
        flightRepository = flightRepository,
    )

    fun importInitialData() {
        applicationScope.launch {
            countryDatasetImporter.importIfNeeded()
            airportDatasetImporter.importIfNeeded()
            airlineDatasetImporter.importIfNeeded()
            aircraftTypeDatasetImporter.importIfNeeded()
            countryStatDatasetImporter.importIfNeeded()
        }
    }

    fun refreshTravelStatusesOnStartup() {
        applicationScope.launch(Dispatchers.IO) {
            updateCurrentTravelStatusesUseCase()
        }
    }

    fun refreshCloudBackupScheduleOnStartup() {
        applicationScope.launch {
            if (cloudBackupPreferencesRepository.getSettings().enabled) {
                cloudBackupScheduler.scheduleMonthly()
            }
        }
    }

    companion object {
        private const val DATABASE_NAME = "atlas.db"
    }
}
