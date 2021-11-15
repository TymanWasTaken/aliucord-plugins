package tech.tyman.plugins.reverseimagesearch

import android.annotation.SuppressLint
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.discord.utilities.color.ColorCompat
import com.discord.views.CheckedSetting
import com.discord.views.RadioManager
import com.lytefast.flexinput.R

var SettingsAPI.engine: Engine
    get() = Engine.fromOrdinal(this.getInt("engine", Engine.GOOGLE.ordinal)) ?: Engine.GOOGLE
    set(value) = this.setInt("engine", value.ordinal)

@SuppressLint("SetTextI18n")
class SearchSettings(private val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View?) {
        super.onViewBound(view)
        setActionBarTitle("ReverseImageSearch")
        val ctx = requireContext()

        val radios = Engine.values().mapNotNull {
            Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.RADIO, it.niceName, null)
        }
        val radioManager = RadioManager(radios)
        val radioGroup = RadioGroup(ctx)

        radios.forEachIndexed { i, radio ->
            radio.e {
                settings.engine = Engine.fromOrdinal(i) ?: return@e
                radioManager.a(radio)
            }
            radioGroup.addView(radio)
            if (settings.engine.ordinal == i) radioManager.a(radio)
        }

        val helpText = TextView(ctx)
        helpText.linksClickable = true
        helpText.text =
                    "To reverse image search an image, open it and long press the \"Open in browser\" icon. " +
                    "Then, a reverse image search using then engine of your choice (you can choose this above)" +
                    "will open in a browser."
        helpText.setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorOnPrimary))

        addView(radioGroup)
        addView(helpText)
    }
}