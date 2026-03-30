/* ═══════════════════════════════════════════════════════
   PIC21 — Users Management Page (solo ADMIN)
   Funcionalidades:
     - Listar todos los usuarios
     - Editar perfil (nombre, apellido, email, username)
     - Editar roles
     - Habilitar / deshabilitar
     - Eliminar
═══════════════════════════════════════════════════════ */

const UsersPage = (() => {
  let allUsers = [];
  const VALID_ROLES = ['ADMIN', 'PROFESOR', 'AYUDANTE', 'ESTUDIANTE', 'EGRESADO'];

  function render(container) {
    container.innerHTML = `
      <div class="page-header">
        <div>
          <h2>Gestión de Usuarios</h2>
          <p>Administración de roles y accesos del sistema</p>
        </div>
      </div>
      <div id="usersContainer">
        <div class="loading"><div class="spinner"></div></div>
      </div>`;
    loadUsers();
  }

  async function loadUsers() {
    try {
      const data = await Api.get('/users');
      allUsers = Array.isArray(data) ? data : (data?.content ?? []);
      renderTable();
    } catch (err) {
      Toast.error('Error al cargar usuarios', err.message);
      showEmpty('#usersContainer', 'Error', err.message);
    }
  }

  function renderTable() {
    const container = document.getElementById('usersContainer');
    if (!container) return;
    if (!allUsers.length) {
      showEmpty(container, 'Sin usuarios', '');
      return;
    }

    const me = AuthService.getUser();

    container.innerHTML = `
      <div class="card">
        <div class="card-body" style="padding:0">
          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Usuario</th>
                  <th>Nombre</th>
                  <th>Email</th>
                  <th>Contraseña (Hash)</th>
                  <th>Roles</th>
                  <th>Estado</th>
                  <th style="text-align:center">Acciones</th>
                </tr>
              </thead>
              <tbody>
                ${allUsers.map((u, i) => userRow(u, i + 1, me?.username)).join('')}
              </tbody>
            </table>
          </div>
        </div>
      </div>`;

    // Bind action buttons
    container.querySelectorAll('[data-action]').forEach(btn => {
      btn.addEventListener('click', () => {
        const id   = Number(btn.dataset.id);
        const user = allUsers.find(u => u.id === id);
        const action = btn.dataset.action;
        if      (action === 'edit')   openEditProfileModal(user);
        else if (action === 'roles')  openEditRolesModal(user);
        else if (action === 'toggle') toggleUser(id, user?.username);
        else if (action === 'delete') deleteUser(id, user?.username);
      });
    });
  }

  function userRow(u, idx, myUsername) {
    const isSelf = u.username === myUsername;
    const roleBadges = (u.roles || []).map(r => `<span class="badge badge-role" style="font-size:0.68rem">${r}</span>`).join(' ');
    const statusBadgeHtml = u.enabled
      ? `<span class="badge badge-active">Activo</span>`
      : `<span class="badge badge-inactive">Inactivo</span>`;

    return `
      <tr>
        <td>${idx}</td>
        <td><strong>${escHtml(u.username)}</strong>${isSelf ? ' <span style="color:var(--text-muted);font-size:0.75rem">(vos)</span>' : ''}</td>
        <td>${escHtml(u.firstName || '')} ${escHtml(u.lastName || '')}</td>
        <td style="font-size:0.85rem">${escHtml(u.email || '—')}</td>
        <td style="font-size:0.7rem;max-width:180px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;color:var(--text-muted)" title="${escHtml(u.passwordHash || '')}">${escHtml(u.passwordHash ? u.passwordHash.substring(0, 20) + '...' : '—')}</td>
        <td>${roleBadges || '—'}</td>
        <td>${statusBadgeHtml}</td>
        <td style="text-align:center;white-space:nowrap;display:flex;gap:.25rem;justify-content:center;flex-wrap:wrap">
          <button class="btn btn-primary btn-sm" data-action="edit" data-id="${u.id}">
            ✏️ Editar
          </button>
          <button class="btn btn-secondary btn-sm" data-action="roles" data-id="${u.id}" ${isSelf ? 'disabled title="No podés modificar tus propios roles"' : ''}>
            🎭 Roles
          </button>
          <button class="btn btn-secondary btn-sm" data-action="toggle" data-id="${u.id}" ${isSelf ? 'disabled' : ''}>
            ${u.enabled ? '🔒 Deshab.' : '✅ Habilitar'}
          </button>
          <button class="btn btn-secondary btn-sm" data-action="delete" data-id="${u.id}"
            style="color:#ef4444" ${isSelf ? 'disabled title="No podés eliminar tu propia cuenta"' : ''}>
            🗑
          </button>
        </td>
      </tr>`;
  }

  // ── Edit Profile Modal ─────────────────────────────────
  function openEditProfileModal(user) {
    if (!user) return;

    Modal.open(`Editar perfil — ${user.username}`, `
      <form id="editProfileForm">
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;">
          <div class="form-group">
            <label class="form-label">Nombre *</label>
            <input class="form-control" id="epFirstName" value="${escHtml(user.firstName || '')}" required maxlength="100" />
          </div>
          <div class="form-group">
            <label class="form-label">Apellido *</label>
            <input class="form-control" id="epLastName" value="${escHtml(user.lastName || '')}" required maxlength="100" />
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">Nombre de usuario *</label>
          <input class="form-control" id="epUsername" value="${escHtml(user.username || '')}" required minlength="3" maxlength="50" />
        </div>
        <div class="form-group">
          <label class="form-label">Email *</label>
          <input class="form-control" id="epEmail" type="email" value="${escHtml(user.email || '')}" required maxlength="150" />
        </div>
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
          <button type="submit" class="btn btn-primary" id="saveProfileBtn">Guardar cambios</button>
        </div>
      </form>`);

    document.getElementById('editProfileForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const btn = document.getElementById('saveProfileBtn');
      btn.disabled = true;
      btn.textContent = 'Guardando...';
      try {
        await Api.put(`/users/${user.id}`, {
          firstName: document.getElementById('epFirstName').value.trim(),
          lastName:  document.getElementById('epLastName').value.trim(),
          username:  document.getElementById('epUsername').value.trim(),
          email:     document.getElementById('epEmail').value.trim(),
        });
        Modal.close();
        Toast.success('Perfil actualizado', `${document.getElementById('epUsername')?.value || user.username}`);
        await loadUsers();
      } catch (err) {
        Toast.error('Error al actualizar', err.message);
        btn.disabled = false;
        btn.textContent = 'Guardar cambios';
      }
    });
  }

  // ── Edit Roles Modal ───────────────────────────────────
  function openEditRolesModal(user) {
    if (!user) return;
    const currentRoles = user.roles || [];

    Modal.open(`Editar roles — ${user.username}`, `
      <p class="text-sm" style="color:var(--text-muted);margin-bottom:1rem">
        Seleccioná los roles para <strong>${escHtml(user.username)}</strong>.
        Debe tener al menos uno.
      </p>
      <div class="form-group">
        ${VALID_ROLES.map(r => `
          <label style="display:flex;align-items:center;gap:.5rem;margin-bottom:.5rem;cursor:pointer">
            <input type="checkbox" id="role_${r}" value="${r}"
              ${currentRoles.includes(r) ? 'checked' : ''}
              style="width:1rem;height:1rem" />
            <span>${r}</span>
          </label>`).join('')}
      </div>
      <div class="form-actions">
        <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
        <button type="button" class="btn btn-primary" id="saveRolesBtn">Guardar roles</button>
      </div>`);

    document.getElementById('saveRolesBtn').addEventListener('click', async () => {
      const btn = document.getElementById('saveRolesBtn');
      const selected = VALID_ROLES.filter(r => document.getElementById(`role_${r}`)?.checked);
      if (!selected.length) { Toast.warning('Seleccioná al menos un rol', ''); return; }
      btn.disabled = true;
      btn.textContent = 'Guardando...';
      try {
        await Api.put(`/users/${user.id}/roles`, { roles: selected });
        Modal.close();
        Toast.success('Roles actualizados', `${user.username} → ${selected.join(', ')}`);
        await loadUsers();
      } catch (err) {
        Toast.error('Error', err.message);
        btn.disabled = false;
        btn.textContent = 'Guardar roles';
      }
    });
  }

  // ── Toggle enabled ─────────────────────────────────────
  async function toggleUser(id, username) {
    try {
      await Api.patch(`/users/${id}/toggle`);
      Toast.success('Estado actualizado', username);
      await loadUsers();
    } catch (err) {
      Toast.error('Error', err.message);
    }
  }

  // ── Delete user ────────────────────────────────────────
  async function deleteUser(id, username) {
    if (!confirm(`¿Eliminar al usuario "${username}"? Esta acción no se puede deshacer.`)) return;
    try {
      await Api.delete(`/users/${id}`);
      Toast.success('Usuario eliminado', username);
      await loadUsers();
    } catch (err) {
      Toast.error('No se pudo eliminar', err.message);
    }
  }

  return { render };
})();
