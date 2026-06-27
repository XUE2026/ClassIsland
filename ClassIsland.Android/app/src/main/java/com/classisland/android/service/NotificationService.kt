package com.classisland.android.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.classisland.android.util.Constants
import kotlinx.coroutines.*

class NotificationService : Service() {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var job: Job? = null

    override fun onCreate() { start() }
    override fun onStartCommand(i: Intent?, f: Int, id: Int) = START_STICKY

    private fun start() {
        job = scope.launch {
            while (isActive) {
                check()
                delay(60000)
            }
        }
    }

    private suspend fun check() {
        withContext(Dispatchers.IO) {
            val p = ProfileService.get(this@NotificationService).load()
            val plan = p.classPlans.firstOrNull { it.isEnabled } ?: return@withContext
            val layout = p.timeLayouts.firstOrNull { it.id == plan.timeLayoutId } ?: return@withContext
            val cal = java.util.Calendar.getInstance()
            val now = "%02d:%02d".format(cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE))

            layout.timeLayoutItems.filter { it.timeType == 0 }.forEach { item ->
                val name = p.subjects.firstOrNull { it.id == item.lessonName }?.name ?: item.lessonName
                val startMin = item.startTime.split(":").let { it[0].toInt()*60 + it[1].toInt() }
                val nowMin = now.split(":").let { it[0].toInt()*60 + it[1].toInt() }
                if (nowMin in (startMin-5) until startMin) send("即将上课", "${name} 将在5分钟后开始")
                if (now == item.startTime) send("上课提醒", "${name} 已经开始上课")
            }
        }
    }

    private fun send(title: String, content: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(System.currentTimeMillis().toInt(), NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(title).setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).build())
    }

    override fun onBind(i: Intent?) = null
    override fun onDestroy() { job?.cancel(); scope.cancel(); super.onDestroy() }
}