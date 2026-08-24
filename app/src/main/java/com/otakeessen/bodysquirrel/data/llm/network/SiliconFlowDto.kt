package com.otakeessen.bodysquirrel.data.llm.network

import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val response_format: ResponseFormat? = null,
    val max_tokens: Int? = null
)

@Serializable
data class ResponseFormat(
    val type: String
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: List<ChatContentPart>
)

@Serializable
data class ChatContentPart(
    val type: String,
    val text: String? = null,
    val image_url: ImageUrlPart? = null
)

@Serializable
data class ImageUrlPart(
    val url: String,
    val detail: String = "low"
)

@Serializable
data class ChatCompletionResponse(
    val choices: List<ChatChoice> = emptyList()
)

@Serializable
data class ChatChoice(
    val message: ChatResponseMessage? = null
)

@Serializable
data class ChatResponseMessage(
    val role: String? = null,
    val content: String? = null
)

