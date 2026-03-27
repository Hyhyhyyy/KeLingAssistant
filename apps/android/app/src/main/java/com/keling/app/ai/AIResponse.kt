package com.keling.app.ai

/**
 * App 内部统一的 AI 返回结构
 * 不参与 JSON 序列化！！！
 */
data class AIResponse(
    val content: String,
    val type: ResponseType = ResponseType.GENERAL,
    val isFromAI: Boolean = true,

    /**
     * （可选）AI 建议执行的工具指令，JSON 字符串形式：
     * {"action":"CREATE_TASK","params":{...}}
     *
     * - UI 用于进一步解析并驱动应用内操作
     * - 为了兼容性，默认值为 null，不影响旧代码
     */
    val toolCommandJson: String? = null
)

/**
 * 用于 UI 区分消息类型
 */
enum class ResponseType {
    PLAN,           // 学习计划
    ANALYSIS,       // 学情分析
    EXPLANATION,    // 知识讲解
    REVIEW,         // 复习提醒
    GENERAL,        // 普通回答
    ERROR           // 错误
}