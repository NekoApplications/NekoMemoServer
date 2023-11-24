package net.zhuruoling.nekomemo

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import net.zhuruoling.nekomemo.http.plugins.configureRouting
import kotlin.test.*
import net.zhuruoling.nekomemo.plugins.*

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
            configureSecurity()
        }
        createClient {

        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }
}
