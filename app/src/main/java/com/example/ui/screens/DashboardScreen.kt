package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.AppleButton
import com.example.ui.components.AppleButtonStyle
import com.example.ui.components.AppleGroupDivider
import com.example.ui.components.AppleInsetGroup
import com.example.ui.components.AppleSettingsRow
import com.example.ui.components.AppleSwitch
import com.example.ui.components.AppleSwitchRow
import com.example.ui.components.gentleMovingGradient
import com.example.ui.theme.AppleGreenDark
import com.example.ui.theme.AppleGreenLight
import com.example.ui.theme.SeverityHigh
import com.example.ui.theme.SeverityLow
import com.example.ui.theme.SeverityMedium
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToPermissions: () -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToPersona: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()
    val currentContext by viewModel.currentScreenContext.collectAsStateWithLifecycle()
    val todayCount by viewModel.todayCount.collectAsStateWithLifecycle()
    val weekCount by viewModel.weekCount.collectAsStateWithLifecycle()
    val categoryStats by viewModel.categoryStats.collectAsStateWithLifecycle()
    val isTestFiring by viewModel.isTestFiring.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isDark = MaterialTheme.colorScheme.background == Color.Black

    // Notification permission launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshPermissions(context)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp)
                    ) {
                        Text(
                            text = "salim",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                        // Live aura dot
                        if (settings.isMonitoringEnabled) {
                            val pulse = rememberInfiniteTransition(label = "pulse")
                            val scale by pulse.animateFloat(
                                initialValue = 0.85f,
                                targetValue = 1.25f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1200),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )
                            Box(
                                modifier = Modifier
                                    .scale(scale)
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) AppleGreenDark else AppleGreenLight)
                            )
                        }
                    }
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
                .padding(bottom = 36.dp)
        ) {
            // Apple Moving Gradient Hero Status Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .gentleMovingGradient(isDark = isDark, alpha = if (isDark) 0.35f else 0.22f)
                    .border(
                        width = 0.8.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (settings.isMonitoringEnabled) "ACCOUNTABILITY ACTIVE" else "STANDBY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = if (settings.isMonitoringEnabled) {
                                    if (isDark) AppleGreenDark else AppleGreenLight
                                } else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (settings.isMonitoringEnabled) "Guarding your attention" else "Monitoring paused",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        AppleSwitch(
                            checked = settings.isMonitoringEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.toggleMonitoring(context, enabled)
                            },
                            testTag = "master_monitoring_switch"
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Foreground Session Tracker
                    val appPackage = currentContext?.packageName?.ifBlank { "Home / Idle" } ?: "Home / Idle"
                    val appDisplay = appPackage.substringAfterLast('.')
                    val seconds = currentContext?.sessionDurationSeconds ?: 0L
                    val minutes = seconds / 60
                    val remainderSec = seconds % 60
                    val formattedDuration = String.format("%02d:%02d", minutes, remainderSec)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.HourglassEmpty,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Current App",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = appDisplay,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formattedDuration,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Limit: ${settings.sessionThresholdMinutes}m",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Missing Permissions Alert Banner (if needed)
            val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true

            if (!hasNotificationPermission || !permissions.allCoreGranted) {
                AppleInsetGroup(header = "Action Needed") {
                    if (!hasNotificationPermission) {
                        AppleSettingsRow(
                            title = "Enable Notifications",
                            subtitle = "Allow Salim to post alerts to phone notification bar",
                            icon = Icons.Outlined.NotificationsActive,
                            iconBackground = Color(0xFFFF9500),
                            trailingText = "Allow",
                            showChevron = true,
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        )
                    }
                    if (!permissions.allCoreGranted) {
                        if (!hasNotificationPermission) AppleGroupDivider()
                        AppleSettingsRow(
                            title = "Accessibility & Usage Permissions",
                            subtitle = "Required for automatic foreground app tracking",
                            icon = Icons.Outlined.Security,
                            iconBackground = Color(0xFF007AFF),
                            trailingText = "Review",
                            showChevron = true,
                            onClick = onNavigateToPermissions
                        )
                    }
                }
            }

            // Interactive Event Simulator & Live Diagnostics (Explicitly Requested Feature!)
            AppleInsetGroup(
                header = "Interactive Event Simulator",
                footer = "Test how Salim immediately catches distractions: sends an alert to your phone's notification bar and drops down the on-screen popup."
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Instant 1-Tap Trigger Tests",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppleButton(
                            onClick = { viewModel.fireTestAlert() },
                            text = if (isTestFiring) "Firing..." else "Send Test Alert",
                            icon = Icons.Outlined.Notifications,
                            style = AppleButtonStyle.GENTLE_GRADIENT,
                            enabled = !isTestFiring,
                            modifier = Modifier.weight(1f),
                            testTag = "trigger_test_alert_button"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Simulate Specific Scenarios:",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Scenario Simulation Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppleButton(
                            onClick = {
                                viewModel.simulateEvent(
                                    packageName = "com.instagram.android",
                                    appName = "Instagram",
                                    durationMinutes = 25,
                                    textSnippet = "Doomscrolling Reels"
                                )
                                scope.launch {
                                    snackbarHostState.showSnackbar("Simulated 25m Instagram session!")
                                }
                            },
                            text = "Instagram (25m)",
                            style = AppleButtonStyle.SECONDARY_TINTED,
                            modifier = Modifier.weight(1f)
                        )

                        AppleButton(
                            onClick = {
                                viewModel.simulateEvent(
                                    packageName = "com.zhiliaoapp.musically",
                                    appName = "TikTok",
                                    durationMinutes = 35,
                                    textSnippet = "Late Night FYP"
                                )
                                scope.launch {
                                    snackbarHostState.showSnackbar("Simulated TikTok session!")
                                }
                            },
                            text = "TikTok (Late Night)",
                            style = AppleButtonStyle.SECONDARY_TINTED,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppleButton(
                            onClick = {
                                viewModel.simulateEvent(
                                    packageName = "com.android.chrome",
                                    appName = "Chrome",
                                    durationMinutes = 5,
                                    textSnippet = "Online casino slots jackpot"
                                )
                                scope.launch {
                                    snackbarHostState.showSnackbar("Simulated Casino keyword match!")
                                }
                            },
                            text = "Flagged Keyword",
                            style = AppleButtonStyle.SECONDARY_TINTED,
                            modifier = Modifier.weight(1f)
                        )

                        AppleButton(
                            onClick = {
                                viewModel.simulateEvent(
                                    packageName = "com.google.android.youtube",
                                    appName = "YouTube",
                                    durationMinutes = 40,
                                    textSnippet = "YouTube Shorts endless loop"
                                )
                                scope.launch {
                                    snackbarHostState.showSnackbar("Simulated YouTube Shorts session!")
                                }
                            },
                            text = "YouTube Shorts",
                            style = AppleButtonStyle.SECONDARY_TINTED,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 1-Tap Rule Preset Packs
            AppleInsetGroup(
                header = "Quick Presets",
                footer = "Activate tailored boundary presets in one tap to protect focus."
            ) {
                AppleSettingsRow(
                    title = "Digital Detox",
                    subtitle = "Flag Instagram, TikTok, YouTube, Reddit, X (15m limit)",
                    icon = Icons.Outlined.Bolt,
                    iconBackground = Color(0xFF34C759),
                    trailingText = "Apply",
                    onClick = {
                        viewModel.applyPresetPack("digital_detox")
                        scope.launch {
                            snackbarHostState.showSnackbar("Applied Digital Detox pack!")
                        }
                    }
                )
                AppleGroupDivider()
                AppleSettingsRow(
                    title = "Late Night Guard",
                    subtitle = "Strict 11 PM – 5 AM alert window with rapid cooldown",
                    icon = Icons.Outlined.Timer,
                    iconBackground = Color(0xFF5856D6),
                    trailingText = "Apply",
                    onClick = {
                        viewModel.applyPresetPack("late_night")
                        scope.launch {
                            snackbarHostState.showSnackbar("Applied Late Night Guard pack!")
                        }
                    }
                )
                AppleGroupDivider()
                AppleSettingsRow(
                    title = "Deep Work Mode",
                    subtitle = "10m session cap, blocks endless feeds and reels",
                    icon = Icons.Outlined.HourglassEmpty,
                    iconBackground = Color(0xFF007AFF),
                    trailingText = "Apply",
                    onClick = {
                        viewModel.applyPresetPack("focus_work")
                        scope.launch {
                            snackbarHostState.showSnackbar("Applied Deep Work pack!")
                        }
                    }
                )
                AppleGroupDivider()
                AppleSettingsRow(
                    title = "Harm Reduction",
                    subtitle = "High-severity protection against gambling & adult content",
                    icon = Icons.Outlined.Shield,
                    iconBackground = Color(0xFFFF3B30),
                    trailingText = "Apply",
                    onClick = {
                        viewModel.applyPresetPack("harm_reduction")
                        scope.launch {
                            snackbarHostState.showSnackbar("Applied Harm Reduction pack!")
                        }
                    }
                )
            }

            // Accountability Summary
            AppleInsetGroup(header = "Accountability Metrics") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatSummaryItem(
                        label = "Fired Today",
                        value = todayCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    StatSummaryItem(
                        label = "Past 7 Days",
                        value = weekCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (categoryStats.isNotEmpty()) {
                    AppleGroupDivider()
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Top Intervened Categories",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        categoryStats.take(4).forEach { stat ->
                            val fraction = if (weekCount > 0) (stat.count.toFloat() / weekCount) else 0f
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stat.category.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${stat.count} alerts",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { fraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Fast Navigation
            AppleInsetGroup(header = "Management") {
                AppleSettingsRow(
                    title = "Rules & App Boundaries",
                    subtitle = "${settings.sessionThresholdMinutes}m session limit, monitored apps & keywords",
                    icon = Icons.Outlined.FilterList,
                    iconBackground = Color(0xFF007AFF),
                    showChevron = true,
                    onClick = onNavigateToRules
                )
                AppleGroupDivider()
                AppleSettingsRow(
                    title = "Persona & AI Engine",
                    subtitle = "${settings.personaType.displayName} • ${settings.themeMode.displayName} Mode",
                    icon = Icons.Outlined.Psychology,
                    iconBackground = Color(0xFF5856D6),
                    showChevron = true,
                    onClick = onNavigateToPersona
                )
            }
        }
    }
}

@Composable
private fun StatSummaryItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(14.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum"
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
