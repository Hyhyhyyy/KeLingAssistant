package com.keling.app.ai

import com.keling.app.data.json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * 将模型返回的 JSON 字符串解析为内部使用的 ToolCommand。
 *
 * - 解析失败时返回 null，不抛出异常，避免影响主流程
 * - 仅识别 action / params 两个字段，其余字段忽略
 */
object ToolCommandParser {

    fun parse(raw: String?): ToolCommand? {
        if (raw.isNullOrBlank()) return null

        return try {
            val element = json.parseToJsonElement(raw)
            val obj = element.jsonObject

            val actionValue = obj["action"] as? JsonPrimitive ?: return null
            val actionName = actionValue.content.uppercase()
            val action = when (actionName) {
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
                else -> return null
            }

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
}

