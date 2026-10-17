'use client';

import { useState, useEffect } from 'react';
import { createClient } from '@supabase/supabase-js';

const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || 'https://placeholder.supabase.co';
const supabaseAnonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || 'placeholder-key';
const supabase = createClient(supabaseUrl, supabaseAnonKey);

export default function AdminPortal() {
  const [session, setSession] = useState<any>(null);
  const [email, setEmail] = useState('');
  const [role, setRole] = useState('CLUB_OFFICER');
  const [orgId, setOrgId] = useState('00000000-0000-0000-0000-000000000001');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
      setSession(session);
    });

    const {
      data: { subscription },
    } = supabase.auth.onAuthStateChange((_event, session) => {
      setSession(session);
    });

    return () => subscription.unsubscribe();
  }, []);

  const handleSignIn = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    const { error } = await supabase.auth.signInWithOAuth({ provider: 'google' });
    if (error) setError(error.message);
    setLoading(false);
  };

  const handleGenerateInvitation = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) return;
    setLoading(true);
    setError('');
    setMessage('');

    const tokenHash = Math.random().toString(36).substring(2) + Date.now().toString(36);
    const expiresAt = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString();

    const { error: insertError } = await supabase.from('invitations').insert({
      token_hash: tokenHash,
      email: email,
      target_organization_id: orgId,
      assigned_role: role,
      expires_at: expiresAt,
      is_redeemed: false
    });

    if (insertError) {
      setError(`Server-enforced authorization error: ${insertError.message}`);
    } else {
      setMessage(`Server confirmed: Invitation token successfully generated and recorded in backend for ${email}.`);
      setEmail('');
    }
    setLoading(false);
  };

  if (!session) {
    return (
      <main style={{ padding: '60px', fontFamily: 'sans-serif', maxWidth: '500px', margin: '0 auto', textAlign: 'center' }}>
        <h1 style={{ color: '#0F4C81' }}>AGILA Hub — Admin Portal</h1>
        <p style={{ color: '#475569' }}>Please authenticate with authorized officer credentials via Supabase Auth.</p>
        <button 
          onClick={handleSignIn}
          style={{ background: '#0F4C81', color: '#FFF', padding: '14px 24px', borderRadius: '8px', border: 'none', fontWeight: 'bold', cursor: 'pointer', marginTop: '20px' }}
        >
          Sign In with Google (Officer SSO)
        </button>
        {error && <p style={{ color: '#EF4444', marginTop: '16px' }}>{error}</p>}
      </main>
    );
  }

  return (
    <main style={{ padding: '40px', fontFamily: 'sans-serif', maxWidth: '800px', margin: '0 auto', background: '#F8FAFC', minHeight: '100vh' }}>
      <header style={{ borderBottom: '2px solid #0F4C81', paddingBottom: '20px', marginBottom: '30px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ color: '#0F4C81', margin: 0 }}>AGILA Hub — Vercel Administrative Portal</h1>
          <p style={{ color: '#475569', margin: '5px 0 0 0' }}>Authenticated Officer: {session.user.email}</p>
        </div>
        <button 
          onClick={() => supabase.auth.signOut()}
          style={{ background: '#EF4444', color: '#FFF', padding: '8px 16px', borderRadius: '6px', border: 'none', fontWeight: 'bold', cursor: 'pointer' }}
        >
          Sign Out
        </button>
      </header>

      <section style={{ background: '#FFFFFF', padding: '24px', borderRadius: '12px', border: '1px solid #E2E8F0', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
        <h2 style={{ color: '#0A2E50', fontSize: '18px', marginTop: 0 }}>Generate Scoped Member Invitation (Server-Enforced)</h2>
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
            <label style={{ display: 'block', fontSize: '13px', fontWeight: 'bold', marginBottom: '6px' }}>Trusted Organization UUID</label>
            <input 
              type="text" 
              value={orgId} 
              onChange={(e) => setOrgId(e.target.value)} 
              placeholder="00000000-0000-0000-0000-000000000001"
              required
              style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #CBD5E1' }}
            />
          </div>

          <button 
            type="submit"
            disabled={loading}
            style={{ background: '#0F4C81', color: '#FFFFFF', padding: '12px', borderRadius: '8px', border: 'none', fontWeight: 'bold', cursor: 'pointer' }}
          >
            {loading ? 'Processing Backend Mutation...' : 'Generate Secure Invitation Token'}
          </button>
        </form>

        {message && (
          <div style={{ marginTop: '20px', padding: '12px', background: '#E2F3E9', color: '#1E6B45', borderRadius: '6px', fontWeight: '600' }}>
            {message}
          </div>
        )}

        {error && (
          <div style={{ marginTop: '20px', padding: '12px', background: '#FEE2E2', color: '#EF4444', borderRadius: '6px', fontWeight: '600' }}>
            {error}
          </div>
        )}
      </section>
    </main>
  );
}
