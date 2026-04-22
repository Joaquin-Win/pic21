/* ═══════════════════════════════════════════════════════
   PIC21 — Sidebar Component (UML v8)
═══════════════════════════════════════════════════════ */

const Sidebar = (() => {
  let built = false;

  // Roles UML v8
  const ALL_ROLES = ['R04_ADMIN','R01_PROFESOR','R05_DIRECTOR','R06_AYUDANTE','R02_ESTUDIANTE','R03_EGRESADO'];

  const allNavItems = [
    {
      section: 'Principal',
      items: [
        {
          label: 'Dashboard',
          href: '/dashboard',
          icon: '📊',
          roles: ['R04_ADMIN','R05_DIRECTOR']
        },
      ]
    },
    {
      section: 'Gestión',
      items: [
        { label: 'Reuniones',             href: '/meetings', icon: '📅', roles: ALL_ROLES },
        { label: 'Recuperar asistencia',  href: '/tasks',    icon: '📋', roles: ALL_ROLES },
      ]
    },
    {
      section: 'Administración',
      items: [
        { label: 'Usuarios', href: '/users', icon: '👥', roles: ['R04_ADMIN'] },
        { label: 'Archivos', href: '/files', icon: '📁', roles: ['R04_ADMIN'] },
      ]
    },
  ];

  function build() {
    if (built) return;
    built = true;

    const nav = document.getElementById('sidebarNav');
    if (!nav) return;

    const user  = AuthService.getUser();
    const roles = user?.roles || [];

    let html = '';
    for (const section of allNavItems) {
      const visibleItems = section.items.filter(item =>
        item.roles.some(r => roles.includes(r))
      );
      if (!visibleItems.length) continue;
      html += `<div class="nav-section-label">${section.section}</div>`;
      for (const item of visibleItems) {
        html += `
          <button class="nav-item" data-href="${item.href}">
            <span style="font-size:1rem;line-height:1">${item.icon}</span>
            <span class="nav-label">${item.label}</span>
          </button>`;
      }
    }
    nav.innerHTML = html;

    // User info
    const userInfo = document.getElementById('userInfo');
    if (userInfo && user) {
      const initials = getInitials(AuthService.getDisplayName());
      userInfo.innerHTML = `
        <div class="user-avatar">${initials}</div>
        <div>
          <div class="user-name">${escHtml(AuthService.getDisplayName())}</div>
          <div class="user-role">${escHtml(AuthService.getDisplayRole())}</div>
        </div>
      `;
    }

    // Logout
    document.getElementById('logoutBtn')?.addEventListener('click', () => {
      AuthService.logout();
    });

    // Collapse toggle
    const toggle  = document.getElementById('sidebarToggle');
    const sidebar = document.getElementById('sidebar');
    toggle?.addEventListener('click', () => {
      sidebar?.classList.toggle('collapsed');
      localStorage.setItem('pic21_sidebar_collapsed', sidebar?.classList.contains('collapsed') ? '1' : '0');
    });

    if (localStorage.getItem('pic21_sidebar_collapsed') === '1') {
      sidebar?.classList.add('collapsed');
    }

    // Mobile hamburger
    const hamburger = document.getElementById('hamburger');
    let overlay = document.getElementById('sidebarOverlay');
    if (!overlay) {
      overlay = document.createElement('div');
      overlay.id = 'sidebarOverlay';
      overlay.className = 'sidebar-overlay';
      sidebar.parentNode.insertBefore(overlay, sidebar.nextSibling);
    }
    hamburger?.addEventListener('click', () => {
      const isMobile = window.innerWidth <= 768;
      if (isMobile) {
        sidebar?.classList.remove('collapsed');
        sidebar?.classList.add('mobile-open');
        overlay.classList.add('show');
      } else {
        sidebar?.classList.remove('collapsed');
        localStorage.setItem('pic21_sidebar_collapsed', '0');
      }
    });
    const closeMobileSidebar = () => {
      sidebar?.classList.remove('mobile-open');
      overlay.classList.remove('show');
    };
    overlay.addEventListener('click', closeMobileSidebar);
    nav.querySelectorAll('.nav-item').forEach(item => {
      item.addEventListener('click', closeMobileSidebar);
    });
  }

  function reset() { built = false; }

  return { build, reset };
})();
