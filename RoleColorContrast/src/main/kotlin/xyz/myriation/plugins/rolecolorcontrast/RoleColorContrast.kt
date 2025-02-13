@file:Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS")

package xyz.myriation.plugins.rolecolorcontrast

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.aliucord.PluginManager
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.discord.models.member.GuildMember
import com.discord.stores.StoreStream
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListAdapter
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.lytefast.flexinput.R
import kotlin.math.pow

enum class ContrastLevel {
    Failing, AA, AAA
}

@AliucordPlugin
class RoleColorContrast : Plugin() {
    lateinit var pluginIcon: Drawable

    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    @get:ColorInt
    val themerBackgroundColor by lazy {
        val classLoader = (PluginManager.plugins["Themer"] ?: return@lazy null)::class.java.classLoader!!
        val resourceManager =
            classLoader.loadClass("dev.vendicated.aliucordplugs.themer.ResourceManager").getDeclaredField("INSTANCE")
                .get(null)
        resourceManager::class.java.getDeclaredMethod("getColorForName", String::class.java)
            .invoke(resourceManager!!, "primary_dark_600") as Int?
    }

    override fun load(context: Context) {
        pluginIcon = ContextCompat.getDrawable(context, R.e.ic_accessibility_24dp)!!
    }

    @SuppressLint("SetTextI18n")
    override fun start(context: Context) {
        // Patch names in chat
        patcher.after<WidgetChatListAdapterItemMessage>("getAuthorTextColor", GuildMember::class.java) {
            it.result = coerceToContrast(it.result as Int, getBackgroundColor() ?: return@after)
        }
        // Patch names in guild member list
        patcher.after<ChannelMembersListAdapter.Item.Member>("getColor") {
            it.result = coerceToContrast(it.result as Int, getBackgroundColor() ?: return@after)
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }

    private fun getBackgroundColor(): Int? {
        val theme = StoreStream.getUserSettingsSystem().theme
        return when (theme) {
            "pureEvil" -> ContextCompat.getColor(Utils.appContext, R.c.black)
            "dark" -> themerBackgroundColor ?: ContextCompat.getColor(Utils.appContext, R.c.primary_dark_600)
            "light" -> ContextCompat.getColor(Utils.appContext, R.c.white)
            else -> return null
        }
    }

    private fun coerceToContrast(text: Int, background: Int): Int {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(text, hsl)
        val factor = if (calculateRelativeLuminance(background) < 0.5) 0.03f else -0.03f
        val comparator = if (settings.getBool("useAAA", false)) ContrastLevel.AAA else ContrastLevel.AA
        while (calculateContrast(
                ColorUtils.setAlphaComponent(ColorUtils.HSLToColor(hsl), 255), background
            ) < comparator
        ) {
            hsl[2] += factor
            // If we can't create full contrast, settle for the best we can
            if (hsl[2] > 1 || hsl[2] < 0) {
                hsl[2] = hsl[2].coerceIn(0f..1f)
                break
            }
        }
        return ColorUtils.HSLToColor(hsl)
    }

    private fun calculateContrast(text: Int, background: Int): ContrastLevel {
        val textLuminance = calculateRelativeLuminance(text)
        val backgroundLuminance = calculateRelativeLuminance(background)
        val (luminanceOne, luminanceTwo) = if (textLuminance >= backgroundLuminance) {
            Pair(textLuminance, backgroundLuminance)
        } else {
            Pair(backgroundLuminance, textLuminance)
        }

        val ratio = (luminanceOne + 0.05) / (luminanceTwo + 0.05)
        return when {
            ratio >= 7 -> ContrastLevel.AAA
            ratio >= 4.5 -> ContrastLevel.AA
            else -> ContrastLevel.Failing
        }
    }

    private fun calculateRelativeLuminance(color: Int): Double {
        val r = Color.red(color) / 255.0
        val g = Color.green(color) / 255.0
        val b = Color.blue(color) / 255.0

        return 0.2126 * if (r <= 0.03928) {
            r / 12.92
        } else {
            ((r + 0.055) / 1.055).pow(2.4)
        } + 0.7152 * if (g <= 0.03928) {
            g / 12.92
        } else {
            ((g + 0.055) / 1.055).pow(2.4)
        } + 0.0722 * if (b <= 0.03928) {
            b / 12.92
        } else {
            ((b + 0.055) / 1.055).pow(2.4)
        }
    }
}