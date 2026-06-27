package com.classisland.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.classisland.android.util.Constants

class ClassIslandApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel(
                Constants.OVERLAY_CHANNEL_ID, "悬浮窗服务", NotificationManager.IMPORTANCE_LOW
            ).apply { setShowBadge(false) })
            nm.createNotificationChannel(NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID, "课程通知", NotificationManager.IMPORTANCE_HIGH
            ).apply { enableVibration(true) })
        }
    }

    companion object {
        lateinit var instance: ClassIslandApp
            private set
    }
}