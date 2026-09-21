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
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PersonaType
import com.example.ui.MainViewModel
import com.example.ui.components.AppleGroupDivider
import com.example.ui.components.AppleInsetGroup
import com.example.ui.components.AppleSettingsRow
import com.example.ui.components.AppleSwitchRow
import com.example.ui.components.SegmentedControl
import com.example.util.PermissionUtils

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

    Scaffold(
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
                .padding(bottom = 40.dp)
        ) {
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
                header = "Groq API Integration",
                footer = "Stored securely in EncryptedSharedPreferences. Raw screen text is never transmitted — only high-level metadata (app category, duration) is sent for reflection generation."
            ) {
                val hasKey = !apiKey.isNullOrBlank()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (hasKey) Icons.Outlined.CheckCircle else Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = if (hasKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Groq API Key",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (hasKey) "Key configured (encrypted)" else "No key (using local reflections)",
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
                            Button(
                                onClick = {
                                    if (keyInput.isNotBlank()) {
                                        viewModel.saveGroqApiKey(keyInput)
                                        keyInput = ""
                                        editingKey = false
                                    }
                                },
                                enabled = keyInput.isNotBlank(),
                                modifier = Modifier.testTag("save_api_key_button")
                            ) {
                                Text("Save Key")
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                editingKey = true
                                keyInput = ""
                            },
                            modifier = Modifier.testTag("edit_api_key_button")
                        ) {
                            Text("Edit Key")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        TextButton(
                            onClick = {
                                viewModel.deleteGroqApiKey()
                            },
                            modifier = Modifier.testTag("delete_api_key_button")
                        ) {
                            Text(
                                text = "Delete Key",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Groq Model Selection
            AppleInsetGroup(
                header = "Inference Model",
                footer = "Choose between Llama 3.3 70B and OpenAI GPT OSS 120B on Groq's high-speed inference engine."
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

            // High Priority Screen Overlay
            AppleInsetGroup(
                header = "Intervention Style",
                footer = "When enabled, high-severity triggers display a subtle floating banner in addition to the phone notification."
            ) {
                AppleSwitchRow(
                    title = "Floating Screen Overlay",
                    subtitle = if (permissions.isOverlayGranted) "Overlay alert permission granted" else "Requires 'Display over other apps' permission",
                    checked = settings.isOverlayEnabled && permissions.isOverlayGranted,
                    onCheckedChange = { enabled ->
                        if (enabled && !permissions.isOverlayGranted) {
                            onNavigateToPermissions()
                        } else {
                            viewModel.setOverlayEnabled(enabled)
                        }
                    },
                    testTag = "overlay_toggle"
                )
            }

            // Test Generation Button
            AppleInsetGroup(header = "Verification") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Test Persona Generation",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Deliver immediate reflection alert",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = { viewModel.fireTestAlert() },
                        enabled = !isTestFiring,
                        modifier = Modifier.testTag("test_persona_alert_button")
                    ) {
                        if (isTestFiring) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Test")
                        }
                    }
                }
            }
        }
    }
}
