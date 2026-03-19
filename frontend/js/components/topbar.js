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

    // Dark mode toggle
    const themeToggle = document.getElementById('themeToggle');
    const sunIcon  = themeToggle?.querySelector('.icon-sun');
    const moonIcon = themeToggle?.querySelector('.icon-moon');

    const savedTheme = localStorage.getItem('pic21_theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
    updateIcons(savedTheme);

    themeToggle?.addEventListener('click', () => {
      const current = document.documentElement.getAttribute('data-theme');
      const next = current === 'dark' ? 'light' : 'dark';
      document.documentElement.setAttribute('data-theme', next);
      localStorage.setItem('pic21_theme', next);
      updateIcons(next);
    });

    function updateIcons(theme) {
      if (!sunIcon || !moonIcon) return;
      if (theme === 'dark') {
        sunIcon.classList.add('hidden');
        moonIcon.classList.remove('hidden');
      } else {
        sunIcon.classList.remove('hidden');
        moonIcon.classList.add('hidden');
      }
    }
  }

  function reset() { built = false; }

  return { build, reset };
})();
