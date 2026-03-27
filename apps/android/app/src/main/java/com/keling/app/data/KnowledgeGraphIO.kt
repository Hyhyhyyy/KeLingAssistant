package com.keling.app.data

/**
 * =========================
 * 知识图谱导入导出工具
 * =========================
 *
 * 支持JSON和Markdown格式的导入导出
 */

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 知识图谱导出数据
 */
@Serializable
data class KnowledgeGraphExport(
    val version: String = "1.0",
    val exportedAt: Long = System.currentTimeMillis(),
    val courseId: String,
    val courseName: String,
    val nodes: List<KnowledgeNodeExport>,
    val edges: List<KnowledgeEdgeExport>
)

@Serializable
data class KnowledgeNodeExport(
    val id: String,
    val name: String,
    val description: String,
    val difficulty: Int,
    val masteryLevel: Float,
    val positionX: Float,
    val positionY: Float,
    val isUnlocked: Boolean,
    val createdAt: Long? = null
)

@Serializable
data class KnowledgeEdgeExport(
    val sourceId: String,
    val targetId: String,
    val relationType: String = "prerequisite" // prerequisite, related, derived
)

/**
 * 知识图谱导入导出工具类
 */
object KnowledgeGraphIO {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * 导出知识图谱为JSON字符串
     */
    fun exportToJSON(
        courseId: String,
        courseName: String,
        nodes: List<KnowledgeNode>
    ): String {
        val nodeExports = nodes.map { node ->
            KnowledgeNodeExport(
                id = node.id,
                name = node.name,
                description = node.description,
                difficulty = node.difficulty,
                masteryLevel = node.masteryLevel,
                positionX = node.positionX,
                positionY = node.positionY,
                isUnlocked = node.isUnlocked
            )
        }

        // 构建边（从父子关系）
        val edgeExports = mutableListOf<KnowledgeEdgeExport>()
        nodes.forEach { node ->
            node.parentIds.forEach { parentId ->
                edgeExports.add(
                    KnowledgeEdgeExport(
                        sourceId = parentId,
                        targetId = node.id,
                        relationType = "prerequisite"
                    )
                )
            }
        }

        val export = KnowledgeGraphExport(
            courseId = courseId,
            courseName = courseName,
            nodes = nodeExports,
            edges = edgeExports
        )

        return json.encodeToString(export)
    }

    /**
     * 从JSON字符串导入知识图谱
     */
    fun importFromJSON(jsonString: String): ImportResult {
        return try {
            val export = json.decodeFromString<KnowledgeGraphExport>(jsonString)

            // 构建父ID映射
            val parentMap = mutableMapOf<String, List<String>>()
            export.edges.forEach { edge ->
                val current = parentMap[edge.targetId] ?: emptyList()
                parentMap[edge.targetId] = current + edge.sourceId
            }

            val nodes = export.nodes.map { nodeExport ->
                KnowledgeNode(
                    id = nodeExport.id,
                    courseId = export.courseId,
                    name = nodeExport.name,
                    description = nodeExport.description,
                    parentIds = parentMap[nodeExport.id] ?: emptyList(),
                    childIds = emptyList(), // 将在后续计算
                    difficulty = nodeExport.difficulty,
                    masteryLevel = nodeExport.masteryLevel,
                    positionX = nodeExport.positionX,
                    positionY = nodeExport.positionY,
                    isUnlocked = nodeExport.isUnlocked
                )
            }

            // 计算子节点ID
            val nodeMap = nodes.associateBy { it.id }
            val nodesWithChildren = nodes.map { node ->
                val children = nodes.filter { it.parentIds.contains(node.id) }.map { it.id }
                node.copy(childIds = children)
            }

            ImportResult(
                success = true,
                courseId = export.courseId,
                courseName = export.courseName,
                nodes = nodesWithChildren,
                errorMessage = null
            )
        } catch (e: Exception) {
            ImportResult(
                success = false,
                courseId = "",
                courseName = "",
                nodes = emptyList(),
                errorMessage = "解析失败: ${e.message}"
            )
        }
    }

    /**
     * 导出知识图谱为Markdown格式
     */
    fun exportToMarkdown(
        courseName: String,
        nodes: List<KnowledgeNode>
    ): String {
        val sb = StringBuilder()

        sb.appendLine("# $courseName - 知识图谱")
        sb.appendLine()
        sb.appendLine("> 导出时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}")
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()

        // 按难度分组
        val groupedByDifficulty = nodes.groupBy { it.difficulty }

        sb.appendLine("## 知识点概览")
        sb.appendLine()
        sb.appendLine("总计 ${nodes.size} 个知识点")
        sb.appendLine()

        // 难度统计
        sb.appendLine("### 难度分布")
        sb.appendLine()
        for (i in 5 downTo 1) {
            val count = groupedByDifficulty[i]?.size ?: 0
            if (count > 0) {
                sb.appendLine("- 难度 $i: $count 个")
            }
        }
        sb.appendLine()

        // 知识点详情
        sb.appendLine("## 知识点列表")
        sb.appendLine()

        nodes.sortedByDescending { it.difficulty }.forEach { node ->
            sb.appendLine("### ${node.name}")
            sb.appendLine()
            sb.appendLine("- **ID**: `${node.id}`")
            sb.appendLine("- **难度**: ${"★".repeat(node.difficulty)}${"☆".repeat(5 - node.difficulty)}")
            sb.appendLine("- **掌握度**: ${(node.masteryLevel * 100).toInt()}%")
            sb.appendLine("- **状态**: ${if (node.isUnlocked) "已解锁" else "未解锁"}")

            if (node.description.isNotBlank()) {
                sb.appendLine()
                sb.appendLine(node.description)
            }

            // 前置知识
            if (node.parentIds.isNotEmpty()) {
                sb.appendLine()
                sb.appendLine("**前置知识**:")
                node.parentIds.forEach { parentId ->
                    val parent = nodes.find { it.id == parentId }
                    if (parent != null) {
                        sb.appendLine("- ${parent.name}")
                    }
                }
            }

            sb.appendLine()
            sb.appendLine("---")
            sb.appendLine()
        }

        // 学习路径建议
        sb.appendLine("## 推荐学习路径")
        sb.appendLine()
        sb.appendLine("```mermaid")
        sb.appendLine("graph TD")

        // 找出根节点（没有前置知识的节点）
        val rootNodes = nodes.filter { it.parentIds.isEmpty() }
        rootNodes.forEach { root ->
            sb.appendLine("    ${root.id}[${root.name}]")
            appendChildren(sb, root, nodes)
        }

        sb.appendLine("```")
        sb.appendLine()

        return sb.toString()
    }

    /**
     * 递归添加子节点到Mermaid图
     */
    private fun appendChildren(
        sb: StringBuilder,
        parent: KnowledgeNode,
        allNodes: List<KnowledgeNode>
    ) {
        val children = allNodes.filter { it.parentIds.contains(parent.id) }
        children.forEach { child ->
            sb.appendLine("    ${parent.id} --> ${child.id}[${child.name}]")
            appendChildren(sb, child, allNodes)
        }
    }

    /**
     * 从Markdown导入（简化版，仅解析基本结构）
     */
    fun importFromMarkdown(markdown: String): ImportResult {
        return try {
            val lines = markdown.lines()
            var courseName = ""
            val nodes = mutableListOf<KnowledgeNode>()
            var currentNode: KnowledgeNode? = null
            var courseId = "imported_${System.currentTimeMillis()}"

            for (line in lines) {
                when {
                    line.startsWith("# ") -> {
                        // 课程名称
                        courseName = line.removePrefix("# ").split(" - ").first()
                    }
                    line.startsWith("### ") -> {
                        // 保存上一个节点
                        if (currentNode != null) {
                            nodes.add(currentNode)
                        }
                        // 开始新节点
                        val name = line.removePrefix("### ").trim()
                        currentNode = KnowledgeNode(
                            id = "kn_${System.currentTimeMillis()}_${nodes.size}",
                            courseId = courseId,
                            name = name,
                            description = "",
                            difficulty = 3,
                            masteryLevel = 0f
                        )
                    }
                    line.startsWith("- **难度**: ") -> {
                        val difficultyStr = line.removePrefix("- **难度**: ")
                        val difficulty = difficultyStr.count { it == '★' }
                        currentNode = currentNode?.copy(difficulty = difficulty)
                    }
                    line.startsWith("- **掌握度**: ") -> {
                        val masteryStr = line.removePrefix("- **掌握度**: ")
                            .removeSuffix("%")
                        currentNode = currentNode?.copy(
                            masteryLevel = masteryStr.toFloatOrNull()?.div(100f) ?: 0f
                        )
                    }
                    line.isNotBlank() && !line.startsWith("-") && currentNode != null -> {
                        // 描述内容
                        if (currentNode?.description.isNullOrBlank()) {
                            currentNode = currentNode?.copy(description = line.trim())
                        }
                    }
                }
            }

            // 保存最后一个节点
            if (currentNode != null) {
                nodes.add(currentNode)
            }

            ImportResult(
                success = true,
                courseId = courseId,
                courseName = courseName,
                nodes = nodes,
                errorMessage = null
            )
        } catch (e: Exception) {
            ImportResult(
                success = false,
                courseId = "",
                courseName = "",
                nodes = emptyList(),
                errorMessage = "Markdown解析失败: ${e.message}"
            )
        }
    }

    /**
     * 导出为Anki卡片格式
     */
    fun exportToAnkiFormat(nodes: List<KnowledgeNode>): String {
        val sb = StringBuilder()

        nodes.forEach { node ->
            // 正面：知识点名称
            sb.append(node.name)
            sb.append("\t")
            // 背面：描述
            sb.append(node.description.ifBlank { "暂无描述" })
            sb.append("\t")
            // 标签
            sb.append("difficulty_${node.difficulty}")
            sb.appendLine()
        }

        return sb.toString()
    }
}

/**
 * 导入结果
 */
data class ImportResult(
    val success: Boolean,
    val courseId: String,
    val courseName: String,
    val nodes: List<KnowledgeNode>,
    val errorMessage: String?
)

/**
 * 导出格式
 */
enum class ExportFormat {
    JSON,
    MARKDOWN,
    ANKI
}