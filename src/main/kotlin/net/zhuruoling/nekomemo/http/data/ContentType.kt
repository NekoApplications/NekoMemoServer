package net.zhuruoling.nekomemo.http.data

import net.zhuruoling.nekomemo.security.Session
import net.zhuruoling.nekomemo.util.toObject

enum class ContentType(val clazz: Class<*>) {
    EMPTY(Unit::class.java),
    RAW(String::class.java),
    SESSION(Session::class.java);

    fun <T> getObject(response: HttpResponse): T {
        return response.content.toObject(clazz as Class<out T>)
    }
}