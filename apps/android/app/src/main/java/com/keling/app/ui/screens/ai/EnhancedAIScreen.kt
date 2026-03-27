package com.keling.app

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keling.app.ai.*
import com.keling.app.ai.tools.*
import com.keling.app.R
import com.keling.app.components.Hexagon
import com.keling.app.data.KnowledgeNode
import com.keling.app.data.TaskStatus
import com.keling.app.ui.components.*
import com.keling.app.ui.theme.*
import com.keling.app.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * =========================
 * 游戏化 AI 聊天界面
 * RPG NPC 对话风格
 * =========================
 *
 * 特点：
 * - NPC角色形象与表情动画
 * - 对话框游戏化设计
 * - 任务式场景选择
 * - 粒子与光效氛围
 * - 沉浸式交互体验
 */

// ==================== 主界面 ====================

/**
 * 游戏化 AI 助手页面
 */
@Composable
fun EnhancedAIScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    // ===== 状态管理 =====
    var messages by remember { mutableStateOf(listOf(
        GameDialogueMessage(
            content = "旅行者，欢迎来到知识星域！\n\n我是恒星引擎，这片星域的守护者。我可以帮助你规划学习之旅、探索知识宝藏、战胜学习难关。\n\n你想要做什么呢？",
            isUser = false,
            type = ResponseType.GENERAL,
            npcExpression = NPCExpression.WELCOME
        )
    )) }

    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var currentExpression by remember { mutableStateOf(NPCExpression.NEUTRAL) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 当前场景
    var currentScenario by remember { mutableStateOf(AIScenario.CASUAL_CHAT) }
    var showQuestPanel by remember { mutableStateOf(true) }

    // AI 协调器
    val aiCoordinator = remember { AICoordinator() }

    // 工具执行器
    val toolExecutor = remember {
        AiToolExecutor(
            taskTool = DefaultTaskTool(viewModel),
            navigationTool = DefaultNavigationTool(viewModel),
            noteTool = DefaultNoteTool(viewModel),
            knowledgeGraphTool = DefaultKnowledgeGraphTool(viewModel),
            scheduleTool = DefaultScheduleTool(viewModel)
        )
    }

    // 自动滚动
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(maxOf(0, messages.size - 1))
        }
    }

    // ===== UI 布局 =====
    Box(modifier = Modifier.fillMaxSize()) {
        // 背景图片
        Image(
            painter = painterResource(id = R.drawable.bg_page),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // ----- 顶部导航栏 -----
            GameNPCHeader(
                npcExpression = if (isLoading) NPCExpression.THINKING else currentExpression,
                isLoading = isLoading,
                onBack = onBack
            )

            // ----- NPC 形象区 -----
            NPCCharacterDisplay(
                expression = if (isLoading) NPCExpression.THINKING else currentExpression,
                isAnimating = isLoading
            )

            // ----- 任务选择面板 -----
            AnimatedVisibility(
                visible = showQuestPanel && messages.size <= 2,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                QuestSelectionPanel(
                    onQuestSelect = { scenario ->
                        showQuestPanel = false
                        currentScenario = scenario
                        currentExpression = NPCExpression.HAPPY

                        val welcomeMessage = when (scenario) {
                            AIScenario.QUICK_PLAN -> "让我看看你的星图...嗯，今天的航线应该是这样的..."
                            AIScenario.WEAKNESS_DIAGNOSE -> "让我用星域探测器扫描一下你的知识图谱..."
                            AIScenario.EXAM_PREP -> "考试挑战！准备好迎接这场冒险了吗？"
                            AIScenario.CONCEPT_EXPLAIN -> "知识的结晶，让我为你展示它的奥秘..."
                            AIScenario.PRACTICE_SESSION -> "实战训练开始！准备好提升你的战斗力了吗？"
                            AIScenario.REVIEW_SESSION -> "根据星历推算，有些知识点需要重温了..."
                            AIScenario.CASUAL_CHAT -> "有什么想聊的？我随时在这里等待。"
                        }

                        messages = messages + GameDialogueMessage(
                            content = welcomeMessage,
                            isUser = false,
                            type = ResponseType.GENERAL,
                            npcExpression = NPCExpression.TALKING
                        )

                        // 如果是计划或诊断，自动执行
                        if (scenario in listOf(AIScenario.QUICK_PLAN, AIScenario.WEAKNESS_DIAGNOSE)) {
                            scope.launch {
                                isLoading = true
                                currentExpression = NPCExpression.THINKING
                                delay(1200)

                                val response = when (scenario) {
                                    AIScenario.QUICK_PLAN -> generateLocalPlan(viewModel)
                                    AIScenario.WEAKNESS_DIAGNOSE -> generateLocalWeaknessAnalysis(viewModel)
                                    else -> null
                                }

                                isLoading = false
                                currentExpression = NPCExpression.HAPPY

                                response?.let {
                                    messages = messages + GameDialogueMessage(
                                        content = it.content,
                                        isUser = false,
                                        type = it.type,
                                        toolJson = it.toolCommandJson,
                                        npcExpression = NPCExpression.NEUTRAL
                                    )
                                }
                            }
                        }
                    }
                )
            }

            // ----- 对话历史 -----
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { message ->
                    GameDialogueBubble(
                        message = message,
                        onToolExecute = { cmd ->
                            val result = toolExecutor.execute(cmd)
                            messages = messages + GameDialogueMessage(
                                content = result.message,
                                isUser = false,
                                type = if (result.success) ResponseType.GENERAL else ResponseType.ERROR,
                                npcExpression = if (result.success) NPCExpression.HAPPY else NPCExpression.WORRIED
                            )
                        }
                    )
                }

                // 思考中指示
                if (isLoading) {
                    item {
                        ThinkingIndicator()
                    }
                }
            }

            // ----- 底部输入区 -----
            GameDialogueInput(
                inputText = inputText,
                onInputChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank() && !isLoading) {
                        val text = inputText
                        inputText = ""
                        showQuestPanel = false

                        // 添加用户消息
                        messages = messages + GameDialogueMessage(
                            content = text,
                            isUser = true,
                            type = ResponseType.GENERAL
                        )

                        // 处理消息
                        scope.launch {
                            isLoading = true
                            currentExpression = NPCExpression.THINKING

                            // 先尝试本地规则
                            val localResponse = tryLocalResponse(text, viewModel)

                            if (localResponse != null) {
                                delay(800)
                                isLoading = false
                                currentExpression = NPCExpression.TALKING

                                messages = messages + GameDialogueMessage(
                                    content = localResponse.content,
                                    isUser = false,
                                    type = localResponse.type,
                                    toolJson = localResponse.toolCommandJson,
                                    npcExpression = NPCExpression.NEUTRAL
                                )
                            } else {
                                // 调用云端 AI
                                val context = buildLearningContext(viewModel)
                                val response = aiCoordinator.process(text, context)

                                isLoading = false
                                currentExpression = NPCExpression.HAPPY

                                messages = messages + GameDialogueMessage(
                                    content = response.content,
                                    isUser = false,
                                    type = response.type,
                                    toolJson = response.toolCommandJson,
                                    npcExpression = NPCExpression.NEUTRAL
                                )
                            }
                        }
                    }
                },
                enabled = !isLoading
            )
        }
    }
}

// ==================== NPC 组件 ====================

/**
 * NPC 表情枚举
 */
enum class NPCExpression {
    NEUTRAL,
    HAPPY,
    THINKING,
    TALKING,
    WELCOME,
    WORRIED,
    VICTORY
}

/**
 * 游戏 NPC 头部
 */
@Composable
private fun GameNPCHeader(
    npcExpression: NPCExpression,
    isLoading: Boolean,
    onBack: () -> Unit
) {
    val glowAlpha by rememberInfiniteTransition(label = "header").animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DeepSpace.copy(alpha = 0.8f),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 返回按钮
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BeigeSurface)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                painter = painterResource(id = R.drawable.ic_arrow_left),
                contentDescription = "返回",
                modifier = Modifier.size(72.dp)
            )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // NPC 名称和状态
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 发光指示器
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .graphicsLayer { alpha = glowAlpha }
                            .background(
                                when (npcExpression) {
                                    NPCExpression.THINKING -> StellarOrange
                                    NPCExpression.HAPPY -> LifeGreen
                                    else -> CrystalBlue
                                },
                                CircleShape
                            )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "恒星引擎",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = when (npcExpression) {
                        NPCExpression.THINKING -> "正在思考..."
                        NPCExpression.TALKING -> "正在解答"
                        NPCExpression.HAPPY -> "很高兴能帮到你"
                        else -> "知识星域守护者"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * NPC 角色显示
 */
@Composable
private fun NPCCharacterDisplay(
    expression: NPCExpression,
    isAnimating: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "npc")

    // 呼吸动画
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    // 旋转光环
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing)
        ),
        label = "ring"
    )

    // 思考时的晃动
    val thinkingShake by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // 外层旋转光环
        Box(
            modifier = Modifier
                .size(140.dp)
                .graphicsLayer { rotationZ = ringRotation }
                .drawBehind {
                    // 绘制旋转光环
                    val strokeWidth = 3.dp.toPx()
                    for (i in 0..2) {
                        drawArc(
                            color = StellarOrange.copy(alpha = 0.3f + i * 0.1f),
                            startAngle = i * 120f,
                            sweepAngle = 60f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokeWidth,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                    }
                }
        )

        // 主角色容器
        Box(
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer {
                    scaleX = breatheScale
                    scaleY = breatheScale
                    translationX = if (isAnimating && expression == NPCExpression.THINKING) thinkingShake else 0f
                },
            contentAlignment = Alignment.Center
        ) {
            // 内发光
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(20.dp)
                    .background(
                        when (expression) {
                            NPCExpression.THINKING -> StellarOrange.copy(alpha = 0.4f)
                            NPCExpression.HAPPY, NPCExpression.VICTORY -> LifeGreen.copy(alpha = 0.4f)
                            NPCExpression.WORRIED -> FlameRed.copy(alpha = 0.3f)
                            else -> CrystalBlue.copy(alpha = 0.3f)
                        },
                        CircleShape
                    )
            )

            // 核心
            Hexagon(
                size = 80.dp,
                backgroundColor = when (expression) {
                    NPCExpression.THINKING -> StellarOrangeDark
                    NPCExpression.HAPPY, NPCExpression.VICTORY -> LifeGreenDark
                    NPCExpression.WORRIED -> FlameRedDark
                    else -> CosmicSurface
                },
                content = {
                    // 表情显示
                    Text(
                        text = when (expression) {
                            NPCExpression.THINKING -> "🤔"
                            NPCExpression.HAPPY -> "😊"
                            NPCExpression.TALKING -> "💬"
                            NPCExpression.WELCOME -> "🌟"
                            NPCExpression.WORRIED -> "😅"
                            NPCExpression.VICTORY -> "🎉"
                            else -> "✨"
                        },
                        fontSize = 36.sp
                    )
                }
            )
        }
    }
}

/**
 * 任务选择面板
 */
@Composable
private fun QuestSelectionPanel(
    onQuestSelect: (AIScenario) -> Unit
) {
    val quests = listOf(
        QuestData(AIScenario.QUICK_PLAN, "今日航线", "规划今天的学习旅程", "📅", StellarOrange),
        QuestData(AIScenario.WEAKNESS_DIAGNOSE, "星域探测", "扫描知识薄弱点", "🔍", CrystalBlue),
        QuestData(AIScenario.CONCEPT_EXPLAIN, "知识解密", "解锁概念奥秘", "💡", NebulaPurple),
        QuestData(AIScenario.PRACTICE_SESSION, "战斗训练", "刷题提升战力", "⚔️", FlameRed)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "选择你的冒险",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        quests.forEach { quest ->
            QuestCard(
                quest = quest,
                onClick = { onQuestSelect(quest.scenario) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private data class QuestData(
    val scenario: AIScenario,
    val title: String,
    val description: String,
    val icon: String,
    val color: Color
)

@Composable
private fun QuestCard(
    quest: QuestData,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "quest")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = quest.color.copy(alpha = glowAlpha),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    size = Size(size.width + 8.dp.toPx(), size.height + 8.dp.toPx()),
                    topLeft = Offset(-4.dp.toPx(), -4.dp.toPx())
                )
            },
        shape = RoundedCornerShape(16.dp),
        color = CosmicSurface.copy(alpha = 0.8f),
        border = androidx.compose.foundation.BorderStroke(1.dp, quest.color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(quest.color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = quest.icon, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 文字
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quest.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = quest.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // 箭头
            Image(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                modifier = Modifier.size(60.dp)
            )
        }
    }
}

/**
 * 游戏化对话气泡
 */
@Composable
private fun GameDialogueBubble(
    message: GameDialogueMessage,
    onToolExecute: (ToolCommand) -> Unit
) {
    val isUser = message.isUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // NPC 对话框样式
        if (!isUser) {
            // NPC 名称
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = "✦",
                    style = MaterialTheme.typography.labelSmall,
                    color = StellarOrange
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "恒星引擎",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 20.dp
            ),
            color = if (isUser) {
                StellarOrange.copy(alpha = 0.2f)
            } else {
                CosmicSurface.copy(alpha = 0.9f)
            },
            border = if (!isUser) {
                androidx.compose.foundation.BorderStroke(
                    1.dp,
                    CrystalBlue.copy(alpha = 0.3f)
                )
            } else null,
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 消息内容
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else Color.White.copy(alpha = 0.95f),
                    lineHeight = 24.sp
                )

                // 工具执行按钮
                if (!isUser && message.toolJson != null) {
                    val toolName = extractToolName(message.toolJson)
                    if (toolName != null && toolName != "NO_ACTION") {
                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            onClick = {
                                val cmd = parseToolCommand(message.toolJson)
                                if (cmd != null) onToolExecute(cmd)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = StellarOrange.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚡",
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "执行: $toolName",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = StellarOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 思考中指示器
 */
@Composable
private fun ThinkingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking")

    val dotAlpha = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer { alpha = dotAlpha.value * (0.5f + index * 0.25f) }
                    .background(StellarOrange, CircleShape)
            )
            if (index < 2) Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

/**
 * 游戏化输入区
 */
@Composable
private fun GameDialogueInput(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    val glowAlpha by rememberInfiniteTransition(label = "input").animateFloat(
        initialValue = 0.1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DeepSpace.copy(alpha = 0.9f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .drawBehind {
                    if (enabled) {
                        drawRect(
                            color = StellarOrange.copy(alpha = glowAlpha * 0.1f),
                            topLeft = Offset(0f, -8.dp.toPx()),
                            size = Size(size.width, size.height + 8.dp.toPx())
                        )
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 输入框
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                placeholder = {
                    Text(
                        "向恒星引擎提问...",
                        color = Color.White.copy(alpha = 0.4f)
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CosmicSurface.copy(alpha = 0.5f),
                    unfocusedContainerColor = CosmicSurface.copy(alpha = 0.3f),
                    focusedBorderColor = StellarOrange,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = StellarOrange
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (enabled) onSend() }),
                enabled = enabled,
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 发送按钮
            Surface(
                onClick = { if (enabled) onSend() },
                shape = RoundedCornerShape(16.dp),
                color = if (enabled) StellarOrange else CosmicSurface,
                modifier = Modifier.size(52.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✦",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (enabled) Color.White else Color.White.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

// ==================== 背景效果 ====================

/**
 * 游戏化星空背景
 */
@Composable
private fun GameStarfieldBackground(
    modifier: Modifier = Modifier
) {
    val stars = remember { List(80) { StarParticle() } }
    val infiniteTransition = rememberInfiniteTransition(label = "starfield")

    Box(
        modifier = modifier
            .background(Brush.verticalGradient(GameGradients.starfield))
    ) {
        // 星星层
        stars.forEach { star ->
            TwinklingStarParticle(star = star)
        }

        // 极光效果
        AuroraEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.TopCenter)
        )
    }
}

private class StarParticle {
    val x = Random.nextFloat()
    val y = Random.nextFloat()
    val size = Random.nextFloat() * 2.5f + 0.5f
    val twinkleSpeed = Random.nextInt(1500, 4000)
    val brightness = Random.nextFloat() * 0.6f + 0.4f
}

@Composable
private fun TwinklingStarParticle(star: StarParticle) {
    val alpha by rememberInfiniteTransition(label = "star").animateFloat(
        initialValue = star.brightness * 0.3f,
        targetValue = star.brightness,
        animationSpec = infiniteRepeatable(
            animation = tween(star.twinkleSpeed, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = star.size.dp.toPx(),
                    center = Offset(x = star.x * size.width, y = star.y * size.height)
                )
            }
    )
}

@Composable
private fun AuroraEffect(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing)
        ),
        label = "offset1"
    )

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing)
        ),
        label = "offset2"
    )

    Box(
        modifier = modifier
            .drawBehind {
                // 极光渐变
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            StellarOrange.copy(alpha = 0.15f),
                            NebulaPurple.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height
                    )
                )
            }
            .graphicsLayer { alpha = 0.6f }
    )
}

// ==================== 数据类与工具函数 ====================

/**
 * 游戏对话消息
 */
data class GameDialogueMessage(
    val content: String,
    val isUser: Boolean,
    val type: ResponseType,
    val toolJson: String? = null,
    val npcExpression: NPCExpression = NPCExpression.NEUTRAL
)

private fun tryLocalResponse(input: String, viewModel: AppViewModel): AIResponse? {
    val engine = EnhancedLocalRuleEngine()
    val context = buildLearningContext(viewModel)
    return engine.process(input, context)
}

private fun buildLearningContext(viewModel: AppViewModel): LearningContext {
    val cal = java.util.Calendar.getInstance()
    val dayOfWeek = (cal.get(java.util.Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 7 else it }

    return LearningContext(
        user = viewModel.currentUser.value,
        courses = viewModel.courses.value,
        tasks = viewModel.tasks.value,
        notes = viewModel.notes.value,
        knowledgeNodes = viewModel.knowledgeNodes.value,
        todaySchedule = viewModel.getTodaySchedule(dayOfWeek),
        currentHour = cal.get(java.util.Calendar.HOUR_OF_DAY),
        dayOfWeek = dayOfWeek
    )
}

private fun generateLocalPlan(viewModel: AppViewModel): AIResponse {
    val ctx = buildLearningContext(viewModel)
    val engine = EnhancedLocalRuleEngine()
    return engine.process("今天怎么学", ctx) ?: AIResponse(
        content = "让我看看星图...",
        type = ResponseType.PLAN
    )
}

private fun generateLocalWeaknessAnalysis(viewModel: AppViewModel): AIResponse {
    val ctx = buildLearningContext(viewModel)
    val engine = EnhancedLocalRuleEngine()
    return engine.process("我哪里薄弱", ctx) ?: AIResponse(
        content = "正在扫描知识图谱...",
        type = ResponseType.ANALYSIS
    )
}

private fun extractToolName(json: String): String? {
    return try {
        val jsonObj = Json.parseToJsonElement(json).jsonObject
        jsonObj["action"]?.jsonPrimitive?.content
    } catch (e: Exception) {
        null
    }
}

private fun parseToolCommand(json: String): ToolCommand? {
    return try {
        val jsonObj = Json.parseToJsonElement(json).jsonObject
        val actionStr = jsonObj["action"]?.jsonPrimitive?.content ?: return null
        val action = ToolAction.values().find { it.name == actionStr } ?: return null
        ToolCommand(action, json)
    } catch (e: Exception) {
        null
    }
}