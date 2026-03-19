/* ═══════════════════════════════════════════════════════
   PIC21 — Configuración global
   Auto-detecta el entorno:
     · Localhost → apunta al backend Spring Boot en :8080
     · Producción → usa rutas relativas (mismo servidor)
═══════════════════════════════════════════════════════ */
const PIC21_CONFIG = (() => {
  const isLocalhost  =
    window.location.hostname === 'localhost' ||
    window.location.hostname === '127.0.0.1';

  return {
    API_URL:   isLocalhost ? 'http://localhost:8080/api' : '/api',
    TOKEN_KEY: 'pic21_token',
    USER_KEY:  'pic21_user',
  };
})();
