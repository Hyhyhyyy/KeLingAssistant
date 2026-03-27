package com.keling.app.ai

import android.util.Log
import com.keling.app.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * 负责与 DeepSeek 云端进行 HTTP 通讯的纯粹客户端。
 *
 * - 不包含任何业务逻辑，只做请求与错误包装
 * - 供上层的 AICoordinator / SimpleAIService 复用
 */
class DeepSeekClient {

    private val tag = "DeepSeekClient"

    private val baseUrl = "https://api.deepseek.com"
    private val model = "deepseek-chat"
    private val apiKey: String = BuildConfig.DEEPSEEK_API_KEY

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
        }

        expectSuccess = false

        defaultRequest {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun chat(systemPrompt: String, userPrompt: String): String {
        val request = ChatRequest(
            model = model,
            messages = listOf(
                ChatMessage(role = "system", content = systemPrompt),
                ChatMessage(role = "user", content = userPrompt)
            )
        )

        val response: HttpResponse = client.post("$baseUrl/v1/chat/completions") {
            setBody(request)
        }

        if (response.status != HttpStatusCode.OK) {
            val body = response.bodyAsText()
            Log.e(tag, "HTTP ${response.status.value}: $body")
            throw RuntimeException("HTTP ${response.status.value}: $body")
        }

        val body: ChatResponse = response.body()
        return body.choices.firstOrNull()?.message?.content
            ?: throw RuntimeException("AI返回为空")
    }

    fun close() {
        client.close()
    }
}

