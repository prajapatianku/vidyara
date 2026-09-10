import React, { useState, useEffect, useCallback } from 'react';
import { MarketingPages } from './pages/MarketingPages';
import { AuthPage } from './pages/AuthPage';
import { OwnerPortal } from './pages/OwnerPortal';
import { SuperAdminPortal } from './pages/SuperAdminPortal';
import { StudentRegistrationForm } from './pages/StudentRegistrationForm';

export type ViewType = 'marketing' | 'auth' | 'owner' | 'superadmin' | 'register-student';

export const parseLocation = (): { view: ViewType; route: string } => {
  const search = window.location.search || '';
  const hash = window.location.hash || '';
  const pathname = window.location.pathname || '';

  if (
    search.includes('register') ||
    search.includes('accId') ||
    hash.includes('register') ||
    pathname.includes('register')
  ) {
    return { view: 'register-student', route: '/' };
  }
  if (hash === '#auth' || hash === '#login' || pathname.endsWith('/auth') || pathname.endsWith('/login')) {
    return { view: 'auth', route: '/' };
  }
  if (hash === '#owner' || pathname.endsWith('/owner')) {
    return { view: 'owner', route: '/' };
  }
  if (hash === '#superadmin' || pathname.endsWith('/superadmin')) {
    return { view: 'superadmin', route: '/' };
  }
  if (hash === '#features' || pathname.endsWith('/features')) {
    return { view: 'marketing', route: '/features' };
  }
  if (hash === '#pricing' || pathname.endsWith('/pricing')) {
    return { view: 'marketing', route: '/pricing' };
  }
  if (hash === '#how-it-works' || pathname.endsWith('/how-it-works')) {
    return { view: 'marketing', route: '/how-it-works' };
  }
  if (hash === '#terms' || pathname.endsWith('/terms')) {
    return { view: 'marketing', route: '/terms' };
  }
  return { view: 'marketing', route: '/' };
};

export const App: React.FC = () => {
  const initial = parseLocation();
  const [view, setView] = useState<ViewType>(initial.view);
  const [marketingRoute, setMarketingRoute] = useState<string>(initial.route);
  const [activeAccount, setActiveAccount] = useState<any>(null);

  // Centralized navigation helper that updates browser history so Back/Forward buttons work cleanly
  const navigateTo = useCallback((newView: ViewType, newRoute: string = '/', pushHistory: boolean = true) => {
    setView(newView);
    setMarketingRoute(newRoute);

    if (pushHistory) {
      let targetHash = '#home';
      if (newView === 'auth') targetHash = '#auth';
      else if (newView === 'owner') targetHash = '#owner';
      else if (newView === 'superadmin') targetHash = '#superadmin';
      else if (newView === 'register-student') {
        const search = window.location.search || '';
        targetHash = search.includes('register') ? window.location.search : '#register-student';
      } else if (newView === 'marketing') {
        if (newRoute === '/features') targetHash = '#features';
        else if (newRoute === '/pricing') targetHash = '#pricing';
        else if (newRoute === '/how-it-works') targetHash = '#how-it-works';
        else if (newRoute === '/terms') targetHash = '#terms';
        else targetHash = '#home';
      }

      if (window.location.hash !== targetHash) {
        window.history.pushState({ view: newView, route: newRoute }, '', targetHash);
      }
    }
  }, []);

  const handleGoBack = useCallback(() => {
    if (window.history.length > 1) {
      window.history.back();
    } else {
      navigateTo('marketing', '/', true);
    }
  }, [navigateTo]);

  useEffect(() => {
    const syncRouteFromUrl = () => {
      const parsed = parseLocation();
      setView(parsed.view);
      setMarketingRoute(parsed.route);
    };

    window.addEventListener('popstate', syncRouteFromUrl);
    window.addEventListener('hashchange', syncRouteFromUrl);

    // Restore session from localStorage if available (unless on register-student form)
    try {
      const search = window.location.search || '';
      const hash = window.location.hash || '';
      if (
        !search.includes('register') &&
        !search.includes('accId') &&
        !hash.includes('register')
      ) {
        const savedSession = localStorage.getItem('vidyara_active_session');
        if (savedSession) {
          const parsed = JSON.parse(savedSession);
          if (parsed.type === 'owner' && parsed.account) {
            setActiveAccount(parsed.account);
            if (!hash || hash === '#home') {
              setView('owner');
            }
          } else if (parsed.type === 'superadmin') {
            if (!hash || hash === '#home') {
              setView('superadmin');
            }
          }
        }
      }
    } catch (e) {
      console.error('Failed to load session:', e);
    }

    return () => {
      window.removeEventListener('popstate', syncRouteFromUrl);
      window.removeEventListener('hashchange', syncRouteFromUrl);
    };
  }, []);

  const handleLoginOwnerSuccess = (accountData: any) => {
    setActiveAccount(accountData);
    localStorage.setItem('vidyara_active_session', JSON.stringify({ type: 'owner', account: accountData }));
    navigateTo('owner', '/', true);
  };

  const handleLoginSuperAdminSuccess = () => {
    localStorage.setItem('vidyara_active_session', JSON.stringify({ type: 'superadmin' }));
    navigateTo('superadmin', '/', true);
  };

  const handleLogout = () => {
    localStorage.removeItem('vidyara_active_session');
    setActiveAccount(null);
    navigateTo('auth', '/', true);
  };

  return (
    <div style={{ width: '100%', minHeight: '100vh', overflowX: 'hidden' }}>
      {view === 'register-student' && (
        <StudentRegistrationForm
          onBackToHome={() => handleGoBack()}
        />
      )}

      {view === 'marketing' && (
        <MarketingPages
          currentRoute={marketingRoute}
          onNavigate={(route) => navigateTo('marketing', route, true)}
          onLaunchOwnerPortal={() => navigateTo('auth', '/', true)}
          onLaunchSuperAdmin={() => navigateTo('auth', '/', true)}
        />
      )}

      {view === 'auth' && (
        <AuthPage
          onLoginOwnerSuccess={handleLoginOwnerSuccess}
          onLoginSuperAdminSuccess={handleLoginSuperAdminSuccess}
          onBackToMarketing={() => handleGoBack()}
        />
      )}

      {view === 'owner' && activeAccount && (
        <OwnerPortal
          accountData={activeAccount}
          onLogout={handleLogout}
          onBackToMarketing={() => navigateTo('marketing', '/', true)}
        />
      )}

      {view === 'superadmin' && (
        <SuperAdminPortal
          onLogout={handleLogout}
          onBackToMarketing={() => navigateTo('marketing', '/', true)}
        />
      )}
    </div>
  );
};

export default App;
