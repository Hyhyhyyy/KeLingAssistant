import { Request, Response } from 'express';
import { prisma } from '../index';
import { AuthRequest } from '../middleware/auth';
import { v4 as uuidv4 } from 'uuid';

// DeepSeek API 配置
const DEEPSEEK_API_URL = process.env.DEEPSEEK_API_URL || 'https://api.deepseek.com/v1/chat/completions';
const DEEPSEEK_API_KEY = process.env.DEEPSEEK_API_KEY || '';

// 场景类型
type AIScenario =
  | 'QUICK_PLAN'
  | 'WEAKNESS_DIAGNOSE'
  | 'EXAM_PREP'
  | 'CONCEPT_EXPLAIN'
  | 'PRACTICE_SESSION'
  | 'REVIEW_SESSION'
  | 'CASUAL_CHAT';

// AI 对话
export const chat = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const { message, scenario, sessionId } = req.body;

    if (!message) {
      return res.status(400).json({ error: '消息不能为空' });
    }

    // 获取用户上下文
    const context = await buildUserContext(req.userId);

    // 构建 System Prompt
    const systemPrompt = buildSystemPrompt(scenario as AIScenario);

    // 获取对话历史
    const history = await getChatHistory(req.userId, sessionId);

    // 调用 DeepSeek API
    const response = await callDeepSeekAPI(systemPrompt, message, history, context);

    // 保存对话记录
    await saveChatHistory(req.userId, sessionId || uuidv4(), 'user', message);
    await saveChatHistory(req.userId, sessionId || uuidv4(), 'assistant', response.content, response.toolJson);

    // 解析工具指令
    let toolResult = null;
    if (response.toolJson) {
      toolResult = await executeToolCommand(req.userId, response.toolJson);
    }

    res.json({
      content: response.content,
      toolCommand: response.toolJson,
      toolResult,
      sessionId: sessionId || uuidv4()
    });
  } catch (error) {
    console.error('Chat error:', error);
    res.status(500).json({ error: 'AI 服务暂时不可用，请稍后重试' });
  }
};

// 生成学习计划
export const generatePlan = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const context = await buildUserContext(req.userId);

    const prompt = `
请根据以下信息，为用户生成今日学习计划：

【今日状态】
- 待办任务: ${context.pendingTasks.length}个
- 进行中: ${context.inProgressTasks.length}个
- 今日课表: ${context.todaySchedule.map((s: any) => s.course.name).join(', ') || '无'}

【薄弱点】
${context.weakCourses.map((c: any) => `- ${c.name} (${Math.round(c.masteryLevel * 100)}%)`).join('\n') || '暂无'}

请给出：
1. 时间安排建议
2. 优先级排序
3. 学习建议
    `;

    const response = await callDeepSeekAPI(buildSystemPrompt('QUICK_PLAN'), prompt, [], context);

    res.json({
      plan: response.content
    });
  } catch (error) {
    console.error('Generate plan error:', error);
    res.status(500).json({ error: '生成计划失败' });
  }
};

// 分析薄弱点
export const analyzeWeakness = async (req: AuthRequest, res: Response) => {
  try {
    if (!req.userId) {
      return res.status(401).json({ error: '未认证' });
    }

    const context = await buildUserContext(req.userId);

    const prompt = `
请分析用户的学习状况，找出薄弱点并给出改进建议：

【课程掌握度】
${context.courses.map((c: any) => `- ${c.name}: ${Math.round(c.masteryLevel * 100)}%`).join('\n')}

【任务完成情况】
- 待完成: ${context.pendingTasks.length}
- 进行中: ${context.inProgressTasks.length}
- 已完成: ${context.completedTasks.length}

请给出：
1. 薄弱知识点分析
2. 改进建议
3. 学习策略建议
    `;

    const response = await callDeepSeekAPI(buildSystemPrompt('WEAKNESS_DIAGNOSE'), prompt, [], context);

    res.json({
      analysis: response.content
    });
  } catch (error) {
    console.error('Analyze weakness error:', error);
    res.status(500).json({ error: '分析失败' });
  }
};

// ========== 辅助函数 ==========

// 构建用户上下文
async function buildUserContext(userId: string) {
  const user = await prisma.user.findUnique({
    where: { id: userId },
    include: {
      courses: {
        where: { isArchived: false },
        include: { knowledgeNodes: true }
      },
      tasks: true
    }
  });

  if (!user) throw new Error('用户不存在');

  const pendingTasks = user.tasks.filter(t => t.status === 'PENDING');
  const inProgressTasks = user.tasks.filter(t => t.status === 'IN_PROGRESS');
  const completedTasks = user.tasks.filter(t => t.status === 'COMPLETED');
  const weakCourses = user.courses.filter(c => c.masteryLevel < 0.5).sort((a, b) => a.masteryLevel - b.masteryLevel);

  // 今日课表
  const today = new Date().getDay();
  const todaySchedule = user.courses
    .filter(c => JSON.parse(c.schedule).some((s: any) => s.dayOfWeek === today))
    .map(c => ({
      course: c,
      slot: JSON.parse(c.schedule).find((s: any) => s.dayOfWeek === today)
    }));

  return {
    user: {
      name: user.name,
      level: user.level,
      energy: user.energy,
      crystals: user.crystals,
      streakDays: user.streakDays
    },
    courses: user.courses,
    pendingTasks,
    inProgressTasks,
    completedTasks,
    weakCourses,
    todaySchedule
  };
}

// 构建 System Prompt（复用 Android 设计）
function buildSystemPrompt(scenario?: AIScenario): string {
  let scenarioPrompt = '';

  switch (scenario) {
    case 'QUICK_PLAN':
      scenarioPrompt = `
【今日计划模式】
- 结合用户的课表空隙和待办任务
- 给出具体的时间安排建议
- 询问用户是否需要调整`;
      break;
    case 'WEAKNESS_DIAGNOSE':
      scenarioPrompt = `
【薄弱诊断模式】
- 分析各门课程的掌握度
- 找出需要重点关注的领域
- 给出具体的改进建议`;
      break;
    case 'EXAM_PREP':
      scenarioPrompt = `
【考试冲刺模式】
- 优先聚焦高频考点
- 每次只讲解一个关键概念
- 讲完立即出2道练习题检验`;
      break;
    case 'CONCEPT_EXPLAIN':
      scenarioPrompt = `
【概念讲解模式】
- 先用一句话概括核心
- 再用类比让概念更容易理解
- 最后给出一个简单例子`;
      break;
    case 'PRACTICE_SESSION':
      scenarioPrompt = `
【刷题练习模式】
- 先确认要练习的知识点
- 每次出一道题，等待用户回答
- 根据回答给出反馈`;
      break;
    case 'REVIEW_SESSION':
      scenarioPrompt = `
【复习回顾模式】
- 基于遗忘曲线选择需要复习的内容
- 用提问的方式引导回忆
- 建议下次复习时间`;
      break;
  }

  return `
你是一位温暖而专业的学习导师，同时也是学习游戏的主持人。

【核心身份】
- 名称：恒星引擎
- 性格：耐心、鼓励、偶尔调皮
- 风格：简洁有力，不过度解释

【回答原则】
1. 优先结合用户学习画像和当前上下文
2. 每次回答控制在3-5句话，除非用户要求详细解释
3. 关键概念用「」标注，数字用具体数据
4. 适度使用 🌱⚡💎，营造轻度游戏化氛围

【多轮对话策略】
- 如果用户问题模糊，追问一个澄清问题
- 如果涉及复杂任务规划，分步引导
- 如果用户表达挫败，先共情再给建议

【工具指令规范（非常重要）】
- 在回答末尾单独一行输出 JSON
- 格式：{"action":"ACTION_TYPE","params":{...}}
- 无操作时：{"action":"NO_ACTION","params":{}}

【工具指令示例】
- 创建任务：{"action":"CREATE_TASK","params":{"title":"复习高数","estimatedMinutes":30}}
- 页面导航：{"action":"GO_TO","params":{"screen":"tasks"}}
- 添加课时：{"action":"ADD_SCHEDULE_SLOT","params":{"courseId":"高等数学","dayOfWeek":1,"startHour":8,"startMinute":0,"durationMinutes":90}}
- 保存笔记：{"action":"CREATE_NOTE_FROM_ANSWER","params":{"title":"学习笔记标题","answerText":"笔记内容"}}
- 添加知识点：{"action":"UPSERT_KG_NODE","params":{"courseId":"高等数学","name":"极限","description":"极限是..."}}
- 批量添加知识点：{"action":"BATCH_UPSERT_KG_NODES","params":{"courseId":"高等数学","nodes":[{"name":"极限","description":"极限的定义"},{"name":"导数","description":"导数的概念"}]}}

${scenarioPrompt}
  `.trim();
}

// 获取对话历史
async function getChatHistory(userId: string, sessionId?: string) {
  if (!sessionId) return [];

  const history = await prisma.chatHistory.findMany({
    where: { userId, sessionId },
    orderBy: { createdAt: 'asc' },
    take: 20
  });

  return history.map(h => ({
    role: h.role,
    content: h.content
  }));
}

// 保存对话历史
async function saveChatHistory(
  userId: string,
  sessionId: string,
  role: string,
  content: string,
  toolJson?: string | null
) {
  await prisma.chatHistory.create({
    data: {
      id: uuidv4(),
      userId,
      sessionId,
      role,
      content,
      toolUsed: toolJson ? JSON.parse(toolJson).action : null,
      toolResult: toolJson
    }
  });
}

// 调用 DeepSeek API
async function callDeepSeekAPI(
  systemPrompt: string,
  userMessage: string,
  history: any[],
  context: any
): Promise<{ content: string; toolJson: string | null }> {
  const messages = [
    { role: 'system', content: systemPrompt },
    ...history,
    {
      role: 'user',
      content: `
【用户学习画像】
用户: ${context.user.name}, Lv.${context.user.level}
能量: ${context.user.energy}⚡ 结晶: ${context.user.crystals}💎
连续学习: ${context.user.streakDays}天

【我的课程】
${context.courses.map((c: any) => c.name).join(' ')}

【用户输入】
${userMessage}
      `.trim()
    }
  ];

  const response = await fetch(DEEPSEEK_API_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${DEEPSEEK_API_KEY}`
    },
    body: JSON.stringify({
      model: 'deepseek-chat',
      messages,
      temperature: 0.7,
      max_tokens: 2000
    })
  });

  if (!response.ok) {
    throw new Error(`DeepSeek API error: ${response.status}`);
  }

  const data = await response.json() as any;
  const rawContent = data.choices[0].message.content as string;

  // 提取工具指令
  const { content, toolJson } = extractToolCommand(rawContent);

  return { content, toolJson };
}

// 从响应中提取工具指令
function extractToolCommand(raw: string): { content: string; toolJson: string | null } {
  const trimmed = raw.trimEnd();

  // 查找最后一个 JSON 对象
  let braceCount = 0;
  let jsonStartIndex = -1;
  let jsonEndIndex = -1;

  for (let i = trimmed.length - 1; i >= 0; i--) {
    const char = trimmed[i];
    if (char === '}') {
      if (jsonEndIndex === -1) jsonEndIndex = i;
      braceCount++;
    } else if (char === '{') {
      braceCount--;
      if (braceCount === 0 && jsonEndIndex !== -1) {
        jsonStartIndex = i;
        break;
      }
    }
  }

  if (jsonStartIndex !== -1 && jsonEndIndex !== -1) {
    const jsonStr = trimmed.substring(jsonStartIndex, jsonEndIndex + 1).trim();

    try {
      const parsed = JSON.parse(jsonStr);
      if (parsed.action) {
        const contentBefore = trimmed.substring(0, jsonStartIndex).trimEnd();
        return {
          content: contentBefore || raw,
          toolJson: jsonStr
        };
      }
    } catch {
      // 不是有效 JSON
    }
  }

  return { content: raw, toolJson: null };
}

// 执行工具指令
async function executeToolCommand(userId: string, toolJson: string): Promise<any> {
  try {
    const tool = JSON.parse(toolJson);
    const { action, params } = tool;

    switch (action) {
      case 'CREATE_TASK': {
        const task = await prisma.task.create({
          data: {
            id: uuidv4(),
            userId,
            title: params.title,
            description: params.description || '',
            type: params.type || 'DAILY_CARE',
            estimatedMinutes: params.estimatedMinutes || 25,
            priority: params.priority || 3,
            rewardsEnergy: 10,
            rewardsCrystals: 5,
            rewardsExp: 20,
            knowledgeNodeIds: '[]'
          }
        });
        return { success: true, task };
      }

      case 'CREATE_NOTE_FROM_ANSWER': {
        const note = await prisma.note.create({
          data: {
            id: uuidv4(),
            userId,
            title: params.title || '学习笔记',
            content: params.answerText,
            sourceType: 'AI_GENERATED',
            tags: '[]',
            relatedNodeIds: '[]'
          }
        });
        return { success: true, note };
      }

      case 'UPSERT_KG_NODE': {
        // 查找课程
        const course = await prisma.course.findFirst({
          where: { userId, name: { contains: params.courseId } }
        });
        if (!course) return { success: false, error: '课程不存在' };

        const node = await prisma.knowledgeNode.create({
          data: {
            id: uuidv4(),
            courseId: course.id,
            name: params.name,
            description: params.description || '',
            parentIds: '[]',
            childIds: '[]',
            difficulty: params.difficulty || 3
          }
        });
        return { success: true, node };
      }

      case 'BATCH_UPSERT_KG_NODES': {
        const course = await prisma.course.findFirst({
          where: { userId, name: { contains: params.courseId } }
        });
        if (!course) return { success: false, error: '课程不存在' };

        const nodes = [];
        for (const nodeData of params.nodes) {
          const node = await prisma.knowledgeNode.create({
            data: {
              id: uuidv4(),
              courseId: course.id,
              name: nodeData.name,
              description: nodeData.description || '',
              parentIds: '[]',
              childIds: '[]'
            }
          });
          nodes.push(node);
        }
        return { success: true, count: nodes.length };
      }

      default:
        return { success: false, error: '未知工具指令' };
    }
  } catch (error) {
    console.error('Execute tool command error:', error);
    return { success: false, error: '执行失败' };
  }
}