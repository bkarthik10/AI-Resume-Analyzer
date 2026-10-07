export default function SummaryCard({ matched, missing, keywords }) {
  return (
    <div className="card score amber">
      <h4>Summary</h4>
      <dl className="kv tight">
        <dt>Skills Matched</dt><dd className="green">{matched}</dd>
        <dt>Skills Missing</dt><dd className="red">{missing}</dd>
        <dt>Keywords Missing</dt><dd className="red">{keywords}</dd>
      </dl>
    </div>
  );
}
