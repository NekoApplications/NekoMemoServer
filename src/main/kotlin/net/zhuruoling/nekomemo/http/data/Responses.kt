package net.zhuruoling.nekomemo.http.data

import kotlinx.serialization.Serializable

@Serializable
enum class Responses(val description: String, val success: Boolean) {
    ACCESS_TOKEN_MISMATCH("accessToken does not match.", false),
    ACCESS_TOKEN_NOT_PROVIDED("Require accessToken.", false),
    ACCESS_TOKEN_VERIFIED("Verified access token.", true),

    SESSION_EXPIRED("AccessToken expired.", false),
    SESSION_NOT_EXIST("AccessToken not exist.", false),
    SESSION_VALIDATED("Validate passed.", true),

    REQUIRE_AUTHORIZATION("Auth required", false),

    REQUIRE_FILE_NAME("File name required.", false)
}