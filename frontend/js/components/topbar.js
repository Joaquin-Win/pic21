/* ═══════════════════════════════════════════════════════
   PIC21 — Topbar Component
═══════════════════════════════════════════════════════ */

const Topbar = (() => {
  let built = false;

  function build() {
    if (built) return;
    built = true;

    // Avatar initials
    const avatar = document.getElementById('topbarAvatar');
    if (avatar) {
      avatar.textContent = getInitials(AuthService.getDisplayName());
      avatar.title = AuthService.getDisplayName();
    }

    // Force light mode (dark mode removed)
    document.documentElement.setAttribute('data-theme', 'light');
    localStorage.removeItem('pic21_theme');

    // Hide dark mode toggle button if it exists
    const themeToggle = document.getElementById('themeToggle');
    if (themeToggle) themeToggle.style.display = 'none';
  }

  function reset() { built = false; }

  return { build, reset };
})();
