package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accessibility.OgSahidAccessibilityService
import com.example.automation.PhoneAutomationHelper
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.FuturisticHudCard
import com.example.ui.theme.BackgroundObsidian
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDarkNavy
import com.example.ui.theme.YesminCyan
import com.example.ui.theme.YesminPink
import com.example.ui.theme.YesminPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.HOME)
    }

    val context = LocalContext.current
    val settings = viewModel.settingsManager

    var isServiceEnabled by remember {
        mutableStateOf(OgSahidAccessibilityService.isAccessibilityPermissionGranted(context))
    }

    var testStatusMessage by remember { mutableStateOf<String?>(null) }

    var allowAppLaunch by remember { mutableStateOf(settings.isAppLaunchAllowed()) }
    var allowTap by remember { mutableStateOf(settings.isTapAllowed()) }
    var allowScroll by remember { mutableStateOf(settings.isScrollAllowed()) }
    var allowTextEntry by remember { mutableStateOf(settings.isTextEntryAllowed()) }
    var confirmSensitive by remember { mutableStateOf(settings.isConfirmSensitiveEnabled()) }

    LaunchedEffect(Unit) {
        isServiceEnabled = OgSahidAccessibilityService.isAccessibilityPermissionGranted(context)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundObsidian,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Accessibility & Automation",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.HOME) },
                        modifier = Modifier.testTag("accessibility_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = YesminCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDarkNavy)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Permission Status Banner
            item {
                Surface(
                    color = if (isServiceEnabled) Color(0x2200E676) else Color(0x22FFAB00),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isServiceEnabled) Color(0xFF00E676) else Color(0xFFFFAB00)
                    )
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
                                        .size(36.dp)
                                        .background(
                                            if (isServiceEnabled) Color(0x3300E676) else Color(0x33FFAB00),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (isServiceEnabled) Icons.Default.CheckCircle else Icons.Default.AccessibilityNew,
                                        contentDescription = null,
                                        tint = if (isServiceEnabled) Color(0xFF00E676) else Color(0xFFFFAB00),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isServiceEnabled) "Accessibility Connected" else "Accessibility Disabled",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isServiceEnabled) Color(0xFF00E676) else Color(0xFFFFAB00)
                                    )
                                    Text(
                                        text = if (isServiceEnabled) "NISU AI can perform phone commands"
                                        else "Enable in Android Settings to activate phone control",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    OgSahidAccessibilityService.openAccessibilitySettings(context)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isServiceEnabled) YesminCyan else Color(0xFFFFAB00)
                                )
                            ) {
                                Icon(
                                    Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    tint = Color(0xFF001F28),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isServiceEnabled) "Open Settings" else "Enable Accessibility",
                                    color = Color(0xFF001F28),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    isServiceEnabled = OgSahidAccessibilityService.isAccessibilityPermissionGranted(context)
                                    testStatusMessage = if (isServiceEnabled) "Status: Service active!" else "Status: Service not yet detected."
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Check")
                            }
                        }
                    }
                }
            }

            // Diagnostics Checklist
            item {
                FuturisticHudCard(
                    title = "Service Diagnostics",
                    icon = Icons.Default.Build,
                    glowColor = YesminCyan
                ) {
                    DiagnosticItem(
                        title = "Service Connected",
                        detail = "Background process bound to Android AccessibilityManager",
                        active = OgSahidAccessibilityService.isRunning
                    )
                    DiagnosticItem(
                        title = "Window Content Access",
                        detail = "Inspects visible UI nodes to locate buttons & contacts",
                        active = isServiceEnabled
                    )
                    DiagnosticItem(
                        title = "Gesture & Action Dispatch",
                        detail = "Executes Back, Home, Scroll, and Tap actions",
                        active = isServiceEnabled
                    )
                }
            }

            // Interactive Test Command Runner
            item {
                FuturisticHudCard(
                    title = "Test Automation Runner",
                    icon = Icons.Default.TouchApp,
                    glowColor = YesminPink
                ) {
                    Text(
                        text = "Verify that NISU AI's automation actions execute properly on your device:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val res = PhoneAutomationHelper.performBack()
                                testStatusMessage = res.message
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Test Back", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val res = PhoneAutomationHelper.performScrollDown()
                                testStatusMessage = res.message
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Test Scroll", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val res = PhoneAutomationHelper.openYouTubeSearch(context, "Trending Music")
                                testStatusMessage = res.message
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Test YouTube", fontSize = 12.sp)
                        }
                    }

                    testStatusMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Result: $msg",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = YesminPink
                        )
                    }
                }
            }

            // Allowed Actions & Safety Toggles
            item {
                FuturisticHudCard(
                    title = "Allowed Actions & Safety Controls",
                    icon = Icons.Default.Security,
                    glowColor = YesminPurple
                ) {
                    ToggleSettingRow(
                        title = "Launch Applications",
                        subtitle = "Open WhatsApp, YouTube, Camera, Settings via voice",
                        checked = allowAppLaunch,
                        onCheckedChange = {
                            allowAppLaunch = it
                            settings.setAppLaunchAllowed(it)
                        }
                    )
                    ToggleSettingRow(
                        title = "Tap Buttons & Links",
                        subtitle = "Auto-click UI elements matching requested names",
                        checked = allowTap,
                        onCheckedChange = {
                            allowTap = it
                            settings.setTapAllowed(it)
                        }
                    )
                    ToggleSettingRow(
                        title = "Scroll Lists & Pages",
                        subtitle = "Perform scroll up/down gestures when requested",
                        checked = allowScroll,
                        onCheckedChange = {
                            allowScroll = it
                            settings.setScrollAllowed(it)
                        }
                    )
                    ToggleSettingRow(
                        title = "Type Text into Inputs",
                        subtitle = "Auto-fill input fields on active screens",
                        checked = allowTextEntry,
                        onCheckedChange = {
                            allowTextEntry = it
                            settings.setTextEntryAllowed(it)
                        }
                    )
                    ToggleSettingRow(
                        title = "Confirm Sensitive Actions",
                        subtitle = "Always prompt before sending messages or placing calls",
                        checked = confirmSensitive,
                        onCheckedChange = {
                            confirmSensitive = it
                            settings.setConfirmSensitiveEnabled(it)
                        }
                    )
                }
            }

            // Step by Step Setup Guide
            item {
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "How to Enable Accessibility in Android:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = YesminCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Tap 'Enable Accessibility' above to open Android Settings.\n" +
                                   "2. Locate 'Installed apps' or 'Downloaded apps'.\n" +
                                   "3. Tap 'NISU AI Phone Assistant'.\n" +
                                   "4. Toggle switch to ON and accept Android's confirmation prompt.\n" +
                                   "5. Return to NISU AI to start voice automation!",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticItem(
    title: String,
    detail: String,
    active: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            if (active) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (active) Color(0xFF00E676) else Color(0xFFFF5252),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ToggleSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = YesminCyan)
        )
    }
}
