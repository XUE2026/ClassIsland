package com.classisland.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.classisland.android.model.AppSettings
import com.classisland.android.overlay.OverlayService
import com.classisland.android.ui.components.LicenseDialog
import com.classisland.android.ui.screens.AboutScreen
import com.classisland.android.ui.screens.OverlaySettingsScreen
import com.classisland.android.ui.theme.ClassIslandTheme
import com.classisland.android.util.SettingsManager

class MainActivity : ComponentActivity() {
    private var settings by mutableStateOf(AppSettings())
    private val permOverlay = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { checkStart() }
    private val permNotify = registerForActivityResult(ActivityResultContracts.RequestPermission()) { checkStart() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = SettingsManager.get(this).load()

        setContent {
            var showLicense by remember { mutableStateOf(settings.firstLaunch && !settings.licenseAccepted) }
            ClassIslandTheme(settings.themeMode) {
                if (showLicense) LicenseDialog(
                    onAccept = {
                        settings = settings.copy(firstLaunch = false, licenseAccepted = true)
                        SettingsManager.get(this@MainActivity).save(settings)
                        showLicense = false
                    },
                    onDecline = { finish() }
                )

                var tab by remember { mutableStateOf(0) }
                Scaffold(bottomBar = {
                    NavigationBar {
                        NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Default.Tune, null) }, label = { Text("设置") })
                        NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Icon(Icons.Default.Info, null) }, label = { Text("关于") })
                    }
                }) { p ->
                    when (tab) {
                        0 -> OverlaySettingsScreen(settings, { s ->
                            settings = s; SettingsManager.get(this@MainActivity).save(s)
                        }, Modifier.padding(p))
                        1 -> AboutScreen(Modifier.padding(p))
                    }
                }
            }
        }
    }

    private fun checkStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            permOverlay.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            return
        }
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permNotify.launch(Manifest.permission.POST_NOTIFICATIONS); return
        }
        startService(Intent(this, OverlayService::class.java))
    }
}