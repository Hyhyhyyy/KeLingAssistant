package com.keling.app.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * =========================
 * 请求体
 * =========================
 */
@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7
)

/**
 * 单条消息
 */
@Serializable
data class ChatMessage(
    val role: String,      // system / user / assistant
    val content: String
)

/**
 * =========================
 * 响应体（DeepSeek 返回）
 * =========================
 */
@Serializable
data class ChatResponse(
    val id: String? = null,

    val choices: List<Choice> = emptyList()
)

/**
 * choice 节点
 */
@Serializable
data class Choice(
    val index: Int? = null,

    val message: ChatMessage? = null,

    @SerialName("finish_reason")
    val finishReason: String? = null
)

