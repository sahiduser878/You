package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SettingsManager
import com.example.overlay.RgbBorderOverlayService
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.FuturisticHudCard
import com.example.ui.components.NisuEdgeGlowBorder
import com.example.ui.theme.BackgroundObsidian
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDarkNavy
import com.example.ui.theme.YesminCyan
import com.example.ui.theme.YesminPink
import com.example.ui.theme.YesminPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdgeGlowScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.HOME)
    }

    val context = LocalContext.current
    val settings = viewModel.settingsManager

    var isGlowEnabled by remember { mutableStateOf(settings.isRgbBorderEnabled()) }
    var selectedMode by remember { mutableStateOf(settings.getRgbBorderMode()) }
    var selectedPalette by remember { mutableStateOf(settings.getRgbBorderPalette()) }
    var speed by remember { mutableFloatStateOf(settings.getRgbBorderSpeed()) }
    var brightness by remember { mutableFloatStateOf(settings.getRgbBorderBrightness()) }
    var strokeWidth by remember { mutableFloatStateOf(settings.getRgbBorderWidth()) }

    var hasOverlayPermission by remember {
        mutableStateOf(RgbBorderOverlayService.isOverlayPermissionGranted(context))
    }

    val audioRms by viewModel.voiceManager.rmsLevel.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundObsidian,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "RGB Edge Glow Customizer",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.HOME) },
                        modifier = Modifier.testTag("edgeglow_back_button")
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
            // Live Interactive Preview Box
            item {
                FuturisticHudCard(
                    title = "Live Edge Glow Preview",
                    icon = Icons.Default.AutoAwesome,
                    glowColor = YesminPink
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF070A10)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Embedded live edge glow preview
                        NisuEdgeGlowBorder(
                            enabled = isGlowEnabled,
                            mode = selectedMode,
                            palette = selectedPalette,
                            speed = speed,
                            brightness = brightness,
                            strokeWidth = strokeWidth.dp,
                            audioRms = audioRms
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "NISU AI GLOW",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                                color = YesminPink
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$selectedMode • $selectedPalette",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Speed: ${"%.1f".format(speed)}x | Width: ${strokeWidth.toInt()}dp",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Master Toggle & Overlay Option
            item {
                FuturisticHudCard(
                    title = "Glow Control",
                    icon = Icons.Default.Layers,
                    glowColor = YesminCyan
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Master RGB Edge Glow",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isGlowEnabled) "Glowing around screen edges" else "Disabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isGlowEnabled,
                            onCheckedChange = { checked ->
                                isGlowEnabled = checked
                                settings.setRgbBorderEnabled(checked)
                                if (checked && hasOverlayPermission) {
                                    RgbBorderOverlayService.start(context)
                                } else if (!checked) {
                                    RgbBorderOverlayService.stop(context)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = YesminCyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // System Overlay Permission Banner / Action
                    Surface(
                        color = SurfaceDarkNavy,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3300E5FF))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "System-Wide Overlay",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = YesminCyan
                                )
                                Text(
                                    text = if (hasOverlayPermission) "Permission granted: runs over all apps"
                                    else "Permission required to glow outside NISU app",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!hasOverlayPermission) {
                                Button(
                                    onClick = {
                                        RgbBorderOverlayService.openOverlaySettings(context)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = YesminCyan)
                                ) {
                                    Text("Grant", color = Color(0xFF001F28), fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0x3300E676), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF00E676),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Animation Mode Selector
            item {
                FuturisticHudCard(
                    title = "Animation Modes",
                    icon = Icons.Default.Animation,
                    glowColor = YesminPurple
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsManager.AVAILABLE_RGB_MODES.forEach { mode ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedMode = mode
                                        settings.setRgbBorderMode(mode)
                                    },
                                color = if (selectedMode == mode) YesminPurple.copy(alpha = 0.15f) else SurfaceDarkNavy,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selectedMode == mode) YesminPurple else Color(0x22FFFFFF)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = mode,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = if (selectedMode == mode) YesminPurple else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (selectedMode == mode) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = YesminPurple,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Color Palette Selector
            item {
                FuturisticHudCard(
                    title = "Color Palettes",
                    icon = Icons.Default.ColorLens,
                    glowColor = YesminPink
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsManager.AVAILABLE_RGB_PALETTES.forEach { palette ->
                            val indicatorColor = when (palette) {
                                SettingsManager.PALETTE_NEON_PINK -> Color(0xFFFF1493)
                                SettingsManager.PALETTE_CYBERPUNK_BLUE -> Color(0xFF00E5FF)
                                SettingsManager.PALETTE_WARM_SUNSET -> Color(0xFFFF512F)
                                SettingsManager.PALETTE_ELECTRIC_GREEN -> Color(0xFF00FF87)
                                else -> Color(0xFFFF0055)
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedPalette = palette
                                        settings.setRgbBorderPalette(palette)
                                    },
                                color = if (selectedPalette == palette) indicatorColor.copy(alpha = 0.15f) else SurfaceDarkNavy,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selectedPalette == palette) indicatorColor else Color(0x22FFFFFF)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(indicatorColor, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = palette,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                            color = if (selectedPalette == palette) indicatorColor else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    if (selectedPalette == palette) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = indicatorColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sliders: Speed, Thickness, Brightness
            item {
                FuturisticHudCard(
                    title = "Fine Adjustments",
                    icon = Icons.Default.Speed,
                    glowColor = YesminCyan
                ) {
                    // Speed
                    Text(
                        text = "Animation Speed: ${"%.1f".format(speed)}x",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = YesminCyan
                    )
                    Slider(
                        value = speed,
                        onValueChange = {
                            speed = it
                            settings.setRgbBorderSpeed(it)
                        },
                        valueRange = 0.5f..3.0f,
                        colors = SliderDefaults.colors(thumbColor = YesminCyan, activeTrackColor = YesminCyan)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Thickness
                    Text(
                        text = "Border Thickness: ${strokeWidth.toInt()}dp",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = YesminPink
                    )
                    Slider(
                        value = strokeWidth,
                        onValueChange = {
                            strokeWidth = it
                            settings.setRgbBorderWidth(it)
                        },
                        valueRange = 2f..12f,
                        colors = SliderDefaults.colors(thumbColor = YesminPink, activeTrackColor = YesminPink)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Brightness
                    Text(
                        text = "Glow Brightness: ${(brightness * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = YesminPurple
                    )
                    Slider(
                        value = brightness,
                        onValueChange = {
                            brightness = it
                            settings.setRgbBorderBrightness(it)
                        },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = YesminPurple, activeTrackColor = YesminPurple)
                    )
                }
            }
        }
    }
}
