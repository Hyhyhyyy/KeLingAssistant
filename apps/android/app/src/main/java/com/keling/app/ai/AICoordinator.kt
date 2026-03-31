package com.keling.app.ai

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * =========================
 * AI 协调器（升级版）
 * =========================
 *
 * 功能：
 * - 编排本地规则与云端调用
 * - 整合对话上下文管理
 * - 整合多轮对话管理
 * - 整合学习画像分析
 * - 支持场景化 System Prompt
 * - 完善的工具指令解析
 */

/**
 * AI 协调器
 */
class AICoordinator(
    private val localRuleEngine: EnhancedLocalRuleEngine = EnhancedLocalRuleEngine(),
    private val deepSeekClient: DeepSeekClient = DeepSeekClient(),
    private val memoryManager: ChatMemoryManager = ChatMemoryManager(),
    private val conversationManager: ConversationManager = ConversationManager(memoryManager),
    private val profileProvider: EnhancedLearningProfileProvider? = null
) {

    /**
     * 处理用户输入
     */
    suspend fun process(input: String, context: LearningContext? = null): AIResponse {
        // 1. 添加用户消息到历史
        memoryManager.addUserMessage(input)

        // 2. 获取学习上下文
        val learningContext = context ?: buildDefaultContext()

        // 3. 先尝试本地规则引擎
        val localResponse = localRuleEngine.process(input, learningContext)
        if (localResponse != null) {
            memoryManager.addAssistantMessage(
                localResponse.content,
                parseToolAction(localResponse.toolCommandJson)
            )
            return localResponse
        }

        // 4. 构建完整请求
        val systemPrompt = buildEnhancedSystemPrompt()
        val userPrompt = buildUserPrompt(input, learningContext)

        // 5. 调用云端 AI
        val rawResponse = try {
            deepSeekClient.chat(
                systemPrompt = systemPrompt,
                userPrompt = userPrompt
            )
        } catch (e: Exception) {
            // 云端失败，返回友好提示
            return AIResponse(
                content = "抱歉，网络连接出现问题，请稍后再试。如果问题持续，可以尝试检查网络设置。",
                type = ResponseType.GENERAL,
                isFromAI = true
            )
        }

        // 6. 解析响应
        val (cleanText, toolJson) = extractToolCommand(rawResponse)

        // 7. 添加到历史
        memoryManager.addAssistantMessage(
            cleanText,
            parseToolAction(toolJson),
            toolJson
        )

        // 8. 返回响应
        return AIResponse(
            content = cleanText,
            type = classifyResponse(cleanText),
            isFromAI = true,
            toolCommandJson = toolJson
        )
    }

    /**
     * 带场景的处理
     */
    suspend fun processWithScenario(
        input: String,
        scenario: AIScenario,
        context: LearningContext? = null
    ): AIResponse {
        // 设置场景
        memoryManager.setScenario(scenario)

        // 添加用户消息
        memoryManager.addUserMessage(input, scenario)

        // 获取学习上下文
        val learningContext = context ?: buildDefaultContext()

        // 构建场景化 System Prompt
        val systemPrompt = buildScenarioPrompt(scenario)
        val userPrompt = buildUserPrompt(input, learningContext)

        // 调用云端
        val rawResponse = deepSeekClient.chat(systemPrompt, userPrompt)

        // 解析
        val (cleanText, toolJson) = extractToolCommand(rawResponse)

        // 添加到历史
        memoryManager.addAssistantMessage(cleanText, parseToolAction(toolJson), toolJson)

        return AIResponse(
            content = cleanText,
            type = classifyResponse(cleanText),
            isFromAI = true,
            toolCommandJson = toolJson
        )
    }

    /**
     * 处理对话管理器的结果
     */
    suspend fun processManagedInput(input: String, context: LearningContext? = null): AIResponse {
        val result = conversationManager.processInput(input)

        return when (result) {
            is ProcessResult.ScenarioChanged -> {
                AIResponse(
                    content = "已切换到「${result.scenario.displayName}」模式。${result.scenario.description}",
                    type = ResponseType.GENERAL,
                    isFromAI = false
                )
            }

            is ProcessResult.NeedClarification -> {
                AIResponse(
                    content = result.question.question,
                    type = ResponseType.GENERAL,
                    isFromAI = false,
                    // 可以附加选项
                    toolCommandJson = null
                )
            }

            is ProcessResult.ReadyToExecute -> {
                // 目标信息足够，生成执行响应
                process(input, context)
            }

            is ProcessResult.QuickActionDetected -> {
                handleQuickAction(result.action)
            }

            is ProcessResult.NormalChat -> {
                process(result.input, context)
            }
        }
    }

    /**
     * 处理快捷动作
     */
    private fun handleQuickAction(action: QuickAction): AIResponse {
        return when (action) {
            is QuickAction.Navigate -> AIResponse(
                content = "好的，正在跳转...",
                type = ResponseType.GENERAL,
                isFromAI = false,
                toolCommandJson = """{"action":"GO_TO","params":{"screen":"${action.screen}"}}"""
            )
            QuickAction.StartLearning -> AIResponse(
                content = "开始学习模式！让我们一起进步 🚀",
                type = ResponseType.PLAN,
                isFromAI = false
            )
            QuickAction.TakeBreak -> AIResponse(
                content = "休息一下也是学习的一部分 🌙",
                type = ResponseType.GENERAL,
                isFromAI = false
            )
        }
    }

    // ==================== Prompt 构建 ====================

    /**
     * 构建增强版 System Prompt
     */
    private fun buildEnhancedSystemPrompt(): String {
        val scenarioPrompt = conversationManager.getScenarioPrompt()

        return """
你是一位温暖而专业的学习导师，同时也是学习游戏的主持人。

【核心身份】
- 名称：恒星引擎
- 性格：耐心、鼓励、偶尔调皮
- 风格：简洁有力，不过度解释

【回答原则】
1. 优先结合用户学习画像和当前上下文
2. 每次回答控制在3-5句话，除非用户要求详细解释
3. 关键概念用「」标注，数字用具体数据
4. 适度使用 🌱⚡💎，营造轻度游戏化氛围

【多轮对话策略】
- 如果用户问题模糊，追问一个澄清问题
- 如果涉及复杂任务规划，分步引导
- 如果用户表达挫败，先共情再给建议

【工具指令规范（非常重要）】
- 在回答末尾单独一行或几行输出 JSON
- 单个指令格式：{"action":"ACTION_TYPE","params":{...}}
- 多个指令格式（用数组）：[{"action":"CREATE_TASK","params":{...}},{"action":"GO_TO","params":{...}}]
- 无操作时：{"action":"NO_ACTION","params":{}}

【多指令执行】
- 当用户要求执行多个操作时，可以一次返回多个指令
- 例如："帮我创建三个复习任务并跳转到任务页" → 返回包含3个CREATE_TASK和1个GO_TO的数组
- 所有指令会按顺序依次执行

【工具指令示例】
- 创建任务：{"action":"CREATE_TASK","params":{"title":"复习高数","estimatedMinutes":30}}
- 页面导航：{"action":"GO_TO","params":{"screen":"tasks"}}
- 添加课时：{"action":"ADD_SCHEDULE_SLOT","params":{"courseId":"高等数学","dayOfWeek":1,"startHour":8,"startMinute":0,"durationMinutes":90}}
- 保存笔记：{"action":"CREATE_NOTE_FROM_ANSWER","params":{"title":"学习笔记标题","answerText":"笔记内容"}}
- 添加单个知识点：{"action":"UPSERT_KG_NODE","params":{"courseId":"高等数学","name":"极限","description":"极限是...","parentNames":["函数"]}}
- 批量添加知识点：{"action":"BATCH_UPSERT_KG_NODES","params":{"courseId":"高等数学","nodes":[{"name":"极限","description":"极限的定义"},{"name":"导数","description":"导数的概念","parentNames":["极限"]},{"name":"积分","description":"积分的概念","parentNames":["导数"]}]}}
- 更新知识节点：{"action":"UPDATE_KG_NODE","params":{"courseId":"高等数学","name":"极限","masteryLevel":0.8}}
- 删除知识节点：{"action":"DELETE_KG_NODE","params":{"courseId":"高等数学","name":"极限"}}
- 查看知识图谱：{"action":"LIST_KG_NODES","params":{"courseId":"高等数学"}}

【知识图谱操作完整指南】

1. 添加单个知识点（UPSERT_KG_NODE）：
   - courseId：课程名称或ID
   - name：知识点名称
   - description：知识点描述（可选）
   - parentNames：父知识点名称列表（可选，用于建立层级关系）

2. 批量添加知识点（BATCH_UPSERT_KG_NODES）【推荐用于创建多个知识点】：
   - 用户说："给高数添加极限、导数、积分三个知识点" / "创建高数的知识图谱，包含..."
   - 一次性创建多个知识点，支持设置父子关系
   - 示例：{"action":"BATCH_UPSERT_KG_NODES","params":{"courseId":"高等数学","nodes":[
     {"name":"极限","description":"研究函数变化趋势的重要工具"},
     {"name":"导数","description":"函数变化率的精确描述","parentNames":["极限"]},
     {"name":"积分","description":"求和的极限","parentNames":["导数"]}
   ]}}
   - parentNames 会自动建立父子关系，形成思维导图的层级结构

3. 更新知识点（UPDATE_KG_NODE）：
   - masteryLevel 是 0-1 之间的小数，0.8 表示 80%

4. 删除知识点（DELETE_KG_NODE）：
   - 只需要 courseId 和 name

5. 查看知识图谱（LIST_KG_NODES）：
   - 会返回该课程所有知识点列表

【创建知识图谱的最佳实践】
- 当用户要求创建知识图谱时，优先使用 BATCH_UPSERT_KG_NODES
- 建议按学科特点组织层级，例如：
  * 数学：基础概念 → 核心定理 → 应用
  * 编程：基础语法 → 数据结构 → 算法
  * 语言：词汇 → 语法 → 阅读理解
- 合理设置 parentNames 建立知识之间的依赖关系
- 每个知识点描述简洁明了（10-30字）

【笔记功能指南】
- "帮我记下来"、"保存这个"、"记笔记" → 使用 CREATE_NOTE_FROM_ANSWER
- answerText 是要保存的内容，title 是笔记标题（可选）
当解释完一个重要概念后，可以主动询问"需要我把这个知识点记录下来吗？"

【重要：课程名称匹配】
- courseId 参数必须使用用户实际拥有的课程名称（见【我的课程】列表）
- 如果用户提到的课程不在列表中，请先询问用户是否要创建新课程
- 例如：用户说"给英语添加知识点"但课程列表没有英语，则询问"你还没有英语这门课程，是添加到现有课程还是先创建英语课程？"

${scenarioPrompt ?: ""}

【重要提醒】
- 除了最后一行的 JSON，其余内容都应该是给用户看的自然语言
- 制定今日计划时，请结合 Schedule 上下文中的课程安排
- 如果不确定用户意图，先询问而非猜测
        """.trimIndent()
    }

    /**
     * 构建场景化 System Prompt
     */
    private fun buildScenarioPrompt(scenario: AIScenario): String {
        val basePrompt = buildEnhancedSystemPrompt()

        val scenarioExtra = when (scenario) {
            AIScenario.QUICK_PLAN -> """
【今日计划模式】
- 结合用户的课表空隙和待办任务
- 给出具体的时间安排建议
- 询问用户是否需要调整
            """.trimIndent()

            AIScenario.WEAKNESS_DIAGNOSE -> """
【薄弱诊断模式】
- 分析各门课程的掌握度
- 找出需要重点关注的领域
- 给出具体的改进建议
            """.trimIndent()

            AIScenario.EXAM_PREP -> """
【考试冲刺模式】
- 优先聚焦高频考点
- 每次只讲解一个关键概念
- 讲完立即出2道练习题检验
- 根据答题结果决定是否进入下一个知识点
- 保持高效，不浪费时间
            """.trimIndent()

            AIScenario.CONCEPT_EXPLAIN -> """
【概念讲解模式】
- 先用一句话概括核心
- 再用类比让概念更容易理解
- 最后给出一个简单例子
- 询问用户是否理解或有疑问
            """.trimIndent()

            AIScenario.PRACTICE_SESSION -> """
【刷题练习模式】
- 先确认要练习的知识点
- 每次出一道题，等待用户回答
- 根据回答给出反馈
- 追踪正确率，决定是否继续
            """.trimIndent()

            AIScenario.REVIEW_SESSION -> """
【复习回顾模式】
- 基于遗忘曲线选择需要复习的内容
- 用提问的方式引导回忆
- 对遗忘的部分进行强化讲解
- 建议下次复习时间
            """.trimIndent()

            AIScenario.CASUAL_CHAT -> ""
        }

        return basePrompt + "\n\n" + scenarioExtra
    }

    /**
     * 构建用户 Prompt
     */
    private fun buildUserPrompt(input: String, context: LearningContext): String {
        val sb = StringBuilder()

        // 学习画像
        sb.appendLine("【用户学习画像】")
        sb.appendLine("用户: ${context.user.name}, Lv.${context.user.level}")
        sb.appendLine("能量: ${context.user.energy}⚡ 结晶: ${context.user.crystals}💎")
        sb.appendLine("连续学习: ${context.user.streakDays}天")
        sb.appendLine()

        // 今日状态
        sb.appendLine("【今日状态】")
        sb.appendLine("待办任务: ${context.pendingTasks.size}个")
        sb.appendLine("进行中: ${context.inProgressTasks.size}个")
        if (context.pendingTasks.isNotEmpty()) {
            sb.append("优先任务: ")
            context.pendingTasks.sortedByDescending { it.priority }.take(3).forEach {
                sb.append("${it.title}(${it.estimatedMinutes}min) ")
            }
            sb.appendLine()
        }
        sb.appendLine()

        // 课表
        if (context.todaySchedule.isNotEmpty()) {
            sb.appendLine("【今日课表】")
            context.todaySchedule.forEach { (course, slot) ->
                sb.appendLine("  ${course.name} ${slot.startHour}:${slot.startMinute}")
            }
            sb.appendLine()
        }

        // 薄弱点
        if (context.weakCourses.isNotEmpty()) {
            sb.appendLine("【薄弱点】")
            context.weakCourses.take(3).forEach {
                sb.append("  ${it.name}(${(it.masteryLevel * 100).toInt()}%) ")
            }
            sb.appendLine()
        }

        // 所有课程列表（供知识图谱操作参考）
        if (context.courses.isNotEmpty()) {
            sb.appendLine("【我的课程】")
            context.courses.forEach { course ->
                sb.append("  ${course.name} ")
            }
            sb.appendLine()
            sb.appendLine("（知识图谱操作时，请使用上述课程名称作为 courseId）")
            sb.appendLine()
        }

        // 对话历史
        val history = memoryManager.buildContextString()
        if (history.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine(history)
        }

        // 用户输入
        sb.appendLine()
        sb.appendLine("【用户输入】")
        sb.appendLine(input)

        return sb.toString()
    }

    /**
     * 构建默认上下文
     */
    private fun buildDefaultContext(): LearningContext {
        return LearningContext.empty()
    }

    // ==================== 工具方法 ====================

    /**
     * 从模型返回的完整文本中抽取自然语言内容 + JSON 工具指令
     * 改进版本：支持多行 JSON、数组和更复杂的格式
     */
    private fun extractToolCommand(raw: String): Pair<String, String?> {
        if (raw.isBlank()) return raw to null

        val trimmed = raw.trimEnd()

        // 方法1：尝试提取 JSON 数组（多个指令）
        val arrayResult = extractJsonArray(trimmed)
        if (arrayResult != null) {
            return arrayResult
        }

        // 方法2：尝试提取单个 JSON 对象
        val objectResult = extractJsonObject(trimmed)
        if (objectResult != null) {
            return objectResult
        }

        return raw to null
    }

    /**
     * 尝试提取 JSON 数组（多指令格式）
     */
    private fun extractJsonArray(text: String): Pair<String, String?>? {
        // 查找以 [ 开头的 JSON 数组
        var bracketCount = 0
        var jsonStartIndex = -1
        var jsonEndIndex = -1

        for (i in text.length - 1 downTo 0) {
            val char = text[i]
            if (char == ']') {
                if (jsonEndIndex == -1) jsonEndIndex = i
                bracketCount++
            } else if (char == '[') {
                bracketCount--
                if (bracketCount == 0 && jsonEndIndex != -1) {
                    jsonStartIndex = i
                    break
                }
            }
        }

        if (jsonStartIndex != -1 && jsonEndIndex != -1) {
            val jsonStr = text.substring(jsonStartIndex, jsonEndIndex + 1).trim()
            val contentBefore = text.substring(0, jsonStartIndex).trimEnd()

            // 验证是否是有效的工具命令数组
            val isValid = try {
                val jsonElement = kotlinx.serialization.json.Json.parseToJsonElement(jsonStr)
                if (jsonElement is kotlinx.serialization.json.JsonArray) {
                    jsonElement.isNotEmpty() &&
                    jsonElement.all { item ->
                        item.jsonObject.containsKey("action")
                    }
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }

            if (isValid && contentBefore.isNotEmpty()) {
                return contentBefore to jsonStr
            }
        }

        return null
    }

    /**
     * 尝试提取单个 JSON 对象
     */
    private fun extractJsonObject(text: String): Pair<String, String?>? {
        var braceCount = 0
        var jsonStartIndex = -1
        var jsonEndIndex = -1

        for (i in text.length - 1 downTo 0) {
            val char = text[i]
            if (char == '}') {
                if (jsonEndIndex == -1) jsonEndIndex = i
                braceCount++
            } else if (char == '{') {
                braceCount--
                if (braceCount == 0 && jsonEndIndex != -1) {
                    jsonStartIndex = i
                    break
                }
            }
        }

        if (jsonStartIndex != -1 && jsonEndIndex != -1) {
            val jsonStr = text.substring(jsonStartIndex, jsonEndIndex + 1).trim()

            val isValidToolCommand = try {
                val jsonElement = kotlinx.serialization.json.Json.parseToJsonElement(jsonStr).jsonObject
                jsonElement.containsKey("action")
            } catch (e: Exception) {
                false
            }

            if (isValidToolCommand) {
                val contentBefore = text.substring(0, jsonStartIndex).trimEnd()
                return (if (contentBefore.isNotEmpty()) contentBefore else text) to jsonStr
            }
        }

        // 检查最后一行是否是单行 JSON
        val lines = text.lines()
        val lastNonBlankIndex = lines.indexOfLast { it.isNotBlank() }
        if (lastNonBlankIndex >= 0) {
            val lastLine = lines[lastNonBlankIndex].trim()
            if (lastLine.startsWith("{") && lastLine.endsWith("}")) {
                val isValidToolCommand = try {
                    val jsonElement = kotlinx.serialization.json.Json.parseToJsonElement(lastLine).jsonObject
                    jsonElement.containsKey("action")
                } catch (e: Exception) {
                    false
                }

                if (isValidToolCommand) {
                    val contentLines = if (lastNonBlankIndex == 0) emptyList() else lines.subList(0, lastNonBlankIndex)
                    val cleanContent = contentLines.joinToString("\n").trimEnd()
                    return (if (cleanContent.isNotEmpty()) cleanContent else text) to lastLine
                }
            }
        }

        return null
    }

    /**
     * 解析工具动作
     */
    private fun parseToolAction(toolJson: String?): ToolAction? {
        if (toolJson.isNullOrBlank()) return null

        return try {
            val json = kotlinx.serialization.json.Json.parseToJsonElement(toolJson).jsonObject
            val actionStr = json["action"]?.jsonPrimitive?.content ?: return null
            ToolAction.values().find { it.name == actionStr }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 分类响应类型
     */
    private fun classifyResponse(text: String): ResponseType {
        return when {
            text.contains("计划") || text.contains("安排") -> ResponseType.PLAN
            text.contains("问题") || text.contains("薄弱") -> ResponseType.ANALYSIS
            text.contains("理解") || text.contains("原理") -> ResponseType.EXPLANATION
            text.contains("复习") -> ResponseType.REVIEW
            else -> ResponseType.GENERAL
        }
    }

    // ==================== 公共接口 ====================

    /**
     * 获取对话历史
     */
    fun getChatHistory(): List<ChatTurn> {
        return memoryManager.currentSession.value.turns
    }

    /**
     * 清空对话历史
     */
    fun clearHistory() {
        memoryManager.clearCurrentSession()
    }

    /**
     * 开始新会话
     */
    fun startNewSession(scenario: AIScenario = AIScenario.CASUAL_CHAT) {
        memoryManager.startNewSession(scenario)
    }

    /**
     * 获取当前场景
     */
    fun getCurrentScenario(): AIScenario {
        return memoryManager.getCurrentScenario()
    }

    /**
     * 导出对话
     */
    fun exportChat(): String {
        return memoryManager.exportSession()
    }

    /**
     * 关闭客户端
     */
    fun close() {
        deepSeekClient.close()
    }
}