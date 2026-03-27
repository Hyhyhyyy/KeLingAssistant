// ==================== 用户相关 ====================

export interface User {
  id: string;
  name: string;
  email: string;
  level: number;
  exp: number;
  energy: number;
  crystals: number;
  streakDays: number;
  totalStudyMinutes: number;
  lastCheckInDate: string | null;
  avatarUrl?: string;
  bio: string;
  weeklyStudyGoal: number;
  createdAt: string;
}

// ==================== 课程相关 ====================

export interface ScheduleSlot {
  dayOfWeek: number;
  startHour: number;
  startMinute: number;
  durationMinutes: number;
}

export interface Course {
  id: string;
  name: string;
  code: string;
  teacher: string;
  schedule: ScheduleSlot[];
  location: string;
  themeColor: string;
  masteryLevel: number;
  plantStage: number;
  planetStyleIndex: number;
  lastStudiedAt: string | null;
  totalStudyMinutes: number;
  isArchived: boolean;
  semester?: string;
  credit: number;
  examDate?: string;
  courseImageUrl?: string;
  studySessionCount: number;
  knowledgeNodeCount?: number;
}

// ==================== 知识节点 ====================

export interface KnowledgeNode {
  id: string;
  courseId: string;
  name: string;
  description: string;
  parentIds: string[];
  childIds: string[];
  difficulty: number;
  masteryLevel: number;
  positionX: number;
  positionY: number;
  isUnlocked: boolean;
}

// ==================== 任务系统 ====================

export type TaskType = 'DAILY_CARE' | 'DEEP_EXPLORATION' | 'REVIEW_RITUAL' | 'BOUNTY' | 'RESCUE';
export type TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED';

export interface Rewards {
  energy: number;
  crystals: number;
  exp: number;
}

export interface Task {
  id: string;
  title: string;
  description: string;
  type: TaskType;
  courseId: string | null;
  knowledgeNodeIds: string[];
  status: TaskStatus;
  priority: number;
  estimatedMinutes: number;
  actualMinutes: number | null;
  rewards: Rewards;
  scheduledAt: string | null;
  completedAt: string | null;
  createdAt: string;
}

// ==================== 笔记 ====================

export type NoteSource = 'AI_GENERATED' | 'USER_CREATED' | 'CLASS_CAPTURE' | 'BOUNTY_REWARD';

export interface Note {
  id: string;
  title: string;
  content: string;
  sourceType: NoteSource;
  aiExplanation?: string;
  relatedNodeIds: string[];
  tags: string[];
  reviewCount: number;
  lastReviewedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

// ==================== 成就系统 ====================

export type AchievementCategory = 'LEARNING' | 'STREAK' | 'EXPLORATION' | 'SOCIAL' | 'MASTERY';

export interface Achievement {
  id: string;
  name: string;
  description: string;
  icon: string;
  category: AchievementCategory;
  requirement: string;
  rewardEnergy: number;
  rewardCrystals: number;
  maxProgress: number;
}

export interface UserAchievement {
  id: string;
  achievementId: string;
  achievement: Achievement;
  isUnlocked: boolean;
  progress: number;
  unlockedAt: string | null;
}

// ==================== 签到系统 ====================

export interface CheckInRecord {
  id: string;
  date: string;
  rewardReceived: boolean;
  createdAt: string;
}

export interface CheckInStatus {
  hasCheckedInToday: boolean;
  streakDays: number;
  lastCheckInDate: string | null;
  nextReward: {
    day: number;
    energy: number;
    crystals: number;
  };
}

// ==================== AI 相关 ====================

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  toolCommand?: string;
  toolResult?: any;
}

export interface AIResponse {
  content: string;
  toolCommand?: string;
  toolResult?: any;
  sessionId: string;
}

// ==================== API 响应 ====================

export interface ApiResponse<T> {
  data?: T;
  error?: string;
  message?: string;
}