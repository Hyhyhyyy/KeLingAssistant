package com.keling.app.ai.tools

import com.keling.app.data.Course
import com.keling.app.data.KnowledgeNode
import com.keling.app.data.Rewards
import com.keling.app.data.ScheduleSlot
import com.keling.app.data.Task
import com.keling.app.data.TaskStatus
import com.keling.app.data.TaskType
import com.keling.app.viewmodel.AppViewModel

/**
 * ============ Tool 层 ============
 *
 * 这一层封装所有可以被 AI 驱动的“应用操作”，
 * 上层只需要通过这些接口即可，不直接操作 ViewModel 内部细节。
 */

interface NavigationTool {
    fun goTo(screen: String, courseId: String? = null)
}

interface TaskTool {
    fun createTask(
        title: String,
        type: String,
        courseId: String? = null,
        estimatedMinutes: Int? = null,
        priority: Int? = null
    ): Task

    fun updateTaskStatus(taskId: String, status: TaskStatus): Task?
}

interface NoteTool {
    fun createNoteFromAnswer(answerText: String, title: String?, courseId: String? = null)
}

interface KnowledgeGraphTool {
    fun upsertNode(
        courseId: String,
        name: String,
        description: String? = null,
        parentNames: List<String> = emptyList()
    ): KnowledgeNode

    fun deleteNode(courseId: String, name: String): Boolean

    fun updateNode(
        courseId: String,
        name: String,
        newName: String? = null,
        description: String? = null,
        masteryLevel: Float? = null
    ): KnowledgeNode?

    fun listNodes(courseId: String): List<KnowledgeNode>
}

interface ScheduleTool {
    /** 为课程添加课时，返回更新后的 Course 或 null */
    fun addSlot(courseIdOrName: String, dayOfWeek: Int, startHour: Int, startMinute: Int, durationMinutes: Int): Course?

    /** 删除某个课时，返回是否成功 */
    fun removeSlot(courseIdOrName: String, dayOfWeek: Int, startHour: Int, startMinute: Int): Boolean

    /** 列出课表，dayOfWeek 为 null 表示本周全部，否则为某天 */
    fun listSchedule(dayOfWeek: Int? = null): List<Pair<Course, ScheduleSlot>>
}

/**
 * 默认实现：直接基于当前的 AppViewModel 内存数据。
 *
 * - 目前仅实现 Task 相关，后续可以按需扩展 Course / Profile 等。
 */
class DefaultTaskTool(
    private val viewModel: AppViewModel
) : TaskTool {

    override fun createTask(
        title: String,
        type: String,
        courseId: String?,
        estimatedMinutes: Int?,
        priority: Int?
    ): Task {
        val resolvedType = parseTaskType(type)
        val baseMinutes = estimatedMinutes ?: defaultMinutesFor(resolvedType)
        val basePriority = priority ?: defaultPriorityFor(resolvedType)
        val rewards = rewardsFor(resolvedType, baseMinutes)

        val task = Task(
            id = "ai_${System.currentTimeMillis()}",
            title = title,
            description = title,
            type = resolvedType,
            courseId = courseId,
            priority = basePriority,
            estimatedMinutes = baseMinutes,
            rewards = rewards
        )

        // 通过 viewModel 暴露的任务列表追加（目前没有专门 addTask 方法，只能通过 setter 实现）
        val current = viewModel.tasks.value
        viewModel.setTasks(current + task)
        return task
    }

    override fun updateTaskStatus(taskId: String, status: TaskStatus): Task? {
        val current = viewModel.tasks.value
        var updatedTask: Task? = null
        val updated = current.map { task ->
            if (task.id == taskId) {
                val newTask = task.copy(status = status)
                updatedTask = newTask
                newTask
            } else {
                task
            }
        }
        if (updatedTask != null) {
            viewModel.setTasks(updated)
        }
        return updatedTask
    }

    private fun parseTaskType(raw: String): TaskType {
        return when (raw.uppercase()) {
            "DEEP_EXPLORATION" -> TaskType.DEEP_EXPLORATION
            "REVIEW_RITUAL" -> TaskType.REVIEW_RITUAL
            "BOUNTY" -> TaskType.BOUNTY
            "RESCUE" -> TaskType.RESCUE
            else -> TaskType.DAILY_CARE
        }
    }

    private fun defaultMinutesFor(type: TaskType): Int {
        return when (type) {
            TaskType.DAILY_CARE -> 25
            TaskType.REVIEW_RITUAL -> 15
            TaskType.DEEP_EXPLORATION -> 60
            TaskType.BOUNTY, TaskType.RESCUE -> 45
        }
    }

    private fun defaultPriorityFor(type: TaskType): Int {
        return when (type) {
            TaskType.DAILY_CARE -> 3
            TaskType.REVIEW_RITUAL -> 3
            TaskType.DEEP_EXPLORATION -> 4
            TaskType.BOUNTY, TaskType.RESCUE -> 5
        }
    }

    private fun rewardsFor(type: TaskType, minutes: Int): Rewards {
        return when (type) {
            TaskType.DAILY_CARE -> Rewards(
                energy = 10 + minutes / 10,
                crystals = 5,
                exp = 15 + minutes / 5
            )
            TaskType.REVIEW_RITUAL -> Rewards(
                energy = 8 + minutes / 10,
                crystals = 8 + minutes / 10,
                exp = 15 + minutes / 5
            )
            TaskType.DEEP_EXPLORATION -> Rewards(
                energy = 20 + minutes / 5,
                crystals = 12 + minutes / 10,
                exp = 40 + minutes / 2
            )
            TaskType.BOUNTY -> Rewards(
                energy = 25 + minutes / 5,
                crystals = 20 + minutes / 5,
                exp = 60 + minutes
            )
            TaskType.RESCUE -> Rewards(
                energy = 18 + minutes / 5,
                crystals = 18 + minutes / 5,
                exp = 50 + minutes
            )
        }
    }
}

/**
 * 默认导航实现：基于 AppViewModel 内部的导航状态。
 *
 * - 仅允许跳转到白名单页面，避免模型输出任意字符串导致异常
 */
class DefaultNavigationTool(
    private val viewModel: AppViewModel
) : NavigationTool {

    override fun goTo(screen: String, courseId: String?) {
        val normalized = when (screen.lowercase()) {
            "home", "homepage" -> "home"
            "ai", "chat", "assistant" -> "ai"
            "tasks", "task_list" -> "tasks"
            "greenhouse", "planet", "courses" -> "greenhouse"
            "temple", "wisdom", "study_hall" -> "temple"
            "nebula", "collab", "team" -> "nebula"
            "schedule", "schedule_edit", "timetable", "课表" -> "schedule_edit"
            else -> "home"
        }
        viewModel.navigateTo(normalized)
        // 目前未使用 courseId，后续可以在进入 greenhouse 时根据课程做进一步筛选
    }
}

class DefaultKnowledgeGraphTool(
    private val viewModel: AppViewModel
) : KnowledgeGraphTool {

    override fun upsertNode(
        courseId: String,
        name: String,
        description: String?,
        parentNames: List<String>
    ): KnowledgeNode {
        val course = viewModel.courses.value.find { it.id == courseId || it.name == courseId }
        val finalCourseId = course?.id ?: courseId

        val existing = viewModel.findKnowledgeNodeByNameInCourse(finalCourseId, name)

        val parentIds: List<String> =
            if (parentNames.isEmpty()) {
                existing?.parentIds ?: emptyList()
            } else {
                parentNames.mapNotNull { parentName ->
                    viewModel.findKnowledgeNodeByNameInCourse(finalCourseId, parentName)?.id
                }
            }

        val base = existing ?: KnowledgeNode(
            id = "kg_${System.currentTimeMillis()}",
            courseId = finalCourseId,
            name = name
        )

        val node = base.copy(
            description = description ?: base.description,
            parentIds = if (parentIds.isNotEmpty()) parentIds else base.parentIds
        )

        return viewModel.upsertKnowledgeNode(node)
    }

    override fun deleteNode(courseId: String, name: String): Boolean {
        val course = viewModel.courses.value.find { it.id == courseId || it.name == courseId }
        val finalCourseId = course?.id ?: courseId
        return viewModel.deleteKnowledgeNodeByCourseAndName(finalCourseId, name)
    }

    override fun updateNode(
        courseId: String,
        name: String,
        newName: String?,
        description: String?,
        masteryLevel: Float?
    ): KnowledgeNode? {
        val course = viewModel.courses.value.find { it.id == courseId || it.name == courseId }
        val finalCourseId = course?.id ?: courseId
        val existing = viewModel.findKnowledgeNodeByNameInCourse(finalCourseId, name) ?: return null

        val updated = existing.copy(
            name = newName ?: existing.name,
            description = description ?: existing.description,
            masteryLevel = masteryLevel ?: existing.masteryLevel
        )
        return viewModel.upsertKnowledgeNode(updated)
    }

    override fun listNodes(courseId: String): List<KnowledgeNode> {
        val course = viewModel.courses.value.find { it.id == courseId || it.name == courseId }
        val finalCourseId = course?.id ?: courseId
        return viewModel.knowledgeNodesForCourse(finalCourseId)
    }
}

class DefaultScheduleTool(
    private val viewModel: AppViewModel
) : ScheduleTool {

    override fun addSlot(courseIdOrName: String, dayOfWeek: Int, startHour: Int, startMinute: Int, durationMinutes: Int): Course? {
        val course = viewModel.courses.value.find { it.id == courseIdOrName || it.name == courseIdOrName } ?: return null
        return viewModel.addScheduleSlot(course.id, ScheduleSlot(dayOfWeek, startHour, startMinute, durationMinutes))
    }

    override fun removeSlot(courseIdOrName: String, dayOfWeek: Int, startHour: Int, startMinute: Int): Boolean {
        val course = viewModel.courses.value.find { it.id == courseIdOrName || it.name == courseIdOrName } ?: return false
        return viewModel.removeScheduleSlot(course.id, dayOfWeek, startHour, startMinute)
    }

    override fun listSchedule(dayOfWeek: Int?): List<Pair<Course, ScheduleSlot>> {
        return if (dayOfWeek != null) {
            viewModel.getTodaySchedule(dayOfWeek)
        } else {
            viewModel.getWeekSchedule()
        }
    }
}

/**
 * 笔记工具默认实现：支持从 AI 对话内容创建学习笔记
 */
class DefaultNoteTool(
    private val viewModel: AppViewModel
) : NoteTool {

    override fun createNoteFromAnswer(answerText: String, title: String?, courseId: String?) {
        val noteTitle = title ?: generateTitleFromContent(answerText)
        val note = com.keling.app.data.Note(
            id = "note_ai_${System.currentTimeMillis()}",
            title = noteTitle,
            content = answerText,
            sourceType = com.keling.app.data.NoteSource.AI_GENERATED,
            aiExplanation = "由 AI 助手生成",
            tags = extractTags(answerText),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        viewModel.addNote(note)
    }

    private fun generateTitleFromContent(content: String): String {
        val firstLine = content.lines().firstOrNull { it.isNotBlank() } ?: "学习笔记"
        return firstLine.take(30).let { if (firstLine.length > 30) "$it..." else it }
    }

    private fun extractTags(content: String): List<String> {
        val tags = mutableListOf<String>()
        val keywords = listOf("算法", "数据结构", "编程", "数学", "英语", "物理", "化学", "历史", "政治", "经济学", "计算机", "网络", "数据库", "人工智能", "机器学习", "深度学习")
        keywords.forEach { keyword ->
            if (content.contains(keyword)) {
                tags.add(keyword)
            }
        }
        return tags.take(3)
    }
}

