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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
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
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AutomationScreen
import com.example.ui.screens.CallScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CodingScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.BackgroundObsidian
import com.example.ui.theme.SurfaceDarkNavy
import com.example.ui.theme.YesminCyan
import com.example.ui.theme.YesminTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YesminTheme(darkTheme = true) {
                YesminApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun YesminApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    var showMoreMenu by remember { mutableStateOf(false) }

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
                        selected = currentScreen == Screen.CALLING,
                        onClick = {
                            viewModel.navigateTo(Screen.CALLING)
                            viewModel.startCalling()
                        },
                        icon = { Icon(Icons.Default.Call, contentDescription = "Call") },
                        label = { Text("Call") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF001F28),
                            selectedTextColor = YesminCyan,
                            indicatorColor = YesminCyan,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_call")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.CODING,
                        onClick = { viewModel.navigateTo(Screen.CODING) },
                        icon = { Icon(Icons.Default.Code, contentDescription = "Code") },
                        label = { Text("Code") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF001F28),
                            selectedTextColor = YesminCyan,
                            indicatorColor = YesminCyan,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_code")
                    )

                    NavigationBarItem(
                        selected = currentScreen in listOf(
                            Screen.AUTOMATION, Screen.MEMORY, Screen.TOOLS,
                            Screen.SETTINGS, Screen.PERMISSIONS, Screen.ABOUT
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
                                        text = { Text("Phone Automation") },
                                        onClick = {
                                            showMoreMenu = false
                                            viewModel.navigateTo(Screen.AUTOMATION)
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
                                        text = { Text("Settings & API Key") },
                                        onClick = {
                                            showMoreMenu = false
                                            viewModel.navigateTo(Screen.SETTINGS)
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
                                        text = { Text("About Yesmin AI") },
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
                Screen.CODING -> CodingScreen(viewModel = viewModel)
                Screen.AUTOMATION -> AutomationScreen(viewModel = viewModel)
                Screen.MEMORY -> MemoryScreen(viewModel = viewModel)
                Screen.TOOLS -> ToolsScreen(viewModel = viewModel)
                Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                Screen.PERMISSIONS -> PermissionsScreen(viewModel = viewModel)
                Screen.ABOUT -> AboutScreen(viewModel = viewModel)
            }
        }
    }
}
