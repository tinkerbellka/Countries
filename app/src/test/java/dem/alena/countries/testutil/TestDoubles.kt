package dem.alena.countries.testutil

import dem.alena.countries.data.local.CachedCountryEntity
import dem.alena.countries.data.local.CollectionCountryCrossRef
import dem.alena.countries.data.local.CollectionEntity
import dem.alena.countries.data.local.CollectionWithMeta
import dem.alena.countries.data.local.CountriesCacheDao
import dem.alena.countries.data.local.CountryNoteEntity
import dem.alena.countries.data.local.CountryNotesDao
import dem.alena.countries.data.local.FavouriteCountryEntity
import dem.alena.countries.data.local.FavouritesDao
import dem.alena.countries.data.local.UserProfileEntity
import dem.alena.countries.data.local.UserProfileDao
import dem.alena.countries.data.local.ViewHistoryDao
import dem.alena.countries.data.local.ViewHistoryEntity
import dem.alena.countries.data.local.CollectionsDao
import dem.alena.countries.data.model.Country
import dem.alena.countries.data.model.Flags
import dem.alena.countries.data.model.Name
import dem.alena.countries.data.network.CountriesApi
import dem.alena.countries.data.preferences.AppThemeMode
import dem.alena.countries.data.preferences.CountriesListSettings
import dem.alena.countries.data.preferences.CountriesSettingsRepository
import dem.alena.countries.data.preferences.CountriesSortOrder
import dem.alena.countries.data.repository.OfflineSyncPolicy
import java.util.ArrayDeque
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

const val TEST_PROFILE_ID = 1L

fun testCountry(
    code: String = "USA",
    name: String = "United States"
): Country {
    return Country(
        code = code,
        name = Name(common = name),
        capital = listOf("Capital"),
        region = "Region",
        population = 1000L,
        flags = Flags(png = "https://example.com/$code.png")
    )
}

open class FakeCountriesApi : CountriesApi {
    private val allResponses = ArrayDeque<Result<List<Country>>>()
    private val searchResponses = mutableMapOf<String, Result<List<Country>>>()
    private val codeResponses = mutableMapOf<String, Result<Country>>()

    var getAllCalls = 0
        private set
    var searchCalls = 0
        private set

    fun enqueueGetAllSuccess(data: List<Country>) {
        allResponses.addLast(Result.success(data))
    }

    fun enqueueGetAllError(error: Exception) {
        allResponses.addLast(Result.failure(error))
    }

    fun setSearchSuccess(query: String, data: List<Country>) {
        searchResponses[query] = Result.success(data)
    }

    fun setCountrySuccess(code: String, country: Country) {
        codeResponses[code] = Result.success(country)
    }

    override suspend fun getAllCountries(fields: String): List<Country> {
        getAllCalls++
        val response = if (allResponses.isEmpty()) {
            Result.success(emptyList())
        } else {
            allResponses.removeFirst()
        }
        return response.getOrThrow()
    }

    override suspend fun searchCountries(name: String, fields: String): List<Country> {
        searchCalls++
        return (searchResponses[name] ?: Result.success(emptyList())).getOrThrow()
    }

    override suspend fun getCountryByCode(code: String, fields: String): Country {
        return codeResponses[code]?.getOrThrow()
            ?: throw IllegalStateException("No fake response for code=$code")
    }
}

class FakeFavouritesDao : FavouritesDao {
    private val items = linkedMapOf<Pair<Long, String>, FavouriteCountryEntity>()
    private val entitiesFlow = MutableStateFlow<List<FavouriteCountryEntity>>(emptyList())

    private fun forProfile(profileId: Long) = entitiesFlow.value.filter { it.profileId == profileId }

    override suspend fun getAllCodes(profileId: Long): List<String> =
        forProfile(profileId).map { it.code }

    override fun observeAllCodes(profileId: Long): Flow<List<String>> =
        entitiesFlow.map { list -> list.filter { it.profileId == profileId }.map { it.code } }

    override suspend fun getAll(profileId: Long): List<FavouriteCountryEntity> = forProfile(profileId)

    override fun observeAll(profileId: Long): Flow<List<FavouriteCountryEntity>> =
        entitiesFlow.map { list -> list.filter { it.profileId == profileId } }

    override suspend fun isFavourite(profileId: Long, code: String): Boolean =
        items.containsKey(profileId to code)

    override suspend fun insert(entity: FavouriteCountryEntity) {
        items[entity.profileId to entity.code] = entity
        entitiesFlow.value = items.values.toList()
    }

    override suspend fun deleteByCode(profileId: Long, code: String) {
        items.remove(profileId to code)
        entitiesFlow.value = items.values.toList()
    }

    override suspend fun getAllForPreload(profileId: Long): List<FavouriteCountryEntity> =
        forProfile(profileId)

    override suspend fun getByCode(profileId: Long, code: String): FavouriteCountryEntity? =
        items[profileId to code]
}

class FakeCountriesCacheDao : CountriesCacheDao {
    private val cache = mutableMapOf<String, List<CachedCountryEntity>>()

    override suspend fun getByListKey(listKey: String): List<CachedCountryEntity> =
        cache[listKey].orEmpty()

    override suspend fun getLatestCachedAt(listKey: String): Long? =
        cache[listKey]?.maxOfOrNull { it.cachedAt }

    override suspend fun deleteByListKey(listKey: String) {
        cache.remove(listKey)
    }

    override suspend fun insertAll(entities: List<CachedCountryEntity>) {
        if (entities.isEmpty()) return
        val key = entities.first().listKey
        cache[key] = (cache[key].orEmpty() + entities).distinctBy { it.code }
    }

    override suspend fun replaceForListKey(listKey: String, entities: List<CachedCountryEntity>) {
        cache[listKey] = entities
    }

    override suspend fun getAllCachedCodes(): List<String> =
        cache.values.flatten().map { it.code }.distinct()

    override suspend fun getByCode(code: String): CachedCountryEntity? =
        cache.values.flatten().firstOrNull { it.code == code }

    fun seed(listKey: String, countries: List<Country>, cachedAt: Long) {
        cache[listKey] = countries.map { country ->
            CachedCountryEntity(
                listKey = listKey,
                code = country.code,
                nameCommon = country.name.common,
                region = country.region,
                population = country.population,
                capital = country.capital?.firstOrNull(),
                flagPng = country.flags.png,
                cachedAt = cachedAt
            )
        }
    }
}

class FakeCountriesSettingsRepository(
    initialSettings: CountriesListSettings = CountriesListSettings(activeProfileId = TEST_PROFILE_ID)
) : CountriesSettingsRepository {
    private val settingsFlow = MutableStateFlow(initialSettings)

    override val settings: Flow<CountriesListSettings> = settingsFlow

    override suspend fun setSortOrder(sortOrder: CountriesSortOrder) {
        settingsFlow.value = settingsFlow.value.copy(sortOrder = sortOrder)
    }

    override suspend fun setActiveProfileId(profileId: Long) {
        settingsFlow.value = settingsFlow.value.copy(activeProfileId = profileId)
    }

    override suspend fun setThemeMode(themeMode: AppThemeMode) {
        settingsFlow.value = settingsFlow.value.copy(themeMode = themeMode)
    }

    override suspend fun setCacheTtlHours(hours: Int) {
        settingsFlow.value = settingsFlow.value.copy(cacheTtlHours = hours)
    }

    override suspend fun setWifiOnlyBackgroundSync(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(wifiOnlyBackgroundSync = enabled)
    }
}

class FakeUserProfileDao : UserProfileDao {
    private val profiles = mutableListOf(
        UserProfileEntity(
            id = TEST_PROFILE_ID,
            name = "Тест",
            pinHash = "",
            shareCollections = false,
            createdAt = 0L
        )
    )
    private val flow = MutableStateFlow(profiles.toList())

    override fun observeAll(): Flow<List<UserProfileEntity>> = flow

    override suspend fun getAll(): List<UserProfileEntity> = profiles

    override suspend fun getById(id: Long): UserProfileEntity? = profiles.find { it.id == id }

    override suspend fun insert(entity: UserProfileEntity): Long {
        val id = (profiles.maxOfOrNull { it.id } ?: 0L) + 1
        profiles.add(entity.copy(id = id))
        flow.value = profiles.toList()
        return id
    }

    override suspend fun updatePin(profileId: Long, pinHash: String) {
        val index = profiles.indexOfFirst { it.id == profileId }
        if (index >= 0) {
            profiles[index] = profiles[index].copy(pinHash = pinHash)
            flow.value = profiles.toList()
        }
    }

    override suspend fun updateShareCollections(profileId: Long, enabled: Boolean) {
        val index = profiles.indexOfFirst { it.id == profileId }
        if (index >= 0) {
            profiles[index] = profiles[index].copy(shareCollections = enabled)
            flow.value = profiles.toList()
        }
    }

    override suspend fun deleteById(id: Long) {
        profiles.removeAll { it.id == id }
        flow.value = profiles.toList()
    }
}

class FakeViewHistoryDao : ViewHistoryDao {
    private val items = mutableListOf<ViewHistoryEntity>()
    private val flow = MutableStateFlow(items.toList())

    override fun observeRecent(profileId: Long, limit: Int): Flow<List<ViewHistoryEntity>> =
        flow.map { list ->
            list.filter { it.profileId == profileId }
                .sortedByDescending { it.viewedAt }
                .take(limit)
        }

    override suspend fun insert(entity: ViewHistoryEntity) {
        items.add(entity.copy(id = (items.maxOfOrNull { it.id } ?: 0L) + 1))
        flow.value = items.toList()
    }

    override suspend fun deleteEntry(profileId: Long, countryCode: String) {
        items.removeAll { it.profileId == profileId && it.countryCode == countryCode }
        flow.value = items.toList()
    }

    override suspend fun clearForProfile(profileId: Long) {
        items.removeAll { it.profileId == profileId }
        flow.value = items.toList()
    }
}

class FakeCountryNotesDao : CountryNotesDao {
    private val notes = mutableListOf<CountryNoteEntity>()
    private val flow = MutableStateFlow(notes.toList())

    override suspend fun getNote(profileId: Long, code: String): CountryNoteEntity? =
        notes.find { it.profileId == profileId && it.countryCode == code }

    override fun observeNotesForProfile(profileId: Long): Flow<List<CountryNoteEntity>> =
        flow.map { list -> list.filter { it.profileId == profileId } }

    override fun observeHasNote(profileId: Long, code: String): Flow<Boolean> =
        flow.map { list ->
            list.any { it.profileId == profileId && it.countryCode == code && it.text.isNotBlank() }
        }

    override suspend fun upsert(entity: CountryNoteEntity) {
        notes.removeAll { it.profileId == entity.profileId && it.countryCode == entity.countryCode }
        notes.add(entity)
        flow.value = notes.toList()
    }

    override suspend fun delete(profileId: Long, code: String) {
        notes.removeAll { it.profileId == profileId && it.countryCode == code }
        flow.value = notes.toList()
    }
}

class FakeCollectionsDao : CollectionsDao {
    private val collections = mutableListOf<CollectionEntity>()
    private val countries = mutableListOf<CollectionCountryCrossRef>()
    private val collectionsFlow = MutableStateFlow(collections.toList())

    override fun observeCollectionsWithMeta(profileId: Long): Flow<List<CollectionWithMeta>> =
        collectionsFlow.map { list ->
            list.filter { it.profileId == profileId }.map { entity ->
                toMeta(entity, ownerName = "Тест")
            }
        }

    override fun observeSharedCollections(excludeProfileId: Long): Flow<List<CollectionWithMeta>> =
        collectionsFlow.map { list ->
            list.filter { it.profileId != excludeProfileId }.map { entity ->
                toMeta(entity, ownerName = "Другой")
            }
        }

    override suspend fun getCollection(id: Long): CollectionEntity? =
        collections.find { it.id == id }

    override suspend fun insertCollection(entity: CollectionEntity): Long {
        val id = (collections.maxOfOrNull { it.id } ?: 0L) + 1
        collections.add(entity.copy(id = id))
        collectionsFlow.value = collections.toList()
        return id
    }

    override suspend fun deleteCollection(id: Long) {
        collections.removeAll { it.id == id }
        collectionsFlow.value = collections.toList()
    }

    override fun observeCountriesInCollection(collectionId: Long): Flow<List<CollectionCountryCrossRef>> =
        MutableStateFlow(countries.filter { it.collectionId == collectionId })

    override suspend fun insertCountry(ref: CollectionCountryCrossRef) {
        countries.add(ref)
    }

    override suspend fun removeCountryFromCollection(collectionId: Long, countryCode: String) {
        countries.removeAll { it.collectionId == collectionId && it.countryCode == countryCode }
    }

    override suspend fun clearCollectionCountries(collectionId: Long) {
        countries.removeAll { it.collectionId == collectionId }
    }

    override suspend fun deleteCollectionWithCountries(collectionId: Long) {
        clearCollectionCountries(collectionId)
        deleteCollection(collectionId)
    }

    override suspend fun countCountries(collectionId: Long): Int =
        countries.count { it.collectionId == collectionId }

    private fun toMeta(entity: CollectionEntity, ownerName: String) = CollectionWithMeta(
        id = entity.id,
        profileId = entity.profileId,
        name = entity.name,
        createdAt = entity.createdAt,
        ownerName = ownerName,
        countryCount = countries.count { it.collectionId == entity.id }
    )
}

fun countryToCacheEntity(listKey: String, country: Country, cachedAt: Long) = CachedCountryEntity(
    listKey = listKey,
    code = country.code,
    nameCommon = country.name.common,
    region = country.region,
    population = country.population,
    capital = country.capital?.firstOrNull(),
    flagPng = country.flags.png,
    cachedAt = cachedAt
)
