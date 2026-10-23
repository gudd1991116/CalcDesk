package com.qiyuan.calcdesk.ui.tools

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qiyuan.calcdesk.ui.components.CalcTextField
import com.qiyuan.calcdesk.ui.components.GhostButton
import com.qiyuan.calcdesk.ui.components.NumberField
import com.qiyuan.calcdesk.ui.components.PrimaryButton
import com.qiyuan.calcdesk.ui.components.ResultCard
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.util.Locale
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Shared helpers (also used by DevTools.kt)
// ---------------------------------------------------------------------------

internal fun toolIsZh(): Boolean = Locale.getDefault().language.startsWith("zh")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ToolChipRow(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEachIndexed { index, label ->
            FilterChip(
                selected = index == selected,
                onClick = { onSelect(index) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
internal fun ToolCopyButton(text: String, modifier: Modifier = Modifier) {
    val clipboard = LocalClipboardManager.current
    GhostButton(
        text = if (toolIsZh()) "复制" else "Copy",
        onClick = { clipboard.setText(AnnotatedString(text)) },
        modifier = modifier,
    )
}

@Composable
internal fun ToolCheckRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.clickable { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = onChange)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun mathFmt(v: Double): String {
    if (v.isNaN() || v.isInfinite()) return "Error"
    val a = abs(v)
    if (a < 1e-12) return "0"
    val bd = BigDecimal(v).round(MathContext(12)).stripTrailingZeros()
    return if (a >= 1e15 || a < 1e-6) bd.toString() else bd.toPlainString()
}

private fun money(v: Double): String = String.format(Locale.US, "%.2f", v)

@Composable
fun MathToolRouter(toolId: String) {
    when (toolId) {
        "calc-basic" -> BasicCalculatorTool()
        "calc-sci" -> ScientificCalculatorTool()
        "radix" -> RadixTool()
        "fraction" -> FractionTool()
        "percent" -> PercentTool()
        "compound" -> CompoundInterestTool()
        "random" -> RandomTool()
        else -> Text("Unknown math tool")
    }
}

// ---------------------------------------------------------------------------
// Basic calculator
// ---------------------------------------------------------------------------

@Composable
private fun RowScope.KeyButton(
    label: String,
    container: Color,
    content: Color,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
    ) { Text(label, maxLines = 1) }
}

@Composable fun BasicCalculatorTool() {
    var display by remember { mutableStateOf("0") }
    var stored by remember { mutableStateOf<Double?>(null) }
    var op by remember { mutableStateOf<String?>(null) }
    var fresh by remember { mutableStateOf(true) }
    var memory by remember { mutableStateOf(0.0) }
    var hasMemory by remember { mutableStateOf(false) }

    fun showError() {
        display = "Error"; stored = null; op = null; fresh = true
    }

    fun pressDigit(d: String) {
        if (fresh || display == "0" || display == "Error") {
            display = d
            fresh = false
        } else if (display.length < 16) {
            display += d
        }
    }

    fun pressDot() {
        if (fresh || display == "Error") { display = "0."; fresh = false; return }
        if (!display.contains('.')) display += "."
    }

    fun compute(a: Double, b: Double, operation: String): Double? = when (operation) {
        "+" -> a + b
        "−" -> a - b
        "×" -> a * b
        "÷" -> if (b != 0.0) a / b else null
        else -> null
    }

    fun pressOp(next: String) {
        val cur = display.toDoubleOrNull() ?: return
        val s = stored
        val o = op
        if (s != null && o != null && !fresh) {
            val r = compute(s, cur, o)
            if (r == null) { showError(); return }
            display = mathFmt(r)
        }
        stored = display.toDoubleOrNull()
        op = next
        fresh = true
    }

    fun pressEquals() {
        val a = stored ?: return
        val b = display.toDoubleOrNull() ?: return
        val o = op ?: return
        val r = compute(a, b, o)
        if (r == null) { showError(); return }
        display = mathFmt(r)
        stored = null
        op = null
        fresh = true
    }

    fun pressPercent() {
        val cur = display.toDoubleOrNull() ?: return
        val s = stored
        val o = op
        val v = if (s != null && o != null && (o == "+" || o == "−")) s * cur / 100.0 else cur / 100.0
        display = mathFmt(v)
        fresh = (o == null)
    }

    fun pressBackspace() {
        if (fresh || display == "Error") return
        display = display.dropLast(1)
        if (display.isEmpty() || display == "-") display = "0"
    }

    fun pressNegate() {
        val cur = display.toDoubleOrNull() ?: return
        if (cur == 0.0) return
        display = if (display.startsWith("-")) display.removePrefix("-") else "-$display"
    }

    fun pressClear() {
        display = "0"; stored = null; op = null; fresh = true
    }

    fun memAdd(sign: Double) {
        val cur = display.toDoubleOrNull() ?: return
        memory += sign * cur
        hasMemory = true
        fresh = true
    }

    val scheme = MaterialTheme.colorScheme
    val subLine = buildString {
        if (hasMemory) append("M  ")
        val s = stored
        if (s != null) append(mathFmt(s)).append(' ').append(op ?: "")
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = subLine.ifEmpty { " " },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.End,
        )
        Text(
            text = display,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.End,
            maxLines = 1,
        )
        val keys = listOf(
            listOf("MC", "MR", "M+", "M−"),
            listOf("C", "⌫", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("±", "0", ".", "="),
        )
        val ops = setOf("+", "−", "×", "÷", "=")
        val ctrl = setOf("C", "⌫", "%")
        val mem = setOf("MC", "MR", "M+", "M−")
        keys.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { k ->
                    val container = when (k) {
                        in ops -> scheme.primary
                        in ctrl -> scheme.secondaryContainer
                        in mem -> scheme.tertiaryContainer
                        else -> scheme.surfaceVariant
                    }
                    val content = when (k) {
                        in ops -> scheme.onPrimary
                        in ctrl -> scheme.onSecondaryContainer
                        in mem -> scheme.onTertiaryContainer
                        else -> scheme.onSurface
                    }
                    KeyButton(k, container, content) {
                        when (k) {
                            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> pressDigit(k)
                            "." -> pressDot()
                            "=" -> pressEquals()
                            "C" -> pressClear()
                            "⌫" -> pressBackspace()
                            "%" -> pressPercent()
                            "±" -> pressNegate()
                            "MC" -> { memory = 0.0; hasMemory = false }
                            "MR" -> if (hasMemory) { display = mathFmt(memory); fresh = true }
                            "M+" -> memAdd(1.0)
                            "M−" -> memAdd(-1.0)
                            else -> pressOp(k)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Scientific calculator (recursive descent parser)
// ---------------------------------------------------------------------------

private class SciParser(private val s: String, private val deg: Boolean) {
    private var i = 0

    fun parse(): Double {
        if (s.isEmpty()) throw IllegalArgumentException("Empty expression")
        val v = parseExpr()
        if (i < s.length) throw IllegalArgumentException("Unexpected '${s[i]}'")
        return v
    }

    private fun peek(): Char = if (i < s.length) s[i] else '\u0000'

    private fun parseExpr(): Double {
        var v = parseTerm()
        while (true) {
            when (peek()) {
                '+' -> { i++; v += parseTerm() }
                '-' -> { i++; v -= parseTerm() }
                else -> return v
            }
        }
    }

    private fun parseTerm(): Double {
        var v = parseUnary()
        while (true) {
            val c = peek()
            when {
                c == '*' -> { i++; v *= parseUnary() }
                c == '/' -> {
                    i++
                    val d = parseUnary()
                    if (d == 0.0) throw ArithmeticException("Division by zero")
                    v /= d
                }
                c == '(' || c.isLetter() -> v *= parsePower() // implicit multiplication
                else -> return v
            }
        }
    }

    private fun parseUnary(): Double = when (peek()) {
        '-' -> { i++; -parseUnary() }
        '+' -> { i++; parseUnary() }
        else -> parsePower()
    }

    private fun parsePower(): Double {
        val base = parsePostfix()
        if (peek() == '^') {
            i++
            val exp = parseUnary()
            return base.pow(exp)
        }
        return base
    }

    private fun parsePostfix(): Double {
        var v = parsePrimary()
        while (true) {
            when (peek()) {
                '!' -> { i++; v = factorial(v) }
                '%' -> { i++; v /= 100.0 }
                else -> return v
            }
        }
    }

    private fun closeParen() {
        if (peek() == ')') i++
        else if (i < s.length) throw IllegalArgumentException("Expected ')'")
        // missing ')' at the very end is tolerated
    }

    private fun parsePrimary(): Double {
        val c = peek()
        if (c == '(') {
            i++
            val v = parseExpr()
            closeParen()
            return v
        }
        if (c.isDigit() || c == '.') return parseNumber()
        if (c.isLetter()) return parseName()
        throw IllegalArgumentException(if (i >= s.length) "Unexpected end" else "Unexpected '$c'")
    }

    private fun parseNumber(): Double {
        val start = i
        while (peek().isDigit() || peek() == '.') i++
        if (peek() == 'e') {
            val save = i
            i++
            if (peek() == '+' || peek() == '-') i++
            if (peek().isDigit()) {
                while (peek().isDigit()) i++
            } else {
                i = save
            }
        }
        return s.substring(start, i).toDoubleOrNull() ?: throw IllegalArgumentException("Bad number")
    }

    private fun parseName(): Double {
        val start = i
        while (peek().isLetter()) i++
        val name = s.substring(start, i)
        if (peek() == '(') {
            i++
            val args = mutableListOf<Double>()
            if (peek() == ')') {
                i++
            } else {
                args += parseExpr()
                while (peek() == ',') { i++; args += parseExpr() }
                closeParen()
            }
            return applyFn(name, args)
        }
        return when (name) {
            "pi" -> PI
            "e" -> E
            else -> throw IllegalArgumentException("Unknown '$name'")
        }
    }

    private fun toRad(a: Double) = if (deg) Math.toRadians(a) else a
    private fun fromRad(a: Double) = if (deg) Math.toDegrees(a) else a

    private fun applyFn(name: String, args: List<Double>): Double = when {
        name == "pow" && args.size == 2 -> args[0].pow(args[1])
        name == "log" && args.size == 2 -> ln(args[0]) / ln(args[1])
        args.size == 1 -> fn1(name, args[0])
        else -> throw IllegalArgumentException("Bad arguments for $name")
    }

    private fun fn1(name: String, a: Double): Double = when (name) {
        "sin" -> sin(toRad(a))
        "cos" -> cos(toRad(a))
        "tan" -> {
            if (deg && a % 180.0 == 90.0 || deg && a % 180.0 == -90.0) {
                throw ArithmeticException("tan undefined")
            }
            tan(toRad(a))
        }
        "asin" -> fromRad(asin(a))
        "acos" -> fromRad(acos(a))
        "atan" -> fromRad(atan(a))
        "ln" -> ln(a)
        "log" -> log10(a)
        "sqrt" -> sqrt(a)
        "cbrt" -> Math.cbrt(a)
        "abs" -> abs(a)
        "exp" -> kotlin.math.exp(a)
        else -> throw IllegalArgumentException("Unknown function '$name'")
    }

    private fun factorial(a: Double): Double {
        if (a < 0 || a != floor(a) || a > 170) throw ArithmeticException("Bad factorial")
        var r = 1.0
        for (k in 2..a.toInt()) r *= k
        return r
    }
}

private fun evalSci(raw: String, deg: Boolean): String {
    val s = raw.lowercase(Locale.ROOT)
        .replace("×", "*").replace("÷", "/").replace("−", "-")
        .replace("π", "pi").replace("√", "sqrt").replace("，", ",")
        .filterNot { it.isWhitespace() }
    val v = SciParser(s, deg).parse()
    if (v.isNaN() || v.isInfinite()) throw ArithmeticException("Math error")
    return mathFmt(v)
}

@Composable
private fun RowScope.SmallKey(label: String, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}

@Composable fun ScientificCalculatorTool() {
    val zh = toolIsZh()
    var expr by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var deg by remember { mutableStateOf(true) }

    fun backspace() {
        val m = Regex("[a-z]+\\($").find(expr)
        expr = if (m != null) expr.removeRange(m.range) else expr.dropLast(1)
    }

    fun evaluate() {
        error = null
        result = ""
        runCatching { evalSci(expr, deg) }
            .onSuccess { result = it }
            .onFailure { error = it.message ?: "Error" }
    }

    ToolChipRow(listOf("DEG", "RAD"), if (deg) 0 else 1) { deg = it == 0 }
    CalcTextField(expr, { expr = it }, label = if (zh) "表达式" else "Expression")

    val rows = listOf(
        listOf("sin(", "cos(", "tan(", "asin(", "acos(", "atan("),
        listOf("ln(", "log(", "sqrt(", "pow(", "^", "!"),
        listOf("pi", "e", "(", ")", ",", "%"),
        listOf("+", "−", "×", "÷", "⌫", "C"),
    )
    rows.forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            row.forEach { k ->
                SmallKey(k.removeSuffix("(")) {
                    when (k) {
                        "⌫" -> backspace()
                        "C" -> { expr = ""; result = ""; error = null }
                        else -> expr += k
                    }
                }
            }
        }
    }
    PrimaryButton(if (zh) "计算" else "Evaluate", onClick = { evaluate() })
    error?.let { ResultCard(main = if (zh) "错误" else "Error", lines = listOf(it)) }
    if (result.isNotEmpty()) {
        ResultCard(main = result, lines = listOf(if (deg) "DEG" else "RAD"))
        ToolCopyButton(result)
    }
}

// ---------------------------------------------------------------------------
// Radix
// ---------------------------------------------------------------------------

@Composable fun RadixTool() {
    val zh = toolIsZh()
    var bin by remember { mutableStateOf("11111111") }
    var oct by remember { mutableStateOf("377") }
    var dec by remember { mutableStateOf("255") }
    var hex by remember { mutableStateOf("FF") }
    var errorBase by remember { mutableStateOf<Int?>(null) }

    fun update(base: Int, text: String) {
        when (base) {
            2 -> bin = text
            8 -> oct = text
            10 -> dec = text
            else -> hex = text
        }
        val clean = text.trim()
        if (clean.isEmpty() || clean == "-" || clean == "+") {
            errorBase = null
            if (base != 2) bin = ""
            if (base != 8) oct = ""
            if (base != 10) dec = ""
            if (base != 16) hex = ""
            return
        }
        val n = runCatching { BigInteger(clean, base) }.getOrNull()
        if (n == null) {
            errorBase = base
            return
        }
        errorBase = null
        if (base != 2) bin = n.toString(2)
        if (base != 8) oct = n.toString(8)
        if (base != 10) dec = n.toString(10)
        if (base != 16) hex = n.toString(16).uppercase(Locale.ROOT)
    }

    CalcTextField(bin, { update(2, it) }, label = if (zh) "二进制 Bin (2)" else "Binary (2)")
    CalcTextField(oct, { update(8, it) }, label = if (zh) "八进制 Oct (8)" else "Octal (8)")
    CalcTextField(dec, { update(10, it) }, label = if (zh) "十进制 Dec (10)" else "Decimal (10)")
    CalcTextField(hex, { update(16, it) }, label = if (zh) "十六进制 Hex (16)" else "Hexadecimal (16)")
    errorBase?.let {
        Text(
            text = (if (zh) "输入对 $it 进制无效" else "Invalid digits for base $it"),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

// ---------------------------------------------------------------------------
// Fraction
// ---------------------------------------------------------------------------

private fun gcdLong(a: Long, b: Long): Long = if (b == 0L) a else gcdLong(b, a % b)

private fun mixedText(n: Long, d: Long): String? {
    if (d == 1L || abs(n) <= d) return null
    val whole = n / d
    val rem = abs(n) % d
    return if (rem == 0L) null else "$whole $rem/$d"
}

/** Continued-fraction approximation. Returns (numerator, denominator) with denominator > 0. */
private fun decimalToFraction(x: Double, maxDen: Long = 1_000_000L, tol: Double = 1e-9): Pair<Long, Long> {
    val neg = x < 0
    val target = abs(x)
    var h1 = 1L; var h0 = 0L
    var k1 = 0L; var k0 = 1L
    var b = target
    for (iter in 0 until 40) {
        val a = floor(b).toLong()
        val h = a * h1 + h0
        val k = a * k1 + k0
        if (k > maxDen) break
        h0 = h1; h1 = h
        k0 = k1; k1 = k
        if (abs(target - h1.toDouble() / k1.toDouble()) <= tol * maxOf(1.0, target)) break
        val frac = b - a
        if (frac < 1e-12) break
        b = 1.0 / frac
    }
    if (k1 == 0L) return Pair(if (neg) -floor(target).toLong() else floor(target).toLong(), 1L)
    return Pair(if (neg) -h1 else h1, k1)
}

@Composable fun FractionTool() {
    val zh = toolIsZh()
    var mode by remember { mutableStateOf(0) }
    var num by remember { mutableStateOf("6") }
    var den by remember { mutableStateOf("8") }
    var dec by remember { mutableStateOf("0.375") }

    ToolChipRow(
        listOf(if (zh) "约分" else "Simplify", if (zh) "小数转分数" else "Decimal → Fraction"),
        mode,
    ) { mode = it }

    if (mode == 0) {
        val n = num.trim().toLongOrNull()
        val d = den.trim().toLongOrNull()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(num, { num = it }, label = if (zh) "分子" else "Numerator", modifier = Modifier.weight(1f))
            NumberField(den, { den = it }, label = if (zh) "分母" else "Denominator", modifier = Modifier.weight(1f))
        }
        if (n != null && d != null && d != 0L) {
            val g = gcdLong(abs(n), abs(d)).let { if (it == 0L) 1L else it }
            val sign = if (d < 0) -1L else 1L
            val sn = sign * n / g
            val sd = sign * d / g
            val lines = mutableListOf<String>()
            mixedText(sn, sd)?.let { lines += (if (zh) "带分数：" else "Mixed: ") + it }
            lines += (if (zh) "小数值：" else "Decimal: ") + mathFmt(n.toDouble() / d.toDouble())
            lines += (if (zh) "最大公约数：" else "GCD: ") + g
            ResultCard(main = if (sd == 1L) "$sn" else "$sn/$sd", lines = lines)
        } else {
            ResultCard(main = if (d == 0L) (if (zh) "分母不能为 0" else "Denominator cannot be 0") else "—")
        }
    } else {
        NumberField(dec, { dec = it }, label = if (zh) "小数" else "Decimal")
        val x = dec.trim().toDoubleOrNull()
        if (x != null && !x.isNaN() && !x.isInfinite() && abs(x) < 1e12) {
            val (fn, fd) = decimalToFraction(x)
            val lines = mutableListOf<String>()
            mixedText(fn, fd)?.let { lines += (if (zh) "带分数：" else "Mixed: ") + it }
            lines += (if (zh) "小数值：" else "Decimal: ") + mathFmt(fn.toDouble() / fd.toDouble())
            ResultCard(main = if (fd == 1L) "$fn" else "$fn/$fd", lines = lines)
        } else {
            ResultCard(main = "—")
        }
    }
}

// ---------------------------------------------------------------------------
// Percent
// ---------------------------------------------------------------------------

@Composable fun PercentTool() {
    val zh = toolIsZh()
    var mode by remember { mutableStateOf(0) }
    var a by remember { mutableStateOf("25") }
    var b by remember { mutableStateOf("200") }

    val chips = if (zh) listOf("占比", "求总量", "变化率", "折扣")
    else listOf("A of B", "A is X% of ?", "Change", "Discount")
    ToolChipRow(chips, mode) { mode = it }

    val (labelA, labelB) = when (mode) {
        0 -> (if (zh) "A（部分）" else "A (part)") to (if (zh) "B（整体）" else "B (whole)")
        1 -> (if (zh) "A（数值）" else "A (value)") to (if (zh) "X（百分比 %）" else "X (percent %)")
        2 -> (if (zh) "A（原值）" else "A (from)") to (if (zh) "B（新值）" else "B (to)")
        else -> (if (zh) "原价" else "Price") to (if (zh) "折扣 %（减免）" else "Discount % off")
    }
    NumberField(a, { a = it }, label = labelA)
    NumberField(b, { b = it }, label = labelB)

    val x = a.trim().toDoubleOrNull()
    val y = b.trim().toDoubleOrNull()
    if (x == null || y == null) {
        ResultCard(main = "—")
        return
    }
    when (mode) {
        0 -> if (y == 0.0) ResultCard(main = if (zh) "B 不能为 0" else "B cannot be 0")
        else ResultCard(
            main = "${mathFmt(x / y * 100)}%",
            lines = listOf(if (zh) "${mathFmt(x)} 是 ${mathFmt(y)} 的 ${mathFmt(x / y * 100)}%" else "${mathFmt(x)} is ${mathFmt(x / y * 100)}% of ${mathFmt(y)}"),
        )
        1 -> if (y == 0.0) ResultCard(main = if (zh) "X 不能为 0" else "X cannot be 0")
        else ResultCard(
            main = mathFmt(x / (y / 100.0)),
            lines = listOf(if (zh) "${mathFmt(x)} 是 ${mathFmt(y)}% 对应的整体值" else "${mathFmt(x)} is ${mathFmt(y)}% of this value"),
        )
        2 -> if (x == 0.0) ResultCard(main = if (zh) "原值不能为 0" else "Original value cannot be 0")
        else {
            val pct = (y - x) / abs(x) * 100
            ResultCard(
                main = (if (pct >= 0) "+" else "") + mathFmt(pct) + "%",
                lines = listOf(
                    (if (zh) "差值：" else "Difference: ") + mathFmt(y - x),
                    if (pct >= 0) (if (zh) "增长" else "Increase") else (if (zh) "下降" else "Decrease"),
                ),
            )
        }
        else -> {
            val after = x * (1 - y / 100.0)
            ResultCard(
                main = (if (zh) "折后价：" else "Price after discount: ") + money(after),
                lines = listOf((if (zh) "节省：" else "You save: ") + money(x - after)),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Compound interest
// ---------------------------------------------------------------------------

@Composable fun CompoundInterestTool() {
    val zh = toolIsZh()
    var principal by remember { mutableStateOf("10000") }
    var rate by remember { mutableStateOf("5") }
    var years by remember { mutableStateOf("10") }
    var freq by remember { mutableStateOf(0) }

    val names = if (zh) listOf("每年", "每季度", "每月", "每天") else listOf("Annual", "Quarterly", "Monthly", "Daily")
    val periods = listOf(1, 4, 12, 365)

    NumberField(principal, { principal = it }, label = if (zh) "本金" else "Principal")
    NumberField(rate, { rate = it }, label = if (zh) "年利率 %" else "Annual rate %")
    NumberField(years, { years = it }, label = if (zh) "年数" else "Years")
    ToolChipRow(names, freq) { freq = it }

    val p = principal.trim().toDoubleOrNull()
    val r = rate.trim().toDoubleOrNull()?.div(100)
    val y = years.trim().toDoubleOrNull()
    if (p != null && r != null && y != null) {
        val n = periods[freq].toDouble()
        val amount = p * (1 + r / n).pow(n * y)
        val eay = ((1 + r / n).pow(n) - 1) * 100
        if (amount.isNaN() || amount.isInfinite()) {
            ResultCard(main = "—")
        } else {
            ResultCard(
                main = (if (zh) "最终金额：" else "Final amount: ") + money(amount),
                lines = listOf(
                    (if (zh) "利息收益：" else "Interest earned: ") + money(amount - p),
                    (if (zh) "有效年收益率：" else "Effective annual yield: ") + String.format(Locale.US, "%.4f%%", eay),
                ),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Random
// ---------------------------------------------------------------------------

@Composable fun RandomTool() {
    val zh = toolIsZh()
    var mode by remember { mutableStateOf(0) }
    var min by remember { mutableStateOf("1") }
    var max by remember { mutableStateOf("100") }
    var count by remember { mutableStateOf("5") }
    var length by remember { mutableStateOf("1") }
    var words by remember { mutableStateOf("") }
    var unique by remember { mutableStateOf(false) }
    var output by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    ToolChipRow(
        listOf(if (zh) "数字" else "Number", if (zh) "字母" else "Letters", if (zh) "自定义词语" else "Custom words"),
        mode,
    ) { mode = it; output = ""; error = null }

    when (mode) {
        0 -> {
            NumberField(min, { min = it }, label = if (zh) "最小值" else "Min")
            NumberField(max, { max = it }, label = if (zh) "最大值" else "Max")
        }
        1 -> NumberField(length, { length = it }, label = if (zh) "每项长度" else "Length per item")
        else -> CalcTextField(
            words, { words = it },
            label = if (zh) "每行一个词语" else "One word per line",
            minLines = 4, maxLines = 10,
        )
    }
    NumberField(count, { count = it }, label = if (zh) "生成数量" else "Count")
    ToolCheckRow(if (zh) "不重复" else "Unique (no repeats)", unique) { unique = it }

    PrimaryButton(if (zh) "生成" else "Generate", onClick = {
        error = null
        val c = count.trim().toIntOrNull()?.coerceIn(1, 500)
        if (c == null) {
            error = if (zh) "请输入有效数量" else "Enter a valid count"
            return@PrimaryButton
        }
        when (mode) {
            0 -> {
                val a = min.trim().toLongOrNull()
                val b = max.trim().toLongOrNull()
                if (a == null || b == null || a == Long.MAX_VALUE || b == Long.MAX_VALUE) {
                    error = if (zh) "请输入有效整数" else "Enter valid integers"
                    return@PrimaryButton
                }
                val lo = minOf(a, b)
                val hi = maxOf(a, b)
                val size = hi - lo + 1
                val items: List<Long> = if (unique) {
                    if (size in 1..c.toLong() - 1) {
                        error = if (zh) "范围内只有 $size 个不同数字" else "Only $size distinct values in range"
                        return@PrimaryButton
                    }
                    if (size <= 5000L) {
                        (lo..hi).shuffled().take(c)
                    } else {
                        val set = LinkedHashSet<Long>()
                        while (set.size < c) set.add(Random.nextLong(lo, hi + 1))
                        set.toList()
                    }
                } else {
                    List(c) { Random.nextLong(lo, hi + 1) }
                }
                output = items.joinToString(", ")
            }
            1 -> {
                val len = length.trim().toIntOrNull()?.coerceIn(1, 64)
                if (len == null) {
                    error = if (zh) "请输入有效长度" else "Enter a valid length"
                    return@PrimaryButton
                }
                val alphabet = ('A'..'Z') + ('a'..'z')
                if (unique && 52.0.pow(len) < c) {
                    error = if (zh) "组合数不足以生成 $c 个不重复项" else "Not enough combinations for $c unique items"
                    return@PrimaryButton
                }
                val items: List<String> = if (unique) {
                    val set = LinkedHashSet<String>()
                    while (set.size < c) {
                        set.add(String(CharArray(len) { alphabet[Random.nextInt(alphabet.size)] }))
                    }
                    set.toList()
                } else {
                    List(c) { String(CharArray(len) { alphabet[Random.nextInt(alphabet.size)] }) }
                }
                output = items.joinToString(", ")
            }
            else -> {
                val list = words.lines().map { it.trim() }.filter { it.isNotEmpty() }
                if (list.isEmpty()) {
                    error = if (zh) "请至少输入一个词语" else "Enter at least one word"
                    return@PrimaryButton
                }
                val items: List<String> = if (unique) {
                    if (c > list.size) {
                        error = if (zh) "数量超过词语总数 (${list.size})" else "Count exceeds number of words (${list.size})"
                        return@PrimaryButton
                    }
                    list.shuffled().take(c)
                } else {
                    List(c) { list[Random.nextInt(list.size)] }
                }
                output = items.joinToString("\n")
            }
        }
    })
    error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
    if (output.isNotEmpty()) {
        ResultCard(main = output)
        ToolCopyButton(output)
    }
}
