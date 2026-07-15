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
fun VideoGenScreen(
    viewModel: NexusViewModel,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val videoState by viewModel.videoGenState.collectAsState()
    
    var prompt by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf("Kling AI") }
    var selectedQuality by remember { mutableStateOf("1080p") }
    var selectedDuration by remember { mutableStateOf("5s") }

    // Playback control state
    var isPlaying by remember { mutableStateOf(false) }

    val providers = listOf("Kling AI", "Runway Gen-3", "Pika 2.0", "Luma Dream Machine")
    val qualities = listOf("720p", "1080p", "4K Ultra")
    val durations = listOf("5s", "10s", "15s")

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
                Text("Cinematic Video Engine", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
            Text(
                "Generate cinematic clips, expand existing videos, or interpolate frame paths using generative AI.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Dynamic Player/Rendering Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Slate900)
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                when (val state = videoState) {
                    is com.example.ui.viewmodel.NexusViewModel.VideoGenUiState.Idle -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(Icons.Default.MovieFilter, contentDescription = "Placeholder", tint = Rose500, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Cinematic stage is inactive", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Describe action, motion, lighting details below", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }
                    is com.example.ui.viewmodel.NexusViewModel.VideoGenUiState.Rendering -> {
                        val progress = state.progress
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { progress.percent / 100f },
                                color = Rose500,
                                strokeWidth = 6.dp,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Rendering: ${progress.percent}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Elapsed Time: ${progress.elapsedSec}s • Thread active", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                    }
                    is com.example.ui.viewmodel.NexusViewModel.VideoGenUiState.Success -> {
                        val asset = state.asset
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = asset.filePath, // Represents the cinematic preview
                                contentDescription = "Cinematic video asset representation",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Interactive Playback overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = if (isPlaying) 0.1f else 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = { isPlaying = !isPlaying },
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(
                                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Playback",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            // Rendering Details & Operations Toolbar
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Black.copy(alpha = 0.8f))
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = asset.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Provider: ${asset.provider} • Quality: $selectedQuality",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = {
                                        asset.filePath?.let { url ->
                                            MediaUtils.downloadMediaFile(context, url, "${asset.title}.mp4", "video/mp4")
                                        }
                                    }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Download, contentDescription = "Download Video", tint = Rose500)
                                    }
                                    IconButton(onClick = {
                                        asset.filePath?.let { url ->
                                            MediaUtils.shareMedia(context, asset.title, url)
                                        }
                                    }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Share, contentDescription = "Share Video", tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                    is com.example.ui.viewmodel.NexusViewModel.VideoGenUiState.Error -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = "Error icon", tint = Rose500, modifier = Modifier.size(45.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Renderer Crash", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(state.message, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Params UI
            Text("Renderer Directives", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            // Provider Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Video Engine Provider", color = Color.White, fontSize = 13.sp)
                var exp by remember { mutableStateOf(false) }
                Box {
                    Text(
                        selectedProvider,
                        color = Rose500,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { exp = true }
                    )
                    DropdownMenu(
                        expanded = exp,
                        onDismissRequest = { exp = false },
                        modifier = Modifier.background(Slate900)
                    ) {
                        providers.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p, color = Color.White) },
                                onClick = {
                                    selectedProvider = p
                                    exp = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quality choices
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Export Quality", color = Color.White, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    qualities.forEach { q ->
                        val selected = selectedQuality == q
                        Box(
                            modifier = Modifier
                                .background(if (selected) Rose500 else Slate800, RoundedCornerShape(6.dp))
                                .clickable { selectedQuality = q }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(q, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Duration selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sequence Duration", color = Color.White, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    durations.forEach { d ->
                        val selected = selectedDuration == d
                        Box(
                            modifier = Modifier
                                .background(if (selected) Rose500 else Slate800, RoundedCornerShape(6.dp))
                                .clickable { selectedDuration = d }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(d, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text Prompt input
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                placeholder = { Text("E.g., An astronaut riding a neon cybercycle inside a glowing matrix corridor, 4k cinematic camera panning...", color = Color.White.copy(alpha = 0.4f)) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Rose500,
                    unfocusedBorderColor = GlassBorder,
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate900
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("video_prompt_input"),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Build Clip Button
            Button(
                onClick = {
                    if (prompt.trim().isNotEmpty()) {
                        viewModel.generateVideo(prompt, selectedProvider, selectedQuality)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("generate_video_submit"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Rose500),
                enabled = prompt.trim().isNotEmpty()
            ) {
                Icon(Icons.Default.Movie, contentDescription = "Movie")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Render Cinematic Clip", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
