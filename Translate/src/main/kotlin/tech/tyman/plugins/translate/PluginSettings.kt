package tech.tyman.plugins.translate

import android.text.Editable
import android.text.util.Linkify
import android.view.View
import android.widget.TextView
import com.aliucord.views.TextInput
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.aliucord.views.Button
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.view.text.TextWatcher
import com.lytefast.flexinput.R


class PluginSettings(private val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View?) {
        super.onViewBound(view)
        setActionBarTitle("Translate")
        val ctx = requireContext()
        val input = TextInput(ctx, "Language to translate messages to by default")
        val editText = input.editText
        val button = Button(ctx)
        button.text = "Save"
        button.setOnClickListener {
            settings.setString("defaultLanguage", editText.text.toString())
            Utils.showToast("Saved translate settings!")
            close()
        }
        editText.maxLines = 1
        editText.setText(settings.getString("defaultLanguage", "en"))
        editText.addTextChangedListener(object : TextWatcher() {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (!languageCodes.containsValue(s.toString())) {
                    button.alpha = 0.5f
                    button.isClickable = false
                } else {
                    button.alpha = 1f
                    button.isClickable = true
                }
            }
        })
        val helpText = TextView(ctx)
        helpText.linksClickable = true
        helpText.text = "Supported language codes:\n\n${languageCodes.map { "${it.key} -> ${it.value}" }.joinToString("\n")}"
        Linkify.addLinks(helpText, Linkify.WEB_URLS)
        helpText.setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorOnPrimary))
        addView(input)
        addView(button)
        addView(helpText)
    }
}
