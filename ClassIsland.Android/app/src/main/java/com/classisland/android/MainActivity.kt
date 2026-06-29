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
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.classisland.android.model.AppSettings
import com.classisland.android.overlay.OverlayService
import com.classisland.android.service.ProfileService
import com.classisland.android.ui.components.LicenseDialog
import com.classisland.android.ui.components.OverlayPermissionDialog
import com.classisland.android.ui.screens.AboutScreen
import com.classisland.android.ui.screens.SettingsScreen
import com.classisland.android.ui.screens.TimetableScreen
import com.classisland.android.ui.theme.ClassIslandTheme
import com.classisland.android.util.SettingsManager

class MainActivity : ComponentActivity() {
    private var settings by mutableStateOf(AppSettings())
    private val permOverlay =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { checkStart() }
    private val permNotify =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { checkStart() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = SettingsManager.get(this).load()

        setContent {
            val allSettings = settings
            var showLicense by remember {
                mutableStateOf(allSettings.firstLaunch && !allSettings.licenseAccepted)
            }
            var showOverlayGuide by remember { mutableStateOf(false) }

            ClassIslandTheme(allSettings.themeMode) {
                if (showLicense) {
                    LicenseDialog(
                        onAccept = {
                            settings = allSettings.copy(
                                firstLaunch = false,
                                licenseAccepted = true
                            )
                            SettingsManager.get(this@MainActivity).save(settings)
                            showLicense = false
                            showOverlayGuide = true
                        },
                        onDecline = { finish() }
                    )
                }

                if (showOverlayGuide) {
                    OverlayPermissionDialog(
                        onDismiss = { showOverlayGuide = false },
                        onRequestPermission = {
                            showOverlayGuide = false
                            requestOverlayPermission()
                        }
                    )
                }

                MainScreen(
                    settings = allSettings,
                    onSettingsChanged = { s ->
                        settings = s
                        SettingsManager.get(this@MainActivity).save(s)
                    },
                    onStartOverlay = {
                        if (checkOverlayPermission()) {
                            startService(Intent(this@MainActivity, OverlayService::class.java))
                        } else {
                            showOverlayGuide = true
                        }
                    },
                    onRequestOverlayPermission = { requestOverlayPermission() }
                )
            }
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permOverlay.launch(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                Settings.canDrawOverlays(this)
    }

    private fun checkStart() {
        if (!checkOverlayPermission()) {
            requestOverlayPermission()
            return
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permNotify.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        if (settings.enableOverlay) {
            startService(Intent(this, OverlayService::class.java))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    settings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit,
    onStartOverlay: () -> Unit,
    onRequestOverlayPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    var tab by remember { mutableIntStateOf(0) }
    val profile = remember { ProfileService.get(ctx).load() }
    var refreshKey by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (tab) {
                            0 -> "课表"
                            1 -> "设置"
                            2 -> "关于"
                            else -> "ClassIsland"
                        }
                    )
                },
                actions = {
                    if (tab == 0) {
                        IconButton(onClick = {
                            refreshKey++
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "刷新")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Today, null) },
                    label = { Text("课表") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("设置") }
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.Info, null) },
                    label = { Text("关于") }
                )
            }
        }
    ) { padding ->
        when (tab) {
            0 -> TimetableScreen(modifier = Modifier.padding(padding).then(Modifier.fillMaxSize()))
            1 -> SettingsScreen(
                settings = settings,
                onChanged = onSettingsChanged,
                classPlans = profile.classPlans.toList(),
                onSyncNow = {
                    // 手动同步触发
                },
                modifier = Modifier.padding(padding)
            )
            2 -> AboutScreen(modifier = Modifier.padding(padding))
        }
    }
}