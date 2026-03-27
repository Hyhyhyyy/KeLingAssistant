package com.keling.app.ui.screens.notes

/**
 * =========================
 * 田园治愈风知识笔记系统
 * =========================
 *
 * 特点：
 * - 笔记列表展示
 * - 创建和编辑笔记
 * - AI生成笔记
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keling.app.R
import com.keling.app.data.Note
import com.keling.app.data.NoteSource
import com.keling.app.ui.components.RichTextEditor
import com.keling.app.ui.components.MarkdownRenderer
import com.keling.app.ui.theme.*
import com.keling.app.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PastoralNotesScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onAskAI: () -> Unit
) {
    val notes = viewModel.notes.value

    // 创建笔记对话框
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }

    if (showCreateDialog || editingNote != null) {
        NoteEditDialog(
            note = editingNote,
            onDismiss = {
                showCreateDialog = false
                editingNote = null
            },
            onSave = { title, content ->
                val note = Note(
                    id = editingNote?.id ?: "note_${System.currentTimeMillis()}",
                    title = title,
                    content = content,
                    sourceType = editingNote?.sourceType ?: NoteSource.USER_CREATED,
                    createdAt = editingNote?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                viewModel.addNote(note)
                showCreateDialog = false
                editingNote = null
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 背景图片
        Image(
            painter = painterResource(id = R.drawable.bg_page),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            // 顶部导航
            item {
                NotesHeader(
                    onBack = onBack,
                    onCreateNote = { showCreateDialog = true }
                )
            }

            // AI生成笔记入口
            item {
                AINoteCard(onAskAI = onAskAI)
            }

            // 笔记列表
            if (notes.isEmpty()) {
                item {
                    EmptyNotesState(onCreateNote = { showCreateDialog = true })
                }
            } else {
                items(notes) { note ->
                    NoteCard(
                        note = note,
                        onClick = { editingNote = note }
                    )
                }
            }

            // 底部留白
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ==================== 组件 ====================

/**
 * 页面头部
 */
@Composable
private fun NotesHeader(
    onBack: () -> Unit,
    onCreateNote: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
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

        // 标题
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "知识笔记",
                style = MaterialTheme.typography.titleLarge,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "记录学习点滴",
                style = MaterialTheme.typography.labelSmall,
                color = MintGreen
            )
        }

        // 创建按钮
        Surface(
            onClick = onCreateNote,
            shape = RoundedCornerShape(12.dp),
            color = MintGreen
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * AI生成笔记卡片
 */
@Composable
private fun AINoteCard(onAskAI: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai")

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
        onClick = onAskAI,
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = LavenderPurple.copy(alpha = glowAlpha),
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    size = Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                    topLeft = Offset(-2.dp.toPx(), -2.dp.toPx())
                )
            },
        shape = RoundedCornerShape(20.dp),
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
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            // AI图标
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = LavenderPurple.copy(alpha = 0.2f)
            ) {
                Box(
                    modifier = Modifier.padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_icon),
                        contentDescription = "AI",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI帮你记笔记",
                    style = MaterialTheme.typography.titleMedium,
                    color = LavenderPurple,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "和精灵聊聊，自动生成知识笔记",
                    style = MaterialTheme.typography.bodySmall,
                    color = EarthBrownLight
                )
            }

            Image(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                modifier = Modifier.size(60.dp)
            )
        }
        }
    }
}

/**
 * 笔记卡片
 */
@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "note")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val sourceColor = when (note.sourceType) {
        NoteSource.AI_GENERATED -> LavenderPurple
        NoteSource.USER_CREATED -> MintGreen
        NoteSource.CLASS_CAPTURE -> WarmSunOrange
        NoteSource.BOUNTY_REWARD -> CreamYellow
    }

    val sourceIcon = when (note.sourceType) {
        NoteSource.AI_GENERATED -> "🤖"
        NoteSource.USER_CREATED -> "📝"
        NoteSource.CLASS_CAPTURE -> "📸"
        NoteSource.BOUNTY_REWARD -> "🎁"
    }

    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(note.updatedAt))

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = sourceColor.copy(alpha = glowAlpha),
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    size = Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                    topLeft = Offset(-2.dp.toPx(), -2.dp.toPx())
                )
            },
        shape = RoundedCornerShape(20.dp),
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
                modifier = Modifier.padding(20.dp)
            ) {
            // 标题和来源
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = sourceColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = sourceIcon, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = sourceColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 内容预览
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodySmall,
                color = EarthBrownLight,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            // 标签
            if (note.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    note.tags.take(3).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BeigeSurface
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = EarthBrownLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
 * 空状态
 */
@Composable
private fun EmptyNotesState(onCreateNote: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Box(
                modifier = Modifier
                    .graphicsLayer { translationY = floatOffset }
                    .size(88.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MintGreen.copy(alpha = 0.2f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_icon),
                            contentDescription = "笔记",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "还没有笔记",
                style = MaterialTheme.typography.titleMedium,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "创建你的第一篇学习笔记\n或让AI帮你生成",
                style = MaterialTheme.typography.bodySmall,
                color = EarthBrownLight,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                onClick = onCreateNote,
                shape = RoundedCornerShape(12.dp),
                color = MintGreen
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "✏️", color = Color.White, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "创建笔记",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        }
    }
}

/**
 * 笔记编辑对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteEditDialog(
    note: Note?,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var previewMode by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CreamWhite
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (note == null) "新建笔记" else "编辑笔记",
                    style = MaterialTheme.typography.headlineSmall,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold
                )

                // 预览/编辑切换
                TextButton(onClick = { previewMode = !previewMode }) {
                    Text(
                        text = if (previewMode) "编辑" else "预览",
                        style = MaterialTheme.typography.labelMedium,
                        color = MintGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("标题", color = EarthBrownLight) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MintGreen,
                    unfocusedBorderColor = WarmGray
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (previewMode) {
                // 预览模式 - 显示渲染后的Markdown
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = BeigeSurface.copy(alpha = 0.5f)
                ) {
                    if (content.isBlank()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无内容",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EarthBrownLight
                            )
                        }
                    } else {
                        MarkdownRenderer(
                            markdown = content,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                // 编辑模式 - 使用富文本编辑器
                RichTextEditor(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    placeholder = "开始输入笔记内容...",
                    minLines = 10,
                    maxLines = 15
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("取消", color = EarthBrownLight)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    onClick = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            onSave(title, content)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = MintGreen,
                    enabled = title.isNotBlank() && content.isNotBlank()
                ) {
                    Text(
                        text = "保存",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
}