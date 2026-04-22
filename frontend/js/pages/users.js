/* ═══════════════════════════════════════════════════════
   PIC21 — Users Management Page (solo R04_ADMIN) — UML v8
   Funcionalidades:
     - Listar todos los usuarios
     - Editar perfil (nombre, apellido, username, perfiles A/B)
     - Editar roles
     - Habilitar / deshabilitar
     - Eliminar
═══════════════════════════════════════════════════════ */

const UsersPage = (() => {
  let allUsers = [];

  // Roles del sistema (un rol por línea; el admin asigna los que correspondan)
  const VALID_ROLES = [
    { value: 'R04_ADMIN',      label: 'Admin' },
    { value: 'R05_DIRECTOR',   label: 'Director' },
    { value: 'R01_PROFESOR',   label: 'Profesor' },
    { value: 'R03_EGRESADO',   label: 'Egresado' },
    { value: 'R06_AYUDANTE',   label: 'Ayudante' },
    { value: 'R02_ESTUDIANTE', label: 'Estudiante' },
  ];

  // Roles Grupo A (PerfilPersonal)
  const GRUPO_A = ['R04_ADMIN','R01_PROFESOR','R05_DIRECTOR','R03_EGRESADO'];
  // Roles Grupo B (PerfilEstudiantil)
  const GRUPO_B = ['R02_ESTUDIANTE','R06_AYUDANTE'];

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
    if (!allUsers.length) { showEmpty(container, 'Sin usuarios', ''); return; }

    const me = AuthService.getUser();

    container.innerHTML = `
      <div class="card">
        <div class="card-body" style="padding:0">
          <div class="table-wrapper">
            <table>
              <thead>
              <tr>
                <th>#</th>
                <th>Nombre</th>
                <th>Email</th>
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

    container.querySelectorAll('[data-action]').forEach(btn => {
      btn.addEventListener('click', () => {
        const id     = Number(btn.dataset.id);
        const user   = allUsers.find(u => u.id === id);
        const action = btn.dataset.action;
        if      (action === 'edit')   openEditProfileModal(user);
        else if (action === 'roles')  openEditRolesModal(user);
        else if (action === 'toggle') toggleUser(id, user?.username);
        else if (action === 'delete') deleteUser(id, user?.username);
      });
    });
  }

  function rolLabel(r) {
    return VALID_ROLES.find(x => x.value === r)?.label || r;
  }

  function userRow(u, idx, myUsername) {
    const isSelf = u.username === myUsername;
    const roleBadges = (u.roles || []).map(r =>
      `<span class="badge badge-role" style="font-size:0.68rem">${escHtml(rolLabel(r))}</span>`
    ).join(' ');

    const statusBadgeHtml = u.activo
      ? `<span class="badge badge-active">Activo</span>`
      : `<span class="badge badge-inactive">Inactivo</span>`;

    const fullName = `${escHtml(u.nombre || '')} ${escHtml(u.apellido || '')}`.trim() || '—';

    return `
      <tr>
        <td>${idx}</td>
        <td><strong>${fullName}</strong>${isSelf ? ' <span style="color:var(--text-muted);font-size:0.75rem">(vos)</span>' : ''}</td>
        <td style="font-size:0.85rem">${escHtml(u.email || '—')}</td>
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
            ${u.activo ? '🔒 Deshab.' : '✅ Habilitar'}
          </button>
          <button class="btn btn-secondary btn-sm" data-action="delete" data-id="${u.id}"
            style="color:#ef4444" ${isSelf ? 'disabled title="No podés eliminar tu propia cuenta"' : ''}>
            🗑
          </button>
        </td>
      </tr>`;
  }

  // ── Edit Profile Modal ──────────────────────────────
  function openEditProfileModal(user) {
    if (!user) return;
    const roles      = user.roles || [];
    const esGrupoA   = roles.some(r => GRUPO_A.includes(r));
    const esGrupoB   = roles.some(r => GRUPO_B.includes(r));

    Modal.open(`Editar perfil — ${user.username}`, `
      <form id="editProfileForm">
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;">
          <div class="form-group">
            <label class="form-label">Nombre *</label>
            <input class="form-control" id="epNombre"   value="${escHtml(user.nombre   || '')}" required maxlength="80" />
          </div>
          <div class="form-group">
            <label class="form-label">Apellido *</label>
            <input class="form-control" id="epApellido" value="${escHtml(user.apellido || '')}" required maxlength="80" />
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">Nombre de usuario *</label>
          <input class="form-control" id="epUsername" value="${escHtml(user.username || '')}" required minlength="3" maxlength="50" />
        </div>
        ${esGrupoA ? `
        <hr style="margin:1rem 0;opacity:.2"/>
        <p style="font-size:.8rem;color:var(--text-muted);margin-bottom:.5rem">Perfil Personal (Grupo A)</p>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;">
          <div class="form-group">
            <label class="form-label">DNI</label>
            <input class="form-control" id="epDni"    value="${escHtml(user.dni    || '')}" maxlength="8" placeholder="12345678" />
          </div>
          <div class="form-group">
            <label class="form-label">Correo personal</label>
            <input class="form-control" id="epCorreo" value="${escHtml(user.correo || '')}" maxlength="150" type="email" />
          </div>
        </div>` : ''}
        ${esGrupoB ? `
        <hr style="margin:1rem 0;opacity:.2"/>
        <p style="font-size:.8rem;color:var(--text-muted);margin-bottom:.5rem">Perfil Estudiantil (Grupo B)</p>
        <div class="form-group">
          <label class="form-label">Correo institucional</label>
          <input class="form-control" id="epCorreoInst" value="${escHtml(user.correoInstitucional || '')}" maxlength="150" type="email" />
        </div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;">
          <div class="form-group">
            <label class="form-label">Legajo</label>
            <input class="form-control" id="epLegajo"  value="${escHtml(user.legajo  || '')}" maxlength="20" />
          </div>
          <div class="form-group">
            <label class="form-label">Carrera</label>
            <input class="form-control" id="epCarrera" value="${escHtml(user.carrera || '')}" maxlength="150" />
          </div>
        </div>` : ''}
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
          <button type="submit" class="btn btn-primary" id="saveProfileBtn">Guardar cambios</button>
        </div>
      </form>`);

    document.getElementById('editProfileForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const btn = document.getElementById('saveProfileBtn');
      btn.disabled = true; btn.textContent = 'Guardando...';
      try {
        const payload = {
          nombre:   document.getElementById('epNombre')?.value.trim(),
          apellido: document.getElementById('epApellido')?.value.trim(),
          username: document.getElementById('epUsername')?.value.trim(),
          // Grupo A
          dni:    document.getElementById('epDni')?.value.trim()    || null,
          correo: document.getElementById('epCorreo')?.value.trim() || null,
          // Grupo B
          correoInstitucional: document.getElementById('epCorreoInst')?.value.trim() || null,
          legajo:  document.getElementById('epLegajo')?.value.trim()  || null,
          carrera: document.getElementById('epCarrera')?.value.trim() || null,
        };
        await Api.put(`/users/${user.id}`, payload);
        Modal.close();
        Toast.success('Perfil actualizado', payload.username);
        await loadUsers();
      } catch (err) {
        Toast.error('Error al actualizar', err.message);
        btn.disabled = false; btn.textContent = 'Guardar cambios';
      }
    });
  }

  // ── Edit Roles Modal ───────────────────────────────
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
          <input type="checkbox" id="role_${r.value}" value="${r.value}"
            ${currentRoles.includes(r.value) ? 'checked' : ''}
            style="width:1rem;height:1rem" />
          <span>${r.label}</span>
        </label>`).join('')}
      </div>
      <div class="form-actions">
        <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
        <button type="button" class="btn btn-primary" id="saveRolesBtn">Guardar roles</button>
      </div>`);

    document.getElementById('saveRolesBtn').addEventListener('click', async () => {
      const btn      = document.getElementById('saveRolesBtn');
      const selected = VALID_ROLES.map(r => r.value).filter(v => document.getElementById(`role_${v}`)?.checked);
      if (!selected.length) { Toast.warning('Seleccioná al menos un rol', ''); return; }
      btn.disabled = true; btn.textContent = 'Guardando...';
      try {
        const updatedUser = await Api.put(`/users/${user.id}/roles`, { roles: selected });
        user.roles = [...selected];
        if (updatedUser && Array.isArray(updatedUser.roles)) {
          user.roles = [...updatedUser.roles];
        }
        Modal.close();
        Toast.success('Roles actualizados', `${user.username} → ${selected.join(', ')}`);
        renderTable();
        setTimeout(() => { loadUsers(); }, 200);
      } catch (err) {
        Toast.error('Error', err.message);
        btn.disabled = false; btn.textContent = 'Guardar roles';
      }
    });
  }

  // ── Toggle activo ──────────────────────────────────
  async function toggleUser(id, username) {
    try {
      await Api.patch(`/users/${id}/toggle`);
      Toast.success('Estado actualizado', username);
      await loadUsers();
    } catch (err) {
      Toast.error('Error', err.message);
    }
  }

  // ── Delete user ────────────────────────────────────
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
