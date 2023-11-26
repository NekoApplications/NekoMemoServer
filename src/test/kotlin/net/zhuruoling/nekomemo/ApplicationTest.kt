package net.zhuruoling.nekomemo

import io.ktor.client.*
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
import net.zhuruoling.nekomemo.util.toObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    val logger: Logger = LoggerFactory.getLogger("Test")
    lateinit var sessionId: String
    suspend fun ApplicationTestBuilder.get(
        route: String,
        block: suspend io.ktor.client.statement.HttpResponse.() -> Unit
    ) {
        block(client.get(route) {
            this.headers {
                this.append("Session-Id", sessionId)
            }
        })
    }

    suspend fun ApplicationTestBuilder.delete(
        route: String,
        block: suspend io.ktor.client.statement.HttpResponse.() -> Unit
    ) {
        block(client.delete(route) {
            this.headers {
                this.append("Session-Id", sessionId)
            }
        })
    }

    suspend fun ApplicationTestBuilder.post(
        route: String,
        body: String,
        block: suspend io.ktor.client.statement.HttpResponse.() -> Unit
    ) {
        block(client.post(route) {
            this.headers {
                this.append("Session-Id", sessionId)
            }
            this.setBody(body)
        })
    }

    @OptIn(InternalAPI::class)
    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
            FileStore.init()
        }
        client.get("/version").apply {
            logger.info(this.body<String>())
        }
        sessionId = client.post("/session/auth") {
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

        get("/session/validate") {
            val resp = this.body<String>().toObject(HttpResponse::class.java)
            assertEquals(Responses.SESSION_VALIDATED, resp.code)
            assertEquals(ContentType.EMPTY, resp.contentType)
        }

        post("/file/update", "test_content") {
            val resp = this.body<String>().toObject(HttpResponse::class.java)
            assertEquals(Responses.REQUIRE_FILE_NAME, resp.code)
        }

        post("/file/update?name=test_file&replace=true", "test_content") {
            val resp = this.body<String>().toObject(HttpResponse::class.java)
            assertEquals(Responses.FILE_UPDATE_SUCCESS, resp.code)
        }

        get("/file/list") {
            val resp = this.body<String>().toObject(HttpResponse::class.java)
            assertEquals(Responses.FILE_LIST, resp.code)
            assertEquals(ContentType.STRING_ARRAY, resp.contentType)
            val fileList = resp.content
                .decodeBase64String()
                .toObject(Array<String>::class.java)
                .toMutableList()
            logger.info("Files: " + fileList.joinToString(", "))
            assert("test_file" in fileList)
        }

        get("/file/fetch") {
            val resp = this.body<String>().toObject(HttpResponse::class.java)
            assertEquals(Responses.REQUIRE_FILE_NAME, resp.code)
        }

        get("/file/fetch?name=test_file") {
            val resp = this.body<String>()
            logger.info("fileContent: $resp")
            assertEquals("test_content", resp)
        }

        delete("/file/delete"){
            val resp = this.body<String>().toObject(HttpResponse::class.java)
            assertEquals(Responses.REQUIRE_FILE_NAME, resp.code)
        }

        delete("/file/delete?name=test_file"){
            val resp = this.body<String>().toObject(HttpResponse::class.java)
            assertEquals(Responses.FILE_DELETED, resp.code)
        }

        get("/file/list") {
            val resp = this.body<String>().toObject(HttpResponse::class.java)
            assertEquals(Responses.FILE_LIST, resp.code)
            assertEquals(ContentType.STRING_ARRAY, resp.contentType)
            val fileList = resp.content
                .decodeBase64String()
                .toObject(Array<String>::class.java)
                .toMutableList()
            logger.info("Files: " + fileList.joinToString(", "))
            assert("test_file" !in fileList)
        }

        get("/file/fetch?name=test_file") {
            val s = this.body<String>()
            logger.info("Fetch: $s")
            val resp = s.toObject(HttpResponse::class.java)
            assertEquals(Responses.FILE_NOT_EXIST, resp.code)
        }

        delete("/file/delete?name=test_file"){
            val resp = this.body<String>().toObject(HttpResponse::class.java)
            assertEquals(Responses.FILE_NOT_EXIST, resp.code)
        }

        get("/session/deactivate") {
            val resp = this.body<String>().toObject(HttpResponse::class.java)
            assertEquals(Responses.SESSION_DEACTIVATED, resp.code)
            assertEquals(ContentType.EMPTY, resp.contentType)
        }

        get("/session/validate") {
            val resp = this.body<String>().toObject(HttpResponse::class.java)
            assertEquals(Responses.SESSION_NOT_EXIST, resp.code)
            assertEquals(ContentType.EMPTY, resp.contentType)
        }
    }
}
