package com.littlecloud.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.littlecloud.app.resources.*
import com.littlecloud.app.data.AppState
import com.littlecloud.app.data.Discipline
import com.littlecloud.app.data.PrayerBook
import com.littlecloud.app.data.rubricsLabel
import com.littlecloud.app.data.PrayerItem
import com.littlecloud.app.data.SpanishPrayers

/** "Latin / English / Both" or "Latín / Español / Ambos", following the vernacular setting. */
/**
 * What to show of a text that exists in Latin and English only (the collect, the readings,
 * the Office). In Spanish the vernacular choice shows the English, clearly marked, rather
 * than repeating the Latin, until a public-domain Spanish text is on file.
 */
data class LaEn(val latin: Boolean, val english: Boolean, val englishForSpanish: Boolean)

fun laEn(state: AppState) = LaEn(
    latin = state.officeLang != "english",
    english = state.officeLang != "latin",
    englishForSpanish = state.vernacular == "es" && state.officeLang != "latin",
)

/** The note shown where English stands in for Spanish. */
const val NO_SPANISH_NOTE = "Aún no hay traducción española de dominio público de este texto; se muestra la inglesa."

fun langOptions(state: AppState): List<Pair<String, String>> =
    if (state.vernacular == "es") listOf("latin" to "Latín", "english" to "Español", "both" to "Ambos")
    else listOf("latin" to "Latin", "english" to "English", "both" to "Both")

/**
 * Reads one prayer, or swipes through a rule of prayer (morning or evening) page by page.
 * When [keyPrefix] is given, each prayer can be marked as said from within the reader.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(state: AppState, itemsArg: List<PrayerItem>, start: Int, keyPrefix: String?, onBack: () -> Unit) {
    // A morning or evening rule is read live, so edits to the user's own prayers show at once.
    val items = when (keyPrefix) {
        "m|" -> state.morning
        "e|" -> state.evening
        else -> itemsArg
    }
    if (items.isEmpty()) { androidx.compose.runtime.LaunchedEffect(Unit) { onBack() }; return }
    val pager = rememberPagerState(initialPage = start.coerceIn(0, items.size - 1)) { items.size }
    val scope = rememberCoroutineScope()
    val current = items[pager.currentPage.coerceIn(0, items.size - 1)]
    var editing by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<PrayerItem?>(null) }
    editing?.let { item ->
        PrayerEditor(state, item, evening = keyPrefix == "e|") { editing = null }
        return
    }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 6.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBtn(Res.drawable.ic_back, "Back", Lc.ink, onClick = onBack)
            LText(
                (state.prayer(current.prayerId)?.subtitle ?: current.note.ifEmpty { "Your prayer" }) +
                    if (state.isEdited(current.prayerId)) " · edited" else "",
                15, Lc.muted, Modifier.weight(1f), maxLines = 1,
            )
            if (items.size > 1) LText("${pager.currentPage + 1} of ${items.size}", 14, Lc.muted)
            val canEdit = current.prayerId != null && PrayerBook.get(current.prayerId) != null ||
                (current.prayerId == null && keyPrefix in listOf("m|", "e|"))
            if (canEdit) IconBtn(Res.drawable.ic_edit, "Edit this prayer", Lc.muted, size = 20.dp) { editing = current }
        }
        HorizontalPager(state = pager, modifier = Modifier.weight(1f), beyondViewportPageCount = 1) { page ->
            PrayerPage(state, items[page])
        }
        if (keyPrefix != null) {
            val key = keyPrefix + current.title
            val said = state.isDone(key)
            val last = pager.currentPage >= items.size - 1
            Column(Modifier.fillMaxWidth().background(Lc.bg)) {
                HLine()
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (items.size > 1) {
                        IconBtn(Res.drawable.ic_chevron_left, "Previous prayer", if (pager.currentPage > 0) Lc.ink else Lc.line) {
                            if (pager.currentPage > 0) scope.launch { pager.animateScrollToPage(pager.currentPage - 1) }
                        }
                    }
                    PrimaryButton(
                        when {
                            said -> "Said · Deo gratias"
                            items.size > 1 && !last -> "Mark as said · next"
                            else -> "Mark as said"
                        },
                        Modifier.weight(1f).padding(horizontal = 6.dp),
                        icon = if (said) Res.drawable.ic_check else null,
                    ) {
                        state.toggleDone(key)
                        if (!said && !last) scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                    }
                    if (items.size > 1) {
                        IconBtn(Res.drawable.ic_chevron_right, "Next prayer", if (!last) Lc.ink else Lc.line) {
                            if (!last) scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerPage(state: AppState, item: PrayerItem) {
    val p = state.prayer(item.prayerId)
    val lang = state.officeLang
    val es = state.vernacular == "es"
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp).padding(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Heading(p?.title ?: item.title, 32)
        if (p != null) {
            // The vernacular text: Spanish only where an approved text is on file.
            val spanish = if (es) state.spanish(p.id) else null
            val vern = if (es) spanish else p.english
            if (p.latin != null) {
                Segmented(langOptions(state), lang) { state.updateOfficeLang(it) }
            }
            val showLatin = p.latin != null && (lang != "english" || vern == null)
            val showVern = vern != null && (lang != "latin" || p.latin == null)
            if (showLatin) PrayerText(p.latin!!, 19, Lc.ink)
            if (showVern) PrayerText(vern!!, 18, if (showLatin) Lc.muted else Lc.ink)
            if (es && spanish == null) {
                if (p.latin == null) PrayerText(p.english, 18, Lc.ink)
                LText(
                    if (p.latin != null) "Aún no hay una traducción española aprobada de esta oración; se muestra el latín."
                    else "Aún no hay una traducción española aprobada de esta oración; se muestra el inglés.",
                    14, Lc.muted,
                )
            }
        } else if (item.text != null) {
            PrayerText(item.text, 18, Lc.ink)
        } else {
            LText("No words are written for this prayer. Say it from memory or your own book, then mark it as said.", 17, Lc.muted, lineHeight = 1.5f)
        }
    }
}

/** Prayer text with ℣ ℟ marks in the accent colour. */
@Composable
private fun PrayerText(text: String, size: Int, ink: androidx.compose.ui.graphics.Color) {
    val accent = Lc.accent
    val styled = androidx.compose.ui.text.buildAnnotatedString {
        text.split('\n').forEachIndexed { i, line ->
            if (i > 0) append('\n')
            val mark = listOf("℣.", "℟.").firstOrNull { line.startsWith(it) }
            if (mark != null) {
                pushStyle(androidx.compose.ui.text.SpanStyle(color = accent)); append(mark); pop()
                append(line.removePrefix(mark))
            } else append(line)
        }
    }
    androidx.compose.material3.Text(
        styled,
        style = androidx.compose.ui.text.TextStyle(
            fontFamily = LcFont, fontSize = size.sp, lineHeight = (size * 1.55f).sp, color = ink,
        ),
    )
}

/** Lets the user change the words of a prayer. Prayer-book texts can always be restored. */
@Composable
private fun PrayerEditor(state: AppState, item: PrayerItem, evening: Boolean, onDone: () -> Unit) {
    val id = item.prayerId
    val base = PrayerBook.get(id)
    val p = state.prayer(id)
    var title by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(p?.title ?: item.title) }
    var latin by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(p?.latin ?: "") }
    var english by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(p?.english ?: item.text ?: "") }
    var spanish by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(state.spanish(id) ?: "") }
    var confirmRestore by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    com.littlecloud.app.platform.PlatformBackHandler(enabled = true, onBack = onDone)
    val colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Lc.accent, unfocusedBorderColor = Lc.line,
        focusedContainerColor = Lc.surface, unfocusedContainerColor = Lc.surface,
        cursorColor = Lc.accent, focusedLabelColor = Lc.accent, unfocusedLabelColor = Lc.muted,
        focusedTextColor = Lc.ink, unfocusedTextColor = Lc.ink,
    )
    val style = androidx.compose.ui.text.TextStyle(fontFamily = LcFont, fontSize = 17.sp, lineHeight = 26.sp)
    @Composable
    fun Field(label: String, value: String, single: Boolean = false, onChange: (String) -> Unit) {
        androidx.compose.material3.OutlinedTextField(
            value = value, onValueChange = onChange,
            label = { LText(label, 15) },
            singleLine = single, minLines = if (single) 1 else 4, textStyle = style, colors = colors,
            shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(),
        )
    }
    Column(Modifier.fillMaxSize().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBtn(Res.drawable.ic_close, "Cancel", Lc.ink, onClick = onDone)
            LText("Edit prayer", 20, modifier = Modifier.weight(1f))
            TextLink("Save") {
                if (id != null && base != null) state.editPrayer(id, title, latin, english, spanish)
                else state.editOwnPrayer(evening, item, title, english)
                onDone()
            }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Field("Title", title, single = true) { title = it }
            if (base != null) {
                Field("Latin", latin) { latin = it }
                Field("English", english) { english = it }
                Field("Español", spanish) { spanish = it }
                LText(
                    "Your changes are kept on this phone only. Lines beginning with ℣. or ℟. are marked in colour.",
                    14, Lc.muted,
                )
                if (state.isEdited(id)) {
                    OutlineButton("Restore the original text", Modifier.fillMaxWidth()) { confirmRestore = true }
                }
            } else {
                Field("Words of the prayer", english) { english = it }
            }
        }
    }
    if (confirmRestore && id != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmRestore = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { state.restorePrayer(id); confirmRestore = false; onDone() }) {
                    LText("Restore", 16, Lc.accent)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { confirmRestore = false }) { LText("Keep my text", 16, Lc.muted) }
            },
            title = { LText("Restore the original?", 22) },
            text = { LText("Your changes to this prayer will be removed.", 16, Lc.muted) },
            containerColor = Lc.surface,
        )
    }
}

@Composable
fun SettingsScreen(state: AppState, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBtn(Res.drawable.ic_back, "Back", Lc.ink, onClick = onBack)
            LText("Settings", 20)
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column {
                SectionLabel("Appearance")
                Segmented(listOf("system" to "System", "light" to "Light", "dark" to "Dark"), state.theme) { state.updateTheme(it) }
            }
            Column {
                SectionLabel("Language of prayers and the Office")
                if (com.littlecloud.app.data.SPANISH_ENABLED) {
                    Segmented(listOf("en" to "English", "es" to "Español"), state.vernacular) { state.updateVernacular(it) }
                    Spacer(Modifier.height(10.dp))
                }
                Segmented(langOptions(state), state.officeLang) { state.updateOfficeLang(it) }
                Spacer(Modifier.height(8.dp))
                LText(
                    if (state.vernacular == "es") "Solo se usan traducciones españolas aprobadas. Donde aún no la hay, se muestra el latín."
                    else "English: Douay-Rheims psalms, Britt’s hymns, and other texts from the Bute translation (1908).",
                    14, Lc.muted,
                )
            }
            Column {
                SectionLabel("Rubrics")
                Segmented(listOf("1955" to "1955", "1954" to "Before 1955", "1962" to "1962"), state.rubrics) { state.updateRubrics(it) }
                Spacer(Modifier.height(8.dp))
                LText(
                    when (state.rubrics) {
                        "1955" -> "The rubrics of Pope Pius XII: the simplification of 1955 and the restored Holy Week of 1956, as kept by the CMRI. The calendar, collects, readings and Little Office follow them."
                        "1954" -> "The rubrics before the reforms of 1955 (Divino Afflatu). The calendar, collects, readings and Little Office follow them."
                        else -> "The rubrics of 1960, as in the 1962 books. The calendar, collects, readings and Little Office follow them."
                    },
                    14, Lc.muted,
                )
            }
            Column {
                SectionLabel("Fast and abstinence")
                LCard {
                    Discipline.entries.forEachIndexed { i, d ->
                        if (i > 0) HLine()
                        val on = state.discipline == d
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(selected = on, role = Role.RadioButton) { state.updateDiscipline(d) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier.size(22.dp).clip(CircleShape).border(2.dp, if (on) Lc.accent else Lc.muted, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) { if (on) Box(Modifier.size(10.dp).clip(CircleShape).background(Lc.accent)) }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                LText(d.label, 17)
                                LText(d.detail, 14, Lc.muted)
                            }
                        }
                    }
                }
            }
            LCard {
                RowItem(Modifier.padding(start = 16.dp, end = 8.dp)) {
                    Column(Modifier.weight(1f).padding(vertical = 10.dp)) {
                        LText("Carmelite proper", 17)
                        LText(
                            if (state.rubrics != "1962") "Feasts of the Discalced Carmelites on Today and the Calendar. Taken from the 1962 Carmelite calendar; not yet checked against an older Carmelite Ordo."
                            else "Feasts of the Order on Today and the Calendar",
                            14, Lc.muted,
                        )
                    }
                    LSwitch(state.carmelite) { state.updateCarmelite(it) }
                }
            }
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Lc.soft).padding(16.dp)) {
                LText(
                    "Little Cloud follows the traditional Roman rite, by the rubrics chosen above. " +
                        "The calendar is computed in the app and should be checked against a printed Ordo. " +
                        "Fasting and abstinence are shown as the law stood; your confessor or ordinary may dispense.",
                    15, Lc.muted, lineHeight = 1.45f,
                )
            }
            AboutSection(state)
        }
    }
}

/** The address that receives feedback from the app. */
const val FEEDBACK_EMAIL = "LittleCloudProject@proton.me"

/** About and feedback, at the foot of Settings. Feedback opens the phone's own mail app. */
@Composable
private fun AboutSection(state: AppState) {
    val platform = com.littlecloud.app.platform.LocalPlatform.current
    val uri = androidx.compose.ui.platform.LocalUriHandler.current
    Column {
        SectionLabel("About")
        LCard {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                LText("Little Cloud ${platform.appVersion}", 17)
                LText("Ecce nubecula parva quasi vestigium hominis. A companion for the Little Office and the rule of prayer, made for traditional Catholics and Carmelite tertiaries.", 14, Lc.muted, lineHeight = 1.4f)
            }
            HLine()
            RowItem(Modifier.padding(start = 16.dp, end = 12.dp), onClick = {
                val body = "\n\n\n— \nLittle Cloud ${platform.appVersion} · ${platform.deviceInfo} · rubrics ${rubricsLabel(state.rubrics)}"
                runCatching { uri.openUri("mailto:$FEEDBACK_EMAIL?subject=${encode("Little Cloud feedback")}&body=${encode(body)}") }
            }) {
                Column(Modifier.weight(1f).padding(vertical = 12.dp)) {
                    LText("Send feedback", 17)
                    LText("Corrections, suggestions or a problem: opens your mail app", 14, Lc.muted)
                }
                Ic(Res.drawable.ic_chevron_right, 18.dp, Lc.muted)
            }
        }
    }
}

/** Percent-encoding for a mailto: link. */
private fun encode(s: String): String = buildString {
    for (b in s.encodeToByteArray()) {
        val c = (b.toInt() and 0xFF)
        val ch = c.toChar()
        if (ch.isLetterOrDigit() && c < 128 || ch in "-._~") append(ch)
        else append('%').append(c.toString(16).uppercase().padStart(2, '0'))
    }
}

