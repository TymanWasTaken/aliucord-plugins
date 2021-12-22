package tech.tyman.plugins.encryptdms

import com.aliucord.Http
import com.aliucord.Logger
import com.aliucord.Utils
import com.aliucord.wrappers.messages.AttachmentWrapper.Companion.filename
import com.aliucord.wrappers.messages.AttachmentWrapper.Companion.url
import com.discord.models.message.Message
import java.io.IOException

fun Message.getAttachmentText(name: String): String? {
    val url = this.attachments.find { attachment -> attachment.filename == name }?.url
            ?: run {
                Utils.showToast("Unable to find $name attachment for some reason", true)
                return null
            }
    return try {
        Http.simpleGet(url)
    } catch (e: IOException) {
        Logger("EncryptDMs").error(e)
        Utils.showToast("Unable to load $name attachment for some reason", true)
        return null
    }
}

fun Message.getAttachmentTextWithError(name: String): Pair<Boolean, String> {
    val url = this.attachments.find { attachment -> attachment.filename == name }?.url
            ?: run {
                return Pair(false, "Unable to find $name attachment for some reason")
            }
    return try {
        Pair(true, Http.simpleGet(url))
    } catch (e: IOException) {
        Logger("EncryptDMs").error(e)
        return Pair(false, "Unable to load $name attachment for some reason")
    }
}