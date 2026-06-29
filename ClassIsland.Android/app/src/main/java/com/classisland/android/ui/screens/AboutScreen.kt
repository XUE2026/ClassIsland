package com.classisland.android.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.classisland.android.util.Constants

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(32.dp))
        Icon(Icons.Default.School, null, Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text("ClassIsland", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("智慧课表 · 悬浮窗", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Text("版本 0.1.0 (预览)", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp)); Divider(); Spacer(Modifier.height(16.dp))

        ListItem(headlineContent = { Text("原作者") }, supportingContent = { Text("ClassIsland 项目组") }, leadingContent = { Icon(Icons.Default.Person, null) })
        ListItem(headlineContent = { Text("修改者") }, supportingContent = { Text(Constants.MODIFIER) }, leadingContent = { Icon(Icons.Default.Build, null) })
        ListItem(headlineContent = { Text("原始 GitHub 项目") }, supportingContent = { Text(Constants.ORIGINAL_GITHUB) }, leadingContent = { Icon(Icons.Default.Code, null) }, trailingContent = { IconButton({ ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.ORIGINAL_GITHUB))) }) { Icon(Icons.Default.OpenInNew, null) } })
        ListItem(headlineContent = { Text("修改者 GitHub") }, supportingContent = { Text(Constants.MODIFIER_GITHUB) }, leadingContent = { Icon(Icons.Default.Code, null) }, trailingContent = { IconButton({ ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.MODIFIER_GITHUB))) }) { Icon(Icons.Default.OpenInNew, null) } })

        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("许可信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("本软件基于 ClassIsland 项目修改而来。\n\n原作者：ClassIsland 项目组\n修改者：XUE2026\n\n本软件仅供学习交流使用，不得用于商业用途。", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}