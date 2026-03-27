# 课灵 KeLing

> 🌱 多端知识管理应用 - 培育你的知识星球

## 📱 项目结构

```
KeLing/
├── apps/
│   ├── android/          # Android 移动端 (Kotlin + Jetpack Compose)
│   ├── web/              # Web 网页端 (React + TypeScript + Vite)
│   └── server/           # 后端服务器 (Node.js + Express + Prisma)
├── packages/
│   └── shared/           # 共享类型定义
├── start.bat             # Windows 启动脚本
└── package.json          # Monorepo 配置
```

## 🚀 快速开始

### Windows 用户

双击 `start.bat` 文件，选择要启动的服务。

### 手动启动

```bash
# 安装依赖
npm run install:all

# 同时启动后端和前端
npm run dev

# 或单独启动
npm run dev:server  # 后端: http://localhost:3001
npm run dev:web     # 前端: http://localhost:5173
```

### Android 开发

1. 使用 Android Studio 打开 `apps/android` 目录
2. 等待 Gradle 同步完成
3. 点击 Run 按钮启动

## 🛠 技术栈

### 移动端 (Android)
- Kotlin + Jetpack Compose
- Material Design 3
- DataStore (本地存储)
- Ktor (网络请求)
- MVVM 架构

### 网页端 (Web)
- React 18 + TypeScript
- Vite (构建工具)
- Zustand (状态管理)
- Framer Motion (动画)
- React Router (路由)

### 后端 (Server)
- Node.js + Express
- Prisma ORM
- SQLite 数据库
- JWT 认证
- RESTful API

## 📡 API 端点

| 端点 | 说明 |
|------|------|
| `POST /api/auth/register` | 用户注册 |
| `POST /api/auth/login` | 用户登录 |
| `GET /api/auth/me` | 获取当前用户 |
| `GET /api/courses` | 获取课程列表 |
| `POST /api/courses` | 创建课程 |
| `GET /api/tasks` | 获取任务列表 |
| `POST /api/tasks` | 创建任务 |
| `POST /api/checkin` | 每日签到 |

## 🔄 数据同步

移动端和网页端共享同一套后端 API，用户数据实时同步：

- ✅ 用户认证 (JWT Token)
- ✅ 课程管理
- ✅ 任务系统
- ✅ 笔记同步
- ✅ 签到记录

## 📝 开发说明

### 环境要求
- Node.js >= 18
- Android Studio (用于移动端开发)
- JDK 17+

### 配置文件

- `apps/server/.env` - 后端环境变量
- `apps/android/app/src/main/java/com/keling/app/network/ApiClient.kt` - 移动端 API 配置

### API 地址配置

| 环境 | Web | Android 模拟器 | Android 真机 |
|------|-----|---------------|-------------|
| 开发 | localhost:3001 | 10.0.2.2:3001 | 局域网IP:3001 |

## 📄 License

MIT License