package com.qiyuan.calcdesk.ui.tools

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qiyuan.calcdesk.R
import com.qiyuan.calcdesk.data.ToolCatalog
import com.qiyuan.calcdesk.data.ToolCategoryId
import com.qiyuan.calcdesk.data.displayName
import com.qiyuan.calcdesk.ui.components.CalcDeskScaffold
import com.qiyuan.calcdesk.ui.components.DisclaimerBanner
import com.qiyuan.calcdesk.ui.components.ToolContentColumn
import java.util.Locale

@Composable
fun ToolScreen(
    toolId: String,
    onBack: () -> Unit,
    onOpenTimestampSnippets: () -> Unit = {},
    onOpenCountdownHistory: () -> Unit = {},
) {
    val tool = remember(toolId) { ToolCatalog.find(toolId) }
    val zh = remember { Locale.getDefault().language.startsWith("zh") }

    if (tool == null) {
        CalcDeskScaffold(title = toolId, onBack = onBack) { padding ->
            ToolContentColumn(Modifier.padding(padding)) {
                androidx.compose.material3.Text("Unknown tool: $toolId")
            }
        }
        return
    }

    var showHelp by remember { mutableStateOf(false) }
    val showTopHelp = toolId != "timestamp"

    if (showHelp && showTopHelp) {
        ToolHelpDialog(
            toolId = toolId,
            toolName = tool.displayName(zh),
            zh = zh,
            onDismiss = { showHelp = false },
        )
    }

    CalcDeskScaffold(
        title = tool.displayName(zh),
        onBack = onBack,
        actions = {
            if (showTopHelp) {
                IconButton(onClick = { showHelp = true }) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = stringResource(R.string.help),
                    )
                }
            }
            if (toolId == "countdown") {
                IconButton(onClick = onOpenCountdownHistory) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = stringResource(R.string.countdown_history),
                    )
                }
            }
        },
    ) { padding ->
        ToolContentColumn(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            DisclaimerBanner(health = tool.healthDisclaimer, finance = tool.financeDisclaimer)
            when (tool.category) {
                ToolCategoryId.Units -> UnitToolRouter(toolId)
                ToolCategoryId.Datetime -> DateTimeToolRouter(
                    toolId = toolId,
                    onOpenTimestampSnippets = onOpenTimestampSnippets,
                )
                ToolCategoryId.Math -> MathToolRouter(toolId)
                ToolCategoryId.Life -> LifeToolRouter(toolId)
                ToolCategoryId.Dev -> DevToolRouter(toolId)
            }
        }
    }
}
