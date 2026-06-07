package dem.alena.countries.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val name: String,
    val createdAt: Long
)

@Entity(
    tableName = "collection_countries",
    primaryKeys = ["collectionId", "countryCode"]
)
data class CollectionCountryCrossRef(
    val collectionId: Long,
    val countryCode: String,
    val countryName: String,
    val addedAt: Long
)
