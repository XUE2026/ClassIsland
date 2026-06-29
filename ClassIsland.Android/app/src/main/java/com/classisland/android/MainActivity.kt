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
    private var pendingTab by mutableIntStateOf(0)

    private val permOverlay =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { checkStart() }
    private val permNotify =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { checkStart() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = SettingsManager.get(this).load()

        // 处理启动时的 classisland:// URI
        handleClassIslandUri(intent)

        setContent {
            val allSettings = settings
            var showLicense by remember {
                mutableStateOf(allSettings.firstLaunch && !allSettings.licenseAccepted)
            }
            var showOverlayGuide by remember {
                mutableStateOf(shouldShowOverlayGuide(allSettings))
            }
            var currentTab by remember { mutableIntStateOf(pendingTab) }

            // 处理 URI 导航切换 Tab
            LaunchedEffect(pendingTab) {
                currentTab = pendingTab
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
                            // 接受许可后，如果启用了悬浮窗但无权限，才引导
                            if (updated.enableOverlay && !checkOverlayPermission()) {
                                showOverlayGuide = true
                            }
                        },
                        onDecline = { finish() }
                    )
                }

                if (showOverlayGuide) {
                    OverlayPermissionDialog(
                        onDismiss = {
                            // 用户跳过：关闭悬浮窗开关，下次不再无端引导
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
                    tab = currentTab,
                    onTabChange = { currentTab = it },
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleClassIslandUri(intent)
    }

    /** 解析 classisland:// URI 并导航到对应页面 */
    private fun handleClassIslandUri(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != "classisland") return

        val host = uri.host ?: return
        if (host != "app") return

        val path = uri.path ?: "/"
        when {
            path.startsWith("/settings") -> pendingTab = 1
            path.startsWith("/edit") || path.startsWith("/profile") -> pendingTab = 0
            else -> pendingTab = 0
        }
    }

    /** 判断是否需要显示悬浮窗权限引导 */
    private fun shouldShowOverlayGuide(s: AppSettings): Boolean {
        // 首次启动时 license 还没处理，不抢先引导
        if (s.firstLaunch) return false
        return s.enableOverlay && !checkOverlayPermission()
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
    tab: Int,
    onTabChange: (Int) -> Unit,
    onSettingsChanged: (AppSettings) -> Unit,
    onStartOverlay: () -> Unit,
    onRequestOverlayPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
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
                    selected = tab == 0,
                    onClick = { onTabChange(0) },
                    icon = { Icon(Icons.Default.Today, null) },
                    label = { Text("课表") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { onTabChange(1) },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("设置") }
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { onTabChange(2) },
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