package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.*
import kotlinx.coroutines.launch
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.components.GlobalLoadingIndicator
import com.example.ui.viewmodel.AuthState
import com.example.ui.viewmodel.NexusViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val viewModel = ViewModelProvider(this)[NexusViewModel::class.java]

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val accentColor by viewModel.accentColor.collectAsState()
            val globalTaskState by viewModel.globalTaskState.collectAsState()

            MyApplicationTheme(
                darkTheme = when (themeMode) {
                    "light" -> false
                    else -> true
                },
                accentColor = accentColor
            ) {
                val authState by viewModel.authState.collectAsState()
                val navController = rememberNavController()

                // Redirect to Auth screen if not logged in
                LaunchedEffect(authState) {
                    if (authState is AuthState.Unauthenticated) {
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    } else if (authState is AuthState.Authenticated && navController.currentDestination?.route == "auth") {
                        navController.navigate("dashboard") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize().immersiveBackground(accentColor),
                    color = Color.Transparent
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = "auth"
                        ) {
                            composable("auth") {
                                AuthScreen(
                                    viewModel = viewModel,
                                    onAuthSuccess = {
                                        navController.navigate("dashboard") {
                                            popUpTo("auth") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable("dashboard") {
                                val pagerState = rememberPagerState(
                                    initialPage = 1,
                                    pageCount = { 3 }
                                )
                                val coroutineScope = rememberCoroutineScope()

                                AuthenticatedLayout(
                                    currentRoute = "dashboard",
                                    navController = navController,
                                    onHomeClick = {
                                        if (pagerState.currentPage != 1) {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(1)
                                            }
                                        }
                                    }
                                ) {
                                    HorizontalPager(
                                        state = pagerState,
                                        modifier = Modifier.fillMaxSize()
                                    ) { page ->
                                        when (page) {
                                            0 -> {
                                                SettingsScreen(
                                                    viewModel = viewModel,
                                                    onBack = {
                                                        coroutineScope.launch {
                                                            pagerState.animateScrollToPage(1)
                                                        }
                                                    }
                                                )
                                            }
                                            1 -> {
                                                DashboardScreen(
                                                    viewModel = viewModel,
                                                    onNavigate = { route ->
                                                        when (route) {
                                                            "settings" -> {
                                                                coroutineScope.launch {
                                                                    pagerState.animateScrollToPage(0)
                                                                }
                                                            }
                                                            "history" -> {
                                                                coroutineScope.launch {
                                                                    pagerState.animateScrollToPage(2)
                                                                }
                                                            }
                                                            else -> {
                                                                navController.navigate(route)
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                            2 -> {
                                                HistoryScreen(
                                                    viewModel = viewModel,
                                                    onBack = {
                                                        coroutineScope.launch {
                                                            pagerState.animateScrollToPage(1)
                                                        }
                                                    },
                                                    onNavigateToChatSession = { navController.navigate("chat") },
                                                    onNavigateToAssetScreen = { route -> navController.navigate(route) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            composable("chat") {
                                ChatScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("image_gen") {
                                ImageGenScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("video_gen") {
                                VideoGenScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("music_gen") {
                                MusicGenScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("app_builder") {
                                AppBuilderScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("apps_store") {
                                AuthenticatedLayout(
                                    currentRoute = "apps_store",
                                    navController = navController
                                ) {
                                    AppsStoreScreen(
                                        viewModel = viewModel,
                                        onBack = { navController.navigate("dashboard") },
                                        onNavigateToBuilder = { navController.navigate("app_builder") }
                                    )
                                }
                            }
                        }

                        // central overlay indicator
                        GlobalLoadingIndicator(taskState = globalTaskState)
                    }
                }
            }
        }
    }
}

@Composable
fun AuthenticatedLayout(
    currentRoute: String,
    navController: androidx.navigation.NavHostController,
    onHomeClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content()
        }
    }
}

data class NavItem(
    val label: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
