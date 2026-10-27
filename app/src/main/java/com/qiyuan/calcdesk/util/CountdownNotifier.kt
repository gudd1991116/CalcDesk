package com.qiyuan.calcdesk.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.qiyuan.calcdesk.MainActivity
import com.qiyuan.calcdesk.data.CountdownPrefs
import com.qiyuan.calcdesk.data.db.CountdownEntity
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

data class CountdownDueAlert(
    val item: CountdownEntity,
    /** 0 = today, 1 = tomorrow */
    val daysUntil: Long,
) {
    fun message(zh: Boolean): String {
        val title = item.title.trim().ifEmpty {
            if (zh) "未命名" else "Untitled"
        }
        return when (daysUntil) {
            0L -> if (zh) "今天是「$title」" else "Today is \"$title\""
            else -> if (zh) "明天就是「$title」了" else "Tomorrow is \"$title\""
        }
    }
}

object CountdownNotifier {
    const val CHANNEL_ID = "countdown_due"
    private const val NOTIFICATION_ID_BASE = 4100

    /** Bumped when badge / cleared-alert prefs change so UI can refresh. */
    val badgeRevision = kotlinx.coroutines.flow.MutableStateFlow(0)

    private fun bumpBadgeRevision() {
        badgeRevision.value = badgeRevision.value + 1
    }

    fun notificationId(itemId: Long): Int = NOTIFICATION_ID_BASE + itemId.toInt()

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = context.getSystemService(NotificationManager::class.java) ?: return
        val zh = Locale.getDefault().language.startsWith("zh")
        val channel = NotificationChannel(
            CHANNEL_ID,
            if (zh) "倒计时提醒" else "Countdown alerts",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = if (zh) "今天或明天到期的倒计时" else "Countdowns due today or tomorrow"
        }
        mgr.createNotificationChannel(channel)
    }

    fun dueAlerts(items: List<CountdownEntity>, today: LocalDate = LocalDate.now()): List<CountdownDueAlert> =
        items.mapNotNull { item ->
            val date = runCatching { LocalDate.parse(item.targetDate) }.getOrNull() ?: return@mapNotNull null
            val days = ChronoUnit.DAYS.between(today, date)
            if (days !in 0L..1L) return@mapNotNull null
            CountdownDueAlert(item, days)
        }.sortedBy { it.daysUntil }

    /** Alerts that still contribute to the launcher badge (not cleared in-app today). */
    fun unreadDueAlerts(
        context: Context,
        items: List<CountdownEntity>,
        today: LocalDate = LocalDate.now(),
    ): List<CountdownDueAlert> {
        val cleared = CountdownPrefs.clearedAlertIds(context, today)
        return dueAlerts(items, today).filter { it.item.id !in cleared }
    }

    fun cancelAllCountdownNotifications(context: Context) {
        val mgr = NotificationManagerCompat.from(context)
        // Cancel a reasonable id range; also cancel known due ids when available.
        for (i in 0 until 500) {
            mgr.cancel(NOTIFICATION_ID_BASE + i)
        }
    }

    fun cancelAlert(context: Context, itemId: Long) {
        NotificationManagerCompat.from(context).cancel(notificationId(itemId))
    }

    /** Clears system notifications (launcher badge) and marks them read for today. */
    fun clearBadgeAndMarkRead(context: Context, items: List<CountdownEntity>) {
        val today = LocalDate.now()
        val ids = dueAlerts(items, today).map { it.item.id }
        cancelAllCountdownNotifications(context)
        CountdownPrefs.markAlertsCleared(context, today, ids)
        bumpBadgeRevision()
    }

    fun markAlertRead(context: Context, itemId: Long) {
        cancelAlert(context, itemId)
        CountdownPrefs.markAlertsCleared(context, LocalDate.now(), listOf(itemId))
        bumpBadgeRevision()
    }

    /**
     * Posts a local notification for each unread due countdown (today / tomorrow).
     * Tap opens the in-app alerts screen (or countdown tool).
     */
    fun notifyIfDue(context: Context, items: List<CountdownEntity>) {
        ensureChannel(context)
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val today = LocalDate.now()
        val zh = Locale.getDefault().language.startsWith("zh")
        val due = unreadDueAlerts(context, items, today)

        // Drop stale notifications for items no longer due / already cleared.
        val dueIds = due.map { it.item.id }.toSet()
        items.forEach { item ->
            if (item.id !in dueIds) cancelAlert(context, item.id)
        }
        if (due.isEmpty()) return

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_ALERTS, true)
        }
        val pending = PendingIntent.getActivity(
            context,
            1001,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        due.forEach { alert ->
            val text = alert.message(zh)
            val notifTitle = if (zh) "倒计时提醒" else "Countdown"
            val viewLabel = if (zh) "查看" else "View"
            val itemIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_OPEN_ALERTS, true)
                putExtra(MainActivity.EXTRA_ALERT_ITEM_ID, alert.item.id)
            }
            val itemPending = PendingIntent.getActivity(
                context,
                notificationId(alert.item.id),
                itemIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                .setContentTitle(notifTitle)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(itemPending)
                .addAction(0, viewLabel, pending)
                .setAutoCancel(true)
                .setNumber(due.size)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            runCatching {
                NotificationManagerCompat.from(context)
                    .notify(notificationId(alert.item.id), notification)
            }
        }
    }
}
