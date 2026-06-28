package com.classisland.android.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val language: String = "zh",
    val themeMode: String = "system",
    val enableOverlay: Boolean = true,
    val overlayX: Int = 0,
    val overlayY: Int = 100,
    val overlayWidth: Int = 400,
    val overlayHeight: Int = 600,
    val overlayOpacity: Float = 0.9f,
    val overlayStyle: OverlayStyle = OverlayStyle(),
    val enableGpuAcceleration: Boolean = true,
    val autoStartEnabled: Boolean = false,
    val firstLaunch: Boolean = true,
    val licenseAccepted: Boolean = false,
    val apiUrl: String = "https://classisland.example.com/api",
    val apiEnabled: Boolean = false,
    val syncIntervalMinutes: Int = 30,
    val notificationEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val autoUpdateCheck: Boolean = true,
    val defaultClassPlanId: String = "",
    val startOfWeek: Int = 1,
    val totalWeeks: Int = 20
)

@Serializable
data class OverlayStyle(
    val backgroundColor: String = "#B2000000",
    val textColor: String = "#FFFFFFFF",
    val borderColor: String = "#FF2196F3",
    val borderWidth: Int = 2,
    val borderRadius: Int = 12,
    val fontSize: Int = 14,
    val titleFontSize: Int = 18,
    val showTeacherName: Boolean = true,
    val showClassroom: Boolean = true,
    val showTime: Boolean = true,
    val showCurrentClass: Boolean = true,
    val showUpcomingClasses: Boolean = true,
    val upcomingClassesCount: Int = 5,
    val itemHeight: Int = 48,
    val itemSpacing: Int = 4,
    val showWeekInfo: Boolean = true,
    val showDateInfo: Boolean = true,
    val useRoundedCorners: Boolean = true
)