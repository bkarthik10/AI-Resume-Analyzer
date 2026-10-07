export default function Header({ onReset }) {
  return (
    <header className="topbar">
      <h1>AI Resume Analyzer</h1>
      <button className="btn btn-outline" onClick={onReset}>&#8635; Upload New Resume</button>
    </header>
  );
}
