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
import com.aliucord.Utils
import com.aliucord.api.CommandsAPI
import com.aliucord.api.PatcherAPI
import com.aliucord.api.SettingsAPI
import com.aliucord.settings.delegate
import com.aliucord.utils.DimenUtils
import com.discord.api.commands.ApplicationCommandType
import com.discord.databinding.WidgetChatListActionsBinding
import com.discord.models.user.CoreUser
import com.discord.stores.StoreStream
import com.discord.widgets.chat.list.actions.WidgetChatListActions
import java.io.File

@AliucordPlugin
class EncryptDMs : Plugin() {
    // AES keys
    private var SettingsAPI.aesKeys: HashMap<String, String> by settings.delegate(HashMap())

    private fun SettingsAPI.getKey(channel: Long) = this.aesKeys[channel.toString()]
    private fun SettingsAPI.setKey(channel: Long, key: String) {
        this.aesKeys[channel.toString()] = key
        this.aesKeys = this.aesKeys
    }

    // RSA keys (encryption)
    private var SettingsAPI.pubKeys: HashMap<String, String> by settings.delegate(HashMap())
    private var SettingsAPI.publicKey: String by settings.delegate("")
    private var SettingsAPI.privateKey: String by settings.delegate("")

    private fun SettingsAPI.getPubKey(user: Long) = this.pubKeys[user.toString()]
    private fun SettingsAPI.setPubKey(user: Long, key: String) {
        val keys = this.pubKeys
        keys[user.toString()] = key
        this.pubKeys = keys
    }

    // RSA keys (signing)
    private var SettingsAPI.pubSigningKeys: HashMap<String, String> by settings.delegate(HashMap())
    private var SettingsAPI.publicSigningKey: String by settings.delegate("")
    private var SettingsAPI.privateSigningKey: String by settings.delegate("")

    private fun SettingsAPI.getPubSigningKey(user: Long) = this.pubSigningKeys[user.toString()]
    private fun SettingsAPI.setPubSigningKey(user: Long, key: String) {
        val keys = this.pubSigningKeys
        keys[user.toString()] = key
        this.pubSigningKeys = keys
    }

    private val curChannel: Long
        get() = StoreStream.getChannelsSelected().id

    override fun start(context: Context) {
        // Patch context menus
        patcher.patchDirectMessageChannelActions()
        patcher.patchMessageActions()

        // Add commands
        commands.registerCommand(
                "sendpubkeys",
                "Sends your public keys (both encryption and signing) in the chat, and if either private or public keys are absent then regenerates them"
        ) {
            // Encryption keys
            var publicKey = settings.publicKey
            if (publicKey == "" || settings.privateKey == "") {
                genRsaKeys().run {
                    settings.publicKey = public.asBase64()
                    settings.privateKey = private.asBase64()
                    publicKey = public.asBase64()
                }
            }
            with (File.createTempFile("encryptdms", null, context.cacheDir)) {
                this.writeText(publicKey)
                it.addAttachment(
                        this.toURI().toASCIIString(),
                        "publickey.txt"
                )
            }
            // Signing keys
            var publicSigningKey = settings.publicSigningKey
            if (publicSigningKey == "" || settings.privateSigningKey == "") {
                genRsaKeys().run {
                    settings.publicSigningKey = public.asBase64()
                    settings.privateSigningKey = private.asBase64()
                    publicSigningKey = public.asBase64()
                }
            }
            with (File.createTempFile("encryptdms", null, context.cacheDir)) {
                this.writeText(publicSigningKey)
                it.addAttachment(
                        this.toURI().toASCIIString(),
                        "publicsigningkey.txt"
                )
            }
            return@registerCommand CommandsAPI.CommandResult(
                    "<enc:publickeys>",
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

            val encryptedText = userPubKey.asPublicKey().encrypt(channelKey.asKey().encoded)
            with (File.createTempFile("encryptdms", null, context.cacheDir)) {
                this.writeText(encryptedText)
                it.addAttachment(
                        this.toURI().toASCIIString(),
                        "aeskey.txt"
                )
            }
            var privateSigningKey = settings.privateSigningKey
            if (privateSigningKey == "" || settings.publicSigningKey == "") {
                genRsaKeys().run {
                    settings.publicSigningKey = public.asBase64()
                    settings.privateSigningKey = private.asBase64()
                    privateSigningKey = private.asBase64()
                }
            }
            with (File.createTempFile("encryptdms", null, context.cacheDir)) {
                this.writeText(privateSigningKey.asPrivateKey().sign(encryptedText))
                it.addAttachment(
                        this.toURI().toASCIIString(),
                        "signature.txt"
                )
            }

            return@registerCommand CommandsAPI.CommandResult(
                    "<enc:aeskey>",
                    null,
                    true
            )
        }
    }

    private fun PatcherAPI.patchDirectMessageChannelActions() {
        this.after<WidgetChannelsListItemChannelActions>("configureUI", WidgetChannelsListItemChannelActions.Model::class.java) {
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
                Utils.showToast("Key saved for channel. To send this key to the other user, tell the other user to run the /sendpubkey command, press and hold their message, and then run the /sendkey command.", true)
                this.dismiss()
            }
            layout.addView(tw)
        }
    }

    private fun PatcherAPI.patchMessageActions() {
        this.after<WidgetChatListActions>("configureUI", WidgetChatListActions.Model::class.java) {
            val message = (it.args[0] as WidgetChatListActions.Model).message
            val userId = CoreUser(message.author).id
            val content = message.content
            if (
                    !listOf(
                            "<enc:publickeys>",
                            "<enc:aeskey>"
                    ).contains(content)
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
            tw.text = when (content) {
                "<enc:publickeys>" -> "Save public keys for user (EncryptDMs)"
                "<enc:aeskey>" -> "Save channel key from user (EncryptDMs)"
                else -> "This should never happen wtf"
            }
            tw.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(view.context, R.e.avd_show_password), null, null, null)
            tw.layoutParams = view.layoutParams
            tw.id = View.generateViewId()
            tw.setOnClickListener {
                when (content) {
                    "<enc:publickeys>" -> Utils.threadPool.execute {
                        val key = message.getAttachmentText("publickey.txt") ?: return@execute
                        settings.setPubKey(userId, key)
                        val signingKey = message.getAttachmentText("publicsigningkey.txt") ?: return@execute
                        settings.setPubSigningKey(userId, signingKey)
                        Utils.showToast("Public keys saved for user.")
                        this.dismiss()
                    }
                    "<enc:aeskey>" -> Utils.threadPool.execute {
                        // Encrypted text
                        val encryptedText = message.getAttachmentText("aeskey.txt") ?: return@execute
                        // Signature verification
                        val signature = message.getAttachmentText("signature.txt") ?: return@execute
                        val publicSigningKey = settings.getPubSigningKey(userId)
                                ?: return@execute Utils.showToast("Public signing key for user was not found, make sure you have saved their public keys first!", true)
                        val verified = try {
                            publicSigningKey.asPublicKey().verify(encryptedText, signature)
                        } catch (e: Exception) {
                            logger.error("Verification error:", e)
                            Utils.showToast("Error occurred while verifying signature, message could be modified, not saving key", true)
                            return@execute
                        }
                        if (!verified) {
                            Utils.showToast("Signature verification failed, message could be modified, not saving key", true)
                            return@execute
                        }
                        val key = settings.privateKey.asPrivateKey()
                        settings.setKey(message.channelId, key.decrypt(encryptedText))
                        Utils.showToast("Key decrypted and saved for this channel.")
                        this.dismiss()
                    }
                }
            }
            layout.addView(tw)
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
        commands.unregisterAll()
    }
}
