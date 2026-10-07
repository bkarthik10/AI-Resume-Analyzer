export default function ScoreCard({ title, value, suffix, tone }) {
  return (
    <div className={`card score ${tone}`}>
      <h4>{title}</h4>
      <div className="score-value">{value ?? '-'}<span>{suffix}</span></div>
    </div>
  );
}
