# KeLing AI智能驱动大学生学习生活全流程游戏化闭环设计

## 一、设计理念

### 核心理念
将大学生从入学到毕业的完整学习生活流程游戏化，通过AI智能驱动，形成"规划→执行→追踪→反馈→激励"的完整闭环。

### 设计原则
1. **无感融入**：AI主动服务，减少用户操作负担
2. **正向激励**：以奖励代替惩罚，保护学习动力
3. **社交驱动**：引入竞争与合作，增强粘性
4. **个性定制**：AI学习用户习惯，提供个性化方案

---

## 二、功能架构总览

```
┌─────────────────────────────────────────────────────────────────┐
│                     AI智能中枢（恒星引擎）                        │
│   ┌─────────┬─────────┬─────────┬─────────┬─────────┐          │
│   │学习分析 │任务调度 │情感关怀 │智能推荐 │路径规划 │          │
│   └─────────┴─────────┴─────────┴─────────┴─────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                       六大功能模块                               │
├──────────────┬──────────────┬──────────────┬───────────────────┤
│  智能规划中心 │  专注执行引擎 │  知识成长树  │   复习巩固系统    │
│  (PLAN)      │  (FOCUS)     │  (KNOWLEDGE) │   (REVIEW)        │
├──────────────┼──────────────┼──────────────┼───────────────────┤
│  社交竞技场  │  激励成长体系 │              │                   │
│  (SOCIAL)    │  (REWARD)    │              │                   │
└──────────────┴──────────────┴──────────────┴───────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                       数据持久层                                 │
│         DataStore + SQLite + 云端同步（预留）                    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 三、模块详细设计

### 模块1：智能规划中心 (Intelligent Planning Center)

#### 1.1 学期目标规划器
```
功能流程：
用户输入学期目标（GPA、证书、技能等）
    ↓
AI分析课程难度、考试时间、个人能力
    ↓
生成学期里程碑计划
    ↓
分解为月计划→周计划→日计划
    ↓
动态调整（根据执行反馈）
```

**数据模型**：
```kotlin
data class SemesterGoal(
    val id: String,
    val userId: String,
    val semester: String,          // "2024春季"
    val targetType: GoalType,      // GPA, CERTIFICATE, SKILL, COMPETITION
    val targetValue: String,       // "3.5", "六级500分", "掌握Python"
    val deadline: Long,
    val milestones: List<Milestone>,
    val aiAnalysis: String,        // AI对目标的可行性分析
    val createdAt: Long
)

data class Milestone(
    val id: String,
    val goalId: String,
    val title: String,
    val description: String,
    val targetDate: Long,
    val isCompleted: Boolean = false,
    val subTasks: List<SubTask>
)
```

#### 1.2 智能周计划生成器
```kotlin
data class WeeklyPlan(
    val id: String,
    val userId: String,
    val weekStart: Long,
    val weekEnd: Long,
    val dailyPlans: List<DailyPlan>,
    val totalStudyHoursTarget: Int,
    val focusCourses: List<String>,      // 本周重点课程
    val aiSuggestions: String,           // AI建议
    val generatedAt: Long
)

data class DailyPlan(
    val date: Long,
    val morningSlots: List<PlannedSlot>,
    val afternoonSlots: List<PlannedSlot>,
    val eveningSlots: List<PlannedSlot>,
    val classes: List<CourseClass>,      // 当天课程
    val tasks: List<PlannedTask>,
    val flexibleTime: Int                // 可自由支配时间(分钟)
)
```

#### 1.3 考试冲刺模式
```kotlin
data class ExamSprintPlan(
    val id: String,
    val courseId: String,
    val examDate: Long,
    val daysRemaining: Int,
    val phases: List<SprintPhase>,
    val dailyReviewTasks: List<ReviewTask>,
    val predictedScore: Float,           // AI预测分数
    val confidenceLevel: Float,          // 预测置信度
    val createdAt: Long
)

data class SprintPhase(
    val phase: Int,                      // 第几阶段
    val name: String,                    // "基础巩固"、"重点突破"、"考前冲刺"
    val startDate: Long,
    val endDate: Long,
    val dailyHours: Int,
    val focusTopics: List<String>,
    val practiceCount: Int               // 练习题数量
)
```

---

### 模块2：专注执行引擎 (Focus Execution Engine)

#### 2.1 深度专注模式
```kotlin
data class DeepFocusSession(
    val id: String,
    val userId: String,
    val courseId: String?,
    val taskId: String?,
    val startTime: Long,
    val plannedDuration: Int,            // 计划时长(分钟)
    val actualDuration: Int = 0,
    val focusScore: Float = 0f,          // 专注度评分 0-1
    val distractions: List<DistractionEvent>,
    val breakCount: Int = 0,
    val status: FocusSessionStatus,
    val aiIntervention: List<AIIntervention>,  // AI干预记录
    val completedAt: Long?
)

data class DistractionEvent(
    val timestamp: Long,
    val type: DistractionType,           // SCREEN_OFF, APP_SWITCH, IDLE
    val duration: Int                    // 持续秒数
)

data class AIIntervention(
    val timestamp: Long,
    val type: InterventionType,          // ENCOURAGE, REMIND, BREAK_SUGGEST
    val message: String,
    val userResponse: String?            // POSITIVE, NEGATIVE, IGNORED
)
```

#### 2.2 学习状态追踪器
```kotlin
data class LearningStateTracker(
    val userId: String,
    val currentDate: Long,
    val energyLevel: Int,                // 1-10 精力水平
    val moodLevel: Int,                  // 1-10 心情水平
    val focusTrend: List<FocusRecord>,   // 专注力趋势
    val productivity: Float,             // 生产力指数
    val aiInsight: String,               // AI洞察
    val suggestions: List<String>        // 改进建议
)

data class FocusRecord(
    val timestamp: Long,
    val score: Float,
    val activity: String                 // 学习活动
)
```

#### 2.3 白噪音与氛围系统
```kotlin
data class StudyAmbience(
    val id: String,
    val name: String,                    // "图书馆"、"咖啡厅"、"雨天"
    val backgroundSound: String,         // 音频资源路径
    val visualTheme: String,             // 视觉主题
    val isCustom: Boolean,
    val userRating: Float?
)

// 预设氛围
val PRESET_AMBIENCES = listOf(
    StudyAmbience("lib", "图书馆", "library.mp3", "warm_light", false, null),
    StudyAmbience("cafe", "咖啡厅", "cafe.mp3", "cozy", false, null),
    StudyAmbience("rain", "雨天", "rain.mp3", "calm", false, null),
    StudyAmbience("night", "深夜", "night.mp3", "dark", false, null),
    StudyAmbience("forest", "森林", "forest.mp3", "nature", false, null)
)
```

---

### 模块3：知识成长树 (Knowledge Growth Tree)

#### 3.1 课程星球进化系统
```kotlin
data class CoursePlanet(
    val courseId: String,
    val planetType: PlanetType,          // 岩石、气态、冰冻、类地
    val evolutionStage: Int,             // 0-5 星球进化阶段
    val population: Int,                 // 知识点数量 = 人口
    val development: Float,              // 掌握度 = 发展度
    val resources: Int,                  // 学习时长 = 资源
    val achievements: List<PlanetAchievement>,
    val climate: PlanetClimate,          // 学习状态 = 气候
    val satellites: List<String>         // 关联课程 = 卫星
)

enum class PlanetType {
    ROCKY,       // 理科类课程
    GAS,         // 文科类课程
    ICE,         // 语言类课程
    TERRESTRIAL, // 工科类课程
    DWARF        // 选修类课程
}

data class PlanetAchievement(
    val id: String,
    val name: String,
    val description: String,
    val unlockedAt: Long
)
```

#### 3.2 知识树可视化
```kotlin
data class KnowledgeTree(
    val courseId: String,
    val trunkHeight: Int,                // 核心知识点数量
    val branchCount: Int,                // 分支知识点数量
    val leafCount: Int,                  // 细节知识点数量
    val fruitCount: Int,                 // 已掌握知识点 = 果实
    val health: Float,                   // 整体健康度
    val season: TreeSeason,              // 学习阶段 = 季节
    val nodes: List<TreeKnowledgeNode>
)

data class TreeKnowledgeNode(
    val knowledgeNodeId: String,
    val position: Offset,                // 树上的位置
    val size: Float,                     // 节点大小
    val color: Color,                    // 掌握度颜色
    val fruitType: FruitType?            // 果实类型（已掌握）
)
```

#### 3.3 AI知识问答系统
```kotlin
data class KnowledgeQA(
    val id: String,
    val courseId: String,
    val nodeId: String,
    val question: String,
    val userAnswer: String?,
    val correctAnswer: String,
    val difficulty: Int,
    val aiExplanation: String,           // AI生成的解释
    val relatedNodes: List<String>,
    val askedAt: Long,
    val answeredAt: Long?,
    val isCorrect: Boolean?
)
```

---

### 模块4：复习巩固系统 (Review & Consolidation System)

#### 4.1 遗忘曲线复习引擎
```kotlin
data class ForgettingCurveEngine(
    val userId: String,
    val reviewItems: List<ReviewItem>
)

data class ReviewItem(
    val id: String,
    val nodeId: String,
    val courseId: String,
    val lastReviewAt: Long,
    val nextReviewAt: Long,              // AI计算的最佳复习时间
    val reviewCount: Int,
    val easeFactor: Float,               // 记忆难易度因子
    val interval: Int,                   // 复习间隔(天)
    val retentionRate: Float,            // 预测记忆保持率
    val status: ReviewStatus             // DUE, LEARNING, MASTERED
)

// 复习提醒
data class ReviewReminder(
    val itemId: String,
    val reminderTime: Long,
    val urgency: Int,                    // 1-5 紧急程度
    val aiMessage: String                // AI生成的温馨提醒
)
```

#### 4.2 错题本系统
```kotlin
data class MistakeBook(
    val userId: String,
    val courseId: String,
    val mistakes: List<MistakeRecord>
)

data class MistakeRecord(
    val id: String,
    val courseId: String,
    val nodeId: String,
    val question: String,
    val userAnswer: String,
    val correctAnswer: String,
    val mistakeType: MistakeType,        // CONCEPT, CALCULATION, CARELESS
    val aiAnalysis: String,              // AI分析错误原因
    val aiSolution: String,              // AI解题思路
    val similarQuestions: List<String>,  // AI推荐的相似题
    val reviewCount: Int,
    val lastReviewAt: Long?,
    val masteredAt: Long?,
    val createdAt: Long
)

enum class MistakeType {
    CONCEPT,      // 概念理解错误
    CALCULATION,  // 计算错误
    CARELESS,     // 粗心大意
    METHOD,       // 方法选择错误
    KNOWLEDGE     // 知识点遗忘
}
```

#### 4.3 AI出题系统
```kotlin
data class AIQuizGenerator(
    val courseId: String,
    val nodeIds: List<String>,
    val difficulty: QuizDifficulty,
    val count: Int,
    val timeLimit: Int?,                 // 时间限制(分钟)
    val quizType: QuizType
)

data class GeneratedQuiz(
    val id: String,
    val courseId: String,
    val questions: List<QuizQuestion>,
    val totalScore: Int,
    val passingScore: Int,
    val aiTips: String,                  // AI备考建议
    val createdAt: Long
)

data class QuizQuestion(
    val id: String,
    val type: QuestionType,              // SINGLE_CHOICE, MULTI_CHOICE, FILL_BLANK, SHORT_ANSWER
    val content: String,
    val options: List<String>?,          // 选择题选项
    val correctAnswer: String,
    val explanation: String,             // AI生成的解析
    val relatedNodes: List<String>,
    val difficulty: Int,
    val score: Int
)

data class QuizResult(
    val quizId: String,
    val userId: String,
    val score: Int,
    val correctCount: Int,
    val wrongQuestions: List<WrongAnswer>,
    val aiFeedback: String,              // AI综合反馈
    val studySuggestions: List<String>,  // AI学习建议
    val completedAt: Long
)
```

---

### 模块5：社交竞技场 (Social Arena)

#### 5.1 学习小组系统
```kotlin
data class StudyGroup(
    val id: String,
    val name: String,
    val description: String,
    val creatorId: String,
    val members: List<GroupMember>,
    val targetCourse: String?,           // 目标课程
    val groupGoal: String,               // 小组目标
    val startDate: Long,
    val endDate: Long?,
    val status: GroupStatus,
    val weeklyChallenge: GroupChallenge?,
    val chatHistory: List<GroupMessage>,
    val createdAt: Long
)

data class GroupMember(
    val userId: String,
    val userName: String,
    val role: GroupRole,                 // LEADER, MEMBER
    val joinedAt: Long,
    val contribution: Int,               // 贡献值
    val weeklyStudyMinutes: Int,
    val completedTasks: Int
)

data class GroupChallenge(
    val id: String,
    val groupId: String,
    val title: String,
    val description: String,
    val target: Int,                     // 目标值
    val progress: Int,                   // 当前进度
    val unit: String,                    // "分钟"、"任务"、"知识点"
    val rewards: GroupRewards,
    val endDate: Long
)
```

#### 5.2 排行榜系统
```kotlin
data class Leaderboard(
    val type: LeaderboardType,           // DAILY, WEEKLY, MONTHLY, ALL_TIME
    val category: LeaderboardCategory,   // STUDY_TIME, TASKS, STREAK, MASTERY
    val entries: List<LeaderboardEntry>,
    val updatedAt: Long
)

data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val userName: String,
    val avatar: String?,
    val score: Long,                     // 分数/时长/数量
    val level: Int,
    val streakDays: Int,
    val isCurrentUser: Boolean
)

// 排行榜类型
enum class LeaderboardCategory {
    STUDY_TIME,    // 学习时长榜
    TASKS,         // 任务完成榜
    STREAK,        // 连续签到榜
    MASTERY,       // 知识掌握榜
    CRYSTALS,      // 结晶收集榜
    ACHIEVEMENTS   // 成就解锁榜
}
```

#### 5.3 学习PK系统
```kotlin
data class StudyPK(
    val id: String,
    val challengerId: String,
    val challengerName: String,
    val opponentId: String,
    val opponentName: String,
    val pkType: PKType,                  // STUDY_TIME, TASK_COUNT, QUIZ_SCORE
    val duration: Int,                   // PK持续时间(小时)
    val startTime: Long,
    val endTime: Long,
    val challengerScore: Int,
    val opponentScore: Int,
    val status: PKStatus,                // PENDING, ONGOING, COMPLETED
    val winnerId: String?,
    val rewards: PKRewards,
    val aiCommentary: String             // AI解说
)

data class PKRewards(
    val winnerCrystals: Int,
    val winnerExp: Int,
    val loserCrystals: Int,
    val loserExp: Int
)
```

#### 5.4 学习动态与分享
```kotlin
data class StudyMoment(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String?,
    val type: MomentType,                // CHECK_IN, ACHIEVEMENT, TASK_DONE, LEVEL_UP
    val content: String,
    val imageUrl: String?,
    val relatedData: String?,            // JSON格式的关联数据
    val likes: Int,
    val comments: List<MomentComment>,
    val createdAt: Long
)

data class MomentComment(
    val id: String,
    val userId: String,
    val userName: String,
    val content: String,
    val createdAt: Long
)
```

---

### 模块6：激励成长体系 (Reward & Growth System)

#### 6.1 用户成长系统
```kotlin
data class UserGrowth(
    val userId: String,
    val level: Int,
    val exp: Int,
    val expToNextLevel: Int,
    val title: String,                   // "星际园丁"、"知识探险家"
    val growthPath: GrowthPath,          // 成长方向
    val skills: Map<String, Int>,        // 技能等级
    val dailyEnergy: Int,                // 今日能量
    val maxEnergy: Int,                  // 最大能量
    val crystals: Int,                   // 结晶货币
    val vipLevel: Int,                   // VIP等级
    val vipExpiresAt: Long?
)

data class GrowthPath(
    val type: GrowthPathType,            // SCHOLAR, EXPLORER, SOCIALITE
    val name: String,
    val description: String,
    val perks: List<String>,
    val progress: Float
)

// 等级系统
val LEVEL_SYSTEM = mapOf(
    1 to LevelInfo("新星", 0, 100),
    2 to LevelInfo("探索者", 100, 200),
    3 to LevelInfo("学徒", 300, 300),
    5 to LevelInfo("学者", 600, 500),
    10 to LevelInfo("专家", 1500, 800),
    15 to LevelInfo("大师", 3500, 1200),
    20 to LevelInfo("宗师", 7000, 2000),
    25 to LevelInfo("传奇", 12000, 3000),
    30 to LevelInfo("神话", 20000, 5000)
)
```

#### 6.2 商城系统
```kotlin
data class Shop(
    val categories: List<ShopCategory>
)

data class ShopCategory(
    val id: String,
    val name: String,                    // "主题"、"道具"、"特权"
    val items: List<ShopItem>
)

data class ShopItem(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val price: Int,                      // 结晶价格
    val originalPrice: Int?,             // 原价（打折时显示）
    val type: ItemType,                  // THEME, PROP, PRIVILEGE, AVATAR
    val rarity: ItemRarity,              // COMMON, RARE, EPIC, LEGENDARY
    val stock: Int?,                     // 库存（null为无限）
    val expiresAt: Long?,                // 限时商品
    val effect: ItemEffect?              // 道具效果
)

data class ItemEffect(
    val type: EffectType,                // ENERGY_BOOST, EXP_BOOST, STREAK_PROTECT
    val value: Float,
    val duration: Int?                   // 持续时间(小时)
)

// 用户拥有的物品
data class UserInventory(
    val userId: String,
    val items: List<InventoryItem>
)

data class InventoryItem(
    val itemId: String,
    val quantity: Int,
    val purchasedAt: Long,
    val expiresAt: Long?                 // 道具有效期
)
```

#### 6.3 称号与徽章系统
```kotlin
data class Title(
    val id: String,
    val name: String,
    val description: String,
    val rarity: ItemRarity,
    val unlockCondition: String,
    val effect: TitleEffect?,            // 称号加成效果
    val equipped: Boolean = false
)

data class TitleEffect(
    val type: EffectType,
    val value: Float,
    val description: String              // "学习时长+10%"
)

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val category: BadgeCategory,
    val rarity: ItemRarity,
    val unlockedAt: Long?,
    val isDisplayed: Boolean             // 是否在个人页展示
)

enum class BadgeCategory {
    ACADEMIC,      // 学业成就
    STREAK,        // 坚持类
    SOCIAL,        // 社交类
    EXPLORATION,   // 探索类
    SPECIAL        // 特殊活动
}
```

#### 6.4 每日签到增强
```kotlin
data class EnhancedCheckIn(
    val userId: String,
    val todayChecked: Boolean,
    val streakDays: Int,
    val maxStreak: Int,
    val todayReward: CheckInReward,
    val tomorrowReward: CheckInReward,
    val streakBonus: Float,              // 连续签到加成
    val makeUpCards: Int,                // 补签卡数量
    val monthlyCheckIns: List<CheckInStatus>,
    val aiEncouragement: String          // AI鼓励语
)

data class CheckInStatus(
    val date: String,
    val checked: Boolean,
    val rewardClaimed: Boolean
)

// 签到奖励表（增强版）
val ENHANCED_CHECK_IN_REWARDS = listOf(
    CheckInReward(1, 10, 5, false, null, "良好的开始！"),
    CheckInReward(2, 15, 8, false, null, "继续加油！"),
    CheckInReward(3, 20, 10, false, null, "三天成习！"),
    CheckInReward(4, 25, 12, false, null, "习惯养成中..."),
    CheckInReward(5, 30, 15, false, null, "一周过半！"),
    CheckInReward(6, 40, 20, false, null, "周末冲刺！"),
    CheckInReward(7, 50, 30, true, "周冠军礼包！", "完美的一周！")
)

// 月度签到奖励
val MONTHLY_CHECK_IN_BONUS = mapOf(
    7 to MonthlyBonus("周冠军", Rewards(100, 50, 150), "连续7天签到"),
    14 to MonthlyBonus("双周达人", Rewards(200, 100, 300), "连续14天签到"),
    21 to MonthlyBonus("月度之星", Rewards(500, 200, 500), "连续21天签到"),
    30 to MonthlyBonus("满月成就", Rewards(1000, 500, 1000), "连续30天签到")
)
```

---

### 模块7：AI智能伴侣 (AI Companion)

#### 7.1 主动关怀系统
```kotlin
data class AICareSystem(
    val userId: String,
    val lastInteraction: Long,
    val careLevel: CareLevel,            // ATTENTIVE, NORMAL, RELAXED
    val scheduledCares: List<ScheduledCare>,
    val careHistory: List<CareEvent>
)

data class ScheduledCare(
    val id: String,
    val type: CareType,
    val scheduledTime: Long,
    val message: String,
    val triggered: Boolean
)

enum class CareType {
    MORNING_GREETING,          // 早安问候
    STUDY_REMINDER,            // 学习提醒
    BREAK_SUGGESTION,          // 休息建议
    NIGHT_SUMMARY,             // 晚间总结
    ACHIEVEMENT_CELEBRATION,   // 成就庆祝
    STREAK_WARNING,            // 连续签到预警
    EXAM_COUNTDOWN,            // 考试倒计时
    WEATHER_ALERT,             // 天气提醒
    HEALTH_CHECK               // 健康关怀
}

data class CareEvent(
    val id: String,
    val type: CareType,
    val timestamp: Long,
    val message: String,
    val userAction: UserAction?,        // POSITIVE, NEGATIVE, IGNORED
    val followUp: String?
)
```

#### 7.2 学习风格分析
```kotlin
data class LearningStyleAnalysis(
    val userId: String,
    val style: LearningStyle,
    val traits: Map<String, Float>,      // 特质分数
    val peakHours: List<Int>,            // 最佳学习时段
    val optimalDuration: Int,            // 最佳单次学习时长
    val preferences: LearningPreferences,
    val aiInsights: String,
    val recommendations: List<String>,
    val lastUpdated: Long
)

data class LearningStyle(
    val type: StyleType,                 // VISUAL, AUDITORY, KINESTHETIC, READING
    val name: String,
    val description: String,
    val strengths: List<String>,
    val weaknesses: List<String>
)

data class LearningPreferences(
    val preferredEnvironment: String,    // "安静", "有背景音"
    val preferredDuration: Int,          // 单次学习时长
    val preferredBreakInterval: Int,     // 休息间隔
    val preferredDifficulty: String,     // "循序渐进", "直接挑战"
    val groupStudyPreference: Float      // 群体学习偏好 0-1
)
```

#### 7.3 情绪识别与支持
```kotlin
data class EmotionalSupport(
    val userId: String,
    val currentMood: MoodState,
    val moodHistory: List<MoodRecord>,
    val stressLevel: Int,                // 1-10 压力水平
    val burnoutRisk: Float,              // 倦怠风险 0-1
    val supportStrategies: List<SupportStrategy>,
    val aiMessage: String
)

data class MoodRecord(
    val timestamp: Long,
    val mood: MoodState,
    val trigger: String?,                // 触发因素
    val context: String?                 // 情境描述
)

enum class MoodState {
    ENERGETIC,    // 精力充沛
    HAPPY,        // 愉快
    CALM,         // 平静
    ANXIOUS,      // 焦虑
    FRUSTRATED,   // 挫败
    TIRED,        // 疲惫
    OVERWHELMED   // 不堪重负
}

data class SupportStrategy(
    val type: StrategyType,
    val title: String,
    val description: String,
    val aiSuggestion: String
)

enum class StrategyType {
    BREAK,           // 休息
    EXERCISE,        // 运动
    SOCIAL,          // 社交
    MINDFULNESS,     // 正念
    GOAL_ADJUSTMENT, // 目标调整
    SEEK_HELP        // 寻求帮助
}
```

---

## 四、数据流与触发机制

### 4.1 自动触发规则
```kotlin
data class AutoTriggerRule(
    val id: String,
    val name: String,
    val condition: TriggerCondition,
    val action: TriggerAction,
    val priority: Int,
    val enabled: Boolean
)

data class TriggerCondition(
    val type: ConditionType,
    val params: Map<String, Any>
)

enum class ConditionType {
    TIME_BASED,        // 时间触发
    STREAK_RISK,       // 连续签到风险
    LOW_ENERGY,        // 能量低
    EXAM_APPROACHING,  // 考试临近
    INACTIVITY,        // 长时间未学习
    MILESTONE_REACHED, // 达成里程碑
    GOAL_BEHIND        // 目标进度落后
}

data class TriggerAction(
    val type: ActionType,
    val content: String,
    val params: Map<String, Any>?
)
```

### 4.2 AI决策引擎
```kotlin
class AIDecisionEngine {
    // 分析用户状态，生成个性化建议
    suspend fun analyzeAndSuggest(context: UserContext): AISuggestion

    // 根据学习数据预测未来表现
    suspend fun predictPerformance(courseId: String): PerformancePrediction

    // 生成最佳学习计划
    suspend fun generateOptimalPlan(constraints: TimeConstraints): StudyPlan

    // 情感分析与关怀
    suspend fun analyzeEmotion(recentActivities: List<Activity>): EmotionalState
}
```

---

## 五、实现优先级

### Phase 1：核心闭环 (高优先级)
1. ✅ 基础数据模型已完善
2. 🔄 遗忘曲线复习引擎
3. 🔄 AI主动关怀系统
4. 🔄 学习状态追踪器

### Phase 2：社交增强 (中优先级)
1. 排行榜系统
2. 学习小组
3. 学习PK
4. 学习动态分享

### Phase 3：深度游戏化 (中优先级)
1. 商城系统
2. 称号徽章
3. 星球进化可视化
4. 签到系统增强

### Phase 4：智能升级 (持续迭代)
1. 学习风格分析
2. 情绪识别支持
3. 考试冲刺模式
4. 学期目标规划器

---

## 六、技术要点

### 6.1 数据同步策略
- 本地优先：核心数据存储在 DataStore
- 定期同步：每5分钟同步到云端（预留）
- 冲突解决：以最新修改时间为准

### 6.2 性能优化
- 懒加载：大数据集分页加载
- 缓存策略：常用数据内存缓存
- 后台任务：WorkManager处理定时任务

### 6.3 AI调用策略
- 本地优先：简单规则本地处理
- 智能节流：避免频繁调用云端AI
- 降级策略：网络失败时本地兜底

---

## 七、预期效果

### 用户价值
1. **学习效率提升**：AI智能规划 + 专注追踪
2. **知识掌握深化**：遗忘曲线 + 间隔复习
3. **学习动力增强**：游戏化激励 + 社交竞争
4. **身心健康保护**：情感关怀 + 压力预警

### 产品价值
1. **用户粘性**：游戏化闭环提升留存
2. **活跃度**：签到 + 社交 + AI互动
3. **变现能力**：商城 + VIP + 道具
4. **数据价值**：学习行为数据积累