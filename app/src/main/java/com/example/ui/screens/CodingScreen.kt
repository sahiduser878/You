package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun CodingScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.HOME)
    }

    val context = LocalContext.current
    val generatedCode by viewModel.generatedCode.collectAsState()
    val codeExplanation by viewModel.codeExplanation.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    var selectedLanguage by remember { mutableStateOf("Kotlin") }
    var taskPrompt by remember { mutableStateOf("") }
    var showTermuxDialog by remember { mutableStateOf(false) }

    val languages = listOf("Kotlin", "Java", "Python", "JavaScript", "HTML/CSS", "C++", "Bash/Termux")

    if (showTermuxDialog) {
        TermuxGuideDialog(onDismiss = { showTermuxDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Coding Assistant & Termux",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.HOME) },
                        modifier = Modifier.testTag("coding_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = YesminCyan
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showTermuxDialog = true },
                        modifier = Modifier.testTag("coding_termux_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Termux Guide",
                            tint = YesminPink
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
            // --- Termux Banner ---
            item {
                Surface(
                    color = Color(0xFF141E33),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, YesminCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Build APK in Termux",
                                fontWeight = FontWeight.Bold,
                                color = YesminCyan,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Generate Android code & compile using mobile terminal commands.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = { showTermuxDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = YesminCyan)
                        ) {
                            Text("Guide", fontSize = 12.sp)
                        }
                    }
                }
            }

            // --- Language Chips ---
            item {
                Text(
                    text = "TARGET LANGUAGE",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(languages) { lang ->
                        FilterChip(
                            selected = selectedLanguage == lang,
                            onClick = { selectedLanguage = lang },
                            label = { Text(lang, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = YesminCyan,
                                selectedLabelColor = Color(0xFF001F28),
                                containerColor = SurfaceCard,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            // --- Prompt / Code Input ---
            item {
                FuturisticHudCard(
                    title = "Code Request or Debug",
                    icon = Icons.Default.Code,
                    glowColor = YesminPurple
                ) {
                    OutlinedTextField(
                        value = taskPrompt,
                        onValueChange = { taskPrompt = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp)
                            .testTag("code_prompt_input"),
                        placeholder = {
                            Text(
                                "Describe what code you want to build or paste broken code to debug...",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YesminPurple,
                            unfocusedBorderColor = Color(0xFF263352),
                            focusedContainerColor = Color(0xFF0B101D),
                            unfocusedContainerColor = Color(0xFF0B101D)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (taskPrompt.isNotBlank()) {
                                    viewModel.generateCode(taskPrompt, selectedLanguage, isDebugging = false)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("generate_code_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YesminCyan,
                                contentColor = Color(0xFF001F28)
                            ),
                            enabled = !isGenerating && taskPrompt.isNotBlank()
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF001F28))
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate Code", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                if (taskPrompt.isNotBlank()) {
                                    viewModel.generateCode(taskPrompt, selectedLanguage, isDebugging = true)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("debug_code_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = YesminPink),
                            enabled = !isGenerating && taskPrompt.isNotBlank()
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Debug / Fix", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- Generated Code Display ---
            if (generatedCode != null) {
                item {
                    FuturisticHudCard(
                        title = "$selectedLanguage Implementation",
                        icon = Icons.Default.Terminal,
                        glowColor = YesminCyan,
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clip.setPrimaryClip(ClipData.newPlainText("Code", generatedCode ?: ""))
                                    Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Code",
                                    tint = YesminCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF080C14), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            SelectionContainer {
                                Text(
                                    text = generatedCode ?: "",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = Color(0xFF80D8FF),
                                    modifier = Modifier.horizontalScroll(rememberScrollState())
                                )
                            }
                        }

                        if (!codeExplanation.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "EXPLANATION & ARCHITECTURE:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = YesminPink
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            SelectionContainer {
                                Text(
                                    text = codeExplanation ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}
