package com.qiyuan.calcdesk.ui.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qiyuan.calcdesk.R
import com.qiyuan.calcdesk.data.db.AppDatabase
import com.qiyuan.calcdesk.ui.components.CalcDeskScaffold
import com.qiyuan.calcdesk.ui.components.ToolContentColumn
import com.qiyuan.calcdesk.util.CountdownDueAlert
import com.qiyuan.calcdesk.util.CountdownNotifier
import java.time.LocalDate
import java.util.Locale

@Composable
fun AlertsScreen(
    onBack: () -> Unit,
    onOpenCountdown: () -> Unit,
    highlightItemId: Long? = null,
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.get(context).countdownDao() }
    val allItems by dao.observeAll().collectAsState(initial = emptyList())
    val today = remember { LocalDate.now() }
    val zh = remember { Locale.getDefault().language.startsWith("zh") }

    val due = remember(allItems, today) {
        CountdownNotifier.dueAlerts(allItems, today)
    }

    // Opening this screen clears the launcher badge (system notifications).
    LaunchedEffect(allItems) {
        if (allItems.isNotEmpty() || due.isNotEmpty()) {
            CountdownNotifier.clearBadgeAndMarkRead(context, allItems)
        } else {
            CountdownNotifier.cancelAllCountdownNotifications(context)
        }
    }

    CalcDeskScaffold(
        title = stringResource(R.string.alerts_title),
        onBack = onBack,
        actions = {
            if (due.isNotEmpty()) {
                TextButton(onClick = {
                    CountdownNotifier.clearBadgeAndMarkRead(context, allItems)
                    onOpenCountdown()
                }) {
                    Text(stringResource(R.string.alerts_clear_badge))
                }
            }
        },
    ) { padding ->
        ToolContentColumn(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.alerts_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (due.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.alerts_empty),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        due.forEachIndexed { index, alert ->
                            AlertRow(
                                alert = alert,
                                zh = zh,
                                highlighted = highlightItemId != null && alert.item.id == highlightItemId,
                                onClick = {
                                    CountdownNotifier.markAlertRead(context, alert.item.id)
                                    onOpenCountdown()
                                },
                            )
                            if (index < due.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertRow(
    alert: CountdownDueAlert,
    zh: Boolean,
    highlighted: Boolean,
    onClick: () -> Unit,
) {
    val item = alert.item
    val badge = when (alert.daysUntil) {
        0L -> if (zh) "今天" else "Today"
        else -> if (zh) "明天" else "Tomorrow"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = alert.message(zh),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (highlighted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            Text(
                text = item.targetDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = badge,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
