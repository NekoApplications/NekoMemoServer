package net.zhuruoling.nekomemo.http.plugins

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import net.zhuruoling.nekomemo.config.Config
import net.zhuruoling.nekomemo.http.data.ContentType
import net.zhuruoling.nekomemo.http.data.HttpResponse
import net.zhuruoling.nekomemo.http.data.Responses
import net.zhuruoling.nekomemo.security.SessionManager
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
        route("/auth"){
            post {
                val text = call.receiveText()
                if (text.isEmpty()){
                    call.respond(HttpResponse(Responses.ACCESS_TOKEN_NOT_PROVIDED))
                    return@post
                }
                if (verifyAccessToken(text)){
                    val session = SessionManager.createNewSession()
                    call.respond(HttpResponse(Responses.ACCESS_TOKEN_VERIFIED, null, ContentType.SESSION, session.toJson()))
                    return@post
                }
                call.respond(HttpResponse(Responses.ACCESS_TOKEN_MISMATCH))
            }
        }
    }
}
