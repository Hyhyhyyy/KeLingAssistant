package com.keling.app.update

import com.keling.app.network.ApiConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * 版本信息
 */
@Serializable
data class VersionInfo(
    val versionCode: Int,
    val versionName: String,
    val updateUrl: String,
    val updateLog: String,
    val fileSize: Long
)

/**
 * 版本检查响应
 */
@Serializable
data class VersionCheckResponse(
    val success: Boolean,
    val currentVersion: VersionInfo? = null,
    val latestVersion: VersionInfo,
    val needUpdate: Boolean,
    val forceUpdate: Boolean
)

/**
 * 版本更新服务
 * 负责检查版本更新、下载APK
 */
class UpdateService {

    private val client = HttpClient(Android) {
        install(io.ktor.client.plugins.HttpTimeout) {
            // 增加超时时间以应对服务器冷启动
            requestTimeoutMillis = 60_000  // 60秒
            connectTimeoutMillis = 30_000  // 30秒
            socketTimeoutMillis = 60_000   // 60秒
        }
    }

    /**
     * 检查版本更新
     * @param currentVersionCode 当前版本号
     * @param currentVersionName 当前版本名
     */
    suspend fun checkUpdate(
        currentVersionCode: Int,
        currentVersionName: String
    ): Result<VersionCheckResponse> {
        return try {
            val response = client.get("${ApiConfig.actualBaseUrl}/app/version") {
                parameter("versionCode", currentVersionCode)
                parameter("versionName", currentVersionName)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 下载APK
     * @param url 下载地址
     * @param onProgress 进度回调 (已下载字节数, 总字节数)
     */
    suspend fun downloadApk(
        url: String,
        onProgress: (downloaded: Long, total: Long) -> Unit = { _, _ -> }
    ): Result<ByteArray> {
        return try {
            val response = client.get(url)
            val contentLength = response.headers[HttpHeaders.ContentLength]?.toLong() ?: 0L

            val bytes = response.body<ByteArray>()
            onProgress(bytes.size.toLong(), contentLength)
            Result.success(bytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 关闭客户端
     */
    fun close() {
        client.close()
    }
}