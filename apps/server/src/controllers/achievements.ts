import { Request, Response } from 'express';
import { prisma } from '../index';
import { AuthRequest } from '../middleware/auth';

// 预定义成就列表（对应 Android PREDEFINED_ACHIEVEMENTS）
const PREDEFINED_ACHIEVEMENTS = [
  { id: 'first_task', name: '初学者', description: '完成第一个学习任务', icon: '🌱', category: 'LEARNING', rewardEnergy: 50, rewardCrystals: 30, maxProgress: 1 },
  { id: 'task_master_10', name: '任务达人', description: '累计完成10个学习任务', icon: '📋', category: 'LEARNING', rewardEnergy: 100, rewardCrystals: 30, maxProgress: 10 },
  { id: 'task_master_50', name: '任务大师', description: '累计完成50个学习任务', icon: '🏆', category: 'LEARNING', rewardEnergy: 300, rewardCrystals: 100, maxProgress: 50 },
  { id: 'first_course', name: '星际探索者', description: '创建第一颗知识星球', icon: '🌍', category: 'EXPLORATION', rewardEnergy: 50, rewardCrystals: 30, maxProgress: 1 },
  { id: 'mastery_80', name: '知识精通', description: '将任意课程掌握度提升到80%以上', icon: '✨', category: 'MASTERY', rewardEnergy: 200, rewardCrystals: 50, maxProgress: 1 },
  { id: 'streak_3', name: '坚持三天', description: '连续学习3天', icon: '🔥', category: 'STREAK', rewardEnergy: 50, rewardCrystals: 30, maxProgress: 3 },
  { id: 'streak_7', name: '周周坚持', description: '连续学习7天', icon: '🌟', category: 'STREAK', rewardEnergy: 150, rewardCrystals: 30, maxProgress: 7 },
  { id: 'streak_30', name: '月度之星', description: '连续学习30天', icon: '💫', category: 'STREAK', rewardEnergy: 500, rewardCrystals: 200, maxProgress: 30 },
  { id: 'knowledge_10', name: '知识收集者', description: '解锁10个知识点', icon: '📚', category: 'EXPLORATION', rewardEnergy: 80, rewardCrystals: 30, maxProgress: 10 },
  { id: 'notes_5', name: '笔记达人', description: '创建5篇学习笔记', icon: '📝', category: 'LEARNING', rewardEnergy: 100, rewardCrystals: 30, maxProgress: 5 }
];

// 初始化成就数据
export async function initAchievements() {
  for (const achievement of PREDEFINED_ACHIEVEMENTS) {
    await prisma.achievement.upsert({
      where: { id: achievement.id },
      create: achievement,
      update: achievement
    });
  }
}

// 获取所有成就定义
export const getAchievements = async (req: AuthRequest, res: Response) => {
  try {
    const achievements = await prisma.achievement.findMany();
    res.json({ achievements });
  } catch (error) {
    console.error('Get achievements error:', error);
    res.status(500).json({ error: '获取成就失败' });
  }
};

// 获取用户成就进度
export const getUserAchievements = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const userAchievements = await prisma.userAchievement.findMany({
      where: { userId: req.userId }
    });

    // 获取所有成就定义
    const achievements = await prisma.achievement.findMany();
    const achievementMap = new Map(achievements.map(a => [a.id, a]));

    // 计算统计数据
    const unlocked = userAchievements.filter(ua => ua.isUnlocked);
    const totalRewards = unlocked.reduce((sum, ua) => {
      const achievement = achievementMap.get(ua.achievementId);
      return sum + (achievement?.rewardEnergy || 0);
    }, 0);

    res.json({
      achievements: userAchievements.map(ua => ({
        ...ua,
        achievement: achievementMap.get(ua.achievementId)
      })),
      stats: {
        total: userAchievements.length,
        unlocked: unlocked.length,
        totalRewards
      }
    });
  } catch (error) {
    console.error('Get user achievements error:', error);
    res.status(500).json({ error: '获取用户成就失败' });
  }
};