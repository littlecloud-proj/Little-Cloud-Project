@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.littlecloud.app.bells

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.littlecloud.app.data.AppState
import com.littlecloud.app.data.Bell
import com.littlecloud.app.data.Calendar1962
import com.littlecloud.app.data.Store
import com.littlecloud.app.platform.BellSystem
import com.littlecloud.app.time.*
import com.littlecloud.app.ui.LCard
import com.littlecloud.app.ui.LText
import com.littlecloud.app.ui.OutlineButton
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionMixWithOthers
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.Foundation.NSDateComponents
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.*
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * Bells on iPhone, as local notifications. iOS keeps at most 64 waiting notifications, so bells
 * repeat daily where they can; see [reschedule].
 */
object IosBells : BellSystem {
    private const val KEY_IDS = "iosBellIds"
    private const val RENEW_ID = "bell-renew"
    /** A bell unchanged for this many days ahead is set to repeat daily. */
    private const val STEADY_DAYS = 21

    private data class Snd(val key: String, val label: String, val note: String)
    private val all = listOf(
        Snd("angelus", "Angelus bells", "Three, three, three, then nine"),
        Snd("peal", "Cathedral peal", "Rounds on eight bells"),
        Snd("tower", "Single tower bell", "One deep toll"),
        Snd("hand", "Monastery hand bell", "Soft and brief"),
    )
    private val clapper = Snd("clapper", "Wooden clapper", "For the Sacred Triduum")

    override val sounds = all.map { BellSystem.Sound(it.key, it.label, it.note) }
    override fun label(key: String) = (all + clapper).firstOrNull { it.key == key }?.label ?: "Bell"
    override val canRingLoud = false
    override val footnote =
        "Tap a bell to change its times or sound. On iPhone the bells come as notifications, so the ring/silent switch and Focus apply to them."

    private val center get() = UNUserNotificationCenter.currentNotificationCenter()

    private fun soundFile(key: String) =
        "bell_" + ((all + clapper).firstOrNull { it.key == key }?.key ?: "tower") + ".caf"

    fun requestPermission() {
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, _ -> if (granted) dispatch_async(dispatch_get_main_queue()) { reschedule() } }
    }

    /** What a bell says and how it sounds on a given day. */
    private data class Ring(val title: String, val body: String, val opens: String?, val sound: String)

    private fun ringOn(bell: Bell, day: LocalDate, silence: Boolean): Ring {
        var title = bell.name
        var opens = bell.opens
        var body = when (bell.opens) {
            "office" -> "Time to pray the Little Office."
            "intimacy" -> "Time for mental prayer."
            "evening" -> "Time for night prayers."
            else -> "Time to pray."
        }
        if (bell.opens == "prayer:angelus") {
            if (Calendar1962.isPaschaltide(day)) {
                title = "Regina Cæli"; body = "Queen of Heaven, rejoice, alleluia."; opens = "prayer:regina_caeli"
            } else body = "The Angel of the Lord declared unto Mary."
        }
        return Ring(title, body, opens, if (silence && Calendar1962.isTriduum(day)) "clapper" else bell.sound)
    }

    private fun add(id: String, ring: Ring, comps: NSDateComponents, repeats: Boolean) {
        val content = UNMutableNotificationContent().apply {
            setTitle(ring.title)
            setBody(ring.body)
            setSound(UNNotificationSound.soundNamed(soundFile(ring.sound)))
            setUserInfo(mapOf<Any?, Any?>("opens" to (ring.opens ?: "")))
        }
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(comps, repeats = repeats)
        center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier(id, content, trigger), null)
    }

    /**
     * A bell that says and sounds the same for the coming weeks is set once, to repeat every day, and
     * so never runs out. Near the Triduum and at either end of Paschaltide, where the sound or the
     * words change with the date, the bells are set day by day for as many days as iOS has room for,
     * with a reminder at the end to open the app so the next days can be set.
     */
    override fun reschedule() {
        val prefs = Store.prefs()
        prefs.getStringSet(KEY_IDS, emptySet())?.toList()?.let { center.removePendingNotificationRequestsWithIdentifiers(it) }

        val bells = Store.loadBells().filter { it.enabled && it.times.isNotEmpty() }
        val silence = Store.triduumSilence()
        val today = LocalDate.now()
        val nowMin = LocalTime.now().let { it.hour * 60 + it.minute }
        val ids = mutableSetOf<String>()

        val (daily, dated) = bells.partition { bell ->
            val first = ringOn(bell, today, silence)
            (1..STEADY_DAYS).all { ringOn(bell, today.plusDays(it), silence) == first }
        }

        for (bell in daily) for ((idx, minutes) in bell.times.withIndex()) {
            val comps = NSDateComponents().apply { setHour((minutes / 60).toLong()); setMinute((minutes % 60).toLong()) }
            val id = "bell-${bell.id}-$idx-daily"
            add(id, ringOn(bell, today, silence), comps, repeats = true)
            ids += id
        }

        val perDay = dated.sumOf { it.times.size }
        if (perDay > 0) {
            val room = (60 - daily.sumOf { it.times.size }).coerceAtLeast(perDay)
            val days = (room / perDay).coerceIn(1, 14)
            for (offset in 0 until days) {
                val day = today.plusDays(offset)
                for (bell in dated) for ((idx, minutes) in bell.times.withIndex()) {
                    if (offset == 0 && minutes <= nowMin) continue
                    val comps = NSDateComponents().apply {
                        setYear(day.year.toLong()); setMonth(day.monthNumber.toLong()); setDay(day.dayOfMonth.toLong())
                        setHour((minutes / 60).toLong()); setMinute((minutes % 60).toLong())
                    }
                    val id = "bell-${bell.id}-$idx-$day"
                    add(id, ringOn(bell, day, silence), comps, repeats = false)
                    ids += id
                }
            }
            // The day after the last one set: say so, rather than fall silent.
            val after = today.plusDays(days)
            val first = dated.flatMap { it.times }.min()
            val comps = NSDateComponents().apply {
                setYear(after.year.toLong()); setMonth(after.monthNumber.toLong()); setDay(after.dayOfMonth.toLong())
                setHour((first / 60).toLong()); setMinute((first % 60).toLong())
            }
            add(RENEW_ID, Ring("Little Cloud", "Open Little Cloud to set the bells for the coming days.", "tab:bells", "hand"), comps, repeats = false)
            ids += RENEW_ID
        }
        prefs.edit().putStringSet(KEY_IDS, ids).apply()
    }

    override fun ringNow(name: String, sound: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle(name); setBody("This is how this bell will ring.")
            setSound(UNNotificationSound.soundNamed(soundFile(sound)))
        }
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(1.0, repeats = false)
        center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier("bell-test", content, trigger), null)
    }

    /** The prayer timer's bell, as a notification after [minutes]. */
    fun prayerTimer(minutes: Int, id: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Mental prayer"); setBody("Your time of prayer has ended.")
            setSound(UNNotificationSound.soundNamed(soundFile("hand")))
            setUserInfo(mapOf<Any?, Any?>("opens" to "intimacy"))
        }
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(minutes * 60.0, repeats = false)
        center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier(id, content, trigger), null)
    }

    @Composable
    override fun rememberPlayer(): (String?) -> Unit {
        val holder = remember { arrayOfNulls<AVAudioPlayer>(1) }
        DisposableEffect(Unit) { onDispose { holder[0]?.stop(); holder[0] = null } }
        return { key ->
            holder[0]?.stop(); holder[0] = null
            if (key != null) {
                val url: NSURL? = NSBundle.mainBundle.URLForResource(soundFile(key).removeSuffix(".caf"), "caf")
                if (url != null) {
                    // Mixed with whatever is playing, so a preview does not stop the user's music.
                    AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, withOptions = AVAudioSessionCategoryOptionMixWithOthers, error = null)
                    AVAudioSession.sharedInstance().setActive(true, null)
                    holder[0] = AVAudioPlayer(contentsOfURL = url, error = null).also { it.play() }
                }
            }
        }
    }

    @Composable
    override fun rememberCustomPicker(onPicked: (String) -> Unit): (() -> Unit)? = null

    @Composable
    override fun Notices(state: AppState) {
        var denied by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            center.getNotificationSettingsWithCompletionHandler { s ->
                val d = s?.authorizationStatus == UNAuthorizationStatusDenied
                dispatch_async(dispatch_get_main_queue()) { denied = d }
            }
        }
        if (denied) {
            LCard(padding = 16.dp) {
                LText("Allow notifications for Little Cloud so the bells can ring.", 16)
                Spacer(Modifier.height(10.dp))
                OutlineButton("Open settings") {
                    NSURL.URLWithString(UIApplicationOpenSettingsURLString)?.let {
                        UIApplication.sharedApplication.openURL(it, emptyMap<Any?, Any?>(), null)
                    }
                }
            }
        }
    }
}

/** Shows bells while the app is open, and opens the right screen when one is tapped. */
class BellNotificationDelegate(private val open: (String) -> Unit) : NSObject(), UNUserNotificationCenterDelegateProtocol {
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (UNNotificationPresentationOptions) -> Unit,
    ) {
        withCompletionHandler(UNNotificationPresentationOptionBanner or UNNotificationPresentationOptionList or UNNotificationPresentationOptionSound)
    }

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        didReceiveNotificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit,
    ) {
        val opens = didReceiveNotificationResponse.notification.request.content.userInfo["opens"] as? String
        if (!opens.isNullOrEmpty()) open(opens)
        withCompletionHandler()
    }
}
