package net.zhuruoling.nekomemo.http.plugins

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import net.zhuruoling.nekomemo.http.data.ContentType
import net.zhuruoling.nekomemo.http.data.HttpResponse
import net.zhuruoling.nekomemo.http.data.Responses
import net.zhuruoling.nekomemo.http.data.SessionData
import net.zhuruoling.nekomemo.security.SessionManager
import net.zhuruoling.nekomemo.security.ValidateResult
import net.zhuruoling.nekomemo.security.verifyAccessToken
import net.zhuruoling.nekomemo.util.BuildProperties
import net.zhuruoling.nekomemo.util.getVersionInfoString
import net.zhuruoling.nekomemo.util.toJson

fun Application.configureRouting() {
    routing {
        route("/version") {
            get {
                call.respondText(getVersionInfoString())
            }
            get("simple") {
                call.respondText(getVersionInfoString())
            }
            get("build") {
                call.respond(BuildProperties.map.apply {
                    this += "versionInfoString" to getVersionInfoString()
                })
            }
        }
        route("/session") {
            post("auth") {
                val text = call.receiveText()
                if (text.isEmpty()) {
                    call.respond(HttpResponse(Responses.ACCESS_TOKEN_NOT_PROVIDED))
                    return@post
                }
                if (verifyAccessToken(text)) {
                    val session = SessionManager.createNewSession()
                    call.respond(
                        HttpResponse(
                            Responses.ACCESS_TOKEN_VERIFIED,
                            null,
                            ContentType.SESSION,
                            session.toJson().encodeBase64()
                        )
                    )
                    return@post
                }
                call.respond(HttpResponse(Responses.ACCESS_TOKEN_MISMATCH))
            }
            get("validate") {
                withValidatedSession {
                    call.respond(HttpResponse(Responses.SESSION_VALIDATED))
                }
            }
        }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.withValidatedSession(block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit) {
    val authHeader =
        this.call.request.header("Session-Id") ?: return call.respond(HttpResponse(Responses.REQUIRE_AUTHORIZATION))
    val (result, session) = SessionManager.validateSession(authHeader)
    when (result) {
        ValidateResult.PASS -> {
            block(this)
        }
        ValidateResult.EXPIRED -> {
            call.respond(
                HttpResponse(
                    Responses.SESSION_EXPIRED,
                    contentType = ContentType.SESSION,
                    content = SessionData.fromSession(session!!).toJson().encodeBase64()
                )
            )
        }
        ValidateResult.NOT_EXIST -> {
            call.respond(HttpResponse(Responses.SESSION_NOT_EXIST))
        }
    }
}
