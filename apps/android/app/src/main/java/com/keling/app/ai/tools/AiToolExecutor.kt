package com.keling.app.ai.tools

import com.keling.app.ai.ToolAction
import com.keling.app.ai.ToolCommand
import com.keling.app.data.TaskStatus
import com.keling.app.data.fromJson
import com.keling.app.data.json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

data class ToolExecutionResult(
    val success: Boolean,
    val action: ToolAction,
    val message: String
)

/**
 * 负责根据 ToolCommand 调用具体的 Tool 实现。
 *
 * - 所有异常都被吞掉并转化为失败结果，避免影响聊天主流程
 */
class AiToolExecutor(
    private val taskTool: TaskTool? = null,
    private val navigationTool: NavigationTool? = null,
    private val noteTool: NoteTool? = null,
    private val knowledgeGraphTool: KnowledgeGraphTool? = null,
    private val scheduleTool: ScheduleTool? = null
) {

    fun execute(command: ToolCommand): ToolExecutionResult {
        return try {
            when (command.action) {
                ToolAction.NO_ACTION -> ToolExecutionResult(
                    success = true,
                    action = command.action,
                    message = "无需执行应用内操作。"
                )

                ToolAction.CREATE_TASK -> {
                    val tool = requireNotNull(taskTool) { "TaskTool not provided" }

                    val paramsObj = json.parseToJsonElement(command.rawParamsJson).jsonObject

                    fun str(vararg keys: String): String? {
                        for (key in keys) {
                            val primitive = paramsObj[key] as? JsonPrimitive ?: continue
                            val value = primitive.content
                            if (value.isNotBlank()) return value
                        }
                        return null
                    }

                    fun int(vararg keys: String): Int? {
                        for (key in keys) {
                            val primitive = paramsObj[key] as? JsonPrimitive ?: continue
                            val value = primitive.content.toIntOrNull()
                            if (value != null) return value
                        }
                        return null
                    }

                    val title = str("title", "name", "task_title")
                    if (title.isNullOrBlank()) {
                        return ToolExecutionResult(
                            success = false,
                            action = command.action,
                            message = "AI 提供的任务缺少标题，暂时无法创建。可以请我重新用更清晰的标题生成一次任务。"
                        )
                    }

                    val type = str("type", "task_type") ?: "DAILY_CARE"
                    val courseId = str("courseId", "course_id")
                    val estimatedMinutes = int("estimatedMinutes", "estimated_minutes", "duration", "duration_minutes")
                    val priority = int("priority", "priority_level")

                    val task = tool.createTask(
                        title = title,
                        type = type,
                        courseId = courseId,
                        estimatedMinutes = estimatedMinutes,
                        priority = priority
                    )
                    ToolExecutionResult(
                        success = true,
                        action = command.action,
                        message = "已为你创建任务「${task.title}」，预计 ${task.estimatedMinutes} 分钟。"
                    )
                }

                ToolAction.UPDATE_TASK_STATUS -> {
                    val params: UpdateTaskStatusParams = command.rawParamsJson.fromJson()
                    val tool = requireNotNull(taskTool) { "TaskTool not provided" }
                    val status = when (params.status.uppercase()) {
                        "IN_PROGRESS" -> TaskStatus.IN_PROGRESS
                        "COMPLETED" -> TaskStatus.COMPLETED
                        "ABANDONED" -> TaskStatus.ABANDONED
                        else -> TaskStatus.PENDING
                    }
                    val updated = tool.updateTaskStatus(params.taskId, status)
                    if (updated != null) {
                        ToolExecutionResult(
                            success = true,
                            action = command.action,
                            message = "已更新任务「${updated.title}」状态为 $status。"
                        )
                    } else {
                        ToolExecutionResult(
                            success = false,
                            action = command.action,
                            message = "没有找到要更新的任务。"
                        )
                    }
                }

                ToolAction.GO_TO_SCREEN -> {
                    val params: GoToScreenParams = command.rawParamsJson.fromJson()
                    val tool = requireNotNull(navigationTool) { "NavigationTool not provided" }
                    tool.goTo(params.screen, params.courseId)
                    ToolExecutionResult(
                        success = true,
                        action = command.action,
                        message = "已为你切换到「${params.screen}」页面。"
                    )
                }

                ToolAction.CREATE_NOTE_FROM_ANSWER -> {
                    val params: CreateNoteFromAnswerParams = command.rawParamsJson.fromJson()
                    val tool = requireNotNull(noteTool) { "NoteTool not provided" }
                    tool.createNoteFromAnswer(
                        answerText = params.answerText,
                        title = params.title,
                        courseId = params.courseId
                    )
                    ToolExecutionResult(
                        success = true,
                        action = command.action,
                        message = "已将本次 AI 解释保存为笔记。"
                    )
                }

                ToolAction.UPSERT_KG_NODE -> {
                    val tool = requireNotNull(knowledgeGraphTool) { "KnowledgeGraphTool not provided" }
                    val params: UpsertKgNodeParams = command.rawParamsJson.fromJson()
                    val node = tool.upsertNode(
                        courseId = params.courseId,
                        name = params.name,
                        description = params.description,
                        parentNames = params.parentNames ?: emptyList()
                    )
                    ToolExecutionResult(
                        success = true,
                        action = command.action,
                        message = "已在「${params.courseId}」的知识图谱中创建/更新节点「${node.name}」。"
                    )
                }

                ToolAction.DELETE_KG_NODE -> {
                    val tool = requireNotNull(knowledgeGraphTool) { "KnowledgeGraphTool not provided" }
                    val params: DeleteKgNodeParams = command.rawParamsJson.fromJson()
                    val ok = tool.deleteNode(params.courseId, params.name)
                    ToolExecutionResult(
                        success = ok,
                        action = command.action,
                        message = if (ok) {
                            "已从「${params.courseId}」的知识图谱中删除节点「${params.name}」。"
                        } else {
                            "没有在「${params.courseId}」的知识图谱中找到要删除的节点「${params.name}」。"
                        }
                    )
                }

                ToolAction.UPDATE_KG_NODE -> {
                    val tool = requireNotNull(knowledgeGraphTool) { "KnowledgeGraphTool not provided" }
                    val params: UpdateKgNodeParams = command.rawParamsJson.fromJson()
                    val updated = tool.updateNode(
                        courseId = params.courseId,
                        name = params.name,
                        newName = params.newName,
                        description = params.description,
                        masteryLevel = params.masteryLevel
                    )
                    if (updated != null) {
                        ToolExecutionResult(
                            success = true,
                            action = command.action,
                            message = "已更新「${params.courseId}」知识图谱中节点「${updated.name}」的信息。"
                        )
                    } else {
                        ToolExecutionResult(
                            success = false,
                            action = command.action,
                            message = "没有在「${params.courseId}」中找到要更新的节点「${params.name}」。"
                        )
                    }
                }

                ToolAction.LIST_KG_NODES -> {
                    val tool = requireNotNull(knowledgeGraphTool) { "KnowledgeGraphTool not provided" }
                    val params: ListKgNodesParams = command.rawParamsJson.fromJson()
                    val nodes = tool.listNodes(params.courseId)
                    val summary = if (nodes.isEmpty()) {
                        "当前「${params.courseId}」的知识图谱还没有任何节点。"
                    } else {
                        val lines = nodes.take(20).joinToString("\n") { n ->
                            "- ${n.name}（掌握度 ${(n.masteryLevel * 100).toInt()}%）"
                        }
                        "「${params.courseId}」知识图谱中的节点：\n$lines"
                    }
                    ToolExecutionResult(
                        success = true,
                        action = command.action,
                        message = summary
                    )
                }

                ToolAction.BATCH_UPSERT_KG_NODES -> {
                    val tool = requireNotNull(knowledgeGraphTool) { "KnowledgeGraphTool not provided" }
                    val params: BatchUpsertKgNodesParams = command.rawParamsJson.fromJson()

                    if (params.nodes.isEmpty()) {
                        return ToolExecutionResult(
                            success = false,
                            action = command.action,
                            message = "没有提供要创建的知识节点。"
                        )
                    }

                    val createdNodes = mutableListOf<String>()
                    params.nodes.forEach { nodeParams ->
                        val node = tool.upsertNode(
                            courseId = params.courseId,
                            name = nodeParams.name,
                            description = nodeParams.description,
                            parentNames = nodeParams.parentNames ?: emptyList()
                        )
                        createdNodes.add(node.name)
                    }

                    ToolExecutionResult(
                        success = true,
                        action = command.action,
                        message = "已在「${params.courseId}」中批量创建 ${createdNodes.size} 个知识点：${createdNodes.joinToString("、")}。"
                    )
                }

                ToolAction.ADD_SCHEDULE_SLOT -> {
                    val tool = scheduleTool ?: return@execute ToolExecutionResult(
                        success = false,
                        action = command.action,
                        message = "课表工具未就绪。"
                    )
                    val params = command.rawParamsJson.fromJson<AddScheduleSlotParams>()
                    val course = tool.addSlot(
                        params.courseId ?: params.courseName ?: "",
                        params.dayOfWeek,
                        params.startHour,
                        params.startMinute,
                        params.durationMinutes
                    )
                    if (course != null) {
                        val dow = when (params.dayOfWeek) { 1 -> "一" 2 -> "二" 3 -> "三" 4 -> "四" 5 -> "五" 6 -> "六" 7 -> "日" else -> "?" }
                        ToolExecutionResult(
                            success = true,
                            action = command.action,
                            message = "已为「${course.name}」添加课时：周$dow ${params.startHour}:${"%02d".format(params.startMinute)}，时长 ${params.durationMinutes} 分钟。"
                        )
                    } else {
                        ToolExecutionResult(
                            success = false,
                            action = command.action,
                            message = "未找到对应课程，请确认课程名称正确。"
                        )
                    }
                }

                ToolAction.REMOVE_SCHEDULE_SLOT -> {
                    val tool = scheduleTool ?: return@execute ToolExecutionResult(
                        success = false,
                        action = command.action,
                        message = "课表工具未就绪。"
                    )
                    val params = command.rawParamsJson.fromJson<RemoveScheduleSlotParams>()
                    val ok = tool.removeSlot(
                        params.courseId ?: params.courseName ?: "",
                        params.dayOfWeek,
                        params.startHour,
                        params.startMinute
                    )
                    ToolExecutionResult(
                        success = ok,
                        action = command.action,
                        message = if (ok) "已删除该课时。" else "未找到要删除的课时。"
                    )
                }

                ToolAction.LIST_SCHEDULE -> {
                    val tool = scheduleTool ?: return@execute ToolExecutionResult(
                        success = false,
                        action = command.action,
                        message = "课表工具未就绪。"
                    )
                    val params = kotlin.runCatching { command.rawParamsJson.fromJson<ListScheduleParams>() }.getOrNull()
                    val dayOfWeek = params?.dayOfWeek
                    val slots = tool.listSchedule(dayOfWeek)
                    val dayNames = listOf("", "周一", "周二", "周三", "周四", "周五", "周六", "周日")
                    val summary = if (slots.isEmpty()) {
                        if (dayOfWeek != null) "当天暂无课程安排。" else "本周暂无课程安排。"
                    } else {
                        slots.take(30).joinToString("\n") { (c, s) ->
                            val endH = s.startHour + (s.startMinute + s.durationMinutes) / 60
                            val endM = (s.startMinute + s.durationMinutes) % 60
                            val timeStr = "%02d:%02d-%02d:%02d".format(s.startHour, s.startMinute, endH, endM)
                            "- ${dayNames.getOrElse(s.dayOfWeek) { "周?" }} ${c.name} $timeStr"
                        }
                    }
                    ToolExecutionResult(success = true, action = command.action, message = summary)
                }
            }
        } catch (e: Exception) {
            ToolExecutionResult(
                success = false,
                action = command.action,
                message = "AI 建议的操作执行失败，但聊天内容仍然有效。"
            )
        }
    }
}

// ============ 内部用的参数数据类（用于 JSON 解析） ============

@kotlinx.serialization.Serializable
private data class UpdateTaskStatusParams(
    val taskId: String,
    val status: String
)

@kotlinx.serialization.Serializable
private data class GoToScreenParams(
    val screen: String,
    val courseId: String? = null
)

@kotlinx.serialization.Serializable
private data class CreateNoteFromAnswerParams(
    val answerText: String,
    val title: String? = null,
    val courseId: String? = null
)

@kotlinx.serialization.Serializable
private data class UpsertKgNodeParams(
    val courseId: String,
    val name: String,
    val description: String? = null,
    val parentNames: List<String>? = null
)

@kotlinx.serialization.Serializable
private data class DeleteKgNodeParams(
    val courseId: String,
    val name: String
)

@kotlinx.serialization.Serializable
private data class UpdateKgNodeParams(
    val courseId: String,
    val name: String,
    val newName: String? = null,
    val description: String? = null,
    val masteryLevel: Float? = null
)

@kotlinx.serialization.Serializable
private data class ListKgNodesParams(
    val courseId: String
)

@kotlinx.serialization.Serializable
private data class BatchUpsertKgNodesParams(
    val courseId: String,
    val nodes: List<KgNodeItemParams>
)

@kotlinx.serialization.Serializable
private data class KgNodeItemParams(
    val name: String,
    val description: String? = null,
    val parentNames: List<String>? = null
)

@kotlinx.serialization.Serializable
private data class AddScheduleSlotParams(
    val courseId: String? = null,
    val courseName: String? = null,
    val dayOfWeek: Int = 1,
    val startHour: Int = 8,
    val startMinute: Int = 0,
    val durationMinutes: Int = 90
)

@kotlinx.serialization.Serializable
private data class RemoveScheduleSlotParams(
    val courseId: String? = null,
    val courseName: String? = null,
    val dayOfWeek: Int,
    val startHour: Int,
    val startMinute: Int
)

@kotlinx.serialization.Serializable
private data class ListScheduleParams(
    val dayOfWeek: Int? = null
)

