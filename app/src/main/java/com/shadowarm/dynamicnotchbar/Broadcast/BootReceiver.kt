package com.shadowarm.dynamicnotchbar.Broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.shadowarm.dynamicnotchbar.OverLayService.OverlayService
//Boot
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, OverlayService::class.java)
            context?.startService(serviceIntent)
        }
    }
}


class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            Log.d("BootChecking", "onReceive: Called") // Check if this log appears
            val notificationIntent = Intent("com.shadowarm.dynamicnotchbar.NOTIFICATION_RECEIVED")
            it.sendBroadcast(notificationIntent)
        }
    }
}
