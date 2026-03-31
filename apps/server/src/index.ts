import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { PrismaClient } from '@prisma/client';

// 导入路由
import authRoutes from './routes/auth';
import userRoutes from './routes/user';
import courseRoutes from './routes/courses';
import taskRoutes from './routes/tasks';
import knowledgeRoutes from './routes/knowledge';
import aiRoutes from './routes/ai';
import noteRoutes from './routes/notes';
import achievementRoutes from './routes/achievements';
import checkinRoutes from './routes/checkin';

dotenv.config();

export const prisma = new PrismaClient();

const app = express();
const PORT = process.env.PORT || 3001;

// 中间件 - 手动设置CORS以确保正确
app.use((req, res, next) => {
  const allowedOrigins = ['http://localhost:5173', 'http://localhost:5174', 'http://localhost:5175', 'http://localhost:5176', 'http://localhost:3000', 'https://web-ashy-rho-36.vercel.app'];
  const origin = req.headers.origin;
  if (origin && allowedOrigins.includes(origin)) {
    res.setHeader('Access-Control-Allow-Origin', origin);
  } else {
    res.setHeader('Access-Control-Allow-Origin', '*');
  }
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, PATCH, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');
  res.setHeader('Access-Control-Allow-Credentials', 'true');
  if (req.method === 'OPTIONS') {
    return res.sendStatus(200);
  }
  next();
});
app.use(express.json({ limit: '10mb' }));

// API 路由
app.use('/api/auth', authRoutes);
app.use('/api/user', userRoutes);
app.use('/api/courses', courseRoutes);
app.use('/api/tasks', taskRoutes);
app.use('/api/knowledge', knowledgeRoutes);
app.use('/api/ai', aiRoutes);
app.use('/api/notes', noteRoutes);
app.use('/api/achievements', achievementRoutes);
app.use('/api/checkin', checkinRoutes);

// 健康检查
app.get('/api/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    version: '1.0.0'
  });
});

// ==================== APP版本更新API ====================

// 最新版本信息（可通过环境变量或数据库配置）
const APP_VERSION = {
  versionCode: 7,        // 当前最新版本号
  versionName: '3.0.8',  // 当前版本名
  minVersionCode: 1,     // 最低支持版本
  updateUrl: 'https://keling-server.onrender.com/api/app/download',  // APK下载地址
  updateLog: `【课灵 3.0.8 更新内容】
✨ 新增数据本地持久化，退出APP数据不丢失
✨ 知识图谱支持可拖动编辑、曲线箭头连接
✨ 笔记编辑器支持字体大小、颜色、高亮、分类
✨ 个人中心头像显示用户头像
✨ 固定底部导航栏，优化退出逻辑
✨ 签到日历移至首页
✨ 移动端添加网页端链接，网页端添加移动端下载链接
✨ 优化检查更新功能，增加加载状态和错误提示
🔧 修复星球图像显示问题
🔧 修复数据不保存问题
🔧 修复底部导航栏与页面协调问题`,
  forceUpdate: false,    // 是否强制更新
  fileSize: 50 * 1024 * 1024  // APK文件大小(字节)
};

// 版本检查接口
app.get('/api/app/version', (req, res) => {
  const clientVersionCode = parseInt(req.query.versionCode as string) || 0;
  const clientVersionName = req.query.versionName as string || '0.0.0';

  // 判断是否需要更新
  const needUpdate = clientVersionCode < APP_VERSION.versionCode;
  const forceUpdate = clientVersionCode < APP_VERSION.minVersionCode;

  res.json({
    success: true,
    currentVersion: {
      versionCode: clientVersionCode,
      versionName: clientVersionName
    },
    latestVersion: {
      versionCode: APP_VERSION.versionCode,
      versionName: APP_VERSION.versionName,
      updateUrl: APP_VERSION.updateUrl,
      updateLog: APP_VERSION.updateLog,
      fileSize: APP_VERSION.fileSize
    },
    needUpdate,
    forceUpdate
  });
});

// APK下载接口（重定向到实际文件）
app.get('/api/app/download', (req, res) => {
  // 这里可以重定向到CDN或直接提供文件
  // 目前返回提示信息，实际APK文件需要托管在CDN或服务器上
  res.json({
    message: 'APK下载功能',
    downloadUrl: 'https://github.com/your-repo/releases/latest',  // 可替换为实际下载地址
    note: '实际部署时请将APK文件托管在CDN或对象存储服务'
  });
});

// 错误处理中间件
app.use((err: Error, req: express.Request, res: express.Response, next: express.NextFunction) => {
  console.error('Error:', err.message);
  res.status(500).json({
    error: 'Internal Server Error',
    message: process.env.NODE_ENV === 'development' ? err.message : undefined
  });
});

// 启动服务器
async function startServer() {
  try {
    // 测试数据库连接
    await prisma.$connect();
    console.log('✅ Database connected');

    app.listen(PORT, () => {
      console.log(`🚀 KeLing Server running on http://localhost:${PORT}`);
      console.log(`📚 API Docs: http://localhost:${PORT}/api/health`);
    });
  } catch (error) {
    console.error('❌ Failed to start server:', error);
    process.exit(1);
  }
}

// 优雅关闭
process.on('SIGINT', async () => {
  await prisma.$disconnect();
  console.log('👋 Server shutdown gracefully');
  process.exit(0);
});

startServer();