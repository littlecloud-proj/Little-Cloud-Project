package com.littlecloud.app.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.littlecloud.app.data.AppState
import com.littlecloud.app.data.PrayerItem
import com.littlecloud.app.data.Store

private sealed interface Overlay {
    data class Reader(val items: List<PrayerItem>, val start: Int, val keyPrefix: String?) : Overlay
    data object Intimacy : Overlay
    data object Settings : Overlay
}

@Composable
fun LittleCloudApp(state: AppState, openRequest: MutableState<String?>) {
    val dark = when (state.theme) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }

    // Ask once for permission to post the bells as notifications (Android 13+).
    val ctx = LocalContext.current
    val askNotif = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        val prefs = Store.prefs(ctx)
        if (Build.VERSION.SDK_INT >= 33 && !prefs.getBoolean("askedNotif", false) &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            prefs.edit().putBoolean("askedNotif", true).apply()
            askNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LittleCloudTheme(dark) {
        var tab by rememberSaveable { mutableStateOf(Tab.TODAY) }
        var overlay by remember { mutableStateOf<Overlay?>(null) }
        var eveningTab by rememberSaveable { mutableStateOf(false) }
        // Scroll positions live here, so they survive opening a prayer and coming back.
        val todayScroll = androidx.compose.foundation.rememberScrollState()
        val officeScroll = androidx.compose.foundation.rememberScrollState()
        val calendarScroll = androidx.compose.foundation.rememberScrollState()
        val prayersScroll = androidx.compose.foundation.rememberScrollState()
        val bellsScroll = androidx.compose.foundation.rememberScrollState()

        val nav = remember {
            object : Nav {
                override fun goTab(tab: Tab) { overlay = null; setTab(tab) }
                override fun openPrayer(item: PrayerItem, keyPrefix: String?) {
                    if (item.prayerId != null || item.text != null) overlay = Overlay.Reader(listOf(item), 0, keyPrefix)
                }
                override fun openRule(evening: Boolean, index: Int) {
                    val list = if (evening) state.evening else state.morning
                    if (list.isNotEmpty()) overlay = Overlay.Reader(list, index, if (evening) "e|" else "m|")
                }
                override fun openIntimacy() { overlay = Overlay.Intimacy }
                override fun openSettings() { overlay = Overlay.Settings }
                override fun arrange(evening: Boolean) { eveningTab = evening; overlay = null; setTab(Tab.PRAYERS) }
                fun setTab(t: Tab) { tab = t }
            }
        }

        LaunchedEffect(openRequest.value) {
            val o = openRequest.value ?: return@LaunchedEffect
            when {
                o == "office" -> nav.goTab(Tab.HOURS)
                o == "intimacy" -> nav.openIntimacy()
                o == "evening" -> nav.arrange(true)
                o.startsWith("prayer:") -> nav.openPrayer(PrayerItem("", prayerId = o.removePrefix("prayer:")), null)
            }
            openRequest.value = null
        }

        BackHandler(enabled = overlay != null || tab != Tab.TODAY) {
            if (overlay != null) overlay = null else tab = Tab.TODAY
        }

        Column(Modifier.fillMaxSize().background(Lc.bg)) {
            Box(Modifier.weight(1f).statusBarsPadding()) {
                when (val o = overlay) {
                    is Overlay.Reader -> ReaderScreen(state, o.items, o.start, o.keyPrefix) { overlay = null }
                    Overlay.Intimacy -> IntimacyScreen(state) { overlay = null }
                    Overlay.Settings -> SettingsScreen(state) { overlay = null }
                    null -> when (tab) {
                        Tab.TODAY -> TodayScreen(state, nav, todayScroll)
                        Tab.HOURS -> OfficeScreen(state, nav, officeScroll)
                        Tab.CALENDAR -> CalendarScreen(state, calendarScroll)
                        Tab.PRAYERS -> PrayersScreen(state, nav, eveningTab, { eveningTab = it }, prayersScroll)
                        Tab.BELLS -> BellsScreen(state, bellsScroll)
                    }
                }
            }
            BottomNav(tab) { nav.goTab(it) }
        }
    }
}
