import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  BookOpen, CheckCircle, Flame, Sparkles, Target,
  TrendingUp, Lightbulb, ChevronRight
} from 'lucide-react';
import { useAppStore } from '../../store/useAppStore';
import './StudyReport.css';

interface StudyReportData {
  startDate: Date;
  endDate: Date;
  totalStudyMinutes: number;
  completedTasks: number;
  coursesStudied: number;
  averageMastery: number;
  streakDays: number;
  aiInsight: string;
  strongPoints: string[];
  weakPoints: string[];
  suggestions: string[];
}

const StudyReport: React.FC = () => {
  const navigate = useNavigate();
  const { user, courses, tasks } = useAppStore();
  const [report, setReport] = useState<StudyReportData | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    generateReport();
  }, [user, courses, tasks]);

  const generateReport = async () => {
    // 基于用户数据生成报告
    const now = new Date();
    const startDate = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);

    // 计算统计数据
    const completedTasksCount = tasks.filter(t =>
      t.status === 'COMPLETED' &&
      t.completedAt &&
      new Date(t.completedAt) >= startDate
    ).length;

    const totalStudyMinutes = courses.reduce((sum, c) => sum + c.totalStudyMinutes, 0);
    const averageMastery = courses.length > 0
      ? courses.reduce((sum, c) => sum + c.masteryLevel, 0) / courses.length
      : 0;

    // AI 洞察生成（模拟）
    const aiInsight = generateAIInsight(user, courses, tasks, completedTasksCount);
    const strongPoints = identifyStrongPoints(courses, tasks);
    const weakPoints = identifyWeakPoints(courses, tasks);
    const suggestions = generateSuggestions(weakPoints, user);

    setReport({
      startDate,
      endDate: now,
      totalStudyMinutes,
      completedTasks: completedTasksCount,
      coursesStudied: courses.filter(c => c.totalStudyMinutes > 0).length,
      averageMastery,
      streakDays: user?.streakDays || 0,
      aiInsight,
      strongPoints,
      weakPoints,
      suggestions,
    });

    setIsLoading(false);
  };

  const generateAIInsight = (user: any, courses: any[], _tasks: any[], completedTasks: number) => {
    const insights = [
      `本周你保持了良好的学习节奏，完成了 ${completedTasks} 个任务，展现了持之以恒的学习态度。`,
      `你的 ${courses.length} 门课程学习进度稳定，特别是掌握度较高的课程值得继续深入学习。`,
      `连续签到 ${user?.streakDays || 0} 天，这份坚持正在转化为实实在在的知识积累！`,
    ];
    return insights.join('\n');
  };

  const identifyStrongPoints = (courses: any[], tasks: any[]) => {
    const points: string[] = [];
    const highMasteryCourses = courses.filter(c => c.masteryLevel >= 0.7);
    if (highMasteryCourses.length > 0) {
      points.push(`课程「${highMasteryCourses[0].name}」掌握度达到 ${Math.round(highMasteryCourses[0].masteryLevel * 100)}%`);
    }
    const completedToday = tasks.filter(t =>
      t.status === 'COMPLETED' &&
      t.completedAt &&
      new Date(t.completedAt).toDateString() === new Date().toDateString()
    ).length;
    if (completedToday >= 3) {
      points.push(`今日高效完成了 ${completedToday} 个任务`);
    }
    if (points.length === 0) {
      points.push('持续学习，稳步前进');
    }
    return points;
  };

  const identifyWeakPoints = (courses: any[], tasks: any[]) => {
    const points: string[] = [];
    const lowMasteryCourses = courses.filter(c => c.masteryLevel < 0.3 && c.masteryLevel > 0);
    if (lowMasteryCourses.length > 0) {
      points.push(`课程「${lowMasteryCourses[0].name}」掌握度较低，需要加强`);
    }
    const pendingTasks = tasks.filter(t => t.status === 'PENDING').length;
    if (pendingTasks > 5) {
      points.push(`有 ${pendingTasks} 个待处理任务，建议及时规划`);
    }
    if (points.length === 0) {
      points.push('目前没有明显的薄弱领域');
    }
    return points;
  };

  const generateSuggestions = (_weakPoints: string[], user: any) => {
    const suggestions: string[] = [];
    suggestions.push('建议每天安排固定的学习时间，培养稳定的学习习惯');
    suggestions.push('利用AI助手进行知识点梳理，加深理解');
    suggestions.push('完成任务后及时回顾，巩固学习成果');
    if (user?.streakDays < 7) {
      suggestions.push('保持连续签到，积累更多学习能量');
    }
    return suggestions;
  };

  const formatDate = (date: Date) => {
    return `${date.getMonth() + 1}/${date.getDate()}`;
  };

  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>正在生成学习报告...</p>
      </div>
    );
  }

  return (
    <div className="report-container">
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
          <h1>📊 学习报告</h1>
          <p>本周学习分析</p>
        </div>
      </motion.div>

      {/* 报告概览 */}
      <motion.div
        className="report-overview"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
      >
        <div className="overview-icon">
          <TrendingUp size={40} />
        </div>
        <h2>本周学习报告</h2>
        <p className="date-range">
          {report && `${formatDate(report.startDate)} - ${formatDate(report.endDate)}`}
        </p>
        <div className="overview-stats">
          <div className="stat-box study">
            <BookOpen size={20} />
            <span className="stat-value">{report?.totalStudyMinutes || 0}</span>
            <span className="stat-unit">分钟</span>
            <span className="stat-label">本周学习</span>
          </div>
          <div className="stat-box tasks">
            <CheckCircle size={20} />
            <span className="stat-value">{report?.completedTasks || 0}</span>
            <span className="stat-unit">个</span>
            <span className="stat-label">完成任务</span>
          </div>
          <div className="stat-box streak">
            <Flame size={20} />
            <span className="stat-value">{report?.streakDays || 0}</span>
            <span className="stat-unit">天</span>
            <span className="stat-label">连续学习</span>
          </div>
        </div>
      </motion.div>

      {/* AI 洞察 */}
      <motion.div
        className="ai-insight-card"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
      >
        <div className="card-header">
          <div className="card-icon ai">
            <Sparkles size={24} />
          </div>
          <h3>AI 洞察</h3>
        </div>
        <p className="insight-content">{report?.aiInsight}</p>
      </motion.div>

      {/* 学习数据详情 */}
      <motion.div
        className="data-detail-card"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
      >
        <h3 className="card-title">学习数据详情</h3>
        <div className="data-rows">
          <div className="data-row">
            <span className="data-label">本周学习时长</span>
            <span className="data-value">{report?.totalStudyMinutes} 分钟</span>
          </div>
          <div className="data-row">
            <span className="data-label">完成任务数</span>
            <span className="data-value">{report?.completedTasks} 个</span>
          </div>
          <div className="data-row">
            <span className="data-label">学习课程数</span>
            <span className="data-value">{report?.coursesStudied} 门</span>
          </div>
          <div className="data-row">
            <span className="data-label">平均掌握度</span>
            <span className="data-value">{Math.round((report?.averageMastery || 0) * 100)}%</span>
          </div>
        </div>
      </motion.div>

      {/* 优势领域 */}
      <motion.div
        className="analysis-card strong"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.4 }}
      >
        <div className="card-header">
          <div className="card-icon strong-icon">
            <Target size={24} />
          </div>
          <h3>优势领域 ✨</h3>
        </div>
        <div className="analysis-list">
          {report?.strongPoints.map((point, index) => (
            <div key={index} className="analysis-item">
              <span className="item-dot strong"></span>
              <span className="item-text">{point}</span>
            </div>
          ))}
        </div>
      </motion.div>

      {/* 待提升领域 */}
      <motion.div
        className="analysis-card weak"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.5 }}
      >
        <div className="card-header">
          <div className="card-icon weak-icon">
            <TrendingUp size={24} />
          </div>
          <h3>待提升领域 📈</h3>
        </div>
        <div className="analysis-list">
          {report?.weakPoints.map((point, index) => (
            <div key={index} className="analysis-item">
              <span className="item-dot weak"></span>
              <span className="item-text">{point}</span>
            </div>
          ))}
        </div>
      </motion.div>

      {/* AI 建议 */}
      <motion.div
        className="analysis-card suggestions"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.6 }}
      >
        <div className="card-header">
          <div className="card-icon suggestion-icon">
            <Lightbulb size={24} />
          </div>
          <h3>AI 建议 💡</h3>
        </div>
        <div className="analysis-list">
          {report?.suggestions.map((suggestion, index) => (
            <div key={index} className="analysis-item">
              <span className="item-dot suggestion"></span>
              <span className="item-text">{suggestion}</span>
            </div>
          ))}
        </div>
      </motion.div>

      {/* 快捷入口 */}
      <motion.div
        className="quick-actions-card"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.7 }}
      >
        <h3>继续学习</h3>
        <div className="action-buttons">
          <button className="action-btn ai" onClick={() => navigate('/ai')}>
            <Sparkles size={20} />
            <span>AI 学习规划</span>
            <ChevronRight size={16} />
          </button>
          <button className="action-btn tasks" onClick={() => navigate('/tasks')}>
            <CheckCircle size={20} />
            <span>完成任务</span>
            <ChevronRight size={16} />
          </button>
        </div>
      </motion.div>
    </div>
  );
};

export default StudyReport;