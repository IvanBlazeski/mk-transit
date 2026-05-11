package mk.fikt.mktransit.data.local.dao

import androidx.room.*
import mk.fikt.mktransit.data.local.entity.CachedLineEntity

@Dao
interface CachedLineDao {

    @Query("SELECT * FROM cached_lines ORDER BY lineNumber ASC")
    suspend fun getAllCachedLines(): List<CachedLineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<CachedLineEntity>)

    @Query("DELETE FROM cached_lines")
    suspend fun clearCache()
}