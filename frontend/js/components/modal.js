/* ═══════════════════════════════════════════════════════
   PIC21 — Modal Component
═══════════════════════════════════════════════════════ */

const Modal = (() => {
  let onClose = null;
  let _persistent = false;

  function open(title, bodyHtml, options = {}) {
    const overlay = document.getElementById('modal-overlay');
    const modal   = document.getElementById('modal');
    setText('#modal-title', title);
    setHTML('#modal-body', bodyHtml);
    overlay.classList.remove('hidden');
    onClose = options.onClose || null;
    _persistent = options.persistent || false;

    // Focus first input
    setTimeout(() => {
      const first = modal.querySelector('input, select, textarea');
      first?.focus();
    }, 100);
  }

  function close() {
    document.getElementById('modal-overlay')?.classList.add('hidden');
    _persistent = false;
    if (onClose) { onClose(); onClose = null; }
  }

  function init() {
    document.getElementById('modalClose')?.addEventListener('click', close);
    document.getElementById('modal-overlay')?.addEventListener('click', (e) => {
      if (e.target === e.currentTarget && !_persistent) close();
    });
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && !_persistent) close();
    });
  }

  return { open, close, init };
})();
