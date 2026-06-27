package com.classisland.android.service

import android.content.Context
import com.classisland.android.model.profile.*
import com.classisland.android.util.Constants
import com.google.gson.GsonBuilder
import java.io.File

class ProfileService private constructor(private val ctx: Context) {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun load(): Profile = try {
        val f = File(ctx.filesDir, Constants.PROFILE_FILE)
        if (f.exists()) gson.fromJson(f.readText(), Profile::class.java) ?: defaultProfile()
        else defaultProfile()
    } catch (e: Exception) { defaultProfile() }

    fun save(p: Profile) = try {
        val f = File(ctx.filesDir, Constants.PROFILE_FILE)
        f.parentFile?.mkdirs(); f.writeText(gson.toJson(p))
    } catch (_: Exception) {}

    fun export(): String? = try { gson.toJson(load()) } catch (_: Exception) { null }
    fun import(json: String): Boolean = try {
        gson.fromJson(json, Profile::class.java)?.let { save(it); true } ?: false
    } catch (_: Exception) { false }

    private fun defaultProfile(): Profile {
        val subjects = listOf("语文","数学","英语","物理","化学","生物","历史","地理","政治","体育").mapIndexed { i, n ->
            Subject(id = "s_$i", name = n, color = listOf("#FFE91E63","#FF9C27B0","#FF673AB7","#FF3F51B5","#FF2196F3","#FF009688","#FF4CAF50","#FF8BC34A","#FFFFC107","#FFFF5722")[i])
        }.toMutableList()

        val items = listOf("08:00" to "08:45","08:55" to "09:40","09:50" to "10:35","10:45" to "11:30","11:40" to "12:25","14:00" to "14:45","14:55" to "15:40","15:50" to "16:35","16:45" to "17:30").mapIndexed { i, (s, e) ->
            TimeLayoutItem(id = "tli_$i", startTime = s, endTime = e, lessonName = if (i < subjects.size) subjects[i].id else "", timeType = 0)
        }.toMutableList()

        return Profile(subjects = subjects, timeLayouts = mutableListOf(TimeLayout(id = "layout_default", name = "默认时间布局", timeLayoutItems = items)))
    }

    companion object {
        @Volatile private var i: ProfileService? = null
        fun get(ctx: Context) = i ?: synchronized(this) { i ?: ProfileService(ctx.applicationContext).also { i = it } }
    }
}