package net.zhuruoling.nekomemo.http.data

import kotlinx.serialization.Serializable
import net.zhuruoling.nekomemo.util.toJson
import net.zhuruoling.nekomemo.util.toObject

@Serializable
enum class Responses(val description: String, val success: Boolean) {
    ACCESS_TOKEN_MISMATCH("accessToken does not match.", false),
    ACCESS_TOKEN_NOT_PROVIDED("Require accessToken.", false),
    ACCESS_TOKEN_VERIFIED("Verified access token.", true)
}