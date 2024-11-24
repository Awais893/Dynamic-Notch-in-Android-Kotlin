package com.shadowarm.dynamicnotchbar.OverLayService

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.shadowarm.dynamicnotchbar.R

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var notificationReceiver: BroadcastReceiver
    private val CHANNEL_ID = "OverlayServiceChannel"
    private val initialWidth = 232 // Initial width in dp
    private val expandedWidth = 270 // Expanded width in dp

    override fun onCreate() {
        super.onCreate()

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        // Start foreground service notification
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Overlay Service")
            .setContentText("Overlay is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your icon
            .build()

        startForeground(1, notification)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        // Get display metrics to calculate center position
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        // Center the overlay in the status bar
        layoutParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        layoutParams.y = 16 // Adjust top margin if needed

        windowManager.addView(overlayView, layoutParams)

        // Register receiver to listen for notification events
        registerNotificationObserver()
    }

    private fun registerNotificationObserver() {
        notificationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d("NotificationReceiver", "Broadcast received with action: ${intent.action}")
                animateOverlayExpansion()
            }


        }

        val filter = IntentFilter("com.shadowarm.dynamicnotchbar.NOTIFICATION_RECEIVED")
        registerReceiver(notificationReceiver, filter)
    }

    private fun animateOverlayExpansion() {
        val layoutParams = overlayView.layoutParams as WindowManager.LayoutParams
        val startWidth = dpToPx(initialWidth)
        val endWidth = dpToPx(expandedWidth)

        // Animation to expand and contract from the center
        val animator = ValueAnimator.ofInt(startWidth, endWidth, startWidth)
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int

            // Update the width
            layoutParams.width = animatedValue

            // Keep overlay centered horizontally
            layoutParams.x = 0

            windowManager.updateViewLayout(overlayView, layoutParams)
        }

        // Add scaling effect to the overlay view
        overlayView.animate()
            .scaleX(1.1f) // Slight horizontal scaling
            .scaleY(1.1f) // Slight vertical scaling
            .setDuration(250) // Duration for scaling effect
            .withEndAction {
                overlayView.animate()
                    .scaleX(1f) // Reset to original scale
                    .scaleY(1f)
                    .setDuration(250)
                    .start()
            }
            .start()

        animator.duration = 500 // Set duration for expansion and contraction
        animator.start()
    }

    private fun dpToPx(dp: Int): Int {
        val displayMetrics = resources.displayMetrics
        return (dp * displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationReceiver)
        windowManager.removeView(overlayView)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
