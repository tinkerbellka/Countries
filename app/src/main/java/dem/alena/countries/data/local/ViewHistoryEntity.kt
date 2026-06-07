package dem.alena.countries.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "view_history")
data class ViewHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val countryCode: String,
    val countryName: String,
    val viewedAt: Long
)
