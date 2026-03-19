/* ═══════════════════════════════════════════════════════
   PIC21 — Meetings Page (lista, crear, cambiar estado, asistencia)
═══════════════════════════════════════════════════════ */

const MeetingsPage = (() => {
  let allMeetings = [];
  let currentFilter = 'ALL';

  function render(container) {
    const isAdmin   = AuthService.isAdmin();
    const isProfesor = AuthService.isProfesor();
    const canCreate = isAdmin || isProfesor;  // crear reuniones
    const canManage = isAdmin || isProfesor || AuthService.isAyudante(); // ver botones de gestión
    container.innerHTML = `
      <div class="page-header">
        <div>
          <h2>Reuniones</h2>
          <p>Gestión de reuniones del sistema</p>
        </div>
        ${canCreate ? `<button class="btn btn-primary" id="btnNewMeeting">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          Nueva reunión
        </button>` : ''}
      </div>

      <div class="filter-bar">
        <button class="filter-btn active" data-filter="ALL">Todas</button>
        <button class="filter-btn" data-filter="NO_INICIADA">No iniciadas</button>
        <button class="filter-btn" data-filter="ACTIVA">Activas</button>
        <button class="filter-btn" data-filter="BLOQUEADA">Bloqueadas</button>
      </div>

      <div id="meetingsContainer">
        <div class="loading"><div class="spinner"></div></div>
      </div>`;

    container.querySelectorAll('.filter-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        container.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        currentFilter = btn.dataset.filter;
        renderList();
      });
    });

    if (canCreate) {
      document.getElementById('btnNewMeeting')?.addEventListener('click', openCreateModal);
    }

    loadMeetings();
  }

  async function loadMeetings() {
    try {
      const response = await Api.get('/meetings');
      allMeetings = response?.content ? response.content : response;
      if (!Array.isArray(allMeetings)) allMeetings = [];
      renderList();
    } catch (err) {
      Toast.error('Error', err.message);
      showEmpty('#meetingsContainer', 'Error al cargar reuniones', err.message);
    }
  }

  function renderList() {
    const filtered = currentFilter === 'ALL'
      ? allMeetings
      : allMeetings.filter(m => m.status === currentFilter);

    const container = document.getElementById('meetingsContainer');
    if (!container) return;

    if (!filtered.length) {
      showEmpty(container, 'Sin reuniones', 'No hay reuniones para mostrar');
      return;
    }

    const isAdmin    = AuthService.isAdmin();
    const isProfesor = AuthService.isProfesor();
    const canManage  = isAdmin || isProfesor || AuthService.isAyudante();

    container.innerHTML = `<div class="meetings-grid">${filtered.map(m => meetingCard(m, canManage, isAdmin, isProfesor)).join('')}</div>`;

    container.querySelectorAll('[data-action]').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const id = btn.dataset.id;
        const action = btn.dataset.action;
        const meeting = allMeetings.find(m => String(m.id) === String(id));
        if      (action === 'activate')   changeStatus(id, 'ACTIVA');
        else if (action === 'block')      changeStatus(id, 'BLOQUEADA');
        else if (action === 'reactivate') changeStatus(id, 'ACTIVA');
        else if (action === 'delete')     deleteMeeting(id, meeting?.title);
        else if (action === 'edit')       openEditModal(meeting);
        else if (action === 'attend')     openAttendanceModal(id, meeting?.title);
        else if (action === 'detail')     Router.navigate('/meetings/' + id);
      });
    });
  }

  function meetingCard(m, canManage, isAdmin, isProfesor) {
    const isActiva    = m.status === 'ACTIVA';
    const isBloqueada = m.status === 'BLOQUEADA';
    const isNoIniciada = m.status === 'NO_INICIADA';

    let actions = '';
    if (canManage) {
      // Activar: ADMIN, PROFESOR, AYUDANTE
      if (isNoIniciada)               actions += `<button class="btn btn-primary btn-sm"   data-action="activate"   data-id="${m.id}">&#9654; Activar</button>`;
      // Bloquear: solo ADMIN
      if (isActiva    && isAdmin)      actions += `<button class="btn btn-danger btn-sm"    data-action="block"      data-id="${m.id}">🔒 Bloquear</button>`;
      // Reactivar: solo ADMIN
      if (isBloqueada && isAdmin)      actions += `<button class="btn btn-primary btn-sm"   data-action="reactivate" data-id="${m.id}">&#8617; Reactivar</button>`;
      // Editar: ADMIN y PROFESOR
      if (!isBloqueada && (isAdmin || isProfesor)) actions += `<button class="btn btn-secondary btn-sm" data-action="edit"       data-id="${m.id}">✏️ Editar</button>`;
      // Eliminar: solo ADMIN
      if (isAdmin)                     actions += `<button class="btn btn-secondary btn-sm" data-action="delete"     data-id="${m.id}" style="color:#ef4444">🗑 Eliminar</button>`;
    }
    if (isActiva) {
      actions += `<button class="btn btn-success btn-sm" data-action="attend" data-id="${m.id}">✅ Asistir</button>`;
    }
    actions += `<button class="btn btn-secondary btn-sm" data-action="detail" data-id="${m.id}">👁 Ver detalles</button>`;

    return `
      <div class="meeting-card status-${m.status}">
        <div class="meeting-card-header">
          <div class="meeting-card-title">${escHtml(m.title)}</div>
          ${statusBadge(m.status)}
        </div>
        <div class="meeting-card-meta">
          ${m.scheduledAt ? `<span>📅 ${formatDate(m.scheduledAt)}</span>` : ''}
          ${m.accessCode  ? `<span>🔑 Código: <strong>${escHtml(m.accessCode)}</strong></span>` : ''}
          ${m.description ? `<span>📝 ${escHtml(m.description)}</span>` : ''}
          <span>👤 ${escHtml(m.createdBy || '—')}</span>
        </div>
        <div class="meeting-card-actions">${actions}</div>
      </div>`;
  }

  // ── Create Modal ──────────────────────────────────────
  function openCreateModal() {
    Modal.open('Nueva Reunión', `
      <form id="meetingForm">
        <div class="form-group">
          <label class="form-label">Título *</label>
          <input class="form-control" id="mTitle" placeholder="Ej: Reunión de inicio" required maxlength="200" />
        </div>
        <div class="form-group">
          <label class="form-label">Descripción</label>
          <textarea class="form-control" id="mDesc" placeholder="Descripción opcional" rows="2"></textarea>
        </div>
        <div class="form-group">
          <label class="form-label">Fecha y hora *</label>
          <input class="form-control" id="mDate" type="datetime-local" required />
        </div>
        <div class="form-group">
          <label class="form-label">Código de acceso</label>
          <input class="form-control" id="mCode" placeholder="Ej: ABC123 (opcional)" maxlength="20" />
        </div>
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
          <button type="submit" class="btn btn-primary" id="saveMeetingBtn">Crear reunión</button>
        </div>
      </form>`);

    const dt = new Date(Date.now() + 3600000);
    dt.setSeconds(0, 0);
    document.getElementById('mDate').value = dt.toISOString().slice(0, 16);

    document.getElementById('meetingForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const btn = document.getElementById('saveMeetingBtn');
      btn.disabled = true; btn.textContent = 'Creando...';
      try {
        await Api.post('/meetings', {
          title:       document.getElementById('mTitle').value.trim(),
          description: document.getElementById('mDesc').value.trim() || null,
          scheduledAt: document.getElementById('mDate').value,
          accessCode:  document.getElementById('mCode').value.trim() || null,
        });
        Modal.close();
        Toast.success('Reunión creada', '');
        await loadMeetings();
      } catch (err) {
        Toast.error('Error', err.message);
        btn.disabled = false; btn.textContent = 'Crear reunión';
      }
    });
  }

  // ── Edit Modal ────────────────────────────────────────
  function openEditModal(m) {
    if (!m) return;
    Modal.open('Editar Reunión', `
      <form id="meetingEditForm">
        <div class="form-group">
          <label class="form-label">Título *</label>
          <input class="form-control" id="eTitle" value="${escHtml(m.title)}" required maxlength="200" />
        </div>
        <div class="form-group">
          <label class="form-label">Descripción</label>
          <textarea class="form-control" id="eDesc" rows="2">${escHtml(m.description || '')}</textarea>
        </div>
        <div class="form-group">
          <label class="form-label">Fecha y hora *</label>
          <input class="form-control" id="eDate" type="datetime-local"
                 value="${m.scheduledAt ? m.scheduledAt.slice(0,16) : ''}" required />
        </div>
        <div class="form-group">
          <label class="form-label">Código de acceso</label>
          <input class="form-control" id="eCode" value="${escHtml(m.accessCode || '')}" maxlength="20" />
        </div>
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
          <button type="submit" class="btn btn-primary" id="saveEditBtn">Guardar cambios</button>
        </div>
      </form>`);

    document.getElementById('meetingEditForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const btn = document.getElementById('saveEditBtn');
      btn.disabled = true; btn.textContent = 'Guardando...';
      try {
        await Api.put(`/meetings/${m.id}`, {
          title:       document.getElementById('eTitle').value.trim(),
          description: document.getElementById('eDesc').value.trim() || null,
          scheduledAt: document.getElementById('eDate').value,
          accessCode:  document.getElementById('eCode').value.trim() || null,
        });
        Modal.close();
        Toast.success('Reunión actualizada', '');
        await loadMeetings();
      } catch (err) {
        Toast.error('Error', err.message);
        btn.disabled = false; btn.textContent = 'Guardar cambios';
      }
    });
  }

  // ── Attendance Form Modal ─────────────────────────────
  function openAttendanceModal(meetingId, meetingTitle) {
    const user = AuthService.getUser();
    Modal.open('Registrar Asistencia', `
      <p style="color:var(--text-muted);font-size:.9rem;margin-bottom:1rem">
        📅 <strong>${escHtml(meetingTitle || 'Reunión')}</strong><br>
        Completá tus datos para registrar asistencia.
      </p>
      <form id="attendanceForm">
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:.75rem">
          <div class="form-group">
            <label class="form-label">Nombre *</label>
            <input class="form-control" id="attNombre" value="${escHtml(user?.firstName || '')}" required />
          </div>
          <div class="form-group">
            <label class="form-label">Apellido *</label>
            <input class="form-control" id="attApellido" value="${escHtml(user?.lastName || '')}" required />
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">Correo institucional *</label>
          <input class="form-control" id="attEmail" type="email"
                 value="${escHtml(user?.email || '')}" required
                 placeholder="usuario@universidad.edu.ar" />
        </div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:.75rem">
          <div class="form-group">
            <label class="form-label">Legajo *</label>
            <input class="form-control" id="attLegajo" placeholder="Ej: 12345" required maxlength="20" />
          </div>
          <div class="form-group">
            <label class="form-label">Carrera que cursás *</label>
            <input class="form-control" id="attCarrera" placeholder="Ej: Ing. en Sistemas" required maxlength="150" />
          </div>
        </div>
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
          <button type="submit" class="btn btn-success" id="saveAttendanceBtn">✅ Confirmar asistencia</button>
        </div>
      </form>`);

    document.getElementById('attendanceForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const btn = document.getElementById('saveAttendanceBtn');
      btn.disabled = true; btn.textContent = 'Registrando...';
      try {
        await Api.post(`/attendances/meeting/${meetingId}/self`, {
          nombre:              document.getElementById('attNombre').value.trim(),
          apellido:            document.getElementById('attApellido').value.trim(),
          correoInstitucional: document.getElementById('attEmail').value.trim(),
          legajo:              document.getElementById('attLegajo').value.trim(),
          carrera:             document.getElementById('attCarrera').value.trim(),
        });
        Modal.close();
        Toast.success('✅ Asistencia registrada', `Reunión: ${meetingTitle}`);
      } catch (err) {
        Toast.error('No se pudo registrar', err.message);
        btn.disabled = false; btn.textContent = '✅ Confirmar asistencia';
      }
    });
  }

  // ── Status change ─────────────────────────────────────
  async function changeStatus(id, newStatus) {
    const labels = { 'ACTIVA': 'activada', 'BLOQUEADA': 'bloqueada' };
    try {
      await Api.patch(`/meetings/${id}/status`, { status: newStatus });
      Toast.success(`Reunión ${labels[newStatus] || 'actualizada'}`, '');
      await loadMeetings();
    } catch (err) {
      Toast.error('No se pudo cambiar el estado', err.message);
    }
  }

  // ── Delete ────────────────────────────────────────────
  async function deleteMeeting(id, title) {
    if (!confirm(`¿Eliminar la reunión "${title}"? Esta acción no se puede deshacer.`)) return;
    try {
      await Api.delete(`/meetings/${id}`);
      Toast.success('Reunión eliminada', '');
      await loadMeetings();
    } catch (err) {
      Toast.error('No se pudo eliminar', err.message);
    }
  }

  return { render };
})();
