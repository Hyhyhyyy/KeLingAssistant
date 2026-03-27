/**
 * Shapes.kt
 * 形状系统 - 定义应用的圆角形状
 */

package com.keling.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 形状配置
 * 用于Card、Button等组件的默认形状
 */
val AppShapes = Shapes(
    // 小型组件：Chip、小按钮
    small = RoundedCornerShape(8.dp),

    // 中型组件：Card、TextField
    medium = RoundedCornerShape(12.dp),

    // 大型组件：Modal、BottomSheet
    large = RoundedCornerShape(20.dp),

    // 超大型组件：NavigationDrawer
    extraLarge = RoundedCornerShape(28.dp)
)