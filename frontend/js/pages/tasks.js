/* ═══════════════════════════════════════════════════════
   PIC21 — Tasks Page
   Vista diferenciada por rol:
     ADMIN    → todas las tareas + editar/eliminar/cambiar estado
     PROFESOR → tareas que creó + crear nuevas
     AYUDANTE/ESTUDIANTE → tareas asignadas (solo lectura)
═══════════════════════════════════════════════════════ */

const TasksPage = (() => {
  let allTasks  = [];
  let meetingId = null;

  function render(container) {
    const user    = AuthService.getUser();
    const isAdmin = user?.roles?.includes('ADMIN');
    const isProf  = user?.roles?.includes('PROFESOR');
    const isStaff = isAdmin || isProf;

    container.innerHTML = `
      <div class="page-header">
        <div>
          <h2>📋 Tareas</h2>
          <p>${isAdmin ? 'Gestión completa de tareas' : isProf ? 'Tareas que creaste' : 'Mis tareas asignadas'}</p>
        </div>
        ${isStaff ? `
        <div style="display:flex;gap:.5rem;flex-wrap:wrap;align-items:center;">
          ${isAdmin || isProf ? `<button class="btn btn-primary" id="btnNewTask">➕ Crear tarea</button>` : ''}
          <button class="btn btn-secondary" id="btnRefreshTasks">🔄 Actualizar</button>
        </div>` : ''}
      </div>

      ${isStaff ? `
      <div id="createTaskPanel" class="card" style="margin-bottom:1rem;display:none;">
        <div class="card-header"><span class="card-title">➕ Nueva tarea para ausentes</span></div>
        <div class="card-body">
          <form id="createTaskForm">
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;">
              <div class="form-group">
                <label class="form-label">Reunión *</label>
                <select class="form-control" id="taskMeetingId" required>
                  <option value="">Cargando...</option>
                </select>
              </div>
              <div class="form-group">
                <label class="form-label">Título *</label>
                <input class="form-control" id="taskTitle" required maxlength="200" placeholder="Ej: Resumen de clase" />
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">Descripción *</label>
              <textarea class="form-control" id="taskDesc" rows="2" required placeholder="Describí la tarea..."></textarea>
            </div>
            <div class="form-group">
              <label class="form-label">Link (URL)</label>
              <input class="form-control" id="taskLink" type="url" placeholder="https://..." />
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-secondary" id="btnCancelTask">Cancelar</button>
              <button type="submit" class="btn btn-primary" id="saveNewTask">Crear y asignar</button>
            </div>
          </form>
        </div>
      </div>` : ''}

      <div id="tasksContainer">
        <div class="loading"><div class="spinner"></div></div>
      </div>`;

    // Bind buttons
    document.getElementById('btnNewTask')?.addEventListener('click', async () => {
      const panel = document.getElementById('createTaskPanel');
      panel.style.display = panel.style.display === 'none' ? '' : 'none';
      if (panel.style.display !== 'none') await setupMeetingsDropdown();
    });
    document.getElementById('btnCancelTask')?.addEventListener('click', () => {
      document.getElementById('createTaskPanel').style.display = 'none';
    });
    document.getElementById('btnRefreshTasks')?.addEventListener('click', () => loadTasks(isAdmin, user));
    document.getElementById('createTaskForm')?.addEventListener('submit', e => submitCreateTask(e, container, user));

    loadTasks(isAdmin, isProf, user);
  }

  async function setupMeetingsDropdown() {
    const sel = document.getElementById('taskMeetingId');
    if (!sel) return;
    try {
      const paged = await Api.get('/meetings?size=200&sort=scheduledAt,desc');
      const list  = Array.isArray(paged) ? paged : (paged?.content ?? []);
      sel.innerHTML = list.length
        ? '<option value="">— Seleccioná una reunión —</option>' +
          list.map(m => `<option value="${m.id}">[${m.status}] ${escHtml(m.title)}</option>`).join('')
        : '<option value="">Sin reuniones disponibles</option>';
    } catch { sel.innerHTML = '<option value="">Error al cargar</option>'; }
  }

  async function submitCreateTask(e, container, user) {
    e.preventDefault();
    const btn = document.getElementById('saveNewTask');
    btn.disabled = true; btn.textContent = 'Creando...';
    const mid = document.getElementById('taskMeetingId').value;
    if (!mid) { Toast.warning('Seleccioná una reunión', ''); btn.disabled=false; btn.textContent='Crear y asignar'; return; }
    try {
      const result = await Api.post(`/tasks/meeting/${mid}`, {
        title: document.getElementById('taskTitle').value.trim(),
        description: document.getElementById('taskDesc').value.trim(),
        link: document.getElementById('taskLink').value.trim() || null,
      });
      const count = Array.isArray(result) ? result.length : 1;
      Modal.close && Modal.close();
      document.getElementById('createTaskPanel').style.display = 'none';
      document.getElementById('createTaskForm').reset();
      Toast.success('Tarea creada', `Asignada a ${count} estudiante(s) ausente(s)`);
      loadTasks(user?.roles?.includes('ADMIN'), user?.roles?.includes('PROFESOR'), user);
    } catch (err) {
      Toast.error('Error', err.message);
      btn.disabled = false; btn.textContent = 'Crear y asignar';
    }
  }

  async function loadTasks(isAdmin, isProf, user) {
    const container = document.getElementById('tasksContainer');
    if (!container) return;
    showLoading(container);
    try {
      let tasks;
      if (isAdmin || isProf) {
        tasks = await Api.get('/tasks');
      } else {
        tasks = await Api.get('/tasks/my');
      }
      allTasks = Array.isArray(tasks) ? tasks : [];
      renderTasks(container, isAdmin, isProf);
    } catch (err) {
      Toast.error('Error al cargar tareas', err.message);
      showEmpty(container, 'Error al cargar', err.message);
    }
  }

  function renderTasks(container, isAdmin, isProf) {
    if (!allTasks.length) {
      showEmpty(container, 'Sin tareas', isAdmin ? 'No hay tareas en el sistema.' : 'No tenés tareas asignadas.');
      return;
    }

    container.innerHTML = `
      <div class="card">
        <div class="card-header">
          <span class="card-title">📋 Tareas</span>
          <span class="text-sm" style="color:var(--text-muted)">${allTasks.length} tarea(s)</span>
        </div>
        <div class="card-body" style="padding:0">
          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Título</th>
                  <th>Reunión</th>
                  ${isAdmin || isProf ? '<th>Asignado a</th>' : ''}
                  ${isAdmin || isProf ? '<th>Creado por</th>' : ''}
                  <th>Estado</th>
                  <th>Fecha</th>
                  ${isAdmin ? '<th style="text-align:center">Acciones</th>' : ''}
                </tr>
              </thead>
              <tbody>
                ${allTasks.map((t, i) => taskRow(t, i + 1, isAdmin, isProf)).join('')}
              </tbody>
            </table>
          </div>
        </div>
      </div>`;

    // Admin action buttons
    if (isAdmin) {
      container.querySelectorAll('[data-task-action]').forEach(btn => {
        btn.addEventListener('click', () => handleTaskAction(btn, isAdmin, isProf));
      });
    }
  }

  function taskRow(t, idx, isAdmin, isProf) {
    const link = t.link ? `<a href="${escHtml(t.link)}" target="_blank" style="font-size:.8rem">🔗 Ver</a>` : '—';
    return `
      <tr>
        <td>${idx}</td>
        <td>
          <strong>${escHtml(t.title)}</strong>
          ${t.description ? `<div style="font-size:.8rem;color:var(--text-muted)">${escHtml(t.description.substring(0,60))}${t.description.length>60?'…':''}</div>` : ''}
          ${link !== '—' ? `<div>${link}</div>` : ''}
        </td>
        <td style="font-size:.85rem">${escHtml(t.meetingTitle || '—')}</td>
        ${(isAdmin || isProf) ? `<td style="font-size:.85rem">${escHtml(t.assignedToFirstName||'')} ${escHtml(t.assignedToLastName||'')} <span style="color:var(--text-muted)">(${escHtml(t.assignedToUsername||'')})</span></td>` : ''}
        ${(isAdmin || isProf) ? `<td style="font-size:.85rem">${escHtml(t.createdByUsername||'—')}</td>` : ''}
        <td>${statusBadge(t.status)}</td>
        <td style="font-size:.8rem">${formatDateShort(t.createdAt)}</td>
        ${isAdmin ? `
        <td style="text-align:center;white-space:nowrap">
          <select class="form-control" style="font-size:.8rem;padding:.2rem .4rem;width:auto;display:inline-block;"
            data-task-action="status" data-id="${t.id}">
            <option value="PENDING"    ${t.status==='PENDING'?'selected':''}>Pendiente</option>
            <option value="COMPLETED"  ${t.status==='COMPLETED'?'selected':''}>Completada</option>
            <option value="CORRECTED"  ${t.status==='CORRECTED'?'selected':''}>Corregida</option>
          </select>
          <button class="btn btn-secondary btn-sm" data-task-action="edit" data-id="${t.id}" data-title="${escHtml(t.title)}" data-desc="${escHtml(t.description||'')}" data-link="${escHtml(t.link||'')}">✏️</button>
          <button class="btn btn-secondary btn-sm" data-task-action="delete" data-id="${t.id}" data-title="${escHtml(t.title)}" style="color:#ef4444">🗑</button>
        </td>` : ''}
      </tr>`;
  }

  async function handleTaskAction(btn, isAdmin, isProf) {
    const id = Number(btn.dataset.id);
    const action = btn.dataset.taskAction;

    if (action === 'status') {
      const newStatus = btn.value;
      try {
        await Api.patch(`/tasks/${id}/status`, { status: newStatus });
        Toast.success('Estado actualizado', '');
        loadTasks(isAdmin, isProf, AuthService.getUser());
      } catch(err) { Toast.error('Error', err.message); }
    }

    if (action === 'edit') {
      openEditTaskModal(id, btn.dataset.title, btn.dataset.desc, btn.dataset.link, isAdmin, isProf);
    }

    if (action === 'delete') {
      if (!confirm(`¿Eliminar tarea "${btn.dataset.title}"?`)) return;
      try {
        await Api.delete(`/tasks/${id}`);
        Toast.success('Tarea eliminada', btn.dataset.title);
        loadTasks(isAdmin, isProf, AuthService.getUser());
      } catch(err) { Toast.error('Error', err.message); }
    }
  }

  function openEditTaskModal(id, title, desc, link, isAdmin, isProf) {
    Modal.open(`Editar tarea`, `
      <form id="editTaskForm">
        <div class="form-group">
          <label class="form-label">Título *</label>
          <input class="form-control" id="etTitle" value="${escHtml(title)}" required maxlength="200" />
        </div>
        <div class="form-group">
          <label class="form-label">Descripción *</label>
          <textarea class="form-control" id="etDesc" rows="3" required>${escHtml(desc)}</textarea>
        </div>
        <div class="form-group">
          <label class="form-label">Link (URL)</label>
          <input class="form-control" id="etLink" type="url" value="${escHtml(link)}" />
        </div>
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
          <button type="submit" class="btn btn-primary" id="saveEditTask">Guardar</button>
        </div>
      </form>`);

    document.getElementById('editTaskForm').addEventListener('submit', async e => {
      e.preventDefault();
      const btn = document.getElementById('saveEditTask');
      btn.disabled = true; btn.textContent = 'Guardando...';
      try {
        await Api.put(`/tasks/${id}`, {
          title:       document.getElementById('etTitle').value.trim(),
          description: document.getElementById('etDesc').value.trim(),
          link:        document.getElementById('etLink').value.trim() || null,
        });
        Modal.close();
        Toast.success('Tarea actualizada', '');
        loadTasks(isAdmin, isProf, AuthService.getUser());
      } catch(err) {
        Toast.error('Error', err.message);
        btn.disabled = false; btn.textContent = 'Guardar';
      }
    });
  }

  return { render };
})();
