import { Request, Response } from 'express';
import { prisma } from '../index';
import { AuthRequest } from '../middleware/auth';
import { v4 as uuidv4 } from 'uuid';

// 获取所有笔记
export const getNotes = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const { tag, sourceType } = req.query;

    const where: any = { userId: req.userId };
    // SQLite 不支持 JSON 查询，这里简化处理

    const notes = await prisma.note.findMany({
      where,
      orderBy: { updatedAt: 'desc' }
    });

    res.json({
      notes: notes.map(n => ({
        ...n,
        relatedNodeIds: JSON.parse(n.relatedNodeIds),
        tags: JSON.parse(n.tags)
      }))
    });
  } catch (error) {
    console.error('Get notes error:', error);
    res.status(500).json({ error: '获取笔记失败' });
  }
};

// 创建笔记
export const createNote = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const { title, content, sourceType, aiExplanation, relatedNodeIds, tags } = req.body;

    if (!title || !content) {
      return res.status(400).json({ error: '标题和内容为必填项' });
    }

    const note = await prisma.note.create({
      data: {
        id: uuidv4(),
        userId: req.userId,
        title,
        content,
        sourceType: sourceType || 'USER_CREATED',
        aiExplanation,
        relatedNodeIds: JSON.stringify(relatedNodeIds || []),
        tags: JSON.stringify(tags || [])
      }
    });

    // 更新成就
    await updateNoteAchievement(req.userId);

    res.status(201).json({
      message: '创建成功',
      note: {
        ...note,
        relatedNodeIds: JSON.parse(note.relatedNodeIds),
        tags: JSON.parse(note.tags)
      }
    });
  } catch (error) {
    console.error('Create note error:', error);
    res.status(500).json({ error: '创建笔记失败' });
  }
};

// 更新笔记
export const updateNote = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const noteId = String(req.params.id);
    const { title, content, tags, reviewCount } = req.body;

    const existing = await prisma.note.findFirst({
      where: { id: noteId, userId: req.userId }
    });

    if (!existing) {
      return res.status(404).json({ error: '笔记不存在' });
    }

    const note = await prisma.note.update({
      where: { id: noteId },
      data: {
        ...(title && { title }),
        ...(content && { content }),
        ...(tags && { tags: JSON.stringify(tags) }),
        ...(reviewCount !== undefined && { reviewCount }),
        updatedAt: new Date()
      }
    });

    res.json({
      message: '更新成功',
      note: {
        ...note,
        relatedNodeIds: JSON.parse(note.relatedNodeIds),
        tags: JSON.parse(note.tags)
      }
    });
  } catch (error) {
    console.error('Update note error:', error);
    res.status(500).json({ error: '更新笔记失败' });
  }
};

// 删除笔记
export const deleteNote = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const noteId = String(req.params.id);

    const existing = await prisma.note.findFirst({
      where: { id: noteId, userId: req.userId }
    });

    if (!existing) {
      return res.status(404).json({ error: '笔记不存在' });
    }

    await prisma.note.delete({
      where: { id: noteId }
    });

    res.json({ message: '删除成功' });
  } catch (error) {
    console.error('Delete note error:', error);
    res.status(500).json({ error: '删除笔记失败' });
  }
};

// 辅助函数：更新笔记成就
async function updateNoteAchievement(userId: string) {
  try {
    const count = await prisma.note.count({
      where: { userId }
    });

    const ua = await prisma.userAchievement.findUnique({
      where: { userId_achievementId: { userId, achievementId: 'notes_5' } }
    });

    if (ua && !ua.isUnlocked) {
      await prisma.userAchievement.update({
        where: { id: ua.id },
        data: {
          progress: Math.min(count, 5),
          isUnlocked: count >= 5,
          unlockedAt: count >= 5 ? new Date() : null
        }
      });
    }
  } catch (error) {
    console.error('Update note achievement error:', error);
  }
}