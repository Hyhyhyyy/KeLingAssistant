package com.keling.app.ai

import com.keling.app.data.json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * 将模型返回的 JSON 字符串解析为内部使用的 ToolCommand。
 *
 * - 解析失败时返回 null，不抛出异常，避免影响主流程
 * - 仅识别 action / params 两个字段，其余字段忽略
 * - 支持单个指令和多个指令（数组格式）
 */
object ToolCommandParser {

    /**
     * 解析单个指令
     */
    fun parse(raw: String?): ToolCommand? {
        if (raw.isNullOrBlank()) return null

        return try {
            val element = json.parseToJsonElement(raw)
            val obj = element.jsonObject

            val actionValue = obj["action"] as? JsonPrimitive ?: return null
            val actionName = actionValue.content.uppercase()
            val action = parseAction(actionName) ?: return null

            val paramsElement = (obj["params"] as? JsonObject) ?: JsonObject(emptyMap())
            val paramsJson = paramsElement.toString()

            ToolCommand(
                action = action,
                rawParamsJson = paramsJson
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 解析多个指令（支持单个对象和数组格式）
     *
     * 支持格式：
     * - 单个：{"action":"CREATE_TASK","params":{...}}
     * - 多个：[{"action":"CREATE_TASK","params":{...}},{"action":"GO_TO","params":{...}}]
     */
    fun parseMultiple(raw: String?): List<ToolCommand> {
        if (raw.isNullOrBlank()) return emptyList()

        return try {
            val element = json.parseToJsonElement(raw)

            when (element) {
                is JsonArray -> {
                    // 数组格式，解析多个指令
                    element.jsonArray.mapNotNull { item ->
                        parseToolCommandFromObject(item.jsonObject)
                    }
                }
                is JsonObject -> {
                    // 单个对象格式
                    val cmd = parseToolCommandFromObject(element.jsonObject)
                    if (cmd != null) listOf(cmd) else emptyList()
                }
                else -> emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * 从 JSON 对象解析单个指令
     */
    private fun parseToolCommandFromObject(obj: JsonObject): ToolCommand? {
        val actionValue = obj["action"] as? JsonPrimitive ?: return null
        val actionName = actionValue.content.uppercase()
        val action = parseAction(actionName) ?: return null

        val paramsElement = (obj["params"] as? JsonObject) ?: JsonObject(emptyMap())
        val paramsJson = paramsElement.toString()

        return ToolCommand(
            action = action,
            rawParamsJson = paramsJson
        )
    }

    /**
     * 解析 action 字符串
     */
    private fun parseAction(actionName: String): ToolAction? {
        return when (actionName) {
            "NO_ACTION" -> ToolAction.NO_ACTION
            "CREATE_TASK" -> ToolAction.CREATE_TASK
            "UPDATE_TASK_STATUS" -> ToolAction.UPDATE_TASK_STATUS
            "GO_TO_SCREEN", "GO_TO" -> ToolAction.GO_TO_SCREEN
            "CREATE_NOTE_FROM_ANSWER" -> ToolAction.CREATE_NOTE_FROM_ANSWER
            "UPSERT_KG_NODE" -> ToolAction.UPSERT_KG_NODE
            "DELETE_KG_NODE" -> ToolAction.DELETE_KG_NODE
            "UPDATE_KG_NODE" -> ToolAction.UPDATE_KG_NODE
            "LIST_KG_NODES" -> ToolAction.LIST_KG_NODES
            "BATCH_UPSERT_KG_NODES" -> ToolAction.BATCH_UPSERT_KG_NODES
            "ADD_SCHEDULE_SLOT" -> ToolAction.ADD_SCHEDULE_SLOT
            "REMOVE_SCHEDULE_SLOT" -> ToolAction.REMOVE_SCHEDULE_SLOT
            "LIST_SCHEDULE" -> ToolAction.LIST_SCHEDULE
            else -> null
        }
    }
}

