package mk.fikt.mktransit.domain.model

data class OperatorProfile(
    val operatorId: String = "",
    val uid: String = "",
    val companyName: String = "",
    val logoUrl: String = "",
    val description: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val coverageArea: String = ""
)