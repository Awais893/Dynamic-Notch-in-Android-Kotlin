package com.shadowarm.dynamicnotchbar

import android.app.Notification
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.content.pm.PackageManager
import java.io.ByteArrayOutputStream

class MyNotificationListenerService : NotificationListenerService() {

    private val TAG = "NotificationListener"

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Get notification details
        val notification = sbn.notification ?: return
        val packageName = sbn.packageName

        // Get notification title and text
        val notificationTitle = notification.extras.getString(Notification.EXTRA_TITLE) ?: "No Title"
        val notificationText = notification.extras.getString(Notification.EXTRA_TEXT) ?: "No Text"

        Log.d(TAG, "Notification Posted: Title: $notificationTitle, Text: $notificationText, Package: $packageName")

        // Get the app icon as Bitmap
        val appIcon = getAppIcon(packageName)

        // Convert the Bitmap to ByteArray
        val appIconByteArray = appIcon?.let { bitmapToByteArray(it) }

        // Create an intent to start the overlay service
        val overlayIntent = Intent(this, OVERLAY_SERVICE::class.java).apply {
            putExtra("NOTIFICATION_TITLE", notificationTitle)
            putExtra("NOTIFICATION_TEXT", notificationText)
            putExtra("APP_ICON", appIconByteArray)  // Pass the ByteArray instead of Bitmap
        }

        startService(overlayIntent)
    }

    private fun getAppIcon(packageName: String): Bitmap? {
        return try {
            val pm = packageManager
            val drawable = pm.getApplicationIcon(packageName)
            drawable.toBitmap()  // Convert Drawable to Bitmap
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Error retrieving app icon for package: $packageName", e)
            null
        }
    }

    // Convert Drawable to Bitmap
    private fun Drawable.toBitmap(): Bitmap {
        val width = if (intrinsicWidth <= 0) 100 else intrinsicWidth
        val height = if (intrinsicHeight <= 0) 100 else intrinsicHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }

    // Convert Bitmap to ByteArray
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Handle notification removal if needed
        Log.d(TAG, "Notification Removed: ${sbn.packageName}")
    }
}
