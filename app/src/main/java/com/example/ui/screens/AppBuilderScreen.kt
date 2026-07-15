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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.NexusViewModel

@Composable
fun AppBuilderScreen(
    viewModel: NexusViewModel,
    onBack: () -> Unit
) {
    val builderState by viewModel.appBuilderState.collectAsState()
    
    var prompt by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf("Web React") }
    val platforms = listOf("Web React", "Android Kotlin", "HTML/CSS/JS", "Python GUI", "Express API")

    // Tab Selection for successful build
    var selectedTab by remember { mutableStateOf(0) } // 0 = Source Code, 1 = Live Preview

    // Live preview interactive states for simulator
    var countSeconds by remember { mutableStateOf(1500) }
    var isTimerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (countSeconds > 0 && isTimerRunning) {
                kotlinx.coroutines.delay(1000)
                countSeconds--
            }
        }
    }

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
                Text("AI App Architect Studio", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                "Instruct the AI Architect to generate complete single-file software platforms with live-rendering interactive simulations.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Dynamic Compiler Result Console
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Slate900)
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                when (val state = builderState) {
                    is com.example.ui.viewmodel.NexusViewModel.AppBuilderUiState.Idle -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(Icons.Default.DeveloperMode, contentDescription = "Code", tint = Teal400, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Architect Sandbox Is Idle", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Instruct the compiler on any app feature below", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }
                    is com.example.ui.viewmodel.NexusViewModel.AppBuilderUiState.Building -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = Teal400, modifier = Modifier.align(Alignment.CenterHorizontally))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("NEXUS COMPILER CORE STARTED", color = Teal400, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("> Allocating system execution buffers...", color = Color.White.copy(alpha = 0.7f), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            Text("> Initializing prompt synthesis parsing vectors...", color = Color.White.copy(alpha = 0.7f), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            Text("> Compiling structural AST blocks with Gemini...", color = Color.White.copy(alpha = 0.7f), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            Text("> Injecting styled elements into CSS layer...", color = Color.White.copy(alpha = 0.7f), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }
                    }
                    is com.example.ui.viewmodel.NexusViewModel.AppBuilderUiState.Success -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Sub-Tabs Header Selector
                            TabRow(
                                selectedTabIndex = selectedTab,
                                containerColor = Slate950,
                                contentColor = Teal400,
                                indicator = { tabPositions ->
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                        color = Teal400
                                    )
                                }
                            ) {
                                Tab(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    text = { Text("Source Code", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    text = { Text("Live Sandbox Preview", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                )
                            }

                            if (selectedTab == 0) {
                                // Code view (high-fidelity custom scroll panel)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .background(Color(0xFF010A15))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                            .horizontalScroll(rememberScrollState())
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = state.code,
                                            color = Color(0xFF2DD4BF),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp
                                        )
                                    }

                                    // Floating Copy Code
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        IconButton(
                                            onClick = { /* copy simulated */ },
                                            modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            } else {
                                // Live Preview Simulator (Avoid generic empty screen, show interactive widgets based on prompt keywords)
                                val pLower = prompt.lowercase()
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .background(Slate950)
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (pLower.contains("timer") || pLower.contains("pomodoro") || pLower.contains("clock")) {
                                        // Working Interactive Pomodoro simulation widget
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Slate900),
                                            modifier = Modifier
                                                .fillMaxWidth(0.85f)
                                                .border(0.5.dp, GlassBorder, RoundedCornerShape(16.dp))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("Pomodoro Timer Sandbox", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text(
                                                    text = "${countSeconds / 60}:${(countSeconds % 60).toString().padStart(2, '0')}",
                                                    color = Rose500,
                                                    fontSize = 32.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                    Button(
                                                        onClick = { isTimerRunning = !isTimerRunning },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Teal500)
                                                    ) {
                                                        Text(if (isTimerRunning) "Pause" else "Start", fontSize = 12.sp)
                                                    }
                                                    OutlinedButton(
                                                        onClick = {
                                                            isTimerRunning = false
                                                            countSeconds = 1500
                                                        },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                                    ) {
                                                        Text("Reset", fontSize = 12.sp)
                                                    }
                                                }
                                            }
                                        }
                                    } else if (pLower.contains("crypto") || pLower.contains("price") || pLower.contains("tracker") || pLower.contains("bitcoin")) {
                                        // Cryptocurrency Dashboard mock simulation widget
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Slate900),
                                            modifier = Modifier.fillMaxWidth(0.9f)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text("Crypto Dashboard Simulator", color = Teal400, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.AttachMoney, contentDescription = "Bitcoin", tint = Teal400)
                                                        Text("BTC / USD", color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                    Text("$94,204.50", color = Teal400, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.AttachMoney, contentDescription = "Ethereum", tint = Indigo300)
                                                        Text("ETH / USD", color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                    Text("$3,412.10", color = Indigo300, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                                }
                                            }
                                        }
                                    } else {
                                        // Default beautiful mockup for any user-described prompt
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Slate900),
                                            modifier = Modifier.fillMaxWidth(0.9f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(Icons.Default.Webhook, contentDescription = "Platform API", tint = Teal400, modifier = Modifier.size(36.dp))
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Text(state.asset.title, color = Color.White, fontWeight = FontWeight.Bold)
                                                Text("Platform Compiler Result Sandbox", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Color.Black, RoundedCornerShape(8.dp))
                                                        .padding(8.dp)
                                                ) {
                                                    Text(
                                                        "GET /api/status - 200 OK\n{'nexus_engine': 'live', 'status': 'functional'}",
                                                        color = Color(0xFF2DD4BF),
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Download/Export Row footer
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Slate950)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                TextButton(onClick = { /* simulated ZIP download */ }) {
                                    Icon(Icons.Default.FolderZip, contentDescription = "ZIP", tint = Teal400)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ZIP Download", color = Teal400, fontSize = 11.sp)
                                }
                                TextButton(onClick = { /* simulated code export */ }) {
                                    Icon(Icons.Default.Download, contentDescription = "Export Code", tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Export", color = Color.White, fontSize = 11.sp)
                                }
                                TextButton(onClick = { /* simulated git push */ }) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = "GitHub", tint = Indigo300)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Push to GitHub", color = Indigo300, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    is com.example.ui.viewmodel.NexusViewModel.AppBuilderUiState.Error -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = Rose500, modifier = Modifier.size(45.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Compiler Panic", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(state.message, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Params Area
            Text("Compiler Directives", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            // Platform selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Target Frame / Platform", color = Color.White, fontSize = 13.sp)
                var exp by remember { mutableStateOf(false) }
                Box {
                    Text(
                        selectedPlatform,
                        color = Teal400,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { exp = true }
                    )
                    DropdownMenu(
                        expanded = exp,
                        onDismissRequest = { exp = false },
                        modifier = Modifier.background(Slate900)
                    ) {
                        platforms.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p, color = Color.White) },
                                onClick = {
                                    selectedPlatform = p
                                    exp = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Prompt Box
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                placeholder = { Text("E.g., A clean modern Pomodoro Timer with dark mode toggle and sleek circles, built with local storage...", color = Color.White.copy(alpha = 0.4f)) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Teal400,
                    unfocusedBorderColor = GlassBorder,
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate900
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_prompt_input"),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Build Clip Button
            Button(
                onClick = {
                    if (prompt.trim().isNotEmpty()) {
                        viewModel.buildApp(prompt, selectedPlatform)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("build_app_submit"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                enabled = prompt.trim().isNotEmpty()
            ) {
                Icon(Icons.Default.Memory, contentDescription = "Memory")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compile Software Sandbox", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
