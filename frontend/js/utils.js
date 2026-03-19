/* ═══════════════════════════════════════════════════════
   PIC21 — Utils: toast, formatDate, formatters
═══════════════════════════════════════════════════════ */

/* ── Toast ─────────────────────────────────────────── */
const Toast = (() => {
  const icons = {
    success: '✅',
    error:   '❌',
    warning: '⚠️',
    info:    'ℹ️',
  };

  function show(title, message = '', type = 'success', duration = 4000) {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
      <span class="toast-icon">${icons[type] || icons.info}</span>
      <div class="toast-content">
        <div class="toast-title">${title}</div>
        ${message ? `<div class="toast-msg">${message}</div>` : ''}
      </div>
      <button class="toast-close" onclick="this.closest('.toast').remove()">×</button>
    `;
    container.appendChild(toast);
    if (duration > 0) {
      setTimeout(() => {
        toast.classList.add('removing');
        setTimeout(() => toast.remove(), 320);
      }, duration);
    }
    return toast;
  }

  return {
    success: (t, m) => show(t, m, 'success'),
    error:   (t, m) => show(t, m, 'error'),
    warning: (t, m) => show(t, m, 'warning'),
    info:    (t, m) => show(t, m, 'info'),
  };
})();

/* ── Date helpers ──────────────────────────────────── */
function formatDate(iso) {
  if (!iso) return '—';
  try {
    return new Date(iso).toLocaleDateString('es-AR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  } catch { return iso; }
}

function formatDateShort(iso) {
  if (!iso) return '—';
  try {
    return new Date(iso).toLocaleDateString('es-AR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
    });
  } catch { return iso; }
}

/* ── Status helpers ────────────────────────────────── */
function statusBadge(status) {
  const map = {
    NO_INICIADA: { cls: 'badge-inactive', label: 'No iniciada' },
    ACTIVA:      { cls: 'badge-active',   label: 'Activa'      },
    BLOQUEADA:   { cls: 'badge-blocked',  label: 'Bloqueada'   },
    PENDING:     { cls: 'badge-pending',  label: 'Pendiente'   },
    IN_PROGRESS: { cls: 'badge-pending',  label: 'En progreso' },
    DONE:        { cls: 'badge-done',     label: 'Completada'  },
    CANCELLED:   { cls: 'badge-inactive', label: 'Cancelada'   },
  };
  const s = map[status] || { cls: 'badge-inactive', label: status };
  return `<span class="badge ${s.cls}">${s.label}</span>`;
}

function roleBadge(role) {
  return `<span class="badge badge-role">${role}</span>`;
}

/* ── Loading helpers ───────────────────────────────── */
function showLoading(container) {
  if (typeof container === 'string') container = document.querySelector(container);
  if (container) container.innerHTML = '<div class="loading"><div class="spinner"></div></div>';
}

function showEmpty(container, title = 'Sin datos', desc = '') {
  if (typeof container === 'string') container = document.querySelector(container);
  if (container) {
    container.innerHTML = `
      <div class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <rect x="3" y="3" width="18" height="18" rx="2"/><path d="M9 9h6M9 12h6M9 15h4"/>
        </svg>
        <h3>${title}</h3>
        ${desc ? `<p>${desc}</p>` : ''}
      </div>`;
  }
}

/* ── DOM helpers ───────────────────────────────────── */
function el(selector) { return document.querySelector(selector); }
function els(selector) { return document.querySelectorAll(selector); }
function setHTML(selector, html) {
  const node = typeof selector === 'string' ? document.querySelector(selector) : selector;
  if (node) node.innerHTML = html;
}
function setText(selector, text) {
  const node = typeof selector === 'string' ? document.querySelector(selector) : selector;
  if (node) node.textContent = text;
}

/* ── Initials ──────────────────────────────────────── */
function getInitials(name) {
  if (!name) return '?';
  return name.split(' ').slice(0, 2).map(w => w[0]).join('').toUpperCase();
}

/* ── Escape HTML ───────────────────────────────────── */
function escHtml(str) {
  const d = document.createElement('div');
  d.textContent = str || '';
  return d.innerHTML;
}

/* ── Download blob ─────────────────────────────────── */
function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url; a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}
