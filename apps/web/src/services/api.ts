import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://keling-server.onrender.com/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30秒超时，适应服务器唤醒
});

// 请求拦截器 - 添加 Token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器 - 处理错误
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ==================== 认证 API ====================

export const authAPI = {
  register: (data: { name?: string; email: string; password: string }) =>
    api.post('/auth/register', data),

  login: (data: { email: string; password: string }) =>
    api.post('/auth/login', data),

  getMe: () => api.get('/auth/me'),
};

// ==================== 用户 API ====================

export const userAPI = {
  getProfile: () => api.get('/user/profile'),

  updateProfile: (data: { name?: string; bio?: string; avatarUrl?: string }) =>
    api.put('/user/profile', data),

  updateEnergy: (data: { energy?: number; change?: number }) =>
    api.post('/user/energy', data),

  updateCrystals: (data: { crystals?: number; change?: number }) =>
    api.post('/user/crystals', data),
};

// ==================== 课程 API ====================

export const courseAPI = {
  getAll: () => api.get('/courses'),

  getById: (id: string) => api.get(`/courses/${id}`),

  create: (data: {
    name: string;
    code?: string;
    teacher?: string;
    schedule?: any[];
    location?: string;
    themeColor?: string;
  }) => api.post('/courses', data),

  update: (id: string, data: Partial<{
    name: string;
    code: string;
    teacher: string;
    schedule: any[];
    masteryLevel: number;
    isArchived: boolean;
  }>) => api.put(`/courses/${id}`, data),

  delete: (id: string) => api.delete(`/courses/${id}`),

  updateMastery: (id: string, data: { masteryLevel?: number; studyMinutes?: number }) =>
    api.post(`/courses/${id}/mastery`, data),
};

// ==================== 任务 API ====================

export const taskAPI = {
  getAll: (params?: { status?: string; courseId?: string }) =>
    api.get('/tasks', { params }),

  getById: (id: string) => api.get(`/tasks/${id}`),

  create: (data: {
    title: string;
    description?: string;
    type?: string;
    courseId?: string;
    priority?: number;
    estimatedMinutes?: number;
    rewards?: { energy?: number; crystals?: number; exp?: number };
  }) => api.post('/tasks', data),

  update: (id: string, data: Partial<{
    title: string;
    description: string;
    status: string;
    priority: number;
    actualMinutes: number;
  }>) => api.put(`/tasks/${id}`, data),

  delete: (id: string) => api.delete(`/tasks/${id}`),

  complete: (id: string, data?: { actualMinutes?: number }) =>
    api.post(`/tasks/${id}/complete`, data),
};

// ==================== 知识图谱 API ====================

export const knowledgeAPI = {
  getNodes: (courseId: string) => api.get(`/knowledge/${courseId}/nodes`),

  createNode: (courseId: string, data: {
    name: string;
    description?: string;
    parentIds?: string[];
    difficulty?: number;
  }) => api.post(`/knowledge/${courseId}/nodes`, data),

  batchCreateNodes: (courseId: string, data: { nodes: any[] }) =>
    api.post(`/knowledge/${courseId}/nodes/batch`, data),

  updateNode: (id: string, data: Partial<{
    name: string;
    description: string;
    masteryLevel: number;
    isUnlocked: boolean;
  }>) => api.put(`/knowledge/node/${id}`, data),

  deleteNode: (id: string) => api.delete(`/knowledge/node/${id}`),
};

// ==================== AI API ====================

export const aiAPI = {
  chat: (data: { message: string; scenario?: string; sessionId?: string }) =>
    api.post('/ai/chat', data),

  generatePlan: () => api.post('/ai/plan'),

  analyzeWeakness: () => api.post('/ai/analyze'),
};

// ==================== 笔记 API ====================

export const noteAPI = {
  getAll: () => api.get('/notes'),

  create: (data: {
    title: string;
    content: string;
    sourceType?: string;
    aiExplanation?: string;
    tags?: string[];
  }) => api.post('/notes', data),

  update: (id: string, data: Partial<{
    title: string;
    content: string;
    tags: string[];
  }>) => api.put(`/notes/${id}`, data),

  delete: (id: string) => api.delete(`/notes/${id}`),
};

// ==================== 签到 API ====================

export const checkinAPI = {
  checkIn: () => api.post('/checkin'),

  getStatus: () => api.get('/checkin/status'),

  getRecords: () => api.get('/checkin/records'),
};

// ==================== 成就 API ====================

export const achievementAPI = {
  getAll: () => api.get('/achievements'),

  getUserAchievements: () => api.get('/achievements/user'),
};

export default api;