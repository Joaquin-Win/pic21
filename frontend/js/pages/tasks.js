/* ═══════════════════════════════════════════════════════
   PIC21 — Tasks Page (crear / ver mis tareas)
═══════════════════════════════════════════════════════ */

const TasksPage = (() => {
  function render(container) {
    const isStaff = AuthService.isStaff();
    container.innerHTML = `
      <div class="page-header">
        <div>
          <h2>Tareas</h2>
          <p>${isStaff ? 'Creación y seguimiento de tareas' : 'Mis tareas asignadas'}</p>
        </div>
      </div>

      ${isStaff ? renderStaffView() : ''}
      <div id="myTasksSection">
        <div class="loading"><div class="spinner"></div></div>
      </div>`;

    if (isStaff) {
      setupCreateTaskForm();
    }
    loadMyTasks();
  }

  function renderStaffView() {
    return `
      <div class="card" style="margin-bottom:1.5rem;">
        <div class="card-header"><span class="card-title">➕ Crear tarea para ausentes</span></div>
        <div class="card-body">
          <p class="text-sm" style="color:var(--text-muted);margin-bottom:1rem;">
            La tarea se asignará automáticamente a todos los estudiantes que <strong>no asistieron</strong> a la reunión seleccionada.
          </p>
          <form id="quickTaskForm">
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;">
              <div class="form-group" style="grid-column:1/-1">
                <label class="form-label">Reunión *</label>
                <select class="form-control" id="taskMeetingId" required>
                  <option value="">Cargando reuniones...</option>
                </select>
              </div>
              <div class="form-group">
                <label class="form-label">Título *</label>
                <input class="form-control" id="taskTitle" placeholder="Título de la tarea" required maxlength="200" />
              </div>
              <div class="form-group">
                <label class="form-label">Link (URL)</label>
                <input class="form-control" id="taskLink" type="url" placeholder="https://..." />
              </div>
              <div class="form-group" style="grid-column:1/-1">
                <label class="form-label">Descripción *</label>
                <textarea class="form-control" id="taskDesc" rows="3" placeholder="Descripción detallada" required></textarea>
              </div>
            </div>
            <div class="form-actions" style="margin-top:0">
              <button type="submit" class="btn btn-primary" id="createTaskBtn">
                ➕ Crear y asignar tarea
              </button>
            </div>
          </form>
        </div>
      </div>`;
  }

  async function setupCreateTaskForm() {
    // Load meetings for dropdown — backend returns a Page object, extract .content
    try {
      const paged   = await Api.get('/meetings?size=200&sort=scheduledAt,desc');
      const list    = Array.isArray(paged) ? paged : (paged?.content ?? []);
      const select  = document.getElementById('taskMeetingId');
      if (!select) return;
      if (!list.length) {
        select.innerHTML = '<option value="">Sin reuniones disponibles</option>';
      } else {
        select.innerHTML =
          '<option value="">— Seleccioná una reunión —</option>' +
          list.map(m =>
            `<option value="${m.id}">[${m.status}] ${escHtml(m.title)}</option>`
          ).join('');
      }
    } catch (err) {
      const sel = document.getElementById('taskMeetingId');
      if (sel) sel.innerHTML = '<option value="">Error al cargar reuniones</option>';
    }

    // Form submit
    document.getElementById('quickTaskForm')?.addEventListener('submit', async (e) => {
      e.preventDefault();
      const btn = document.getElementById('createTaskBtn');
      btn.disabled = true;
      btn.textContent = 'Creando...';
      const meetingId = document.getElementById('taskMeetingId').value;
      try {
        const result = await Api.post(`/tasks/meeting/${meetingId}`, {
          title:       document.getElementById('taskTitle').value.trim(),
          description: document.getElementById('taskDesc').value.trim(),
          link:        document.getElementById('taskLink').value.trim() || null,
        });
        const count = Array.isArray(result) ? result.length : 1;
        Toast.success('Tarea creada', `Asignada a ${count} estudiante(s) ausente(s)`);
        document.getElementById('quickTaskForm').reset();
        loadMyTasks();
      } catch (err) {
        Toast.error('Error al crear tarea', err.message);
      } finally {
        btn.disabled = false;
        btn.textContent = '➕ Crear y asignar tarea';
      }
    });
  }

  async function loadMyTasks() {
    const section = document.getElementById('myTasksSection');
    if (!section) return;
    try {
      const response = await Api.get('/tasks/my');
      const tasks = response?.content ? response.content : response;
      const arr = Array.isArray(tasks) ? tasks : [];
      renderMyTasks(section, arr);
    } catch (err) {
      Toast.error('Error', err.message);
      showEmpty(section, 'Error al cargar tareas', err.message);
    }
  }

  function renderMyTasks(container, tasks) {
    const title = AuthService.isEstudiante() ? 'Mis tareas pendientes' : 'Tareas asignadas por mí';

    if (!tasks.length) {
      container.innerHTML = `
        <div class="card">
          <div class="card-header"><span class="card-title">📋 ${title}</span></div>
          <div class="card-body">
            <div class="empty-state">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
              <h3>¡Sin tareas!</h3>
              <p>${AuthService.isEstudiante() ? 'No tenés tareas pendientes asignadas.' : 'No hay tareas creadas aún.'}</p>
            </div>
          </div>
        </div>`;
      return;
    }

    container.innerHTML = `
      <div class="card">
        <div class="card-header">
          <span class="card-title">📋 ${title}</span>
          <span class="text-sm" style="color:var(--text-muted)">${tasks.length} tarea(s)</span>
        </div>
        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Título</th>
                <th>Reunión</th>
                <th>Estado</th>
                ${!AuthService.isEstudiante() ? '<th>Asignada a</th>' : ''}
                <th>Link</th>
              </tr>
            </thead>
            <tbody>
              ${tasks.map(t => `
                <tr>
                  <td>
                    <strong>${escHtml(t.title)}</strong>
                    ${t.description ? `<div class="text-xs" style="color:var(--text-muted);margin-top:.2rem">${escHtml(t.description.slice(0, 80))}${t.description.length > 80 ? '…' : ''}</div>` : ''}
                  </td>
                  <td>${escHtml(t.meetingTitle || '—')}</td>
                  <td>${statusBadge(t.status || 'PENDING')}</td>
                  ${!AuthService.isEstudiante() ? `<td>${escHtml(t.assignedToUsername || '—')}</td>` : ''}
                  <td>${t.link ? `<a href="${escHtml(t.link)}" target="_blank" rel="noreferrer" class="btn btn-secondary btn-sm">🔗 Abrir</a>` : '—'}</td>
                </tr>`).join('')}
            </tbody>
          </table>
        </div>
      </div>`;
  }

  return { render };
})();
