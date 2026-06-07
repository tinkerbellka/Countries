package dem.alena.countries.testutil

import dem.alena.countries.data.local.CollectionCountryCrossRef
import dem.alena.countries.data.local.CollectionEntity
import dem.alena.countries.data.local.CollectionWithMeta
import dem.alena.countries.data.local.CollectionsDao
import dem.alena.countries.data.local.CountriesCacheDao
import dem.alena.countries.data.local.CountryNoteEntity
import dem.alena.countries.data.local.CountryNotesDao
import dem.alena.countries.data.local.UserProfileEntity
import dem.alena.countries.data.local.UserProfileDao
import dem.alena.countries.data.local.ViewHistoryDao
import dem.alena.countries.data.local.ViewHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AndroidEmptyCacheDao : CountriesCacheDao {
    override suspend fun getByListKey(listKey: String) = emptyList<dem.alena.countries.data.local.CachedCountryEntity>()
    override suspend fun getLatestCachedAt(listKey: String): Long? = null
    override suspend fun deleteByListKey(listKey: String) {}
    override suspend fun insertAll(entities: List<dem.alena.countries.data.local.CachedCountryEntity>) {}
    override suspend fun replaceForListKey(listKey: String, entities: List<dem.alena.countries.data.local.CachedCountryEntity>) {}
    override suspend fun getAllCachedCodes(): List<String> = emptyList()

    override suspend fun getByCode(code: String): dem.alena.countries.data.local.CachedCountryEntity? = null
}

class AndroidStubProfileDao : UserProfileDao {
    private val profile = UserProfileEntity(1L, "Test", "", false, 0L)
    override fun observeAll(): Flow<List<UserProfileEntity>> = flowOf(listOf(profile))
    override suspend fun getAll(): List<UserProfileEntity> = listOf(profile)
    override suspend fun getById(id: Long): UserProfileEntity? = profile
    override suspend fun insert(entity: UserProfileEntity): Long = 1L
    override suspend fun updatePin(profileId: Long, pinHash: String) {}
    override suspend fun updateShareCollections(profileId: Long, enabled: Boolean) {}
    override suspend fun deleteById(id: Long) {}
}

class AndroidEmptyViewHistoryDao : ViewHistoryDao {
    override fun observeRecent(profileId: Long, limit: Int): Flow<List<ViewHistoryEntity>> = flowOf(emptyList())
    override suspend fun insert(entity: ViewHistoryEntity) {}
    override suspend fun deleteEntry(profileId: Long, countryCode: String) {}
    override suspend fun clearForProfile(profileId: Long) {}
}

class AndroidEmptyNotesDao : CountryNotesDao {
    override suspend fun getNote(profileId: Long, code: String): CountryNoteEntity? = null
    override fun observeNotesForProfile(profileId: Long): Flow<List<CountryNoteEntity>> = flowOf(emptyList())
    override fun observeHasNote(profileId: Long, code: String): Flow<Boolean> = flowOf(false)
    override suspend fun upsert(entity: CountryNoteEntity) {}
    override suspend fun delete(profileId: Long, code: String) {}
}

class AndroidEmptyCollectionsDao : CollectionsDao {
    override fun observeCollectionsWithMeta(profileId: Long): Flow<List<CollectionWithMeta>> = flowOf(emptyList())
    override fun observeSharedCollections(excludeProfileId: Long): Flow<List<CollectionWithMeta>> = flowOf(emptyList())
    override suspend fun getCollection(id: Long): CollectionEntity? = null
    override suspend fun insertCollection(entity: CollectionEntity): Long = 0L
    override suspend fun deleteCollection(id: Long) {}
    override fun observeCountriesInCollection(collectionId: Long): Flow<List<CollectionCountryCrossRef>> =
        flowOf(emptyList())
    override suspend fun countCountries(collectionId: Long): Int = 0
    override suspend fun insertCountry(ref: CollectionCountryCrossRef) {}
    override suspend fun removeCountryFromCollection(collectionId: Long, countryCode: String) {}
    override suspend fun clearCollectionCountries(collectionId: Long) {}
    override suspend fun deleteCollectionWithCountries(collectionId: Long) {}
}
