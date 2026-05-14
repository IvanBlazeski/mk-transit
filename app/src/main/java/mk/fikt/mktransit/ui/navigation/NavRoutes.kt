package mk.fikt.mktransit.ui.navigation

object NavRoutes {
    // Auth
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val ROLE_SELECTION = "role_selection"

    // Passenger
    const val HOME = "home"
    const val MAP = "map"
    const val LINE_LIST = "line_list"
    const val LINE_DETAIL = "line_detail/{lineId}"
    const val TICKETS = "tickets"
    const val TICKET_PURCHASE = "ticket_purchase/{lineId}"
    const val QR_TICKET = "qr_ticket/{ticketId}"
    const val FAVORITES = "favorites"
    const val MESSAGES = "messages"
    const val CHAT = "chat/{conversationId}"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"

    // Operator
    const val OPERATOR_DASHBOARD = "operator_dashboard"
    const val OPERATOR_LINES = "operator_lines"
    const val LINE_EDITOR = "line_editor/{lineId}"
    const val STOP_MANAGER = "stop_manager/{lineId}"
    const val TIMETABLE_EDITOR = "timetable_editor/{lineId}"
    const val ANNOUNCEMENTS = "announcements"
    const val DRIVER_MODE = "driver_mode"
    const val QR_SCANNER = "qr_scanner"


    // Helpers
    fun lineDetail(lineId: String) = "line_detail/$lineId"
    fun ticketPurchase(lineId: String) = "ticket_purchase/$lineId"
    fun qrTicket(ticketId: String) = "qr_ticket/$ticketId"
    fun lineEditor(lineId: String = "new") = "line_editor/$lineId"
    fun stopManager(lineId: String) = "stop_manager/$lineId"
    fun timetableEditor(lineId: String) = "timetable_editor/$lineId"
    fun chat(conversationId: String) = "chat/$conversationId"
}