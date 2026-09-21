package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.MainViewModel
import com.example.ui.components.AppleGroupDivider
import com.example.ui.components.AppleInsetGroup
import com.example.ui.components.AppleSettingsRow
import com.example.ui.components.AppleSwitchRow
import com.example.util.PermissionUtils

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

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "salim",
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
                .padding(bottom = 32.dp)
        ) {
            // Master Monitoring Status Card
            AppleInsetGroup(header = "Status") {
                AppleSwitchRow(
                    title = if (settings.isMonitoringEnabled) "Monitoring Active" else "Monitoring Paused",
                    subtitle = if (settings.isMonitoringEnabled) "Boundary detection is running" else "Interventions temporarily stopped",
                    checked = settings.isMonitoringEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.toggleMonitoring(context, enabled)
                    },
                    icon = Icons.Outlined.Shield,
                    iconTint = if (settings.isMonitoringEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    testTag = "monitoring_master_toggle"
                )
            }

            // Permissions Status Banner (shown if not all core permissions granted)
            if (!permissions.allCoreGranted) {
                AppleInsetGroup(
                    header = "Required Setup",
                    footer = "Salim requires Accessibility and Usage Access permissions to monitor foreground usage sessions."
                ) {
                    AppleSettingsRow(
                        title = "Permissions Needed",
                        subtitle = "Tap to grant missing system permissions",
                        icon = Icons.Outlined.WarningAmber,
                        iconTint = MaterialTheme.colorScheme.error,
                        trailingText = "Configure",
                        showChevron = true,
                        onClick = onNavigateToPermissions
                    )
                }
            }

            // Live Focus & Active App Card
            AppleInsetGroup(header = "Current Activity") {
                val appPackage = currentContext?.packageName?.ifBlank { "None active" } ?: "Home / Idle"
                val appDisplay = appPackage.substringAfterLast('.')
                val seconds = currentContext?.sessionDurationSeconds ?: 0L
                val minutes = seconds / 60
                val remainderSec = seconds % 60
                val formattedDuration = String.format("%02d:%02d", minutes, remainderSec)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.HourglassEmpty,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Foreground Focus",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = appDisplay,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formattedDuration,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Session",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Accountability Stats (Today vs This Week)
            AppleInsetGroup(header = "Accountability Summary") {
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
                    Spacer(modifier = Modifier.width(16.dp))
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
                            text = "Top Flagged Categories",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        categoryStats.take(4).forEach { stat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stat.category.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${stat.count} alerts",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Quick Configuration Rows
            AppleInsetGroup(header = "Configuration") {
                AppleSettingsRow(
                    title = "Rules & Boundaries",
                    subtitle = "${settings.sessionThresholdMinutes}m session limit, monitored apps & keywords",
                    icon = Icons.Outlined.Timer,
                    showChevron = true,
                    onClick = onNavigateToRules
                )
                AppleGroupDivider()
                AppleSettingsRow(
                    title = "Persona & AI Engine",
                    subtitle = settings.personaType.displayName,
                    icon = Icons.Outlined.Security,
                    showChevron = true,
                    onClick = onNavigateToPersona
                )
            }

            // Test Diagnostic Alert
            AppleInsetGroup(
                header = "Diagnostics",
                footer = "Triggers a live accountability alert using your configured persona and Groq API (or local reflection fallback)."
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Test Reflection",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Send test notification now",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = { viewModel.fireTestAlert() },
                        enabled = !isTestFiring,
                        modifier = Modifier.testTag("test_alert_button")
                    ) {
                        if (isTestFiring) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Send Test")
                        }
                    }
                }
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
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
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
