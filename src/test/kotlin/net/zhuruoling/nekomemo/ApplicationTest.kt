package net.zhuruoling.nekomemo

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import net.zhuruoling.nekomemo.http.plugins.configureRouting
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
        }
        createClient {

        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}
