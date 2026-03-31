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

// GitHub仓库配置
const GITHUB_REPO_OWNER = 'Hyhyhyyy';
const GITHUB_REPO_NAME = 'KeLingAssistant';

// 缓存版本信息，避免频繁请求GitHub API
let cachedVersionInfo: any = null;
let cacheTimestamp = 0;
const CACHE_DURATION = 5 * 60 * 1000; // 5分钟缓存

// 从GitHub API获取最新Release信息
async function fetchLatestRelease() {
  const now = Date.now();

  // 使用缓存
  if (cachedVersionInfo && (now - cacheTimestamp) < CACHE_DURATION) {
    return cachedVersionInfo;
  }

  try {
    const response = await fetch(
      `https://api.github.com/repos/${GITHUB_REPO_OWNER}/${GITHUB_REPO_NAME}/releases/latest`,
      {
        headers: {
          'Accept': 'application/vnd.github.v3+json',
          'User-Agent': 'KeLing-Server'
        }
      }
    );

    if (!response.ok) {
      throw new Error(`GitHub API error: ${response.status}`);
    }

    const release = await response.json();

    // 解析版本信息
    const versionName = release.tag_name.replace('v', '');

    // 从APK文件名解析versionCode，或使用默认值
    const apkAsset = release.assets?.find((a: any) => a.name.endsWith('.apk'));
    const downloadUrl = apkAsset?.browser_download_url ||
      `https://github.com/${GITHUB_REPO_OWNER}/${GITHUB_REPO_NAME}/releases/download/${release.tag_name}/KeLing-${release.tag_name}.apk`;

    // 版本代码从版本名解析 (3.0.8 -> 308)
    const versionCode = parseInt(versionName.replace(/\./g, '')) || 7;
    const fileSize = apkAsset?.size || 50 * 1024 * 1024;

    cachedVersionInfo = {
      versionCode,
      versionName,
      minVersionCode: 1,
      updateUrl: downloadUrl,
      updateLog: release.body || generateDefaultChangelog(versionName),
      forceUpdate: false,
      fileSize,
      releaseDate: release.published_at,
      htmlUrl: release.html_url
    };

    cacheTimestamp = now;
    return cachedVersionInfo;
  } catch (error) {
    console.error('Failed to fetch GitHub release:', error);

    // 返回默认版本信息
    return getDefaultVersionInfo();
  }
}

// 默认版本信息（当GitHub API不可用时）
function getDefaultVersionInfo() {
  return {
    versionCode: 7,
    versionName: '3.0.8',
    minVersionCode: 1,
    updateUrl: `https://github.com/${GITHUB_REPO_OWNER}/${GITHUB_REPO_NAME}/releases/latest`,
    updateLog: generateDefaultChangelog('3.0.8'),
    forceUpdate: false,
    fileSize: 50 * 1024 * 1024
  };
}

// 生成默认更新日志
function generateDefaultChangelog(version: string) {
  return `【课灵 ${version} 更新内容】
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
🔧 修复底部导航栏与页面协调问题`;
}

// 版本检查接口
app.get('/api/app/version', async (req, res) => {
  const clientVersionCode = parseInt(req.query.versionCode as string) || 0;
  const clientVersionName = req.query.versionName as string || '0.0.0';

  try {
    const latestVersion = await fetchLatestRelease();

    // 判断是否需要更新
    const needUpdate = clientVersionCode < latestVersion.versionCode;
    const forceUpdate = clientVersionCode < latestVersion.minVersionCode;

    res.json({
      success: true,
      currentVersion: {
        versionCode: clientVersionCode,
        versionName: clientVersionName
      },
      latestVersion: {
        versionCode: latestVersion.versionCode,
        versionName: latestVersion.versionName,
        updateUrl: latestVersion.updateUrl,
        updateLog: latestVersion.updateLog,
        fileSize: latestVersion.fileSize
      },
      needUpdate,
      forceUpdate
    });
  } catch (error) {
    res.json({
      success: false,
      error: 'Failed to check version',
      currentVersion: {
        versionCode: clientVersionCode,
        versionName: clientVersionName
      }
    });
  }
});

// APK下载接口（重定向到GitHub Release）
app.get('/api/app/download', async (req, res) => {
  try {
    const latestVersion = await fetchLatestRelease();
    res.redirect(latestVersion.updateUrl);
  } catch (error) {
    // 重定向到GitHub Releases页面
    res.redirect(`https://github.com/${GITHUB_REPO_OWNER}/${GITHUB_REPO_NAME}/releases/latest`);
  }
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