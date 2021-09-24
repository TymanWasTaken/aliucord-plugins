package tech.tyman.plugins

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.aliucord.CollectionUtils
import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.aliucord.patcher.PinePatchFn
import tech.tyman.plugins.translate.*
import com.discord.api.commands.ApplicationCommandType
import com.discord.databinding.WidgetChatListActionsBinding
import com.discord.models.commands.ApplicationCommandOption
import com.discord.utilities.textprocessing.node.EditedMessageNode
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.widgets.chat.list.WidgetChatList
import com.discord.widgets.chat.list.actions.WidgetChatListActions
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.MessageEntry
import com.facebook.drawee.span.DraweeSpanStringBuilder
import com.lytefast.flexinput.R
import org.json.JSONArray
import top.canyie.pine.Pine.CallFrame
import java.lang.reflect.Field
import java.util.regex.Pattern

@AliucordPlugin
class Translate : Plugin() {
    lateinit var pluginIcon: Drawable
    private val translatedMessages = mutableMapOf<Long, TranslateSuccessData>()
    private var chatList: WidgetChatList? = null
    private val messageLoggerEditedRegex = Pattern.compile("(?:.+ \\(.+: .+\\)\\n)+(.+)\$")

    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    override fun load(ctx: Context) {
        pluginIcon = ContextCompat.getDrawable(ctx, R.d.ic_locale_24dp)!!
    }

    override fun start(context: Context) {
        patchMessageContextMenu(context)
        patchProcessMessageText()
        commands.registerCommand(
                "translate",
                "Translates text from one language to another, sends by default",
                listOf(
                        ApplicationCommandOption(ApplicationCommandType.STRING, "text", "The text to translate", null, true, true, null, null, null),
                        ApplicationCommandOption(ApplicationCommandType.STRING, "to", "The language to translate to (default en, must be a language code described in plugin settings)", null, false, true, null, languageCodeChoices, null),
                        ApplicationCommandOption(ApplicationCommandType.STRING, "from", "The language to translate from (default auto, must be a language code described in plugin settings)", null, false, true, null, languageCodeChoices, null),
                        ApplicationCommandOption(ApplicationCommandType.BOOLEAN, "send", "Whether or not to send the message in chat (default true)", null, false, true, null, null, null)
                )
        ) { ctx ->
            val translateData = translateMessage(
                ctx.getRequiredString("text"),
                ctx.getString("from"),
                ctx.getString("to")
            )
            if (translateData !is TranslateSuccessData) {
                with(translateData as TranslateErrorData) {
                    return@registerCommand CommandsAPI.CommandResult(
                        "$errorText ($errorCode)",
                        null,
                        false
                    )
                }
            }
            return@registerCommand CommandsAPI.CommandResult(
                translateData.translatedText,
                null,
                ctx.getBoolOrDefault("send", true)
            )
        }
    }

    private fun DraweeSpanStringBuilder.setTranslated(translateData: TranslateSuccessData, context: Context) {
        val contentStartIndex = messageLoggerEditedRegex.matcher(this.toString()).let {
            if (it.find()) {
                it.start(1)
            } else 0
        }
        this.replace(contentStartIndex, contentStartIndex + translateData.sourceText.length, translateData.translatedText)
        val textEnd = this.length
        this.append(" (translated: ${translateData.sourceLanguage} -> ${translateData.translatedLanguage})")
        this.setSpan(RelativeSizeSpan(0.75f), textEnd, this.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (textEnd != this.length) {
            this.setSpan(EditedMessageNode.Companion.`access$getForegroundColorSpan`(EditedMessageNode.Companion, context),
                    textEnd, this.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun patchProcessMessageText() {
        patcher.patch(WidgetChatList::class.java.getDeclaredConstructor(), PinePatchFn { callFrame: CallFrame -> chatList = callFrame.thisObject as WidgetChatList })

        val mDraweeStringBuilder: Field = SimpleDraweeSpanTextView::class.java.getDeclaredField("mDraweeStringBuilder")
        mDraweeStringBuilder.isAccessible = true
        patcher.patch(WidgetChatListAdapterItemMessage::class.java, "processMessageText", arrayOf(SimpleDraweeSpanTextView::class.java, MessageEntry::class.java), PinePatchFn { callFrame: CallFrame ->
            val messageEntry = callFrame.args[1] as MessageEntry
            val message = messageEntry.message ?: return@PinePatchFn
            val id = message.id
            val translateData = translatedMessages[id] ?: return@PinePatchFn
            if (translateData.sourceText != message.content) {
                translatedMessages.remove(id)
                return@PinePatchFn
            }
            val textView = callFrame.args[0] as SimpleDraweeSpanTextView
            val builder = mDraweeStringBuilder[textView] as DraweeSpanStringBuilder?
                    ?: return@PinePatchFn
            val context = textView.context
            builder.setTranslated(translateData, context)
            textView.setDraweeSpanStringBuilder(builder)
        })
    }

    private fun patchMessageContextMenu(ctx: Context) {
        val viewId = View.generateViewId()
        val messageContextMenu = WidgetChatListActions::class.java
        val getBinding = messageContextMenu.getDeclaredMethod("getBinding").apply { isAccessible = true }

        patcher.patch(messageContextMenu.getDeclaredMethod("configureUI", WidgetChatListActions.Model::class.java), PinePatchFn {
            val menu = it.thisObject as WidgetChatListActions
            val binding = getBinding.invoke(menu) as WidgetChatListActionsBinding
            val translateButton = binding.a.findViewById<TextView>(viewId)
            translateButton.setOnClickListener { e ->
                val message = (it.args[0] as WidgetChatListActions.Model).message
                Utils.threadPool.execute {
                    val response = translateMessage(message.content)
                    if (response !is TranslateSuccessData) {
                        with (response as TranslateErrorData) {
                            Utils.showToast(e.context, "$errorText ($errorCode)", true)
                            return@execute
                        }
                    }
                    translatedMessages[message.id] = response
                    if (chatList != null) {
                        val adapter = WidgetChatList.`access$getAdapter$p`(chatList)
                        val data = adapter.internalData
                        val i = CollectionUtils.findIndex(data) { m ->
                            m is MessageEntry && m.message.id == message.id
                        }
                        if (i != -1) adapter.notifyItemChanged(i)
                    }
                    Utils.showToast(ctx, "Translated message")
                    menu.dismiss()
                }
            }
        })

        patcher.patch(messageContextMenu, "onViewCreated", arrayOf(View::class.java, Bundle::class.java), PinePatchFn {
            val linearLayout = (it.args[0] as NestedScrollView).getChildAt(0) as LinearLayout
            val context = linearLayout.context
            linearLayout.addView(TextView(context, null, 0, R.h.UiKit_Settings_Item_Icon).apply {
                id = viewId
                text = "Translate message"
                setCompoundDrawablesRelativeWithIntrinsicBounds(pluginIcon, null, null, null)
            })
        })
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
        val translatedJsonReq = Http.Request(queryBuilder.toString(), "GET").apply {
            setHeader("Content-Type", "application/json")
            setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4592.0 Safari/537.36")
        }.execute()

        if (!translatedJsonReq.ok()) {
            return when (translatedJsonReq.statusCode) {
                429 -> TranslateErrorData(
                    errorCode = 429,
                    errorText = "Translate API ratelimit reached. Please try again later."
                )
                else -> TranslateErrorData(
                    errorCode = translatedJsonReq.statusCode,
                    errorText = "An unknown error occurred. Please report this to the developer of Translate."
                )
            }
        }
        val parsedJson = JSONArray(translatedJsonReq.text())

        return TranslateSuccessData(
                sourceLanguage = parsedJson.getString(2),
                translatedLanguage = toLang,
                sourceText = text,
                translatedText = parsedJson.getJSONArray(0).getJSONArray(0).getString(0)
        )
    }

//    private fun Message.guildId(): Long? {
//        val channel = ChannelWrapper(StoreStream.getChannels().getChannel(this.channelId))
//        return if (channel.isGuild()) channel.guildId else null
//    }
}