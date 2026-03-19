/* ═══════════════════════════════════════════════════════
   PIC21 — Sidebar Component
═══════════════════════════════════════════════════════ */

const Sidebar = (() => {
  let built = false;

  const allNavItems = [
    {
      section: 'Principal',
      items: [
        { label: 'Dashboard', href: '/dashboard', icon: '📊', roles: ['ADMIN','PROFESOR','AYUDANTE'] },
      ]
    },
    {
      section: 'Gestión',
      items: [
        { label: 'Reuniones', href: '/meetings', icon: '📅', roles: ['ADMIN','PROFESOR','AYUDANTE','ESTUDIANTE'] },
        { label: 'Tareas',    href: '/tasks',    icon: '📋', roles: ['ADMIN','PROFESOR','AYUDANTE','ESTUDIANTE'] },
      ]
    },
    {
      section: 'Administración',
      items: [
        { label: 'Usuarios', href: '/users', icon: '👥', roles: ['ADMIN'] },
      ]
    },
  ];

  function build() {
    if (built) return;
    built = true;

    // Nav
    const nav = document.getElementById('sidebarNav');
    if (!nav) return;

    const user = AuthService.getUser();
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
          <div class="user-role">${escHtml(AuthService.getPrimaryRole())}</div>
        </div>
      `;
    }

    // Logout
    document.getElementById('logoutBtn')?.addEventListener('click', () => {
      AuthService.logout();
    });

    // Collapse toggle
    const toggle = document.getElementById('sidebarToggle');
    const sidebar = document.getElementById('sidebar');
    toggle?.addEventListener('click', () => {
      sidebar?.classList.toggle('collapsed');
      localStorage.setItem('pic21_sidebar_collapsed', sidebar?.classList.contains('collapsed') ? '1' : '0');
    });

    // Restore collapsed state
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
      document.body.appendChild(overlay);
    }
    hamburger?.addEventListener('click', () => {
      sidebar?.classList.add('mobile-open');
      overlay.classList.add('show');
    });
    overlay.addEventListener('click', () => {
      sidebar?.classList.remove('mobile-open');
      overlay.classList.remove('show');
    });
  }

  function reset() { built = false; }

  return { build, reset };
})();
