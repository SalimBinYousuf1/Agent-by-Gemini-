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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ThemeMode
import com.example.data.model.PersonaType
import com.example.ui.MainViewModel
import com.example.ui.components.AppleButton
import com.example.ui.components.AppleButtonStyle
import com.example.ui.components.AppleGroupDivider
import com.example.ui.components.AppleInsetGroup
import com.example.ui.components.AppleSettingsRow
import com.example.ui.components.AppleSwitchRow
import com.example.ui.components.SegmentedControl
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaSettingsScreen(
    viewModel: MainViewModel,
    onNavigateToPermissions: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val apiKey by viewModel.groqApiKey.collectAsStateWithLifecycle()
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()
    val isTestFiring by viewModel.isTestFiring.collectAsStateWithLifecycle()

    var editingKey by remember { mutableStateOf(false) }
    var keyInput by remember { mutableStateOf("") }
    var showKeyText by remember { mutableStateOf(false) }
    var customPromptInput by remember(settings.customPersonaPrompt) {
        mutableStateOf(settings.customPersonaPrompt)
    }

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Persona & AI",
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
                .padding(bottom = 44.dp)
        ) {
            // Appearance Theme Mode (Apple HIG Segmented Control)
            AppleInsetGroup(
                header = "Appearance",
                footer = "Choose your display theme. Apple-inspired light mode features clean neutrals, while dark mode provides an eye-safe OLED canvas."
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val themeModes = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK)
                    SegmentedControl(
                        items = themeModes,
                        selectedItem = settings.themeMode,
                        onItemSelected = { viewModel.setThemeMode(it) },
                        labelProvider = { it.displayName }
                    )
                }
            }

            // Notification & Feedback Controls
            AppleInsetGroup(
                header = "Alert Delivery & Feedback",
                footer = "Controls how reminders reach you. Alerts appear in the phone's notification bar and optionally in an Apple Dynamic Island-style in-app popup."
            ) {
                AppleSwitchRow(
                    title = "In-App Floating Popup",
                    subtitle = "Display dynamic banner when attention slip is detected",
                    checked = settings.inAppPopupEnabled,
                    onCheckedChange = { viewModel.setInAppPopupEnabled(it) },
                    icon = Icons.Outlined.NotificationsActive,
                    iconBackground = Color(0xFF34C759),
                    testTag = "in_app_popup_toggle"
                )
                AppleGroupDivider()
                AppleSwitchRow(
                    title = "Sound Alert",
                    subtitle = "Play gentle chime on intervention",
                    checked = settings.soundEnabled,
                    onCheckedChange = { viewModel.setSoundEnabled(it) },
                    icon = Icons.Outlined.VolumeUp,
                    iconBackground = Color(0xFF007AFF),
                    testTag = "sound_toggle"
                )
                AppleGroupDivider()
                AppleSwitchRow(
                    title = "Haptic Vibration",
                    subtitle = "Distinct double-pulse haptic tap",
                    checked = settings.hapticEnabled,
                    onCheckedChange = { viewModel.setHapticEnabled(it) },
                    icon = Icons.Outlined.Vibration,
                    iconBackground = Color(0xFF5856D6),
                    testTag = "haptic_toggle"
                )
                AppleGroupDivider()
                AppleSwitchRow(
                    title = "System Screen Overlay",
                    subtitle = if (permissions.isOverlayGranted) "Active for high severity events" else "Requires 'Display over other apps' permission",
                    checked = settings.isOverlayEnabled && permissions.isOverlayGranted,
                    onCheckedChange = { enabled ->
                        if (enabled && !permissions.isOverlayGranted) {
                            onNavigateToPermissions()
                        } else {
                            viewModel.setOverlayEnabled(enabled)
                        }
                    },
                    icon = Icons.Outlined.Palette,
                    iconBackground = Color(0xFFFF9500),
                    testTag = "overlay_toggle"
                )
            }

            // Persona Tone Selector
            AppleInsetGroup(
                header = "Accountability Persona",
                footer = when (settings.personaType) {
                    PersonaType.SUPPORTIVE_BROTHER ->
                        "A caring, observant brother. Grounded and honest, wanting what is best for you without sounding preachy."
                    PersonaType.STERN_MENTOR ->
                        "A disciplined, direct mentor. Stoic and firm, calling out avoidance and reminding you of your focus."
                    PersonaType.GENTLE_REMINDER ->
                        "A calm mirror. Quiet, reflective, and minimal. A breath to check if you are being present."
                    PersonaType.CUSTOM ->
                        "Write custom instructions to define how Salim reflects and speaks to you."
                }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val personaList = listOf(
                        PersonaType.SUPPORTIVE_BROTHER,
                        PersonaType.STERN_MENTOR,
                        PersonaType.GENTLE_REMINDER,
                        PersonaType.CUSTOM
                    )

                    SegmentedControl(
                        items = personaList,
                        selectedItem = settings.personaType,
                        onItemSelected = { viewModel.setPersona(it) },
                        labelProvider = { it.displayName.substringBefore(" ") }
                    )

                    if (settings.personaType == PersonaType.CUSTOM) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = customPromptInput,
                            onValueChange = {
                                customPromptInput = it
                                viewModel.setCustomPersonaPrompt(it)
                            },
                            label = { Text("Custom Persona Guidance") },
                            placeholder = { Text("e.g. Speak like Marcus Aurelius with stoic brevity") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                }
            }

            // Groq API Key Management
            AppleInsetGroup(
                header = "Groq Cloud AI Engine",
                footer = "Stored securely in EncryptedSharedPreferences. Raw screen text is never transmitted — only high-level metadata (app category, duration) is sent for reflection generation."
            ) {
                val hasKey = !apiKey.isNullOrBlank()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (hasKey) Color(0xFF34C759) else Color(0xFF8E8E93)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasKey) Icons.Outlined.CheckCircle else Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Groq API Key",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (hasKey) "Key active (encrypted storage)" else "Using offline deterministic reflections",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (hasKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                AppleGroupDivider()

                if (editingKey || !hasKey) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = keyInput,
                            onValueChange = { keyInput = it },
                            label = { Text("Enter Groq API Key (gsk_...)") },
                            singleLine = true,
                            visualTransformation = if (showKeyText) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { showKeyText = !showKeyText }) {
                                    Icon(
                                        imageVector = if (showKeyText) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = "Toggle key visibility"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("api_key_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (hasKey) {
                                TextButton(onClick = {
                                    editingKey = false
                                    keyInput = ""
                                }) {
                                    Text("Cancel")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            AppleButton(
                                onClick = {
                                    if (keyInput.isNotBlank()) {
                                        viewModel.saveGroqApiKey(keyInput)
                                        keyInput = ""
                                        editingKey = false
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Groq API Key saved successfully!")
                                        }
                                    }
                                },
                                text = "Save Key",
                                style = AppleButtonStyle.PRIMARY_FILLED,
                                enabled = keyInput.isNotBlank(),
                                testTag = "save_api_key_button"
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        AppleButton(
                            onClick = {
                                editingKey = true
                                keyInput = ""
                            },
                            text = "Edit Key",
                            style = AppleButtonStyle.SECONDARY_TINTED,
                            testTag = "edit_api_key_button"
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        TextButton(
                            onClick = {
                                viewModel.deleteGroqApiKey()
                                scope.launch {
                                    snackbarHostState.showSnackbar("API Key removed. Switched to offline reflections.")
                                }
                            },
                            modifier = Modifier.testTag("delete_api_key_button")
                        ) {
                            Text(
                                text = "Remove Key",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Groq Model Selection
            AppleInsetGroup(
                header = "Inference Model",
                footer = "Select your preferred neural reasoning model hosted on Groq LPU inference."
            ) {
                val models = listOf("llama-3.3-70b-versatile", ".openai/gpt-oss-120b")
                Column(modifier = Modifier.padding(16.dp)) {
                    SegmentedControl(
                        items = models,
                        selectedItem = settings.groqModel,
                        onItemSelected = { viewModel.setGroqModel(it) },
                        labelProvider = {
                            if (it.contains("llama")) "Llama 3.3 (70B)" else "GPT OSS (120B)"
                        }
                    )
                }
            }

            // Immediate Test & Preview
            AppleInsetGroup(
                header = "Persona Verification",
                footer = "Fires a live test reflection using your active persona settings. Both a system notification and an in-app popup will appear."
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
                            text = "Delivers notification & popup",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AppleButton(
                        onClick = {
                            viewModel.fireTestAlert()
                            scope.launch {
                                snackbarHostState.showSnackbar("Test alert dispatched!")
                            }
                        },
                        text = if (isTestFiring) "Generating..." else "Send Test",
                        icon = Icons.Outlined.Notifications,
                        style = AppleButtonStyle.GENTLE_GRADIENT,
                        enabled = !isTestFiring,
                        testTag = "test_persona_alert_button"
                    )
                }
            }
        }
    }
}
