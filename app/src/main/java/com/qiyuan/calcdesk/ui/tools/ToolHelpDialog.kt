package com.qiyuan.calcdesk.ui.tools

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.qiyuan.calcdesk.R
import com.qiyuan.calcdesk.data.HelpDetailSection
import com.qiyuan.calcdesk.data.ToolHelp
import com.qiyuan.calcdesk.data.ToolHelpDetails
import com.qiyuan.calcdesk.data.sections
import com.qiyuan.calcdesk.data.summary

@Composable
fun ToolHelpDialog(
    toolId: String,
    toolName: String,
    zh: Boolean,
    onDismiss: () -> Unit,
) {
    val help = ToolHelp.get(toolId)
    val details = ToolHelpDetails.sections(toolId)
    var showDetails by remember { mutableStateOf(false) }

    if (showDetails && details != null) {
        ToolHelpDetailsDialog(
            toolName = toolName,
            zh = zh,
            sections = details,
            onDismiss = { showDetails = false },
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.tool_help, toolName)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = help.summary(zh),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                help.sections(zh).forEach { (title, body) ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(text = body, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (details != null) {
                    HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                    Text(
                        text = stringResource(R.string.help_more_details),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDetails = true }
                            .padding(vertical = 4.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                    )
                    Text(
                        text = stringResource(R.string.help_more_details_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.got_it)) }
        },
    )
}

@Composable
private fun ToolHelpDetailsDialog(
    toolName: String,
    zh: Boolean,
    sections: List<HelpDetailSection>,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.help_details_title, toolName)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                sections.forEach { section ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = section.title(zh),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = section.body(zh),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.got_it)) }
        },
    )
}
