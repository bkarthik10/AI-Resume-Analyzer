export default function LearningResources({ items }) {
  return (
    <div className="panel">
      <h3 className="panel-title">Learning Resources</h3>
      <div className="table-wrap">
        <table>
          <thead><tr><th>Skill</th><th>Resource</th><th>Provider</th><th>Link</th></tr></thead>
          <tbody>
            {items.length === 0 && <tr><td colSpan="4" className="empty">No learning resources.</td></tr>}
            {items.map((r, i) => {
              const href = r.link ?? r.url;
              return (
                <tr key={i}>
                  <td>{r.skill}</td><td>{r.resource ?? r.title}</td><td>{r.provider}</td>
                  <td>{href ? <a href={href} target="_blank" rel="noreferrer noopener">Open</a> : '-'}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
