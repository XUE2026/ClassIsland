package com.classisland.android.model.profile

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Profile(
    val name: String = "我的课表",
    val classPlans: MutableList<ClassPlan> = mutableListOf(),
    val timeLayouts: MutableList<TimeLayout> = mutableListOf(),
    val subjects: MutableList<Subject> = mutableListOf(),
    val classAssignments: MutableList<ClassAssignment> = mutableListOf(),
    val currentWeek: Int = 1,
    val totalWeeks: Int = 20,
    val currentWeekOffset: Int = 0
)

@Serializable
data class ClassAssignment(
    val id: String = UUID.randomUUID().toString(),
    val week: Int = 0,
    val dayOfWeek: Int = 1,
    val timeLayoutItemId: String = "",
    val subjectId: String = ""
)

@Serializable
data class ClassPlan(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val timeLayoutId: String = "",
    val isEnabled: Boolean = true
)

@Serializable
data class Subject(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val teacherName: String = "",
    val classroom: String = "",
    val color: String = "#FF2196F3"
)

@Serializable
data class TimeLayout(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val timeLayoutItems: MutableList<TimeLayoutItem> = mutableListOf()
)

@Serializable
data class TimeLayoutItem(
    val id: String = UUID.randomUUID().toString(),
    val startTime: String = "08:00",
    val endTime: String = "08:45",
    val lessonName: String = "",
    val timeType: Int = 0
)