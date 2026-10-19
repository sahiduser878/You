package com.example.automation

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import android.provider.Settings
import com.example.accessibility.OgSahidAccessibilityService

data class AutomationResult(
    val success: Boolean,
    val actionName: String,
    val message: String,
    val requiresUserConfirmation: Boolean = false,
    val pendingAction: (() -> AutomationResult)? = null
)

object PhoneAutomationHelper {

    // --- WhatsApp Direct Workflows ---

    fun openWhatsApp(context: Context): AutomationResult {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                AutomationResult(true, "Open WhatsApp", "WhatsApp application launched.")
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.whatsapp.com")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                AutomationResult(true, "Open WhatsApp Web", "Opened WhatsApp Web in browser.")
            }
        } catch (e: Exception) {
            AutomationResult(false, "Open WhatsApp", "Could not open WhatsApp: ${e.localizedMessage}")
        }
    }

    fun prepareWhatsAppMessage(
        context: Context,
        contactName: String,
        message: String,
        confirmed: Boolean
    ): AutomationResult {
        if (!confirmed) {
            return AutomationResult(
                success = true,
                actionName = "WhatsApp Message Confirmation",
                message = "Confirm Sending: Send '$message' to contact '$contactName' on WhatsApp?",
                requiresUserConfirmation = true,
                pendingAction = {
                    executeWhatsAppSend(context, contactName, message)
                }
            )
        }
        return executeWhatsAppSend(context, contactName, message)
    }

    private fun executeWhatsAppSend(context: Context, contactName: String, message: String): AutomationResult {
        return try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_TEXT, message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (sendIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(sendIntent)
                // If accessibility is enabled, attempt to select contact
                OgSahidAccessibilityService.instance?.let { service ->
                    service.clickNodeByText(contactName)
                }
                AutomationResult(
                    true,
                    "WhatsApp Message Sent",
                    "Dispatched '$message' for '$contactName' on WhatsApp."
                )
            } else {
                // Fallback to general send intent
                val generalIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(Intent.createChooser(generalIntent, "Send message to $contactName").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
                AutomationResult(
                    true,
                    "Message Prepared",
                    "Opened messaging app chooser with text '$message'."
                )
            }
        } catch (e: Exception) {
            AutomationResult(false, "WhatsApp Send Failed", "Failed: ${e.localizedMessage}")
        }
    }

    // --- Accessibility Interactions ---

    fun performBack(): AutomationResult {
        val service = OgSahidAccessibilityService.instance
        return if (service != null) {
            val ok = service.performBack()
            AutomationResult(ok, "Go Back", if (ok) "Navigated back." else "Accessibility back action failed.")
        } else {
            AutomationResult(false, "Go Back", "Accessibility service is not enabled. Please enable it in Settings.")
        }
    }

    fun performHome(): AutomationResult {
        val service = OgSahidAccessibilityService.instance
        return if (service != null) {
            val ok = service.performHome()
            AutomationResult(ok, "Go Home", if (ok) "Returned to Home screen." else "Accessibility home action failed.")
        } else {
            AutomationResult(false, "Go Home", "Accessibility service is not enabled. Please enable it in Settings.")
        }
    }

    fun performScrollDown(): AutomationResult {
        val service = OgSahidAccessibilityService.instance
        return if (service != null) {
            val ok = service.scrollDown()
            AutomationResult(ok, "Scroll Down", if (ok) "Scrolled down visible content." else "No scrollable container detected.")
        } else {
            AutomationResult(false, "Scroll Down", "Accessibility service is not enabled. Please enable it in Settings.")
        }
    }

    fun performScrollUp(): AutomationResult {
        val service = OgSahidAccessibilityService.instance
        return if (service != null) {
            val ok = service.scrollUp()
            AutomationResult(ok, "Scroll Up", if (ok) "Scrolled up visible content." else "No scrollable container detected.")
        } else {
            AutomationResult(false, "Scroll Up", "Accessibility service is not enabled. Please enable it in Settings.")
        }
    }

    fun tapTargetByText(targetText: String): AutomationResult {
        val service = OgSahidAccessibilityService.instance
        return if (service != null) {
            val ok = service.clickNodeByText(targetText)
            AutomationResult(ok, "Tap Target", if (ok) "Tapped '$targetText'." else "Could not locate '$targetText' on current screen.")
        } else {
            AutomationResult(false, "Tap Target", "Accessibility service is not enabled. Please enable it in Settings.")
        }
    }

    fun enterText(text: String): AutomationResult {
        val service = OgSahidAccessibilityService.instance
        return if (service != null) {
            val ok = service.typeTextIntoFocusedOrFirstInput(text)
            AutomationResult(ok, "Type Text", if (ok) "Entered '$text' into active input field." else "No focused editable input field found.")
        } else {
            AutomationResult(false, "Type Text", "Accessibility service is not enabled. Please enable it in Settings.")
        }
    }

    // --- Standard Android Apps & Intents ---

    fun openYouTubeSearch(context: Context, query: String): AutomationResult {
        return try {
            val intent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra("query", query)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                AutomationResult(true, "YouTube Search", "Launched YouTube searching for '$query'")
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(query))).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                AutomationResult(true, "YouTube Web", "Opened YouTube in browser for '$query'")
            }
        } catch (e: Exception) {
            AutomationResult(false, "YouTube Search", "Failed to launch YouTube: ${e.localizedMessage}")
        }
    }

    fun openCamera(context: Context): AutomationResult {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult(true, "Camera", "Opened Camera application")
        } catch (e: Exception) {
            AutomationResult(false, "Camera", "Could not open camera: ${e.localizedMessage}")
        }
    }

    fun openSettings(context: Context): AutomationResult {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult(true, "Settings", "Opened Android System Settings")
        } catch (e: Exception) {
            AutomationResult(false, "Settings", "Failed to open settings: ${e.localizedMessage}")
        }
    }

    fun openAccessibilitySettings(context: Context): AutomationResult {
        return try {
            OgSahidAccessibilityService.openAccessibilitySettings(context)
            AutomationResult(true, "Accessibility Settings", "Navigated to Android Accessibility Settings")
        } catch (e: Exception) {
            AutomationResult(false, "Accessibility Settings", "Failed to open accessibility settings: ${e.localizedMessage}")
        }
    }

    fun openMapsSearch(context: Context, locationQuery: String): AutomationResult {
        return try {
            val gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(locationQuery))
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(mapIntent)
            AutomationResult(true, "Maps", "Launched Maps query for '$locationQuery'")
        } catch (e: Exception) {
            AutomationResult(false, "Maps", "Failed to open maps: ${e.localizedMessage}")
        }
    }

    fun setAlarm(context: Context, hour: Int, minute: Int, message: String): AutomationResult {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult(true, "Set Alarm", "Created alarm for %02d:%02d ('$message')".format(hour, minute))
        } catch (e: Exception) {
            AutomationResult(false, "Set Alarm", "Could not set alarm: ${e.localizedMessage}")
        }
    }

    fun setTimer(context: Context, seconds: Int, message: String): AutomationResult {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult(true, "Set Timer", "Started $seconds second timer ('$message')")
        } catch (e: Exception) {
            AutomationResult(false, "Set Timer", "Timer intent failed: ${e.localizedMessage}")
        }
    }

    fun openDialer(context: Context, phoneNumber: String): AutomationResult {
        return try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult(true, "Phone Dialer", "Opened dialer with $phoneNumber", requiresUserConfirmation = true)
        } catch (e: Exception) {
            AutomationResult(false, "Phone Dialer", "Failed to open dialer: ${e.localizedMessage}")
        }
    }

    fun openSms(context: Context, phoneNumber: String, body: String): AutomationResult {
        return try {
            val uri = Uri.parse("smsto:$phoneNumber")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult(true, "SMS Messaging", "Prepared SMS to $phoneNumber", requiresUserConfirmation = true)
        } catch (e: Exception) {
            AutomationResult(false, "SMS Messaging", "Failed to prepare SMS: ${e.localizedMessage}")
        }
    }

    fun openWebSearch(context: Context, query: String): AutomationResult {
        return try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult(true, "Web Search", "Launched search for '$query'")
        } catch (_: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
            AutomationResult(true, "Web Search", "Opened Google search in browser for '$query'")
        }
    }

    fun openApp(context: Context, appName: String): AutomationResult {
        val pm = context.packageManager
        val queryLower = appName.lowercase().trim()

        val packageMap = mapOf(
            "whatsapp" to "com.whatsapp",
            "youtube" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "camera" to "com.android.camera",
            "settings" to "com.android.settings",
            "calculator" to "com.google.android.calculator",
            "clock" to "com.google.android.deskclock",
            "maps" to "com.google.android.apps.maps",
            "gallery" to "com.google.android.apps.photos",
            "play store" to "com.android.vending",
            "gmail" to "com.google.android.gm"
        )

        val targetPkg = packageMap[queryLower]
        if (targetPkg != null) {
            val intent = pm.getLaunchIntentForPackage(targetPkg)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return AutomationResult(true, "Open $appName", "$appName opened successfully.")
            }
        }

        // Search installed applications
        try {
            val packages = pm.getInstalledApplications(0)
            for (app in packages) {
                val label = pm.getApplicationLabel(app).toString().lowercase()
                if (label == queryLower || label.contains(queryLower)) {
                    val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                    if (launchIntent != null) {
                        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(launchIntent)
                        return AutomationResult(true, "Open ${pm.getApplicationLabel(app)}", "${pm.getApplicationLabel(app)} launched.")
                    }
                }
            }
        } catch (_: Exception) {}

        return AutomationResult(false, "Open $appName", "Could not locate app '$appName' installed on this device.")
    }

    fun adjustVolume(context: Context, raise: Boolean): AutomationResult {
        return try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            val direction = if (raise) android.media.AudioManager.ADJUST_RAISE else android.media.AudioManager.ADJUST_LOWER
            am.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC, direction, android.media.AudioManager.FLAG_SHOW_UI)
            AutomationResult(true, "Volume Control", if (raise) "Volume increased." else "Volume decreased.")
        } catch (e: Exception) {
            AutomationResult(false, "Volume Control", "Could not adjust volume: ${e.localizedMessage}")
        }
    }

    fun setMuteVolume(context: Context, mute: Boolean): AutomationResult {
        return try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            val direction = if (mute) android.media.AudioManager.ADJUST_MUTE else android.media.AudioManager.ADJUST_UNMUTE
            am.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC, direction, android.media.AudioManager.FLAG_SHOW_UI)
            AutomationResult(true, "Mute Volume", if (mute) "Volume muted." else "Volume unmuted.")
        } catch (e: Exception) {
            AutomationResult(false, "Mute Volume", "Failed: ${e.localizedMessage}")
        }
    }

    fun openDisplaySettings(context: Context): AutomationResult {
        return try {
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult(true, "Display Settings", "Opened Display and Brightness settings.")
        } catch (e: Exception) {
            AutomationResult(false, "Display Settings", "Failed to open display settings: ${e.localizedMessage}")
        }
    }

    fun addCalendarEvent(context: Context, title: String, description: String): AutomationResult {
        return try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = android.provider.CalendarContract.Events.CONTENT_URI
                putExtra(android.provider.CalendarContract.Events.TITLE, title)
                putExtra(android.provider.CalendarContract.Events.DESCRIPTION, description)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult(true, "Calendar Event", "Opened calendar to schedule '$title'", requiresUserConfirmation = true)
        } catch (e: Exception) {
            AutomationResult(false, "Calendar Event", "Failed to create calendar event: ${e.localizedMessage}")
        }
    }
}
