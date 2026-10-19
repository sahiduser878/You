package com.example.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo

class OgSahidAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "OgSahidAccessService"

        @Volatile
        var instance: OgSahidAccessibilityService? = null
            private set

        val isRunning: Boolean
            get() = instance != null

        fun isAccessibilityPermissionGranted(context: Context): Boolean {
            if (isRunning) return true
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
            val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            val expectedPackage = context.packageName
            for (service in enabledServices) {
                if (service.resolveInfo.serviceInfo.packageName == expectedPackage) {
                    return true
                }
            }
            // Secondary check via Settings.Secure
            val enabled = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
            return enabled.contains(expectedPackage)
        }

        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "OG SAHID AI Accessibility Service Connected")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "OG SAHID AI Accessibility Service Destroyed")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Active window observation for target detection
    }

    override fun onInterrupt() {
        Log.d(TAG, "OG SAHID AI Accessibility Service Interrupted")
    }

    // --- System Global Navigation ---

    fun performBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun performHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun performRecents(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_RECENTS)
    }

    fun performNotifications(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    }

    // --- Screen Node Interactions ---

    fun scrollDown(): Boolean {
        val root = rootInActiveWindow ?: return false
        val scrollableNode = findScrollableNode(root)
        val result = scrollableNode?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) ?: false
        root.recycle()
        return result
    }

    fun scrollUp(): Boolean {
        val root = rootInActiveWindow ?: return false
        val scrollableNode = findScrollableNode(root)
        val result = scrollableNode?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) ?: false
        root.recycle()
        return result
    }

    private fun findScrollableNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (root.isScrollable) return root
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val found = findScrollableNode(child)
            if (found != null) return found
        }
        return null
    }

    fun clickNodeByText(targetText: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(targetText)
        if (nodes.isNullOrEmpty()) {
            root.recycle()
            return false
        }

        for (node in nodes) {
            var clickableNode: AccessibilityNodeInfo? = node
            while (clickableNode != null && !clickableNode.isClickable) {
                clickableNode = clickableNode.parent
            }
            if (clickableNode != null && clickableNode.isClickable) {
                val success = clickableNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                root.recycle()
                return success
            }
        }
        root.recycle()
        return false
    }

    fun typeTextIntoFocusedOrFirstInput(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val targetNode = findFocusedOrEditableNode(root)
        if (targetNode == null) {
            root.recycle()
            return false
        }

        val arguments = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        val result = targetNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        root.recycle()
        return result
    }

    private fun findFocusedOrEditableNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (focused != null && focused.isEditable) return focused

        if (root.isEditable) return root
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val found = findFocusedOrEditableNode(child)
            if (found != null) return found
        }
        return null
    }

    // --- WhatsApp Workflow Assistance ---

    fun prepareWhatsAppMessage(contactName: String, message: String): String {
        // WhatsApp search or direct chat intent preparation
        return "Target Contact: '$contactName' | Message: '$message'\nReady to open WhatsApp and compose. Please confirm to send."
    }
}
