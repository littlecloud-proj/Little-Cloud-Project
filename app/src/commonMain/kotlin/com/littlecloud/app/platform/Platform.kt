package com.littlecloud.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.littlecloud.app.data.AppState

/**
 * Saved settings, shaped like Android's SharedPreferences so the same code runs on both
 * phones. Android keeps them in SharedPreferences ("littlecloud"), iOS in NSUserDefaults.
 */
interface Prefs {
    fun getString(key: String, default: String?): String?
    fun getInt(key: String, default: Int): Int
    fun getLong(key: String, default: Long): Long
    fun getBoolean(key: String, default: Boolean): Boolean
    fun getStringSet(key: String, default: Set<String>?): Set<String>?
    fun contains(key: String): Boolean
    /** Every saved key (values are not needed by the app). */
    val keys: Set<String>
    fun edit(): Editor

    interface Editor {
        fun putString(key: String, value: String?): Editor
        fun putInt(key: String, value: Int): Editor
        fun putLong(key: String, value: Long): Editor
        fun putBoolean(key: String, value: Boolean): Editor
        fun putStringSet(key: String, value: Set<String>?): Editor
        fun remove(key: String): Editor
        fun apply()
    }
}

/** The app's settings store. */
expect fun appPrefs(): Prefs

/** Reads a bundled data file from composeResources/files, e.g. "calendar1954.json". Blocks. */
expect fun readDataFile(name: String): String

/** Things only the phone itself can do: print, ring a bell for silent prayer. */
interface PlatformActions {
    /** Whether printing is available on this phone. */
    val canPrint: Boolean
    fun print(state: AppState, evening: Boolean)

    /** Sets a bell to ring after [minutes] of silent prayer; returns when it will ring (epoch ms). */
    fun startPrayerTimer(minutes: Int): Long
    fun cancelPrayerTimer()
}

val LocalPlatform = staticCompositionLocalOf<PlatformActions> { error("PlatformActions not provided") }

/** System back gesture or button. */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)

/**
 * Runs once the app is on screen: keeps the status bar icons readable in light and dark,
 * and on Android asks once for permission to show the bells as notifications.
 */
@Composable
expect fun PlatformEffects(dark: Boolean)
