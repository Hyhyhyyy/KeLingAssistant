package com.keling.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * =========================
 * 数据持久化层
 * =========================
 *
 * 使用 DataStore 进行数据持久化
 * - 用户设置
 * - 用户数据
 * - 对话历史
 * - 学习进度
 */

// DataStore 扩展
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "keling_data")

/**
 * 用户偏好设置仓库
 */
class UserPreferencesRepository(private val context: Context) {

    // ==================== Keys ====================

    private object Keys {
        // 用户信息
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_LEVEL = intPreferencesKey("user_level")
        val USER_EXP = intPreferencesKey("user_exp")
        val USER_ENERGY = intPreferencesKey("user_energy")
        val USER_CRYSTALS = intPreferencesKey("user_crystals")
        val USER_STREAK_DAYS = intPreferencesKey("user_streak_days")
        val USER_TOTAL_STUDY_MINUTES = intPreferencesKey("user_total_study_minutes")
        val USER_CREATED_AT = longPreferencesKey("user_created_at")

        // 设置
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val AUTO_REMINDER = booleanPreferencesKey("auto_reminder")

        // API 配置
        val API_KEY = stringPreferencesKey("api_key")
        val API_ENDPOINT = stringPreferencesKey("api_endpoint")

        // 最后同步时间
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    }

    // ==================== 用户数据 ====================

    /**
     * 获取用户数据
     */
    fun getUser(): Flow<User> {
        return context.dataStore.data.map { prefs ->
            User(
                id = prefs[Keys.USER_ID] ?: "",
                name = prefs[Keys.USER_NAME] ?: "星际园丁",
                level = prefs[Keys.USER_LEVEL] ?: 1,
                exp = prefs[Keys.USER_EXP] ?: 0,
                energy = prefs[Keys.USER_ENERGY] ?: 100,
                crystals = prefs[Keys.USER_CRYSTALS] ?: 10,
                streakDays = prefs[Keys.USER_STREAK_DAYS] ?: 0,
                totalStudyMinutes = prefs[Keys.USER_TOTAL_STUDY_MINUTES] ?: 0,
                createdAt = prefs[Keys.USER_CREATED_AT] ?: System.currentTimeMillis()
            )
        }
    }

    /**
     * 保存用户数据
     */
    suspend fun saveUser(user: User) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_ID] = user.id
            prefs[Keys.USER_NAME] = user.name
            prefs[Keys.USER_LEVEL] = user.level
            prefs[Keys.USER_EXP] = user.exp
            prefs[Keys.USER_ENERGY] = user.energy
            prefs[Keys.USER_CRYSTALS] = user.crystals
            prefs[Keys.USER_STREAK_DAYS] = user.streakDays
            prefs[Keys.USER_TOTAL_STUDY_MINUTES] = user.totalStudyMinutes
            prefs[Keys.USER_CREATED_AT] = user.createdAt
        }
    }

    /**
     * 更新用户能量
     */
    suspend fun updateEnergy(energy: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_ENERGY] = energy
        }
    }

    /**
     * 更新用户结晶
     */
    suspend fun updateCrystals(crystals: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_CRYSTALS] = crystals
        }
    }

    /**
     * 更新等级和经验
     */
    suspend fun updateLevelAndExp(level: Int, exp: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_LEVEL] = level
            prefs[Keys.USER_EXP] = exp
        }
    }

    /**
     * 更新连续学习天数
     */
    suspend fun updateStreakDays(days: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_STREAK_DAYS] = days
        }
    }

    /**
     * 更新总学习时长
     */
    suspend fun updateTotalStudyMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_TOTAL_STUDY_MINUTES] = minutes
        }
    }

    // ==================== 设置 ====================

    /**
     * 获取深色模式设置
     */
    fun getDarkMode(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.DARK_MODE] ?: false
        }
    }

    /**
     * 设置深色模式
     */
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = enabled
        }
    }

    /**
     * 获取通知设置
     */
    fun getNotificationEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.NOTIFICATION_ENABLED] ?: true
        }
    }

    /**
     * 设置通知开关
     */
    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATION_ENABLED] = enabled
        }
    }

    /**
     * 获取音效设置
     */
    fun getSoundEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.SOUND_ENABLED] ?: true
        }
    }

    /**
     * 设置音效开关
     */
    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    /**
     * 获取自动提醒设置
     */
    fun getAutoReminder(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.AUTO_REMINDER] ?: true
        }
    }

    /**
     * 设置自动提醒
     */
    suspend fun setAutoReminder(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_REMINDER] = enabled
        }
    }

    // ==================== API 配置 ====================

    /**
     * 获取 API Key
     */
    fun getApiKey(): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.API_KEY] ?: ""
        }
    }

    /**
     * 设置 API Key
     */
    suspend fun setApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.API_KEY] = key
        }
    }

    /**
     * 获取 API Endpoint
     */
    fun getApiEndpoint(): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.API_ENDPOINT] ?: "https://api.deepseek.com"
        }
    }

    /**
     * 设置 API Endpoint
     */
    suspend fun setApiEndpoint(endpoint: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.API_ENDPOINT] = endpoint
        }
    }

    // ==================== 同步时间 ====================

    /**
     * 获取最后同步时间
     */
    fun getLastSyncTime(): Flow<Long> {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.LAST_SYNC_TIME] ?: 0L
        }
    }

    /**
     * 更新最后同步时间
     */
    suspend fun updateLastSyncTime() {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_SYNC_TIME] = System.currentTimeMillis()
        }
    }

    // ==================== 清理 ====================

    /**
     * 清除所有数据
     */
    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}

/**
 * 课程数据仓库
 */
class CourseDataRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    // Keys
    private val COURSES_KEY = stringPreferencesKey("courses_data")

    /**
     * 获取所有课程
     */
    fun getCourses(): Flow<List<Course>> {
        return context.dataStore.data.map { prefs ->
            val data = prefs[COURSES_KEY] ?: "[]"
            try {
                json.decodeFromString<List<Course>>(data)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * 保存所有课程
     */
    suspend fun saveCourses(courses: List<Course>) {
        context.dataStore.edit { prefs ->
            prefs[COURSES_KEY] = json.encodeToString(courses)
        }
    }

    /**
     * 添加课程
     */
    suspend fun addCourse(course: Course) {
        getCourses().collect { courses ->
            if (courses.none { it.id == course.id }) {
                saveCourses(courses + course)
            }
        }
    }

    /**
     * 更新课程
     */
    suspend fun updateCourse(course: Course) {
        getCourses().collect { courses ->
            val updated = courses.map { if (it.id == course.id) course else it }
            saveCourses(updated)
        }
    }

    /**
     * 删除课程
     */
    suspend fun deleteCourse(courseId: String) {
        getCourses().collect { courses ->
            saveCourses(courses.filter { it.id != courseId })
        }
    }
}

/**
 * 任务数据仓库
 */
class TaskDataRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val TASKS_KEY = stringPreferencesKey("tasks_data")

    /**
     * 获取所有任务
     */
    fun getTasks(): Flow<List<Task>> {
        return context.dataStore.data.map { prefs ->
            val data = prefs[TASKS_KEY] ?: "[]"
            try {
                json.decodeFromString<List<Task>>(data)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * 保存所有任务
     */
    suspend fun saveTasks(tasks: List<Task>) {
        context.dataStore.edit { prefs ->
            prefs[TASKS_KEY] = json.encodeToString(tasks)
        }
    }

    /**
     * 添加任务
     */
    suspend fun addTask(task: Task) {
        getTasks().collect { tasks ->
            if (tasks.none { it.id == task.id }) {
                saveTasks(tasks + task)
            }
        }
    }

    /**
     * 更新任务状态
     */
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus, actualMinutes: Int? = null) {
        getTasks().collect { tasks ->
            val updated = tasks.map { task ->
                if (task.id == taskId) {
                    task.copy(
                        status = status,
                        actualMinutes = actualMinutes ?: task.actualMinutes,
                        completedAt = if (status == TaskStatus.COMPLETED) System.currentTimeMillis() else null
                    )
                } else {
                    task
                }
            }
            saveTasks(updated)
        }
    }

    /**
     * 删除任务
     */
    suspend fun deleteTask(taskId: String) {
        getTasks().collect { tasks ->
            saveTasks(tasks.filter { it.id != taskId })
        }
    }
}

/**
 * 笔记数据仓库
 */
class NoteDataRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val NOTES_KEY = stringPreferencesKey("notes_data")

    /**
     * 获取所有笔记
     */
    fun getNotes(): Flow<List<Note>> {
        return context.dataStore.data.map { prefs ->
            val data = prefs[NOTES_KEY] ?: "[]"
            try {
                json.decodeFromString<List<Note>>(data)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * 保存所有笔记
     */
    suspend fun saveNotes(notes: List<Note>) {
        context.dataStore.edit { prefs ->
            prefs[NOTES_KEY] = json.encodeToString(notes)
        }
    }

    /**
     * 添加笔记
     */
    suspend fun addNote(note: Note) {
        getNotes().collect { notes ->
            if (notes.none { it.id == note.id }) {
                saveNotes(notes + note)
            }
        }
    }

    /**
     * 更新笔记复习次数
     */
    suspend fun updateNoteReview(noteId: String) {
        getNotes().collect { notes ->
            val updated = notes.map { note ->
                if (note.id == noteId) {
                    note.copy(
                        reviewCount = note.reviewCount + 1,
                        lastReviewedAt = System.currentTimeMillis()
                    )
                } else {
                    note
                }
            }
            saveNotes(updated)
        }
    }

    /**
     * 删除笔记
     */
    suspend fun deleteNote(noteId: String) {
        getNotes().collect { notes ->
            saveNotes(notes.filter { it.id != noteId })
        }
    }
}

/**
 * 知识图谱数据仓库
 */
class KnowledgeGraphRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val NODES_KEY = stringPreferencesKey("knowledge_nodes_data")

    /**
     * 获取所有知识节点
     */
    fun getNodes(): Flow<List<KnowledgeNode>> {
        return context.dataStore.data.map { prefs ->
            val data = prefs[NODES_KEY] ?: "[]"
            try {
                json.decodeFromString<List<KnowledgeNode>>(data)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * 保存所有知识节点
     */
    suspend fun saveNodes(nodes: List<KnowledgeNode>) {
        context.dataStore.edit { prefs ->
            prefs[NODES_KEY] = json.encodeToString(nodes)
        }
    }

    /**
     * 添加或更新知识节点
     */
    suspend fun upsertNode(node: KnowledgeNode) {
        getNodes().collect { nodes ->
            val existing = nodes.indexOfFirst { it.id == node.id }
            val updated = if (existing >= 0) {
                nodes.toMutableList().apply { set(existing, node) }
            } else {
                nodes + node
            }
            saveNodes(updated)
        }
    }

    /**
     * 删除知识节点
     */
    suspend fun deleteNode(nodeId: String) {
        getNodes().collect { nodes ->
            saveNodes(nodes.filter { it.id != nodeId })
        }
    }
}

/**
 * 对话历史数据仓库
 */
class ChatHistoryRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val HISTORY_KEY = stringPreferencesKey("chat_history")
    private val MAX_HISTORY = 100  // 最多保存100条

    /**
     * 获取对话历史
     */
    fun getHistory(): Flow<List<ChatHistoryEntry>> {
        return context.dataStore.data.map { prefs ->
            val data = prefs[HISTORY_KEY] ?: "[]"
            try {
                json.decodeFromString<List<ChatHistoryEntry>>(data)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * 添加对话记录
     */
    suspend fun addEntry(entry: ChatHistoryEntry) {
        getHistory().collect { history ->
            val updated = (history + entry).takeLast(MAX_HISTORY)
            saveHistory(updated)
        }
    }

    /**
     * 保存对话历史
     */
    private suspend fun saveHistory(history: List<ChatHistoryEntry>) {
        context.dataStore.edit { prefs ->
            prefs[HISTORY_KEY] = json.encodeToString(history)
        }
    }

    /**
     * 清空对话历史
     */
    suspend fun clearHistory() {
        context.dataStore.edit { prefs ->
            prefs[HISTORY_KEY] = "[]"
        }
    }
}

/**
 * 对话历史条目
 */
@Serializable
data class ChatHistoryEntry(
    val sessionId: String,
    val role: String,
    val content: String,
    val timestamp: Long,
    val toolUsed: String? = null,
    val toolResult: String? = null
)

/**
 * 签到记录数据仓库
 */
class CheckInRecordRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val RECORDS_KEY = stringPreferencesKey("check_in_records")

    fun getRecords(): Flow<List<CheckInRecord>> {
        return context.dataStore.data.map { prefs ->
            val data = prefs[RECORDS_KEY] ?: "[]"
            try {
                json.decodeFromString<List<CheckInRecord>>(data)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun saveRecords(records: List<CheckInRecord>) {
        context.dataStore.edit { prefs ->
            prefs[RECORDS_KEY] = json.encodeToString(records)
        }
    }
}

/**
 * 学习记录数据仓库
 */
class StudyRecordRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val RECORDS_KEY = stringPreferencesKey("study_records")

    fun getRecords(): Flow<List<StudyRecord>> {
        return context.dataStore.data.map { prefs ->
            val data = prefs[RECORDS_KEY] ?: "[]"
            try {
                json.decodeFromString<List<StudyRecord>>(data)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun saveRecords(records: List<StudyRecord>) {
        context.dataStore.edit { prefs ->
            prefs[RECORDS_KEY] = json.encodeToString(records)
        }
    }
}

/**
 * 成就数据仓库
 */
class AchievementRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val ACHIEVEMENTS_KEY = stringPreferencesKey("achievements_unlocked")

    /**
     * 获取已解锁的成就ID列表
     */
    fun getUnlockedIds(): Flow<List<String>> {
        return context.dataStore.data.map { prefs ->
            val data = prefs[ACHIEVEMENTS_KEY] ?: "[]"
            try {
                json.decodeFromString<List<String>>(data)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun saveUnlockedIds(ids: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[ACHIEVEMENTS_KEY] = json.encodeToString(ids)
        }
    }
}

/**
 * 学习会话数据仓库
 */
class StudySessionRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val SESSIONS_KEY = stringPreferencesKey("study_sessions")

    fun getSessions(): Flow<List<StudySession>> {
        return context.dataStore.data.map { prefs ->
            val data = prefs[SESSIONS_KEY] ?: "[]"
            try {
                json.decodeFromString<List<StudySession>>(data)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun saveSessions(sessions: List<StudySession>) {
        context.dataStore.edit { prefs ->
            prefs[SESSIONS_KEY] = json.encodeToString(sessions)
        }
    }
}

/**
 * 番茄钟设置数据仓库
 */
class PomodoroSettingsRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val SETTINGS_KEY = stringPreferencesKey("pomodoro_settings")

    fun getSettings(): Flow<PomodoroSettings> {
        return context.dataStore.data.map { prefs ->
            val data = prefs[SETTINGS_KEY]
            try {
                if (data != null) json.decodeFromString<PomodoroSettings>(data) else PomodoroSettings()
            } catch (e: Exception) {
                PomodoroSettings()
            }
        }
    }

    suspend fun saveSettings(settings: PomodoroSettings) {
        context.dataStore.edit { prefs ->
            prefs[SETTINGS_KEY] = json.encodeToString(settings)
        }
    }
}

/**
 * 笔记附件数据仓库
 */
class NoteAttachmentRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val ATTACHMENTS_KEY = stringPreferencesKey("note_attachments")

    fun getAttachments(): Flow<Map<String, List<NoteAttachment>>> {
        return context.dataStore.data.map { prefs ->
            val data = prefs[ATTACHMENTS_KEY] ?: "{}"
            try {
                json.decodeFromString<Map<String, List<NoteAttachment>>>(data)
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }

    suspend fun saveAttachments(attachments: Map<String, List<NoteAttachment>>) {
        context.dataStore.edit { prefs ->
            prefs[ATTACHMENTS_KEY] = json.encodeToString(attachments)
        }
    }
}

/**
 * 数据仓库聚合类
 */
class DataRepository(context: Context) {
    val userPrefs: UserPreferencesRepository = UserPreferencesRepository(context)
    val courses: CourseDataRepository = CourseDataRepository(context)
    val tasks: TaskDataRepository = TaskDataRepository(context)
    val notes: NoteDataRepository = NoteDataRepository(context)
    val knowledgeGraph: KnowledgeGraphRepository = KnowledgeGraphRepository(context)
    val chatHistory: ChatHistoryRepository = ChatHistoryRepository(context)
    val checkInRecords: CheckInRecordRepository = CheckInRecordRepository(context)
    val studyRecords: StudyRecordRepository = StudyRecordRepository(context)
    val achievements: AchievementRepository = AchievementRepository(context)
    val studySessions: StudySessionRepository = StudySessionRepository(context)
    val pomodoroSettings: PomodoroSettingsRepository = PomodoroSettingsRepository(context)
    val noteAttachments: NoteAttachmentRepository = NoteAttachmentRepository(context)
}