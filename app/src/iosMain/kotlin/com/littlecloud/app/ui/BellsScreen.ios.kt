package com.littlecloud.app.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.littlecloud.app.data.AppState

@Composable
actual fun BellsScreen(state: AppState, scroll: ScrollState) {
    Column(
        Modifier.fillMaxSize().verticalScroll(scroll).padding(horizontal = 22.dp).padding(top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Heading("Bells", 31)
        LText("The bells are coming to iPhone in a later build.", 16, Lc.muted, lineHeight = 1.45f)
    }
}
