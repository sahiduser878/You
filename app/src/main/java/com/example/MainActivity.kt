package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.NisuEdgeGlowBorder
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AccessibilityScreen
import com.example.ui.screens.AutomationScreen
import com.example.ui.screens.CallScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CodingScreen
import com.example.ui.screens.EdgeGlowScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.ScreenShareScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.BackgroundObsidian
import com.example.ui.theme.SurfaceDarkNavy
import com.example.ui.theme.YesminCyan
import com.example.ui.theme.YesminPink
import com.example.ui.theme.YesminTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YesminTheme(darkTheme = true) {
                NisuApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun NisuApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    var showMoreMenu by remember { mutableStateOf(false) }

    val isRgbBorderOn = viewModel.settingsManager.isRgbBorderEnabled()
    val rgbMode = viewModel.settingsManager.getRgbBorderMode()
    val rgbPalette = viewModel.settingsManager.getRgbBorderPalette()
    val rgbSpeed = viewModel.settingsManager.getRgbBorderSpeed()
    val rgbBrightness = viewModel.settingsManager.getRgbBorderBrightness()
    val rgbWidth = viewModel.settingsManager.getRgbBorderWidth()
    val audioRms by viewModel.voiceManager.rmsLevel.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = BackgroundObsidian,
            bottomBar = {
                // Hide bottom bar during immersive Call Screen
                if (currentScreen != Screen.CALLING) {
                    NavigationBar(
                        containerColor = SurfaceDarkNavy,
                        contentColor = YesminCyan
                    ) {
                        NavigationBarItem(
                            selected = currentScreen == Screen.HOME,
                            onClick = { viewModel.navigateTo(Screen.HOME) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF001F28),
                                selectedTextColor = YesminCyan,
                                indicatorColor = YesminCyan,
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.testTag("nav_home")
                        )

                        NavigationBarItem(
                            selected = currentScreen == Screen.CHAT,
                            onClick = { viewModel.navigateTo(Screen.CHAT) },
                            icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Chat") },
                            label = { Text("Chat") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF001F28),
                                selectedTextColor = YesminCyan,
                                indicatorColor = YesminCyan,
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.testTag("nav_chat")
                        )

                        NavigationBarItem(
                            selected = currentScreen == Screen.SCREEN_SHARE,
                            onClick = { viewModel.navigateTo(Screen.SCREEN_SHARE) },
                            icon = { Icon(Icons.Default.ScreenShare, contentDescription = "Screen") },
                            label = { Text("Screen") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF001F28),
                                selectedTextColor = YesminPink,
                                indicatorColor = YesminPink,
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.testTag("nav_screen")
                        )

                        NavigationBarItem(
                            selected = currentScreen == Screen.EDGE_GLOW,
                            onClick = { viewModel.navigateTo(Screen.EDGE_GLOW) },
                            icon = { Icon(Icons.Default.ColorLens, contentDescription = "Glow") },
                            label = { Text("Glow") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF001F28),
                                selectedTextColor = YesminCyan,
                                indicatorColor = YesminCyan,
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.testTag("nav_glow")
                        )

                        NavigationBarItem(
                            selected = currentScreen in listOf(
                                Screen.ACCESSIBILITY, Screen.SETTINGS, Screen.CALLING,
                                Screen.CODING, Screen.MEMORY, Screen.TOOLS,
                                Screen.PERMISSIONS, Screen.ABOUT
                            ),
                            onClick = { showMoreMenu = true },
                            icon = {
                                Box {
                                    Icon(Icons.Default.Menu, contentDescription = "More")
                                    DropdownMenu(
                                        expanded = showMoreMenu,
                                        onDismissRequest = { showMoreMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Accessibility & Automation") },
                                            onClick = {
                                                showMoreMenu = false
                                                viewModel.navigateTo(Screen.ACCESSIBILITY)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Real-Time Voice Call") },
                                            onClick = {
                                                showMoreMenu = false
                                                viewModel.navigateTo(Screen.CALLING)
                                                viewModel.startCalling()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Settings & Voice Pack") },
                                            onClick = {
                                                showMoreMenu = false
                                                viewModel.navigateTo(Screen.SETTINGS)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Coding Assistant") },
                                            onClick = {
                                                showMoreMenu = false
                                                viewModel.navigateTo(Screen.CODING)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Memory Vault (Room DB)") },
                                            onClick = {
                                                showMoreMenu = false
                                                viewModel.navigateTo(Screen.MEMORY)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Tools & Vision OCR") },
                                            onClick = {
                                                showMoreMenu = false
                                                viewModel.navigateTo(Screen.TOOLS)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Permissions & Privacy") },
                                            onClick = {
                                                showMoreMenu = false
                                                viewModel.navigateTo(Screen.PERMISSIONS)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("About NISU AI") },
                                            onClick = {
                                                showMoreMenu = false
                                                viewModel.navigateTo(Screen.ABOUT)
                                            }
                                        )
                                    }
                                }
                            },
                            label = { Text("More") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF001F28),
                                selectedTextColor = YesminCyan,
                                indicatorColor = YesminCyan,
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.testTag("nav_more")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    Screen.HOME -> HomeScreen(viewModel = viewModel)
                    Screen.CHAT -> ChatScreen(viewModel = viewModel)
                    Screen.CALLING -> CallScreen(viewModel = viewModel)
                    Screen.SCREEN_SHARE -> ScreenShareScreen(viewModel = viewModel)
                    Screen.EDGE_GLOW -> EdgeGlowScreen(viewModel = viewModel)
                    Screen.ACCESSIBILITY -> AccessibilityScreen(viewModel = viewModel)
                    Screen.CODING -> CodingScreen(viewModel = viewModel)
                    Screen.AUTOMATION -> AccessibilityScreen(viewModel = viewModel)
                    Screen.MEMORY -> MemoryScreen(viewModel = viewModel)
                    Screen.TOOLS -> ToolsScreen(viewModel = viewModel)
                    Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    Screen.PERMISSIONS -> PermissionsScreen(viewModel = viewModel)
                    Screen.ABOUT -> AboutScreen(viewModel = viewModel)
                }
            }
        }

        // In-App RGB Edge Glow Border Overlay
        NisuEdgeGlowBorder(
            enabled = isRgbBorderOn,
            mode = rgbMode,
            palette = rgbPalette,
            speed = rgbSpeed,
            brightness = rgbBrightness,
            strokeWidth = rgbWidth.dp,
            audioRms = audioRms
        )
    }
}
