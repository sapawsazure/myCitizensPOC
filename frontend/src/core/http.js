// Tiny fetch wrapper shared by every feature. Relative URLs work both in production
// (UI served by Spring Boot) and in dev mode (Vite proxies /api to the backend).

export class ApiError extends Error {
  constructor(message, status, fieldErrors = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

async function request(method, url, body) {
  const options = { method, headers: { Accept: 'application/json' } };
  if (body !== undefined) {
    options.headers['Content-Type'] = 'application/json';
    options.body = JSON.stringify(body);
  }

  let response;
  try {
    response = await fetch(url, options);
  } catch {
    throw new ApiError('Cannot reach the server. Is the backend running?', 0);
  }

  if (response.status === 204) return null;

  const text = await response.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = null;
    }
  }

  if (!response.ok) {
    throw new ApiError(data?.detail || `Request failed (${response.status})`, response.status, data?.errors ?? {});
  }
  return data;
}

export const http = {
  get: (url) => request('GET', url),
  post: (url, body) => request('POST', url, body ?? {}),
  put: (url, body) => request('PUT', url, body ?? {}),
  patch: (url, body) => request('PATCH', url, body ?? {}),
  delete: (url) => request('DELETE', url),
};

/** withQuery('/api/tasks', { status: 'DONE', q: '' }) -> '/api/tasks?status=DONE' (empty values are skipped). */
export function withQuery(url, params = {}) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && String(value).trim() !== '') query.set(key, value);
  });
  const text = query.toString();
  return text ? `${url}?${text}` : url;
}

/** Human-readable text for an error, including per-field validation messages. */
export function describeError(error) {
  if (!error) return '';
  const fields = Object.entries(error.fieldErrors ?? {}).map(([field, message]) => `${field}: ${message}`);
  return fields.length ? `${error.message} (${fields.join('; ')})` : error.message;
}
