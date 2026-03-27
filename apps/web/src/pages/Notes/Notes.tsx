import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Plus, X, Search, FileText, Clock,
  BookOpen, Sparkles, Trash2
} from 'lucide-react';
import { useAppStore } from '../../store/useAppStore';
import { noteAPI } from '../../services/api';
import type { Note, NoteSource } from '../../types';
import './Notes.css';

const SOURCE_CONFIG: Record<NoteSource, { label: string; icon: string; color: string }> = {
  AI_GENERATED: { label: 'AI生成', icon: '✨', color: '#E8A87C' },
  USER_CREATED: { label: '原创', icon: '📝', color: '#85CDA9' },
  CLASS_CAPTURE: { label: '课堂', icon: '📚', color: '#7A6B5D' },
  BOUNTY_REWARD: { label: '悬赏', icon: '🎯', color: '#FF6B6B' },
};

const Notes: React.FC = () => {
  const { notes, createNote } = useAppStore();

  const [searchQuery, setSearchQuery] = useState('');
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [selectedNote, setSelectedNote] = useState<Note | null>(null);
  const [newNote, setNewNote] = useState({
    title: '',
    content: '',
    tags: [] as string[],
  });
  const [tagInput, setTagInput] = useState('');

  const filteredNotes = notes.filter(note =>
    note.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
    note.content.toLowerCase().includes(searchQuery.toLowerCase()) ||
    note.tags.some(tag => tag.toLowerCase().includes(searchQuery.toLowerCase()))
  );

  const handleCreateNote = async () => {
    if (!newNote.title.trim()) return;

    try {
      await createNote({
        title: newNote.title,
        content: newNote.content,
        sourceType: 'USER_CREATED',
        tags: newNote.tags,
      });
      setShowCreateDialog(false);
      setNewNote({ title: '', content: '', tags: [] });
    } catch (error: any) {
      alert(error.message);
    }
  };

  const handleDeleteNote = async (noteId: string) => {
    if (!confirm('确定要删除这篇笔记吗？')) return;

    try {
      await noteAPI.delete(noteId);
      // 重新加载笔记
      window.location.reload();
    } catch (error: any) {
      alert(error.message);
    }
  };

  const addTag = () => {
    const tag = tagInput.trim();
    if (tag && !newNote.tags.includes(tag)) {
      setNewNote({ ...newNote, tags: [...newNote.tags, tag] });
      setTagInput('');
    }
  };

  const removeTag = (tag: string) => {
    setNewNote({ ...newNote, tags: newNote.tags.filter(t => t !== tag) });
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="notes-container">
      {/* 页面标题 */}
      <motion.div
        className="page-header"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <h1>📝 学习笔记</h1>
        <p>记录你的知识宝藏</p>
      </motion.div>

      {/* 搜索栏 */}
      <motion.div
        className="search-bar"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
      >
        <Search size={18} />
        <input
          type="text"
          placeholder="搜索笔记..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
      </motion.div>

      {/* 笔记列表 */}
      <div className="notes-list">
        <AnimatePresence>
          {filteredNotes.length === 0 ? (
            <motion.div
              className="empty-state"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
            >
              <FileText size={48} />
              <p>暂无笔记</p>
              <button className="btn btn-primary" onClick={() => setShowCreateDialog(true)}>
                创建笔记
              </button>
            </motion.div>
          ) : (
            filteredNotes.map((note, index) => {
              const sourceConfig = SOURCE_CONFIG[note.sourceType];

              return (
                <motion.div
                  key={note.id}
                  className="note-card"
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -20 }}
                  transition={{ delay: index * 0.05 }}
                  onClick={() => setSelectedNote(note)}
                >
                  <div className="note-header">
                    <span
                      className="source-badge"
                      style={{ background: sourceConfig.color }}
                    >
                      {sourceConfig.icon} {sourceConfig.label}
                    </span>
                    <span className="note-date">
                      <Clock size={12} />
                      {formatDate(note.createdAt)}
                    </span>
                  </div>

                  <h3>{note.title}</h3>
                  <p className="note-preview">
                    {note.content.substring(0, 100)}
                    {note.content.length > 100 && '...'}
                  </p>

                  {note.tags.length > 0 && (
                    <div className="note-tags">
                      {note.tags.slice(0, 3).map(tag => (
                        <span key={tag} className="tag">#{tag}</span>
                      ))}
                      {note.tags.length > 3 && (
                        <span className="tag more">+{note.tags.length - 3}</span>
                      )}
                    </div>
                  )}

                  <div className="note-footer">
                    <span className="review-count">
                      <BookOpen size={12} />
                      复习 {note.reviewCount} 次
                    </span>
                    <button
                      className="delete-btn"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleDeleteNote(note.id);
                      }}
                    >
                      <Trash2 size={14} />
                    </button>
                  </div>
                </motion.div>
              );
            })
          )}
        </AnimatePresence>
      </div>

      {/* 添加笔记按钮 */}
      <motion.button
        className="fab-add"
        onClick={() => setShowCreateDialog(true)}
        whileHover={{ scale: 1.1 }}
        whileTap={{ scale: 0.9 }}
      >
        <Plus size={24} />
      </motion.button>

      {/* 创建笔记对话框 */}
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
                <h2>创建笔记</h2>
                <button className="close-btn" onClick={() => setShowCreateDialog(false)}>
                  <X size={20} />
                </button>
              </div>

              <div className="dialog-content">
                <div className="form-group">
                  <label>标题 *</label>
                  <input
                    type="text"
                    className="input"
                    placeholder="笔记标题"
                    value={newNote.title}
                    onChange={(e) => setNewNote({ ...newNote, title: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label>内容</label>
                  <textarea
                    className="input textarea"
                    placeholder="写下你的想法..."
                    value={newNote.content}
                    onChange={(e) => setNewNote({ ...newNote, content: e.target.value })}
                    rows={8}
                  />
                </div>

                <div className="form-group">
                  <label>标签</label>
                  <div className="tags-input">
                    {newNote.tags.map(tag => (
                      <span key={tag} className="tag removable">
                        #{tag}
                        <button onClick={() => removeTag(tag)}>×</button>
                      </span>
                    ))}
                    <input
                      type="text"
                      placeholder="添加标签"
                      value={tagInput}
                      onChange={(e) => setTagInput(e.target.value)}
                      onKeyPress={(e) => e.key === 'Enter' && addTag()}
                    />
                  </div>
                </div>
              </div>

              <div className="dialog-footer">
                <button className="btn btn-outline" onClick={() => setShowCreateDialog(false)}>
                  取消
                </button>
                <button
                  className="btn btn-primary"
                  onClick={handleCreateNote}
                  disabled={!newNote.title.trim()}
                >
                  创建笔记
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* 笔记详情对话框 */}
      <AnimatePresence>
        {selectedNote && (
          <motion.div
            className="dialog-overlay"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setSelectedNote(null)}
          >
            <motion.div
              className="note-detail-dialog"
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.9, opacity: 0 }}
              onClick={(e) => e.stopPropagation()}
            >
              <div className="dialog-header">
                <h2>{selectedNote.title}</h2>
                <button className="close-btn" onClick={() => setSelectedNote(null)}>
                  <X size={20} />
                </button>
              </div>

              <div className="dialog-content">
                <div className="note-meta">
                  <span className="source-badge" style={{ background: SOURCE_CONFIG[selectedNote.sourceType].color }}>
                    {SOURCE_CONFIG[selectedNote.sourceType].icon} {SOURCE_CONFIG[selectedNote.sourceType].label}
                  </span>
                  <span className="note-date">
                    {formatDate(selectedNote.createdAt)}
                  </span>
                </div>

                <div className="note-content">
                  {selectedNote.content.split('\n').map((line, i) => (
                    <p key={i}>{line}</p>
                  ))}
                </div>

                {selectedNote.tags.length > 0 && (
                  <div className="note-tags">
                    {selectedNote.tags.map(tag => (
                      <span key={tag} className="tag">#{tag}</span>
                    ))}
                  </div>
                )}

                {selectedNote.aiExplanation && (
                  <div className="ai-explanation">
                    <Sparkles size={16} />
                    <p>{selectedNote.aiExplanation}</p>
                  </div>
                )}
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default Notes;