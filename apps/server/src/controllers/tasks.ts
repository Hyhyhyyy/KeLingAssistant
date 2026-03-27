import { Request, Response } from 'express';
import { prisma } from '../index';
import { AuthRequest } from '../middleware/auth';
import { v4 as uuidv4 } from 'uuid';

// 获取所有任务
export const getTasks = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const { status, courseId } = req.query;

    const where: any = { userId: req.userId };
    if (status) where.status = String(status);
    if (courseId) where.courseId = String(courseId);

    const tasks = await prisma.task.findMany({
      where,
      orderBy: [
        { priority: 'desc' },
        { createdAt: 'desc' }
      ]
    });

    res.json({
      tasks: tasks.map(t => ({
        ...t,
        knowledgeNodeIds: JSON.parse(t.knowledgeNodeIds),
        rewards: {
          energy: t.rewardsEnergy,
          crystals: t.rewardsCrystals,
          exp: t.rewardsExp
        }
      }))
    });
  } catch (error) {
    console.error('Get tasks error:', error);
    res.status(500).json({ error: '获取任务失败' });
  }
};

// 获取单个任务
export const getTask = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const taskId = String(req.params.id);

    const task = await prisma.task.findFirst({
      where: { id: taskId, userId: req.userId }
    });

    if (!task) {
      return res.status(404).json({ error: '任务不存在' });
    }

    res.json({
      task: {
        ...task,
        knowledgeNodeIds: JSON.parse(task.knowledgeNodeIds),
        rewards: {
          energy: task.rewardsEnergy,
          crystals: task.rewardsCrystals,
          exp: task.rewardsExp
        }
      }
    });
  } catch (error) {
    console.error('Get task error:', error);
    res.status(500).json({ error: '获取任务失败' });
  }
};

// 创建任务
export const createTask = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const {
      title,
      description,
      type,
      courseId,
      knowledgeNodeIds,
      priority,
      estimatedMinutes,
      rewards,
      scheduledAt
    } = req.body;

    if (!title) {
      return res.status(400).json({ error: '任务标题为必填项' });
    }

    const task = await prisma.task.create({
      data: {
        id: uuidv4(),
        userId: req.userId,
        title,
        description: description || '',
        type: type || 'DAILY_CARE',
        courseId: courseId || null,
        knowledgeNodeIds: JSON.stringify(knowledgeNodeIds || []),
        status: 'PENDING',
        priority: priority || 3,
        estimatedMinutes: estimatedMinutes || 25,
        rewardsEnergy: rewards?.energy || 10,
        rewardsCrystals: rewards?.crystals || 5,
        rewardsExp: rewards?.exp || 20,
        scheduledAt: scheduledAt ? new Date(scheduledAt) : null
      }
    });

    res.status(201).json({
      message: '创建成功',
      task: {
        ...task,
        knowledgeNodeIds: JSON.parse(task.knowledgeNodeIds),
        rewards: {
          energy: task.rewardsEnergy,
          crystals: task.rewardsCrystals,
          exp: task.rewardsExp
        }
      }
    });
  } catch (error) {
    console.error('Create task error:', error);
    res.status(500).json({ error: '创建任务失败' });
  }
};

// 更新任务
export const updateTask = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const taskId = String(req.params.id);
    const { title, description, status, priority, actualMinutes } = req.body;

    const existing = await prisma.task.findFirst({
      where: { id: taskId, userId: req.userId }
    });

    if (!existing) {
      return res.status(404).json({ error: '任务不存在' });
    }

    const updateData: any = {};
    if (title) updateData.title = title;
    if (description !== undefined) updateData.description = description;
    if (status) updateData.status = status;
    if (priority !== undefined) updateData.priority = priority;
    if (actualMinutes !== undefined) updateData.actualMinutes = actualMinutes;

    const task = await prisma.task.update({
      where: { id: taskId },
      data: updateData
    });

    res.json({
      message: '更新成功',
      task: {
        ...task,
        knowledgeNodeIds: JSON.parse(task.knowledgeNodeIds),
        rewards: {
          energy: task.rewardsEnergy,
          crystals: task.rewardsCrystals,
          exp: task.rewardsExp
        }
      }
    });
  } catch (error) {
    console.error('Update task error:', error);
    res.status(500).json({ error: '更新任务失败' });
  }
};

// 完成任务
export const completeTask = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const taskId = String(req.params.id);
    const { actualMinutes } = req.body;

    const existing = await prisma.task.findFirst({
      where: { id: taskId, userId: req.userId }
    });

    if (!existing) {
      return res.status(404).json({ error: '任务不存在' });
    }

    if (existing.status === 'COMPLETED') {
      return res.status(400).json({ error: '任务已完成' });
    }

    // 更新任务状态
    const task = await prisma.task.update({
      where: { id: taskId },
      data: {
        status: 'COMPLETED',
        actualMinutes: actualMinutes || existing.estimatedMinutes,
        completedAt: new Date()
      }
    });

    // 发放奖励
    await prisma.user.update({
      where: { id: req.userId },
      data: {
        energy: { increment: existing.rewardsEnergy },
        crystals: { increment: existing.rewardsCrystals },
        exp: { increment: existing.rewardsExp }
      }
    });

    // 检查等级提升
    const user = await prisma.user.findUnique({
      where: { id: req.userId }
    });

    if (user) {
      const expNeeded = user.level * 100;
      if (user.exp >= expNeeded) {
        await prisma.user.update({
          where: { id: req.userId },
          data: {
            level: { increment: 1 },
            exp: user.exp - expNeeded
          }
        });
      }
    }

    // 更新成就
    await updateTaskAchievements(req.userId);

    res.json({
      message: '任务完成！',
      rewards: {
        energy: existing.rewardsEnergy,
        crystals: existing.rewardsCrystals,
        exp: existing.rewardsExp
      },
      task: {
        ...task,
        knowledgeNodeIds: JSON.parse(task.knowledgeNodeIds)
      }
    });
  } catch (error) {
    console.error('Complete task error:', error);
    res.status(500).json({ error: '完成任务失败' });
  }
};

// 删除任务
export const deleteTask = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const taskId = String(req.params.id);

    const existing = await prisma.task.findFirst({
      where: { id: taskId, userId: req.userId }
    });

    if (!existing) {
      return res.status(404).json({ error: '任务不存在' });
    }

    await prisma.task.delete({
      where: { id: taskId }
    });

    res.json({ message: '删除成功' });
  } catch (error) {
    console.error('Delete task error:', error);
    res.status(500).json({ error: '删除任务失败' });
  }
};

// 辅助函数：更新任务相关成就
async function updateTaskAchievements(userId: string) {
  try {
    // 获取已完成任务数
    const completedCount = await prisma.task.count({
      where: { userId, status: 'COMPLETED' }
    });

    // 更新成就进度
    const achievements = [
      { id: 'first_task', target: 1 },
      { id: 'task_master_10', target: 10 },
      { id: 'task_master_50', target: 50 }
    ];

    for (const a of achievements) {
      const ua = await prisma.userAchievement.findUnique({
        where: { userId_achievementId: { userId, achievementId: a.id } }
      });

      if (ua && !ua.isUnlocked) {
        await prisma.userAchievement.update({
          where: { id: ua.id },
          data: {
            progress: Math.min(completedCount, a.target),
            isUnlocked: completedCount >= a.target,
            unlockedAt: completedCount >= a.target ? new Date() : null
          }
        });
      }
    }
  } catch (error) {
    console.error('Update task achievements error:', error);
  }
}