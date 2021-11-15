package tech.tyman.plugins.reverseimagesearch

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.widgets.BottomSheet
import com.lytefast.flexinput.R

class EngineSheet(val url: Uri?, val settings: SettingsAPI) : BottomSheet() {
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        val context = view.context
        val textViews = Engine.values().mapNotNull {
            if (it == Engine.ASK) return@mapNotNull null
            TextView(context, null, 0, R.i.UiKit_Settings_Item_Icon).apply {
                val iconDrawable = ResourcesCompat
                    .getDrawable(resources, R.e.ic_open_in_browser_white_24dp, null)
                text = "Open in ${it.niceName}"
                setCompoundDrawablesRelativeWithIntrinsicBounds(iconDrawable, null, null, null)
                setOnClickListener { _ ->
                    val url = this@EngineSheet.url ?: return@setOnClickListener
                    val intent = Intent(Intent.ACTION_VIEW, it.getUrl(url))
                    startActivity(intent)
                }
            }
        }
        textViews.forEach {
            linearLayout.addView(it)
        }
    }
}