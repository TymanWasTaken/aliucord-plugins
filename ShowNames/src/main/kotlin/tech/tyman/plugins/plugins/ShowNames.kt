package tech.tyman.plugins.plugins

import android.content.Context
import com.aliucord.entities.Plugin.Manifest.Author
import com.aliucord.patcher.PinePatchFn
import androidx.core.graphics.ColorUtils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.discord.models.member.GuildMember
import com.discord.stores.StoreStream
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage

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
            val colorBrightness = ColorUtils.calculateLuminance(color)
            val newColor = if (colorBrightness < 0.01 && theme == "pureEvil") { // pureEvil = AMOLED
                0xFF333333
            } else if (colorBrightness > 0.99 && theme == "light") {
                0xFFDEDEDE
            } else if (colorBrightness > 0.035 && colorBrightness < 0.045 && theme == "dark") { // not sure about these numbers but they work ¯\_(ツ)_/¯
                0xFF4E535A
            } else color
            it.result = newColor.toInt()
        })
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}