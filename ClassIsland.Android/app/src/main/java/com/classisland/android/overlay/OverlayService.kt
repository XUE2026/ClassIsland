package com.classisland.android.overlay

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.FrameLayout
import androidx.compose.runtime.mutableStateOf
import com.classisland.android.MainActivity
import com.classisland.android.model.AppSettings
import com.classisland.android.util.Constants
import com.classisland.android.util.SettingsManager

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var view: OverlayView? = null

    override fun onCreate() { wm = getSystemService(WINDOW_SERVICE) as WindowManager }

    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int {
        startForeground(Constants.FOREGROUND_NOTIFICATION_ID, notif())
        if (view == null) show()
        return START_STICKY
    }

    private fun notif(): Notification {
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val b = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, Constants.OVERLAY_CHANNEL_ID) else Notification.Builder(this)
        return b.setContentTitle("ClassIsland").setContentText("课表悬浮窗运行中").setSmallIcon(android.R.drawable.ic_dialog_info).setContentIntent(pi).setOngoing(true).build()
    }

    private fun show() {
        val s = SettingsManager.get(this).load()
        val lp = WindowManager.LayoutParams(s.overlayWidth, s.overlayHeight,
            if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.START; x = s.overlayX; y = s.overlayY }
        view = OverlayView(this).apply { setS(s) }
        try { wm.addView(view, lp) } catch (_: Exception) {}
    }

    override fun onBind(i: Intent?) = null
    override fun onDestroy() { view?.let { wm.removeView(it) }; super.onDestroy() }

    companion object {
        fun start(c: Context) { c.startService(Intent(c, OverlayService::class.java)) }
        fun stop(c: Context) { c.stopService(Intent(c, OverlayService::class.java)) }
    }
}