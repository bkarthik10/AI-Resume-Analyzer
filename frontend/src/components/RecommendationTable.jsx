export default function RecommendationTable({ items }) {
  return (
    <div className="panel">
      <h3 className="panel-title blue-bg">Recommendations</h3>
      <div className="table-wrap">
        <table>
          <thead><tr><th>No.</th><th>Recommendation</th><th>Description</th></tr></thead>
          <tbody>
            {items.length === 0 && <tr><td colSpan="3" className="empty">No recommendations.</td></tr>}
            {items.map((r, i) => (
              <tr key={i}><td>{i + 1}</td><td>{r.title ?? r.recommendation}</td><td>{r.description}</td></tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
