/* ═══════════════════════════════════════════════════════
   PIC21 — Client-side Router with role guards (UML v8)
═══════════════════════════════════════════════════════ */

const Router = (() => {
  // Roles UML v8 completos para rutas que admiten todos
  const ALL_ROLES = ['R04_ADMIN','R01_PROFESOR','R05_DIRECTOR','R06_AYUDANTE','R02_ESTUDIANTE','R03_EGRESADO'];

  function getRoutes() {
    /* eslint-disable no-undef */
    return {
      '/login':        { page: LoginPage,         title: 'Iniciar sesión',      public: true  },
      '/dashboard':    { page: DashboardPage,     title: 'Dashboard',           roles: ['R04_ADMIN','R05_DIRECTOR'] },
      '/meetings':     { page: MeetingsPage,      title: 'Reuniones',           roles: ALL_ROLES },
      '/meetings/:id': { page: MeetingDetailPage, title: 'Detalle de reunión',  roles: ALL_ROLES },
      '/tasks':        { page: TasksPage,         title: 'Recuperar asistencia',roles: ALL_ROLES },
      '/users':        { page: UsersPage,         title: 'Usuarios',            roles: ['R04_ADMIN'] },
      '/files':        { page: FilesPage,         title: 'Archivos',            roles: ['R04_ADMIN'] },
    };
  }

  let currentPath = null;

  function navigate(path, params = {}) {
    const clean = path.startsWith('/') ? path : '/' + path;
    window.history.pushState({ path: clean, params }, '', '#' + clean);
    render(clean, params);
  }

  function render(path, params = {}) {
    const routes = getRoutes();
    let route = null;
    let routeParams = { ...params };
    for (const [pattern, def] of Object.entries(routes)) {
      const match = matchRoute(pattern, path);
      if (match) { route = def; Object.assign(routeParams, match); break; }
    }

    if (!route) { redirectDefault(); return; }

    if (!route.public) {
      if (!AuthService.isAuthenticated()) { navigate('/login'); return; }
      if (route.roles?.length) {
        const user = AuthService.getUser();
        const allowed = route.roles.some(r => user?.roles?.includes(r));
        if (!allowed) { navigate(getDefaultRoute()); return; }
      }
    } else if (AuthService.isAuthenticated() && path === '/login') {
      navigate(getDefaultRoute());
      return;
    }

    currentPath = path;
    showAppropriateShell(route.public);
    updatePageTitle(route.title);
    updateActiveNav(path);

    const content    = document.getElementById('page-content');
    const authScreen = document.getElementById('auth-screen');

    if (route.public) {
      if (authScreen) { authScreen.classList.remove('hidden'); route.page.render(authScreen, routeParams); }
    } else {
      if (content) { showLoading(content); route.page.render(content, routeParams); }
    }
  }

  function matchRoute(pattern, path) {
    const patternParts = pattern.split('/');
    const pathParts    = path.split('/');
    if (patternParts.length !== pathParts.length) return null;
    const params = {};
    for (let i = 0; i < patternParts.length; i++) {
      if (patternParts[i].startsWith(':')) {
        params[patternParts[i].slice(1)] = pathParts[i];
      } else if (patternParts[i] !== pathParts[i]) {
        return null;
      }
    }
    return params;
  }

  function showAppropriateShell(isPublic) {
    const shell      = document.getElementById('app-shell');
    const authScreen = document.getElementById('auth-screen');
    if (isPublic) {
      shell?.classList.add('hidden');
      authScreen?.classList.remove('hidden');
    } else {
      shell?.classList.remove('hidden');
      authScreen?.classList.add('hidden');
      Sidebar.build();
      Topbar.build();
    }
  }

  function getDefaultRoute() {
    if (AuthService.isAdmin() || AuthService.isDirector()) return '/dashboard';
    return '/meetings';
  }

  function redirectDefault() {
    if (!AuthService.isAuthenticated()) { navigate('/login'); return; }
    navigate(getDefaultRoute());
  }

  function updatePageTitle(title) {
    setText('#pageTitle', title);
    document.title = `${title} — PIC21`;
  }

  function updateActiveNav(path) {
    document.querySelectorAll('.nav-item').forEach(item => {
      const href = item.dataset.href;
      const isActive = href && (path === href || path.startsWith(href + '/') && href !== '/');
      item.classList.toggle('active', !!isActive);
    });
  }

  function init() {
    window.addEventListener('popstate', (e) => {
      const path = e.state?.path || getHashPath();
      render(path, e.state?.params || {});
    });
    document.addEventListener('click', (e) => {
      const link = e.target.closest('[data-href]');
      if (link) { e.preventDefault(); navigate(link.dataset.href); }
    });
    const path = getHashPath();
    render(path || (AuthService.isAuthenticated() ? getDefaultRoute() : '/login'));
  }

  function getHashPath() {
    const hash = window.location.hash;
    return hash.startsWith('#/') ? hash.slice(1) : null;
  }

  function currentRoute() { return currentPath; }

  return { navigate, init, currentRoute, getDefaultRoute };
})();
