package mk.fikt.mktransit.domain.model

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val senderName: String = "",
    val lineId: String = "",
    val lineName: String = "",
    val content: String = "",
    val isRead: Boolean = false,
    val sentAt: Long = System.currentTimeMillis()
)

data class Conversation(
    val conversationId: String = "",
    val operatorId: String = "",
    val operatorName: String = "",
    val passengerId: String = "",
    val lineId: String = "",
    val lineName: String = "",
    val lastMessage: String = "",
    val lastMessageAt: Long = 0L,
    val unreadCount: Int = 0
)