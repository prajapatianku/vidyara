import React, { useState, useEffect } from 'react';
import { MarketingPages } from './pages/MarketingPages';
import { AuthPage } from './pages/AuthPage';
import { OwnerPortal } from './pages/OwnerPortal';
import { SuperAdminPortal } from './pages/SuperAdminPortal';
import { StudentRegistrationForm } from './pages/StudentRegistrationForm';

export const App: React.FC = () => {
  const [view, setView] = useState<'marketing' | 'auth' | 'owner' | 'superadmin' | 'register-student'>('marketing');
  const [marketingRoute, setMarketingRoute] = useState<string>('/');
  const [activeAccount, setActiveAccount] = useState<any>(null);

  useEffect(() => {
    // Check initial hash route
    const checkHash = () => {
      const hash = window.location.hash;
      if (hash.startsWith('#register-student')) {
        setView('register-student');
      }
    };

    checkHash();
    window.addEventListener('hashchange', checkHash);

    // Restore session from localStorage if available
    try {
      if (!window.location.hash.startsWith('#register-student')) {
        const savedSession = localStorage.getItem('vidyara_active_session');
        if (savedSession) {
          const parsed = JSON.parse(savedSession);
          if (parsed.type === 'owner' && parsed.account) {
            setActiveAccount(parsed.account);
            setView('owner');
          } else if (parsed.type === 'superadmin') {
            setView('superadmin');
          }
        }
      }
    } catch (e) {
      console.error('Failed to load session:', e);
    }

    return () => window.removeEventListener('hashchange', checkHash);
  }, []);

  const handleLoginOwnerSuccess = (accountData: any) => {
    setActiveAccount(accountData);
    localStorage.setItem('vidyara_active_session', JSON.stringify({ type: 'owner', account: accountData }));
    setView('owner');
  };

  const handleLoginSuperAdminSuccess = () => {
    localStorage.setItem('vidyara_active_session', JSON.stringify({ type: 'superadmin' }));
    setView('superadmin');
  };

  const handleLogout = () => {
    localStorage.removeItem('vidyara_active_session');
    setActiveAccount(null);
    setView('auth');
  };

  return (
    <div>
      {view === 'register-student' && (
        <StudentRegistrationForm
          onBackToHome={() => {
            window.location.hash = '';
            setView('marketing');
          }}
        />
      )}

      {view === 'marketing' && (
        <MarketingPages
          currentRoute={marketingRoute}
          onNavigate={(route) => setMarketingRoute(route)}
          onLaunchOwnerPortal={() => setView('auth')}
          onLaunchSuperAdmin={() => setView('auth')}
        />
      )}

      {view === 'auth' && (
        <AuthPage
          onLoginOwnerSuccess={handleLoginOwnerSuccess}
          onLoginSuperAdminSuccess={handleLoginSuperAdminSuccess}
          onBackToMarketing={() => setView('marketing')}
        />
      )}

      {view === 'owner' && activeAccount && (
        <OwnerPortal
          accountData={activeAccount}
          onLogout={handleLogout}
          onBackToMarketing={() => setView('marketing')}
        />
      )}

      {view === 'superadmin' && (
        <SuperAdminPortal
          onLogout={handleLogout}
          onBackToMarketing={() => setView('marketing')}
        />
      )}
    </div>
  );
};

export default App;
