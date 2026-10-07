import { useMemo, useState } from 'react';

export default function RoleSelector({ roles, value, onChange, loading, error }) {
  const [q, setQ] = useState('');
  // Keep the chosen role in the list even if the search box would filter it out.
  const filtered = useMemo(
    () => roles.filter((r) => String(r.id) === String(value) || r.name?.toLowerCase().includes(q.toLowerCase())),
    [roles, q, value]
  );
  const selected = roles.find((r) => String(r.id) === String(value));

  return (
    <section id="target-job-role" className="card step">
      <h3 className="label">Target Job Role</h3>
      <p className="muted">What role are you applying for?</p>
      <input className="field" placeholder="Search roles..." value={q} onChange={(e) => setQ(e.target.value)} disabled={loading} />
      <select className="field" value={value} onChange={(e) => onChange(e.target.value)} disabled={loading}>
        <option value="">{loading ? 'Loading roles...' : 'Select a job role'}</option>
        {filtered.map((r) => <option key={r.id} value={r.id}>{r.name}</option>)}
      </select>
      {error && <p className="error-text">{error}</p>}
      {selected && (
        <>
          <p className="ok-text">&#10003; Target job role selected</p>
          <div className="selected-role">
            <span>Selected Role:</span>
            <strong>{selected.name}</strong>
          </div>
        </>
      )}
    </section>
  );
}
