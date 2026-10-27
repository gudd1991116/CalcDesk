package com.qiyuan.calcdesk.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.qiyuan.calcdesk.R

/** Same bold style as timestamp conversion fields. */
@Composable
fun InputFieldTextStyle(): TextStyle =
    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalcDeskScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                            )
                        }
                    }
                },
                actions = { actions() },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        content = content,
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(text)
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Text(text)
    }
}

@Composable
fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    placeholder: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        textStyle = InputFieldTextStyle(),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}

@Composable
fun CalcTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    readOnly: Boolean = false,
    placeholder: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        modifier = modifier.fillMaxWidth(),
        minLines = minLines,
        maxLines = maxLines,
        readOnly = readOnly,
        textStyle = InputFieldTextStyle(),
        shape = RoundedCornerShape(16.dp),
    )
}

/**
 * @param mainIsResult when false, [main] is treated as a hint/tag (muted), not a highlighted result.
 */
@Composable
fun ResultCard(
    main: String,
    lines: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    mainIsResult: Boolean = true,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            if (main.isNotEmpty()) {
                Text(
                    text = main,
                    style = if (mainIsResult) {
                        MaterialTheme.typography.headlineSmall
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    fontWeight = FontWeight.Bold,
                    color = if (mainIsResult) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            lines.forEach { line ->
                ResultLine(line = line)
            }
        }
    }
}

/**
 * Renders "标签：值" / "Label: value" with bold theme-colored value after a *label* colon.
 * Uses one Text + AnnotatedString so label/value share the same baseline (no vertical jump).
 * Does not split on time colons inside `HH:mm:ss` / `UTC+08:00`.
 */
@Composable
fun ResultLine(line: String, modifier: Modifier = Modifier) {
    val split = splitResultLabelValue(line)
    val body = MaterialTheme.typography.bodyLarge
    if (split == null) {
        Text(
            text = line,
            modifier = modifier.fillMaxWidth(),
            style = body,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    } else {
        val (label, colon, value) = split
        val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        val valueColor = MaterialTheme.colorScheme.primary
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = labelColor, fontWeight = FontWeight.Bold)) {
                    append(label)
                    append(colon)
                }
                withStyle(SpanStyle(color = valueColor, fontWeight = FontWeight.Bold)) {
                    append(value)
                }
            },
            modifier = modifier.fillMaxWidth(),
            style = body,
        )
    }
}

/**
 * Only treat as "label / value" when the colon is a real separator:
 * - Fullwidth `：` (Chinese labels), or
 * - ASCII `:` followed by whitespace (e.g. `ISO-8601: 2026…`, `New York: 2026…`).
 * Never split on the colon inside `07:51:42` or `UTC+08:00`.
 */
private fun splitResultLabelValue(line: String): Triple<String, String, String>? {
    val idxCn = line.indexOf('：')
    val idxEn = indexOfAsciiLabelColon(line)
    val idx = when {
        idxCn >= 0 && (idxEn < 0 || idxCn <= idxEn) -> idxCn
        idxEn >= 0 -> idxEn
        else -> return null
    }
    if (idx == 0) return null
    val label = line.substring(0, idx)
    // Pure time / ISO fragment before colon → keep as one value line.
    if (label.all { it.isDigit() || it == '-' || it == ' ' || it == 'T' || it == '+' || it == 'Z' }) {
        return null
    }
    val colon = line.substring(idx, idx + 1)
    val value = line.substring(idx + 1).trimStart()
    if (value.isEmpty()) return null
    return Triple(label, colon, value)
}

/** First ASCII `:` that looks like a label separator (`Label: value`), not `HH:mm`. */
private fun indexOfAsciiLabelColon(line: String): Int {
    var i = 0
    while (i < line.length) {
        if (line[i] == ':') {
            val next = line.getOrNull(i + 1)
            // Require whitespace after colon so times like 07:51 stay intact.
            if (next == null || next.isWhitespace()) return i
        }
        i++
    }
    return -1
}

@Composable
fun DisclaimerBanner(
    health: Boolean,
    finance: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!health && !finance) return
    val text = when {
        health -> stringResource(R.string.disclaimer_health)
        finance -> stringResource(R.string.disclaimer_finance)
        else -> ""
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.padding(top = 8.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
fun ToolContentColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}
