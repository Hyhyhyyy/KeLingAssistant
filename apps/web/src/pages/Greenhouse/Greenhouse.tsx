import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Plus, X, BookOpen, Clock, MapPin, User, ChevronRight } from 'lucide-react';
import { useAppStore } from '../../store/useAppStore';
import './Greenhouse.css';

const PLANET_EMOJIS = ['🌍', '🌙', '🌟', '💫', '🪐', '⭐', '🌕', '🔮'];
const THEME_COLORS = [
  '#6366f1', '#8b5cf6', '#ec4899', '#f59e0b',
  '#10b981', '#3b82f6', '#ef4444', '#14b8a6'
];

const Greenhouse: React.FC = () => {
  const navigate = useNavigate();
  const { courses, createCourse, deleteCourse } = useAppStore();
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [newCourse, setNewCourse] = useState({
    name: '',
    code: '',
    teacher: '',
    location: '',
    themeColor: THEME_COLORS[0],
    planetStyleIndex: 0
  });

  const handleCreateCourse = async () => {
    if (!newCourse.name.trim()) return;

    try {
      await createCourse(newCourse);
      setShowCreateDialog(false);
      setNewCourse({
        name: '',
        code: '',
        teacher: '',
        location: '',
        themeColor: THEME_COLORS[0],
        planetStyleIndex: 0
      });
    } catch (error: any) {
      alert(error.message);
    }
  };

  const handleDeleteCourse = async (e: React.MouseEvent, courseId: string) => {
    e.stopPropagation();
    if (confirm('确定要删除这门课程吗？')) {
      try {
        await deleteCourse(courseId);
      } catch (error: any) {
        alert(error.message);
      }
    }
  };

  return (
    <div className="greenhouse-page">
      {/* 页面标题 */}
      <motion.div
        className="page-header"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <div className="header-content">
          <h1>知识星球</h1>
          <p>管理和培育你的课程知识</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowCreateDialog(true)}>
          <Plus size={18} />
          添加课程
        </button>
      </motion.div>

      {/* 课程网格 */}
      {courses.length === 0 ? (
        <motion.div
          className="empty-state"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
        >
          <div className="empty-icon">🌍</div>
          <h3>还没有知识星球</h3>
          <p>创建你的第一门课程，开始知识探索之旅</p>
          <button className="btn btn-primary" onClick={() => setShowCreateDialog(true)}>
            创建课程
          </button>
        </motion.div>
      ) : (
        <div className="courses-grid">
          <AnimatePresence>
            {courses.map((course, index) => (
              <motion.div
                key={course.id}
                className="course-card"
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -20 }}
                transition={{ delay: index * 0.05 }}
                onClick={() => navigate(`/greenhouse/${course.id}`)}
              >
                <div className="course-header" style={{ background: course.themeColor }}>
                  <span className="course-emoji">
                    {PLANET_EMOJIS[course.planetStyleIndex % PLANET_EMOJIS.length]}
                  </span>
                  <button
                    className="delete-btn"
                    onClick={(e) => handleDeleteCourse(e, course.id)}
                  >
                    <X size={14} />
                  </button>
                </div>

                <div className="course-body">
                  <h3>{course.name}</h3>
                  {course.code && <span className="course-code">{course.code}</span>}

                  <div className="course-meta">
                    {course.teacher && (
                      <span><User size={12} /> {course.teacher}</span>
                    )}
                    {course.location && (
                      <span><MapPin size={12} /> {course.location}</span>
                    )}
                  </div>

                  <div className="mastery-section">
                    <div className="mastery-label">
                      <span>掌握度</span>
                      <span className="mastery-value">{Math.round(course.masteryLevel * 100)}%</span>
                    </div>
                    <div className="mastery-bar">
                      <div
                        className="mastery-fill"
                        style={{ width: `${course.masteryLevel * 100}%`, background: course.themeColor }}
                      />
                    </div>
                  </div>

                  <div className="course-stats">
                    <span><Clock size={12} /> {course.totalStudyMinutes}分钟</span>
                    <span><BookOpen size={12} /> {course.knowledgeNodeCount || 0}节点</span>
                  </div>
                </div>

                <div className="course-footer">
                  <span>查看详情</span>
                  <ChevronRight size={16} />
                </div>
              </motion.div>
            ))}
          </AnimatePresence>
        </div>
      )}

      {/* 创建课程对话框 */}
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
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              onClick={(e) => e.stopPropagation()}
            >
              <div className="dialog-header">
                <h2>添加新课程</h2>
                <button className="close-btn" onClick={() => setShowCreateDialog(false)}>
                  <X size={20} />
                </button>
              </div>

              <div className="dialog-content">
                <div className="form-group">
                  <label>课程名称 *</label>
                  <input
                    type="text"
                    className="input"
                    placeholder="如：高等数学"
                    value={newCourse.name}
                    onChange={(e) => setNewCourse({ ...newCourse, name: e.target.value })}
                  />
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>课程代码</label>
                    <input
                      type="text"
                      className="input"
                      placeholder="MATH101"
                      value={newCourse.code}
                      onChange={(e) => setNewCourse({ ...newCourse, code: e.target.value })}
                    />
                  </div>
                  <div className="form-group">
                    <label>教师</label>
                    <input
                      type="text"
                      className="input"
                      placeholder="授课教师"
                      value={newCourse.teacher}
                      onChange={(e) => setNewCourse({ ...newCourse, teacher: e.target.value })}
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label>上课地点</label>
                  <input
                    type="text"
                    className="input"
                    placeholder="如：教学楼A101"
                    value={newCourse.location}
                    onChange={(e) => setNewCourse({ ...newCourse, location: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label>图标</label>
                  <div className="emoji-picker">
                    {PLANET_EMOJIS.map((emoji, index) => (
                      <button
                        key={emoji}
                        className={`emoji-option ${newCourse.planetStyleIndex === index ? 'selected' : ''}`}
                        onClick={() => setNewCourse({ ...newCourse, planetStyleIndex: index })}
                      >
                        {emoji}
                      </button>
                    ))}
                  </div>
                </div>

                <div className="form-group">
                  <label>主题色</label>
                  <div className="color-picker">
                    {THEME_COLORS.map((color) => (
                      <button
                        key={color}
                        className={`color-option ${newCourse.themeColor === color ? 'selected' : ''}`}
                        style={{ background: color }}
                        onClick={() => setNewCourse({ ...newCourse, themeColor: color })}
                      />
                    ))}
                  </div>
                </div>
              </div>

              <div className="dialog-footer">
                <button className="btn btn-secondary" onClick={() => setShowCreateDialog(false)}>
                  取消
                </button>
                <button
                  className="btn btn-primary"
                  onClick={handleCreateCourse}
                  disabled={!newCourse.name.trim()}
                >
                  创建课程
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default Greenhouse;