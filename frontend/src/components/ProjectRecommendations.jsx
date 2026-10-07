export default function ProjectRecommendations({ items }) {
  return (
    <div className="panel">
      <h3 className="panel-title">Project Recommendations</h3>
      <div className="table-wrap">
        <table>
          <thead><tr><th>Project Name</th><th>Description</th><th>Skills Covered</th><th>Difficulty</th></tr></thead>
          <tbody>
            {items.length === 0 && <tr><td colSpan="4" className="empty">No project recommendations.</td></tr>}
            {items.map((p, i) => (
              <tr key={i}>
                <td>{p.name ?? p.title}</td><td>{p.description}</td>
                <td>{Array.isArray(p.skills) ? p.skills.join(', ') : p.skills}</td><td>{p.difficulty}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
