/**
 * Navigation.kt
 * 导航组件 - 顶部栏、底部栏等
 */

package com.keling.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keling.app.ui.theme.*

/**
 * 顶部导航栏
 */
@Composable
fun KeLingTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回按钮
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = EarthBrown
            )
        }

        // 标题区域
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = EarthBrown,
                textAlign = TextAlign.Center
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = EarthBrownLight
                )
            }
        }

        // 操作按钮
        if (action != null) {
            action()
        } else {
            Spacer(modifier = Modifier.width(44.dp))
        }
    }
}

/**
 * 页面标题组件
 * 用于页面顶部的大标题
 */
@Composable
fun PageTitle(
    title: String,
    modifier: Modifier = Modifier,
    icon: String? = null,
    subtitle: String? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Text(icon, fontSize = 36.sp)
            Spacer(modifier = Modifier.height(Spacing.sm))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = EarthBrown,
            textAlign = TextAlign.Center
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = EarthBrownLight,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 底部操作栏
 */
@Composable
fun BottomActionBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DawnWhite,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            content = content
        )
    }
}

/**
 * 分段控制器
 */
@Composable
fun SegmentedControl(
    segments: List<String>,
    selectedSegment: Int,
    onSegmentSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.md))
            .background(WarmSand.copy(alpha = 0.3f))
            .padding(4.dp)
    ) {
        segments.forEachIndexed { index, segment ->
            val isSelected = selectedSegment == index

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(
                        if (isSelected) DawnWhite else Color.Transparent
                    )
                    .clickable { onSegmentSelected(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = segment,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) EarthBrown else EarthBrownLight
                )
            }
        }
    }
}

/**
 * 搜索栏组件
 */
@Composable
fun SearchBar(
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearch: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = EarthBrownLight
            )
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.md),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = StellarOrange,
            unfocusedBorderColor = BorderGray,
            focusedContainerColor = DawnWhite,
            unfocusedContainerColor = DawnWhite
        ),
        singleLine = true
    )
}

/**
 * 悬浮操作按钮
 */
@Composable
fun FloatingActionButton(
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = StellarOrange
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    val glowAlpha by rememberGlowPulse(enabled = true, minAlpha = 0.2f, maxAlpha = 0.4f)

    Box(
        modifier = modifier
            .size(56.dp)
            .scale(scale)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .clickable {
                isPressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        // 发光背景
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = glowAlpha }
                .background(backgroundColor, CircleShape)
        )

        // 主容器
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 24.sp)
        }
    }
}

/**
 * 标签页组件
 */
@Composable
fun KeLingTabRow(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.TabRow(
        selectedTabIndex = selectedTab,
        modifier = modifier,
        containerColor = DawnWhite,
        contentColor = EarthBrown
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = tab,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}