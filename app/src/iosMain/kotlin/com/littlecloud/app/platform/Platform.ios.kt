package com.littlecloud.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.littlecloud.app.bells.IosBells
import com.littlecloud.app.data.AppState
import com.littlecloud.app.resources.Res
import com.littlecloud.app.time.nowMillis
import com.littlecloud.app.ui.PrayerBooklet
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIMarkupTextPrintFormatter
import platform.UIKit.UIPrintInfo
import platform.UIKit.UIPrintInteractionController
import platform.UserNotifications.UNUserNotificationCenter

private class UserDefaultsPrefs : Prefs {
    private val d = NSUserDefaults.standardUserDefaults

    override fun getString(key: String, default: String?) = d.stringForKey(key) ?: default
    override fun getInt(key: String, default: Int) = if (contains(key)) d.integerForKey(key).toInt() else default
    override fun getLong(key: String, default: Long) = if (contains(key)) d.integerForKey(key) else default
    override fun getBoolean(key: String, default: Boolean) = if (contains(key)) d.boolForKey(key) else default
    override fun getStringSet(key: String, default: Set<String>?): Set<String>? =
        d.arrayForKey(key)?.filterIsInstance<String>()?.toSet() ?: default
    override fun contains(key: String) = d.objectForKey(key) != null
    override val keys: Set<String> get() = d.dictionaryRepresentation().keys.filterIsInstance<String>().toSet()
    override fun edit(): Prefs.Editor = Editor(d)

    /** NSUserDefaults saves as it goes, so apply() has nothing left to do. */
    private class Editor(private val d: NSUserDefaults) : Prefs.Editor {
        override fun putString(key: String, value: String?) = apply { if (value == null) d.removeObjectForKey(key) else d.setObject(value, key) }
        override fun putInt(key: String, value: Int) = apply { d.setInteger(value.toLong(), key) }
        override fun putLong(key: String, value: Long) = apply { d.setInteger(value, key) }
        override fun putBoolean(key: String, value: Boolean) = apply { d.setBool(value, key) }
        override fun putStringSet(key: String, value: Set<String>?) = apply { if (value == null) d.removeObjectForKey(key) else d.setObject(value.toList(), key) }
        override fun remove(key: String) = apply { d.removeObjectForKey(key) }
        override fun apply() {}
    }
}

private val prefs: Prefs by lazy { UserDefaultsPrefs() }

actual fun appPrefs(): Prefs = prefs

@OptIn(ExperimentalResourceApi::class)
actual fun readDataFile(name: String): String = runBlocking { Res.readBytes("files/$name").decodeToString() }

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS has no system back button; every screen has its own back arrow.
}

@Composable
actual fun PlatformEffects(dark: Boolean) {
    // Ask once for permission to show the bells as notifications.
    LaunchedEffect(Unit) {
        val prefs = appPrefs()
        if (!prefs.getBoolean("askedNotif", false)) {
            prefs.edit().putBoolean("askedNotif", true).apply()
            IosBells.requestPermission()
        }
    }
}

/** Printing through AirPrint, and the prayer timer as a local notification. */
object IosPlatform : PlatformActions {
    private const val TIMER_ID = "littlecloud.prayerTimer"

    override val appVersion: String =
        platform.Foundation.NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: ""
    override val deviceInfo: String =
        platform.UIKit.UIDevice.currentDevice.let { "${it.model} · ${it.systemName} ${it.systemVersion}" }

    override val canPrint: Boolean get() = UIPrintInteractionController.isPrintingAvailable()

    override fun printPage(title: String, html: String) {
        val pc = UIPrintInteractionController.sharedPrintController
        pc.printInfo = UIPrintInfo.printInfo().apply { setJobName("Little Cloud · $title") }
        pc.printFormatter = UIMarkupTextPrintFormatter(markupText = html)
        pc.presentAnimated(true, completionHandler = null)
    }

    override val bells = IosBells

    override fun startPrayerTimer(minutes: Int): Long {
        IosBells.prayerTimer(minutes, TIMER_ID)
        return nowMillis() + minutes * 60_000L
    }

    override fun cancelPrayerTimer() {
        UNUserNotificationCenter.currentNotificationCenter().removePendingNotificationRequestsWithIdentifiers(listOf(TIMER_ID))
    }
}
