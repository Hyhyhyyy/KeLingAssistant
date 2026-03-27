package com.keling.app.ui.components

/**
 * =========================
 * 知识节点编辑对话框
 * =========================
 *
 * 用于创建和编辑知识图谱节点
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.keling.app.R
import com.keling.app.data.KnowledgeNode
import com.keling.app.ui.screens.knowledge.getLevelColor
import com.keling.app.ui.theme.*
import java.util.UUID

/**
 * 知识节点编辑对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeNodeEditDialog(
    node: KnowledgeNode?,
    courseId: String,
    availableParents: List<KnowledgeNode>,
    onDismiss: () -> Unit,
    onSave: (KnowledgeNode) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    // 表单数据
    var nodeName by remember { mutableStateOf(node?.name ?: "") }
    var nodeDescription by remember { mutableStateOf(node?.description ?: "") }
    var difficulty by remember { mutableStateOf(node?.difficulty ?: 3) }
    var masteryLevel by remember { mutableStateOf(node?.masteryLevel ?: 0f) }
    var selectedParentIds by remember { mutableStateOf(node?.parentIds ?: emptyList()) }

    // 验证
    val canSave = nodeName.isNotBlank()

    // 计算节点层级（用于显示颜色）
    val nodeLevel = remember(availableParents, selectedParentIds) {
        calculateNodeLevel(selectedParentIds, availableParents)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = CreamWhite,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (node == null) "创建知识点" else "编辑知识点",
                        style = MaterialTheme.typography.headlineSmall,
                        color = EarthBrown,
                        fontWeight = FontWeight.Bold
                    )

                    // 删除按钮
                    if (node != null && onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Text(
                                text = "🗑️",
                                fontSize = 20.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 表单内容
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 知识点名称
                    OutlinedTextField(
                        value = nodeName,
                        onValueChange = { nodeName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("知识点名称 *", color = EarthBrownLight) },
                        placeholder = { Text("如：极限的定义", color = EarthBrownLight.copy(alpha = 0.5f)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = getLevelColor(nodeLevel),
                            unfocusedBorderColor = WarmGray
                        )
                    )

                    // 描述
                    OutlinedTextField(
                        value = nodeDescription,
                        onValueChange = { nodeDescription = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        label = { Text("知识点描述", color = EarthBrownLight) },
                        placeholder = { Text("简要描述这个知识点...", color = EarthBrownLight.copy(alpha = 0.5f)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = getLevelColor(nodeLevel),
                            unfocusedBorderColor = WarmGray
                        )
                    )

                    // 难度选择
                    Text(
                        text = "难度等级",
                        style = MaterialTheme.typography.labelMedium,
                        color = EarthBrown
                    )

                    DifficultySelector(
                        difficulty = difficulty,
                        onDifficultyChange = { difficulty = it }
                    )

                    // 掌握度滑块
                    Text(
                        text = "掌握度：${(masteryLevel * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = EarthBrown
                    )

                    Slider(
                        value = masteryLevel,
                        onValueChange = { masteryLevel = it },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = getLevelColor(nodeLevel),
                            activeTrackColor = getLevelColor(nodeLevel),
                            inactiveTrackColor = WarmGray.copy(alpha = 0.3f)
                        )
                    )

                    // 父节点选择
                    if (availableParents.isNotEmpty()) {
                        Text(
                            text = "前置知识点",
                            style = MaterialTheme.typography.labelMedium,
                            color = EarthBrown
                        )

                        ParentNodeSelector(
                            availableParents = availableParents.filter { it.id != node?.id },
                            selectedParentIds = selectedParentIds,
                            onSelectionChange = { selectedParentIds = it }
                        )
                    }

                    // 预览卡片
                    if (nodeName.isNotBlank()) {
                        NodePreviewCard(
                            name = nodeName,
                            description = nodeDescription,
                            difficulty = difficulty,
                            masteryLevel = masteryLevel,
                            level = nodeLevel
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("取消", color = EarthBrownLight)
                    }

                    Button(
                        onClick = {
                            val finalNode = KnowledgeNode(
                                id = node?.id ?: UUID.randomUUID().toString(),
                                courseId = courseId,
                                name = nodeName.trim(),
                                description = nodeDescription.trim(),
                                parentIds = selectedParentIds,
                                difficulty = difficulty,
                                masteryLevel = masteryLevel,
                                isUnlocked = true
                            )
                            onSave(finalNode)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = canSave,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = getLevelColor(nodeLevel)
                        )
                    ) {
                        Text(
                            text = if (node == null) "创建" else "保存",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==================== 难度选择器 ====================

@Composable
private fun DifficultySelector(
    difficulty: Int,
    onDifficultyChange: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        (1..5).forEach { level ->
            val isSelected = difficulty == level
            val stars = "⭐".repeat(level)

            Surface(
                onClick = { onDifficultyChange(level) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) WarmSunOrange.copy(alpha = 0.2f) else BeigeSurface,
                modifier = Modifier.then(
                    if (isSelected) Modifier.border(1.dp, WarmSunOrange, RoundedCornerShape(8.dp))
                    else Modifier
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stars,
                        fontSize = 12.sp
                    )
                    Text(
                        text = when (level) {
                            1 -> "简单"
                            2 -> "较易"
                            3 -> "中等"
                            4 -> "较难"
                            5 -> "困难"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) WarmSunOrange else EarthBrownLight
                    )
                }
            }
        }
    }
}

// ==================== 父节点选择器 ====================

@Composable
private fun ParentNodeSelector(
    availableParents: List<KnowledgeNode>,
    selectedParentIds: List<String>,
    onSelectionChange: (List<String>) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(availableParents) { parent ->
            val isSelected = selectedParentIds.contains(parent.id)
            val parentLevel = calculateNodeLevel(parent.parentIds, availableParents)

            Surface(
                onClick = {
                    onSelectionChange(
                        if (isSelected) {
                            selectedParentIds - parent.id
                        } else {
                            selectedParentIds + parent.id
                        }
                    )
                },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) getLevelColor(parentLevel).copy(alpha = 0.2f) else BeigeSurface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 复选框
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isSelected) getLevelColor(parentLevel) else Color.Transparent
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else WarmGray,
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Text(
                                text = "✓",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 节点名称
                    Text(
                        text = parent.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = EarthBrown,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // 掌握度
                    Text(
                        text = "${(parent.masteryLevel * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = EarthBrownLight
                    )
                }
            }
        }
    }
}

// ==================== 节点预览卡片 ====================

@Composable
private fun NodePreviewCard(
    name: String,
    description: String,
    difficulty: Int,
    masteryLevel: Float,
    level: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "preview")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = getLevelColor(level).copy(alpha = glowAlpha),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    size = Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                    topLeft = Offset(-2.dp.toPx(), -2.dp.toPx())
                )
            },
        shape = RoundedCornerShape(16.dp),
        color = CreamWhite.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 颜色标识
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(getLevelColor(level))
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 名称
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // 难度星星
                Text(
                    text = "⭐".repeat(difficulty),
                    fontSize = 10.sp
                )
            }

            if (description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = EarthBrownLight,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 掌握度进度条
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "掌握度",
                    style = MaterialTheme.typography.labelSmall,
                    color = EarthBrownLight
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(WarmGray.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(masteryLevel)
                            .clip(RoundedCornerShape(2.dp))
                            .background(getLevelColor(level))
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(masteryLevel * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = getLevelColor(level),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==================== 工具函数 ====================

/**
 * 计算节点层级
 */
private fun calculateNodeLevel(
    parentIds: List<String>,
    allNodes: List<KnowledgeNode>
): Int {
    if (parentIds.isEmpty()) return 0

    val nodeById = allNodes.associateBy { it.id }
    var maxParentLevel = 0

    for (parentId in parentIds) {
        val parent = nodeById[parentId] ?: continue
        val parentLevel = calculateNodeLevel(parent.parentIds, allNodes)
        maxParentLevel = maxOf(maxParentLevel, parentLevel)
    }

    return maxParentLevel + 1
}