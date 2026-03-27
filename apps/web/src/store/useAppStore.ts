import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User, Course, Task, Note } from '../types';
import { authAPI, courseAPI, taskAPI, noteAPI } from '../services/api';

interface AppState {
  // 用户状态
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;

  // 数据状态
  courses: Course[];
  tasks: Task[];
  notes: Note[];

  // UI 状态
  currentScreen: string;

  // Actions
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => void;
  loadUser: () => Promise<void>;

  // 数据操作
  loadCourses: () => Promise<void>;
  loadTasks: () => Promise<void>;
  loadNotes: () => Promise<void>;

  // 课程操作
  createCourse: (data: any) => Promise<void>;
  updateCourse: (id: string, data: any) => Promise<void>;
  deleteCourse: (id: string) => Promise<void>;

  // 任务操作
  createTask: (data: any) => Promise<void>;
  updateTask: (id: string, data: any) => Promise<void>;
  completeTask: (id: string, actualMinutes?: number) => Promise<void>;
  deleteTask: (id: string) => Promise<void>;

  // 笔记操作
  createNote: (data: any) => Promise<void>;

  // 用户操作
  updateEnergy: (change: number) => void;
  updateCrystals: (change: number) => void;

  // 导航
  navigateTo: (screen: string) => void;
}

export const useAppStore = create<AppState>()(
  persist(
    (set, get) => ({
      // 初始状态
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: true,
      courses: [],
      tasks: [],
      notes: [],
      currentScreen: 'home',

      // 登录
      login: async (email, password) => {
        try {
          const { data } = await authAPI.login({ email, password });
          localStorage.setItem('token', data.token);
          set({
            token: data.token,
            user: data.user,
            isAuthenticated: true,
            isLoading: false,
          });
        } catch (error: any) {
          throw new Error(error.response?.data?.error || '登录失败');
        }
      },

      // 注册
      register: async (name, email, password) => {
        try {
          const { data } = await authAPI.register({ name, email, password });
          localStorage.setItem('token', data.token);
          set({
            token: data.token,
            user: data.user,
            isAuthenticated: true,
            isLoading: false,
          });
        } catch (error: any) {
          throw new Error(error.response?.data?.error || '注册失败');
        }
      },

      // 登出
      logout: () => {
        localStorage.removeItem('token');
        set({
          user: null,
          token: null,
          isAuthenticated: false,
          courses: [],
          tasks: [],
          notes: [],
        });
      },

      // 加载用户数据
      loadUser: async () => {
        const token = localStorage.getItem('token');
        if (!token) {
          set({ isLoading: false, isAuthenticated: false });
          return;
        }

        try {
          const { data } = await authAPI.getMe();
          set({
            user: data.user,
            isAuthenticated: true,
            isLoading: false,
          });

          // 加载其他数据
          get().loadCourses();
          get().loadTasks();
          get().loadNotes();
        } catch {
          localStorage.removeItem('token');
          set({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
          });
        }
      },

      // 加载课程
      loadCourses: async () => {
        try {
          const { data } = await courseAPI.getAll();
          set({ courses: data.courses || [] });
        } catch (error) {
          console.error('加载课程失败:', error);
        }
      },

      // 加载任务
      loadTasks: async () => {
        try {
          const { data } = await taskAPI.getAll();
          set({ tasks: data.tasks || [] });
        } catch (error) {
          console.error('加载任务失败:', error);
        }
      },

      // 加载笔记
      loadNotes: async () => {
        try {
          const { data } = await noteAPI.getAll();
          set({ notes: data.notes || [] });
        } catch (error) {
          console.error('加载笔记失败:', error);
        }
      },

      // 创建课程
      createCourse: async (data) => {
        try {
          await courseAPI.create(data);
          await get().loadCourses();
        } catch (error: any) {
          throw new Error(error.response?.data?.error || '创建课程失败');
        }
      },

      // 更新课程
      updateCourse: async (id, data) => {
        try {
          await courseAPI.update(id, data);
          await get().loadCourses();
        } catch (error: any) {
          throw new Error(error.response?.data?.error || '更新课程失败');
        }
      },

      // 删除课程
      deleteCourse: async (id) => {
        try {
          await courseAPI.delete(id);
          await get().loadCourses();
        } catch (error: any) {
          throw new Error(error.response?.data?.error || '删除课程失败');
        }
      },

      // 创建任务
      createTask: async (data) => {
        try {
          await taskAPI.create(data);
          await get().loadTasks();
        } catch (error: any) {
          throw new Error(error.response?.data?.error || '创建任务失败');
        }
      },

      // 更新任务
      updateTask: async (id, data) => {
        try {
          await taskAPI.update(id, data);
          await get().loadTasks();
        } catch (error: any) {
          throw new Error(error.response?.data?.error || '更新任务失败');
        }
      },

      // 完成任务
      completeTask: async (id, actualMinutes) => {
        try {
          const { data } = await taskAPI.complete(id, { actualMinutes });

          // 更新用户数据
          const user = get().user;
          if (user) {
            set({
              user: {
                ...user,
                energy: user.energy + data.rewards.energy,
                crystals: user.crystals + data.rewards.crystals,
                exp: user.exp + data.rewards.exp,
              },
            });
          }

          await get().loadTasks();
        } catch (error: any) {
          throw new Error(error.response?.data?.error || '完成任务失败');
        }
      },

      // 删除任务
      deleteTask: async (id) => {
        try {
          await taskAPI.delete(id);
          await get().loadTasks();
        } catch (error: any) {
          throw new Error(error.response?.data?.error || '删除任务失败');
        }
      },

      // 创建笔记
      createNote: async (data) => {
        try {
          await noteAPI.create(data);
          await get().loadNotes();
        } catch (error: any) {
          throw new Error(error.response?.data?.error || '创建笔记失败');
        }
      },

      // 更新能量
      updateEnergy: (change) => {
        const user = get().user;
        if (user) {
          set({
            user: {
              ...user,
              energy: Math.max(0, Math.min(200, user.energy + change)),
            },
          });
        }
      },

      // 更新结晶
      updateCrystals: (change) => {
        const user = get().user;
        if (user) {
          set({
            user: {
              ...user,
              crystals: Math.max(0, user.crystals + change),
            },
          });
        }
      },

      // 导航
      navigateTo: (screen) => {
        set({ currentScreen: screen });
      },
    }),
    {
      name: 'keling-storage',
      partialize: (state) => ({
        token: state.token,
      }),
    }
  )
);