package net.zhuruoling.nekomemo.security

import io.ktor.server.sessions.*
import kotlin.random.Random

object SessionManager {

    fun createNewSession(): Session{
        val sessionId = generateSessionId()
        TODO()
    }

}

data class Session(val sessionId: String)
