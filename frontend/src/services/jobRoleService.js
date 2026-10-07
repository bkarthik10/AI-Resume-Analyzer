import { request } from './api';

// Roles are always loaded from the backend: GET /api/job-roles
export async function getJobRoles() {
  const data = await request('/job-roles');
  const list = Array.isArray(data) ? data : data.content || [];
  return list.map((r) => ({
    id: r.id,
    name: r.name ?? r.title ?? r.roleName,
    description: r.description ?? '',
  }));
}
