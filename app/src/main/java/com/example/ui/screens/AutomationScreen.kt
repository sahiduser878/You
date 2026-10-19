package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
fun AutomationScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.HOME)
    }

    val context = LocalContext.current
    val lastResult by viewModel.lastAutomationResult.collectAsState()

    var youtubeQuery by remember { mutableStateOf("Lo-Fi Beats for Coding") }
    var phoneNumber by remember { mutableStateOf("1234567890") }
    var smsBody by remember { mutableStateOf("Hey, sent via OG SAHID AI!") }
    var alarmMessage by remember { mutableStateOf("Wake Up with OG SAHID AI") }
    var timerSeconds by remember { mutableStateOf("120") }
    var mapQuery by remember { mutableStateOf("Coffee shop near me") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Phone Automation Engine",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.HOME) },
                        modifier = Modifier.testTag("automation_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = YesminCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDarkNavy
                )
            )
        },
        containerColor = BackgroundObsidian
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Confirmation Result Banner
            if (lastResult != null) {
                item {
                    Surface(
                        color = if (lastResult!!.success) Color(0xFF00382E) else Color(0xFF3E1212),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (lastResult!!.success) YesminCyan else Color(0xFFFF5252)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lastResult!!.actionName,
                                    fontWeight = FontWeight.Bold,
                                    color = if (lastResult!!.success) YesminCyan else Color(0xFFFF5252),
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = lastResult!!.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { viewModel.dismissAutomationBanner() }) {
                                Text("✕", color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            // Quick App Launchers
            item {
                FuturisticHudCard(
                    title = "System App Controls",
                    icon = Icons.Default.Settings,
                    glowColor = YesminCyan
                ) {
                    Text(
                        text = "Direct Android system intents with verified confirmation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { PhoneAutomationHelper.openCamera(context) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = YesminCyan)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Camera", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { PhoneAutomationHelper.openSettings(context) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = YesminCyan)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Settings", fontSize = 12.sp)
                        }
                    }
                }
            }

            // YouTube Search & Music
            item {
                FuturisticHudCard(
                    title = "YouTube & Music Player",
                    icon = Icons.Default.PlayCircle,
                    glowColor = Color(0xFFFF0000)
                ) {
                    OutlinedTextField(
                        value = youtubeQuery,
                        onValueChange = { youtubeQuery = it },
                        label = { Text("Song or Video Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF0000),
                            unfocusedBorderColor = Color(0xFF263352),
                            focusedContainerColor = Color(0xFF0F1524),
                            unfocusedContainerColor = Color(0xFF0F1524)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val res = PhoneAutomationHelper.openYouTubeSearch(context, youtubeQuery)
                            Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Search & Play on YouTube", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Alarms & Timers
            item {
                FuturisticHudCard(
                    title = "Alarms & Timers",
                    icon = Icons.Default.Alarm,
                    glowColor = YesminPurple
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = alarmMessage,
                            onValueChange = { alarmMessage = it },
                            label = { Text("Alarm Note") },
                            modifier = Modifier.weight(1.5f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = YesminPurple,
                                focusedContainerColor = Color(0xFF0F1524),
                                unfocusedContainerColor = Color(0xFF0F1524)
                            )
                        )
                        Button(
                            onClick = {
                                val res = PhoneAutomationHelper.setAlarm(context, 7, 0, alarmMessage)
                                Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YesminPurple),
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                        ) {
                            Text("Set 7 AM", fontSize = 12.sp, color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = timerSeconds,
                            onValueChange = { timerSeconds = it },
                            label = { Text("Timer (Seconds)") },
                            modifier = Modifier.weight(1.5f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = YesminPurple,
                                focusedContainerColor = Color(0xFF0F1524),
                                unfocusedContainerColor = Color(0xFF0F1524)
                            )
                        )
                        Button(
                            onClick = {
                                val sec = timerSeconds.toIntOrNull() ?: 60
                                val res = PhoneAutomationHelper.setTimer(context, sec, "Yesmin Timer")
                                Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YesminPurple),
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                        ) {
                            Text("Start Timer", fontSize = 12.sp, color = Color.Black)
                        }
                    }
                }
            }

            // Calls & SMS (with user confirmation guard)
            item {
                FuturisticHudCard(
                    title = "Calls & Messaging (User Confirmed)",
                    icon = Icons.Default.Phone,
                    glowColor = YesminPink
                ) {
                    Text(
                        text = "Android security requires user confirmation before initiating real calls or sending SMS.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YesminPink,
                            focusedContainerColor = Color(0xFF0F1524),
                            unfocusedContainerColor = Color(0xFF0F1524)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val res = PhoneAutomationHelper.openDialer(context, phoneNumber)
                                Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YesminPink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open Dialer", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val res = PhoneAutomationHelper.openSms(context, phoneNumber, smsBody)
                                Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = YesminPink),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open SMS", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Maps & Calendar
            item {
                FuturisticHudCard(
                    title = "Maps & Calendar",
                    icon = Icons.Default.Map,
                    glowColor = YesminCyan
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = mapQuery,
                            onValueChange = { mapQuery = it },
                            label = { Text("Location Query") },
                            modifier = Modifier.weight(1.5f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = YesminCyan,
                                focusedContainerColor = Color(0xFF0F1524),
                                unfocusedContainerColor = Color(0xFF0F1524)
                            )
                        )
                        Button(
                            onClick = {
                                PhoneAutomationHelper.openMapsSearch(context, mapQuery)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YesminCyan, contentColor = Color(0xFF001F28)),
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                        ) {
                            Text("Search Map", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            PhoneAutomationHelper.addCalendarEvent(context, "Meeting with Yesmin AI", "Review AI Companion roadmap")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = YesminCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Calendar Event Intent")
                    }
                }
            }

            // Accessibility Mode Guide
            item {
                FuturisticHudCard(
                    title = "Accessibility Screen Automation",
                    icon = Icons.Default.AccessibilityNew,
                    glowColor = YesminPurple
                ) {
                    Text(
                        text = "Android restricts background screen taps unless the user manually enables an Accessibility Service in Android Settings. Yesmin adheres to Google Play privacy safeguards.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            PhoneAutomationHelper.openAccessibilitySettings(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YesminPurple, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Accessibility Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
