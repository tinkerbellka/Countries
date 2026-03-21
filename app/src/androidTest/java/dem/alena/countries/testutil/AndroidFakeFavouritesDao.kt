package dem.alena.countries.testutil

import dem.alena.countries.data.local.FavouriteCountryEntity
import dem.alena.countries.data.local.FavouritesDao

class AndroidFakeFavouritesDao : FavouritesDao {
    private val items = linkedMapOf<String, FavouriteCountryEntity>()

    override suspend fun getAllCodes(): List<String> = items.keys.toList()

    override suspend fun getAll(): List<FavouriteCountryEntity> = items.values.toList()

    override suspend fun isFavourite(code: String): Boolean = items.containsKey(code)

    override suspend fun insert(entity: FavouriteCountryEntity) {
        items[entity.code] = entity
    }

    override suspend fun deleteByCode(code: String) {
        items.remove(code)
    }
}

