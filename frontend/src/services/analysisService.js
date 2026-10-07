import { request, json } from './api';

// The backend does ALL calculation. This file only fetches and maps field names.
export async function createAnalysis({ resumeId, jobRoleId, jobDescription }) {
  const created = await request(
    '/analysis/create',
    json('POST', { resumeId, jobRoleId, jobDescription: jobDescription?.trim() || null })
  );
  const id = created.id ?? created.analysisId;
  const full = created.atsScore !== undefined ? created : await request(`/analysis/${id}`);

  let recommendations = full.recommendations;
  if (!recommendations) {
    try { recommendations = await request(`/recommendations/${id}`); } catch { recommendations = []; }
  }
  return normalize({ ...full, id, recommendations });
}

function normalize(a) {
  const asList = (v) => (Array.isArray(v) ? v : v?.items || []);
  return {
    id: a.id,
    resume: a.resume ?? { fileName: a.fileName, fileType: a.fileType, fileSize: a.fileSize },
    role: a.role ?? { name: a.roleName ?? a.jobRoleName, description: a.roleDescription ?? '' },
    source: a.source ?? (a.jobDescriptionProvided ? 'Job Description' : 'Selected Role'),
    analyzedAt: a.analyzedAt ?? a.createdAt,
    status: a.status ?? 'Completed',
    atsScore: a.atsScore,
    matchedSkillsCount: a.matchedSkillsCount,
    missingSkillsCount: a.missingSkillsCount,
    missingKeywordsCount: a.missingKeywordsCount,
    skillMatch: a.skillMatch ?? a.skillMatchPercentage,
    matchedSkills: asList(a.matchedSkills),
    missingSkills: asList(a.missingSkills),
    missingKeywords: asList(a.missingKeywords),
    recommendations: asList(a.recommendations),
    projectRecommendations: asList(a.projectRecommendations),
    learningResources: asList(a.learningResources),
    atsBreakdown: asList(a.atsBreakdown),
  };
}
