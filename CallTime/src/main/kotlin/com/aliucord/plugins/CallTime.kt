package com.aliucord.plugins

import android.content.Context
import com.aliucord.Logger
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

    override fun start(context: Context) {
        patcher.patchConnectedText()
    }

    private fun PatcherAPI.patchConnectedText() {
        val currentVoiceStateField = StoreRtcConnection::class.java.getDeclaredField("currentVoiceState").apply { isAccessible = true }
        val onVoiceStateUpdateMethod = StoreRtcConnection::class.java.getDeclaredMethod("onVoiceStateUpdated").apply { isAccessible = true }
        val setupIndicatorStatusMethod = WidgetGlobalStatusIndicator::class.java.getDeclaredMethod("setupIndicatorStatus", WidgetGlobalStatusIndicatorViewModel.ViewState.CallOngoing::class.java).apply { isAccessible = true }
        val clock = ClockFactory.get()

        patch(setupIndicatorStatusMethod, PinePatchFn {
            val widget = it.thisObject as WidgetGlobalStatusIndicator
            timerTask = timer.schedule(0, 1000) {
                setupIndicatorStatusMethod.invoke(widget, it.args[0])
            }
        })

        patch(StoreRtcConnection::class.java.getDeclaredMethod("onVoiceStateUpdated"), PinePatchFn {
            val store = it.thisObject as StoreRtcConnection
            val currentVoiceState = currentVoiceStateField[store] as OutgoingPayload.VoiceStateUpdate?
                ?: return@PinePatchFn
            vcConnectedTime = if (currentVoiceState.channelId != null && cachedVoiceState?.channelId == null) {
                clock.currentTimeMillis()
            } else {
                timerTask?.cancel()
                timerTask = null
                null
            }
            cachedVoiceState = currentVoiceState
        })
        patch(VoiceViewUtils::class.java.getDeclaredMethod("getConnectedText", Context::class.java, RtcConnection.State::class.java, StreamContext::class.java, Boolean::class.javaPrimitiveType), PinePatchFn {
            val vcConnectedTime = vcConnectedTime ?: return@PinePatchFn
            val duration = clock.currentTimeMillis() - vcConnectedTime
            val hours = String.format("%02d", TimeUnit.MILLISECONDS.toHours(duration))
            val minutes = String.format("%02d", TimeUnit.MILLISECONDS.toMinutes(duration))
            val seconds = String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(duration))
            it.result = "${it.result} ($hours:$minutes:$seconds)"
        })

    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}