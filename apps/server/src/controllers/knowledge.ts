import { Request, Response } from 'express';
import { prisma } from '../index';
import { AuthRequest } from '../middleware/auth';
import { v4 as uuidv4 } from 'uuid';

// 获取课程的所有知识节点
export const getNodes = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const courseId = String(req.params.courseId);

    // 验证课程归属
    const course = await prisma.course.findFirst({
      where: { id: courseId, userId: req.userId }
    });

    if (!course) {
      return res.status(404).json({ error: '课程不存在' });
    }

    const nodes = await prisma.knowledgeNode.findMany({
      where: { courseId: courseId }
    });

    res.json({
      nodes: nodes.map(n => ({
        ...n,
        parentIds: JSON.parse(n.parentIds),
        childIds: JSON.parse(n.childIds)
      }))
    });
  } catch (error) {
    console.error('Get nodes error:', error);
    res.status(500).json({ error: '获取知识节点失败' });
  }
};

// 获取单个知识节点
export const getNode = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const nodeId = String(req.params.id);

    const node = await prisma.knowledgeNode.findUnique({
      where: { id: nodeId }
    });

    if (!node) {
      return res.status(404).json({ error: '知识节点不存在' });
    }

    // 验证课程归属
    const course = await prisma.course.findFirst({
      where: { id: node.courseId, userId: req.userId }
    });

    if (!course) {
      return res.status(404).json({ error: '知识节点不存在' });
    }

    res.json({
      node: {
        ...node,
        parentIds: JSON.parse(node.parentIds),
        childIds: JSON.parse(node.childIds)
      }
    });
  } catch (error) {
    console.error('Get node error:', error);
    res.status(500).json({ error: '获取知识节点失败' });
  }
};

// 创建知识节点
export const createNode = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const courseId = String(req.params.courseId);
    const { name, description, parentIds, childIds, difficulty, positionX, positionY } = req.body;

    if (!name) {
      return res.status(400).json({ error: '知识点名称为必填项' });
    }

    // 验证课程归属
    const course = await prisma.course.findFirst({
      where: { id: courseId, userId: req.userId }
    });

    if (!course) {
      return res.status(404).json({ error: '课程不存在' });
    }

    const node = await prisma.knowledgeNode.create({
      data: {
        id: uuidv4(),
        courseId: courseId,
        name,
        description: description || '',
        parentIds: JSON.stringify(parentIds || []),
        childIds: JSON.stringify(childIds || []),
        difficulty: difficulty || 3,
        positionX: positionX || 0.5,
        positionY: positionY || 0.5,
        isUnlocked: false
      }
    });

    // 更新成就
    await updateKnowledgeAchievement(req.userId);

    res.status(201).json({
      message: '创建成功',
      node: {
        ...node,
        parentIds: JSON.parse(node.parentIds),
        childIds: JSON.parse(node.childIds)
      }
    });
  } catch (error) {
    console.error('Create node error:', error);
    res.status(500).json({ error: '创建知识节点失败' });
  }
};

// 批量创建知识节点
export const batchCreateNodes = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const courseId = String(req.params.courseId);
    const { nodes } = req.body;

    if (!nodes || !Array.isArray(nodes)) {
      return res.status(400).json({ error: '节点数据格式错误' });
    }

    // 验证课程归属
    const course = await prisma.course.findFirst({
      where: { id: courseId, userId: req.userId }
    });

    if (!course) {
      return res.status(404).json({ error: '课程不存在' });
    }

    // 创建所有节点
    const createdNodes = [];
    const nameToIdMap: Record<string, string> = {};

    // 第一遍：创建节点
    for (const nodeData of nodes) {
      const id = uuidv4();
      nameToIdMap[nodeData.name] = id;

      const node = await prisma.knowledgeNode.create({
        data: {
          id,
          courseId: courseId,
          name: nodeData.name,
          description: nodeData.description || '',
          parentIds: '[]',
          childIds: '[]',
          difficulty: nodeData.difficulty || 3,
          positionX: nodeData.positionX || Math.random(),
          positionY: nodeData.positionY || Math.random(),
          isUnlocked: false
        }
      });
      createdNodes.push(node);
    }

    // 第二遍：更新父子关系
    for (const nodeData of nodes) {
      if (nodeData.parentNames && Array.isArray(nodeData.parentNames)) {
        const parentIdStrings = nodeData.parentNames
          .map((name: string) => nameToIdMap[name])
          .filter(Boolean);

        if (parentIdStrings.length > 0) {
          await prisma.knowledgeNode.update({
            where: { id: nameToIdMap[nodeData.name] },
            data: { parentIds: JSON.stringify(parentIdStrings) }
          });

          // 更新父节点的 childIds
          for (const parentId of parentIdStrings) {
            const parent = await prisma.knowledgeNode.findUnique({
              where: { id: parentId }
            });
            if (parent) {
              const childIds = JSON.parse(parent.childIds);
              childIds.push(nameToIdMap[nodeData.name]);
              await prisma.knowledgeNode.update({
                where: { id: parentId },
                data: { childIds: JSON.stringify(childIds) }
              });
            }
          }
        }
      }
    }

    // 更新成就
    await updateKnowledgeAchievement(req.userId);

    res.status(201).json({
      message: '批量创建成功',
      count: createdNodes.length,
      nodes: createdNodes.map(n => ({
        ...n,
        parentIds: JSON.parse(n.parentIds),
        childIds: JSON.parse(n.childIds)
      }))
    });
  } catch (error) {
    console.error('Batch create nodes error:', error);
    res.status(500).json({ error: '批量创建知识节点失败' });
  }
};

// 更新知识节点
export const updateNode = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const nodeId = String(req.params.id);
    const { name, description, masteryLevel, isUnlocked, positionX, positionY } = req.body;

    const existing = await prisma.knowledgeNode.findUnique({
      where: { id: nodeId }
    });

    if (!existing) {
      return res.status(404).json({ error: '知识节点不存在' });
    }

    // 验证课程归属
    const course = await prisma.course.findFirst({
      where: { id: existing.courseId, userId: req.userId }
    });

    if (!course) {
      return res.status(404).json({ error: '知识节点不存在' });
    }

    const updateData: any = {};
    if (name) updateData.name = name;
    if (description !== undefined) updateData.description = description;
    if (masteryLevel !== undefined) updateData.masteryLevel = masteryLevel;
    if (isUnlocked !== undefined) updateData.isUnlocked = isUnlocked;
    if (positionX !== undefined) updateData.positionX = positionX;
    if (positionY !== undefined) updateData.positionY = positionY;

    const node = await prisma.knowledgeNode.update({
      where: { id: nodeId },
      data: updateData
    });

    res.json({
      message: '更新成功',
      node: {
        ...node,
        parentIds: JSON.parse(node.parentIds),
        childIds: JSON.parse(node.childIds)
      }
    });
  } catch (error) {
    console.error('Update node error:', error);
    res.status(500).json({ error: '更新知识节点失败' });
  }
};

// 删除知识节点
export const deleteNode = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const nodeId = String(req.params.id);

    const existing = await prisma.knowledgeNode.findUnique({
      where: { id: nodeId }
    });

    if (!existing) {
      return res.status(404).json({ error: '知识节点不存在' });
    }

    // 验证课程归属
    const course = await prisma.course.findFirst({
      where: { id: existing.courseId, userId: req.userId }
    });

    if (!course) {
      return res.status(404).json({ error: '知识节点不存在' });
    }

    await prisma.knowledgeNode.delete({
      where: { id: nodeId }
    });

    res.json({ message: '删除成功' });
  } catch (error) {
    console.error('Delete node error:', error);
    res.status(500).json({ error: '删除知识节点失败' });
  }
};

// 辅助函数：更新知识点成就
async function updateKnowledgeAchievement(userId: string) {
  try {
    // 获取用户的所有课程ID
    const courses = await prisma.course.findMany({
      where: { userId: userId },
      select: { id: true }
    });

    const courseIds = courses.map(c => c.id);

    const count = await prisma.knowledgeNode.count({
      where: {
        courseId: { in: courseIds },
        isUnlocked: true
      }
    });

    const ua = await prisma.userAchievement.findUnique({
      where: { userId_achievementId: { userId, achievementId: 'knowledge_10' } }
    });

    if (ua && !ua.isUnlocked) {
      await prisma.userAchievement.update({
        where: { id: ua.id },
        data: {
          progress: Math.min(count, 10),
          isUnlocked: count >= 10,
          unlockedAt: count >= 10 ? new Date() : null
        }
      });
    }
  } catch (error) {
    console.error('Update knowledge achievement error:', error);
  }
}