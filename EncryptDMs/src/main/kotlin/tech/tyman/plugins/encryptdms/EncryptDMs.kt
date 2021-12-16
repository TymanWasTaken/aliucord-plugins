package tech.tyman.plugins.encryptdms

import android.content.Context
import android.view.View
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.discord.widgets.channels.list.WidgetChannelsListItemChannelActions
import com.lytefast.flexinput.R
import com.aliucord.wrappers.ChannelWrapper.Companion.id
import android.widget.TextView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.discord.databinding.WidgetChannelsListItemActionsBinding
import androidx.core.widget.NestedScrollView
import com.aliucord.Logger
import com.aliucord.Utils
import com.aliucord.api.CommandsAPI
import com.aliucord.api.PatcherAPI
import com.aliucord.api.SettingsAPI
import com.aliucord.entities.CommandContext
import com.aliucord.settings.delegate
import com.aliucord.utils.DimenUtils
import com.discord.api.commands.ApplicationCommandType
import com.discord.databinding.WidgetChatListActionsBinding
import com.discord.models.commands.ApplicationCommandOption
import com.discord.models.user.CoreUser
import com.discord.widgets.chat.list.actions.WidgetChatListActions
import javax.crypto.Cipher

@AliucordPlugin
class EncryptDMs : Plugin() {
    private var SettingsAPI.aesKeys: HashMap<String, String> by settings.delegate(HashMap())
    private var SettingsAPI.pubKeys: HashMap<String, String> by settings.delegate(HashMap())
    private var SettingsAPI.publicKey: String by settings.delegate("")
    private var SettingsAPI.privateKey: String by settings.delegate("")

    private fun SettingsAPI.getKey(channel: Long) = this.aesKeys[channel.toString()]
    private fun SettingsAPI.setKey(channel: Long, key: String) {
        this.aesKeys[channel.toString()] = key
        this.aesKeys = this.aesKeys
    }

    private fun SettingsAPI.getPubKey(user: Long) = this.pubKeys[user.toString()]
    private fun SettingsAPI.setPubKey(user: Long, key: String) {
        this.pubKeys[user.toString()] = key
        this.pubKeys = this.pubKeys
    }

    override fun start(context: Context) {
        patcher.patchDirectMessageChannelActions()
        patcher.patchMessageActions()

        commands.registerCommand(
                "sendpubkey",
                "Sends your public key in the chat, and if either private or public key are absent then regenerates them"
        ) {
            var publicKey = settings.publicKey
            if (publicKey == "" || settings.privateKey == "") {
                genRsaKeys().run {
                    settings.publicKey = public.asBase64()
                    settings.privateKey = private.asBase64()
                    publicKey = public.asBase64()
                }
            }
            return@registerCommand CommandsAPI.CommandResult(
                    "<enc:publickey>\n$publicKey",
                    null,
                    true
            )
        }

        commands.registerCommand(
                "sendkey",
                "Sends your generated channel key in the chat, encrypted with the other user's public key, so that the other user can decrypt and save it.",
                listOf(
                        Utils.createCommandOption(
                                type = ApplicationCommandType.STRING,
                                name = "channel id",
                                description = "The channel id of the DM",
                                required = true
                        ),
                        Utils.createCommandOption(
                                type = ApplicationCommandType.USER,
                                name = "user",
                                description = "The user to send this to",
                                required = true
                        )
                )
        ) {
            val userPubKey = settings.getPubKey(it.getRequiredUser("user").id)
                    ?: return@registerCommand CommandsAPI.CommandResult("This user's public key is not saved, ask them to run /sendpubkey, and then press and hold on their message to save their key!")
            val channelKey = settings.getKey(it.getRequiredString("channel id").toLong())
                    ?: return@registerCommand CommandsAPI.CommandResult("This DM does not have a key generated! Press and hold on the DM in the list, and generate the key.")

            return@registerCommand CommandsAPI.CommandResult(
                    "<enc:aeskey>\n${userPubKey.asPublicKey().encrypt(channelKey.asKey().encoded)}",
                    null,
                    true
            )
        }

//        commands.registerCommand(
//                "sendencrypted",
//                "aaaaaaa can decrypt and save it.",
//                listOf(
//                        Utils.createCommandOption(
//                                type = ApplicationCommandType.STRING,
//                                name = "text",
//                                description = "text",
//                                required = true
//                        ),
//                        Utils.createCommandOption(
//                                type = ApplicationCommandType.USER,
//                                name = "user",
//                                description = "The user to send this to",
//                                required = true
//                        )
//                )
//        ) {
//            val userPubKey = settings.getPubKey(it.getRequiredUser("user").id)
//                    ?: return@registerCommand CommandsAPI.CommandResult("This user's public key is not saved, ask them to run /sendpubkey, and then press and hold on their message to save their key!")
//
//            return@registerCommand CommandsAPI.CommandResult(
//                    "<enc:aeskey>\n${userPubKey.asPublicKey().encrypt(it.getRequiredString("text"))}",
//                    null,
//                    true
//            )
//        }
    }

    private fun PatcherAPI.patchDirectMessageChannelActions() {
        this.after<WidgetChannelsListItemChannelActions>("configureUI", WidgetChannelsListItemChannelActions.Model::class.java) {
            try {
                val channelId = (it.args[0] as WidgetChannelsListItemChannelActions.Model).channel.id
                if (settings.getKey(channelId) != null) return@after // If key already generated then ignore the rest of this
                val nestedScrollView = this.requireView() as NestedScrollView
                val layout = nestedScrollView.getChildAt(0) as LinearLayout
                val binding = com.discord.widgets.channels.list.WidgetChannelsListItemChannelActions::class.java.getDeclaredMethod("getBinding")
                        .apply { isAccessible = true }
                        .invoke(this) as WidgetChannelsListItemActionsBinding
                val view = binding.j
                val param = view.layoutParams
                val params = LinearLayout.LayoutParams(param.width, param.height)
                params.leftMargin = DimenUtils.dpToPx(20)
                val tw = TextView(view.context, null, 0, R.i.UiKit_Settings_Item_Icon)
                tw.text = "Setup encrypted DMs (EncryptDMs)"
                tw.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(view.context, R.e.avd_show_password), null, null, null)
                tw.layoutParams = view.layoutParams
                tw.id = View.generateViewId()
                tw.setOnClickListener {
                    val key = genAesKey().asHex()
                    settings.setKey(channelId, key)
                    Utils.showToast("Key saved for channel. To send this key to the other user, tell the other user to run the /sendpubkey command, press and hold their message, and then run the /sendkey command.")
                    this.dismiss()
                }
                layout.addView(tw)
            } catch (e: Exception) {
                logger.error(e) // Patcher extension error catching errors itself lmaoS
            }
        }
    }

    private fun PatcherAPI.patchMessageActions() {
        this.after<WidgetChatListActions>("configureUI", WidgetChatListActions.Model::class.java) {
            try {
                val userId = CoreUser((it.args[0] as WidgetChatListActions.Model).message.author).id
                val content = (it.args[0] as WidgetChatListActions.Model).message.content
                if (
                        !(
                                content.startsWith("<enc:publickey>\n") ||
                                content.startsWith("<enc:aeskey>\n")
                        )
                ) return@after // If message is not relevant ignore it
                val nestedScrollView = this.requireView() as NestedScrollView
                val layout = nestedScrollView.getChildAt(0) as LinearLayout
                val binding = WidgetChatListActions::class.java.getDeclaredMethod("getBinding")
                        .apply { isAccessible = true }
                        .invoke(this) as WidgetChatListActionsBinding
                val view = binding.j
                val param = view.layoutParams
                val params = LinearLayout.LayoutParams(param.width, param.height)
                params.leftMargin = DimenUtils.dpToPx(20)
                val tw = TextView(view.context, null, 0, R.i.UiKit_Settings_Item_Icon)
                tw.text = when (content.split("\n")[0]) {
                    "<enc:publickey>" -> "Save public key for user (EncryptDMs)"
                    "<enc:aeskey>" -> "Save channel key from user (EncryptDMs)"
                    else -> "This should never happen wtf"
                }
                tw.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(view.context, R.e.avd_show_password), null, null, null)
                tw.layoutParams = view.layoutParams
                tw.id = View.generateViewId()
                tw.setOnClickListener {
                    when (content.split("\n")[0]) {
                        "<enc:publickey>" -> {
                            val key = content.split("<enc:publickey>\n")[1]
                            settings.setPubKey(userId, key)
                            Utils.showToast("Public key saved for user.")
                        }
                        "<enc:aeskey>" -> {
                            val encryptedText = content.split("<enc:aeskey>\n")[1]
                            val key = settings.privateKey.asPrivateKey()
                            Utils.showToast("Decrypted text: ${key.decrypt(encryptedText)}", true)
                        }
                        else -> {}
                    }
                    this.dismiss()
                }
                layout.addView(tw)
            } catch (e: Exception) {
                logger.error(e) // Patcher extension error catching errors itself lmaoS
            }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
