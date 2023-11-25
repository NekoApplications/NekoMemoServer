package net.zhuruoling.nekomemo.http.data

import kotlinx.serialization.Serializable

@Serializable
enum class Responses(val description: String, val success: Boolean) {
    SERVER_INTERNAL_ERROR("Server Internal Error", false),

    ACCESS_TOKEN_MISMATCH("accessToken does not match.", false),
    ACCESS_TOKEN_NOT_PROVIDED("Require accessToken.", false),
    ACCESS_TOKEN_VERIFIED("Verified access token.", true),

    SESSION_EXPIRED("AccessToken expired.", false),
    SESSION_NOT_EXIST("AccessToken not exist.", false),
    SESSION_VALIDATED("Validate passed.", true),
    SESSION_CLIENT_KEY_UPDATED("Client key updated.", true),
    SESSION_DEACTIVATED("Successfully deactived session", true),

    REQUIRE_AUTHORIZATION("Auth required", false),
    REQUIRE_FILE_NAME("File name required.", false),

    FILE_NOT_EXIST("File not exist", false),
    FILE_UPDATE_SUCCESS("File upload success", true),
    FILE_DELETED("File deleted.", true),
    FILE_ALREADY_EXISTS("File already exists", false),
    FILE_LIST("Listing file matching pattern", true)
}