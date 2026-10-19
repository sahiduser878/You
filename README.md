# NISU AI — Fast, Beautiful Female Voice Assistant & Phone Controller

NISU AI is a production-grade Android AI assistant combining a natural, soft, sweet, romantic-style female voice with real-time event-driven voice commands, deep Android Accessibility automation, live screen sharing & multimodal understanding, and fully customizable RGB edge glow.

## Key Features

### 1. Beautiful Female Voice (Zero Delay & Sweet Tone)
- **Natural, Sweet Romantic Female Voice**: Warm, gentle, affectionate tone with natural Hindi, Hinglish, and English pronunciation.
- **Voice Packs**:
  - *Romantic Sweet* (1.22x pitch, smooth relaxed pacing)
  - *Gentle Soft* (1.15x pitch, calm whisper)
  - *Warm Conversational* (1.10x pitch, engaging and natural)
  - *Expressive Melodic* (1.28x pitch, playful and melodic)
- **Voice Controls**: Rate slider, pitch slider, volume slider, and instant voice preview.
- **Ultra-Fast Playback**: Zero-latency local TTS engine + optional ElevenLabs Studio & Gemini TTS with immediate fallback.

### 2. Real-Time Voice Command Pipeline
- Instant listening on microphone tap without delays or blocking operations.
- Direct local command execution (< 100ms) for opening apps, adjusting brightness/volume, scrolling, navigating back/home, and WhatsApp messaging.
- Real-time status pipeline: `Listening` → `Processing` → `Executing` → `Completed`.
- Instant interrupt & stop speaking controls.

### 3. Accessibility & System-Wide Automation
- Native `OgSahidAccessibilityService` for system-wide control:
  - App launching (WhatsApp, YouTube, Settings, Camera, Chrome, Maps, Calculator).
  - UI element inspection & node tapping (`clickNodeByText`).
  - Text entry into focused fields (`typeTextIntoFocusedOrFirstInput`).
  - System gestures: Back, Home, Recents, Notifications, Scroll Up, Scroll Down.
- Multi-step WhatsApp messaging workflow with user confirmation safety.
- Dedicated Accessibility screen with diagnostics checklist and interactive test runner.

### 4. Screen Sharing & Understanding
- Android `MediaProjection` and Accessibility screenshot pipeline.
- Live frame preview thumbnail with instant snapshot capture.
- Quick AI actions:
  - **Analyze Screen**: Full multimodal description of active screen content.
  - **Summarize Page**: Extracts and condenses visible text and articles.
  - **Find Elements**: Locates interactive buttons, fields, and links.
- Privacy-first: frames only captured on user request, zero third-party telemetry.

### 5. Customizable RGB Edge Glow
- Stunning animated glow along screen edges in-app and as a system overlay.
- Animation modes: *Rainbow Wave*, *Breathing Glow*, *Pulse*, *Static Gradient*, *Sound Reactive*.
- Color palettes: *Rainbow*, *Neon Pink*, *Cyberpunk Blue*, *Warm Sunset*, *Electric Green*.
- Interactive controls for speed, thickness, and brightness.
