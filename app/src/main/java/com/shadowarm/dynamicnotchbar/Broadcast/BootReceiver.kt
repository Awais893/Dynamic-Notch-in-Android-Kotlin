package com.shadowarm.dynamicnotchbar.Broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
