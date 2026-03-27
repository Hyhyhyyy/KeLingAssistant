package com.keling.app.ui.components

/**
 * =========================
 * 笔记模板组件
 * =========================
 *
 * 提供多种笔记模板：
 * - 康奈尔笔记法
 * - 思维导图
 * - 学习日志
 * - 错题记录
 */

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keling.app.R
import com.keling.app.ui.theme.*

/**
 * 笔记模板类型
 */
enum class NoteTemplateType(val displayName: String, val icon: String, val description: String) {
    CORNELL("康奈尔笔记", "📝", "经典的大学笔记法，包含线索栏、笔记栏、总结栏"),
    MIND_MAP("思维导图", "🧠", "以中心主题向外发散的结构化笔记"),
    STUDY_LOG("学习日志", "📅", "记录每日学习内容和心得"),
    ERROR_RECORD("错题记录", "❌", "记录错误题目、分析和正确解法"),
    MEETING("会议记录", "📋", "结构化的会议内容记录"),
    READING("读书笔记", "📖", "阅读书籍时的摘录和感悟")
}

/**
 * 笔记模板数据
 */
data class NoteTemplateData(
    val type: NoteTemplateType,
    val content: String
)

/**
 * 笔记模板选择对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteTemplateSelectionDialog(
    onDismiss: () -> Unit,
    onSelect: (NoteTemplateType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CreamWhite
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "选择笔记模板",
                    style = MaterialTheme.typography.headlineSmall,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(NoteTemplateType.entries) { template ->
                        TemplateItem(
                            template = template,
                            onClick = { onSelect(template) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("取消", color = EarthBrownLight)
                }
            }
        }
    }
}

/**
 * 模板项
 */
@Composable
private fun TemplateItem(
    template: NoteTemplateType,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = BeigeSurface.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MintGreen.copy(alpha = 0.2f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = template.icon,
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 文字
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = EarthBrown,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = EarthBrownLight
                )
            }
        }
    }
}

/**
 * 获取模板内容
 */
fun getTemplateContent(type: NoteTemplateType): String {
    return when (type) {
        NoteTemplateType.CORNELL -> """
# 笔记主题

## 线索栏（关键词/问题）
- 关键词1
- 关键词2
- 关键词3

## 笔记栏（主要内容）


## 总结栏

_用一句话概括本页内容_
""".trimIndent()

        NoteTemplateType.MIND_MAP -> """
# 中心主题

## 分支一
- 要点1.1
- 要点1.2
  - 子要点1.2.1

## 分支二
- 要点2.1
- 要点2.2

## 分支三
- 要点3.1
- 要点3.2

---
_提示：可以使用思维导图软件可视化此结构_
""".trimIndent()

        NoteTemplateType.STUDY_LOG -> """
# 学习日志 - ${java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())}

## 今日学习内容
-

## 学习心得
-

## 遇到的问题
-

## 明日计划
-

---
_学习时长：_ 分钟
""".trimIndent()

        NoteTemplateType.ERROR_RECORD -> """
# 错题记录

## 题目
（粘贴或输入题目）

## 我的答案
（错误的答案和解题过程）

## 正确答案
（正确答案）

## 错误原因分析
-

## 知识点总结
-

## 相似题型
-

---
_日期：_  |  _科目：_
""".trimIndent()

        NoteTemplateType.MEETING -> """
# 会议记录

## 会议信息
- 时间：
- 地点：
- 参与人：
- 主持人：

## 会议议题
1.

## 讨论内容
### 议题一


### 议题二


## 决议事项
- [ ]

## 下次会议
- 时间：
- 议题：
""".trimIndent()

        NoteTemplateType.READING -> """
# 读书笔记

## 书籍信息
- 书名：
- 作者：
- 阅读日期：

## 精彩摘录
>

## 我的感悟


## 关键观点
1.
2.
3.

## 行动计划
- [ ]
""".trimIndent()
    }
}

/**
 * 康奈尔笔记模板组件
 */
@Composable
fun CornellNoteTemplate(
    cues: String,
    notes: String,
    summary: String,
    onCuesChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSummaryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 400.dp)
    ) {
        // 左侧线索栏
        Column(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight()
                .background(BeigeSurface.copy(alpha = 0.3f))
                .padding(8.dp)
        ) {
            Text(
                text = "线索栏",
                style = MaterialTheme.typography.labelMedium,
                color = EarthBrownLight
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = cues,
                onValueChange = onCuesChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("关键词、问题...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MintGreen,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }

        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 1.dp,
            color = WarmGray.copy(alpha = 0.3f)
        )

        // 右侧笔记栏
        Column(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            Text(
                text = "笔记栏",
                style = MaterialTheme.typography.labelMedium,
                color = EarthBrownLight
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("主要内容...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MintGreen,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }
    }

    // 底部总结栏
    HorizontalDivider(
        thickness = 1.dp,
        color = WarmGray.copy(alpha = 0.3f)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CreamYellow.copy(alpha = 0.2f))
            .padding(8.dp)
    ) {
        Text(
            text = "总结栏",
            style = MaterialTheme.typography.labelMedium,
            color = EarthBrownLight
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = summary,
            onValueChange = onSummaryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("用一句话总结...") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MintGreen,
                unfocusedBorderColor = Color.Transparent
            )
        )
    }
}

/**
 * 思维导图模板组件（简化版）
 */
@Composable
fun MindMapNoteTemplate(
    centerTopic: String,
    branches: List<MindMapBranch>,
    onCenterTopicChange: (String) -> Unit,
    onBranchesChange: (List<MindMapBranch>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 中心主题
        OutlinedTextField(
            value = centerTopic,
            onValueChange = onCenterTopicChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("中心主题") },
            placeholder = { Text("输入中心主题") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MintGreen
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 分支列表
        Text(
            text = "分支",
            style = MaterialTheme.typography.labelMedium,
            color = EarthBrown
        )

        branches.forEachIndexed { index, branch ->
            var expanded by remember { mutableStateOf(false) }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = BeigeSurface.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "分支 ${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EarthBrownLight,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { expanded = !expanded }) {
                            Text(if (expanded) "收起" else "展开")
                        }
                    }

                    if (expanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = branch.title,
                            onValueChange = { newTitle ->
                                val newBranches = branches.toMutableList()
                                newBranches[index] = branch.copy(title = newTitle)
                                onBranchesChange(newBranches)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("分支标题") }
                        )
                    }
                }
            }
        }

        // 添加分支按钮
        TextButton(
            onClick = {
                onBranchesChange(branches + MindMapBranch("新分支", emptyList()))
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("+ 添加分支", color = MintGreen)
        }
    }
}

/**
 * 思维导图分支
 */
data class MindMapBranch(
    val title: String,
    val subItems: List<String>
)