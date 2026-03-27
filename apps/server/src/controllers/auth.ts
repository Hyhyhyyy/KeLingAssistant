import { Request, Response } from 'express';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { v4 as uuidv4 } from 'uuid';
import { prisma } from '../index';
import { AuthRequest } from '../middleware/auth';

// JWT 过期时间
const JWT_EXPIRES_IN = '7d';

// 注册
export const register = async (req: Request, res: Response) => {
  try {
    const { name, email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({ error: '邮箱和密码为必填项' });
    }

    // 检查用户是否已存在
    const existingUser = await prisma.user.findUnique({
      where: { email }
    });

    if (existingUser) {
      return res.status(409).json({ error: '该邮箱已被注册' });
    }

    // 密码加密
    const passwordHash = await bcrypt.hash(password, 10);

    // 创建用户
    const user = await prisma.user.create({
      data: {
        id: uuidv4(),
        name: name || '星际园丁',
        email,
        passwordHash,
        level: 1,
        exp: 0,
        energy: 100,
        crystals: 10,
        streakDays: 0,
        totalStudyMinutes: 0
      }
    });

    // 初始化用户成就
    const achievements = await prisma.achievement.findMany();
    if (achievements.length > 0) {
      await prisma.userAchievement.createMany({
        data: achievements.map(a => ({
          id: uuidv4(),
          userId: user.id,
          achievementId: a.id,
          isUnlocked: false,
          progress: 0
        }))
      });
    }

    // 生成 Token
    const token = jwt.sign(
      { userId: user.id },
      process.env.JWT_SECRET || 'keling_secret_key',
      { expiresIn: JWT_EXPIRES_IN }
    );

    res.status(201).json({
      message: '注册成功',
      token,
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        level: user.level,
        exp: user.exp,
        energy: user.energy,
        crystals: user.crystals,
        streakDays: user.streakDays,
        totalStudyMinutes: user.totalStudyMinutes
      }
    });
  } catch (error) {
    console.error('Register error:', error);
    res.status(500).json({ error: '注册失败，请稍后重试' });
  }
};

// 登录
export const login = async (req: Request, res: Response) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({ error: '邮箱和密码为必填项' });
    }

    // 查找用户
    const user = await prisma.user.findUnique({
      where: { email }
    });

    if (!user) {
      return res.status(401).json({ error: '邮箱或密码错误' });
    }

    // 验证密码
    const isValidPassword = await bcrypt.compare(password, user.passwordHash);

    if (!isValidPassword) {
      return res.status(401).json({ error: '邮箱或密码错误' });
    }

    // 生成 Token
    const token = jwt.sign(
      { userId: user.id },
      process.env.JWT_SECRET || 'keling_secret_key',
      { expiresIn: JWT_EXPIRES_IN }
    );

    res.json({
      message: '登录成功',
      token,
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        level: user.level,
        exp: user.exp,
        energy: user.energy,
        crystals: user.crystals,
        streakDays: user.streakDays,
        totalStudyMinutes: user.totalStudyMinutes,
        lastCheckInDate: user.lastCheckInDate
      }
    });
  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({ error: '登录失败，请稍后重试' });
  }
};

// 刷新 Token
export const refreshToken = async (req: Request, res: Response) => {
  try {
    const authHeader = req.headers.authorization;
    const oldToken = authHeader?.split(' ')[1];

    if (!oldToken) {
      return res.status(401).json({ error: '未提供令牌' });
    }

    // 验证旧 Token（即使过期也尝试解析）
    let decoded;
    try {
      decoded = jwt.verify(oldToken, process.env.JWT_SECRET || 'keling_secret_key') as { userId: string };
    } catch {
      // Token 过期，尝试解码（不验证）
      decoded = jwt.decode(oldToken) as { userId: string };
      if (!decoded) {
        return res.status(401).json({ error: '令牌无效' });
      }
    }

    // 检查用户是否存在
    const user = await prisma.user.findUnique({
      where: { id: decoded.userId }
    });

    if (!user) {
      return res.status(401).json({ error: '用户不存在' });
    }

    // 生成新 Token
    const newToken = jwt.sign(
      { userId: user.id },
      process.env.JWT_SECRET || 'keling_secret_key',
      { expiresIn: JWT_EXPIRES_IN }
    );

    res.json({ token: newToken });
  } catch (error) {
    console.error('Refresh token error:', error);
    res.status(500).json({ error: '令牌刷新失败' });
  }
};

// 获取当前用户信息
export const getMe = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const user = await prisma.user.findUnique({
      where: { id: req.userId }
    });

    if (!user) {
      return res.status(404).json({ error: '用户不存在' });
    }

    // 单独统计
    const courseCount = await prisma.course.count({ where: { userId: req.userId } });
    const completedTaskCount = await prisma.task.count({ where: { userId: req.userId, status: 'COMPLETED' } });
    const noteCount = await prisma.note.count({ where: { userId: req.userId } });

    res.json({
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        level: user.level,
        exp: user.exp,
        energy: user.energy,
        crystals: user.crystals,
        streakDays: user.streakDays,
        totalStudyMinutes: user.totalStudyMinutes,
        lastCheckInDate: user.lastCheckInDate,
        createdAt: user.createdAt,
        stats: {
          totalCourses: courseCount,
          completedTasks: completedTaskCount,
          totalNotes: noteCount
        }
      }
    });
  } catch (error) {
    console.error('Get me error:', error);
    res.status(500).json({ error: '获取用户信息失败' });
  }
};