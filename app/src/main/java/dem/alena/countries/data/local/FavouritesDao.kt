package dem.alena.countries.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouritesDao {

    @Query("SELECT code FROM favourites WHERE profileId = :profileId")
    suspend fun getAllCodes(profileId: Long): List<String>

    @Query("SELECT code FROM favourites WHERE profileId = :profileId")
    fun observeAllCodes(profileId: Long): Flow<List<String>>

    @Query("SELECT * FROM favourites WHERE profileId = :profileId ORDER BY nameCommon")
    suspend fun getAll(profileId: Long): List<FavouriteCountryEntity>

    @Query("SELECT * FROM favourites WHERE profileId = :profileId ORDER BY nameCommon")
    fun observeAll(profileId: Long): Flow<List<FavouriteCountryEntity>>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM favourites WHERE profileId = :profileId AND code = :code
        )
        """
    )
    suspend fun isFavourite(profileId: Long, code: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavouriteCountryEntity)

    @Query("DELETE FROM favourites WHERE profileId = :profileId AND code = :code")
    suspend fun deleteByCode(profileId: Long, code: String)

    @Query("SELECT * FROM favourites WHERE profileId = :profileId")
    suspend fun getAllForPreload(profileId: Long): List<FavouriteCountryEntity>

    @Query("SELECT * FROM favourites WHERE profileId = :profileId AND code = :code LIMIT 1")
    suspend fun getByCode(profileId: Long, code: String): FavouriteCountryEntity?
}
