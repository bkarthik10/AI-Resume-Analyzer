export default function MissingKeywords({ keywords }) {
  return (
    <div className="panel">
      <h3 className="panel-title">Missing Keywords ({keywords.length})</h3>
      <div className="tags">
        {keywords.length === 0 && <span className="empty">No missing keywords.</span>}
        {keywords.map((k) => {
          const t = typeof k === 'string' ? k : k.keyword ?? k.name;
          return <span key={t} className="tag">{t}</span>;
        })}
      </div>
    </div>
  );
}
