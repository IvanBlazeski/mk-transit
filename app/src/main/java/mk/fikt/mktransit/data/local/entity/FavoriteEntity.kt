package mk.fikt.mktransit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val favoriteId: String,
    val userId: String,
    val lineId: String,
    val lineName: String,
    val lineNumber: String,
    val stopId: String = "",
    val stopName: String = "",
    val notifyMinutesBefore: Int = 10
)