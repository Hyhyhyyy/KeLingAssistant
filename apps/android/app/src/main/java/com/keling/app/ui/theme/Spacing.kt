/**
 * Spacing.kt
 * 统一间距系统 - 基于4dp网格
 * 确保整个应用的间距一致性和视觉节奏
 */

package com.keling.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * 间距系统
 * 基于4dp网格，便于维护和扩展
 */
object Spacing {
    /** 极小间距 - 用于紧密排列的元素内部 */
    val xs = 4.dp

    /** 小间距 - 用于图标与文字、小元素之间 */
    val sm = 8.dp

    /** 中等间距 - 默认间距，用于大部分场景 */
    val md = 16.dp

    /** 大间距 - 用于卡片之间、区块分隔 */
    val lg = 24.dp

    /** 超大间距 - 用于页面区块之间 */
    val xl = 32.dp

    /** 巨大间距 - 用于页面顶部/底部留白 */
    val xxl = 48.dp
}

/**
 * 内边距快捷访问
 */
object Padding {
    val cardHorizontal = 16.dp
    val cardVertical = 12.dp
    val screenHorizontal = 16.dp
    val screenVertical = 16.dp
    val buttonHorizontal = 24.dp
    val buttonVertical = 12.dp
    val listItemHorizontal = 16.dp
    val listItemVertical = 12.dp
}