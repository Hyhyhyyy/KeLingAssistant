package com.keling.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.keling.app.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

/**
 * 认证状态管理
 * 管理用户的登录状态、Token存储等
 */

// 认证相关的DataStore
private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_data")

/**
 * 认证状态
 */
@Serializable
sealed class AuthState {
    @Serializable
    data object Loading : AuthState()

    @Serializable
    data class Authenticated(
        val token: String,
        val userId: String,
        val email: String,
        val name: String
    ) : AuthState()

    @Serializable
    data object Unauthenticated : AuthState()
}

/**
 * 认证仓库
 */
class AuthRepository(private val context: Context) {

    private object Keys {
        val TOKEN = stringPreferencesKey("auth_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_NAME = stringPreferencesKey("user_name")
    }

    private val api = AuthApi()

    /**
     * 获取认证状态
     */
    fun getAuthState(): Flow<AuthState> {
        return context.authDataStore.data.map { prefs ->
            val token = prefs[Keys.TOKEN]
            if (token.isNullOrEmpty()) {
                AuthState.Unauthenticated
            } else {
                AuthState.Authenticated(
                    token = token,
                    userId = prefs[Keys.USER_ID] ?: "",
                    email = prefs[Keys.USER_EMAIL] ?: "",
                    name = prefs[Keys.USER_NAME] ?: ""
                )
            }
        }
    }

    /**
     * 获取Token
     */
    fun getToken(): Flow<String?> {
        return context.authDataStore.data.map { prefs ->
            prefs[Keys.TOKEN]
        }
    }

    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Flow<Boolean> {
        return context.authDataStore.data.map { prefs ->
            !prefs[Keys.TOKEN].isNullOrEmpty()
        }
    }

    /**
     * 用户注册
     */
    suspend fun register(name: String, email: String, password: String): Result<AuthResponse> {
        val result = api.register(name, email, password)

        if (result.isSuccess) {
            val response = result.getOrNull()
            if (response?.token != null && response.user != null) {
                saveAuthData(
                    token = response.token,
                    userId = response.user.id,
                    email = response.user.email,
                    name = response.user.name
                )
            }
        }

        return result
    }

    /**
     * 用户登录
     */
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        val result = api.login(email, password)

        if (result.isSuccess) {
            val response = result.getOrNull()
            if (response?.token != null && response.user != null) {
                saveAuthData(
                    token = response.token,
                    userId = response.user.id,
                    email = response.user.email,
                    name = response.user.name
                )
            }
        }

        return result
    }

    /**
     * 用户登出
     */
    suspend fun logout() {
        context.authDataStore.edit { prefs ->
            prefs.remove(Keys.TOKEN)
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.USER_EMAIL)
            prefs.remove(Keys.USER_NAME)
        }
    }

    /**
     * 保存认证数据
     */
    private suspend fun saveAuthData(token: String, userId: String, email: String, name: String) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.USER_ID] = userId
            prefs[Keys.USER_EMAIL] = email
            prefs[Keys.USER_NAME] = name
        }
    }

    /**
     * 更新Token
     */
    suspend fun updateToken(token: String) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
        }
    }

    /**
     * 获取当前用户ID
     */
    fun getCurrentUserId(): Flow<String?> {
        return context.authDataStore.data.map { prefs ->
            prefs[Keys.USER_ID]
        }
    }
}

/**
 * 同步服务
 * 负责本地数据与云端的同步
 */
class SyncService(private val context: Context) {

    private val apiService = ApiService()
    private val dataRepository = DataRepository(context)

    /**
     * 从云端拉取数据到本地
     */
    suspend fun pullFromCloud(token: String): Result<Unit> {
        return try {
            // 拉取课程数据
            val coursesResult = apiService.courses.getAll(token)
            if (coursesResult.isSuccess) {
                val courses = coursesResult.getOrNull()?.courses ?: emptyList()
                // 保存到本地
                dataRepository.courses.saveCourses(courses.map { it.toCourse() })
            }

            // 拉取任务数据
            val tasksResult = apiService.tasks.getAll(token)
            if (tasksResult.isSuccess) {
                val tasks = tasksResult.getOrNull()?.tasks ?: emptyList()
                dataRepository.tasks.saveTasks(tasks.map { it.toTask() })
            }

            // 拉取用户数据
            val userResult = apiService.auth.getMe(token)
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()?.user
                if (user != null) {
                    dataRepository.userPrefs.saveUser(user.toUser())
                }
            }

            // 更新同步时间
            dataRepository.userPrefs.updateLastSyncTime()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 推送本地数据到云端
     */
    suspend fun pushToCloud(token: String): Result<Unit> {
        return try {
            // 推送课程数据
            dataRepository.courses.getCourses().collect { courses ->
                for (course in courses) {
                    val courseData = course.toCourseData()
                    apiService.courses.create(token, courseData)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ==================== 转换扩展函数 ====================

/**
 * CourseData 转换为 Course
 */
fun CourseData.toCourse(): Course {
    return Course(
        id = id,
        name = name,
        code = code,
        teacher = teacher,
        location = location,
        themeColor = themeColor.removePrefix("#").toLong(16) or 0xFF000000,
        masteryLevel = masteryLevel.toFloat(),
        plantStage = plantStage,
        planetStyleIndex = planetStyleIndex,
        totalStudyMinutes = totalStudyMinutes,
        isArchived = isArchived,
        studySessionCount = studySessionCount
    )
}

/**
 * Course 转换为 CourseData
 */
fun Course.toCourseData(): CourseData {
    return CourseData(
        id = id,
        name = name,
        code = code,
        teacher = teacher,
        location = location,
        themeColor = "#${themeColor.toString(16).takeLast(6)}",
        masteryLevel = masteryLevel.toDouble(),
        plantStage = plantStage,
        planetStyleIndex = planetStyleIndex,
        totalStudyMinutes = totalStudyMinutes,
        isArchived = isArchived,
        studySessionCount = studySessionCount
    )
}

/**
 * TaskData 转换为 Task
 */
fun TaskData.toTask(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        type = TaskType.valueOf(type),
        courseId = courseId,
        status = TaskStatus.valueOf(status),
        priority = priority,
        estimatedMinutes = estimatedMinutes,
        actualMinutes = actualMinutes,
        rewards = Rewards(
            energy = rewardsEnergy,
            crystals = rewardsCrystals,
            exp = rewardsExp
        ),
        completedAt = completedAt?.let {
            try { it.toLong() } catch (e: Exception) { null }
        }
    )
}

/**
 * UserResponse 转换为 User
 */
fun UserResponse.toUser(): User {
    return User(
        id = id,
        name = name,
        level = level,
        exp = exp,
        energy = energy,
        crystals = crystals,
        streakDays = streakDays,
        totalStudyMinutes = totalStudyMinutes,
        lastCheckInDate = lastCheckInDate,
        createdAt = createdAt?.let {
            try { it.toLong() } catch (e: Exception) { System.currentTimeMillis() }
        } ?: System.currentTimeMillis()
    )
}