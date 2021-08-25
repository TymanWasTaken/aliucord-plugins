package com.aliucord.plugins

import android.content.Context
import com.aliucord.entities.Plugin.Manifest.Author
import com.aliucord.patcher.PinePatchFn
import androidx.core.graphics.ColorUtils
import com.aliucord.entities.Plugin
import com.discord.models.member.GuildMember
import com.discord.stores.StoreStream
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage

class ShowNames : Plugin() {
    override fun getManifest() = Manifest().apply {
        authors = arrayOf(Author("Tyman", 487443883127472129L))
        description = "A plugin that changes the color of usernames to stop them from blending into the background."
        version = "1.0.3"
        updateUrl = "https://raw.githubusercontent.com/TymanWasTaken/aliucord-plugins/builds/updater.json"
        changelog =
                """
                    # Version 1.0.0
                    - Initial release
                    # Version 1.0.1
                    - Fixed changing color when on amoled mode and user has no role color
                    # Version 1.0.2
                    - Fixed crashing on outdated aliucord
                    # Version 1.0.3
                    - Converted code to use kotlin
                """.trimIndent()
    }

    override fun start(context: Context) {
        patcher.patch(WidgetChatListAdapterItemMessage::class.java.getDeclaredMethod("getAuthorTextColor", GuildMember::class.java), PinePatchFn {
            val member = it.args[0] as GuildMember
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