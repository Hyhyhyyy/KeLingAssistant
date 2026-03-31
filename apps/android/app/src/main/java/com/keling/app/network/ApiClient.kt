package com.keling.app.network

/**
 * API客户端 - 用于与后端服务器通信
 * 实现用户认证、数据同步等功能
 */

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * API配置
 */
object ApiConfig {
    // 后端服务器地址 - 生产环境
    const val BASE_URL = "https://keling-server.onrender.com/api"

    // 开发调试时可使用本地地址
    // 真机上需要改成电脑的局域网IP，如 http://192.168.1.100:3001
    // const val BASE_URL = "http://10.0.2.2:3001/api"  // Android模拟器访问本机

    // 也可以动态设置
    var customBaseUrl: String? = null

    val actualBaseUrl: String
        get() = customBaseUrl ?: BASE_URL
}

/**
 * HTTP客户端单例
 */
object KelingHttpClient {
    val client: io.ktor.client.HttpClient by lazy {
        io.ktor.client.HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 10_000
            }

            defaultRequest {
                url(ApiConfig.actualBaseUrl)
            }
        }
    }
}

// ==================== 请求/响应数据类 ====================

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val message: String? = null,
    val token: String? = null,
    val user: UserResponse? = null,
    val error: String? = null
)

@Serializable
data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val level: Int = 1,
    val exp: Int = 0,
    val energy: Int = 100,
    val crystals: Int = 10,
    val streakDays: Int = 0,
    val totalStudyMinutes: Int = 0,
    val lastCheckInDate: String? = null,
    val createdAt: String? = null
)

@Serializable
data class CourseResponse(
    val courses: List<CourseData>? = null,
    val course: CourseData? = null,
    val error: String? = null
)

@Serializable
data class CourseData(
    val id: String,
    val name: String,
    val code: String = "",
    val teacher: String = "",
    val schedule: String = "[]",
    val location: String = "",
    val themeColor: String = "#E8A87C",
    val masteryLevel: Double = 0.0,
    val plantStage: Int = 0,
    val planetStyleIndex: Int = 0,
    val totalStudyMinutes: Int = 0,
    val isArchived: Boolean = false,
    val studySessionCount: Int = 0
)

@Serializable
data class TaskResponse(
    val tasks: List<TaskData>? = null,
    val task: TaskData? = null,
    val error: String? = null,
    val rewards: RewardsData? = null
)

@Serializable
data class TaskData(
    val id: String,
    val title: String,
    val description: String = "",
    val type: String = "DAILY_CARE",
    val courseId: String? = null,
    val status: String = "PENDING",
    val priority: Int = 3,
    val estimatedMinutes: Int = 25,
    val actualMinutes: Int? = null,
    val rewardsEnergy: Int = 10,
    val rewardsCrystals: Int = 5,
    val rewardsExp: Int = 20,
    val scheduledAt: String? = null,
    val completedAt: String? = null,
    val createdAt: String? = null
)

@Serializable
data class RewardsData(
    val energy: Int = 0,
    val crystals: Int = 0,
    val exp: Int = 0
)

@Serializable
data class NoteResponse(
    val notes: List<NoteData>? = null,
    val error: String? = null
)

@Serializable
data class NoteData(
    val id: String,
    val title: String,
    val content: String,
    val sourceType: String = "USER_CREATED",
    val aiExplanation: String? = null,
    val tags: String = "[]",
    val reviewCount: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CheckInResponse(
    val message: String? = null,
    val streakDays: Int = 0,
    val rewards: RewardsData? = null,
    val isSpecial: Boolean = false,
    val specialReward: String? = null,
    val hasCheckedInToday: Boolean = false,
    val nextReward: RewardsData? = null,
    val error: String? = null
)

@Serializable
data class SyncData(
    val user: UserResponse? = null,
    val courses: List<CourseData>? = null,
    val tasks: List<TaskData>? = null,
    val notes: List<NoteData>? = null
)

// ==================== API服务类 ====================

/**
 * 认证API服务
 */
class AuthApi {
    private val client = KelingHttpClient.client

    /**
     * 用户注册
     */
    suspend fun register(name: String, email: String, password: String): Result<AuthResponse> {
        return try {
            val response = client.post("${ApiConfig.actualBaseUrl}/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(name, email, password))
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 用户登录
     */
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = client.post("${ApiConfig.actualBaseUrl}/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取当前用户信息
     */
    suspend fun getMe(token: String): Result<AuthResponse> {
        return try {
            val response = client.get("${ApiConfig.actualBaseUrl}/auth/me") {
                bearerAuth(token)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 课程API服务
 */
class CourseApi {
    private val client = KelingHttpClient.client

    suspend fun getAll(token: String): Result<CourseResponse> {
        return try {
            val response = client.get("${ApiConfig.actualBaseUrl}/courses") {
                bearerAuth(token)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun create(token: String, course: CourseData): Result<CourseResponse> {
        return try {
            val response = client.post("${ApiConfig.actualBaseUrl}/courses") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(course)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun update(token: String, courseId: String, course: CourseData): Result<CourseResponse> {
        return try {
            val response = client.put("${ApiConfig.actualBaseUrl}/courses/$courseId") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(course)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(token: String, courseId: String): Result<Unit> {
        return try {
            client.delete("${ApiConfig.actualBaseUrl}/courses/$courseId") {
                bearerAuth(token)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 任务API服务
 */
class TaskApi {
    private val client = KelingHttpClient.client

    suspend fun getAll(token: String): Result<TaskResponse> {
        return try {
            val response = client.get("${ApiConfig.actualBaseUrl}/tasks") {
                bearerAuth(token)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun create(token: String, task: TaskData): Result<TaskResponse> {
        return try {
            val response = client.post("${ApiConfig.actualBaseUrl}/tasks") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(task)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun complete(token: String, taskId: String, actualMinutes: Int?): Result<TaskResponse> {
        return try {
            val response = client.post("${ApiConfig.actualBaseUrl}/tasks/$taskId/complete") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(mapOf("actualMinutes" to actualMinutes))
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 签到API服务
 */
class CheckInApi {
    private val client = KelingHttpClient.client

    suspend fun checkIn(token: String): Result<CheckInResponse> {
        return try {
            val response = client.post("${ApiConfig.actualBaseUrl}/checkin") {
                bearerAuth(token)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStatus(token: String): Result<CheckInResponse> {
        return try {
            val response = client.get("${ApiConfig.actualBaseUrl}/checkin/status") {
                bearerAuth(token)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * API服务聚合类
 */
class ApiService {
    val auth: AuthApi = AuthApi()
    val courses: CourseApi = CourseApi()
    val tasks: TaskApi = TaskApi()
    val checkIn: CheckInApi = CheckInApi()
}

// ==================== 扩展函数 ====================

/**
 * 添加Bearer Token认证头
 */
private fun HttpRequestBuilder.bearerAuth(token: String) {
    header("Authorization", "Bearer $token")
}