package com.qiyuan.calcdesk.data

import android.content.Context
import java.time.LocalDate

object CountdownPrefs {
    private const val NAME = "countdown_prefs"
    private const val KEY_SKIP_DELETE_CONFIRM = "skip_delete_confirm"
    private const val KEY_CLEARED_ALERTS_DAY = "cleared_alerts_day"
    private const val KEY_CLEARED_ALERT_IDS = "cleared_alert_ids"

    fun skipDeleteConfirm(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SKIP_DELETE_CONFIRM, false)

    fun setSkipDeleteConfirm(context: Context, skip: Boolean) {
        prefs(context).edit().putBoolean(KEY_SKIP_DELETE_CONFIRM, skip).apply()
    }

    /** Alert item ids the user already cleared for [day] (no launcher badge / no re-notify). */
    fun clearedAlertIds(context: Context, day: LocalDate): Set<Long> {
        val p = prefs(context)
        val storedDay = p.getString(KEY_CLEARED_ALERTS_DAY, null)
        if (storedDay != day.toString()) return emptySet()
        val raw = p.getString(KEY_CLEARED_ALERT_IDS, "").orEmpty()
        if (raw.isBlank()) return emptySet()
        return raw.split(',')
            .mapNotNull { it.trim().toLongOrNull() }
            .toSet()
    }

    fun markAlertsCleared(context: Context, day: LocalDate, ids: List<Long>) {
        if (ids.isEmpty()) return
        val p = prefs(context)
        val existing = clearedAlertIds(context, day).toMutableSet()
        existing.addAll(ids)
        p.edit()
            .putString(KEY_CLEARED_ALERTS_DAY, day.toString())
            .putString(KEY_CLEARED_ALERT_IDS, existing.joinToString(","))
            .apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
}
