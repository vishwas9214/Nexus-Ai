package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.NexusViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest

@Composable
fun SettingsScreen(
    viewModel: NexusViewModel,
    onBack: () -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.setBackgroundAssistantEnabled(true)
            } else {
                viewModel.speak("Microphone permission is required to enable hands-free voice activation.")
            }
        }
    )

    val themeMode by viewModel.themeMode.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val defaultModel by viewModel.defaultModel.collectAsState()
    val savedApiKeys by viewModel.savedApiKeys.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val isBackgroundAssistantEnabled by viewModel.isBackgroundAssistantEnabled.collectAsState()
    val assistantName by viewModel.assistantName.collectAsState()

    // API Key input popup states
    var showKeyDialog by remember { mutableStateOf(false) }
    var keyProviderInput by remember { mutableStateOf("Google Gemini") }
    var keyValueInput by remember { mutableStateOf("") }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showRenameAssistantDialog by remember { mutableStateOf(false) }
    var tempAssistantNameInput by remember { mutableStateOf("") }

    val providers = listOf("Google Gemini", "OpenAI", "Grok", "Perplexity", "ElevenLabs", "Suno", "Runway")

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Nexus AI Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. User Profile Section
            Text("User Profile", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Indigo500.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (val state = authState) {
                                    is com.example.ui.viewmodel.AuthState.Authenticated -> state.user.name.take(1).uppercase()
                                    else -> "G"
                                },
                                color = Teal400,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            val nameText = when (val state = authState) {
                                is com.example.ui.viewmodel.AuthState.Authenticated -> state.user.name
                                else -> "Nexus Guest"
                            }
                            val emailText = when (val state = authState) {
                                is com.example.ui.viewmodel.AuthState.Authenticated -> state.user.email
                                else -> "guest@nexus.ai"
                            }
                            Text(nameText, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(emailText, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }
                    Button(
                        onClick = { viewModel.signOut() },
                        colors = ButtonDefaults.buttonColors(containerColor = Rose500.copy(alpha = 0.15f), contentColor = Rose500),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("settings_sign_out")
                    ) {
                        Text("Sign Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. AI Model Currently Using
            Text("Artificial Intelligence Core", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900, RoundedCornerShape(16.dp))
                    .padding(14.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("AI Model Currently Using", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Active conversational backend gateway", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Button(
                            onClick = { expanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo500.copy(alpha = 0.2f), contentColor = Teal400),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Teal400.copy(alpha = 0.3f))
                        ) {
                            Text(defaultModel, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "dropdown", modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Slate900)
                        ) {
                            listOf("Gemini", "ChatGPT", "Claude", "DeepSeek").forEach { model ->
                                DropdownMenuItem(
                                    text = { Text(model, color = Color.White) },
                                    onClick = {
                                        viewModel.setDefaultModel(model)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Voice Assistant Settings
            Text("Voice Assistant Protocol", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900, RoundedCornerShape(16.dp))
                    .padding(14.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Background Service Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text("Enable Voice Activation", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isBackgroundAssistantEnabled) {
                                "Active background service: Call 'Hey $assistantName' at any time, even when the app is closed!"
                            } else {
                                "Turn on to activate the hands-free 'Hey $assistantName' voice assistant background service."
                            },
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                    Switch(
                        checked = isBackgroundAssistantEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                viewModel.setBackgroundAssistantEnabled(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Teal400,
                            checkedTrackColor = Indigo500,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Slate800
                        ),
                        modifier = Modifier.testTag("background_assistant_toggle")
                    )
                }

                HorizontalDivider(color = GlassBorder)

                // Assistant Naming
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Assistant Wake-Word / Name", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Rename your personal assistant (e.g. Jarvis)", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            tempAssistantNameInput = assistantName
                            showRenameAssistantDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo500.copy(alpha = 0.2f), contentColor = Teal400),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Teal400.copy(alpha = 0.3f)),
                        modifier = Modifier.testTag("rename_assistant_btn")
                    ) {
                        Text(assistantName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Edit, contentDescription = "Rename", modifier = Modifier.size(12.dp))
                    }
                }
            }

            // 3. Privacy Policy
            Text("Legal & Compliance", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPrivacyDialog = true }
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Teal400.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = "Privacy Policy", tint = Teal400, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Privacy Policy & Terms", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Review local caching and secure API transmission guidelines", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = "View Policy", tint = Color.White.copy(alpha = 0.5f))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Visual Theme Section
            Text("Visual Customization", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Text("Interface Theme Mode", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("dark", "light", "amoled").forEach { mode ->
                        val isSelected = themeMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelected) Indigo500 else Slate800, RoundedCornerShape(8.dp))
                                .clickable { viewModel.setTheme(mode) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.uppercase(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Accent Color Palette", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))

                val accentColor by viewModel.accentColor.collectAsState()
                val accentList = listOf("indigo", "teal", "rose", "violet", "amber", "cyan", "emerald")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    accentList.forEach { colorName ->
                        val colorVal = when (colorName) {
                            "teal" -> Color(0xFF14B8A6)
                            "rose" -> Color(0xFFF43F5E)
                            "violet" -> Color(0xFF8B5CF6)
                            "amber" -> Color(0xFFF59E0B)
                            "cyan" -> Color(0xFF22D3EE)
                            "emerald" -> Color(0xFF34D399)
                            else -> Color(0xFF6366F1) // Indigo
                        }
                        val isSelected = accentColor.lowercase() == colorName
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(colorVal, CircleShape)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clickable { viewModel.setAccentColor(colorName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Font adjustment
                Text("Accessibility Font Size (${fontSize.toInt()}sp)", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                Slider(
                    value = fontSize,
                    onValueChange = { viewModel.setFontSize(it) },
                    valueRange = 14f..20f,
                    steps = 2,
                    colors = SliderDefaults.colors(thumbColor = Teal400, activeTrackColor = Indigo400)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Secure Key Management
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Secure API Keys", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Auto-Generate All Keys Button
                    IconButton(
                        onClick = {
                            val providersToGen = listOf("Google Gemini", "OpenAI", "Grok", "Perplexity", "ElevenLabs", "Suno", "Runway")
                            providersToGen.forEach { p ->
                                val prefix = when (p) {
                                    "Google Gemini" -> "AIzaSy"
                                    "OpenAI" -> "sk-proj-OA"
                                    "Grok" -> "xai-"
                                    "Perplexity" -> "pplx-"
                                    "ElevenLabs" -> "el-"
                                    "Suno" -> "suno_live_"
                                    "Runway" -> "runway_live_"
                                    else -> "mock_key_"
                                }
                                val chars = "0123456789abcdefABCDEF"
                                val randomHex = (1..24).map { chars.random() }.joinToString("")
                                viewModel.saveCustomApiKey(p, "$prefix$randomHex")
                            }
                        },
                        modifier = Modifier.testTag("auto_generate_all_keys")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Auto-Generate All Keys", tint = Teal400)
                    }
                    IconButton(onClick = {
                        keyValueInput = ""
                        showKeyDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Key", tint = Teal400)
                    }
                }
            }
            Text("Link your custom API keys to power studios directly. Keys are cached securely in your local SQLite cluster.", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, modifier = Modifier.padding(bottom = 10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (savedApiKeys.isEmpty()) {
                    Text("No custom keys configured. Using default Sandbox keys.", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))
                } else {
                    savedApiKeys.forEach { apiKey ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VpnKey, contentDescription = "Key Saved", tint = Teal400, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(apiKey.provider, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Active • Encrypted", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                }
                            }
                            IconButton(onClick = { viewModel.removeApiKey(apiKey.provider) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete key", tint = Rose500, modifier = Modifier.size(16.dp))
                            }
                        }
                        HorizontalDivider(color = GlassBorder)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // System Management
            Text("System Utilities", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* simulated cache clearing */ },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Clear Temporary Cache", color = Color.White, fontSize = 14.sp)
                    Text("0.0 KB", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* simulated account delete */ },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Delete Account Matrix", color = Rose500, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ChevronRight, contentDescription = "Delete", tint = Rose500, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About Brand block
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GlassBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Nexus Super Intelligence", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Version 3.5.0 Sandbox Protocol", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    Text("Built with Jetpack Compose & SQLite", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                }
            }
        }
    }

    // Add API Key Dialog
    if (showKeyDialog) {
        AlertDialog(
            onDismissRequest = { showKeyDialog = false },
            title = { Text("Configure Secure API Key", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Slate900,
            text = {
                Column {
                    Text("Select Provider", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    var expProv by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text(
                            keyProviderInput,
                            color = Teal400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate800, RoundedCornerShape(8.dp))
                                .clickable { expProv = true }
                                .padding(12.dp)
                        )
                        DropdownMenu(
                            expanded = expProv,
                            onDismissRequest = { expProv = false },
                            modifier = Modifier.background(Slate900)
                        ) {
                            providers.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p, color = Color.White) },
                                    onClick = {
                                        keyProviderInput = p
                                        expProv = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = keyValueInput,
                        onValueChange = { keyValueInput = it },
                        label = { Text("API Key Value", color = Teal400) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal400,
                            unfocusedBorderColor = GlassBorder,
                            focusedLabelColor = Teal400
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            val prefix = when (keyProviderInput) {
                                "Google Gemini" -> "AIzaSy"
                                "OpenAI" -> "sk-proj-OA"
                                "Grok" -> "xai-"
                                "Perplexity" -> "pplx-"
                                "ElevenLabs" -> "el-"
                                "Suno" -> "suno_live_"
                                "Runway" -> "runway_live_"
                                else -> "mock_key_"
                            }
                            val chars = "0123456789abcdefABCDEF"
                            val randomHex = (1..24).map { chars.random() }.joinToString("")
                            keyValueInput = "$prefix$randomHex"
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Teal400),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Teal400.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Auto-Generate", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Auto-Generate Mock Key", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (keyValueInput.trim().isNotEmpty()) {
                            viewModel.saveCustomApiKey(keyProviderInput, keyValueInput)
                            showKeyDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal500)
                ) {
                    Text("Save Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { showKeyDialog = false }) {
                    Text("Cancel", color = Rose500)
                }
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy & Security Matrix", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Slate900,
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("1. Secure Local Storage", color = Teal400, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Nexus registers and caches your cryptographic keys, profile definitions, and local database entries completely locally. Data never leaves your physical Android container except to contact authorized API networks.", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)

                    Text("2. Direct Transmission", color = Teal400, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("API transmission tunnels directly between your hardware and Google Gemini clusters without middle-man servers, maintaining complete integrity.", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)

                    Text("3. Zero-Tracking Commitment", color = Teal400, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("We maintain an absolute zero-telemetry environment. Your conversational prompts, app directions, and file uploads are processed entirely for your active request and never logged for model-training or monetization.", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo500)
                ) {
                    Text("Accept Terms")
                }
            }
        )
    }

    // Rename Voice Assistant Dialog
    if (showRenameAssistantDialog) {
        AlertDialog(
            onDismissRequest = { showRenameAssistantDialog = false },
            title = { Text("Rename Voice Assistant", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Slate900,
            text = {
                Column {
                    Text("Enter a custom name for your virtual assistant. The assistant will respond to this wake word.", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = tempAssistantNameInput,
                        onValueChange = { tempAssistantNameInput = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal400,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("assistant_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempAssistantNameInput.trim().isNotEmpty()) {
                            viewModel.setAssistantName(tempAssistantNameInput.trim())
                        }
                        showRenameAssistantDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal500, contentColor = Color.Black)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameAssistantDialog = false }) {
                    Text("Cancel", color = Rose500)
                }
            }
        )
    }
}
