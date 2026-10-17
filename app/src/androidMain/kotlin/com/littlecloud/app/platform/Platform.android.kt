package com.littlecloud.app.platform

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.littlecloud.app.bells.BellScheduler
import com.littlecloud.app.data.AppState
import com.littlecloud.app.data.Store
import com.littlecloud.app.resources.Res
import com.littlecloud.app.ui.PrayerPrinter
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi

/** The application context, set when the app process starts (see LittleCloudApplication). */
@SuppressLint("StaticFieldLeak")
object AndroidApp {
    lateinit var context: Context
}

private class SharedPrefs(private val p: SharedPreferences) : Prefs {
    override fun getString(key: String, default: String?) = p.getString(key, default)
    override fun getInt(key: String, default: Int) = p.getInt(key, default)
    override fun getLong(key: String, default: Long) = p.getLong(key, default)
    override fun getBoolean(key: String, default: Boolean) = p.getBoolean(key, default)
    override fun getStringSet(key: String, default: Set<String>?): Set<String>? = p.getStringSet(key, default)
    override fun contains(key: String) = p.contains(key)
    override val keys: Set<String> get() = p.all.keys
    override fun edit(): Prefs.Editor = Editor(p.edit())

    private class Editor(private val e: SharedPreferences.Editor) : Prefs.Editor {
        override fun putString(key: String, value: String?) = apply { e.putString(key, value) }
        override fun putInt(key: String, value: Int) = apply { e.putInt(key, value) }
        override fun putLong(key: String, value: Long) = apply { e.putLong(key, value) }
        override fun putBoolean(key: String, value: Boolean) = apply { e.putBoolean(key, value) }
        override fun putStringSet(key: String, value: Set<String>?) = apply { e.putStringSet(key, value) }
        override fun remove(key: String) = apply { e.remove(key) }
        override fun apply() = e.apply()
    }
}

@Volatile private var prefs: Prefs? = null

actual fun appPrefs(): Prefs =
    prefs ?: SharedPrefs(AndroidApp.context.getSharedPreferences(Store.PREFS, Context.MODE_PRIVATE)).also { prefs = it }

@OptIn(ExperimentalResourceApi::class)
actual fun readDataFile(name: String): String = runBlocking { Res.readBytes("files/$name").decodeToString() }

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = BackHandler(enabled, onBack)

@Composable
actual fun PlatformEffects(dark: Boolean) {
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
        val prefs = Store.prefs()
        if (Build.VERSION.SDK_INT >= 33 && !prefs.getBoolean("askedNotif", false) &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            prefs.edit().putBoolean("askedNotif", true).apply()
            askNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

/** Printing and the prayer timer, through Android's print service and alarms. */
class AndroidPlatform(private val activity: Activity) : PlatformActions {
    override val bells = com.littlecloud.app.bells.AndroidBells(activity)
    override val canPrint = true
    override val appVersion: String =
        runCatching { activity.packageManager.getPackageInfo(activity.packageName, 0).versionName }.getOrNull() ?: ""
    override val deviceInfo: String = "${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE}"
    override fun print(state: AppState, evening: Boolean) = PrayerPrinter.print(activity, state, evening)
    override fun startPrayerTimer(minutes: Int): Long = BellScheduler.startPrayerTimer(activity.applicationContext, minutes)
    override fun cancelPrayerTimer() = BellScheduler.cancelPrayerTimer(activity.applicationContext)
}
