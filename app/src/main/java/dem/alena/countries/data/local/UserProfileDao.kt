package dem.alena.countries.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profiles ORDER BY createdAt")
    fun observeAll(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles ORDER BY createdAt")
    suspend fun getAll(): List<UserProfileEntity>

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getById(id: Long): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: UserProfileEntity): Long

    @Query(
        """
        UPDATE user_profiles
        SET pinHash = :pinHash
        WHERE id = :profileId
        """
    )
    suspend fun updatePin(profileId: Long, pinHash: String)

    @Query(
        """
        UPDATE user_profiles
        SET shareCollections = :enabled
        WHERE id = :profileId
        """
    )
    suspend fun updateShareCollections(profileId: Long, enabled: Boolean)

    @Query("DELETE FROM user_profiles WHERE id = :id")
    suspend fun deleteById(id: Long)
}
