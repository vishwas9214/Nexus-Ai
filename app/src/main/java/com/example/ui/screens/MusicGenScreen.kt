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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.NexusViewModel

@Composable
fun MusicGenScreen(
    viewModel: NexusViewModel,
    onBack: () -> Unit
) {
    val musicState by viewModel.musicGenState.collectAsState()
    
    var prompt by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("Lofi Chill") }
    var instrumental by remember { mutableStateOf(false) }

    // Playback state
    var isPlaying by remember { mutableStateOf(false) }
    var playPercent by remember { mutableStateOf(0.35f) }

    val genres = listOf("Lofi Chill", "Cyberpunk Synthwave", "Cinematic Orchestral", "Deep House EDM", "Smooth Jazz", "Epic Metal")

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
                Text("Music Forge Studio", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                "Generate poetic lyric matrices, compile synthesis waveforms, and assemble master tracks dynamically.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Dynamic Player / Equalizer HUD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (val state = musicState) {
                        is com.example.ui.viewmodel.NexusViewModel.MusicGenUiState.Idle -> {
                            Icon(Icons.Default.MusicNote, contentDescription = "Music", tint = Indigo300, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Music Forge Inactive", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Formulate lyrics and compile a wavetone below", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                        is com.example.ui.viewmodel.NexusViewModel.MusicGenUiState.GeneratingLyrics -> {
                            CircularProgressIndicator(color = Indigo400)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Compiling AI Lyric Matrix...", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Aligning lyrical rhythm with $selectedGenre...", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                        is com.example.ui.viewmodel.NexusViewModel.MusicGenUiState.Success -> {
                            val asset = state.asset
                            
                            // Audio equalizing visualizer (dynamic canvas representation)
                            Text(asset.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp, textAlign = TextAlign.Center)
                            Text("Style: ${asset.extraInfo}", color = Teal400, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            // Equalizer Simulation (Animated Row of columns)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(45.dp)
                                    .padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val barsCount = 18
                                for (i in 0 until barsCount) {
                                    val h = if (isPlaying) (15..45).random().dp else 8.dp
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(h)
                                            .background(Brush.verticalGradient(listOf(Indigo400, Teal400)), RoundedCornerShape(2.dp))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Playback timeline slider
                            Slider(
                                value = playPercent,
                                onValueChange = { playPercent = it },
                                colors = SliderDefaults.colors(thumbColor = Teal400, activeTrackColor = Indigo400),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("0:${(playPercent * 40).toInt().toString().padStart(2, '0')}", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                Text("1:10", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Player Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { playPercent = 0.0f }) {
                                    Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                IconButton(
                                    onClick = { isPlaying = !isPlaying },
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(Indigo500, CircleShape)
                                ) {
                                    Icon(
                                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Playback toggle",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                IconButton(onClick = { playPercent = 1.0f }) {
                                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action download buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { /* simulated download mp3 */ },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = "Download MP3", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("MP3", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { /* simulated download wav */ },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Icon(Icons.Default.DownloadForOffline, contentDescription = "Download WAV", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("WAV", fontSize = 11.sp)
                                }
                            }
                        }
                        is com.example.ui.viewmodel.NexusViewModel.MusicGenUiState.Error -> {
                            Icon(Icons.Default.Error, contentDescription = "Error", tint = Rose500, modifier = Modifier.size(45.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Music Forge Error", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(state.message, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            // Lyric Sheet display
            if (musicState is com.example.ui.viewmodel.NexusViewModel.MusicGenUiState.Success) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Generated Lyric Matrix", color = Teal400, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = (musicState as com.example.ui.viewmodel.NexusViewModel.MusicGenUiState.Success).lyrics,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Params selection
            Text("Lyrical Parameters", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            // Genre Grid
            Text("Select Musical Style", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                genres.forEach { genre ->
                    val isSel = selectedGenre == genre
                    Box(
                        modifier = Modifier
                            .background(if (isSel) Indigo500 else Slate900, RoundedCornerShape(10.dp))
                            .border(0.5.dp, if (isSel) Indigo300 else GlassBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedGenre = genre }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(genre, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Instrumental switch toggle
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
                    Text("Pure Instrumental", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Deactivate lyrical synthesis and focus on orchestration", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
                Switch(
                    checked = instrumental,
                    onCheckedChange = { instrumental = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Teal400, checkedTrackColor = Teal400.copy(alpha = 0.4f))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Prompt Box
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                placeholder = { Text("E.g., A melancholic ballad about code compilers dreaming of floating clouds and blue skies...", color = Color.White.copy(alpha = 0.4f)) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Indigo400,
                    unfocusedBorderColor = GlassBorder,
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate900
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("music_prompt_input"),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Compile Audio Button
            Button(
                onClick = {
                    if (prompt.trim().isNotEmpty()) {
                        viewModel.generateMusic(prompt, selectedGenre, instrumental)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("generate_music_submit"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                enabled = prompt.trim().isNotEmpty()
            ) {
                Icon(Icons.Default.MusicVideo, contentDescription = "Music Video Icon")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Forge Lyrical Soundwave", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
