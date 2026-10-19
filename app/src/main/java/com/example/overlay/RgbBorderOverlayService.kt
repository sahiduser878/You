package com.example.overlay

import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.SweepGradient
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import androidx.core.app.NotificationCompat
import com.example.data.local.SettingsManager

class RgbBorderOverlayService : Service() {

    companion object {
        const val ACTION_START = "com.example.overlay.START"
        const val ACTION_STOP = "com.example.overlay.STOP"
        const val ACTION_UPDATE_CONFIG = "com.example.overlay.UPDATE"

        @Volatile
        var isServiceRunning: Boolean = false
            private set

        fun isOverlayPermissionGranted(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }

        fun openOverlaySettings(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        }

        fun start(context: Context) {
            val intent = Intent(context, RgbBorderOverlayService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, RgbBorderOverlayService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var windowManager: WindowManager? = null
    private var borderView: RgbBorderView? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        startForegroundNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                removeOverlay()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START, ACTION_UPDATE_CONFIG -> {
                showOverlay()
            }
        }
        return START_STICKY
    }

    private fun showOverlay() {
        if (!isOverlayPermissionGranted(this)) {
            stopSelf()
            return
        }

        if (borderView == null && windowManager != null) {
            val settings = SettingsManager(this)
            val strokeWidthDp = settings.getRgbBorderWidth()
            val brightness = settings.getRgbBorderBrightness()
            val speedFactor = settings.getRgbBorderSpeed()

            borderView = RgbBorderView(this, strokeWidthDp, brightness, speedFactor)

            val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            // CRITICAL: FLAG_NOT_TOUCHABLE ensures screen touches are NEVER blocked!
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 0
                y = 0
            }

            try {
                windowManager?.addView(borderView, params)
                isServiceRunning = true
            } catch (e: Exception) {
                isServiceRunning = false
            }
        } else if (borderView != null) {
            val settings = SettingsManager(this)
            borderView?.updateConfig(
                settings.getRgbBorderWidth(),
                settings.getRgbBorderBrightness(),
                settings.getRgbBorderSpeed()
            )
        }
    }

    private fun removeOverlay() {
        if (borderView != null && windowManager != null) {
            try {
                windowManager?.removeView(borderView)
            } catch (_: Exception) {}
            borderView?.destroy()
            borderView = null
        }
        isServiceRunning = false
    }

    private fun startForegroundNotification() {
        val channelId = "rgb_border_service_channel"
        val channelName = "NISU AI RGB Border"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("NISU AI Active")
            .setContentText("Rainbow RGB edge border is running over apps")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1001, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

/**
 * High performance hardware-accelerated Rainbow RGB Edge border View
 */
class RgbBorderView(
    context: Context,
    private var strokeWidthDp: Float,
    private var brightness: Float,
    private var speedFactor: Float
) : View(context) {

    private fun getPaletteColors(palette: String): IntArray {
        return when (palette) {
            SettingsManager.PALETTE_NEON_PINK -> intArrayOf(
                Color.parseColor("#FF1493"), Color.parseColor("#FF69B4"),
                Color.parseColor("#FF007F"), Color.parseColor("#FF80BF"), Color.parseColor("#FF1493")
            )
            SettingsManager.PALETTE_CYBERPUNK_BLUE -> intArrayOf(
                Color.parseColor("#00E5FF"), Color.parseColor("#0077FF"),
                Color.parseColor("#7000FF"), Color.parseColor("#00E5FF")
            )
            SettingsManager.PALETTE_WARM_SUNSET -> intArrayOf(
                Color.parseColor("#FF512F"), Color.parseColor("#DD2476"),
                Color.parseColor("#FF8800"), Color.parseColor("#FF512F")
            )
            SettingsManager.PALETTE_ELECTRIC_GREEN -> intArrayOf(
                Color.parseColor("#00FF87"), Color.parseColor("#60EFFF"),
                Color.parseColor("#00E676"), Color.parseColor("#00FF87")
            )
            else -> intArrayOf(
                Color.parseColor("#FF0055"),
                Color.parseColor("#FF6600"),
                Color.parseColor("#FFCC00"),
                Color.parseColor("#00FF66"),
                Color.parseColor("#00FFFF"),
                Color.parseColor("#0066FF"),
                Color.parseColor("#9900FF"),
                Color.parseColor("#FF0099"),
                Color.parseColor("#FF0055")
            )
        }
    }

    private var activeColors = getPaletteColors(SettingsManager(context).getRgbBorderPalette())

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private var currentPhase = 0f
    private var animator: ValueAnimator? = null

    init {
        updatePaint()
        startAnimation()
    }

    fun updateConfig(widthDp: Float, bright: Float, speed: Float) {
        val settings = SettingsManager(context)
        this.strokeWidthDp = widthDp
        this.brightness = bright
        this.speedFactor = speed
        this.activeColors = getPaletteColors(settings.getRgbBorderPalette())
        updatePaint()
        startAnimation()
        invalidate()
    }

    private fun updatePaint() {
        val density = resources.displayMetrics.density
        paint.strokeWidth = strokeWidthDp * density
        paint.alpha = (brightness * 255).toInt().coerceIn(0, 255)
    }

    private fun startAnimation() {
        animator?.cancel()
        val durationMs = (3000L / speedFactor.coerceAtLeast(0.2f)).toLong()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = durationMs
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { va ->
                currentPhase = va.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val halfStroke = paint.strokeWidth / 2f

        // Animated SweepGradient centered on screen for continuous smooth rainbow flow around all edges
        canvas.save()
        canvas.rotate(currentPhase * 360f, w / 2f, h / 2f)
        val shader = SweepGradient(w / 2f, h / 2f, activeColors, null)
        paint.shader = shader
        canvas.restore()

        // Draw rectangle strictly along the outer edges of the screen
        canvas.drawRect(
            halfStroke,
            halfStroke,
            w - halfStroke,
            h - halfStroke,
            paint
        )
    }

    fun destroy() {
        animator?.cancel()
        animator = null
    }
}
