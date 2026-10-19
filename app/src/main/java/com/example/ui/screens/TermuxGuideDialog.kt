package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.YesminCyan

@Composable
fun TermuxGuideDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = YesminCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Termux APK Build Guide",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Step-by-step instructions to compile and build OG SAHID AI APK directly on your Android phone using Termux without a PC:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                CommandStepCard(
                    step = "1. Update Termux & Install Packages",
                    desc = "Install OpenJDK 17, git, unzip, wget, and gradle dependencies:",
                    command = "pkg update -y && pkg install -y openjdk-17 git wget unzip curl",
                    context = context
                )

                Spacer(modifier = Modifier.height(10.dp))

                CommandStepCard(
                    step = "2. Setup Android SDK Command-line Tools",
                    desc = "Set ANDROID_HOME and download sdkmanager:",
                    command = "mkdir -p ~/android-sdk/cmdline-tools\nexport ANDROID_HOME=~/android-sdk\nexport PATH=\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools",
                    context = context
                )

                Spacer(modifier = Modifier.height(10.dp))

                CommandStepCard(
                    step = "3. Extract or Clone Project",
                    desc = "Navigate to project directory:",
                    command = "cd ~/og-sahid-ai || cd ~/storage/downloads/og-sahid-ai",
                    context = context
                )

                Spacer(modifier = Modifier.height(10.dp))

                CommandStepCard(
                    step = "4. Grant Executable Permissions",
                    desc = "Ensure gradlew has execute rights:",
                    command = "chmod +x gradlew",
                    context = context
                )

                Spacer(modifier = Modifier.height(10.dp))

                CommandStepCard(
                    step = "5. Build Debug APK",
                    desc = "Run Gradle build with memory optimization for mobile CPUs:",
                    command = "./gradlew assembleDebug --no-daemon -Dorg.gradle.jvmargs=\"-Xmx1536m -XX:MaxMetaspaceSize=512m\"",
                    context = context
                )

                Spacer(modifier = Modifier.height(10.dp))

                CommandStepCard(
                    step = "6. Locate & Install APK",
                    desc = "Your installable APK will be at:",
                    command = "cp app/build/outputs/apk/debug/app-debug.apk /sdcard/Download/OGSahidAI.apk",
                    context = context
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Note: Once copied to Download folder, tap OGSahidAI.apk in your File Manager to install!",
                    style = MaterialTheme.typography.labelSmall,
                    color = YesminCyan
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = YesminCyan, contentColor = Color(0xFF001F28))
            ) {
                Text("Got It")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    val fullScript = """
pkg update -y && pkg install -y openjdk-17 git wget unzip curl
mkdir -p ~/android-sdk
export ANDROID_HOME=~/android-sdk
chmod +x gradlew
./gradlew assembleDebug --no-daemon -Dorg.gradle.jvmargs="-Xmx1536m"
cp app/build/outputs/apk/debug/app-debug.apk /sdcard/Download/OGSahidAI.apk
                    """.trimIndent()
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Termux Commands", fullScript))
                    Toast.makeText(context, "All commands copied to clipboard!", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text("Copy All Script")
            }
        },
        containerColor = SurfaceCard
    )
}

@Composable
fun CommandStepCard(step: String, desc: String, command: String, context: Context) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F1524), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(text = step, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = YesminCyan)
        Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF080C14), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = command,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFF81D4FA),
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText(step, command))
                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy command",
                        tint = YesminCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
