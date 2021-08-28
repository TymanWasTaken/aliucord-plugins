package com.aliucord.plugins

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.aliucord.patcher.PinePatchFn
import com.aliucord.plugins.translate.PluginSettings
import com.aliucord.plugins.translate.TranslateData
import com.discord.databinding.WidgetChatListActionsBinding
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
import com.discord.api.commands.ApplicationCommandType
import com.discord.models.commands.ApplicationCommandOption

class Translate : Plugin() {

    lateinit var pluginIcon: Drawable

    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    override fun getManifest() = Manifest().apply {
        authors = arrayOf(Manifest.Author("Tyman", 487443883127472129L))
        description = "Adds an option to translate messages."
        version = "1.1.2"
        updateUrl = "https://raw.githubusercontent.com/TymanWasTaken/aliucord-plugins/builds/updater.json"
        changelog =
                """
                    # Version 1.0.0
                    * Initial release
                    # Version 1.1.0
                    * Add /translate to translate text from one language to another, and send it in chat by default.
                    # Version 1.1.1
                    * Added settings to modify the default translation language
                    # Version 1.1.2
                    * Fixed message links not having /channels/
                    * Moved classes to com.aliucord.plugins.translate
                """.trimIndent()
    }

    override fun load(ctx: Context) {
        pluginIcon = ContextCompat.getDrawable(ctx, R.d.ic_locale_24dp)!!
    }

    override fun start(context: Context) {
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
Message link: https://discord.com/channels/${message.guildId()}/${message.channelId}/${message.id}""",
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
                    Utils.showToast(context, "Translated message")
                    menu.dismiss()
                }
            }
        })

        patcher.patch(messageContextMenu, "onViewCreated", arrayOf(View::class.java, Bundle::class.java), PinePatchFn {
            val linearLayout = (it.args[0] as NestedScrollView).getChildAt(0) as LinearLayout
            val ctx = linearLayout.context
            linearLayout.addView(TextView(ctx, null, 0, R.h.UiKit_Settings_Item_Icon).apply {
                id = viewId
                text = "Translate message"
                setCompoundDrawablesRelativeWithIntrinsicBounds(pluginIcon, null, null, null)
            })
        })

        commands.registerCommand(
                "translate",
                "Translates text from one language to another, sends by default",
                listOf(
                        ApplicationCommandOption(ApplicationCommandType.STRING, "text", "The text to translate", null, true, true, null, null),
                        ApplicationCommandOption(ApplicationCommandType.STRING, "to", "The language to translate to (default en, must be a language code)", null, false, true, null, null),
                        ApplicationCommandOption(ApplicationCommandType.STRING, "from", "The language to translate from (default auto, must be a language code)", null, false, true, null, null),
                        ApplicationCommandOption(ApplicationCommandType.BOOLEAN, "send", "Whether or not to send the message in chat (default true)", null, false, true, null, null)
                )
        ) { ctx ->
            return@registerCommand CommandsAPI.CommandResult(
                    translateMessage(
                            ctx.getRequiredString("text"),
                            ctx.getString("from"),
                            ctx.getString("to")
                    ).translatedText,
                    null,
                    ctx.getBoolOrDefault("send", true)
            )
        }
    }

    override fun stop(context: Context?) = patcher.unpatchAll()

    private fun translateMessage(text: String, from: String? = null, to: String? = null): TranslateData {
        val toLang = to ?: settings.getString("defaultLanguage", "en")
        val fromLang = from ?: "auto"
        val queryBuilder = Http.QueryBuilder("https://translate.googleapis.com/translate_a/single").run {
            append("client", "gtx")
            append("sl", fromLang)
            append("tl", toLang)
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
                translatedLanguage = toLang,
                sourceText = text,
                translatedText = parsedJson.getJSONArray(0).getJSONArray(0).getString(0)
        )
    }

    private fun Message.guildId(): Long? {
        val channel = ChannelWrapper(StoreStream.getChannels().getChannel(this.channelId))
        return if (channel.isGuild()) channel.guildId else null
    }
}