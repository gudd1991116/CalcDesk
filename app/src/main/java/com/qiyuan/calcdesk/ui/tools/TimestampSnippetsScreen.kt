package com.qiyuan.calcdesk.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qiyuan.calcdesk.R
import com.qiyuan.calcdesk.data.TimestampSnippets
import com.qiyuan.calcdesk.ui.components.CalcDeskScaffold
import java.util.Locale

@Composable
fun TimestampSnippetsScreen(onBack: () -> Unit) {
    val zh = remember { Locale.getDefault().language.startsWith("zh") }
    val clipboard = LocalClipboardManager.current
    var copiedKey by remember { mutableStateOf<String?>(null) }

    CalcDeskScaffold(
        title = if (zh) "获取当前时间戳" else "Get current timestamp",
        onBack = onBack,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = if (zh) {
                        "以下为各语言获取当前 Unix 时间戳（秒）的常用写法，可单独复制。"
                    } else {
                        "Common snippets to get the current Unix timestamp (seconds). Each block can be copied separately."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
            }
            itemsIndexed(TimestampSnippets.all, key = { _, item -> item.lang }) { _, item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = item.lang,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        item.blocks.forEachIndexed { blockIdx, block ->
                            val key = "${item.lang}#$blockIdx"
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = block.label
                                            ?: if (zh) "代码" else "Code",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    TextButton(onClick = {
                                        clipboard.setText(AnnotatedString(block.code))
                                        copiedKey = key
                                    }) {
                                        Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                                        Text(
                                            text = if (copiedKey == key) {
                                                stringResource(R.string.copied)
                                            } else {
                                                stringResource(R.string.copy)
                                            },
                                            modifier = Modifier.padding(start = 4.dp),
                                        )
                                    }
                                }
                                Text(
                                    text = block.code,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
            item { androidx.compose.foundation.layout.Spacer(Modifier.padding(12.dp)) }
        }
    }
}
