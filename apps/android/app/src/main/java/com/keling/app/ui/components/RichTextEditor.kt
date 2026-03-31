package com.keling.app.ui.components

/**
 * =========================
 * 增强版富文本编辑器组件
 * =========================
 *
 * 支持：
 * - 标题级别 (H1, H2, H3)
 * - 加粗、斜体、下划线、删除线
 * - 文字大小调节
 * - 高亮背景颜色
 * - 文字颜色
 * - 笔记分类标签
 */

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keling.app.ui.theme.*

/**
 * 富文本编辑器状态
 */
data class RichTextState(
    val text: TextFieldValue = TextFieldValue(""),
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val headingLevel: Int = 0, // 0=普通, 1=H1, 2=H2, 3=H3
    val fontSize: Int = 16, // 字体大小
    val textColor: Color = EarthBrown, // 文字颜色
    val highlightColor: Color? = null // 高亮背景颜色
)

/**
 * 笔记分类
 */
data class NoteCategory(
    val id: String,
    val name: String,
    val color: Color
)

// 预定义的分类
val DefaultCategories = listOf(
    NoteCategory("important", "重要", RoseRed),
    NoteCategory("review", "待复习", WarmSunOrange),
    NoteCategory("idea", "想法", MintGreen),
    NoteCategory("todo", "待办", SkyBlue),
    NoteCategory("reference", "参考", LavenderPurple)
)

// 高亮颜色选项
val HighlightColors = listOf(
    null, // 无高亮
    Color(0xFFFFFF00).copy(alpha = 0.3f), // 黄色
    Color(0xFF00FF00).copy(alpha = 0.2f), // 绿色
    Color(0xFF00FFFF).copy(alpha = 0.2f), // 青色
    Color(0xFFFF00FF).copy(alpha = 0.2f), // 粉色
    Color(0xFFFFA500).copy(alpha = 0.2f), // 橙色
    Color(0xFF87CEEB).copy(alpha = 0.2f)  // 天蓝
)

// 文字颜色选项
val TextColors = listOf(
    EarthBrown,
    RoseRed,
    WarmSunOrange,
    MintGreen,
    SkyBlue,
    LavenderPurple
)

// 字体大小选项
val FontSizes = listOf(12, 14, 16, 18, 20, 24, 28, 32)

/**
 * 增强版富文本编辑器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedRichTextEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "开始输入...",
    minLines: Int = 10,
    maxLines: Int = 20,
    categories: List<NoteCategory> = DefaultCategories,
    selectedCategory: String? = null,
    onCategoryChange: (String?) -> Unit = {}
) {
    var state by remember { mutableStateOf(RichTextState()) }
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showHighlightPicker by remember { mutableStateOf(false) }
    var showSizePicker by remember { mutableStateOf(false) }

    // 同步外部值
    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = TextFieldValue(value)
        }
    }

    Column(
        modifier = modifier
    ) {
        // 增强版工具栏
        EnhancedRichTextToolbar(
            state = state,
            onBoldClick = { state = state.copy(isBold = !state.isBold) },
            onItalicClick = { state = state.copy(isItalic = !state.isItalic) },
            onUnderlineClick = { state = state.copy(isUnderline = !state.isUnderline) },
            onStrikethroughClick = { state = state.copy(isStrikethrough = !state.isStrikethrough) },
            onHeadingClick = { level -> state = state.copy(headingLevel = if (state.headingLevel == level) 0 else level) },
            onColorClick = { showColorPicker = true },
            onHighlightClick = { showHighlightPicker = true },
            onSizeClick = { showSizePicker = true },
            onClearFormat = { state = RichTextState(text = state.text) }
        )

        // 颜色选择器
        if (showColorPicker) {
            ColorPickerDialog(
                colors = TextColors,
                selectedColor = state.textColor,
                onColorSelected = { color ->
                    state = state.copy(textColor = color)
                    showColorPicker = false
                },
                onDismiss = { showColorPicker = false }
            )
        }

        // 高亮颜色选择器
        if (showHighlightPicker) {
            HighlightPickerDialog(
                colors = HighlightColors,
                selectedColor = state.highlightColor,
                onColorSelected = { color ->
                    state = state.copy(highlightColor = color)
                    showHighlightPicker = false
                },
                onDismiss = { showHighlightPicker = false }
            )
        }

        // 字体大小选择器
        if (showSizePicker) {
            FontSizePickerDialog(
                sizes = FontSizes,
                selectedSize = state.fontSize,
                onSizeSelected = { size ->
                    state = state.copy(fontSize = size)
                    showSizePicker = false
                },
                onDismiss = { showSizePicker = false }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 编辑区域
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                onValueChange(newValue.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = (minLines * 24).dp, max = (maxLines * 24).dp),
            placeholder = {
                Text(
                    text = placeholder,
                    color = EarthBrownLight.copy(alpha = 0.5f)
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MintGreen,
                unfocusedBorderColor = WarmGray,
                focusedContainerColor = DawnWhite,
                unfocusedContainerColor = DawnWhite
            ),
            textStyle = TextStyle(
                color = state.textColor,
                fontSize = state.fontSize.sp,
                fontWeight = if (state.isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (state.isItalic) FontStyle.Italic else FontStyle.Normal,
                textDecoration = when {
                    state.isUnderline && state.isStrikethrough -> TextDecoration.combine(
                        listOf(TextDecoration.Underline, TextDecoration.LineThrough)
                    )
                    state.isUnderline -> TextDecoration.Underline
                    state.isStrikethrough -> TextDecoration.LineThrough
                    else -> TextDecoration.None
                },
                background = state.highlightColor ?: Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 分类选择
        if (categories.isNotEmpty()) {
            CategorySelector(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategoryChange = onCategoryChange
            )
        }
    }
}

/**
 * 增强版富文本工具栏
 */
@Composable
private fun EnhancedRichTextToolbar(
    state: RichTextState,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    onStrikethroughClick: () -> Unit,
    onHeadingClick: (Int) -> Unit,
    onColorClick: () -> Unit,
    onHighlightClick: () -> Unit,
    onSizeClick: () -> Unit,
    onClearFormat: () -> Unit
) {
    Column {
        // 第一行：标题和格式
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = BeigeSurface.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 标题按钮
                HeadingButton(
                    level = 1,
                    isSelected = state.headingLevel == 1,
                    onClick = { onHeadingClick(1) }
                )
                HeadingButton(
                    level = 2,
                    isSelected = state.headingLevel == 2,
                    onClick = { onHeadingClick(2) }
                )
                HeadingButton(
                    level = 3,
                    isSelected = state.headingLevel == 3,
                    onClick = { onHeadingClick(3) }
                )

                VerticalDivider()

                // 格式按钮
                FormatButton(
                    icon = "B",
                    isSelected = state.isBold,
                    onClick = onBoldClick
                )
                FormatButton(
                    icon = "I",
                    isSelected = state.isItalic,
                    onClick = onItalicClick,
                    isItalic = true
                )
                FormatButton(
                    icon = "U",
                    isSelected = state.isUnderline,
                    onClick = onUnderlineClick,
                    isUnderline = true
                )
                FormatButton(
                    icon = "S",
                    isSelected = state.isStrikethrough,
                    onClick = onStrikethroughClick,
                    isStrikethrough = true
                )

                VerticalDivider()

                // 颜色和高亮
                ColorButton(
                    color = state.textColor,
                    onClick = onColorClick,
                    label = "颜色"
                )
                HighlightButton(
                    color = state.highlightColor,
                    onClick = onHighlightClick
                )

                VerticalDivider()

                // 字体大小
                FontSizeButton(
                    size = state.fontSize,
                    onClick = onSizeClick
                )

                VerticalDivider()

                // 清除格式
                Surface(
                    onClick = onClearFormat,
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Transparent
                ) {
                    Text(
                        text = "清除",
                        style = MaterialTheme.typography.labelSmall,
                        color = EarthBrownLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(WarmGray.copy(alpha = 0.5f))
    )
}

/**
 * 标题按钮
 */
@Composable
private fun HeadingButton(
    level: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = if (isSelected) MintGreen.copy(alpha = 0.2f) else Color.Transparent
    ) {
        Text(
            text = "H$level",
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) MintGreen else EarthBrown,
            fontWeight = FontWeight.Bold,
            fontSize = (14 + level * 2).sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * 格式按钮
 */
@Composable
private fun FormatButton(
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isItalic: Boolean = false,
    isUnderline: Boolean = false,
    isStrikethrough: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = if (isSelected) MintGreen.copy(alpha = 0.2f) else Color.Transparent
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) MintGreen else EarthBrown,
            fontWeight = FontWeight.Bold,
            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
            textDecoration = when {
                isUnderline -> TextDecoration.Underline
                isStrikethrough -> TextDecoration.LineThrough
                else -> TextDecoration.None
            },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/**
 * 颜色按钮
 */
@Composable
private fun ColorButton(
    color: Color,
    onClick: () -> Unit,
    label: String = ""
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = Color.Transparent
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color, CircleShape)
                    .border(1.dp, EarthBrownLight.copy(alpha = 0.3f), CircleShape)
            )
            if (label.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = EarthBrown
                )
            }
        }
    }
}

/**
 * 高亮按钮
 */
@Composable
private fun HighlightButton(
    color: Color?,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = Color.Transparent
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color ?: Color.Transparent, RoundedCornerShape(2.dp))
                    .border(1.dp, EarthBrownLight.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "高亮",
                style = MaterialTheme.typography.labelSmall,
                color = EarthBrown
            )
        }
    }
}

/**
 * 字体大小按钮
 */
@Composable
private fun FontSizeButton(
    size: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = Color.Transparent
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${size}sp",
                style = MaterialTheme.typography.labelSmall,
                color = EarthBrown,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * 颜色选择器对话框
 */
@Composable
private fun ColorPickerDialog(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = CreamWhite,
        title = {
            Text(
                text = "选择文字颜色",
                style = MaterialTheme.typography.titleMedium,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(color, CircleShape)
                            .border(
                                width = if (color == selectedColor) 3.dp else 0.dp,
                                color = if (color == selectedColor) MintGreen else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(color) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成", color = MintGreen)
            }
        }
    )
}

/**
 * 高亮颜色选择器对话框
 */
@Composable
private fun HighlightPickerDialog(
    colors: List<Color?>,
    selectedColor: Color?,
    onColorSelected: (Color?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = CreamWhite,
        title = {
            Text(
                text = "选择高亮颜色",
                style = MaterialTheme.typography.titleMedium,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(color ?: Color.White, RoundedCornerShape(4.dp))
                            .border(
                                width = if (color == selectedColor) 3.dp else 1.dp,
                                color = if (color == selectedColor) MintGreen else EarthBrownLight.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { onColorSelected(color) }
                    ) {
                        if (color == null) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("无", style = MaterialTheme.typography.labelSmall, color = EarthBrownLight)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成", color = MintGreen)
            }
        }
    )
}

/**
 * 字体大小选择器对话框
 */
@Composable
private fun FontSizePickerDialog(
    sizes: List<Int>,
    selectedSize: Int,
    onSizeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = CreamWhite,
        title = {
            Text(
                text = "选择字体大小",
                style = MaterialTheme.typography.titleMedium,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sizes.chunked(4).forEach { rowSizes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowSizes.forEach { size ->
                            Surface(
                                onClick = { onSizeSelected(size) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (size == selectedSize) MintGreen.copy(alpha = 0.2f) else BeigeSurface
                            ) {
                                Text(
                                    text = "${size}sp",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (size == selectedSize) MintGreen else EarthBrown,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成", color = MintGreen)
            }
        }
    )
}

/**
 * 分类选择器
 */
@Composable
fun CategorySelector(
    categories: List<NoteCategory>,
    selectedCategory: String?,
    onCategoryChange: (String?) -> Unit
) {
    Column {
        Text(
            text = "笔记分类",
            style = MaterialTheme.typography.labelMedium,
            color = EarthBrown,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "无分类"选项
            Surface(
                onClick = { onCategoryChange(null) },
                shape = RoundedCornerShape(16.dp),
                color = if (selectedCategory == null) BeigeSurface else BeigeSurface.copy(alpha = 0.5f),
                border = if (selectedCategory == null) {
                    androidx.compose.foundation.BorderStroke(1.dp, EarthBrown)
                } else null
            ) {
                Text(
                    text = "无分类",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedCategory == null) EarthBrown else EarthBrownLight,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            categories.forEach { category ->
                Surface(
                    onClick = { onCategoryChange(category.id) },
                    shape = RoundedCornerShape(16.dp),
                    color = category.color.copy(alpha = if (selectedCategory == category.id) 0.3f else 0.1f),
                    border = if (selectedCategory == category.id) {
                        androidx.compose.foundation.BorderStroke(1.dp, category.color)
                    } else null
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(category.color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedCategory == category.id) category.color else EarthBrown
                        )
                    }
                }
            }
        }
    }
}

/**
 * 原有 RichTextEditor 的兼容包装器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RichTextEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "开始输入...",
    minLines: Int = 10,
    maxLines: Int = 20
) {
    // 使用简化版本的编辑器，保持向后兼容
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    var headingLevel by remember { mutableStateOf(0) }
    var isBold by remember { mutableStateOf(false) }
    var isItalic by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = TextFieldValue(value)
        }
    }

    Column(modifier = modifier) {
        // 简化工具栏
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = BeigeSurface.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(1, 2, 3).forEach { level ->
                    Surface(
                        onClick = { headingLevel = if (headingLevel == level) 0 else level },
                        shape = RoundedCornerShape(4.dp),
                        color = if (headingLevel == level) MintGreen.copy(alpha = 0.2f) else Color.Transparent
                    ) {
                        Text(
                            text = "H$level",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (headingLevel == level) MintGreen else EarthBrown,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(WarmGray.copy(alpha = 0.5f))
                )

                Surface(
                    onClick = { isBold = !isBold },
                    shape = RoundedCornerShape(4.dp),
                    color = if (isBold) MintGreen.copy(alpha = 0.2f) else Color.Transparent
                ) {
                    Text(
                        text = "B",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isBold) MintGreen else EarthBrown,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    onClick = { isItalic = !isItalic },
                    shape = RoundedCornerShape(4.dp),
                    color = if (isItalic) MintGreen.copy(alpha = 0.2f) else Color.Transparent
                ) {
                    Text(
                        text = "I",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isItalic) MintGreen else EarthBrown,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                onValueChange(newValue.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = (minLines * 24).dp, max = (maxLines * 24).dp),
            placeholder = {
                Text(
                    text = placeholder,
                    color = EarthBrownLight.copy(alpha = 0.5f)
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MintGreen,
                unfocusedBorderColor = WarmGray,
                focusedContainerColor = DawnWhite,
                unfocusedContainerColor = DawnWhite
            ),
            textStyle = TextStyle(
                color = EarthBrown,
                fontSize = when (headingLevel) {
                    1 -> 24.sp
                    2 -> 20.sp
                    3 -> 18.sp
                    else -> 16.sp
                },
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal
            )
        )
    }
}

/**
 * Markdown渲染器
 */
@Composable
fun MarkdownRenderer(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val annotatedString = remember(markdown) { parseMarkdown(markdown) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium,
            color = EarthBrown
        )
    }
}

/**
 * 解析Markdown
 */
private fun parseMarkdown(markdown: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = markdown.lines()

        for (line in lines) {
            when {
                line.startsWith("### ") -> {
                    withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("### "))
                    }
                    append("\n")
                }
                line.startsWith("## ") -> {
                    withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("## "))
                    }
                    append("\n")
                }
                line.startsWith("# ") -> {
                    withStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("# "))
                    }
                    append("\n")
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    append("• ")
                    append(line.removePrefix("- ").removePrefix("* "))
                    append("\n")
                }
                line.matches(Regex("^\\d+\\.\\s")) -> {
                    append(line)
                    append("\n")
                }
                else -> {
                    append(line)
                    append("\n")
                }
            }
        }
    }
}