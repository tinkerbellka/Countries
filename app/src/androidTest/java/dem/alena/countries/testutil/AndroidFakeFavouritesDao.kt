package dem.alena.countries.testutil

import dem.alena.countries.data.local.FavouriteCountryEntity
import dem.alena.countries.data.local.FavouritesDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class AndroidFakeFavouritesDao : FavouritesDao {
    private val items = linkedMapOf<String, FavouriteCountryEntity>()
    private val entitiesFlow = MutableStateFlow<List<FavouriteCountryEntity>>(emptyList())

    override suspend fun getAllCodes(): List<String> = entitiesFlow.value.map { it.code }

    override fun observeAllCodes(): Flow<List<String>> {
        return entitiesFlow.map { entities -> entities.map(FavouriteCountryEntity::code) }
    }

    override suspend fun getAll(): List<FavouriteCountryEntity> = entitiesFlow.value

    override fun observeAll(): Flow<List<FavouriteCountryEntity>> = entitiesFlow

    override suspend fun isFavourite(code: String): Boolean = items.containsKey(code)

    override suspend fun insert(entity: FavouriteCountryEntity) {
        items[entity.code] = entity
        syncState()
    }

    override suspend fun deleteByCode(code: String) {
        items.remove(code)
        syncState()
    }

    private fun syncState() {
        entitiesFlow.value = items.values.toList()
    }
}

