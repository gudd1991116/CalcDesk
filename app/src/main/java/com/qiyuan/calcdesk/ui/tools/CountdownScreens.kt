package com.qiyuan.calcdesk.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.qiyuan.calcdesk.R
import com.qiyuan.calcdesk.data.CountdownPrefs
import com.qiyuan.calcdesk.data.db.AppDatabase
import com.qiyuan.calcdesk.data.db.CountdownEntity
import com.qiyuan.calcdesk.ui.components.CalcDeskScaffold
import com.qiyuan.calcdesk.ui.components.DateFieldWithPicker
import com.qiyuan.calcdesk.ui.components.InputFieldTextStyle
import com.qiyuan.calcdesk.ui.components.ResultCard
import com.qiyuan.calcdesk.ui.components.SwipeRevealDelete
import com.qiyuan.calcdesk.ui.components.ToolContentColumn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

private enum class CountdownSort { NearToFar, FarToNear }

private fun cdZh(): Boolean = Locale.getDefault().language.startsWith("zh")

private fun cdTr(zh: String, en: String): String = if (cdZh()) zh else en

private fun cdDaysBetween(today: LocalDate, target: LocalDate): Long =
    ChronoUnit.DAYS.between(today, target)

private fun cdParseDate(raw: String): LocalDate? =
    runCatching { LocalDate.parse(raw.trim()) }.getOrNull()

private fun cdAbsDays(today: LocalDate, targetDate: String): Long {
    val t = cdParseDate(targetDate) ?: return Long.MAX_VALUE
    return abs(cdDaysBetween(today, t))
}

private fun cdIsActive(today: LocalDate, item: CountdownEntity): Boolean {
    val t = cdParseDate(item.targetDate) ?: return false
    return cdDaysBetween(today, t) >= 0
}

private fun cdIsExpired(today: LocalDate, item: CountdownEntity): Boolean {
    val t = cdParseDate(item.targetDate) ?: return false
    return cdDaysBetween(today, t) < 0
}

/** 今天 / 还有 N 天 / 昨天 / 前天 / 前 N 天 */
fun countdownRelativeLabel(days: Long): String = when {
    days == 0L -> cdTr("今天", "Today")
    days == 1L -> cdTr("还有 1 天", "In 1 day")
    days > 1L -> cdTr("还有 $days 天", "In $days days")
    days == -1L -> cdTr("昨天", "Yesterday")
    days == -2L -> cdTr("前天", "2 days ago")
    else -> cdTr("前 ${-days} 天", "${-days} days ago")
}

private fun cdWeekday(date: LocalDate): String {
    val zhNames = listOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")
    return if (cdZh()) zhNames[date.dayOfWeek.value - 1]
    else date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
}

private fun cdMainText(name: String, days: Long): String {
    val n = abs(days)
    return when {
        days > 0 -> if (name.isEmpty()) cdTr("还有 $n 天", "$n days remaining")
        else cdTr("距离「$name」还有 $n 天", "$n days until \"$name\"")
        days < 0 -> if (name.isEmpty()) cdTr("已过去 $n 天", "$n days ago")
        else cdTr("「$name」已过去 $n 天", "\"$name\" was $n days ago")
        else -> if (name.isEmpty()) cdTr("就是今天！", "Today!")
        else cdTr("「$name」就是今天！", "\"$name\" is today!")
    }
}

private fun cdDetailLines(name: String, target: LocalDate, today: LocalDate): List<String> {
    val days = cdDaysBetween(today, target)
    val n = abs(days)
    val p = if (days >= 0) Period.between(today, target) else Period.between(target, today)
    val weeks = String.format(Locale.ROOT, "%.1f", n / 7.0)
    val months = String.format(Locale.ROOT, "%.1f", n / 30.44)
    return listOf(
        (if (name.isEmpty()) "" else "$name · ") + "$target · ${cdWeekday(target)}",
        cdTr("约 $weeks 周 · 约 $months 个月", "≈ $weeks weeks · ≈ $months months"),
        cdTr(
            "${p.years} 年 ${p.months} 个月 ${p.days} 天",
            "${p.years}y ${p.months}m ${p.days}d",
        ),
    )
}

private fun nearestActive(items: List<CountdownEntity>, today: LocalDate): CountdownEntity? =
    items.filter { cdIsActive(today, it) }
        .minByOrNull { cdAbsDays(today, it.targetDate) }

@Composable
private fun CountdownDeleteConfirmDialog(
    onConfirm: (dontAskAgain: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var dontAsk by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(cdTr("删除倒计时？", "Delete countdown?")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(cdTr("删除后无法恢复。", "This cannot be undone."))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = dontAsk, onCheckedChange = { dontAsk = it })
                    Text(
                        text = cdTr("下次不再提示", "Don't ask again"),
                        modifier = Modifier.padding(start = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(dontAsk) }) {
                Text(cdTr("删除", "Delete"), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cdTr("取消", "Cancel"))
            }
        },
    )
}

@Composable
private fun CountdownListItemRow(
    item: CountdownEntity,
    today: LocalDate,
    onRequestDelete: () -> Unit,
) {
    val target = cdParseDate(item.targetDate)
    val name = item.title.trim().ifEmpty { cdTr("未命名", "Untitled") }
    val days = target?.let { cdDaysBetween(today, it) }
    val relative = days?.let { countdownRelativeLabel(it) } ?: "—"
    val dateLine = (target?.toString() ?: item.targetDate) +
        (target?.let { " · ${cdWeekday(it)}" } ?: "")

    SwipeRevealDelete(
        deleteLabel = cdTr("删除", "Delete"),
        onDelete = onRequestDelete,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = dateLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = relative,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun CountdownItemList(
    items: List<CountdownEntity>,
    today: LocalDate,
    emptyText: String,
    onDelete: (CountdownEntity) -> Unit,
) {
    val context = LocalContext.current
    var pendingDelete by remember { mutableStateOf<CountdownEntity?>(null) }

    pendingDelete?.let { item ->
        CountdownDeleteConfirmDialog(
            onConfirm = { dontAsk ->
                if (dontAsk) CountdownPrefs.setSkipDeleteConfirm(context, true)
                onDelete(item)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        if (items.isEmpty()) {
            Text(
                text = emptyText,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    CountdownListItemRow(
                        item = item,
                        today = today,
                        onRequestDelete = {
                            if (CountdownPrefs.skipDeleteConfirm(context)) {
                                onDelete(item)
                            } else {
                                pendingDelete = item
                            }
                        },
                    )
                    if (index < items.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CountdownTool() {
    val context = LocalContext.current
    val dao = remember { AppDatabase.get(context).countdownDao() }
    val scope = rememberCoroutineScope()
    val allItems by dao.observeAll().collectAsState(initial = emptyList())
    val today = remember { LocalDate.now() }

    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf(today.toString()) }
    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(CountdownSort.NearToFar) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    val activeItems = remember(allItems, today) {
        allItems.filter { cdIsActive(today, it) }
    }
    val nearest = remember(activeItems, today) { nearestActive(activeItems, today) }
    val filtered = remember(activeItems, query, sort, today) {
        val q = query.trim()
        val base = if (q.isEmpty()) activeItems
        else activeItems.filter { it.title.contains(q, ignoreCase = true) }
        when (sort) {
            CountdownSort.NearToFar -> base.sortedBy { cdAbsDays(today, it.targetDate) }
            CountdownSort.FarToNear -> base.sortedByDescending { cdAbsDays(today, it.targetDate) }
        }
    }

    // 1) 搜索 + 列表
    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(cdTr("搜索标题", "Search title")) },
        textStyle = InputFieldTextStyle(),
        shape = RoundedCornerShape(16.dp),
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { query = "" }) {
                    Icon(Icons.Outlined.Clear, contentDescription = cdTr("清除", "Clear"))
                }
            }
        },
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = cdTr("进行中", "Active"),
            modifier = Modifier
                .weight(1f)
                .padding(top = 8.dp, bottom = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box {
            IconButton(onClick = { sortMenuOpen = true }) {
                Icon(
                    Icons.Outlined.SwapVert,
                    contentDescription = cdTr("排序", "Sort"),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(cdTr("由近及远", "Near → far")) },
                    onClick = {
                        sort = CountdownSort.NearToFar
                        sortMenuOpen = false
                    },
                )
                DropdownMenuItem(
                    text = { Text(cdTr("由远及近", "Far → near")) },
                    onClick = {
                        sort = CountdownSort.FarToNear
                        sortMenuOpen = false
                    },
                )
            }
        }
    }
    CountdownItemList(
        items = filtered,
        today = today,
        emptyText = if (query.isNotBlank()) {
            cdTr("没有匹配的标题", "No matching titles")
        } else {
            cdTr("暂无进行中的倒计时", "No active countdowns")
        },
        onDelete = { scope.launch { dao.delete(it) } },
    )

    // 2) 高亮结果（无数据时显示今天）
    if (nearest == null) {
        ResultCard(
            main = cdTr("今天", "Today"),
            lines = listOf(
                "$today · ${cdWeekday(today)}",
                cdTr("还没有保存倒计时，可在下方添加", "No saved countdowns yet — add one below"),
            ),
        )
    } else {
        val t = cdParseDate(nearest.targetDate)
        if (t != null) {
            val name = nearest.title.trim()
            val days = cdDaysBetween(today, t)
            ResultCard(
                main = cdMainText(name, days),
                lines = cdDetailLines(name, t, today),
            )
        }
    }

    // 3) 标题与日期输入
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(cdTr("标题", "Title")) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = InputFieldTextStyle(),
            shape = RoundedCornerShape(16.dp),
            trailingIcon = {
                if (title.isNotEmpty()) {
                    IconButton(onClick = { title = "" }) {
                        Icon(Icons.Outlined.Clear, contentDescription = cdTr("清除", "Clear"))
                    }
                }
            },
        )
        Button(
            onClick = {
                val date = cdParseDate(target) ?: return@Button
                val name = title.trim().ifEmpty { cdTr("未命名", "Untitled") }
                scope.launch {
                    dao.insert(CountdownEntity(title = name, targetDate = date.toString()))
                    title = ""
                    target = LocalDate.now().toString()
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(cdTr("保存", "Save"))
        }
    }
    DateFieldWithPicker(
        target,
        { target = it },
        label = cdTr("目标日期 (yyyy-MM-dd)", "Target date (yyyy-MM-dd)"),
    )
}

@Composable
fun CountdownHistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.get(context).countdownDao() }
    val scope = rememberCoroutineScope()
    val allItems by dao.observeAll().collectAsState(initial = emptyList())
    val today = remember { LocalDate.now() }
    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(CountdownSort.NearToFar) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    val history = remember(allItems, today) {
        allItems.filter { cdIsExpired(today, it) }
    }

    CalcDeskScaffold(
        title = stringResource(R.string.countdown_history),
        onBack = onBack,
    ) { padding ->
        ToolContentColumn(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(cdTr("搜索标题", "Search title")) },
                textStyle = InputFieldTextStyle(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Outlined.Clear, contentDescription = cdTr("清除", "Clear"))
                        }
                    }
                },
            )

            val filtered = remember(history, query, sort, today) {
                val q = query.trim()
                val base = if (q.isEmpty()) history
                else history.filter { it.title.contains(q, ignoreCase = true) }
                when (sort) {
                    // 由近及远：刚过期的更靠前（abs 更小）
                    CountdownSort.NearToFar -> base.sortedBy { cdAbsDays(today, it.targetDate) }
                    CountdownSort.FarToNear -> base.sortedByDescending { cdAbsDays(today, it.targetDate) }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = cdTr("历史记录", "History"),
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box {
                    IconButton(onClick = { sortMenuOpen = true }) {
                        Icon(
                            Icons.Outlined.SwapVert,
                            contentDescription = cdTr("排序", "Sort"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(cdTr("由近及远", "Near → far")) },
                            onClick = {
                                sort = CountdownSort.NearToFar
                                sortMenuOpen = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(cdTr("由远及近", "Far → near")) },
                            onClick = {
                                sort = CountdownSort.FarToNear
                                sortMenuOpen = false
                            },
                        )
                    }
                }
            }

            CountdownItemList(
                items = filtered,
                today = today,
                emptyText = cdTr("暂无过期倒计时", "No expired countdowns"),
                onDelete = { scope.launch { dao.delete(it) } },
            )
        }
    }
}
