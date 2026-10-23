package com.qiyuan.calcdesk.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Locale

private val DefaultPickerFieldStyle: TextStyle?
    @Composable get() = InputFieldTextStyle()

private val DATE_FMT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("uuuu-M-d").withResolverStyle(ResolverStyle.STRICT)

private val DATE_TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

private val DATE_TIME_PARSERS: List<DateTimeFormatter> = listOf(
    DateTimeFormatter.ofPattern("uuuu-M-d H:mm:ss").withResolverStyle(ResolverStyle.STRICT),
    DateTimeFormatter.ofPattern("uuuu-M-d H:mm").withResolverStyle(ResolverStyle.STRICT),
    DateTimeFormatter.ofPattern("uuuu-M-d'T'H:mm:ss").withResolverStyle(ResolverStyle.STRICT),
    DateTimeFormatter.ofPattern("uuuu-M-d'T'H:mm").withResolverStyle(ResolverStyle.STRICT),
)

private fun isZh(): Boolean = Locale.getDefault().language.startsWith("zh")

private fun tr(zh: String, en: String): String = if (isZh()) zh else en

fun parsePickerDate(text: String): LocalDate? =
    runCatching { LocalDate.parse(text.trim(), DATE_FMT) }.getOrNull()

fun parsePickerDateTime(text: String): LocalDateTime? {
    val s = text.trim()
    return DATE_TIME_PARSERS.firstNotNullOfOrNull { f ->
        runCatching { LocalDateTime.parse(s, f) }.getOrNull()
    } ?: parsePickerDate(s)?.atStartOfDay()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialDate: LocalDate,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onDatePicked: (LocalDate) -> Unit,
) {
    val millis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val dateState = rememberDatePickerState(initialSelectedDateMillis = millis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val selected = dateState.selectedDateMillis ?: return@TextButton
                onDatePicked(
                    Instant.ofEpochMilli(selected).atZone(ZoneOffset.UTC).toLocalDate(),
                )
            }) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(tr("取消", "Cancel")) }
        },
    ) {
        DatePicker(state = dateState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onTimePicked: (hour: Int, minute: Int) -> Unit,
) {
    val timeState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onTimePicked(timeState.hour, timeState.minute) }) {
                Text(tr("确定", "OK"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(tr("取消", "Cancel")) }
        },
        title = { Text(tr("选择时间", "Pick time")) },
        text = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = timeState)
            }
        },
    )
}

/** Date-only field (`yyyy-MM-dd`) with a calendar button. */
@Composable
fun DateFieldWithPicker(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle? = null,
) {
    var showDate by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = textStyle ?: InputFieldTextStyle(),
        shape = RoundedCornerShape(16.dp),
        trailingIcon = {
            IconButton(onClick = { showDate = true }) {
                Icon(
                    Icons.Outlined.EditCalendar,
                    contentDescription = tr("选择日期", "Pick date"),
                )
            }
        },
    )
    if (showDate) {
        AppDatePickerDialog(
            initialDate = parsePickerDate(value) ?: LocalDate.now(),
            confirmLabel = tr("确定", "OK"),
            onDismiss = { showDate = false },
            onDatePicked = { date ->
                onValueChange(date.toString())
                showDate = false
            },
        )
    }
}

/** Date-time field (`yyyy-MM-dd HH:mm:ss`) with calendar → time picker. */
@Composable
fun DateTimeFieldWithPicker(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    trailingExtras: @Composable (() -> Unit)? = null,
) {
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var pendingDate by remember { mutableStateOf<LocalDate?>(null) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = textStyle ?: InputFieldTextStyle(),
        isError = isError,
        supportingText = supportingText,
        shape = RoundedCornerShape(16.dp),
        trailingIcon = {
            androidx.compose.foundation.layout.Row {
                trailingExtras?.invoke()
                IconButton(onClick = { showDate = true }) {
                    Icon(
                        Icons.Outlined.EditCalendar,
                        contentDescription = tr("选择日期时间", "Pick date & time"),
                    )
                }
            }
        },
    )

    if (showDate) {
        val base = parsePickerDateTime(value) ?: LocalDateTime.now()
        AppDatePickerDialog(
            initialDate = base.toLocalDate(),
            confirmLabel = tr("下一步", "Next"),
            onDismiss = { showDate = false },
            onDatePicked = { date ->
                pendingDate = date
                showDate = false
                showTime = true
            },
        )
    }

    if (showTime) {
        val base = parsePickerDateTime(value) ?: LocalDateTime.now()
        AppTimePickerDialog(
            initialHour = base.hour,
            initialMinute = base.minute,
            onDismiss = {
                showTime = false
                pendingDate = null
            },
            onTimePicked = { hour, minute ->
                val date = pendingDate ?: base.toLocalDate()
                val picked = LocalDateTime.of(
                    date.year,
                    date.month,
                    date.dayOfMonth,
                    hour,
                    minute,
                    0,
                )
                onValueChange(picked.format(DATE_TIME_FMT))
                showTime = false
                pendingDate = null
            },
        )
    }
}
