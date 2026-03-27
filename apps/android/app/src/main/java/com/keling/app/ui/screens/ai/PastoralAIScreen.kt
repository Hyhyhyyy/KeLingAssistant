package com.keling.app.ui.screens.ai

/**
 * =========================
 * 田园治愈风AI助手界面
 * 星球精灵助手
 * =========================
 *
 * 特点：
 * - 精灵形象的AI助手
 * - 温暖治愈的对话风格
 * - 柔和的消息气泡
 * - 漂浮花瓣装饰
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.keling.app.R
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keling.app.ai.AIResponse
import com.keling.app.ai.EnhancedLearningProfileProvider
import com.keling.app.ai.LearningContext
import com.keling.app.ai.ResponseType
import com.keling.app.ai.SimpleAIService
import com.keling.app.ai.ToolAction
import com.keling.app.ai.ToolCommand
import com.keling.app.ai.ToolCommandParser
import com.keling.app.ai.tools.AiToolExecutor
import com.keling.app.ai.tools.DefaultNavigationTool
import com.keling.app.ai.tools.DefaultTaskTool
import com.keling.app.ai.tools.DefaultKnowledgeGraphTool
import com.keling.app.ai.tools.DefaultNoteTool
import com.keling.app.data.KnowledgeNode
import com.keling.app.data.TaskStatus
import com.keling.app.ui.theme.*
import com.keling.app.viewmodel.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import com.keling.app.data.json
import kotlin.random.Random

// ==================== 主页面 ====================

/**
 * 田园风AI助手页面
 */
@Composable
fun PastoralAIScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    // ===== 状态管理 =====
    var messages by remember { mutableStateOf(listOf(
        ChatMessageUi(
            content = "你好呀~ 我是你的星球精灵助手 ✿\n\n我可以帮你：\n• 制定培育计划\n• 分析学习薄弱点\n• 提醒复习时机\n\n试试问我「今天怎么学」吧~",
            isUser = false,
            type = ResponseType.GENERAL
        )
    )) }

    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val scope = rememberCoroutineScope()

    val toolExecutor = remember {
        AiToolExecutor(
            taskTool = DefaultTaskTool(viewModel),
            navigationTool = DefaultNavigationTool(viewModel),
            noteTool = DefaultNoteTool(viewModel),
            knowledgeGraphTool = DefaultKnowledgeGraphTool(viewModel),
            scheduleTool = com.keling.app.ai.tools.DefaultScheduleTool(viewModel)
        )
    }

    var pendingCreateCommand by remember { mutableStateOf<ToolCommand?>(null) }
    var kgPreviewCourseId by remember { mutableStateOf<String?>(null) }
    var kgPreviewFocusNodeId by remember { mutableStateOf<String?>(null) }
    var kgPreviewNodes by remember { mutableStateOf<List<KnowledgeNode>>(emptyList()) }

    val onKgPreview: (String, String?) -> Unit = { courseIdOrName, focusNodeName ->
        // 尝试通过 ID 或名称查找课程
        val course = viewModel.courses.value.find { it.id == courseIdOrName || it.name == courseIdOrName }

        if (course != null) {
            // 找到课程，使用其真实 ID
            val finalCourseId = course.id
            val nodes = viewModel.knowledgeNodesForCourse(finalCourseId)
            kgPreviewCourseId = finalCourseId
            kgPreviewNodes = nodes

            kgPreviewFocusNodeId = focusNodeName?.let { name ->
                nodes.firstOrNull { it.name == name }?.id
            }
        } else {
            // 没找到课程，检查是否有使用该名称创建的知识节点
            val nodesByCourseId = viewModel.knowledgeNodes.value.filter { it.courseId == courseIdOrName }
            if (nodesByCourseId.isNotEmpty()) {
                // 有知识节点使用该名称作为 courseId，显示它们
                kgPreviewCourseId = courseIdOrName
                kgPreviewNodes = nodesByCourseId
                kgPreviewFocusNodeId = focusNodeName?.let { name ->
                    nodesByCourseId.firstOrNull { it.name == name }?.id
                }
            } else {
                // 完全找不到，清空预览
                kgPreviewCourseId = null
                kgPreviewNodes = emptyList()
                kgPreviewFocusNodeId = null
            }
        }
    }

    // ===== UI布局 =====
    Box(modifier = Modifier.fillMaxSize()) {
        // 背景
        PastoralAIBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            // ----- 顶部导航栏 -----
            PastoralAIHeader(onBack = onBack, isLoading = isLoading)

            HorizontalDivider(color = EarthBrown.copy(alpha = 0.08f))

            // ----- 消息列表区域 -----
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { message ->
                    PastoralMessageBubble(message = message)
                }

                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = WarmSunOrange
                            )
                        }
                    }
                }
            }

            // ----- 任务确认卡片 -----
            pendingCreateCommand?.let {
                PastoralConfirmCard(
                    onConfirm = {
                        val cmd = pendingCreateCommand ?: return@PastoralConfirmCard
                        val result = toolExecutor.execute(cmd)
                        pendingCreateCommand = null
                        messages = messages + ChatMessageUi(
                            content = result.message,
                            isUser = false,
                            type = if (result.success) ResponseType.GENERAL else ResponseType.ERROR
                        )
                    },
                    onCancel = {
                        pendingCreateCommand = null
                        messages = messages + ChatMessageUi(
                            content = "已取消本次任务创建~ 需要的话我可以重新帮你设计哦 ✿",
                            isUser = false,
                            type = ResponseType.GENERAL
                        )
                    }
                )
            }

            // ----- 知识图谱预览卡片 -----
            kgPreviewCourseId?.let { courseId ->
                PastoralKnowledgeCard(
                    courseName = viewModel.courses.value.firstOrNull { it.id == courseId }?.name
                        ?: viewModel.knowledgeNodes.value.firstOrNull { it.courseId == courseId }?.courseId
                        ?: "知识图谱",
                    nodes = kgPreviewNodes,
                    onOpenGraph = {
                        viewModel.openKnowledgeGraph(courseId)
                    },
                    onLocate = {
                        val focusId = kgPreviewFocusNodeId
                        viewModel.openKnowledgeGraph(courseId, focusId)
                    }
                )
            }

            // ----- 快捷建议区域 -----
            if (!isLoading && messages.size <= 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("今天怎么学", "我哪里薄弱", "提醒我复习", "帮我制定计划").forEach { suggestion ->
                        PastoralSuggestionChip(
                            text = suggestion,
                            onClick = {
                                val text = suggestion
                                inputText = ""
                                sendPastoralMessage(
                                    text = text,
                                    viewModel = viewModel,
                                    scope = scope,
                                    currentMessages = messages,
                                    onUpdateMessages = { messages = it },
                                    onLoadingChange = { isLoading = it },
                                    toolExecutor = toolExecutor,
                                    onPendingCreate = { cmd -> pendingCreateCommand = cmd },
                                    onKnowledgeGraphPreview = onKgPreview,
                                    onComplete = { }
                                )
                            }
                        )
                    }
                }
            }

            HorizontalDivider(color = EarthBrown.copy(alpha = 0.08f))

            // ----- 底部输入区域 -----
            PastoralInputArea(
                inputText = inputText,
                onInputChange = { inputText = it },
                isLoading = isLoading,
                onSend = {
                    if (inputText.isNotBlank() && !isLoading) {
                        val text = inputText
                        inputText = ""
                        sendPastoralMessage(
                            text = text,
                            viewModel = viewModel,
                            scope = scope,
                            currentMessages = messages,
                            onUpdateMessages = { messages = it },
                            onLoadingChange = { isLoading = it },
                            toolExecutor = toolExecutor,
                            onPendingCreate = { cmd -> pendingCreateCommand = cmd },
                            onKnowledgeGraphPreview = onKgPreview,
                            onComplete = { }
                        )
                    }
                }
            )
        }
    }
}

// ==================== 组件 ====================

/**
 * 田园风AI背景
 */
@Composable
private fun PastoralAIBackground() {
    Image(
        painter = painterResource(id = R.drawable.bg_page),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds
    )
}

/**
 * 田园风AI头部
 */
@Composable
private fun PastoralAIHeader(
    onBack: () -> Unit,
    isLoading: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "header")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回按钮
        Surface(
            onClick = onBack,
            shape = CircleShape,
            color = BeigeSurface,
            shadowElevation = 0.dp
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_left),
                    contentDescription = "返回",
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 精灵头像
        Box(
            modifier = Modifier.size(52.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color.Transparent
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_planet_sprite),
                    contentDescription = "星球精灵",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 标题和状态
        Column {
            Text(
                text = "星球精灵",
                style = MaterialTheme.typography.titleMedium,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            if (isLoading) WarmSunOrange else MintGreen,
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isLoading) "思考中..." else "在线",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isLoading) WarmSunOrange else MintGreen
                )
            }
        }
    }
}

/**
 * 田园风消息气泡
 */
@Composable
private fun PastoralMessageBubble(message: ChatMessageUi) {
    val isUser = message.isUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) {
                WarmSunOrange
            } else {
                CreamWhite
            },
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else EarthBrown,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

/**
 * 田园风建议标签
 */
@Composable
private fun PastoralSuggestionChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = CreamWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, WarmSunOrange.copy(alpha = 0.3f)),
        shadowElevation = 0.dp
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = WarmSunOrange,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

/**
 * 田园风确认卡片
 */
@Composable
private fun PastoralConfirmCard(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = CreamWhite.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Box {
            Image(
                painter = painterResource(id = R.drawable.bg_card_module),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "✿ 精灵为你设计了一个学习任务",
                style = MaterialTheme.typography.bodyMedium,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "你可以查看任务详情，确认后再添加到计划板~",
                style = MaterialTheme.typography.bodySmall,
                color = EarthBrown.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onCancel) {
                    Text("暂不创建", color = EarthBrown.copy(alpha = 0.6f))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarmSunOrange
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("确认创建")
                }
            }
            }
        }
    }
}

/**
 * 田园风知识图谱卡片
 */
@Composable
@Suppress("UNUSED_PARAMETER")
private fun PastoralKnowledgeCard(
    courseName: String,
    nodes: List<KnowledgeNode>,
    onOpenGraph: () -> Unit,
    onLocate: () -> Unit
) {
    val limited = nodes.take(6)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = CreamWhite.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Box {
            Image(
                painter = painterResource(id = R.drawable.bg_card_module),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "✿ 知识图谱已更新（${courseName}）",
                style = MaterialTheme.typography.bodyMedium,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (limited.isEmpty()) {
                Text(
                    text = "还没有知识节点，和精灵聊聊添加一些吧~",
                    style = MaterialTheme.typography.bodySmall,
                    color = EarthBrown.copy(alpha = 0.6f)
                )
            } else {
                limited.forEach { node ->
                    Text(
                        text = "• ${node.name}（掌握度 ${(node.masteryLevel * 100).toInt()}%）",
                        style = MaterialTheme.typography.bodySmall,
                        color = EarthBrown.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onOpenGraph) {
                    Text("打开图谱", color = WarmSunOrange)
                }
            }
            }
        }
    }
}

/**
 * 田园风输入区域
 */
@Composable
private fun PastoralInputArea(
    inputText: String,
    onInputChange: (String) -> Unit,
    isLoading: Boolean,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            placeholder = {
                Text("和精灵聊聊...", color = EarthBrown.copy(alpha = 0.4f))
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CreamWhite,
                unfocusedContainerColor = CreamWhite,
                focusedBorderColor = WarmSunOrange,
                unfocusedBorderColor = EarthBrown.copy(alpha = 0.15f)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            maxLines = 3
        )

        Spacer(modifier = Modifier.width(10.dp))

        // 发送按钮
        Surface(
            onClick = onSend,
            enabled = inputText.isNotBlank() && !isLoading,
            shape = RoundedCornerShape(14.dp),
            color = if (inputText.isNotBlank() && !isLoading) WarmSunOrange else EarthBrown.copy(alpha = 0.15f)
        ) {
            Box(
                modifier = Modifier.padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "➤",
                    fontSize = 18.sp,
                    color = if (inputText.isNotBlank() && !isLoading) Color.White else EarthBrown.copy(alpha = 0.3f)
                )
            }
        }
    }
}

// ==================== 消息数据类 ====================

data class ChatMessageUi(
    val content: String,
    val isUser: Boolean,
    val type: ResponseType
)

// ==================== 发送消息函数 ====================

private fun sendPastoralMessage(
    text: String,
    viewModel: AppViewModel,
    scope: CoroutineScope,
    currentMessages: List<ChatMessageUi>,
    onUpdateMessages: (List<ChatMessageUi>) -> Unit,
    onLoadingChange: (Boolean) -> Unit,
    toolExecutor: AiToolExecutor,
    onPendingCreate: (ToolCommand) -> Unit,
    onKnowledgeGraphPreview: (courseIdOrName: String, focusNodeName: String?) -> Unit = { _, _ -> },
    onComplete: () -> Unit = {}
) {
    val userMessage = ChatMessageUi(
        content = text,
        isUser = true,
        type = ResponseType.GENERAL
    )
    onUpdateMessages(currentMessages + userMessage)
    onLoadingChange(true)

    scope.launch(Dispatchers.IO) {
        try {
            // 构建完整的学习上下文
            val cal = java.util.Calendar.getInstance()
            val dayOfWeek = (cal.get(java.util.Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 7 else it }

            val learningContext = LearningContext(
                user = viewModel.currentUser.value,
                courses = viewModel.courses.value,
                tasks = viewModel.tasks.value,
                notes = viewModel.notes.value,
                knowledgeNodes = viewModel.knowledgeNodes.value,
                todaySchedule = viewModel.getTodaySchedule(dayOfWeek),
                currentHour = cal.get(java.util.Calendar.HOUR_OF_DAY),
                dayOfWeek = dayOfWeek
            )

            val response = SimpleAIService.processWith(text, learningContext)

            withContext(Dispatchers.Main) {
                onLoadingChange(false)

                val baseAiMessage = ChatMessageUi(
                    content = response.content,
                    isUser = false,
                    type = response.type
                )

                val messages = mutableListOf<ChatMessageUi>()
                messages += currentMessages
                messages += userMessage
                messages += baseAiMessage

                val cmd = ToolCommandParser.parse(response.toolCommandJson)
                if (cmd != null) {
                    if (cmd.action == ToolAction.CREATE_TASK) {
                        onPendingCreate(cmd)
                    } else {
                        val result = toolExecutor.execute(cmd)

                        // 知识图谱相关操作后显示预览
                        if (
                            cmd.action == ToolAction.UPSERT_KG_NODE ||
                            cmd.action == ToolAction.BATCH_UPSERT_KG_NODES ||
                            cmd.action == ToolAction.DELETE_KG_NODE ||
                            cmd.action == ToolAction.UPDATE_KG_NODE ||
                            cmd.action == ToolAction.LIST_KG_NODES
                        ) {
                            val params = json.parseToJsonElement(cmd.rawParamsJson).jsonObject
                            val courseIdOrName = (params["courseId"] as? kotlinx.serialization.json.JsonPrimitive)?.content
                            val name = (params["name"] as? kotlinx.serialization.json.JsonPrimitive)?.content

                            if (!courseIdOrName.isNullOrBlank()) {
                                onKnowledgeGraphPreview(courseIdOrName, name)
                            }
                        }

                        // 显示工具执行结果消息
                        if (result.message.isNotBlank()) {
                            messages += ChatMessageUi(
                                content = result.message,
                                isUser = false,
                                type = if (result.success) ResponseType.GENERAL else ResponseType.ERROR
                            )
                        }
                    }
                }

                onUpdateMessages(messages)
                onComplete()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onLoadingChange(false)
                onUpdateMessages(currentMessages + userMessage + ChatMessageUi(
                    content = "抱歉，精灵遇到了一些问题...请稍后再试 ✿",
                    isUser = false,
                    type = ResponseType.ERROR
                ))
                onComplete()
            }
        }
    }
}