/**
 * Radius.kt
 * 统一圆角系统 - 创造有机、流动的视觉体验
 */

package com.keling.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * 圆角系统
 * 从锐利到柔和的完整圆角体系
 */
object Radius {
    /** 无圆角 - 用于需要锐利边缘的场景 */
    val none = 0.dp

    /** 极小圆角 - 用于标签、小按钮 */
    val xs = 4.dp

    /** 小圆角 - 用于按钮、输入框、小卡片 */
    val sm = 8.dp

    /** 中等圆角 - 用于常规卡片、弹窗元素 */
    val md = 12.dp

    /** 大圆角 - 用于主要卡片、底部抽屉 */
    val lg = 16.dp

    /** 超大圆角 - 用于大型卡片、模态框 */
    val xl = 20.dp

    /** 巨大圆角 - 用于特殊强调元素 */
    val xxl = 28.dp

    /** 全圆角 - 用于头像、徽章、圆形按钮 */
    val full = 999.dp
}

/**
 * 预设的不规则圆角形状
 * 用于创造有机、有个性的UI元素
 */
object OrganicRadius {
    /** 晶体形状 - 左上和右下圆角更大 */
    val crystal = androidx.compose.foundation.shape.RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 8.dp,
        bottomStart = 8.dp,
        bottomEnd = 24.dp
    )

    /** 气泡形状 - 左上圆角更大，类似对话气泡 */
    val bubble = androidx.compose.foundation.shape.RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp
    )

    /** 叶片形状 - 不对称圆角 */
    val leaf = androidx.compose.foundation.shape.RoundedCornerShape(
        topStart = 32.dp,
        topEnd = 8.dp,
        bottomStart = 8.dp,
        bottomEnd = 32.dp
    )

    /** 星球形状 - 大圆角，略带不规则感 */
    val planet = androidx.compose.foundation.shape.RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 22.dp,
        bottomStart = 20.dp,
        bottomEnd = 16.dp
    )
}