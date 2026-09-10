import React, { useState, useEffect } from 'react';
import { MarketingPages } from './pages/MarketingPages';
import { AuthPage } from './pages/AuthPage';
import { OwnerPortal } from './pages/OwnerPortal';
import { SuperAdminPortal } from './pages/SuperAdminPortal';
import { StudentRegistrationForm } from './pages/StudentRegistrationForm';

export const App: React.FC = () => {
  // Initialize view state immediately on first render based on URL search / hash / pathname
  const [view, setView] = useState<'marketing' | 'auth' | 'owner' | 'superadmin' | 'register-student'>(() => {
    const search = window.location.search || '';
    const hash = window.location.hash || '';
    const href = window.location.href || '';
    const pathname = window.location.pathname || '';

    if (
      search.includes('register') ||
      search.includes('accId') ||
      hash.includes('register') ||
      href.includes('register') ||
      pathname.includes('register')
    ) {
      return 'register-student';
    }
    return 'marketing';
  });

  const [marketingRoute, setMarketingRoute] = useState<string>('/');
  const [activeAccount, setActiveAccount] = useState<any>(null);

  useEffect(() => {
    // Check route changes dynamically for camera QR scans and link clicks
    const checkRoute = () => {
      const search = window.location.search || '';
      const hash = window.location.hash || '';
      const href = window.location.href || '';
      const pathname = window.location.pathname || '';

      if (
        search.includes('register') ||
        search.includes('accId') ||
        hash.includes('register') ||
        href.includes('register') ||
        pathname.includes('register')
      ) {
        setView('register-student');
      }
    };

    checkRoute();
    window.addEventListener('hashchange', checkRoute);
    window.addEventListener('popstate', checkRoute);

    // Restore session from localStorage if available (unless on register-student form)
    try {
      const search = window.location.search || '';
      const hash = window.location.hash || '';
      const href = window.location.href || '';
      if (
        !search.includes('register') &&
        !search.includes('accId') &&
        !hash.includes('register') &&
        !href.includes('register')
      ) {
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

    return () => {
      window.removeEventListener('hashchange', checkRoute);
      window.removeEventListener('popstate', checkRoute);
    };
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
    <div style={{ width: '100%', minHeight: '100vh', overflowX: 'hidden' }}>
      {view === 'register-student' && (
        <StudentRegistrationForm
          onBackToHome={() => {
            window.location.hash = '';
            if (window.history.pushState) {
              window.history.pushState('', document.title, window.location.pathname);
            }
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
