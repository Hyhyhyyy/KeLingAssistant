import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppStore } from '../../store/useAppStore';
import { motion } from 'framer-motion';
import {
  Sparkles, Target, BookOpen, Trophy,
  Calendar, ChevronRight, Clock
} from 'lucide-react';
import { checkinAPI } from '../../services/api';

// 自定义图标
import energyIcon from '../../assets/icons/energy.jpg';
import crystalsIcon from '../../assets/icons/crystals.jpg';
import tasksIcon from '../../assets/icons/tasks.jpg';
import streakIcon from '../../assets/icons/streak.jpg';
import notesIcon from '../../assets/icons/notes.jpg';
import achievementsIcon from '../../assets/icons/achievements.jpg';
import profileIcon from '../../assets/icons/profile.jpg';
import greenhouseIcon from '../../assets/icons/greenhouse.jpg';
import planetIcon from '../../assets/icons/planet.jpg';

import './Home.css';

const Home: React.FC = () => {
  const { user, courses, tasks, loadUser, loadCourses, loadTasks } = useAppStore();
  const navigate = useNavigate();
  const [checkInStatus, setCheckInStatus] = useState<any>(null);
  const [showCheckInDialog, setShowCheckInDialog] = useState(false);
  const [checkInReward, setCheckInReward] = useState<any>(null);

  useEffect(() => {
    loadUser();
    loadCourses();
    loadTasks();
    loadCheckInStatus();
  }, []);

  const loadCheckInStatus = async () => {
    try {
      const { data } = await checkinAPI.getStatus();
      setCheckInStatus(data);
    } catch (error) {
      console.error('加载签到状态失败:', error);
    }
  };

  const handleCheckIn = async () => {
    try {
      const { data } = await checkinAPI.checkIn();
      setCheckInReward(data);
      setShowCheckInDialog(true);
      setCheckInStatus({ ...checkInStatus, hasCheckedInToday: true });
    } catch (error: any) {
      alert(error.response?.data?.error || '签到失败');
    }
  };

  const pendingTasks = tasks.filter(t => t.status === 'PENDING' || t.status === 'IN_PROGRESS');

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour >= 5 && hour < 12) return '早上好';
    if (hour >= 12 && hour < 18) return '下午好';
    return '晚上好';
  };

  return (
    <div className="home-page">
      {/* 欢迎区域 */}
      <motion.div
        className="welcome-section"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <div className="welcome-text">
          <h1>{getGreeting()}，{user?.name || '星际园丁'}</h1>
          <p>今天是学习的好日子，准备好培育你的知识星球了吗？</p>
        </div>

        {/* 签到卡片 */}
        {checkInStatus && !checkInStatus.hasCheckedInToday && (
          <div className="checkin-card" onClick={handleCheckIn}>
            <div className="checkin-info">
              <Calendar size={20} />
              <span>今日签到</span>
            </div>
            <div className="checkin-reward">
              <span>+{checkInStatus.nextReward?.energy}⚡</span>
              <span>+{checkInStatus.nextReward?.crystals}💎</span>
            </div>
          </div>
        )}
      </motion.div>

      {/* 统计卡片 */}
      <motion.div
        className="stats-grid"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
      >
        <div className="stat-card">
          <div className="stat-icon energy"><img src={energyIcon} alt="能量" /></div>
          <div className="stat-content">
            <span className="stat-value">{user?.energy || 0}</span>
            <span className="stat-label">能量</span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon crystals"><img src={crystalsIcon} alt="结晶" /></div>
          <div className="stat-content">
            <span className="stat-value">{user?.crystals || 0}</span>
            <span className="stat-label">结晶</span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon tasks"><img src={tasksIcon} alt="任务" /></div>
          <div className="stat-content">
            <span className="stat-value">{pendingTasks.length}</span>
            <span className="stat-label">待完成任务</span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon streak"><img src={streakIcon} alt="连续签到" /></div>
          <div className="stat-content">
            <span className="stat-value">{user?.streakDays || 0}</span>
            <span className="stat-label">连续签到</span>
          </div>
        </div>
      </motion.div>

      <div className="main-grid">
        {/* 左侧：课程和任务 */}
        <div className="left-column">
          {/* AI 入口 */}
          <motion.div
            className="ai-card"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            onClick={() => navigate('/ai')}
          >
            <div className="ai-icon"><Sparkles size={32} /></div>
            <div className="ai-content">
              <h3>AI 学习助手</h3>
              <p>智能规划学习、答疑解惑、知识提炼</p>
            </div>
            <ChevronRight size={20} className="chevron" />
          </motion.div>

          {/* 我的课程 */}
          <motion.section
            className="section"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3 }}
          >
            <div className="section-header">
              <h2>我的知识星球</h2>
              <button className="see-all" onClick={() => navigate('/greenhouse')}>
                查看全部 <ChevronRight size={16} />
              </button>
            </div>

            {courses.length === 0 ? (
              <div className="empty-card" onClick={() => navigate('/greenhouse')}>
                <div className="empty-icon"><img src={planetIcon} alt="星球" className="planet-icon-img" /></div>
                <p>创建你的第一颗知识星球</p>
              </div>
            ) : (
              <div className="courses-grid">
                {courses.slice(0, 4).map((course) => (
                  <div
                    key={course.id}
                    className="course-card"
                    onClick={() => navigate(`/greenhouse/${course.id}`)}
                  >
                    <div className="course-icon" style={{ background: course.themeColor }}>
                      {['🌍', '🌙', '🌟', '💫'][course.planetStyleIndex % 4]}
                    </div>
                    <div className="course-info">
                      <h4>{course.name}</h4>
                      <div className="course-progress">
                        <div className="progress-bar">
                          <div
                            className="progress-fill"
                            style={{ width: `${course.masteryLevel * 100}%`, background: course.themeColor }}
                          />
                        </div>
                        <span>{Math.round(course.masteryLevel * 100)}%</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </motion.section>

          {/* 今日任务 */}
          <motion.section
            className="section"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4 }}
          >
            <div className="section-header">
              <h2>今日任务</h2>
              <button className="see-all" onClick={() => navigate('/tasks')}>
                查看全部 <ChevronRight size={16} />
              </button>
            </div>

            {pendingTasks.length === 0 ? (
              <div className="empty-card" onClick={() => navigate('/ai')}>
                <Target size={32} />
                <p>让 AI 帮你规划今天的任务</p>
              </div>
            ) : (
              <div className="tasks-list">
                {pendingTasks.slice(0, 5).map((task) => (
                  <div
                    key={task.id}
                    className="task-item"
                    onClick={() => navigate('/tasks')}
                  >
                    <div className="task-type" style={{ background: getTypeColor(task.type) }}>
                      {getTypeIcon(task.type)}
                    </div>
                    <div className="task-content">
                      <h4>{task.title}</h4>
                      <div className="task-meta">
                        <span><Clock size={12} />{task.estimatedMinutes}分钟</span>
                        <span>+{task.rewards.energy}⚡</span>
                      </div>
                    </div>
                    <span className={`task-status ${task.status.toLowerCase()}`}>
                      {task.status === 'IN_PROGRESS' ? '进行中' : '待处理'}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </motion.section>
        </div>

        {/* 右侧：快捷入口 */}
        <div className="right-column">
          {/* 快捷入口 */}
          <motion.div
            className="quick-actions"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3 }}
          >
            <h3>快捷入口</h3>
            <div className="actions-grid">
              <div className="action-item" onClick={() => navigate('/notes')}>
                <img src={notesIcon} alt="笔记" className="action-icon-img" />
                <span>笔记</span>
              </div>
              <div className="action-item" onClick={() => navigate('/achievements')}>
                <img src={achievementsIcon} alt="成就" className="action-icon-img" />
                <span>成就</span>
              </div>
              <div className="action-item" onClick={() => navigate('/profile')}>
                <img src={profileIcon} alt="我的" className="action-icon-img" />
                <span>我的</span>
              </div>
              <div className="action-item" onClick={() => navigate('/greenhouse')}>
                <img src={greenhouseIcon} alt="温室" className="action-icon-img" />
                <span>温室</span>
              </div>
            </div>
          </motion.div>

          {/* 学习统计 */}
          <motion.div
            className="study-stats"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4 }}
          >
            <h3>本周学习</h3>
            <div className="stats-chart">
              {/* 简单的柱状图 */}
              {[0, 1, 2, 3, 4, 5, 6].map((day) => {
                const height = Math.random() * 60 + 20;
                return (
                  <div key={day} className="chart-bar">
                    <div className="bar" style={{ height: `${height}%` }} />
                    <span className="day-label">{['日', '一', '二', '三', '四', '五', '六'][day]}</span>
                  </div>
                );
              })}
            </div>
            <div className="total-time">
              <Clock size={16} />
              <span>本周共学习 <strong>{user?.totalStudyMinutes || 0}</strong> 分钟</span>
            </div>
          </motion.div>
        </div>
      </div>

      {/* 签到成功弹窗 */}
      {showCheckInDialog && checkInReward && (
        <div className="dialog-overlay" onClick={() => setShowCheckInDialog(false)}>
          <motion.div
            className="checkin-dialog"
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            onClick={(e) => e.stopPropagation()}
          >
            <div className="dialog-icon">🎉</div>
            <h2>签到成功！</h2>
            <p>连续签到 {checkInReward.streakDays} 天</p>
            <div className="dialog-rewards">
              <span>+{checkInReward.rewards.energy}⚡</span>
              <span>+{checkInReward.rewards.crystals}💎</span>
            </div>
            <button className="btn btn-primary" onClick={() => setShowCheckInDialog(false)}>
              太棒了！
            </button>
          </motion.div>
        </div>
      )}
    </div>
  );
};

function getTypeColor(type: string) {
  const colors: Record<string, string> = {
    DAILY_CARE: '#10b981',
    DEEP_EXPLORATION: '#f59e0b',
    REVIEW_RITUAL: '#6366f1',
    BOUNTY: '#ef4444',
    RESCUE: '#ec4899',
  };
  return colors[type] || '#6366f1';
}

function getTypeIcon(type: string) {
  const icons: Record<string, string> = {
    DAILY_CARE: '🌱',
    DEEP_EXPLORATION: '🔍',
    REVIEW_RITUAL: '📚',
    BOUNTY: '🎯',
    RESCUE: '🚨',
  };
  return icons[type] || '📋';
}

export default Home;