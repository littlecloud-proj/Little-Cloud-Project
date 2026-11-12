package com.littlecloud.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.littlecloud.app.data.AppState
import com.littlecloud.app.data.Lives
import com.littlecloud.app.resources.*
import com.littlecloud.app.time.*

private val livesDate = DateTimeFormatter.ofPattern("EEEE, d MMMM")

/** The accounts of the day's feast and commemorations, read off the main thread. Null while loading. */
@Composable
fun rememberLives(state: AppState, date: LocalDate): List<Lives.Life>? {
    val day = state.dayInfo(date)
    val r by produceState<List<Lives.Life>?>(null, day.title, day.commemorations) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { Lives.forDay(day) }
    }
    return r
}

/** The short account of the day's feast or saint, with those of its commemorations. */
@Composable
fun LivesScreen(state: AppState, onBack: () -> Unit) {
    val list = rememberLives(state, state.today)
    val size = state.readingSize

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBtn(Res.drawable.ic_back, "Back", Lc.ink, onClick = onBack)
            LText("Feast of the day", 20, modifier = Modifier.weight(1f))
            LetterBtn("Smaller text", 16) { state.updateReadingSize((size - 1).coerceAtLeast(15)) }
            LetterBtn("Larger text", 23) { state.updateReadingSize((size + 1).coerceAtMost(26)) }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp).padding(top = 6.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Eyebrow(state.today.format(livesDate))
            when {
                list == null -> LText("…", 18, Lc.muted)
                list.isEmpty() -> LText("No account is on file for this day.", 16, Lc.muted)
                else -> list.forEach { life ->
                    Column {
                        Heading(life.title, 26)
                        Spacer(Modifier.height(10.dp))
                        life.text.split("\n").forEach {
                            LText(it, size, lineHeight = 1.5f, modifier = Modifier.padding(bottom = 8.dp))
                        }
                    }
                }
            }
            LText(
                "From the “Short Accounts of Certain Feasts and Lives of Saints” in The New Roman Missal " +
                    "of Rev. F. X. Lasance (Benziger Brothers, 1937).",
                13, Lc.muted,
            )
        }
    }
}
