package net.zhuruoling.nekomemo.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.zhuruoling.nekomemo.util.BuildProperties
import net.zhuruoling.nekomemo.util.getVersionInfoString

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
            get {
            }
        }
    }
}
