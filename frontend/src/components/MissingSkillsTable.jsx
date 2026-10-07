export default function MissingSkillsTable({ skills }) {
  return (
    <div className="panel">
      <h3 className="panel-title red-bg">Missing Skills ({skills.length})</h3>
      <div className="table-wrap">
        <table>
          <thead><tr><th>No.</th><th>Skill</th><th>Priority</th><th>Reason</th></tr></thead>
          <tbody>
            {skills.length === 0 && <tr><td colSpan="4" className="empty">No missing skills.</td></tr>}
            {skills.map((s, i) => {
              const p = String(s.priority || 'Low');
              return (
                <tr key={s.skill ?? s.name ?? i}>
                  <td>{i + 1}</td><td>{s.skill ?? s.name}</td>
                  <td><span className={`pill ${p.toLowerCase()}`}>{p[0].toUpperCase() + p.slice(1).toLowerCase()}</span></td>
                  <td>{s.reason || 'Not detected in uploaded resume.'}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
