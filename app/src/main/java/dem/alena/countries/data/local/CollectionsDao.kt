package dem.alena.countries.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionsDao {

    @Query(
        """
        SELECT c.id, c.profileId, c.name, c.createdAt, p.name AS ownerName,
            (SELECT COUNT(*) FROM collection_countries cc WHERE cc.collectionId = c.id) AS countryCount
        FROM collections c
        INNER JOIN user_profiles p ON c.profileId = p.id
        WHERE c.profileId = :profileId
        ORDER BY c.createdAt DESC
        """
    )
    fun observeCollectionsWithMeta(profileId: Long): Flow<List<CollectionWithMeta>>

    @Query(
        """
        SELECT c.id, c.profileId, c.name, c.createdAt, p.name AS ownerName,
            (SELECT COUNT(*) FROM collection_countries cc WHERE cc.collectionId = c.id) AS countryCount
        FROM collections c
        INNER JOIN user_profiles p ON c.profileId = p.id
        WHERE p.shareCollections = 1 AND c.profileId != :excludeProfileId
        ORDER BY c.createdAt DESC
        """
    )
    fun observeSharedCollections(excludeProfileId: Long): Flow<List<CollectionWithMeta>>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getCollection(id: Long): CollectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(entity: CollectionEntity): Long

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteCollection(id: Long)

    @Query(
        """
        SELECT * FROM collection_countries
        WHERE collectionId = :collectionId
        ORDER BY addedAt DESC
        """
    )
    fun observeCountriesInCollection(collectionId: Long): Flow<List<CollectionCountryCrossRef>>

    @Query("SELECT COUNT(*) FROM collection_countries WHERE collectionId = :collectionId")
    suspend fun countCountries(collectionId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountry(ref: CollectionCountryCrossRef)

    @Query(
        """
        DELETE FROM collection_countries
        WHERE collectionId = :collectionId AND countryCode = :countryCode
        """
    )
    suspend fun removeCountryFromCollection(collectionId: Long, countryCode: String)

    @Query("DELETE FROM collection_countries WHERE collectionId = :collectionId")
    suspend fun clearCollectionCountries(collectionId: Long)

    @Transaction
    suspend fun deleteCollectionWithCountries(collectionId: Long) {
        clearCollectionCountries(collectionId)
        deleteCollection(collectionId)
    }
}
