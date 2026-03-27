import { Request, Response } from 'express';
import { prisma } from '../index';
import { AuthRequest } from '../middleware/auth';
import { v4 as uuidv4 } from 'uuid';

// 签到奖励配置（对应 Android CHECK_IN_REWARDS）
const CHECK_IN_REWARDS = [
  { day: 1, energy: 10, crystals: 5 },
  { day: 2, energy: 15, crystals: 8 },
  { day: 3, energy: 20, crystals: 10 },
  { day: 4, energy: 25, crystals: 12 },
  { day: 5, energy: 30, crystals: 15 },
  { day: 6, energy: 40, crystals: 20 },
  { day: 7, energy: 50, crystals: 30, isSpecial: true, specialReward: '周奖励已解锁！' }
];

// 获取今日日期字符串
function getTodayString(): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}${month}${day}`;
}

// 签到
export const checkIn = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const today = getTodayString();

    // 检查今日是否已签到
    const existing = await prisma.checkInRecord.findUnique({
      where: {
        userId_date: { userId: req.userId, date: today }
      }
    });

    if (existing) {
      return res.status(400).json({ error: '今日已签到' });
    }

    // 获取用户信息
    const user = await prisma.user.findUnique({
      where: { id: req.userId }
    });

    if (!user) {
      return res.status(404).json({ error: '用户不存在' });
    }

    // 计算连续签到天数
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    const yesterdayStr = `${yesterday.getFullYear()}${String(yesterday.getMonth() + 1).padStart(2, '0')}${String(yesterday.getDate()).padStart(2, '0')}`;

    const yesterdayRecord = await prisma.checkInRecord.findUnique({
      where: {
        userId_date: { userId: req.userId, date: yesterdayStr }
      }
    });

    let newStreakDays = 1;
    if (yesterdayRecord || user.lastCheckInDate === yesterdayStr) {
      newStreakDays = user.streakDays + 1;
    }

    // 获取今日奖励
    const dayInCycle = ((newStreakDays - 1) % 7) + 1;
    const reward = CHECK_IN_REWARDS[dayInCycle - 1];

    // 创建签到记录
    await prisma.checkInRecord.create({
      data: {
        id: uuidv4(),
        userId: req.userId,
        date: today,
        rewardReceived: true
      }
    });

    // 更新用户数据
    await prisma.user.update({
      where: { id: req.userId },
      data: {
        lastCheckInDate: today,
        streakDays: newStreakDays,
        energy: { increment: reward.energy },
        crystals: { increment: reward.crystals }
      }
    });

    // 更新连续签到成就
    await updateStreakAchievements(req.userId, newStreakDays);

    res.json({
      message: '签到成功！',
      rewards: {
        energy: reward.energy,
        crystals: reward.crystals
      },
      streakDays: newStreakDays,
      dayInCycle,
      isSpecial: reward.isSpecial,
      specialReward: reward.specialReward
    });
  } catch (error) {
    console.error('Check in error:', error);
    res.status(500).json({ error: '签到失败' });
  }
};

// 获取签到记录
export const getCheckInRecords = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const records = await prisma.checkInRecord.findMany({
      where: { userId: req.userId },
      orderBy: { createdAt: 'desc' },
      take: 30
    });

    res.json({ records });
  } catch (error) {
    console.error('Get check-in records error:', error);
    res.status(500).json({ error: '获取签到记录失败' });
  }
};

// 获取签到状态
export const getCheckInStatus = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const today = getTodayString();
    const user = await prisma.user.findUnique({
      where: { id: req.userId },
      select: { streakDays: true, lastCheckInDate: true }
    });

    if (!user) {
      return res.status(404).json({ error: '用户不存在' });
    }

    const todayRecord = await prisma.checkInRecord.findUnique({
      where: {
        userId_date: { userId: req.userId, date: today }
      }
    });

    const dayInCycle = ((user.streakDays) % 7) + 1;
    const nextReward = CHECK_IN_REWARDS[dayInCycle - 1];

    res.json({
      hasCheckedInToday: !!todayRecord,
      streakDays: user.streakDays,
      lastCheckInDate: user.lastCheckInDate,
      nextReward: {
        day: dayInCycle,
        energy: nextReward.energy,
        crystals: nextReward.crystals
      }
    });
  } catch (error) {
    console.error('Get check-in status error:', error);
    res.status(500).json({ error: '获取签到状态失败' });
  }
};

// 辅助函数：更新连续签到成就
async function updateStreakAchievements(userId: string, streakDays: number) {
  try {
    const achievements = [
      { id: 'streak_3', target: 3 },
      { id: 'streak_7', target: 7 },
      { id: 'streak_30', target: 30 }
    ];

    for (const a of achievements) {
      const ua = await prisma.userAchievement.findUnique({
        where: { userId_achievementId: { userId, achievementId: a.id } }
      });

      if (ua && !ua.isUnlocked) {
        await prisma.userAchievement.update({
          where: { id: ua.id },
          data: {
            progress: Math.min(streakDays, a.target),
            isUnlocked: streakDays >= a.target,
            unlockedAt: streakDays >= a.target ? new Date() : null
          }
        });
      }
    }
  } catch (error) {
    console.error('Update streak achievements error:', error);
  }
}