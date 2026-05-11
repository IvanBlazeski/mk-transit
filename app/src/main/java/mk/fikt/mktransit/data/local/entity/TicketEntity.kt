package mk.fikt.mktransit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey val ticketId: String,
    val userId: String,
    val lineId: String,
    val lineName: String,
    val lineNumber: String,
    val ticketType: String,
    val status: String,
    val qrContent: String,
    val pricePaid: Float,
    val purchasedAt: Long,
    val validUntil: Long,
    val usedAt: Long? = null
)