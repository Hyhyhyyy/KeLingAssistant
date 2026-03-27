import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Plus, X, Clock, Zap, Gem, Play, Check,
  Target, Timer
} from 'lucide-react';
import { useAppStore } from '../../store/useAppStore';
import type { TaskType, TaskStatus } from '../../types';
import './Tasks.css';

const TASK_TYPES: { value: TaskType; label: string; icon: string; color: string }[] = [
  { value: 'DAILY_CARE', label: '日常照料', icon: '🌱', color: '#85CDA9' },
  { value: 'DEEP_EXPLORATION', label: '深度探索', icon: '🔍', color: '#E8A87C' },
  { value: 'REVIEW_RITUAL', label: '复习仪式', icon: '📚', color: '#7A6B5D' },
  { value: 'BOUNTY', label: '悬赏任务', icon: '🎯', color: '#FF6B6B' },
  { value: 'RESCUE', label: '紧急救援', icon: '🚨', color: '#F44336' },
];

const TASK_STATUS_CONFIG: Record<TaskStatus, { label: string; color: string }> = {
  PENDING: { label: '待处理', color: '#999' },
  IN_PROGRESS: { label: '进行中', color: '#4ECDC4' },
  COMPLETED: { label: '已完成', color: '#4CAF50' },
  ABANDONED: { label: '已放弃', color: '#F44336' },
};

const Tasks: React.FC = () => {
  const { tasks, courses, createTask, updateTask, completeTask, deleteTask } = useAppStore();

  const [filter, setFilter] = useState<'all' | TaskStatus>('all');
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [newTask, setNewTask] = useState({
    title: '',
    description: '',
    type: 'DAILY_CARE' as TaskType,
    courseId: '',
    priority: 2,
    estimatedMinutes: 25,
  });
  const [activeTimer, setActiveTimer] = useState<string | null>(null);
  const [timerSeconds, setTimerSeconds] = useState(0);

  // 番茄钟计时器
  useEffect(() => {
    let interval: ReturnType<typeof setInterval>;
    if (activeTimer) {
      interval = setInterval(() => {
        setTimerSeconds(s => s + 1);
      }, 1000);
    }
    return () => clearInterval(interval);
  }, [activeTimer]);

  const handleCreateTask = async () => {
    if (!newTask.title.trim()) return;

    try {
      const rewards = calculateRewards(newTask.type, newTask.estimatedMinutes);
      await createTask({
        ...newTask,
        courseId: newTask.courseId || undefined,
        rewards,
      });
      setShowCreateDialog(false);
      setNewTask({
        title: '',
        description: '',
        type: 'DAILY_CARE',
        courseId: '',
        priority: 2,
        estimatedMinutes: 25,
      });
    } catch (error: any) {
      alert(error.message);
    }
  };

  const handleStartTask = async (taskId: string) => {
    try {
      await updateTask(taskId, { status: 'IN_PROGRESS' });
      setActiveTimer(taskId);
      setTimerSeconds(0);
    } catch (error: any) {
      alert(error.message);
    }
  };

  const handleCompleteTask = async (taskId: string) => {
    try {
      const actualMinutes = Math.round(timerSeconds / 60);
      await completeTask(taskId, activeTimer === taskId ? actualMinutes : undefined);
      if (activeTimer === taskId) {
        setActiveTimer(null);
        setTimerSeconds(0);
      }
    } catch (error: any) {
      alert(error.message);
    }
  };

  const handleDeleteTask = async (taskId: string) => {
    if (confirm('确定要删除这个任务吗？')) {
      try {
        await deleteTask(taskId);
      } catch (error: any) {
        alert(error.message);
      }
    }
  };

  const calculateRewards = (type: TaskType, minutes: number) => {
    const baseEnergy = Math.round(minutes * 0.8);
    const baseCrystals = Math.round(minutes / 15);
    const baseExp = Math.round(minutes * 1.2);

    const multipliers: Record<TaskType, number> = {
      DAILY_CARE: 1,
      DEEP_EXPLORATION: 1.5,
      REVIEW_RITUAL: 1.2,
      BOUNTY: 2,
      RESCUE: 1.8,
    };

    const mult = multipliers[type];
    return {
      energy: Math.round(baseEnergy * mult),
      crystals: Math.round(baseCrystals * mult),
      exp: Math.round(baseExp * mult),
    };
  };

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const filteredTasks = tasks.filter(task =>
    filter === 'all' || task.status === filter
  );

  const pendingTasks = tasks.filter(t => t.status === 'PENDING');
  const inProgressTasks = tasks.filter(t => t.status === 'IN_PROGRESS');
  const completedToday = tasks.filter(t =>
    t.status === 'COMPLETED' &&
    t.completedAt &&
    new Date(t.completedAt).toDateString() === new Date().toDateString()
  );

  return (
    <div className="tasks-container">
      {/* 页面标题 */}
      <motion.div
        className="page-header"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <h1>📋 培育任务</h1>
        <p>完成任务，收获成长</p>
      </motion.div>

      {/* 统计卡片 */}
      <motion.div
        className="stats-cards"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
      >
        <div className="stat-card pending">
          <span className="stat-num">{pendingTasks.length}</span>
          <span className="stat-label">待完成</span>
        </div>
        <div className="stat-card progress">
          <span className="stat-num">{inProgressTasks.length}</span>
          <span className="stat-label">进行中</span>
        </div>
        <div className="stat-card completed">
          <span className="stat-num">{completedToday.length}</span>
          <span className="stat-label">今日完成</span>
        </div>
      </motion.div>

      {/* 计时器显示 */}
      {activeTimer && (
        <motion.div
          className="active-timer"
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
        >
          <Timer size={20} />
          <span className="timer-display">{formatTime(timerSeconds)}</span>
          <button
            className="btn-timer-stop"
            onClick={() => {
              setActiveTimer(null);
              setTimerSeconds(0);
            }}
          >
            停止
          </button>
        </motion.div>
      )}

      {/* 筛选标签 */}
      <div className="filter-tabs">
        {(['all', 'PENDING', 'IN_PROGRESS', 'COMPLETED'] as const).map((status) => (
          <button
            key={status}
            className={`filter-tab ${filter === status ? 'active' : ''}`}
            onClick={() => setFilter(status)}
          >
            {status === 'all' ? '全部' : TASK_STATUS_CONFIG[status].label}
          </button>
        ))}
      </div>

      {/* 任务列表 */}
      <div className="tasks-list">
        <AnimatePresence>
          {filteredTasks.length === 0 ? (
            <motion.div
              className="empty-state"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
            >
              <Target size={48} />
              <p>暂无任务</p>
              <button className="btn btn-primary" onClick={() => setShowCreateDialog(true)}>
                创建任务
              </button>
            </motion.div>
          ) : (
            filteredTasks.map((task, index) => {
              const typeConfig = TASK_TYPES.find(t => t.value === task.type) || TASK_TYPES[0];
              const statusConfig = TASK_STATUS_CONFIG[task.status];
              const course = task.courseId ? courses.find(c => c.id === task.courseId) : null;

              return (
                <motion.div
                  key={task.id}
                  className={`task-card ${task.status.toLowerCase()} ${activeTimer === task.id ? 'timing' : ''}`}
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: 20 }}
                  transition={{ delay: index * 0.05 }}
                >
                  <div className="task-left">
                    <div
                      className="task-type-badge"
                      style={{ background: typeConfig.color }}
                    >
                      {typeConfig.icon}
                    </div>
                    <div className="task-info">
                      <h4>{task.title}</h4>
                      <div className="task-meta">
                        {course && <span className="course-tag">{course.name}</span>}
                        <span className="time-tag">
                          <Clock size={12} />
                          {task.estimatedMinutes}分钟
                        </span>
                        <span className="status-tag" style={{ color: statusConfig.color }}>
                          {statusConfig.label}
                        </span>
                      </div>
                    </div>
                  </div>

                  <div className="task-right">
                    {/* 奖励显示 */}
                    <div className="task-rewards">
                      <span className="reward-item">
                        <Zap size={12} />+{task.rewards.energy}
                      </span>
                      <span className="reward-item crystals">
                        <Gem size={12} />+{task.rewards.crystals}
                      </span>
                    </div>

                    {/* 操作按钮 */}
                    <div className="task-actions">
                      {task.status === 'PENDING' && (
                        <button
                          className="action-btn start"
                          onClick={() => handleStartTask(task.id)}
                          title="开始任务"
                        >
                          <Play size={16} />
                        </button>
                      )}
                      {task.status === 'IN_PROGRESS' && (
                        <button
                          className="action-btn complete"
                          onClick={() => handleCompleteTask(task.id)}
                          title="完成任务"
                        >
                          <Check size={16} />
                        </button>
                      )}
                      {task.status === 'PENDING' && (
                        <button
                          className="action-btn delete"
                          onClick={() => handleDeleteTask(task.id)}
                          title="删除任务"
                        >
                          <X size={14} />
                        </button>
                      )}
                    </div>
                  </div>

                  {/* 计时器 */}
                  {activeTimer === task.id && (
                    <div className="task-timer">
                      <span>{formatTime(timerSeconds)}</span>
                    </div>
                  )}
                </motion.div>
              );
            })
          )}
        </AnimatePresence>
      </div>

      {/* 添加任务按钮 */}
      <motion.button
        className="fab-add"
        onClick={() => setShowCreateDialog(true)}
        whileHover={{ scale: 1.1 }}
        whileTap={{ scale: 0.9 }}
      >
        <Plus size={24} />
      </motion.button>

      {/* 创建任务对话框 */}
      <AnimatePresence>
        {showCreateDialog && (
          <motion.div
            className="dialog-overlay"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setShowCreateDialog(false)}
          >
            <motion.div
              className="create-dialog"
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.9, opacity: 0 }}
              onClick={(e) => e.stopPropagation()}
            >
              <div className="dialog-header">
                <h2>创建培育任务</h2>
                <button className="close-btn" onClick={() => setShowCreateDialog(false)}>
                  <X size={20} />
                </button>
              </div>

              <div className="dialog-content">
                {/* 任务标题 */}
                <div className="form-group">
                  <label>任务标题 *</label>
                  <input
                    type="text"
                    className="input"
                    placeholder="输入任务标题"
                    value={newTask.title}
                    onChange={(e) => setNewTask({ ...newTask, title: e.target.value })}
                  />
                </div>

                {/* 任务类型 */}
                <div className="form-group">
                  <label>任务类型</label>
                  <div className="type-selector">
                    {TASK_TYPES.map((type) => (
                      <button
                        key={type.value}
                        className={`type-option ${newTask.type === type.value ? 'selected' : ''}`}
                        style={{ borderColor: newTask.type === type.value ? type.color : '#eee' }}
                        onClick={() => setNewTask({ ...newTask, type: type.value })}
                      >
                        <span className="type-icon">{type.icon}</span>
                        <span className="type-label">{type.label}</span>
                      </button>
                    ))}
                  </div>
                </div>

                {/* 关联课程 */}
                <div className="form-group">
                  <label>关联课程（可选）</label>
                  <select
                    className="input"
                    value={newTask.courseId}
                    onChange={(e) => setNewTask({ ...newTask, courseId: e.target.value })}
                  >
                    <option value="">不关联课程</option>
                    {courses.map(course => (
                      <option key={course.id} value={course.id}>
                        {course.name}
                      </option>
                    ))}
                  </select>
                </div>

                {/* 预计时长 */}
                <div className="form-group">
                  <label>
                    <Clock size={14} style={{ marginRight: 4 }} />
                    预计时长
                  </label>
                  <div className="duration-slider">
                    <input
                      type="range"
                      min="5"
                      max="120"
                      step="5"
                      value={newTask.estimatedMinutes}
                      onChange={(e) => setNewTask({ ...newTask, estimatedMinutes: parseInt(e.target.value) })}
                    />
                    <span className="duration-value">{newTask.estimatedMinutes} 分钟</span>
                  </div>
                </div>

                {/* 预计奖励 */}
                <div className="rewards-preview">
                  <span className="rewards-label">预计奖励:</span>
                  <div className="rewards-values">
                    {(() => {
                      const rewards = calculateRewards(newTask.type, newTask.estimatedMinutes);
                      return (
                        <>
                          <span><Zap size={12} />+{rewards.energy}</span>
                          <span><Gem size={12} />+{rewards.crystals}</span>
                        </>
                      );
                    })()}
                  </div>
                </div>
              </div>

              <div className="dialog-footer">
                <button className="btn btn-outline" onClick={() => setShowCreateDialog(false)}>
                  取消
                </button>
                <button
                  className="btn btn-primary"
                  onClick={handleCreateTask}
                  disabled={!newTask.title.trim()}
                >
                  创建任务
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default Tasks;