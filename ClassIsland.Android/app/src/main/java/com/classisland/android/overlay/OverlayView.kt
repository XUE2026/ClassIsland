package com.classisland.android.overlay

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.classisland.android.model.AppSettings
import com.classisland.android.model.profile.Subject
import com.classisland.android.service.ProfileService
import java.text.SimpleDateFormat
import java.util.*

class OverlayView(context: Context) : View(context) {
    private var s = AppSettings()
    private var classes = listOf<ClassSlot>()
    private var idx = -1
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tp = Paint(Paint.ANTI_ALIAS_FLAG)
    private val hp = Paint(Paint.ANTI_ALIAS_FLAG)
    private var dx = 0f; private var dy = 0f

    data class ClassSlot(val name: String, val teacher: String, val room: String, val st: String, val et: String, val color: String, val isCur: Boolean)

    private val h = Handler(Looper.getMainLooper())
    private val r = object : Runnable { override fun run() { refresh(); invalidate(); h.postDelayed(this, 30000) } }
    init { h.post(r) }

    fun setS(settings: AppSettings) { s = settings; refresh(); invalidate() }

    private fun refresh() {
        val profile = ProfileService.get(context).load()
        val layout = profile.timeLayouts.firstOrNull { it.id == (profile.classPlans.firstOrNull { it.isEnabled }?.timeLayoutId ?: "") } ?: return
        val now = Calendar.getInstance(); val cur = "%02d:%02d".format(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))
        val list = mutableListOf<ClassSlot>(); var ci = -1; var i = 0
        layout.timeLayoutItems.filter { it.timeType == 0 }.forEach { item ->
            val sub = profile.subjects.firstOrNull { it.id == item.lessonName } ?: Subject(name = item.lessonName)
            val ic = cur >= item.startTime && cur < item.endTime; if (ic) ci = i
            list.add(ClassSlot(sub.name, sub.teacherName, sub.classroom, item.startTime, item.endTime, sub.color, ic)); i++
        }
        classes = list; idx = ci
    }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat(); val d = density; val style = s.overlayStyle
        p.color = parseColor(style.backgroundColor)
        val r = if (style.useRoundedCorners) style.borderRadius.toFloat() else 0f
        c.drawRoundRect(0f, 0f, w, h, r, r, p)
        if (style.borderWidth > 0) { p.color = parseColor(style.borderColor); p.style = Paint.Style.STROKE; p.strokeWidth = style.borderWidth.toFloat(); c.drawRoundRect(1f,1f,w-1f,h-1f,r,r,p); p.style = Paint.Style.FILL }

        hp.color = parseColor(style.textColor); hp.textSize = style.titleFontSize.toFloat() * d; hp.isFakeBoldText = true
        val df = SimpleDateFormat("MM/dd EEEE", Locale.getDefault())
        val title = if (style.showDateInfo) "今日课表 · ${df.format(Date())}" else "今日课表"
        if (style.showWeekInfo) {
            val wk = ProfileService.get(context).load().currentWeek
            c.drawText("第${wk}周", 16f*d, 24f*d, hp)
            c.drawText(title, w - hp.measureText(title) - 16f*d, 24f*d, hp)
        } else c.drawText(title, 16f*d, 24f*d, hp)

        tp.color = parseColor(style.textColor); tp.textSize = style.fontSize.toFloat() * d
        val ih = style.itemHeight * d; val is_ = style.itemSpacing * d; var y = 40f * d
        val show = if (style.showCurrentClass && idx>=0) classes.drop(idx).take(style.upcomingClassesCount) else classes.take(style.upcomingClassesCount)

        for (cl in show) {
            if (y+ih > h) break
            p.color = if (cl.isCur) { val c2 = parseColor(cl.color); Color.argb(60, Color.red(c2), Color.green(c2), Color.blue(c2)) } else Color.argb(20,255,255,255)
            c.drawRoundRect(8f*d, y, w-8f*d, y+ih, 8f, 8f, p)
            p.color = parseColor(cl.color); c.drawRoundRect(8f*d, y+4f*d, 12f*d, y+ih-4f*d, 4f, 4f, p)

            var x = 20f*d; tp.color = parseColor(style.textColor)
            if (style.showTime) {
                tp.textSize = (style.fontSize-2).toFloat()*d; tp.color = Color.argb(180,255,255,255)
                c.drawText("${cl.st}-${cl.et}", x, y+ih/2f+4f*d, tp); x += tp.measureText("${cl.st}-${cl.et}") + 12f*d
            }
            tp.textSize = style.fontSize.toFloat()*d; tp.color = parseColor(style.textColor); tp.isFakeBoldText = cl.isCur
            c.drawText(cl.name, x, y+ih/2f+5f*d, tp); tp.isFakeBoldText = false
            if (style.showTeacherName && cl.teacher.isNotEmpty()) {
                tp.textSize = (style.fontSize-3).toFloat()*d; tp.color = Color.argb(150,255,255,255)
                c.drawText(cl.teacher, w-tp.measureText(cl.teacher)-12f*d, y+ih/2f+4f*d, tp)
            }
            y += ih + is_
        }
        if (classes.isEmpty()) { tp.textSize = (style.fontSize+4).toFloat()*d; tp.color = Color.argb(120,255,255,255); tp.textAlign = Paint.Align.CENTER; c.drawText("当前无课", w/2f, h/2f, tp) }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> { dx = e.rawX; dy = e.rawY; return true }
            MotionEvent.ACTION_MOVE -> {
                val ddx = e.rawX-dx; val ddy = e.rawY-dy; dx = e.rawX; dy = e.rawY
                (parent as? ViewGroup)?.let { p ->
                    val lp = p.layoutParams as? WindowManager.LayoutParams ?: return@let
                    lp.x += ddx.toInt(); lp.y += ddy.toInt()
                    (context.getSystemService(Context.WINDOW_SERVICE) as? android.view.WindowManager)?.updateViewLayout(p, lp)
                }
                return true
            }
        }
        return super.onTouchEvent(e)
    }

    override fun onDetachedFromWindow() { super.onDetachedFromWindow(); h.removeCallbacks(r) }
    private val density get() = resources.displayMetrics.density
    private fun parseColor(c: String) = try { Color.parseColor(c) } catch (_: Exception) { Color.argb(178,0,0,0) }
}