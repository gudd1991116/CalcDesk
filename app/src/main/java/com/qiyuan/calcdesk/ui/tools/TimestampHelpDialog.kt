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
import com.qiyuan.calcdesk.data.TimestampHelp
import java.util.Locale

/** Inline help block placed below the timestamp result panel. */
@Composable
fun TimestampHelpSection(onOpenSnippets: () -> Unit) {
    val zh = remember { Locale.getDefault().language.startsWith("zh") }
    var detailId by remember { mutableStateOf<String?>(null) }
    val detail = detailId?.let { TimestampHelp.article(it) }

    if (detail != null) {
        AlertDialog(
            onDismissRequest = { detailId = null },
            title = { Text(detail.title(zh)) },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    detail.paragraphs(zh).forEach { p ->
                        Text(text = p, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { detailId = null }) {
                    Text(stringResource(R.string.got_it))
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = if (zh) "说明" else "About",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = if (zh) TimestampHelp.aboutSummaryZh else TimestampHelp.aboutSummaryEn,
            style = MaterialTheme.typography.bodyMedium,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = if (zh) TimestampHelp.originTitleZh else TimestampHelp.originTitleEn,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = if (zh) TimestampHelp.originBodyZh else TimestampHelp.originBodyEn,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = if (zh) TimestampHelp.howToTitleZh else TimestampHelp.howToTitleEn,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = if (zh) TimestampHelp.howToBodyZh else TimestampHelp.howToBodyEn,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        HorizontalDivider(modifier = Modifier.padding(top = 2.dp, bottom = 2.dp))
        Text(
            text = if (zh) "更多说明" else "More topics",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = if (zh) "点击下方链接查看详细介绍：" else "Tap a link for details:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text(
                text = if (zh) "获取当前时间戳（各语言代码）" else "Get current timestamp (code snippets)",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenSnippets)
                    .padding(vertical = 2.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
            )

            TimestampHelp.linkArticles.forEach { article ->
                Text(
                    text = article.title(zh),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { detailId = article.id }
                        .padding(vertical = 2.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    textDecoration = TextDecoration.Underline,
                )
            }
        }
    }
}
