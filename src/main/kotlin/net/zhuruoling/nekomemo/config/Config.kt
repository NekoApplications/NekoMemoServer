package net.zhuruoling.nekomemo.config

import io.ktor.util.*
import net.mamoe.yamlkt.Yaml
import net.zhuruoling.nekomemo.security.generateAccessToken
import java.io.Reader
import kotlin.io.path.*
import kotlin.properties.Delegates

object Config {
    var sessionTimeOut by Delegates.notNull<Int>()
    lateinit var serverName: String
    lateinit var accessTokenHash: String
    private val configPath = Path("./config.yaml")

    private fun writeAccessToken(token: String) {
        Path("accessToken.txt").apply {
            deleteIfExists()
            createFile()
            writer().use {
                it.write(token)
            }
        }
    }

    init {
        load()
    }

    fun load(): Boolean {
        var configCreated = false
        if (configPath.notExists() || (configPath.fileSize() <= 0)) {
            configPath.deleteIfExists()
            configPath.toFile().createNewFile()
            val token = generateAccessToken()
            val tokenHashString = sha1(token.toByteArray()).decodeToString().encodeBase64()
            configPath.writer().use {
                it.write(Yaml.encodeToString(buildMap {
                    this += "serverName" to "NekoMemoServer"
                    this += "accessTokenHash" to tokenHashString
                    this += "sessionTimeOut" to 36000
                }))
            }
            writeAccessToken(token)
            configCreated = true
        }
        val content = configPath.toFile().reader(Charsets.UTF_8).use(Reader::readText)
        val obj = Yaml.decodeAnyFromString(content)
        val token = generateAccessToken()
        val tokenHashString = sha1(token.toByteArray()).decodeToString().encodeBase64()
        if (obj is Map<*, *>) {
            serverName = obj["serverName"] as String? ?: run { configCreated = true; "NekoMemoServer" }
            accessTokenHash = obj["accessTokenHash"] as String? ?: run {
                writeAccessToken(token)
                configCreated = true
                tokenHashString
            }
            sessionTimeOut = obj["sessionTimeOut"] as Int? ?: run { configCreated = true; 36000 }
            save()
            return configCreated
        }
        serverName = "NekoMemoServer"
        accessTokenHash = tokenHashString
        sessionTimeOut = 36000
        save()
        return configCreated
    }

    fun save() {
        configPath.deleteIfExists()
        configPath.toFile().createNewFile()
        configPath.writer().use {
            it.write(Yaml.encodeToString(buildMap {
                this += "serverName" to serverName
                this += "accessTokenHash" to accessTokenHash
                this += "sessionTimeOut" to sessionTimeOut
            }))
        }
    }
}