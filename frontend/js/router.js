/* ═══════════════════════════════════════════════════════
   PIC21 — Client-side Router with role guards
═══════════════════════════════════════════════════════ */

const Router = (() => {
  // Route definitions — resolved LAZILY at call time (pages loaded in later scripts)
  function getRoutes() {
    /* eslint-disable no-undef */
    return {
      '/login':        { page: LoginPage,         title: 'Iniciar sesión',     public: true  },
      '/dashboard':    { page: DashboardPage,     title: 'Dashboard',          roles: ['ADMIN','PROFESOR','AYUDANTE'] },
      '/meetings':     { page: MeetingsPage,      title: 'Reuniones',          roles: ['ADMIN','PROFESOR','AYUDANTE','ESTUDIANTE'] },
      '/meetings/:id': { page: MeetingDetailPage, title: 'Detalle de reunión', roles: ['ADMIN','PROFESOR','AYUDANTE','ESTUDIANTE'] },
      '/tasks':        { page: TasksPage,         title: 'Tareas',             roles: ['ADMIN','PROFESOR','AYUDANTE','ESTUDIANTE'] },
      '/users':        { page: UsersPage,         title: 'Usuarios',           roles: ['ADMIN'] },
    };
  }

  let currentPath = null;

  // ── Navigate ─────────────────────────────────────────
  function navigate(path, params = {}) {
    // Clean hash
    const clean = path.startsWith('/') ? path : '/' + path;
    window.history.pushState({ path: clean, params }, '', '#' + clean);
    render(clean, params);
  }

  // ── Render ────────────────────────────────────────────
  function render(path, params = {}) {
    // Find matching route (support :id params)
    const routes = getRoutes();
    let route = null;
    let routeParams = { ...params };
    for (const [pattern, def] of Object.entries(routes)) {
      const match = matchRoute(pattern, path);
      if (match) { route = def; Object.assign(routeParams, match); break; }
    }

    if (!route) {
      // Redirect unknown to appropriate default
      redirectDefault();
      return;
    }

    // Auth guard
    if (!route.public) {
      if (!AuthService.isAuthenticated()) {
        navigate('/login');
        return;
      }
      // Role guard
      if (route.roles?.length) {
        const user = AuthService.getUser();
        const allowed = route.roles.some(r => user?.roles?.includes(r));
        if (!allowed) {
          navigate(getDefaultRoute());
          return;
        }
      }
    } else if (AuthService.isAuthenticated() && path === '/login') {
      navigate(getDefaultRoute());
      return;
    }

    currentPath = path;
    showAppropriateShell(route.public);
    updatePageTitle(route.title);
    updateActiveNav(path);

    const content = document.getElementById('page-content');
    const authScreen = document.getElementById('auth-screen');

    if (route.public) {
      if (authScreen) {
        authScreen.classList.remove('hidden');
        route.page.render(authScreen, routeParams);
      }
    } else {
      if (content) {
        showLoading(content);
        route.page.render(content, routeParams);
      }
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
      // Ensure sidebar/topbar are built
      Sidebar.build();
      Topbar.build();
    }
  }

  function getDefaultRoute() {
    if (AuthService.isAdmin() || AuthService.isStaff()) return '/dashboard';
    return '/meetings';
  }

  function redirectDefault() {
    if (!AuthService.isAuthenticated()) { navigate('/login'); return; }
    navigate(getDefaultRoute());
  }

  // ── Page title in topbar ─────────────────────────────
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

  // ── Init: handle back/forward + initial load ─────────
  function init() {
    window.addEventListener('popstate', (e) => {
      const path = e.state?.path || getHashPath();
      render(path, e.state?.params || {});
    });
    // Handle clicks on [data-href]
    document.addEventListener('click', (e) => {
      const link = e.target.closest('[data-href]');
      if (link) {
        e.preventDefault();
        navigate(link.dataset.href);
      }
    });
    // Initial render
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
