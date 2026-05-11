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

    // Вчитај ги тикетите на корисникот
    fun loadMyTickets() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            ticketDao.getTicketsByUser(uid).collect { tickets ->
                _ticketState.value = TicketState.Success(tickets)
            }
        }
    }

    // Купи тикет (без Stripe за сега — симулација)
    fun purchaseTicket(
        lineId: String,
        lineName: String,
        lineNumber: String,
        ticketType: String,
        price: Float
    ) {
        viewModelScope.launch {
            _ticketState.value = TicketState.Loading
            try {
                val uid = auth.currentUser?.uid ?: throw Exception("Not logged in")
                val ticketId = UUID.randomUUID().toString()
                val now = System.currentTimeMillis()
                val validUntil = when (ticketType) {
                    "SINGLE" -> now + (2 * 60 * 60 * 1000) // 2 часа
                    "DAILY" -> now + (24 * 60 * 60 * 1000) // 1 ден
                    "WEEKLY" -> now + (7 * 24 * 60 * 60 * 1000) // 7 дена
                    else -> now + (2 * 60 * 60 * 1000)
                }

                // QR содржина
                val qrContent = "MKTRANSIT|$ticketId|$lineId|$uid|$now"

                val ticket = TicketEntity(
                    ticketId = ticketId,
                    userId = uid,
                    lineId = lineId,
                    lineName = lineName,
                    lineNumber = lineNumber,
                    ticketType = ticketType,
                    status = "PAID",
                    qrContent = qrContent,
                    pricePaid = price,
                    purchasedAt = now,
                    validUntil = validUntil
                )

                // Зачувај локално (Room)
                ticketDao.insertTicket(ticket)

                // Зачувај во Firestore
                val firestoreTicket = hashMapOf(
                    "ticketId" to ticketId,
                    "userId" to uid,
                    "lineId" to lineId,
                    "lineName" to lineName,
                    "ticketType" to ticketType,
                    "status" to "PAID",
                    "qrContent" to qrContent,
                    "pricePaid" to price,
                    "purchasedAt" to now,
                    "validUntil" to validUntil
                )
                firestore.collection("tickets")
                    .document(ticketId)
                    .set(firestoreTicket).await()

                _ticketState.value = TicketState.PurchaseSuccess(ticketId)
            } catch (e: Exception) {
                _ticketState.value = TicketState.Error(e.message ?: "Purchase failed")
            }
        }
    }
}