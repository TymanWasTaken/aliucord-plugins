package com.aliucord.plugins

import android.content.Context
import com.aliucord.Utils
import com.aliucord.entities.Plugin.Manifest.Author
import com.aliucord.patcher.PinePatchFn
import com.aliucord.api.PatcherAPI
import com.aliucord.entities.Plugin
import com.discord.gateway.io.OutgoingPayload
import com.discord.rtcconnection.RtcConnection
import com.discord.stores.StoreRtcConnection
import com.discord.utilities.streams.StreamContext
import com.discord.utilities.time.ClockFactory
import com.discord.utilities.voice.VoiceViewUtils
import com.discord.widgets.status.WidgetGlobalStatusIndicator
import com.discord.widgets.status.WidgetGlobalStatusIndicatorViewModel
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

class CallTime : Plugin() {
    private var vcConnectedTime: Long? = null
    private var cachedVoiceState: OutgoingPayload.VoiceStateUpdate? = null
    private val timer = Timer()
    private var timerTask: TimerTask? = null
    private val clock = ClockFactory.get()
    private val elapsedTime: String
        get() = run {
            val vcConnectedTime = vcConnectedTime ?: return "00:00:00"
            val duration = clock.currentTimeMillis() - vcConnectedTime
            val hoursNumber = TimeUnit.MILLISECONDS.toHours(duration)
            val minutesNumber = TimeUnit.MILLISECONDS.toMinutes(duration) - hoursNumber * 60
            val secondsNumber = TimeUnit.MILLISECONDS.toSeconds(duration) - minutesNumber * 60
            val hours = String.format("%02d", hoursNumber)
            val minutes = String.format("%02d", minutesNumber)
            val seconds = String.format("%02d", secondsNumber)
            return "$hours:$minutes:$seconds"
        }

    override fun getManifest() = Manifest().apply {
        authors = arrayOf(Author("Tyman", 487443883127472129L))
        description = "A plugin that shows how long you have been in a VC for."
        version = "0.0.1"
        updateUrl = "https://raw.githubusercontent.com/TymanWasTaken/aliucord-plugins/builds/updater.json"
        changelog =
            """
                    # Version 1.0.0
                    - Initial release
                """.trimIndent()
    }

    override fun start(ctx: Context) {
        patcher.patchConnectedText(ctx)
    }

    private fun PatcherAPI.patchConnectedText(ctx: Context) {
        val currentVoiceStateField = StoreRtcConnection::class.java.getDeclaredField("currentVoiceState").apply { isAccessible = true }
        val setupIndicatorStatusMethod = WidgetGlobalStatusIndicator::class.java.getDeclaredMethod("setupIndicatorStatus", WidgetGlobalStatusIndicatorViewModel.ViewState.CallOngoing::class.java).apply { isAccessible = true }
        val getConnectedTextMethod = VoiceViewUtils::class.java.getDeclaredMethod("getConnectedText", Context::class.java, RtcConnection.State::class.java, StreamContext::class.java, Boolean::class.javaPrimitiveType)

        patch(setupIndicatorStatusMethod, PinePatchFn {
            val widget = it.thisObject as WidgetGlobalStatusIndicator
            timerTask?.cancel()
            timerTask = timer.schedule(1000) {
                widget.activity?.runOnUiThread {
                    setupIndicatorStatusMethod.invoke(widget, it.args[0])
                }
            }
        })

        patch(StoreRtcConnection::class.java.getDeclaredMethod("onVoiceStateUpdated"), PinePatchFn {
            val store = it.thisObject as StoreRtcConnection
            val currentVoiceState = currentVoiceStateField[store] as OutgoingPayload.VoiceStateUpdate?
                ?: return@PinePatchFn
            vcConnectedTime = if (currentVoiceState.channelId != null && cachedVoiceState?.channelId != currentVoiceState.channelId) {
                clock.currentTimeMillis()
            } else {
                timerTask?.cancel()
                timerTask = null
                if (vcConnectedTime != null) Utils.showToast(ctx, "Call ended. Elapsed time: $elapsedTime", true)
                null
            }
            cachedVoiceState = currentVoiceState
        })
        patch(getConnectedTextMethod, PinePatchFn {
            it.result = "${it.result} ($elapsedTime)"
        })

    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}