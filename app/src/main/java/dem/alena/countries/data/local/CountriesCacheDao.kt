package dem.alena.countries.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface CountriesCacheDao {

    @Query("SELECT * FROM cached_countries WHERE listKey = :listKey ORDER BY nameCommon")
    suspend fun getByListKey(listKey: String): List<CachedCountryEntity>

    @Query("SELECT MAX(cachedAt) FROM cached_countries WHERE listKey = :listKey")
    suspend fun getLatestCachedAt(listKey: String): Long?

    @Query("DELETE FROM cached_countries WHERE listKey = :listKey")
    suspend fun deleteByListKey(listKey: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CachedCountryEntity>)

    @Transaction
    suspend fun replaceForListKey(listKey: String, entities: List<CachedCountryEntity>) {
        deleteByListKey(listKey)
        if (entities.isNotEmpty()) {
            insertAll(entities)
        }
    }

    @Query("SELECT DISTINCT code FROM cached_countries")
    suspend fun getAllCachedCodes(): List<String>

    @Query("SELECT * FROM cached_countries WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): CachedCountryEntity?
}
