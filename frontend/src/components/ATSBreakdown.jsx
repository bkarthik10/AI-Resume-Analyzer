export default function ATSBreakdown({ items, total }) {
  return (
    <div className="panel">
      <h3 className="panel-title">ATS Score Breakdown</h3>
      <div className="table-wrap">
        <table>
          <thead><tr><th>Category</th><th>Score</th><th>Progress</th></tr></thead>
          <tbody>
            {items.map((b, i) => {
              const max = b.max ?? b.maxScore ?? 0;
              const score = b.score ?? 0;
              const pct = max ? Math.round((score / max) * 100) : 0;
              return (
                <tr key={i}>
                  <td>{b.section ?? b.category ?? b.name}</td>
                  <td>{score} / {max}</td>
                  <td><div className="bar"><div style={{ width: `${pct}%` }} /></div></td>
                </tr>
              );
            })}
            <tr className="total"><td>Total</td><td colSpan="2">{total} / 100</td></tr>
          </tbody>
        </table>
      </div>
    </div>
  );
}
