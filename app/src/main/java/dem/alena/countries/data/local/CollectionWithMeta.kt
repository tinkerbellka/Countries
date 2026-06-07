package dem.alena.countries.data.local

import androidx.room.ColumnInfo

data class CollectionWithMeta(
    val id: Long,
    val profileId: Long,
    val name: String,
    val createdAt: Long,
    @ColumnInfo(name = "ownerName") val ownerName: String,
    @ColumnInfo(name = "countryCount") val countryCount: Int
)
