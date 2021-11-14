package tech.tyman.plugins.reverseimagesearch

import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import androidx.appcompat.view.menu.ActionMenuItemView
import com.aliucord.Utils
import com.discord.widgets.media.WidgetMedia
import com.aliucord.patcher.after
import android.net.Uri
import android.content.Intent
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@AliucordPlugin
class ReverseImageSearch : Plugin() {
    init {
        settingsTab = SettingsTab(SearchSettings::class.java).withArgs(settings)
    }

    override fun start(context: Context) {
        val imageUriField = WidgetMedia::class.java.getDeclaredField("imageUri").apply {
            isAccessible = true
        }
        val isVideoMethod = WidgetMedia::class.java.getDeclaredMethod("isVideo").apply {
            isAccessible = true
        }
        patcher.after<WidgetMedia>("onViewBoundOrOnResume") {
            if (isVideoMethod.invoke(this) as Boolean) return@after
            val btnId = Utils.getResId("menu_media_browser", "id")
            val btn = Utils.appActivity.findViewById<ActionMenuItemView>(btnId)
            btn.setOnLongClickListener {
                val url = imageUriField.get(this) as Uri? ?: return@setOnLongClickListener false
                val intent = Intent(Intent.ACTION_VIEW, settings.engine.getUrl(url))
                startActivity(intent)
                true
            }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}

@Suppress("unused")
enum class Engine(val urlTemplate: String) {
    GOOGLE("https://www.google.com/searchbyimage?image_url=%s"),
    TIN_EYE("https://www.tineye.com/search?url=%s"),
    YANDEX("https://yandex.com/images/search?url=%s&rpt=imageview"),
    BING("https://www.bing.com/images/search?q=imgurl:%s&view=detailv2&iss=sbi&FORM=IRSBIQ");

    val niceName: String
        get() {
            val sb = StringBuilder()
            for (oneString in this.name.lowercase().split("_").toTypedArray()) {
                sb.append(oneString.substring(0, 1).uppercase())
                sb.append(oneString.substring(1))
            }
            return sb.toString()
        }

    fun getUrl(url: Uri): Uri = Uri.parse(
        urlTemplate.replace(
            "%s",
            URLEncoder.encode(url.toString(), StandardCharsets.UTF_8.toString())
        )
    )

    companion object {
        fun fromOrdinal(ordinal: Int): Engine? {
            return values().find { it.ordinal == ordinal }
        }
    }
}