package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiService
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.FuturisticHudCard
import com.example.ui.theme.BackgroundObsidian
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDarkNavy
import com.example.ui.theme.YesminCyan
import com.example.ui.theme.YesminPink
import com.example.ui.theme.YesminPurple
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.HOME)
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val geminiService = remember { GeminiService() }

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var visionPrompt by remember { mutableStateOf("Describe and explain this image in detail.") }
    var visionResult by remember { mutableStateOf<String?>(null) }
    var isAnalyzingVision by remember { mutableStateOf(false) }

    var textToTranslate by remember { mutableStateOf("") }
    var translationResult by remember { mutableStateOf<String?>(null) }
    var isTranslating by remember { mutableStateOf(false) }

    var docText by remember { mutableStateOf("") }
    var docSummaryResult by remember { mutableStateOf<String?>(null) }
    var isSummarizing by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            selectedBitmap = bitmap
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tools, Vision & Documents",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.HOME) },
                        modifier = Modifier.testTag("tools_back_button")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. Vision & Camera Image Understanding ---
            item {
                FuturisticHudCard(
                    title = "Vision: Image Understanding & OCR",
                    icon = Icons.Default.CameraAlt,
                    glowColor = YesminCyan
                ) {
                    Text(
                        text = "Select a photo or document to extract text and analyze with Gemini Multimodal AI.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (selectedBitmap != null) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(Color(0xFF0F1524), RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = YesminCyan)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick Image", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (selectedBitmap != null) {
                                    scope.launch {
                                        isAnalyzingVision = true
                                        visionResult = null
                                        val key = viewModel.settingsManager.getApiKey()
                                        val model = viewModel.settingsManager.getSelectedModel()
                                        val res = geminiService.sendMessage(
                                            apiKey = key,
                                            model = model,
                                            userPrompt = visionPrompt,
                                            recentHistory = emptyList(),
                                            memories = emptyList(),
                                            persona = "JARVIS",
                                            language = "English",
                                            bitmap = selectedBitmap
                                        )
                                        res.fold(
                                            onSuccess = { visionResult = it },
                                            onFailure = { visionResult = "Analysis error: ${it.message}" }
                                        )
                                        isAnalyzingVision = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = YesminCyan, contentColor = Color(0xFF001F28)),
                            enabled = selectedBitmap != null && !isAnalyzingVision
                        ) {
                            if (isAnalyzingVision) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF001F28))
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Analyze", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (visionResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF080C14), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            SelectionContainer {
                                Text(
                                    text = visionResult ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // --- 2. Translation & Language Assistant ---
            item {
                FuturisticHudCard(
                    title = "Translation & Hinglish Assistant",
                    icon = Icons.Default.Language,
                    glowColor = YesminPurple
                ) {
                    OutlinedTextField(
                        value = textToTranslate,
                        onValueChange = { textToTranslate = it },
                        label = { Text("Text to translate (English / Hindi / Hinglish)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YesminPurple,
                            focusedContainerColor = Color(0xFF0F1524),
                            unfocusedContainerColor = Color(0xFF0F1524)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (textToTranslate.isNotBlank()) {
                                scope.launch {
                                    isTranslating = true
                                    val key = viewModel.settingsManager.getApiKey()
                                    val model = viewModel.settingsManager.getSelectedModel()
                                    val prompt = "Translate and explain this in English, Hindi, and natural Hinglish:\n$textToTranslate"
                                    val res = geminiService.sendMessage(
                                        apiKey = key,
                                        model = model,
                                        userPrompt = prompt,
                                        recentHistory = emptyList(),
                                        memories = emptyList(),
                                        persona = "Balanced",
                                        language = "English"
                                    )
                                    res.fold(
                                        onSuccess = { translationResult = it },
                                        onFailure = { translationResult = "Error: ${it.message}" }
                                    )
                                    isTranslating = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YesminPurple, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isTranslating && textToTranslate.isNotBlank()
                    ) {
                        if (isTranslating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black)
                        } else {
                            Text("Translate Across English & Hindi", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (translationResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF080C14), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            SelectionContainer {
                                Text(
                                    text = translationResult ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // --- 3. Document & Study Summarizer ---
            item {
                FuturisticHudCard(
                    title = "Document & Article Summarizer",
                    icon = Icons.Default.DocumentScanner,
                    glowColor = YesminPink
                ) {
                    OutlinedTextField(
                        value = docText,
                        onValueChange = { docText = it },
                        label = { Text("Paste document notes, article, or essay") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YesminPink,
                            focusedContainerColor = Color(0xFF0F1524),
                            unfocusedContainerColor = Color(0xFF0F1524)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (docText.isNotBlank()) {
                                scope.launch {
                                    isSummarizing = true
                                    val key = viewModel.settingsManager.getApiKey()
                                    val model = viewModel.settingsManager.getSelectedModel()
                                    val prompt = "Provide a comprehensive, bulleted executive summary and 3 key takeaways for this text:\n$docText"
                                    val res = geminiService.sendMessage(
                                        apiKey = key,
                                        model = model,
                                        userPrompt = prompt,
                                        recentHistory = emptyList(),
                                        memories = emptyList(),
                                        persona = "JARVIS",
                                        language = "English"
                                    )
                                    res.fold(
                                        onSuccess = { docSummaryResult = it },
                                        onFailure = { docSummaryResult = "Error: ${it.message}" }
                                    )
                                    isSummarizing = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YesminPink),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSummarizing && docText.isNotBlank()
                    ) {
                        if (isSummarizing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text("Generate Summary & Study Points", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    if (docSummaryResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF080C14), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            SelectionContainer {
                                Text(
                                    text = docSummaryResult ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
