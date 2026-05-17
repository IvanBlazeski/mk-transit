package mk.fikt.mktransit.domain.model

data class BusLine(
    val lineId: String = "",
    val operatorId: String = "",
    val lineNumber: String = "",
    val lineName: String = "",
    val lineType: LineType = LineType.BUS,
    val startStop: String = "",
    val endStop: String = "",
    val isActive: Boolean = true,
    val averageRating: Float = 0f,
    val ratingCount: Int = 0,
    val scheduleStart: Long = 0L,
    val scheduleEnd: Long = 0L,
    val priceOneWay: Float = 50f,
    val priceReturn: Float = 90f

)

enum class LineType {
    BUS, MINIBUS, TROLLEY
}