import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  ArrowLeft, BookOpen, Clock, MapPin, User, Trash2,
  Edit3, Target, Zap, ChevronRight
} from 'lucide-react';
import { useAppStore } from '../../store/useAppStore';
import { courseAPI, knowledgeAPI } from '../../services/api';
import type { Course, KnowledgeNode } from '../../types';
import './CourseDetail.css';

const CourseDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { updateCourse, deleteCourse } = useAppStore();

  const [course, setCourse] = useState<Course | null>(null);
  const [nodes, setNodes] = useState<KnowledgeNode[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [showEditDialog, setShowEditDialog] = useState(false);
  const [editData, setEditData] = useState({
    name: '',
    code: '',
    teacher: '',
    location: ''
  });

  useEffect(() => {
    loadCourseDetail();
  }, [id]);

  const loadCourseDetail = async () => {
    if (!id) return;
    setIsLoading(true);
    try {
      const [courseRes, nodesRes] = await Promise.all([
        courseAPI.getById(id),
        knowledgeAPI.getNodes(id)
      ]);
      setCourse(courseRes.data.course);
      setNodes(nodesRes.data.nodes || []);
      setEditData({
        name: courseRes.data.course.name,
        code: courseRes.data.course.code || '',
        teacher: courseRes.data.course.teacher || '',
        location: courseRes.data.course.location || ''
      });
    } catch (error) {
      console.error('加载课程详情失败:', error);
      navigate('/greenhouse');
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpdateCourse = async () => {
    if (!course || !editData.name.trim()) return;
    try {
      await updateCourse(course.id, editData);
      setShowEditDialog(false);
      loadCourseDetail();
    } catch (error: any) {
      alert(error.message);
    }
  };

  const handleDeleteCourse = async () => {
    if (!course) return;
    if (confirm('确定要删除这门课程吗？所有相关数据将被删除。')) {
      try {
        await deleteCourse(course.id);
        navigate('/greenhouse');
      } catch (error: any) {
        alert(error.message);
      }
    }
  };

  const handleCreateKnowledgeNode = async () => {
    if (!course) return;
    const name = prompt('输入知识点名称:');
    if (!name?.trim()) return;

    try {
      await knowledgeAPI.createNode(course.id, { name });
      loadCourseDetail();
    } catch (error: any) {
      alert(error.message);
    }
  };

  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>加载中...</p>
      </div>
    );
  }

  if (!course) {
    return (
      <div className="error-container">
        <p>课程不存在</p>
        <button className="btn btn-primary" onClick={() => navigate('/greenhouse')}>
          返回温室
        </button>
      </div>
    );
  }

  const masteryPercent = Math.round(course.masteryLevel * 100);

  return (
    <div className="course-detail-container">
      {/* 头部区域 */}
      <motion.div
        className="course-header"
        style={{ background: `linear-gradient(135deg, ${course.themeColor}, ${course.themeColor}dd)` }}
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
      >
        <div className="header-nav">
          <button className="back-btn" onClick={() => navigate('/greenhouse')}>
            <ArrowLeft size={20} />
          </button>
          <div className="header-actions">
            <button className="action-btn" onClick={() => setShowEditDialog(true)}>
              <Edit3 size={18} />
            </button>
            <button className="action-btn delete" onClick={handleDeleteCourse}>
              <Trash2 size={18} />
            </button>
          </div>
        </div>

        <div className="header-content">
          <div className="course-icon">
            {['🌍', '🌙', '🌟', '💫', '🪐', '⭐'][course.planetStyleIndex % 6]}
          </div>
          <h1>{course.name}</h1>
          {course.code && <span className="course-code">{course.code}</span>}
        </div>

        <div className="course-meta">
          {course.teacher && (
            <span><User size={14} /> {course.teacher}</span>
          )}
          {course.location && (
            <span><MapPin size={14} /> {course.location}</span>
          )}
        </div>
      </motion.div>

      {/* 掌握度卡片 */}
      <motion.div
        className="mastery-card"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
      >
        <div className="mastery-header">
          <Target size={20} />
          <h3>掌握度</h3>
        </div>
        <div className="mastery-visual">
          <div className="mastery-circle">
            <svg viewBox="0 0 100 100">
              <circle
                cx="50" cy="50" r="45"
                fill="none"
                stroke="#eee"
                strokeWidth="8"
              />
              <circle
                cx="50" cy="50" r="45"
                fill="none"
                stroke={course.themeColor}
                strokeWidth="8"
                strokeDasharray={`${masteryPercent * 2.83} 283`}
                strokeLinecap="round"
                transform="rotate(-90 50 50)"
              />
            </svg>
            <div className="mastery-value" style={{ color: course.themeColor }}>
              {masteryPercent}%
            </div>
          </div>
        </div>
        <div className="mastery-stats">
          <div className="stat-item">
            <Clock size={16} />
            <span className="stat-value">{course.totalStudyMinutes}</span>
            <span className="stat-label">分钟</span>
          </div>
          <div className="stat-item">
            <BookOpen size={16} />
            <span className="stat-value">{nodes.length}</span>
            <span className="stat-label">知识点</span>
          </div>
          <div className="stat-item">
            <Zap size={16} />
            <span className="stat-value">{course.studySessionCount}</span>
            <span className="stat-label">次学习</span>
          </div>
        </div>
      </motion.div>

      {/* 快捷操作 */}
      <motion.div
        className="quick-actions"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
      >
        <div className="action-card" onClick={() => navigate('/ai')}>
          <div className="action-icon" style={{ background: course.themeColor }}>
            <Zap size={24} />
          </div>
          <div className="action-content">
            <h4>AI 辅导</h4>
            <p>智能学习助手</p>
          </div>
          <ChevronRight size={20} className="chevron" />
        </div>

        <div className="action-card" onClick={() => navigate('/tasks')}>
          <div className="action-icon" style={{ background: '#85CDA9' }}>
            <Target size={24} />
          </div>
          <div className="action-content">
            <h4>创建任务</h4>
            <p>安排学习计划</p>
          </div>
          <ChevronRight size={20} className="chevron" />
        </div>
      </motion.div>

      {/* 知识点列表 */}
      <motion.section
        className="knowledge-section"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
      >
        <div className="section-header">
          <h3>知识节点</h3>
          <button className="btn btn-outline small" onClick={handleCreateKnowledgeNode}>
            添加
          </button>
        </div>

        {nodes.length === 0 ? (
          <div className="empty-nodes" onClick={handleCreateKnowledgeNode}>
            <BookOpen size={32} />
            <p>点击添加第一个知识点</p>
          </div>
        ) : (
          <div className="nodes-list">
            {nodes.map((node) => (
              <div key={node.id} className="node-item">
                <div className="node-info">
                  <h4>{node.name}</h4>
                  {node.description && (
                    <p className="node-desc">{node.description}</p>
                  )}
                </div>
                <div className="node-mastery">
                  <div
                    className="mini-bar"
                    style={{ background: course.themeColor, width: `${node.masteryLevel * 100}%` }}
                  />
                  <span>{Math.round(node.masteryLevel * 100)}%</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </motion.section>

      {/* 编辑对话框 */}
      {showEditDialog && (
        <div className="dialog-overlay" onClick={() => setShowEditDialog(false)}>
          <motion.div
            className="edit-dialog"
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            onClick={(e) => e.stopPropagation()}
          >
            <div className="dialog-header">
              <h2>编辑课程</h2>
              <button className="close-btn" onClick={() => setShowEditDialog(false)}>×</button>
            </div>

            <div className="dialog-content">
              <div className="form-group">
                <label>课程名称</label>
                <input
                  type="text"
                  className="input"
                  value={editData.name}
                  onChange={(e) => setEditData({ ...editData, name: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>课程代码</label>
                <input
                  type="text"
                  className="input"
                  value={editData.code}
                  onChange={(e) => setEditData({ ...editData, code: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>教师</label>
                <input
                  type="text"
                  className="input"
                  value={editData.teacher}
                  onChange={(e) => setEditData({ ...editData, teacher: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>地点</label>
                <input
                  type="text"
                  className="input"
                  value={editData.location}
                  onChange={(e) => setEditData({ ...editData, location: e.target.value })}
                />
              </div>
            </div>

            <div className="dialog-footer">
              <button className="btn btn-outline" onClick={() => setShowEditDialog(false)}>
                取消
              </button>
              <button className="btn btn-primary" onClick={handleUpdateCourse}>
                保存
              </button>
            </div>
          </motion.div>
        </div>
      )}
    </div>
  );
};

export default CourseDetail;