package com.littlecloud.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littlecloud.app.resources.*
import com.littlecloud.app.data.AppState
import com.littlecloud.app.data.PrayerBook
import com.littlecloud.app.data.PrayerItem

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrayersScreen(state: AppState, nav: Nav, evening: Boolean, onTab: (Boolean) -> Unit, scroll: androidx.compose.foundation.ScrollState = rememberScrollState()) {
    val list = if (evening) state.evening else state.morning
    fun set(l: List<PrayerItem>) = state.setList(evening, l)
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var deleting by remember { mutableStateOf<com.littlecloud.app.data.Prayer?>(null) }
    var query by remember { mutableStateOf("") }
    fun matches(p: com.littlecloud.app.data.Prayer) =
        query.isBlank() || fold(p.title + " " + p.subtitle).contains(fold(query.trim()))
    deleting?.let { p ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { deleting = null },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { state.removeOwnPrayer(p.id); deleting = null }) {
                    LText("Delete", 16, Lc.accent)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { deleting = null }) { LText("Keep it", 16, Lc.muted) }
            },
            title = { LText("Delete “${p.title}”?", 22) },
            text = { LText("It will be removed from your prayer book and from your morning and evening prayer.", 16, Lc.muted) },
            containerColor = Lc.surface,
        )
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)) {
            Heading("My Prayers", 30)
            LText("Choose, write and order your rule of prayer", 15, Lc.muted)
            Spacer(Modifier.height(14.dp))
            Segmented(listOf("m" to "Morning", "e" to "Evening"), if (evening) "e" else "m") { onTab(it == "e") }
        }
        Column(
            Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 18.dp).padding(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LCard {
                if (list.isEmpty()) {
                    LText("Nothing here yet. Add a prayer below.", 16, Lc.muted, Modifier.padding(18.dp))
                }
                list.forEachIndexed { i, item ->
                    if (i > 0) HLine()
                    RowItem(Modifier.padding(start = 14.dp, end = 2.dp), onClick = { nav.openRule(evening, i) }) {
                        LText("${i + 1}", 15, Lc.muted, Modifier.width(24.dp))
                        Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                            LText(item.title, 17)
                            if (item.note.isNotEmpty()) LText(item.note, 14, Lc.muted)
                        }
                        IconBtn(Res.drawable.ic_up, "Move up: ${item.title}") {
                            if (i > 0) set(list.toMutableList().apply { add(i - 1, removeAt(i)) })
                        }
                        IconBtn(Res.drawable.ic_down, "Move down: ${item.title}") {
                            if (i < list.size - 1) set(list.toMutableList().apply { add(i + 1, removeAt(i)) })
                        }
                        IconBtn(Res.drawable.ic_close, "Remove: ${item.title}", size = 18.dp) {
                            set(list.filterIndexed { j, _ -> j != i })
                        }
                    }
                }
            }

            val platform = com.littlecloud.app.platform.LocalPlatform.current
            if (platform.canPrint) Column {
                SectionLabel("Print or save as PDF")
                LText("A clean copy of your rule, in the language chosen in Settings, to keep in your missal or save as a PDF.", 14, Lc.muted)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlineButton("Morning Prayer", Modifier.weight(1f)) { platform.printPage(PrayerBooklet.title(false), PrayerBooklet.html(state, false)) }
                    OutlineButton("Evening Prayer", Modifier.weight(1f)) { platform.printPage(PrayerBooklet.title(true), PrayerBooklet.html(state, true)) }
                }
            }

            Column {
                SectionLabel("Write your own")
                val colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Lc.accent, unfocusedBorderColor = Lc.line,
                    focusedContainerColor = Lc.surface, unfocusedContainerColor = Lc.surface,
                    cursorColor = Lc.accent, focusedLabelColor = Lc.accent, unfocusedLabelColor = Lc.muted,
                    focusedTextColor = Lc.ink, unfocusedTextColor = Lc.ink,
                )
                val style = TextStyle(fontFamily = LcFont, fontSize = 17.sp)
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { LText("Title or intention", 15) },
                    singleLine = true, textStyle = style, colors = colors,
                    shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text, onValueChange = { text = it },
                    label = { LText("Words of the prayer (optional)", 15) },
                    minLines = 3, textStyle = style, colors = colors,
                    shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                PrimaryButton("Add to ${if (evening) "evening" else "morning"} prayer", icon = Res.drawable.ic_plus) {
                    if (title.isNotBlank()) {
                        val p = state.addOwnPrayer(title, text)
                        set(list + PrayerItem(p.title, p.subtitle, p.id))
                        title = ""; text = ""
                    }
                }
                Spacer(Modifier.height(6.dp))
                LText("It is also kept in your prayer book below.", 14, Lc.muted)
            }

            Column {
                SectionLabel("Prayer book")
                LText("Tap a prayer to read or edit it; tap + to add it to your ${if (evening) "evening" else "morning"} prayer.", 14, Lc.muted)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    placeholder = { LText("Search the prayer book", 16, Lc.muted) },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = LcFont, fontSize = 17.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Lc.accent, unfocusedBorderColor = Lc.line,
                        focusedContainerColor = Lc.surface, unfocusedContainerColor = Lc.surface,
                        cursorColor = Lc.accent, focusedTextColor = Lc.ink, unfocusedTextColor = Lc.ink,
                    ),
                    trailingIcon = if (query.isNotEmpty()) {
                        { IconBtn(Res.drawable.ic_close, "Clear search", Lc.muted, size = 18.dp) { query = "" } }
                    } else null,
                    shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                val own = state.ownPrayers.filter { matches(it) }
                val book = PrayerBook.every.map { state.prayer(it.id) ?: it }.filter { matches(it) }
                if (own.isEmpty() && book.isEmpty()) {
                    LText("No prayer in the book matches “${query.trim()}”.", 15, Lc.muted)
                }
                if (own.isNotEmpty()) {
                    LCard {
                        own.forEachIndexed { i, p ->
                            if (i > 0) HLine()
                            RowItem(Modifier.padding(start = 16.dp, end = 4.dp), onClick = {
                                nav.openPrayer(PrayerItem(p.title, p.subtitle, p.id))
                            }) {
                                Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                                    LText(p.title, 17)
                                    LText(p.subtitle, 14, Lc.muted, maxLines = 1)
                                }
                                IconBtn(Res.drawable.ic_plus, "Add ${p.title} to ${if (evening) "evening" else "morning"} prayer", Lc.accent, size = 18.dp) {
                                    set(list + PrayerItem(p.title, p.subtitle, p.id))
                                }
                                IconBtn(Res.drawable.ic_close, "Delete ${p.title}", size = 18.dp) { deleting = p }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                if (book.isNotEmpty()) LCard {
                    book.forEachIndexed { i, p ->
                        if (i > 0) HLine()
                        RowItem(Modifier.padding(start = 16.dp, end = 4.dp), onClick = {
                            nav.openPrayer(PrayerItem(p.title, p.subtitle, p.id))
                        }) {
                            Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                                LText(p.title, 17)
                                LText(p.subtitle + if (state.isEdited(p.id)) " · edited" else "", 14, Lc.muted, maxLines = 1)
                            }
                            IconBtn(Res.drawable.ic_plus, "Add ${p.title} to ${if (evening) "evening" else "morning"} prayer", Lc.accent, size = 18.dp) {
                                set(list + PrayerItem(p.title, p.subtitle, p.id))
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Lower case, without accents, so "memorare" finds "Memoráre" and "regina caeli" finds "Regína Cæli". */
private fun fold(s: String): String {
    val map = mapOf('á' to "a", 'à' to "a", 'â' to "a", 'é' to "e", 'è' to "e", 'ê' to "e", 'ë' to "e", 'í' to "i", 'ì' to "i",
        'ó' to "o", 'ò' to "o", 'ô' to "o", 'ú' to "u", 'ù' to "u", 'ü' to "u", 'ý' to "y", 'ñ' to "n", 'ç' to "c",
        'æ' to "ae", 'ǽ' to "ae", 'œ' to "oe", 'j' to "i")
    return buildString { s.lowercase().forEach { c -> append(map[c] ?: c.toString()) } }
}
