package com.littlecloud.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.littlecloud.app.platform.Prefs
import com.littlecloud.app.platform.appPrefs
import com.littlecloud.app.time.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

/** One line of the user's morning or evening rule. */
data class PrayerItem(
    val title: String,
    val note: String = "",
    val prayerId: String? = null,
    val text: String? = null,
)

/** A reminder bell. [times] are minutes after midnight. [opens] says what a tap on it opens. */
data class Bell(
    val id: Int,
    val name: String,
    val times: List<Int>,
    val sound: String,
    val enabled: Boolean,
    val opens: String? = null,
    /** Ring on the alarm stream (through silent mode) and show over the lock screen. */
    val loud: Boolean = false,
)

/** The user's own changes to a prayer-book text. A null field keeps the original. */
data class PrayerEdit(
    val title: String? = null,
    val latin: String? = null,
    val english: String? = null,
    val spanish: String? = null,
)

data class OfficeHour(
    val id: String,
    val latin: String,
    val english: String,
    val minutes: Int,
)

/**
 * One hour of the Little Office as it falls in a day's list. Matins and Lauds said by
 * anticipation belong to [date] (tomorrow) but are said this evening, after Compline.
 */
data class HourSlot(val hour: OfficeHour, val date: LocalDate, val anticipated: Boolean, val minutes: Int) {
    val id: String get() = hour.id
    val key: String get() = "o|${hour.id}"
    /** Distinguishes tonight's anticipated Matins from this morning's. */
    val slotId: String get() = if (anticipated) "${hour.id}+1" else hour.id
}

object Store {
    const val PREFS = "littlecloud"
    fun prefs(): Prefs = appPrefs()

    val defaultBells = listOf(
        Bell(1, "The Angelus", listOf(6 * 60, 12 * 60, 18 * 60), "angelus", true, "prayer:angelus"),
        Bell(2, "Little Office", listOf(5 * 60, 17 * 60 + 30, 21 * 60), "tower", true, "office"),
        Bell(3, "Mental prayer", listOf(5 * 60 + 30, 20 * 60), "hand", true, "intimacy"),
        Bell(4, "Examen & night prayers", listOf(21 * 60 + 30), "hand", false, "evening"),
    )

    val defaultMorning = listOf(
        PrayerItem("Sign of the Cross", "In nomine Patris…", "sign_cross"),
        PrayerItem("Morning Offering", "Through the Immaculate Heart of Mary", "morning_offering"),
        PrayerItem("Pater, Ave, Gloria", "Our Father · Hail Mary · Glory Be", "pag"),
        PrayerItem("Apostles’ Creed", "Credo in Deum", "creed"),
        PrayerItem("Acts of Faith, Hope & Charity", "", "acts"),
        PrayerItem("Angele Dei", "To my Guardian Angel", "angele_dei"),
    )

    val defaultEvening = listOf(
        PrayerItem("Examination of Conscience", "Followed by the Act of Contrition", "examen"),
        PrayerItem("Act of Contrition", "", "contrition"),
        PrayerItem("Ad te, beate Joseph", "Prayer to St. Joseph", "st_joseph"),
        PrayerItem("Spiritual Communion", "", "spiritual_communion"),
        PrayerItem("Flos Carmeli", "To Our Lady of Mount Carmel", "flos_carmeli"),
        PrayerItem("Sub tuum præsidium", "Before sleep", "sub_tuum"),
    )

    val defaultHours = listOf(
        OfficeHour("matins", "Matins", "Night office", 5 * 60),
        OfficeHour("lauds", "Lauds", "Morning praise", 5 * 60 + 20),
        OfficeHour("prime", "Prime", "The first hour", 6 * 60 + 30),
        OfficeHour("terce", "Terce", "The third hour", 9 * 60),
        OfficeHour("sext", "Sext", "The sixth hour", 12 * 60),
        OfficeHour("none", "None", "The ninth hour", 15 * 60),
        OfficeHour("vespers", "Vespers", "Evening praise", 17 * 60 + 30),
        OfficeHour("compline", "Compline", "Night prayer", 21 * 60),
    )

    // ---- bells (also read by the alarm receivers) ----

    fun loadBells(): List<Bell> {
        val raw = prefs().getString("bells", null) ?: return defaultBells
        return try {
            Json.parseToJsonElement(raw).jsonArray.map { e ->
                val o = e.jsonObject
                Bell(
                    id = o.int("id"),
                    name = o.str("name")!!,
                    times = o.getValue("times").jsonArray.map { it.jsonPrimitive.int },
                    sound = o.str("sound")!!,
                    enabled = o.getValue("enabled").jsonPrimitive.boolean,
                    opens = o.str("opens"),
                    loud = o["loud"]?.jsonPrimitive?.booleanOrNull ?: false,
                )
            }
        } catch (e: Exception) {
            defaultBells
        }
    }

    fun saveBells(bells: List<Bell>) {
        val arr = buildJsonArray {
            bells.forEach { b ->
                addJsonObject {
                    put("id", b.id); put("name", b.name); put("sound", b.sound); put("enabled", b.enabled); put("loud", b.loud)
                    putJsonArray("times") { b.times.forEach { add(it) } }
                    b.opens?.let { put("opens", it) }
                }
            }
        }
        prefs().edit().putString("bells", arr.toString()).apply()
    }

    fun triduumSilence() = prefs().getBoolean("triduum", true)

    // ---- prayer lists ----

    fun loadList(key: String, default: List<PrayerItem>): List<PrayerItem> {
        val raw = prefs().getString(key, null) ?: return default
        return try {
            Json.parseToJsonElement(raw).jsonArray.map { e ->
                val o = e.jsonObject
                PrayerItem(
                    title = o.str("title")!!,
                    note = o.str("note") ?: "",
                    prayerId = o.str("prayerId"),
                    text = o.str("text"),
                )
            }
        } catch (e: Exception) {
            default
        }
    }

    fun saveList(key: String, list: List<PrayerItem>) {
        val arr = buildJsonArray {
            list.forEach { p ->
                addJsonObject {
                    put("title", p.title); put("note", p.note)
                    p.prayerId?.let { put("prayerId", it) }
                    p.text?.let { put("text", it) }
                }
            }
        }
        prefs().edit().putString(key, arr.toString()).apply()
    }

    fun loadHours(): List<OfficeHour> {
        val p = prefs()
        return defaultHours.map { it.copy(minutes = p.getInt("hour_" + it.id, it.minutes)) }
    }
}

/**
 * Spanish is set aside until traditional translations of the collects, readings and Office are
 * on file. The Spanish prayer texts stay in the code; set this to true to offer Spanish again.
 */
const val SPANISH_ENABLED = false

/** The note shown under prayers the user wrote. */
const val OWN_NOTE = "Written by you"

/** The rubrics the app can follow: Pius XII (1955, the default), before 1955, and 1962. */
val RUBRICS = listOf("1955", "1954", "1962")

/** How each choice of rubrics is named in the app. */
fun rubricsLabel(r: String) = when (r) { "1955" -> "1955 (Pius XII)"; "1954" -> "before 1955"; else -> "1962" }

/** UI state, backed by the saved settings. */
class AppState {
    private val prefs = Store.prefs()

    var theme by mutableStateOf(prefs.getString("theme", "system") ?: "system"); private set
    var discipline by mutableStateOf(
        runCatching { Discipline.valueOf(prefs.getString("discipline", null) ?: "") }.getOrDefault(Discipline.UNIVERSAL_1917)
    ); private set
    var officeLang by mutableStateOf(prefs.getString("officeLang", "both") ?: "both"); private set
    var carmelite by mutableStateOf(prefs.getBoolean("carmelite", true)); private set
    /** Vernacular for prayers and the Office: "en" or "es". */
    var vernacular by mutableStateOf(if (SPANISH_ENABLED) prefs.getString("vernacular", "en") ?: "en" else "en"); private set
    var triduum by mutableStateOf(prefs.getBoolean("triduum", true)); private set
    /**
     * "1955": the rubrics of Pius XII (1955, with the restored Holy Week of 1956), as kept by the
     * CMRI; the default. "1954": the rubrics before 1955 (Divino Afflatu). "1962": those of 1960/62.
     */
    var rubrics by mutableStateOf(
        (prefs.getString("rubrics", "1955") ?: "1955").takeIf { it in RUBRICS } ?: "1955"
    ); private set
    val pre1955: Boolean get() = rubrics == "1954"
    private val olderCalendar: OlderCalendar?
        get() = when (rubrics) { "1954" -> OlderCalendar.Before1955; "1955" -> OlderCalendar.Pius1955; else -> null }

    init {
        // Read the older calendar in the background, so the first screen does not wait for it.
        CoroutineScope(Dispatchers.Default).launch {
            runCatching { olderCalendar?.day(LocalDate.now(), Discipline.UNIVERSAL_1917, false) }
        }
    }

    var morning by mutableStateOf(Store.loadList("morning", Store.defaultMorning)); private set
    var evening by mutableStateOf(Store.loadList("evening", Store.defaultEvening)); private set
    var bells by mutableStateOf(Store.loadBells()); private set
    var hours by mutableStateOf(Store.loadHours()); private set

    var today by mutableStateOf(LocalDate.now()); private set
    var done by mutableStateOf(loadDone(LocalDate.now())); private set
    /** Tomorrow's ticks: Matins and Lauds said tonight by anticipation count for tomorrow. */
    private var doneNext by mutableStateOf(loadDone(LocalDate.now().plusDays(1)))

    fun refreshDay() {
        val now = LocalDate.now()
        if (now != today) {
            today = now
            done = loadDone(now)
            doneNext = loadDone(now.plusDays(1))
        }
    }

    private fun loadDone(d: LocalDate): Set<String> = prefs.getStringSet("done_$d", emptySet())?.toSet() ?: emptySet()

    fun isDone(key: String) = key in done
    fun toggleDone(key: String) = toggleDoneOn(today, key)

    fun isDoneOn(d: LocalDate, key: String) = when (d) {
        today -> key in done
        today.plusDays(1) -> key in doneNext
        else -> key in loadDone(d)
    }

    fun toggleDoneOn(d: LocalDate, key: String) {
        val set = loadDone(d).let { if (d == today) done else if (d == today.plusDays(1)) doneNext else it }
        val new = if (key in set) set - key else set + key
        when (d) {
            today -> done = new
            today.plusDays(1) -> doneNext = new
        }
        val edit = prefs.edit().putStringSet("done_$d", new)
        // forget ticks older than a week
        prefs.keys.filter { it.startsWith("done_") && it < "done_${today.minusDays(7)}" }.forEach { edit.remove(it) }
        edit.apply()
    }

    // ---- Matins and Lauds by anticipation ------------------------------------------------

    /** Say Matins the evening before. */
    var anticipateMatins by mutableStateOf(prefs.getBoolean("anticipateMatins", false)); private set
    /** Say Lauds with Matins the evening before (only when Matins is anticipated). */
    var anticipateLauds by mutableStateOf(prefs.getBoolean("anticipateLauds", false)); private set

    /** The evening times for anticipated Matins and Lauds, after Compline. */
    fun eveMinutes(id: String): Int = prefs.getInt("hour_${id}_eve", if (id == "matins") 21 * 60 + 15 else 21 * 60 + 35)
    fun setEveTime(id: String, minutes: Int) {
        prefs.edit().putInt("hour_${id}_eve", minutes).apply()
        hours = Store.loadHours() // recompose the lists
    }

    /** Thursday, Friday and Saturday of Holy Week: Tenebrae, always said the evening before. */
    private fun tenebrae(d: LocalDate) = Calendar1962.isTriduum(d)

    /** Whether the [id] hour ("matins" or "lauds") of day [d] is said on the eve. */
    fun anticipates(d: LocalDate, id: String): Boolean {
        if (tenebrae(d)) return id == "matins" || id == "lauds"
        // Christmas: Matins is the night office of the feast itself, said that night.
        if (d.monthNumber == 12 && d.dayOfMonth == 25) return false
        return when (id) {
            "matins" -> anticipateMatins
            "lauds" -> anticipateMatins && anticipateLauds
            else -> false
        }
    }

    /** The hours to be said on day [d], in order: the day's own, then any of tomorrow's said tonight. */
    fun slots(d: LocalDate): List<HourSlot> {
        val own = hours.filter { !anticipates(d, it.id) }.map { HourSlot(it, d, false, it.minutes) }
        val next = d.plusDays(1)
        val eve = hours.filter { (it.id == "matins" || it.id == "lauds") && anticipates(next, it.id) }
            .map { HourSlot(it, next, true, eveMinutes(it.id)) }
        return own + eve
    }

    fun isSaid(slot: HourSlot) = isDoneOn(slot.date, slot.key)
    fun toggleSaid(slot: HourSlot) = toggleDoneOn(slot.date, slot.key)

    /**
     * Changes when Matins (and Lauds) are said. The Little Office bell set for the old time of
     * Matins moves with it. Returns true when the bells changed and need scheduling again.
     */
    fun updateAnticipation(matins: Boolean, lauds: Boolean): Boolean {
        val oldMatins = if (anticipateMatins) eveMinutes("matins") else hours.first { it.id == "matins" }.minutes
        anticipateMatins = matins
        anticipateLauds = matins && lauds
        prefs.edit().putBoolean("anticipateMatins", anticipateMatins).putBoolean("anticipateLauds", anticipateLauds).apply()
        val newMatins = if (matins) eveMinutes("matins") else hours.first { it.id == "matins" }.minutes
        if (oldMatins == newMatins) return false
        var changed = false
        val list = bells.map { b ->
            if (b.opens == "office" && oldMatins in b.times && newMatins !in b.times) {
                changed = true
                b.copy(times = (b.times - oldMatins + newMatins).sorted())
            } else b
        }
        if (changed) updateBells(list)
        return changed
    }

    fun updateTheme(v: String) { theme = v; prefs.edit().putString("theme", v).apply() }
    fun updateDiscipline(v: Discipline) { discipline = v; prefs.edit().putString("discipline", v.name).apply() }
    fun updateOfficeLang(v: String) { officeLang = v; prefs.edit().putString("officeLang", v).apply() }
    fun updateVernacular(v: String) { vernacular = v; prefs.edit().putString("vernacular", v).apply() }
    fun updateCarmelite(v: Boolean) { carmelite = v; prefs.edit().putBoolean("carmelite", v).apply() }
    fun updateRubrics(v: String) { rubrics = v; prefs.edit().putString("rubrics", v).apply() }
    fun updateTriduum(v: Boolean) { triduum = v; prefs.edit().putBoolean("triduum", v).apply() }

    fun setList(evening: Boolean, list: List<PrayerItem>) {
        if (evening) { this.evening = list; Store.saveList("evening", list) }
        else { this.morning = list; Store.saveList("morning", list) }
    }

    fun updateBells(list: List<Bell>) {
        bells = list
        Store.saveBells(list)
    }

    fun setHourTime(id: String, minutes: Int) {
        prefs.edit().putInt("hour_$id", minutes).apply()
        hours = Store.loadHours()
    }

    fun dayInfo(d: LocalDate): LiturgicalDay = Americas.apply(
        olderCalendar?.day(d, discipline, carmelite) ?: Calendar1962.day(d, discipline, carmelite),
        rubrics,
    )

    /** The Epistle and Gospel of the day's Mass, by the rubrics chosen. */
    fun readings(d: LocalDate): List<Readings.Reading> =
        if (Americas.kept(d)) Americas.readings().ifEmpty { Readings.forDate(d, rubrics) } else Readings.forDate(d, rubrics)

    /** Text size for the readings, shared with mental prayer. */
    var readingSize by mutableStateOf(prefs.getInt("readSize", 18)); private set
    fun updateReadingSize(v: Int) { readingSize = v; prefs.edit().putInt("readSize", v).apply() }

    fun collect(d: LocalDate): Collects.Collect? =
        if (Americas.kept(d)) Americas.collect
        else olderCalendar.let { if (it != null) it.collect(d) else Collects.forDate(d) }

    // ---- The user's edits to prayer-book texts --------------------------------------------

    var prayerEdits by mutableStateOf(loadEdits()); private set

    private fun loadEdits(): Map<String, PrayerEdit> = runCatching {
        val o = Json.parseToJsonElement(prefs.getString("prayerEdits", "{}") ?: "{}").jsonObject
        o.mapValues { (_, v) ->
            val e = v.jsonObject
            PrayerEdit(e.str("title"), e.str("latin"), e.str("english"), e.str("spanish"))
        }
    }.getOrDefault(emptyMap())

    private fun saveEdits(m: Map<String, PrayerEdit>) {
        prayerEdits = m
        val o = buildJsonObject {
            m.forEach { (k, e) ->
                putJsonObject(k) {
                    e.title?.let { put("title", it) }; e.latin?.let { put("latin", it) }
                    e.english?.let { put("english", it) }; e.spanish?.let { put("spanish", it) }
                }
            }
        }
        prefs.edit().putString("prayerEdits", o.toString()).apply()
    }

    // ---- Prayers the user writes: kept in the prayer book under "Your prayers" -------------

    /** The user's own prayers, ids "own:1", "own:2" …; the words are kept in [Prayer.english]. */
    var ownPrayers by mutableStateOf(loadOwn()); private set

    private fun loadOwn(): List<Prayer> = runCatching {
        Json.parseToJsonElement(prefs.getString("ownPrayers", null) ?: "[]").jsonArray.map {
            val o = it.jsonObject
            Prayer(o.str("id")!!, o.str("title")!!, OWN_NOTE, null, o.str("text") ?: "")
        }
    }.getOrDefault(emptyList())

    private fun saveOwn(list: List<Prayer>) {
        ownPrayers = list
        val arr = buildJsonArray {
            list.forEach { p -> addJsonObject { put("id", p.id); put("title", p.title); put("text", p.english) } }
        }
        prefs.edit().putString("ownPrayers", arr.toString()).apply()
    }

    /** Adds a prayer the user wrote to the prayer book and returns it. */
    fun addOwnPrayer(title: String, text: String): Prayer {
        val n = (ownPrayers.mapNotNull { it.id.removePrefix("own:").toIntOrNull() }.maxOrNull() ?: 0) + 1
        val p = Prayer("own:$n", title.trim(), OWN_NOTE, null, text.trim())
        saveOwn(ownPrayers + p)
        return p
    }

    /** Changes one of the user's prayers, and its title wherever it stands in the morning or evening rule. */
    fun updateOwnPrayer(id: String, title: String, text: String) {
        if (title.isBlank()) return
        saveOwn(ownPrayers.map { if (it.id == id) it.copy(title = title.trim(), english = text.trim()) else it })
        fun fix(l: List<PrayerItem>) = l.map { if (it.prayerId == id) it.copy(title = title.trim()) else it }
        setList(false, fix(morning)); setList(true, fix(evening))
    }

    /** Removes one of the user's prayers from the book and from the morning and evening rule. */
    fun removeOwnPrayer(id: String) {
        saveOwn(ownPrayers.filter { it.id != id })
        setList(false, morning.filter { it.prayerId != id }); setList(true, evening.filter { it.prayerId != id })
    }

    init {
        // Prayers written before 0.9.2 lived only in the rule; move each into the prayer book.
        if (!prefs.contains("ownPrayers")) {
            val seen = mutableMapOf<Pair<String, String?>, String>()
            fun move(l: List<PrayerItem>) = l.map { item ->
                if (item.prayerId != null || item.note != OWN_NOTE) item
                else {
                    val id = seen.getOrPut(item.title to item.text) { addOwnPrayer(item.title, item.text ?: "").id }
                    item.copy(prayerId = id, text = null)
                }
            }
            val m = move(morning); val e = move(evening)
            if (seen.isNotEmpty()) { setList(false, m); setList(true, e) } else saveOwn(emptyList())
        }
    }

    /** A prayer-book prayer with the user's edits applied, or one of the user's own prayers. */
    fun prayer(id: String?): Prayer? {
        if (id != null && id.startsWith("own:")) return ownPrayers.firstOrNull { it.id == id }
        val base = PrayerBook.get(id) ?: return null
        val e = prayerEdits[base.id] ?: return base
        return base.copy(
            title = e.title ?: base.title,
            latin = if (e.latin != null) e.latin.ifBlank { null } else base.latin,
            english = e.english ?: base.english,
        )
    }

    /** The Spanish text of a prayer, with the user's edit if any. */
    fun spanish(id: String?): String? {
        if (id == null) return null
        val e = prayerEdits[id]?.spanish
        return if (e != null) e.ifBlank { null } else SpanishPrayers.get(id)
    }

    fun isEdited(id: String?) = id != null && id in prayerEdits

    /** Saves the user's text, keeping only what differs from the original. */
    fun editPrayer(id: String, title: String, latin: String, english: String, spanish: String) {
        val base = PrayerBook.get(id) ?: return
        fun diff(new: String, old: String?) = new.trim().takeIf { it != (old ?: "").trim() }
        val e = PrayerEdit(
            diff(title, base.title)?.ifBlank { null },
            diff(latin, base.latin),
            diff(english, base.english)?.ifBlank { null },
            diff(spanish, SpanishPrayers.get(id)),
        )
        val m = prayerEdits.toMutableMap()
        if (e == PrayerEdit()) m.remove(id) else m[id] = e
        saveEdits(m)
    }

    fun restorePrayer(id: String) { saveEdits(prayerEdits - id) }

    /** Edits one of the user's own prayers in the morning or evening rule. */
    fun editOwnPrayer(evening: Boolean, old: PrayerItem, title: String, text: String) {
        val list = if (evening) this.evening else morning
        val i = list.indexOf(old)
        if (i < 0 || title.isBlank()) return
        setList(evening, list.toMutableList().also { it[i] = old.copy(title = title.trim(), text = text.trim().ifEmpty { null }) })
    }
}

/** A string field, or null when it is missing or JSON null. */
internal fun JsonObject.str(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
internal fun JsonObject.int(key: String): Int = getValue(key).jsonPrimitive.int
