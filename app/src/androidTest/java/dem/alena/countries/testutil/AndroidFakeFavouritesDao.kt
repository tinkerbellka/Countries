package dem.alena.countries.testutil

import dem.alena.countries.data.local.FavouriteCountryEntity
import dem.alena.countries.data.local.FavouritesDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class AndroidFakeFavouritesDao : FavouritesDao {
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
