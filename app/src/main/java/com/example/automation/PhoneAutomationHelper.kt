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

    fun addCalendarEvent(context: Context, title: String, description: String): AutomationResult {
        return try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.DESCRIPTION, description)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult(true, "Calendar Event", "Opened calendar to schedule '$title'", requiresUserConfirmation = true)
        } catch (e: Exception) {
            AutomationResult(false, "Calendar Event", "Failed to create calendar event: ${e.localizedMessage}")
        }
    }
}
