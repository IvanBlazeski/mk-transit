package mk.fikt.mktransit.domain.model

data class Schedule(
    val scheduleId: String = "",
    val direction: String = "FORWARD", // FORWARD или RETURN
    val departureTime: String = "", // "08:00"
    val days: List<String> = emptyList() // ["MON","TUE","WED","THU","FRI","SAT","SUN"]
)