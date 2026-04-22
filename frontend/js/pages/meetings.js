/* ═══════════════════════════════════════════════════════
   PIC21 — Meetings Page (lista, crear, cambiar estado, asistencia)
═══════════════════════════════════════════════════════ */

const MeetingsPage = (() => {
  let allMeetings = [];

  function render(container) {
    const isAdmin    = AuthService.isAdmin();
    const isDirector = AuthService.isDirector();
    const canCreate  = isAdmin || isDirector;
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
      <div id="meetingsContainer">
        <div class="loading"><div class="spinner"></div></div>
      </div>`;

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
    const container = document.getElementById('meetingsContainer');
    if (!container) return;
    if (!allMeetings.length) {
      showEmpty(container, 'Sin reuniones', 'No hay reuniones para mostrar');
      return;
    }
    const isAdmin    = AuthService.isAdmin();
    const isDirector = AuthService.isDirector();
    const canManage  = isAdmin || isDirector;

    container.innerHTML = `<div class="meetings-grid">${allMeetings.map(m => meetingCard(m, canManage, isAdmin, isDirector)).join('')}</div>`;

    container.querySelectorAll('[data-action]').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const id = btn.dataset.id;
        const action = btn.dataset.action;
        const meeting = allMeetings.find(m => String(m.id) === String(id));
        if      (action === 'activate')   changeStatus(id, 'EN_CURSO');
        else if (action === 'block')      changeStatus(id, 'BLOQUEADA');
        else if (action === 'reactivate') changeStatus(id, 'EN_CURSO');
        else if (action === 'delete')     deleteMeeting(id, meeting?.titulo);
        else if (action === 'edit')       openEditModal(meeting);
        else if (action === 'attend')     openAttendanceModal(id, meeting?.titulo);
        else if (action === 'detail')     Router.navigate('/meetings/' + id);
      });
    });
  }

  function meetingCard(m, canManage, isAdmin, isDirector) {
    const isEnCurso   = m.estado === 'EN_CURSO';
    const isBloqueada = m.estado === 'BLOQUEADA';
    const isNoIniciada = m.estado === 'NO_INICIADA';

    let actions = '';
    if (canManage) {
      if (isNoIniciada && isAdmin)               actions += `<button class="btn btn-primary btn-sm"   data-action="activate"   data-id="${m.id}">&#9654; Activar</button>`;
      if (isEnCurso   && isAdmin)                actions += `<button class="btn btn-danger btn-sm"    data-action="block"      data-id="${m.id}">🔒 Bloquear</button>`;
      if (isBloqueada && isAdmin)                actions += `<button class="btn btn-primary btn-sm"   data-action="reactivate" data-id="${m.id}">&#8617; Reactivar</button>`;
      if (!isBloqueada && (isAdmin || isDirector)) actions += `<button class="btn btn-secondary btn-sm" data-action="edit"       data-id="${m.id}">✏️ Editar</button>`;
      if (isAdmin)                               actions += `<button class="btn btn-secondary btn-sm" data-action="delete"     data-id="${m.id}" style="color:#ef4444">🗑 Eliminar</button>`;
    }
    const canAttend = isEnCurso && !AuthService.isAdmin() && !AuthService.isDirector() && !AuthService.isProfesor()
      && (AuthService.isEstudiante() || AuthService.isAyudante() || AuthService.isEgresado());
    if (canAttend) {
      actions += `<button class="btn btn-success btn-sm" data-action="attend" data-id="${m.id}">✅ Registrar asistencia</button>`;
    }
    actions += `<button class="btn btn-secondary btn-sm" data-action="detail" data-id="${m.id}">👁 Ver detalles</button>`;

    return `
      <div class="meeting-card status-${m.estado}">
        <div class="meeting-card-header">
          <div class="meeting-card-title">${escHtml(m.titulo)}</div>
          ${statusBadge(m.estado)}
        </div>
        <div class="meeting-card-meta">
          ${m.fechaInicio  ? `<span>📅 ${formatDate(m.fechaInicio)}</span>` : ''}
          ${m.accessCode   ? `<span style="word-break:break-all;overflow-wrap:anywhere">🔗 <a href="${escHtml(m.accessCode)}" target="_blank" rel="noopener" style="color:var(--primary);text-decoration:underline;">Link de reunión</a></span>` : ''}
          ${m.descripcion  ? `<span>📝 ${escHtml(m.descripcion)}</span>` : ''}
          <span>👤 ${escHtml(m.creadoPorUsername || '—')}</span>
        </div>
        <div class="meeting-card-actions">${actions}</div>
      </div>`;
  }

  // ── Helpers de links extra ────────────────────────────
  let _linksCreate = [];
  let _linksEdit   = [];
  let _newsLinksCreate = [];
  let _newsLinksEdit   = [];

  function renderLinksBuilder(containerId, linksArr, prefix) {
    const c = document.getElementById(containerId);
    if (!c) return;
    c.innerHTML = linksArr.map((l, i) => `
      <div style="display:flex;gap:.5rem;align-items:center;margin-bottom:.4rem;">
        <input class="form-control" id="${prefix}_link_${i}" value="${escHtml(l)}"
               placeholder="https://..." style="flex:1;font-size:.9rem;" />
        <button type="button" class="btn btn-secondary btn-sm" data-li="${i}"
                style="color:#ef4444;padding:.25rem .5rem;"
                onclick="this.closest('[id]').parentNode.querySelector('[id]') || 0">✕</button>
      </div>`).join('');
    c.querySelectorAll('button').forEach(btn => {
      btn.addEventListener('click', () => {
        linksArr.splice(Number(btn.dataset.li), 1);
        renderLinksBuilder(containerId, linksArr, prefix);
      });
    });
  }

  function collectLinks(containerId, prefix, linksArr) {
    linksArr.length = 0;
    linksArr.push(...Array.from(document.querySelectorAll(`[id^="${prefix}_link_"]`))
      .map(el => el.value.trim()).filter(Boolean));
    return linksArr.slice();
  }

  // ── Create Modal ──────────────────────────────────────
  function openCreateModal() {
    _linksCreate = [];
    _newsLinksCreate = [];
    Modal.open('Nueva Reunión', `
      <form id="meetingForm" style="max-height:70vh;overflow-y:auto;padding-right:.25rem;">
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
          <label class="form-label">Link de reunión</label>
          <input class="form-control" id="mCode" type="text" placeholder="Ej: https://meet.google.com/xxx" />
        </div>
        <div class="form-group">
          <label class="form-label">🎥 Link de grabación</label>
          <input class="form-control" id="mRecording" type="text" placeholder="Link de la grabación (opcional)" />
        </div>
        <div class="form-group">
          <label class="form-label">📊 Link de presentación</label>
          <input class="form-control" id="mPresentation" type="text" placeholder="Canva, slides... (opcional)" />
        </div>
        <div class="form-group">
          <label class="form-label">📰 Link de noticias</label>
          <input class="form-control" id="mNews" type="text" placeholder="Link de noticias (opcional)" />
        </div>
        <div class="form-group">
          <label class="form-label">📰 Links extra de noticias</label>
          <div id="newsLinksCreateContainer"></div>
          <button type="button" class="btn btn-secondary btn-sm" id="btnAddNewsLinkCreate" style="margin-top:.35rem;font-size:.8rem;">+ Agregar otra noticia (link)</button>
        </div>
        <div class="form-group">
          <label class="form-label">📝 Link de actividad</label>
          <input class="form-control" id="mActivity" type="text" placeholder="Link de actividad (opcional)" />
        </div>
        <div class="form-group">
          <label class="form-label">🔗 Links adicionales</label>
          <div id="linksCreateContainer"></div>
          <button type="button" class="btn btn-secondary btn-sm" id="btnAddLinkCreate" style="margin-top:.35rem;font-size:.8rem;">+ Agregar link</button>
        </div>
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
          <button type="submit" class="btn btn-primary" id="saveMeetingBtn">Crear reunión</button>
        </div>
      </form>`, { persistent: true });

    const dt = new Date(Date.now() + 3600000);
    dt.setSeconds(0, 0);
    document.getElementById('mDate').value = dt.toISOString().slice(0, 16);

    document.getElementById('btnAddLinkCreate').addEventListener('click', () => {
      _linksCreate.push('');
      renderLinksBuilder('linksCreateContainer', _linksCreate, 'c');
    });
    renderLinksBuilder('linksCreateContainer', _linksCreate, 'c');

    document.getElementById('btnAddNewsLinkCreate').addEventListener('click', () => {
      _newsLinksCreate.push('');
      renderLinksBuilder('newsLinksCreateContainer', _newsLinksCreate, 'nc');
    });
    renderLinksBuilder('newsLinksCreateContainer', _newsLinksCreate, 'nc');

    document.getElementById('meetingForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const btn = document.getElementById('saveMeetingBtn');
      btn.disabled = true; btn.textContent = 'Creando...';
      try {
        collectLinks('linksCreateContainer', 'c', _linksCreate);
        collectLinks('newsLinksCreateContainer', 'nc', _newsLinksCreate);
        await Api.post('/meetings', {
          title:            document.getElementById('mTitle').value.trim(),
          description:      document.getElementById('mDesc').value.trim() || null,
          scheduledAt:      document.getElementById('mDate').value,
          accessCode:       document.getElementById('mCode').value.trim() || null,
          recordingLink:    document.getElementById('mRecording').value.trim() || null,
          presentacionLink: document.getElementById('mPresentation').value.trim() || null,
          newsLink:         document.getElementById('mNews').value.trim() || null,
          activityLink:     document.getElementById('mActivity').value.trim() || null,
          linksExtra:       _linksCreate.filter(Boolean),
          newsLinksExtra:   _newsLinksCreate.filter(Boolean),
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
    _linksEdit = Array.isArray(m.linksExtra) ? [...m.linksExtra] : [];
    _newsLinksEdit = Array.isArray(m.newsLinksExtra) ? [...m.newsLinksExtra] : [];

    Modal.open('Editar Reunión', `
      <form id="meetingEditForm" style="max-height:70vh;overflow-y:auto;padding-right:.25rem;">
        <div class="form-group">
          <label class="form-label">Título *</label>
          <input class="form-control" id="eTitle" value="${escHtml(m.titulo)}" required maxlength="200" />
        </div>
        <div class="form-group">
          <label class="form-label">Descripción</label>
          <textarea class="form-control" id="eDesc" rows="2">${escHtml(m.descripcion || '')}</textarea>
        </div>
        <div class="form-group">
          <label class="form-label">Fecha y hora *</label>
          <input class="form-control" id="eDate" type="datetime-local"
                 value="${m.fechaInicio ? m.fechaInicio.slice(0,16) : ''}" required />
        </div>
        <div class="form-group">
          <label class="form-label">Link de reunión</label>
          <input class="form-control" id="eCode" type="text" value="${escHtml(m.accessCode || '')}" placeholder="Ej: https://meet.google.com/xxx" />
        </div>
        <div class="form-group">
          <label class="form-label">🎥 Link de grabación</label>
          <input class="form-control" id="eRecording" type="text" value="${escHtml(m.recordingLink || '')}" placeholder="Link de la grabación (opcional)" />
        </div>
        <div class="form-group">
          <label class="form-label">📊 Link de presentación</label>
          <input class="form-control" id="ePresentation" type="text" value="${escHtml(m.presentacionLink || '')}" placeholder="Canva, slides... (opcional)" />
        </div>
        <div class="form-group">
          <label class="form-label">📰 Link de noticias</label>
          <input class="form-control" id="eNews" type="text" value="${escHtml(m.newsLink || '')}" placeholder="Link de noticias (opcional)" />
        </div>
        <div class="form-group">
          <label class="form-label">📰 Links extra de noticias</label>
          <div id="newsLinksEditContainer"></div>
          <button type="button" class="btn btn-secondary btn-sm" id="btnAddNewsLinkEdit" style="margin-top:.35rem;font-size:.8rem;">+ Agregar otra noticia (link)</button>
        </div>
        <div class="form-group">
          <label class="form-label">📝 Link de actividad</label>
          <input class="form-control" id="eActivity" type="text" value="${escHtml(m.activityLink || '')}" placeholder="Link de actividad (opcional)" />
        </div>
        <div class="form-group">
          <label class="form-label">🔗 Links adicionales</label>
          <div id="linksEditContainer"></div>
          <button type="button" class="btn btn-secondary btn-sm" id="btnAddLinkEdit" style="margin-top:.35rem;font-size:.8rem;">+ Agregar link</button>
        </div>
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
          <button type="submit" class="btn btn-primary" id="saveEditBtn">Guardar cambios</button>
        </div>
      </form>`, { persistent: true });

    document.getElementById('btnAddLinkEdit').addEventListener('click', () => {
      _linksEdit.push('');
      renderLinksBuilder('linksEditContainer', _linksEdit, 'e');
    });
    renderLinksBuilder('linksEditContainer', _linksEdit, 'e');

    document.getElementById('btnAddNewsLinkEdit').addEventListener('click', () => {
      _newsLinksEdit.push('');
      renderLinksBuilder('newsLinksEditContainer', _newsLinksEdit, 'ne');
    });
    renderLinksBuilder('newsLinksEditContainer', _newsLinksEdit, 'ne');

    document.getElementById('meetingEditForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const btn = document.getElementById('saveEditBtn');
      btn.disabled = true; btn.textContent = 'Guardando...';
      try {
        collectLinks('linksEditContainer', 'e', _linksEdit);
        collectLinks('newsLinksEditContainer', 'ne', _newsLinksEdit);
        await Api.put(`/meetings/${m.id}`, {
          title:            document.getElementById('eTitle').value.trim(),
          description:      document.getElementById('eDesc').value.trim() || null,
          scheduledAt:      document.getElementById('eDate').value,
          accessCode:       document.getElementById('eCode').value.trim() || null,
          recordingLink:    document.getElementById('eRecording').value.trim() || null,
          presentacionLink: document.getElementById('ePresentation').value.trim() || null,
          newsLink:         document.getElementById('eNews').value.trim() || null,
          activityLink:     document.getElementById('eActivity').value.trim() || null,
          linksExtra:       _linksEdit.filter(Boolean),
          newsLinksExtra:   _newsLinksEdit.filter(Boolean),
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
            <input class="form-control" id="attNombre" value="${escHtml(user?.nombre || '')}" required />
          </div>
          <div class="form-group">
            <label class="form-label">Apellido *</label>
            <input class="form-control" id="attApellido" value="${escHtml(user?.apellido || '')}" required />
          </div>
        </div>
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
          <button type="submit" class="btn btn-success" id="saveAttendanceBtn">✅ Confirmar asistencia</button>
        </div>
      </form>`, { persistent: true });

    document.getElementById('attendanceForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const btn = document.getElementById('saveAttendanceBtn');
      btn.disabled = true; btn.textContent = 'Registrando...';
      try {
        await Api.post(`/attendances/meeting/${meetingId}/self`, { presente: true });
        Modal.close();
        Toast.success('✅ Asistencia registrada', `Reunión: ${meetingTitle}`);
      } catch (err) {
        Toast.error('No se pudo registrar', err.message);
        btn.disabled = false; btn.textContent = '✅ Confirmar asistencia';
      }
    });
  }

  // ── Status change ─────────────────────────────────────
  async function changeStatus(id, newEstado) {
    const labels = { 'EN_CURSO': 'activada', 'BLOQUEADA': 'bloqueada' };
    try {
      await Api.patch(`/meetings/${id}/status`, { estado: newEstado });
      Toast.success(`Reunión ${labels[newEstado] || 'actualizada'}`, '');
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
