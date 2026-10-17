package com.littlecloud.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.littlecloud.app.data.AppState
import com.littlecloud.app.data.Readings
import com.littlecloud.app.resources.*
import com.littlecloud.app.time.*

private val readingsDate = DateTimeFormatter.ofPattern("EEEE, d MMMM")

/** The day's readings, loaded off the main thread (the file is large). Null while loading. */
@Composable
fun rememberReadings(state: AppState, date: LocalDate): List<Readings.Reading>? {
    val r by produceState<List<Readings.Reading>?>(null, date, state.rubrics) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            runCatching { state.readings(date) }.getOrDefault(emptyList())
        }
    }
    return r
}

private fun label(r: Readings.Reading) = when {
    r.gospel -> "Gospel"
    r.titleLa.contains("Epíst", ignoreCase = true) || r.titleLa.contains("Epist", ignoreCase = true) -> "Epistle"
    else -> "Lesson"
}

/** Today's card: the citations and the opening of each reading. */
@Composable
fun ReadingsCard(state: AppState, onOpen: () -> Unit) {
    val list = rememberReadings(state, state.today) ?: return
    if (list.isEmpty()) return
    val english = state.officeLang == "english"
    LCard(padding = 20.dp) {
        Eyebrow("Today’s readings")
        list.forEachIndexed { i, r ->
            Spacer(Modifier.height(if (i == 0) 10.dp else 14.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                LText(label(r), 19, modifier = Modifier.weight(1f))
                LText(r.citation, 14, Lc.muted)
            }
            Spacer(Modifier.height(4.dp))
            val text = if (english && r.en != null) r.en else r.la
            LText(text, 16, Lc.muted, lineHeight = 1.4f, maxLines = 3)
        }
        Spacer(Modifier.height(14.dp))
        TextLink("Read the Epistle and Gospel", onOpen)
    }
}

/** The day's Epistle and Gospel in full, in Latin, English or both. */
@Composable
fun ReadingsScreen(state: AppState, onBack: () -> Unit) {
    val list = rememberReadings(state, state.today)
    val show = laEn(state)
    val showLatin = show.latin
    val showEnglish = show.english
    val size = state.readingSize

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBtn(Res.drawable.ic_back, "Back", Lc.ink, onClick = onBack)
            LText("Today’s readings", 20, modifier = Modifier.weight(1f))
            LetterBtn("Smaller text", 16) { state.updateReadingSize((size - 1).coerceAtLeast(15)) }
            LetterBtn("Larger text", 23) { state.updateReadingSize((size + 1).coerceAtMost(26)) }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp).padding(top = 6.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column {
                Eyebrow(state.today.format(readingsDate))
                Spacer(Modifier.height(8.dp))
                Heading(state.dayInfo(state.today).title, 28)
            }
            Segmented(langOptions(state), state.officeLang) { state.updateOfficeLang(it) }
            if (show.englishForSpanish) LText(NO_SPANISH_NOTE, 14, Lc.muted)
            when {
                list == null -> LText("…", 18, Lc.muted)
                list.isEmpty() -> LText("No readings are on file for this day.", 16, Lc.muted)
                else -> list.forEach { r ->
                    Column {
                        SectionLabel(label(r))
                        LText(r.citation, 14, Lc.muted)
                        val latinHere = showLatin || r.en == null
                        if (latinHere) {
                            Spacer(Modifier.height(8.dp))
                            LText(r.titleLa, size - 2, Lc.accent, lineHeight = 1.35f)
                            Spacer(Modifier.height(4.dp))
                            r.la.split("\n").forEach {
                                LText(it, size, lineHeight = 1.5f, modifier = Modifier.padding(bottom = 6.dp))
                            }
                        }
                        if (showEnglish && r.en != null) {
                            Spacer(Modifier.height(if (latinHere) 12.dp else 8.dp))
                            if (r.titleEn.isNotEmpty()) {
                                LText(r.titleEn, size - 2, Lc.accent, lineHeight = 1.35f)
                                Spacer(Modifier.height(4.dp))
                            }
                            LText(r.en, if (latinHere) size - 1 else size, if (latinHere) Lc.muted else Lc.ink, lineHeight = 1.5f)
                        }
                    }
                }
            }
            LText(
                "Latin: the Roman Missal, from the Divinum Officium Project. English: the Douay-Rheims Bible " +
                    "(Challoner revision), from Project Gutenberg, taken by the citation.",
                13, Lc.muted,
            )
        }
    }
}
