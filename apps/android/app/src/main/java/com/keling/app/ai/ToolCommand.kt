package com.keling.app.ai

/**
 * AI 工具指令的内部表示结构。
 *
 * - 由模型按照约定输出 JSON（{"action":"...","params":{...}}），
 *   再由上层解析为此数据类，驱动应用内实际操作。
 * - 这里只定义通用结构和受支持的 action 枚举，具体参数字段由调用方解析。
 */
data class ToolCommand(
    val action: ToolAction,
    val rawParamsJson: String
)

/**
 * 目前支持的工具动作类型。
 *
 * - NO_ACTION：占位，表示本次对话不需要执行任何操作
 * - CREATE_TASK：创建学习任务
 * - UPDATE_TASK_STATUS：修改任务状态
 * - GO_TO_SCREEN：页面导航（首页/AI/任务/温室等）
 * - CREATE_NOTE_FROM_ANSWER：从当前 AI 回答生成学习笔记
 * - UPSERT_KG_NODE：在某课程的知识图谱中创建或更新节点
 * - DELETE_KG_NODE：删除知识图谱中的节点
 * - UPDATE_KG_NODE：更新已有节点的名称/描述/掌握度
 * - LIST_KG_NODES：列出某课程下的全部知识图谱节点
 * - ADD_SCHEDULE_SLOT：为课程添加课时
 * - REMOVE_SCHEDULE_SLOT：删除某个课时
 * - LIST_SCHEDULE：列出课表（今日/本周）
 *
 * 后续可以在不破坏现有逻辑的前提下，逐步扩展更多枚举值。
 */
enum class ToolAction {
    NO_ACTION,
    CREATE_TASK,
    UPDATE_TASK_STATUS,
    GO_TO_SCREEN,
    CREATE_NOTE_FROM_ANSWER,
    UPSERT_KG_NODE,
    DELETE_KG_NODE,
    UPDATE_KG_NODE,
    LIST_KG_NODES,
    BATCH_UPSERT_KG_NODES,  // 批量创建知识节点
    ADD_SCHEDULE_SLOT,
    REMOVE_SCHEDULE_SLOT,
    LIST_SCHEDULE
}

