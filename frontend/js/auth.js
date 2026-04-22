/* ═══════════════════════════════════════════════════════
   PIC21 — AuthService: login, logout, token, roles (UML v8)
═══════════════════════════════════════════════════════ */

const AuthService = (() => {
  const TOKEN_KEY = PIC21_CONFIG.TOKEN_KEY;
  const USER_KEY  = PIC21_CONFIG.USER_KEY;

  // Mapa UML v8: rol enum → etiqueta legible
  const ROLE_DISPLAY_NAMES = {
    'R04_ADMIN':      'Admin',
    'R05_DIRECTOR':   'Director',
    'R01_PROFESOR':   'Profesor',
    'R03_EGRESADO':   'Egresado',
    'R06_AYUDANTE':   'Ayudante',
    'R02_ESTUDIANTE': 'Estudiante',
  };

  // ── Login ────────────────────────────────────────────
  async function login(username, password) {
    const data = await Api.post('/auth/login', { username, password });
    if (data?.token) {
      localStorage.setItem(TOKEN_KEY, data.token);
      const user = {
        id:       data.id,
        username: data.username,
        nombre:   data.nombre,
        apellido: data.apellido,
        email:    data.email,
        roles:    data.roles || [],
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
    if (window.Sidebar) Sidebar.reset();
    if (window.Topbar)  Topbar.reset();
    if (window.Router)  Router.navigate('/login');
    else window.location.hash = '#/login';
  }

  // ── State ────────────────────────────────────────────
  function getToken() { return localStorage.getItem(TOKEN_KEY); }

  function getUser() {
    try { return JSON.parse(localStorage.getItem(USER_KEY)); }
    catch { return null; }
  }

  function isAuthenticated() { return !!getToken() && !!getUser(); }

  // ── Role checks (UML v8) ─────────────────────────────
  function hasRole(role) {
    const user = getUser();
    return user?.roles?.includes(role) ?? false;
  }

  function isAdmin()     { return hasRole('R04_ADMIN'); }
  function isDirector()  { return hasRole('R05_DIRECTOR'); }
  function isProfesor()  { return hasRole('R01_PROFESOR'); }
  function isAyudante()  { return hasRole('R06_AYUDANTE'); }
  function isEstudiante(){ return hasRole('R02_ESTUDIANTE'); }
  function isEgresado()  { return hasRole('R03_EGRESADO'); }

  function isStaff()     { return isAdmin() || isDirector() || isProfesor() || isAyudante(); }

  function getPrimaryRole() {
    const user = getUser();
    if (!user?.roles?.length) return 'R02_ESTUDIANTE';
    if (user.roles.includes('R04_ADMIN'))    return 'R04_ADMIN';
    if (user.roles.includes('R01_PROFESOR')) return 'R01_PROFESOR';
    if (user.roles.includes('R05_DIRECTOR')) return 'R05_DIRECTOR';
    if (user.roles.includes('R06_AYUDANTE')) return 'R06_AYUDANTE';
    if (user.roles.includes('R03_EGRESADO')) return 'R03_EGRESADO';
    return 'R02_ESTUDIANTE';
  }

  function getDisplayName() {
    const u = getUser();
    if (!u) return '';
    if (u.nombre || u.apellido) return `${u.nombre || ''} ${u.apellido || ''}`.trim();
    return u.username;
  }

  function getDisplayRole() {
    const role = getPrimaryRole();
    return ROLE_DISPLAY_NAMES[role] || role;
  }

  return {
    login, logout, getToken, getUser,
    isAuthenticated, hasRole,
    isAdmin, isDirector, isProfesor, isAyudante, isEstudiante, isEgresado, isStaff,
    getPrimaryRole, getDisplayName, getDisplayRole,
  };
})();
