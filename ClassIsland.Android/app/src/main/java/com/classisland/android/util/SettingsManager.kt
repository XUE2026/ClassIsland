package com.classisland.android.util

import android.content.Context
import com.classisland.android.model.AppSettings
import com.google.gson.GsonBuilder
import java.io.File

class SettingsManager private constructor(private val ctx: Context) {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun load(): AppSettings = try {
        val f = File(ctx.filesDir, Constants.SETTINGS_FILE)
        if (f.exists()) gson.fromJson(f.readText(), AppSettings::class.java) ?: AppSettings()
        else AppSettings()
    } catch (e: Exception) { AppSettings() }

    fun save(s: AppSettings) = try {
        val f = File(ctx.filesDir, Constants.SETTINGS_FILE)
        f.parentFile?.mkdirs()
        f.writeText(gson.toJson(s))
    } catch (_: Exception) {}

    companion object {
        @Volatile
        private var i: SettingsManager? = null
        fun get(ctx: Context) = i ?: synchronized(this) {
            i ?: SettingsManager(ctx.applicationContext).also { i = it }
        }
    }
}