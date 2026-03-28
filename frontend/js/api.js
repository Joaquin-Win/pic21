/* ═══════════════════════════════════════════════════════
   PIC21 — API Service (fetch wrapper + JWT interceptor)
═══════════════════════════════════════════════════════ */

const Api = (() => {
  const BASE = PIC21_CONFIG.API_URL;

  async function request(method, path, body = null, options = {}) {
    const token = localStorage.getItem(PIC21_CONFIG.TOKEN_KEY);
    const headers = {};
    if (token) headers['Authorization'] = `Bearer ${token}`;

    let fetchBody = null;
    if (body && method !== 'GET') {
      if (body instanceof FormData) {
        fetchBody = body; // let browser set content-type with boundary
      } else {
        headers['Content-Type'] = 'application/json';
        headers['Accept'] = 'application/json';
        fetchBody = JSON.stringify(body);
      }
    } else {
      headers['Accept'] = 'application/json';
    }

    if (options.headers) Object.assign(headers, options.headers);
    const fetchOptions = { method, headers };
    if (fetchBody) fetchOptions.body = fetchBody;

    try {
      const resp = await fetch(`${BASE}${path}`, fetchOptions);

      // 401 → logout
      if (resp.status === 401) {
        AuthService.logout();
        return;
      }

      // Binary response (Excel)
      if (options.binary) {
        if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
        return resp.blob();
      }

      // No content
      if (resp.status === 204) return null;

      const data = await resp.json().catch(() => null);

      if (!resp.ok) {
        const msg = data?.message || data?.error || `Error ${resp.status}`;
        throw new ApiError(msg, resp.status, data);
      }
      return data;
    } catch (err) {
      if (err instanceof ApiError) throw err;
      console.error('[Api] Network error:', err);
      throw new ApiError(
        err.message && err.message !== 'Failed to fetch'
          ? err.message
          : 'No se pudo conectar con el servidor. Verificá tu conexión a internet.',
        0
      );
    }
  }

  class ApiError extends Error {
    constructor(message, status, body) {
      super(message);
      this.status = status;
      this.body = body;
    }
  }

  return {
    get:    (path, opts)       => request('GET',    path, null, opts),
    post:   (path, body, opts) => request('POST',   path, body, opts),
    put:    (path, body, opts) => request('PUT',    path, body, opts),
    patch:  (path, body, opts) => request('PATCH',  path, body, opts),
    delete: (path, opts)       => request('DELETE', path, null, opts),
    ApiError,
  };
})();
