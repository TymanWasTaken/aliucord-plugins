@file:Suppress("MISSING_DEPENDENCY_CLASS", "MISSING_DEPENDENCY_SUPERCLASS")
package xyz.myriation.plugins.rolecolorcontrast

import android.view.View
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.discord.views.CheckedSetting

// TODO: Add example text
class PluginSettings(private val settings: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View?) {
        super.onViewBound(view)
        val ctx = requireContext()

        setActionBarTitle("RoleColorContrast")
        addView(
            Utils.createCheckedSetting(
                ctx,
                CheckedSetting.ViewType.SWITCH,
                "Use AAA requirements instead of AA requirements for text contrast",
                "AAA requirements are stricter, and require more contrast than the AA ones"
            ).apply {
                isChecked = settings.getBool("useAAA", false)
                setOnCheckedListener { settings.setBool("useAAA", it) }
            }
        )
    }
}