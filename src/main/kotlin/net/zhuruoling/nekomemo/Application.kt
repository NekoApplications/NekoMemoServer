package net.zhuruoling.nekomemo

import io.ktor.server.application.*
import net.zhuruoling.nekomemo.config.Config
import net.zhuruoling.nekomemo.filestore.FileStore
import net.zhuruoling.nekomemo.http.plugins.configureMonitoring
import net.zhuruoling.nekomemo.http.plugins.configureRouting
import net.zhuruoling.nekomemo.http.plugins.configureSerialization
import net.zhuruoling.nekomemo.http.plugins.configureSockets
import net.zhuruoling.nekomemo.util.getVersionInfoString
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import kotlin.io.path.Path


private val logger = LoggerFactory.getLogger("Main")

fun main(args: Array<String>) {
    val os = ManagementFactory.getOperatingSystemMXBean()
    val runtime = ManagementFactory.getRuntimeMXBean()
    logger.info("${getVersionInfoString()} is running on ${os.name} ${os.arch} ${os.version} at pid ${runtime.pid}")
    if(Config.load()){
        logger.info("Config created.")
        logger.info("New accessToken file is located at ${Path("./accessToken.txt").toAbsolutePath()}")
        logger.info("NekoMemoServer will not save this token but save a sha1 value to verify.")
    }
    FileStore.init()
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureMonitoring()
    configureSerialization()
    configureSockets()
    configureRouting()
}
