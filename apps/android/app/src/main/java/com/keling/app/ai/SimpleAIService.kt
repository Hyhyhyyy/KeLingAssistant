package com.keling.app.ai

import android.util.Log

/**
 * 对外暴露的简化 AI 服务入口。
 *
 * - 兼容旧代码：保持 process(input, context) 签名不变
 * - 内部委托给分层后的 AICoordinator
 */
object SimpleAIService {

    private const val TAG = "SimpleAIService"

    private val localRuleEngine = EnhancedLocalRuleEngine()
    private val coordinator = AICoordinator()

    /**
     * 可直接访问的本地规则处理（供特殊场景使用）
     */
    fun localProcess(input: String, context: LearningContext = LearningContext.empty()): AIResponse? {
        return localRuleEngine.process(input, context)
    }

    // ==================== 对外主入口 ====================

    /**
     * 处理用户输入（带完整上下文）
     */
    suspend fun processWith(input: String, context: LearningContext): AIResponse {
        return try {
            coordinator.process(input = input, context = context)
        } catch (e: Exception) {
            Log.e(TAG, "AI调用失败", e)
            AIResponse(
                content = "恒星引擎暂时离线 🌑\n${e.message}",
                type = ResponseType.ERROR,
                isFromAI = false
            )
        }
    }

    suspend fun process(input: String, @Suppress("UNUSED_PARAMETER") context: String = ""): AIResponse {
        return try {
            // 注意：这里的 context 是字符串格式，用于日志或简单场景
            // 实际上下文通过 AICoordinator 内部构建
            coordinator.process(input = input, context = LearningContext.empty())
        } catch (e: Exception) {
            Log.e(TAG, "AI调用失败", e)
            AIResponse(
                content = "恒星引擎暂时离线 🌑\n${e.message}",
                type = ResponseType.ERROR,
                isFromAI = false
            )
        }
    }

    /**
     * 为了与旧接口兼容，保留 close 方法，但当前仅用于关闭底层客户端。
     */
    fun close() {
        coordinator.close()
    }
}