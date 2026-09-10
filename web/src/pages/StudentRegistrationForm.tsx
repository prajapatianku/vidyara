import React, { useState, useEffect } from 'react';
import { BookOpen, CheckCircle2, User, Phone, Mail, Award, Clock, MapPin, Send, AlertCircle } from 'lucide-react';
import { fetchLibraryAccountById, upsertLibraryAccount } from '../services/SupabaseService';

interface StudentRegistrationFormProps {
  onBackToHome?: () => void;
}

export const StudentRegistrationForm: React.FC<StudentRegistrationFormProps> = ({ onBackToHome }) => {
  const [loading, setLoading] = useState(true);
  const [libraryData, setLibraryData] = useState<any>(null);
  const [accountRecord, setAccountRecord] = useState<any>(null);
  const [accountId, setAccountId] = useState<string>('');
  const [submitted, setSubmitted] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // Form states
  const [fullName, setFullName] = useState('');
  const [mobile, setMobile] = useState('');
  const [email, setEmail] = useState('');
  const [course, setCourse] = useState('UPSC / Civil Services');
  const [shift, setShift] = useState('Full Day (24x7)');
  const [preferredSeat, setPreferredSeat] = useState('');

  useEffect(() => {
    // Parse accId from window location hash or query string
    let id = '';
    const hash = window.location.hash;
    if (hash.includes('?')) {
      const queryStr = hash.substring(hash.indexOf('?'));
      const params = new URLSearchParams(queryStr);
      id = params.get('accId') || '';
    }
    if (!id) {
      const searchParams = new URLSearchParams(window.location.search);
      id = searchParams.get('accId') || '';
    }

    setAccountId(id);

    async function loadAccount() {
      if (!id) {
        setLoading(false);
        return;
      }
      try {
        const record = await fetchLibraryAccountById(id);
        if (record && record.data) {
          setAccountRecord(record);
          const parsed = typeof record.data === 'string' ? JSON.parse(record.data) : record.data;
          setLibraryData(parsed);
        }
      } catch (err) {
        console.error('Failed to load library account:', err);
      } finally {
        setLoading(false);
      }
    }

    loadAccount();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!fullName.trim() || !mobile.trim()) {
      setErrorMessage('Please fill in your name and mobile number.');
      return;
    }
    if (mobile.replace(/\D/g, '').length < 10) {
      setErrorMessage('Please enter a valid 10-digit mobile number.');
      return;
    }

    setSubmitting(true);
    setErrorMessage('');

    try {
      let parsedData = libraryData || {};
      if (!libraryData && accountRecord && accountRecord.data) {
        parsedData = typeof accountRecord.data === 'string' ? JSON.parse(accountRecord.data) : accountRecord.data;
      }

      const existingRequests = parsedData.registrationRequests || [];
      const newRequest = {
        id: 'req_' + Date.now(),
        studentName: fullName.trim(),
        mobile: mobile.trim(),
        email: email.trim(),
        course: course.trim(),
        requestedShift: shift,
        preferredSeat: preferredSeat.trim() ? (preferredSeat.toLowerCase().includes('seat') ? preferredSeat.trim() : `Seat ${preferredSeat.trim()}`) : 'Any Available',
        requestDate: new Date().toISOString(),
        status: 'pending'
      };

      const updatedAccount = {
        ...parsedData,
        registrationRequests: [newRequest, ...existingRequests]
      };

      const targetId = accountId || parsedData.accountId || 'acc_default';
      const success = await upsertLibraryAccount(targetId, updatedAccount);

      if (success) {
        setSubmitted(true);
      } else {
        setErrorMessage('Failed to submit registration. Please check internet connection and try again.');
      }
    } catch (err) {
      console.error('Submission error:', err);
      setErrorMessage('An unexpected error occurred. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: '#F8FAFC' }}>
        <div style={{ textAlign: 'center', color: '#6750A4' }}>
          <BookOpen size={48} className="spin" style={{ marginBottom: '16px' }} />
          <h3 style={{ fontSize: '18px', fontWeight: 800 }}>Loading Registration Portal...</h3>
        </div>
      </div>
    );
  }

  const libraryName = libraryData?.library?.name || 'Vidyara Library & Study Center';
  const libraryCity = libraryData?.library?.city || 'Study Center';
  const libraryAddress = libraryData?.library?.address || '';

  if (submitted) {
    return (
      <div style={{ minHeight: '100vh', backgroundColor: '#F8FAFC', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
        <div style={{ backgroundColor: '#FFFFFF', borderRadius: '24px', padding: '40px 32px', maxWidth: '480px', width: '100%', textAlign: 'center', boxShadow: '0 10px 25px rgba(0,0,0,0.05)', border: '1px solid #E2E8F0' }}>
          <div style={{ width: '72px', height: '72px', borderRadius: '50%', backgroundColor: '#DCFCE7', color: '#166534', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 20px auto' }}>
            <CheckCircle2 size={40} />
          </div>
          <h2 style={{ fontSize: '24px', fontWeight: 900, color: '#0F172A', marginBottom: '8px' }}>Registration Submitted!</h2>
          <p style={{ fontSize: '15px', color: '#475569', lineHeight: 1.5, marginBottom: '24px' }}>
            Thank you, <strong style={{ color: '#6750A4' }}>{fullName}</strong>! Your admission request has been sent to <strong>{libraryName}</strong>.
          </p>
          <div style={{ backgroundColor: '#F3EDF7', borderRadius: '16px', padding: '16px', textAlign: 'left', marginBottom: '28px', fontSize: '13px', color: '#49454F' }}>
            <p style={{ margin: '0 0 6px 0', fontWeight: 700 }}>📋 Application Summary:</p>
            <p style={{ margin: '0 0 4px 0' }}>• <strong>Shift:</strong> {shift}</p>
            <p style={{ margin: '0 0 4px 0' }}>• <strong>Preferred Seat:</strong> {preferredSeat || 'Any Available'}</p>
            <p style={{ margin: 0 }}>• <strong>Status:</strong> Pending Admin Approval</p>
          </div>
          <p style={{ fontSize: '13px', color: '#64748B', marginBottom: '24px' }}>
            You will receive a WhatsApp notification with your Digital Pass once the owner approves your seat.
          </p>
          {onBackToHome && (
            <button onClick={onBackToHome} style={{ width: '100%', padding: '14px', borderRadius: '12px', border: 'none', backgroundColor: '#6750A4', color: '#FFFFFF', fontWeight: 800, cursor: 'pointer', fontSize: '15px' }}>
              Back to Home
            </button>
          )}
        </div>
      </div>
    );
  }

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#F8FAFC', paddingBottom: '40px' }}>
      {/* Top Banner */}
      <header style={{ backgroundColor: '#6750A4', color: '#FFFFFF', padding: '24px 20px', textAlign: 'center' }}>
        <div style={{ maxWidth: '600px', margin: '0 auto', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <div style={{ width: '56px', height: '56px', borderRadius: '16px', backgroundColor: '#FFFFFF', color: '#6750A4', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '12px', boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }}>
            <BookOpen size={30} />
          </div>
          <h1 style={{ fontSize: '24px', fontWeight: 900, margin: '0 0 4px 0' }}>{libraryName}</h1>
          <p style={{ fontSize: '13px', color: '#E8DEF8', margin: 0, display: 'flex', alignItems: 'center', gap: '4px' }}>
            <MapPin size={14} /> {libraryCity} {libraryAddress ? `• ${libraryAddress}` : ''}
          </p>
        </div>
      </header>

      {/* Form Container */}
      <div style={{ maxWidth: '520px', margin: '-20px auto 0 auto', padding: '0 16px' }}>
        <div style={{ backgroundColor: '#FFFFFF', borderRadius: '20px', padding: '28px 24px', boxShadow: '0 8px 24px rgba(0,0,0,0.06)', border: '1px solid #E2E8F0' }}>
          <h2 style={{ fontSize: '20px', fontWeight: 800, color: '#1C1B1F', marginBottom: '6px', textAlign: 'center' }}>
            Student Self Registration Form
          </h2>
          <p style={{ fontSize: '13px', color: '#64748B', textAlign: 'center', marginBottom: '24px' }}>
            Fill in your details below to request instant admission & seat allocation.
          </p>

          {errorMessage && (
            <div style={{ backgroundColor: '#FEE2E2', border: '1px solid #FCA5A5', color: '#991B1B', padding: '12px 16px', borderRadius: '12px', fontSize: '13px', fontWeight: 700, marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <AlertCircle size={18} /> {errorMessage}
            </div>
          )}

          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
            {/* Student Full Name */}
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 700, color: '#334155', marginBottom: '6px' }}>
                Full Name <span style={{ color: '#EF4444' }}>*</span>
              </label>
              <div style={{ position: 'relative' }}>
                <User size={18} style={{ position: 'absolute', left: '12px', top: '12px', color: '#94A3B8' }} />
                <input
                  type="text"
                  required
                  placeholder="Enter your full name"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  style={{ width: '100%', padding: '11px 12px 11px 40px', borderRadius: '10px', border: '1px solid #CBD5E1', fontSize: '14px', outline: 'none', boxSizing: 'border-box' }}
                />
              </div>
            </div>

            {/* Mobile Phone */}
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 700, color: '#334155', marginBottom: '6px' }}>
                WhatsApp / Mobile Phone <span style={{ color: '#EF4444' }}>*</span>
              </label>
              <div style={{ position: 'relative' }}>
                <Phone size={18} style={{ position: 'absolute', left: '12px', top: '12px', color: '#94A3B8' }} />
                <input
                  type="tel"
                  required
                  placeholder="10-digit mobile number"
                  value={mobile}
                  onChange={(e) => setMobile(e.target.value)}
                  style={{ width: '100%', padding: '11px 12px 11px 40px', borderRadius: '10px', border: '1px solid #CBD5E1', fontSize: '14px', outline: 'none', boxSizing: 'border-box' }}
                />
              </div>
            </div>

            {/* Email Address */}
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 700, color: '#334155', marginBottom: '6px' }}>
                Email Address (Optional)
              </label>
              <div style={{ position: 'relative' }}>
                <Mail size={18} style={{ position: 'absolute', left: '12px', top: '12px', color: '#94A3B8' }} />
                <input
                  type="email"
                  placeholder="student@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  style={{ width: '100%', padding: '11px 12px 11px 40px', borderRadius: '10px', border: '1px solid #CBD5E1', fontSize: '14px', outline: 'none', boxSizing: 'border-box' }}
                />
              </div>
            </div>

            {/* Target Exam / Course */}
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 700, color: '#334155', marginBottom: '6px' }}>
                Target Exam / Course <span style={{ color: '#EF4444' }}>*</span>
              </label>
              <div style={{ position: 'relative' }}>
                <Award size={18} style={{ position: 'absolute', left: '12px', top: '12px', color: '#94A3B8' }} />
                <select
                  value={course}
                  onChange={(e) => setCourse(e.target.value)}
                  style={{ width: '100%', padding: '11px 12px 11px 40px', borderRadius: '10px', border: '1px solid #CBD5E1', fontSize: '14px', outline: 'none', backgroundColor: '#FFFFFF', boxSizing: 'border-box' }}
                >
                  <option value="UPSC / Civil Services">UPSC / Civil Services</option>
                  <option value="SSC / State PCS">SSC / State PCS</option>
                  <option value="NEET / Medical">NEET / Medical</option>
                  <option value="JEE / Engineering">JEE / Engineering</option>
                  <option value="Banking / Insurance">Banking / Insurance</option>
                  <option value="CA / CS / Finance">CA / CS / Finance</option>
                  <option value="School / College Studies">School / College Studies</option>
                  <option value="Other Competitive Exams">Other Competitive Exams</option>
                </select>
              </div>
            </div>

            {/* Shift Preference */}
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 700, color: '#334155', marginBottom: '6px' }}>
                Shift Preference <span style={{ color: '#EF4444' }}>*</span>
              </label>
              <div style={{ position: 'relative' }}>
                <Clock size={18} style={{ position: 'absolute', left: '12px', top: '12px', color: '#94A3B8' }} />
                <select
                  value={shift}
                  onChange={(e) => setShift(e.target.value)}
                  style={{ width: '100%', padding: '11px 12px 11px 40px', borderRadius: '10px', border: '1px solid #CBD5E1', fontSize: '14px', outline: 'none', backgroundColor: '#FFFFFF', boxSizing: 'border-box' }}
                >
                  <option value="Full Day (24x7)">Full Day (24x7)</option>
                  <option value="Morning Shift (6 AM - 2 PM)">Morning Shift (6 AM - 2 PM)</option>
                  <option value="Afternoon Shift (2 PM - 10 PM)">Afternoon Shift (2 PM - 10 PM)</option>
                  <option value="Night Shift (10 PM - 6 AM)">Night Shift (10 PM - 6 AM)</option>
                </select>
              </div>
            </div>

            {/* Preferred Seat Number */}
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 700, color: '#334155', marginBottom: '6px' }}>
                Preferred Seat Number (Optional)
              </label>
              <input
                type="text"
                placeholder="e.g. 12 or leave empty for auto-assign"
                value={preferredSeat}
                onChange={(e) => setPreferredSeat(e.target.value)}
                style={{ width: '100%', padding: '11px 12px', borderRadius: '10px', border: '1px solid #CBD5E1', fontSize: '14px', outline: 'none', boxSizing: 'border-box' }}
              />
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={submitting}
              style={{
                marginTop: '8px',
                width: '100%',
                padding: '14px',
                borderRadius: '12px',
                border: 'none',
                backgroundColor: '#6750A4',
                color: '#FFFFFF',
                fontSize: '15px',
                fontWeight: 800,
                cursor: submitting ? 'not-allowed' : 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '8px',
                boxShadow: '0 4px 14px rgba(103,80,164,0.35)',
                opacity: submitting ? 0.7 : 1
              }}
            >
              <Send size={18} /> {submitting ? 'Submitting Registration...' : 'Submit Registration Request'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
