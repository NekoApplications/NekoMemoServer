package net.zhuruoling.nekomemo.security

import io.ktor.util.*
import net.zhuruoling.nekomemo.config.Config
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher


fun generateRandomSeed(): ByteArray {
    return System.currentTimeMillis().toString().encodeToByteArray()
}

fun generateKeyPair(length: Int = 2048, seed: ByteArray? = null): Pair<ByteArray, ByteArray> {
    val generator = KeyPairGenerator.getInstance("RSA")
    generator.initialize(
        length,
        SecureRandom.getInstance("DRBG", DrbgParameters.instantiation(128, DrbgParameters.Capability.RESEED_ONLY, seed))
    )
    val pair = generator.generateKeyPair()
    return pair.public.encoded to pair.private.encoded
}

fun generateAccessToken(): String {
    return generateNonce(64).map { it.toInt().toChar() }.joinToString("")
}

fun verifyAccessToken(content: String): Boolean {
    return Config.accessTokenHash == content
}

fun rsaEncrypt(content: ByteArray, key: ByteArray): ByteArray {
    val keySpec = X509EncodedKeySpec(key)
    val keyFactory = KeyFactory.getInstance("RSA")
    val publicKey = keyFactory.generatePublic(keySpec)
    val cipher = Cipher.getInstance("RSA/CBC/PKCS1Padding")
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    return cipher.doFinal(content)
}

fun rsaDecrypt(content: ByteArray, key: ByteArray): ByteArray {
    val keySpec = X509EncodedKeySpec(key)
    val keyFactory = KeyFactory.getInstance("RSA")
    val privateKey = keyFactory.generatePrivate(keySpec)
    val cipher = Cipher.getInstance("RSA/CBC/PKCS1Padding")
    cipher.init(Cipher.DECRYPT_MODE, privateKey)
    return cipher.doFinal(content)
}