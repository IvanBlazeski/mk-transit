package mk.fikt.mktransit.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import mk.fikt.mktransit.data.local.entity.FavoriteEntity

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites WHERE userId = :userId")
    fun getFavoritesByUser(userId: String): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE userId = :userId AND lineId = :lineId")
    suspend fun getFavorite(userId: String, lineId: String): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE userId = :userId AND lineId = :lineId")
    suspend fun deleteFavorite(userId: String, lineId: String)
}