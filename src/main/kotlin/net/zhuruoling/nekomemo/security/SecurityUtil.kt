package net.zhuruoling.nekomemo.security

import io.ktor.util.*
import net.zhuruoling.nekomemo.config.Config
import java.security.DrbgParameters
import java.security.KeyPairGenerator
import java.security.SecureRandom

fun generateRandomSeed(): ByteArray {
    return System.currentTimeMillis().toString().encodeToByteArray()
}

fun generateKeyPair(length: Int = 2048, seed: ByteArray? = null): Pair<ByteArray, ByteArray> {
    val generator = KeyPairGenerator.getInstance("RSA")
    generator.initialize(length, SecureRandom.getInstance("DRBG", DrbgParameters.instantiation(128, DrbgParameters.Capability.RESEED_ONLY, seed)))
    val pair = generator.generateKeyPair()
    return pair.public.encoded to pair.private.encoded
}

fun generateAccessToken(): String {
    return generateNonce(64).map { it.toInt().toChar() }.joinToString("")
}

fun verifyAccessToken(content: String): Boolean {
    return Config.accessTokenHash == content
}