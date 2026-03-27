import { Request, Response } from 'express';
import { prisma } from '../index';
import { AuthRequest } from '../middleware/auth';
import { v4 as uuidv4 } from 'uuid';

// 获取所有课程
export const getCourses = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const courses = await prisma.course.findMany({
      where: {
        userId: req.userId,
        isArchived: false
      },
      orderBy: { updatedAt: 'desc' }
    });

    // 单独获取知识节点数量
    const coursesWithCount = await Promise.all(
      courses.map(async (c) => {
        const nodeCount = await prisma.knowledgeNode.count({
          where: { courseId: c.id }
        });
        return {
          ...c,
          schedule: JSON.parse(c.schedule),
          knowledgeNodeCount: nodeCount
        };
      })
    );

    res.json({ courses: coursesWithCount });
  } catch (error) {
    console.error('Get courses error:', error);
    res.status(500).json({ error: '获取课程失败' });
  }
};

// 获取单个课程
export const getCourse = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const courseId = String(req.params.id);

    const course = await prisma.course.findFirst({
      where: {
        id: courseId,
        userId: req.userId
      }
    });

    if (!course) {
      return res.status(404).json({ error: '课程不存在' });
    }

    // 获取知识节点
    const nodes = await prisma.knowledgeNode.findMany({
      where: { courseId: course.id }
    });

    res.json({
      course: {
        ...course,
        schedule: JSON.parse(course.schedule),
        knowledgeNodes: nodes.map(n => ({
          ...n,
          parentIds: JSON.parse(n.parentIds),
          childIds: JSON.parse(n.childIds)
        }))
      }
    });
  } catch (error) {
    console.error('Get course error:', error);
    res.status(500).json({ error: '获取课程失败' });
  }
};

// 创建课程
export const createCourse = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const { name, code, teacher, schedule, location, themeColor, semester, credit, examDate } = req.body;

    if (!name) {
      return res.status(400).json({ error: '课程名称为必填项' });
    }

    const course = await prisma.course.create({
      data: {
        id: uuidv4(),
        userId: req.userId,
        name,
        code: code || '',
        teacher: teacher || '',
        schedule: JSON.stringify(schedule || []),
        location: location || '',
        themeColor: themeColor || '#E8A87C',
        semester: semester || null,
        credit: credit || 0,
        examDate: examDate ? new Date(examDate) : null
      }
    });

    // 更新成就：首次创建课程
    await updateAchievement(req.userId, 'first_course', 1);

    res.status(201).json({
      message: '创建成功',
      course: {
        ...course,
        schedule: JSON.parse(course.schedule)
      }
    });
  } catch (error) {
    console.error('Create course error:', error);
    res.status(500).json({ error: '创建课程失败' });
  }
};

// 更新课程
export const updateCourse = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const courseId = String(req.params.id);
    const { name, code, teacher, schedule, location, themeColor, masteryLevel, isArchived } = req.body;

    // 检查课程归属
    const existing = await prisma.course.findFirst({
      where: { id: courseId, userId: req.userId }
    });

    if (!existing) {
      return res.status(404).json({ error: '课程不存在' });
    }

    const updateData: any = {};
    if (name) updateData.name = name;
    if (code !== undefined) updateData.code = code;
    if (teacher !== undefined) updateData.teacher = teacher;
    if (schedule) updateData.schedule = JSON.stringify(schedule);
    if (location !== undefined) updateData.location = location;
    if (themeColor) updateData.themeColor = themeColor;
    if (masteryLevel !== undefined) updateData.masteryLevel = masteryLevel;
    if (isArchived !== undefined) updateData.isArchived = isArchived;

    const course = await prisma.course.update({
      where: { id: courseId },
      data: updateData
    });

    // 检查掌握度成就
    if (masteryLevel !== undefined && masteryLevel >= 0.8) {
      await updateAchievement(req.userId, 'mastery_80', 1);
    }

    res.json({
      message: '更新成功',
      course: {
        ...course,
        schedule: JSON.parse(course.schedule)
      }
    });
  } catch (error) {
    console.error('Update course error:', error);
    res.status(500).json({ error: '更新课程失败' });
  }
};

// 删除课程
export const deleteCourse = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const courseId = String(req.params.id);

    // 检查课程归属
    const existing = await prisma.course.findFirst({
      where: { id: courseId, userId: req.userId }
    });

    if (!existing) {
      return res.status(404).json({ error: '课程不存在' });
    }

    await prisma.course.delete({
      where: { id: courseId }
    });

    res.json({ message: '删除成功' });
  } catch (error) {
    console.error('Delete course error:', error);
    res.status(500).json({ error: '删除课程失败' });
  }
};

// 更新掌握度
export const updateMastery = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const courseId = String(req.params.id);
    const { masteryLevel, studyMinutes } = req.body;

    const existing = await prisma.course.findFirst({
      where: { id: courseId, userId: req.userId }
    });

    if (!existing) {
      return res.status(404).json({ error: '课程不存在' });
    }

    const updateData: any = {};
    if (masteryLevel !== undefined) {
      updateData.masteryLevel = masteryLevel;
    }
    if (studyMinutes !== undefined) {
      updateData.totalStudyMinutes = existing.totalStudyMinutes + studyMinutes;
      updateData.lastStudiedAt = new Date();
      updateData.studySessionCount = existing.studySessionCount + 1;
    }

    const course = await prisma.course.update({
      where: { id: courseId },
      data: updateData
    });

    // 更新用户总学习时长
    if (studyMinutes) {
      await prisma.user.update({
        where: { id: req.userId },
        data: {
          totalStudyMinutes: {
            increment: studyMinutes
          }
        }
      });
    }

    res.json({
      message: '更新成功',
      course: {
        masteryLevel: course.masteryLevel,
        totalStudyMinutes: course.totalStudyMinutes,
        lastStudiedAt: course.lastStudiedAt
      }
    });
  } catch (error) {
    console.error('Update mastery error:', error);
    res.status(500).json({ error: '更新掌握度失败' });
  }
};

// 辅助函数：更新成就进度
async function updateAchievement(userId: string, achievementId: string, progress: number) {
  try {
    const ua = await prisma.userAchievement.findUnique({
      where: {
        userId_achievementId: { userId, achievementId }
      }
    });

    if (ua) {
      await prisma.userAchievement.update({
        where: { id: ua.id },
        data: {
          progress: ua.progress + progress,
          isUnlocked: ua.progress + progress >= 1,
          unlockedAt: ua.progress + progress >= 1 ? new Date() : null
        }
      });
    }
  } catch (error) {
    console.error('Update achievement error:', error);
  }
}