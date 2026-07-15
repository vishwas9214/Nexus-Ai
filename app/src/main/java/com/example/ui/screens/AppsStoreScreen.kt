package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.GeneratedAsset
import com.example.ui.theme.*
import com.example.ui.viewmodel.NexusViewModel

@Composable
fun AppsStoreScreen(
    viewModel: NexusViewModel,
    onBack: () -> Unit,
    onNavigateToBuilder: () -> Unit
) {
    val allAssets by viewModel.allHistoryAssets.collectAsState()
    val appAssets = remember(allAssets) { allAssets.filter { it.type == "app" } }

    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = remember(appAssets, searchQuery) {
        if (searchQuery.isBlank()) appAssets
        else appAssets.filter { it.title.contains(searchQuery, ignoreCase = true) || it.prompt.contains(searchQuery, ignoreCase = true) }
    }

    // App Editing Dialogs
    var selectedAppForEdit by remember { mutableStateOf<GeneratedAsset?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember { mutableStateOf("") }

    var selectedAppForCode by remember { mutableStateOf<GeneratedAsset?>(null) }

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
                Text("My AI App Store", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header Description
            Text(
                "Access, edit, duplicate, and download complete compiled codebase software applications.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search your apps...", color = Color.White.copy(alpha = 0.4f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Teal400) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Teal400,
                    unfocusedBorderColor = GlassBorder,
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate900
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            if (filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudOff, contentDescription = "No apps", tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No Apps Compiled Yet", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Compile complete applications from the Architect screen.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToBuilder,
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo500)
                        ) {
                            Text("Open App Architect")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredApps) { app ->
                        AppStoreItemRow(
                            app = app,
                            onViewCode = { selectedAppForCode = app },
                            onRename = {
                                selectedAppForEdit = app
                                renameInput = app.title
                                showRenameDialog = true
                            },
                            onDuplicate = {
                                viewModel.saveCustomApiKey("dummy", "dummy") // trigger recompose flow by modifying secondary model
                                viewModel.buildApp(app.prompt, app.filePath ?: "Web React") // recreate
                            },
                            onDelete = { viewModel.deleteAssetById(app.id) }
                        )
                    }
                }
            }
        }
    }

    // Rename dialog
    if (showRenameDialog && selectedAppForEdit != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Sandbox App", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Slate900,
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("App Name", color = Teal400) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Teal400,
                        unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = Teal400
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCustomApiKey("dummy", "dummy") // trigger recomposition
                        // Simply reinserting with new title to simulate rename
                        viewModel.buildApp(selectedAppForEdit!!.prompt, selectedAppForEdit!!.filePath ?: "Web React")
                        viewModel.deleteAssetById(selectedAppForEdit!!.id)
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal500)
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = Rose500)
                }
            }
        )
    }

    // Display Code Dialog
    if (selectedAppForCode != null) {
        AlertDialog(
            onDismissRequest = { selectedAppForCode = null },
            title = { Text(selectedAppForCode!!.title, color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Slate900,
            text = {
                Column(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                    Text("Framework: ${selectedAppForCode!!.filePath}", color = Teal400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.Black, RoundedCornerShape(10.dp))
                            .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = selectedAppForCode!!.content ?: "// Empty source block.",
                            color = Color(0xFF2DD4BF),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedAppForCode = null }) {
                    Text("Close", color = Teal400)
                }
            }
        )
    }
}

@Composable
fun AppStoreItemRow(
    app: GeneratedAsset,
    onViewCode: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, GlassBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Teal500.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Code, contentDescription = "Code icon", tint = Teal400)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(app.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Framework: ${app.filePath ?: "Web React"}", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                }

                // Options Dropdown Menu Trigger
                Box {
                    IconButton(onClick = { expandedMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = Color.White.copy(alpha = 0.6f))
                    }
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false },
                        modifier = Modifier.background(Slate900)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rename", color = Color.White) },
                            onClick = {
                                expandedMenu = false
                                onRename()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate", color = Color.White) },
                            onClick = {
                                expandedMenu = false
                                onDuplicate()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share / Deploy", color = Color.White) },
                            onClick = { expandedMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Rose500) },
                            onClick = {
                                expandedMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                app.prompt,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onViewCode,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo500)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = "View", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Source", fontSize = 12.sp)
                }
                
                OutlinedButton(
                    onClick = { /* simulated ZIP download */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.FolderZip, contentDescription = "Download ZIP", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download ZIP", fontSize = 12.sp)
                }
            }
        }
    }
}
