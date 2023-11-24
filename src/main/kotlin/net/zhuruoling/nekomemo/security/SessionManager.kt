package net.zhuruoling.nekomemo.security

import io.ktor.server.sessions.*
import net.zhuruoling.nekomemo.config.Config

object SessionManager {
    private val sessions = mutableMapOf<String, Session>()
    fun createNewSession(): Session {
        val sessionId = generateSessionId()
        val (pub, priv) = generateKeyPair(seed = generateRandomSeed())
        val ks = SessionKeyStore(pub, priv)
        val session = Session(sessionId, System.currentTimeMillis() + Config.sessionTimeOut, ks)
        sessions += sessionId to session
        return session
    }

    fun removeExpiredSessions() {
        synchronized(sessions) {
            val removed = mutableListOf<String>()
            sessions.forEach {
                if (it.value.sessionTimeout < System.currentTimeMillis()) {
                    removed += it.key
                }
            }
            removed.forEach(sessions::remove)
        }
    }

    fun validateSession(sessionId: String): Pair<ValidateResult, Session?> {
        return synchronized(sessions) {
            if ((sessions[sessionId]
                    ?: return ValidateResult.NOT_EXIST to null).sessionTimeout < System.currentTimeMillis()
            ) {
                ValidateResult.PASS to sessions[sessionId]
            } else {
                removeExpiredSessions()
                ValidateResult.EXPIRED to sessions[sessionId]
            }
        }
    }
}

data class Session(val sessionId: String, val sessionTimeout: Long = 3600, val keyStore: SessionKeyStore)

class SessionKeyStore(
    val serverPublicKey: ByteArray,
    val serverPrivateKey: ByteArray,
    var clientPublicKey: ByteArray = ByteArray(0),
    var gotPublicKeyFromClient: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SessionKeyStore) return false
        if (!serverPublicKey.contentEquals(other.serverPublicKey)) return false
        if (!serverPrivateKey.contentEquals(other.serverPrivateKey)) return false
        if (!clientPublicKey.contentEquals(other.clientPublicKey)) return false
        if (gotPublicKeyFromClient != other.gotPublicKeyFromClient) return false
        return true
    }

    override fun hashCode(): Int {
        var result = serverPublicKey.contentHashCode()
        result = 31 * result + serverPrivateKey.contentHashCode()
        result = 31 * result + clientPublicKey.contentHashCode()
        result = 31 * result + gotPublicKeyFromClient.hashCode()
        return result
    }
}

enum class ValidateResult {
    PASS, EXPIRED, NOT_EXIST
}