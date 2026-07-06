package com.classisland.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.classisland.android.model.AppSettings
import com.classisland.android.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onChanged: (AppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "设置",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        DataSyncSection(settings, onChanged)

        Divider(Modifier.padding(vertical = 16.dp))

        TimetableSection(settings, onChanged)

        Divider(Modifier.padding(vertical = 16.dp))

        OverlaySection(settings, onChanged)

        Divider(Modifier.padding(vertical = 16.dp))

        NotificationSection(settings, onChanged)

        Divider(Modifier.padding(vertical = 16.dp))

        GeneralSection(settings, onChanged)

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun DataSyncSection(
    settings: AppSettings,
    onChanged: (AppSettings) -> Unit
) {
    Text(
        "数据同步",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    SwitchSetting(
        "启用 API 同步",
        settings.apiEnabled
    ) { onChanged(settings.copy(apiEnabled = it)) }

    if (settings.apiEnabled) {
        var apiUrlText by remember(settings.apiUrl) { mutableStateOf(settings.apiUrl) }
        ListItem(
            headlineContent = { Text("API 地址") },
            supportingContent = {
                OutlinedTextField(
                    value = apiUrlText,
                    onValueChange = {
                        apiUrlText = it
                        onChanged(settings.copy(apiUrl = it))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(Constants.DEFAULT_API_URL) }
                )
            }
        )

        val syncIntervals = listOf(10 to "10 分钟", 30 to "30 分钟", 60 to "1 小时", 120 to "2 小时")
        var expanded by remember { mutableStateOf(false) }
        val currentInterval = syncIntervals.find { it.first == settings.syncIntervalMinutes } ?: syncIntervals[1]
        ListItem(
            headlineContent = { Text("同步间隔") },
            supportingContent = { Text(currentInterval.second) },
            trailingContent = {
                Box {
                    TextButton({ expanded = true }) { Text("更改") }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        syncIntervals.forEach { (minutes, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    onChanged(settings.copy(syncIntervalMinutes = minutes))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        )

        ListItem(
            headlineContent = { Text("立即同步") },
            supportingContent = { Text("手动触发一次数据同步") },
            trailingContent = {
                Button(onClick = { /* 手动同步暂未实现 */ }) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.width(4.dp))
                    Text("同步")
                }
            }
        )
    }
}

@Composable
private fun TimetableSection(
    settings: AppSettings,
    onChanged: (AppSettings) -> Unit
) {
    Text(
        "课表设置",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    var planExpanded by remember { mutableStateOf(false) }
    ListItem(
        headlineContent = { Text("默认课表计划") },
        supportingContent = { Text(settings.defaultClassPlanId.ifEmpty { "默认" }) },
        trailingContent = {
            Box {
                TextButton({ planExpanded = true }) { Text("更改") }
                DropdownMenu(
                    expanded = planExpanded,
                    onDismissRequest = { planExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("默认") },
                        onClick = {
                            onChanged(settings.copy(defaultClassPlanId = ""))
                            planExpanded = false
                        }
                    )
                }
            }
        }
    )

    val weekStarts = listOf(1 to "周一", 0 to "周日")
    var weekExpanded by remember { mutableStateOf(false) }
    val currentWeekStart = weekStarts.find { it.first == settings.startOfWeek } ?: weekStarts[0]
    ListItem(
        headlineContent = { Text("周起始日") },
        supportingContent = { Text(currentWeekStart.second) },
        trailingContent = {
            Box {
                TextButton({ weekExpanded = true }) { Text("更改") }
                DropdownMenu(
                    expanded = weekExpanded,
                    onDismissRequest = { weekExpanded = false }
                ) {
                    weekStarts.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onChanged(settings.copy(startOfWeek = value))
                                weekExpanded = false
                            }
                        )
                    }
                }
            }
        }
    )

    NumSetting("总周数", settings.totalWeeks) { onChanged(settings.copy(totalWeeks = it)) }
}

@Composable
private fun OverlaySection(
    settings: AppSettings,
    onChanged: (AppSettings) -> Unit
) {
    Text(
        "悬浮窗设置",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    SwitchSetting(
        "启用悬浮窗",
        settings.enableOverlay
    ) { onChanged(settings.copy(enableOverlay = it)) }

    if (settings.enableOverlay) {
        Divider(Modifier.padding(vertical = 8.dp))
        Text(
            "位置设置",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        NumSetting("X 坐标", settings.overlayX) { onChanged(settings.copy(overlayX = it)) }
        NumSetting("Y 坐标", settings.overlayY) { onChanged(settings.copy(overlayY = it)) }
        NumSetting("宽度", settings.overlayWidth) { onChanged(settings.copy(overlayWidth = it)) }
        NumSetting("高度", settings.overlayHeight) { onChanged(settings.copy(overlayHeight = it)) }
        Text(
            "透明度: ${(settings.overlayOpacity * 100).toInt()}%",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Slider(
            settings.overlayOpacity,
            { onChanged(settings.copy(overlayOpacity = it)) },
            valueRange = 0.1f..1.0f,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Divider(Modifier.padding(vertical = 8.dp))
        Text(
            "样式设置",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        ColorSetting(
            "背景颜色",
            settings.overlayStyle.backgroundColor
        ) { onChanged(settings.copy(overlayStyle = settings.overlayStyle.copy(backgroundColor = it))) }
        ColorSetting(
            "文字颜色",
            settings.overlayStyle.textColor
        ) { onChanged(settings.copy(overlayStyle = settings.overlayStyle.copy(textColor = it))) }
        ColorSetting(
            "边框颜色",
            settings.overlayStyle.borderColor
        ) { onChanged(settings.copy(overlayStyle = settings.overlayStyle.copy(borderColor = it))) }
        NumSetting("边框宽度", settings.overlayStyle.borderWidth) {
            onChanged(
                settings.copy(
                    overlayStyle = settings.overlayStyle.copy(borderWidth = it)
                )
            )
        }
        NumSetting("圆角大小", settings.overlayStyle.borderRadius) {
            onChanged(
                settings.copy(
                    overlayStyle = settings.overlayStyle.copy(borderRadius = it)
                )
            )
        }
        NumSetting("字体大小", settings.overlayStyle.fontSize) {
            onChanged(
                settings.copy(
                    overlayStyle = settings.overlayStyle.copy(fontSize = it)
                )
            )
        }

        Divider(Modifier.padding(vertical = 8.dp))
        Text(
            "显示设置",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        SwitchSetting("显示教师", settings.overlayStyle.showTeacherName) {
            onChanged(
                settings.copy(
                    overlayStyle = settings.overlayStyle.copy(showTeacherName = it)
                )
            )
        }
        SwitchSetting("显示教室", settings.overlayStyle.showClassroom) {
            onChanged(
                settings.copy(
                    overlayStyle = settings.overlayStyle.copy(showClassroom = it)
                )
            )
        }
        SwitchSetting("显示时间", settings.overlayStyle.showTime) {
            onChanged(
                settings.copy(
                    overlayStyle = settings.overlayStyle.copy(showTime = it)
                )
            )
        }
        SwitchSetting("显示当前课程", settings.overlayStyle.showCurrentClass) {
            onChanged(
                settings.copy(
                    overlayStyle = settings.overlayStyle.copy(showCurrentClass = it)
                )
            )
        }
        SwitchSetting("显示后续课程", settings.overlayStyle.showUpcomingClasses) {
            onChanged(
                settings.copy(
                    overlayStyle = settings.overlayStyle.copy(showUpcomingClasses = it)
                )
            )
        }
        NumSetting("后续课数量", settings.overlayStyle.upcomingClassesCount) {
            onChanged(
                settings.copy(
                    overlayStyle = settings.overlayStyle.copy(upcomingClassesCount = it)
                )
            )
        }
        NumSetting("行高", settings.overlayStyle.itemHeight) {
            onChanged(
                settings.copy(
                    overlayStyle = settings.overlayStyle.copy(itemHeight = it)
                )
            )
        }
        NumSetting("行间距", settings.overlayStyle.itemSpacing) {
            onChanged(
                settings.copy(
                    overlayStyle = settings.overlayStyle.copy(itemSpacing = it)
                )
            )
        }
    }
}

@Composable
private fun NotificationSection(
    settings: AppSettings,
    onChanged: (AppSettings) -> Unit
) {
    Text(
        "通知与提醒",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    SwitchSetting(
        "启用通知",
        settings.notificationEnabled
    ) { onChanged(settings.copy(notificationEnabled = it)) }

    SwitchSetting(
        "震动提醒",
        settings.vibrationEnabled
    ) { onChanged(settings.copy(vibrationEnabled = it)) }
}

@Composable
private fun GeneralSection(
    settings: AppSettings,
    onChanged: (AppSettings) -> Unit
) {
    Text(
        "通用设置",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    val themes = listOf("system" to "跟随系统", "light" to "浅色", "dark" to "深色")
    var themeExpanded by remember { mutableStateOf(false) }
    val curTheme = themes.find { it.first == settings.themeMode } ?: themes[0]
    ListItem(
        headlineContent = { Text("主题模式") },
        supportingContent = { Text(curTheme.second) },
        trailingContent = {
            Box {
                TextButton({ themeExpanded = true }) { Text("更改") }
                DropdownMenu(
                    expanded = themeExpanded,
                    onDismissRequest = { themeExpanded = false }
                ) {
                    themes.forEach { (m, n) ->
                        DropdownMenuItem(
                            text = { Text(n) },
                            onClick = {
                                onChanged(settings.copy(themeMode = m))
                                themeExpanded = false
                            }
                        )
                    }
                }
            }
        }
    )

    val langs = listOf("zh" to "中文", "en" to "English", "ja" to "日本語")
    var langExpanded by remember { mutableStateOf(false) }
    val curLang = langs.find { it.first == settings.language } ?: langs[0]
    ListItem(
        headlineContent = { Text("语言") },
        supportingContent = { Text(curLang.second) },
        trailingContent = {
            Box {
                TextButton({ langExpanded = true }) { Text("更改") }
                DropdownMenu(
                    expanded = langExpanded,
                    onDismissRequest = { langExpanded = false }
                ) {
                    langs.forEach { (c, n) ->
                        DropdownMenuItem(
                            text = { Text(n) },
                            onClick = {
                                onChanged(settings.copy(language = c))
                                langExpanded = false
                            }
                        )
                    }
                }
            }
        }
    )

    SwitchSetting(
        "GPU 加速",
        settings.enableGpuAcceleration
    ) { onChanged(settings.copy(enableGpuAcceleration = it)) }

    SwitchSetting(
        "开机自启",
        settings.autoStartEnabled
    ) { onChanged(settings.copy(autoStartEnabled = it)) }

    SwitchSetting(
        "自动检查更新",
        settings.autoUpdateCheck
    ) { onChanged(settings.copy(autoUpdateCheck = it)) }
}

@Composable
fun SwitchSetting(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = { Switch(checked, onChange) }
    )
}

@Composable
fun NumSetting(title: String, value: Int, onChange: (Int) -> Unit) {
    var t by remember(value) { mutableStateOf(value.toString()) }
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            OutlinedTextField(
                t,
                { t = it; onChange(it.toIntOrNull() ?: value) },
                singleLine = true,
                modifier = Modifier.width(100.dp)
            )
        }
    )
}

@Composable
fun ColorSetting(title: String, value: String, onChange: (String) -> Unit) {
    var t by remember(value) { mutableStateOf(value) }
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    t,
                    { t = it; onChange(it) },
                    singleLine = true,
                    modifier = Modifier.width(140.dp)
                )
                Surface(
                    Modifier.size(32.dp),
                    shape = MaterialTheme.shapes.small,
                    color = try {
                        androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(value))
                    } catch (_: Exception) {
                        MaterialTheme.colorScheme.surface
                    }
                ) {}
            }
        }
    )
}
