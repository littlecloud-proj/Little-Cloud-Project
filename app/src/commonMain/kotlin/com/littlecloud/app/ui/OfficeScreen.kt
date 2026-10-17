package com.littlecloud.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.littlecloud.app.data.AppState
import com.littlecloud.app.data.Calendar1962
import com.littlecloud.app.data.HourSlot
import com.littlecloud.app.data.LittleOffice
import com.littlecloud.app.time.*

private data class Verse(val latin: String, val english: String)

private fun opening(hourId: String, alleluia: Boolean): List<Pair<Verse, Verse>> {
    val deus = Verse("Deus, in adjutórium meum inténde.", "O God, come to my assistance.") to
        Verse("Dómine, ad adjuvándum me festína.", "O Lord, make haste to help me.")
    val first = when (hourId) {
        "matins" -> listOf(
            Verse("Dómine, lábia mea apéries.", "O Lord, Thou wilt open my lips.") to
                Verse("Et os meum annuntiábit laudem tuam.", "And my mouth shall declare Thy praise."),
        )
        "compline" -> listOf(
            Verse("Convérte nos, Deus, salutáris noster.", "Convert us, O God our Saviour.") to
                Verse("Et avérte iram tuam a nobis.", "And turn away Thy anger from us."),
        )
        else -> emptyList()
    }
    val gloria = Verse("Glória Patri, et Fílio, et Spirítui Sancto.", "Glory be to the Father, and to the Son, and to the Holy Ghost.") to
        Verse(
            "Sicut erat in princípio, et nunc, et semper, et in sǽcula sæculórum. Amen. " +
                if (alleluia) "Allelúja." else "Laus tibi, Dómine, Rex ætérnæ glóriæ.",
            "As it was in the beginning, is now, and ever shall be, world without end. Amen. " +
                if (alleluia) "Alleluia." else "Praise be to Thee, O Lord, King of eternal glory.",
        )
    return first + deus + gloria
}

private fun outline(hourId: String, antiphon: String): String = when (hourId) {
    "matins" -> "[Invitatory Ave Maria · Hymn Quem terra, pontus, sidera · Psalms of the nocturn · Three lessons · Te Deum]"
    "lauds" -> "[Psalms with antiphons · Little chapter · Hymn O gloriosa virginum · Benedictus · Prayer]"
    "vespers" -> "[Psalms with antiphons · Little chapter · Hymn Ave maris stella · Magnificat · Prayer]"
    "compline" -> "[Psalms · Hymn Memento rerum Conditor · Little chapter · Nunc dimittis · Prayer]\n\nFinal antiphon: $antiphon"
    else -> "[Hymn Memento rerum Conditor · Three psalms with antiphon · Little chapter · Versicle · Prayer]"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficeScreen(state: AppState, nav: Nav, scroll: androidx.compose.foundation.ScrollState = rememberScrollState()) {
    val today = state.today
    val nowMin = LocalTime.now().let { it.hour * 60 + it.minute }
    val slots = state.slots(today)
    val next = slots.firstOrNull { !state.isSaid(it) && it.minutes >= nowMin - 60 }
        ?: slots.firstOrNull { !state.isSaid(it) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = slots.firstOrNull { it.slotId == selectedId } ?: next ?: slots.last()
    var editing by remember { mutableStateOf<HourSlot?>(null) }
    val platform = com.littlecloud.app.platform.LocalPlatform.current
    val tomorrowTitle = remember(today, state.rubrics) { state.dayInfo(today.plusDays(1)).title }
    val (antiphon, antiphonId) = Calendar1962.finalMarianAntiphon(today)

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)) {
            Eyebrow("The Little Office")
            Spacer(Modifier.height(4.dp))
            Heading("of the Blessed Virgin Mary", 30)
            Spacer(Modifier.height(6.dp))
            LText(Calendar1962.littleOfficeSeason(today), 15, Lc.muted)
        }
        Column(
            Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 18.dp).padding(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LCard {
                slots.forEachIndexed { i, h ->
                    if (i > 0) HLine()
                    val d = state.isSaid(h)
                    val isSel = h.slotId == selected.slotId
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(if (isSel) Lc.soft else Color.Transparent)
                            .clickable { selectedId = h.slotId }
                            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier.width(78.dp).heightIn(min = 44.dp)
                                .clickable(role = Role.Button, onClickLabel = "Change time of ${h.hour.latin}") { editing = h }
                                .padding(start = 10.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) { LText(fmtTime(h.minutes), 15, Lc.muted) }
                        Column(Modifier.weight(1f)) {
                            LText(h.hour.latin, 19)
                            LText(if (h.anticipated) "Of tomorrow · $tomorrowTitle" else h.hour.english, 14, Lc.muted, maxLines = 2)
                        }
                        if (h == next) {
                            Box(Modifier.clip(RoundedCornerShape(12.dp)).background(Lc.accent).padding(horizontal = 10.dp, vertical = 4.dp)) {
                                LText("Next", 13, Lc.on, spacing = 0.1f, upper = true)
                            }
                        }
                        CheckDot(d, (if (d) "Unmark " else "Mark as said: ") + h.hour.latin) { state.toggleSaid(h) }
                    }
                }
            }

            LCard(padding = 18.dp) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Heading(selected.hour.latin + if (selected.anticipated) " of tomorrow" else "", 24, Modifier.weight(1f))
                    LText(fmtTime(selected.minutes), 14, Lc.muted)
                }
                Spacer(Modifier.height(14.dp))
                Segmented(langOptions(state), state.officeLang) { state.updateOfficeLang(it) }
                Spacer(Modifier.height(16.dp))
                val rubrics = state.rubrics
                val officeDate = selected.date
                val blocks by produceState(initialValue = emptyList<LittleOffice.Block>(), officeDate, selected.id, rubrics) {
                    value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                        runCatching { LittleOffice.blocks(officeDate, selected.id, rubrics) }.getOrDefault(emptyList())
                    }
                }
                val show = laEn(state)
                val showLatin = show.latin
                val showEnglish = show.english
                if (show.englishForSpanish) {
                    LText(NO_SPANISH_NOTE, 14, Lc.muted)
                    Spacer(Modifier.height(12.dp))
                }
                if (blocks.isEmpty()) LText("…", 18, Lc.muted)
                blocks.forEach { b ->
                    val head = if (showLatin) b.headLa ?: b.headEn else b.headEn ?: b.headLa
                    if (head != null) {
                        Spacer(Modifier.height(10.dp))
                        SectionLabel(head)
                    }
                    if (showLatin) b.la.forEach { OfficeLine(it, latin = true, dim = false) }
                    if (showLatin && showEnglish) Spacer(Modifier.height(8.dp))
                    if (showEnglish) b.en.forEach { OfficeLine(it, latin = false, dim = showLatin) }
                    Spacer(Modifier.height(12.dp))
                }
                if (selected.id == "compline" && antiphonId != null) {
                    TextLink("Pray the $antiphon on its own") {
                        nav.openPrayer(com.littlecloud.app.data.PrayerItem(antiphon, prayerId = antiphonId))
                    }
                }
                LText(
                    "Text: Divinum Officium Project. English: Douay-Rheims psalms, Britt’s hymns, other texts from the Bute translation (1908).",
                    13, Lc.muted,
                )
                Spacer(Modifier.height(16.dp))
                val d = state.isSaid(selected)
                PrimaryButton(if (d) "${selected.hour.latin} said · Deo gratias" else "Mark ${selected.hour.latin} as said") {
                    state.toggleSaid(selected)
                }
            }

            AnticipationCard(state) { if (it) platform.bells.reschedule() }

            LText("Tap a time to change it. The Little Office bell is set on the Bells tab.", 14, Lc.muted)
        }
    }

    editing?.let { h ->
        val tp = rememberTimePickerState(h.minutes / 60, h.minutes % 60, false)
        AlertDialog(
            onDismissRequest = { editing = null },
            confirmButton = {
                TextButton(onClick = {
                    val m = tp.hour * 60 + tp.minute
                    if (h.anticipated) state.setEveTime(h.id, m) else state.setHourTime(h.id, m)
                    editing = null
                }) { LText("Save", 16, Lc.accent) }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { LText("Cancel", 16, Lc.muted) } },
            title = { LText("Time for ${h.hour.latin}" + if (h.anticipated) " (the evening before)" else "", 22) },
            text = { TimePicker(state = tp) },
            containerColor = Lc.surface,
        )
    }
}

/**
 * When Matins and Lauds are said: in the morning, or by the old custom the evening before.
 * [onBellsChanged] is told when the Little Office bell moved with Matins.
 */
@Composable
private fun AnticipationCard(state: AppState, onBellsChanged: (Boolean) -> Unit) {
    val choices = listOf("eve" to "The evening before", "morning" to "In the morning")
    LCard(padding = 18.dp) {
        SectionLabel("Anticipation")
        LText("Matutinum", 17)
        Spacer(Modifier.height(8.dp))
        Segmented(choices, if (state.anticipateMatins) "eve" else "morning") {
            onBellsChanged(state.updateAnticipation(it == "eve", state.anticipateLauds))
        }
        if (state.anticipateMatins) {
            Spacer(Modifier.height(14.dp))
            LText("Laudes", 17)
            Spacer(Modifier.height(8.dp))
            Segmented(choices, if (state.anticipateLauds) "eve" else "morning") {
                state.updateAnticipation(true, it == "eve")
            }
        }
        Spacer(Modifier.height(12.dp))
        LText(
            "By ancient custom, Matins and Lauds may be said from two o’clock on the eve. " +
                "Said tonight, they are the hours of tomorrow and count for tomorrow.",
            14, Lc.muted, lineHeight = 1.4f,
        )
    }
}

/** Rubrical directions ("is said silently", "Commemoratio de Sanctis" …), shown small and muted. */
private val rubricLine = Regex("^(secreto|silently|Antiphona finalis.*|Final Antiphon.*|Commemoratio de Sanctis|Commemoration of the Saints|" +
    ".*(dicitur|dicuntur|incipiuntur|separatur|is said|are said|Otherwise, the hour|Our Father is said).*)$")
private val verseNum = Regex("^(\\d+:\\d+[a-z]?)\\s+(.*)$")
private val titleLine = Regex("^(Psalmus|Psalm|Canticum|Canticle|Hymnus|Hymn|Lectio|Lesson|Capitulum|Chapter|Absolutio|Absolution|Benedictio|Blessing|Responsorium|Responsory|Invitatorium|Invitatory)\\b.*|^[1-4]?\\s?[A-Z][a-z]{1,4} \\d+:\\d+.*")

/** One line of the Office: verse numbers small, ℣ ℟ and Ant. in the accent colour, titles muted. */
@Composable
private fun OfficeLine(line: String, latin: Boolean, dim: Boolean) {
    val size = if (latin) 18 else 17
    val ink = if (dim) Lc.muted else Lc.ink
    val m = verseNum.find(line)
    when {
        line == "_" -> Spacer(Modifier.height(6.dp))
        line.length < 110 && rubricLine.matches(line) && !line.startsWith("℣") && !line.startsWith("(") && !Regex("^\\d+:\\d+").containsMatchIn(line) ->
            LText(line, 14, Lc.accent, Modifier.padding(top = 4.dp, bottom = 2.dp), lineHeight = 1.35f)
        m != null -> Row(Modifier.padding(vertical = 2.dp)) {
            LText(m.groupValues[1], 11, Lc.muted, Modifier.width(44.dp).padding(top = 4.dp))
            LText(m.groupValues[2], size, ink, Modifier.weight(1f), lineHeight = 1.45f)
        }
        titleLine.matches(line) -> LText(line, 14, Lc.muted, Modifier.padding(top = 6.dp, bottom = 2.dp), spacing = 0.04f)
        line.startsWith("℣.") || line.startsWith("℟.") || line.startsWith("Ant.") || line.startsWith("V.") || line.startsWith("R.") -> {
            val cut = line.indexOf(' ').let { if (it < 0) line.length else it }
            Row(Modifier.padding(vertical = 2.dp)) {
                LText(line.substring(0, cut), size, Lc.accent)
                LText(line.substring(cut), size, ink, Modifier.weight(1f), lineHeight = 1.45f)
            }
        }
        else -> LText(line, size, ink, Modifier.padding(vertical = 2.dp), lineHeight = 1.45f)
    }
}
