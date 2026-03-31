import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  User, Trophy, LogOut, Bell, Clock, Calendar,
  Volume2, Vibrate, RefreshCw, BookOpen, MessageCircle,
  Info, ChevronRight, X, Smartphone
} from 'lucide-react';
import { useAppStore } from '../../store/useAppStore';
import './Settings.css';

const Settings: React.FC = () => {
  const navigate = useNavigate();
  const { logout } = useAppStore();

  // 设置状态
  const [notificationEnabled, setNotificationEnabled] = useState(true);
  const [studyReminderEnabled, setStudyReminderEnabled] = useState(true);
  const [soundEnabled, setSoundEnabled] = useState(true);
  const [vibrationEnabled, setVibrationEnabled] = useState(false);
  const [checkInReminderEnabled, setCheckInReminderEnabled] = useState(true);
  const [showAboutDialog, setShowAboutDialog] = useState(false);

  const handleLogout = () => {
    if (confirm('确定要退出登录吗？')) {
      logout();
      navigate('/login');
    }
  };

  const handleCheckUpdate = () => {
    alert('当前已是最新版本 v3.0.8');
  };

  const handleDownloadApp = () => {
    // 打开APK下载页面
    window.open('https://github.com/keling/keling/releases', '_blank');
  };

  return (
    <div className="settings-container">
      {/* 页面标题 */}
      <motion.div
        className="page-header"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <button className="back-btn" onClick={() => navigate(-1)}>
          ‹ 返回
        </button>
        <div className="header-title">
          <h1>⚙️ 设置</h1>
          <p>应用配置</p>
        </div>
      </motion.div>

      {/* 账户设置 */}
      <motion.section
        className="settings-section"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
      >
        <h2 className="section-title">账户</h2>
        <div className="settings-card">
          <div className="setting-item" onClick={() => navigate('/profile')}>
            <div className="setting-icon account">
              <User size={20} />
            </div>
            <div className="setting-content">
              <span className="setting-title">个人资料</span>
              <span className="setting-subtitle">修改昵称和头像</span>
            </div>
            <ChevronRight size={18} className="chevron" />
          </div>
          <div className="setting-item" onClick={() => navigate('/achievements')}>
            <div className="setting-icon achievements">
              <Trophy size={20} />
            </div>
            <div className="setting-content">
              <span className="setting-title">成就管理</span>
              <span className="setting-subtitle">查看已解锁成就</span>
            </div>
            <ChevronRight size={18} className="chevron" />
          </div>
          <div className="setting-item" onClick={handleLogout}>
            <div className="setting-icon logout">
              <LogOut size={20} />
            </div>
            <div className="setting-content">
              <span className="setting-title">退出登录</span>
              <span className="setting-subtitle">切换账号或使用游客模式</span>
            </div>
            <ChevronRight size={18} className="chevron" />
          </div>
        </div>
      </motion.section>

      {/* 通知设置 */}
      <motion.section
        className="settings-section"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
      >
        <h2 className="section-title">通知</h2>
        <div className="settings-card">
          <div className="setting-item">
            <div className="setting-icon notification">
              <Bell size={20} />
            </div>
            <div className="setting-content">
              <span className="setting-title">推送通知</span>
              <span className="setting-subtitle">接收学习提醒和任务通知</span>
            </div>
            <label className="toggle-switch">
              <input
                type="checkbox"
                checked={notificationEnabled}
                onChange={(e) => setNotificationEnabled(e.target.checked)}
              />
              <span className="toggle-slider"></span>
            </label>
          </div>
          <div className="setting-item">
            <div className="setting-icon reminder">
              <Clock size={20} />
            </div>
            <div className="setting-content">
              <span className="setting-title">学习提醒</span>
              <span className="setting-subtitle">每日固定时间提醒学习</span>
            </div>
            <label className="toggle-switch">
              <input
                type="checkbox"
                checked={studyReminderEnabled}
                onChange={(e) => setStudyReminderEnabled(e.target.checked)}
              />
              <span className="toggle-slider"></span>
            </label>
          </div>
          <div className="setting-item">
            <div className="setting-icon calendar">
              <Calendar size={20} />
            </div>
            <div className="setting-content">
              <span className="setting-title">签到提醒</span>
              <span className="setting-subtitle">每日签到提醒通知</span>
            </div>
            <label className="toggle-switch">
              <input
                type="checkbox"
                checked={checkInReminderEnabled}
                onChange={(e) => setCheckInReminderEnabled(e.target.checked)}
              />
              <span className="toggle-slider"></span>
            </label>
          </div>
        </div>
      </motion.section>

      {/* 效果设置 */}
      <motion.section
        className="settings-section"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
      >
        <h2 className="section-title">效果</h2>
        <div className="settings-card">
          <div className="setting-item">
            <div className="setting-icon sound">
              <Volume2 size={20} />
            </div>
            <div className="setting-content">
              <span className="setting-title">音效</span>
              <span className="setting-subtitle">按钮点击和完成任务的音效</span>
            </div>
            <label className="toggle-switch">
              <input
                type="checkbox"
                checked={soundEnabled}
                onChange={(e) => setSoundEnabled(e.target.checked)}
              />
              <span className="toggle-slider"></span>
            </label>
          </div>
          <div className="setting-item">
            <div className="setting-icon vibration">
              <Vibrate size={20} />
            </div>
            <div className="setting-content">
              <span className="setting-title">震动反馈</span>
              <span className="setting-subtitle">交互时的震动提示</span>
            </div>
            <label className="toggle-switch">
              <input
                type="checkbox"
                checked={vibrationEnabled}
                onChange={(e) => setVibrationEnabled(e.target.checked)}
              />
              <span className="toggle-slider"></span>
            </label>
          </div>
        </div>
      </motion.section>

      {/* 其他设置 */}
      <motion.section
        className="settings-section"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.4 }}
      >
        <h2 className="section-title">其他</h2>
        <div className="settings-card">
          <div className="setting-item" onClick={handleCheckUpdate}>
            <div className="setting-icon update">
              <RefreshCw size={20} />
            </div>
            <div className="setting-content">
              <span className="setting-title">检查更新</span>
              <span className="setting-subtitle">检查是否有新版本</span>
            </div>
            <ChevronRight size={18} className="chevron" />
          </div>
          <div className="setting-item">
            <div className="setting-icon guide">
              <BookOpen size={20} />
            </div>
            <div className="setting-content">
              <span className="setting-title">使用指南</span>
              <span className="setting-subtitle">了解如何使用课灵</span>
            </div>
            <ChevronRight size={18} className="chevron" />
          </div>
          <div className="setting-item">
            <div className="setting-icon feedback">
              <MessageCircle size={20} />
            </div>
            <div className="setting-content">
              <span className="setting-title">意见反馈</span>
              <span className="setting-subtitle">帮助我们改进应用</span>
            </div>
            <ChevronRight size={18} className="chevron" />
          </div>
          <div className="setting-item" onClick={() => setShowAboutDialog(true)}>
            <div className="setting-icon about">
              <Info size={20} />
            </div>
            <div className="setting-content">
              <span className="setting-title">关于课灵</span>
              <span className="setting-subtitle">版本 3.0.8</span>
            </div>
            <ChevronRight size={18} className="chevron" />
          </div>
          <div className="setting-item" onClick={handleDownloadApp}>
            <div className="setting-icon mobile">
              <Smartphone size={20} />
            </div>
            <div className="setting-content">
              <span className="setting-title">下载移动端</span>
              <span className="setting-subtitle">获取Android应用</span>
            </div>
            <ChevronRight size={18} className="chevron" />
          </div>
        </div>
      </motion.section>

      {/* 关于对话框 */}
      {showAboutDialog && (
        <div className="dialog-overlay" onClick={() => setShowAboutDialog(false)}>
          <motion.div
            className="about-dialog"
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            onClick={(e) => e.stopPropagation()}
          >
            <button className="close-btn" onClick={() => setShowAboutDialog(false)}>
              <X size={20} />
            </button>
            <div className="about-logo">✨</div>
            <h2>课灵 KeLing</h2>
            <p className="version">版本 3.0.8</p>
            <p className="description">
              一款AI驱动的学习管理应用<br />
              让学习像培育星球一样有趣
            </p>
            <div className="about-links">
              <button className="link-btn" onClick={handleDownloadApp}>
                <Smartphone size={16} />
                下载移动端
              </button>
            </div>
            <div className="about-footer">
              <p>开发者：课灵团队</p>
              <p>© 2024-2025 KeLing</p>
            </div>
          </motion.div>
        </div>
      )}
    </div>
  );
};

export default Settings;