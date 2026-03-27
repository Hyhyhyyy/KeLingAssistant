/**
 * HexagonShape.kt
 * 六边形是课灵的核心视觉元素
 * 这个文件定义六边形的绘制和基础组件
 */

package com.keling.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.keling.app.ui.theme.KeLingTheme
import com.keling.app.ui.theme.StellarOrange
import com.keling.app.ui.theme.MossGreen
import com.keling.app.ui.theme.DawnWhite

/**
 * 创建六边形路径的函数
 *
 * 原理：六边形有6个顶点，均匀分布在圆周上
 * 角度计算：360° / 6 = 60°，每个顶点间隔60°
 * 从顶部开始（-90°），这样六边形是"立起来"的
 *
 * 参数：
 * - size: 六边形的外接矩形大小
 * - radius: 中心到顶点的距离（外接圆半径）
 */
fun createHexagonPath(size: androidx.compose.ui.geometry.Size): Path {
    return Path().apply {
        val centerX = size.width / 2
        val centerY = size.height / 2
        // 取宽高的较小值作为直径，确保六边形不会超出边界
        val radius = (size.width.coerceAtMost(size.height) / 2) * 0.95f // 留一点边距

        // 计算6个顶点的坐标
        val points = (0 until 6).map { i ->
            // 角度转弧度：角度 × π / 180
            // 从-90°开始（顶部），这样六边形是正的
            val angle = Math.toRadians((60 * i - 90).toDouble())
            val x = centerX + (radius * kotlin.math.cos(angle)).toFloat()
            val y = centerY + (radius * kotlin.math.sin(angle)).toFloat()
            x to y
        }

        // 绘制路径：移动到第一个点，然后连线到其他点
        moveTo(points[0].first, points[0].second)
        for (i in 1 until points.size) {
            lineTo(points[i].first, points[i].second)
        }
        // 闭合路径（回到第一个点）
        close()
    }
}

/**
 * 六边形形状定义
 * GenericShape是Compose提供的通用形状，可以用Path自定义
 */
val HexagonShape: Shape = GenericShape { size, _ ->
    // 将Path添加到Shape
    addPath(createHexagonPath(size))
}

/**
 * 六边形组件
 * 这是可复用的基础组件，所有六边形UI都基于它
 *
 * 参数说明：
 * - modifier: 修饰符，用于设置尺寸、位置等（必须传）
 * - size: 六边形大小，默认64dp
 * - backgroundColor: 背景色，默认使用主题主色
 * - borderColor: 边框色，默认透明（无边框）
 * - borderWidth: 边框宽度，默认0
 * - shadowElevation: 阴影高度，默认4dp（有立体感）
 * - onClick: 点击回调，为null时不可点击
 * - content: 六边形内部的内容，可以是文字、图标等
 */
@Composable
fun Hexagon(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 0.dp,
    shadowElevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            // 设置尺寸
            .size(size)
            // 添加阴影，elevation控制阴影高度
            .shadow(shadowElevation, HexagonShape)
            // 裁剪为六边形，超出部分不显示
            .clip(HexagonShape)
            // 设置背景色
            .background(backgroundColor)
            // 添加边框（如果有）
            .border(borderWidth, borderColor, HexagonShape)
            // 添加点击事件（如果提供了onClick）
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        // 内容居中显示
        contentAlignment = Alignment.Center,
        content = content
    )
}
/**
 * 简化的预览版本
 */
@Preview(showBackground = true)
@Composable
fun SimpleHexagonPreview() {
    KeLingTheme {
        Hexagon(
            backgroundColor = StellarOrange,
            content = {
                androidx.compose.material3.Text(
                    text = "A",
                    color = DawnWhite,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        )
    }
}