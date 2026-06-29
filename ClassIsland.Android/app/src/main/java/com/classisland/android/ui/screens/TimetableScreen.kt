package com.classisland.android.ui.screens

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.classisland.android.model.profile.*
import com.classisland.android.service.ProfileService
import java.util.UUID

@Composable
fun TimetableScreen(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    var profile by remember { mutableStateOf(ProfileService.get(ctx).load()) }
    var currentWeek by remember { mutableIntStateOf(profile.currentWeek) }
    var showSubjectDialog by remember { mutableStateOf(false) }
    var showTimeLayoutDialog by remember { mutableStateOf(false) }
    var showCellDialog by remember { mutableStateOf<Pair<Int, String>?>(null) }

    val timeLayout = profile.timeLayouts.firstOrNull()
    val timeItems = timeLayout?.timeLayoutItems ?: mutableListOf()
    val daysOfWeek = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    fun saveProfile(p: Profile) {
        profile = p
        ProfileService.get(ctx).save(p)
    }

    fun getSubjectForCell(dayOfWeek: Int, timeItemId: String): Subject? {
        val assignment = profile.classAssignments.find {
            (it.week == 0 || it.week == currentWeek) &&
            it.dayOfWeek == dayOfWeek &&
            it.timeLayoutItemId == timeItemId
        }
        return assignment?.let { a -> profile.subjects.find { it.id == a.subjectId } }
    }

    fun setSubjectForCell(dayOfWeek: Int, timeItemId: String, subjectId: String?) {
        val newAssignments = profile.classAssignments.toMutableList()
        newAssignments.removeAll {
            it.week == 0 && it.dayOfWeek == dayOfWeek && it.timeLayoutItemId == timeItemId
        }
        if (subjectId != null && subjectId.isNotEmpty()) {
            newAssignments.add(
                ClassAssignment(
                    week = 0,
                    dayOfWeek = dayOfWeek,
                    timeLayoutItemId = timeItemId,
                    subjectId = subjectId
                )
            )
        }
        saveProfile(profile.copy(classAssignments = newAssignments))
    }

    if (profile.subjects.isEmpty() && timeItems.isEmpty()) {
        EmptyStateGuide(
            onAddSubject = { showSubjectDialog = true },
            onAddTimeLayout = { showTimeLayoutDialog = true },
            modifier
        )
    } else {
        Column(modifier.fillMaxSize()) {
            WeekSelector(
                currentWeek = currentWeek,
                totalWeeks = profile.totalWeeks,
                onWeekChange = {
                    currentWeek = it
                    saveProfile(profile.copy(currentWeek = it))
                },
                onManageSubjects = { showSubjectDialog = true },
                onManageTimeLayout = { showTimeLayoutDialog = true }
            )

            if (timeItems.isEmpty()) {
                EmptyTimeLayout(onAddTimeLayout = { showTimeLayoutDialog = true })
            } else if (profile.subjects.isEmpty()) {
                EmptySubjects(onAddSubject = { showSubjectDialog = true })
            } else {
                TimetableGrid(
                    timeItems = timeItems,
                    daysOfWeek = daysOfWeek,
                    getSubject = { day, itemId -> getSubjectForCell(day, itemId) },
                    onCellClick = { day, itemId -> showCellDialog = day to itemId },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showSubjectDialog) {
        SubjectManageDialog(
            profile = profile,
            onDismiss = { showSubjectDialog = false },
            onSave = { saveProfile(it) }
        )
    }

    if (showTimeLayoutDialog) {
        TimeLayoutManageDialog(
            profile = profile,
            onDismiss = { showTimeLayoutDialog = false },
            onSave = { saveProfile(it) }
        )
    }

    showCellDialog?.let { (dayOfWeek, timeItemId) ->
        CellEditDialog(
            subjects = profile.subjects,
            currentSubject = getSubjectForCell(dayOfWeek, timeItemId),
            dayOfWeek = dayOfWeek,
            dayName = daysOfWeek[dayOfWeek - 1],
            timeItem = timeItems.find { it.id == timeItemId },
            onDismiss = { showCellDialog = null },
            onSave = { subjectId ->
                setSubjectForCell(dayOfWeek, timeItemId, subjectId)
                showCellDialog = null
            }
        )
    }
}

@Composable
private fun WeekSelector(
    currentWeek: Int,
    totalWeeks: Int,
    onWeekChange: (Int) -> Unit,
    onManageSubjects: () -> Unit,
    onManageTimeLayout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showWeekPicker by remember { mutableStateOf(false) }

    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "第 $currentWeek 周",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showWeekPicker = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "选择周数")
                }
            }
            Row {
                IconButton(onClick = onManageSubjects) {
                    Icon(Icons.Default.Book, contentDescription = "科目管理")
                }
                IconButton(onClick = onManageTimeLayout) {
                    Icon(Icons.Default.Schedule, contentDescription = "时间布局")
                }
            }
        }
        Divider()
    }

    if (showWeekPicker) {
        var weekSliderValue by remember { mutableFloatStateOf(currentWeek.toFloat()) }
        AlertDialog(
            onDismissRequest = { showWeekPicker = false },
            title = { Text("选择周数") },
            text = {
                Column {
                    Text("当前：第 $currentWeek 周 / 共 $totalWeeks 周")
                    Spacer(Modifier.height(16.dp))
                    Slider(
                        value = weekSliderValue,
                        onValueChange = { weekSliderValue = it },
                        valueRange = 1f..totalWeeks.toFloat(),
                        steps = totalWeeks - 1
                    )
                    Text("第 ${weekSliderValue.toInt()} 周", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onWeekChange(weekSliderValue.toInt())
                    showWeekPicker = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWeekPicker = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun TimetableGrid(
    timeItems: List<TimeLayoutItem>,
    daysOfWeek: List<String>,
    getSubject: (Int, String) -> Subject?,
    onCellClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(modifier.verticalScroll(scrollState)) {
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.width(60.dp)) { }
            daysOfWeek.forEach { day ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
        Divider()

        timeItems.forEach { timeItem ->
            Row(Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = timeItem.startTime,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "↓",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = timeItem.endTime,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp
                        )
                    }
                }
                (1..7).forEach { dayOfWeek ->
                    val subject = getSubject(dayOfWeek, timeItem.id)
                    val bgColor = if (subject != null) {
                        androidx.compose.ui.graphics.Color(Color.parseColor(subject.color))
                    } else {
                            MaterialTheme.colorScheme.surface
                        }
                    val textColor = if (subject != null) {
                        androidx.compose.ui.graphics.Color.White
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .padding(2.dp)
                            .background(bgColor, RoundedCornerShape(6.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onCellClick(dayOfWeek, timeItem.id) }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (subject != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = subject.name,
                                    color = textColor,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (subject.classroom.isNotEmpty()) {
                                    Text(
                                        text = subject.classroom,
                                        color = textColor.copy(alpha = 0.85f),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (subject.teacherName.isNotEmpty()) {
                                    Text(
                                        text = subject.teacherName,
                                        color = textColor.copy(alpha = 0.75f),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "+",
                                color = textColor.copy(alpha = 0.4f),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun EmptyStateGuide(
    onAddSubject: () -> Unit,
    onAddTimeLayout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.School,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "欢迎使用课表",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "开始创建你的课程表吧",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onAddSubject) {
            Icon(Icons.Default.Book, null)
            Spacer(Modifier.width(8.dp))
            Text("添加科目")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onAddTimeLayout) {
            Icon(Icons.Default.Schedule, null)
            Spacer(Modifier.width(8.dp))
            Text("设置时间段")
        }
    }
}

@Composable
private fun EmptyTimeLayout(onAddTimeLayout: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Schedule,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "暂无时间段",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "请先设置每天的上课时间段",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAddTimeLayout) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("添加时间段")
        }
    }
}

@Composable
private fun EmptySubjects(onAddSubject: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Book,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "暂无科目",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "请先添加课程科目",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAddSubject) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("添加科目")
        }
    }
}

@Composable
private fun SubjectManageDialog(
    profile: Profile,
    onDismiss: () -> Unit,
    onSave: (Profile) -> Unit
) {
    var editingSubject by remember { mutableStateOf<Subject?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("科目管理") },
        text = {
            Column {
                val subjects = profile.subjects
                if (subjects.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无科目，点击下方按钮添加", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                        items(subjects, key = { it.id }) { subject ->
                            ListItem(
                                headlineContent = { Text(subject.name) },
                                supportingContent = {
                                    val info = buildString {
                                        if (subject.teacherName.isNotEmpty()) append(subject.teacherName)
                                        if (subject.classroom.isNotEmpty()) {
                                            if (isNotEmpty()) append(" · ")
                                            append(subject.classroom)
                                        }
                                    }
                                    if (info.isNotEmpty()) Text(info)
                                },
                                leadingContent = {
                                    Box(
                                        Modifier
                                            .size(40.dp)
                                            .background(
                                                androidx.compose.ui.graphics.Color(Color.parseColor(subject.color)),
                                                RoundedCornerShape(8.dp)
                                            )
                                    )
                                },
                                trailingContent = {
                                    Row {
                                        IconButton(onClick = {
                                            editingSubject = subject.copy()
                                            showEditDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, null)
                                        }
                                        IconButton(onClick = {
                                            val newSubjects = profile.subjects.toMutableList()
                                            newSubjects.removeAll { it.id == subject.id }
                                            val newAssignments = profile.classAssignments.toMutableList()
                                            newAssignments.removeAll { it.subjectId == subject.id }
                                            onSave(profile.copy(subjects = newSubjects, classAssignments = newAssignments))
                                        }) {
                                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            )
                            Divider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                editingSubject = Subject()
                showEditDialog = true
            }) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(4.dp))
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )

    if (showEditDialog && editingSubject != null) {
        SubjectEditDialog(
            subject = editingSubject!!,
            isNew = editingSubject!!.name.isEmpty() && editingSubject!!.id !in profile.subjects.map { it.id },
            onDismiss = { showEditDialog = false },
            onSave = { s ->
                val newSubjects = profile.subjects.toMutableList()
                val idx = newSubjects.indexOfFirst { it.id == s.id }
                if (idx >= 0) newSubjects[idx] = s else newSubjects.add(s)
                onSave(profile.copy(subjects = newSubjects))
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun SubjectEditDialog(
    subject: Subject,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (Subject) -> Unit
) {
    var name by remember { mutableStateOf(subject.name) }
    var teacherName by remember { mutableStateOf(subject.teacherName) }
    var classroom by remember { mutableStateOf(subject.classroom) }
    var color by remember { mutableStateOf(subject.color) }

    val presetColors = listOf(
        "#FFE91E63", "#FF9C27B0", "#FF673AB7", "#FF3F51B5", "#FF2196F3",
        "#FF009688", "#FF4CAF50", "#FF8BC34A", "#FFFFC107", "#FFFF5722",
        "#FF795548", "#FF607D8B", "#FF00BCD4", "#FFE91E63"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "添加科目" else "编辑科目") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("科目名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = teacherName,
                    onValueChange = { teacherName = it },
                    label = { Text("教师姓名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = classroom,
                    onValueChange = { classroom = it },
                    label = { Text("教室") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("颜色", style = MaterialTheme.typography.bodyMedium)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val half = presetColors.size / 2 + presetColors.size % 2
                    listOf(presetColors.take(half), presetColors.drop(half)).forEach { rowColors ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowColors.forEach { c ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            androidx.compose.ui.graphics.Color(Color.parseColor(c)),
                                            RoundedCornerShape(50)
                                        )
                                        .border(
                                            if (color == c) 3.dp else 0.dp,
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(50)
                                        )
                                        .clickable { color = c }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            subject.copy(
                                name = name.trim(),
                                teacherName = teacherName.trim(),
                                classroom = classroom.trim(),
                                color = color
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun TimeLayoutManageDialog(
    profile: Profile,
    onDismiss: () -> Unit,
    onSave: (Profile) -> Unit
) {
    var editingItem by remember { mutableStateOf<TimeLayoutItem?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    val timeLayout = profile.timeLayouts.firstOrNull() ?: TimeLayout(name = "默认时间布局")
    val timeItems = timeLayout.timeLayoutItems

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("时间段管理") },
        text = {
            Column {
                if (timeItems.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无时间段，点击下方按钮添加", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                        items(timeItems, key = { it.id }) { item ->
                            ListItem(
                                headlineContent = { Text("${item.startTime} - ${item.endTime}") },
                                supportingContent = { Text("第 ${timeItems.indexOf(item) + 1} 节") },
                                leadingContent = {
                                    Icon(Icons.Default.AccessTime, null)
                                },
                                trailingContent = {
                                    Row {
                                        IconButton(onClick = {
                                            editingItem = item.copy()
                                            showEditDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, null)
                                        }
                                        IconButton(onClick = {
                                            val newItems = timeItems.toMutableList()
                                            newItems.removeAll { it.id == item.id }
                                            val newLayout = timeLayout.copy(timeLayoutItems = newItems)
                                            val newLayouts = profile.timeLayouts.toMutableList()
                                            if (newLayouts.isNotEmpty()) {
                                                newLayouts[0] = newLayout
                                            } else {
                                                newLayouts.add(newLayout)
                                            }
                                            val newAssignments = profile.classAssignments.toMutableList()
                                            newAssignments.removeAll { it.timeLayoutItemId == item.id }
                                            onSave(profile.copy(timeLayouts = newLayouts, classAssignments = newAssignments))
                                        }) {
                                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            )
                            Divider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                editingItem = TimeLayoutItem()
                showEditDialog = true
            }) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(4.dp))
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )

    if (showEditDialog && editingItem != null) {
        TimeItemEditDialog(
            item = editingItem!!,
            isNew = editingItem!!.id !in timeItems.map { it.id },
            onDismiss = { showEditDialog = false },
            onSave = { newItem ->
                val newItems = timeItems.toMutableList()
                val idx = newItems.indexOfFirst { it.id == newItem.id }
                if (idx >= 0) newItems[idx] = newItem else newItems.add(newItem)
                newItems.sortBy { it.startTime }
                val newLayout = timeLayout.copy(timeLayoutItems = newItems)
                val newLayouts = profile.timeLayouts.toMutableList()
                if (newLayouts.isNotEmpty()) {
                    newLayouts[0] = newLayout
                } else {
                    newLayouts.add(newLayout)
                }
                onSave(profile.copy(timeLayouts = newLayouts))
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun TimeItemEditDialog(
    item: TimeLayoutItem,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (TimeLayoutItem) -> Unit
) {
    var startTime by remember { mutableStateOf(item.startTime) }
    var endTime by remember { mutableStateOf(item.endTime) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    fun parseTime(time: String): Pair<Int, Int> {
        val parts = time.split(":")
        return (parts.getOrNull(0)?.toIntOrNull() ?: 8) to (parts.getOrNull(1)?.toIntOrNull() ?: 0)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "添加时间段" else "编辑时间段") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("开始时间", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.AccessTime, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showStartPicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text("结束时间", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.AccessTime, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEndPicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(item.copy(startTime = startTime, endTime = endTime)) }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )

    if (showStartPicker) {
        val (h, m) = parseTime(startTime)
        TimePickerDialog(
            initialHour = h,
            initialMinute = m,
            onDismiss = { showStartPicker = false },
            onTimeSelected = { hour, minute ->
                startTime = String.format("%02d:%02d", hour, minute)
                showStartPicker = false
            }
        )
    }

    if (showEndPicker) {
        val (h, m) = parseTime(endTime)
        TimePickerDialog(
            initialHour = h,
            initialMinute = m,
            onDismiss = { showEndPicker = false },
            onTimeSelected = { hour, minute ->
                endTime = String.format("%02d:%02d", hour, minute)
                showEndPicker = false
            }
        )
    }
}

@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    var hour by remember { mutableIntStateOf(initialHour) }
    var minute by remember { mutableIntStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择时间") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { hour = (hour + 1) % 24 }) {
                        Icon(Icons.Default.KeyboardArrowUp, null)
                    }
                    Text(
                        String.format("%02d", hour),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { hour = (hour + 23) % 24 }) {
                        Icon(Icons.Default.KeyboardArrowDown, null)
                    }
                }
                Text(":", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { minute = (minute + 1) % 60 }) {
                        Icon(Icons.Default.KeyboardArrowUp, null)
                    }
                    Text(
                        String.format("%02d", minute),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { minute = (minute + 59) % 60 }) {
                        Icon(Icons.Default.KeyboardArrowDown, null)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onTimeSelected(hour, minute) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun CellEditDialog(
    subjects: List<Subject>,
    currentSubject: Subject?,
    dayOfWeek: Int,
    dayName: String,
    timeItem: TimeLayoutItem?,
    onDismiss: () -> Unit,
    onSave: (String?) -> Unit
) {
    var selectedSubjectId by remember { mutableStateOf(currentSubject?.id ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$dayName ${timeItem?.let { "${it.startTime}-${it.endTime}" } ?: ""}") },
        text = {
            Column {
                Text("选择科目", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                if (subjects.isEmpty()) {
                    Text("暂无科目，请先在科目管理中添加", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                        items(subjects, key = { it.id }) { subject ->
                            ListItem(
                                headlineContent = { Text(subject.name) },
                                supportingContent = {
                                    val info = buildString {
                                        if (subject.teacherName.isNotEmpty()) append(subject.teacherName)
                                        if (subject.classroom.isNotEmpty()) {
                                            if (isNotEmpty()) append(" · ")
                                            append(subject.classroom)
                                        }
                                    }
                                    if (info.isNotEmpty()) Text(info)
                                },
                                leadingContent = {
                                    Box(
                                        Modifier
                                            .size(40.dp)
                                            .background(
                                                androidx.compose.ui.graphics.Color(Color.parseColor(subject.color)),
                                                RoundedCornerShape(8.dp)
                                            )
                                    )
                                },
                                trailingContent = {
                                    if (selectedSubjectId == subject.id) {
                                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                modifier = Modifier.clickable {
                                    selectedSubjectId = if (selectedSubjectId == subject.id) "" else subject.id
                                }
                            )
                            Divider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row {
                if (currentSubject != null) {
                    TextButton(
                        onClick = { onSave(null) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("清除")
                    }
                }
                TextButton(onClick = { onSave(selectedSubjectId.ifEmpty { null }) }) {
                    Text("确定")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
