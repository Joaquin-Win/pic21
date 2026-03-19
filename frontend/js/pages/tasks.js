/* ═══════════════════════════════════════════════════════
   PIC21 — Tasks Page
   Vista diferenciada por rol:
     ADMIN    → Sección 1: Tareas Generales (editar/eliminar)
                Sección 2: expand → Asignaciones con cambio de estado
     PROFESOR → Tareas que creó + panel para crear nuevas
     AYUDANTE/ESTUDIANTE → Mis asignaciones (solo lectura con estado)
═══════════════════════════════════════════════════════ */

const TasksPage = (() => {
  let generalTasks    = [];    // Para ADMIN / PROFESOR
  let myAssignments   = [];    // Para AYUDANTE / ESTUDIANTE
  let expandedTaskId  = null;  // ID de la tarea expandida

  //─────────────────────────────────────────────────────
  // RENDER PRINCIPAL
  //─────────────────────────────────────────────────────
  function render(container) {
    const user    = AuthService.getUser();
    const isAdmin = user?.roles?.includes('ADMIN');
    const isProf  = user?.roles?.includes('PROFESOR');
    const isStaff = isAdmin || isProf;

    container.innerHTML = `
      <div class="page-header">
        <div>
          <h2>📋 Tareas</h2>
          <p>${isAdmin ? 'Gestión completa — tareas generales y asignaciones' : isProf ? 'Tareas que creaste' : 'Mis tareas asignadas'}</p>
        </div>
        ${isStaff ? `<button class="btn btn-primary" id="btnToggleCreate">➕ Nueva tarea</button>` : ''}
      </div>

      ${isStaff ? `
      <div id="createPanel" class="card" style="margin-bottom:1rem;display:none;">
        <div class="card-header"><span class="card-title">➕ Asignar tarea a ausentes</span></div>
        <div class="card-body">
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;">
            <div class="form-group">
              <label class="form-label">Reunión *</label>
              <select class="form-control" id="ctMeetingId" required>
                <option value="">Cargando reuniones...</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Título *</label>
              <input class="form-control" id="ctTitle" required maxlength="200" placeholder="Ej: Resumen de la clase" />
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">Descripción *</label>
            <textarea class="form-control" id="ctDesc" rows="2" required placeholder="Describí qué deben hacer..."></textarea>
          </div>
          <div class="form-group">
            <label class="form-label">Link (URL, opcional)</label>
            <input class="form-control" id="ctLink" type="url" placeholder="https://..." />
          </div>
          <div class="form-actions">
            <button type="button" class="btn btn-secondary" id="btnCancelCreate">Cancelar</button>
            <button type="button" class="btn btn-primary" id="btnSaveCreate">✅ Crear y asignar</button>
          </div>
        </div>
      </div>` : ''}

      <div id="tasksMain">
        <div class="loading"><div class="spinner"></div></div>
      </div>`;

    bindCreatePanel(isAdmin, isProf);
    loadView(isAdmin, isProf);
  }

  //─────────────────────────────────────────────────────
  // PANEL CREAR
  //─────────────────────────────────────────────────────
  function bindCreatePanel(isAdmin, isProf) {
    const toggle  = document.getElementById('btnToggleCreate');
    const panel   = document.getElementById('createPanel');
    const cancel  = document.getElementById('btnCancelCreate');
    const saveBtn = document.getElementById('btnSaveCreate');

    toggle?.addEventListener('click', async () => {
      const visible = panel.style.display !== 'none';
      panel.style.display = visible ? 'none' : '';
      if (!visible) await loadMeetingsDropdown();
    });
    cancel?.addEventListener('click', () => { panel.style.display = 'none'; });
    saveBtn?.addEventListener('click', () => submitCreate(isAdmin, isProf));
  }

  async function loadMeetingsDropdown() {
    const sel = document.getElementById('ctMeetingId');
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

  async function submitCreate(isAdmin, isProf) {
    const btn = document.getElementById('btnSaveCreate');
    const mid  = document.getElementById('ctMeetingId')?.value;
    const title= document.getElementById('ctTitle')?.value.trim();
    const desc = document.getElementById('ctDesc')?.value.trim();
    const link = document.getElementById('ctLink')?.value.trim();

    if (!mid)   { Toast.warning('Seleccioná una reunión',''); return; }
    if (!title) { Toast.warning('El título es obligatorio',''); return; }
    if (!desc)  { Toast.warning('La descripción es obligatoria',''); return; }

    btn.disabled = true; btn.textContent = 'Creando...';
    try {
      const result = await Api.post(`/tasks/meeting/${mid}`, { title, description: desc, link: link || null });
      const count  = Array.isArray(result) ? result.length : 1;
      document.getElementById('createPanel').style.display = 'none';
      document.getElementById('ctTitle').value = '';
      document.getElementById('ctDesc').value = '';
      document.getElementById('ctLink').value = '';
      Toast.success('Tarea creada', `Asignada a ${count} usuario(s) ausente(s)`);
      loadView(isAdmin, isProf);
    } catch(err) {
      Toast.error('Error', err.message);
    } finally {
      btn.disabled = false; btn.textContent = '✅ Crear y asignar';
    }
  }

  //─────────────────────────────────────────────────────
  // CARGA PRINCIPAL SEGÚN ROL
  //─────────────────────────────────────────────────────
  async function loadView(isAdmin, isProf) {
    const main = document.getElementById('tasksMain');
    if (!main) return;
    showLoading(main);
    try {
      if (isAdmin || isProf) {
        generalTasks = await Api.get('/tasks');
        renderGeneralTasks(main, isAdmin, isProf);
      } else {
        myAssignments = await Api.get('/tasks/my');
        renderMyAssignments(main);
      }
    } catch(err) {
      Toast.error('Error al cargar tareas', err.message);
      showEmpty(main, 'Error', err.message);
    }
  }

  //─────────────────────────────────────────────────────
  // VISTA ADMIN / PROFESOR — Tareas Generales
  //─────────────────────────────────────────────────────
  function renderGeneralTasks(main, isAdmin, isProf) {
    if (!generalTasks.length) {
      showEmpty(main, 'Sin tareas', 'Aún no se han creado tareas.');
      return;
    }

    main.innerHTML = `
      <div class="card">
        <div class="card-header">
          <span class="card-title">📋 Tareas generales</span>
          <span class="text-sm" style="color:var(--text-muted)">${generalTasks.length} tarea(s)</span>
        </div>
        <div class="card-body" style="padding:0">
          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th></th>
                  <th>Título</th>
                  <th>Reunión</th>
                  <th>Creado por</th>
                  <th>Asignados</th>
                  <th>Pendientes</th>
                  <th>Fecha</th>
                  ${isAdmin ? '<th style="text-align:center">Acciones</th>' : ''}
                </tr>
              </thead>
              <tbody id="generalTasksTbody">
                ${generalTasks.map(t => taskRow(t, isAdmin)).join('')}
              </tbody>
            </table>
          </div>
        </div>
      </div>
      <div id="assignmentsPanel" style="margin-top:1rem;"></div>`;

    bindGeneralTaskButtons(isAdmin, isProf);
  }

  function taskRow(t, isAdmin) {
    const pendBadge = t.pendingCount > 0
      ? `<span class="badge badge-pending">${t.pendingCount}</span>`
      : `<span class="badge badge-done">0</span>`;
    return `
      <tr id="task-row-${t.id}">
        <td>
          <button type="button" class="btn btn-secondary btn-sm task-expand-btn" data-id="${t.id}" title="Ver asignaciones">
            ${expandedTaskId === t.id ? '▲' : '▼'}
          </button>
        </td>
        <td>
          <strong>${escHtml(t.title)}</strong>
          ${t.description ? `<div style="font-size:.8rem;color:var(--text-muted)">${escHtml(t.description.substring(0,60))}${t.description.length>60?'…':''}</div>` : ''}
          ${t.link ? `<a href="${escHtml(t.link)}" target="_blank" style="font-size:.8rem">🔗 Ver</a>` : ''}
        </td>
        <td style="font-size:.85rem">${escHtml(t.meetingTitle||'—')}</td>
        <td style="font-size:.85rem">${escHtml(t.createdByUsername||'—')}</td>
        <td style="text-align:center"><strong>${t.assignmentCount}</strong></td>
        <td style="text-align:center">${pendBadge}</td>
        <td style="font-size:.8rem">${formatDateShort(t.createdAt)}</td>
        ${isAdmin ? `
        <td style="text-align:center;white-space:nowrap">
          <button type="button" class="btn btn-secondary btn-sm task-edit-btn" data-id="${t.id}" data-title="${escHtml(t.title)}" data-desc="${escHtml(t.description||'')}" data-link="${escHtml(t.link||'')}">✏️ Editar</button>
          <button type="button" class="btn btn-secondary btn-sm task-del-btn" data-id="${t.id}" data-title="${escHtml(t.title)}" style="color:#ef4444">🗑</button>
        </td>` : ''}
      </tr>`;
  }

  function bindGeneralTaskButtons(isAdmin, isProf) {
    const tbody = document.getElementById('generalTasksTbody');
    if (!tbody) return;

    // Expand / collapse asignaciones
    tbody.querySelectorAll('.task-expand-btn').forEach(btn => {
      btn.addEventListener('click', async () => {
        const id = Number(btn.dataset.id);
        if (expandedTaskId === id) {
          expandedTaskId = null;
          document.getElementById('assignmentsPanel').innerHTML = '';
          btn.textContent = '▼';
        } else {
          // Collapse previous
          if (expandedTaskId) {
            document.querySelector(`.task-expand-btn[data-id="${expandedTaskId}"]`)?.let(b => b.textContent = '▼');
          }
          expandedTaskId = id;
          btn.textContent = '▲';
          await loadAndRenderAssignments(id, isAdmin);
        }
      });
    });

    // Edit
    if (isAdmin) {
      tbody.querySelectorAll('.task-edit-btn').forEach(btn => {
        btn.addEventListener('click', () => {
          openEditTaskModal(Number(btn.dataset.id), btn.dataset.title, btn.dataset.desc, btn.dataset.link, isAdmin, isProf);
        });
      });

      // Delete
      tbody.querySelectorAll('.task-del-btn').forEach(btn => {
        btn.addEventListener('click', async () => {
          const id = Number(btn.dataset.id);
          if (!confirm(`¿Eliminar la tarea "${btn.dataset.title}" y TODAS sus asignaciones?`)) return;
          try {
            await Api.delete(`/tasks/${id}`);
            Toast.success('Tarea eliminada', btn.dataset.title);
            if (expandedTaskId === id) {
              expandedTaskId = null;
              document.getElementById('assignmentsPanel').innerHTML = '';
            }
            loadView(isAdmin, isProf);
          } catch(err) { Toast.error('Error', err.message); }
        });
      });
    }
  }

  //─────────────────────────────────────────────────────
  // PANEL DE ASIGNACIONES (expandido al hacer click)
  //─────────────────────────────────────────────────────
  async function loadAndRenderAssignments(taskId, isAdmin) {
    const panel = document.getElementById('assignmentsPanel');
    panel.innerHTML = '<div class="loading"><div class="spinner"></div></div>';
    try {
      const assignments = await Api.get(`/tasks/${taskId}/assignments`);
      const task = generalTasks.find(t => t.id === taskId);
      renderAssignmentsPanel(panel, taskId, task?.title || '', assignments, isAdmin);
    } catch(err) {
      panel.innerHTML = `<div class="card"><div class="card-body"><p style="color:#ef4444">Error al cargar asignaciones: ${escHtml(err.message)}</p></div></div>`;
    }
  }

  function renderAssignmentsPanel(panel, taskId, taskTitle, assignments, isAdmin) {
    if (!assignments.length) {
      panel.innerHTML = `<div class="card"><div class="card-body"><p style="color:var(--text-muted)">Sin asignaciones para esta tarea.</p></div></div>`;
      return;
    }

    panel.innerHTML = `
      <div class="card">
        <div class="card-header">
          <span class="card-title">👥 Asignaciones — ${escHtml(taskTitle)}</span>
          <span class="text-sm" style="color:var(--text-muted)">${assignments.length} usuario(s)</span>
        </div>
        <div class="card-body" style="padding:0">
          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Usuario</th>
                  <th>Nombre</th>
                  <th>Estado actual</th>
                  ${isAdmin ? '<th style="text-align:center">Cambiar estado</th>' : ''}
                </tr>
              </thead>
              <tbody>
                ${assignments.map((a, i) => assignmentRow(a, i + 1, isAdmin)).join('')}
              </tbody>
            </table>
          </div>
        </div>
      </div>`;

    // FIX BUG: usar type="button" en botones y event listeners correctos (no dentro de form)
    if (isAdmin) {
      panel.querySelectorAll('.assignment-status-btn').forEach(btn => {
        btn.addEventListener('click', async () => {
          const assignmentId = Number(btn.dataset.aid);
          const sel = panel.querySelector(`.assignment-status-sel[data-aid="${assignmentId}"]`);
          const newStatus = sel?.value;
          if (!newStatus) return;
          btn.disabled = true;
          try {
            await Api.patch(`/task-assignments/${assignmentId}/status`, { status: newStatus });
            Toast.success('Estado actualizado', '');
            await loadAndRenderAssignments(taskId, isAdmin);
          } catch(err) {
            Toast.error('Error', err.message);
            btn.disabled = false;
          }
        });
      });
    }
  }

  function assignmentRow(a, idx, isAdmin) {
    return `
      <tr>
        <td>${idx}</td>
        <td><strong>${escHtml(a.username||'—')}</strong></td>
        <td>${escHtml(a.firstName||'')} ${escHtml(a.lastName||'')}</td>
        <td>${statusBadge(a.status)}</td>
        ${isAdmin ? `
        <td style="text-align:center;white-space:nowrap">
          <select class="form-control assignment-status-sel" data-aid="${a.id}"
            style="font-size:.8rem;padding:.2rem .5rem;width:auto;display:inline-block;margin-right:.5rem;">
            <option value="PENDING"   ${a.status==='PENDING'?'selected':''}>Pendiente</option>
            <option value="COMPLETED" ${a.status==='COMPLETED'?'selected':''}>Completada</option>
            <option value="CORRECTED" ${a.status==='CORRECTED'?'selected':''}>Corregida</option>
          </select>
          <button type="button" class="btn btn-primary btn-sm assignment-status-btn" data-aid="${a.id}">
            💾 Guardar
          </button>
        </td>` : ''}
      </tr>`;
  }

  //─────────────────────────────────────────────────────
  // VISTA AYUDANTE / ESTUDIANTE — Mis asignaciones
  //─────────────────────────────────────────────────────
  function renderMyAssignments(main) {
    if (!myAssignments.length) {
      showEmpty(main, 'Sin tareas asignadas', 'No tenés tareas pendientes. ¡Bien hecho! 🎉');
      return;
    }

    main.innerHTML = `
      <div class="card">
        <div class="card-header">
          <span class="card-title">📋 Mis tareas asignadas</span>
          <span class="text-sm" style="color:var(--text-muted)">${myAssignments.length} tarea(s)</span>
        </div>
        <div class="card-body" style="padding:0">
          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Tarea</th>
                  <th>Estado</th>
                  <th>Asignada</th>
                </tr>
              </thead>
              <tbody>
                ${myAssignments.map((a, i) => `
                  <tr>
                    <td>${i + 1}</td>
                    <td>
                      <strong>${escHtml(a.taskTitle||'—')}</strong>
                    </td>
                    <td>${statusBadge(a.status)}</td>
                    <td style="font-size:.8rem">${formatDateShort(a.createdAt)}</td>
                  </tr>`).join('')}
              </tbody>
            </table>
          </div>
        </div>
      </div>`;
  }

  //─────────────────────────────────────────────────────
  // MODAL EDITAR TAREA GENERAL
  //─────────────────────────────────────────────────────
  function openEditTaskModal(id, title, desc, link, isAdmin, isProf) {
    Modal.open(`Editar tarea — afecta a todos los asignados`, `
      <p class="text-sm" style="color:var(--text-muted);margin-bottom:1rem;">
        ⚠️ Editar esta tarea afectará a <strong>todos</strong> los usuarios asignados.
      </p>
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
        <button type="button" class="btn btn-primary" id="saveEditTask">Guardar cambios</button>
      </div>`);

    // FIX BUG: type="button" — no submit, no recarga
    document.getElementById('saveEditTask').addEventListener('click', async () => {
      const btn  = document.getElementById('saveEditTask');
      btn.disabled = true; btn.textContent = 'Guardando...';
      try {
        await Api.put(`/tasks/${id}`, {
          title:       document.getElementById('etTitle').value.trim(),
          description: document.getElementById('etDesc').value.trim(),
          link:        document.getElementById('etLink').value.trim() || null,
        });
        Modal.close();
        Toast.success('Tarea actualizada', 'Impacta a todos los asignados');
        loadView(isAdmin, isProf);
      } catch(err) {
        Toast.error('Error', err.message);
        btn.disabled = false; btn.textContent = 'Guardar cambios';
      }
    });
  }

  return { render };
})();
