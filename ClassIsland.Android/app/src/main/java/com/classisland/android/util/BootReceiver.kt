package com.classisland.android.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.classisland.android.overlay.OverlayService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        if (i.action == Intent.ACTION_BOOT_COMPLETED) {
            val s = SettingsManager.get(c).load()
            if (s.autoStartEnabled && s.enableOverlay) OverlayService.start(c)
        }
    }
}