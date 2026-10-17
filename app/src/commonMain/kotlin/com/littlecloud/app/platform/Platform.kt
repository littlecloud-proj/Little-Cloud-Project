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
    /** The app's version, e.g. "0.8". */
    val appVersion: String
    /** The phone and its system, e.g. "Pixel 8 · Android 15", for feedback emails. */
    val deviceInfo: String

    /** Whether printing is available on this phone. */
    val canPrint: Boolean
    fun print(state: AppState, evening: Boolean)

    /** Sets a bell to ring after [minutes] of silent prayer; returns when it will ring (epoch ms). */
    fun startPrayerTimer(minutes: Int): Long
    fun cancelPrayerTimer()

    val bells: BellSystem
}

/** How each phone schedules and sounds the bells. */
interface BellSystem {
    data class Sound(val key: String, val label: String, val note: String)

    /** The bundled bell sounds a bell may use. */
    val sounds: List<Sound>
    fun label(key: String): String

    /** Whether a bell can ring like an alarm, through silent mode and over the lock screen. */
    val canRingLoud: Boolean

    /** The small print under the list of bells. */
    val footnote: String

    /** Schedules every enabled bell again, after a change or when the app opens. */
    fun reschedule()

    /** Rings a bell at once, as it will ring when it is due. */
    fun ringNow(name: String, sound: String)

    /** Plays a short preview of a sound (null stops it); stops when the screen goes away. */
    @Composable
    fun rememberPlayer(): (String?) -> Unit

    /** A way to choose any sound on the phone, or null where there is none. */
    @Composable
    fun rememberCustomPicker(onPicked: (String) -> Unit): (() -> Unit)?

    /** Cards asking for permissions the bells need, if any are missing. */
    @Composable
    fun Notices(state: AppState)
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
