package net.zhuruoling.nekomemo.security

import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import net.zhuruoling.nekomemo.config.Config
import java.security.PublicKey
import kotlin.random.Random

object SessionManager {
    private val sessions = mutableMapOf<String, Pair<Session, SessionKeyStore>>()
    fun createNewSession(): Session {
        val sessionId = generateSessionId()
        val (pub, priv) = generateKeyPair(seed = generateRandomSeed())
        val session = Session(sessionId, Config.sessionTimeOut, pub.encodeBase64())
        val ks = SessionKeyStore(pub,priv)
        sessions += sessionId to (session to ks)
        return session
    }
}

@Serializable
data class Session(val sessionId: String, val sessionTimeout:Int = 3600, val pubKeyFwd: String)

internal class SessionKeyStore(
    val serverPublicKey: ByteArray,
    val serverPrivateKey: ByteArray,
    var clientPublicKey: ByteArray = ByteArray(0),
    var gotPublickKeyFromClient: Boolean = false
)
