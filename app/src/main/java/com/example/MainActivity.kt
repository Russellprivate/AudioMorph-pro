package com.example

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.localization.StringsLocalization
import com.example.model.AppLanguage
import com.example.model.ThemeMode
import com.example.ui.components.AudioPlayerBottomBar
import com.example.ui.screens.CloudSyncScreen
import com.example.ui.screens.ConvertScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.AudioMorphTheme
import com.example.ui.theme.PrimaryViolet
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenTab

class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appSettings by viewModel.appSettings.collectAsState()
            val isLocked by viewModel.isLocked.collectAsState()
            val language = appSettings.language

            AudioMorphTheme(themeMode = appSettings.themeMode) {
                if (isLocked) {
                    LockScreen(viewModel = viewModel, language = language)
                } else {
                    MainAppScaffold(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val playerState by viewModel.playerManager.state.collectAsState()
    val language = appSettings.language

    // Handle back button: if not in Convert tab, go back to Convert tab
    BackHandler(enabled = currentTab != ScreenTab.CONVERT) {
        viewModel.selectTab(ScreenTab.CONVERT)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryViolet),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = StringsLocalization.get("app_title", language),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                actions = {
                    // Quick language toggle pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .clickable {
                                val nextLang = when (language) {
                                    AppLanguage.ENGLISH -> AppLanguage.BENGALI
                                    AppLanguage.BENGALI -> AppLanguage.SPANISH
                                    AppLanguage.SPANISH -> AppLanguage.HINDI
                                    AppLanguage.HINDI -> AppLanguage.ENGLISH
                                }
                                viewModel.setLanguage(nextLang)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("quick_language_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = language.nativeName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Lock button if lock is configured
                    if (appSettings.biometricEnabled || appSettings.pinCode.isNotBlank()) {
                        IconButton(
                            onClick = { viewModel.lockApp() },
                            modifier = Modifier.testTag("topbar_lock_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock App",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Column {
                // Persistent Docked Player Bar above navigation bar
                AnimatedVisibility(
                    visible = playerState.currentItem != null,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    AudioPlayerBottomBar(
                        state = playerState,
                        onTogglePlayPause = { viewModel.playerManager.togglePlayPause() },
                        onSeekTo = { viewModel.playerManager.seekTo(it) },
                        onToggleLoop = { viewModel.playerManager.toggleLoop() },
                        onSetSpeed = { viewModel.playerManager.setSpeed(it) },
                        onStop = { viewModel.playerManager.stop() }
                    )
                }

                // Standard Material 3 Navigation Bar
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 8.dp
                ) {
                    val tabs = listOf(
                        ScreenTab.CONVERT to (
                                StringsLocalization.get("tab_convert", language) to
                                        (Icons.Filled.Movie to Icons.Outlined.Movie)
                                ),
                        ScreenTab.STUDIO to (
                                StringsLocalization.get("tab_studio", language) to
                                        (Icons.Filled.ContentCut to Icons.Outlined.ContentCut)
                                ),
                        ScreenTab.LIBRARY to (
                                StringsLocalization.get("tab_library", language) to
                                        (Icons.Filled.QueueMusic to Icons.Outlined.QueueMusic)
                                ),
                        ScreenTab.CLOUD_SYNC to (
                                StringsLocalization.get("tab_cloud", language) to
                                        (Icons.Filled.CloudSync to Icons.Outlined.CloudSync)
                                ),
                        ScreenTab.SETTINGS to (
                                StringsLocalization.get("tab_settings", language) to
                                        (Icons.Filled.Settings to Icons.Outlined.Settings)
                                )
                    )

                    tabs.forEach { (tab, details) ->
                        val (label, icons) = details
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) icons.first else icons.second,
                                    contentDescription = label
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    ScreenTab.CONVERT -> ConvertScreen(viewModel = viewModel)
                    ScreenTab.STUDIO -> StudioScreen(viewModel = viewModel)
                    ScreenTab.LIBRARY -> LibraryScreen(viewModel = viewModel)
                    ScreenTab.CLOUD_SYNC -> CloudSyncScreen(viewModel = viewModel)
                    ScreenTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
