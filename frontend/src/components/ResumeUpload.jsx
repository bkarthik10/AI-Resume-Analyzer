import { useRef, useState } from 'react';

const MAX = 5 * 1024 * 1024;
export const formatSize = (b) => (b >= 1048576 ? `${(b / 1048576).toFixed(1)} MB` : `${Math.max(1, Math.round(b / 1024))} KB`);
const typeOf = (name) => (name.toLowerCase().endsWith('.pdf') ? 'PDF' : 'DOCX');

// `resume` is the record returned by POST /api/resumes/upload (null until an upload succeeds).
export default function ResumeUpload({ resume, uploading, error, onSelect, onRemove, onError }) {
  const input = useRef(null);
  const [drag, setDrag] = useState(false);

  const pick = (f) => {
    if (!f || uploading) return;
    if (!/\.(pdf|docx)$/i.test(f.name)) return onError('Please upload a PDF or DOCX resume.');
    if (f.size > MAX) return onError('File is larger than 5 MB.');
    onError('');
    onSelect(f);
  };

  return (
    <section id="upload-resume" className="card step">
      <h3 className="label">Upload Resume</h3>
      <div
        className={`dropzone ${drag ? 'drag' : ''}`}
        onDragOver={(e) => { e.preventDefault(); setDrag(true); }}
        onDragLeave={() => setDrag(false)}
        onDrop={(e) => { e.preventDefault(); setDrag(false); pick(e.dataTransfer.files[0]); }}
      >
        <div className="cloud">&#9729;</div>
        <p>{uploading ? 'Uploading resume...' : 'Drag & drop your resume here'}</p>
        <small>or</small>
        <button type="button" className="btn btn-primary sm" disabled={uploading} onClick={() => input.current.click()}>
          {uploading ? 'Uploading...' : resume ? 'Replace Resume' : 'Browse Resume'}
        </button>
        <p className="hint">Upload your latest resume in PDF or DOCX format. Maximum size: 5 MB</p>
        <input ref={input} type="file" accept=".pdf,.docx" hidden onChange={(e) => { pick(e.target.files[0]); e.target.value = ''; }} />
      </div>

      {error && <div className="alert" role="alert">{error}</div>}

      {resume && (
        <>
          <p className="ok-text">&#10003; Resume uploaded</p>
          <div className="file-row">
            <span className={`file-badge ${resume.fileType || typeOf(resume.fileName)}`}>{resume.fileType || typeOf(resume.fileName)}</span>
            <div className="file-info">
              <strong>{resume.fileName}</strong>
              <span>{resume.fileType || typeOf(resume.fileName)} Document &bull; {formatSize(resume.fileSize)}</span>
            </div>
            <button type="button" className="icon-btn" title="Remove" disabled={uploading} onClick={onRemove}>&#10005;</button>
          </div>
        </>
      )}
    </section>
  );
}
