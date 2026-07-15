package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ChatMessage
import com.example.data.database.ChatSession
import com.example.ui.theme.*
import com.example.ui.viewmodel.NexusViewModel
import com.example.ui.util.MediaUtils
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: NexusViewModel,
    onBack: () -> Unit
) {
    val currentMessages by viewModel.currentMessages.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val activeSessionId by viewModel.activeSessionId.collectAsState()
    val chatSessions by viewModel.chatSessions.collectAsState()
    
    val thinkingMode by viewModel.thinkingMode.collectAsState()
    val webSearch by viewModel.webSearch.collectAsState()
    val uploadedFile by viewModel.uploadedFile.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var isImagenMode by remember { mutableStateOf(false) }
    var showProviderDialog by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInputText by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Photo Picker
    val photoLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)
            var displayName = "gallery_photo.jpg"
            var sizeInBytes = 1536 * 1024L
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val nameIndex = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        displayName = c.getString(nameIndex)
                    }
                    val sizeIndex = c.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        sizeInBytes = c.getLong(sizeIndex)
                    }
                }
            }
            val sizeStr = if (sizeInBytes > 1024 * 1024) {
                String.format("%.1f MB", sizeInBytes / (1024 * 1024f))
            } else {
                String.format("%d KB", sizeInBytes / 1024)
            }
            viewModel.attachFile(displayName, "jpg", "User attached a photo from device gallery: $displayName", sizeStr)
        }
    }

    // System File Picker
    val fileLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)
            var displayName = "document.bin"
            var sizeInBytes = 256 * 1024L
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val nameIndex = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        displayName = c.getString(nameIndex)
                    }
                    val sizeIndex = c.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        sizeInBytes = c.getLong(sizeIndex)
                    }
                }
            }
            val sizeStr = if (sizeInBytes > 1024 * 1024) {
                String.format("%.1f MB", sizeInBytes / (1024 * 1024f))
            } else {
                String.format("%d KB", sizeInBytes / 1024)
            }
            val extension = displayName.substringAfterLast('.', "bin")
            viewModel.attachFile(displayName, extension, "User attached a document file from device storage: $displayName", sizeStr)
        }
    }

    // --- Speech Recognizer Integration ---
    var showVoiceDialog by remember { mutableStateOf(false) }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var voiceTranscriptionText by remember { mutableStateOf("") }
    var voiceDuration by remember { mutableStateOf(0) }
    val voiceAmplitudes = remember { mutableStateListOf<Float>() }
    var speechErrorMsg by remember { mutableStateOf<String?>(null) }

    val speechRecognizer = remember { android.speech.SpeechRecognizer.createSpeechRecognizer(context) }
    val speechRecognizerIntent = remember {
        android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(android.speech.RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    val recognitionListener = remember {
        object : android.speech.RecognitionListener {
            override fun onReadyForSpeech(params: android.os.Bundle?) {
                speechErrorMsg = null
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {
                val normalized = (rmsdB + 2f).coerceIn(0f, 10f) / 10f
                if (voiceAmplitudes.size > 24) voiceAmplitudes.removeAt(0)
                voiceAmplitudes.add(normalized)
            }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                val message = when (error) {
                    android.speech.SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    android.speech.SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission denied"
                    android.speech.SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    android.speech.SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try again."
                    android.speech.SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy"
                    android.speech.SpeechRecognizer.ERROR_SERVER -> "Server error"
                    android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input. Speak clearer."
                    else -> "Transcription paused"
                }
                if (error != android.speech.SpeechRecognizer.ERROR_NO_MATCH && error != android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    speechErrorMsg = message
                }
            }
            override fun onResults(results: android.os.Bundle?) {
                val matches = results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    voiceTranscriptionText = matches[0]
                }
            }
            override fun onPartialResults(partialResults: android.os.Bundle?) {
                val matches = partialResults?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    voiceTranscriptionText = matches[0]
                }
            }
            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        }
    }

    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            voiceDuration = 0
            while (isRecordingVoice) {
                kotlinx.coroutines.delay(1000)
                voiceDuration++
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                speechRecognizer.destroy()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // Permission Launcher for recording audio
    val audioPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showVoiceDialog = true
            isRecordingVoice = true
            voiceTranscriptionText = ""
            speechErrorMsg = null
            speechRecognizer.setRecognitionListener(recognitionListener)
            speechRecognizer.startListening(speechRecognizerIntent)
        } else {
            // Permission denied
        }
    }

    val providers = listOf("Gemini", "ChatGPT", "Claude", "DeepSeek", "Grok", "Perplexity", "Llama", "Mistral")
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Create session automatically on launch if empty
    LaunchedEffect(activeSessionId) {
        if (activeSessionId == null) {
            viewModel.createNewChat()
        }
    }

    // Scroll to bottom on new message
    LaunchedEffect(currentMessages.size) {
        if (currentMessages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(currentMessages.size - 1)
            }
        }
    }

    // Find active session to show title in top bar
    val activeSession = remember(chatSessions, activeSessionId) {
        chatSessions.find { it.id == activeSessionId }
    }

    // Modal navigation drawer state for mobile view
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .immersiveBackground()
    ) {
        val isTablet = maxWidth > 720.dp

        if (isTablet) {
            // Tablet Layout: Split screen
            Row(modifier = Modifier.fillMaxSize()) {
                // Sidebar Panel (Sessions list)
                SessionSidebar(
                    sessions = chatSessions,
                    activeId = activeSessionId,
                    onSelectSession = { id -> viewModel.selectChat(id) },
                    onNewChat = { viewModel.createNewChat() },
                    onDeleteSession = { id -> viewModel.deleteChat(id) },
                    onTogglePin = { session -> viewModel.togglePinChat(session) },
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight()
                )

                VerticalDivider(color = GlassBorder, modifier = Modifier.fillMaxHeight().width(1.dp))

                // Main Chat Panel
                ChatContentArea(
                    isTablet = true,
                    activeSession = activeSession,
                    selectedProvider = selectedProvider,
                    thinkingMode = thinkingMode,
                    webSearch = webSearch,
                    isImagenMode = isImagenMode,
                    currentMessages = currentMessages,
                    isGenerating = isGenerating,
                    uploadedFile = uploadedFile,
                    textInput = textInput,
                    onTextInputChange = { textInput = it },
                    onBack = onBack,
                    onToggleThinking = { viewModel.toggleThinking() },
                    onToggleWebSearch = { viewModel.toggleWebSearch() },
                    onToggleImagenMode = { isImagenMode = !isImagenMode },
                    onOpenDrawer = {},
                    onOpenProviderSelect = { showProviderDialog = true },
                    onOpenRenameSession = {
                        activeSession?.let {
                            renameInputText = it.title
                            showRenameDialog = true
                        }
                    },
                    onOpenAttachment = { showAttachmentSheet = true },
                    onOpenVoiceRecorder = { audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO) },
                    onRemoveAttachedFile = { viewModel.removeAttachedFile() },
                    onSendMessage = {
                        if (textInput.trim().isNotEmpty() || uploadedFile != null) {
                            if (isImagenMode) {
                                viewModel.sendImagenMessage(textInput)
                            } else {
                                viewModel.sendMessage(textInput)
                            }
                            textInput = ""
                            isImagenMode = false
                        }
                    },
                    listState = listState,
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            // Mobile Layout: Navigation Drawer + Chat Content Area
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = Slate950,
                        drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                        modifier = Modifier.width(280.dp)
                    ) {
                        SessionSidebar(
                            sessions = chatSessions,
                            activeId = activeSessionId,
                            onSelectSession = { id ->
                                viewModel.selectChat(id)
                                scope.launch { drawerState.close() }
                            },
                            onNewChat = {
                                viewModel.createNewChat()
                                scope.launch { drawerState.close() }
                            },
                            onDeleteSession = { id -> viewModel.deleteChat(id) },
                            onTogglePin = { session -> viewModel.togglePinChat(session) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                },
                gesturesEnabled = true
            ) {
                ChatContentArea(
                    isTablet = false,
                    activeSession = activeSession,
                    selectedProvider = selectedProvider,
                    thinkingMode = thinkingMode,
                    webSearch = webSearch,
                    isImagenMode = isImagenMode,
                    currentMessages = currentMessages,
                    isGenerating = isGenerating,
                    uploadedFile = uploadedFile,
                    textInput = textInput,
                    onTextInputChange = { textInput = it },
                    onBack = onBack,
                    onToggleThinking = { viewModel.toggleThinking() },
                    onToggleWebSearch = { viewModel.toggleWebSearch() },
                    onToggleImagenMode = { isImagenMode = !isImagenMode },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onOpenProviderSelect = { showProviderDialog = true },
                    onOpenRenameSession = {
                        activeSession?.let {
                            renameInputText = it.title
                            showRenameDialog = true
                        }
                    },
                    onOpenAttachment = { showAttachmentSheet = true },
                    onOpenVoiceRecorder = { audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO) },
                    onRemoveAttachedFile = { viewModel.removeAttachedFile() },
                    onSendMessage = {
                        if (textInput.trim().isNotEmpty() || uploadedFile != null) {
                            if (isImagenMode) {
                                viewModel.sendImagenMessage(textInput)
                            } else {
                                viewModel.sendMessage(textInput)
                            }
                            textInput = ""
                            isImagenMode = false
                        }
                    },
                    listState = listState,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Rename Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Chat Title", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Slate900,
            text = {
                OutlinedTextField(
                    value = renameInputText,
                    onValueChange = { renameInputText = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Teal400,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        activeSessionId?.let { id ->
                            if (renameInputText.trim().isNotEmpty()) {
                                viewModel.renameChat(id, renameInputText.trim())
                            }
                        }
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal500, contentColor = Color.Black)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = Rose500)
                }
            }
        )
    }

    // Provider Dropdown Selector Dialog
    if (showProviderDialog) {
        AlertDialog(
            onDismissRequest = { showProviderDialog = false },
            title = { Text("Choose AI Reasoning Engine", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Slate900,
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    providers.forEach { provider ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setChatProvider(provider)
                                    showProviderDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(provider, color = Color.White, fontSize = 16.sp)
                            if (selectedProvider == provider) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = Teal400)
                            }
                        }
                        HorizontalDivider(color = GlassBorder)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProviderDialog = false }) {
                    Text("Close", color = Indigo300)
                }
            }
        )
    }

    // Attachment Picker Dialog / Sheet
    if (showAttachmentSheet) {
        val files = listOf(
            NexusViewModel.AttachedFile("quarterly_results.pdf", "pdf", "Table: Revenue: $450M (+12%), Gross Margins: 64%, Operating income: $112M. Major Growth Drivers: Cloud Suite v4 (+24% YoY), API Webhook Subscriptions (+41%). Major Headwinds: Chip procurement latency, marketing CPC inflation.", "1.4 MB"),
            NexusViewModel.AttachedFile("app_architecture.docx", "docx", "Nexus AI Architecture Design Spec. Client layer constructed in Jetpack Compose MVVM with SQLite Room Local Cache Database. Direct HTTP REST API proxy channels to Gemini multi-modality engines. Speeds supported down to edge networks via stream buffer lines.", "420 KB"),
            NexusViewModel.AttachedFile("system_logs.csv", "csv", "Timestamp,Level,Component,Message\n10:14:02,INFO,NexusCore,Boot sequence success\n10:14:05,WARN,Network,Ping latency elevated (184ms)\n10:14:10,ERROR,Auth,Expired JWT session verified", "12 KB"),
            NexusViewModel.AttachedFile("simple_api.py", "py", "import os\ndef get_status():\n    return {'nexus_core': 'active', 'tokens_queued': 1024}\nprint(get_status())", "2 KB")
        )

        AlertDialog(
            onDismissRequest = { showAttachmentSheet = false },
            title = { Text("Attach Media or Document", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Slate900,
            text = {
                Column {
                    Text("Select device content or use secure mock sandbox assets.", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, modifier = Modifier.padding(bottom = 16.dp))
                    
                    // Real Pickers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    showAttachmentSheet = false
                                    photoLauncher.launch("image/*")
                                }
                                .testTag("pick_photo_btn"),
                            colors = CardDefaults.cardColors(containerColor = Indigo500.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, Indigo400.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = Teal400, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Gallery Photo", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Pick image", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    showAttachmentSheet = false
                                    fileLauncher.launch("*/*")
                                }
                                .testTag("pick_file_btn"),
                            colors = CardDefaults.cardColors(containerColor = Teal500.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, Teal400.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = "Files", tint = Indigo300, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Device Files", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Browse files", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = GlassBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("SANDBOX DOCUMENT PRESETS", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    files.forEach { file ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.attachFile(file.name, file.extension, file.content, file.size)
                                    showAttachmentSheet = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp)
                                .background(GlassOverlay, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FilePresent, contentDescription = "File type icon", tint = Indigo300)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(file.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("${file.extension.uppercase()} • ${file.size}", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAttachmentSheet = false }) {
                    Text("Cancel", color = Rose500)
                }
            }
        )
    }

    // Voice Recorder & Dictation Dialog
    if (showVoiceDialog) {
        val voiceSuggestions = listOf(
            "Compare Kotlin's Coroutines with standard Java threads.",
            "What are the best practices for Material 3 responsive layouts?",
            "Can you write a Room database initialization snippet?",
            "Write a detailed breakdown of MVVM architecture."
        )

        AlertDialog(
            onDismissRequest = {
                try {
                    speechRecognizer.stopListening()
                } catch(e: Exception) {}
                isRecordingVoice = false
                showVoiceDialog = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isRecordingVoice) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Voice dictation",
                        tint = Teal400,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isRecordingVoice) "Listening..." else "Recording Paused",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            containerColor = Slate900,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Stopwatch timer
                    val mins = voiceDuration / 60
                    val secs = voiceDuration % 60
                    val timeStr = String.format("%02d:%02d", mins, secs)
                    
                    Text(
                        text = timeStr,
                        color = if (isRecordingVoice) Teal400 else Color.White.copy(alpha = 0.5f),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Waveform visualizer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isRecordingVoice) {
                            // Show jumping bars
                            val bars = 12
                            for (i in 0 until bars) {
                                val amp = if (voiceAmplitudes.size > i) voiceAmplitudes[voiceAmplitudes.size - 1 - i] else 0.1f
                                val barHeight = (amp * 45f + 5f).dp
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .width(5.dp)
                                        .height(barHeight)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Teal400, Indigo400)
                                            ),
                                            RoundedCornerShape(3.dp)
                                        )
                                )
                            }
                        } else {
                            // Flat line / idle state
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(Color.White.copy(alpha = 0.15f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live transcription preview box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp, max = 150.dp)
                            .background(GlassOverlay, RoundedCornerShape(12.dp))
                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (voiceTranscriptionText.isNotEmpty()) {
                            Text(
                                text = voiceTranscriptionText,
                                color = Color.White,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        } else {
                            Text(
                                text = if (isRecordingVoice) "Speak clearly. Your transcription will appear here in real-time..." else "No audio captured yet. Tap record to speak.",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 13.sp,
                                style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            )
                        }
                    }

                    if (speechErrorMsg != null) {
                        Text(
                            text = speechErrorMsg ?: "",
                            color = Rose500,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons to control the voice recording or start/stop
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isRecordingVoice) {
                            Button(
                                onClick = {
                                    try {
                                        speechRecognizer.stopListening()
                                    } catch(e: Exception) {}
                                    isRecordingVoice = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Rose500.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, Rose500.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Rose500, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pause", color = Color.White, fontSize = 13.sp)
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    isRecordingVoice = true
                                    speechErrorMsg = null
                                    try {
                                        speechRecognizer.setRecognitionListener(recognitionListener)
                                        speechRecognizer.startListening(speechRecognizerIntent)
                                    } catch(e: Exception) {
                                        speechErrorMsg = "Unable to start voice recorder."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Teal500.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, Teal400.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = Teal400, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Record", color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = GlassBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Fast emulator suggestions
                    Text(
                        text = "SPEECH SIMULATOR / SUGGESTIONS",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    voiceSuggestions.forEach { sug ->
                        Text(
                            text = sug,
                            color = Indigo300,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    voiceTranscriptionText = sug
                                }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            try {
                                speechRecognizer.stopListening()
                            } catch(e: Exception) {}
                            isRecordingVoice = false
                            showVoiceDialog = false
                        }
                    ) {
                        Text("Discard", color = Rose500)
                    }
                    Button(
                        onClick = {
                            if (voiceTranscriptionText.isNotEmpty()) {
                                textInput = voiceTranscriptionText
                            }
                            try {
                                speechRecognizer.stopListening()
                            } catch(e: Exception) {}
                            isRecordingVoice = false
                            showVoiceDialog = false
                        },
                        enabled = voiceTranscriptionText.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal500),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apply to Chat", color = Color.White)
                    }
                }
            }
        )
    }
}

@Composable
fun SessionSidebar(
    sessions: List<ChatSession>,
    activeId: Int?,
    onSelectSession: (Int) -> Unit,
    onNewChat: () -> Unit,
    onDeleteSession: (Int) -> Unit,
    onTogglePin: (ChatSession) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(Slate950)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Conversations",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = onNewChat,
                modifier = Modifier
                    .background(Indigo500.copy(alpha = 0.2f), CircleShape)
                    .size(36.dp)
                    .testTag("new_chat_sidebar_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Chat",
                    tint = Teal400,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active chats.\nTap '+' to begin.",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            // Sort pinned sessions first, then by lastUpdated descending
            val sortedSessions = remember(sessions) {
                sessions.sortedWith(compareByDescending<ChatSession> { it.isPinned }.thenByDescending { it.lastUpdated })
            }
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedSessions) { session ->
                    val isActive = session.id == activeId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectSession(session.id) }
                            .border(
                                width = 1.dp,
                                color = if (isActive) Teal400 else Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("session_card_${session.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) Slate900 else Slate900.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (session.isPinned) Icons.Default.Star else Icons.Default.Chat,
                                contentDescription = "Session icon",
                                tint = if (isActive) Teal400 else if (session.isPinned) Indigo400 else Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = session.title,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(Indigo500.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = session.provider,
                                            color = Indigo300,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            
                            // Pin option
                            IconButton(
                                onClick = { onTogglePin(session) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (session.isPinned) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Pin session",
                                    tint = if (session.isPinned) Indigo400 else Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            // Delete option
                            IconButton(
                                onClick = { onDeleteSession(session.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete session",
                                    tint = Rose500.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatContentArea(
    isTablet: Boolean,
    activeSession: ChatSession?,
    selectedProvider: String,
    thinkingMode: Boolean,
    webSearch: Boolean,
    isImagenMode: Boolean,
    currentMessages: List<ChatMessage>,
    isGenerating: Boolean,
    uploadedFile: NexusViewModel.AttachedFile?,
    textInput: String,
    onTextInputChange: (String) -> Unit,
    onBack: () -> Unit,
    onToggleThinking: () -> Unit,
    onToggleWebSearch: () -> Unit,
    onToggleImagenMode: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenProviderSelect: () -> Unit,
    onOpenRenameSession: () -> Unit,
    onOpenAttachment: () -> Unit,
    onOpenVoiceRecorder: () -> Unit,
    onRemoveAttachedFile: () -> Unit,
    onSendMessage: () -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState,
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column(modifier = Modifier.background(Color.Transparent)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isTablet) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    } else {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Default.Menu, contentDescription = "Sessions Menu", tint = Color.White)
                        }
                    }

                    // Active Session Title and Provider select
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenRenameSession() }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = activeSession?.title ?: "New Conversation",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Edit, contentDescription = "Rename Chat", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
                            }
                            
                            Row(
                                modifier = Modifier.clickable { onOpenProviderSelect() },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedProvider,
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    // Toggles shortcut row
                    IconButton(onClick = onToggleThinking) {
                        Icon(
                            Icons.Default.Psychology, 
                            contentDescription = "Thinking Mode", 
                            tint = if (thinkingMode) Indigo400 else Color.White.copy(alpha = 0.4f)
                        )
                    }

                    IconButton(onClick = onToggleWebSearch) {
                        Icon(
                            Icons.Default.Language, 
                            contentDescription = "Web Search", 
                            tint = if (webSearch) Teal400 else Color.White.copy(alpha = 0.4f)
                        )
                    }

                    IconButton(
                        onClick = onToggleImagenMode,
                        modifier = Modifier.testTag("imagen_mode_toggle_btn")
                    ) {
                        Icon(
                            Icons.Default.Brush, 
                            contentDescription = "Imagen Mode", 
                            tint = if (isImagenMode) Teal400 else Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
                
                HorizontalDivider(color = GlassBorder)
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Message List
            if (currentMessages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(Indigo500.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Forum, contentDescription = "Forum", tint = Indigo300, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Start chatting with $selectedProvider",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "You can ask questions, write code, paste documents, or analyze raw files. Choose any reasoning mode above.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 100.dp), // make space for bottom inputs
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(currentMessages) { message ->
                        ChatMessageItem(
                            message = message,
                            onSpeak = { viewModel.speak(message.content) },
                            onStopSpeak = { viewModel.stopSpeaking() }
                        )
                    }
                    if (isGenerating) {
                        item {
                            ShimmerChatBubble(selectedProvider)
                        }
                    }
                }
            }

            // Bottom Input HUD
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Slate950.copy(alpha = 0.9f))
                        )
                    )
                    .padding(16.dp)
            ) {
                // File Attachment Indicator Banner
                uploadedFile?.let { file ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .background(Indigo500.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .border(0.5.dp, Indigo300.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FilePresent, contentDescription = "File icon", tint = Indigo300, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(file.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${file.extension.uppercase()} • ${file.size}", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                            }
                        }
                        IconButton(
                            onClick = onRemoveAttachedFile,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove File", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                        }
                    }
                }

                // Chat Input Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Attachment Trigger Button
                    IconButton(
                        onClick = onOpenAttachment,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .size(48.dp)
                            .background(GlassBackground, CircleShape)
                            .border(0.5.dp, GlassBorder, CircleShape)
                            .testTag("attachment_button")
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attach File", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Voice Dictation (Mic) Button
                    IconButton(
                        onClick = onOpenVoiceRecorder,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .size(48.dp)
                            .background(GlassBackground, CircleShape)
                            .border(0.5.dp, GlassBorder, CircleShape)
                            .testTag("voice_mic_button")
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Dictation", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Main Text Input Area - ENLARGED
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = onTextInputChange,
                        placeholder = {
                            Text(
                                if (isImagenMode) "Describe the image to generate with Imagen..." else "Message $selectedProvider...",
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cyan400,
                            unfocusedBorderColor = GlassBorder.copy(alpha = 0.5f),
                            focusedContainerColor = Slate900.copy(alpha = 0.95f),
                            unfocusedContainerColor = Slate900.copy(alpha = 0.9f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .testTag("chat_input"),
                        minLines = 3,
                        maxLines = 8,
                        trailingIcon = {
                            if (textInput.isNotEmpty()) {
                                IconButton(onClick = { onTextInputChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear text", tint = Color.White.copy(alpha = 0.4f))
                                }
                            }
                        }
                    )

                    // Send Action Button
                    IconButton(
                        onClick = onSendMessage,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .size(48.dp)
                            .background(Brush.linearGradient(listOf(Indigo500, Teal500, Rose500)), CircleShape)
                            .testTag("send_button")
                    ) {
                        Icon(
                            imageVector = if (isImagenMode) Icons.Default.AutoAwesome else Icons.AutoMirrored.Filled.Send,
                            contentDescription = if (isImagenMode) "Generate Image" else "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onSpeak: () -> Unit,
    onStopSpeak: () -> Unit
) {
    val isUser = message.role == "user"
    val context = androidx.compose.ui.platform.LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Brush.linearGradient(listOf(Indigo500, Teal500)), CircleShape)
                    .align(Alignment.Top),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false)
        ) {
            // Main Message Balloon
            Card(
                shape = if (isUser) {
                    RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
                } else {
                    RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
                },
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) Indigo500 else Slate900
                ),
                modifier = Modifier.border(
                    width = 0.5.dp,
                    color = if (isUser) Color.Transparent else GlassBorder,
                    shape = if (isUser) RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomEnd = 16.dp, bottomStart = 16.dp) else RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Attachment Metadata inside message
                    if (isUser && message.filePath != null) {
                        Row(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FilePresent, contentDescription = "Attachment", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.fileName ?: "Document",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = message.content,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )

                    // Display generated output image inside history bubble if available
                    if (!isUser && message.filePath != null && (message.filePath.startsWith("http") || message.filePath.contains("unsplash") || message.filePath.endsWith(".jpg") || message.filePath.endsWith(".png"))) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(0.5.dp, GlassBorder, RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = message.filePath,
                                contentDescription = "Generated Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("chat_generated_image")
                            )

                            // Floating Download button over the image
                            IconButton(
                                onClick = {
                                    MediaUtils.downloadMediaFile(context, message.filePath, "chat_image.jpg", "image/jpeg")
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(36.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Download Image", tint = Color.White)
                            }
                        }
                    }

                    // Speak, Copy & Share Actions Row
                    if (!isUser) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                MediaUtils.copyToClipboard(context, message.content)
                            }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Teal400, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                MediaUtils.shareText(context, message.content)
                            }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = Teal400, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = onSpeak, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Listen", tint = Teal400, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = onStopSpeak, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.VolumeOff, contentDescription = "Stop Listening", tint = Rose500, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShimmerChatBubble(provider: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // Glowing background color oscillation
    val glowColor by infiniteTransition.animateColor(
        initialValue = Slate900.copy(alpha = 0.6f),
        targetValue = Indigo500.copy(alpha = 0.15f),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Smooth physics-based bouncing offsets for three separate typing dots
    val dot1Transition = rememberInfiniteTransition(label = "dot1")
    val dot2Transition = rememberInfiniteTransition(label = "dot2")
    val dot3Transition = rememberInfiniteTransition(label = "dot3")

    val dot1Y by dot1Transition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Y by dot2Transition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, delayMillis = 120, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Y by dot3Transition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, delayMillis = 240, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    // Dynamic spinning rotation angle for maximum visual feedback while thinking
    val rotationTransition = rememberInfiniteTransition(label = "avatar_rotation")
    val avatarAngle by rotationTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "avatar_angle"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        // AI Avatar with dynamic rotating outer ring for maximum polish
        Box(
            modifier = Modifier
                .size(36.dp)
                .graphicsLayer(rotationZ = avatarAngle)
                .background(Brush.sweepGradient(listOf(Indigo500, Teal500, Rose500, Indigo500)), CircleShape)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Slate950, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "N",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            // Skeleton Thinking/Reasoning Chip with bouncing dot indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .background(glowColor, RoundedCornerShape(12.dp))
                    .border(0.5.dp, GlassBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // Outer core glowing pulse
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Teal400, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$provider is thinking",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Bouncing Dots Typing Container
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .height(14.dp)
                        .padding(bottom = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .graphicsLayer(translationY = dot1Y)
                            .background(Teal400, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .graphicsLayer(translationY = dot2Y)
                            .background(Indigo400, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .graphicsLayer(translationY = dot3Y)
                            .background(Rose500, CircleShape)
                    )
                }
            }

            // Main Skeleton Card
            Card(
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.85f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 0.5.dp,
                        color = GlassBorder,
                        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Shimmering Paragraph Line 1 (85% width)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(14.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.White.copy(alpha = alpha),
                                        Color.White.copy(alpha = alpha * 0.4f)
                                    )
                                ), RoundedCornerShape(7.dp)
                            )
                    )
                    // Shimmering Paragraph Line 2 (95% width)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(14.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.White.copy(alpha = alpha * 0.6f),
                                        Color.White.copy(alpha = alpha)
                                    )
                                ), RoundedCornerShape(7.dp)
                            )
                    )
                    // Shimmering Paragraph Line 3 (55% width)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(14.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.White.copy(alpha = alpha),
                                        Color.White.copy(alpha = alpha * 0.3f)
                                    )
                                ), RoundedCornerShape(7.dp)
                            )
                    )
                }
            }
        }
    }
}
