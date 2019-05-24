@file:Suppress("NOTHING_TO_INLINE")
package com.fone.android.util

import android.os.Build
import com.fone.android.extension.toLeByteArray
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


fun getRSAPrivateKeyFromString(privateKeyPEM: String): PrivateKey {
    val striped = stripRsaPrivateKeyHeaders(privateKeyPEM)
    val keySpec = PKCS8EncodedKeySpec(Base64.decode(striped, 0))
    val kf = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        KeyFactory.getInstance("RSA")
    } else {
        KeyFactory.getInstance("RSA", "BC")
    }
    return kf.generatePrivate(keySpec)
}

private fun stripRsaPrivateKeyHeaders(privatePem: String): String {
    val strippedKey = StringBuilder()
    val lines = privatePem.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    lines.filter { line -> !line.contains("BEGIN RSA PRIVATE KEY") &&
        !line.contains("END RSA PRIVATE KEY") && !line.trim { it <= ' ' }.isEmpty() }
        .forEach { line -> strippedKey.append(line.trim { it <= ' ' }) }
    return strippedKey.toString().trim { it <= ' ' }
}

fun aesEncrypt(key: String, iterator: Long, code: String): String? {
    val keySpec = SecretKeySpec(Base64.decode(key), "AES")
    val iv = ByteArray(16)
    SecureRandom().nextBytes(iv)

    val pinByte = code.toByteArray() + (System.currentTimeMillis() / 1000).toLeByteArray() + iterator.toLeByteArray()
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iv))
    val result = cipher.doFinal(pinByte)
    return Base64.encodeBytes(iv.plus(result))
}