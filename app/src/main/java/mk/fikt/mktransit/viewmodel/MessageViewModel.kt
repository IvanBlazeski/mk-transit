package mk.fikt.mktransit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mk.fikt.mktransit.domain.model.Conversation
import mk.fikt.mktransit.domain.model.Message
import javax.inject.Inject

sealed class MessageState {
    object Loading : MessageState()
    data class ConversationsLoaded(val conversations: List<Conversation>) : MessageState()
    data class MessagesLoaded(val messages: List<Message>) : MessageState()
    data class Error(val message: String) : MessageState()
}

@HiltViewModel
class MessageViewModel @Inject constructor() : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow<MessageState>(MessageState.Loading)
    val state: StateFlow<MessageState> = _state

    private val _newMessage = MutableStateFlow("")
    val newMessage: StateFlow<String> = _newMessage

    private val _currentConversationId = MutableStateFlow("")
    val currentConversationId: StateFlow<String> = _currentConversationId

    fun updateNewMessage(text: String) {
        _newMessage.value = text
    }

    fun loadConversations() {
        viewModelScope.launch {
            _state.value = MessageState.Loading
            try {
                val uid = auth.currentUser?.uid ?: return@launch

                // Вчитај разговори каде е патник
                val passengerSnapshot = firestore.collection("messages")
                    .whereEqualTo("passengerId", uid)
                    .get().await()

                // Вчитај разговори каде е оператор по Auth UID
                val operatorByUidSnapshot = firestore.collection("messages")
                    .whereEqualTo("operatorId", uid)
                    .get().await()

                val allDocs = passengerSnapshot.documents + operatorByUidSnapshot.documents

                val conversations = allDocs.mapNotNull { doc ->
                    try {
                        Conversation(
                            conversationId = doc.id,
                            operatorId = doc.getString("operatorId") ?: "",
                            operatorName = doc.getString("operatorName") ?: "Operator",
                            passengerId = doc.getString("passengerId") ?: "",
                            lineId = doc.getString("lineId") ?: "",
                            lineName = doc.getString("lineName") ?: "",
                            lastMessage = doc.getString("lastMessage") ?: "",
                            lastMessageAt = doc.getLong("lastMessageAt") ?: 0L,
                            unreadCount = doc.getLong("unreadCount")?.toInt() ?: 0
                        )
                    } catch (e: Exception) { null }
                }.sortedByDescending { it.lastMessageAt }

                _state.value = MessageState.ConversationsLoaded(conversations)
            } catch (e: Exception) {
                _state.value = MessageState.Error(e.message ?: "Failed to load")
            }
        }
    }

    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            _state.value = MessageState.Loading
            try {
                val snapshot = firestore.collection("messages")
                    .document(conversationId)
                    .collection("msgs")
                    .orderBy("sentAt", Query.Direction.ASCENDING)
                    .get().await()

                val messages = snapshot.documents.map { doc ->
                    Message(
                        messageId = doc.id,
                        senderId = doc.getString("senderId") ?: "",
                        receiverId = doc.getString("receiverId") ?: "",
                        senderName = doc.getString("senderName") ?: "",
                        content = doc.getString("content") ?: "",
                        isRead = doc.getBoolean("isRead") ?: false,
                        sentAt = doc.getLong("sentAt") ?: 0L
                    )
                }
                _state.value = MessageState.MessagesLoaded(messages)
            } catch (e: Exception) {
                _state.value = MessageState.Error(e.message ?: "Failed to load messages")
            }
        }
    }

    fun loadOrCreateConversation(conversationIdOrOperatorId: String, operatorId: String) {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: return@launch

                // Прво обиди се директно со conversationId
                try {
                    val directDoc = firestore.collection("messages")
                        .document(conversationIdOrOperatorId)
                        .get().await()

                    if (directDoc.exists()) {
                        _currentConversationId.value = conversationIdOrOperatorId
                        loadMessages(conversationIdOrOperatorId)
                        return@launch
                    }
                } catch (e: Exception) { }

                // Е operatorId — барај разговор каде е патник
                val asPassenger = firestore.collection("messages")
                    .whereEqualTo("passengerId", uid)
                    .whereEqualTo("operatorId", conversationIdOrOperatorId)
                    .get().await()

                if (!asPassenger.isEmpty) {
                    val convId = asPassenger.documents.first().id
                    _currentConversationId.value = convId
                    loadMessages(convId)
                    return@launch
                }

                // Барај разговор каде е оператор
                val asOperator = firestore.collection("messages")
                    .whereEqualTo("operatorId", uid)
                    .whereEqualTo("passengerId", conversationIdOrOperatorId)
                    .get().await()

                if (!asOperator.isEmpty) {
                    val convId = asOperator.documents.first().id
                    _currentConversationId.value = convId
                    loadMessages(convId)
                    return@launch
                }

                _currentConversationId.value = ""
                _state.value = MessageState.MessagesLoaded(emptyList())
            } catch (e: Exception) {
                _state.value = MessageState.MessagesLoaded(emptyList())
            }
        }
    }

    fun sendMessage(conversationId: String, receiverId: String, lineName: String = "") {
        viewModelScope.launch {
            val content = _newMessage.value.trim()
            if (content.isBlank()) return@launch

            try {
                val uid = auth.currentUser?.uid ?: return@launch
                val now = System.currentTimeMillis()

                val convId = when {
                    _currentConversationId.value.isNotBlank() -> _currentConversationId.value
                    else -> {
                        // Секогаш создај нов разговор ако нема постоечки
                        val conv = hashMapOf(
                            "passengerId" to uid,
                            "operatorId" to receiverId,
                            "operatorName" to "Operator",
                            "lastMessage" to content,
                            "lastMessageAt" to now,
                            "unreadCount" to 0,
                            "lineName" to lineName,  // ← ДОДАЈ
                            "lineId" to ""
                        )
                        val ref = firestore.collection("messages").add(conv).await()
                        _currentConversationId.value = ref.id
                        ref.id
                    }
                }

                val msg = hashMapOf(
                    "senderId" to uid,
                    "receiverId" to receiverId,
                    "content" to content,
                    "isRead" to false,
                    "sentAt" to now
                )

                firestore.collection("messages").document(convId)
                    .collection("msgs").add(msg).await()

                firestore.collection("messages").document(convId)
                    .update("lastMessage", content, "lastMessageAt", now)

                _newMessage.value = ""
                loadMessages(convId)
            } catch (e: Exception) {
                _state.value = MessageState.Error(e.message ?: "Failed to send")
            }
        }
    }

    fun startConversation(
        operatorId: String,
        operatorName: String,
        lineId: String,
        lineName: String,
        firstMessage: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                val now = System.currentTimeMillis()

                val conversation = hashMapOf(
                    "passengerId" to uid,
                    "operatorId" to operatorId,
                    "operatorName" to operatorName,
                    "lineId" to lineId,
                    "lineName" to lineName,
                    "lastMessage" to firstMessage,
                    "lastMessageAt" to now,
                    "unreadCount" to 0
                )

                val convRef = firestore.collection("messages").add(conversation).await()

                val msg = hashMapOf(
                    "senderId" to uid,
                    "receiverId" to operatorId,
                    "content" to firstMessage,
                    "isRead" to false,
                    "sentAt" to now
                )

                firestore.collection("messages")
                    .document(convRef.id)
                    .collection("msgs")
                    .add(msg).await()

                onSuccess(convRef.id)
            } catch (e: Exception) {
                _state.value = MessageState.Error(e.message ?: "Failed")
            }
        }
    }
}