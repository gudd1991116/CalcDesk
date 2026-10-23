package com.qiyuan.calcdesk.navigation

object Routes {
    const val HOME = "home"
    const val TOOL = "tool/{id}"
    const val ABOUT = "about"
    const val PRIVACY = "privacy"
    const val CONTACT = "contact"
    const val TIMESTAMP_SNIPPETS = "timestamp-snippets"

    fun tool(id: String): String = "tool/$id"
}
