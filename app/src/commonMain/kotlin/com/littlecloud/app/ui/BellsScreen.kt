package com.littlecloud.app.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import com.littlecloud.app.data.AppState

/** The Bells tab. Each phone schedules and rings bells its own way. */
@Composable
expect fun BellsScreen(state: AppState, scroll: ScrollState)
