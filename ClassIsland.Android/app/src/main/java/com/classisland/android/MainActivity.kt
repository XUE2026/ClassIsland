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

    // 从系统覆盖层设置返回后，只启动服务，不重新请求权限
    private val permOverlay =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            checkAndStartService()
        }
    private val permNotify =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) checkAndStartService()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = SettingsManager.get(this).load()
        handleClassIslandUri(intent)

        setContent {
            val allSettings = settings
            var showLicense by remember {
                mutableStateOf(allSettings.firstLaunch && !allSettings.licenseAccepted)
            }
            // 响应 enableOverlay 变化，实时检测权限引导
            var showOverlayGuide by remember {
                mutableStateOf(shouldShowOverlayGuide(allSettings))
            }

            // 当 settings 中的 enableOverlay 变化时，重新评估引导弹窗
            LaunchedEffect(allSettings.enableOverlay) {
                if (!showLicense) {
                    showOverlayGuide = shouldShowOverlayGuide(allSettings)
                }
            }

            ClassIslandTheme(allSettings.themeMode) {
                if (showLicense) {
                    LicenseDialog(
                        onAccept = {
                            val updated = allSettings.copy(
                                firstLaunch = false,
                                licenseAccepted = true
                            )
                            settings = updated
                            SettingsManager.get(this@MainActivity).save(updated)
                            showLicense = false
                            if (shouldShowOverlayGuide(updated)) {
                                showOverlayGuide = true
                            }
                        },
                        onDecline = { finish() }
                    )
                }

                if (showOverlayGuide) {
                    OverlayPermissionDialog(
                        onDismiss = {
                            showOverlayGuide = false
                            if (allSettings.enableOverlay) {
                                val updated = allSettings.copy(enableOverlay = false)
                                settings = updated
                                SettingsManager.get(this@MainActivity).save(updated)
                            }
                        },
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
                        // 如果刚开启了悬浮窗，检查权限
                        if (s.enableOverlay && !checkOverlayPermission()) {
                            showOverlayGuide = true
                        }
                        // 如果开启了悬浮窗且有权限，启动服务
                        if (s.enableOverlay && checkOverlayPermission()) {
                            startService(Intent(this@MainActivity, OverlayService::class.java))
                        }
                        // 如果关闭了悬浮窗，停止服务
                        if (!s.enableOverlay) {
                            stopService(Intent(this@MainActivity, OverlayService::class.java))
                        }
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleClassIslandUri(intent)
    }

    private fun handleClassIslandUri(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != "classisland") return
        val host = uri.host ?: return
        if (host != "app") return
        // 不再用 pendingTab（因为现在用 LaunchedEffect 响应 settings 变化）
    }

    private fun shouldShowOverlayGuide(s: AppSettings): Boolean {
        if (s.firstLaunch) return false
        return s.enableOverlay && !checkOverlayPermission()
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permOverlay.launch(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"))
            )
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                Settings.canDrawOverlays(this)
    }

    /** 从系统设置返回后：检查权限，如果 OK 则启动服务 */
    private fun checkAndStartService() {
        val current = SettingsManager.get(this).load()
        if (!checkOverlayPermission()) return
        // 通知权限（Android 13+）
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permNotify.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        if (current.enableOverlay) {
            startService(Intent(this, OverlayService::class.java))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    settings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val profile = remember { ProfileService.get(ctx).load() }
    var refreshKey by remember { mutableIntStateOf(0) }
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(when (tab) {
                        0 -> "课表"
                        1 -> "设置"
                        2 -> "关于"
                        else -> "ClassIsland"
                    })
                },
                actions = {
                    if (tab == 0) {
                        IconButton(onClick = { refreshKey++ }) {
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
                    selected = tab == 0, onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Today, null) },
                    label = { Text("课表") }
                )
                NavigationBarItem(
                    selected = tab == 1, onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("设置") }
                )
                NavigationBarItem(
                    selected = tab == 2, onClick = { tab = 2 },
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
                modifier = Modifier.padding(padding)
            )
            2 -> AboutScreen(modifier = Modifier.padding(padding))
        }
    }
}