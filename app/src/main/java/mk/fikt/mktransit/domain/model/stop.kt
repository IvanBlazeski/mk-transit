package mk.fikt.mktransit.domain.model

data class Stop(
    val stopId: String = "",
    val stopName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val stopOrder: Int = 0,
    val minutesFromStart: Int = 0
)

data class Rating(
    val ratingId: String = "",
    val userId: String = "",
    val stars: Int = 0,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)