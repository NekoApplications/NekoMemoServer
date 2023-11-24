package net.zhuruoling.nekomemo.http.data

import io.ktor.util.*
import kotlinx.serialization.Serializable
import net.zhuruoling.nekomemo.security.Session

@Serializable
data class ErrorResponse(val name: String, val message: String, val stackTraceElements: List<String>)

@Serializable
data class HttpResponse(val code: Responses, val error: ErrorResponse? = null, val contentType: ContentType = ContentType.EMPTY, val content: String = "")

fun Throwable.toResponse(): ErrorResponse {
    return ErrorResponse(this.javaClass.toString(), this.message ?: "", buildList {
        this@toResponse.stackTrace.forEach {
            this += it.toString()
        }
    })
}
@Serializable
data class SessionData(val sessionId: String, val publicKey: String){
    companion object{
        fun fromSession(session: Session): SessionData {
            return SessionData(sessionId = session.sessionId, publicKey = session.keyStore.serverPublicKey.encodeBase64())
        }
    }
}
