package com.keling.app.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keling.app.ai.AIResponse
import com.keling.app.ai.EnhancedLearningProfileProvider
import com.keling.app.ai.LearningContext
import com.keling.app.ai.ResponseType
import com.keling.app.ai.SimpleAIService
import com.keling.app.ai.ToolCommandParser
import com.keling.app.ai.tools.AiToolExecutor
import com.keling.app.ai.tools.DefaultNavigationTool
import com.keling.app.ai.tools.DefaultTaskTool
import com.keling.app.ai.tools.DefaultKnowledgeGraphTool
import com.keling.app.ai.tools.DefaultNoteTool
import com.keling.app.data.TaskStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * AI 聊天编排 ViewModel。
 *
 * - 管理聊天消息列表、加载状态和模式
 * - 负责拼接学习画像上下文，并调用 SimpleAIService
 * - 在拿到 toolCommandJson 后解析并调用工具层执行
 */
class AiChatViewModel(
    private val appViewModel: AppViewModel
) : ViewModel() {

    private val _state = mutableStateOf(
        AiChatUiState(
            messages = listOf(
                ChatMessageUiData(
                    content = "你好！我是恒星引擎 🌟\n\n我可以帮你制定计划、分析薄弱点、安排复习，还能直接在 App 里为你创建任务。",
                    isUser = false,
                    type = ResponseType.GENERAL
                )
            )
        )
    )
    val state: State<AiChatUiState> = _state

    private val toolExecutor = AiToolExecutor(
        taskTool = DefaultTaskTool(appViewModel),
        navigationTool = DefaultNavigationTool(appViewModel),
        noteTool = DefaultNoteTool(appViewModel),
        knowledgeGraphTool = DefaultKnowledgeGraphTool(appViewModel),
        scheduleTool = com.keling.app.ai.tools.DefaultScheduleTool(appViewModel)
    )

    fun send(text: String, mode: ChatMode = state.value.currentMode) {
        if (text.isBlank()) return
        val current = state.value
        if (current.isLoading) return

        // 1. 立即加入用户消息
        val userMsg = ChatMessageUiData(
            content = text,
            isUser = true,
            type = ResponseType.GENERAL
        )
        _state.value = current.copy(
            messages = current.messages + userMsg,
            isLoading = true,
            currentMode = mode,
            lastError = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            val context = buildLearningContext()
            val aiResponse: AIResponse = try {
                SimpleAIService.process(text, context)
            } catch (e: Exception) {
                AIResponse(
                    content = "恒星引擎暂时离线 🌑\n${e.message ?: ""}",
                    type = ResponseType.ERROR,
                    isFromAI = false
                )
            }

            withContext(Dispatchers.Main) {
                handleAiResponse(aiResponse, userMsg)
            }
        }
    }

    private fun buildLearningContext(): String {
        val cal = Calendar.getInstance()
        val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 7 else it }
        val user = appViewModel.currentUser.value
        val courses = appViewModel.courses.value
        val tasks = appViewModel.tasks.value
        val todaySchedule = appViewModel.getTodaySchedule(dayOfWeek)

        return buildString {
            appendLine("【用户画像】")
            appendLine("  姓名: ${user.name}, 等级: Lv.${user.level}")
            appendLine("  能量: ${user.energy}⚡ 结晶: ${user.crystals}💎")
            appendLine()

            appendLine("【今日状态】")
            val pending = tasks.filter { it.status == TaskStatus.PENDING }
            appendLine("  待办任务: ${pending.size}个")
            if (pending.isNotEmpty()) {
                append("  优先: ")
                pending.sortedByDescending { it.priority }.take(3).forEach {
                    append("${it.title} ")
                }
                appendLine()
            }
            appendLine()

            if (todaySchedule.isNotEmpty()) {
                appendLine("【今日课表】")
                todaySchedule.forEach { (course, slot) ->
                    appendLine("  ${course.name} ${slot.startHour}:${slot.startMinute}")
                }
                appendLine()
            }

            val weakCourses = courses.filter { it.masteryLevel < 0.6f }
            if (weakCourses.isNotEmpty()) {
                appendLine("【薄弱点】")
                weakCourses.take(3).forEach {
                    append("  ${it.name}(${(it.masteryLevel * 100).toInt()}%) ")
                }
                appendLine()
            }
        }
    }

    private fun handleAiResponse(
        ai: AIResponse,
        userMsg: ChatMessageUiData
    ) {
        val current = state.value
        val aiMsg = ChatMessageUiData(
            content = ai.content,
            isUser = false,
            type = ai.type
        )

        var messages = current.messages + userMsg + aiMsg
        var lastError: String? = null

        // 解析并执行工具指令（如果有）
        val cmd = ToolCommandParser.parse(ai.toolCommandJson)
        if (cmd != null) {
            val result = toolExecutor.execute(cmd)
            if (result.message.isNotBlank()) {
                val toolMsg = ChatMessageUiData(
                    content = result.message,
                    isUser = false,
                    type = if (result.success) ResponseType.GENERAL else ResponseType.ERROR
                )
                messages = messages + toolMsg
            }
            if (!result.success) {
                lastError = "部分 AI 建议的操作未能执行，但聊天内容仍然有效。"
            }
        }

        _state.value = current.copy(
            messages = messages,
            isLoading = false,
            lastError = lastError
        )
    }
}

data class AiChatUiState(
    val messages: List<ChatMessageUiData> = emptyList(),
    val isLoading: Boolean = false,
    val currentMode: ChatMode = ChatMode.GENERAL,
    val lastError: String? = null
)

data class ChatMessageUiData(
    val content: String,
    val isUser: Boolean,
    val type: ResponseType,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ChatMode {
    GENERAL,
    PLAN_TODAY,
    WEAKNESS_ANALYSIS,
    REVIEW_ADVICE
}