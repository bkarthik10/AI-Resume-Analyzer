import { useEffect, useMemo, useRef, useState } from 'react';
import Header from './components/Header';
import ResumeUpload, { formatSize } from './components/ResumeUpload';
import RoleSelector from './components/RoleSelector';
import JobDescriptionInput from './components/JobDescriptionInput';
import ScoreCard from './components/ScoreCard';
import SummaryCard from './components/SummaryCard';
import MatchedSkillsTable from './components/MatchedSkillsTable';
import MissingSkillsTable from './components/MissingSkillsTable';
import MissingKeywords from './components/MissingKeywords';
import RecommendationTable from './components/RecommendationTable';
import ProjectRecommendations from './components/ProjectRecommendations';
import LearningResources from './components/LearningResources';
import ATSBreakdown from './components/ATSBreakdown';
import { getJobRoles } from './services/jobRoleService';
import { uploadResume } from './services/resumeService';
import { createAnalysis } from './services/analysisService';
import { friendlyError } from './services/api';

const SECTIONS = [
  { id: 'upload-resume', label: 'Upload Resume' },
  { id: 'target-job-role', label: 'Target Job Role' },
  { id: 'job-description', label: 'Job Description' },
  { id: 'analyze-resume', label: 'Analyze Resume' },
];
const RESULTS_ID = 'analysis-results';

const scrollToSection = (id) => {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
};

export default function App() {
  const [roles, setRoles] = useState([]);
  const [rolesLoading, setRolesLoading] = useState(true);
  const [rolesError, setRolesError] = useState('');
  const [uploadedResume, setUploadedResume] = useState(null); // record returned by the backend
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState('');
  const [roleId, setRoleId] = useState('');
  const [jobDescription, setJobDescription] = useState('');
  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [activeSection, setActiveSection] = useState(SECTIONS[0].id);
  const inFlight = useRef({ upload: false, analyze: false });

  const selectedRole = useMemo(() => roles.find((r) => String(r.id) === String(roleId)) || null, [roles, roleId]);
  const canAnalyze = Boolean(uploadedResume?.id) && Boolean(selectedRole?.id);
  const hasResults = Boolean(analysis);

  // Load job roles from GET /api/job-roles
  useEffect(() => {
    getJobRoles()
      .then(setRoles)
      .catch((e) => setRolesError(friendlyError(e, 'Unable to load job roles. Please refresh and try again.')))
      .finally(() => setRolesLoading(false));
  }, []);

  // Highlight the section that is crossing a thin line 35% down the viewport.
  // Only one section can cross it at a time; in the gaps between sections the previous highlight stays.
  useEffect(() => {
    const ids = [...SECTIONS.map((s) => s.id), ...(hasResults ? [RESULTS_ID] : [])];
    const observer = new IntersectionObserver(
      (entries) => {
        const hit = entries.filter((e) => e.isIntersecting).pop();
        if (hit) setActiveSection(hit.target.id === RESULTS_ID ? 'analyze-resume' : hit.target.id);
      },
      { root: null, threshold: 0, rootMargin: '-35% 0px -64% 0px' }
    );
    ids.forEach((id) => {
      const el = document.getElementById(id);
      if (el) observer.observe(el);
    });
    return () => observer.disconnect();
  }, [hasResults]);

  // After a successful analysis, bring the results into view.
  useEffect(() => {
    if (analysis) scrollToSection(RESULTS_ID);
  }, [analysis]);

  const handleFileSelected = async (file) => {
    if (inFlight.current.upload) return;
    inFlight.current.upload = true;
    setUploadError('');
    setError('');
    setUploading(true);
    try {
      const resume = await uploadResume(file); // POST /api/resumes/upload
      setUploadedResume(resume);
      setAnalysis(null); // results belong to the previous resume
    } catch (e) {
      setUploadError(friendlyError(e, 'Resume upload failed. Please try again.'));
    } finally {
      inFlight.current.upload = false;
      setUploading(false);
    }
  };

  const handleRemoveResume = () => {
    setUploadedResume(null);
    setAnalysis(null);
    setUploadError('');
  };

  const analyze = async () => {
    if (inFlight.current.analyze) return;
    if (!uploadedResume?.id) return setError('Please upload a resume first.');
    if (!selectedRole?.id) return setError('Please select a target job role.');
    inFlight.current.analyze = true;
    setError('');
    setAnalysis(null);
    setLoading(true);
    try {
      const result = await createAnalysis({
        resumeId: uploadedResume.id,
        jobRoleId: selectedRole.id,
        jobDescription, // optional; the service sends null when blank
      });
      setAnalysis(result);
    } catch (e) {
      setError(friendlyError(e, 'Resume analysis failed. Please try again.'));
    } finally {
      inFlight.current.analyze = false;
      setLoading(false);
    }
  };

  const reset = () => {
    setUploadedResume(null);
    setUploadError('');
    setRoleId('');
    setJobDescription('');
    setError('');
    setAnalysis(null);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const completed = {
    'upload-resume': Boolean(uploadedResume),
    'target-job-role': Boolean(selectedRole),
    'job-description': jobDescription.trim() !== '', // optional: only ticked if the user typed one
    'analyze-resume': hasResults,
  };

  const missingHint = !uploadedResume ? 'Please upload a resume first.' : !selectedRole ? 'Please select a target job role.' : '';

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="brand"><span className="logo">&#128196;</span><span>AI Resume<br />Analyzer</span></div>
        <nav aria-label="Sections">
          {SECTIONS.map(({ id, label }) => {
            const active = activeSection === id;
            const done = completed[id];
            return (
              <a
                key={id}
                href={`#${id}`}
                className={`${active ? 'active' : ''} ${done ? 'done' : ''}`.trim()}
                aria-current={active ? 'step' : undefined}
                onClick={(e) => { e.preventDefault(); scrollToSection(id); }}
              >
                <span className="step-icon" aria-hidden="true">{done ? '\u2713' : active ? '\u25CF' : '\u25CB'}</span>
                {label}
              </a>
            );
          })}
        </nav>
      </aside>

      <main className="main">
        <Header onReset={reset} />

        <ResumeUpload
          resume={uploadedResume}
          uploading={uploading}
          error={uploadError}
          onSelect={handleFileSelected}
          onRemove={handleRemoveResume}
          onError={setUploadError}
        />

        <RoleSelector roles={roles} value={roleId} onChange={setRoleId} loading={rolesLoading} error={rolesError} />

        <JobDescriptionInput value={jobDescription} onChange={setJobDescription} />

        <section id="analyze-resume" className="card step">
          <h3 className="label">Analyze Resume</h3>
          <p className="muted">
            Resume: <strong>{uploadedResume ? uploadedResume.fileName : 'not uploaded'}</strong>
            {' \u2022 '}Role: <strong>{selectedRole ? selectedRole.name : 'not selected'}</strong>
            {' \u2022 '}Job description: <strong>{jobDescription.trim() ? 'provided' : 'optional, not provided'}</strong>
          </p>
          {!canAnalyze && !loading && <p className="hint">{missingHint}</p>}
          {error && <div className="alert" role="alert">{error}</div>}
          <div className="actions">
            <button className="btn btn-primary lg" onClick={analyze} disabled={!canAnalyze || loading || uploading}>
              {loading ? 'Analyzing Resume...' : 'Analyze Resume'}
            </button>
          </div>
          {loading && <div className="spinner" role="status" aria-label="Analyzing Resume" />}
        </section>

        {analysis && <Results data={analysis} resume={uploadedResume} />}
      </main>
    </div>
  );
}

function Results({ data, resume }) {
  const d = new Date(data.analyzedAt || Date.now());
  const date = d.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
  const time = d.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
  const r = data.resume || {};
  const fileName = r.fileName || resume?.fileName || '-';
  const fileType = r.fileType || resume?.fileType || (fileName.toLowerCase().endsWith('.pdf') ? 'PDF' : 'DOCX');
  const sizeRaw = r.fileSize ?? resume?.fileSize;
  const fileSize = typeof sizeRaw === 'number' ? formatSize(sizeRaw) : sizeRaw || '-';

  return (
    <section id={RESULTS_ID} className="results">
      <h2>Analysis Results</h2>

      <div className="grid-3">
        <div className="card info">
          <h4>Uploaded Resume</h4>
          <dl className="kv">
            <dt>File Name</dt><dd>{fileName}</dd>
            <dt>File Type</dt><dd>{fileType}</dd>
            <dt>File Size</dt><dd>{fileSize}</dd>
          </dl>
        </div>
        <div className="card info">
          <h4>Target Job Role</h4>
          <dl className="kv">
            <dt>Role</dt><dd>{data.role?.name}</dd>
            <dt>Source</dt><dd>{data.source}</dd>
            <dt>Description</dt><dd>{data.role?.description || '-'}</dd>
          </dl>
        </div>
        <div className="card info">
          <h4>Analysis Date</h4>
          <dl className="kv">
            <dt>Date</dt><dd>{date}</dd>
            <dt>Time</dt><dd>{time}</dd>
            <dt>Status</dt><dd><span className="pill done">{data.status}</span></dd>
          </dl>
        </div>
      </div>

      <div className="grid-3">
        <ScoreCard title="ATS Compatibility" value={data.atsScore} suffix="%" tone="green" />
        <ScoreCard title="Skill Match" value={data.skillMatch} suffix="%" tone="purple" />
        <SummaryCard matched={data.matchedSkillsCount ?? data.matchedSkills.length} missing={data.missingSkillsCount ?? data.missingSkills.length} keywords={data.missingKeywordsCount ?? data.missingKeywords.length} />
      </div>

      <div className="grid-2">
        <MatchedSkillsTable skills={data.matchedSkills} />
        <MissingSkillsTable skills={data.missingSkills} />
      </div>

      <MissingKeywords keywords={data.missingKeywords} />
      <RecommendationTable items={data.recommendations} />
      <ProjectRecommendations items={data.projectRecommendations} />
      <LearningResources items={data.learningResources} />
      <ATSBreakdown items={data.atsBreakdown} total={data.atsScore} />
    </section>
  );
}
