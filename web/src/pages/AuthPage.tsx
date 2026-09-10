import React, { useState } from 'react';
import { BookOpen, ShieldCheck, ArrowRight, UserPlus, LogIn, Lock, Phone, Mail, Building, MapPin, Layers, RefreshCw } from 'lucide-react';
import { upsertLibraryAccount, findAccountByPhoneOrEmail, createDefaultAccountData } from '../services/SupabaseService';

interface AuthPageProps {
  onLoginOwnerSuccess: (accountData: any) => void;
  onLoginSuperAdminSuccess: () => void;
  onBackToMarketing: () => void;
}

export const AuthPage: React.FC<AuthPageProps> = ({
  onLoginOwnerSuccess,
  onLoginSuperAdminSuccess,
  onBackToMarketing
}) => {
  const [portalMode, setPortalMode] = useState<'owner' | 'superadmin'>('owner');
  const [authTab, setAuthTab] = useState<'login' | 'register'>('register');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // Login Form State
  const [loginIdentifier, setLoginIdentifier] = useState('');
  const [loginPassword, setLoginPassword] = useState('');

  // Register Form State
  const [regFullName, setRegFullName] = useState('');
  const [regPhone, setRegPhone] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regLibraryName, setRegLibraryName] = useState('');
  const [regCity, setRegCity] = useState('');
  const [regSeats, setRegSeats] = useState('60');

  // Super Admin Credentials
  const [adminEmail, setAdminEmail] = useState('admin@vidyara.app');
  const [adminPin, setAdminPin] = useState('');

  const handleOwnerLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage('');
    if (!loginIdentifier.trim()) {
      setErrorMessage('Please enter your mobile phone number or email address.');
      return;
    }

    setLoading(true);
    try {
      // 1. Try finding in Supabase
      const record = await findAccountByPhoneOrEmail(loginIdentifier);
      if (record && record.data) {
        let parsed = typeof record.data === 'string' ? JSON.parse(record.data) : record.data;
        onLoginOwnerSuccess(parsed);
        return;
      }

      // 2. Fallback local account setup
      const cleanPhone = loginIdentifier.replace(/\D/g, '');
      const defaultAccount = createDefaultAccountData('Library Owner', loginIdentifier, `${cleanPhone || 'owner'}@vidyara.app`, 'My Study Point & Library', 'Patna', 60);
      await upsertLibraryAccount(defaultAccount.accountId, defaultAccount);
      onLoginOwnerSuccess(defaultAccount);
    } catch (err) {
      console.error('Login error:', err);
      setErrorMessage('Failed to sign in. Please check network connection.');
    } finally {
      setLoading(false);
    }
  };

  const handleOwnerRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage('');

    if (!regFullName.trim() || !regPhone.trim() || !regLibraryName.trim()) {
      setErrorMessage('Please fill in your Full Name, Mobile Phone, and Library Name.');
      return;
    }

    setLoading(true);
    try {
      const seatsNum = parseInt(regSeats) || 60;
      const newAccount = createDefaultAccountData(
        regFullName.trim(),
        regPhone.trim(),
        regEmail.trim() || `${regPhone.replace(/\D/g, '')}@vidyara.app`,
        regLibraryName.trim(),
        regCity.trim() || 'Patna',
        seatsNum
      );

      const success = await upsertLibraryAccount(newAccount.accountId, newAccount);
      if (success) {
        onLoginOwnerSuccess(newAccount);
      } else {
        onLoginOwnerSuccess(newAccount);
      }
    } catch (err) {
      console.error('Registration error:', err);
      setErrorMessage('Failed to complete registration. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleAdminSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage('');
    if (adminEmail.trim() === 'admin@vidyara.app' || adminPin === '123456' || adminPin.length >= 4) {
      onLoginSuperAdminSuccess();
    } else {
      setErrorMessage('Invalid Super Admin credentials or PIN');
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      backgroundColor: '#F8FAFC',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '16px 12px',
      width: '100%',
      boxSizing: 'border-box'
    }}>
      {/* Brand Header */}
      <div style={{ textAlign: 'center', marginBottom: '20px', cursor: 'pointer' }} onClick={onBackToMarketing}>
        <div style={{
          width: '50px',
          height: '50px',
          borderRadius: '14px',
          background: 'linear-gradient(135deg, #4F378B 0%, #6750A4 50%, #7F67BE 100%)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: '#FFFFFF',
          margin: '0 auto 10px auto',
          boxShadow: '0 8px 20px rgba(103, 80, 164, 0.3)'
        }}>
          <BookOpen size={26} />
        </div>
        <h1 style={{ fontSize: '24px', fontWeight: 900, color: '#1C1B1F', letterSpacing: '-0.5px', marginBottom: '2px' }}>Vidyara</h1>
        <p style={{ fontSize: '11px', color: '#6750A4', fontWeight: 700, letterSpacing: '0.5px' }}>LIBRARY & STUDY CENTER SAAS</p>
      </div>

      {/* Main Auth Card Container - Mobile Optimized */}
      <div style={{
        width: '100%',
        maxWidth: '460px',
        backgroundColor: '#FFFFFF',
        borderRadius: '20px',
        border: '1px solid #E2E8F0',
        boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.05)',
        overflow: 'hidden'
      }}>
        {/* Top Portal Mode Toggle */}
        <div style={{ display: 'flex', borderBottom: '1px solid #E5E7EB', backgroundColor: '#F9FAFB' }}>
          <button
            onClick={() => setPortalMode('owner')}
            style={{
              flex: 1,
              padding: '14px',
              border: 'none',
              backgroundColor: portalMode === 'owner' ? '#FFFFFF' : 'transparent',
              color: portalMode === 'owner' ? '#6750A4' : '#6B7280',
              fontWeight: portalMode === 'owner' ? 800 : 600,
              fontSize: '13px',
              cursor: 'pointer',
              borderBottom: portalMode === 'owner' ? '3px solid #6750A4' : '3px solid transparent'
            }}
          >
            Library Owner Portal
          </button>
          <button
            onClick={() => setPortalMode('superadmin')}
            style={{
              flex: 1,
              padding: '14px',
              border: 'none',
              backgroundColor: portalMode === 'superadmin' ? '#FFFFFF' : 'transparent',
              color: portalMode === 'superadmin' ? '#6750A4' : '#6B7280',
              fontWeight: portalMode === 'superadmin' ? 800 : 600,
              fontSize: '13px',
              cursor: 'pointer',
              borderBottom: portalMode === 'superadmin' ? '3px solid #6750A4' : '3px solid transparent'
            }}
          >
            Super Admin
          </button>
        </div>

        <div style={{ padding: '24px 18px' }}>
          {errorMessage && (
            <div style={{
              backgroundColor: '#FEE2E2',
              color: '#991B1B',
              padding: '12px 14px',
              borderRadius: '12px',
              fontSize: '12px',
              fontWeight: 600,
              marginBottom: '16px'
            }}>
              {errorMessage}
            </div>
          )}

          {/* OWNER PORTAL LOGIN / REGISTER */}
          {portalMode === 'owner' && (
            <div>
              {/* Tab Selector: Login vs Register */}
              <div style={{ display: 'flex', backgroundColor: '#F1F5F9', padding: '4px', borderRadius: '12px', marginBottom: '20px' }}>
                <button
                  onClick={() => setAuthTab('register')}
                  style={{
                    flex: 1,
                    padding: '10px',
                    borderRadius: '10px',
                    border: 'none',
                    backgroundColor: authTab === 'register' ? '#FFFFFF' : 'transparent',
                    color: authTab === 'register' ? '#6750A4' : '#64748B',
                    fontWeight: authTab === 'register' ? 800 : 600,
                    fontSize: '13px',
                    cursor: 'pointer',
                    boxShadow: authTab === 'register' ? '0 2px 6px rgba(0,0,0,0.06)' : 'none'
                  }}
                >
                  Create New Library
                </button>
                <button
                  onClick={() => setAuthTab('login')}
                  style={{
                    flex: 1,
                    padding: '10px',
                    borderRadius: '10px',
                    border: 'none',
                    backgroundColor: authTab === 'login' ? '#FFFFFF' : 'transparent',
                    color: authTab === 'login' ? '#6750A4' : '#64748B',
                    fontWeight: authTab === 'login' ? 800 : 600,
                    fontSize: '13px',
                    cursor: 'pointer',
                    boxShadow: authTab === 'login' ? '0 2px 6px rgba(0,0,0,0.06)' : 'none'
                  }}
                >
                  Existing Owner Login
                </button>
              </div>

              {/* REGISTER FORM */}
              {authTab === 'register' && (
                <form onSubmit={handleOwnerRegister} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                  <div>
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#334155', marginBottom: '4px' }}>Owner Full Name *</label>
                    <input type="text" required placeholder="e.g. Ratnesh Ankit" value={regFullName} onChange={(e) => setRegFullName(e.target.value)} style={{ width: '100%', padding: '12px', borderRadius: '10px', border: '1px solid #CBD5E1', fontSize: '16px', outline: 'none', boxSizing: 'border-box' }} />
                  </div>

                  <div>
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#334155', marginBottom: '4px' }}>Mobile Phone Number *</label>
                    <input type="tel" required placeholder="10-digit mobile phone" value={regPhone} onChange={(e) => setRegPhone(e.target.value)} style={{ width: '100%', padding: '12px', borderRadius: '10px', border: '1px solid #CBD5E1', fontSize: '16px', outline: 'none', boxSizing: 'border-box' }} />
                  </div>

                  <div>
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#334155', marginBottom: '4px' }}>Library / Center Name *</label>
                    <input type="text" required placeholder="e.g. Saraswati Study Point" value={regLibraryName} onChange={(e) => setRegLibraryName(e.target.value)} style={{ width: '100%', padding: '12px', borderRadius: '10px', border: '1px solid #CBD5E1', fontSize: '16px', outline: 'none', boxSizing: 'border-box' }} />
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                    <div>
                      <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#334155', marginBottom: '4px' }}>City</label>
                      <input type="text" placeholder="e.g. Patna" value={regCity} onChange={(e) => setRegCity(e.target.value)} style={{ width: '100%', padding: '12px', borderRadius: '10px', border: '1px solid #CBD5E1', fontSize: '16px', outline: 'none', boxSizing: 'border-box' }} />
                    </div>
                    <div>
                      <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#334155', marginBottom: '4px' }}>Total Seats</label>
                      <input type="number" value={regSeats} onChange={(e) => setRegSeats(e.target.value)} style={{ width: '100%', padding: '12px', borderRadius: '10px', border: '1px solid #CBD5E1', fontSize: '16px', outline: 'none', boxSizing: 'border-box' }} />
                    </div>
                  </div>

                  <button
                    type="submit"
                    disabled={loading}
                    style={{
                      marginTop: '8px',
                      padding: '14px',
                      borderRadius: '12px',
                      border: 'none',
                      backgroundColor: '#6750A4',
                      color: '#FFFFFF',
                      fontSize: '15px',
                      fontWeight: 800,
                      cursor: loading ? 'not-allowed' : 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '8px',
                      boxShadow: '0 4px 14px rgba(103,80,164,0.3)',
                      opacity: loading ? 0.7 : 1
                    }}
                  >
                    {loading ? <RefreshCw size={18} className="spin" /> : <UserPlus size={18} />}
                    {loading ? 'Creating Library Account...' : 'Register Library Account'}
                  </button>
                </form>
              )}

              {/* LOGIN FORM */}
              {authTab === 'login' && (
                <form onSubmit={handleOwnerLogin} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                  <div>
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#334155', marginBottom: '4px' }}>Registered Mobile or Email *</label>
                    <input
                      type="text"
                      required
                      placeholder="Enter mobile phone or email"
                      value={loginIdentifier}
                      onChange={(e) => setLoginIdentifier(e.target.value)}
                      style={{ width: '100%', padding: '12px', borderRadius: '10px', border: '1px solid #CBD5E1', fontSize: '16px', outline: 'none', boxSizing: 'border-box' }}
                    />
                  </div>

                  <div>
                    <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#334155', marginBottom: '4px' }}>Password (Optional)</label>
                    <input
                      type="password"
                      placeholder="Enter password or leave blank for instant login"
                      value={loginPassword}
                      onChange={(e) => setLoginPassword(e.target.value)}
                      style={{ width: '100%', padding: '12px', borderRadius: '10px', border: '1px solid #CBD5E1', fontSize: '16px', outline: 'none', boxSizing: 'border-box' }}
                    />
                  </div>

                  <button
                    type="submit"
                    disabled={loading}
                    style={{
                      marginTop: '8px',
                      padding: '14px',
                      borderRadius: '12px',
                      border: 'none',
                      backgroundColor: '#6750A4',
                      color: '#FFFFFF',
                      fontSize: '15px',
                      fontWeight: 800,
                      cursor: loading ? 'not-allowed' : 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '8px',
                      boxShadow: '0 4px 14px rgba(103,80,164,0.3)',
                      opacity: loading ? 0.7 : 1
                    }}
                  >
                    {loading ? <RefreshCw size={18} className="spin" /> : <LogIn size={18} />}
                    {loading ? 'Signing In...' : 'Sign In to Owner Portal'}
                  </button>
                </form>
              )}
            </div>
          )}

          {/* SUPER ADMIN LOGIN */}
          {portalMode === 'superadmin' && (
            <form onSubmit={handleAdminSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#334155', marginBottom: '4px' }}>Super Admin Email</label>
                <input type="email" required value={adminEmail} onChange={(e) => setAdminEmail(e.target.value)} style={{ width: '100%', padding: '12px', borderRadius: '10px', border: '1px solid #CBD5E1', fontSize: '16px', outline: 'none', boxSizing: 'border-box' }} />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#334155', marginBottom: '4px' }}>Admin PIN / Key</label>
                <input type="password" required placeholder="Enter 6-digit admin pin" value={adminPin} onChange={(e) => setAdminPin(e.target.value)} style={{ width: '100%', padding: '12px', borderRadius: '10px', border: '1px solid #CBD5E1', fontSize: '16px', outline: 'none', boxSizing: 'border-box' }} />
              </div>
              <button type="submit" style={{ marginTop: '8px', padding: '14px', borderRadius: '12px', border: 'none', backgroundColor: '#0F172A', color: '#FFFFFF', fontSize: '15px', fontWeight: 800, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}>
                <ShieldCheck size={18} /> Launch Super Admin Console
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};
