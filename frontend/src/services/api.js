const BASE = import.meta.env.VITE_API_URL || '/api';

export const BACKEND_DOWN =
  'Unable to connect to the analyzer backend. Please make sure Spring Boot is running on port 8080.';

export class ApiError extends Error {
  constructor(message, { status = 0, unreachable = false, serverMessage = null } = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.unreachable = unreachable;
    this.serverMessage = serverMessage;
  }
}

export async function request(path, options = {}) {
  let res;
  try {
    res = await fetch(`${BASE}${path}`, options);
  } catch {
    throw new ApiError(BACKEND_DOWN, { unreachable: true });
  }
  if (!res.ok) {
    let serverMessage = null;
    try {
      const body = await res.json();
      serverMessage = body.message || body.error || null;
    } catch { /* body was not JSON */ }
    // Vite's proxy answers 5xx with an empty (non-JSON) body when Spring Boot is not running.
    if (res.status >= 500 && !serverMessage) {
      throw new ApiError(BACKEND_DOWN, { status: res.status, unreachable: true });
    }
    throw new ApiError(`Request failed (${res.status})`, { status: res.status, serverMessage });
  }
  return res.status === 204 ? null : res.json();
}

/**
 * Turn any thrown error into a message that is safe to show to the user.
 * - backend not reachable  -> the "start Spring Boot" message
 * - 4xx with a message     -> that message (e.g. "Only PDF or DOCX resumes are supported.")
 * - anything else (5xx)    -> the caller's generic fallback, never raw server/exception text
 */
export function friendlyError(e, fallback) {
  if (e?.unreachable) return BACKEND_DOWN;
  if (e?.status >= 400 && e.status < 500 && e.serverMessage) return e.serverMessage;
  return fallback;
}

export const json = (method, body) => ({
  method,
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(body),
});
