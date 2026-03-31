/**
 * AppViewModel.kt
 * 管理应用的所有状态和数据
 * 使用ViewModel是因为：
 * 1. 屏幕旋转时数据不会丢失
 * 2. 自动管理生命周期，避免内存泄漏
 * 3. 分离UI逻辑和业务逻辑
 */

package com.keling.app.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.keling.app.data.*
import com.keling.app.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

/**
 * AppViewModel继承自AndroidViewModel，这是Android架构组件
 * 所有数据都在这里集中管理，页面只负责显示
 * 使用AndroidViewModel以便访问Application Context进行数据持久化
 */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    // ==================== 数据持久化仓库 ====================
    private val dataRepository = DataRepository(application)

    // ==================== 用户数据 ====================

    /**
     * mutableStateOf创建可观察的状态
     * 当值改变时，Compose会自动重新绘制使用这个值的UI
     *
     * _currentUser是私有变量，可以在ViewModel内部修改
     * currentUser是公开变量，页面只能读取不能修改
     */
    private val _currentUser = mutableStateOf(
        User(
            id = UUID.randomUUID().toString(),
            name = "星际园丁"
        )
    )
    val currentUser: State<User> = _currentUser

    // ==================== 课程数据 ====================

    private val _courses = mutableStateOf<List<Course>>(emptyList())
    val courses: State<List<Course>> = _courses

    // ==================== 任务数据 ====================

    private val _tasks = mutableStateOf<List<Task>>(emptyList())
    val tasks: State<List<Task>> = _tasks

    // ==================== 知识图谱数据 ====================

    private val _knowledgeNodes = mutableStateOf<List<KnowledgeNode>>(emptyList())
    val knowledgeNodes: State<List<KnowledgeNode>> = _knowledgeNodes

    // ==================== 笔记数据 ====================

    private val _notes = mutableStateOf<List<Note>>(emptyList())
    val notes: State<List<Note>> = _notes

    // ==================== 导航状态 ====================

    private val _currentScreen = mutableStateOf("home")
    val currentScreen: State<String> = _currentScreen

    // 当前查看的任务详情 ID（如果有）
    private val _selectedTaskId = mutableStateOf<String?>(null)
    val selectedTaskId: State<String?> = _selectedTaskId

    // 当前查看的课程（星球）ID（如果有）
    private val _selectedCourseId = mutableStateOf<String?>(null)
    val selectedCourseId: State<String?> = _selectedCourseId

    // 当前图谱页面聚焦的知识节点（用于“精准定位到卡片位置”）
    private val _selectedKnowledgeNodeId = mutableStateOf<String?>(null)
    val selectedKnowledgeNodeId: State<String?> = _selectedKnowledgeNodeId

    // 每个课程是否在温室中启用知识图谱展示
    private val _courseGraphEnabled = mutableStateOf<Map<String, Boolean>>(emptyMap())
    val courseGraphEnabled: State<Map<String, Boolean>> = _courseGraphEnabled

    // ==================== 成就系统 ====================

    private val _achievements = mutableStateOf<List<Achievement>>(PREDEFINED_ACHIEVEMENTS)
    val achievements: State<List<Achievement>> = _achievements

    // ==================== 签到系统 ====================

    private val _checkInRecords = mutableStateOf<List<CheckInRecord>>(emptyList())
    val checkInRecords: State<List<CheckInRecord>> = _checkInRecords

    private val _showCheckInDialog = mutableStateOf(false)
    val showCheckInDialog: State<Boolean> = _showCheckInDialog

    // ==================== 学习记录 ====================

    private val _studyRecords = mutableStateOf<List<StudyRecord>>(emptyList())
    val studyRecords: State<List<StudyRecord>> = _studyRecords

    // ==================== 学习报告 ====================

    private val _latestReport = mutableStateOf<StudyReport?>(null)
    val latestReport: State<StudyReport?> = _latestReport

    // ==================== 初始化 ====================

    /**
     * init块在ViewModel创建时执行
     * 首先从本地加载持久化数据，然后可以从服务器同步
     */
    init {
        // 从本地加载持久化的数据
        loadLocalData()
    }

    /**
     * 从本地存储加载所有数据
     * 使用 first() 获取初始数据，避免 collect 的持续监听阻塞
     */
    private fun loadLocalData() {
        viewModelScope.launch {
            // 加载用户数据 - 使用 first() 获取初始值
            val savedUser = dataRepository.userPrefs.getUser().first()
            if (savedUser.id.isNotEmpty()) {
                // 有保存的用户数据，使用它
                _currentUser.value = savedUser
            } else {
                // 首次启动，保存当前初始化的用户到 DataStore
                dataRepository.userPrefs.saveUser(_currentUser.value)
            }
        }

        viewModelScope.launch {
            // 加载课程数据，分离已归档和未归档
            val savedCourses = dataRepository.courses.getCourses().first()
            _courses.value = savedCourses.filter { !it.isArchived }
            _archivedCourses.value = savedCourses.filter { it.isArchived }
        }

        viewModelScope.launch {
            // 加载任务数据
            val savedTasks = dataRepository.tasks.getTasks().first()
            _tasks.value = savedTasks
        }

        viewModelScope.launch {
            // 加载笔记数据
            val savedNotes = dataRepository.notes.getNotes().first()
            _notes.value = savedNotes
        }

        viewModelScope.launch {
            // 加载知识节点数据
            val savedNodes = dataRepository.knowledgeGraph.getNodes().first()
            _knowledgeNodes.value = savedNodes
        }

        viewModelScope.launch {
            // 加载签到记录
            val savedRecords = dataRepository.checkInRecords.getRecords().first()
            _checkInRecords.value = savedRecords
        }

        viewModelScope.launch {
            // 加载学习记录
            val savedStudyRecords = dataRepository.studyRecords.getRecords().first()
            _studyRecords.value = savedStudyRecords
        }

        viewModelScope.launch {
            // 加载已解锁成就
            val unlockedIds = dataRepository.achievements.getUnlockedIds().first()
            if (unlockedIds.isNotEmpty()) {
                _achievements.value = PREDEFINED_ACHIEVEMENTS.map { achievement ->
                    if (unlockedIds.contains(achievement.id)) {
                        achievement.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis(), progress = achievement.maxProgress)
                    } else {
                        achievement
                    }
                }
            }
        }

        viewModelScope.launch {
            // 加载学习会话
            val savedSessions = dataRepository.studySessions.getSessions().first()
            _studySessions.value = savedSessions
        }

        viewModelScope.launch {
            // 加载番茄钟设置
            val savedSettings = dataRepository.pomodoroSettings.getSettings().first()
            _pomodoroSettings.value = savedSettings
        }

        viewModelScope.launch {
            // 加载笔记附件
            val savedAttachments = dataRepository.noteAttachments.getAttachments().first()
            _noteAttachments.value = savedAttachments
        }
    }

    /**
     * 保存所有数据到本地存储
     * 在应用退出或数据变更时调用
     */
    fun saveAllData() {
        viewModelScope.launch {
            dataRepository.userPrefs.saveUser(_currentUser.value)
            dataRepository.courses.saveCourses(_courses.value)
            dataRepository.tasks.saveTasks(_tasks.value)
            dataRepository.notes.saveNotes(_notes.value)
            dataRepository.knowledgeGraph.saveNodes(_knowledgeNodes.value)
            dataRepository.checkInRecords.saveRecords(_checkInRecords.value)
            dataRepository.studyRecords.saveRecords(_studyRecords.value)
            dataRepository.achievements.saveUnlockedIds(_achievements.value.filter { it.isUnlocked }.map { it.id })
            dataRepository.studySessions.saveSessions(_studySessions.value)
            dataRepository.pomodoroSettings.saveSettings(_pomodoroSettings.value)
            dataRepository.noteAttachments.saveAttachments(_noteAttachments.value)
        }
    }

    /**
     * 从服务器同步用户数据
     * 登录成功后调用此方法
     */
    fun syncFromServer(token: String, userId: String) {
        viewModelScope.launch {
            try {
                // 使用 ApiService 从服务器获取数据
                val apiService = ApiService()

                // 拉取课程数据
                val coursesResult = apiService.courses.getAll(token)
                if (coursesResult.isSuccess) {
                    val coursesData = coursesResult.getOrNull()?.courses ?: emptyList()
                    _courses.value = coursesData.map { data ->
                        Course(
                            id = data.id,
                            name = data.name,
                            code = data.code,
                            teacher = data.teacher,
                            location = data.location,
                            themeColor = data.themeColor.removePrefix("#").toLong(16) or 0xFF000000,
                            masteryLevel = data.masteryLevel.toFloat(),
                            plantStage = data.plantStage,
                            planetStyleIndex = data.planetStyleIndex,
                            totalStudyMinutes = data.totalStudyMinutes,
                            isArchived = data.isArchived,
                            studySessionCount = data.studySessionCount
                        )
                    }
                }

                // 拉取任务数据
                val tasksResult = apiService.tasks.getAll(token)
                if (tasksResult.isSuccess) {
                    val tasksData = tasksResult.getOrNull()?.tasks ?: emptyList()
                    _tasks.value = tasksData.map { data ->
                        Task(
                            id = data.id,
                            title = data.title,
                            description = data.description,
                            type = TaskType.valueOf(data.type),
                            courseId = data.courseId,
                            status = TaskStatus.valueOf(data.status),
                            priority = data.priority,
                            estimatedMinutes = data.estimatedMinutes,
                            actualMinutes = data.actualMinutes,
                            rewards = Rewards(
                                energy = data.rewardsEnergy,
                                crystals = data.rewardsCrystals,
                                exp = data.rewardsExp
                            )
                        )
                    }
                }

                // 拉取用户信息
                val userResult = apiService.auth.getMe(token)
                if (userResult.isSuccess) {
                    val userData = userResult.getOrNull()?.user
                    if (userData != null) {
                        _currentUser.value = User(
                            id = userData.id,
                            name = userData.name,
                            level = userData.level,
                            exp = userData.exp,
                            energy = userData.energy,
                            crystals = userData.crystals,
                            streakDays = userData.streakDays,
                            totalStudyMinutes = userData.totalStudyMinutes,
                            lastCheckInDate = userData.lastCheckInDate,
                            createdAt = userData.createdAt?.let { try { it.toLong() } catch (e: Exception) { System.currentTimeMillis() } } ?: System.currentTimeMillis()
                        )
                    }
                }
            } catch (e: Exception) {
                // 同步失败，保持空数据
                e.printStackTrace()
            }
        }
    }

    /**
     * 清空所有数据（登出时调用）
     */
    fun clearAllData() {
        _courses.value = emptyList()
        _tasks.value = emptyList()
        _knowledgeNodes.value = emptyList()
        _notes.value = emptyList()
        _studyRecords.value = emptyList()
        _currentUser.value = User(
            id = UUID.randomUUID().toString(),
            name = "星际园丁"
        )
    }

    /**
     * 更新当前用户信息（登录成功后）
     */
    fun updateUser(user: User) {
        _currentUser.value = user
    }

    // ==================== 业务方法 ====================

    /**
     * 开始任务
     * 在实际应用中，这里会启动计时器，记录学习时长
     */
    fun startTask(taskId: String) {
        val updatedTasks = _tasks.value.map { task ->
            if (task.id == taskId) {
                task.copy(status = TaskStatus.IN_PROGRESS)
            } else task
        }
        _tasks.value = updatedTasks
    }

    /**
     * 供 AI / Tool 层统一设置任务列表的内部入口。
     * - 保持为最小粒度的封装，避免在 Tool 中直接操作 _tasks。
     */
    fun setTasks(newTasks: List<Task>) {
        _tasks.value = newTasks
    }

    /**
     * 添加新任务
     */
    fun addTask(task: Task) {
        _tasks.value = _tasks.value + task
        // 自动保存到本地
        viewModelScope.launch {
            dataRepository.tasks.saveTasks(_tasks.value)
        }
    }

    /**
     * 更新任务
     */
    fun updateTask(task: Task) {
        _tasks.value = _tasks.value.map { if (it.id == task.id) task else it }
        // 自动保存到本地
        viewModelScope.launch {
            dataRepository.tasks.saveTasks(_tasks.value)
        }
    }

    /**
     * 删除任务
     */
    fun deleteTask(taskId: String) {
        _tasks.value = _tasks.value.filter { it.id != taskId }
        // 自动保存到本地
        viewModelScope.launch {
            dataRepository.tasks.saveTasks(_tasks.value)
        }
    }

    /**
     * 供导航 / AI 工具使用的统一导航入口。
     */
    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    /**
     * 打开某个任务的详情页。
     */
    fun openTaskDetail(taskId: String) {
        _selectedTaskId.value = taskId
        _currentScreen.value = "task_detail"
    }

    /**
     * 从任务详情返回时清空选中任务（可选）。
     */
    fun clearSelectedTask() {
        _selectedTaskId.value = null
    }

    /**
     * 打开某个课程的专属培育温室页面。
     */
    fun openCourseGreenhouse(courseId: String) {
        _selectedCourseId.value = courseId
        _currentScreen.value = "greenhouse_course"
    }

    fun clearSelectedCourse() {
        _selectedCourseId.value = null
    }

    fun enableCourseGraph(courseId: String) {
        _courseGraphEnabled.value = _courseGraphEnabled.value + (courseId to true)
    }

    fun isCourseGraphEnabled(courseId: String): Boolean {
        // 检查是否有实际的知识节点，或者是否手动启用了图谱
        val hasNodes = _knowledgeNodes.value.any { it.courseId == courseId }
        val manuallyEnabled = _courseGraphEnabled.value[courseId] == true
        return hasNodes || manuallyEnabled
    }

    fun getKnowledgeNodeCount(courseId: String): Int {
        return _knowledgeNodes.value.count { it.courseId == courseId }
    }

    /**
     * 完成任务
     * 更新任务状态，增加用户奖励
     */
    fun completeTask(taskId: String, actualMinutes: Int) {
        val task = _tasks.value.find { it.id == taskId } ?: return

        // 更新任务状态
        val updatedTasks = _tasks.value.map { t ->
            if (t.id == taskId) {
                t.copy(
                    status = TaskStatus.COMPLETED,
                    actualMinutes = actualMinutes,
                    completedAt = System.currentTimeMillis()
                )
            } else t
        }
        _tasks.value = updatedTasks
        // 自动保存任务数据到本地
        viewModelScope.launch {
            dataRepository.tasks.saveTasks(_tasks.value)
        }

        // 增加用户奖励 + 经验，并根据经验简单更新等级
        val user = _currentUser.value
        val newExp = user.exp + task.rewards.exp
        val (newLevel, normalizedExp) = computeLevel(user.level, newExp)

        // 使用 updateUser 自动保存用户数据
        updateUser { it.copy(
            level = newLevel,
            exp = normalizedExp,
            energy = it.energy + task.rewards.energy,
            crystals = it.crystals + task.rewards.crystals,
            totalStudyMinutes = it.totalStudyMinutes + actualMinutes
        ) }
    }

    /**
     * 简单的等级计算：
     * - 当前等级每级需要 100 经验
     * - 升级后多余经验保留到下一等级
     */
    private fun computeLevel(currentLevel: Int, totalExp: Int): Pair<Int, Int> {
        var level = currentLevel
        var exp = totalExp
        val expPerLevel = 100

        while (exp >= expPerLevel) {
            exp -= expPerLevel
            level += 1
        }
        return level to exp
    }

    // ==================== 知识图谱相关方法 ====================

    /**
     * 为指定课程添加或更新一个知识节点。
     * 以 (courseId, name) 作为”自然键”做去重，方便通过名称精确定位。
     * 自动维护父子节点的双向关系。
     */
    fun upsertKnowledgeNode(node: KnowledgeNode): KnowledgeNode {
        val existing = _knowledgeNodes.value.find {
            it.courseId == node.courseId && it.name == node.name
        }

        // 确定最终的 parentIds
        val finalParentIds = if (node.parentIds.isNotEmpty()) node.parentIds
                              else existing?.parentIds ?: emptyList()

        // 创建或更新节点
        val finalNode = if (existing != null) {
            existing.copy(
                id = existing.id,  // 保持原有ID
                description = node.description.ifBlank { existing.description },
                parentIds = finalParentIds,
                childIds = if (node.childIds.isNotEmpty()) node.childIds else existing.childIds,
                difficulty = node.difficulty,
                masteryLevel = node.masteryLevel,
                positionX = node.positionX,
                positionY = node.positionY,
                isUnlocked = node.isUnlocked
            )
        } else {
            node.copy(parentIds = finalParentIds)
        }

        // 更新节点列表
        val currentNodes = _knowledgeNodes.value
            .filterNot { it.id == finalNode.id || (it.courseId == finalNode.courseId && it.name == finalNode.name) }
            .toMutableList()

        // 更新父节点的 childIds（双向关系维护）
        val oldParentIds = existing?.parentIds ?: emptyList()
        val newParentIds = finalParentIds

        // 从旧父节点移除引用
        oldParentIds.filter { it !in newParentIds }.forEach { oldParentId ->
            val oldParentIndex = currentNodes.indexOfFirst { it.id == oldParentId }
            if (oldParentIndex >= 0) {
                val oldParent = currentNodes[oldParentIndex]
                currentNodes[oldParentIndex] = oldParent.copy(
                    childIds = oldParent.childIds.filter { it != finalNode.id }
                )
            }
        }

        // 添加到新父节点
        newParentIds.forEach { parentId ->
            val parentIndex = currentNodes.indexOfFirst { it.id == parentId }
            if (parentIndex >= 0) {
                val parent = currentNodes[parentIndex]
                if (finalNode.id !in parent.childIds) {
                    currentNodes[parentIndex] = parent.copy(
                        childIds = parent.childIds + finalNode.id
                    )
                }
            }
        }

        // 添加最终节点
        currentNodes.add(finalNode)
        _knowledgeNodes.value = currentNodes

        // 自动保存到本地
        viewModelScope.launch {
            dataRepository.knowledgeGraph.saveNodes(currentNodes)
        }

        recomputeKnowledgeGraphLayout(finalNode.courseId)

        return finalNode
    }

    /**
     * 通过课程 + 名称删除节点，返回是否真的删除了东西。
     */
    fun deleteKnowledgeNodeByCourseAndName(courseId: String, name: String): Boolean {
        val before = _knowledgeNodes.value
        val removedIds = before.filter {
            it.courseId == courseId && it.name == name
        }.map { it.id }.toSet()

        if (removedIds.isEmpty()) return false

        // 同时从其他节点的 parentIds/childIds 中移除引用
        val after = before
            .filterNot { it.id in removedIds }
            .map { node ->
                node.copy(
                    parentIds = node.parentIds.filterNot { it in removedIds },
                    childIds = node.childIds.filterNot { it in removedIds }
                )
            }

        _knowledgeNodes.value = after

        // 自动保存到本地
        viewModelScope.launch {
            dataRepository.knowledgeGraph.saveNodes(after)
        }

        recomputeKnowledgeGraphLayout(courseId)

        return true
    }

    private fun recomputeKnowledgeGraphLayout(courseId: String) {
        val nodes = _knowledgeNodes.value.filter { it.courseId == courseId }
        if (nodes.isEmpty()) return

        val nodeById = nodes.associateBy { it.id }

        val depthMemo = mutableMapOf<String, Int>()
        val visiting = mutableSetOf<String>()

        fun depthOf(nodeId: String): Int {
            depthMemo[nodeId]?.let { return it }
            if (visiting.contains(nodeId)) return 0 // 防止环导致死递归

            visiting.add(nodeId)
            val node = nodeById[nodeId]
            val parents = node?.parentIds?.mapNotNull { nodeById[it] } ?: emptyList()

            val depth = if (parents.isEmpty()) {
                0
            } else {
                parents.maxOf { depthOf(it.id) } + 1
            }

            visiting.remove(nodeId)
            depthMemo[nodeId] = depth
            return depth
        }

        val depthById = nodes.associate { it.id to depthOf(it.id) }
        val maxDepth = (depthById.values.maxOrNull() ?: 0).coerceAtLeast(1)

        val byDepth = depthById.entries.groupBy { it.value }
        val depthKeys = byDepth.keys.sorted()

        val baseX = mutableMapOf<String, Float>()
        val baseY = mutableMapOf<String, Float>()

        depthKeys.forEach { depth ->
            val layerNodes = byDepth[depth].orEmpty()
                .mapNotNull { nodeById[it.key] }
                .sortedBy { it.name }

            val count = layerNodes.size
            val y = 0.12f + depth.toFloat() * 0.78f / (maxDepth + 1f)

            layerNodes.forEachIndexed { idx, node ->
                val x = ((idx + 1).toFloat() / (count + 1).toFloat()).coerceIn(0.05f, 0.95f)
                baseX[node.id] = x
                baseY[node.id] = y
            }
        }

        // 融合父节点的 x，让节点在“视觉上”更像思维导图结构
        val finalX = mutableMapOf<String, Float>()
        depthKeys.forEach { depth ->
            val layerNodes = byDepth[depth].orEmpty()
                .mapNotNull { nodeById[it.key] }

            layerNodes.forEach { node ->
                val parents = node.parentIds.mapNotNull { nodeById[it] }
                val parentX = parents.mapNotNull { baseX[it.id] }.takeIf { it.isNotEmpty() }?.average()?.toFloat()

                val base = baseX[node.id] ?: 0.5f
                val x = if (parentX == null) {
                    base
                } else {
                    (0.65f * base + 0.35f * parentX).coerceIn(0.05f, 0.95f)
                }
                finalX[node.id] = x
            }
        }

        _knowledgeNodes.value = _knowledgeNodes.value.map { existing ->
            if (existing.courseId != courseId) return@map existing
            existing.copy(
                positionX = finalX[existing.id] ?: existing.positionX,
                positionY = baseY[existing.id] ?: existing.positionY
            )
        }
    }

    /**
     * 在某门课程内按名称模糊查找节点，用于 AI 根据自然语言定位。
     */
    fun findKnowledgeNodeByNameInCourse(courseId: String, name: String): KnowledgeNode? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return null

        // 先尝试精确匹配，再尝试包含关系
        return _knowledgeNodes.value.firstOrNull {
            it.courseId == courseId && it.name == trimmed
        } ?: _knowledgeNodes.value.firstOrNull {
            it.courseId == courseId && (it.name.contains(trimmed) || trimmed.contains(it.name))
        }
    }

    /**
     * 获取某门课程下的全部知识节点。
     */
    fun knowledgeNodesForCourse(courseId: String): List<KnowledgeNode> {
        return _knowledgeNodes.value.filter { it.courseId == courseId }
    }

    /**
     * 为课程创建新的知识点节点，自动计算不重叠的位置
     * @param courseId 课程ID
     * @param name 节点名称
     * @param parentName 父节点名称（可选，如果为空则连接到根节点）
     * @param description 描述
     */
    fun createKnowledgeNode(
        courseId: String,
        name: String,
        parentName: String? = null,
        description: String = ""
    ): KnowledgeNode? {
        if (name.isBlank()) return null

        val existingNodes = _knowledgeNodes.value.filter { it.courseId == courseId }

        // 检查名称是否已存在
        if (existingNodes.any { it.name == name }) {
            return null // 名称已存在，避免重复
        }

        // 找到父节点
        val parentNode = if (parentName != null) {
            existingNodes.find { it.name == parentName }
        } else {
            // 如果没有指定父节点，连接到根节点
            existingNodes.firstOrNull { it.parentIds.isEmpty() }
        }

        // 计算新节点的位置（避免重叠）
        val siblingCount = parentNode?.childIds?.size ?: existingNodes.count { it.parentIds.isEmpty() }
        val baseY = parentNode?.positionY ?: 0.5f
        val offsetY = (siblingCount - existingNodes.count { it.parentIds.isEmpty() } / 2f) * 0.15f
        val newX = (parentNode?.positionX ?: 0.1f) + 0.25f
        val newY = (baseY + offsetY).coerceIn(0.1f, 0.9f)

        val newNodeId = "kn_${courseId}_${System.currentTimeMillis()}"

        val newNode = KnowledgeNode(
            id = newNodeId,
            courseId = courseId,
            name = name,
            description = description,
            parentIds = if (parentNode != null) listOf(parentNode.id) else emptyList(),
            childIds = emptyList(),
            difficulty = 3,
            masteryLevel = 0f,
            positionX = newX.coerceIn(0.1f, 0.9f),
            positionY = newY,
            isUnlocked = true
        )

        // 如果有父节点，更新父节点的 childIds
        if (parentNode != null) {
            val updatedParent = parentNode.copy(
                childIds = parentNode.childIds + newNodeId
            )
            _knowledgeNodes.value = _knowledgeNodes.value.map {
                if (it.id == parentNode.id) updatedParent else it
            }
        }

        // 添加新节点
        _knowledgeNodes.value = _knowledgeNodes.value + newNode

        // 自动保存
        viewModelScope.launch {
            dataRepository.knowledgeGraph.saveNodes(_knowledgeNodes.value)
        }

        return newNode
    }

    fun openKnowledgeGraph(courseIdOrName: String, focusNodeId: String? = null) {
        val course = _courses.value.find { it.id == courseIdOrName || it.name == courseIdOrName }
        val finalCourseId = course?.id ?: courseIdOrName
        _selectedCourseId.value = finalCourseId
        _selectedKnowledgeNodeId.value = focusNodeId
        _currentScreen.value = "knowledge_graph"
    }

    fun clearSelectedKnowledgeNode() {
        _selectedKnowledgeNodeId.value = null
    }

    /**
     * 添加新课程
     */
    fun addCourse(course: Course) {
        _courses.value = _courses.value + course
        // 自动保存到本地
        viewModelScope.launch {
            dataRepository.courses.saveCourses(_courses.value)
        }
    }

    fun createCourse(name: String, planetStyleIndex: Int): Course? {
        val finalName = name.trim()
        if (finalName.isEmpty()) return null

        val newId = "course_${System.currentTimeMillis()}"
        // themeColor 先沿用默认恒星橙；后续如你希望可以基于 planetStyleIndex 生成配色
        val course = Course(
            id = newId,
            name = finalName,
            code = "NEW_${newId.takeLast(4)}",
            teacher = "恒星引擎",
            themeColor = 0xFFE8A87C,
            masteryLevel = 0f,
            plantStage = 0,
            planetStyleIndex = planetStyleIndex
        )
        _courses.value = _courses.value + course

        // 自动创建根知识点节点（以课程名为节点名）
        val rootNode = KnowledgeNode(
            id = "kn_${newId}_root",
            courseId = newId,
            name = finalName,
            description = "${finalName}的知识体系",
            parentIds = emptyList(),
            childIds = emptyList(),
            difficulty = 1,
            masteryLevel = 0f,
            positionX = 0.1f,
            positionY = 0.5f,
            isUnlocked = true
        )
        _knowledgeNodes.value = _knowledgeNodes.value + rootNode

        // 自动保存到本地
        viewModelScope.launch {
            dataRepository.courses.saveCourses(_courses.value)
            dataRepository.knowledgeGraph.saveNodes(_knowledgeNodes.value)
        }
        return course
    }

    fun updateCourse(courseId: String, newName: String, planetStyleIndex: Int) {
        val finalName = newName.trim()
        if (finalName.isEmpty()) return

        _courses.value = _courses.value.map { course ->
            if (course.id == courseId) {
                course.copy(name = finalName, planetStyleIndex = planetStyleIndex)
            } else course
        }
        // 自动保存到本地
        viewModelScope.launch {
            dataRepository.courses.saveCourses(_courses.value)
        }
    }

    /**
     * 添加笔记
     */
    fun addNote(note: Note) {
        _notes.value = _notes.value + note
        // 自动保存到本地
        viewModelScope.launch {
            dataRepository.notes.saveNotes(_notes.value)
        }
    }

    /**
     * 更新用户名称
     */
    fun updateUserName(newName: String) {
        updateUser { it.copy(name = newName) }
    }

    /**
     * 统一的用户更新方法，自动保存到本地存储
     */
    private inline fun updateUser(transform: (User) -> User) {
        _currentUser.value = transform(_currentUser.value)
        viewModelScope.launch {
            dataRepository.userPrefs.saveUser(_currentUser.value)
        }
    }

    // ==================== 课表相关 ====================

    /**
     * 为课程添加一个上课时段。若相同时段已存在则覆盖。
     */
    fun addScheduleSlot(courseId: String, slot: ScheduleSlot): Course? {
        val course = _courses.value.find { it.id == courseId } ?: return null
        val existing = course.schedule.filter {
            it.dayOfWeek != slot.dayOfWeek ||
                it.startHour != slot.startHour ||
                it.startMinute != slot.startMinute
        }
        val newSchedule = (existing + slot).sortedWith(
            compareBy({ it.dayOfWeek }, { it.startHour }, { it.startMinute })
        )
        val updated = course.copy(schedule = newSchedule)
        _courses.value = _courses.value.map { if (it.id == courseId) updated else it }
        // 自动保存到本地
        viewModelScope.launch {
            dataRepository.courses.saveCourses(_courses.value)
        }
        return updated
    }

    /**
     * 移除课程的某个上课时段（按 dayOfWeek + startHour + startMinute 匹配）。
     */
    fun removeScheduleSlot(courseId: String, dayOfWeek: Int, startHour: Int, startMinute: Int): Boolean {
        val course = _courses.value.find { it.id == courseId } ?: return false
        val newSchedule = course.schedule.filter {
            !(it.dayOfWeek == dayOfWeek && it.startHour == startHour && it.startMinute == startMinute)
        }
        if (newSchedule.size == course.schedule.size) return false
        val updated = course.copy(schedule = newSchedule)
        _courses.value = _courses.value.map { if (it.id == courseId) updated else it }
        // 自动保存到本地
        viewModelScope.launch {
            dataRepository.courses.saveCourses(_courses.value)
        }
        return true
    }

    /**
     * 获取指定星期、当前时刻的「上一节 / 当前节 / 下一节」。
     * @return Triple(上一节, 当前节, 下一节)，每一项为 (Course, ScheduleSlot)，无则为 null
     */
    fun getCurrentPrevNextSlots(dayOfWeek: Int, hour: Int, minute: Int): Triple<Pair<Course, ScheduleSlot>?, Pair<Course, ScheduleSlot>?, Pair<Course, ScheduleSlot>?> {
        val nowMinutes = hour * 60 + minute
        val allSlots = _courses.value.flatMap { course ->
            course.schedule
                .filter { it.dayOfWeek == dayOfWeek }
                .map { course to it }
        }
            .sortedBy { (_, s) -> s.startHour * 60 + s.startMinute }

        var prev: Pair<Course, ScheduleSlot>? = null
        var current: Pair<Course, ScheduleSlot>? = null
        var next: Pair<Course, ScheduleSlot>? = null

        for ((c, s) in allSlots) {
            val startM = s.startHour * 60 + s.startMinute
            val endM = startM + s.durationMinutes
            if (nowMinutes >= startM && nowMinutes < endM) {
                current = c to s
                prev = allSlots.indexOf(c to s).let { i -> if (i > 0) allSlots[i - 1] else null }
                next = allSlots.indexOf(c to s).let { i -> if (i < allSlots.size - 1) allSlots[i + 1] else null }
                break
            }
            if (nowMinutes < startM) {
                next = c to s
                prev = allSlots.indexOf(c to s).let { i -> if (i > 0) allSlots[i - 1] else null }
                break
            }
            prev = c to s
        }
        if (current == null && next == null && prev != null) {
            // 全部已过
        } else if (current == null && next == null) {
            next = allSlots.firstOrNull()
        }
        return Triple(prev, current, next)
    }

    /**
     * 获取今日全部课表（按时间排序），用于课表编辑/展示。
     */
    fun getTodaySchedule(dayOfWeek: Int): List<Pair<Course, ScheduleSlot>> {
        return _courses.value.flatMap { course ->
            course.schedule
                .filter { it.dayOfWeek == dayOfWeek }
                .map { course to it }
        }
            .sortedBy { (_, s) -> s.startHour * 60 + s.startMinute }
    }

    /**
     * 获取本周全部课表（按星期、时间排序）。
     */
    fun getWeekSchedule(): List<Pair<Course, ScheduleSlot>> {
        return _courses.value.flatMap { course ->
            course.schedule.map { course to it }
        }
            .sortedWith(compareBy({ it.second.dayOfWeek }, { it.second.startHour }, { it.second.startMinute }))
    }

    // ==================== 成就系统相关 ====================

    /**
     * 检查并解锁成就
     */
    fun checkAndUnlockAchievements() {
        val user = _currentUser.value
        val tasks = _tasks.value
        val courses = _courses.value
        val notes = _notes.value
        val knowledgeNodes = _knowledgeNodes.value

        val completedTasks = tasks.count { it.status == TaskStatus.COMPLETED }
        val unlockedNodes = knowledgeNodes.count { it.isUnlocked }

        val updatedAchievements = _achievements.value.map { achievement ->
            val newProgress = when (achievement.id) {
                "first_task" -> if (completedTasks >= 1) 1 else 0
                "task_master_10" -> completedTasks.coerceAtMost(10)
                "task_master_50" -> completedTasks.coerceAtMost(50)
                "first_course" -> if (courses.isNotEmpty()) 1 else 0
                "mastery_80" -> if (courses.any { it.masteryLevel >= 0.8f }) 1 else 0
                "streak_3" -> user.streakDays.coerceAtMost(3)
                "streak_7" -> user.streakDays.coerceAtMost(7)
                "streak_30" -> user.streakDays.coerceAtMost(30)
                "knowledge_10" -> unlockedNodes.coerceAtMost(10)
                "notes_5" -> notes.size.coerceAtMost(5)
                else -> achievement.progress
            }

            val shouldUnlock = newProgress >= achievement.maxProgress && !achievement.isUnlocked

            if (shouldUnlock) {
                // 解锁成就，发放奖励
                updateUser { it.copy(
                    energy = it.energy + achievement.rewardEnergy,
                    crystals = it.crystals + achievement.rewardCrystals
                ) }
                achievement.copy(
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis(),
                    progress = newProgress
                )
            } else {
                achievement.copy(progress = newProgress)
            }
        }

        _achievements.value = updatedAchievements
        // 自动保存已解锁成就
        viewModelScope.launch {
            dataRepository.achievements.saveUnlockedIds(updatedAchievements.filter { it.isUnlocked }.map { it.id })
        }
    }

    /**
     * 获取已解锁成就数量
     */
    fun getUnlockedAchievementCount(): Int {
        return _achievements.value.count { it.isUnlocked }
    }

    /**
     * 获取成就进度百分比
     */
    fun getAchievementProgress(): Float {
        val unlocked = _achievements.value.count { it.isUnlocked }
        return unlocked.toFloat() / _achievements.value.size
    }

    // ==================== 签到系统相关 ====================

    /**
     * 检查今日是否已签到
     */
    fun hasCheckedInToday(): Boolean {
        val today = getTodayDateString()
        return _checkInRecords.value.any { it.date == today }
    }

    /**
     * 获取连续签到天数
     */
    fun getConsecutiveCheckInDays(): Int {
        val records = _checkInRecords.value.sortedByDescending { it.date }
        if (records.isEmpty()) return 0

        var consecutiveDays = 1
        val cal = Calendar.getInstance()

        for (i in 0 until records.size - 1) {
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.DAY_OF_MONTH, -consecutiveDays)
            val expectedDate = String.format(
                "%04d%02d%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )

            if (records.getOrNull(i + 1)?.date == expectedDate) {
                consecutiveDays++
            } else {
                break
            }
        }

        return consecutiveDays
    }

    /**
     * 执行签到
     */
    fun checkIn(): CheckInReward? {
        if (hasCheckedInToday()) return null

        val today = getTodayDateString()
        val consecutiveDays = getConsecutiveCheckInDays() + 1
        val rewardIndex = ((consecutiveDays - 1) % 7)
        val reward = CHECK_IN_REWARDS.getOrNull(rewardIndex) ?: CHECK_IN_REWARDS.first()

        // 添加签到记录
        val record = CheckInRecord(
            date = today,
            userId = _currentUser.value.id,
            rewardReceived = true
        )
        _checkInRecords.value = _checkInRecords.value + record
        // 自动保存签到记录
        viewModelScope.launch {
            dataRepository.checkInRecords.saveRecords(_checkInRecords.value)
        }

        // 更新用户数据
        updateUser { it.copy(
            energy = it.energy + reward.energy,
            crystals = it.crystals + reward.crystals,
            streakDays = consecutiveDays
        ) }

        // 检查成就
        checkAndUnlockAchievements()

        return reward
    }

    /**
     * 显示签到弹窗
     */
    fun showCheckInDialog() {
        _showCheckInDialog.value = true
    }

    /**
     * 隐藏签到弹窗
     */
    fun hideCheckInDialog() {
        _showCheckInDialog.value = false
    }

    // ==================== 学习记录相关 ====================

    /**
     * 添加学习记录
     */
    fun addStudyRecord(
        courseId: String?,
        taskId: String?,
        type: StudyType,
        durationMinutes: Int,
        notes: String = ""
    ) {
        val record = StudyRecord(
            id = "record_${System.currentTimeMillis()}",
            userId = _currentUser.value.id,
            courseId = courseId,
            taskId = taskId,
            type = type,
            durationMinutes = durationMinutes,
            notes = notes
        )
        _studyRecords.value = _studyRecords.value + record
        // 自动保存学习记录
        viewModelScope.launch {
            dataRepository.studyRecords.saveRecords(_studyRecords.value)
        }
    }

    /**
     * 获取今日学习时长
     */
    fun getTodayStudyMinutes(): Int {
        val today = getTodayDateString()
        val cal = Calendar.getInstance()
        cal.timeInMillis = System.currentTimeMillis()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis

        return _studyRecords.value
            .filter { it.createdAt >= startOfDay }
            .sumOf { it.durationMinutes }
    }

    /**
     * 获取本周学习时长
     */
    fun getWeekStudyMinutes(): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfWeek = cal.timeInMillis

        return _studyRecords.value
            .filter { it.createdAt >= startOfWeek }
            .sumOf { it.durationMinutes }
    }

    // ==================== 学习报告相关 ====================

    /**
     * 生成学习报告
     */
    fun generateStudyReport() {
        val cal = Calendar.getInstance()
        val endDate = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, -7)
        val startDate = cal.timeInMillis

        val weekRecords = _studyRecords.value.filter { it.createdAt >= startDate && it.createdAt <= endDate }
        val weekTasks = _tasks.value.filter {
            it.completedAt != null && it.completedAt >= startDate && it.completedAt <= endDate
        }

        val totalMinutes = weekRecords.sumOf { it.durationMinutes }
        val completedCount = weekTasks.count { it.status == TaskStatus.COMPLETED }
        val studiedCourses = weekRecords.mapNotNull { it.courseId }.distinct().size

        val courses = _courses.value
        val avgMastery = if (courses.isNotEmpty()) courses.map { it.masteryLevel }.average().toFloat() else 0f

        // 分析强弱点
        val strongPoints = courses.filter { it.masteryLevel >= 0.7f }.map { it.name }
        val weakPoints = courses.filter { it.masteryLevel < 0.5f }.map { it.name }

        // 生成建议
        val suggestions = mutableListOf<String>()
        if (weakPoints.isNotEmpty()) {
            suggestions.add("建议重点关注「${weakPoints.first()}」的复习")
        }
        if (totalMinutes < 300) {
            suggestions.add("本周学习时长较少，建议增加学习时间")
        }
        if (completedCount < 5) {
            suggestions.add("可以尝试分解大任务，提高完成效率")
        }
        if (suggestions.isEmpty()) {
            suggestions.add("保持良好的学习习惯，继续加油！")
        }

        val report = StudyReport(
            id = "report_${System.currentTimeMillis()}",
            userId = _currentUser.value.id,
            startDate = startDate,
            endDate = endDate,
            totalStudyMinutes = totalMinutes,
            completedTasks = completedCount,
            coursesStudied = studiedCourses,
            averageMastery = avgMastery,
            streakDays = _currentUser.value.streakDays,
            aiInsight = generateAIInsight(totalMinutes, completedCount, avgMastery),
            strongPoints = strongPoints.take(3),
            weakPoints = weakPoints.take(3),
            suggestions = suggestions
        )

        _latestReport.value = report
    }

    /**
     * 生成AI洞察
     */
    private fun generateAIInsight(minutes: Int, tasks: Int, mastery: Float): String {
        return when {
            minutes >= 600 -> "本周学习非常勤奋！累计学习${minutes / 60}小时，保持了高效的学习节奏。"
            minutes >= 300 -> "本周学习表现良好，完成了${tasks}个任务，继续保持这个势头！"
            minutes >= 100 -> "本周有所进步，建议合理安排时间，提高学习效率。"
            else -> "本周学习时间较少，可以尝试制定更具体的学习计划哦~"
        }
    }

    // ==================== 统计数据 ====================

    /**
     * 获取统计数据摘要
     */
    fun getStatisticsSummary(): StatisticsSummary {
        return StatisticsSummary(
            totalStudyMinutes = _currentUser.value.totalStudyMinutes,
            todayStudyMinutes = getTodayStudyMinutes(),
            weekStudyMinutes = getWeekStudyMinutes(),
            completedTasks = _tasks.value.count { it.status == TaskStatus.COMPLETED },
            totalCourses = _courses.value.size,
            totalNotes = _notes.value.size,
            unlockedAchievements = getUnlockedAchievementCount(),
            streakDays = _currentUser.value.streakDays
        )
    }

    // ==================== 课程归档 ====================

    private val _archivedCourses = mutableStateOf<List<Course>>(emptyList())
    val archivedCourses: State<List<Course>> = _archivedCourses

    /**
     * 归档课程
     */
    fun archiveCourse(courseId: String) {
        val course = _courses.value.find { it.id == courseId } ?: return
        _courses.value = _courses.value.filter { it.id != courseId }
        _archivedCourses.value = _archivedCourses.value + course.copy(isArchived = true)
        // 自动保存课程数据（包括已归档的）
        viewModelScope.launch {
            dataRepository.courses.saveCourses(_courses.value + _archivedCourses.value)
        }
    }

    /**
     * 取消归档课程
     */
    fun unarchiveCourse(courseId: String) {
        val course = _archivedCourses.value.find { it.id == courseId } ?: return
        _archivedCourses.value = _archivedCourses.value.filter { it.id != courseId }
        _courses.value = _courses.value + course.copy(isArchived = false)
        // 自动保存课程数据（包括已归档的）
        viewModelScope.launch {
            dataRepository.courses.saveCourses(_courses.value + _archivedCourses.value)
        }
    }

    /**
     * 删除课程（永久）
     */
    fun deleteCoursePermanently(courseId: String) {
        _courses.value = _courses.value.filter { it.id != courseId }
        _archivedCourses.value = _archivedCourses.value.filter { it.id != courseId }
        // 同时删除相关数据
        _tasks.value = _tasks.value.filter { it.courseId != courseId }
        _knowledgeNodes.value = _knowledgeNodes.value.filter { it.courseId != courseId }
        // 自动保存所有变更到本地
        viewModelScope.launch {
            dataRepository.courses.saveCourses(_courses.value)
            dataRepository.tasks.saveTasks(_tasks.value)
            dataRepository.knowledgeGraph.saveNodes(_knowledgeNodes.value)
        }
    }

    // ==================== 挑战系统 ====================

    private val _activeChallenges = mutableStateOf<List<Challenge>>(emptyList())
    val activeChallenges: State<List<Challenge>> = _activeChallenges

    /**
     * 初始化本周挑战
     */
    fun initWeeklyChallenges() {
        val weeklyChallenges = generateWeeklyChallenges()
        _activeChallenges.value = weeklyChallenges
    }

    /**
     * 更新挑战进度
     */
    fun updateChallengeProgress(type: ChallengeType, increment: Int = 1) {
        _activeChallenges.value = _activeChallenges.value.map { challenge ->
            if (challenge.type == type && !challenge.isCompleted) {
                val newProgress = (challenge.progress + increment).coerceAtMost(challenge.target)
                val isCompleted = newProgress >= challenge.target
                challenge.copy(
                    progress = newProgress,
                    isCompleted = isCompleted,
                    completedAt = if (isCompleted) System.currentTimeMillis() else null
                )
            } else {
                challenge
            }
        }

        // 发放已完成挑战的奖励
        _activeChallenges.value.filter { it.isCompleted && it.completedAt != null }.forEach { challenge ->
            if (challenge.progress >= challenge.target) {
                updateUser { it.copy(
                    energy = it.energy + challenge.rewards.energy,
                    crystals = it.crystals + challenge.rewards.crystals,
                    exp = it.exp + challenge.rewards.exp
                ) }
            }
        }
    }

    // ==================== 任务推荐 ====================

    private val _taskRecommendations = mutableStateOf<List<TaskRecommendation>>(emptyList())
    val taskRecommendations: State<List<TaskRecommendation>> = _taskRecommendations

    /**
     * 生成任务推荐
     */
    fun generateTaskRecommendations() {
        val recommendations = mutableListOf<TaskRecommendation>()
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        // 1. 基于遗忘曲线的推荐
        _courses.value.forEach { course ->
            val daysSinceLastStudy = if (course.lastStudiedAt != null) {
                ((now - course.lastStudiedAt) / (1000 * 60 * 60 * 24)).toInt()
            } else {
                7 // 从未学习
            }

            if (daysSinceLastStudy >= 2) {
                recommendations.add(
                    TaskRecommendation(
                        id = "rec_forget_${course.id}",
                        title = "${course.name}·复习提醒",
                        description = "距离上次学习已${daysSinceLastStudy}天，该复习啦！",
                        courseId = course.id,
                        type = TaskType.REVIEW_RITUAL,
                        estimatedMinutes = 20,
                        priority = if (daysSinceLastStudy >= 5) 5 else 3,
                        reason = "根据遗忘曲线，现在是最佳复习时机",
                        relevanceScore = 0.9f - (daysSinceLastStudy * 0.1f)
                    )
                )
            }
        }

        // 2. 基于掌握度的推荐
        _courses.value.filter { it.masteryLevel < 0.5f }.forEach { course ->
            recommendations.add(
                TaskRecommendation(
                    id = "rec_mastery_${course.id}",
                    title = "${course.name}·加强训练",
                    description = "当前掌握度${(course.masteryLevel * 100).toInt()}%，需要重点学习",
                    courseId = course.id,
                    type = TaskType.DEEP_EXPLORATION,
                    estimatedMinutes = 30,
                    priority = 4,
                    reason = "掌握度较低，建议重点突破",
                    relevanceScore = 0.8f
                )
            )
        }

        // 3. 基于课表的推荐
        val todaySchedule = getTodaySchedule(cal.get(Calendar.DAY_OF_WEEK))
        todaySchedule.forEach { (course, slot) ->
            val classTime = cal.apply {
                set(Calendar.HOUR_OF_DAY, slot.startHour)
                set(Calendar.MINUTE, slot.startMinute)
            }.timeInMillis
            val hoursBeforeClass = (classTime - now) / (1000 * 60 * 60)

            if (hoursBeforeClass in 1..4) {
                recommendations.add(
                    TaskRecommendation(
                        id = "rec_class_${course.id}",
                        title = "${course.name}·课前预习",
                        description = "还有${hoursBeforeClass}小时上课，快速预习一下",
                        courseId = course.id,
                        type = TaskType.DAILY_CARE,
                        estimatedMinutes = 15,
                        priority = 5,
                        reason = "即将上课，预习效果更好",
                        relevanceScore = 0.95f
                    )
                )
            }
        }

        // 4. 基于考试日期的推荐
        _courses.value.filter { it.examDate != null }.forEach { course ->
            val daysUntilExam = ((course.examDate!! - now) / (1000 * 60 * 60 * 24)).toInt()
            if (daysUntilExam in 1..14) {
                recommendations.add(
                    TaskRecommendation(
                        id = "rec_exam_${course.id}",
                        title = "${course.name}·考前冲刺",
                        description = "距离考试还有${daysUntilExam}天",
                        courseId = course.id,
                        type = TaskType.DEEP_EXPLORATION,
                        estimatedMinutes = 45,
                        priority = 5,
                        reason = "考试临近，建议集中复习",
                        relevanceScore = 0.98f
                    )
                )
            }
        }

        // 按相关度排序
        _taskRecommendations.value = recommendations.sortedByDescending { it.relevanceScore }
    }

    /**
     * 从推荐创建任务
     */
    fun createTaskFromRecommendation(recommendation: TaskRecommendation): Task {
        val task = Task(
            id = "task_${System.currentTimeMillis()}",
            title = recommendation.title,
            description = recommendation.description,
            type = recommendation.type,
            courseId = recommendation.courseId,
            priority = recommendation.priority,
            estimatedMinutes = recommendation.estimatedMinutes,
            rewards = Rewards(
                energy = recommendation.estimatedMinutes,
                crystals = recommendation.estimatedMinutes / 2,
                exp = recommendation.estimatedMinutes * 2
            )
        )
        _tasks.value = _tasks.value + task
        return task
    }

    // ==================== 学习会话 ====================

    private val _currentStudySession = mutableStateOf<StudySession?>(null)
    val currentStudySession: State<StudySession?> = _currentStudySession

    private val _studySessions = mutableStateOf<List<StudySession>>(emptyList())
    val studySessions: State<List<StudySession>> = _studySessions

    /**
     * 开始学习会话
     */
    fun startStudySession(courseId: String?, taskId: String?, type: StudyType) {
        _currentStudySession.value = StudySession(
            id = "session_${System.currentTimeMillis()}",
            userId = _currentUser.value.id,
            courseId = courseId,
            taskId = taskId,
            startTime = System.currentTimeMillis(),
            type = type
        )
    }

    /**
     * 结束学习会话
     */
    fun endStudySession(notes: String = "") {
        val session = _currentStudySession.value ?: return
        val endTime = System.currentTimeMillis()
        val durationMinutes = ((endTime - session.startTime) / 60000).toInt()

        val completedSession = session.copy(
            endTime = endTime,
            durationMinutes = durationMinutes,
            notes = notes
        )

        _studySessions.value = _studySessions.value + completedSession
        _currentStudySession.value = null
        // 自动保存学习会话
        viewModelScope.launch {
            dataRepository.studySessions.saveSessions(_studySessions.value)
        }

        // 更新课程学习时间
        if (session.courseId != null) {
            _courses.value = _courses.value.map { course ->
                if (course.id == session.courseId) {
                    course.copy(
                        totalStudyMinutes = course.totalStudyMinutes + durationMinutes,
                        lastStudiedAt = endTime,
                        studySessionCount = course.studySessionCount + 1
                    )
                } else course
            }
            // 自动保存课程数据到本地
            viewModelScope.launch {
                dataRepository.courses.saveCourses(_courses.value)
            }
        }

        // 更新用户总学习时间
        updateUser { it.copy(
            totalStudyMinutes = it.totalStudyMinutes + durationMinutes
        ) }

        // 更新挑战进度
        updateChallengeProgress(ChallengeType.DAILY_STUDY, durationMinutes)

        // 添加学习记录
        addStudyRecord(session.courseId, session.taskId, session.type, durationMinutes, notes)
    }

    // ==================== 番茄钟 ====================

    private val _pomodoroSettings = mutableStateOf(PomodoroSettings())
    val pomodoroSettings: State<PomodoroSettings> = _pomodoroSettings

    private val _pomodoroTimeLeft = mutableStateOf(25 * 60) // 秒
    val pomodoroTimeLeft: State<Int> = _pomodoroTimeLeft

    private val _pomodoroSessionCount = mutableStateOf(0)
    val pomodoroSessionCount: State<Int> = _pomodoroSessionCount

    private val _isPomodoroRunning = mutableStateOf(false)
    val isPomodoroRunning: State<Boolean> = _isPomodoroRunning

    private val _isPomodoroBreak = mutableStateOf(false)
    val isPomodoroBreak: State<Boolean> = _isPomodoroBreak

    /**
     * 更新番茄钟设置
     */
    fun updatePomodoroSettings(settings: PomodoroSettings) {
        _pomodoroSettings.value = settings
        if (!_isPomodoroRunning.value) {
            _pomodoroTimeLeft.value = settings.focusMinutes * 60
        }
        // 自动保存番茄钟设置
        viewModelScope.launch {
            dataRepository.pomodoroSettings.saveSettings(settings)
        }
    }

    /**
     * 开始番茄钟
     */
    fun startPomodoro() {
        _isPomodoroRunning.value = true
        if (!_isPomodoroBreak.value) {
            _pomodoroTimeLeft.value = _pomodoroSettings.value.focusMinutes * 60
        }
    }

    /**
     * 暂停番茄钟
     */
    fun pausePomodoro() {
        _isPomodoroRunning.value = false
    }

    /**
     * 重置番茄钟
     */
    fun resetPomodoro() {
        _isPomodoroRunning.value = false
        _isPomodoroBreak.value = false
        _pomodoroTimeLeft.value = _pomodoroSettings.value.focusMinutes * 60
    }

    /**
     * 番茄钟计时（每秒调用）
     */
    fun tickPomodoro() {
        if (!_isPomodoroRunning.value) return

        if (_pomodoroTimeLeft.value > 0) {
            _pomodoroTimeLeft.value -= 1
        } else {
            // 时间到
            if (_isPomodoroBreak.value) {
                // 休息结束，开始新的专注
                _isPomodoroBreak.value = false
                _pomodoroTimeLeft.value = _pomodoroSettings.value.focusMinutes * 60
            } else {
                // 专注结束
                _pomodoroSessionCount.value += 1

                // 检查是否需要长休息
                if (_pomodoroSessionCount.value % _pomodoroSettings.value.sessionsBeforeLongBreak == 0) {
                    _pomodoroTimeLeft.value = _pomodoroSettings.value.longBreakMinutes * 60
                } else {
                    _pomodoroTimeLeft.value = _pomodoroSettings.value.shortBreakMinutes * 60
                }
                _isPomodoroBreak.value = true
            }
        }
    }

    // ==================== 笔记附件 ====================

    private val _noteAttachments = mutableStateOf<Map<String, List<NoteAttachment>>>(emptyMap())
    val noteAttachments: State<Map<String, List<NoteAttachment>>> = _noteAttachments

    /**
     * 添加笔记附件
     */
    fun addNoteAttachment(noteId: String, attachment: NoteAttachment) {
        val currentList = _noteAttachments.value[noteId] ?: emptyList()
        _noteAttachments.value = _noteAttachments.value + (noteId to currentList + attachment)
        // 自动保存笔记附件
        viewModelScope.launch {
            dataRepository.noteAttachments.saveAttachments(_noteAttachments.value)
        }
    }

    /**
     * 删除笔记附件
     */
    fun removeNoteAttachment(noteId: String, attachmentId: String) {
        val currentList = _noteAttachments.value[noteId] ?: return
        _noteAttachments.value = _noteAttachments.value + (noteId to currentList.filter { it.id != attachmentId })
        // 自动保存笔记附件
        viewModelScope.launch {
            dataRepository.noteAttachments.saveAttachments(_noteAttachments.value)
        }
    }

    /**
     * 获取笔记附件列表
     */
    fun getNoteAttachments(noteId: String): List<NoteAttachment> {
        return _noteAttachments.value[noteId] ?: emptyList()
    }

    // ==================== 断签保护 ====================

    /**
     * 使用断签保护卡
     */
    fun useStreakProtectionCard(): Boolean {
        if (_currentUser.value.streakProtectionCards <= 0) return false

        updateUser { it.copy(
            streakProtectionCards = it.streakProtectionCards - 1
        ) }
        return true
    }

    /**
     * 获得断签保护卡
     */
    fun earnStreakProtectionCard(count: Int = 1) {
        updateUser { it.copy(
            streakProtectionCards = it.streakProtectionCards + count
        ) }
    }

    // ==================== 学习路径 ====================

    /**
     * 生成学习路径
     */
    fun generateLearningPath(courseId: String, targetNodeName: String): LearningPath? {
        val course = _courses.value.find { it.id == courseId } ?: return null
        val nodes = _knowledgeNodes.value.filter { it.courseId == courseId }
        val targetNode = nodes.find { it.name == targetNodeName } ?: return null

        // 递归获取所有前置节点
        val pathNodes = mutableListOf<LearningPathNode>()
        val visited = mutableSetOf<String>()

        fun collectPrerequisites(node: KnowledgeNode, order: Int): Int {
            if (node.id in visited) return order
            visited.add(node.id)

            var currentOrder = order
            for (parentId in node.parentIds) {
                val parent = nodes.find { it.id == parentId }
                if (parent != null && parent.id !in visited) {
                    currentOrder = collectPrerequisites(parent, currentOrder)
                }
            }

            pathNodes.add(
                LearningPathNode(
                    nodeId = node.id,
                    nodeName = node.name,
                    order = currentOrder,
                    isCompleted = node.masteryLevel >= 0.8f,
                    estimatedMinutes = node.difficulty * 15
                )
            )
            return currentOrder + 1
        }

        collectPrerequisites(targetNode, 0)

        // 按顺序排序
        pathNodes.sortBy { it.order }

        return LearningPath(
            id = "path_${courseId}_${targetNode.id}",
            courseId = courseId,
            title = "学习路径：${targetNode.name}",
            description = "从基础到${targetNode.name}的学习路径",
            nodes = pathNodes,
            totalEstimatedMinutes = pathNodes.sumOf { it.estimatedMinutes }
        )
    }
}

/**
 * 统计数据摘要
 */
data class StatisticsSummary(
    val totalStudyMinutes: Int,
    val todayStudyMinutes: Int,
    val weekStudyMinutes: Int,
    val completedTasks: Int,
    val totalCourses: Int,
    val totalNotes: Int,
    val unlockedAchievements: Int,
    val streakDays: Int
)