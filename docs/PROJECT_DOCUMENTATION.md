# 课灵 (KeLing) - AI学习助手

## 完整技术文档

---

# 一、新应用结构及开发环境设计

## 1.1 项目架构概览

课灵采用 **Monorepo（单体仓库）** 架构，统一管理多个应用平台：

```
KeLing/
├── apps/
│   ├── android/          # Android 原生应用
│   │   ├── app/          # 主应用模块
│   │   │   ├── src/main/java/com/keling/app/
│   │   │   │   ├── ai/                 # AI核心模块
│   │   │   │   ├── data/               # 数据层
│   │   │   │   ├── network/            # 网络层
│   │   │   │   ├── ui/                 # UI组件
│   │   │   │   │   ├── components/     # 通用组件
│   │   │   │   │   ├── screens/        # 页面屏幕
│   │   │   │   │   └── theme/          # 主题样式
│   │   │   │   ├── viewmodel/          # 视图模型
│   │   │   │   └── MainActivity.kt     # 主入口
│   │   │   └── build.gradle.kts        # 构建配置
│   │   └── keling-release.jks          # 签名密钥
│   │
│   ├── web/              # Web PWA 应用
│   │   ├── src/
│   │   │   ├── pages/        # 页面组件
│   │   │   ├── components/   # 通用组件
│   │   │   ├── services/     # API服务
│   │   │   ├── store/        # 状态管理
│   │   │   └── App.tsx       # 主入口
│   │   ├── public/           # 静态资源
│   │   └── vite.config.ts    # Vite配置
│   │
│   └── server/           # Node.js 后端服务
│       ├── src/
│       │   ├── controllers/  # 控制器
│       │   ├── routes/       # 路由定义
│       │   ├── middleware/   # 中间件
│       │   └── index.ts      # 服务入口
│       └── prisma/           # 数据库模型
│
├── render.yaml           # Render部署配置
└── package.json          # Monorepo配置
```

## 1.2 开发环境

### 1.2.1 硬件环境

| 组件 | 配置要求 | 实际使用 |
|------|----------|----------|
| 操作系统 | Windows 10/11, macOS, Linux | Windows 11 Home China |
| 处理器 | Intel i5/AMD Ryzen 5 及以上 | - |
| 内存 | 8GB RAM 及以上 | - |
| 存储空间 | 20GB 可用空间 | - |
| Android设备 | Android 8.0+ (API 26+) | 用于测试APK |
| iOS设备 | iOS 12.0+ | 通过PWA访问 |

### 1.2.2 软件环境

| 软件 | 版本 | 用途 |
|------|------|------|
| **Android Studio** | Hedgehog (2023.1.1) | Android开发IDE |
| **JDK** | 17 | Java/Kotlin编译 |
| **Node.js** | v18+ | Web/Server运行时 |
| **npm** | v9+ | 包管理器 |
| **Git** | v2.40+ | 版本控制 |
| **VS Code** | 最新版 | Web/Server开发IDE |
| **PostgreSQL** | v14+ | 生产数据库（Render托管） |

### 1.2.3 开发工具链

```
Android 开发：
├── Kotlin 1.9.22
├── Jetpack Compose (UI框架)
├── Gradle 8.x (构建工具)
├── DataStore (本地存储)
├── Coil (图片加载)
└── Coroutines (异步处理)

Web 开发：
├── React 19.2.4
├── TypeScript 5.9
├── Vite 6.4
├── React Router 7
├── Zustand (状态管理)
├── TanStack Query (数据请求)
└── vite-plugin-pwa (PWA支持)

后端开发：
├── Node.js + Express
├── Prisma ORM
├── PostgreSQL
├── JWT认证
└── CORS跨域
```

---

# 二、用户界面设计 (UI Prototype)

## 2.1 设计理念

课灵采用 **"星球培育"** 游戏化设计理念，将学习过程拟物化为培育星球：

- 🌍 **课程** = 知识星球
- 🌱 **学习** = 培育成长
- ⚡ **能量** = 学习动力
- 💎 **结晶** = 学习成果
- 🎯 **任务** = 探索任务

## 2.2 色彩系统

```css
主色调 (Primary):     #E8A87C  /* 恒星橙 - 温暖、活力 */
背景色 (Background):  #FFF8F0  /* 星际白 - 柔和、护眼 */
强调色 (Accent):      #88B04B  /* 生机绿 - 成长、希望 */
次级色 (Secondary):   #D4A574  /* 沙漠金 - 稳重、深沉 */
成功色 (Success):     #4CAF50  /* 完成绿 */
警告色 (Warning):     #FFD54F  /* 提示黄 */
```

## 2.3 主要页面设计

### 2.3.1 首页 (Home Screen)

```
┌─────────────────────────────────────┐
│  ✨ 课灵 KeLing          [能量] [结晶] │
├─────────────────────────────────────┤
│                                     │
│   ┌─────────────────────────────┐   │
│   │     🌟 欢迎回来，星际园丁！    │   │
│   │     今日已学习 45 分钟        │   │
│   │     ────────────────         │   │
│   │     🔥 连续签到 3 天          │   │
│   └─────────────────────────────┘   │
│                                     │
│   📚 我的星球                        │
│   ┌─────┐ ┌─────┐ ┌─────┐          │
│   │ 🪐  │ │ 🌍  │ │ 🌙  │ [+创建]  │
│   │高等 │ │线性 │ │数据 │          │
│   │数学 │ │代数 │ │结构 │          │
│   └─────┘ └─────┘ └─────┘          │
│                                     │
│   📋 今日任务                        │
│   ├─ 完成数学作业           [开始]  │
│   ├─ 复习线性代数笔记       [开始]  │
│   └─ 预习数据结构           [开始]  │
│                                     │
├─────────────────────────────────────┤
│ [🏠首页] [🌱温室] [✨AI] [📋任务] [👤我的] │
└─────────────────────────────────────┘
```

### 2.3.2 星球温室 (Greenhouse Screen)

```
┌─────────────────────────────────────┐
│  ← 星球温室              [知识图谱]  │
├─────────────────────────────────────┤
│                                     │
│         ┌─────────────────┐         │
│         │                 │         │
│         │    🪐 高等数学   │         │
│         │    ████████░░   │         │
│         │    掌握度: 78%   │         │
│         │    阶段: 成长期  │         │
│         │                 │         │
│         └─────────────────┘         │
│                                     │
│   📊 学习统计                        │
│   ├─ 累计学习: 1,250 分钟           │
│   ├─ 完成任务: 32 个                │
│   └─ 知识节点: 15 个                │
│                                     │
│   📖 知识节点                        │
│   ┌─────┐ ┌─────┐ ┌─────┐          │
│   │极限 │ → │导数 │ → │积分 │        │
│   └─────┘ └─────┘ └─────┘          │
│                                     │
├─────────────────────────────────────┤
│ [🏠首页] [🌱温室] [✨AI] [📋任务] [👤我的] │
└─────────────────────────────────────┘
```

### 2.3.3 AI助手 (AI Assistant Screen)

```
┌─────────────────────────────────────┐
│  ✨ AI学习助手                       │
├─────────────────────────────────────┤
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 🤖 你好！我是你的学习助手。  │   │
│   │ 有什么问题可以问我~         │   │
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 👤 请帮我解释一下极限的定义  │   │
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 🤖 极限是微积分的基础概念... │   │
│   │                             │   │
│   │ 极限的严格定义（ε-δ定义）： │   │
│   │ 设函数f(x)在点x₀的某去心... │   │
│   │                             │   │
│   │ [📚 保存为笔记] [🎯 生成题目]│   │
│   └─────────────────────────────┘   │
│                                     │
│   ┌─────────────────────────────┐   │
│   │ 💬 输入你的问题...     [发送]│   │
│   └─────────────────────────────┘   │
│                                     │
├─────────────────────────────────────┤
│ [🏠首页] [🌱温室] [✨AI] [📋任务] [👤我的] │
└─────────────────────────────────────┘
```

### 2.3.4 知识图谱 (Knowledge Graph)

```
┌─────────────────────────────────────┐
│  ← 知识图谱 - 高等数学    [+添加节点] │
├─────────────────────────────────────┤
│                                     │
│              ┌──────┐               │
│              │ 极限 │               │
│              └──┬───┘               │
│                 │                   │
│         ┌───────┴───────┐           │
│         ↓               ↓           │
│    ┌──────┐        ┌──────┐         │
│    │ 导数 │        │ 连续│         │
│    └──┬───┘        └──────┘         │
│       │                             │
│       ↓                             │
│    ┌──────┐    ┌──────┐             │
│    │ 积分 │ ←─ │微分  │             │
│    └──────┘    └──────┘             │
│                                     │
│   ─────────────────────────────     │
│   节点详情: 导数                     │
│   ├─ 难度: ★★★☆☆                   │
│   ├─ 掌握度: 65%                     │
│   └─ 描述: 函数变化率的度量...       │
│                                     │
├─────────────────────────────────────┤
│ [🏠首页] [🌱温室] [✨AI] [📋任务] [👤我的] │
└─────────────────────────────────────┘
```

---

# 三、软件和硬件说明

## 3.1 硬件需求

### 客户端硬件

| 平台 | 最低配置 | 推荐配置 |
|------|----------|----------|
| **Android** | Android 8.0 (API 26)<br>2GB RAM<br>16GB存储 | Android 12+<br>4GB RAM<br>32GB存储 |
| **iOS** | iOS 12.0+<br>Safari浏览器 | iOS 15+<br>Safari浏览器 |
| **Web** | 任意现代浏览器<br>Chrome 90+, Safari 14+ | Chrome最新版<br>支持PWA |

### 服务器硬件

| 组件 | 配置 | 说明 |
|------|------|------|
| **云服务商** | Render.com | 免费套餐 |
| **CPU** | 0.1 vCPU | 共享CPU |
| **内存** | 512MB RAM | 容器限制 |
| **存储** | 1GB | 临时存储 |
| **数据库** | PostgreSQL | Render托管 |

## 3.2 软件依赖

### Android 应用依赖

```gradle
// 核心依赖
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
implementation("androidx.activity:activity-compose:1.8.2")

// Jetpack Compose
implementation(platform("androidx.compose:compose-bom:2024.02.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.ui:ui-tooling-preview")

// 数据持久化
implementation("androidx.datastore:datastore-preferences:1.0.0")

// 图片加载
implementation("io.coil-kt:coil-compose:2.5.0")

// 网络请求
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.google.code.gson:gson:2.10.1")

// 序列化
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

// 导航
implementation("androidx.navigation:navigation-compose:2.7.6")
```

### Web 应用依赖

```json
{
  "dependencies": {
    "react": "^19.2.4",
    "react-dom": "^19.2.4",
    "react-router-dom": "^7.13.2",
    "@tanstack/react-query": "^5.95.2",
    "axios": "^1.13.6",
    "zustand": "^5.0.12",
    "framer-motion": "^12.38.0",
    "lucide-react": "^1.7.0"
  },
  "devDependencies": {
    "vite": "^6.4.1",
    "typescript": "~5.9.3",
    "vite-plugin-pwa": "^0.21.2",
    "@vitejs/plugin-react": "^4.4.1"
  }
}
```

### 后端服务依赖

```json
{
  "dependencies": {
    "express": "^4.18.2",
    "@prisma/client": "^5.8.0",
    "cors": "^2.8.5",
    "dotenv": "^16.3.1",
    "jsonwebtoken": "^9.0.2",
    "bcryptjs": "^2.4.3"
  },
  "devDependencies": {
    "prisma": "^5.8.0",
    "typescript": "^5.3.3"
  }
}
```

---

# 四、程序高层设计 (High Level Design)

## 4.1 系统架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        客户端层 (Client Layer)                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌───────────────┐    ┌───────────────┐    ┌───────────────┐   │
│  │  Android App  │    │   Web PWA     │    │   iOS PWA     │   │
│  │  (Kotlin +    │    │  (React +     │    │  (Safari +    │   │
│  │   Compose)    │    │   TypeScript) │    │   Manifest)   │   │
│  └───────┬───────┘    └───────┬───────┘    └───────┬───────┘   │
│          │                    │                    │           │
│          └────────────────────┼────────────────────┘           │
│                               │                                 │
├───────────────────────────────┼─────────────────────────────────┤
│                        API网关层 (API Gateway)                  │
├───────────────────────────────┼─────────────────────────────────┤
│                               │                                 │
│                     ┌─────────▼─────────┐                       │
│                     │   RESTful API     │                       │
│                     │  keling-server   │                       │
│                     │ .onrender.com     │                       │
│                     └─────────┬─────────┘                       │
│                               │                                 │
├───────────────────────────────┼─────────────────────────────────┤
│                        业务逻辑层 (Business Logic)              │
├───────────────────────────────┼─────────────────────────────────┤
│                               │                                 │
│  ┌──────────────┐ ┌──────────▼──────────┐ ┌──────────────┐     │
│  │  Auth Service│ │   Course Service    │ │  Task Service│     │
│  │  (用户认证)  │ │   (课程管理)        │ │  (任务管理)  │     │
│  └──────────────┘ └─────────────────────┘ └──────────────┘     │
│                                                                 │
│  ┌──────────────┐ ┌─────────────────────┐ ┌──────────────┐     │
│  │  AI Service  │ │   Note Service      │ │Checkin Service│    │
│  │  (AI对话)    │ │   (笔记管理)        │ │  (签到系统)  │     │
│  └──────────────┘ └─────────────────────┘ └──────────────┘     │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                        数据层 (Data Layer)                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────┐    ┌─────────────────────┐            │
│  │    PostgreSQL       │    │     DataStore       │            │
│  │   (服务端存储)      │    │   (本地存储)        │            │
│  │   - 用户数据        │    │   - 用户设置        │            │
│  │   - 课程数据        │    │   - 离线缓存        │            │
│  │   - 任务数据        │    │   - 学习记录        │            │
│  └─────────────────────┘    └─────────────────────┘            │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                        外部服务层 (External Services)           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────┐    ┌─────────────────────┐            │
│  │   DeepSeek API      │    │      Vercel         │            │
│  │   (AI大模型服务)    │    │   (Web静态托管)     │            │
│  └─────────────────────┘    └─────────────────────┘            │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 4.2 模块设计

### 4.2.1 Android 模块结构

```
com.keling.app/
│
├── ai/                          # AI核心模块
│   ├── AICoordinator.kt         # AI协调器（总调度）
│   ├── DeepSeekClient.kt        # DeepSeek API客户端
│   ├── AIResponse.kt            # AI响应解析
│   ├── LocalRuleEngine.kt       # 本地规则引擎
│   ├── ToolCommandParser.kt     # 工具命令解析器
│   ├── ConversationManager.kt   # 对话管理
│   ├── ChatMemory.kt            # 聊天记忆
│   ├── AIQuizGenerator.kt       # 题目生成器
│   ├── StudyPlanGenerator.kt    # 学习计划生成
│   └── tools/
│       └── Tools.kt             # AI工具定义
│
├── data/                        # 数据层
│   ├── Models.kt                # 数据模型定义
│   ├── DataRepository.kt        # 数据仓库（持久化）
│   └── AuthRepository.kt        # 认证仓库
│
├── network/                     # 网络层
│   └── ApiClient.kt             # API客户端
│
├── ui/                          # UI层
│   ├── components/              # 通用组件
│   │   ├── Cards.kt             # 卡片组件
│   │   ├── Buttons.kt           # 按钮组件
│   │   ├── PomodoroTimer.kt     # 番茄钟组件
│   │   ├── RichTextEditor.kt    # 富文本编辑器
│   │   └── ...
│   │
│   ├── screens/                 # 页面屏幕
│   │   ├── home/                # 首页
│   │   ├── greenhouse/          # 星球温室
│   │   ├── ai/                  # AI助手
│   │   ├── tasks/               # 任务管理
│   │   ├── notes/               # 笔记
│   │   ├── knowledge/           # 知识图谱
│   │   ├── achievements/        # 成就系统
│   │   ├── profile/             # 个人中心
│   │   ├── settings/            # 设置
│   │   ├── report/              # 学习报告
│   │   └── auth/                # 认证页面
│   │
│   └── theme/                   # 主题样式
│       ├── Color.kt             # 颜色定义
│       ├── Theme.kt             # 主题配置
│       └── Type.kt              # 字体排版
│
├── viewmodel/                   # 视图模型
│   ├── AppViewModel.kt          # 主视图模型
│   └── AiChatViewModel.kt       # AI聊天视图模型
│
├── update/                      # 更新模块
│   ├── UpdateService.kt         # 更新服务
│   └── UpdateDialog.kt          # 更新对话框
│
└── MainActivity.kt              # 主入口
```

### 4.2.2 数据流设计

```
用户操作 → ViewModel → Repository → DataStore/API
                │
                ├─→ 本地数据 (DataStore)
                │   └─ 即时响应，离线可用
                │
                └─→ 远程数据 (API Server)
                    └─ 同步到云端，多端共享
```

## 4.3 核心算法设计

### 4.3.1 AI对话流程

```
┌─────────────┐
│ 用户输入消息 │
└──────┬──────┘
       │
       ▼
┌─────────────────────┐
│ ConversationManager │
│ 添加消息到上下文     │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│   DeepSeekClient    │
│ 调用AI API生成回复   │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  ToolCommandParser  │
│ 解析AI返回的工具命令 │
└──────────┬──────────┘
           │
           ├─ 普通文本回复 → 显示给用户
           │
           └─ 工具命令 → 执行对应操作
              ├─ create_course → 创建课程
              ├─ create_task → 创建任务
              ├─ create_note → 创建笔记
              └─ ...
```

### 4.3.2 成就解锁算法

```kotlin
fun checkAndUnlockAchievements() {
    val completedTasks = tasks.count { it.status == TaskStatus.COMPLETED }
    val unlockedNodes = knowledgeNodes.count { it.isUnlocked }

    achievements.forEach { achievement ->
        val newProgress = when (achievement.id) {
            "first_task" -> if (completedTasks >= 1) 1 else 0
            "task_master_10" -> completedTasks.coerceAtMost(10)
            "streak_7" -> user.streakDays.coerceAtMost(7)
            // ...
        }

        if (newProgress >= achievement.maxProgress && !achievement.isUnlocked) {
            // 解锁成就，发放奖励
            unlockAchievement(achievement)
        }
    }
}
```

---

# 五、共享数据样例

## 5.1 用户数据 (User)

```json
{
  "id": "user_abc123",
  "name": "星际园丁",
  "email": "user@example.com",
  "level": 5,
  "exp": 450,
  "energy": 120,
  "crystals": 85,
  "streakDays": 7,
  "totalStudyMinutes": 1250,
  "createdAt": 1709827200000,
  "avatarUrl": "https://example.com/avatar.jpg",
  "bio": "热爱学习的大学生",
  "weeklyStudyGoal": 300
}
```

## 5.2 课程数据 (Course)

```json
{
  "id": "course_math001",
  "name": "高等数学",
  "code": "MATH101",
  "teacher": "张教授",
  "schedule": [
    {
      "dayOfWeek": 1,
      "startHour": 8,
      "startMinute": 0,
      "durationMinutes": 90
    },
    {
      "dayOfWeek": 3,
      "startHour": 14,
      "startMinute": 30,
      "durationMinutes": 90
    }
  ],
  "location": "教学楼A-301",
  "themeColor": 15263964,
  "masteryLevel": 0.78,
  "plantStage": 3,
  "planetStyleIndex": 2,
  "lastStudiedAt": 1710259200000,
  "totalStudyMinutes": 450,
  "isArchived": false,
  "studySessionCount": 12
}
```

## 5.3 任务数据 (Task)

```json
{
  "id": "task_xyz789",
  "title": "完成高数第三章习题",
  "description": "完成教材P125-P130的所有练习题",
  "type": "DEEP_EXPLORATION",
  "courseId": "course_math001",
  "status": "IN_PROGRESS",
  "priority": 4,
  "estimatedMinutes": 45,
  "actualMinutes": null,
  "rewards": {
    "energy": 15,
    "crystals": 8,
    "exp": 25
  },
  "createdAt": 1710172800000,
  "scheduledAt": 1710259200000,
  "completedAt": null
}
```

## 5.4 知识节点数据 (KnowledgeNode)

```json
{
  "id": "kn_limit001",
  "courseId": "course_math001",
  "name": "极限",
  "description": "函数极限的定义与计算方法",
  "parentIds": [],
  "childIds": ["kn_derivative001", "kn_continuous001"],
  "difficulty": 3,
  "masteryLevel": 0.85,
  "positionX": 0.5,
  "positionY": 0.2,
  "isUnlocked": true
}
```

## 5.5 笔记数据 (Note)

```json
{
  "id": "note_qwe456",
  "title": "极限的ε-δ定义",
  "content": "## 定义\n极限的严格定义...\n\n### 例子\n1. 证明 lim(x→0) x² = 0\n2. ...",
  "sourceType": "AI_GENERATED",
  "aiExplanation": "极限是微积分的基础概念...",
  "relatedNodeIds": ["kn_limit001"],
  "tags": ["数学", "极限", "微积分"],
  "createdAt": 1710172800000,
  "updatedAt": 1710259200000,
  "reviewCount": 3,
  "lastReviewedAt": 1710432000000
}
```

## 5.6 成就数据 (Achievement)

```json
{
  "id": "streak_7",
  "name": "周周坚持",
  "description": "连续学习7天",
  "icon": "🌟",
  "category": "STREAK",
  "requirement": "连续学习7天",
  "rewardEnergy": 150,
  "rewardCrystals": 30,
  "isUnlocked": true,
  "unlockedAt": 1710432000000,
  "progress": 7,
  "maxProgress": 7
}
```

## 5.7 学习记录数据 (StudyRecord)

```json
{
  "id": "record_001",
  "userId": "user_abc123",
  "courseId": "course_math001",
  "taskId": "task_xyz789",
  "type": "TASK_COMPLETION",
  "durationMinutes": 45,
  "createdAt": 1710259200000,
  "notes": "完成了极限相关的习题"
}
```

---

# 六、数据流转及展示方式

## 6.1 数据流转架构图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           数据流转全景图                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────┐                                                        │
│  │  用户操作   │                                                        │
│  │ (点击/输入) │                                                        │
│  └──────┬──────┘                                                        │
│         │                                                               │
│         ▼                                                               │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                      UI Layer (Compose/React)                    │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │   │
│  │  │  Screen  │  │  Screen  │  │  Screen  │  │  Screen  │        │   │
│  │  │  (首页)  │  │  (温室)  │  │  (AI)    │  │  (任务)  │        │   │
│  │  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘        │   │
│  │       │             │             │             │               │   │
│  │       └──────────────┴──────┬──────┴─────────────┘               │   │
│  │                             │                                   │   │
│  └─────────────────────────────┼───────────────────────────────────┘   │
│                                │                                       │
│                                ▼                                       │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    ViewModel Layer (MVVM)                        │   │
│  │                                                                  │   │
│  │  ┌──────────────────────────────────────────────────────────┐   │   │
│  │  │                    AppViewModel                           │   │   │
│  │  │                                                           │   │   │
│  │  │  State:                                                   │   │   │
│  │  │  ├─ _currentUser: MutableState<User>                      │   │   │
│  │  │  ├─ _courses: MutableState<List<Course>>                  │   │   │
│  │  │  ├─ _tasks: MutableState<List<Task>>                      │   │   │
│  │  │  ├─ _knowledgeNodes: MutableState<List<KnowledgeNode>>    │   │   │
│  │  │  ├─ _notes: MutableState<List<Note>>                      │   │   │
│  │  │  └─ _achievements: MutableState<List<Achievement>>        │   │   │
│  │  │                                                           │   │   │
│  │  │  Methods:                                                 │   │   │
│  │  │  ├─ addCourse() → 更新State + 保存到Repository            │   │   │
│  │  │  ├─ completeTask() → 更新State + 发放奖励                 │   │   │
│  │  │  ├─ createKnowledgeNode() → 更新图谱                      │   │   │
│  │  │  └─ checkAndUnlockAchievements() → 检查成就               │   │   │
│  │  │                                                           │   │   │
│  │  └──────────────────────────┬───────────────────────────────┘   │   │
│  │                             │                                   │   │
│  └─────────────────────────────┼───────────────────────────────────┘   │
│                                │                                       │
│                ┌───────────────┴───────────────┐                       │
│                │                               │                       │
│                ▼                               ▼                       │
│  ┌─────────────────────────┐    ┌─────────────────────────┐           │
│  │   Local Data Layer      │    │   Remote Data Layer     │           │
│  │   (DataStore)           │    │   (API Server)          │           │
│  │                         │    │                         │           │
│  │  ┌───────────────────┐  │    │  ┌───────────────────┐  │           │
│  │  │ UserPreferences   │  │    │  │   /api/auth       │  │           │
│  │  │ - 用户设置        │  │    │  │   - 登录/注册     │  │           │
│  │  └───────────────────┘  │    │  └───────────────────┘  │           │
│  │  ┌───────────────────┐  │    │  ┌───────────────────┐  │           │
│  │  │ CourseData        │  │    │  │   /api/courses    │  │           │
│  │  │ - 课程列表        │  │    │  │   - 课程CRUD      │  │           │
│  │  └───────────────────┘  │    │  └───────────────────┘  │           │
│  │  ┌───────────────────┐  │    │  ┌───────────────────┐  │           │
│  │  │ TaskData          │  │    │  │   /api/tasks      │  │           │
│  │  │ - 任务列表        │  │    │  │   - 任务CRUD      │  │           │
│  │  └───────────────────┘  │    │  └───────────────────┘  │           │
│  │  ┌───────────────────┐  │    │  ┌───────────────────┐  │           │
│  │  │ KnowledgeGraph    │  │    │  │   /api/knowledge  │  │           │
│  │  │ - 知识节点        │  │    │  │   - 图谱数据      │  │           │
│  │  └───────────────────┘  │    │  └───────────────────┘  │           │
│  │  ┌───────────────────┐  │    │  ┌───────────────────┐  │           │
│  │  │ CheckInRecords    │  │    │  │   /api/checkin    │  │           │
│  │  │ - 签到记录        │  │    │  │   - 签到数据      │  │           │
│  │  └───────────────────┘  │    │  └───────────────────┘  │           │
│  │                         │    │                         │           │
│  │  特点:                  │    │  特点:                  │           │
│  │  ✓ 离线可用            │    │  ✓ 多端同步            │           │
│  │  ✓ 即时响应            │    │  ✓ 数据备份            │           │
│  │  ✓ 自动保存            │    │  ✓ 云端存储            │           │
│  │                         │    │                         │           │
│  └─────────────────────────┘    └─────────────────────────┘           │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

## 6.2 数据展示流程

### 6.2.1 首页数据展示

```
AppViewModel State
       │
       ├── _currentUser ──────────────────┐
       │                                  │
       │   User {                         │   首页展示:
       │     name: "星际园丁",           │   ├─ 欢迎语: "欢迎回来，${name}！"
       │     level: 5,                    │   ├─ 等级进度条
       │     energy: 120,                 │   ├─ 能量/结晶数值
       │     crystals: 85,                │   └─ 签到状态
       │     streakDays: 7                │
       │   }                              │
       │                                  │
       ├── _courses ──────────────────────┤
       │                                  │
       │   [Course, Course, ...]          │   首页展示:
       │                                  │   ├─ 星球列表
       │   每个Course:                    │   ├─ 星球名称/图片
       │   {                              │   ├─ 掌握度进度
       │     name,                        │   └─ 点击进入详情
       │     planetStyleIndex,            │
       │     masteryLevel                 │
       │   }                              │
       │                                  │
       ├── _tasks ────────────────────────┤
       │                                  │
       │   [Task, Task, ...]              │   首页展示:
       │                                  │   ├─ 今日任务列表
       │   筛选: status != COMPLETED      │   ├─ 任务状态
       │                                  │   ├─ 预计时长
       │                                  │   └─ 开始按钮
       │                                  │
       └── _achievements ─────────────────┘
                                          │
                              成就弹窗展示:
                              ├─ 最新解锁成就
                              └─ 成就进度
```

### 6.2.2 AI对话数据流

```
用户输入: "帮我创建一个数学星球"
         │
         ▼
┌─────────────────────────────────┐
│     AiChatViewModel             │
│                                 │
│  1. 添加用户消息到记忆          │
│  2. 构建上下文                  │
│  3. 调用 DeepSeek API           │
│                                 │
└────────────────┬────────────────┘
                 │
                 ▼
┌─────────────────────────────────┐
│     DeepSeek API 响应           │
│                                 │
│  "好的，我帮你创建一个数学星球  │
│   [CREATE_COURSE: name=数学]"   │
│                                 │
└────────────────┬────────────────┘
                 │
                 ▼
┌─────────────────────────────────┐
│     ToolCommandParser           │
│                                 │
│  解析出命令:                    │
│  - type: CREATE_COURSE          │
│  - params: {name: "数学"}       │
│                                 │
└────────────────┬────────────────┘
                 │
                 ▼
┌─────────────────────────────────┐
│     AppViewModel                │
│                                 │
│  createCourse("数学")           │
│  ├─ 生成Course对象              │
│  ├─ 更新 _courses State         │
│  └─ 保存到 DataStore            │
│                                 │
└────────────────┬────────────────┘
                 │
                 ▼
┌─────────────────────────────────┐
│     UI 更新                     │
│                                 │
│  - 首页星球列表新增"数学"       │
│  - 温室显示新星球               │
│  - AI回复确认消息               │
│                                 │
└─────────────────────────────────┘
```

## 6.3 数据同步策略

### 6.3.1 本地优先策略

```
┌─────────────────────────────────────────────────────────────┐
│                     本地优先数据流                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  用户操作                                                    │
│     │                                                       │
│     ▼                                                       │
│  ┌──────────────────┐                                       │
│  │  立即更新UI      │  ← 即时响应，用户体验好               │
│  └────────┬─────────┘                                       │
│           │                                                 │
│           ▼                                                 │
│  ┌──────────────────┐                                       │
│  │  保存到DataStore │  ← 本地持久化，离线可用               │
│  └────────┬─────────┘                                       │
│           │                                                 │
│           │ 网络可用时                                       │
│           ▼                                                 │
│  ┌──────────────────┐                                       │
│  │  同步到服务器    │  ← 后台同步，多端共享                 │
│  └──────────────────┘                                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 6.3.2 冲突解决策略

```
当本地与服务器数据冲突时：

1. 时间戳优先：使用最新修改时间的数据
2. 服务器优先：关键数据以服务器为准
3. 合并策略：列表数据尝试合并

示例：
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   本地数据   │     │   服务器数据  │     │   合并结果   │
│ updatedAt:   │     │ updatedAt:   │     │              │
│   10:30      │ vs │   10:35      │ →  │ 使用服务器   │
│              │     │              │     │ 数据         │
└──────────────┘     └──────────────┘     └──────────────┘
```

---

# 七、其他重要信息

## 7.1 API接口清单

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/register` | POST | 用户注册 |
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/me` | GET | 获取当前用户 |
| `/api/courses` | GET/POST | 课程列表/创建 |
| `/api/courses/:id` | GET/PUT/DELETE | 课程详情/更新/删除 |
| `/api/tasks` | GET/POST | 任务列表/创建 |
| `/api/tasks/:id` | PUT/DELETE | 任务更新/删除 |
| `/api/knowledge` | GET/POST | 知识节点列表/创建 |
| `/api/notes` | GET/POST | 笔记列表/创建 |
| `/api/achievements` | GET | 成就列表 |
| `/api/checkin` | POST | 签到 |
| `/api/checkin/status` | GET | 签到状态 |
| `/api/health` | GET | 服务健康检查 |
| `/api/app/version` | GET | APP版本检查 |

## 7.2 安全设计

### 7.2.1 认证机制

```
用户登录流程：

1. 用户输入邮箱密码
        │
        ▼
2. 服务器验证 → 生成 JWT Token
        │
        ▼
3. 客户端存储 Token (SecureStorage / localStorage)
        │
        ▼
4. 后续请求携带 Token 在 Header:
   Authorization: Bearer <token>
        │
        ▼
5. 服务器中间件验证 Token → 允许/拒绝请求
```

### 7.2.2 数据安全

- **密码加密**: bcrypt 哈希存储
- **传输加密**: HTTPS (TLS 1.2+)
- **Token有效期**: 7天自动过期
- **敏感数据**: 不在本地存储明文密码

## 7.3 性能优化

### 7.3.1 Android优化

```kotlin
// 1. 使用 State 代替 Flow 减少 recomposition
val courses: State<List<Course>> = _courses

// 2. LazyColumn 分页加载
LazyColumn {
    items(courses, key = { it.id }) { course ->
        CourseCard(course)
    }
}

// 3. 图片缓存 (Coil)
AsyncImage(
    model = ImageRequest.Builder(context)
        .data(imageUrl)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .build(),
    ...
)
```

### 7.3.2 Web优化

```typescript
// 1. 代码分割 (React.lazy)
const AIAssistant = React.lazy(() => import('./pages/AI/AI'));

// 2. Service Worker 缓存 (PWA)
VitePWA({
  workbox: {
    globPatterns: ['**/*.{js,css,html,ico,png,svg}'],
    runtimeCaching: [
      {
        urlPattern: /^https:\/\/keling-server\.onrender\.com\/api\/.*/i,
        handler: 'NetworkFirst',
        options: {
          expiration: { maxAgeSeconds: 60 * 60 * 24 }
        }
      }
    ]
  }
})

// 3. 状态持久化 (Zustand)
const useAppStore = create(
  persist(
    (set) => ({
      user: null,
      setUser: (user) => set({ user })
    }),
    { name: 'keling-storage' }
  )
)
```

## 7.4 部署架构

```
┌─────────────────────────────────────────────────────────────┐
│                        生产环境部署                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                    Vercel                            │   │
│  │  ┌─────────────────────────────────────────────┐    │   │
│  │  │  Web Application (React PWA)                │    │   │
│  │  │  - Domain: web-ashy-rho-36.vercel.app       │    │   │
│  │  │  - HTTPS: 自动SSL证书                        │    │   │
│  │  │  - CDN: 全球边缘节点                         │    │   │
│  │  │  - 自动部署: GitHub集成                      │    │   │
│  │  └─────────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────┘   │
│                           │                                 │
│                           │ HTTPS                           │
│                           ▼                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                    Render                            │   │
│  │  ┌─────────────────────────────────────────────┐    │   │
│  │  │  API Server (Node.js + Express)             │    │   │
│  │  │  - Domain: keling-server.onrender.com       │    │   │
│  │  │  - Port: 3001                               │    │   │
│  │  │  - Region: Oregon (US West)                 │    │   │
│  │  └─────────────────────────────────────────────┘    │   │
│  │                                                       │   │
│  │  ┌─────────────────────────────────────────────┐    │   │
│  │  │  PostgreSQL Database                         │    │   │
│  │  │  - Managed by Render                         │    │   │
│  │  │  - Connection pooling                        │    │   │
│  │  │  - Automatic backups                         │    │   │
│  │  └─────────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                 External Services                    │   │
│  │                                                       │   │
│  │  ┌─────────────────┐  ┌─────────────────┐           │   │
│  │  │  DeepSeek API   │  │  GitHub Repo    │           │   │
│  │  │  (AI服务)       │  │  (代码托管)     │           │   │
│  │  └─────────────────┘  └─────────────────┘           │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 7.5 版本历史

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| v1.0.0 | 2026-01 | 初始版本，基础功能 |
| v2.0.0 | 2026-02 | 添加AI助手、知识图谱 |
| v3.0.0 | 2026-03 | 重构为Monorepo架构 |
| v3.0.5 | 2026-03-27 | 数据持久化修复 |
| v3.0.6 | 2026-03-30 | 完整数据持久化、PWA支持 |

## 7.6 未来规划

### 短期目标 (1-2个月)

- [ ] 添加错题本功能
- [ ] 优化AI响应速度
- [ ] 添加学习提醒推送
- [ ] 支持课程分享

### 中期目标 (3-6个月)

- [ ] 社交功能（好友、排行榜）
- [ ] 更多AI模型支持
- [ ] 学习数据分析报告
- [ ] 跨平台桌面应用

### 长期目标 (6-12个月)

- [ ] iOS原生应用
- [ ] 教师管理端
- [ ] 企业版功能
- [ ] AI个性化学习路径

---

# 附录

## A. 项目文件统计

```
总代码行数: ~25,000 行

Android:
├── Kotlin文件: 85个
├── 代码行数: ~15,000行
└── 主要模块:
    ├── ai/: ~3,000行
    ├── ui/: ~8,000行
    ├── data/: ~2,000行
    └── viewmodel/: ~2,000行

Web:
├── TypeScript/TSX文件: 45个
├── 代码行数: ~6,000行
└── 主要模块:
    ├── pages/: ~3,000行
    ├── components/: ~1,500行
    └── services/: ~1,000行

Server:
├── TypeScript文件: 25个
├── 代码行数: ~4,000行
└── 主要模块:
    ├── controllers/: ~2,000行
    ├── routes/: ~800行
    └── middleware/: ~500行
```

## B. 数据库Schema (Prisma)

```prisma
model User {
  id        String   @id @default(uuid())
  email     String   @unique
  password  String
  name      String
  level     Int      @default(1)
  exp       Int      @default(0)
  energy    Int      @default(100)
  crystals  Int      @default(10)
  createdAt DateTime @default(now())
  updatedAt DateTime @updatedAt

  courses       Course[]
  tasks         Task[]
  notes         Note[]
  achievements  UserAchievement[]
  checkInRecords CheckInRecord[]
}

model Course {
  id              String   @id @default(uuid())
  name            String
  code            String?
  teacher         String?
  themeColor      String?
  masteryLevel    Float    @default(0)
  planetStyleIndex Int     @default(0)
  isArchived      Boolean  @default(false)
  userId          String
  user            User     @relation(fields: [userId], references: [id])

  tasks           Task[]
  knowledgeNodes  KnowledgeNode[]
  createdAt       DateTime @default(now())
  updatedAt       DateTime @updatedAt
}

// ... 更多模型定义
```

## C. 联系方式

- **项目仓库**: GitHub (私有)
- **在线体验**: https://web-ashy-rho-36.vercel.app
- **技术支持**: 通过GitHub Issues

---

*文档版本: 1.0*
*最后更新: 2026-03-30*
*作者: 课灵开发团队*