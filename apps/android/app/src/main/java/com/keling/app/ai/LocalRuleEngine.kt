package com.keling.app.ai

import com.keling.app.data.*
import java.util.Calendar

/**
 * =========================
 * 智能本地规则引擎（增强版）
 * =========================
 *
 * 功能：
 * - 基于真实用户数据生成动态响应
 * - 零延迟即时响应常见问题
 * - 支持快捷指令解析
 * - 智能计划编排
 * - 薄弱点深度分析
 * - 复习提醒生成
 */

/**
 * 学习上下文，用于本地规则引擎生成个性化响应
 */
data class LearningContext(
    val user: User,
    val courses: List<Course>,
    val tasks: List<Task>,
    val notes: List<Note>,
    val knowledgeNodes: List<KnowledgeNode>,
    val todaySchedule: List<Pair<Course, ScheduleSlot>>,
    val currentHour: Int,
    val dayOfWeek: Int
) {
    companion object {
        fun empty() = LearningContext(
            user = User(id = ""),
            courses = emptyList(),
            tasks = emptyList(),
            notes = emptyList(),
            knowledgeNodes = emptyList(),
            todaySchedule = emptyList(),
            currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            dayOfWeek = getTodayDayOfWeek()
        )

        private fun getTodayDayOfWeek(): Int {
            val cal = Calendar.getInstance()
            return (cal.get(Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 7 else it }
        }
    }

    // 便捷属性
    val pendingTasks: List<Task> get() = tasks.filter { it.status == TaskStatus.PENDING }
    val inProgressTasks: List<Task> get() = tasks.filter { it.status == TaskStatus.IN_PROGRESS }
    val completedTasks: List<Task> get() = tasks.filter { it.status == TaskStatus.COMPLETED }
    val weakCourses: List<Course> get() = courses.filter { it.masteryLevel < 0.6f }.sortedBy { it.masteryLevel }
    val strongCourses: List<Course> get() = courses.filter { it.masteryLevel >= 0.8f }.sortedByDescending { it.masteryLevel }
}

/**
 * 增强版本地规则引擎
 */
class EnhancedLocalRuleEngine {

    /**
     * 处理用户输入，尝试匹配本地规则
     *
     * @param input 用户输入
     * @param context 学习上下文
     * @return 匹配成功返回 AIResponse，否则返回 null
     */
    fun process(input: String, context: LearningContext): AIResponse? {
        val normalizedInput = input.trim()

        return when {
            // ===== 今日计划类 =====
            matchesPlanIntent(normalizedInput) -> generateDynamicPlan(context)

            // ===== 薄弱点分析类 =====
            matchesWeaknessIntent(normalizedInput) -> analyzeWeakness(context)

            // ===== 复习提醒类 =====
            matchesReviewIntent(normalizedInput) -> generateReviewReminder(context)

            // ===== 任务状态类 =====
            matchesTaskStatusIntent(normalizedInput) -> reportTaskStatus(context)

            // ===== 课表查询类 =====
            matchesScheduleIntent(normalizedInput) -> reportSchedule(context)

            // ===== 快捷指令：添加任务 =====
            normalizedInput.startsWith("添加任务") || normalizedInput.startsWith("创建任务") ->
                parseQuickTask(normalizedInput, context)

            // ===== 快捷指令：查看课程 =====
            normalizedInput.startsWith("查看") -> handleQuickView(normalizedInput, context)

            // ===== 问候语 =====
            matchesGreeting(normalizedInput) -> generateGreeting(context)

            // ===== 激励/鼓励 =====
            matchesEncouragement(normalizedInput) -> generateEncouragement(context)

            // ===== 未匹配，返回 null 表示需要调用云端 =====
            else -> null
        }
    }

    // ==================== 意图匹配 ====================

    private fun matchesPlanIntent(input: String): Boolean {
        val keywords = listOf("计划", "安排", "今天学", "今天做", "今日", "学什么", "做什么", "怎么安排")
        return keywords.any { input.contains(it) }
    }

    private fun matchesWeaknessIntent(input: String): Boolean {
        val keywords = listOf("薄弱", "不会", "不懂", "难", "差", "弱项", "短板", "哪门不行", "哪门差")
        return keywords.any { input.contains(it) }
    }

    private fun matchesReviewIntent(input: String): Boolean {
        val keywords = listOf("复习", "记得", "遗忘", "忘了", "回忆", "巩固")
        return keywords.any { input.contains(it) }
    }

    private fun matchesTaskStatusIntent(input: String): Boolean {
        val keywords = listOf("任务", "待办", "进度", "完成情况", "还剩")
        return keywords.any { input.contains(it) }
    }

    private fun matchesScheduleIntent(input: String): Boolean {
        val keywords = listOf("课表", "今天课", "什么课", "有课吗", "课程安排")
        return keywords.any { input.contains(it) }
    }

    private fun matchesGreeting(input: String): Boolean {
        val keywords = listOf("你好", "在吗", "嗨", "hi", "hello", "早", "晚上好", "下午好")
        return keywords.any { input.contains(it, ignoreCase = true) }
    }

    private fun matchesEncouragement(input: String): Boolean {
        val keywords = listOf("加油", "鼓励", "没动力", "不想学", "累", "疲惫", "坚持")
        return keywords.any { input.contains(it) }
    }

    // ==================== 响应生成 ====================

    /**
     * 生成动态今日计划
     */
    private fun generateDynamicPlan(ctx: LearningContext): AIResponse {
        val sb = StringBuilder()

        // 1. 时间问候
        val greeting = when {
            ctx.currentHour < 12 -> "🌅 早上好"
            ctx.currentHour < 18 -> "☀️ 下午好"
            else -> "🌙 晚上好"
        }
        sb.appendLine("$greeting！今日学习节律建议：")
        sb.appendLine()

        // 2. 基于课表空隙安排
        if (ctx.todaySchedule.isNotEmpty()) {
            sb.appendLine("📚 今日课程：")
            ctx.todaySchedule.forEach { (course, slot) ->
                sb.appendLine("  ${slot.startHour}:${"%02d".format(slot.startMinute)} ${course.name}")
            }
            sb.appendLine()
        }

        // 3. 优先处理紧急任务
        val urgentTasks = ctx.pendingTasks.filter { it.priority >= 4 }.sortedByDescending { it.priority }
        if (urgentTasks.isNotEmpty()) {
            sb.appendLine("🔥 优先处理：")
            urgentTasks.take(3).forEach { task ->
                val course = ctx.courses.find { it.id == task.courseId }
                sb.appendLine("  ⚡ ${task.title}（${task.estimatedMinutes}分钟）")
            }
            sb.appendLine()
        }

        // 4. 薄弱点补强
        if (ctx.weakCourses.isNotEmpty()) {
            val weakest = ctx.weakCourses.first()
            sb.appendLine("💧 建议补强：${weakest.name}（掌握度 ${(weakest.masteryLevel * 100).toInt()}%）")
            sb.appendLine()
        }

        // 5. 预计收益
        val totalMinutes = ctx.pendingTasks.sumOf { it.estimatedMinutes }
        val estimatedEnergy = 10 + totalMinutes / 10
        val estimatedCrystals = 5 + ctx.pendingTasks.size * 3
        sb.appendLine("💎 完成可获得：约 ${estimatedEnergy}⚡ ${estimatedCrystals}💎")

        // 6. 工具指令：可以跳转到任务页
        val toolJson = if (ctx.pendingTasks.isNotEmpty()) {
            """{"action":"GO_TO","params":{"screen":"tasks"}}"""
        } else {
            """{"action":"NO_ACTION","params":{}}"""
        }

        return AIResponse(
            content = sb.toString().trimEnd(),
            type = ResponseType.PLAN,
            isFromAI = false,
            toolCommandJson = toolJson
        )
    }

    /**
     * 分析薄弱点
     */
    private fun analyzeWeakness(ctx: LearningContext): AIResponse {
        val sb = StringBuilder()
        sb.appendLine("📊 当前知识生态分析：")
        sb.appendLine()

        if (ctx.courses.isEmpty()) {
            sb.appendLine("  暂无课程数据，请先添加课程～")
            return AIResponse(
                content = sb.toString().trimEnd(),
                type = ResponseType.ANALYSIS,
                isFromAI = false
            )
        }

        // 按掌握度排序
        val sortedCourses = ctx.courses.sortedBy { it.masteryLevel }

        sortedCourses.forEach { course ->
            val status = when {
                course.masteryLevel < 0.4 -> "🔴 急需灌溉"
                course.masteryLevel < 0.6 -> "🟡 需要关注"
                course.masteryLevel < 0.8 -> "🟢 状态良好"
                else -> "🌟 优秀"
            }
            val bar = generateProgressBar(course.masteryLevel)
            sb.appendLine("  ${course.name} $bar ${(course.masteryLevel * 100).toInt()}% $status")
        }

        sb.appendLine()

        // 建议
        if (ctx.weakCourses.isNotEmpty()) {
            val weakest = ctx.weakCourses.first()
            sb.appendLine("💡 建议：优先攻克「${weakest.name}」，从基础概念开始复习")

            // 返回工具指令：跳转到该课程的温室
            val toolJson = """{"action":"GO_TO","params":{"screen":"greenhouse","courseId":"${weakest.id}"}}"""
            return AIResponse(
                content = sb.toString().trimEnd(),
                type = ResponseType.ANALYSIS,
                isFromAI = false,
                toolCommandJson = toolJson
            )
        } else {
            sb.appendLine("🎉 太棒了！各门课程状态都不错，继续保持～")
        }

        return AIResponse(
            content = sb.toString().trimEnd(),
            type = ResponseType.ANALYSIS,
            isFromAI = false
        )
    }

    /**
     * 生成复习提醒
     */
    private fun generateReviewReminder(ctx: LearningContext): AIResponse {
        val sb = StringBuilder()
        sb.appendLine("🔄 记忆回响提醒：")
        sb.appendLine()

        if (ctx.notes.isEmpty()) {
            sb.appendLine("  暂无笔记记录，学完知识点记得记笔记哦～")
            return AIResponse(
                content = sb.toString().trimEnd(),
                type = ResponseType.REVIEW,
                isFromAI = false
            )
        }

        // 基于复习次数和最后复习时间排序
        val sortedNotes = ctx.notes.sortedWith(
            compareBy<Note> { it.reviewCount }
                .thenBy { it.lastReviewedAt ?: 0L }
        ).take(5)

        sortedNotes.forEach { note ->
            val daysSinceReview = note.lastReviewedAt?.let {
                ((System.currentTimeMillis() - it) / (1000 * 60 * 60 * 24)).toInt()
            } ?: 999

            val urgency = when {
                daysSinceReview > 7 -> "⚠️ 急需复习"
                daysSinceReview > 3 -> "📌 建议复习"
                else -> "✅ 近期已复习"
            }
            sb.appendLine("  • ${note.title} (复习${note.reviewCount}次, ${daysSinceReview}天前) $urgency")
        }

        sb.appendLine()
        sb.appendLine("💎 完成复习可获得额外知识结晶奖励")

        return AIResponse(
            content = sb.toString().trimEnd(),
            type = ResponseType.REVIEW,
            isFromAI = false
        )
    }

    /**
     * 报告任务状态
     */
    private fun reportTaskStatus(ctx: LearningContext): AIResponse {
        val sb = StringBuilder()
        sb.appendLine("📋 任务状态报告：")
        sb.appendLine()

        val pending = ctx.pendingTasks.size
        val inProgress = ctx.inProgressTasks.size
        val completed = ctx.completedTasks.size

        sb.appendLine("  ⏳ 待完成: $pending")
        sb.appendLine("  🔄 进行中: $inProgress")
        sb.appendLine("  ✅ 已完成: $completed")
        sb.appendLine()

        if (ctx.pendingTasks.isNotEmpty()) {
            val totalMinutes = ctx.pendingTasks.sumOf { it.estimatedMinutes }
            sb.appendLine("  预计剩余: $totalMinutes 分钟")

            // 显示前3个待办
            sb.appendLine()
            sb.appendLine("  下一步可以：")
            ctx.pendingTasks.take(3).forEach { task ->
                sb.appendLine("    → ${task.title}")
            }
        } else if (completed > 0 && pending == 0) {
            sb.appendLine("  🎉 太棒了！所有任务都完成了！")
        }

        return AIResponse(
            content = sb.toString().trimEnd(),
            type = ResponseType.GENERAL,
            isFromAI = false
        )
    }

    /**
     * 报告课表
     */
    private fun reportSchedule(ctx: LearningContext): AIResponse {
        val sb = StringBuilder()
        val dayNames = listOf("", "周一", "周二", "周三", "周四", "周五", "周六", "周日")

        sb.appendLine("📅 今日课表（${dayNames[ctx.dayOfWeek]}）：")
        sb.appendLine()

        if (ctx.todaySchedule.isEmpty()) {
            sb.appendLine("  今天没有课程安排 🎉")
        } else {
            ctx.todaySchedule.forEach { (course, slot) ->
                val endH = slot.startHour + (slot.startMinute + slot.durationMinutes) / 60
                val endM = (slot.startMinute + slot.durationMinutes) % 60
                val timeStr = "${slot.startHour}:${"%02d".format(slot.startMinute)}-${endH}:${"%02d".format(endM)}"
                sb.appendLine("  $timeStr ${course.name} @${course.location}")
            }
        }

        return AIResponse(
            content = sb.toString().trimEnd(),
            type = ResponseType.GENERAL,
            isFromAI = false
        )
    }

    /**
     * 解析快捷任务指令
     */
    private fun parseQuickTask(input: String, ctx: LearningContext): AIResponse? {
        // 格式: "添加任务 高数复习 30分钟" 或 "创建任务 英语单词"
        val taskContent = input.removePrefix("添加任务").removePrefix("创建任务").trim()

        if (taskContent.isBlank()) {
            return AIResponse(
                content = "请告诉我任务内容，例如：添加任务 复习高数 30分钟",
                type = ResponseType.GENERAL,
                isFromAI = false
            )
        }

        // 尝试解析时长
        val minutePattern = "(\\d+)\\s*(分钟|min)".toRegex()
        val minuteMatch = minutePattern.find(taskContent)
        val minutes = minuteMatch?.groupValues?.get(1)?.toIntOrNull() ?: 25

        // 提取标题
        val title = minutePattern.replace(taskContent, "").trim()
            .ifBlank { taskContent }

        // 尝试匹配课程
        val matchedCourse = ctx.courses.firstOrNull { course ->
            title.contains(course.name) || course.name.contains(title)
        }

        val params = mutableMapOf<String, Any>(
            "title" to title,
            "estimatedMinutes" to minutes
        )
        matchedCourse?.let { params["courseId"] = it.id }

        val toolJson = """{"action":"CREATE_TASK","params":${
            params.entries.joinToString(",", "{", "}") { "\"${it.key}\":${if (it.value is String) "\"${it.value}\"" else it.value}" }
        }}"""

        return AIResponse(
            content = "好的！我来创建任务「$title」，预计 $minutes 分钟。",
            type = ResponseType.GENERAL,
            isFromAI = false,
            toolCommandJson = toolJson
        )
    }

    /**
     * 处理快捷查看指令
     */
    private fun handleQuickView(input: String, ctx: LearningContext): AIResponse? {
        val target = input.removePrefix("查看").trim()

        return when {
            target.contains("任务") || target.contains("待办") -> {
                AIResponse(
                    content = "好的，带你去任务列表～",
                    type = ResponseType.GENERAL,
                    isFromAI = false,
                    toolCommandJson = """{"action":"GO_TO","params":{"screen":"tasks"}}"""
                )
            }
            target.contains("课表") -> {
                AIResponse(
                    content = "好的，打开课表编辑页～",
                    type = ResponseType.GENERAL,
                    isFromAI = false,
                    toolCommandJson = """{"action":"GO_TO","params":{"screen":"schedule_edit"}}"""
                )
            }
            target.contains("温室") || target.contains("星球") -> {
                AIResponse(
                    content = "好的，进入温室看看你的知识星球～",
                    type = ResponseType.GENERAL,
                    isFromAI = false,
                    toolCommandJson = """{"action":"GO_TO","params":{"screen":"greenhouse"}}"""
                )
            }
            // 尝试匹配课程名
            ctx.courses.any { target.contains(it.name) } -> {
                val course = ctx.courses.first { target.contains(it.name) }
                AIResponse(
                    content = "好的，查看「${course.name}」的学习进度～",
                    type = ResponseType.GENERAL,
                    isFromAI = false,
                    toolCommandJson = """{"action":"GO_TO","params":{"screen":"greenhouse","courseId":"${course.id}"}}"""
                )
            }
            else -> null
        }
    }

    /**
     * 生成问候语
     */
    private fun generateGreeting(ctx: LearningContext): AIResponse {
        val greeting = when {
            ctx.currentHour < 12 -> "早安"
            ctx.currentHour < 18 -> "午安"
            else -> "晚安"
        }

        val sb = StringBuilder()
        sb.append("$greeting，${ctx.user.name}！")

        // 添加一些上下文相关的内容
        when {
            ctx.pendingTasks.isNotEmpty() -> {
                sb.append(" 今天还有 ${ctx.pendingTasks.size} 个任务等你完成哦～")
            }
            ctx.completedTasks.isNotEmpty() && ctx.pendingTasks.isEmpty() -> {
                sb.append(" 今天的任务都完成了，真棒！🎉")
            }
            ctx.weakCourses.isNotEmpty() -> {
                sb.append(" 「${ctx.weakCourses.first().name}」需要多关注一下～")
            }
            else -> {
                sb.append(" 有什么可以帮你的？")
            }
        }

        return AIResponse(
            content = sb.toString(),
            type = ResponseType.GENERAL,
            isFromAI = false
        )
    }

    /**
     * 生成鼓励语
     */
    private fun generateEncouragement(ctx: LearningContext): AIResponse {
        val encouragements = listOf(
            "每一步都是进步，继续加油！💪",
            "学习是一场马拉松，不是短跑，保持节奏～ 🏃",
            "你已经做得很棒了，相信自己！✨",
            "休息一下也是学习的一部分，别太累～ 🌙",
            "困难只是暂时的，坚持下去就会看到曙光！🌅"
        )

        val contextualEncouragement = when {
            ctx.weakCourses.isNotEmpty() -> {
                "「${ctx.weakCourses.first().name}」确实有挑战，但正是克服困难让你成长！"
            }
            ctx.pendingTasks.size > 5 -> {
                "任务有点多？试着一次只专注一件事，慢慢来～"
            }
            else -> encouragements.random()
        }

        return AIResponse(
            content = contextualEncouragement,
            type = ResponseType.GENERAL,
            isFromAI = false
        )
    }

    // ==================== 工具方法 ====================

    /**
     * 生成进度条字符串
     */
    private fun generateProgressBar(level: Float, length: Int = 10): String {
        val filled = (level * length).toInt()
        val empty = length - filled
        return "█".repeat(filled) + "░".repeat(empty)
    }
}