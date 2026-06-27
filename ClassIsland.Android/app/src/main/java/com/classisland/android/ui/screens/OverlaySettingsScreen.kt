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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlaySettingsScreen(settings: AppSettings, onChanged: (AppSettings) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("悬浮窗设置", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))

        SwitchSetting("启用悬浮窗", settings.enableOverlay, { onChanged(settings.copy(enableOverlay = it)) })

        if (settings.enableOverlay) {
            Divider(Modifier.padding(vertical = 8.dp))
            Text("位置设置", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
            NumSetting("X 坐标", settings.overlayX) { onChanged(settings.copy(overlayX = it)) }
            NumSetting("Y 坐标", settings.overlayY) { onChanged(settings.copy(overlayY = it)) }
            NumSetting("宽度", settings.overlayWidth) { onChanged(settings.copy(overlayWidth = it)) }
            NumSetting("高度", settings.overlayHeight) { onChanged(settings.copy(overlayHeight = it)) }
            Text("透明度: ${(settings.overlayOpacity*100).toInt()}%", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            Slider(settings.overlayOpacity, { onChanged(settings.copy(overlayOpacity = it)) }, valueRange = 0.1f..1.0f, modifier = Modifier.padding(horizontal = 16.dp))

            Divider(Modifier.padding(vertical = 8.dp))
            Text("样式设置", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
            ColorSetting("背景颜色", settings.overlayStyle.backgroundColor) { onChanged(settings.copy(overlayStyle = settings.overlayStyle.copy(backgroundColor = it))) }
            ColorSetting("文字颜色", settings.overlayStyle.textColor) { onChanged(settings.copy(overlayStyle = settings.overlayStyle.copy(textColor = it))) }
            ColorSetting("边框颜色", settings.overlayStyle.borderColor) { onChanged(settings.copy(overlayStyle = settings.overlayStyle.copy(borderColor = it))) }
            NumSetting("边框宽度", settings.overlayStyle.borderWidth) { onChanged(settings.copy(overlayStyle = settings.overlayStyle.copy(borderWidth = it))) }
            NumSetting("圆角大小", settings.overlayStyle.borderRadius) { onChanged(settings.copy(overlayStyle = settings.overlayStyle.copy(borderRadius = it))) }
            NumSetting("字体大小", settings.overlayStyle.fontSize) { onChanged(settings.copy(overlayStyle = settings.overlayStyle.copy(fontSize = it))) }

            Divider(Modifier.padding(vertical = 8.dp))
            Text("显示设置", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
            SwitchSetting("显示教师", settings.overlayStyle.showTeacherName) { onChanged(settings.copy(overlayStyle = settings.overlayStyle.copy(showTeacherName = it))) }
            SwitchSetting("显示教室", settings.overlayStyle.showClassroom) { onChanged(settings.copy(overlayStyle = settings.overlayStyle.copy(showClassroom = it))) }
            SwitchSetting("显示时间", settings.overlayStyle.showTime) { onChanged(settings.copy(overlayStyle = settings.overlayStyle.copy(showTime = it))) }
            SwitchSetting("显示当前课程", settings.overlayStyle.showCurrentClass) { onChanged(settings.copy(overlayStyle = settings.overlayStyle.copy(showCurrentClass = it))) }
            SwitchSetting("显示后续课程", settings.overlayStyle.showUpcomingClasses) { onChanged(settings.copy(overlayStyle = settings.overlayStyle.copy(showUpcomingClasses = it))) }
            NumSetting("后续课数量", settings.overlayStyle.upcomingClassesCount) { onChanged(settings.copy(overlayStyle = settings.overlayStyle.copy(upcomingClassesCount = it))) }
            NumSetting("行高", settings.overlayStyle.itemHeight) { onChanged(settings.copy(overlayStyle = settings.overlayStyle.copy(itemHeight = it))) }
            NumSetting("行间距", settings.overlayStyle.itemSpacing) { onChanged(settings.copy(overlayStyle = settings.overlayStyle.copy(itemSpacing = it))) }
        }

        Divider(Modifier.padding(vertical = 8.dp))
        Text("通用设置", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
        SwitchSetting("GPU 加速", settings.enableGpuAcceleration) { onChanged(settings.copy(enableGpuAcceleration = it)) }
        SwitchSetting("开机自启", settings.autoStartEnabled) { onChanged(settings.copy(autoStartEnabled = it)) }

        // Language
        val langs = listOf("zh" to "中文", "en" to "English", "ja" to "日本語")
        var expL by remember { mutableStateOf(false) }
        val curLang = langs.find { it.first == settings.language } ?: langs[0]
        ListItem(headlineContent = { Text("语言") }, supportingContent = { Text(curLang.second) }, trailingContent = {
            Box { TextButton({ expL = true }) { Text("更改") }; DropdownMenu(expL, { expL = false }) { langs.forEach { (c,n) -> DropdownMenuItem({ Text(n) }, onClick = { onChanged(settings.copy(language = c)); expL = false }) } } }
        })

        // Theme
        val themes = listOf("system" to "跟随系统", "light" to "浅色", "dark" to "深色")
        var expT by remember { mutableStateOf(false) }
        val curTheme = themes.find { it.first == settings.themeMode } ?: themes[0]
        ListItem(headlineContent = { Text("主题模式") }, supportingContent = { Text(curTheme.second) }, trailingContent = {
            Box { TextButton({ expT = true }) { Text("更改") }; DropdownMenu(expT, { expT = false }) { themes.forEach { (m,n) -> DropdownMenuItem({ Text(n) }, onClick = { onChanged(settings.copy(themeMode = m)); expT = false }) } } }
        })

        Spacer(Modifier.height(32.dp))
    }
}

@Composable fun SwitchSetting(title: String, checked: Boolean, onChange: (Boolean)->Unit) {
    ListItem(headlineContent = { Text(title) }, trailingContent = { Switch(checked, onChange) })
}

@Composable fun NumSetting(title: String, value: Int, onChange: (Int)->Unit) {
    var t by remember(value) { mutableStateOf(value.toString()) }
    ListItem(headlineContent = { Text(title) }, trailingContent = { OutlinedTextField(t, { t = it; onChange(it.toIntOrNull()?:value) }, singleLine = true, modifier = Modifier.width(100.dp)) })
}

@Composable fun ColorSetting(title: String, value: String, onChange: (String)->Unit) {
    var t by remember(value) { mutableStateOf(value) }
    ListItem(headlineContent = { Text(title) }, trailingContent = {
        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(t, { t = it; onChange(it) }, singleLine = true, modifier = Modifier.width(140.dp))
            androidx.compose.material3.Surface(Modifier.size(32.dp), shape = MaterialTheme.shapes.small, color = try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(value)) } catch (_: Exception) { MaterialTheme.colorScheme.surface }) {}
        }
    })
}