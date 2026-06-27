package com.classisland.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun LicenseDialog(onAccept: () -> Unit, onDecline: () -> Unit) {
    Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
        Card(Modifier.width(350.dp).wrapContentHeight()) {
            Column(Modifier.padding(24.dp)) {
                Text("许可协议", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                Column(Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        text = "本软件基于 ClassIsland 项目修改而来。\n\n原作者：ClassIsland 项目组\n原始项目：https://github.com/ClassIsland/ClassIsland\n\n修改者：XUE2026\n\n使用本软件即表示您同意：\n1. 本软件仅供学习交流使用\n2. 不得将本软件用于商业用途\n3. 修改者不对使用本软件产生的任何问题负责\n4. 请遵守相关法律法规",
                        fontSize = 14.sp, lineHeight = 22.sp
                    )
                }
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton({ onDecline() }, Modifier.weight(1f)) { Text("不同意") }
                    Button({ onAccept() }, Modifier.weight(1f)) { Text("同意并继续") }
                }
            }
        }
    }
}