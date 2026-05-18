package mk.fikt.mktransit.domain.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val role: UserRole = UserRole.PASSENGER,
    val fcmToken: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    PASSENGER, OPERATOR, DRIVER, BOTH
}