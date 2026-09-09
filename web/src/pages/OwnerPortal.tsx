import React, { useState, useEffect } from 'react';
import { BookOpen, Users, LayoutGrid, QrCode, CreditCard, Settings, CheckCircle, RefreshCw, Plus, ArrowLeft, LogOut, Phone, Mail, Trash2, Send, MessageSquare } from 'lucide-react';
import { upsertLibraryAccount } from '../services/SupabaseService';

interface OwnerPortalProps {
  accountData: any;
  onLogout: () => void;
  onBackToMarketing: () => void;
}

export const OwnerPortal: React.FC<OwnerPortalProps> = ({ accountData, onLogout, onBackToMarketing }) => {
  const [account, setAccount] = useState<any>(accountData);
  const [activeTab, setActiveTab] = useState<'dashboard' | 'seats' | 'students' | 'attendance' | 'payments' | 'settings'>('dashboard');
  const [syncState, setSyncState] = useState<'synced' | 'pending' | 'error'>('synced');
  const [lastSyncedTime, setLastSyncedTime] = useState<string>('Just now');

  // Modals & UI Controls
  const [showAddStudentModal, setShowAddStudentModal] = useState(false);
  const [newStudentName, setNewStudentName] = useState('');
  const [newStudentPhone, setNewStudentPhone] = useState('');
  const [newStudentSeat, setNewStudentSeat] = useState('1');
  const [newStudentShift, setNewStudentShift] = useState('Full Day');
  const [newStudentFee, setNewStudentFee] = useState('1200');

  useEffect(() => {
    if (accountData) {
      setAccount(accountData);
    }
  }, [accountData]);

  const handleManualSync = async () => {
    setSyncState('pending');
    const success = await upsertLibraryAccount(account.accountId, account);
    if (success) {
      setSyncState('synced');
      setLastSyncedTime(new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }));
    } else {
      setSyncState('error');
    }
  };

  const handleAddStudent = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newStudentName.trim() || !newStudentPhone.trim()) return;

    const seatNum = parseInt(newStudentSeat) || (account.students?.length || 0) + 1;
    const newStudentObj = {
      id: 'std_' + Date.now(),
      name: newStudentName.trim(),
      phone: newStudentPhone.trim(),
      seatNo: seatNum,
      shift: newStudentShift,
      feeAmount: parseInt(newStudentFee) || 1000,
      dueStatus: 'PAID',
      joinDate: new Date().toISOString().split('T')[0]
    };

    const updatedStudents = [...(account.students || []), newStudentObj];
    const updatedAccount = {
      ...account,
      studentsCount: updatedStudents.length,
      occupiedSeatsCount: Math.min(account.library?.totalSeats || 60, updatedStudents.length),
      students: updatedStudents
    };

    setAccount(updatedAccount);
    setShowAddStudentModal(false);
    setNewStudentName('');
    setNewStudentPhone('');
    await upsertLibraryAccount(updatedAccount.accountId, updatedAccount);
  };

  const handleDeleteStudent = async (studentId: string) => {
    if (!confirm('Are you sure you want to delete this student from database?')) return;
    const updatedStudents = (account.students || []).filter((s: any) => s.id !== studentId);
    const updatedAccount = {
      ...account,
      studentsCount: updatedStudents.length,
      occupiedSeatsCount: Math.min(account.library?.totalSeats || 60, updatedStudents.length),
      students: updatedStudents
    };
    setAccount(updatedAccount);
    await upsertLibraryAccount(updatedAccount.accountId, updatedAccount);
  };

  const openWhatsAppReminder = (student: any) => {
    const libName = (account.library?.name || 'VIDYARA LIBRARY').toUpperCase();
    const message = `🏛️ *${libName}*\n📢 *FEE PAYMENT REMINDER*\n\nDear ${student.name},\nThis is a friendly reminder that your monthly library fee payment of ₹${student.feeAmount} is due. Please clear your dues at your earliest convenience.\n\nThank you!\n*${account.library?.name} Management*`;
    const encoded = encodeURIComponent(message);
    window.open(`https://wa.me/91${student.phone.replace(/\D/g, '')}?text=${encoded}`, '_blank');
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: '#F8FAFC' }}>
      {/* Top Header & Navigation Bar */}
      <header style={{
        backgroundColor: '#FFFFFF',
        borderBottom: '1px solid #E2E8F0',
        padding: '12px 24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        position: 'sticky',
        top: 0,
        zIndex: 100
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <div style={{
            width: '40px',
            height: '40px',
            borderRadius: '12px',
            background: 'linear-gradient(135deg, #4F378B 0%, #6750A4 50%, #7F67BE 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#FFFFFF'
          }}>
            <BookOpen size={22} />
          </div>
          <div>
            <h2 style={{ fontSize: '18px', fontWeight: 900, color: '#1C1B1F', lineHeight: 1.1 }}>
              {account.library?.name || 'Vidyara Library'}
            </h2>
            <p style={{ fontSize: '12px', color: '#6750A4', fontWeight: 700 }}>
              Owner: {account.ownerProfile?.fullName || 'Owner'} ({account.ownerProfile?.phone || ''})
            </p>
          </div>
        </div>

        {/* Sync Status Badge & Action Controls */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{
            padding: '6px 12px',
            borderRadius: '20px',
            fontSize: '12px',
            fontWeight: 700,
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            backgroundColor: syncState === 'synced' ? '#ECFDF5' : '#FEF3C7',
            color: syncState === 'synced' ? '#059669' : '#D97706'
          }}>
            {syncState === 'synced' ? <CheckCircle size={14} /> : <RefreshCw size={14} className="spin" />}
            {syncState === 'synced' ? `Cloud Synced (${lastSyncedTime})` : 'Syncing...'}
          </div>

          <button onClick={handleManualSync} style={{ padding: '8px 14px', borderRadius: '8px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer', fontSize: '12px', fontWeight: 700 }}>
            Sync Cloud
          </button>

          <button onClick={onLogout} style={{
            padding: '8px 16px',
            borderRadius: '8px',
            border: 'none',
            backgroundColor: '#FEE2E2',
            color: '#991B1B',
            fontWeight: 800,
            fontSize: '13px',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '6px'
          }}>
            <LogOut size={16} /> Logout
          </button>
        </div>
      </header>

      {/* Main Layout Container */}
      <div style={{ flex: 1, display: 'flex' }}>
        {/* Sidebar */}
        <aside style={{
          width: '240px',
          backgroundColor: '#FFFFFF',
          borderRight: '1px solid #E2E8F0',
          padding: '24px 16px',
          display: 'flex',
          flexDirection: 'column',
          gap: '8px'
        }}>
          <button onClick={() => setActiveTab('dashboard')} style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '12px', borderRadius: '10px', border: 'none', backgroundColor: activeTab === 'dashboard' ? '#F3EDF7' : 'transparent', color: activeTab === 'dashboard' ? '#6750A4' : '#475569', fontWeight: activeTab === 'dashboard' ? 800 : 600, cursor: 'pointer', textAlign: 'left' }}>
            <BookOpen size={20} /> Dashboard
          </button>
          <button onClick={() => setActiveTab('seats')} style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '12px', borderRadius: '10px', border: 'none', backgroundColor: activeTab === 'seats' ? '#F3EDF7' : 'transparent', color: activeTab === 'seats' ? '#6750A4' : '#475569', fontWeight: activeTab === 'seats' ? 800 : 600, cursor: 'pointer', textAlign: 'left' }}>
            <LayoutGrid size={20} /> Visual Seat Map
          </button>
          <button onClick={() => setActiveTab('students')} style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '12px', borderRadius: '10px', border: 'none', backgroundColor: activeTab === 'students' ? '#F3EDF7' : 'transparent', color: activeTab === 'students' ? '#6750A4' : '#475569', fontWeight: activeTab === 'students' ? 800 : 600, cursor: 'pointer', textAlign: 'left' }}>
            <Users size={20} /> Students Directory
          </button>
          <button onClick={() => setActiveTab('attendance')} style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '12px', borderRadius: '10px', border: 'none', backgroundColor: activeTab === 'attendance' ? '#F3EDF7' : 'transparent', color: activeTab === 'attendance' ? '#6750A4' : '#475569', fontWeight: activeTab === 'attendance' ? 800 : 600, cursor: 'pointer', textAlign: 'left' }}>
            <QrCode size={20} /> Live Attendance QR
          </button>
          <button onClick={() => setActiveTab('payments')} style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '12px', borderRadius: '10px', border: 'none', backgroundColor: activeTab === 'payments' ? '#F3EDF7' : 'transparent', color: activeTab === 'payments' ? '#6750A4' : '#475569', fontWeight: activeTab === 'payments' ? 800 : 600, cursor: 'pointer', textAlign: 'left' }}>
            <CreditCard size={20} /> Dues & Payments
          </button>
          <button onClick={() => setActiveTab('settings')} style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '12px', borderRadius: '10px', border: 'none', backgroundColor: activeTab === 'settings' ? '#F3EDF7' : 'transparent', color: activeTab === 'settings' ? '#6750A4' : '#475569', fontWeight: activeTab === 'settings' ? 800 : 600, cursor: 'pointer', textAlign: 'left' }}>
            <Settings size={20} /> Settings & SaaS Tier
          </button>
        </aside>

        {/* Dynamic Main Workspace */}
        <main style={{ flex: 1, padding: '32px', maxWidth: '1200px' }}>
          {/* DASHBOARD TAB */}
          {activeTab === 'dashboard' && (
            <div>
              <h3 style={{ fontSize: '24px', fontWeight: 900, marginBottom: '24px' }}>Library Owner Dashboard</h3>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '32px' }}>
                <div style={{ backgroundColor: '#FFFFFF', padding: '24px', borderRadius: '16px', border: '1px solid #E2E8F0', boxShadow: '0 2px 8px rgba(0,0,0,0.02)' }}>
                  <p style={{ fontSize: '13px', color: '#64748B', fontWeight: 700 }}>Total Registered Students</p>
                  <h4 style={{ fontSize: '32px', fontWeight: 900, color: '#6750A4', marginTop: '8px' }}>{account.students?.length || 0}</h4>
                </div>
                <div style={{ backgroundColor: '#FFFFFF', padding: '24px', borderRadius: '16px', border: '1px solid #E2E8F0', boxShadow: '0 2px 8px rgba(0,0,0,0.02)' }}>
                  <p style={{ fontSize: '13px', color: '#64748B', fontWeight: 700 }}>Seat Occupancy</p>
                  <h4 style={{ fontSize: '32px', fontWeight: 900, color: '#059669', marginTop: '8px' }}>
                    {account.occupiedSeatsCount || 0} / {account.library?.totalSeats || 60}
                  </h4>
                </div>
                <div style={{ backgroundColor: '#FFFFFF', padding: '24px', borderRadius: '16px', border: '1px solid #E2E8F0', boxShadow: '0 2px 8px rgba(0,0,0,0.02)' }}>
                  <p style={{ fontSize: '13px', color: '#64748B', fontWeight: 700 }}>SaaS Subscription Plan</p>
                  <h4 style={{ fontSize: '24px', fontWeight: 900, color: '#2563EB', marginTop: '8px' }}>
                    {account.saasSubscription?.planType || 'FREE'} PLAN
                  </h4>
                </div>
              </div>
            </div>
          )}

          {/* VISUAL SEAT MAP */}
          {activeTab === 'seats' && (
            <div>
              <h3 style={{ fontSize: '24px', fontWeight: 900, marginBottom: '12px' }}>
                Visual Seat Map ({account.library?.totalSeats || 60} Total Seats)
              </h3>
              <p style={{ fontSize: '14px', color: '#64748B', marginBottom: '24px' }}>
                Green = Available Desk, Blue = Occupied Desk.
              </p>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(68px, 1fr))', gap: '12px', backgroundColor: '#FFFFFF', padding: '24px', borderRadius: '20px', border: '1px solid #E2E8F0' }}>
                {Array.from({ length: account.library?.totalSeats || 60 }).map((_, idx) => {
                  const isOccupied = idx < (account.students?.length || 0);
                  return (
                    <div key={idx} style={{
                      height: '68px',
                      borderRadius: '12px',
                      backgroundColor: isOccupied ? '#EFF6FF' : '#ECFDF5',
                      border: `2px solid ${isOccupied ? '#3B82F6' : '#10B981'}`,
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontWeight: 900,
                      fontSize: '15px',
                      color: isOccupied ? '#1D4ED8' : '#047857',
                      cursor: 'pointer'
                    }}>
                      <span>{idx + 1}</span>
                      <span style={{ fontSize: '9px', fontWeight: 700 }}>{isOccupied ? 'OCCUPIED' : 'FREE'}</span>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* STUDENTS DIRECTORY */}
          {activeTab === 'students' && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <h3 style={{ fontSize: '24px', fontWeight: 900 }}>Students Directory ({account.students?.length || 0})</h3>
                <button
                  onClick={() => setShowAddStudentModal(true)}
                  style={{
                    padding: '12px 20px',
                    backgroundColor: '#6750A4',
                    color: '#FFFFFF',
                    borderRadius: '12px',
                    border: 'none',
                    fontWeight: 800,
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    boxShadow: '0 4px 12px rgba(103,80,164,0.3)'
                  }}
                >
                  <Plus size={18} /> Register Student
                </button>
              </div>

              {/* Students Table */}
              <div style={{ backgroundColor: '#FFFFFF', borderRadius: '16px', border: '1px solid #E2E8F0', overflow: 'hidden' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '14px' }}>
                  <thead>
                    <tr style={{ backgroundColor: '#F8FAFC', borderBottom: '1px solid #E2E8F0', color: '#64748B', fontWeight: 700 }}>
                      <th style={{ padding: '16px 20px' }}>Seat #</th>
                      <th style={{ padding: '16px 20px' }}>Student Name</th>
                      <th style={{ padding: '16px 20px' }}>Phone</th>
                      <th style={{ padding: '16px 20px' }}>Shift</th>
                      <th style={{ padding: '16px 20px' }}>Monthly Fee</th>
                      <th style={{ padding: '16px 20px' }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(account.students || []).map((student: any) => (
                      <tr key={student.id} style={{ borderBottom: '1px solid #F1F5F9' }}>
                        <td style={{ padding: '16px 20px', fontWeight: 800, color: '#6750A4' }}>Seat {student.seatNo}</td>
                        <td style={{ padding: '16px 20px', fontWeight: 700, color: '#0F172A' }}>{student.name}</td>
                        <td style={{ padding: '16px 20px', color: '#475569' }}>{student.phone}</td>
                        <td style={{ padding: '16px 20px' }}>
                          <span style={{ backgroundColor: '#F1F5F9', color: '#334155', padding: '4px 10px', borderRadius: '12px', fontSize: '12px', fontWeight: 700 }}>
                            {student.shift}
                          </span>
                        </td>
                        <td style={{ padding: '16px 20px', fontWeight: 800, color: '#059669' }}>₹{student.feeAmount}</td>
                        <td style={{ padding: '16px 20px', display: 'flex', gap: '8px' }}>
                          <button
                            onClick={() => openWhatsAppReminder(student)}
                            style={{ padding: '6px 12px', borderRadius: '8px', border: '1px solid #25D366', backgroundColor: '#DCFCE7', color: '#166534', fontWeight: 700, fontSize: '12px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px' }}
                          >
                            <MessageSquare size={14} /> Remind
                          </button>
                          <button
                            onClick={() => handleDeleteStudent(student.id)}
                            style={{ padding: '6px 12px', borderRadius: '8px', border: 'none', backgroundColor: '#FEE2E2', color: '#991B1B', fontWeight: 700, fontSize: '12px', cursor: 'pointer' }}
                          >
                            <Trash2 size={14} />
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* ATTENDANCE QR TAB */}
          {activeTab === 'attendance' && (
            <div>
              <h3 style={{ fontSize: '24px', fontWeight: 900, marginBottom: '16px' }}>Live Attendance & QR Identity Scanner</h3>
              <div style={{ backgroundColor: '#FFFFFF', padding: '40px', borderRadius: '20px', border: '1px solid #E2E8F0', textAlign: 'center' }}>
                <QrCode size={72} color="#6750A4" style={{ margin: '0 auto 16px auto' }} />
                <h4 style={{ fontSize: '20px', fontWeight: 800, marginBottom: '8px' }}>Student QR Scanner Ready</h4>
                <p style={{ fontSize: '14px', color: '#64748B', maxWidth: '420px', margin: '0 auto 24px auto' }}>
                  Point your smartphone or web camera at the student ID card to auto-register entry/exit timestamps.
                </p>
                <button style={{ padding: '14px 28px', backgroundColor: '#6750A4', color: '#FFFFFF', borderRadius: '12px', border: 'none', fontWeight: 800, cursor: 'pointer' }}>
                  Start Live Scanner
                </button>
              </div>
            </div>
          )}

          {/* PAYMENTS & DUES TAB */}
          {activeTab === 'payments' && (
            <div>
              <h3 style={{ fontSize: '24px', fontWeight: 900, marginBottom: '16px' }}>Student Fee Collection & Receipts</h3>
              <p style={{ fontSize: '14px', color: '#64748B', marginBottom: '24px' }}>
                Domain A: Manage student monthly fees, receipts, and automatic WhatsApp due alerts.
              </p>
            </div>
          )}

          {/* SETTINGS TAB */}
          {activeTab === 'settings' && (
            <div>
              <h3 style={{ fontSize: '24px', fontWeight: 900, marginBottom: '24px' }}>Library Profile & SaaS Settings</h3>
              <div style={{ backgroundColor: '#FFFFFF', padding: '28px', borderRadius: '20px', border: '1px solid #E2E8F0' }}>
                <h4 style={{ fontSize: '18px', fontWeight: 800, marginBottom: '12px' }}>Active SaaS Subscription Plan</h4>
                <div style={{ padding: '16px', backgroundColor: '#F3EDF7', borderRadius: '12px', color: '#6750A4', fontWeight: 800, fontSize: '16px', marginBottom: '16px' }}>
                  {account.saasSubscription?.planType || 'FREE'} PLAN ACTIVE
                </div>
              </div>
            </div>
          )}
        </main>
      </div>

      {/* ADD STUDENT MODAL */}
      {showAddStudentModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 200 }}>
          <div style={{ backgroundColor: '#FFFFFF', borderRadius: '20px', padding: '32px', width: '100%', maxWidth: '440px' }}>
            <h3 style={{ fontSize: '20px', fontWeight: 900, marginBottom: '20px' }}>Register New Student</h3>
            <form onSubmit={handleAddStudent} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, marginBottom: '4px' }}>Student Name</label>
                <input type="text" required value={newStudentName} onChange={(e) => setNewStudentName(e.target.value)} placeholder="e.g. Rahul Sharma" style={{ width: '100%', padding: '10px', borderRadius: '8px', border: '1px solid #CBD5E1' }} />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, marginBottom: '4px' }}>Mobile Phone</label>
                <input type="tel" required value={newStudentPhone} onChange={(e) => setNewStudentPhone(e.target.value)} placeholder="10-digit phone" style={{ width: '100%', padding: '10px', borderRadius: '8px', border: '1px solid #CBD5E1' }} />
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, marginBottom: '4px' }}>Seat Number</label>
                  <input type="number" value={newStudentSeat} onChange={(e) => setNewStudentSeat(e.target.value)} style={{ width: '100%', padding: '10px', borderRadius: '8px', border: '1px solid #CBD5E1' }} />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, marginBottom: '4px' }}>Shift</label>
                  <select value={newStudentShift} onChange={(e) => setNewStudentShift(e.target.value)} style={{ width: '100%', padding: '10px', borderRadius: '8px', border: '1px solid #CBD5E1' }}>
                    <option>Full Day</option>
                    <option>Morning</option>
                    <option>Evening</option>
                  </select>
                </div>
              </div>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '16px' }}>
                <button type="button" onClick={() => setShowAddStudentModal(false)} style={{ padding: '10px 16px', borderRadius: '8px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer' }}>Cancel</button>
                <button type="submit" style={{ padding: '10px 20px', borderRadius: '8px', border: 'none', backgroundColor: '#6750A4', color: '#FFFFFF', fontWeight: 800, cursor: 'pointer' }}>Save Student</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
