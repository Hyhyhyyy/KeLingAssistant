// ==================== 用户相关类型 ====================

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
  lastCheckInDate?: string;
  createdAt?: string;
}

export interface AuthResponse {
  message?: string;
  token?: string;
  user?: User;
  error?: string;
}

// ==================== 课程相关类型 ====================

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
  totalStudyMinutes: number;
  isArchived: boolean;
  studySessionCount: number;
}

export interface CourseResponse {
  courses?: Course[];
  course?: Course;
  error?: string;
}

// ==================== 任务相关类型 ====================

export type TaskType = 'DAILY_CARE' | 'WATERING' | 'FERTILIZING' | 'HARVEST' | 'STUDY' | 'REVIEW' | 'PRACTICE' | 'CUSTOM';
export type TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

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
  courseId?: string;
  status: TaskStatus;
  priority: number;
  estimatedMinutes: number;
  actualMinutes?: number;
  rewards: Rewards;
  scheduledAt?: string;
  completedAt?: string;
  createdAt?: string;
}

export interface TaskResponse {
  tasks?: Task[];
  task?: Task;
  rewards?: Rewards;
  error?: string;
}

// ==================== 笔记相关类型 ====================

export type NoteSourceType = 'USER_CREATED' | 'AI_GENERATED' | 'IMPORTED';

export interface Note {
  id: string;
  title: string;
  content: string;
  sourceType: NoteSourceType;
  aiExplanation?: string;
  tags: string[];
  reviewCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface NoteResponse {
  notes?: Note[];
  note?: Note;
  error?: string;
}

// ==================== 签到相关类型 ====================

export interface CheckInResponse {
  message?: string;
  streakDays: number;
  rewards?: Rewards;
  isSpecial: boolean;
  specialReward?: string;
  hasCheckedInToday: boolean;
  nextReward?: Rewards;
  error?: string;
}

// ==================== 知识图谱相关类型 ====================

export interface KnowledgeNode {
  id: string;
  courseId: string;
  title: string;
  description: string;
  content?: string;
  parentId?: string;
  order: number;
  masteryLevel: number;
  isKeyPoint: boolean;
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

// ==================== 成就相关类型 ====================

export interface Achievement {
  id: string;
  name: string;
  description: string;
  icon: string;
  category: string;
  requirement: number;
  reward: Rewards;
}

// ==================== API 配置 ====================

export const API_CONFIG = {
  // 本地开发
  LOCAL: {
    WEB: 'http://localhost:3001/api',
    ANDROID_EMULATOR: 'http://10.0.2.2:3001/api',
    ANDROID_DEVICE: 'http://192.168.1.100:3001/api', // 需要替换为实际IP
  },
  // 生产环境
  PRODUCTION: {
    BASE_URL: 'https://api.keling.app/api', // 需要替换为实际域名
  }
};

// ==================== 默认值 ====================

export const DEFAULT_USER: Partial<User> = {
  level: 1,
  exp: 0,
  energy: 100,
  crystals: 10,
  streakDays: 0,
  totalStudyMinutes: 0,
};

export const DEFAULT_REWARDS: Rewards = {
  energy: 10,
  crystals: 5,
  exp: 20,
};