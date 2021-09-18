package tech.tyman.plugins.calltime

import android.content.Context
import com.aliucord.Logger
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
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

@AliucordPlugin
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
            Logger().warn("""
                cache
                    channel: ${cachedVoiceState?.channelId}
                    guild: ${cachedVoiceState?.guildId}
                    mute: ${cachedVoiceState?.selfMute}
                    deaf: ${cachedVoiceState?.selfMute}
                current
                    channel: ${currentVoiceState.channelId}
                    guild: ${currentVoiceState.guildId}
                    mute: ${currentVoiceState.selfMute}
                    deaf: ${currentVoiceState.selfMute}
            """)
            if (currentVoiceState.channelId != null && cachedVoiceState?.channelId != currentVoiceState.channelId) {
                vcConnectedTime = clock.currentTimeMillis()
            } else if (currentVoiceState.channelId == null && cachedVoiceState?.channelId != null) {
                timerTask?.cancel()
                timerTask = null
                if (vcConnectedTime != null) Utils.showToast(ctx, "Call ended. Elapsed time: $elapsedTime", true)
                vcConnectedTime = null
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