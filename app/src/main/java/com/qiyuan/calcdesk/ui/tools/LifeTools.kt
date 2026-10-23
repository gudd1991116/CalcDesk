package com.qiyuan.calcdesk.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.qiyuan.calcdesk.R
import com.qiyuan.calcdesk.ui.components.CalcTextField
import com.qiyuan.calcdesk.ui.components.DateFieldWithPicker
import com.qiyuan.calcdesk.ui.components.GhostButton
import com.qiyuan.calcdesk.ui.components.NumberField
import com.qiyuan.calcdesk.ui.components.PrimaryButton
import com.qiyuan.calcdesk.ui.components.ResultCard
import com.qiyuan.calcdesk.ui.components.SectionLabel
import java.security.SecureRandom
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

@Composable
fun LifeToolRouter(toolId: String) {
    when (toolId) {
        "bmi" -> BmiTool()
        "fuel" -> FuelTool()
        "download-time" -> DownloadTimeTool()
        "tip" -> TipTool()
        "discount" -> DiscountTool()
        "due-date" -> DueDateTool()
        "mortgage" -> MortgageTool()
        "password" -> PasswordTool()
        "wordcount" -> WordCountTool()
        else -> Text("Unknown life tool")
    }
}

// ---------------------------------------------------------------------------
// Shared helpers
// ---------------------------------------------------------------------------

@Composable
private fun rememberLifeZh(): Boolean =
    remember { Locale.getDefault().language.startsWith("zh") }

private fun lifeFmt(v: Double, digits: Int = 2): String =
    String.format(Locale.US, "%.${digits}f", v)

/** Single-select chip row. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LifeChoiceChips(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { opt ->
            FilterChip(
                selected = opt == selected,
                onClick = { onSelect(opt) },
                label = { Text(opt) },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// BMI
// ---------------------------------------------------------------------------

@Composable
fun BmiTool() {
    val zh = rememberLifeZh()
    var heightCm by remember { mutableStateOf("170") }
    var weightKg by remember { mutableStateOf("65") }
    val hm = heightCm.toDoubleOrNull()?.div(100)
    val w = weightKg.toDoubleOrNull()
    val bmi = if (hm != null && w != null && hm > 0 && w > 0) w / (hm * hm) else null

    NumberField(heightCm, { heightCm = it }, label = if (zh) "身高 (cm)" else "Height (cm)")
    NumberField(weightKg, { weightKg = it }, label = if (zh) "体重 (kg)" else "Weight (kg)")

    bmi?.let {
        val cat = when {
            it < 18.5 -> if (zh) "偏瘦" else "Underweight"
            it < 24 -> if (zh) "正常" else "Normal"
            it < 28 -> if (zh) "超重" else "Overweight"
            else -> if (zh) "肥胖" else "Obese"
        }
        val lines = mutableListOf(
            if (zh) "中国成人参考：<18.5 偏瘦 · 18.5–24 正常 · 24–28 超重 · ≥28 肥胖"
            else "China adult reference: <18.5 underweight · 18.5–24 normal · 24–28 overweight · ≥28 obese",
        )
        if (hm != null) {
            val lo = 18.5 * hm * hm
            val hi = 24.0 * hm * hm
            lines += if (zh) "正常体重范围约 ${lifeFmt(lo, 1)}–${lifeFmt(hi, 1)} kg"
            else "Normal weight range ≈ ${lifeFmt(lo, 1)}–${lifeFmt(hi, 1)} kg"
        }
        lines += if (zh) "仅适用于成年人，不适用于运动员、孕妇等特殊人群。"
        else "For adults only; not suitable for athletes, pregnancy, etc."
        ResultCard(main = "${lifeFmt(it, 2)} · $cat", lines = lines)
    }
}

// ---------------------------------------------------------------------------
// Fuel
// ---------------------------------------------------------------------------

@Composable
fun FuelTool() {
    val zh = rememberLifeZh()
    var km by remember { mutableStateOf("480") }
    var liters by remember { mutableStateOf("40") }
    var price by remember { mutableStateOf("7.8") }
    var plan by remember { mutableStateOf("100") }

    val k = km.toDoubleOrNull()
    val l = liters.toDoubleOrNull()
    val p = price.toDoubleOrNull()
    val planKm = plan.toDoubleOrNull()

    NumberField(km, { km = it }, label = if (zh) "行驶里程 (km)" else "Distance driven (km)")
    NumberField(liters, { liters = it }, label = if (zh) "加油量 (L)" else "Fuel used (L)")
    NumberField(price, { price = it }, label = if (zh) "油价 (每升)" else "Price per L")
    NumberField(plan, { plan = it }, label = if (zh) "计划行程 (km，可选)" else "Planned trip (km, optional)")

    if (k != null && l != null && k > 0 && l > 0) {
        val per100 = l / k * 100
        val lines = mutableListOf<String>()
        lines += if (zh) "每公里油耗：${lifeFmt(l / k, 3)} L" else "Per km: ${lifeFmt(l / k, 3)} L"
        lines += if (zh) "约 ${lifeFmt(k / l, 2)} km/L" else "≈ ${lifeFmt(k / l, 2)} km/L"
        if (p != null && p > 0) {
            lines += if (zh) "每百公里油费：${lifeFmt(per100 * p, 2)}" else "Cost per 100 km: ${lifeFmt(per100 * p, 2)}"
            lines += if (zh) "本次行程油费：${lifeFmt(l * p, 2)}" else "This trip's fuel cost: ${lifeFmt(l * p, 2)}"
        }
        if (planKm != null && planKm > 0) {
            val planL = per100 / 100 * planKm
            lines += if (zh) "计划 ${lifeFmt(planKm, 0)} km 需油 ${lifeFmt(planL, 2)} L"
            else "Planned ${lifeFmt(planKm, 0)} km needs ${lifeFmt(planL, 2)} L"
            if (p != null && p > 0) {
                lines += if (zh) "计划行程油费：${lifeFmt(planL * p, 2)}" else "Planned trip fuel cost: ${lifeFmt(planL * p, 2)}"
            }
        }
        ResultCard(main = "${lifeFmt(per100, 2)} L/100km", lines = lines)
    }
}

// ---------------------------------------------------------------------------
// Download time
// ---------------------------------------------------------------------------

private data class DtPreset(val zh: String, val en: String, val size: String, val unit: String)

private val dtPresets = listOf(
    DtPreset("邮件", "Email", "30", "KB"),
    DtPreset("网页", "Web page", "600", "KB"),
    DtPreset("照片", "Photo", "1", "MB"),
    DtPreset("MP3", "MP3", "5", "MB"),
    DtPreset("短视频", "Short video", "40", "MB"),
    DtPreset("标清电影", "SD movie", "750", "MB"),
    DtPreset("高清电影", "HD movie", "4", "GB"),
)

private fun dtSpeedBits(v: Double, unit: String): Double = when (unit) {
    "Kbps" -> v * 1e3
    "Gbps" -> v * 1e9
    else -> v * 1e6
}

private fun dtSizeBytes(v: Double, unit: String): Double {
    val k = 1024.0
    return when (unit) {
        "KB" -> v * k
        "MB" -> v * k * k
        "TB" -> v * k * k * k * k
        else -> v * k * k * k
    }
}

private fun dtFormatDuration(sec: Double, zh: Boolean): String {
    if (sec.isNaN() || sec.isInfinite() || sec < 0) return "—"
    if (sec == 0.0) return if (zh) "瞬间完成" else "Instant"
    if (sec < 1.0) return if (zh) "不到 1 秒" else "Under 1 second"
    var s = floor(sec).toLong()
    val days = s / 86400
    s %= 86400
    val hours = s / 3600
    s %= 3600
    val mins = s / 60
    s %= 60
    val parts = mutableListOf<String>()
    if (days > 0) parts += if (zh) "${days} 天" else "$days d"
    if (hours > 0) parts += if (zh) "${hours} 小时" else "$hours h"
    if (mins > 0) parts += if (zh) "${mins} 分钟" else "$mins min"
    if (s > 0 || parts.isEmpty()) parts += if (zh) "${s} 秒" else "$s s"
    return parts.joinToString(" ")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DownloadTimeTool() {
    val zh = rememberLifeZh()
    var speed by remember { mutableStateOf("10") }
    var speedUnit by remember { mutableStateOf("Mbps") }
    var overhead by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("1") }
    var sizeUnit by remember { mutableStateOf("GB") }

    val sp = speed.toDoubleOrNull()
    val sz = size.toDoubleOrNull()
    val oh = overhead.toDoubleOrNull() ?: 0.0

    SectionLabel(if (zh) "你的网速是多少？" else "What is your connection speed?")
    NumberField(speed, { speed = it }, label = if (zh) "网速" else "Speed")
    LifeChoiceChips(listOf("Kbps", "Mbps", "Gbps"), speedUnit) { speedUnit = it }

    SectionLabel(if (zh) "协议开销（%）" else "Overhead (%)")
    LifeChoiceChips(
        listOf("0", "1", "5", "10", "15", "20", "25", "50"),
        overhead.trim().removeSuffix("%").ifBlank { "0" },
    ) { overhead = it }
    NumberField(
        overhead,
        { overhead = it },
        label = if (zh) "自定义开销 %（常见约 5%）" else "Custom overhead % (typical ≈ 5%)",
        placeholder = "0",
    )

    SectionLabel(if (zh) "文件有多大？" else "How large is the file?")
    NumberField(size, { size = it }, label = if (zh) "文件大小" else "File size")
    LifeChoiceChips(listOf("KB", "MB", "GB", "TB"), sizeUnit) { sizeUnit = it }

    SectionLabel(if (zh) "常见文件大小" else "Common file sizes")
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        dtPresets.forEach { p ->
            FilterChip(
                selected = size == p.size && sizeUnit == p.unit,
                onClick = { size = p.size; sizeUnit = p.unit },
                label = { Text("${if (zh) p.zh else p.en} · ${p.size} ${p.unit}") },
            )
        }
    }

    val valid = sp != null && sp > 0 && sz != null && sz >= 0 && oh >= 0 && oh < 100
    if (valid) {
        val bits = dtSpeedBits(sp!!, speedUnit) * (1 - oh / 100.0)
        val bytes = dtSizeBytes(sz!!, sizeUnit)
        val seconds = (bytes * 8) / bits
        val mibPerSec = bits / 8 / (1024 * 1024)
        val sizeWord = lifeFmt(sz, if (sz % 1.0 == 0.0) 0 else 2) + " " + sizeUnit
        val effMbps = bits / 1e6
        ResultCard(
            main = (if (zh) "预计需要 " else "Estimated: ") + dtFormatDuration(seconds, zh),
            lines = listOf(
                if (zh) "以 ${lifeFmt(mibPerSec, 2)} MiB/s 传输 $sizeWord"
                else "Transferring $sizeWord at ${lifeFmt(mibPerSec, 2)} MiB/s",
                if (zh) "有效速率 ${lifeFmt(effMbps, 2)} Mbps（扣除 ${lifeFmt(oh, 0)}% 开销）"
                else "Effective rate ${lifeFmt(effMbps, 2)} Mbps (after ${lifeFmt(oh, 0)}% overhead)",
                if (zh) "公式：(字节 × 8) ÷ (比特率 × (1 − 开销))" else "Formula: (bytes × 8) ÷ (bits/s × (1 − overhead))",
            ),
        )
    } else {
        Text(
            text = if (zh) "请输入大于 0 的网速、非负的文件大小，开销需小于 100%。"
            else "Enter a speed above 0, a non-negative file size, and overhead below 100%.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ---------------------------------------------------------------------------
// Tip
// ---------------------------------------------------------------------------

@Composable
fun TipTool() {
    val zh = rememberLifeZh()
    var bill by remember { mutableStateOf("328") }
    var tipPct by remember { mutableStateOf("10") }
    var people by remember { mutableStateOf("4") }

    val b = bill.toDoubleOrNull()
    val pct = tipPct.toDoubleOrNull()
    val n = (people.toIntOrNull() ?: 1).coerceAtLeast(1)

    NumberField(bill, { bill = it }, label = if (zh) "账单金额" else "Bill amount")
    SectionLabel(if (zh) "小费比例" else "Tip %")
    LifeChoiceChips(
        listOf("8", "10", "15", "18", "20").map { "$it%" },
        pct?.let { v -> if (v % 1.0 == 0.0) "${v.toInt()}%" else "" } ?: "",
    ) { tipPct = it.removeSuffix("%") }
    NumberField(tipPct, { tipPct = it }, label = if (zh) "小费 %" else "Tip %")
    NumberField(people, { people = it }, label = if (zh) "人数" else "People")

    if (b != null && pct != null) {
        val tip = b * pct / 100
        val total = b + tip
        ResultCard(
            main = if (zh) "人均 ${lifeFmt(total / n)}" else "Per person ${lifeFmt(total / n)}",
            lines = listOf(
                if (zh) "小费：${lifeFmt(tip)}" else "Tip: ${lifeFmt(tip)}",
                if (zh) "合计（含小费）：${lifeFmt(total)}" else "Total with tip: ${lifeFmt(total)}",
                if (zh) "人均小费：${lifeFmt(tip / n)}" else "Tip per person: ${lifeFmt(tip / n)}",
            ),
        )
    }
}

// ---------------------------------------------------------------------------
// Discount
// ---------------------------------------------------------------------------

@Composable
fun DiscountTool() {
    val zh = rememberLifeZh()
    val modeZhe = if (zh) "折扣（折）" else "Zhe (折)"
    val modePct = if (zh) "减百分比" else "Percent off"
    val modeFlat = if (zh) "立减金额" else "Flat reduction"
    var mode by remember { mutableStateOf(0) }
    var price by remember { mutableStateOf("299") }
    var zhe by remember { mutableStateOf("8.5") }
    var pctOff by remember { mutableStateOf("20") }
    var flat by remember { mutableStateOf("30") }

    val modes = listOf(modeZhe, modePct, modeFlat)
    val p = price.toDoubleOrNull()

    NumberField(price, { price = it }, label = if (zh) "原价" else "Original price")
    LifeChoiceChips(modes, modes[mode]) { mode = modes.indexOf(it) }
    when (mode) {
        0 -> NumberField(zhe, { zhe = it }, label = if (zh) "几折（如 8 = 8折）" else "Zhe (e.g. 8 = 80% of price)")
        1 -> NumberField(pctOff, { pctOff = it }, label = if (zh) "优惠百分比 %" else "Percent off %")
        else -> NumberField(flat, { flat = it }, label = if (zh) "立减金额" else "Amount off")
    }

    if (p != null && p >= 0) {
        val paid: Double? = when (mode) {
            0 -> zhe.toDoubleOrNull()?.takeIf { it in 0.0..10.0 }?.let { p * it / 10 }
            1 -> pctOff.toDoubleOrNull()?.takeIf { it in 0.0..100.0 }?.let { p * (1 - it / 100) }
            else -> flat.toDoubleOrNull()?.takeIf { it >= 0 }?.let { (p - it).coerceAtLeast(0.0) }
        }
        if (paid != null) {
            val saved = p - paid
            val equivZhe = if (p > 0) paid / p * 10 else 0.0
            ResultCard(
                main = if (zh) "折后价 ${lifeFmt(paid)}" else "Final price ${lifeFmt(paid)}",
                lines = listOf(
                    if (zh) "节省：${lifeFmt(saved)}" else "You save: ${lifeFmt(saved)}",
                    if (zh) "相当于 ${lifeFmt(equivZhe)} 折（${lifeFmt(equivZhe * 10, 1)}% 的价格）"
                    else "Equivalent to ${lifeFmt(equivZhe)} zhe (${lifeFmt(equivZhe * 10, 1)}% of price)",
                    if (zh) "实际优惠 ${lifeFmt(100 - equivZhe * 10, 1)}%" else "Effective discount ${lifeFmt(100 - equivZhe * 10, 1)}%",
                ),
            )
        } else {
            Text(
                text = when (mode) {
                    0 -> if (zh) "折数需在 0–10 之间。" else "Zhe must be between 0 and 10."
                    1 -> if (zh) "百分比需在 0–100 之间。" else "Percent must be between 0 and 100."
                    else -> if (zh) "请输入有效的立减金额。" else "Enter a valid reduction amount."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Due date
// ---------------------------------------------------------------------------

@Composable
fun DueDateTool() {
    val zh = rememberLifeZh()
    var lmp by remember { mutableStateOf(LocalDate.now().minusDays(60).toString()) }
    var cycle by remember { mutableStateOf("28") }
    val base = remember(lmp) { runCatching { LocalDate.parse(lmp.trim()) }.getOrNull() }
    val cycleDays = cycle.toIntOrNull() ?: 28

    DateFieldWithPicker(
        value = lmp,
        onValueChange = { lmp = it },
        label = if (zh) "末次月经第一天 (yyyy-MM-dd)" else "Last menstrual period (yyyy-MM-dd)",
    )
    NumberField(cycle, { cycle = it }, label = if (zh) "月经周期（天，默认 28）" else "Cycle length (days, default 28)")

    if (base != null) {
        val due = base.plusDays(280L + (cycleDays - 28))
        val today = LocalDate.now()
        val gestDays = ChronoUnit.DAYS.between(base, today)
        val left = ChronoUnit.DAYS.between(today, due)
        val lines = mutableListOf<String>()
        if (gestDays >= 0) {
            val w = gestDays / 7
            val d = gestDays % 7
            lines += if (zh) "当前孕周：${w} 周 ${d} 天" else "Gestational age now: $w weeks $d days"
        } else {
            lines += if (zh) "末次月经日期在未来" else "LMP date is in the future"
        }
        lines += when {
            left > 0 -> if (zh) "距离预产期还有 $left 天" else "$left days until due date"
            left == 0L -> if (zh) "今天就是预产期" else "Due date is today"
            else -> if (zh) "已超过预产期 ${-left} 天" else "${-left} days past due date"
        }
        lines += if (zh) "公式：末次月经 + 280 天 +（周期 − 28）。仅供参考，以医生为准。"
        else "Formula: LMP + 280 days + (cycle − 28). Reference only — follow your clinician."
        ResultCard(main = if (zh) "预产期：$due" else "Due: $due", lines = lines)
    } else {
        Text(
            text = if (zh) "请按 yyyy-MM-dd 输入日期。" else "Enter a date as yyyy-MM-dd.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ---------------------------------------------------------------------------
// Mortgage
// ---------------------------------------------------------------------------

@Composable
fun MortgageTool() {
    val zh = rememberLifeZh()
    val modeEqualPi = if (zh) "等额本息" else "Equal payment"
    val modeEqualP = if (zh) "等额本金" else "Equal principal"
    var mode by remember { mutableStateOf(0) }
    var principal by remember { mutableStateOf("1000000") }
    var rate by remember { mutableStateOf("3.5") }
    var years by remember { mutableStateOf("30") }

    val modes = listOf(modeEqualPi, modeEqualP)
    val p = principal.toDoubleOrNull()
    val annual = rate.toDoubleOrNull()?.div(100)
    val y = years.toDoubleOrNull()

    LifeChoiceChips(modes, modes[mode]) { mode = modes.indexOf(it) }
    NumberField(principal, { principal = it }, label = if (zh) "贷款金额" else "Loan amount")
    NumberField(rate, { rate = it }, label = if (zh) "年利率 %" else "Annual rate %")
    NumberField(years, { years = it }, label = if (zh) "贷款年限" else "Term (years)")

    if (p != null && annual != null && y != null && p > 0 && annual >= 0 && y > 0) {
        val n = (y * 12).toInt().coerceAtLeast(1)
        val r = annual / 12
        if (mode == 0) {
            val monthly = if (r == 0.0) p / n else p * r * (1 + r).pow(n) / ((1 + r).pow(n) - 1)
            val total = monthly * n
            ResultCard(
                main = if (zh) "月供 ${lifeFmt(monthly)}" else "Monthly payment ${lifeFmt(monthly)}",
                lines = listOf(
                    if (zh) "首月还款：${lifeFmt(monthly)}（每月相同）" else "First month: ${lifeFmt(monthly)} (same every month)",
                    if (zh) "总利息：${lifeFmt(total - p)}" else "Total interest: ${lifeFmt(total - p)}",
                    if (zh) "还款总额：${lifeFmt(total)}" else "Total payment: ${lifeFmt(total)}",
                    if (zh) "期数：$n 个月" else "Periods: $n months",
                ),
            )
        } else {
            val monthlyPrincipal = p / n
            val first = monthlyPrincipal + p * r
            val last = monthlyPrincipal + monthlyPrincipal * r
            val totalInterest = (n + 1) * p * r / 2
            ResultCard(
                main = if (zh) "首月还款 ${lifeFmt(first)}" else "First month ${lifeFmt(first)}",
                lines = listOf(
                    if (zh) "末月还款：${lifeFmt(last)}" else "Last month: ${lifeFmt(last)}",
                    if (zh) "每月递减：${lifeFmt(monthlyPrincipal * r)}" else "Monthly decrease: ${lifeFmt(monthlyPrincipal * r)}",
                    if (zh) "每月本金：${lifeFmt(monthlyPrincipal)}" else "Principal per month: ${lifeFmt(monthlyPrincipal)}",
                    if (zh) "总利息：${lifeFmt(totalInterest)}" else "Total interest: ${lifeFmt(totalInterest)}",
                    if (zh) "还款总额：${lifeFmt(p + totalInterest)}" else "Total payment: ${lifeFmt(p + totalInterest)}",
                    if (zh) "期数：$n 个月" else "Periods: $n months",
                ),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Password
// ---------------------------------------------------------------------------

private const val PW_LOWER = "abcdefghijklmnopqrstuvwxyz"
private const val PW_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
private const val PW_DIGITS = "0123456789"
private const val PW_SYMBOLS = "!@#$%^&*-_=+"

private fun generatePassword(
    len: Int,
    pools: List<String>,
    must: String,
    contiguous: Boolean,
): String {
    val rnd = SecureRandom()
    val all = pools.joinToString("")
    val out = CharArray(len)
    val used = BooleanArray(len)

    if (must.isNotEmpty()) {
        if (contiguous) {
            val start = rnd.nextInt(len - must.length + 1)
            must.forEachIndexed { i, c ->
                out[start + i] = c
                used[start + i] = true
            }
        } else {
            val slots = (0 until len).toMutableList()
            slots.shuffle(rnd)
            val picked = slots.take(must.length).sorted()
            must.forEachIndexed { i, c ->
                out[picked[i]] = c
                used[picked[i]] = true
            }
        }
    }

    val free = (0 until len).filter { !used[it] }.toMutableList()
    free.shuffle(rnd)
    var idx = 0
    // Guarantee at least one character from every enabled set (unless already covered by `must`).
    for (pool in pools) {
        if (idx >= free.size) break
        if (must.any { it in pool }) continue
        out[free[idx]] = pool[rnd.nextInt(pool.length)]
        used[free[idx]] = true
        idx++
    }
    for (i in 0 until len) {
        if (!used[i]) out[i] = all[rnd.nextInt(all.length)]
    }
    return String(out)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PasswordTool() {
    val zh = rememberLifeZh()
    val clipboard = LocalClipboardManager.current
    var length by remember { mutableStateOf("16") }
    var useLower by remember { mutableStateOf(true) }
    var useUpper by remember { mutableStateOf(true) }
    var useDigits by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }
    var must by remember { mutableStateOf("") }
    var contiguous by remember { mutableStateOf(true) }
    var pwd by remember {
        mutableStateOf(generatePassword(16, listOf(PW_LOWER, PW_UPPER, PW_DIGITS, PW_SYMBOLS), "", true))
    }
    var error by remember { mutableStateOf<String?>(null) }
    var copied by remember { mutableStateOf(false) }

    NumberField(length, { length = it }, label = if (zh) "长度（4–64）" else "Length (4–64)")

    SectionLabel(if (zh) "字符类型" else "Character sets")
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(useLower, { useLower = !useLower }, label = { Text(if (zh) "小写 a-z" else "Lowercase a-z") })
        FilterChip(useUpper, { useUpper = !useUpper }, label = { Text(if (zh) "大写 A-Z" else "Uppercase A-Z") })
        FilterChip(useDigits, { useDigits = !useDigits }, label = { Text(if (zh) "数字 0-9" else "Digits 0-9") })
        FilterChip(useSymbols, { useSymbols = !useSymbols }, label = { Text(if (zh) "符号 !@#" else "Symbols !@#") })
    }

    CalcTextField(
        must,
        { must = it },
        label = if (zh) "必须包含的字符（可选）" else "Must include (optional)",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = if (contiguous) {
                if (zh) "必须内容连续出现" else "Keep must-include text contiguous"
            } else {
                if (zh) "必须字符分散放置" else "Scatter must-include characters"
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = contiguous, onCheckedChange = { contiguous = it })
    }

    PrimaryButton(if (zh) "生成" else "Generate", onClick = {
        copied = false
        val len = (length.toIntOrNull() ?: 16).coerceIn(4, 64)
        val pools = buildList {
            if (useLower) add(PW_LOWER)
            if (useUpper) add(PW_UPPER)
            if (useDigits) add(PW_DIGITS)
            if (useSymbols) add(PW_SYMBOLS)
        }
        when {
            pools.isEmpty() -> error = if (zh) "请至少选择一种字符类型。" else "Select at least one character set."
            must.length > len -> error = if (zh) "必须包含的内容长度不能超过密码长度。" else "Must-include text is longer than the password length."
            else -> {
                error = null
                pwd = generatePassword(len, pools, must, contiguous)
            }
        }
    })

    error?.let {
        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
    }

    if (pwd.isNotEmpty()) {
        val lines = mutableListOf(
            if (zh) "使用 SecureRandom 在本地生成" else "Generated locally with SecureRandom",
            if (zh) "长度：${pwd.length}" else "Length: ${pwd.length}",
        )
        if (must.isNotEmpty()) {
            val ok = if (contiguous) pwd.contains(must) else must.all { pwd.indexOf(it) >= 0 }
            lines += if (ok) {
                if (zh) "已满足必须包含条件" else "Must-include satisfied"
            } else {
                if (zh) "（当前结果为上次生成，必须包含内容已修改）" else "(Showing previous result; must-include text has changed)"
            }
        }
        ResultCard(main = pwd, lines = lines)
        GhostButton(
            text = if (copied) stringResource(R.string.copied) else stringResource(R.string.copy),
            onClick = {
                clipboard.setText(AnnotatedString(pwd))
                copied = true
            },
        )
    }
}

// ---------------------------------------------------------------------------
// Word count
// ---------------------------------------------------------------------------

private val wcEnWordRegex = Regex("[A-Za-z0-9]+(?:'[A-Za-z0-9]+)?")
private val wcNewlines = Regex("\\n+")

@Composable
fun WordCountTool() {
    val zh = rememberLifeZh()
    var text by remember { mutableStateOf("") }
    CalcTextField(
        text,
        { text = it },
        label = if (zh) "输入或粘贴文本" else "Type or paste text",
        minLines = 6,
        maxLines = 14,
    )

    val chars = text.length
    val charsNoSpace = text.count { !it.isWhitespace() }
    val cjk = text.count { it.code in 0x4E00..0x9FFF }
    val enWords = wcEnWordRegex.findAll(text).count()
    val paras = text.split(wcNewlines).count { it.isNotBlank() }
    val minutes = ceil((cjk + enWords) / 400.0).toInt().coerceAtLeast(1)

    ResultCard(
        main = if (zh) "不含空格字数：$charsNoSpace" else "Characters (no spaces): $charsNoSpace",
        lines = listOf(
            if (zh) "总字符数：$chars" else "Total characters: $chars",
            if (zh) "汉字：$cjk · 英文单词：$enWords" else "CJK characters: $cjk · English words: $enWords",
            if (zh) "段落：$paras · 阅读约 $minutes 分钟" else "Paragraphs: $paras · Reading ≈ $minutes min",
        ),
    )
}
