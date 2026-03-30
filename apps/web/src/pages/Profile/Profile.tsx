import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  Settings, LogOut, ChevronRight, Target
} from 'lucide-react';
import { useAppStore } from '../../store/useAppStore';
import { checkinAPI } from '../../services/api';

// 自定义图标
import energyIcon from '../../assets/icons/energy.jpg';
import crystalsIcon from '../../assets/icons/crystals.jpg';
import streakIcon from '../../assets/icons/streak.jpg';
import achievementsIcon from '../../assets/icons/achievements.jpg';
import notesIcon from '../../assets/icons/notes.jpg';

import './Profile.css';

const Profile: React.FC = () => {
  const navigate = useNavigate();
  const { user, logout, courses, tasks } = useAppStore();
  const [checkInRecords, setCheckInRecords] = useState<any[]>([]);
  const [currentMonth, setCurrentMonth] = useState(new Date());

  useEffect(() => {
    loadCheckInRecords();
  }, []);

  const loadCheckInRecords = async () => {
    try {
      const { data } = await checkinAPI.getRecords();
      setCheckInRecords(data.records || []);
    } catch (error) {
      console.error('加载签到记录失败:', error);
    }
  };

  const handleLogout = () => {
    if (confirm('确定要退出登录吗？')) {
      logout();
      navigate('/login');
    }
  };

  // 计算等级进度
  const getLevelProgress = () => {
    if (!user) return 0;
    const currentLevelExp = user.level * 100;
    const nextLevelExp = (user.level + 1) * 100;
    const progress = ((user.exp - currentLevelExp) / (nextLevelExp - currentLevelExp)) * 100;
    return Math.min(100, Math.max(0, progress));
  };

  // 获取等级称号
  const getLevelTitle = (level: number) => {
    const titles = [
      '新星园丁', '学徒园丁', '初级园丁', '中级园丁',
      '高级园丁', '专家园丁', '大师园丁', '传奇园丁', '星际园丁'
    ];
    return titles[Math.min(level - 1, titles.length - 1)];
  };

  // 生成日历数据
  const generateCalendarData = () => {
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startDayOfWeek = firstDay.getDay();

    const days: { date: Date; isCurrentMonth: boolean; checked: boolean }[] = [];

    // 上个月的日期
    for (let i = 0; i < startDayOfWeek; i++) {
      const date = new Date(year, month, -startDayOfWeek + i + 1);
      days.push({ date, isCurrentMonth: false, checked: false });
    }

    // 当前月的日期
    for (let i = 1; i <= daysInMonth; i++) {
      const date = new Date(year, month, i);
      const dateStr = date.toISOString().split('T')[0];
      const checked = checkInRecords.some(
        r => r.date.split('T')[0] === dateStr
      );
      days.push({ date, isCurrentMonth: true, checked });
    }

    // 下个月的日期
    const remainingDays = 42 - days.length;
    for (let i = 1; i <= remainingDays; i++) {
      const date = new Date(year, month + 1, i);
      days.push({ date, isCurrentMonth: false, checked: false });
    }

    return days;
  };

  const calendarDays = generateCalendarData();
  const weekDays = ['日', '一', '二', '三', '四', '五', '六'];

  // 统计数据
  const completedTasks = tasks.filter(t => t.status === 'COMPLETED').length;
  const totalStudyMinutes = courses.reduce((sum, c) => sum + c.totalStudyMinutes, 0);

  return (
    <div className="profile-container">
      {/* 用户信息卡片 */}
      <motion.div
        className="user-card"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <div className="user-avatar">
          {user?.name?.charAt(0) || '园'}
        </div>
        <div className="user-info">
          <h2>{user?.name || '星际园丁'}</h2>
          <div className="level-badge">
            <span className="level">Lv.{user?.level || 1}</span>
            <span className="title">{getLevelTitle(user?.level || 1)}</span>
          </div>
          <div className="exp-bar">
            <div className="exp-fill" style={{ width: `${getLevelProgress()}%` }} />
          </div>
          <span className="exp-text">
            {user?.exp || 0} / {(user?.level || 1 + 1) * 100} EXP
          </span>
        </div>
      </motion.div>

      {/* 资源卡片 */}
      <motion.div
        className="resources-card"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
      >
        <div className="resource-item energy">
          <img src={energyIcon} alt="能量" className="resource-icon-img" />
          <div className="resource-info">
            <span className="resource-value">{user?.energy || 100}</span>
            <span className="resource-label">能量</span>
          </div>
        </div>
        <div className="resource-divider" />
        <div className="resource-item crystals">
          <img src={crystalsIcon} alt="结晶" className="resource-icon-img" />
          <div className="resource-info">
            <span className="resource-value">{user?.crystals || 10}</span>
            <span className="resource-label">结晶</span>
          </div>
        </div>
        <div className="resource-divider" />
        <div className="resource-item streak">
          <img src={streakIcon} alt="连续签到" className="resource-icon-img" />
          <div className="resource-info">
            <span className="resource-value">{user?.streakDays || 0}</span>
            <span className="resource-label">连续签到</span>
          </div>
        </div>
      </motion.div>

      {/* 学习统计 */}
      <motion.div
        className="stats-card"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
      >
        <h3>
          <Target size={18} />
          学习统计
        </h3>
        <div className="stats-grid">
          <div className="stat-item">
            <span className="stat-value">{courses.length}</span>
            <span className="stat-label">课程</span>
          </div>
          <div className="stat-item">
            <span className="stat-value">{completedTasks}</span>
            <span className="stat-label">完成任务</span>
          </div>
          <div className="stat-item">
            <span className="stat-value">{Math.floor(totalStudyMinutes / 60)}</span>
            <span className="stat-label">学习小时</span>
          </div>
          <div className="stat-item">
            <span className="stat-value">{user?.totalStudyMinutes || 0}</span>
            <span className="stat-label">总分钟</span>
          </div>
        </div>
      </motion.div>

      {/* 签到日历 */}
      <motion.div
        className="calendar-card"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
      >
        <div className="calendar-header">
          <button
            className="month-nav"
            onClick={() => setCurrentMonth(new Date(currentMonth.setMonth(currentMonth.getMonth() - 1)))}
          >
            ‹
          </button>
          <h3>
            {currentMonth.getFullYear()}年{currentMonth.getMonth() + 1}月
          </h3>
          <button
            className="month-nav"
            onClick={() => setCurrentMonth(new Date(currentMonth.setMonth(currentMonth.getMonth() + 1)))}
          >
            ›
          </button>
        </div>

        <div className="calendar-weekdays">
          {weekDays.map(day => (
            <span key={day}>{day}</span>
          ))}
        </div>

        <div className="calendar-days">
          {calendarDays.map((day, index) => (
            <div
              key={index}
              className={`calendar-day ${day.isCurrentMonth ? 'current' : 'other'} ${day.checked ? 'checked' : ''}`}
            >
              {day.date.getDate()}
              {day.checked && <span className="check-mark">✓</span>}
            </div>
          ))}
        </div>
      </motion.div>

      {/* 快捷入口 */}
      <motion.div
        className="menu-list"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.4 }}
      >
        <div className="menu-item" onClick={() => navigate('/achievements')}>
          <img src={achievementsIcon} alt="成就" className="menu-icon-img" />
          <span>成就殿堂</span>
          <ChevronRight size={18} className="chevron" />
        </div>
        <div className="menu-item" onClick={() => navigate('/study-report')}>
          <span className="menu-icon-emoji">📊</span>
          <span>学习报告</span>
          <ChevronRight size={18} className="chevron" />
        </div>
        <div className="menu-item" onClick={() => navigate('/notes')}>
          <img src={notesIcon} alt="笔记" className="menu-icon-img" />
          <span>我的笔记</span>
          <ChevronRight size={18} className="chevron" />
        </div>
        <div className="menu-item" onClick={() => navigate('/settings')}>
          <Settings size={20} className="menu-icon" />
          <span>设置</span>
          <ChevronRight size={18} className="chevron" />
        </div>
      </motion.div>

      {/* 退出登录 */}
      <motion.button
        className="logout-btn"
        onClick={handleLogout}
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.5 }}
      >
        <LogOut size={18} />
        退出登录
      </motion.button>
    </div>
  );
};

export default Profile;