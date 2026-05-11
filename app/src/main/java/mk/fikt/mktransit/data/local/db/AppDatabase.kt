package mk.fikt.mktransit.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import mk.fikt.mktransit.data.local.dao.CachedLineDao
import mk.fikt.mktransit.data.local.dao.FavoriteDao
import mk.fikt.mktransit.data.local.dao.TicketDao
import mk.fikt.mktransit.data.local.entity.CachedLineEntity
import mk.fikt.mktransit.data.local.entity.FavoriteEntity
import mk.fikt.mktransit.data.local.entity.TicketEntity

@Database(
    entities = [
        TicketEntity::class,
        FavoriteEntity::class,
        CachedLineEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun cachedLineDao(): CachedLineDao
}