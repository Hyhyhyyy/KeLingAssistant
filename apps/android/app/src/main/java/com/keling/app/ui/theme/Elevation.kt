/**
 * Elevation.kt
 * 统一阴影层级系统 - 创造深度和层次感
 */

package com.keling.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * 阴影层级系统
 * 定义UI元素的浮起程度，创造视觉层次
 */
object Elevation {
    /** 无阴影 - 用于平铺元素 */
    val none = 0.dp

    /** 极轻阴影 - 用于轻微浮起的元素 */
    val xs = 1.dp

    /** 轻阴影 - 用于悬停状态、小卡片 */
    val sm = 2.dp

    /** 中等阴影 - 默认卡片阴影 */
    val md = 4.dp

    /** 明显阴影 - 用于强调元素、按钮 */
    val lg = 8.dp

    /** 重阴影 - 用于弹窗、浮动面板 */
    val xl = 12.dp

    /** 超重阴影 - 用于模态框、重要提示 */
    val xxl = 16.dp

    /** 极重阴影 - 用于需要极大突出的元素 */
    val xxxl = 24.dp
}

/**
 * 阴影动画配置
 * 用于交互时的阴影变化
 */
object ElevationAnimation {
    /** 按下时的阴影减少量 */
    val pressReduction = 2.dp

    /** 悬停时的阴影增加量 */
    val hoverIncrease = 2.dp

    /** 焦点时的阴影增加量 */
    val focusIncrease = 4.dp
}