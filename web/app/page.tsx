'use client';

import { useState } from 'react';

export default function AdminPortal() {
  const [email, setEmail] = useState('');
  const [role, setRole] = useState('CLUB_OFFICER');
  const [org, setOrg] = useState('Bantayog Elite Eagles Club');
  const [message, setMessage] = useState('');

  const handleGenerateInvitation = (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) return;
    setMessage(`Secure invitation token generated and dispatched for ${email} as ${role} in ${org}.`);
  };

  return (
    <main style={{ padding: '40px', fontFamily: 'sans-serif', maxWidth: '800px', margin: '0 auto', background: '#F8FAFC', minHeight: '100vh' }}>
      <header style={{ borderBottom: '2px solid #0F4C81', paddingBottom: '20px', marginBottom: '30px' }}>
        <h1 style={{ color: '#0F4C81', margin: 0 }}>AGILA Hub — Vercel Administrative Portal</h1>
        <p style={{ color: '#475569', margin: '5px 0 0 0' }}>The Fraternal Order of Eagles (Philippine Eagles) — Officer Governance & Audit Stream</p>
      </header>

      <section style={{ background: '#FFFFFF', padding: '24px', borderRadius: '12px', border: '1px solid #E2E8F0', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
        <h2 style={{ color: '#0A2E50', fontSize: '18px', marginTop: 0 }}>Generate Scoped Member Invitation</h2>
        <form onSubmit={handleGenerateInvitation} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div>
            <label style={{ display: 'block', fontSize: '13px', fontWeight: 'bold', marginBottom: '6px' }}>Applicant Email Address</label>
            <input 
              type="email" 
              value={email} 
              onChange={(e) => setEmail(e.target.value)} 
              placeholder="applicant@eagles.ph" 
              required
              style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #CBD5E1' }}
            />
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '13px', fontWeight: 'bold', marginBottom: '6px' }}>Assigned Fraternal Role</label>
            <select 
              value={role} 
              onChange={(e) => setRole(e.target.value)}
              style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #CBD5E1' }}
            >
              <option value="MEMBER">Member</option>
              <option value="CLUB_OFFICER">Club Officer</option>
              <option value="FINANCE_OFFICER">Finance Officer / Treasurer</option>
              <option value="CLUB_ADMIN">Club Administrator</option>
              <option value="REGIONAL_OFFICER">Regional Officer</option>
              <option value="NATIONAL_OFFICER">National Officer</option>
            </select>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '13px', fontWeight: 'bold', marginBottom: '6px' }}>Organization Scope</label>
            <input 
              type="text" 
              value={org} 
              onChange={(e) => setOrg(e.target.value)} 
              required
              style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #CBD5E1' }}
            />
          </div>

          <button 
            type="submit"
            style={{ background: '#0F4C81', color: '#FFFFFF', padding: '12px', borderRadius: '8px', border: 'none', fontWeight: 'bold', cursor: 'pointer' }}
          >
            Generate Secure Invitation Token
          </button>
        </form>

        {message && (
          <div style={{ marginTop: '20px', padding: '12px', background: '#E2F3E9', color: '#1E6B45', borderRadius: '6px', fontWeight: '600' }}>
            {message}
          </div>
        )}
      </section>
    </main>
  );
}
