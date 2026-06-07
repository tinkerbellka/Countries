package dem.alena.countries.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ViewHistoryDao {

    @Query(
        """
        SELECT * FROM view_history
        WHERE profileId = :profileId
        ORDER BY viewedAt DESC
        LIMIT :limit
        """
    )
    fun observeRecent(profileId: Long, limit: Int = 50): Flow<List<ViewHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ViewHistoryEntity)

    @Query(
        """
        DELETE FROM view_history
        WHERE profileId = :profileId
        AND countryCode = :countryCode
        """
    )
    suspend fun deleteEntry(profileId: Long, countryCode: String)

    @Query("DELETE FROM view_history WHERE profileId = :profileId")
    suspend fun clearForProfile(profileId: Long)
}
