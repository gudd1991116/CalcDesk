package com.qiyuan.calcdesk.ui.tools

import android.icu.util.Calendar as IcuCalendar
import android.icu.util.ChineseCalendar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.qiyuan.calcdesk.R
import com.qiyuan.calcdesk.data.TimeIntervalHelp
import com.qiyuan.calcdesk.ui.components.CalcTextField
import com.qiyuan.calcdesk.ui.components.DateFieldWithPicker
import com.qiyuan.calcdesk.ui.components.DateTimeFieldWithPicker
import com.qiyuan.calcdesk.ui.components.GhostButton
import com.qiyuan.calcdesk.ui.components.InputFieldTextStyle
import com.qiyuan.calcdesk.ui.components.NumberField
import com.qiyuan.calcdesk.ui.components.PrimaryButton
import com.qiyuan.calcdesk.ui.components.ResultCard
import com.qiyuan.calcdesk.ui.components.SectionLabel
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.MonthDay
import java.time.Period
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

// ───────────────────────────── Router ─────────────────────────────

@Composable
fun DateTimeToolRouter(
    toolId: String,
    onOpenTimestampSnippets: () -> Unit = {},
) {
    when (toolId) {
        "timestamp" -> TimestampTool(onOpenSnippets = onOpenTimestampSnippets)
        "time-interval" -> TimeIntervalTool()
        "date-interval" -> DateIntervalTool()
        "date-offset" -> DateOffsetTool()
        "age" -> AgeTool()
        "timezone" -> TimezoneTool()
        "countdown" -> CountdownTool()
        "stopwatch" -> StopwatchTool()
        else -> Text("Unknown datetime tool")
    }
}

// ───────────────────────────── Shared helpers ─────────────────────────────

private fun isZh(): Boolean = Locale.getDefault().language.startsWith("zh")

private fun tr(zh: String, en: String): String = if (isZh()) zh else en

private val OUT_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

private fun strictFmt(pattern: String): DateTimeFormatter =
    DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT)

private val DT_PARSERS: List<DateTimeFormatter> = listOf(
    strictFmt("uuuu-M-d H:mm:ss"),
    strictFmt("uuuu-M-d H:mm"),
    strictFmt("uuuu-M-d'T'H:mm:ss"),
    strictFmt("uuuu-M-d'T'H:mm"),
)
private val DATE_PARSER: DateTimeFormatter = strictFmt("uuuu-M-d")

private fun parseDate(text: String): LocalDate? =
    runCatching { LocalDate.parse(text.trim(), DATE_PARSER) }.getOrNull()

/** Accepts yyyy-MM-dd HH:mm:ss, yyyy-MM-dd HH:mm, ISO "T" form, or date only (00:00:00). */
private fun parseDateTime(text: String): LocalDateTime? {
    val s = text.trim()
    return DT_PARSERS.firstNotNullOfOrNull { f -> runCatching { LocalDateTime.parse(s, f) }.getOrNull() }
        ?: parseDate(s)?.atStartOfDay()
}

private fun weekdayName(date: LocalDate): String =
    date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())

private fun weekdayName(day: DayOfWeek): String =
    day.getDisplayName(TextStyle.FULL, Locale.getDefault())

private fun offsetLabel(offset: ZoneOffset): String {
    val total = offset.totalSeconds
    val sign = if (total >= 0) "+" else "-"
    val a = abs(total)
    return String.format(Locale.ROOT, "UTC%s%02d:%02d", sign, a / 3600, (a % 3600) / 60)
}

private fun fmt1(x: Double): String = String.format(Locale.ROOT, "%.1f", x)

private fun fmtInt(n: Long): String = String.format(Locale.getDefault(), "%,d", n)

private fun nowSecondText(): String = LocalDateTime.now().withNano(0).format(OUT_FMT)

@Composable
private fun ToggleRow(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { i, label ->
            val m = Modifier.weight(1f)
            if (i == selected) PrimaryButton(label, onClick = { onSelect(i) }, modifier = m)
            else GhostButton(label, onClick = { onSelect(i) }, modifier = m)
        }
    }
}

// ───────────────────────────── 1. Timestamp ─────────────────────────────

private data class EpochParse(
    val millis: Long?,
    val asSeconds: Boolean?,
    val digits: Int,
    val error: String?,
)

/** Auto: digit length ≤ 11 → seconds, else milliseconds. */
private fun parseEpochInput(text: String): EpochParse {
    val raw = text.trim()
    if (raw.isEmpty()) {
        return EpochParse(null, null, 0, tr("请输入时间戳", "Enter a timestamp"))
    }
    if (!raw.matches(Regex("^[+-]?\\d+$"))) {
        return EpochParse(null, null, 0, tr("时间戳格式不正确（仅支持整数）", "Invalid format (digits only)"))
    }
    val digits = raw.trimStart('-', '+').length
    if (digits == 0) {
        return EpochParse(null, null, 0, tr("时间戳格式不正确", "Invalid timestamp format"))
    }
    // Typical Unix: ~10 digits (s) or ~13 (ms). Reject absurd lengths.
    if (digits < 9 || digits > 16) {
        return EpochParse(
            null,
            null,
            digits,
            tr(
                "时间戳长度不正确（秒约 10 位，毫秒约 13 位）",
                "Invalid length (≈10 digits for seconds, ≈13 for millis)",
            ),
        )
    }
    val v = raw.toLongOrNull()
        ?: return EpochParse(null, null, digits, tr("时间戳格式不正确", "Invalid timestamp format"))
    val asSeconds = digits <= 11
    val ms = if (asSeconds) {
        runCatching { Math.multiplyExact(v, 1000L) }.getOrNull()
    } else {
        v
    }
    if (ms == null || runCatching { Instant.ofEpochMilli(ms) }.getOrNull() == null) {
        return EpochParse(
            null,
            asSeconds,
            digits,
            tr("时间戳超出可表示范围", "Timestamp out of representable range"),
        )
    }
    return EpochParse(ms, asSeconds, digits, null)
}

private fun formatLocalFromMillis(ms: Long, zone: ZoneId): String =
    Instant.ofEpochMilli(ms).atZone(zone).toLocalDateTime().withNano(0).format(OUT_FMT)

@Composable
fun TimestampTool(onOpenSnippets: () -> Unit = {}) {
    val zone = remember { ZoneId.systemDefault() }
    val clipboard = LocalClipboardManager.current
    var live by remember { mutableStateOf(true) }
    var epochText by remember { mutableStateOf(System.currentTimeMillis().toString()) }
    var localText by remember { mutableStateOf(nowSecondText()) }
    var epochError by remember { mutableStateOf<String?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }
    var copiedFlash by remember { mutableStateOf(false) }

    fun applyNow(ms: Long = System.currentTimeMillis()) {
        epochText = ms.toString()
        localText = formatLocalFromMillis(ms, zone)
        epochError = null
        localError = null
    }

    fun syncFromEpoch(text: String) {
        val parsed = parseEpochInput(text)
        epochError = parsed.error
        if (parsed.millis != null) {
            localText = formatLocalFromMillis(parsed.millis, zone)
            localError = null
        }
    }

    fun syncFromLocal(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            localError = tr("请输入本地时间", "Enter local time")
            return
        }
        val ldt = parseDateTime(trimmed)
        if (ldt == null) {
            localError = tr(
                "本地时间格式不正确（yyyy-MM-dd HH:mm:ss）",
                "Invalid local time format (yyyy-MM-dd HH:mm:ss)",
            )
            return
        }
        localError = null
        runCatching { ldt.atZone(zone).toInstant().toEpochMilli() }.getOrNull()?.let { ms ->
            epochText = ms.toString()
            epochError = null
        } ?: run {
            localError = tr("本地时间超出可表示范围", "Local time out of representable range")
        }
    }

    LaunchedEffect(live) {
        if (!live) return@LaunchedEffect
        var lastSec = -1L
        while (true) {
            val now = System.currentTimeMillis()
            epochText = now.toString()
            epochError = null
            val sec = now / 1000L
            if (sec != lastSec) {
                lastSec = sec
                localText = formatLocalFromMillis(now, zone)
                localError = null
            }
            delay(200)
        }
    }

    LaunchedEffect(copiedFlash) {
        if (copiedFlash) {
            delay(1500)
            copiedFlash = false
        }
    }

    OutlinedTextField(
        value = epochText,
        onValueChange = {
            live = false
            epochText = it
            syncFromEpoch(it)
        },
        label = { Text(tr("Unix 时间戳", "Unix timestamp")) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = InputFieldTextStyle(),
        isError = epochError != null,
        supportingText = epochError?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(16.dp),
        trailingIcon = {
            Row {
                IconButton(onClick = {
                    live = false
                    epochText = ""
                    epochError = tr("请输入时间戳", "Enter a timestamp")
                }) {
                    Icon(Icons.Outlined.Clear, contentDescription = stringResource(R.string.clear))
                }
                IconButton(onClick = {
                    val clip = clipboard.getText()?.text?.trim().orEmpty()
                    if (clip.isNotEmpty()) {
                        live = false
                        epochText = clip
                        syncFromEpoch(clip)
                    }
                }) {
                    Icon(Icons.Outlined.ContentPaste, contentDescription = tr("粘贴", "Paste"))
                }
            }
        },
    )

    DateTimeFieldWithPicker(
        value = localText,
        onValueChange = {
            live = false
            localText = it
            syncFromLocal(it)
        },
        label = tr("本地时间 (yyyy-MM-dd HH:mm:ss)", "Local time (yyyy-MM-dd HH:mm:ss)"),
        isError = localError != null,
        supportingText = localError?.let { { Text(it) } },
        trailingExtras = {
            IconButton(onClick = {
                live = false
                localText = ""
                localError = tr("请输入本地时间", "Enter local time")
            }) {
                Icon(Icons.Outlined.Clear, contentDescription = stringResource(R.string.clear))
            }
            IconButton(onClick = {
                val clip = clipboard.getText()?.text?.trim().orEmpty()
                if (clip.isNotEmpty()) {
                    live = false
                    localText = clip
                    syncFromLocal(clip)
                }
            }) {
                Icon(Icons.Outlined.ContentPaste, contentDescription = tr("粘贴", "Paste"))
            }
        },
    )

    PrimaryButton(
        text = if (live) tr("跟随当前时间中…", "Following now…") else tr("现在", "Now"),
        onClick = {
            live = true
            applyNow()
        },
        enabled = !live,
    )

    val parsed = parseEpochInput(epochText)
    val ms = parsed.millis
    val zdt = ms?.let { runCatching { Instant.ofEpochMilli(it).atZone(zone) }.getOrNull() }

    if (ms == null || zdt == null) {
        ResultCard(
            main = epochError ?: localError ?: tr("无效的时间戳", "Invalid timestamp"),
        )
    } else {
        val detectedUnit = if (parsed.asSeconds == true) {
            tr("秒（自动识别）", "seconds (auto)")
        } else {
            tr("毫秒（自动识别）", "milliseconds (auto)")
        }
        val mainTime = zdt.toLocalDateTime().format(OUT_FMT)
        val rows = listOf(
            tr("输入单位", "Input unit") to detectedUnit,
            tr("秒", "Seconds") to Math.floorDiv(ms, 1000L).toString(),
            tr("毫秒", "Millis") to ms.toString(),
            "ISO-8601 (UTC)" to Instant.ofEpochMilli(ms).toString(),
            "ISO-8601 (${tr("本地", "local")})" to DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zdt),
            tr("时区", "Zone") to "${zone.id} (${offsetLabel(zdt.offset)})",
            tr("星期", "Weekday") to weekdayName(zdt.dayOfWeek),
        )
        TimestampResultPanel(
            main = mainTime,
            rows = rows,
            copied = copiedFlash,
            onCopy = {
                val body = buildString {
                    appendLine(mainTime)
                    rows.forEach { (label, value) -> appendLine("$label: $value") }
                }.trimEnd()
                clipboard.setText(AnnotatedString(body))
                copiedFlash = true
            },
        )
    }

    TimestampHelpSection(onOpenSnippets = onOpenSnippets)
}

@Composable
private fun TimestampResultPanel(
    main: String,
    rows: List<Pair<String, String>>,
    copied: Boolean,
    onCopy: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.result),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton(onClick = onCopy) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                    Text(
                        text = if (copied) stringResource(R.string.copied) else stringResource(R.string.copy),
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }
            Text(
                text = main,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            rows.forEach { (label, value) ->
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        text = "$label：",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

// ───────────────────────────── 2. Time interval ─────────────────────────────

@Composable
fun TimeIntervalTool() {
    var start by remember { mutableStateOf(LocalDate.now().atTime(9, 0).format(OUT_FMT)) }
    var end by remember { mutableStateOf(LocalDate.now().atTime(18, 30).format(OUT_FMT)) }

    DateTimeFieldWithPicker(
        value = start,
        onValueChange = { start = it },
        label = tr("开始 (yyyy-MM-dd HH:mm:ss)", "Start (yyyy-MM-dd HH:mm:ss)"),
    )
    DateTimeFieldWithPicker(
        value = end,
        onValueChange = { end = it },
        label = tr("结束 (yyyy-MM-dd HH:mm:ss)", "End (yyyy-MM-dd HH:mm:ss)"),
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        GhostButton(tr("交换", "Swap"), onClick = {
            val tmp = start
            start = end
            end = tmp
        }, modifier = Modifier.weight(1f))
        GhostButton(tr("结束=现在", "End = now"), onClick = {
            end = nowSecondText()
        }, modifier = Modifier.weight(1f))
    }

    val a = parseDateTime(start)
    val b = parseDateTime(end)
    if (a == null || b == null) {
        ResultCard(main = tr("请输入有效的日期时间", "Enter valid date-times"))
    } else {
        val totalSec = Duration.between(a, b).seconds
        val negative = totalSec < 0
        val s = abs(totalSec)
        val days = s / 86400
        val hours = (s % 86400) / 3600
        val minutes = (s % 3600) / 60
        val seconds = s % 60
        val sign = if (negative) "-" else ""
        val main = if (isZh()) {
            "$sign$days 天 $hours 小时 $minutes 分 $seconds 秒"
        } else {
            "$sign${days}d ${hours}h ${minutes}m ${seconds}s"
        }
        val lines = mutableListOf(
            tr("总小时：", "Total hours: ") + sign + fmt1(s / 3600.0),
            tr("总分钟：", "Total minutes: ") + sign + fmt1(s / 60.0),
            tr("总秒数：", "Total seconds: ") + sign + s,
            tr("总毫秒：", "Total millis: ") + sign + s * 1000,
        )
        if (negative) lines.add(0, tr("结束时间早于开始时间", "End is before start"))
        ResultCard(main = main, lines = lines)
    }

    TimeIntervalHelpSection()
}

@Composable
private fun TimeIntervalHelpSection() {
    val zh = isZh()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = if (zh) TimeIntervalHelp.titleZh else TimeIntervalHelp.titleEn,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = if (zh) TimeIntervalHelp.introZh else TimeIntervalHelp.introEn,
            style = MaterialTheme.typography.bodyMedium,
        )
        TimeIntervalHelp.scenes.forEach { scene ->
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = scene.title(zh),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = scene.body(zh),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = if (zh) TimeIntervalHelp.tipZh else TimeIntervalHelp.tipEn,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ───────────────────────────── 3. Date interval ─────────────────────────────

@Composable fun DateIntervalTool() {
    var d1 by remember { mutableStateOf(LocalDate.now().toString()) }
    var d2 by remember { mutableStateOf(LocalDate.now().plusDays(30).toString()) }
    var include by remember { mutableIntStateOf(0) } // 0 = exclude end day, 1 = include end day (+1)

    DateFieldWithPicker(d1, { d1 = it }, label = tr("开始日期 (yyyy-MM-dd)", "Start date (yyyy-MM-dd)"))
    DateFieldWithPicker(d2, { d2 = it }, label = tr("结束日期 (yyyy-MM-dd)", "End date (yyyy-MM-dd)"))
    ToggleRow(
        listOf(tr("不含结束日", "Exclude end day"), tr("含结束日 (+1日)", "Include end day (+1)")),
        include,
    ) { include = it }
    GhostButton(tr("交换", "Swap"), onClick = {
        val tmp = d1
        d1 = d2
        d2 = tmp
    })

    val a = parseDate(d1)
    val b = parseDate(d2)
    if (a == null || b == null) {
        ResultCard(main = tr("请输入有效日期", "Enter valid dates"))
    } else {
        val reversed = b.isBefore(a)
        val earlier = if (reversed) b else a
        val later = if (reversed) a else b
        val extra = if (include == 1) 1L else 0L
        val effEnd = later.plusDays(extra)
        val days = ChronoUnit.DAYS.between(earlier, effEnd)
        val p = Period.between(earlier, effEnd)
        val totalMonths = ChronoUnit.MONTHS.between(earlier, effEnd)
        val weeks = days / 7
        val remDays = days % 7
        val sign = if (reversed) "-" else ""
        val lines = mutableListOf(
            tr("年月日：", "Y/M/D: ") + if (isZh()) {
                "${p.years} 年 ${p.months} 个月 ${p.days} 天"
            } else {
                "${p.years}y ${p.months}m ${p.days}d"
            },
            tr("总月数：", "Total months: ") + sign + totalMonths,
            tr("折合周数：", "Weeks: ") + sign + if (isZh()) {
                "$weeks 周 $remDays 天"
            } else {
                "$weeks weeks $remDays days"
            },
            tr("折合年数：", "Years (approx): ") + sign + fmt1(days / 365.25),
            tr("总小时：", "Total hours: ") + sign + fmtInt(days * 24),
            "${earlier} → ${later}" + if (include == 1) tr("（含结束日）", " (end day included)") else "",
        )
        if (reversed) lines.add(0, tr("结束日期早于开始日期", "End date is before start date"))
        ResultCard(main = sign + tr("$days 天", "$days days"), lines = lines)
    }
}

// ───────────────────────────── 4. Date offset ─────────────────────────────

@Composable fun DateOffsetTool() {
    var base by remember { mutableStateOf(LocalDate.now().toString()) }
    var years by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("7") }
    var dir by remember { mutableIntStateOf(0) } // 0 = future, 1 = past

    DateFieldWithPicker(base, { base = it }, label = tr("基准日期 (yyyy-MM-dd)", "Base date (yyyy-MM-dd)"))
    GhostButton(tr("今天", "Today"), onClick = { base = LocalDate.now().toString() })
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NumberField(
            years,
            { years = it.filter(Char::isDigit).take(4) },
            label = tr("年", "Years"),
            modifier = Modifier.weight(1f),
            placeholder = "0",
        )
        NumberField(
            months,
            { months = it.filter(Char::isDigit).take(5) },
            label = tr("月", "Months"),
            modifier = Modifier.weight(1f),
            placeholder = "0",
        )
        NumberField(days, { days = it.filter(Char::isDigit).take(7) }, label = tr("日", "Days"), modifier = Modifier.weight(1f))
    }
    ToggleRow(listOf(tr("未来 (+)", "Future (+)"), tr("过去 (−)", "Past (−)")), dir) { dir = it }

    val baseDate = parseDate(base)
    val y = if (years.isBlank()) 0L else years.toLongOrNull()
    val m = if (months.isBlank()) 0L else months.toLongOrNull()
    val d = if (days.isBlank()) 0L else days.toLongOrNull()
    val out = if (baseDate == null || y == null || m == null || d == null) {
        null
    } else {
        val s = if (dir == 0) 1L else -1L
        runCatching { baseDate.plusYears(s * y).plusMonths(s * m).plusDays(s * d) }.getOrNull()
    }
    if (out == null) {
        ResultCard(main = tr("请输入有效日期与数值", "Enter a valid date and numbers"))
    } else {
        val today = LocalDate.now()
        val diffToday = ChronoUnit.DAYS.between(today, out)
        val rel = when {
            diffToday > 0 -> tr("距今天还有 $diffToday 天", "$diffToday days from today")
            diffToday < 0 -> tr("已过去 ${-diffToday} 天", "${-diffToday} days ago")
            else -> tr("就是今天", "Today")
        }
        val opSign = if (dir == 0) "+" else "-"
        ResultCard(
            main = out.toString(),
            lines = listOf(
                "$base $opSign ${y}${tr("年", "y")} ${m}${tr("月", "m")} ${d}${tr("日", "d")}",
                tr("星期：", "Weekday: ") + weekdayName(out),
                tr("当年第 ${out.dayOfYear} 天", "Day ${out.dayOfYear} of the year") +
                    " · " + if (out.isLeapYear) tr("闰年", "leap year") else tr("平年", "common year"),
                rel,
            ),
        )
    }
}

// ───────────────────────────── 5. Age (+ zodiac / lunar) ─────────────────────────────

private data class WesternSign(val zh: String, val en: String, val from: Int, val to: Int, val range: String)

private val WESTERN_SIGNS = listOf(
    WesternSign("摩羯座", "Capricorn", 1222, 119, "12.22 - 01.19"),
    WesternSign("水瓶座", "Aquarius", 120, 218, "01.20 - 02.18"),
    WesternSign("双鱼座", "Pisces", 219, 320, "02.19 - 03.20"),
    WesternSign("白羊座", "Aries", 321, 419, "03.21 - 04.19"),
    WesternSign("金牛座", "Taurus", 420, 520, "04.20 - 05.20"),
    WesternSign("双子座", "Gemini", 521, 621, "05.21 - 06.21"),
    WesternSign("巨蟹座", "Cancer", 622, 722, "06.22 - 07.22"),
    WesternSign("狮子座", "Leo", 723, 822, "07.23 - 08.22"),
    WesternSign("处女座", "Virgo", 823, 922, "08.23 - 09.22"),
    WesternSign("天秤座", "Libra", 923, 1023, "09.23 - 10.23"),
    WesternSign("天蝎座", "Scorpio", 1024, 1122, "10.24 - 11.22"),
    WesternSign("射手座", "Sagittarius", 1123, 1221, "11.23 - 12.21"),
)

private fun westernSign(month: Int, day: Int): WesternSign {
    val md = month * 100 + day
    return WESTERN_SIGNS.firstOrNull { z ->
        if (z.from > z.to) md >= z.from || md <= z.to else md in z.from..z.to
    } ?: WESTERN_SIGNS[0]
}

private const val STEMS = "甲乙丙丁戊己庚辛壬癸"
private const val BRANCHES = "子丑寅卯辰巳午未申酉戌亥"
private val ANIMALS_ZH = listOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")
private val ANIMALS_EN = listOf("Rat", "Ox", "Tiger", "Rabbit", "Dragon", "Snake", "Horse", "Goat", "Monkey", "Rooster", "Dog", "Pig")

/** index0 = 0 for 甲子 (rat) … 59 for 癸亥. */
private fun ganzhiOf(index0: Int): String {
    val i = Math.floorMod(index0, 60)
    return "" + STEMS[i % 10] + BRANCHES[i % 12]
}

private fun animalName(index0: Int): String {
    val i = Math.floorMod(index0, 12)
    return if (isZh()) ANIMALS_ZH[i] else ANIMALS_EN[i]
}

private data class LunarInfo(val cycleYear: Int, val month: Int, val day: Int, val leap: Boolean)

private const val CN_NUM = "一二三四五六七八九十"
private val LUNAR_MONTHS = listOf("正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊")

private fun lunarDayName(d: Int): String = when {
    d <= 10 -> "初" + CN_NUM[d - 1]
    d < 20 -> "十" + CN_NUM[d - 11]
    d == 20 -> "二十"
    d < 30 -> "廿" + CN_NUM[d - 21]
    else -> "三十"
}

private fun lunarOf(cal: ChineseCalendar, date: LocalDate, zone: ZoneId): LunarInfo? = runCatching {
    cal.timeInMillis = date.atStartOfDay(zone).toInstant().toEpochMilli()
    LunarInfo(
        cycleYear = cal.get(IcuCalendar.YEAR),
        month = cal.get(IcuCalendar.MONTH) + 1,
        day = cal.get(IcuCalendar.DATE),
        leap = cal.get(IcuCalendar.IS_LEAP_MONTH) == 1,
    )
}.getOrNull()

private fun lunarText(l: LunarInfo): String {
    val gz = ganzhiOf(l.cycleYear - 1)
    return if (isZh()) {
        "${gz}年 ${if (l.leap) "闰" else ""}${LUNAR_MONTHS[(l.month - 1).coerceIn(0, 11)]}月${lunarDayName(l.day)}"
    } else {
        "$gz year, ${if (l.leap) "leap " else ""}month ${l.month}, day ${l.day}"
    }
}

/** Next solar date (from [from], inclusive) whose lunar month/day matches the lunar birthday. */
private fun nextLunarBirthday(cal: ChineseCalendar, birth: LunarInfo, from: LocalDate, zone: ZoneId): LocalDate? {
    var d = from
    repeat(400) {
        val l = lunarOf(cal, d, zone)
        if (l != null && !l.leap && l.month == birth.month && l.day == birth.day) return d
        d = d.plusDays(1)
    }
    return null
}

private data class AgeResult(val main: String, val lines: List<String>)

private fun buildAge(birth: LocalDate, today: LocalDate, zone: ZoneId): AgeResult {
    if (birth.isAfter(today)) {
        return AgeResult(tr("出生日期晚于今天", "Birth date is after today"), emptyList())
    }
    val p = Period.between(birth, today)
    val totalDays = ChronoUnit.DAYS.between(birth, today)
    val totalMonths = ChronoUnit.MONTHS.between(birth, today)
    val totalWeeks = totalDays / 7

    var next = MonthDay.from(birth).atYear(today.year)
    if (next.isBefore(today)) next = MonthDay.from(birth).atYear(today.year + 1)
    val toNext = ChronoUnit.DAYS.between(today, next)
    val turning = next.year - birth.year

    val sign = westernSign(birth.monthValue, birth.dayOfMonth)
    val signName = (if (isZh()) sign.zh else sign.en) + " (${sign.range})"

    val cal = ChineseCalendar()
    val lunar = lunarOf(cal, birth, zone)
    val lines = mutableListOf<String>()

    lines += tr("出生日期：", "Born: ") + "$birth · ${weekdayName(birth)}"
    lines += tr("总天数：", "Total days: ") + fmtInt(totalDays)
    lines += tr("总月数：", "Total months: ") + fmtInt(totalMonths) + tr(" · 总周数：", " · weeks: ") + fmtInt(totalWeeks)

    lines += if (toNext == 0L) {
        tr("今天是你的生日！满 $turning 岁", "Today is your birthday! Turning $turning")
    } else {
        tr(
            "下次生日：$next（${weekdayName(next)}）· 还有 $toNext 天 · 将满 $turning 岁",
            "Next birthday: $next (${weekdayName(next)}) · in $toNext days · turning $turning",
        )
    }

    lines += tr("星座：", "Zodiac sign: ") + signName

    if (lunar != null) {
        val animal = animalName(lunar.cycleYear - 1)
        lines += tr("生肖：", "Chinese zodiac: ") + animal + " · " + ganzhiOf(lunar.cycleYear - 1) + tr("年", " year")
        lines += tr("农历生日：", "Lunar birthday: ") + lunarText(lunar)
        val todayLunar = lunarOf(cal, today, zone)
        if (todayLunar != null) {
            val base = Math.floorMod(todayLunar.cycleYear - lunar.cycleYear, 60) + 1
            val k = ((p.years + 1 - base) / 60.0).roundToInt()
            lines += tr("虚岁：", "Nominal age (虚岁): ") + (base + 60 * k)
        }
        val nl = nextLunarBirthday(cal, lunar, today, zone)
        lines += tr("下次农历生日：", "Next lunar birthday: ") + if (nl == null) "—" else {
            val n = ChronoUnit.DAYS.between(today, nl)
            tr("$nl（${weekdayName(nl)}）· 还有 $n 天", "$nl (${weekdayName(nl)}) · in $n days")
        }
    } else {
        // Fallback: Gregorian-year based zodiac (approximate, ignores Lunar New Year boundary).
        val idx = birth.year - 4
        lines += tr("生肖（按公历年份）：", "Chinese zodiac (by calendar year): ") +
            animalName(idx) + " · " + ganzhiOf(idx) + tr("年", " year")
    }

    val main = if (isZh()) {
        "${p.years} 岁 ${p.months} 个月 ${p.days} 天"
    } else {
        "${p.years} y ${p.months} m ${p.days} d"
    }
    return AgeResult(main, lines)
}

@Composable fun AgeTool() {
    var birth by remember { mutableStateOf("1998-06-15") }
    val zone = remember { ZoneId.systemDefault() }
    val today = remember { LocalDate.now() }

    DateFieldWithPicker(birth, { birth = it }, label = tr("出生日期 (yyyy-MM-dd)", "Birth date (yyyy-MM-dd)"))
    val result = remember(birth) {
        parseDate(birth)?.let { buildAge(it, today, zone) }
    }
    if (result == null) {
        ResultCard(main = tr("请输入有效日期", "Enter a valid date"))
    } else {
        ResultCard(main = result.main, lines = result.lines)
    }
}

// ───────────────────────────── 6. Timezone ─────────────────────────────

private data class CityZone(val id: String, val zh: String, val en: String) {
    fun title(): String = if (isZh()) zh else en
}

private val CITY_ZONES = listOf(
    CityZone("UTC", "协调世界时", "UTC"),
    CityZone("Asia/Shanghai", "北京 / 上海", "Beijing / Shanghai"),
    CityZone("Asia/Hong_Kong", "香港", "Hong Kong"),
    CityZone("Asia/Tokyo", "东京", "Tokyo"),
    CityZone("Asia/Seoul", "首尔", "Seoul"),
    CityZone("Asia/Singapore", "新加坡", "Singapore"),
    CityZone("Asia/Kolkata", "印度（加尔各答）", "India (Kolkata)"),
    CityZone("Asia/Dubai", "迪拜", "Dubai"),
    CityZone("Europe/Moscow", "莫斯科", "Moscow"),
    CityZone("Europe/London", "伦敦", "London"),
    CityZone("Europe/Paris", "巴黎", "Paris"),
    CityZone("Europe/Berlin", "柏林", "Berlin"),
    CityZone("America/New_York", "纽约", "New York"),
    CityZone("America/Chicago", "芝加哥", "Chicago"),
    CityZone("America/Denver", "丹佛", "Denver"),
    CityZone("America/Los_Angeles", "洛杉矶", "Los Angeles"),
    CityZone("America/Sao_Paulo", "圣保罗", "São Paulo"),
    CityZone("Australia/Sydney", "悉尼", "Sydney"),
    CityZone("Pacific/Auckland", "奥克兰", "Auckland"),
)

private val WORLD_CLOCK_IDS = listOf(
    "UTC", "Asia/Shanghai", "Asia/Tokyo", "Europe/London",
    "Europe/Paris", "America/New_York", "America/Los_Angeles", "Australia/Sydney",
)

@Composable
private fun ZonePicker(
    label: String,
    zones: List<CityZone>,
    selectedId: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val now = Instant.now()
    fun describe(z: CityZone): String =
        "${z.title()} · ${offsetLabel(ZoneId.of(z.id).rules.getOffset(now))}"
    val selected = zones.firstOrNull { it.id == selectedId }
    Column {
        SectionLabel(label)
        Box(Modifier.fillMaxWidth()) {
            GhostButton(
                text = (selected?.let { describe(it) } ?: selectedId) + "  ▾",
                onClick = { expanded = true },
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                zones.forEach { z ->
                    DropdownMenuItem(
                        text = { Text(describe(z)) },
                        onClick = {
                            onSelect(z.id)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable fun TimezoneTool() {
    val localId = remember { ZoneId.systemDefault().id }
    val zones = remember(localId) {
        if (CITY_ZONES.any { it.id == localId }) CITY_ZONES
        else listOf(CityZone(localId, "本地（$localId）", "Local ($localId)")) + CITY_ZONES
    }
    var fromId by remember { mutableStateOf(localId) }
    var toId by remember { mutableStateOf(if (localId == "America/New_York") "Asia/Shanghai" else "America/New_York") }
    var timeText by remember {
        mutableStateOf(ZonedDateTime.now(ZoneId.of(localId)).toLocalDateTime().withNano(0).format(OUT_FMT))
    }
    var tick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            tick = System.currentTimeMillis()
        }
    }

    ZonePicker(tr("源时区", "Source timezone"), zones, fromId) { fromId = it }
    ZonePicker(tr("目标时区", "Target timezone"), zones, toId) { toId = it }
    DateTimeFieldWithPicker(
        value = timeText,
        onValueChange = { timeText = it },
        label = tr("源时区的本地时间 (yyyy-MM-dd HH:mm:ss)", "Local time in source zone (yyyy-MM-dd HH:mm:ss)"),
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        GhostButton(tr("现在", "Now"), onClick = {
            timeText = ZonedDateTime.now(ZoneId.of(fromId)).toLocalDateTime().withNano(0).format(OUT_FMT)
        }, modifier = Modifier.weight(1f))
        GhostButton(tr("交换", "Swap"), onClick = {
            val a = fromId
            fromId = toId
            toId = a
            // Keep the same instant: re-express the typed time in the new source zone.
            parseDateTime(timeText)?.let { ldt ->
                runCatching {
                    timeText = ldt.atZone(ZoneId.of(a)).withZoneSameInstant(ZoneId.of(fromId))
                        .toLocalDateTime().format(OUT_FMT)
                }
            }
        }, modifier = Modifier.weight(1f))
    }

    val ldt = parseDateTime(timeText)
    if (ldt == null) {
        ResultCard(main = tr("请输入有效的日期时间", "Enter a valid date-time"))
    } else {
        val fromZone = ZoneId.of(fromId)
        val toZone = ZoneId.of(toId)
        val src = ZonedDateTime.of(ldt, fromZone)
        val dst = src.withZoneSameInstant(toZone)
        val dayDiff = ChronoUnit.DAYS.between(src.toLocalDate(), dst.toLocalDate())
        val dayNote = when {
            dayDiff > 0 -> tr("（次日 +$dayDiff）", " (+$dayDiff day)")
            dayDiff < 0 -> tr("（前一日 $dayDiff）", " ($dayDiff day)")
            else -> ""
        }
        val hoursDiff = (dst.offset.totalSeconds - src.offset.totalSeconds) / 3600.0
        val fromTitle = zones.firstOrNull { it.id == fromId }?.title() ?: fromId
        val toTitle = zones.firstOrNull { it.id == toId }?.title() ?: toId
        ResultCard(
            main = dst.toLocalDateTime().format(OUT_FMT) + " " + offsetLabel(dst.offset) + dayNote,
            lines = listOf(
                "$toTitle · ${toZone.id} · ${weekdayName(dst.dayOfWeek)}",
                "$fromTitle: ${src.toLocalDateTime().format(OUT_FMT)} ${offsetLabel(src.offset)}",
                tr("时差：", "Offset difference: ") + (if (hoursDiff >= 0) "+" else "") + fmt1(hoursDiff) + tr(" 小时", " h"),
                "ISO-8601: ${src.toInstant()}",
            ),
        )
    }

    val nowInstant = Instant.ofEpochMilli(tick)
    val clockLines = WORLD_CLOCK_IDS.map { id ->
        val z = ZoneId.of(id)
        val zdt = nowInstant.atZone(z)
        val name = CITY_ZONES.firstOrNull { it.id == id }?.title() ?: id
        "$name  ${zdt.format(DateTimeFormatter.ofPattern("MM-dd HH:mm:ss"))}  ${offsetLabel(zdt.offset)}"
    }
    SectionLabel(tr("世界时钟", "World clocks"))
    ResultCard(main = tr("此刻", "Right now"), lines = clockLines)
}

// ───────────────────────────── 7. Countdown ─────────────────────────────

@Composable fun CountdownTool() {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf(LocalDate.now().plusMonths(1).toString()) }

    CalcTextField(title, { title = it }, label = tr("标题（可选）", "Title (optional)"))
    DateFieldWithPicker(target, { target = it }, label = tr("目标日期 (yyyy-MM-dd)", "Target date (yyyy-MM-dd)"))

    val t = parseDate(target)
    if (t == null) {
        ResultCard(main = tr("请输入有效日期", "Enter a valid date"))
    } else {
        val today = LocalDate.now()
        val days = ChronoUnit.DAYS.between(today, t)
        val n = abs(days)
        val name = title.trim()
        val main = when {
            days > 0 -> if (name.isEmpty()) tr("还有 $n 天", "$n days remaining")
            else tr("距离「$name」还有 $n 天", "$n days until \"$name\"")
            days < 0 -> if (name.isEmpty()) tr("已过去 $n 天", "$n days ago")
            else tr("「$name」已过去 $n 天", "\"$name\" was $n days ago")
            else -> if (name.isEmpty()) tr("就是今天！", "Today!") else tr("「$name」就是今天！", "\"$name\" is today!")
        }
        val p = if (days >= 0) Period.between(today, t) else Period.between(t, today)
        ResultCard(
            main = main,
            lines = listOf(
                (if (name.isEmpty()) "" else "$name · ") + "$t · ${weekdayName(t)}",
                tr("约 ${fmt1(n / 7.0)} 周 · 约 ${fmt1(n / 30.44)} 个月", "≈ ${fmt1(n / 7.0)} weeks · ≈ ${fmt1(n / 30.44)} months"),
                tr(
                    "${p.years} 年 ${p.months} 个月 ${p.days} 天",
                    "${p.years}y ${p.months}m ${p.days}d",
                ),
            ),
        )
    }
}

// ───────────────────────────── 8. Stopwatch ─────────────────────────────

private data class SwLap(val num: Int, val splitMs: Long, val totalMs: Long, val deltaMs: Long?)

private fun formatSw(ms: Long): String {
    val m = max(0L, ms)
    val h = m / 3_600_000
    val mi = (m % 3_600_000) / 60_000
    val s = (m % 60_000) / 1000
    val milli = m % 1000
    return String.format(Locale.ROOT, "%02d:%02d:%02d.%03d", h, mi, s, milli)
}

private fun formatSwDelta(ms: Long): String = (if (ms >= 0) "+" else "-") + formatSw(abs(ms))

@Composable fun StopwatchTool() {
    var running by remember { mutableStateOf(false) }
    var startMark by remember { mutableLongStateOf(0L) }
    var elapsedBase by remember { mutableLongStateOf(0L) }
    var displayMs by remember { mutableLongStateOf(0L) }
    var lastLapTotal by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<SwLap>() }

    fun nowElapsed(): Long =
        if (running) elapsedBase + (System.nanoTime() / 1_000_000 - startMark) else elapsedBase

    LaunchedEffect(running) {
        while (running) {
            displayMs = nowElapsed()
            kotlinx.coroutines.delay(16)
        }
    }

    ResultCard(main = formatSw(displayMs))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PrimaryButton(
            text = when {
                running -> tr("暂停", "Pause")
                elapsedBase > 0 -> tr("继续", "Resume")
                else -> tr("开始", "Start")
            },
            onClick = {
                if (!running) {
                    startMark = System.nanoTime() / 1_000_000
                    running = true
                } else {
                    val e = nowElapsed()
                    running = false
                    elapsedBase = e
                    displayMs = e
                }
            },
            modifier = Modifier.weight(1f),
        )
        GhostButton(tr("计次", "Lap"), onClick = {
            val total = nowElapsed()
            if (total > 0) {
                val split = total - lastLapTotal
                val delta = laps.firstOrNull()?.let { split - it.splitMs }
                laps.add(0, SwLap(laps.size + 1, split, total, delta))
                lastLapTotal = total
                displayMs = total
            }
        }, modifier = Modifier.weight(1f))
        GhostButton(tr("重置", "Reset"), onClick = {
            running = false
            elapsedBase = 0L
            displayMs = 0L
            lastLapTotal = 0L
            laps.clear()
        }, modifier = Modifier.weight(1f))
    }

    if (laps.size >= 2) {
        val best = laps.minBy { it.splitMs }
        val worst = laps.maxBy { it.splitMs }
        ResultCard(
            main = tr("最佳 / 最差分段", "Best / worst lap"),
            lines = listOf(
                tr("最佳：第 ${best.num} 圈 ", "Best: lap ${best.num} ") + formatSw(best.splitMs),
                tr("最差：第 ${worst.num} 圈 ", "Worst: lap ${worst.num} ") + formatSw(worst.splitMs),
            ),
        )
    }
    if (laps.isNotEmpty()) {
        val best = laps.minOf { it.splitMs }
        val worst = laps.maxOf { it.splitMs }
        val mark = laps.size >= 2 && best != worst
        SectionLabel(tr("分段记录", "Laps"))
        ResultCard(
            main = tr("共 ${laps.size} 圈", "${laps.size} laps"),
            lines = laps.map { lap ->
                val tag = when {
                    !mark -> ""
                    lap.splitMs == best -> tr(" ★最佳", " ★best")
                    lap.splitMs == worst -> tr(" ▼最差", " ▼worst")
                    else -> ""
                }
                val delta = lap.deltaMs?.let { "  Δ ${formatSwDelta(it)}" } ?: ""
                "#${lap.num}  ${formatSw(lap.splitMs)}$delta  (${formatSw(lap.totalMs)})$tag"
            },
        )
    }
}
