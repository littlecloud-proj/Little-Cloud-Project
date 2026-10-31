package com.littlecloud.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.launch
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.littlecloud.app.resources.*
import com.littlecloud.app.data.LColor
import com.littlecloud.app.time.*

@Composable
fun LText(
    text: String,
    size: Int = 17,
    color: Color = Lc.ink,
    modifier: Modifier = Modifier,
    lineHeight: Float = 1.3f,
    spacing: Float = 0f,
    upper: Boolean = false,
    align: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        text = if (upper) text.uppercase() else text,
        modifier = modifier,
        color = color,
        textAlign = align,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            fontFamily = LcFont,
            fontSize = size.sp,
            lineHeight = (size * lineHeight).sp,
            letterSpacing = spacing.em,
        ),
    )
}

@Composable
fun Eyebrow(text: String, color: Color = Lc.muted, modifier: Modifier = Modifier) =
    LText(text, 13, color, modifier, spacing = 0.16f, upper = true)

@Composable
fun SectionLabel(text: String) =
    LText(text, 14, Lc.accent, Modifier.padding(bottom = 10.dp).semantics { heading() }, spacing = 0.16f, upper = true)

@Composable
fun Heading(text: String, size: Int = 30, modifier: Modifier = Modifier) =
    LText(text, size, Lc.ink, modifier.semantics { heading() }, lineHeight = 1.12f)

@Composable
fun Ic(res: DrawableResource, size: Dp = 22.dp, tint: Color = Lc.ink, modifier: Modifier = Modifier) =
    Icon(painterResource(res), contentDescription = null, modifier = modifier.size(size), tint = tint)

@Composable
fun IconBtn(res: DrawableResource, label: String, tint: Color = Lc.muted, size: Dp = 22.dp, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
        Icon(painterResource(res), contentDescription = label, modifier = Modifier.size(size), tint = tint)
    }
}

/** One corner radius for every card, tile and row on the screens. */
val CardShape = RoundedCornerShape(22.dp)

@Composable
fun LCard(modifier: Modifier = Modifier, padding: Dp = 0.dp, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(Lc.surface)
            .border(1.dp, Lc.line, CardShape)
            .padding(padding),
        content = content,
    )
}

@Composable
fun HLine() = Box(Modifier.fillMaxWidth().height(1.dp).background(Lc.line))

@Composable
fun CircleIcon(res: DrawableResource, size: Dp = 40.dp) {
    Box(
        Modifier.size(size).clip(CircleShape).background(Lc.soft),
        contentAlignment = Alignment.Center,
    ) { Ic(res, size / 2, Lc.accent) }
}

/** A 44dp round check toggle. */
@Composable
fun CheckDot(checked: Boolean, label: String, onToggle: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() })
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (checked) Lc.accent else Color.Transparent)
                .border(1.5.dp, if (checked) Lc.accent else Lc.muted, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) Ic(Res.drawable.ic_check, 15.dp, Lc.on)
        }
    }
}

@Composable
fun LiturgicalDot(color: LColor, size: Dp = 10.dp) {
    val m = Modifier.size(size).clip(CircleShape).background(Color(color.argb))
    Box(if (color == LColor.WHITE) m.border(1.dp, Lc.muted, CircleShape) else m)
}

@Composable
fun Segmented(options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Lc.soft).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (key, label) ->
            val on = key == selected
            Box(
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (on) Lc.surface else Color.Transparent)
                    .selectable(selected = on, role = Role.Tab, onClick = { onSelect(key) }),
                contentAlignment = Alignment.Center,
            ) { LText(label, 16, if (on) Lc.ink else Lc.muted) }
        }
    }
}

@Composable
fun LSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Lc.on,
            checkedTrackColor = Lc.accent,
            checkedBorderColor = Lc.accent,
            uncheckedThumbColor = Lc.muted,
            uncheckedTrackColor = Color.Transparent,
            uncheckedBorderColor = Lc.muted,
        ),
    )
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, icon: DrawableResource? = null, onClick: () -> Unit) {
    Row(
        modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(Lc.accent)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) { Ic(icon, 20.dp, Lc.on); Spacer(Modifier.width(10.dp)) }
        LText(text, 17, Lc.on)
    }
}

@Composable
fun OutlineButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Lc.line, RoundedCornerShape(24.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) { LText(text, 16) }
}

@Composable
fun TextLink(text: String, onClick: () -> Unit) {
    Box(
        Modifier.heightIn(min = 44.dp).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart,
    ) { LText(text, 15, Lc.accent) }
}

/** A tappable row inside a card. */
@Composable
fun RowItem(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable RowScope.() -> Unit) {
    val m = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    Row(
        m.fillMaxWidth().heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

enum class Tab(val label: String, val icon: DrawableResource) {
    TODAY("Today", Res.drawable.ic_cloud),
    HOURS("Hours", Res.drawable.ic_book),
    CALENDAR("Calendar", Res.drawable.ic_calendar),
    PRAYERS("Prayers", Res.drawable.ic_candle),
    BELLS("Bells", Res.drawable.ic_bell),
}

@Composable
fun BottomNav(current: Tab, onSelect: (Tab) -> Unit) {
    Column(Modifier.fillMaxWidth().background(Lc.surface)) {
        HLine()
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(top = 8.dp, bottom = 6.dp)) {
            Tab.entries.forEach { t ->
                val on = t == current
                Column(
                    Modifier
                        .weight(1f)
                        .selectable(selected = on, role = Role.Tab, onClick = { onSelect(t) })
                        .padding(vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier
                            .size(width = 58.dp, height = 32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (on) Lc.soft else Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) { Ic(t.icon, 22.dp, if (on) Lc.ink else Lc.muted) }
                    Spacer(Modifier.height(4.dp))
                    LText(t.label, 13, if (on) Lc.ink else Lc.muted)
                }
            }
        }
    }
}

private val timeFmt = DateTimeFormatter.ofPattern("h:mm a")
fun fmtTime(minutes: Int): String =
    LocalTime.of(minutes / 60 % 24, minutes % 60).format(timeFmt).lowercase()

/** The collect of a day, in the chosen language (Spanish shows the Latin until an approved text is on file). */
@Composable
fun CollectText(state: com.littlecloud.app.data.AppState, date: com.littlecloud.app.time.LocalDate, size: Int = 17) {
    val c by androidx.compose.runtime.produceState<com.littlecloud.app.data.Collects.Collect?>(null, date, state.rubrics) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            runCatching { state.collect(date) }.getOrNull()
        }
    }
    val col = c
    if (col == null) {
        LText("No collect is said today.", size - 2, Lc.muted)
        return
    }
    val show = laEn(state)
    val showLa = show.latin
    val showEn = show.english
    if (show.englishForSpanish) {
        LText(NO_SPANISH_NOTE, 13, Lc.muted, lineHeight = 1.35f)
        Spacer(Modifier.height(6.dp))
    }
    if (showLa) LText(col.la, size, lineHeight = 1.45f)
    if (showLa && showEn) Spacer(Modifier.height(8.dp))
    if (showEn) LText(col.en, size - 1, if (showLa) Lc.muted else Lc.ink, lineHeight = 1.45f)
}

/**
 * A one-line card: round icon, title, a muted line beneath, and a chevron. Used for every
 * tappable row on Today so they read as one family with the cards and tiles.
 */
@Composable
fun RowCard(
    icon: DrawableResource,
    title: String,
    sub: String,
    highlighted: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(if (highlighted) Lc.soft else Lc.surface)
            .border(1.dp, Lc.line, CardShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIcon(icon)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            LText(title, 17)
            if (sub.isNotEmpty()) LText(sub, 14, Lc.muted)
        }
        Ic(Res.drawable.ic_chevron_right, 18.dp, Lc.muted)
    }
}

/** A thin progress bar in [segments] parts, the first [filled] of them coloured. */
@Composable
fun SegmentBar(segments: Int, filled: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(segments) { i ->
            Box(
                Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp))
                    .background(if (i < filled) Lc.accent else Lc.line)
            )
        }
    }
}

/** Said at the foot of the liturgical screens. */
const val LITURGY_NOTE = "Little Cloud is a private aid to prayer, not an approved liturgical book. " +
    "Where you can, follow your approved missal, breviary and Ordo, and the direction of your priest."

/**
 * A thin, always-visible scroll bar for long pages such as an hour of the Office: shows how far
 * along you are, and can be dragged or tapped to move through the page.
 */
@Composable
fun ScrollIndicator(scroll: androidx.compose.foundation.ScrollState, modifier: Modifier = Modifier) {
    val max = scroll.maxValue
    if (max <= 0 || max == Int.MAX_VALUE) return
    var trackPx by remember { mutableStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val viewport = scroll.viewportSize.coerceAtLeast(1)
    val thumbFrac = (viewport.toFloat() / (max + viewport)).coerceIn(0.06f, 1f)
    val thumbPx = trackPx * thumbFrac
    fun jump(y: Float) {
        if (trackPx <= thumbPx) return
        val frac = ((y - thumbPx / 2) / (trackPx - thumbPx)).coerceIn(0f, 1f)
        scope.launch { scroll.scrollTo((frac * max).toInt()) }
    }
    Box(
        modifier
            .width(20.dp)
            .onSizeChanged { trackPx = it.height.toFloat() }
            .pointerInput(max) {
                detectVerticalDragGestures(
                    onDragStart = { dragging = true; jump(it.y) },
                    onDragEnd = { dragging = false },
                    onDragCancel = { dragging = false },
                ) { change, _ -> change.consume(); jump(change.position.y) }
            }
            .pointerInput(max) { detectTapGestures { jump(it.y) } },
    ) {
        val top = (trackPx - thumbPx) * scroll.value / max
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset { androidx.compose.ui.unit.IntOffset(0, top.toInt()) }
                .padding(end = 4.dp)
                .width(if (dragging) 6.dp else 4.dp)
                .height(with(density) { thumbPx.toDp() })
                .clip(RoundedCornerShape(3.dp))
                .background(Lc.accent.copy(alpha = if (dragging) 0.85f else 0.4f)),
        )
    }
}
