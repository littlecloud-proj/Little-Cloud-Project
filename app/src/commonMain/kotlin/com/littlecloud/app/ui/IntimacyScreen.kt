package com.littlecloud.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.littlecloud.app.resources.*
import com.littlecloud.app.platform.LocalPlatform
import com.littlecloud.app.data.AppState
import com.littlecloud.app.data.Store
import kotlinx.coroutines.delay
import com.littlecloud.app.time.*

private val headFmt = DateTimeFormatter.ofPattern("EEEE, d MMMM")

@Composable
fun IntimacyScreen(state: AppState, onBack: () -> Unit) {
    val platform = LocalPlatform.current
    val prefs = remember { Store.prefs() }
    var minutes by remember { mutableIntStateOf(prefs.getInt("prayerMinutes", 30)) }
    var size by remember { mutableIntStateOf(prefs.getInt("readSize", 18)) }
    var endsAt by remember { mutableLongStateOf(prefs.getLong("prayerEndsAt", 0L)) }
    var now by remember { mutableLongStateOf(nowMillis()) }
    val running = endsAt > now

    LaunchedEffect(endsAt) {
        while (endsAt > nowMillis()) {
            now = nowMillis()
            delay(1000)
        }
        now = nowMillis()
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBtn(Res.drawable.ic_back, "Back", Lc.ink, onClick = onBack)
            LText("Mental prayer", 20, modifier = Modifier.weight(1f))
            LetterBtn("Smaller text", 16) {
                size = (size - 1).coerceAtLeast(15); prefs.edit().putInt("readSize", size).apply()
            }
            LetterBtn("Larger text", 23) {
                size = (size + 1).coerceAtMost(26); prefs.edit().putInt("readSize", size).apply()
            }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp).padding(top = 6.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column {
                Eyebrow(state.today.format(headFmt))
                Spacer(Modifier.height(8.dp))
                Heading("Mental prayer", 31)
                Spacer(Modifier.height(6.dp))
                LText("Mental prayer, St. Teresa teaches, is friendship with God: speaking often, alone, with Him who we know loves us.", 15, Lc.muted, lineHeight = 1.45f)
            }

            LCard(padding = 16.dp) {
                LText("Timer", 16)
                Spacer(Modifier.height(12.dp))
                if (running) {
                    val left = ((endsAt - now) / 1000).coerceAtLeast(0)
                    LText("${left / 60}:${(left % 60).toString().padStart(2, '0')}", 40, Lc.accent)
                    LText("A bell will ring when the time is over, even if the phone is locked.", 14, Lc.muted)
                    Spacer(Modifier.height(12.dp))
                    OutlineButton("End early", Modifier.fillMaxWidth()) {
                        platform.cancelPrayerTimer()
                        endsAt = 0L; prefs.edit().putLong("prayerEndsAt", 0L).apply()
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(15, 30, 45, 60).forEach { m ->
                            val on = m == minutes
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(if (on) Lc.soft else Color.Transparent)
                                    .border(1.dp, if (on) Lc.accent else Lc.line, RoundedCornerShape(22.dp))
                                    .selectable(selected = on, role = Role.RadioButton) {
                                        minutes = m; prefs.edit().putInt("prayerMinutes", m).apply()
                                    },
                                contentAlignment = Alignment.Center,
                            ) { LText("$m min", 16, if (on) Lc.ink else Lc.muted) }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton("Begin · a bell will ring at $minutes minutes", icon = Res.drawable.ic_bell) {
                        val at = platform.startPrayerTimer(minutes)
                        endsAt = at; now = nowMillis()
                        prefs.edit().putLong("prayerEndsAt", at).apply()
                    }
                }
            }

            Part("The Carmelite method", listOf(
                "Preparation · Place yourself in the presence of God, make an act of humility and ask for light.",
                "Reading · Read a short passage of Scripture or a spiritual book, slowly.",
                "Meditation · Turn it over in your mind and apply it to your soul.",
                "Colloquy · Speak with Our Lord simply, as a friend; then rest in His presence and listen.",
                "Thanksgiving · Thank God for the graces of this prayer.",
                "Offering · Make a small resolution for the day and offer it to Him.",
                "Petition · Ask His help for yourself, the Church, the souls in Purgatory and sinners.",
            ), size)

            val key = "di|${state.today}"
            val made = state.isDone(key)
            PrimaryButton(if (made) "Prayer made · Deo gratias" else "Mark mental prayer as made") { state.toggleDone(key) }
        }
    }
}

@Composable
private fun Part(title: String, paragraphs: List<String>, size: Int) {
    Column {
        SectionLabel(title)
        paragraphs.forEach {
            LText(it, size, lineHeight = 1.55f)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun LetterBtn(label: String, size: Int, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) { LText("A", size, Lc.muted) }
}
