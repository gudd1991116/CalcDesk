package com.qiyuan.calcdesk.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qiyuan.calcdesk.R
import com.qiyuan.calcdesk.data.ToolCatalog
import com.qiyuan.calcdesk.data.ToolCategoryId
import com.qiyuan.calcdesk.data.ToolItem
import com.qiyuan.calcdesk.data.displayDesc
import com.qiyuan.calcdesk.data.displayName
import com.qiyuan.calcdesk.ui.components.InputFieldTextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenTool: (String) -> Unit,
    onAbout: () -> Unit,
    onPrivacy: () -> Unit,
    onContact: () -> Unit,
) {
    val zh = remember { Locale.getDefault().language.startsWith("zh") }
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ToolCategoryId?>(null) }
    var menuOpen by remember { mutableStateOf(false) }
    val categoryTabs = remember {
        listOf<ToolCategoryId?>(null) + ToolCatalog.categories.map { it.id }
    }
    val selectedTab = categoryTabs.indexOf(selectedCategory).coerceAtLeast(0)

    val filtered = remember(query, selectedCategory) {
        ToolCatalog.allTools.filter { tool ->
            val q = query.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                tool.nameEn.lowercase().contains(q) ||
                tool.nameZh.contains(query.trim()) ||
                tool.descEn.lowercase().contains(q) ||
                tool.descZh.contains(query.trim())
            val matchesCat = selectedCategory == null || tool.category == selectedCategory
            matchesQuery && matchesCat
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.about)) },
                            onClick = { menuOpen = false; onAbout() },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.privacy)) },
                            onClick = { menuOpen = false; onPrivacy() },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.contact)) },
                            onClick = { menuOpen = false; onContact() },
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_tools)) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true,
                    textStyle = InputFieldTextStyle(),
                    shape = RoundedCornerShape(20.dp),
                )
                Spacer(Modifier.height(8.dp))
            }
            CategoryTabs(
                tabs = categoryTabs,
                selectedIndex = selectedTab,
                zh = zh,
                onSelect = { selectedCategory = categoryTabs[it] },
            )
            Spacer(Modifier.height(12.dp))
            AnimatedVisibility(
                visible = filtered.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.weight(1f),
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(filtered, key = { it.id }) { tool ->
                        ToolCard(tool = tool, zh = zh, onClick = { onOpenTool(tool.id) })
                    }
                }
            }
            AnimatedVisibility(visible = filtered.isEmpty(), enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (zh) "没有匹配的工具" else "No matching tools",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryTabs(
    tabs: List<ToolCategoryId?>,
    selectedIndex: Int,
    zh: Boolean,
    onSelect: (Int) -> Unit,
) {
    val allTools = stringResource(R.string.all_tools)
    SecondaryScrollableTabRow(
        selectedTabIndex = selectedIndex,
        modifier = Modifier.fillMaxWidth(),
        edgePadding = 4.dp,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        },
    ) {
        tabs.forEachIndexed { index, categoryId ->
            val selected = index == selectedIndex
            val label = if (categoryId == null) {
                allTools
            } else {
                ToolCatalog.categories.first { it.id == categoryId }.displayName(zh)
            }
            Tab(
                selected = selected,
                onClick = { onSelect(index) },
                text = {
                    Text(
                        text = label,
                        maxLines = 1,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ToolCard(tool: ToolItem, zh: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = tool.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = tool.displayName(zh),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
            )
            Text(
                text = tool.displayDesc(zh),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        }
    }
}
