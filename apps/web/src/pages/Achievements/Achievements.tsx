import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Trophy, Lock, Zap, Gem, Star } from 'lucide-react';
import { achievementAPI } from '../../services/api';
import type { Achievement, UserAchievement, AchievementCategory } from '../../types';
import './Achievements.css';

const CATEGORY_CONFIG: Record<AchievementCategory, { label: string; icon: string; color: string }> = {
  LEARNING: { label: '学习之路', icon: '📚', color: '#E8A87C' },
  STREAK: { label: '坚持不懈', icon: '🔥', color: '#FF6B6B' },
  EXPLORATION: { label: '探索发现', icon: '🔍', color: '#4ECDC4' },
  SOCIAL: { label: '社交达人', icon: '👥', color: '#85CDA9' },
  MASTERY: { label: '精通大师', icon: '🏆', color: '#FFD700' },
};

const Achievements: React.FC = () => {
  const [achievements, setAchievements] = useState<UserAchievement[]>([]);
  const [allAchievements, setAllAchievements] = useState<Achievement[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<AchievementCategory | 'all'>('all');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadAchievements();
  }, []);

  const loadAchievements = async () => {
    try {
      const [userRes, allRes] = await Promise.all([
        achievementAPI.getUserAchievements(),
        achievementAPI.getAll(),
      ]);
      setAchievements(userRes.data.achievements || []);
      setAllAchievements(allRes.data.achievements || []);
    } catch (error) {
      console.error('加载成就失败:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const getAchievementStatus = (achievementId: string) => {
    return achievements.find(a => a.achievementId === achievementId);
  };

  const getProgressPercent = (userAchievement?: UserAchievement) => {
    if (!userAchievement) return 0;
    return Math.min(100, (userAchievement.progress / userAchievement.achievement.maxProgress) * 100);
  };

  const filteredAchievements = allAchievements.filter(achievement =>
    selectedCategory === 'all' || achievement.category === selectedCategory
  );

  const unlockedCount = achievements.filter(a => a.isUnlocked).length;
  const totalPoints = achievements
    .filter(a => a.isUnlocked)
    .reduce((sum, a) => sum + a.achievement.rewardEnergy + a.achievement.rewardCrystals * 10, 0);

  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>加载成就数据...</p>
      </div>
    );
  }

  return (
    <div className="achievements-container">
      {/* 页面标题 */}
      <motion.div
        className="page-header"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <div className="header-icon">🏆</div>
        <h1>成就殿堂</h1>
        <p>记录你的荣耀时刻</p>
      </motion.div>

      {/* 统计卡片 */}
      <motion.div
        className="stats-card"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
      >
        <div className="stat-item">
          <div className="stat-circle">
            <span className="stat-num">{unlockedCount}</span>
            <span className="stat-divider">/</span>
            <span className="stat-total">{allAchievements.length}</span>
          </div>
          <span className="stat-label">已解锁</span>
        </div>
        <div className="stat-divider" />
        <div className="stat-item">
          <div className="stat-points">
            <Star size={20} />
            <span className="stat-num">{totalPoints}</span>
          </div>
          <span className="stat-label">成就点数</span>
        </div>
      </motion.div>

      {/* 分类筛选 */}
      <div className="category-tabs">
        <button
          className={`category-tab ${selectedCategory === 'all' ? 'active' : ''}`}
          onClick={() => setSelectedCategory('all')}
        >
          全部
        </button>
        {Object.entries(CATEGORY_CONFIG).map(([key, config]) => (
          <button
            key={key}
            className={`category-tab ${selectedCategory === key ? 'active' : ''}`}
            onClick={() => setSelectedCategory(key as AchievementCategory)}
          >
            {config.icon} {config.label}
          </button>
        ))}
      </div>

      {/* 成就列表 */}
      <div className="achievements-list">
        {filteredAchievements.map((achievement, index) => {
          const userAchievement = getAchievementStatus(achievement.id);
          const categoryConfig = CATEGORY_CONFIG[achievement.category];
          const progressPercent = getProgressPercent(userAchievement);
          const isUnlocked = userAchievement?.isUnlocked;

          return (
            <motion.div
              key={achievement.id}
              className={`achievement-card ${isUnlocked ? 'unlocked' : 'locked'}`}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.05 }}
            >
              <div className="achievement-icon-container">
                <div
                  className="achievement-icon"
                  style={{
                    background: isUnlocked ? categoryConfig.color : '#ccc',
                  }}
                >
                  {isUnlocked ? achievement.icon : <Lock size={24} />}
                </div>
                {isUnlocked && (
                  <div className="unlocked-badge">✓</div>
                )}
              </div>

              <div className="achievement-info">
                <div className="achievement-header">
                  <h3>{isUnlocked ? achievement.name : '???'}</h3>
                  {isUnlocked && (
                    <span className="unlocked-time">
                      {new Date(userAchievement.unlockedAt!).toLocaleDateString()}
                    </span>
                  )}
                </div>

                <p className="achievement-desc">
                  {isUnlocked ? achievement.description : '完成相关任务后解锁'}
                </p>

                {/* 进度条 */}
                {!isUnlocked && userAchievement && (
                  <div className="progress-section">
                    <div className="progress-bar">
                      <div
                        className="progress-fill"
                        style={{ width: `${progressPercent}%`, background: categoryConfig.color }}
                      />
                    </div>
                    <span className="progress-text">
                      {userAchievement.progress} / {achievement.maxProgress}
                    </span>
                  </div>
                )}

                {/* 奖励 */}
                <div className="achievement-rewards">
                  <span className="reward">
                    <Zap size={12} />+{achievement.rewardEnergy}
                  </span>
                  <span className="reward crystals">
                    <Gem size={12} />+{achievement.rewardCrystals}
                  </span>
                </div>
              </div>
            </motion.div>
          );
        })}
      </div>

      {/* 空状态 */}
      {filteredAchievements.length === 0 && (
        <div className="empty-state">
          <Trophy size={48} />
          <p>暂无该类成就</p>
        </div>
      )}
    </div>
  );
};

export default Achievements;