import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useAppStore } from './store/useAppStore';

// 页面组件
import Login from './pages/Auth/Login';
import Register from './pages/Auth/Register';
import Home from './pages/Home/Home';
import AIAssistant from './pages/AI/AI';
import Greenhouse from './pages/Greenhouse/Greenhouse';
import CourseDetail from './pages/Greenhouse/CourseDetail';
import Tasks from './pages/Tasks/Tasks';
import Profile from './pages/Profile/Profile';
import Notes from './pages/Notes/Notes';
import Achievements from './pages/Achievements/Achievements';

// 样式
import './styles/globals.css';

const queryClient = new QueryClient();

// 受保护的路由
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading, loadUser } = useAppStore();
  const [checked, setChecked] = React.useState(false);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token && !isAuthenticated) {
      loadUser().finally(() => setChecked(true));
    } else {
      setChecked(true);
    }
  }, []);

  if (isLoading || !checked) {
    return (
      <div className="loading-screen">
        <div className="loading-spinner"></div>
        <p>加载中...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

// 顶部导航栏
const TopNav: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAppStore();

  const navItems = [
    { path: '/', icon: '🏠', label: '首页' },
    { path: '/greenhouse', icon: '🌍', label: '温室' },
    { path: '/ai', icon: '✨', label: 'AI助手' },
    { path: '/tasks', icon: '📋', label: '任务' },
    { path: '/notes', icon: '📝', label: '笔记' },
  ];

  const isActive = (path: string) => {
    if (path === '/') return location.pathname === '/';
    return location.pathname.startsWith(path);
  };

  return (
    <header className="top-nav">
      <div className="nav-brand" onClick={() => navigate('/')}>
        <span className="brand-icon">✨</span>
        <span className="brand-name">课灵 KeLing</span>
      </div>

      <nav className="nav-links">
        {navItems.map((item) => (
          <button
            key={item.path}
            className={`nav-link ${isActive(item.path) ? 'active' : ''}`}
            onClick={() => navigate(item.path)}
          >
            <span className="nav-icon">{item.icon}</span>
            <span>{item.label}</span>
          </button>
        ))}
      </nav>

      <div className="nav-user">
        <div className="user-stats">
          <span className="stat energy">⚡ {user?.energy || 0}</span>
          <span className="stat crystals">💎 {user?.crystals || 0}</span>
        </div>
        <div className="user-avatar" onClick={() => navigate('/profile')}>
          {user?.name?.charAt(0) || '园'}
        </div>
      </div>
    </header>
  );
};

// 主布局
const MainLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <div className="app-layout">
      <TopNav />
      <main className="main-content">
        {children}
      </main>
    </div>
  );
};

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Router>
        <Routes>
          {/* 认证页面 */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* 主应用页面 */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <MainLayout>
                  <Home />
                </MainLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/ai"
            element={
              <ProtectedRoute>
                <MainLayout>
                  <AIAssistant />
                </MainLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/greenhouse"
            element={
              <ProtectedRoute>
                <MainLayout>
                  <Greenhouse />
                </MainLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/greenhouse/:id"
            element={
              <ProtectedRoute>
                <MainLayout>
                  <CourseDetail />
                </MainLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/tasks"
            element={
              <ProtectedRoute>
                <MainLayout>
                  <Tasks />
                </MainLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/notes"
            element={
              <ProtectedRoute>
                <MainLayout>
                  <Notes />
                </MainLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/achievements"
            element={
              <ProtectedRoute>
                <MainLayout>
                  <Achievements />
                </MainLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <MainLayout>
                  <Profile />
                </MainLayout>
              </ProtectedRoute>
            }
          />

          {/* 默认重定向 */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Router>
    </QueryClientProvider>
  );
}

export default App;