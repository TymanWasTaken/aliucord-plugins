package tech.tyman.plugins.encryptdms

import android.util.Base64
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import android.R.attr.data




fun genAesKey(): SecretKey {
    val keyGenerator = KeyGenerator.getInstance("AES")
    keyGenerator.init(256)
    return keyGenerator.generateKey()
}

fun Key.asHex() = this.encoded.asHex()
fun ByteArray.asHex() = this.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
fun Key.asBase64() = Base64.encodeToString(this.encoded, Base64.DEFAULT)

fun String.asKey(): SecretKey {
    val encodedKey = this.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    return SecretKeySpec(encodedKey, 0, encodedKey.size, "AES")
}

fun genRsaKeys(): KeyPair {
    val generator = KeyPairGenerator.getInstance("RSA")
    generator.initialize(4096)
    return generator.generateKeyPair()
}

fun String.asPublicKey() =
        KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(Base64.decode(this, Base64.DEFAULT)))

fun String.asPrivateKey() =
        KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(Base64.decode(this, Base64.DEFAULT)))

fun PublicKey.encrypt(text: ByteArray): String {
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING")
    cipher.init(Cipher.PUBLIC_KEY, this)
    return Base64.encodeToString(cipher.doFinal(text), Base64.DEFAULT)
}

fun PrivateKey.decrypt(text: String): String {
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING")
    cipher.init(Cipher.PRIVATE_KEY, this)
    return cipher.doFinal(Base64.decode(text, Base64.DEFAULT)).asHex()
}

fun PrivateKey.sign(text: String): String {
    val signature = Signature.getInstance("SHA512WithRSA")
    signature.initSign(this)
    signature.update(text.toByteArray())
    return Base64.encodeToString(signature.sign(), Base64.DEFAULT)
}

fun PublicKey.verify(text: String, signature: String): Boolean {
    val signatureInstance = Signature.getInstance("SHA512WithRSA")
    signatureInstance.initVerify(this)
    signatureInstance.update(text.toByteArray())

    return signatureInstance.verify(Base64.decode(signature, Base64.DEFAULT))
}