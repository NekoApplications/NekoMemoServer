package net.zhuruoling.nekomemo

import io.ktor.server.application.*
import net.zhuruoling.nekomemo.plugins.*
import net.zhuruoling.nekomemo.util.getVersionInfoString
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory


private val logger = LoggerFactory.getLogger("Main")

fun main(args: Array<String>) {
    val os = ManagementFactory.getOperatingSystemMXBean()
    val runtime = ManagementFactory.getRuntimeMXBean()
    logger.info("${getVersionInfoString()} is running on ${os.name} ${os.arch} ${os.version} at pid ${runtime.pid}")
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureSockets()
    configureRouting()
}
