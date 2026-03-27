/**
 * Color.kt
 * 田园治愈风主题色彩系统
 * 温暖、自然、治愈、唯美
 * 配合星球形象的暖色调设计
 */

package com.keling.app.ui.theme

import androidx.compose.ui.graphics.Color

// ==================== 核心主题色：田园治愈 ====================

/**
 * 暖阳橙：主品牌色、温暖、活力
 * 与星球形象的橙色调相呼应
 */
val WarmSunOrange = Color(0xFFFFB07C)
val WarmSunOrangeDark = Color(0xFFE89B5F)
val WarmSunOrangeLight = Color(0xFFFFE4C9)
val WarmSunOrangeGlow = Color(0x33FFB07C)

/**
 * 蜜桃粉：柔和、可爱、治愈
 */
val PeachPink = Color(0xFFFFCDB2)
val PeachPinkDark = Color(0xFFE8B399)
val PeachPinkLight = Color(0xFFFFF0E6)

/**
 * 奶油黄：温暖、舒适、阳光
 */
val CreamYellow = Color(0xFFFFF3B0)
val CreamYellowDark = Color(0xFFE8D98F)
val CreamYellowLight = Color(0xFFFFFBEB)

/**
 * 薄荷绿：清新、成长、希望
 */
val MintGreen = Color(0xFFB8E0D2)
val MintGreenDark = Color(0xFF96C9B8)
val MintGreenLight = Color(0xFFE5F5F0)

/**
 * 薰衣草紫：梦幻、神秘、优雅
 */
val LavenderPurple = Color(0xFFD4A5FF)
val LavenderPurpleDark = Color(0xFFB888E8)
val LavenderPurpleLight = Color(0xFFF3E8FF)

/**
 * 天空蓝：宁静、清新、自由
 */
val SkyBlue = Color(0xFFA8D8EA)
val SkyBlueDark = Color(0xFF8BC5D8)
val SkyBlueLight = Color(0xFFE5F4F9)

/**
 * 玫瑰红：热情、爱、温暖
 */
val RoseRed = Color(0xFFE8A0BF)
val RoseRedDark = Color(0xFFD488A8)
val RoseRedLight = Color(0xFFFFF0F8)

// ==================== 背景与表面 ====================

/**
 * 奶油白背景 - 温暖舒适
 */
val CreamWhite = Color(0xFFFFFBF5)
val CreamWhiteWarm = Color(0xFFFFF8F0)
val CreamWhiteCool = Color(0xFFFFFCF8)

/**
 * 米色表面
 */
val BeigeSurface = Color(0xFFF5EDE3)
val BeigeSurfaceLight = Color(0xFFFAF6F0)
val BeigeSurfaceDark = Color(0xFFEDE4D8)

/**
 * 浅棕大地
 */
val EarthBrown = Color(0xFF8B7355)
val EarthBrownLight = Color(0xFFA69076)
val EarthBrownDark = Color(0xFF6B5B4A)

/**
 * 暖灰边框
 */
val WarmGray = Color(0xFFD4CCC4)
val WarmGrayDark = Color(0xFFB8AFA6)
val WarmGrayLight = Color(0xFFEBE6E0)

// ==================== 星球主题色（暖色系） ====================

object PlanetColors {
    /** 蜜桃星球 */
    val Peach = Color(0xFFFFCDB2)
    val PeachGlow = Color(0x33FFCDB2)

    /** 橙子星球 */
    val Orange = Color(0xFFFFB07C)
    val OrangeGlow = Color(0x33FFB07C)

    /** 柠檬星球 */
    val Lemon = Color(0xFFFFF3B0)
    val LemonGlow = Color(0x33FFF3B0)

    /** 薄荷星球 */
    val Mint = Color(0xFFB8E0D2)
    val MintGlow = Color(0x33B8E0D2)

    /** 天空星球 */
    val Sky = Color(0xFFA8D8EA)
    val SkyGlow = Color(0x33A8D8EA)

    /** 薰衣草星球 */
    val Lavender = Color(0xFFD4A5FF)
    val LavenderGlow = Color(0x33D4A5FF)

    /** 玫瑰星球 */
    val Rose = Color(0xFFE8A0BF)
    val RoseGlow = Color(0x33E8A0BF)

    /** 自定义星球色 - 暖色系 */
    val Custom = listOf(
        Color(0xFFFFCDB2), // 蜜桃
        Color(0xFFFFB07C), // 橙子
        Color(0xFFFFF3B0), // 柠檬
        Color(0xFFB8E0D2), // 薄荷
        Color(0xFFA8D8EA), // 天空
        Color(0xFFD4A5FF), // 薰衣草
        Color(0xFFE8A0BF), // 玫瑰
        Color(0xFFFFB8D0)  // 樱花
    )
}

// ==================== 田园渐变 ====================

object PastoralGradients {
    /** 日出暖阳 */
    val sunrise = listOf(
        Color(0xFFFFFBF5),
        Color(0xFFFFF8F0),
        Color(0xFFFFF3E0)
    )

    /** 晨曦花园 */
    val morningGarden = listOf(
        Color(0xFFE5F5F0),
        Color(0xFFFFFBF5),
        Color(0xFFFFF0E6)
    )

    /** 暖阳能量 */
    val warmEnergy = listOf(
        Color(0xFFFFB07C),
        Color(0xFFFFCDB2),
        Color(0xFFFFB07C)
    )

    /** 成长进度 */
    val growth = listOf(
        Color(0xFFB8E0D2),
        Color(0xFF96C9B8),
        Color(0xFF7BB3A3)
    )

    /** 成就金 */
    val achievement = listOf(
        Color(0xFFFFF3B0),
        Color(0xFFE8D98F),
        Color(0xFFD4C476)
    )

    /** 梦幻紫 */
    val dreamyPurple = listOf(
        Color.Transparent,
        Color(0x33D4A5FF),
        Color(0x66D4A5FF),
        Color(0x33D4A5FF),
        Color.Transparent
    )

    /** 星球光环 */
    val planetGlow = listOf(
        Color.Transparent,
        Color(0x22FFB07C),
        Color(0x44FFB07C),
        Color(0x22FFB07C),
        Color.Transparent
    )

    /** 能量条 - 兼容旧代码 */
    val energyBar = listOf(
        Color(0xFFFFB07C),
        Color(0xFFFFCDB2),
        Color(0xFFFFF3B0)
    )

    /** 星空背景 - 兼容旧代码 */
    val starfield = listOf(
        Color(0xFFFFFBF5),
        Color(0xFFFFF8F0),
        Color(0xFFFFF3E0)
    )
}

// ==================== 任务优先级颜色（暖色系） ====================

object PriorityColors {
    /** 紧急 (P5) - 玫瑰红 */
    val urgent = Color(0xFFE8A0BF)
    val urgentGlow = Color(0x33E8A0BF)

    /** 高优先 (P4) - 暖阳橙 */
    val high = Color(0xFFFFB07C)
    val highGlow = Color(0x33FFB07C)

    /** 中等 (P3) - 奶油黄 */
    val medium = Color(0xFFFFF3B0)
    val mediumGlow = Color(0x33FFF3B0)

    /** 低优先 (P2) - 薄荷绿 */
    val low = Color(0xFFB8E0D2)
    val lowGlow = Color(0x33B8E0D2)

    /** 可选 (P1) - 天空蓝 */
    val optional = Color(0xFFA8D8EA)
    val optionalGlow = Color(0x33A8D8EA)
}

// ==================== 植物生长阶段颜色 ====================

object GrowthColors {
    /** 种子期 */
    val seed = Color(0xFFD4B896)

    /** 萌芽期 */
    val sprout = Color(0xFFB8E0D2)

    /** 生长期 */
    val growing = Color(0xFF96C9B8)

    /** 开花期 */
    val flowering = Color(0xFFFFB8D0)

    /** 结果期 */
    val fruiting = Color(0xFFFFB07C)

    /** 繁茂期 */
    val lush = Color(0xFF7BB3A3)

    /** 干旱警告 */
    val drought = Color(0xFFE8B399)
}

// ==================== 辅助色 ====================

/** 苔藓绿 */
val MossGreen = Color(0xFFB8E0D2)
val MossGreenDark = Color(0xFF96C9B8)
val MossGreenLight = Color(0xFFE5F5F0)

/** 温暖沙色 */
val WarmSand = Color(0xFFD4B896)
val WarmSandLight = Color(0xFFE8D5B7)

/** 雾玫瑰 */
val MistRose = Color(0xFFFFB8D0)
val MistRoseDark = Color(0xFFE8A0BF)

/** 黄昏金 */
val DuskGold = Color(0xFFFFF3B0)
val DuskGoldLight = Color(0xFFFFFBE0)

/** 深海蓝 */
val DeepSeaBlue = Color(0xFFA8D8EA)
val DeepSeaBlueLight = Color(0xFFC5E6F0)

/** 边框灰 */
val BorderGray = Color(0xFFD4CCC4)
val BorderGrayDark = Color(0xFFB8AFA6)

/** 错误红 */
val ErrorRed = Color(0xFFE8A0BF)
val ErrorRedLight = Color(0xFFFFF0F8)

/** 深色主题色 */
val DarkBackground = Color(0xFF3D3530)
val DarkSurface = Color(0xFF4A4340)
val DarkSurfaceVariant = Color(0xFF57504A)
val DarkOutline = Color(0xFF6B6460)

/** 星星紫 */
val StarPurple = Color(0xFFD4A5FF)
val StarPurpleLight = Color(0xFFF3E8FF)

// ==================== 兼容旧颜色名称 ====================

// 保持与旧代码的兼容性
val StellarOrange = WarmSunOrange
val StellarOrangeDark = WarmSunOrangeDark
val StellarOrangeLight = WarmSunOrangeLight
val StellarOrangeGlow = WarmSunOrangeGlow

val NebulaPurple = LavenderPurple
val NebulaPurpleDark = LavenderPurpleDark
val NebulaPurpleLight = LavenderPurpleLight
val NebulaPurpleGlow = Color(0x33D4A5FF)

val EnergyBlue = SkyBlue
val EnergyBlueDark = SkyBlueDark
val EnergyBlueLight = SkyBlueLight
val EnergyBlueGlow = Color(0x33A8D8EA)

val LifeGreen = MintGreen
val LifeGreenDark = MintGreenDark
val LifeGreenLight = MintGreenLight
val LifeGreenGlow = Color(0x33B8E0D2)

val FlameRed = RoseRed
val FlameRedDark = RoseRedDark
val FlameRedLight = RoseRedLight
val FlameRedGlow = Color(0x33E8A0BF)

val StarGold = CreamYellow
val StarGoldDark = CreamYellowDark
val StarGoldLight = CreamYellowLight
val StarGoldGlow = Color(0x33FFF3B0)

val CrystalBlue = SkyBlue
val CrystalBlueDark = SkyBlueDark
val CrystalBlueLight = SkyBlueLight
val CrystalBlueGlow = Color(0x33A8D8EA)

val DeepSpace = Color(0xFF3D3530)
val DeepSpaceLight = Color(0xFF4A4340)
val DeepSpaceMedium = Color(0xFF57504A)

val CosmicSurface = BeigeSurface
val CosmicSurfaceLight = BeigeSurfaceLight
val CosmicSurfaceDark = BeigeSurfaceDark

val DawnWhite = CreamWhite
val DawnWhiteWarm = CreamWhiteWarm

// 兼容旧渐变
val GameGradients = PastoralGradients

// ==================== 透明度预设 ====================

object Alpha {
    const val full = 1.0f
    const val high = 0.9f
    const val medium = 0.7f
    const val low = 0.5f
    const val subtle = 0.3f
    const val faint = 0.15f
    const val barely = 0.08f
    const val glow = 0.4f
    const val glowLight = 0.2f
    const val glowStrong = 0.6f
}

// ==================== 游戏化特效色 ====================

/**
 * 发光效果颜色
 */
object GlowColors {
    val warmGlow = Color(0x40FFB07C)
    val peachGlow = Color(0x40FFCDB2)
    val mintGlow = Color(0x40B8E0D2)
    val lavenderGlow = Color(0x40D4A5FF)
    val goldGlow = Color(0x40FFF3B0)
    val roseGlow = Color(0x40E8A0BF)
}

/**
 * 游戏卡片渐变
 */
object CardGradients {
    val warmCard = listOf(
        Color(0xFFFFFBF5),
        Color(0xFFFFF5E8)
    )

    val peachCard = listOf(
        Color(0xFFFFF8F5),
        Color(0xFFFFF0E6)
    )

    val mintCard = listOf(
        Color(0xFFF5FFFB),
        Color(0xFFE8FFF5)
    )

    val lavenderCard = listOf(
        Color(0xFFFCF8FF),
        Color(0xFFF5EDFF)
    )
}

/**
 * 按钮渐变
 */
object ButtonGradients {
    val primary = listOf(
        Color(0xFFFFB07C),
        Color(0xFFE89B5F)
    )

    val secondary = listOf(
        Color(0xFFB8E0D2),
        Color(0xFF96C9B8)
    )

    val accent = listOf(
        Color(0xFFD4A5FF),
        Color(0xFFB888E8)
    )
}

/**
 * 边框渐变
 */
object BorderGradients {
    val warm = listOf(
        WarmSunOrange.copy(alpha = 0.4f),
        WarmSunOrange.copy(alpha = 0.1f)
    )

    val mint = listOf(
        MintGreen.copy(alpha = 0.4f),
        MintGreen.copy(alpha = 0.1f)
    )

    val lavender = listOf(
        LavenderPurple.copy(alpha = 0.4f),
        LavenderPurple.copy(alpha = 0.1f)
    )
}

/**
 * 装饰元素颜色
 */
object DecorationColors {
    val sparkle = Color(0xFFFFE4B5)
    val star = Color(0xFFFFD700)
    val heart = Color(0xFFFFB6C1)
    val leaf = Color(0xFF90EE90)
    val flower = Color(0xFFFFB7C5)
    val water = Color(0xFF87CEEB)
}