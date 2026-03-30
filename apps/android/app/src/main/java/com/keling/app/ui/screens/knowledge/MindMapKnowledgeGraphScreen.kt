package com.keling.app.ui.screens.knowledge

/**
 * =========================
 * 思维导图式知识图谱界面 - 增强版
 * =========================
 *
 * 特点：
 * - 可拖动节点
 * - 曲线箭头连接
 * - 树形自动布局
 * - 不同层级不同颜色
 * - 支持缩放和平移
 * - 位置保存
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keling.app.R
import com.keling.app.data.KnowledgeNode
import com.keling.app.ui.components.KnowledgeNodeEditDialog
import com.keling.app.ui.theme.*
import com.keling.app.viewmodel.AppViewModel
import kotlin.math.max
import kotlin.math.roundToInt

// ==================== 层级颜色定义 ====================

val LevelColors = listOf(
    Color(0xFF6B5B95),  // 第0层（根节点）- 深紫色
    Color(0xFF88B04B),  // 第1层 - 翠绿色
    Color(0xFFF7CAC9),  // 第2层 - 粉红色
    Color(0xFF92A8D1),  // 第3层 - 淡蓝色
    Color(0xFFFFB07C),  // 第4层 - 橙色
    Color(0xFFB8E0D2),  // 第5层 - 薄荷绿
    Color(0xFFD4A5FF),  // 第6层 - 薰衣草紫
    Color(0xFFFFF3B0),  // 第7层+ - 柠檬黄
)

fun getLevelColor(level: Int): Color {
    return LevelColors.getOrElse(level) { LevelColors.last() }
}

fun getLevelColorLight(level: Int): Color {
    return getLevelColor(level).copy(alpha = 0.15f)
}

// ==================== 节点位置数据 ====================

/**
 * 可拖动节点位置状态
 */
data class DraggableNodeState(
    val node: KnowledgeNode,
    var offsetX: Float,
    var offsetY: Float,
    val level: Int
)

// ==================== 主界面 ====================

@Composable
fun MindMapKnowledgeGraphScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val selectedCourseId = viewModel.selectedCourseId.value
    val courses = viewModel.courses.value

    // 尝试通过 ID 或名称查找课程
    val course = courses.find { it.id == selectedCourseId || it.name == selectedCourseId }
    val effectiveCourseId = course?.id ?: selectedCourseId

    val rawNodes = viewModel.knowledgeNodes.value
    val nodes = remember(effectiveCourseId, rawNodes) {
        effectiveCourseId?.let { id ->
            rawNodes.filter { it.courseId == id }
        } ?: emptyList()
    }

    // 缩放和平移状态
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // 可拖动节点状态
    val nodeStates = remember(nodes) {
        mutableStateMapOf<String, DraggableNodeState>().apply {
            nodes.forEach { node ->
                val level = calculateNodeLevel(node, nodes)
                this[node.id] = DraggableNodeState(
                    node = node,
                    offsetX = node.positionX * 800f,
                    offsetY = node.positionY * 600f,
                    level = level
                )
            }
        }
    }

    // 节点编辑对话框状态
    var showEditDialog by remember { mutableStateOf(false) }
    var editingNode by remember { mutableStateOf<KnowledgeNode?>(null) }

    // 计算内容边界，用于初始居中
    val density = LocalDensity.current
    val contentBounds = remember(nodeStates.values.toList()) {
        val allStates = nodeStates.values.toList()
        if (allStates.isEmpty()) {
            null
        } else {
            var minX = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var minY = Float.MAX_VALUE
            var maxY = Float.MIN_VALUE
            allStates.forEach { state ->
                minX = minOf(minX, state.offsetX)
                maxX = maxOf(maxX, state.offsetX + 180f)
                minY = minOf(minY, state.offsetY)
                maxY = maxOf(maxY, state.offsetY + 60f)
            }
            Size(maxX - minX, maxY - minY) to Offset(minX, minY)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 背景图片
        Image(
            painter = painterResource(id = R.drawable.bg_page),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 顶部导航栏
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 返回按钮
                    Surface(
                        onClick = onBack,
                        shape = CircleShape,
                        color = BeigeSurface
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

                    Spacer(modifier = Modifier.width(16.dp))

                    // 标题
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = course?.name ?: "知识图谱",
                            style = MaterialTheme.typography.titleMedium,
                            color = EarthBrown,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "共 ${nodes.size} 个知识点 · 可拖动编辑",
                            style = MaterialTheme.typography.labelSmall,
                            color = EarthBrownLight
                        )
                    }

                    // 缩放控制
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            onClick = { scale = (scale * 0.8f).coerceIn(0.3f, 3f) },
                            shape = CircleShape,
                            color = BeigeSurface,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("−", color = EarthBrown, fontSize = 20.sp)
                            }
                        }

                        Text(
                            text = "${(scale * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = EarthBrown,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Surface(
                            onClick = { scale = (scale * 1.25f).coerceIn(0.3f, 3f) },
                            shape = CircleShape,
                            color = BeigeSurface,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("+", color = EarthBrown, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }
        }

        // 主画布区域
        if (nodes.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "📭", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (effectiveCourseId == null) "请先选择一个课程"
                               else "${course?.name ?: effectiveCourseId} 还没有知识点",
                        style = MaterialTheme.typography.titleMedium,
                        color = EarthBrown,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (effectiveCourseId == null) "返回温室选择一个星球"
                               else "和 AI 聊聊，让它帮你创建知识图谱吧！",
                        style = MaterialTheme.typography.bodySmall,
                        color = EarthBrownLight,
                        textAlign = TextAlign.Center
                    )
                    if (effectiveCourseId != null && course != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            onClick = { viewModel.navigateTo("ai") },
                            shape = RoundedCornerShape(12.dp),
                            color = WarmSunOrange
                        ) {
                            Text(
                                text = "和 AI 聊聊",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        } else {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 72.dp)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.3f, 3f)
                            offset += pan
                        }
                    }
            ) {
                val widthPx = with(density) { maxWidth.toPx() }
                val heightPx = with(density) { maxHeight.toPx() }

                // 初始居中
                LaunchedEffect(contentBounds) {
                    contentBounds?.let { (size, minOffset) ->
                        val contentWidth = size.width
                        val contentHeight = size.height

                        val scaleX = widthPx / contentWidth
                        val scaleY = heightPx / contentHeight
                        scale = minOf(scaleX, scaleY, 1.5f).coerceIn(0.5f, 2f)

                        offset = Offset(
                            x = (widthPx - contentWidth * scale) / 2f - minOffset.x * scale,
                            y = (heightPx - contentHeight * scale) / 2f - minOffset.y * scale
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                ) {
                    // 绘制曲线连线
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        nodeStates.values.forEach { state ->
                            val node = state.node
                            node.childIds.forEach { childId ->
                                val childState = nodeStates[childId]
                                if (childState != null) {
                                    val startX = state.offsetX + 180f
                                    val startY = state.offsetY + 30f
                                    val endX = childState.offsetX
                                    val endY = childState.offsetY + 30f

                                    // 绘制曲线箭头
                                    val path = Path().apply {
                                        moveTo(startX, startY)
                                        val midX = (startX + endX) / 2f
                                        cubicTo(
                                            midX, startY,
                                            midX, endY,
                                            endX, endY
                                        )
                                    }

                                    val color = getLevelColor(state.level)
                                    drawPath(
                                        path = path,
                                        color = color.copy(alpha = 0.6f),
                                        style = Stroke(width = 2.5f)
                                    )

                                    // 绘制箭头
                                    val arrowSize = 8f
                                    val angle = kotlin.math.atan2(endY - (startY + endY) / 2, endX - midX)
                                    val arrowPath = Path().apply {
                                        moveTo(endX, endY)
                                        lineTo(
                                            endX - arrowSize * kotlin.math.cos(angle - kotlin.math.PI / 6).toFloat(),
                                            endY - arrowSize * kotlin.math.sin(angle - kotlin.math.PI / 6).toFloat()
                                        )
                                        moveTo(endX, endY)
                                        lineTo(
                                            endX - arrowSize * kotlin.math.cos(angle + kotlin.math.PI / 6).toFloat(),
                                            endY - arrowSize * kotlin.math.sin(angle + kotlin.math.PI / 6).toFloat()
                                        )
                                    }
                                    drawPath(
                                        path = arrowPath,
                                        color = color.copy(alpha = 0.6f),
                                        style = Stroke(width = 2f)
                                    )
                                }
                            }
                        }
                    }

                    // 绘制可拖动节点
                    nodeStates.values.forEach { state ->
                        DraggableNodeView(
                            state = state,
                            onPositionChange = { newOffset ->
                                state.offsetX = newOffset.x
                                state.offsetY = newOffset.y
                            },
                            onClick = {
                                editingNode = state.node
                                showEditDialog = true
                            }
                        )
                    }
                }
            }
        }

        // 图例
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
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
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "层级图例",
                        style = MaterialTheme.typography.labelSmall,
                        color = EarthBrown,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LevelColors.take(5).forEachIndexed { index, color ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "第${index + 1}层",
                                style = MaterialTheme.typography.labelSmall,
                                color = EarthBrownLight
                            )
                        }
                    }
                }
            }
        }

        // 添加知识点按钮
        if (effectiveCourseId != null) {
            Surface(
                onClick = {
                    editingNode = null
                    showEditDialog = true
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = WarmSunOrange,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "✨", fontSize = 16.sp, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "添加知识点",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 保存按钮
            Surface(
                onClick = {
                    // 保存节点位置到ViewModel
                    nodeStates.values.forEach { state ->
                        val updatedNode = state.node.copy(
                            positionX = state.offsetX / 800f,
                            positionY = state.offsetY / 600f
                        )
                        viewModel.upsertKnowledgeNode(updatedNode)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MintGreen,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💾", fontSize = 16.sp, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "保存布局",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // 节点编辑对话框
    if (showEditDialog && effectiveCourseId != null) {
        KnowledgeNodeEditDialog(
            node = editingNode,
            courseId = effectiveCourseId,
            availableParents = nodes.filter { it.id != editingNode?.id },
            onDismiss = { showEditDialog = false },
            onSave = { node ->
                viewModel.upsertKnowledgeNode(node)
                showEditDialog = false
            },
            onDelete = if (editingNode != null) {
                {
                    viewModel.deleteKnowledgeNodeByCourseAndName(effectiveCourseId, editingNode!!.name)
                    showEditDialog = false
                }
            } else null
        )
    }
}

// ==================== 辅助函数 ====================

/**
 * 计算节点的层级
 */
fun calculateNodeLevel(node: KnowledgeNode, allNodes: List<KnowledgeNode>): Int {
    if (node.parentIds.isEmpty()) return 0

    val nodeMap = allNodes.associateBy { it.id }
    val visited = mutableSetOf<String>()

    fun dfs(nodeId: String): Int {
        if (nodeId in visited) return 0
        visited.add(nodeId)

        val current = nodeMap[nodeId] ?: return 0
        if (current.parentIds.isEmpty()) return 0

        return 1 + (current.parentIds.mapNotNull { dfs(it) }.maxOrNull() ?: 0)
    }

    return 1 + (node.parentIds.mapNotNull { dfs(it) }.maxOrNull() ?: 0)
}

// ==================== 可拖动节点视图 ====================

@Composable
private fun DraggableNodeView(
    state: DraggableNodeState,
    onPositionChange: (Offset) -> Unit,
    onClick: () -> Unit
) {
    val color = getLevelColor(state.level)
    val colorLight = getLevelColorLight(state.level)

    val infiniteTransition = rememberInfiniteTransition(label = "node")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000 + state.level * 500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .offset { IntOffset(state.offsetX.roundToInt() + dragOffset.x.roundToInt(), state.offsetY.roundToInt() + dragOffset.y.roundToInt()) }
            .widthIn(min = 140.dp, max = 180.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        onPositionChange(Offset(state.offsetX + dragOffset.x, state.offsetY + dragOffset.y))
                        dragOffset = Offset.Zero
                    },
                    onDrag = { _, dragAmount ->
                        dragOffset += Offset(dragAmount.x, dragAmount.y)
                    }
                )
            }
            .drawBehind {
                drawRoundRect(
                    color = color.copy(alpha = if (isDragging) glowAlpha * 2 else glowAlpha),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    size = Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                    topLeft = Offset(-2.dp.toPx(), -2.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(12.dp))
            .background(CreamWhite.copy(alpha = 0.9f))
            .clickable(enabled = !isDragging) { onClick() }
    ) {
        Box {
            Image(
                painter = painterResource(id = R.drawable.bg_card_module),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // 节点名称
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "●",
                        color = color,
                        fontSize = 8.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = state.node.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = EarthBrown,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 掌握度进度条
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(BeigeSurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(state.node.masteryLevel.coerceIn(0f, 1f))
                            .background(color)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 掌握度文本
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(state.node.masteryLevel * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.node.masteryLevel >= 0.6f) MintGreen
                               else if (state.node.masteryLevel >= 0.3f) WarmSunOrange
                               else RoseRed
                    )
                    Text(
                        text = "L${state.level + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = color.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}