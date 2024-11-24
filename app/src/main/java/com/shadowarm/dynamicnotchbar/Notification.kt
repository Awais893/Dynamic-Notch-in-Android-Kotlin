package com.shadowarm.dynamicnotchbar

import android.app.Notification
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.shadowarm.dynamicnotchbar.OverLayService.OverlayService
import java.io.File
import java.io.FileOutputStream

class MyNotificationListenerService : NotificationListenerService() {

    private val TAG = "NotificationListener"

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.notification ?: return
        val packageName = sbn.packageName

        val extras = notification.extras ?: return
        val notificationTitle = extras.getString(Notification.EXTRA_TITLE) ?: "No Title"
        val notificationText = extras.getString(Notification.EXTRA_TEXT) ?: "No Text"

        Log.d(TAG, "Notification Posted: Title: $notificationTitle, Text: $notificationText, Package: $packageName")

        val appIcon = getAppIcon(packageName)
        val appIconPath = appIcon?.let { saveBitmapToFile(it, "$packageName.png") }

        val overlayIntent = Intent(this, OverlayService::class.java).apply {
            putExtra("NOTIFICATION_TITLE", notificationTitle)
            putExtra("NOTIFICATION_TEXT", notificationText)
            putExtra("APP_ICON_PATH", appIconPath)
        }

        startService(overlayIntent)
    }

    private fun getAppIcon(packageName: String): Bitmap? {
        return try {
            val pm = packageManager
            val drawable = pm.getApplicationIcon(packageName)
            drawable.toBitmap()
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving app icon for package: $packageName", e)
            null
        }
    }

    private fun Drawable.toBitmap(): Bitmap {
        val width = if (intrinsicWidth > 0) intrinsicWidth else 100
        val height = if (intrinsicHeight > 0) intrinsicHeight else 100
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }

    private fun saveBitmapToFile(bitmap: Bitmap, fileName: String): String {
        val file = File(cacheDir, fileName)
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return file.absolutePath
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d(TAG, "Notification Removed: ${sbn.packageName}")
    }
}
