package com.aliucord.plugins

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.entities.Plugin
import com.aliucord.patcher.PinePatchFn
import com.discord.databinding.WidgetChatListActionsBinding
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.message.LocalMessageCreatorsKt
import com.discord.utilities.time.ClockFactory
import com.discord.widgets.chat.list.actions.WidgetChatListActions
import com.lytefast.flexinput.R
import com.discord.stores.StoreMessages
import com.discord.api.message.MessageFlags
import com.aliucord.utils.ReflectUtils
import com.discord.api.message.MessageReference
import com.discord.models.message.Message
import com.discord.stores.StoreStream
import org.json.JSONArray
import com.aliucord.wrappers.ChannelWrapper

class Translate : Plugin() {
    override fun getManifest() = Manifest().apply {
        authors = arrayOf(Manifest.Author("Tyman", 487443883127472129L))
        description = "Adds an option to translate messages."
        version = "1.0.0"
        updateUrl = "https://raw.githubusercontent.com/TymanWasTaken/aliucord-plugins/builds/updater.json"
        changelog =
                """
                    # Version 1.0.0
                    - Initial release
                """.trimIndent()
    }

    override fun start(ctx: Context) {
        val icon = ctx.resources.getDrawable(R.d.ic_locale_24dp, null).mutate()
        val viewId = View.generateViewId()
        val messageContextMenu = WidgetChatListActions::class.java
        val getBinding = messageContextMenu.getDeclaredMethod("getBinding").apply { isAccessible = true }

        patcher.patch(messageContextMenu.getDeclaredMethod("configureUI", WidgetChatListActions.Model::class.java), PinePatchFn {
            val menu = it.thisObject as WidgetChatListActions
            val binding = getBinding.invoke(menu) as WidgetChatListActionsBinding
            val translateButton = binding.a.findViewById<TextView>(viewId)
            translateButton.setOnClickListener { _ ->
                val message = (it.args[0] as WidgetChatListActions.Model).message
                Utils.threadPool.execute {
                    val response = translateMessage(message.content)
                    val localMessage = LocalMessageCreatorsKt.createLocalMessage(
                            // AAAAAAAAAAAAAAAAAA why is trimIndent broken
"""Translated text: 
```
${response.translatedText}
```
Source language: ${response.sourceLanguage}
Translated language: ${response.translatedLanguage}
Message link: https://discord.com/${message.guildId()}/${message.channelId}/${message.id}""",
                            message.channelId,
                            Utils.buildClyde(
                                    "Translator",
                                    "https://cdn.discordapp.com/attachments/829790281565601899/880681034931380254/g_translate_white_48dp.png"
                            ),
                            null,
                            false,
                            false,
                            null,
                            null,
                            ClockFactory.get(),
                            null,
                            null,
                            null,
                            null,
                            null,
                            MessageReference(
                                    message.guildId(), // use extension function instead because discord is bad at coding
                                    message.channelId,
                                    message.id
                            ),
                            null
                    )
                    ReflectUtils.setField(Message::class.java, localMessage, "flags", MessageFlags.EPHEMERAL)
                    StoreMessages.`access$handleLocalMessageCreate`(StoreStream.getMessages(), localMessage)
                    Utils.showToast(ctx, "Translated message")
                    menu.dismiss()
                }
            }
        })

        patcher.patch(messageContextMenu, "onViewCreated", arrayOf(View::class.java, Bundle::class.java), PinePatchFn {
            val linearLayout = (it.args[0] as NestedScrollView).getChildAt(0) as LinearLayout
            val context = linearLayout.context
            icon.setTint(ColorCompat.getThemedColor(context, R.b.colorInteractiveNormal))
            linearLayout.addView(TextView(context, null, 0, R.h.UiKit_Settings_Item_Icon).apply {
                id = viewId
                text = "Translate message"
                setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
            })
        })
    }

    override fun stop(context: Context?) = patcher.unpatchAll()

    private fun translateMessage(text: String): TranslateData {
        val queryBuilder = Http.QueryBuilder("https://translate.googleapis.com/translate_a/single").run {
            append("client", "gtx")
            append("sl", "auto")
            append("tl", "en") // TODO: Don't hardcode this
            append("dt", "t")
            append("q", text)
        }
        val translatedJson = Http.Request(queryBuilder.toString(), "GET").apply {
            setHeader("Content-Type", "application/json")
            setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4592.0 Safari/537.36")
        }
                .execute()
                .text()
        val parsedJson = JSONArray(translatedJson)

        return TranslateData(
                sourceLanguage = parsedJson.getString(2),
                translatedLanguage = "en",
                sourceText = text,
                translatedText = parsedJson.getJSONArray(0).getJSONArray(0).getString(0)
        )
    }

    private fun Message.guildId(): Long? {
        val channel = ChannelWrapper(StoreStream.getChannels().getChannel(this.channelId))
        return if (channel.isGuild()) channel.guildId else null
    }
}