import { Request, Response } from 'express';
import { prisma } from '../index';
import { AuthRequest } from '../middleware/auth';

// 获取用户资料
export const getProfile = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const user = await prisma.user.findUnique({
      where: { id: req.userId },
      select: {
        id: true,
        name: true,
        email: true,
        level: true,
        exp: true,
        energy: true,
        crystals: true,
        streakDays: true,
        totalStudyMinutes: true,
        lastCheckInDate: true,
        avatarUrl: true,
        bio: true,
        weeklyStudyGoal: true,
        createdAt: true,
        courses: {
          where: { isArchived: false },
          select: {
            id: true,
            name: true,
            masteryLevel: true,
            themeColor: true,
            planetStyleIndex: true
          }
        },
        _count: {
          select: {
            tasks: { where: { status: 'COMPLETED' } },
            notes: true
          }
        }
      }
    });

    if (!user) {
      return res.status(404).json({ error: '用户不存在' });
    }

    res.json({ user });
  } catch (error) {
    console.error('Get profile error:', error);
    res.status(500).json({ error: '获取资料失败' });
  }
};

// 更新用户资料
export const updateProfile = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const { name, bio, avatarUrl, weeklyStudyGoal } = req.body;

    const user = await prisma.user.update({
      where: { id: req.userId },
      data: {
        ...(name && { name }),
        ...(bio !== undefined && { bio }),
        ...(avatarUrl && { avatarUrl }),
        ...(weeklyStudyGoal && { weeklyStudyGoal })
      }
    });

    res.json({
      message: '更新成功',
      user: {
        id: user.id,
        name: user.name,
        bio: user.bio,
        avatarUrl: user.avatarUrl,
        weeklyStudyGoal: user.weeklyStudyGoal
      }
    });
  } catch (error) {
    console.error('Update profile error:', error);
    res.status(500).json({ error: '更新失败' });
  }
};

// 更新能量
export const updateEnergy = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const { energy, change } = req.body;

    if (energy !== undefined) {
      await prisma.user.update({
        where: { id: req.userId },
        data: { energy: Math.max(0, Math.min(200, energy)) }
      });
    } else if (change !== undefined) {
      await prisma.$executeRaw`
        UPDATE users SET energy = MAX(0, MIN(200, energy + ${change}))
        WHERE id = ${req.userId}
      `;
    }

    const user = await prisma.user.findUnique({
      where: { id: req.userId },
      select: { energy: true }
    });

    res.json({ energy: user?.energy });
  } catch (error) {
    console.error('Update energy error:', error);
    res.status(500).json({ error: '更新能量失败' });
  }
};

// 更新结晶
export const updateCrystals = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const { crystals, change } = req.body;

    if (crystals !== undefined) {
      await prisma.user.update({
        where: { id: req.userId },
        data: { crystals: Math.max(0, crystals) }
      });
    } else if (change !== undefined) {
      await prisma.$executeRaw`
        UPDATE users SET crystals = MAX(0, crystals + ${change})
        WHERE id = ${req.userId}
      `;
    }

    const user = await prisma.user.findUnique({
      where: { id: req.userId },
      select: { crystals: true }
    });

    res.json({ crystals: user?.crystals });
  } catch (error) {
    console.error('Update crystals error:', error);
    res.status(500).json({ error: '更新结晶失败' });
  }
};