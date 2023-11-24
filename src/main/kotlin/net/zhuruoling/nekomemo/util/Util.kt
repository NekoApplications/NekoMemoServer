package net.zhuruoling.nekomemo.util

import com.google.gson.GsonBuilder
import java.util.*

fun getVersionInfoString(): String {
    val version = BuildProperties["version"]
    val buildTimeMillis = BuildProperties["buildTime"]?.toLong() ?: 0L
    val buildTime = Date(buildTimeMillis)
    return "NekoApplications:NekoMemo Server $version (${BuildProperties["branch"]}:${
        BuildProperties["commitId"]?.substring(0, 7)
    } $buildTime)"
}

private val gson = GsonBuilder().serializeNulls().create()

fun Any.toJson():String{
    return gson.toJson(this)
}

fun <T> String.toObject(clazz: Class<out T>): T{
    return gson.fromJson(this, clazz)
}