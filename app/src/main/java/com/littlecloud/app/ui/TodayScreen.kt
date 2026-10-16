package com.littlecloud.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.littlecloud.app.R
import com.littlecloud.app.data.AppState
import com.littlecloud.app.data.Calendar1962
import com.littlecloud.app.data.PrayerItem
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val longDate = DateTimeFormatter.ofPattern("EEEE · d MMMM yyyy")
private val shortDate = DateTimeFormatter.ofPattern("EEEE d MMMM")

@Composable
fun TodayScreen(state: AppState, nav: Nav, scroll: androidx.compose.foundation.ScrollState = rememberScrollState()) {
    val today = state.today
    val day = state.dayInfo(today)
    val tomorrow = state.dayInfo(today.plusDays(1))
    val nowMin = LocalTime.now().let { it.hour * 60 + it.minute }

    Column(Modifier.fillMaxSize()) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 6.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Ic(R.drawable.ic_cloud, 30.dp, Lc.accent)
            Spacer(Modifier.width(10.dp))
            LText("Little Cloud", 25, modifier = Modifier.weight(1f), spacing = 0.03f)
            val isDark = Lc.dark
            IconBtn(
                if (isDark) R.drawable.ic_sun else R.drawable.ic_moon,
                if (isDark) "Switch to light mode" else "Switch to dark mode",
            ) { state.updateTheme(if (isDark) "light" else "dark") }
            IconBtn(R.drawable.ic_settings, "Settings") { nav.openSettings() }
        }

        Column(
            Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 18.dp).padding(top = 6.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(Modifier.padding(horizontal = 2.dp)) {
                Eyebrow(today.format(longDate))
                Spacer(Modifier.height(4.dp))
                LText("${day.season} · ${Calendar1962.monthDevotion(today.monthValue)}", 15, Lc.muted)
            }

            // Feast of the day
            LCard(padding = 20.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiturgicalDot(day.color)
                    Spacer(Modifier.width(8.dp))
                    Eyebrow("Feast of the day")
                }
                Spacer(Modifier.height(10.dp))
                Heading(day.title, 32)
                Spacer(Modifier.height(4.dp))
                LText("${day.rankLabel} · ${day.color.label}", 16, Lc.muted)
                day.commemorations.forEach {
                    Spacer(Modifier.height(6.dp)); LText("Commemoration: $it", 15)
                }
                day.carmelite?.let {
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Lc.soft).padding(12.dp)) {
                        LText("Carmelite proper · $it", 15)
                    }
                }
                Spacer(Modifier.height(14.dp)); HLine(); Spacer(Modifier.height(12.dp))
                Eyebrow("Collect")
                Spacer(Modifier.height(6.dp))
                CollectText(state, today)
                Spacer(Modifier.height(8.dp))
                LText("Tomorrow · ${tomorrow.title}", 14, Lc.muted)
            }

            // Fasting
            FastRow(state, nav)

            // Office and Litany of Loreto tiles
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val hours = state.hours
                val next = hours.firstOrNull { !state.isDone("o|${it.id}") && it.minutes >= nowMin - 60 }
                    ?: hours.firstOrNull { !state.isDone("o|${it.id}") }
                val said = hours.count { state.isDone("o|${it.id}") }
                Tile(
                    Modifier.weight(1f), R.drawable.ic_book, "Little Office",
                    if (next != null) "Next: ${next.latin}\n${fmtTime(next.minutes)}" else "All hours said\nDeo gratias",
                    onClick = { nav.goTab(Tab.HOURS) },
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        hours.forEachIndexed { i, _ ->
                            Box(
                                Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp))
                                    .background(if (i < said) Lc.accent else Lc.line)
                            )
                        }
                    }
                }
                val loretoKey = "x|Litany of Loreto"
                val loretoSaid = state.isDone(loretoKey)
                Tile(
                    Modifier.weight(1f), R.drawable.ic_lily, "Litany of Loreto",
                    "Our Lady’s litany\nDaily for Tertiaries",
                    { nav.openPrayer(PrayerItem("Litany of Loreto", prayerId = "litany_loreto"), "x|") },
                ) {
                    LText(if (loretoSaid) "Said · Deo gratias" else "Pray the litany", 14, Lc.accent)
                }
            }

            // Mental prayer
            MentalPrayerRow(state) { nav.openIntimacy() }

            // Next bells
            NextBells(state, nowMin) { nav.goTab(Tab.BELLS) }

            PrayerList(state, nav, evening = false)
            PrayerList(state, nav, evening = true)

            LText(
                "Ecce nubecula parva quasi vestigium hominis",
                15, Lc.muted, Modifier.fillMaxWidth().padding(top = 6.dp), align = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FastRow(state: AppState, nav: Nav) {
    val f = state.dayInfo(state.today).fast
    val next = if (f.any) null else Calendar1962.nextPenitentialDay(state.today, state.discipline, state.pre1955)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (f.any) Lc.soft else Lc.surface)
            .border(1.dp, Lc.line, RoundedCornerShape(18.dp))
            .clickable(role = Role.Button) { nav.goTab(Tab.CALENDAR) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIcon(if (f.fast) R.drawable.ic_bowl else R.drawable.ic_fish)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            LText(if (f.any) "${f.headline} today" else "No fast or abstinence today", 17)
            val sub = when {
                f.any -> f.reason
                next != null -> "Next: ${next.second.headline.lowercase()} · ${next.first.format(shortDate)}"
                else -> ""
            }
            if (sub.isNotEmpty()) LText(sub, 14, Lc.muted)
        }
        Ic(R.drawable.ic_chevron_right, 18.dp, Lc.muted)
    }
}

@Composable
private fun Tile(
    modifier: Modifier,
    icon: Int,
    title: String,
    sub: String,
    onClick: () -> Unit,
    footer: @Composable () -> Unit,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Lc.surface)
            .border(1.dp, Lc.line, RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 160.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CircleIcon(icon)
        LText(title, 19, lineHeight = 1.15f)
        LText(sub, 15, Lc.muted)
        Spacer(Modifier.weight(1f, fill = false))
        footer()
    }
}

@Composable
private fun MentalPrayerRow(state: AppState, onClick: () -> Unit) {
    val made = state.isDone("di|${state.today}")
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Lc.surface)
            .border(1.dp, Lc.line, RoundedCornerShape(18.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIcon(R.drawable.ic_candle)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            LText("Mental prayer", 17)
            LText(if (made) "Made today · Deo gratias" else "Timer and the Carmelite method", 14, Lc.muted)
        }
        Ic(R.drawable.ic_chevron_right, 18.dp, Lc.muted)
    }
}

@Composable
private fun NextBells(state: AppState, nowMin: Int, onClick: () -> Unit) {
    val upcoming = state.bells.filter { it.enabled }
        .flatMap { b -> b.times.map { t -> b.name to t } }
        .sortedBy { (_, t) -> if (t >= nowMin) t else t + 24 * 60 }
        .take(2)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Lc.soft)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Ic(R.drawable.ic_bell, 22.dp, Lc.accent)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            LText("Next bells", 16)
            LText(
                if (upcoming.isEmpty()) "No bells set" else upcoming.joinToString(" · ") { (n, t) -> "$n ${fmtTime(t)}" },
                15, Lc.muted,
            )
        }
        Ic(R.drawable.ic_chevron_right, 18.dp, Lc.muted)
    }
}

@Composable
fun PrayerList(state: AppState, nav: Nav, evening: Boolean) {
    val list = if (evening) state.evening else state.morning
    val prefix = if (evening) "e|" else "m|"
    val said = list.count { state.isDone(prefix + it.title) }
    LCard {
        Row(
            Modifier.fillMaxWidth().padding(start = 18.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Heading(if (evening) "Evening Prayer" else "Morning Prayer", 22)
                LText("$said of ${list.size} said", 14, Lc.muted)
            }
            TextLink("Arrange") { nav.arrange(evening) }
        }
        list.forEachIndexed { index, item ->
            val key = prefix + item.title
            val d = state.isDone(key)
            HLine()
            RowItem(Modifier.padding(start = 6.dp, end = 16.dp), onClick = { nav.openRule(evening, index) }) {
                CheckDot(d, (if (d) "Unmark " else "Mark as said: ") + item.title) { state.toggleDone(key) }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                    LText(item.title, 17, if (d) Lc.muted else Lc.ink)
                    if (item.note.isNotEmpty()) LText(item.note, 14, Lc.muted)
                }
                Ic(R.drawable.ic_chevron_right, 16.dp, Lc.muted)
            }
        }
    }
}

interface Nav {
    fun goTab(tab: Tab)
    fun openPrayer(item: PrayerItem, keyPrefix: String? = null)
    fun openRule(evening: Boolean, index: Int)
    fun openIntimacy()
    fun openSettings()
    fun arrange(evening: Boolean)
}
