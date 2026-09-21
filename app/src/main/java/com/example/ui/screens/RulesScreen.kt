package com.example.ui.screens

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.KeywordEntity
import com.example.data.local.entity.RuleAppEntity
import com.example.ui.MainViewModel
import com.example.ui.components.AppleGroupDivider
import com.example.ui.components.AppleInsetGroup
import com.example.ui.components.AppleSettingsRow
import com.example.ui.components.SegmentedControl
import com.example.ui.theme.SeverityHigh
import com.example.ui.theme.SeverityMedium
import com.example.ui.theme.SeverityLow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val apps by viewModel.monitoredApps.collectAsStateWithLifecycle()
    val keywords by viewModel.keywords.collectAsStateWithLifecycle()

    var showAddAppSheet by remember { mutableStateOf(false) }
    var showAddKeywordSheet by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Rules & Limits",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(bottom = 40.dp)
        ) {
            // Session Duration Threshold
            AppleInsetGroup(
                header = "Continuous Session Limit",
                footer = "Salim will trigger an accountability reflection when you spend this amount of uninterrupted time in any monitored app."
            ) {
                val thresholds = listOf(15, 20, 30, 45, 60)
                Column(modifier = Modifier.padding(16.dp)) {
                    SegmentedControl(
                        items = thresholds,
                        selectedItem = settings.sessionThresholdMinutes,
                        onItemSelected = { viewModel.setSessionThreshold(it) },
                        labelProvider = { "${it}m" }
                    )
                }
            }

            // Late-Night Usage Window
            AppleInsetGroup(
                header = "Late-Night Usage Window",
                footer = "Triggers high-priority interventions if flagged recreational or social apps are active during late hours."
            ) {
                val windowOptions = listOf(
                    Pair(22, 5) to "10 PM - 5 AM",
                    Pair(23, 5) to "11 PM - 5 AM",
                    Pair(0, 6) to "12 AM - 6 AM"
                )
                val currentPair = Pair(settings.lateNightStartHour, settings.lateNightEndHour)

                Column(modifier = Modifier.padding(16.dp)) {
                    SegmentedControl(
                        items = windowOptions,
                        selectedItem = windowOptions.find { it.first == currentPair } ?: windowOptions[1],
                        onItemSelected = { (pair, _) ->
                            viewModel.setLateNightWindow(pair.first, pair.second)
                        },
                        labelProvider = { it.second }
                    )
                }
            }

            // Notification Cooldown
            AppleInsetGroup(
                header = "Notification Cooldown",
                footer = "Minimum time delay before the same trigger type can alert you again, preventing notification fatigue."
            ) {
                val cooldowns = listOf(3, 5, 10, 15)
                Column(modifier = Modifier.padding(16.dp)) {
                    SegmentedControl(
                        items = cooldowns,
                        selectedItem = settings.cooldownMinutes,
                        onItemSelected = { viewModel.setCooldownMinutes(it) },
                        labelProvider = { "${it}m" }
                    )
                }
            }

            // Monitored Applications Blocklist
            AppleInsetGroup(
                header = "Monitored Applications (${apps.size})",
                footer = "Apps evaluated for continuous usage thresholds and late-night boundaries."
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Monitored Apps",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = { showAddAppSheet = true },
                        modifier = Modifier.testTag("add_app_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add App")
                    }
                }

                if (apps.isNotEmpty()) {
                    AppleGroupDivider()
                    apps.forEachIndexed { index, app ->
                        AppRuleRow(
                            app = app,
                            onToggle = { viewModel.toggleMonitoredApp(app) },
                            onDelete = { viewModel.removeMonitoredApp(app.packageName) }
                        )
                        if (index < apps.lastIndex) {
                            AppleGroupDivider()
                        }
                    }
                }
            }

            // Monitored Keywords
            AppleInsetGroup(
                header = "Monitored Keywords (${keywords.size})",
                footer = "Keywords detected via accessibility text inspection (e.g. gambling, adult, endless feed)."
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Flagged Terms",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = { showAddKeywordSheet = true },
                        modifier = Modifier.testTag("add_keyword_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Keyword")
                    }
                }

                if (keywords.isNotEmpty()) {
                    AppleGroupDivider()
                    keywords.forEachIndexed { index, kw ->
                        KeywordRuleRow(
                            keyword = kw,
                            onToggle = { viewModel.toggleKeyword(kw) },
                            onDelete = { viewModel.removeKeyword(kw.id) }
                        )
                        if (index < keywords.lastIndex) {
                            AppleGroupDivider()
                        }
                    }
                }
            }
        }
    }

    // ModalBottomSheet for Adding an App
    if (showAddAppSheet) {
        AddAppBottomSheet(
            onDismiss = { showAddAppSheet = false },
            onAddApp = { pkg, name, cat ->
                viewModel.addMonitoredApp(pkg, name, cat)
                showAddAppSheet = false
            }
        )
    }

    // ModalBottomSheet for Adding a Keyword
    if (showAddKeywordSheet) {
        AddKeywordBottomSheet(
            onDismiss = { showAddKeywordSheet = false },
            onAddKeyword = { kw, cat, sev ->
                viewModel.addKeyword(kw, cat, sev)
                showAddKeywordSheet = false
            }
        )
    }
}

@Composable
private fun AppRuleRow(
    app: RuleAppEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = app.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = " • ${app.packageName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "Delete app rule",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = app.isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}

@Composable
private fun KeywordRuleRow(
    keyword: KeywordEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val severityColor = when (keyword.severity) {
        "HIGH" -> SeverityHigh
        "MEDIUM" -> SeverityMedium
        else -> SeverityLow
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = keyword.keyword,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = keyword.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(severityColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = keyword.severity,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = severityColor
                    )
                }
            }
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = "Delete keyword",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = keyword.isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAppBottomSheet(
    onDismiss: () -> Unit,
    onAddApp: (packageName: String, appName: String, category: String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var customPackage by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("social") }

    // Load installed launchable apps
    val installedApps = remember {
        val pm = context.packageManager
        val mainIntent = android.content.Intent(android.content.Intent.ACTION_MAIN, null).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        }
        val resolved = pm.queryIntentActivities(mainIntent, 0)
        resolved.mapNotNull { resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == context.packageName) null
            else {
                val label = resolveInfo.loadLabel(pm).toString()
                pkg to label
            }
        }.distinctBy { it.first }.sortedBy { it.second }
    }

    val filteredApps = installedApps.filter {
        it.second.contains(searchQuery, ignoreCase = true) ||
                it.first.contains(searchQuery, ignoreCase = true)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "Add Monitored Application",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Category selector
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            val categories = listOf("social", "entertainment", "adult", "gambling", "gaming")
            SegmentedControl(
                items = categories,
                selectedItem = selectedCategory,
                onItemSelected = { selectedCategory = it },
                labelProvider = { it.replaceFirstChar { c -> c.uppercase() } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search installed apps
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search installed applications...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
            ) {
                items(filteredApps) { (pkg, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAddApp(pkg, label, selectedCategory)
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = pkg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }

                if (filteredApps.isEmpty()) {
                    item {
                        Text(
                            text = "No matching installed app found",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Or manually enter package name
            Text(
                text = "Or enter custom package manually:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customPackage,
                onValueChange = { customPackage = it },
                placeholder = { Text("com.example.app") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customName,
                onValueChange = { customName = it },
                placeholder = { Text("App display label") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (customPackage.isNotBlank()) {
                        onAddApp(
                            customPackage.trim(),
                            customName.trim().ifBlank { customPackage.trim() },
                            selectedCategory
                        )
                    }
                },
                enabled = customPackage.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Custom Package")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddKeywordBottomSheet(
    onDismiss: () -> Unit,
    onAddKeyword: (keyword: String, category: String, severity: String) -> Unit
) {
    var keywordText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("distraction") }
    var selectedSeverity by remember { mutableStateOf("MEDIUM") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "Add Monitored Keyword",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = keywordText,
                onValueChange = { keywordText = it },
                placeholder = { Text("Keyword or phrase (case-insensitive)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Category",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            val categories = listOf("distraction", "gambling", "adult", "custom")
            SegmentedControl(
                items = categories,
                selectedItem = selectedCategory,
                onItemSelected = { selectedCategory = it },
                labelProvider = { it.replaceFirstChar { c -> c.uppercase() } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Severity",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            val severities = listOf("LOW", "MEDIUM", "HIGH")
            SegmentedControl(
                items = severities,
                selectedItem = selectedSeverity,
                onItemSelected = { selectedSeverity = it },
                labelProvider = { it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (keywordText.isNotBlank()) {
                        onAddKeyword(keywordText.trim(), selectedCategory, selectedSeverity)
                    }
                },
                enabled = keywordText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Keyword")
            }
        }
    }
}
