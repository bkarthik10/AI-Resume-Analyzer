import { request } from './api';

// POST /api/resumes/upload  (multipart, field name: "file")
export async function uploadResume(file) {
  const form = new FormData();
  form.append('file', file);
  const r = await request('/resumes/upload', { method: 'POST', body: form });
  return { ...r, id: r.id ?? r.resumeId };
}
