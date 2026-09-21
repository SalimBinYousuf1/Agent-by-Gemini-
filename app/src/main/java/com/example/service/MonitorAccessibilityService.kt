package com.example.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.model.ScreenContext
import com.example.engine.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MonitorAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Session tracking
    private var currentPackageName: String = ""
    private var currentSessionStartTime: Long = 0L
    private var lastEmittedTime: Long = 0L

    // Debounce tracking
    private var pendingDebounceJob: kotlinx.coroutines.Job? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "MonitorAccessibilityService connected")
        // Start foreground service if not already started
        MonitorForegroundService.start(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }

        val packageName = event.packageName?.toString() ?: return
        if (packageName.isBlank() || packageName == "android") return

        val now = System.currentTimeMillis()

        // Continuous session duration tracking
        if (packageName != currentPackageName) {
            currentPackageName = packageName
            currentSessionStartTime = now
        }

        val sessionDurationSeconds = (now - currentSessionStartTime) / 1000

        // Debounce events: 2.5 second minimum gap
        val timeSinceLastEmit = now - lastEmittedTime
        if (timeSinceLastEmit < DEBOUNCE_MILLIS) {
            pendingDebounceJob?.cancel()
            pendingDebounceJob = serviceScope.launch {
                delay(DEBOUNCE_MILLIS - timeSinceLastEmit)
                inspectAndEmitScreen(packageName)
            }
            return
        }

        inspectAndEmitScreen(packageName)
    }

    private fun inspectAndEmitScreen(packageName: String) {
        val now = System.currentTimeMillis()
        lastEmittedTime = now

        val sessionDurationSeconds = (now - currentSessionStartTime) / 1000

        val rootNode = try {
            rootInActiveWindow
        } catch (e: Exception) {
            null
        }

        var windowTitle = ""
        val textSnippets = mutableListOf<String>()

        if (rootNode != null) {
            try {
                windowTitle = rootNode.text?.toString() ?: ""
                extractVisibleText(
                    node = rootNode,
                    results = textSnippets,
                    currentDepth = 0,
                    maxDepth = 5,
                    maxNodes = 30
                )
            } catch (e: Exception) {
                Log.w(TAG, "Error traversing accessibility tree: ${e.message}")
            } finally {
                try {
                    rootNode.recycle()
                } catch (_: Exception) {}
            }
        }

        val combinedSnippet = textSnippets.joinToString(" | ")

        val screenContext = ScreenContext(
            packageName = packageName,
            windowTitle = windowTitle,
            visibleTextSnippet = combinedSnippet,
            sessionDurationSeconds = sessionDurationSeconds,
            timestamp = now
        )

        EventBus.emitScreenContext(screenContext)
    }

    /**
     * Traverses the node tree up to a conservative maxDepth and maxNodes for performance.
     * Skips password fields and empty views.
     */
    private fun extractVisibleText(
        node: AccessibilityNodeInfo?,
        results: MutableList<String>,
        currentDepth: Int,
        maxDepth: Int,
        maxNodes: Int
    ) {
        if (node == null || results.size >= maxNodes || currentDepth > maxDepth) return

        // Respect privacy: never extract password fields
        if (node.isPassword) return

        val text = node.text?.toString()?.trim()
        val desc = node.contentDescription?.toString()?.trim()

        if (!text.isNullOrBlank() && text.length in 2..250) {
            results.add(text)
        } else if (!desc.isNullOrBlank() && desc.length in 2..250) {
            results.add(desc)
        }

        for (i in 0 until node.childCount) {
            if (results.size >= maxNodes) break
            val child = try {
                node.getChild(i)
            } catch (e: Exception) {
                null
            }
            if (child != null) {
                extractVisibleText(child, results, currentDepth + 1, maxDepth, maxNodes)
                try {
                    child.recycle()
                } catch (_: Exception) {}
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "MonitorAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.i(TAG, "MonitorAccessibilityService destroyed")
    }

    companion object {
        private const val TAG = "SalimAccessibility"
        private const val DEBOUNCE_MILLIS = 2500L
    }
}
