export default function MatchedSkillsTable({ skills }) {
  return (
    <div className="panel">
      <h3 className="panel-title green-bg">Matched Skills ({skills.length})</h3>
      <div className="table-wrap">
        <table>
          <thead><tr><th>No.</th><th>Skill</th><th>Category</th><th>Found In Resume</th></tr></thead>
          <tbody>
            {skills.length === 0 && <tr><td colSpan="4" className="empty">No matching skills detected.</td></tr>}
            {skills.map((s, i) => (
              <tr key={s.skill ?? s.name ?? i}>
                <td>{i + 1}</td><td>{s.skill ?? s.name}</td><td>{s.category}</td>
                <td>{Array.isArray(s.foundIn) ? s.foundIn.join(', ') : s.foundIn}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
