package com.qiyuan.calcdesk.ui.tools

import android.icu.util.Calendar as IcuCalendar
import android.icu.util.ChineseCalendar
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private data class WesternSign(
    val id: String,
    val zh: String,
    val en: String,
    val from: Int,
    val to: Int,
    val range: String,
) {
    fun label(): String = if (isZh()) zh else en
}

private val WESTERN_SIGNS = listOf(
    WesternSign("capricorn", "摩羯座", "Capricorn", 1222, 119, "12.22 - 01.19"),
    WesternSign("aquarius", "水瓶座", "Aquarius", 120, 218, "01.20 - 02.18"),
    WesternSign("pisces", "双鱼座", "Pisces", 219, 320, "02.19 - 03.20"),
    WesternSign("aries", "白羊座", "Aries", 321, 419, "03.21 - 04.19"),
    WesternSign("taurus", "金牛座", "Taurus", 420, 520, "04.20 - 05.20"),
    WesternSign("gemini", "双子座", "Gemini", 521, 621, "05.21 - 06.21"),
    WesternSign("cancer", "巨蟹座", "Cancer", 622, 722, "06.22 - 07.22"),
    WesternSign("leo", "狮子座", "Leo", 723, 822, "07.23 - 08.22"),
    WesternSign("virgo", "处女座", "Virgo", 823, 922, "08.23 - 09.22"),
    WesternSign("libra", "天秤座", "Libra", 923, 1023, "09.23 - 10.23"),
    WesternSign("scorpio", "天蝎座", "Scorpio", 1024, 1122, "10.24 - 11.22"),
    WesternSign("sagittarius", "射手座", "Sagittarius", 1123, 1221, "11.23 - 12.21"),
)

private fun westernSign(month: Int, day: Int): WesternSign {
    val md = month * 100 + day
    return WESTERN_SIGNS.firstOrNull { z ->
        if (z.from > z.to) md >= z.from || md <= z.to else md in z.from..z.to
    } ?: WESTERN_SIGNS[0]
}

private fun westernSignDrawable(id: String): Int = when (id) {
    "aquarius" -> R.drawable.aquarius
    "aries" -> R.drawable.aries
    "cancer" -> R.drawable.cancer
    "capricorn" -> R.drawable.capricorn
    "gemini" -> R.drawable.gemini
    "leo" -> R.drawable.leo
    "libra" -> R.drawable.libra
    "pisces" -> R.drawable.pisces
    "sagittarius" -> R.drawable.sagittarius
    "scorpio" -> R.drawable.scorpio
    "taurus" -> R.drawable.taurus
    "virgo" -> R.drawable.virgo
    else -> R.drawable.capricorn
}

private const val STEMS = "甲乙丙丁戊己庚辛壬癸"
private const val BRANCHES = "子丑寅卯辰巳午未申酉戌亥"
private val ANIMALS_ZH = listOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")
private val ANIMALS_EN = listOf("Rat", "Ox", "Tiger", "Rabbit", "Dragon", "Snake", "Horse", "Goat", "Monkey", "Rooster", "Dog", "Pig")
/** 1900 = 鼠；与网页 Lunar.zodiacFromLunarYear 一致。 */
private val SHENGXIAO_DRAWABLES = intArrayOf(
    R.drawable.rat, R.drawable.ox, R.drawable.tiger, R.drawable.rabbit,
    R.drawable.dragon, R.drawable.snake, R.drawable.horse, R.drawable.goat,
    R.drawable.monkey, R.drawable.rooster, R.drawable.dog, R.drawable.pig,
)
/** 对照表示例公历年（鼠…猪），与网页一致。 */
private val SHENGXIAO_SAMPLE_BASE = listOf(1996, 1997, 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007)

/** index0 = 0 for 甲子 (rat) … 59 for 癸亥. */
private fun ganzhiOf(index0: Int): String {
    val i = Math.floorMod(index0, 60)
    return "" + STEMS[i % 10] + BRANCHES[i % 12]
}

private fun animalIndex(index0: Int): Int = Math.floorMod(index0, 12)

private fun animalName(index0: Int): String {
    val i = animalIndex(index0)
    return if (isZh()) ANIMALS_ZH[i] else ANIMALS_EN[i]
}

/** 按农历/公历年份查生肖（1900=鼠），与网页年查询一致。 */
private fun animalIndexFromYear(year: Int): Int = Math.floorMod(year - 1900, 12)

private fun ganzhiFromYear(year: Int): String = ganzhiOf(year - 4)

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

private data class AgeResult(
    val main: String,
    val lines: List<String>,
    val sign: WesternSign? = null,
    val animalIdx: Int? = null,
    val animalGanzhi: String? = null,
)

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
    val signName = sign.label() + " (${sign.range})"

    val cal = ChineseCalendar()
    val lunar = lunarOf(cal, birth, zone)
    val lines = mutableListOf<String>()
    var animalIdx: Int? = null
    var animalGanzhi: String? = null

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
        animalIdx = animalIndex(lunar.cycleYear - 1)
        animalGanzhi = ganzhiOf(lunar.cycleYear - 1)
        lines += tr("生肖：", "Chinese zodiac: ") +
            animalName(lunar.cycleYear - 1) + " · " + animalGanzhi + tr("年", " year")
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
        animalIdx = animalIndex(idx)
        animalGanzhi = ganzhiOf(idx)
        lines += tr("生肖（按公历年份）：", "Chinese zodiac (by calendar year): ") +
            animalName(idx) + " · " + animalGanzhi + tr("年", " year")
    }

    val main = if (isZh()) {
        "${p.years} 岁 ${p.months} 个月 ${p.days} 天"
    } else {
        "${p.years} y ${p.months} m ${p.days} d"
    }
    return AgeResult(main, lines, sign = sign, animalIdx = animalIdx, animalGanzhi = animalGanzhi)
}

@Composable
private fun AgeIconBadge(drawableRes: Int, label: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(drawableRes),
                contentDescription = label,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AgeRefRow(drawableRes: Int, name: String, detail: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = name,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
        Text(
            text = name,
            modifier = Modifier.weight(0.35f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = detail,
            modifier = Modifier.weight(0.65f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable fun AgeTool() {
    var birth by remember { mutableStateOf("1998-06-15") }
    val zone = remember { ZoneId.systemDefault() }
    val today = remember { LocalDate.now() }

    var queryYear by remember { mutableStateOf(today.year.toString()) }
    var queryMonth by remember {
        mutableStateOf(parseDate(birth)?.monthValue?.toString() ?: "6")
    }
    var queryDay by remember {
        mutableStateOf(parseDate(birth)?.dayOfMonth?.toString() ?: "15")
    }

    DateFieldWithPicker(birth, { birth = it }, label = tr("出生日期 (yyyy-MM-dd)", "Birth date (yyyy-MM-dd)"))
    val result = remember(birth) {
        parseDate(birth)?.let { buildAge(it, today, zone) }
    }
    if (result == null) {
        ResultCard(main = tr("请输入有效日期", "Enter a valid date"))
    } else {
        ResultCard(main = result.main, lines = result.lines)

        if (result.animalIdx != null || result.sign != null) {
            SectionLabel(tr("生肖与星座", "Chinese zodiac & Western sign"))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                result.animalIdx?.let { idx ->
                    AgeIconBadge(
                        drawableRes = SHENGXIAO_DRAWABLES[idx],
                        label = (if (isZh()) ANIMALS_ZH[idx] else ANIMALS_EN[idx]) +
                            (result.animalGanzhi?.let { " · $it" } ?: ""),
                        subtitle = tr("出生年生肖（按农历年）", "Birth-year animal (lunar year)"),
                    )
                }
                result.sign?.let { sign ->
                    AgeIconBadge(
                        drawableRes = westernSignDrawable(sign.id),
                        label = sign.label(),
                        subtitle = sign.range,
                    )
                }
            }
        }
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    SectionLabel(tr("查生肖（任意年份）", "Look up Chinese zodiac by year"))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NumberField(
            value = queryYear,
            onValueChange = { queryYear = it.filter { c -> c.isDigit() }.take(4) },
            label = tr("年份", "Year"),
            modifier = Modifier.weight(1f),
        )
    }
    val yearNum = queryYear.toIntOrNull()
    if (yearNum != null && yearNum in 1..9999) {
        val idx = animalIndexFromYear(yearNum)
        val gz = ganzhiFromYear(yearNum)
        AgeIconBadge(
            drawableRes = SHENGXIAO_DRAWABLES[idx],
            label = if (isZh()) ANIMALS_ZH[idx] else ANIMALS_EN[idx],
            subtitle = tr(
                "$yearNum 年 · $gz 年",
                "$yearNum · $gz year",
            ),
        )
    } else {
        Text(
            text = tr("请输入有效年份，例如 1998", "Enter a valid year, e.g. 1998"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    SectionLabel(tr("查星座（月日）", "Look up Western zodiac by month/day"))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        NumberField(
            value = queryMonth,
            onValueChange = { queryMonth = it.filter { c -> c.isDigit() }.take(2) },
            label = tr("月", "Month"),
            modifier = Modifier.weight(1f),
        )
        NumberField(
            value = queryDay,
            onValueChange = { queryDay = it.filter { c -> c.isDigit() }.take(2) },
            label = tr("日", "Day"),
            modifier = Modifier.weight(1f),
        )
    }
    val m = queryMonth.toIntOrNull()
    val d = queryDay.toIntOrNull()
    if (m != null && d != null && m in 1..12 && d in 1..31) {
        val sign = westernSign(m, d)
        AgeIconBadge(
            drawableRes = westernSignDrawable(sign.id),
            label = sign.label(),
            subtitle = sign.range,
        )
    } else {
        Text(
            text = tr("请输入有效月日", "Enter a valid month and day"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    SectionLabel(tr("星座对照表", "Western zodiac table"))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            WESTERN_SIGNS.forEach { sign ->
                AgeRefRow(
                    drawableRes = westernSignDrawable(sign.id),
                    name = sign.label(),
                    detail = sign.range,
                )
            }
        }
    }

    SectionLabel(tr("生肖对照表（示例年份）", "Chinese zodiac table (sample years)"))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            SHENGXIAO_SAMPLE_BASE.forEachIndexed { i, baseYear ->
                val samples = (0 until 5).joinToString(", ") { k -> (baseYear + k * 12).toString() }
                AgeRefRow(
                    drawableRes = SHENGXIAO_DRAWABLES[i],
                    name = if (isZh()) ANIMALS_ZH[i] else ANIMALS_EN[i],
                    detail = samples,
                )
            }
        }
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
        val time = zdt.format(DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")) + " " + offsetLabel(zdt.offset)
        // 城市名是提示标签，时间才是结果 → 用标签冒号拆分着色
        if (isZh()) "$name：$time" else "$name: $time"
    }
    SectionLabel(tr("世界时钟", "World clocks"))
    ResultCard(
        main = tr("此刻", "Right now"),
        lines = clockLines,
        mainIsResult = false,
    )
}

// ───────────────────────────── 7. Countdown → CountdownScreens.kt ─────────────────────────────

// ───────────────────────────── 8. Stopwatch ─────────────────────────────

private data class SwLap(val num: Int, val splitMs: Long, val totalMs: Long, val deltaMs: Long?)

private enum class SwLapSort { ByNumber, FastToSlow, SlowToFast }

private val SwSegH = Color(0xFF0F766E)
private val SwSegM = Color(0xFF0369A1)
private val SwSegS = Color(0xFFB45309)
private val SwSegMs = Color(0xFF64748B)

private fun formatSw(ms: Long): String {
    val m = max(0L, ms)
    val h = m / 3_600_000
    val mi = (m % 3_600_000) / 60_000
    val s = (m % 60_000) / 1000
    val milli = m % 1000
    return String.format(Locale.ROOT, "%02d:%02d:%02d.%03d", h, mi, s, milli)
}

private fun formatSwDeltaPlain(ms: Long): String =
    (if (ms >= 0) "+" else "−") + formatSw(abs(ms))

private fun swSegmentedAnnotated(ms: Long): AnnotatedString {
    val m = max(0L, ms)
    val h = String.format(Locale.ROOT, "%02d", m / 3_600_000)
    val mi = String.format(Locale.ROOT, "%02d", (m % 3_600_000) / 60_000)
    val s = String.format(Locale.ROOT, "%02d", (m % 60_000) / 1000)
    val milli = String.format(Locale.ROOT, "%03d", m % 1000)
    return buildAnnotatedString {
        withStyle(SpanStyle(color = SwSegH, fontWeight = FontWeight.Bold)) { append(h) }
        withStyle(SpanStyle(color = SwSegMs, fontWeight = FontWeight.Bold)) { append(":") }
        withStyle(SpanStyle(color = SwSegM, fontWeight = FontWeight.Bold)) { append(mi) }
        withStyle(SpanStyle(color = SwSegMs, fontWeight = FontWeight.Bold)) { append(":") }
        withStyle(SpanStyle(color = SwSegS, fontWeight = FontWeight.Bold)) { append(s) }
        withStyle(SpanStyle(color = SwSegMs, fontWeight = FontWeight.Bold)) { append(".") }
        withStyle(SpanStyle(color = SwSegMs, fontWeight = FontWeight.Bold)) { append(milli) }
    }
}

@Composable
private fun SwMainDisplay(ms: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = swSegmentedAnnotated(ms),
                fontSize = 36.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SwLapRow(lap: SwLap, showDivider: Boolean) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = lap.num.toString(),
                modifier = Modifier.widthIn(min = 28.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
            Text(
                text = swSegmentedAnnotated(lap.splitMs),
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = lap.deltaMs?.let { formatSwDeltaPlain(it) } ?: "—",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1A1A1A),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable fun StopwatchTool() {
    var running by remember { mutableStateOf(false) }
    var startMark by remember { mutableLongStateOf(0L) }
    var elapsedBase by remember { mutableLongStateOf(0L) }
    var displayMs by remember { mutableLongStateOf(0L) }
    var lastLapTotal by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<SwLap>() }
    var lapSort by remember { mutableStateOf(SwLapSort.ByNumber) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    fun nowElapsed(): Long =
        if (running) elapsedBase + (System.nanoTime() / 1_000_000 - startMark) else elapsedBase

    LaunchedEffect(running) {
        while (running) {
            displayMs = nowElapsed()
            kotlinx.coroutines.delay(16)
        }
    }

    SwMainDisplay(displayMs)
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
                val delta = laps.lastOrNull()?.let { split - it.splitMs }
                laps.add(SwLap(laps.size + 1, split, total, delta))
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
            lapSort = SwLapSort.ByNumber
        }, modifier = Modifier.weight(1f))
    }

    if (laps.size >= 2) {
        val best = laps.minBy { it.splitMs }
        val worst = laps.maxBy { it.splitMs }
        ResultCard(
            main = tr("最佳 / 最差分段", "Best / worst lap"),
            mainIsResult = false,
            lines = listOf(
                tr("最佳：第 ${best.num} 圈 ", "Best: lap ${best.num} ") + formatSw(best.splitMs),
                tr("最差：第 ${worst.num} 圈 ", "Worst: lap ${worst.num} ") + formatSw(worst.splitMs),
            ),
        )
    }
    if (laps.isNotEmpty()) {
        val displayed = remember(laps.toList(), lapSort) {
            when (lapSort) {
                SwLapSort.ByNumber -> laps.sortedBy { it.num }
                SwLapSort.FastToSlow -> laps.sortedBy { it.splitMs }
                SwLapSort.SlowToFast -> laps.sortedByDescending { it.splitMs }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = tr("分段记录", "Laps"),
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
                        contentDescription = tr("排序", "Sort"),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DropdownMenu(
                    expanded = sortMenuOpen,
                    onDismissRequest = { sortMenuOpen = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(tr("正序（按圈号）", "Ascending (by lap)")) },
                        onClick = {
                            lapSort = SwLapSort.ByNumber
                            sortMenuOpen = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(tr("由快到慢", "Fast → slow")) },
                        onClick = {
                            lapSort = SwLapSort.FastToSlow
                            sortMenuOpen = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(tr("由慢到快", "Slow → fast")) },
                        onClick = {
                            lapSort = SwLapSort.SlowToFast
                            sortMenuOpen = false
                        },
                    )
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(Modifier.fillMaxWidth()) {
                displayed.forEachIndexed { index, lap ->
                    SwLapRow(lap = lap, showDivider = index < displayed.lastIndex)
                }
            }
        }
    }
}
