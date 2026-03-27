package com.keling.app.ai

/**
 * =========================
 * 知识提炼器
 * =========================
 *
 * 从笔记内容中提取知识点
 */

import com.keling.app.data.KnowledgeNode
import kotlinx.serialization.Serializable

/**
 * 知识提取器
 */
class KnowledgeExtractor {

    /**
     * 从笔记内容提取知识点
     *
     * @param noteContent 笔记内容
     * @param courseId 课程ID
     * @return 提取的知识点列表
     */
    suspend fun extractFromNote(
        noteContent: String,
        courseId: String
    ): List<KnowledgeNode> {
        // 预处理：清理内容
        val cleanedContent = preprocessContent(noteContent)

        // 提取关键词和概念
        val concepts = extractConcepts(cleanedContent)

        // 构建知识点
        return concepts.mapIndexed { index, concept ->
            KnowledgeNode(
                id = "kn_${courseId}_${System.currentTimeMillis()}_$index",
                courseId = courseId,
                name = concept.name,
                description = concept.description,
                parentIds = emptyList(),
                difficulty = estimateDifficulty(concept),
                masteryLevel = 0f,
                isUnlocked = true
            )
        }
    }

    /**
     * 从AI回答中提取知识点
     */
    suspend fun extractFromAIResponse(
        response: String,
        courseId: String,
        context: String = ""
    ): List<KnowledgeNode> {
        // 解析AI回答中的知识点
        val knowledgePatterns = listOf(
            // 匹配 "概念：xxx" 格式
            Regex("概念[：:](.*?)(?=\\n|概念[：:]|$)", RegexOption.DOT_MATCHES_ALL),
            // 匹配 "- xxx：" 列表格式
            Regex("-\\s*(.+?)[：:](.*?)(?=\\n-|\\n\\n|$)", RegexOption.DOT_MATCHES_ALL),
            // 匹配 "【xxx】" 格式
            Regex("【(.+?)】(.*?)(?=【|$)", RegexOption.DOT_MATCHES_ALL)
        )

        val nodes = mutableListOf<KnowledgeNode>()
        var index = 0

        for (pattern in knowledgePatterns) {
            pattern.findAll(response).forEach { match ->
                val name = match.groupValues.getOrNull(1)?.trim() ?: return@forEach
                val description = match.groupValues.getOrNull(2)?.trim() ?: ""

                if (name.isNotBlank() && name.length <= 20) {
                    nodes.add(
                        KnowledgeNode(
                            id = "kn_${courseId}_${System.currentTimeMillis()}_${index++}",
                            courseId = courseId,
                            name = name,
                            description = description.ifBlank { "从AI对话中提取的知识点" },
                            difficulty = 3,
                            masteryLevel = 0f,
                            isUnlocked = true
                        )
                    )
                }
            }
        }

        return nodes
    }

    /**
     * 预处理内容
     */
    private fun preprocessContent(content: String): String {
        return content
            .replace(Regex("#+\\s*"), "") // 移除标题标记
            .replace(Regex("\\*+([^*]+)\\*+"), "$1") // 移除强调标记
            .replace(Regex("`([^`]+)`"), "$1") // 移除代码标记
            .replace(Regex("\\[([^\\]]+)\\]\\([^)]+\\)"), "$1") // 移除链接
            .trim()
    }

    /**
     * 提取概念
     */
    private fun extractConcepts(content: String): List<Concept> {
        val concepts = mutableListOf<Concept>()

        // 按段落分割
        val paragraphs = content.split(Regex("\\n\\n+"))
            .filter { it.isNotBlank() }

        for (paragraph in paragraphs) {
            // 提取第一句作为潜在的概念名称
            val firstSentence = paragraph.split(Regex("[。！？.!?]"))
                .firstOrNull()?.trim() ?: continue

            if (firstSentence.length in 2..20) {
                // 可能是一个概念名称
                concepts.add(
                    Concept(
                        name = firstSentence,
                        description = paragraph.take(200),
                        context = paragraph
                    )
                )
            }
        }

        // 如果没有提取到概念，尝试其他方法
        if (concepts.isEmpty()) {
            // 寻找高频词汇
            val words = content.split(Regex("\\s+|[,，、；;]"))
                .filter { it.length in 2..8 }
                .groupingBy { it }
                .eachCount()
                .filter { it.value >= 2 }
                .keys
                .take(5)

            for (word in words) {
                concepts.add(
                    Concept(
                        name = word,
                        description = "关键词：$word",
                        context = content.take(100)
                    )
                )
            }
        }

        return concepts.take(10) // 最多返回10个概念
    }

    /**
     * 估算难度
     */
    private fun estimateDifficulty(concept: Concept): Int {
        val text = concept.context

        // 基于关键词判断难度
        val hardKeywords = listOf("证明", "定理", "推导", "复杂", "高级", "深入")
        val easyKeywords = listOf("定义", "概念", "基础", "简单", "入门")

        val hasHard = hardKeywords.any { text.contains(it) }
        val hasEasy = easyKeywords.any { text.contains(it) }

        return when {
            hasHard && !hasEasy -> 4
            hasEasy && !hasHard -> 2
            text.length > 500 -> 4
            text.length < 100 -> 2
            else -> 3
        }
    }
}

/**
 * 概念模型
 */
data class Concept(
    val name: String,
    val description: String,
    val context: String
)

/**
 * 知识点关系
 */
@Serializable
data class KnowledgeRelation(
    val sourceId: String,
    val targetId: String,
    val type: RelationType,
    val strength: Float = 1.0f // 关系强度 0-1
)

enum class RelationType {
    PREREQUISITE,  // 前置关系
    RELATED,       // 相关关系
    PART_OF,       // 组成关系
    EXTENDS        // 扩展关系
}