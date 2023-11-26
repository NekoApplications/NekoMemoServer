package net.zhuruoling.nekomemo

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.testing.*
import io.ktor.util.*
import net.zhuruoling.nekomemo.config.Config
import net.zhuruoling.nekomemo.filestore.FileStore
import net.zhuruoling.nekomemo.http.data.ContentType
import net.zhuruoling.nekomemo.http.data.HttpResponse
import net.zhuruoling.nekomemo.http.data.Responses
import net.zhuruoling.nekomemo.http.data.SessionData
import net.zhuruoling.nekomemo.http.plugins.configureRouting
import net.zhuruoling.nekomemo.http.plugins.configureSerialization
import net.zhuruoling.nekomemo.security.SessionManager
import net.zhuruoling.nekomemo.security.ValidateResult
import net.zhuruoling.nekomemo.security.generateAccessToken
import net.zhuruoling.nekomemo.util.toObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    val logger: Logger = LoggerFactory.getLogger("Test")
    fun validate(sessionId: String, expectVerifyResult: ValidateResult = ValidateResult.PASS) {
        val (result, _) = SessionManager.validateSession(sessionId)
        when (result) {
            ValidateResult.PASS -> {
                logger.info("session $sessionId verified.")
            }

            ValidateResult.EXPIRED -> {
                logger.info("session $sessionId expired.")
            }

            ValidateResult.NOT_EXIST -> {
                logger.info("session $sessionId not found.")
            }
        }
        assertEquals(expectVerifyResult,result)
    }

    @OptIn(InternalAPI::class)
    @Test
    fun testRoot() = testApplication {
//        val accessToken = generateAccessToken()
//        val atHash = sha1(accessToken.toByteArray()).decodeToString().encodeBase64()
//        Config.accessTokenHash = atHash
//        Config.serverName = "TestServer"
//        Config.sessionTimeOut = 360000
        application {
            configureRouting()
            FileStore.init()
        }
        client.get("/version").apply {
            logger.info(this.body<String>())
        }
        val sessionId = client.post("/session/auth") {
            this.body = Config.accessTokenHash
        }.run {
            val resp = this.body<String>().toObject(HttpResponse::class.java)
            logger.info(resp.toString())
            assertEquals(ContentType.SESSION, resp.contentType)
            val sessionData = resp.content.decodeBase64String().toObject(ContentType.SESSION.clazz) as SessionData
            logger.info(resp.content.decodeBase64String())
            logger.info("id: " + sessionData.sessionId)
            logger.info("publicKey: " + sessionData.publicKey)
            sessionData.sessionId
        }
        client.get("/session/validate") {
            this.headers.append("Session-Id", sessionId)
        }.run {
            val resp = this.body<String>().toObject(HttpResponse::class.java)
            assertEquals(Responses.SESSION_VALIDATED,resp.code)
            assertEquals(ContentType.EMPTY, resp.contentType)
        }
        client.get("/session/deactivate") {
            this.headers.append("Session-Id", sessionId)
        }.run {
            val resp = this.body<String>().toObject(HttpResponse::class.java)
            assertEquals(Responses.SESSION_DEACTIVATED,resp.code)
            assertEquals(ContentType.EMPTY, resp.contentType)
        }
        client.get("/session/validate") {
            this.headers.append("Session-Id", sessionId)
        }.run {
            val resp = this.body<String>().toObject(HttpResponse::class.java)
            assertEquals(Responses.SESSION_NOT_EXIST,resp.code)
            assertEquals(ContentType.EMPTY, resp.contentType)
        }
    }
}
