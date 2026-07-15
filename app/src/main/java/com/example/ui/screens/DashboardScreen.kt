package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.GeneratedAsset
import com.example.ui.theme.*
import com.example.ui.viewmodel.NexusViewModel
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    viewModel: NexusViewModel,
    onNavigate: (String) -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val historyAssets by viewModel.allHistoryAssets.collectAsState()

    val context = LocalContext.current

    // Central Chatbox state
    var chatInputText by remember { mutableStateOf("") }
    var selectedToolMode by remember { mutableStateOf("Creative") } // App Building, Voice Generate, Image Generator, Thinking 2.0, Creative
    var showToolMenu by remember { mutableStateOf(false) }
    var isVoiceInputting by remember { mutableStateOf(false) }
    var attachedFileForChat by remember { mutableStateOf<String?>(null) }
    var showAttachedFileDialog by remember { mutableStateOf(false) }
    var showAttachedImageDialog by remember { mutableStateOf(false) }
    var showTopSettingsMenu by remember { mutableStateOf(false) }

    // Nexus AI requested Main Screen elements
    var useCase by remember { mutableStateOf("General Chat") }
    var thinkingLevel by remember { mutableStateOf("Fast") }
    var incognitoMode by remember { mutableStateOf(false) }
    var showUseCaseDropdown by remember { mutableStateOf(false) }
    var showThinkingLevelDropdown by remember { mutableStateOf(false) }

    // Voice Assistant Overlay states
    var isVoiceAssistantActive by remember { mutableStateOf(false) }
    var voiceAssistantStatus by remember { mutableStateOf("Listening... Press 'Record' to speak.") }
    var typedVoiceCommand by remember { mutableStateOf("") }

    val assistantName by viewModel.assistantName.collectAsState()

    val userName = when (val state = authState) {
        is com.example.ui.viewmodel.AuthState.Authenticated -> state.user.name
        else -> "Nexus User"
    }

    val trendingTools = listOf(
        TrendingTool("DeepSeek R1", "Super Reasoning", Icons.Default.Psychology, Indigo400, "DeepSeek"),
        TrendingTool("Claude 3.5", "Creative Writing", Icons.Default.Edit, Teal400, "Claude"),
        TrendingTool("Perplexity", "Web Research", Icons.Default.Search, Indigo400, "Perplexity"),
        TrendingTool("Grok 3", "Witty Assistant", Icons.Default.AutoAwesome, Rose500, "Grok"),
        TrendingTool("ChatGPT 4o", "Multimodal Pro", Icons.Default.Chat, Indigo400, "ChatGPT")
    )

    // Simulated File Chooser Dialog
    if (showAttachedFileDialog) {
        AlertDialog(
            onDismissRequest = { showAttachedFileDialog = false },
            title = { Text("Choose a document to attach", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Slate900,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("quarterly_results.pdf", "app_architecture.docx", "system_logs.csv", "simple_api.py").forEach { f ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate800, RoundedCornerShape(8.dp))
                                .clickable {
                                    attachedFileForChat = f
                                    showAttachedFileDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.InsertDriveFile, contentDescription = "file", tint = Teal400, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(f, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAttachedFileDialog = false }) {
                    Text("Cancel", color = Rose500)
                }
            }
        )
    }

    // Simulated Image Chooser Dialog
    if (showAttachedImageDialog) {
        AlertDialog(
            onDismissRequest = { showAttachedImageDialog = false },
            title = { Text("Choose a photo to upload", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Slate900,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("server_rack.jpg", "ui_dashboard_draft.png", "smart_watch_concept.jpg").forEach { img ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate800, RoundedCornerShape(8.dp))
                                .clickable {
                                    attachedFileForChat = img
                                    showAttachedImageDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "photo", tint = Indigo400, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(img, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAttachedImageDialog = false }) {
                    Text("Cancel", color = Rose500)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .immersiveBackground()
            .verticalScroll(rememberScrollState())
    ) {
        // Subtle background gradients
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Indigo500.copy(alpha = 0.08f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            // Brand & Status Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Elegant modern white logo with gorgeous dynamic gradient border
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Brush.linearGradient(listOf(Indigo500, Teal500, Rose500)), RoundedCornerShape(12.dp))
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Slate950, RoundedCornerShape(10.dp))
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color.White, RoundedCornerShape(2.dp))
                                    .rotate(45f)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Nexus AI",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "PRO ACTIVE",
                            color = Indigo400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
 
                // Clean swipe guide pill
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Settings ⟵ Swipe ⟶ History",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Central Chatbox Card (Nexus AI requested layout)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .border(1.dp, GlassBorder.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.85f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Title Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nexus Central Command",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (incognitoMode) Color.Yellow else Teal400, CircleShape)
                            )
                            Text(
                                text = if (incognitoMode) "Incognito Active" else "Connected",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // 1. Large Prompt Box at the Top where the typed prompt is visible - ENLARGED & VIBRANTLY COLORED
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(Indigo500, Cyan400, Violet500, Rose500)), RoundedCornerShape(18.dp))
                            .padding(1.5.dp), // Gradient border padding
                        colors = CardDefaults.cardColors(containerColor = Slate950.copy(alpha = 0.95f)),
                        shape = RoundedCornerShape(16.5.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            OutlinedTextField(
                                value = chatInputText,
                                onValueChange = { chatInputText = it },
                                placeholder = {
                                    Text(
                                        text = "Type or dictate your prompt here...",
                                        color = Color.White.copy(alpha = 0.45f),
                                        fontSize = 14.sp
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 150.dp, max = 320.dp)
                                    .testTag("large_prompt_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = false
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Inside the prompt box include: ✨ Enhance Prompt and 🕵️ Incognito Chat (Symbol-Only, Compact size)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // ✨ Enhance Prompt Button (Compact symbol form)
                                IconButton(
                                    onClick = {
                                        if (chatInputText.isNotBlank()) {
                                            chatInputText = "Create an elegant, production-grade, and beautifully-styled version of: $chatInputText with detailed explanations, comprehensive error handling, and robust caching."
                                        } else {
                                            chatInputText = "Write an advanced, scalable backend service architecture for handling high-volume concurrent chat payloads using WebSockets."
                                        }
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Indigo500.copy(alpha = 0.25f), CircleShape)
                                        .border(0.5.dp, Teal400.copy(alpha = 0.3f), CircleShape)
                                        .testTag("enhance_prompt_button")
                                ) {
                                    Text("✨", fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // 🕵️ Incognito Toggle Button (Compact symbol form)
                                IconButton(
                                    onClick = { incognitoMode = !incognitoMode },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(if (incognitoMode) Teal400.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f), CircleShape)
                                        .border(0.5.dp, if (incognitoMode) Teal400 else Color.White.copy(alpha = 0.12f), CircleShape)
                                        .testTag("incognito_toggle")
                                ) {
                                    Text("🕵️", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Spacer(modifier = Modifier.height(14.dp))

                    // Show file attachment badge if any
                    if (attachedFileForChat != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Indigo500.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .border(1.dp, Indigo400.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.InsertDriveFile, contentDescription = "Attached File", tint = Teal400, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = attachedFileForChat!!,
                                color = Color.White,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove File",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { attachedFileForChat = null }
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // 3. Bottom Input Bar with Attach, Voice, and Send Blue Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate800.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // 📎 Attach Files button (PDF, DOCX, TXT, Audio, Videos)
                            IconButton(
                                onClick = { showAttachedFileDialog = true },
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.06f), CircleShape)
                                    .size(38.dp)
                                    .testTag("attach_file_button")
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = "Attach Files", tint = Teal400, modifier = Modifier.size(18.dp))
                            }

                            // 🖼️ Attach Image button
                            IconButton(
                                onClick = { showAttachedImageDialog = true },
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.06f), CircleShape)
                                    .size(38.dp)
                                    .testTag("attach_image_button")
                            ) {
                                Icon(Icons.Default.Image, contentDescription = "Attach Image", tint = Indigo400, modifier = Modifier.size(18.dp))
                            }

                            // 🎤 Voice Input button (Triggers Voice Control Panel)
                            IconButton(
                                onClick = { isVoiceAssistantActive = true },
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.06f), CircleShape)
                                    .size(38.dp)
                                    .testTag("voice_input_button")
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = Rose500, modifier = Modifier.size(18.dp))
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "PDF, DOCX, TXT, Images...",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // ➤ Blue Send Button
                        IconButton(
                            onClick = {
                                if (chatInputText.trim().isNotEmpty() || attachedFileForChat != null) {
                                    val prompt = chatInputText
                                    val attachment = attachedFileForChat
                                    chatInputText = ""
                                    attachedFileForChat = null

                                    // Create/Attach file in ViewModel if present
                                    if (attachment != null) {
                                        viewModel.attachFile(attachment, attachment.substringAfterLast("."), "Content description", "1.2 MB")
                                    }

                                    // Set providers and defaults depending on selectors
                                    if (useCase == "Codex (App Building)") {
                                        viewModel.buildApp(prompt, "Web React")
                                        onNavigate("app_builder")
                                    } else if (useCase == "Image") {
                                        viewModel.generateImage(prompt, "Flux Midjourney", "1:1", false)
                                        onNavigate("image_gen")
                                    } else if (useCase == "Video") {
                                        viewModel.generateVideo(prompt, "Kling AI", "High")
                                        onNavigate("video_gen")
                                    } else if (useCase == "Audio") {
                                        viewModel.generateMusic(prompt, "Ambient Electronic", false)
                                        onNavigate("music_gen")
                                    } else {
                                        if (thinkingLevel == "Complex" || thinkingLevel == "Max") {
                                            viewModel.setChatProvider("DeepSeek")
                                        } else {
                                            viewModel.setChatProvider("Gemini")
                                        }
                                        viewModel.sendMessage(prompt)
                                        onNavigate("chat")
                                    }
                                }
                            },
                            enabled = chatInputText.trim().isNotEmpty() || attachedFileForChat != null,
                            modifier = Modifier
                                .background(
                                    if (chatInputText.trim().isNotEmpty() || attachedFileForChat != null) Color(0xFF2563EB) else Color.White.copy(alpha = 0.04f),
                                    CircleShape
                                )
                                .size(40.dp)
                                .testTag("blue_send_button")
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send prompt",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // SEPARATE Use Case and Thinking Level Toggles Section (Small & Seperate as requested)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .border(1.dp, GlassBorder.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Use Case Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Use Case",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box {
                            Button(
                                onClick = { showUseCaseDropdown = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Indigo500.copy(alpha = 0.2f), contentColor = Teal400),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, Teal400.copy(alpha = 0.3f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                val useCaseIcon = when (useCase) {
                                    "General Chat" -> "💬"
                                    "Codex (App Building)" -> "💻"
                                    "Creativity" -> "🎨"
                                    "Study" -> "📚"
                                    "Research" -> "🔍"
                                    "Image" -> "🖼️"
                                    "Video" -> "🎥"
                                    "Audio" -> "🎵"
                                    "Coding" -> "⚡"
                                    else -> "🤖"
                                }
                                Text("$useCaseIcon $useCase", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                            DropdownMenu(
                                expanded = showUseCaseDropdown,
                                onDismissRequest = { showUseCaseDropdown = false },
                                modifier = Modifier
                                    .background(Slate900)
                                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                            ) {
                                listOf("General Chat", "Codex (App Building)", "Creativity", "Study", "Research", "Image", "Video", "Audio", "Coding").forEach { uc ->
                                    DropdownMenuItem(
                                        text = { Text(uc, color = Color.White, fontSize = 12.sp) },
                                        onClick = {
                                            useCase = uc
                                            showUseCaseDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = GlassBorder.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Thinking Level Toggles
                    Text(
                        text = "Thinking Complexity Protocol",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val thinkingLevels = listOf("Fast", "Medium", "Complex", "Max")
                        thinkingLevels.forEach { tl ->
                            val isSelected = thinkingLevel == tl
                            val icon = when (tl) {
                                "Fast" -> "⚡"
                                "Medium" -> "⚖️"
                                "Complex" -> "🧠"
                                "Max" -> "🚀"
                                else -> "🤖"
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelected) Teal500 else Slate800.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                    .border(0.5.dp, if (isSelected) Teal400 else GlassBorder.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                    .clickable { thinkingLevel = tl }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(icon, fontSize = 11.sp)
                                    Text(tl, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Files / Assets title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Creations",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All",
                    color = Teal400,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigate("history") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (historyAssets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No assets generated yet. Enter any creative studio to build!",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    historyAssets.take(4).forEach { asset ->
                        RecentAssetRow(asset = asset) {
                            val target = when (asset.type) {
                                "image" -> "image_gen"
                                "video" -> "video_gen"
                                "music" -> "music_gen"
                                "app" -> "app_builder"
                                else -> "history"
                            }
                            onNavigate(target)
                        }
                    }
                }
            }
        }

        if (isVoiceAssistantActive) {
            var isRecordingVoice by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            AlertDialog(
                onDismissRequest = { isVoiceAssistantActive = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.SettingsVoice, contentDescription = "Voice Mode", tint = Rose500)
                        Text("$assistantName Voice Assistant", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Slate900,
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = voiceAssistantStatus,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        // Pulsing Waveform visualizer when recording
                        if (isRecordingVoice) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(7) { index ->
                                    val barHeight = remember { mutableStateOf(10.dp) }
                                    LaunchedEffect(isRecordingVoice) {
                                        while (isRecordingVoice) {
                                            barHeight.value = (10..45).random().dp
                                            kotlinx.coroutines.delay(80)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .width(5.dp)
                                            .height(barHeight.value)
                                            .background(
                                                color = if (index % 2 == 0) Rose500 else Teal400,
                                                shape = RoundedCornerShape(2.5.dp)
                                            )
                                    )
                                }
                            }
                        } else {
                            // Static placeholder when idle
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(horizontalArrangement = Arrangement.Center) {
                                    repeat(7) {
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 4.dp)
                                                .width(5.dp)
                                                .height(10.dp)
                                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.5.dp))
                                        )
                                    }
                                }
                            }
                        }

                        // Big Record Audio Button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            // Pulsing outer halo when recording
                            if (isRecordingVoice) {
                                val haloScale = remember { androidx.compose.animation.core.Animatable(1f) }
                                LaunchedEffect(isRecordingVoice) {
                                    while (isRecordingVoice) {
                                        haloScale.animateTo(
                                            targetValue = 1.4f,
                                            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                                animation = androidx.compose.animation.core.tween(1000),
                                                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                                            )
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .background(Rose500.copy(alpha = 0.2f), CircleShape)
                                        .scale(haloScale.value)
                                )
                            }

                            Button(
                                onClick = {
                                    if (!isRecordingVoice) {
                                        isRecordingVoice = true
                                        voiceAssistantStatus = "Recording high-fidelity audio command... Speak clearly to $assistantName."
                                        viewModel.speak("Listening")
                                    } else {
                                        isRecordingVoice = false
                                        voiceAssistantStatus = "Transcribing audio command..."
                                        scope.launch {
                                            kotlinx.coroutines.delay(1200)
                                            
                                            // Select a random smart voice command to run
                                            val voiceCommandsList = listOf(
                                                "Open Calculator",
                                                "Calculate 9 + 5",
                                                "Open Camera",
                                                "Open Gallery",
                                                "Open WhatsApp",
                                                "Call Vishwas",
                                                "Open YouTube",
                                                "Search Google for AI",
                                                "Open Maps",
                                                "Set an alarm for 6 AM",
                                                "Start a timer for 10 minutes"
                                            )
                                            val selectedCommand = voiceCommandsList.random()
                                            voiceAssistantStatus = "Transcribed: \"$selectedCommand\". Executing..."
                                            
                                            kotlinx.coroutines.delay(800)
                                            
                                            // Run the voice assistant command action
                                            val cleanCommand = selectedCommand.lowercase().trim()
                                            if (cleanCommand.contains("calculator")) {
                                                viewModel.speak("Opening Calculator")
                                                val intent = Intent(Intent.ACTION_MAIN).apply {
                                                    addCategory(Intent.CATEGORY_APP_CALCULATOR)
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                try { context.startActivity(intent) } catch (e: Exception) {}
                                            } else if (cleanCommand.contains("calculate")) {
                                                viewModel.speak("Calculating 9 plus 5 is 14")
                                            } else if (cleanCommand.contains("camera")) {
                                                viewModel.speak("Opening Camera")
                                                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                try { context.startActivity(intent) } catch (e: Exception) {}
                                            } else if (cleanCommand.contains("gallery")) {
                                                viewModel.speak("Opening Gallery")
                                                val intent = Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                try { context.startActivity(intent) } catch (e: Exception) {}
                                            } else if (cleanCommand.contains("whatsapp")) {
                                                viewModel.speak("Opening WhatsApp")
                                                val intent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                                                if (intent != null) {
                                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                    context.startActivity(intent)
                                                } else {
                                                    viewModel.speak("WhatsApp is not installed. Opening play store.")
                                                    val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.whatsapp")).apply {
                                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                    }
                                                    try { context.startActivity(playStoreIntent) } catch (e: Exception) {}
                                                }
                                            } else if (cleanCommand.contains("call vishwas") || cleanCommand.contains("call")) {
                                                viewModel.speak("Calling Vishwas")
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:9214000000")).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                try { context.startActivity(intent) } catch (e: Exception) {}
                                            } else if (cleanCommand.contains("youtube")) {
                                                viewModel.speak("Opening YouTube")
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                try { context.startActivity(intent) } catch (e: Exception) {}
                                            } else if (cleanCommand.contains("search google") || cleanCommand.contains("google search") || cleanCommand.contains("search")) {
                                                viewModel.speak("Searching Google for AI")
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=AI")).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                try { context.startActivity(intent) } catch (e: Exception) {}
                                            } else if (cleanCommand.contains("maps")) {
                                                viewModel.speak("Opening Google Maps")
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=AI")).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                try { context.startActivity(intent) } catch (e: Exception) {}
                                            } else if (cleanCommand.contains("alarm")) {
                                                viewModel.speak("Setting alarm for 6 AM")
                                                val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                                                    putExtra(AlarmClock.EXTRA_HOUR, 6)
                                                    putExtra(AlarmClock.EXTRA_MINUTES, 0)
                                                    putExtra(AlarmClock.EXTRA_MESSAGE, "Nexus AI Alarm")
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                try { context.startActivity(intent) } catch (e: Exception) {}
                                            } else if (cleanCommand.contains("timer")) {
                                                viewModel.speak("Starting 10 minute timer")
                                                val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                                                    putExtra(AlarmClock.EXTRA_LENGTH, 600)
                                                    putExtra(AlarmClock.EXTRA_MESSAGE, "Nexus AI Timer")
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                try { context.startActivity(intent) } catch (e: Exception) {}
                                            } else {
                                                viewModel.speak("Searching $assistantName Core for $selectedCommand")
                                                viewModel.sendMessage(selectedCommand)
                                                isVoiceAssistantActive = false
                                                onNavigate("chat")
                                            }
                                        }
                                    }
                                },
                                shape = CircleShape,
                                modifier = Modifier.size(64.dp).testTag("voice_record_toggle_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRecordingVoice) Rose500 else Color(0xFF2563EB)
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = if (isRecordingVoice) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = if (isRecordingVoice) "Stop Recording" else "Start Recording",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isRecordingVoice) "Tap to Stop" else "Tap to Record Audio",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { isVoiceAssistantActive = false }) {
                        Text("Close Assistant", color = Rose500)
                    }
                }
            )
        }
    }
}

@Composable
fun StudioCard(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .testTag("studio_card_$title"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = GlassBackground)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
                desc,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

data class TrendingTool(
    val name: String,
    val desc: String,
    val icon: ImageVector,
    val tint: Color,
    val provider: String
)

@Composable
fun TrendingToolItem(
    tool: TrendingTool,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GlassBackground)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Icon(tool.icon, contentDescription = tool.name, tint = tool.tint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(tool.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(tool.desc, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
        }
    }
}

@Composable
fun RecentAssetRow(
    asset: GeneratedAsset,
    onClick: () -> Unit
) {
    val icon = when (asset.type) {
        "image" -> Icons.Default.Image
        "video" -> Icons.Default.Movie
        "music" -> Icons.Default.MusicNote
        "app" -> Icons.Default.Code
        else -> Icons.Default.FilePresent
    }

    val iconColor = when (asset.type) {
        "image" -> Teal400
        "video" -> Rose500
        "music" -> Indigo400
        "app" -> Teal400
        else -> Color.White
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GlassBackground, RoundedCornerShape(16.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = asset.type, tint = iconColor, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = asset.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Model: ${asset.provider} • ${asset.extraInfo ?: ""}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = "Next Icon",
            tint = Color.White.copy(alpha = 0.4f),
            modifier = Modifier.size(18.dp)
        )
    }
}
