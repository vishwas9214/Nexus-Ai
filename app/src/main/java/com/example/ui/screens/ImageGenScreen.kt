package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.ui.viewmodel.NexusViewModel
import com.example.ui.util.MediaUtils

@Composable
fun ImageGenScreen(
    viewModel: NexusViewModel,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val imageState by viewModel.imageGenState.collectAsState()
    
    var prompt by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf("Flux") }
    var selectedRatio by remember { mutableStateOf("1:1") }
    var enhancePrompt by remember { mutableStateOf(false) }

    // BG Removal state variables
    var bgRemoved by remember { mutableStateOf(false) }
    var isRemovingBg by remember { mutableStateOf(false) }

    val providers = listOf("Flux.1", "Gemini Image", "Stable Diffusion v3", "Kling Image")
    val aspectRatios = listOf("1:1", "16:9", "9:16", "4:3")
    val enhancers = listOf("Cyberpunk", "Photorealistic", "Vaporwave", "Pixel Art", "Vibrant Anime", "Studio Lighting")

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
                Text("AI Image Lab", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
            // Screen Header Description
            Text(
                "Convert textual descriptions into premium visual assets using high-fidelity diffusion models.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Result Panel (Static or Generated Success representation)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Slate900)
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                when (val state = imageState) {
                    is com.example.ui.viewmodel.NexusViewModel.ImageGenUiState.Idle -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "Placeholder", tint = Indigo300, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Your creation canvas is empty", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Type a prompt and press Generate below", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                    }
                    is com.example.ui.viewmodel.NexusViewModel.ImageGenUiState.Generating -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Teal400)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Interpreting prompt parameters...", color = Color.White, fontSize = 14.sp)
                            Text("Diffusion sampling in progress...", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                    }
                    is com.example.ui.viewmodel.NexusViewModel.ImageGenUiState.Success -> {
                        val asset = state.asset
                        Box(modifier = Modifier.fillMaxSize()) {
                            // High fidelity Unsplash Loaded Image
                            AsyncImage(
                                model = asset.filePath,
                                contentDescription = "Generated Asset",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("generated_image_view"),
                                contentScale = ContentScale.Crop
                            )

                            // Background Removal Overlay Simulation
                            if (bgRemoved) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "BACKGROUND REMOVED\n(Subject isolated)",
                                        color = Teal400,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }

                            if (isRemovingBg) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.7f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = Indigo400)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("Extracting subject vectors...", color = Color.White, fontSize = 13.sp)
                                    }
                                }
                            }

                            // Operations Toolbar (Glassmorphic)
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                IconButton(onClick = {
                                    isRemovingBg = true
                                    bgRemoved = false
                                    // Simulated background removal timeout
                                    val thread = Thread {
                                        Thread.sleep(1500)
                                        isRemovingBg = false
                                        bgRemoved = true
                                    }
                                    thread.start()
                                }) {
                                    Icon(Icons.Default.Compare, contentDescription = "BG Removal", tint = Teal400)
                                }
                                IconButton(onClick = {
                                    val asset = (imageState as? com.example.ui.viewmodel.NexusViewModel.ImageGenUiState.Success)?.asset
                                    asset?.filePath?.let { url ->
                                        MediaUtils.downloadMediaFile(context, url, "${asset.title}.jpg", "image/jpeg")
                                    }
                                }) {
                                    Icon(Icons.Default.Download, contentDescription = "HD Export", tint = Color.White)
                                }
                                IconButton(onClick = {
                                    val asset = (imageState as? com.example.ui.viewmodel.NexusViewModel.ImageGenUiState.Success)?.asset
                                    asset?.filePath?.let { url ->
                                        MediaUtils.shareMedia(context, asset.title, url)
                                    }
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share Image", tint = Color.White)
                                }
                                IconButton(onClick = {
                                    android.widget.Toast.makeText(context, "Enhancing resolution to 4K...", android.widget.Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.FilterCenterFocus, contentDescription = "Upscale", tint = Color.White)
                                }
                                IconButton(onClick = { viewModel.clearImageState() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Rebuild", tint = Rose500)
                                }
                            }
                        }
                    }
                    is com.example.ui.viewmodel.NexusViewModel.ImageGenUiState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(Icons.Default.Error, contentDescription = "Error icon", tint = Rose500, modifier = Modifier.size(45.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Compilation Failure", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(state.message, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Form Fields
            Text("Diffusion Parameters", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // Model choice Dropdown / Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Select Model", color = Color.White, fontSize = 14.sp)
                var expanded by remember { mutableStateOf(false) }
                Box {
                    Text(
                        selectedProvider,
                        color = Indigo300,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { expanded = true }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Slate900)
                    ) {
                        providers.forEach { modelName ->
                            DropdownMenuItem(
                                text = { Text(modelName, color = Color.White) },
                                onClick = {
                                    selectedProvider = modelName
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Aspect Ratio row selection
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text("Aspect Ratio", color = Color.White, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    aspectRatios.forEach { ratio ->
                        val isSelected = selectedRatio == ratio
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelected) Indigo500 else Slate800, RoundedCornerShape(8.dp))
                                .border(0.5.dp, if (isSelected) Indigo300 else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { selectedRatio = ratio }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(ratio, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Prompt Area
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                placeholder = { Text("Describe what you want to see inside the canvas...", color = Color.White.copy(alpha = 0.4f)) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Indigo400,
                    unfocusedBorderColor = GlassBorder,
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate900
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("image_prompt_input"),
                maxLines = 4
            )

            // Prompt Enhancers list row
            Spacer(modifier = Modifier.height(12.dp))
            Text("Aesthetic Enhancers", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                enhancers.forEach { keyword ->
                    Box(
                        modifier = Modifier
                            .background(GlassBackground, RoundedCornerShape(20.dp))
                            .border(0.5.dp, GlassBorder, RoundedCornerShape(20.dp))
                            .clickable {
                                if (!prompt.contains(keyword)) {
                                    prompt = if (prompt.trim().isEmpty()) keyword else "$prompt, $keyword"
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(keyword, color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            // Prompt Enhancer switch
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("AI Prompt Enhancer", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Auto-expand inputs with high-end aesthetic details", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
                Switch(
                    checked = enhancePrompt,
                    onCheckedChange = { enhancePrompt = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Teal400, checkedTrackColor = Teal400.copy(alpha = 0.4f))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Generation Button
            Button(
                onClick = {
                    if (prompt.trim().isNotEmpty()) {
                        bgRemoved = false
                        viewModel.generateImage(prompt, selectedProvider, selectedRatio, enhancePrompt)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("generate_image_submit"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                enabled = prompt.trim().isNotEmpty()
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Build Icon")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Synthesize Assets", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
