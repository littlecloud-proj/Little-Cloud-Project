package com.littlecloud.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.littlecloud.app.resources.*
import com.littlecloud.app.data.Abstinence
import com.littlecloud.app.data.AppState
import com.littlecloud.app.data.LColor
import com.littlecloud.app.time.*

private val monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy")
private val dayFmt = DateTimeFormatter.ofPattern("EEEE, d MMMM")

@Composable
fun CalendarScreen(state: AppState, scroll: androidx.compose.foundation.ScrollState = rememberScrollState()) {
    var month by remember { mutableStateOf(YearMonth.from(state.today)) }
    var selected by remember { mutableStateOf(state.today) }
    val sel = state.dayInfo(selected)

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 6.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Heading(month.atDay(1).format(monthFmt), 30)
                val devotion = com.littlecloud.app.data.Calendar1962.monthDevotion(month.monthValue)
                LText(devotion, 15, Lc.muted)
            }
            IconBtn(Res.drawable.ic_chevron_left, "Previous month") { month = month.minusMonths(1) }
            IconBtn(Res.drawable.ic_chevron_right, "Next month") { month = month.plusMonths(1) }
        }

        Column(
            Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 18.dp).padding(top = 10.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Weekday header
            Row(Modifier.fillMaxWidth()) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                    LText(it, 13, Lc.muted, Modifier.weight(1f), align = TextAlign.Center, spacing = 0.08f)
                }
            }
            // Month grid
            val first = month.atDay(1)
            val lead = first.dayOfWeek.value % 7 // Sunday = 0
            val cells = lead + month.lengthOfMonth()
            val rows = (cells + 6) / 7
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                for (r in 0 until rows) {
                    Row(Modifier.fillMaxWidth()) {
                        for (c in 0 until 7) {
                            val n = r * 7 + c - lead + 1
                            if (n < 1 || n > month.lengthOfMonth()) {
                                Spacer(Modifier.weight(1f).height(54.dp))
                            } else {
                                val date = month.atDay(n)
                                DayCell(Modifier.weight(1f), state, date, date == selected, date == state.today) { selected = date }
                            }
                        }
                    }
                }
            }

            // Legend
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf(LColor.WHITE, LColor.RED, LColor.GREEN, LColor.VIOLET).forEach { col ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LiturgicalDot(col, 8.dp); Spacer(Modifier.width(5.dp)); LText(col.label, 13, Lc.muted)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Ic(Res.drawable.ic_fish, 14.dp, Lc.muted); Spacer(Modifier.width(5.dp)); LText("Abstinence", 13, Lc.muted)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Ic(Res.drawable.ic_bowl, 14.dp, Lc.muted); Spacer(Modifier.width(5.dp)); LText("Fast", 13, Lc.muted)
                }
            }

            // Selected day
            LCard(padding = 18.dp) {
                Eyebrow(selected.format(dayFmt))
                Spacer(Modifier.height(8.dp))
                Heading(sel.title, 27)
                Spacer(Modifier.height(4.dp))
                LText("${sel.rankLabel} · ${sel.color.label} · ${sel.season}", 16, Lc.muted)
                sel.commemorations.forEach { Spacer(Modifier.height(8.dp)); LText("Commemoration: $it", 16) }
                sel.carmelite?.let {
                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Lc.soft).padding(horizontal = 14.dp, vertical = 12.dp)) {
                        LText("Carmelite proper · $it", 16)
                    }
                }
                Spacer(Modifier.height(12.dp)); HLine(); Spacer(Modifier.height(12.dp))
                Eyebrow("Collect")
                Spacer(Modifier.height(6.dp))
                CollectText(state, selected, 16)
                Spacer(Modifier.height(12.dp)); HLine(); Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Ic(if (sel.fast.fast) Res.drawable.ic_bowl else Res.drawable.ic_fish, 20.dp, Lc.accent, Modifier.padding(top = 2.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        LText(sel.fast.headline + if (sel.fast.reason.isNotEmpty()) " · ${sel.fast.reason}" else "", 16)
                        Spacer(Modifier.height(2.dp))
                        if (sel.fast.note.isNotEmpty()) LText(sel.fast.note, 14, Lc.muted)
                    }
                }
            }

            LCard(padding = 0.dp) {
                RowItem(Modifier.padding(start = 16.dp, end = 8.dp)) {
                    Column(Modifier.weight(1f).padding(vertical = 10.dp)) {
                        LText("Carmelite proper", 17)
                        LText("Show feasts of the Order alongside the 1962 calendar", 14, Lc.muted)
                    }
                    LSwitch(state.carmelite) { state.updateCarmelite(it) }
                }
            }
            LText(
                "Fasting follows: ${state.discipline.label}. Change this in Settings. Your confessor or ordinary may dispense.",
                14, Lc.muted,
            )
        }
    }
}

@Composable
private fun DayCell(modifier: Modifier, state: AppState, date: LocalDate, selected: Boolean, today: Boolean, onClick: () -> Unit) {
    val info = state.dayInfo(date)
    val bg = when { selected -> Lc.accent; today -> Lc.soft; else -> Color.Transparent }
    val fg = if (selected) Lc.on else Lc.ink
    val desc = buildString {
        append("${date.dayOfMonth} ${date.month.name.lowercase()}, ${info.title}")
        if (info.fast.any) append(", ${info.fast.headline}")
    }
    Column(
        modifier
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .selectable(selected = selected, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = desc },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LText(date.dayOfMonth.toString(), 18, fg)
        Spacer(Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            val dot = Modifier.size(7.dp).clip(RoundedCornerShape(4.dp)).background(Color(info.color.argb))
            Box(if (info.color == LColor.WHITE) dot.border(1.dp, if (selected) Lc.on else Lc.muted, RoundedCornerShape(4.dp)) else dot)
            if (info.carmelite != null) {
                Box(Modifier.size(5.dp).clip(RoundedCornerShape(3.dp)).background(if (selected) Lc.on else Lc.accent))
            }
            when {
                info.fast.fast -> Ic(Res.drawable.ic_bowl, 11.dp, fg)
                info.fast.abstinence != Abstinence.NONE -> Ic(Res.drawable.ic_fish, 12.dp, fg)
            }
        }
    }
}
