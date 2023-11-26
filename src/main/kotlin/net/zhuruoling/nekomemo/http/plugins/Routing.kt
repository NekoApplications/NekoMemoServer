package net.zhuruoling.nekomemo.http.plugins

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import net.zhuruoling.nekomemo.filestore.FileStore
import net.zhuruoling.nekomemo.http.data.*
import net.zhuruoling.nekomemo.security.Session
import net.zhuruoling.nekomemo.security.SessionManager
import net.zhuruoling.nekomemo.security.ValidateResult
import net.zhuruoling.nekomemo.security.verifyAccessToken
import net.zhuruoling.nekomemo.util.BuildProperties
import net.zhuruoling.nekomemo.util.getVersionInfoString
import net.zhuruoling.nekomemo.util.toJson
import java.io.FileNotFoundException

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
                    return@post call.respond(
                        HttpResponse(
                            Responses.ACCESS_TOKEN_VERIFIED,
                            null,
                            ContentType.SESSION,
                            SessionData.fromSession(session).toJson().encodeBase64()
                        )
                    )
                }
                call.respond(HttpResponse(Responses.ACCESS_TOKEN_MISMATCH))
            }
            post("uploadClientPublicKey") {
                withValidatedSession {
                    val key = call.receiveStream().readAllBytes()
                    SessionManager.updateClientPublicKey(it.sessionId, key)
                    call.respond(HttpResponse(Responses.SESSION_CLIENT_KEY_UPDATED))
                }
            }
            get("deactivate") {
                withValidatedSession {
                    try {
                        SessionManager.deactivateSession(it.sessionId)
                        call.respond(HttpResponse(Responses.SESSION_DEACTIVATED))
                    } catch (_: IllegalArgumentException) {
                        call.respond(HttpResponse(Responses.SESSION_NOT_EXIST))
                    }
                }
            }
            get("validate") {
                withValidatedSession {
                    call.respond(HttpResponse(Responses.SESSION_VALIDATED))
                }
            }
        }
        route("/file") {
            post("update") {
                // /file/update?name={string}&replace={boolean}
                withValidatedSession session@{ _ ->
                    val name = call.request.queryParameters["name"]
                        ?: return@session call.respond(HttpResponse(Responses.REQUIRE_FILE_NAME))
                    val replace = call.request.queryParameters["replace"].toBoolean()
                    try {
                        FileStore.createNewFile(name, replace) {
                            call.receiveStream().transferTo(it)
                        }
                        call.respond(HttpResponse(Responses.FILE_UPDATE_SUCCESS))
                    } catch (e: FileAlreadyExistsException) {
                        call.respond(HttpResponse(Responses.FILE_ALREADY_EXISTS))
                    }
                }
            }
            get("list") {
                // /file/list?pattern={string}
                withValidatedSession session@{
                    val pattern = call.request.queryParameters["pattern"]
                    call.respond(
                        HttpResponse(
                            Responses.FILE_LIST,
                            null,
                            ContentType.STRING_ARRAY,
                            SessionManager.encryptIfAvailable(
                                it.sessionId,
                                FileStore.filter(pattern).keys.toTypedArray().toJson().toByteArray()
                            ).encodeBase64()
                        )
                    )
                }
            }
            get("fetch") {
                // /file/fetch?name={string}
                withValidatedSession session@{ _ ->
                    val name = call.request.queryParameters["name"]
                        ?: return@session call.respond(HttpResponse(Responses.REQUIRE_FILE_NAME))
                    return@session try {
                        FileStore.useFile(name){
                            call.respondOutputStream {
                                it.transferTo(this)
                            }
                        }
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                        call.respond(HttpResponse(Responses.FILE_NOT_EXIST))
                    }
                }
            }
            delete("delete") {
                // /file/delete?name={string}
                withValidatedSession session@{ _ ->
                    val name = call.request.queryParameters["name"]
                        ?: return@session call.respond(HttpResponse(Responses.REQUIRE_FILE_NAME))
                    try {
                        FileStore.deleteFile(name)
                        call.respond(HttpResponse(Responses.FILE_DELETED))
                    } catch (e: FileNotFoundException) {
                        call.respond(HttpResponse(Responses.FILE_NOT_EXIST))
                    }
                }
            }
        }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.withValidatedSession(block: suspend PipelineContext<Unit, ApplicationCall>.(Session) -> Unit) {
    val authHeader =
        this.call.request.header("Session-Id") ?: return call.respond(HttpResponse(Responses.REQUIRE_AUTHORIZATION))
    val (result, session) = SessionManager.validateSession(authHeader)
    when (result) {
        ValidateResult.PASS -> {
            try {
                block(this, session!!)
            } catch (e: Throwable) {
                HttpResponse(
                    Responses.SERVER_INTERNAL_ERROR,
                    error = e.toResponse()
                )
            }
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
