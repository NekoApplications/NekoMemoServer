package net.zhuruoling.nekomemo.http.data

import io.ktor.util.*
import net.zhuruoling.nekomemo.util.toObject

enum class ContentType(val clazz: Class<*>) {
    EMPTY(Unit::class.java),
    RAW(String::class.java),
    STRING_ARRAY(Array<String>::class.java),
    SESSION(SessionData::class.java);

    companion object {
        fun <T> getObject(response: HttpResponse, clz: Class<T>): T {
            return response.content.decodeBase64String().toObject(clz) as T
        }
    }


}