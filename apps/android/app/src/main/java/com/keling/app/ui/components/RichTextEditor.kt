package com.keling.app.ui.components

/**
 * =========================
 * 富文本编辑器组件
 * =========================
 *
 * 支持基本格式的文本编辑器
 */

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
    val headingLevel: Int = 0 // 0=普通, 1=H1, 2=H2, 3=H3
)

/**
 * 富文本编辑器
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
    var state by remember { mutableStateOf(RichTextState()) }
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }

    // 同步外部值
    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = TextFieldValue(value)
        }
    }

    Column(
        modifier = modifier
    ) {
        // 工具栏
        RichTextToolbar(
            state = state,
            onBoldClick = {
                state = state.copy(isBold = !state.isBold)
            },
            onItalicClick = {
                state = state.copy(isItalic = !state.isItalic)
            },
            onUnderlineClick = {
                state = state.copy(isUnderline = !state.isUnderline)
            },
            onStrikethroughClick = {
                state = state.copy(isStrikethrough = !state.isStrikethrough)
            },
            onHeadingClick = { level ->
                state = state.copy(headingLevel = if (state.headingLevel == level) 0 else level)
            },
            onClearFormat = {
                state = RichTextState(text = state.text)
            }
        )

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
            textStyle = LocalTextStyle.current.copy(
                color = EarthBrown,
                fontSize = when (state.headingLevel) {
                    1 -> 24.sp
                    2 -> 20.sp
                    3 -> 18.sp
                    else -> 16.sp
                },
                fontWeight = if (state.isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (state.isItalic) FontStyle.Italic else FontStyle.Normal
            )
        )
    }
}

/**
 * 富文本工具栏
 */
@Composable
private fun RichTextToolbar(
    state: RichTextState,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    onStrikethroughClick: () -> Unit,
    onHeadingClick: (Int) -> Unit,
    onClearFormat: () -> Unit
) {
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

            Divider(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp),
                color = WarmGray.copy(alpha = 0.5f)
            )

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

            Divider(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp),
                color = WarmGray.copy(alpha = 0.5f)
            )

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
 * Markdown渲染器（简化版）
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
 * 解析Markdown（简化版）
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
                    // 处理行内格式
                    var processedLine = line
                    // 加粗
                    processedLine = processedLine.replace(Regex("\\*\\*(.+?)\\*\\*")) { match ->
                        "BOLD_START${match.groupValues[1]}BOLD_END"
                    }
                    // 斜体
                    processedLine = processedLine.replace(Regex("\\*(.+?)\\*")) { match ->
                        "ITALIC_START${match.groupValues[1]}ITALIC_END"
                    }

                    // 简化处理，直接添加文本
                    append(processedLine)
                    append("\n")
                }
            }
        }
    }
}