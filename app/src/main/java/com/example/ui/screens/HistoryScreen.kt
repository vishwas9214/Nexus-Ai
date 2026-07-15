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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ChatSession
import com.example.data.database.GeneratedAsset
import com.example.ui.theme.*
import com.example.ui.viewmodel.NexusViewModel

@Composable
fun HistoryScreen(
    viewModel: NexusViewModel,
    onBack: () -> Unit,
    onNavigateToChatSession: (Int) -> Unit,
    onNavigateToAssetScreen: (String) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchFilter by viewModel.searchFilter.collectAsState()
    
    val allAssets by viewModel.allHistoryAssets.collectAsState()
    val allChats by viewModel.chatSessions.collectAsState()

    val filters = listOf("All", "Chats", "Images", "Videos", "Music", "Apps")

    // Compile united list items
    val filteredItems = remember(allAssets, allChats, searchQuery, searchFilter) {
        val list = mutableListOf<HistoryUnionItem>()
        
        // Add Chats
        if (searchFilter == "All" || searchFilter == "Chats") {
            allChats.forEach { chat ->
                if (searchQuery.isBlank() || chat.title.contains(searchQuery, ignoreCase = true)) {
                    list.add(HistoryUnionItem.Chat(chat))
                }
            }
        }

        // Add Assets
        allAssets.forEach { asset ->
            val matchesFilter = when (searchFilter) {
                "All" -> true
                "Images" -> asset.type == "image"
                "Videos" -> asset.type == "video"
                "Music" -> asset.type == "music"
                "Apps" -> asset.type == "app"
                else -> false
            }
            if (matchesFilter && (searchQuery.isBlank() || asset.title.contains(searchQuery, ignoreCase = true) || asset.prompt.contains(searchQuery, ignoreCase = true))) {
                list.add(HistoryUnionItem.Asset(asset))
            }
        }

        // Sort by timestamp desc (Chats use lastUpdated, Assets use timestamp)
        list.sortedByDescending { it.sortTimestamp }
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
                Text("Universal Archives", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search chats, images, music, code...", color = Color.White.copy(alpha = 0.4f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = Teal400) },
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
                    .padding(bottom = 12.dp)
                    .testTag("universal_search_input")
            )

            // Horizontal Filters Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSel = searchFilter == filter
                    Box(
                        modifier = Modifier
                            .background(if (isSel) Teal500 else Slate900, RoundedCornerShape(20.dp))
                            .border(0.5.dp, if (isSel) Teal400 else GlassBorder, RoundedCornerShape(20.dp))
                            .clickable { viewModel.setSearchFilter(filter) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Universal Archive list
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Empty", tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No archives match your search criteria.", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredItems) { union ->
                        when (union) {
                            is HistoryUnionItem.Chat -> {
                                ChatHistoryRow(
                                    session = union.session,
                                    onClick = {
                                        viewModel.selectChat(union.session.id)
                                        onNavigateToChatSession(union.session.id)
                                    },
                                    onDelete = { viewModel.deleteChat(union.session.id) }
                                )
                            }
                            is HistoryUnionItem.Asset -> {
                                AssetHistoryRow(
                                    asset = union.asset,
                                    onClick = {
                                        val target = when (union.asset.type) {
                                            "image" -> "image_gen"
                                            "video" -> "video_gen"
                                            "music" -> "music_gen"
                                            "app" -> "app_builder"
                                            else -> "dashboard"
                                        }
                                        onNavigateToAssetScreen(target)
                                    },
                                    onDelete = { viewModel.deleteAssetById(union.asset.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed interface HistoryUnionItem {
    val sortTimestamp: Long

    data class Chat(val session: ChatSession) : HistoryUnionItem {
        override val sortTimestamp: Long get() = session.lastUpdated
    }

    data class Asset(val asset: GeneratedAsset) : HistoryUnionItem {
        override val sortTimestamp: Long get() = asset.timestamp
    }
}

@Composable
fun ChatHistoryRow(
    session: ChatSession,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate900.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .border(0.5.dp, GlassBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Indigo500.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat", tint = Indigo300)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Model Provider: ${session.provider}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Session", tint = Rose500, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun AssetHistoryRow(
    asset: GeneratedAsset,
    onClick: () -> Unit,
    onDelete: () -> Unit
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
            .background(Slate900.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .border(0.5.dp, GlassBorder, RoundedCornerShape(12.dp))
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
            Icon(icon, contentDescription = asset.type, tint = iconColor)
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
                text = "Studio: ${asset.type.uppercase()} • Model: ${asset.provider}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Asset", tint = Rose500, modifier = Modifier.size(18.dp))
        }
    }
}
