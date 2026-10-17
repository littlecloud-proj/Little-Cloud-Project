@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.littlecloud.app.bells

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.littlecloud.app.data.AppState
import com.littlecloud.app.data.Calendar1962
import com.littlecloud.app.data.Store
import com.littlecloud.app.platform.BellSystem
import com.littlecloud.app.time.*
import com.littlecloud.app.ui.LCard
import com.littlecloud.app.ui.LText
import com.littlecloud.app.ui.OutlineButton
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
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
 * Bells on iPhone, as local notifications. iOS keeps at most 64 waiting notifications, so the
 * bells are laid out for the next days only and laid out again each time the app opens.
 */
object IosBells : BellSystem {
    private const val KEY_IDS = "iosBellIds"

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

    override fun reschedule() {
        val prefs = Store.prefs()
        prefs.getStringSet(KEY_IDS, emptySet())?.toList()?.let { center.removePendingNotificationRequestsWithIdentifiers(it) }

        val bells = Store.loadBells().filter { it.enabled && it.times.isNotEmpty() }
        val perDay = bells.sumOf { it.times.size }.coerceAtLeast(1)
        val days = (60 / perDay).coerceIn(1, 14)
        val today = LocalDate.now()
        val nowMin = LocalTime.now().let { it.hour * 60 + it.minute }
        val ids = mutableSetOf<String>()

        for (offset in 0 until days) {
            val day = today.plusDays(offset)
            val silent = Store.triduumSilence() && Calendar1962.isTriduum(day)
            for (bell in bells) for ((idx, minutes) in bell.times.withIndex()) {
                if (offset == 0 && minutes <= nowMin) continue
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
                val content = UNMutableNotificationContent().apply {
                    setTitle(title)
                    setBody(body)
                    setSound(UNNotificationSound.soundNamed(soundFile(if (silent) "clapper" else bell.sound)))
                    setUserInfo(mapOf<Any?, Any?>("opens" to (opens ?: "")))
                }
                val comps = NSDateComponents().apply {
                    setYear(day.year.toLong()); setMonth(day.monthNumber.toLong()); setDay(day.dayOfMonth.toLong())
                    setHour((minutes / 60).toLong()); setMinute((minutes % 60).toLong())
                }
                val id = "bell-${bell.id}-$idx-$day"
                val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(comps, repeats = false)
                center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier(id, content, trigger), null)
                ids += id
            }
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
                    AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, null)
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
