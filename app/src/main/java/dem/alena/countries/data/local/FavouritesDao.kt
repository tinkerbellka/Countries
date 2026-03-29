package dem.alena.countries.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouritesDao {

    @Query("SELECT code FROM favourites")
    suspend fun getAllCodes(): List<String>

    @Query("SELECT code FROM favourites")
    fun observeAllCodes(): Flow<List<String>>

    @Query("SELECT * FROM favourites ORDER BY nameCommon")
    suspend fun getAll(): List<FavouriteCountryEntity>

    @Query("SELECT * FROM favourites ORDER BY nameCommon")
    fun observeAll(): Flow<List<FavouriteCountryEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE code = :code)")
    suspend fun isFavourite(code: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavouriteCountryEntity)

    @Query("DELETE FROM favourites WHERE code = :code")
    suspend fun deleteByCode(code: String)
}

