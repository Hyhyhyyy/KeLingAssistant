package com.keling.app.ui.screens.knowledge

/**
 * =========================
 * 思维导图式知识图谱界面
 * =========================
 *
 * 特点：
 * - 树形自动布局
 * - 不同层级不同颜色
 * - 支持缩放和平移
 * - 清晰的父子关系连线
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keling.app.R
import com.keling.app.data.KnowledgeNode
import com.keling.app.ui.components.KnowledgeNodeEditDialog
import com.keling.app.ui.theme.*
import com.keling.app.viewmodel.AppViewModel
import kotlin.math.max

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

// ==================== 布局计算 ====================

/**
 * 思维导图节点位置
 */
data class MindMapNodePosition(
    val node: KnowledgeNode,
    val x: Float,
    val y: Float,
    val level: Int,
    val children: List<MindMapNodePosition>
)

/**
 * 计算思维导图布局
 * 从左到右的树形布局
 */
fun calculateMindMapLayout(
    nodes: List<KnowledgeNode>
): List<MindMapNodePosition> {
    if (nodes.isEmpty()) return emptyList()

    // 建立节点映射
    val nodeMap = nodes.associateBy { it.id }
    val childMap = mutableMapOf<String, MutableList<String>>()
    val rootNodes = mutableListOf<KnowledgeNode>()

    // 找出所有根节点（没有父节点的节点）
    nodes.forEach { node ->
        if (node.parentIds.isEmpty()) {
            rootNodes.add(node)
        } else {
            // 验证父节点是否存在
            val validParents = node.parentIds.filter { nodeMap.containsKey(it) }
            if (validParents.isEmpty()) {
                rootNodes.add(node)
            } else {
                validParents.forEach { parentId ->
                    childMap.getOrPut(parentId) { mutableListOf() }.add(node.id)
                }
            }
        }
    }

    // 如果没有根节点，把所有节点都作为根节点
    if (rootNodes.isEmpty() && nodes.isNotEmpty()) {
        rootNodes.addAll(nodes)
    }

    // 递归计算布局
    val positions = mutableListOf<MindMapNodePosition>()
    var currentY = 0f

    fun calculateSubtree(
        node: KnowledgeNode,
        level: Int,
        startY: Float
    ): Pair<MindMapNodePosition, Float> {
        val x = level * 280f + 50f

        // 获取子节点
        val childIds = childMap[node.id] ?: emptyList()
        val childNodes = childIds.mapNotNull { nodeMap[it] }

        if (childNodes.isEmpty()) {
            // 叶子节点
            val pos = MindMapNodePosition(node, x, startY, level, emptyList())
            return pos to startY + 80f
        }

        // 有子节点，递归计算
        val childPositions = mutableListOf<MindMapNodePosition>()
        var childY = startY

        childNodes.sortedBy { it.name }.forEach { child ->
            val (childPos, nextY) = calculateSubtree(child, level + 1, childY)
            childPositions.add(childPos)
            childY = nextY
        }

        // 父节点Y位置是子节点的中间
        val parentY = if (childPositions.isEmpty()) {
            startY
        } else {
            (childPositions.first().y + childPositions.last().y) / 2
        }

        return MindMapNodePosition(node, x, parentY, level, childPositions) to childY
    }

    rootNodes.sortedBy { it.name }.forEach { rootNode ->
        val (pos, nextY) = calculateSubtree(rootNode, 0, currentY)
        positions.add(pos)
        currentY = nextY + 40f
    }

    return positions
}

/**
 * 展平所有节点位置（包括嵌套的子节点）
 */
fun flattenPositions(positions: List<MindMapNodePosition>): List<MindMapNodePosition> {
    val result = mutableListOf<MindMapNodePosition>()
    fun collect(pos: MindMapNodePosition) {
        result.add(pos)
        pos.children.forEach { collect(it) }
    }
    positions.forEach { collect(it) }
    return result
}

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

    // 如果找到课程，使用其真实 ID；否则使用原始 selectedCourseId
    val effectiveCourseId = course?.id ?: selectedCourseId

    val rawNodes = viewModel.knowledgeNodes.value
    val nodes = remember(effectiveCourseId, rawNodes) {
        effectiveCourseId?.let { id ->
            rawNodes.filter { it.courseId == id }
        } ?: emptyList()
    }

    // 计算布局
    val positions = remember(nodes) {
        calculateMindMapLayout(nodes)
    }
    val allPositions = remember(positions) {
        flattenPositions(positions)
    }

    // 缩放和平移状态
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // 节点编辑对话框状态
    var showEditDialog by remember { mutableStateOf(false) }
    var editingNode by remember { mutableStateOf<KnowledgeNode?>(null) }

    // 计算内容边界，用于初始居中
    val density = LocalDensity.current
    val contentBounds = remember(allPositions) {
        if (allPositions.isEmpty()) {
            null
        } else {
            var minX = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var minY = Float.MAX_VALUE
            var maxY = Float.MIN_VALUE
            allPositions.forEach { pos ->
                minX = minOf(minX, pos.x)
                maxX = maxOf(maxX, pos.x + 200f)
                minY = minOf(minY, pos.y)
                maxY = maxOf(maxY, pos.y + 60f)
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
                        text = "共 ${nodes.size} 个知识点",
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
                               else "和 AI 聊聊，让它帮你创建知识图谱吧！\n例如：「给${course?.name ?: "这门课"}添加极限、导数、积分三个知识点」",
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

                        // 计算缩放使内容适合屏幕
                        val scaleX = widthPx / contentWidth
                        val scaleY = heightPx / contentHeight
                        scale = minOf(scaleX, scaleY, 1.5f).coerceIn(0.5f, 2f)

                        // 居中偏移
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
                    // 绘制连线
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        fun drawConnections(position: MindMapNodePosition) {
                            position.children.forEach { child ->
                                // 绘制曲线连接
                                val startX = position.x + 180f
                                val startY = position.y + 30f
                                val endX = child.x
                                val endY = child.y + 30f
                                val midX = (startX + endX) / 2f

                                val path = Path().apply {
                                    moveTo(startX, startY)
                                    cubicTo(midX, startY, midX, endY, endX, endY)
                                }
                                drawPath(
                                    path = path,
                                    color = getLevelColor(position.level).copy(alpha = 0.4f),
                                    style = Stroke(width = 2f)
                                )

                                drawConnections(child)
                            }
                        }

                        positions.forEach { drawConnections(it) }
                    }

                    // 绘制节点
                    allPositions.forEach { pos ->
                        MindMapNodeView(
                            node = pos.node,
                            level = pos.level,
                            onClick = {
                                editingNode = pos.node
                                showEditDialog = true
                            },
                            modifier = Modifier.absoluteOffset(
                                x = with(density) { pos.x.toDp() },
                                y = with(density) { pos.y.toDp() }
                            )
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

// ==================== 节点视图 ====================

@Composable
private fun MindMapNodeView(
    node: KnowledgeNode,
    level: Int,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val color = getLevelColor(level)
    val colorLight = getLevelColorLight(level)

    val infiniteTransition = rememberInfiniteTransition(label = "node")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000 + level * 500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .widthIn(min = 140.dp, max = 180.dp)
            .drawBehind {
                drawRoundRect(
                    color = color.copy(alpha = glowAlpha),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                    size = Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                    topLeft = Offset(-2.dp.toPx(), -2.dp.toPx())
                )
            },
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
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // 节点名称
                Text(
                    text = node.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

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
                            .fillMaxWidth(node.masteryLevel.coerceIn(0f, 1f))
                            .background(color)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 掌握度文本
                Text(
                    text = "${(node.masteryLevel * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (node.masteryLevel >= 0.6f) MintGreen
                           else if (node.masteryLevel >= 0.3f) WarmSunOrange
                           else RoseRed
                )
            }
        }
    }
}