package com.qiyuan.calcdesk.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.qiyuan.calcdesk.R
import com.qiyuan.calcdesk.ui.components.GhostButton
import com.qiyuan.calcdesk.ui.components.InputFieldTextStyle
import com.qiyuan.calcdesk.ui.components.NumberField
import com.qiyuan.calcdesk.ui.components.ResultCard
import kotlin.math.pow

@Composable
fun UnitToolRouter(toolId: String) {
    when (toolId) {
        "unit-weight" -> UnitWeightTool()
        "unit-length" -> UnitLengthTool()
        "unit-temp" -> UnitTempTool()
        "unit-area" -> UnitAreaTool()
        "unit-volume" -> UnitVolumeTool()
        "unit-speed" -> UnitSpeedTool()
        "unit-storage" -> UnitStorageTool()
        "unit-angle" -> UnitAngleTool()
        else -> Text("Unknown unit tool")
    }
}

@Composable fun UnitWeightTool() = FactorConverterScreen(
    units = listOf(
        "kg" to 1.0, "g" to 0.001, "mg" to 1e-6, "t" to 1000.0,
        "lb" to 0.45359237, "oz" to 0.028349523125, "jin" to 0.5, "liang" to 0.05,
    ),
    defaultFrom = 0,
    defaultTo = 4,
)

@Composable fun UnitLengthTool() = FactorConverterScreen(
    units = listOf(
        "m" to 1.0, "km" to 1000.0, "cm" to 0.01, "mm" to 0.001,
        "mi" to 1609.344, "yd" to 0.9144, "ft" to 0.3048, "in" to 0.0254, "nmi" to 1852.0,
    ),
    defaultFrom = 0,
    defaultTo = 6,
)

@Composable fun UnitAreaTool() = FactorConverterScreen(
    units = listOf(
        "m²" to 1.0, "km²" to 1e6, "ha" to 10000.0, "mu" to 666.6666667,
        "ft²" to 0.09290304, "acre" to 4046.8564224,
    ),
)

@Composable fun UnitVolumeTool() = FactorConverterScreen(
    units = listOf(
        "L" to 1.0, "mL" to 0.001, "m³" to 1000.0,
        "gal" to 3.785411784, "pt" to 0.473176473, "cup" to 0.2365882365,
    ),
    defaultFrom = 0,
    defaultTo = 3,
)

@Composable fun UnitSpeedTool() = FactorConverterScreen(
    units = listOf(
        "km/h" to 1.0, "m/s" to 3.6, "mph" to 1.609344, "knot" to 1.852, "Mach" to 1234.8,
    ),
    defaultFrom = 0,
    defaultTo = 1,
)

@Composable fun UnitStorageTool() = FactorConverterScreen(
    units = listOf(
        "B" to 1.0,
        "KB" to 1024.0,
        "MB" to 1024.0.pow(2),
        "GB" to 1024.0.pow(3),
        "TB" to 1024.0.pow(4),
        "KiB" to 1024.0,
        "MiB" to 1024.0.pow(2),
        "GiB" to 1024.0.pow(3),
    ),
    defaultFrom = 3,
    defaultTo = 2,
)

@Composable fun UnitAngleTool() = FactorConverterScreen(
    units = listOf(
        "deg" to 1.0,
        "rad" to 180.0 / kotlin.math.PI,
        "grad" to 0.9,
        "turn" to 360.0,
    ),
)

@Composable
fun UnitTempTool() = TemperatureConverterScreen()

private fun factorConvert(value: Double, from: Double, to: Double): Double =
    (value * from) / to

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactorConverter(
    units: List<Pair<String, Double>>,
    defaultFrom: Int = 0,
    defaultTo: Int = 1,
) {
    FactorConverterScreen(units, defaultFrom, defaultTo)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FactorConverterScreen(
    units: List<Pair<String, Double>>,
    defaultFrom: Int = 0,
    defaultTo: Int = 1,
) {
    var valueText by remember { mutableStateOf("1") }
    var fromIndex by remember { mutableIntStateOf(defaultFrom.coerceIn(units.indices)) }
    var toIndex by remember { mutableIntStateOf(defaultTo.coerceIn(units.indices)) }

    val result = remember(valueText, fromIndex, toIndex) {
        val v = valueText.toDoubleOrNull() ?: return@remember null
        val fromF = units[fromIndex].second
        val toF = units[toIndex].second
        factorConvert(v, fromF, toF)
    }

    NumberField(valueText, { valueText = it }, label = "Value")
    UnitDropdownRow("From", units, fromIndex) { fromIndex = it }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        GhostButton(stringResource(R.string.swap), onClick = {
            val tmp = fromIndex
            fromIndex = toIndex
            toIndex = tmp
            result?.let { valueText = formatUnit(it) }
        }, modifier = Modifier.fillMaxWidth(0.5f))
    }
    UnitDropdownRow("To", units, toIndex) { toIndex = it }
    result?.let {
        ResultCard(
            main = "${formatUnit(it)} ${units[toIndex].first}",
            lines = listOf("${valueText.ifBlank { "0" }} ${units[fromIndex].first} = ${formatUnit(it)} ${units[toIndex].first}"),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdownRow(
    label: String,
    units: List<Pair<String, Double>>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = units[selected].first,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            textStyle = InputFieldTextStyle(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            units.forEachIndexed { i, (name, _) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = { onSelect(i); expanded = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemperatureConverterScreen() {
    val units = listOf("°C", "°F", "K")
    var valueText by remember { mutableStateOf("") }
    var fromIndex by remember { mutableIntStateOf(0) }
    var toIndex by remember { mutableIntStateOf(1) }

    fun toCelsius(v: Double, from: Int): Double = when (from) {
        0 -> v
        1 -> (v - 32) * 5 / 9
        else -> v - 273.15
    }

    fun fromCelsius(c: Double, to: Int): Double = when (to) {
        0 -> c
        1 -> c * 9 / 5 + 32
        else -> c + 273.15
    }

    val result = remember(valueText, fromIndex, toIndex) {
        val v = valueText.toDoubleOrNull() ?: if (valueText.isBlank()) 0.0 else return@remember null
        fromCelsius(toCelsius(v, fromIndex), toIndex)
    }

    NumberField(valueText, { valueText = it }, label = "Value", placeholder = "0")
    UnitDropdownRow("From", units.map { it to 1.0 }, fromIndex) { fromIndex = it }
    UnitDropdownRow("To", units.map { it to 1.0 }, toIndex) { toIndex = it }
    result?.let {
        val shown = valueText.ifBlank { "0" }
        ResultCard(
            main = "${formatUnit(it)} ${units[toIndex]}",
            lines = listOf("$shown ${units[fromIndex]} → ${formatUnit(it)} ${units[toIndex]}"),
        )
    }
}

private fun formatUnit(v: Double): String =
    if (v == v.toLong().toDouble() && kotlin.math.abs(v) < 1e15) {
        v.toLong().toString()
    } else {
        "%.8g".format(v).trimEnd('0').trimEnd('.')
    }
