package com.qiyuan.calcdesk

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.qiyuan.calcdesk.data.db.AppDatabase
import com.qiyuan.calcdesk.navigation.CalcDeskNavHost
import com.qiyuan.calcdesk.ui.theme.CalcDeskTheme
import com.qiyuan.calcdesk.util.CountdownNotifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private var openToolId by mutableStateOf<String?>(null)
    private var openAlerts by mutableStateOf(false)
    private var alertItemId by mutableStateOf<Long?>(null)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) refreshCountdownNotifications()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        applyOpenExtras(intent)
        setContent {
            CalcDeskTheme {
                CalcDeskNavHost(
                    openToolId = openToolId,
                    openAlerts = openAlerts,
                    alertItemId = alertItemId,
                    onOpenToolConsumed = { openToolId = null },
                    onOpenAlertsConsumed = {
                        openAlerts = false
                        alertItemId = null
                    },
                )
            }
        }
        ensureNotificationPermissionThenNotify()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        applyOpenExtras(intent)
    }

    private fun applyOpenExtras(intent: Intent?) {
        if (intent == null) return
        openToolId = intent.getStringExtra(EXTRA_OPEN_TOOL)
        openAlerts = intent.getBooleanExtra(EXTRA_OPEN_ALERTS, false)
        alertItemId = if (intent.hasExtra(EXTRA_ALERT_ITEM_ID)) {
            intent.getLongExtra(EXTRA_ALERT_ITEM_ID, -1L).takeIf { it >= 0 }
        } else {
            null
        }
        if (openAlerts) openToolId = null
    }

    private fun ensureNotificationPermissionThenNotify() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        refreshCountdownNotifications()
    }

    private fun refreshCountdownNotifications() {
        lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                AppDatabase.get(this@MainActivity).countdownDao().observeAll().first()
            }
            CountdownNotifier.notifyIfDue(this@MainActivity, items)
        }
    }

    companion object {
        const val EXTRA_OPEN_TOOL = "open_tool"
        const val EXTRA_OPEN_ALERTS = "open_alerts"
        const val EXTRA_ALERT_ITEM_ID = "alert_item_id"
    }
}
