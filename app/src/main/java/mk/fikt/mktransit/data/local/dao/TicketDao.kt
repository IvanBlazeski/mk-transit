package mk.fikt.mktransit.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import mk.fikt.mktransit.data.local.entity.TicketEntity

@Dao
interface TicketDao {

    @Query("SELECT * FROM tickets WHERE userId = :userId ORDER BY purchasedAt DESC")
    fun getTicketsByUser(userId: String): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE ticketId = :ticketId")
    suspend fun getTicketById(ticketId: String): TicketEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity)

    @Update
    suspend fun updateTicket(ticket: TicketEntity)

    @Query("UPDATE tickets SET status = 'used', usedAt = :usedAt WHERE ticketId = :ticketId")
    suspend fun markTicketAsUsed(ticketId: String, usedAt: Long)

    @Query("DELETE FROM tickets WHERE ticketId = :ticketId")
    suspend fun deleteTicket(ticketId: String)
}