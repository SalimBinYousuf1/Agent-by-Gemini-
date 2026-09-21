package com.example.service

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class OverlayHelper(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentOverlayView: View? = null

    fun canDrawOverlays(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun showOverlayAlert(message: String) {
        if (!canDrawOverlays()) return

        mainHandler.post {
            try {
                removeCurrentOverlay()

                val density = context.resources.displayMetrics.density
                val padHoriz = (18 * density).toInt()
                val padVert = (14 * density).toInt()
                val radius = 16 * density

                val container = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor("#EE1C1C1E")) // Calm dark slate
                        cornerRadius = radius
                        setStroke((1 * density).toInt(), Color.parseColor("#33FFFFFF"))
                    }
                    setPadding(padHoriz, padVert, padHoriz, padVert)
                    elevation = 8 * density
                }

                val titleView = TextView(context).apply {
                    text = "salim accountability"
                    setTextColor(Color.parseColor("#8E8E93"))
                    textSize = 12f
                    setPadding(0, 0, 0, (4 * density).toInt())
                }

                val messageView = TextView(context).apply {
                    text = message
                    setTextColor(Color.WHITE)
                    textSize = 14f
                    maxLines = 3
                }

                container.addView(titleView)
                container.addView(messageView)

                container.setOnClickListener {
                    removeCurrentOverlay()
                }

                val layoutParams = WindowManager.LayoutParams(
                    (340 * density).toInt(),
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    y = (48 * density).toInt()
                }

                windowManager.addView(container, layoutParams)
                currentOverlayView = container

                // Auto-dismiss after 10 seconds
                mainHandler.postDelayed({
                    removeCurrentOverlay()
                }, 10_000L)

            } catch (e: Exception) {
                // Log and ignore if window token invalid or activity finished
            }
        }
    }

    private fun removeCurrentOverlay() {
        currentOverlayView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (_: Exception) {}
            currentOverlayView = null
        }
    }
}
