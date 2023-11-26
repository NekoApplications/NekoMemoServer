package net.zhuruoling.nekomemo

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.testing.*
import io.ktor.util.*
import net.zhuruoling.nekomemo.config.Config
import net.zhuruoling.nekomemo.filestore.FileStore
import net.zhuruoling.nekomemo.http.data.ContentType
import net.zhuruoling.nekomemo.http.data.HttpResponse
import net.zhuruoling.nekomemo.http.data.SessionData
import net.zhuruoling.nekomemo.http.plugins.configureRouting
import net.zhuruoling.nekomemo.http.plugins.configureSerialization
import net.zhuruoling.nekomemo.util.toObject
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @OptIn(InternalAPI::class)
    @Test
    fun testRoot() = testApplication {
        val logger = LoggerFactory.getLogger("Test")
        application {
            configureRouting()
            Config.load()
            FileStore.init()
        }
        createClient {

        }
        client.get("/version").apply {
            logger.info(this.body<String>())
        }
        client.post("/session/auth") {
            this.body = Config.accessTokenHash
        }.apply {
            val resp = this.body<String>().toObject(HttpResponse::class.java)
            assertEquals(resp.contentType, ContentType.SESSION)
            val sessionData = resp.content.decodeBase64String().toObject(ContentType.SESSION.clazz) as SessionData
            logger.info(resp.content.decodeBase64String())
            logger.info("id: " + sessionData.sessionId)
            logger.info("publicKey: " + sessionData.publicKey)
        }
    }
}
