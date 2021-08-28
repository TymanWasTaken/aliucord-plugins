package com.aliucord.plugins

import android.text.Editable
import android.view.View

import com.aliucord.views.TextInput

import com.aliucord.Utils

import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.aliucord.views.Button
import com.discord.utilities.view.text.TextWatcher


class PluginSettings(private val settings: SettingsAPI) : SettingsPage() {
    val regex = Regex("^[a-z]{2}\$")
    override fun onViewBound(view: View?) {
        super.onViewBound(view)
        setActionBarTitle("Translate")
        val ctx = requireContext()
        val input = TextInput(ctx)
        input.hint = "Language code to translate messages to by default (default en)"
        val editText = input.editText!!
        val button = Button(ctx)
        button.text = "Save"
        button.setOnClickListener {
            settings.setString("defaultLanguage", editText.text.toString())
            Utils.showToast(ctx, "Saved translate settings!")
            close()
        }
        editText.maxLines = 1
        editText.setText(settings.getString("defaultLanguage", "en"))
        editText.addTextChangedListener(object : TextWatcher() {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (!regex.matches(s.toString())) {
                    button.alpha = 0.5f
                    button.isClickable = false
                } else {
                    button.alpha = 1f
                    button.isClickable = true
                }
            }
        })
        addView(input)
        addView(button)
    }
}