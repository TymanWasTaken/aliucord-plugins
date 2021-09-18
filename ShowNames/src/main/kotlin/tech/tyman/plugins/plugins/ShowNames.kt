package tech.tyman.plugins.plugins

import android.content.Context
import androidx.core.content.ContextCompat
import com.aliucord.patcher.PinePatchFn
import androidx.core.graphics.ColorUtils
import com.aliucord.Logger
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.discord.models.member.GuildMember
import com.discord.stores.StoreStream
import com.discord.utilities.color.ColorCompat
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.lytefast.flexinput.R
import kotlin.math.abs

@AliucordPlugin
class ShowNames : Plugin() {
    override fun start(context: Context) {
        patcher.patch(WidgetChatListAdapterItemMessage::class.java.getDeclaredMethod("getAuthorTextColor", GuildMember::class.java), PinePatchFn {
            val member = it.args[0] as GuildMember? ?: return@PinePatchFn
            val color = member.color
            val theme = StoreStream.getUserSettingsSystem().theme
            if (color == -16777216) { // Default (no role) color
                return@PinePatchFn
            }
            val nameHsl = colorToHsl(color)
            val bg = when (theme) {
                "pureEvil" -> ContextCompat.getColor(context, R.c.black)
                "dark" -> ContextCompat.getColor(context, R.c.primary_600)
                "light" -> ContextCompat.getColor(context, R.c.white)
                else -> return@PinePatchFn
            }
            val bgHsl = colorToHsl(bg)
            Logger("aaaaaaaaaaa").warn("""
                Name: $color
                Bg: $bg
                Diff: ${abs(nameHsl[2] - bgHsl[2])}
            """)
            if (abs(nameHsl[2] - bgHsl[2]) >= 0.01) return@PinePatchFn
            it.result = ColorUtils.HSLToColor(
                if (nameHsl[2] > 0.5) nameHsl.apply { this[2] =- 0.05f }
                else nameHsl.apply { this[2] =+ 0.05f }
            )
        })
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }

    private fun colorToHsl(color: Int): FloatArray = FloatArray(3).apply {
        ColorUtils.colorToHSL(color, this)
    }
}