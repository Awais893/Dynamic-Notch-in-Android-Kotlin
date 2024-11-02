package com.shadowarm.dynamicnotchbar.OverLayService

import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.shadowarm.dynamicnotchbar.R

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
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

        // Trigger animation when notification arrives
        registerNotificationObserver()
    }

    private fun registerNotificationObserver() {
        // Simulate notification arrival or replace this with actual listener logic
        animateOverlayExpansion()
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

            // Calculate the center offset
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels
            layoutParams.x = (screenWidth - animatedValue) / 2 // Center the overlay

            windowManager.updateViewLayout(overlayView, layoutParams)
        }
        animator.duration = 500 // Set duration for expansion and contraction
        animator.start()
    }

    private fun dpToPx(dp: Int): Int {
        val displayMetrics = resources.displayMetrics
        return (dp * displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(overlayView)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
