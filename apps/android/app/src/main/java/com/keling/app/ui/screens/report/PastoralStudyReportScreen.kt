package com.keling.app.ui.screens.report

/**
 * =========================
 * 田园治愈风学习报告页面
 * =========================
 *
 * 特点：
 * - AI生成学习洞察
 * - 数据可视化
 * - 学习建议
 */

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keling.app.R
import com.keling.app.data.StudyReport
import com.keling.app.ui.theme.*
import com.keling.app.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PastoralStudyReportScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val statistics = viewModel.getStatisticsSummary()
    val latestReport = viewModel.latestReport.value

    // 生成报告
    LaunchedEffect(Unit) {
        viewModel.generateStudyReport()
    }

    val report = viewModel.latestReport.value

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 背景图片
        Image(
            painter = painterResource(id = R.drawable.bg_page),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            // 顶部导航
            item {
                ReportHeader(onBack = onBack)
            }

            // 报告概览
            item {
                ReportOverviewCard(report = report, statistics = statistics)
            }

            // AI洞察
            item {
                AIInsightCard(insight = report?.aiInsight ?: "正在分析您的学习数据...")
            }

            // 学习数据详情
            item {
                StudyDataSection(report = report)
            }

            // 强弱点分析
            if (report != null) {
                item {
                    AnalysisSection(
                        title = "优势领域 ✨",
                        items = report.strongPoints,
                        color = MintGreen
                    )
                }

                item {
                    AnalysisSection(
                        title = "待提升领域 📈",
                        items = report.weakPoints,
                        color = WarmSunOrange
                    )
                }

                item {
                    AnalysisSection(
                        title = "AI建议 💡",
                        items = report.suggestions,
                        color = SkyBlue
                    )
                }
            }

            // 底部留白
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ==================== 组件 ====================

/**
 * 页面头部
 */
@Composable
private fun ReportHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回按钮
        Surface(
            onClick = onBack,
            shape = CircleShape,
            color = BeigeSurface,
            shadowElevation = 0.dp
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_left),
                    contentDescription = "返回",
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        // 标题
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "学习报告",
                style = MaterialTheme.typography.titleLarge,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "本周学习分析",
                style = MaterialTheme.typography.labelSmall,
                color = MintGreen
            )
        }

        // 占位
        Spacer(modifier = Modifier.width(52.dp))
    }
}

/**
 * 报告概览卡片
 */
@Composable
private fun ReportOverviewCard(
    report: StudyReport?,
    statistics: com.keling.app.viewmodel.StatisticsSummary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "overview")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = SkyBlue.copy(alpha = glowAlpha),
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    size = Size(size.width + 6.dp.toPx(), size.height + 6.dp.toPx()),
                    topLeft = Offset(-3.dp.toPx(), -3.dp.toPx())
                )
            },
        shape = RoundedCornerShape(24.dp),
        color = CreamWhite.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Box {
            Image(
                painter = painterResource(id = R.drawable.bg_card_module),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            // 报告图标
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(20.dp)
                        .graphicsLayer { alpha = glowAlpha }
                        .background(SkyBlue, CircleShape)
                )

                Surface(
                    modifier = Modifier.size(88.dp),
                    shape = CircleShape,
                    color = SkyBlue.copy(alpha = 0.3f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_report),
                            contentDescription = "报告",
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "本周学习报告",
                style = MaterialTheme.typography.titleLarge,
                color = EarthBrown,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 日期范围
            if (report != null) {
                val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
                val dateRange = "${dateFormat.format(Date(report.startDate))} - ${dateFormat.format(Date(report.endDate))}"
                Text(
                    text = dateRange,
                    style = MaterialTheme.typography.bodySmall,
                    color = EarthBrownLight
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 核心数据
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DataItem(
                    icon = "📚",
                    value = "${statistics.weekStudyMinutes}",
                    unit = "分钟",
                    label = "本周学习",
                    color = WarmSunOrange
                )
                DataItem(
                    icon = "✅",
                    value = "${report?.completedTasks ?: 0}",
                    unit = "个",
                    label = "完成任务",
                    color = MintGreen
                )
                DataItem(
                    icon = "🔥",
                    value = "${statistics.streakDays}",
                    unit = "天",
                    label = "连续学习",
                    color = CreamYellow
                )
            }
        }
        }
    }
}

@Composable
private fun DataItem(
    icon: String,
    value: String,
    unit: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = color.copy(alpha = 0.15f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = icon, fontSize = 28.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelMedium,
                        color = color,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = EarthBrownLight
        )
    }
}

/**
 * AI洞察卡片
 */
@Composable
private fun AIInsightCard(insight: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = LavenderPurple.copy(alpha = glowAlpha),
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    size = Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                    topLeft = Offset(-2.dp.toPx(), -2.dp.toPx())
                )
            },
        shape = RoundedCornerShape(20.dp),
        color = CreamWhite.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Box {
            Image(
                painter = painterResource(id = R.drawable.bg_card_module),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.Top
            ) {
            // AI图标
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = LavenderPurple.copy(alpha = 0.2f)
            ) {
                Box(
                    modifier = Modifier.padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_icon),
                        contentDescription = "亮点",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI洞察",
                    style = MaterialTheme.typography.titleMedium,
                    color = LavenderPurple,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = insight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EarthBrown,
                    lineHeight = 24.sp
                )
            }
        }
        }
    }
}

/**
 * 学习数据详情
 */
@Composable
private fun StudyDataSection(report: StudyReport?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CreamWhite.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Box {
            Image(
                painter = painterResource(id = R.drawable.bg_card_module),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "学习数据详情",
                    style = MaterialTheme.typography.titleMedium,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 数据条目
                DataRow(
                    label = "本周学习时长",
                    value = "${report?.totalStudyMinutes ?: 0} 分钟"
                )
                DataRow(
                    label = "完成任务数",
                    value = "${report?.completedTasks ?: 0} 个"
                )
                DataRow(
                    label = "学习课程数",
                    value = "${report?.coursesStudied ?: 0} 门"
                )
                DataRow(
                    label = "平均掌握度",
                    value = "${((report?.averageMastery ?: 0f) * 100).toInt()}%"
                )
            }
        }
    }
}

@Composable
private fun DataRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = EarthBrownLight
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = EarthBrown,
            fontWeight = FontWeight.Bold
        )
    }
    HorizontalDivider(color = BeigeSurface, thickness = 1.dp)
}

/**
 * 分析区块
 */
@Composable
private fun AnalysisSection(
    title: String,
    items: List<String>,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CreamWhite.copy(alpha = 0.8f),
        shadowElevation = 0.dp
    ) {
        Box {
            Image(
                painter = painterResource(id = R.drawable.bg_card_module),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (items.isEmpty()) {
                    Text(
                        text = "暂无数据",
                        style = MaterialTheme.typography.bodySmall,
                        color = EarthBrownLight
                    )
                } else {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = color.copy(alpha = 0.2f),
                                modifier = Modifier.size(8.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium,
                                color = EarthBrown
                            )
                        }
                    }
                }
            }
        }
    }
}

