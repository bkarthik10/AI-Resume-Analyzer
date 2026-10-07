export default function JobDescriptionInput({ value, onChange }) {
  return (
    <section id="job-description" className="card step">
      <h3 className="label">Job Description (Optional)</h3>
      <textarea
        className="field"
        rows={9}
        placeholder={'Paste the job description here (optional).\n\nThe analyzer will compare your resume against the\nrequirements in the job description.'}
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
      <p className="hint">Leave empty to use the selected role&apos;s predefined requirements.</p>
    </section>
  );
}
