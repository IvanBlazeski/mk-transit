package mk.fikt.mktransit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mk.fikt.mktransit.data.local.dao.TicketDao
import mk.fikt.mktransit.data.local.entity.TicketEntity
import java.util.UUID
import javax.inject.Inject

sealed class TicketState {
    object Idle : TicketState()
    object Loading : TicketState()
    data class Success(val tickets: List<TicketEntity>) : TicketState()
    data class SingleTicket(val ticket: TicketEntity) : TicketState()
    data class PurchaseSuccess(val ticketId: String) : TicketState()
    data class Error(val message: String) : TicketState()
}

@HiltViewModel
class TicketViewModel @Inject constructor(
    private val ticketDao: TicketDao
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _ticketState = MutableStateFlow<TicketState>(TicketState.Idle)
    val ticketState: StateFlow<TicketState> = _ticketState

    fun loadMyTickets() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            ticketDao.getTicketsByUser(uid).collect { tickets ->
                _ticketState.value = TicketState.Success(tickets)
            }
        }
    }

    fun loadTicketById(ticketId: String) {
        viewModelScope.launch {
            _ticketState.value = TicketState.Loading
            try {
                val ticket = ticketDao.getTicketById(ticketId)
                if (ticket != null) {
                    _ticketState.value = TicketState.SingleTicket(ticket)
                } else {
                    _ticketState.value = TicketState.Error("Ticket not found")
                }
            } catch (e: Exception) {
                _ticketState.value = TicketState.Error(e.message ?: "Error")
            }
        }
    }

    fun purchaseTicket(
        lineId: String,
        lineName: String,
        lineNumber: String,
        ticketType: String,
        price: Float,
        quantity: Int = 1,
        paymentMethod: String = "CARD"
    ) {
        viewModelScope.launch {
            _ticketState.value = TicketState.Loading
            try {
                val uid = auth.currentUser?.uid ?: throw Exception("Not logged in")
                val now = System.currentTimeMillis()
                var lastTicketId = ""

                // Создај quantity број на билети
                repeat(quantity) { index ->
                    val ticketId = UUID.randomUUID().toString()
                    val pricePerTicket = price / quantity
                    val validUntil = when (ticketType) {
                        "ONE_WAY" -> now + (24 * 60 * 60 * 1000) // 24 часа
                        "RETURN" -> now + (48 * 60 * 60 * 1000) // 48 часа
                        else -> now + (24 * 60 * 60 * 1000)
                    }

                    val qrContent = "MKTRANSIT|$ticketId|$lineId|$uid|$now|$ticketType"

                    val ticket = TicketEntity(
                        ticketId = ticketId,
                        userId = uid,
                        lineId = lineId,
                        lineName = lineName,
                        lineNumber = lineNumber,
                        ticketType = ticketType,
                        status = if (paymentMethod == "CASH") "PENDING" else "PAID", // ← ПРОМЕНА
                        qrContent = qrContent,
                        pricePaid = pricePerTicket,
                        purchasedAt = now,
                        validUntil = validUntil
                    )

                    ticketDao.insertTicket(ticket)

                    val firestoreTicket = hashMapOf(
                        "ticketId" to ticketId,
                        "userId" to uid,
                        "lineId" to lineId,
                        "lineName" to lineName,
                        "ticketType" to ticketType,
                        "status" to if (paymentMethod == "CASH") "PENDING" else "PAID", // ← ПРОМЕНА
                        "paymentMethod" to paymentMethod, // ←НОВО
                        "qrContent" to qrContent,
                        "pricePaid" to pricePerTicket,
                        "purchasedAt" to now,
                        "validUntil" to validUntil,
                        "quantity" to quantity
                    )
                    firestore.collection("tickets")
                        .document(ticketId)
                        .set(firestoreTicket).await()

                    if (index == 0) lastTicketId = ticketId
                }

                _ticketState.value = TicketState.PurchaseSuccess(lastTicketId)
            } catch (e: Exception) {
                _ticketState.value = TicketState.Error(e.message ?: "Purchase failed")
            }
        }
    }
}