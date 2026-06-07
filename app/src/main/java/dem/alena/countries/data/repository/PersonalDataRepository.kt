package dem.alena.countries.data.repository

import dem.alena.countries.data.local.CollectionCountryCrossRef
import dem.alena.countries.data.local.CollectionEntity
import dem.alena.countries.data.local.CollectionsDao
import dem.alena.countries.data.local.CountryNoteEntity
import dem.alena.countries.data.local.CountryNotesDao
import dem.alena.countries.data.local.ViewHistoryDao
import dem.alena.countries.data.local.ViewHistoryEntity
import dem.alena.countries.data.model.Country
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserCollection(
    val id: Long,
    val name: String,
    val countryCount: Int,
    val ownerName: String,
    val isOwnedByMe: Boolean
)

data class CollectionCountryItem(
    val countryCode: String,
    val countryName: String
)

data class HistoryItem(
    val id: Long,
    val countryCode: String,
    val countryName: String,
    val viewedAt: Long
)

@Singleton
class PersonalDataRepository @Inject constructor(
    private val viewHistoryDao: ViewHistoryDao,
    private val countryNotesDao: CountryNotesDao,
    private val collectionsDao: CollectionsDao
) {

    fun observeRecentHistory(profileId: Long, limit: Int = 30): Flow<List<HistoryItem>> {
        return viewHistoryDao.observeRecent(profileId, limit).map { entities ->
            entities.map { entity ->
                HistoryItem(
                    id = entity.id,
                    countryCode = entity.countryCode,
                    countryName = entity.countryName,
                    viewedAt = entity.viewedAt
                )
            }
        }
    }

    suspend fun recordView(profileId: Long, country: Country) {
        viewHistoryDao.deleteEntry(profileId, country.code)
        viewHistoryDao.insert(
            ViewHistoryEntity(
                profileId = profileId,
                countryCode = country.code,
                countryName = country.name.common,
                viewedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearHistory(profileId: Long) {
        viewHistoryDao.clearForProfile(profileId)
    }

    fun observeNoteCodes(profileId: Long): Flow<Set<String>> {
        return countryNotesDao.observeNotesForProfile(profileId).map { notes ->
            notes.filter { it.text.isNotBlank() }.map { it.countryCode }.toSet()
        }
    }

    fun observeNote(profileId: Long, countryCode: String): Flow<String> {
        return countryNotesDao.observeNotesForProfile(profileId).map { notes ->
            notes.firstOrNull { it.countryCode == countryCode }?.text.orEmpty()
        }
    }

    suspend fun saveNote(profileId: Long, countryCode: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            countryNotesDao.delete(profileId, countryCode)
            return
        }
        countryNotesDao.upsert(
            CountryNoteEntity(
                profileId = profileId,
                countryCode = countryCode,
                text = trimmed,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun observeMyCollections(profileId: Long): Flow<List<UserCollection>> {
        return collectionsDao.observeCollectionsWithMeta(profileId).map { rows ->
            rows.map { row ->
                UserCollection(
                    id = row.id,
                    name = row.name,
                    countryCount = row.countryCount,
                    ownerName = row.ownerName,
                    isOwnedByMe = true
                )
            }
        }
    }

    fun observeSharedCollections(activeProfileId: Long): Flow<List<UserCollection>> {
        return collectionsDao.observeSharedCollections(activeProfileId).map { rows ->
            rows.map { row ->
                UserCollection(
                    id = row.id,
                    name = row.name,
                    countryCount = row.countryCount,
                    ownerName = row.ownerName,
                    isOwnedByMe = false
                )
            }
        }
    }

    suspend fun createCollection(profileId: Long, name: String): Long {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty())
        return collectionsDao.insertCollection(
            CollectionEntity(
                profileId = profileId,
                name = trimmed,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteCollection(collectionId: Long) {
        collectionsDao.deleteCollectionWithCountries(collectionId)
    }

    suspend fun isCollectionOwnedBy(collectionId: Long, profileId: Long): Boolean {
        return collectionsDao.getCollection(collectionId)?.profileId == profileId
    }

    fun observeCollectionCountries(collectionId: Long): Flow<List<CollectionCountryItem>> {
        return collectionsDao.observeCountriesInCollection(collectionId).map { refs ->
            refs.map { ref ->
                CollectionCountryItem(
                    countryCode = ref.countryCode,
                    countryName = ref.countryName
                )
            }
        }
    }

    suspend fun addCountryToCollection(collectionId: Long, country: Country) {
        collectionsDao.insertCountry(
            CollectionCountryCrossRef(
                collectionId = collectionId,
                countryCode = country.code,
                countryName = country.name.common,
                addedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeCountryFromCollection(collectionId: Long, countryCode: String) {
        collectionsDao.removeCountryFromCollection(collectionId, countryCode)
    }

    suspend fun getCollectionName(collectionId: Long): String? {
        return collectionsDao.getCollection(collectionId)?.name
    }
}
