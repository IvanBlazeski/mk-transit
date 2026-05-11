package mk.fikt.mktransit.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import mk.fikt.mktransit.data.local.dao.CachedLineDao
import mk.fikt.mktransit.data.local.dao.FavoriteDao
import mk.fikt.mktransit.data.local.dao.TicketDao
import mk.fikt.mktransit.data.local.db.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mktransit_database"
        ).build()
    }

    @Provides
    fun provideTicketDao(db: AppDatabase): TicketDao = db.ticketDao()

    @Provides
    fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideCachedLineDao(db: AppDatabase): CachedLineDao = db.cachedLineDao()
}