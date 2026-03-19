/* ═══════════════════════════════════════════════════════
   PIC21 — App Bootstrap (entry point)
   Loaded LAST — all dependencies are ready.
═══════════════════════════════════════════════════════ */

(function initApp() {
  // Apply saved theme ASAP
  const savedTheme = localStorage.getItem('pic21_theme') || 'light';
  document.documentElement.setAttribute('data-theme', savedTheme);

  // Init global modal
  Modal.init();

  // Start router
  Router.init();

  // Handle logout from topbar (delegated)
  document.addEventListener('pic21:logout', () => {
    AuthService.logout();
  });

  console.log('[PIC21] Application initialized ✅');
})();
