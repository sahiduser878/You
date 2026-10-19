# OG SAHID AI — Complete Android Assistant & Phone Controller

OG SAHID AI is a comprehensive, production-grade Android assistant combining a natural, warm, expressive female voice with system-wide phone control via Accessibility and Intents, a floating animated RGB edge border, coding assistance, and persistent memory.

## Features Overview

### 1. Natural Female Voice Engine (Highest Priority)
- **Natural, Expressive Female Persona**: Friendly, caring, conversational tone with realistic pauses and smooth pacing. Clear pronunciation in **Hindi**, **Hinglish**, and **English**.
- **Voice Provider Options**:
  - **Gemini TTS**: Native audio generation using Gemini audio models.
  - **ElevenLabs TTS**: Direct integration with ElevenLabs REST API with masked API key input, customizable Voice ID, Model ID, stability and similarity sliders, test voice button, and preview.
  - **System Fallback**: Android system TextToSpeech fallback if offline or no keys are configured.
- **Audio Pipeline Features**:
  - Stops speaking immediately on user interrupt or cancel.
  - Prevents repeated speech and audio overlapping.
  - Full error diagnosis for network issues, invalid keys, or quota limits.

### 2. Full Android Control Through Voice & Actions
- **Voice Command Parsing**: Converts spoken commands into structured actions.
- **Native AccessibilityService (`OgSahidAccessibilityService`)**:
  - Authorized UI interaction: Tapping visible buttons by text, scrolling down/up, navigating Back and Home.
  - Text input into active fields.
- **WhatsApp Workflow** (e.g. *"WhatsApp kholo, Mom ki chat select karo aur Hi bhejo"*):
  1. Opens WhatsApp.
  2. Resolves contact target.
  3. Composes message.
  4. Presents a confirmation prompt before sending to prevent accidental messages.
- **Application Launching**: Direct intents for WhatsApp, YouTube, Settings, Camera, Maps, Phone dialer, and SMS.
- **Live Action Status**: Status HUD cards showing executed and pending actions with confirm/cancel buttons.

### 3. Accessibility & Phone Control Settings
- Dedicated Accessibility settings section with real-time status check.
- "Enable Service" button navigating straight to Android Accessibility Settings.
- Quick test action (simulating Back navigation).
- Clear troubleshooting instructions for user activation.

### 4. Floating Overlay & RGB Border
- System alert window overlay (`RgbBorderOverlayService`) rendering a smooth animated rainbow gradient along the device screen edges.
- Toggle switch to start/stop the border service.
- Real-time controls for border width, brightness, and animation cycle speed.
- Overlay permission check and deep-link to Android settings.

### 5. AI Brain & Local Database
- Official Google Gemini model selection (`gemini-3.5-flash`, `gemini-3.1-pro-preview`, etc.).
- Masked API key management with latency connection testing.
- Room SQLite database (`memories` and `chat_messages`) for continuous memory retention.
- Multi-modal image analysis and OCR tools.

---

## How to Build the APK in Termux (On Android Phone)

Follow these commands inside the **Termux** app:

```bash
# 1. Update Termux and install tools
pkg update -y && pkg install -y openjdk-17 git wget unzip curl

# 2. Grant storage permissions
termux-setup-storage

# 3. Navigate to the project directory
cd ~/og-sahid-ai || cd ~/storage/downloads/og-sahid-ai

# 4. Give executable rights to gradle wrapper
chmod +x gradlew

# 5. Build Debug APK (with memory optimization for mobile CPUs)
./gradlew assembleDebug --no-daemon -Dorg.gradle.jvmargs="-Xmx1536m -XX:MaxMetaspaceSize=512m"

# 6. Copy output APK to your Download folder
cp app/build/outputs/apk/debug/app-debug.apk /sdcard/Download/OGSahidAI.apk
```
Open **Files** on your phone, go to **Downloads**, tap **OGSahidAI.apk**, and install.
