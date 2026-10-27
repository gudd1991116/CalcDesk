package com.qiyuan.calcdesk.navigation

object Routes {
    const val HOME = "home"
    const val TOOL = "tool/{id}"
    const val ABOUT = "about"
    const val PRIVACY = "privacy"
    const val CONTACT = "contact"
    const val TIMESTAMP_SNIPPETS = "timestamp-snippets"
    const val COUNTDOWN_HISTORY = "countdown-history"
    const val ALERTS = "alerts?itemId={itemId}"

    fun tool(id: String): String = "tool/$id"

    fun alerts(itemId: Long? = null): String =
        if (itemId == null) "alerts?itemId=" else "alerts?itemId=$itemId"
}
