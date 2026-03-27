/* ═══════════════════════════════════════════════════════
   PIC21 — AuthService: login, logout, token, roles
═══════════════════════════════════════════════════════ */

const AuthService = (() => {
  const TOKEN_KEY = PIC21_CONFIG.TOKEN_KEY;
  const USER_KEY  = PIC21_CONFIG.USER_KEY;

  // ── Login ────────────────────────────────────────────
  async function login(username, password) {
    const data = await Api.post('/auth/login', { username, password });
    if (data?.token) {
      localStorage.setItem(TOKEN_KEY, data.token);
      const user = {
        id:        data.id,
        username:  data.username,
        email:     data.email,
        firstName: data.firstName,
        lastName:  data.lastName,
        roles:     data.roles || [],
      };
      localStorage.setItem(USER_KEY, JSON.stringify(user));
      return user;
    }
    throw new Error('Correo o contraseña incorrecta');
  }

  // ── Logout ───────────────────────────────────────────
  function logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    // Sidebar needs to be rebuilt on next login
    if (window.Sidebar) Sidebar.reset();
    if (window.Topbar)  Topbar.reset();
    // Router may not be defined yet during early load, guard it
    if (window.Router) Router.navigate('/login');
    else window.location.hash = '#/login';
  }

  // ── State ────────────────────────────────────────────
  function getToken() { return localStorage.getItem(TOKEN_KEY); }

  function getUser() {
    try { return JSON.parse(localStorage.getItem(USER_KEY)); }
    catch { return null; }
  }

  function isAuthenticated() { return !!getToken() && !!getUser(); }

  // ── Role checks ──────────────────────────────────────
  function hasRole(role) {
    const user = getUser();
    return user?.roles?.includes(role) ?? false;
  }

  function isAdmin()   { return hasRole('ADMIN'); }
  function isProfesor(){ return hasRole('PROFESOR'); }
  function isAyudante(){ return hasRole('AYUDANTE'); }
  function isEstudiante(){ return hasRole('ESTUDIANTE'); }

  function isStaff()   { return isAdmin() || isProfesor() || isAyudante(); }

  function getPrimaryRole() {
    const user = getUser();
    if (!user?.roles?.length) return 'ESTUDIANTE';
    if (user.roles.includes('ADMIN')) return 'ADMIN';
    if (user.roles.includes('PROFESOR')) return 'PROFESOR';
    if (user.roles.includes('AYUDANTE')) return 'AYUDANTE';
    return 'ESTUDIANTE';
  }

  function getDisplayName() {
    const u = getUser();
    if (!u) return '';
    if (u.firstName || u.lastName) return `${u.firstName || ''} ${u.lastName || ''}`.trim();
    return u.username;
  }

  return {
    login, logout, getToken, getUser,
    isAuthenticated, hasRole,
    isAdmin, isProfesor, isAyudante, isEstudiante, isStaff,
    getPrimaryRole, getDisplayName,
  };
})();
