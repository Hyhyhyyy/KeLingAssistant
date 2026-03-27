import React, { useState, useRef, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Sparkles, Send, Trash2, Lightbulb, Target, BookOpen, Clock } from 'lucide-react';
import { aiAPI } from '../../services/api';
import { useAppStore } from '../../store/useAppStore';
import './AI.css';

interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  toolCommand?: string;
  toolResult?: any;
}

const AIAssistant: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [sessionId, setSessionId] = useState<string>('');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const { user, courses, tasks } = useAppStore();

  // 快捷场景
  const scenarios = [
    { id: 'QUICK_PLAN', name: '今日计划', icon: <Target size={20} />, prompt: '帮我规划今天的学习安排' },
    { id: 'WEAKNESS_DIAGNOSE', name: '薄弱诊断', icon: <Lightbulb size={20} />, prompt: '分析我的学习薄弱点' },
    { id: 'CONCEPT_EXPLAIN', name: '概念讲解', icon: <BookOpen size={20} />, prompt: '帮我理解一个概念：' },
    { id: 'REVIEW_SESSION', name: '复习回顾', icon: <Clock size={20} />, prompt: '帮我复习今天学的内容' },
  ];

  // 滚动到底部
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // 发送消息
  const sendMessage = async (messageText?: string) => {
    const text = messageText || input.trim();
    if (!text || loading) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      role: 'user',
      content: text,
    };

    setMessages(prev => [...prev, userMessage]);
    setInput('');
    setLoading(true);

    try {
      const { data } = await aiAPI.chat({
        message: text,
        sessionId: sessionId || undefined,
      });

      if (!sessionId && data.sessionId) {
        setSessionId(data.sessionId);
      }

      const assistantMessage: Message = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: data.content,
        toolCommand: data.toolCommand,
        toolResult: data.toolResult,
      };

      setMessages(prev => [...prev, assistantMessage]);
    } catch (error) {
      const errorMessage: Message = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: '抱歉，我遇到了一些问题，请稍后再试。💫',
      };
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setLoading(false);
    }
  };

  // 清空对话
  const clearChat = () => {
    setMessages([]);
    setSessionId('');
  };

  // 处理场景点击
  const handleScenarioClick = (scenario: typeof scenarios[0]) => {
    if (scenario.id === 'CONCEPT_EXPLAIN') {
      setInput(scenario.prompt);
    } else {
      sendMessage(scenario.prompt);
    }
  };

  return (
    <div className="ai-container">
      {/* 头部 */}
      <div className="ai-header">
        <div className="ai-title">
          <motion.div
            className="ai-logo"
            animate={{ rotate: [0, 360] }}
            transition={{ duration: 10, repeat: Infinity, ease: 'linear' }}
          >
            <Sparkles size={28} />
          </motion.div>
          <div>
            <h1>恒星引擎</h1>
            <p>AI 学习助手</p>
          </div>
        </div>
        <button className="clear-btn" onClick={clearChat} title="清空对话">
          <Trash2 size={20} />
        </button>
      </div>

      {/* 用户上下文提示 */}
      <div className="context-bar">
        <span>Lv.{user?.level || 1}</span>
        <span>·</span>
        <span>{courses.length}门课程</span>
        <span>·</span>
        <span>{tasks.filter(t => t.status === 'PENDING').length}个待办</span>
      </div>

      {/* 快捷场景 */}
      {messages.length === 0 && (
        <div className="scenarios">
          <p className="scenarios-title">我能帮你做什么？</p>
          <div className="scenarios-grid">
            {scenarios.map((scenario) => (
              <motion.button
                key={scenario.id}
                className="scenario-btn"
                onClick={() => handleScenarioClick(scenario)}
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
              >
                {scenario.icon}
                <span>{scenario.name}</span>
              </motion.button>
            ))}
          </div>
        </div>
      )}

      {/* 消息列表 */}
      <div className="messages-container">
        <AnimatePresence>
          {messages.map((message) => (
            <motion.div
              key={message.id}
              className={`message ${message.role}`}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
            >
              <div className="message-content">
                {message.content.split('\n').map((line, i) => (
                  <p key={i}>{line}</p>
                ))}
              </div>
              {message.toolResult && (
                <div className="tool-result">
                  <span className="tool-badge">
                    {message.toolResult.success ? '✅' : '❌'} 执行成功
                  </span>
                </div>
              )}
            </motion.div>
          ))}
        </AnimatePresence>

        {loading && (
          <div className="message assistant loading">
            <div className="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* 输入框 */}
      <div className="input-container">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
          placeholder="问我任何关于学习的问题..."
          disabled={loading}
        />
        <motion.button
          className="send-btn"
          onClick={() => sendMessage()}
          disabled={loading || !input.trim()}
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
        >
          <Send size={20} />
        </motion.button>
      </div>
    </div>
  );
};

export default AIAssistant;