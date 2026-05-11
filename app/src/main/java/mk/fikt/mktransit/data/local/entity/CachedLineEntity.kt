package mk.fikt.mktransit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_lines")
data class CachedLineEntity(
    @PrimaryKey val lineId: String,
    val lineNumber: String,
    val lineName: String,
    val operatorName: String,
    val lineType: String,
    val startStop: String,
    val endStop: String,
    val cachedAt: Long = System.currentTimeMillis()
)