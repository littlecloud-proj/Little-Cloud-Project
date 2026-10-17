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
import com.littlecloud.app.resources.*
import org.jetbrains.compose.resources.DrawableResource
import com.littlecloud.app.data.AppState
import com.littlecloud.app.data.Calendar1962
import com.littlecloud.app.data.PrayerItem
import com.littlecloud.app.time.*

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
            Ic(Res.drawable.ic_cloud, 30.dp, Lc.accent)
            Spacer(Modifier.width(10.dp))
            LText("Little Cloud", 25, modifier = Modifier.weight(1f), spacing = 0.03f)
            val isDark = Lc.dark
            IconBtn(
                if (isDark) Res.drawable.ic_sun else Res.drawable.ic_moon,
                if (isDark) "Switch to light mode" else "Switch to dark mode",
            ) { state.updateTheme(if (isDark) "light" else "dark") }
            IconBtn(Res.drawable.ic_settings, "Settings") { nav.openSettings() }
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

            // Office and Litany of Loreto tiles: the same height, the same parts in the same places.
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val slots = state.slots(today)
                val next = slots.firstOrNull { !state.isSaid(it) && it.minutes >= nowMin - 60 }
                    ?: slots.firstOrNull { !state.isSaid(it) }
                val said = slots.count { state.isSaid(it) }
                Tile(
                    Modifier.weight(1f), Res.drawable.ic_book, "Little Office",
                    if (next != null) "Next: ${next.hour.latin}\n${fmtTime(next.minutes)}" + (if (next.anticipated) " · of tomorrow" else "")
                    else "All hours said\nDeo gratias",
                    status = "$said of ${slots.size} hours",
                    segments = slots.size, filled = said,
                    onClick = { nav.goTab(Tab.HOURS) },
                )
                val loretoSaid = state.isDone("x|Litany of Loreto")
                Tile(
                    Modifier.weight(1f), Res.drawable.ic_lily, "Litany of Loreto",
                    "Our Lady’s litany\nDaily for Tertiaries",
                    status = if (loretoSaid) "Said · Deo gratias" else "Not yet said",
                    segments = 1, filled = if (loretoSaid) 1 else 0,
                    onClick = { nav.openPrayer(PrayerItem("Litany of Loreto", prayerId = "litany_loreto"), "x|") },
                )
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
    val sub = when {
        f.any -> f.reason
        next != null -> "Next: ${next.second.headline.lowercase()} · ${next.first.format(shortDate)}"
        else -> ""
    }
    RowCard(
        if (f.fast) Res.drawable.ic_bowl else Res.drawable.ic_fish,
        if (f.any) "${f.headline} today" else "No fast or abstinence today",
        sub,
        highlighted = f.any,
    ) { nav.goTab(Tab.CALENDAR) }
}

@Composable
private fun Tile(
    modifier: Modifier,
    icon: DrawableResource,
    title: String,
    sub: String,
    status: String,
    segments: Int,
    filled: Int,
    onClick: () -> Unit,
) {
    Column(
        modifier
            .fillMaxHeight()
            .clip(CardShape)
            .background(Lc.surface)
            .border(1.dp, Lc.line, CardShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
    ) {
        CircleIcon(icon)
        Spacer(Modifier.height(12.dp))
        LText(title, 19, lineHeight = 1.15f)
        Spacer(Modifier.height(4.dp))
        LText(sub, 15, Lc.muted)
        Spacer(Modifier.weight(1f).heightIn(min = 14.dp))
        SegmentBar(segments, filled)
        Spacer(Modifier.height(8.dp))
        LText(status, 14, Lc.accent)
    }
}

@Composable
private fun MentalPrayerRow(state: AppState, onClick: () -> Unit) {
    val made = state.isDone("di|${state.today}")
    RowCard(
        Res.drawable.ic_candle, "Mental prayer",
        if (made) "Made today · Deo gratias" else "Timer and the Carmelite method",
        onClick = onClick,
    )
}

@Composable
private fun NextBells(state: AppState, nowMin: Int, onClick: () -> Unit) {
    val upcoming = state.bells.filter { it.enabled }
        .flatMap { b -> b.times.map { t -> b.name to t } }
        .sortedBy { (_, t) -> if (t >= nowMin) t else t + 24 * 60 }
        .take(2)
    RowCard(
        Res.drawable.ic_bell, "Next bells",
        if (upcoming.isEmpty()) "No bells set" else upcoming.joinToString(" · ") { (n, t) -> "$n ${fmtTime(t)}" },
        onClick = onClick,
    )
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
                Ic(Res.drawable.ic_chevron_right, 16.dp, Lc.muted)
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
