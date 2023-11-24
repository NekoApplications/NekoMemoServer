package net.zhuruoling.nekomemo.http.data

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val name: String, val message: String, val stackTraceElements: List<String>)

@Serializable
data class HttpResponse(val code: Responses, val error: ErrorResponse? = null, val contentType: ContentType = ContentType.EMPTY, val content: String = "")

fun Throwable.toResponse(): ErrorResponse {
    return ErrorResponse(this.javaClass.toString(), this.message ?: "", buildList {
        this@toResponse.stackTrace.forEach {
            this += it.toString()
        }
    })
}