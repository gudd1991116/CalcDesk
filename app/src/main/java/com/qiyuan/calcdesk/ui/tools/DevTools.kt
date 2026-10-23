package com.qiyuan.calcdesk.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qiyuan.calcdesk.ui.components.CalcTextField
import com.qiyuan.calcdesk.ui.components.GhostButton
import com.qiyuan.calcdesk.ui.components.PrimaryButton
import com.qiyuan.calcdesk.ui.components.ResultCard
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.util.regex.Pattern

@Composable
fun DevToolRouter(toolId: String) {
    when (toolId) {
        "java-formatter" -> JavaFormatterTool()
        "json-formatter" -> JsonFormatterTool()
        "regex" -> RegexTool()
        else -> Text("Unknown dev tool")
    }
}

// ---------------------------------------------------------------------------
// Java formatter
// ---------------------------------------------------------------------------

@Composable fun JavaFormatterTool() {
    val zh = toolIsZh()
    var code by remember { mutableStateOf("public class Demo{\nvoid run(){int[] a={1,2,3};for(int i=0;i<3;i++){System.out.println(\"x;{\"+i);}\nif(a.length>0){return;}else{return;}\n}\n}") }
    var out by remember { mutableStateOf("") }
    CalcTextField(code, { code = it }, label = "Java", minLines = 6, maxLines = 16)
    PrimaryButton(if (zh) "格式化" else "Format", onClick = { out = formatJava(code) })
    if (out.isNotEmpty()) {
        CalcTextField(out, {}, label = if (zh) "输出" else "Output", minLines = 6, readOnly = true)
        ToolCopyButton(out)
    }
}

private fun formatJava(src: String): String {
    val s = src.replace("\r\n", "\n").replace('\r', '\n')
    val lines = ArrayList<Pair<Int, String>>()
    val cur = StringBuilder()
    var indent = 0
    var paren = 0
    var pendingSpace = false
    val braces = ArrayList<Boolean>() // true = inline brace (array initializer)
    val annotation = Regex("@[\\w.]+(\\(.*\\))?")

    fun flush() {
        val t = cur.toString().trim()
        if (t.isNotEmpty()) lines.add(indent to t)
        cur.setLength(0)
        pendingSpace = false
    }

    fun emit(text: String) {
        if (pendingSpace && cur.isNotEmpty()) cur.append(' ')
        pendingSpace = false
        cur.append(text)
    }

    var i = 0
    while (i < s.length) {
        val c = s[i]
        when {
            c == '\n' -> {
                if (annotation.matches(cur.toString().trim())) flush()
                else if (cur.isNotEmpty()) pendingSpace = true
                i++
            }
            c.isWhitespace() -> {
                if (cur.isNotEmpty()) pendingSpace = true
                i++
            }
            s.startsWith("//", i) -> {
                var e = s.indexOf('\n', i)
                if (e < 0) e = s.length
                emit(s.substring(i, e).trimEnd())
                flush()
                i = e
            }
            s.startsWith("/*", i) -> {
                var e = s.indexOf("*/", i + 2)
                e = if (e < 0) s.length else e + 2
                val text = s.substring(i, e)
                if (!text.contains('\n')) {
                    emit(text)
                } else {
                    flush()
                    text.split('\n').forEachIndexed { idx, raw ->
                        val t = raw.trim()
                        lines.add(indent to (if (idx > 0 && t.startsWith("*")) " $t" else t))
                    }
                }
                i = e
            }
            s.startsWith("\"\"\"", i) -> {
                var e = s.indexOf("\"\"\"", i + 3)
                e = if (e < 0) s.length else e + 3
                emit(s.substring(i, e))
                i = e
            }
            c == '"' || c == '\'' -> {
                var e = i + 1
                while (e < s.length && s[e] != c) {
                    if (s[e] == '\\') e++
                    e++
                }
                e = minOf(e + 1, s.length)
                emit(s.substring(i, e))
                i = e
            }
            c == '(' -> {
                paren++
                emit("(")
                i++
            }
            c == ')' -> {
                paren = maxOf(0, paren - 1)
                pendingSpace = false
                emit(")")
                i++
            }
            c == ',' -> {
                pendingSpace = false
                emit(",")
                pendingSpace = true
                i++
            }
            c == ';' -> {
                pendingSpace = false
                cur.append(';')
                if (paren > 0) pendingSpace = true else flush()
                i++
            }
            c == '{' -> {
                val t = cur.toString().trimEnd()
                val inline = braces.lastOrNull() == true ||
                    t.endsWith("=") || t.endsWith("]") || t.endsWith(",") || t.endsWith("(")
                if (inline) {
                    emit("{")
                    braces.add(true)
                } else {
                    var merged = false
                    if (cur.isEmpty() && lines.isNotEmpty()) {
                        val last = lines.last()
                        val lt = last.second.trim()
                        val blocked = lt.endsWith(";") || lt.endsWith("{") || lt.endsWith("}") ||
                            lt.startsWith("//") || lt.startsWith("/*") || lt.startsWith("*") ||
                            lt.endsWith("*/") || lt.startsWith("@")
                        if (!blocked) {
                            lines[lines.lastIndex] = last.first to (last.second + " {")
                            merged = true
                        }
                    }
                    if (!merged) {
                        if (cur.isNotEmpty()) cur.append(' ')
                        cur.append('{')
                    }
                    pendingSpace = false
                    flush()
                    indent++
                    braces.add(false)
                }
                i++
            }
            c == '}' -> {
                if (braces.lastOrNull() == true) {
                    braces.removeAt(braces.lastIndex)
                    pendingSpace = false
                    emit("}")
                } else {
                    if (braces.isNotEmpty()) braces.removeAt(braces.lastIndex)
                    flush()
                    indent = maxOf(0, indent - 1)
                    cur.append('}')
                    var j = i + 1
                    while (j < s.length && s[j].isWhitespace()) j++
                    val rest = s.substring(j, minOf(s.length, j + 8))
                    val joinWord = rest.startsWith("else") || rest.startsWith("catch") || rest.startsWith("finally")
                    val joinPunct = j < s.length && (s[j] == ';' || s[j] == ',' || s[j] == ')')
                    if (joinWord) pendingSpace = true
                    else if (!joinPunct) flush()
                }
                i++
            }
            else -> {
                emit(c.toString())
                i++
            }
        }
    }
    flush()
    return lines.joinToString("\n") { "    ".repeat(it.first) + it.second }
}

// ---------------------------------------------------------------------------
// JSON formatter
// ---------------------------------------------------------------------------

private fun parseJson(raw: String): Any {
    val text = raw.trim()
    if (text.isEmpty()) throw IllegalArgumentException("Empty input")
    val tokener = JSONTokener(text)
    val value = tokener.nextValue()
    if (tokener.nextClean().code != 0) throw IllegalArgumentException("Unexpected content after JSON value")
    return value
}

private fun jsonToString(v: Any, indent: Int): String {
    val out = when (v) {
        is JSONObject -> if (indent > 0) v.toString(indent) else v.toString()
        is JSONArray -> if (indent > 0) v.toString(indent) else v.toString()
        is String -> JSONObject.quote(v)
        else -> v.toString()
    }
    return out.replace("\\/", "/")
}

private fun jsonSummary(v: Any, zh: Boolean): String = when (v) {
    is JSONObject -> if (zh) "对象，${v.length()} 个键" else "object, ${v.length()} keys"
    is JSONArray -> if (zh) "数组，${v.length()} 项" else "array, ${v.length()} items"
    is String -> if (zh) "字符串" else "string"
    is Boolean -> if (zh) "布尔值" else "boolean"
    is Number -> if (zh) "数字" else "number"
    else -> "null"
}

private fun xmlName(k: String): String {
    val cleaned = k.map { if (it.isLetterOrDigit() || it == '_' || it == '-' || it == '.') it else '_' }.joinToString("")
    return if (cleaned.isEmpty() || !(cleaned[0].isLetter() || cleaned[0] == '_')) "_$cleaned" else cleaned
}

private fun xmlEscape(s: String): String =
    s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

private fun jsonToXml(v: Any?, name: String, level: Int, sb: StringBuilder) {
    val pad = "  ".repeat(level)
    when {
        v == null || v == JSONObject.NULL -> sb.append(pad).append('<').append(name).append("/>\n")
        v is JSONObject -> {
            sb.append(pad).append('<').append(name).append(">\n")
            val keys = v.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                jsonToXml(v.opt(k), xmlName(k), level + 1, sb)
            }
            sb.append(pad).append("</").append(name).append(">\n")
        }
        v is JSONArray -> {
            if (v.length() == 0) {
                sb.append(pad).append('<').append(name).append("/>\n")
            } else {
                for (idx in 0 until v.length()) jsonToXml(v.opt(idx), name, level, sb)
            }
        }
        else -> sb.append(pad).append('<').append(name).append('>')
            .append(xmlEscape(v.toString()))
            .append("</").append(name).append(">\n")
    }
}

private fun jsonToXmlDocument(v: Any): String {
    val sb = StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
    if (v is JSONArray) {
        sb.append("<root>\n")
        for (idx in 0 until v.length()) jsonToXml(v.opt(idx), "item", 1, sb)
        sb.append("</root>\n")
    } else {
        jsonToXml(v, "root", 0, sb)
    }
    return sb.toString().trimEnd()
}

@Composable fun JsonFormatterTool() {
    val zh = toolIsZh()
    var input by remember { mutableStateOf("{\"a\":1,\"b\":[2,3],\"c\":{\"d\":\"x\"}}") }
    var output by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf<String?>(null) }

    fun runJson(transform: (Any) -> String) {
        error = null
        status = null
        output = ""
        runCatching { parseJson(input) }
            .onSuccess { v ->
                runCatching { transform(v) }
                    .onSuccess { output = it }
                    .onFailure { e -> error = e.message ?: "Error" }
            }
            .onFailure { e -> error = e.message ?: "Error" }
    }

    CalcTextField(input, { input = it }, label = "JSON", minLines = 5, maxLines = 14)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PrimaryButton(
            if (zh) "格式化" else "Pretty",
            onClick = { runJson { jsonToString(it, 2) } },
            modifier = Modifier.weight(1f),
        )
        GhostButton(
            if (zh) "压缩" else "Minify",
            onClick = { runJson { jsonToString(it, 0) } },
            modifier = Modifier.weight(1f),
        )
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        GhostButton(
            if (zh) "校验" else "Validate",
            onClick = {
                runJson { v ->
                    status = (if (zh) "有效 JSON：" else "Valid JSON: ") + jsonSummary(v, zh)
                    ""
                }
            },
            modifier = Modifier.weight(1f),
        )
        GhostButton(
            if (zh) "转 XML" else "To XML",
            onClick = { runJson { jsonToXmlDocument(it) } },
            modifier = Modifier.weight(1f),
        )
    }
    status?.let { ResultCard(main = "OK", lines = listOf(it)) }
    error?.let { ResultCard(main = if (zh) "错误" else "Error", lines = listOf(it)) }
    if (output.isNotEmpty()) {
        CalcTextField(output, {}, label = if (zh) "结果" else "Result", minLines = 5, readOnly = true)
        ToolCopyButton(output)
    }
}

// ---------------------------------------------------------------------------
// Regex tester
// ---------------------------------------------------------------------------

@Composable fun RegexTool() {
    val zh = toolIsZh()
    var pattern by remember { mutableStateOf("\\d+") }
    var input by remember { mutableStateOf("Order 42 and 99") }
    var replace by remember { mutableStateOf("#") }
    var flagG by remember { mutableStateOf(true) }
    var flagI by remember { mutableStateOf(false) }
    var flagM by remember { mutableStateOf(false) }
    var flagS by remember { mutableStateOf(false) }

    val templates = listOf(
        (if (zh) "邮箱" else "Email") to "[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+",
        "URL" to "https?://[\\w.-]+(?:/[\\w./?%&=#+~-]*)?",
        (if (zh) "数字" else "Digits") to "\\d+",
        (if (zh) "中文" else "Chinese") to "[\\u4e00-\\u9fa5]+",
    )

    ToolChipRow(templates.map { it.first }, -1) { pattern = templates[it].second }
    CalcTextField(pattern, { pattern = it }, label = if (zh) "正则表达式" else "Pattern")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        ToolCheckRow("g", flagG) { flagG = it }
        ToolCheckRow("i", flagI) { flagI = it }
        ToolCheckRow("m", flagM) { flagM = it }
        ToolCheckRow("s", flagS) { flagS = it }
    }
    CalcTextField(input, { input = it }, label = if (zh) "文本" else "Text", minLines = 3)

    var flags = 0
    if (flagI) flags = flags or Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
    if (flagM) flags = flags or Pattern.MULTILINE
    if (flagS) flags = flags or Pattern.DOTALL

    val compiled = if (pattern.isEmpty()) null else runCatching { Pattern.compile(pattern, flags) }
    val compiledPattern = compiled?.getOrNull()
    val compileError = compiled?.exceptionOrNull()

    if (compileError != null) {
        ResultCard(
            main = if (zh) "正则无效" else "Invalid pattern",
            lines = listOf((compileError.message ?: "").lineSequence().firstOrNull() ?: ""),
        )
    }
    if (compiledPattern != null) {
        val matcher = compiledPattern.matcher(input)
        val lines = mutableListOf<String>()
        var total = 0
        while (total < 10000 && matcher.find()) {
            if (total < 10) {
                lines += "#${total + 1} [${matcher.start()}-${matcher.end()}] \"${matcher.group()}\""
                if (total < 5) {
                    for (g in 1..matcher.groupCount()) {
                        lines += "    \$$g = ${matcher.group(g) ?: "null"}"
                    }
                }
            }
            total++
            if (!flagG) break
        }
        if (total > 10) {
            lines += if (zh) "… 其余 ${total - 10} 个匹配未显示" else "… ${total - 10} more not shown"
        }
        ResultCard(
            main = if (zh) "$total 个匹配" else "$total match(es)",
            lines = lines,
        )
    }

    CalcTextField(replace, { replace = it }, label = if (zh) "替换为" else "Replace with")
    if (compiledPattern != null) {
        val replaced = runCatching {
            val m = compiledPattern.matcher(input)
            if (flagG) m.replaceAll(replace) else m.replaceFirst(replace)
        }
        val replacedText = replaced.getOrNull()
        if (replacedText != null) {
            CalcTextField(replacedText, {}, label = if (zh) "替换结果" else "Replace result", minLines = 2, readOnly = true)
            ToolCopyButton(replacedText)
        } else {
            Text(
                replaced.exceptionOrNull()?.message ?: "Error",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
