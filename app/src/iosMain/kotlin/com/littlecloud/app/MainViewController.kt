package com.littlecloud.app

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.ComposeUIViewController
import com.littlecloud.app.data.AppState
import com.littlecloud.app.platform.IosPlatform
import com.littlecloud.app.ui.LittleCloudApp
import platform.UIKit.UIViewController

private val state by lazy { AppState() }
private val openRequest = mutableStateOf<String?>(null)

/**
 * Called from Swift (iosApp/iOSApp.swift). [open] names a screen to show first, as the
 * bells do on Android: "office", "intimacy", "settings", "tab:calendar", "prayer:angelus" …
 */
fun MainViewController(open: String?): UIViewController {
    openRequest.value = open
    return ComposeUIViewController { LittleCloudApp(state, openRequest, IosPlatform) }
}

/** Called from Swift when the app comes back to the foreground, so Today follows the date. */
fun refreshDay() = state.refreshDay()
