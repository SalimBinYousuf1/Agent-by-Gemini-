package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.AppleGroupDivider
import com.example.ui.components.AppleInsetGroup
import com.example.util.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Request runtime notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshPermissions(context)
    }

    // Refresh permission statuses on resume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshPermissions(context)
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Permissions",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
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
            // Core Permissions
            AppleInsetGroup(
                header = "Required System Access",
                footer = "Salim operates entirely on-device to inspect foreground active apps. Screen content is strictly evaluated against your local rules and never sent to external servers."
            ) {
                // 1. Accessibility Service
                PermissionItemRow(
                    title = "Accessibility Service",
                    description = "Monitors foreground applications and detects flagged recreational loops.",
                    instruction = "Settings → Accessibility → Downloaded / Installed apps → Salim → Turn On",
                    isGranted = permissions.isAccessibilityGranted,
                    icon = Icons.Outlined.Visibility,
                    onAction = {
                        try {
                            context.startActivity(PermissionUtils.getAccessibilitySettingsIntent())
                        } catch (_: Exception) {}
                    },
                    testTag = "btn_grant_accessibility"
                )

                AppleGroupDivider()

                // 2. Usage Stats
                PermissionItemRow(
                    title = "Usage Access",
                    description = "Tracks session durations accurately across app transitions.",
                    instruction = "Settings → Usage Access → Salim → Allow usage tracking",
                    isGranted = permissions.isUsageAccessGranted,
                    icon = Icons.Outlined.QueryStats,
                    onAction = {
                        try {
                            context.startActivity(PermissionUtils.getUsageAccessSettingsIntent(context))
                        } catch (_: Exception) {}
                    },
                    testTag = "btn_grant_usage"
                )

                AppleGroupDivider()

                // 3. Notifications
                PermissionItemRow(
                    title = "Notifications",
                    description = "Delivers proactive accountability reflections and session checks.",
                    instruction = "Allows Salim to send alert notifications",
                    isGranted = permissions.isNotificationsGranted,
                    icon = Icons.Outlined.Notifications,
                    onAction = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            try {
                                context.startActivity(PermissionUtils.getNotificationSettingsIntent(context))
                            } catch (_: Exception) {}
                        }
                    },
                    testTag = "btn_grant_notifications"
                )
            }

            // Optional Overlay
            AppleInsetGroup(
                header = "Optional Permissions",
                footer = "Displays a temporary calm floating banner on top of high-severity flagged apps."
            ) {
                PermissionItemRow(
                    title = "Display Over Other Apps",
                    description = "Optional floating intervention banner that auto-dismisses after 10s.",
                    instruction = "Settings → Display over other apps → Salim → Allow",
                    isGranted = permissions.isOverlayGranted,
                    icon = Icons.Outlined.Layers,
                    onAction = {
                        try {
                            context.startActivity(PermissionUtils.getOverlaySettingsIntent(context))
                        } catch (_: Exception) {}
                    },
                    testTag = "btn_grant_overlay"
                )
            }
        }
    }
}

@Composable
private fun PermissionItemRow(
    title: String,
    description: String,
    instruction: String,
    isGranted: Boolean,
    icon: ImageVector,
    onAction: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGranted) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Outlined.Check else icon,
                    contentDescription = null,
                    tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isGranted) "Enabled" else "Not enabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            if (!isGranted) {
                FilledTonalButton(
                    onClick = onAction,
                    modifier = Modifier.testTag(testTag)
                ) {
                    Text("Grant")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!isGranted) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = instruction,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
