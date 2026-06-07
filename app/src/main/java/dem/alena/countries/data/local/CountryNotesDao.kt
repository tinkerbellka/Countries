package dem.alena.countries.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryNotesDao {

    @Query("SELECT * FROM country_notes WHERE profileId = :profileId AND countryCode = :code")
    suspend fun getNote(profileId: Long, code: String): CountryNoteEntity?

    @Query("SELECT * FROM country_notes WHERE profileId = :profileId")
    fun observeNotesForProfile(profileId: Long): Flow<List<CountryNoteEntity>>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM country_notes
            WHERE profileId = :profileId AND countryCode = :code AND text != ''
        )
        """
    )
    fun observeHasNote(profileId: Long, code: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CountryNoteEntity)

    @Query("DELETE FROM country_notes WHERE profileId = :profileId AND countryCode = :code")
    suspend fun delete(profileId: Long, code: String)
}
