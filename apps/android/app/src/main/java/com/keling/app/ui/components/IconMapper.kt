package com.keling.app.ui.components

/**
 * =========================
 * 图标映射工具
 * =========================
 *
 * 将图标标识符映射到drawable资源
 */

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import com.keling.app.R

/**
 * 图标类型枚举
 */
enum class IconType(val iconName: String) {
    // 任务类型
    DAILY_CARE("daily_care"),
    DEEP_EXPLORATION("deep_exploration"),
    REVIEW_RITUAL("review_ritual"),
    BOUNTY("bounty"),
    RESCUE("rescue"),

    // 通用图标
    TASK("task"),
    COURSE("course"),
    NOTE("note"),
    AI("ai"),

    // 状态图标
    CHECK("check"),
    ACHIEVEMENT("achievement"),
    SETTINGS("settings"),
    REPORT("report"),

    // 其他
    ENERGY("energy"),
    CRYSTAL("crystal"),
    EXP("exp"),
    STREAK("streak")
}

/**
 * 根据图标名称获取drawable资源ID
 */
fun getIconResource(iconName: String): Int {
    return when (iconName) {
        // 任务类型
        "daily_care", "🌱" -> R.drawable.ic_icon
        "deep_exploration", "🔬" -> R.drawable.ic_icon
        "review_ritual", "🔄" -> R.drawable.ic_icon
        "bounty", "🎯" -> R.drawable.ic_icon
        "rescue", "🆘" -> R.drawable.ic_icon

        // 通用图标
        "task", "📋" -> R.drawable.ic_icon
        "course", "🌍" -> R.drawable.ic_icon
        "note", "📝" -> R.drawable.ic_icon
        "ai", "🧠" -> R.drawable.ic_icon

        // 状态图标
        "check", "✓", "✅" -> R.drawable.ic_check
        "achievement", "🏆" -> R.drawable.ic_achievement
        "settings", "⚙️" -> R.drawable.ic_settings
        "report", "📊" -> R.drawable.ic_report

        // 资源图标
        "energy", "⚡" -> R.drawable.ic_icon
        "crystal", "💎", "✿" -> R.drawable.ic_icon
        "exp", "⭐" -> R.drawable.ic_icon
        "streak", "🔥" -> R.drawable.ic_icon

        // 其他常见emoji
        "star", "🌟", "💫" -> R.drawable.ic_icon
        "book", "📚" -> R.drawable.ic_icon
        "idea", "💡" -> R.drawable.ic_icon
        "party", "🎉" -> R.drawable.ic_icon
        "coffee", "☕" -> R.drawable.ic_icon
        "heart", "❤️" -> R.drawable.ic_icon
        "leaf", "🌿" -> R.drawable.ic_icon
        "art", "🎨" -> R.drawable.ic_icon
        "rocket", "🚀" -> R.drawable.ic_icon
        "magic", "🔮" -> R.drawable.ic_icon
        "play", "▶" -> R.drawable.ic_icon
        "pause", "⏸" -> R.drawable.ic_icon
        "reset", "↺" -> R.drawable.ic_icon
        "clock", "⏰" -> R.drawable.ic_icon
        "calendar", "📅" -> R.drawable.ic_icon
        "home", "🏠" -> R.drawable.ic_icon
        "tool", "🔧" -> R.drawable.ic_icon

        // 默认
        else -> R.drawable.ic_icon
    }
}

/**
 * 图标组件
 */
@Composable
fun IconImage(
    iconName: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier.size(24.dp)
) {
    Image(
        painter = painterResource(id = getIconResource(iconName)),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}