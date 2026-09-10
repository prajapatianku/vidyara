import React, { useState } from 'react';
import { BookOpen, ShieldCheck, CheckCircle2, QrCode, Smartphone, Users, ChevronRight, Zap, Menu, X } from 'lucide-react';

interface MarketingPagesProps {
  currentRoute: string;
  onNavigate: (route: string) => void;
  onLaunchOwnerPortal: () => void;
  onLaunchSuperAdmin: () => void;
}

export const MarketingPages: React.FC<MarketingPagesProps> = ({
  currentRoute,
  onNavigate,
  onLaunchOwnerPortal,
  onLaunchSuperAdmin
}) => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: '#F8FAFC', width: '100%', overflowX: 'hidden' }}>
      {/* Header / Navbar - Mobile Responsive */}
      <header style={{
        backgroundColor: '#FFFFFF',
        borderBottom: '1px solid #E2E8F0',
        padding: '12px 16px',
        position: 'sticky',
        top: 0,
        zIndex: 100,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        width: '100%'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', cursor: 'pointer' }} onClick={() => onNavigate('/')}>
          <div style={{
            width: '38px',
            height: '38px',
            borderRadius: '10px',
            background: 'linear-gradient(135deg, #4F378B 0%, #6750A4 50%, #7F67BE 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#FFFFFF',
            fontWeight: 'bold',
            fontSize: '18px',
            boxShadow: '0 4px 12px rgba(103, 80, 164, 0.25)',
            flexShrink: 0
          }}>
            <BookOpen size={20} />
          </div>
          <div>
            <h1 style={{ fontSize: '18px', fontWeight: 900, color: '#1C1B1F', letterSpacing: '-0.5px', lineHeight: 1.1 }}>Vidyara</h1>
            <p style={{ fontSize: '9px', color: '#6750A4', fontWeight: 700 }}>LIBRARY SAAS</p>
          </div>
        </div>

        {/* Desktop Navigation */}
        <nav className="mobile-hide" style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <button onClick={() => onNavigate('/')} style={{ background: 'none', border: 'none', fontWeight: currentRoute === '/' ? 700 : 500, color: currentRoute === '/' ? '#6750A4' : '#475569', cursor: 'pointer', fontSize: '14px' }}>Home</button>
          <button onClick={() => onNavigate('/features')} style={{ background: 'none', border: 'none', fontWeight: currentRoute === '/features' ? 700 : 500, color: currentRoute === '/features' ? '#6750A4' : '#475569', cursor: 'pointer', fontSize: '14px' }}>Features</button>
          <button onClick={() => onNavigate('/pricing')} style={{ background: 'none', border: 'none', fontWeight: currentRoute === '/pricing' ? 700 : 500, color: currentRoute === '/pricing' ? '#6750A4' : '#475569', cursor: 'pointer', fontSize: '14px' }}>Pricing</button>
          <button onClick={() => onNavigate('/how-it-works')} style={{ background: 'none', border: 'none', fontWeight: currentRoute === '/how-it-works' ? 700 : 500, color: currentRoute === '/how-it-works' ? '#6750A4' : '#475569', cursor: 'pointer', fontSize: '14px' }}>How it Works</button>
          <button onClick={() => onNavigate('/terms')} style={{ background: 'none', border: 'none', fontWeight: currentRoute === '/terms' ? 700 : 500, color: currentRoute === '/terms' ? '#6750A4' : '#475569', cursor: 'pointer', fontSize: '14px' }}>Terms & Conditions</button>
        </nav>

        {/* Desktop Action Buttons */}
        <div className="mobile-hide" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <button onClick={onLaunchSuperAdmin} style={{ padding: '8px 12px', borderRadius: '8px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', color: '#334155', fontWeight: 600, fontSize: '13px', cursor: 'pointer' }}>Super Admin</button>
          <button onClick={onLaunchOwnerPortal} style={{ padding: '8px 14px', borderRadius: '8px', border: 'none', backgroundColor: '#6750A4', color: '#FFFFFF', fontWeight: 700, fontSize: '13px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px', boxShadow: '0 4px 10px rgba(103,80,164,0.25)' }}>
            Login / Register <ChevronRight size={16} />
          </button>
        </div>

        {/* Mobile Hamburger Menu Toggle Button */}
        <button
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          style={{ display: 'none', padding: '8px', border: 'none', background: 'none', color: '#1C1B1F', cursor: 'pointer' }}
          className="mobile-show"
        >
          {mobileMenuOpen ? <X size={26} /> : <Menu size={26} />}
        </button>
      </header>

      {/* Mobile Drawer Navigation Menu */}
      {mobileMenuOpen && (
        <div style={{ backgroundColor: '#FFFFFF', borderBottom: '1px solid #E2E8F0', padding: '16px', display: 'flex', flexDirection: 'column', gap: '12px', zIndex: 99 }}>
          <button onClick={() => { onNavigate('/'); setMobileMenuOpen(false); }} style={{ padding: '10px', textAlign: 'left', background: 'none', border: 'none', fontSize: '15px', fontWeight: 700, color: currentRoute === '/' ? '#6750A4' : '#1E293B' }}>Home</button>
          <button onClick={() => { onNavigate('/features'); setMobileMenuOpen(false); }} style={{ padding: '10px', textAlign: 'left', background: 'none', border: 'none', fontSize: '15px', fontWeight: 700, color: currentRoute === '/features' ? '#6750A4' : '#1E293B' }}>Features</button>
          <button onClick={() => { onNavigate('/pricing'); setMobileMenuOpen(false); }} style={{ padding: '10px', textAlign: 'left', background: 'none', border: 'none', fontSize: '15px', fontWeight: 700, color: currentRoute === '/pricing' ? '#6750A4' : '#1E293B' }}>Pricing</button>
          <button onClick={() => { onNavigate('/how-it-works'); setMobileMenuOpen(false); }} style={{ padding: '10px', textAlign: 'left', background: 'none', border: 'none', fontSize: '15px', fontWeight: 700, color: currentRoute === '/how-it-works' ? '#6750A4' : '#1E293B' }}>How it Works</button>
          <button onClick={() => { onNavigate('/terms'); setMobileMenuOpen(false); }} style={{ padding: '10px', textAlign: 'left', background: 'none', border: 'none', fontSize: '15px', fontWeight: 700, color: currentRoute === '/terms' ? '#6750A4' : '#1E293B' }}>Terms & Conditions</button>
          <div style={{ borderTop: '1px solid #F1F5F9', paddingTop: '12px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
            <button onClick={() => { onLaunchOwnerPortal(); setMobileMenuOpen(false); }} style={{ padding: '12px', borderRadius: '10px', border: 'none', backgroundColor: '#6750A4', color: '#FFFFFF', fontWeight: 800, fontSize: '14px', cursor: 'pointer', textAlign: 'center' }}>
              Owner Login / Register
            </button>
            <button onClick={() => { onLaunchSuperAdmin(); setMobileMenuOpen(false); }} style={{ padding: '12px', borderRadius: '10px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', color: '#334155', fontWeight: 700, fontSize: '14px', cursor: 'pointer', textAlign: 'center' }}>
              Super Admin Login
            </button>
          </div>
        </div>
      )}

      {/* Main Content Area - Mobile Responsive */}
      <main style={{ flex: 1, padding: '24px 16px', maxWidth: '1100px', margin: '0 auto', width: '100%' }}>
        {currentRoute === '/' && (
          <section style={{ textAlign: 'center' }}>
            <span style={{ backgroundColor: '#E0E7FF', color: '#3730A3', padding: '6px 14px', borderRadius: '20px', fontSize: '11px', fontWeight: 700, display: 'inline-block', marginBottom: '16px' }}>
              🚀 VIDYARA v2.5 NOW LIVE ON VIDYARA.APP
            </span>
            <h1 style={{ fontSize: '28px', fontWeight: 900, color: '#0F172A', lineHeight: 1.25, marginBottom: '16px' }}>
              The Complete SaaS Platform for <span style={{ color: '#0747A6' }}>Library & Study Center Owners</span>
            </h1>
            <p style={{ fontSize: '15px', color: '#475569', maxWidth: '750px', margin: '0 auto 24px auto', lineHeight: 1.6 }}>
              Effortlessly manage seats, shift timings, student fees, live attendance QR scanning, WhatsApp payment reminders, and multi-branch operations — online or offline.
            </p>
            <div style={{ display: 'flex', justifyContent: 'center', gap: '12px', flexWrap: 'wrap', marginBottom: '36px' }}>
              <button onClick={onLaunchOwnerPortal} style={{ padding: '14px 24px', backgroundColor: '#0747A6', color: '#FFFFFF', fontWeight: 800, fontSize: '15px', borderRadius: '12px', border: 'none', cursor: 'pointer', boxShadow: '0 4px 14px rgba(7,71,166,0.3)', width: '100%', maxWidth: '280px' }}>
                Get Started Free
              </button>
              <button onClick={() => onNavigate('/pricing')} style={{ padding: '14px 24px', backgroundColor: '#FFFFFF', color: '#0F172A', fontWeight: 700, fontSize: '15px', borderRadius: '12px', border: '1px solid #CBD5E1', cursor: 'pointer', width: '100%', maxWidth: '280px' }}>
                View SaaS Pricing
              </button>
            </div>

            {/* Feature Cards Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '16px', textAlign: 'left' }}>
              <div style={{ backgroundColor: '#FFFFFF', padding: '20px', borderRadius: '16px', border: '1px solid #E2E8F0', boxShadow: '0 2px 8px rgba(0,0,0,0.04)' }}>
                <div style={{ width: '40px', height: '40px', borderRadius: '10px', backgroundColor: '#EFF6FF', color: '#0747A6', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '12px' }}>
                  <BookOpen size={22} />
                </div>
                <h3 style={{ fontSize: '16px', fontWeight: 800, marginBottom: '6px' }}>Visual Seat Layout</h3>
                <p style={{ fontSize: '13px', color: '#64748B', lineHeight: 1.5 }}>Graphical grid of available, occupied, and reserved seats across customizable Morning, Evening, and 24x7 shifts.</p>
              </div>

              <div style={{ backgroundColor: '#FFFFFF', padding: '20px', borderRadius: '16px', border: '1px solid #E2E8F0', boxShadow: '0 2px 8px rgba(0,0,0,0.04)' }}>
                <div style={{ width: '40px', height: '40px', borderRadius: '10px', backgroundColor: '#ECFDF5', color: '#059669', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '12px' }}>
                  <QrCode size={22} />
                </div>
                <h3 style={{ fontSize: '16px', fontWeight: 800, marginBottom: '6px' }}>Student Registration QR</h3>
                <p style={{ fontSize: '13px', color: '#64748B', lineHeight: 1.5 }}>Instant QR code student self-admission form with automated owner approval and digital WhatsApp pass.</p>
              </div>

              <div style={{ backgroundColor: '#FFFFFF', padding: '20px', borderRadius: '16px', border: '1px solid #E2E8F0', boxShadow: '0 2px 8px rgba(0,0,0,0.04)' }}>
                <div style={{ width: '40px', height: '40px', borderRadius: '10px', backgroundColor: '#FFF7ED', color: '#EA580C', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '12px' }}>
                  <Smartphone size={22} />
                </div>
                <h3 style={{ fontSize: '16px', fontWeight: 800, marginBottom: '6px' }}>WhatsApp Reminders</h3>
                <p style={{ fontSize: '13px', color: '#64748B', lineHeight: 1.5 }}>One-click payment reminder dispatch via WhatsApp with pre-filled fee due messages and receipts.</p>
              </div>
            </div>
          </section>
        )}

        {currentRoute === '/pricing' && (
          <section style={{ textAlign: 'center' }}>
            <h2 style={{ fontSize: '28px', fontWeight: 900, marginBottom: '12px' }}>Simple, Transparent SaaS Pricing</h2>
            <p style={{ fontSize: '14px', color: '#64748B', marginBottom: '32px' }}>Choose the perfect plan for your library study center.</p>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '20px', textAlign: 'left' }}>
              <div style={{ backgroundColor: '#FFFFFF', borderRadius: '20px', padding: '24px', border: '1px solid #E2E8F0' }}>
                <h3 style={{ fontSize: '18px', fontWeight: 800 }}>Free Tier</h3>
                <h4 style={{ fontSize: '32px', fontWeight: 900, color: '#0F172A', margin: '12px 0' }}>₹0</h4>
                <p style={{ fontSize: '13px', color: '#64748B', marginBottom: '20px' }}>For single branch libraries up to 20 students.</p>
                <button onClick={onLaunchOwnerPortal} style={{ width: '100%', padding: '12px', backgroundColor: '#6750A4', color: '#FFFFFF', fontWeight: 800, borderRadius: '10px', border: 'none', cursor: 'pointer' }}>Start Free</button>
              </div>

              <div style={{ backgroundColor: '#FFFFFF', borderRadius: '20px', padding: '24px', border: '2px solid #6750A4', position: 'relative' }}>
                <span style={{ position: 'absolute', top: '-12px', right: '20px', backgroundColor: '#6750A4', color: '#FFFFFF', padding: '4px 12px', borderRadius: '12px', fontSize: '11px', fontWeight: 800 }}>MOST POPULAR</span>
                <h3 style={{ fontSize: '18px', fontWeight: 800 }}>Pro Tier</h3>
                <h4 style={{ fontSize: '32px', fontWeight: 900, color: '#6750A4', margin: '12px 0' }}>₹99 <span style={{ fontSize: '14px', color: '#64748B', fontWeight: 500 }}>/ month</span></h4>
                <p style={{ fontSize: '13px', color: '#64748B', marginBottom: '20px' }}>Unlimited students, seats, and WhatsApp reminders.</p>
                <button onClick={onLaunchOwnerPortal} style={{ width: '100%', padding: '12px', backgroundColor: '#6750A4', color: '#FFFFFF', fontWeight: 800, borderRadius: '10px', border: 'none', cursor: 'pointer' }}>Upgrade to Pro</button>
              </div>

              <div style={{ backgroundColor: '#FFFFFF', borderRadius: '20px', padding: '24px', border: '1px solid #E2E8F0' }}>
                <h3 style={{ fontSize: '18px', fontWeight: 800 }}>Business Tier</h3>
                <h4 style={{ fontSize: '32px', fontWeight: 900, color: '#0F172A', margin: '12px 0' }}>₹199 <span style={{ fontSize: '14px', color: '#64748B', fontWeight: 500 }}>/ month</span></h4>
                <p style={{ fontSize: '13px', color: '#64748B', marginBottom: '20px' }}>Multi-branch management & advanced analytics.</p>
                <button onClick={onLaunchOwnerPortal} style={{ width: '100%', padding: '12px', backgroundColor: '#0F172A', color: '#FFFFFF', fontWeight: 800, borderRadius: '10px', border: 'none', cursor: 'pointer' }}>Get Business</button>
              </div>
            </div>
          </section>
        )}
      </main>

      <style>{`
        @media (max-width: 768px) {
          .mobile-hide { display: none !important; }
          .mobile-show { display: block !important; }
        }
        @media (min-width: 769px) {
          .mobile-show { display: none !important; }
        }
      `}</style>
    </div>
  );
};
