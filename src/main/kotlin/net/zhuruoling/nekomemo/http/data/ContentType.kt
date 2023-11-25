package net.zhuruoling.nekomemo.http.data

import net.zhuruoling.nekomemo.util.toObject

enum class ContentType(val clazz: Class<*>) {
    EMPTY(Unit::class.java),
    RAW(String::class.java),
    STRING_ARRAY(Array<String>::class.java),
    SESSION(SessionData::class.java);


    fun <T> getObject(response: HttpResponse): T {
        return response.content.toObject(clazz as Class<out T>)
    }
}