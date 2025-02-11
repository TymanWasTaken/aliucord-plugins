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
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Logger
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@AliucordPlugin
class ReverseImageSearch : Plugin() {
    lateinit var pluginIcon: Drawable

    init {
        settingsTab = SettingsTab(SearchSettings::class.java).withArgs(settings)
        needsResources = true
    }

    override fun load(ctx: Context) {
        pluginIcon = ResourcesCompat.getDrawable(
            resources,
            resources.getIdentifier("ic_baseline_image_search_24", "drawable", "com.aliucord.plugins"),
            null
        )!!
    }

    override fun start(context: Context) {
        val isVideoMethod = WidgetMedia::class.java.getDeclaredMethod("isVideo").apply {
            isAccessible = true
        }
        patcher.after<WidgetMedia>("onViewBoundOrOnResume") {
            if (isVideoMethod.invoke(this) as Boolean) return@after
            val btnId = Utils.getResId("menu_media_browser", "id")
            val btn = Utils.appActivity.findViewById<ActionMenuItemView>(btnId)
            btn.setOnLongClickListener {
                val url = mostRecentIntent.getStringExtra("INTENT_MEDIA_URL") ?: return@setOnLongClickListener false
                if (settings.engine == Engine.ASK) {
                    EngineSheet(url)
                        .show(Utils.appActivity.supportFragmentManager, "Reverse Image Search")
                } else {
                    val intent = Intent(Intent.ACTION_VIEW, settings.engine.getUrl(url))
                    startActivity(intent)
                }
                true
            }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}

@Suppress("unused")
enum class Engine(val urlTemplate: String?, val niceName: String) {
    ASK(null, "Ask every time"),
    GOOGLE("https://lens.google.com/uploadbyurl?url=%s", "Google Lens"),
    TIN_EYE("https://www.tineye.com/search?url=%s", "TinEye"),
    YANDEX("https://yandex.com/images/search?url=%s&rpt=imageview", "Yandex"),
    BING("https://www.bing.com/images/search?q=imgurl:%s&view=detailv2&iss=sbi&FORM=IRSBIQ", "Bing"),
    IQDB("https://iqdb.org/?url=%s", "IQDB"),
    SAUCE_NAO("https://saucenao.com/search.php?url=%s", "SauceNAO");

    fun getUrl(url: String): Uri {
        val uri = Uri.parse(
            urlTemplate?.format(
                URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
            )
        )

        return uri
    }

    companion object {
        fun fromOrdinal(ordinal: Int): Engine? {
            return values().find { it.ordinal == ordinal }
        }
    }
}
