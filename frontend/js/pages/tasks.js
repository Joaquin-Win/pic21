/* ═══════════════════════════════════════════════════════
   PIC21 — Tasks Page (Multiple Choice Quiz)
   Vista diferenciada por rol:
     ADMIN    → Sección 1: Tareas Generales (editar/eliminar)
                Sección 2: expand → Asignaciones con cambio de estado
     PROFESOR → Tareas que creó + panel para crear nuevas
     AYUDANTE/ESTUDIANTE → Mis asignaciones con botón "Rendir Quiz"
═══════════════════════════════════════════════════════ */

const TasksPage = (() => {
  let generalTasks    = [];
  let myAssignments   = [];
  let expandedTaskId  = null;
  let createLinks     = [];

  // ─── RENDER PRINCIPAL ───────────────────────────────
  function render(container) {
    const user    = AuthService.getUser();
    const isAdmin = user?.roles?.includes('R04_ADMIN');
    const isDirector = user?.roles?.includes('R05_DIRECTOR');
    const canManageTasks = isAdmin || isDirector;

    container.innerHTML = `
      <div class="page-header">
        <div>
          <h2>📋 Recuperar asistencia</h2>
          <p>${isAdmin ? 'Gestión completa — tareas generales y asignaciones' : isDirector ? 'Tareas creadas por dirección' : 'Mis tareas asignadas'}</p>
        </div>
        ${canManageTasks ? `<button class="btn btn-primary" id="btnToggleCreate">➕ Nueva tarea</button>` : ''}
      </div>

      ${canManageTasks ? `
      <div id="createPanel" class="card" style="margin-bottom:1rem;display:none;">
        <div class="card-header"><span class="card-title">➕ Crear tarea con Quiz</span></div>
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
              <input class="form-control" id="ctTitle" required maxlength="200" placeholder="Ej: Quiz de la clase" />
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">Descripción (opcional)</label>
            <textarea class="form-control" id="ctDesc" rows="2" placeholder="Instrucciones para el quiz..."></textarea>
          </div>
          <div class="form-group">
            <label class="form-label">Links de apoyo (opcionales)</label>
            <div id="taskLinksCreateContainer"></div>
            <button type="button" class="btn btn-secondary btn-sm" id="btnAddTaskLink" style="margin-top:.35rem;font-size:.8rem;">+ Agregar link</button>
          </div>

          <!-- Quiz Builder -->
          <div style="margin-top:1rem;margin-bottom:.5rem;">
            <label class="form-label" style="font-size:1rem;font-weight:600;">📝 Preguntas del Quiz (mín. 5 — máx. 20)</label>
          </div>
          <div id="quizBuilder"></div>
          <button type="button" class="btn btn-secondary" id="btnAddQuestion" style="margin-top:.5rem;">➕ Agregar pregunta</button>

          <div class="form-actions" style="margin-top:1rem;">
            <button type="button" class="btn btn-secondary" id="btnCancelCreate">Cancelar</button>
            <button type="button" class="btn btn-primary" id="btnSaveCreate">✅ Crear y asignar</button>
          </div>
        </div>
      </div>` : ''}

      <div id="tasksMain">
        <div class="loading"><div class="spinner"></div></div>
      </div>`;

    bindCreatePanel(isAdmin, isDirector);
    loadView(isAdmin, isDirector);
  }

  // ─── QUIZ BUILDER ─────────────────────────────────────
  let quizQuestions = [];

  function renderTaskLinksBuilder() {
    const container = document.getElementById('taskLinksCreateContainer');
    if (!container) return;
    container.innerHTML = createLinks.map((link, i) => `
      <div style="display:flex;gap:.5rem;margin-bottom:.35rem;">
        <input class="form-control task-link-input" data-li="${i}" value="${escHtml(link)}" type="url" maxlength="500" placeholder="https://..." />
        <button type="button" class="btn btn-secondary btn-sm task-link-remove" data-li="${i}" style="color:#ef4444;">✕</button>
      </div>
    `).join('');

    container.querySelectorAll('.task-link-input').forEach(input => {
      input.addEventListener('input', () => { createLinks[Number(input.dataset.li)] = input.value; });
    });
    container.querySelectorAll('.task-link-remove').forEach(btn => {
      btn.addEventListener('click', () => {
        createLinks.splice(Number(btn.dataset.li), 1);
        renderTaskLinksBuilder();
      });
    });
  }

  function renderQuizBuilder() {
    const container = document.getElementById('quizBuilder');
    if (!container) return;
    container.innerHTML = quizQuestions.map((q, qi) => `
      <div class="card" style="margin-bottom:.75rem;border:1px solid var(--border-color);padding:1rem;">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:.5rem;">
          <strong style="font-size:.95rem;">Pregunta ${qi + 1}</strong>
          <button type="button" class="btn btn-secondary btn-sm quiz-remove-q" data-qi="${qi}" style="color:#ef4444;font-size:.75rem;">🗑 Eliminar</button>
        </div>
        <div class="form-group" style="margin-bottom:.5rem;">
          <input class="form-control quiz-q-text" data-qi="${qi}" value="${escHtml(q.question)}" placeholder="Enunciado de la pregunta" />
        </div>
        <div style="margin-bottom:.35rem;font-size:.85rem;color:var(--text-muted);">Opciones (mín. 3) — marcá la correcta:</div>
        ${q.options.map((opt, oi) => `
          <div style="display:flex;align-items:center;gap:.5rem;margin-bottom:.35rem;">
            <input type="radio" name="correct_${qi}" value="${oi}" ${q.correct === oi ? 'checked' : ''} class="quiz-correct" data-qi="${qi}" data-oi="${oi}" style="width:18px;height:18px;cursor:pointer;" />
            <input class="form-control quiz-opt-text" data-qi="${qi}" data-oi="${oi}" value="${escHtml(opt)}" placeholder="Opción ${oi + 1}" style="flex:1;" />
            ${q.options.length > 3 ? `<button type="button" class="btn btn-secondary btn-sm quiz-remove-opt" data-qi="${qi}" data-oi="${oi}" style="color:#ef4444;padding:.1rem .4rem;font-size:.7rem;">✕</button>` : ''}
          </div>
        `).join('')}
        <button type="button" class="btn btn-secondary btn-sm quiz-add-opt" data-qi="${qi}" style="font-size:.75rem;margin-top:.25rem;">+ Opción</button>
      </div>
    `).join('');

    // Bind events
    container.querySelectorAll('.quiz-q-text').forEach(el => {
      el.addEventListener('input', () => { quizQuestions[+el.dataset.qi].question = el.value; });
    });
    container.querySelectorAll('.quiz-opt-text').forEach(el => {
      el.addEventListener('input', () => { quizQuestions[+el.dataset.qi].options[+el.dataset.oi] = el.value; });
    });
    container.querySelectorAll('.quiz-correct').forEach(el => {
      el.addEventListener('change', () => { quizQuestions[+el.dataset.qi].correct = +el.dataset.oi; });
    });
    container.querySelectorAll('.quiz-remove-q').forEach(el => {
      el.addEventListener('click', () => { quizQuestions.splice(+el.dataset.qi, 1); renderQuizBuilder(); });
    });
    container.querySelectorAll('.quiz-remove-opt').forEach(el => {
      el.addEventListener('click', () => {
        const qi = +el.dataset.qi, oi = +el.dataset.oi;
        quizQuestions[qi].options.splice(oi, 1);
        if (quizQuestions[qi].correct >= quizQuestions[qi].options.length) quizQuestions[qi].correct = 0;
        renderQuizBuilder();
      });
    });
    container.querySelectorAll('.quiz-add-opt').forEach(el => {
      el.addEventListener('click', () => { quizQuestions[+el.dataset.qi].options.push(''); renderQuizBuilder(); });
    });
  }

  function addEmptyQuestion() {
    if (quizQuestions.length >= 20) { Toast.warning('Máximo 20 preguntas', ''); return; }
    quizQuestions.push({ question: '', options: ['', '', ''], correct: 0 });
    renderQuizBuilder();
  }

  function validateQuiz() {
    if (quizQuestions.length < 5) return 'Mínimo 5 preguntas (tenés ' + quizQuestions.length + ')';
    if (quizQuestions.length > 20) return 'Máximo 20 preguntas';
    for (let i = 0; i < quizQuestions.length; i++) {
      const q = quizQuestions[i];
      if (!q.question.trim()) return `Pregunta ${i+1}: falta el enunciado`;
      if (q.options.length < 3) return `Pregunta ${i+1}: mínimo 3 opciones`;
      for (let j = 0; j < q.options.length; j++) {
        if (!q.options[j].trim()) return `Pregunta ${i+1}, opción ${j+1}: está vacía`;
      }
      if (q.correct < 0 || q.correct >= q.options.length) return `Pregunta ${i+1}: seleccioná la respuesta correcta`;
    }
    return null;
  }

  // ─── PANEL CREAR ──────────────────────────────────────
  function bindCreatePanel(isAdmin, isProf) {
    const toggle  = document.getElementById('btnToggleCreate');
    const panel   = document.getElementById('createPanel');
    const cancel  = document.getElementById('btnCancelCreate');
    const saveBtn = document.getElementById('btnSaveCreate');
    const addQ    = document.getElementById('btnAddQuestion');
    const addLink = document.getElementById('btnAddTaskLink');

    toggle?.addEventListener('click', async () => {
      const visible = panel.style.display !== 'none';
      panel.style.display = visible ? 'none' : '';
      if (!visible) {
        await loadMeetingsDropdown();
        if (!quizQuestions.length) { for (let i=0;i<5;i++) addEmptyQuestion(); }
        if (!createLinks.length) createLinks.push('');
        renderTaskLinksBuilder();
        renderQuizBuilder();
      }
    });
    cancel?.addEventListener('click', () => { panel.style.display = 'none'; });
    saveBtn?.addEventListener('click', () => submitCreate(isAdmin, isProf));
    addQ?.addEventListener('click', addEmptyQuestion);
    addLink?.addEventListener('click', () => {
      createLinks.push('');
      renderTaskLinksBuilder();
    });
  }

  async function loadMeetingsDropdown() {
    const sel = document.getElementById('ctMeetingId');
    if (!sel) return;
    try {
      const paged = await Api.get('/meetings?size=200&sort=fechaInicio,desc');
      const list  = Array.isArray(paged) ? paged : (paged?.content ?? []);
      sel.innerHTML = list.length
        ? '<option value="">— Seleccioná una reunión —</option>' +
          list.map(m => `<option value="${m.id}">[${m.estado}] ${escHtml(m.titulo)}</option>`).join('')
        : '<option value="">Sin reuniones disponibles</option>';
    } catch { sel.innerHTML = '<option value="">Error al cargar</option>'; }
  }

  async function submitCreate(isAdmin, isProf) {
    const btn = document.getElementById('btnSaveCreate');
    const mid  = document.getElementById('ctMeetingId')?.value;
    const title= document.getElementById('ctTitle')?.value.trim();
    const desc = document.getElementById('ctDesc')?.value.trim();
    const links = createLinks.map(l => (l || '').trim()).filter(Boolean);

    if (!mid)   { Toast.warning('Seleccioná una reunión',''); return; }
    if (!title) { Toast.warning('El título es obligatorio',''); return; }

    const quizError = validateQuiz();
    if (quizError) { Toast.warning('Quiz inválido', quizError); return; }

    btn.disabled = true; btn.textContent = 'Creando...';
    try {
      const result = await Api.post(`/tasks/meeting/${mid}`, {
        title,
        description: desc || null,
        link: links[0] || null,
        links,
        questionsJson: JSON.stringify(quizQuestions),
      });
      const count  = Array.isArray(result) ? result.length : 1;
      document.getElementById('createPanel').style.display = 'none';
      document.getElementById('ctTitle').value = '';
      document.getElementById('ctDesc').value = '';
      createLinks = [];
      quizQuestions = [];
      Toast.success('Tarea creada', `Quiz asignado a ${count} usuario(s) ausente(s)`);
      loadView(isAdmin, isProf);
    } catch(err) {
      Toast.error('Error', err.message);
    } finally {
      btn.disabled = false; btn.textContent = '✅ Crear y asignar';
    }
  }

  // ─── CARGA PRINCIPAL ───────────────────────────────────
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

  // ─── VISTA ADMIN / PROFESOR ─────────────────────────────
  function renderGeneralTasks(main, isAdmin, isProf) {
    if (!generalTasks.length) {
      showEmpty(main, 'Sin tareas', 'Aún no se han creado tareas.');
      return;
    }

    main.innerHTML = `
      <div class="card">
        <div class="card-header">
          <span class="card-title">📋 Recuperar asistencia — general</span>
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
                  <th>Preguntas</th>
                  <th>Asignados</th>
                  <th>Pendientes</th>
                  <th>Estado</th>
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
    let qCount = '—';
    try { if (t.questionsJson) qCount = JSON.parse(t.questionsJson).length; } catch {}
    const links = Array.isArray(t.links) ? t.links : (t.link ? [t.link] : []);
    return `
      <tr id="task-row-${t.id}">
        <td>
          <button type="button" class="btn btn-secondary btn-sm task-expand-btn" data-id="${t.id}" title="Ver asignaciones">
            ${expandedTaskId === t.id ? '▲' : '▼'}
          </button>
        </td>
        <td>
          <strong>${escHtml(t.titulo)}</strong>
          ${t.descripcion ? `<div style="font-size:.8rem;color:var(--text-muted)">${escHtml(t.descripcion.substring(0,60))}${t.descripcion.length>60?'…':''}</div>` : ''}
          ${links.length ? `<div style="font-size:.78rem;margin-top:.25rem;color:var(--text-muted);">🔗 ${links.length} link(s)</div>` : ''}
        </td>
        <td style="font-size:.85rem">${escHtml(t.reunionTitulo||'—')}</td>
        <td style="text-align:center"><strong>${qCount}</strong></td>
        <td style="text-align:center"><strong>${t.totalAsignaciones ?? t.assignmentCount ?? 0}</strong></td>
        <td style="text-align:center">${pendBadge}</td>
        <td>${statusBadge(t.estado)}</td>
        <td style="font-size:.8rem">${formatDateShort(t.creadoEn || t.createdAt)}</td>
        ${isAdmin ? `
        <td style="text-align:center;white-space:nowrap">
          <button type="button" class="btn btn-secondary btn-sm task-block-btn" data-id="${t.id}" data-title="${escHtml(t.titulo)}" ${t.estado === 'BLOQUEADA' ? 'disabled' : ''}>
            ${t.estado === 'BLOQUEADA' ? '🚫 Bloqueada' : '⛔ Bloquear'}
          </button>
          <button type="button" class="btn btn-secondary btn-sm task-del-btn" data-id="${t.id}" data-title="${escHtml(t.titulo)}" style="color:#ef4444">🗑</button>
        </td>` : ''}
      </tr>`;
  }

  function bindGeneralTaskButtons(isAdmin, isProf) {
    const tbody = document.getElementById('generalTasksTbody');
    if (!tbody) return;

    tbody.querySelectorAll('.task-expand-btn').forEach(btn => {
      btn.addEventListener('click', async () => {
        const id = Number(btn.dataset.id);
        if (expandedTaskId === id) {
          expandedTaskId = null;
          document.getElementById('assignmentsPanel').innerHTML = '';
          btn.textContent = '▼';
        } else {
          if (expandedTaskId) {
            document.querySelector(`.task-expand-btn[data-id="${expandedTaskId}"]`)?.let(b => b.textContent = '▼');
          }
          expandedTaskId = id;
          btn.textContent = '▲';
          await loadAndRenderAssignments(id, isAdmin);
        }
      });
    });

    if (isAdmin) {
      tbody.querySelectorAll('.task-block-btn').forEach(btn => {
        btn.addEventListener('click', async () => {
          const id = Number(btn.dataset.id);
          if (!confirm(`¿Bloquear la tarea "${btn.dataset.title}"? Los pendientes perderán la oportunidad de recuperar.`)) return;
          try {
            await Api.patch(`/tasks/${id}/block`);
            Toast.success('Tarea bloqueada', btn.dataset.title);
            await loadView(isAdmin, isProf);
          } catch (err) {
            Toast.error('Error', err.message);
          }
        });
      });

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

  // ─── ASIGNACIONES PANEL ─────────────────────────────────
  async function loadAndRenderAssignments(taskId, isAdmin) {
    const panel = document.getElementById('assignmentsPanel');
    panel.innerHTML = '<div class="loading"><div class="spinner"></div></div>';
    try {
      const assignments = await Api.get(`/tasks/${taskId}/assignments`);
      const task = generalTasks.find(t => t.id === taskId);
      renderAssignmentsPanel(panel, taskId, task?.title || '', assignments, isAdmin);
    } catch(err) {
      panel.innerHTML = `<div class="card"><div class="card-body"><p style="color:#ef4444">Error: ${escHtml(err.message)}</p></div></div>`;
    }
  }

  function renderAssignmentsPanel(panel, taskId, taskTitle, assignments, isAdmin) {
    if (!assignments.length) {
      panel.innerHTML = `<div class="card"><div class="card-body"><p style="color:var(--text-muted)">Sin asignaciones.</p></div></div>`;
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
                  <th>Estado</th>
                  <th>Score</th>
                  <th>Intentos</th>
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
    const scoreTxt = a.score != null ? `${a.score}%` : '—';
    return `
      <tr>
        <td>${idx}</td>
        <td><strong>${escHtml(a.username||'—')}</strong></td>
        <td>${escHtml(a.nombre||'')} ${escHtml(a.apellido||'')}</td>
        <td>${statusBadge(a.estado || a.status)}</td>
        <td style="text-align:center">${scoreTxt}</td>
        <td style="text-align:center">${a.intentos ?? a.attempts ?? 0}</td>
        ${isAdmin ? `
        <td style="text-align:center;white-space:nowrap">
          <select class="form-control assignment-status-sel" data-aid="${a.id}"
            style="font-size:.8rem;padding:.2rem .5rem;width:auto;display:inline-block;margin-right:.5rem;">
            <option value="PENDIENTE"   ${(a.estado||a.status)==='PENDIENTE'?'selected':''}>Pendiente</option>
            <option value="COMPLETADA"  ${(a.estado||a.status)==='COMPLETADA'?'selected':''}>Completada</option>
            <option value="BLOQUEADA"   ${(a.estado||a.status)==='BLOQUEADA'?'selected':''}>Bloqueada</option>
          </select>
          <button type="button" class="btn btn-primary btn-sm assignment-status-btn" data-aid="${a.id}">
            💾 Guardar
          </button>
        </td>` : ''}
      </tr>`;
  }

  // ─── VISTA ESTUDIANTE — Mis asignaciones ─────────────────
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
                  <th>Score</th>
                  <th>Intentos</th>
                  <th>Acción</th>
                </tr>
              </thead>
              <tbody>
                ${myAssignments.map((a, i) => `
                  <tr>
                    <td>${i + 1}</td>
                    <td>
                      <strong>${escHtml(a.taskTitle || a.tituloTarea || '—')}</strong>
                      ${(a.descripcionTarea || a.taskDescription) ? `<div style="font-size:.8rem;color:var(--text-muted)">${escHtml(a.descripcionTarea || a.taskDescription)}</div>` : ''}
                      ${(() => {
                        const links = Array.isArray(a.linksTarea) ? a.linksTarea : ((a.linkTarea || a.link) ? [a.linkTarea || a.link] : []);
                        return links.length
                          ? `<div style="font-size:.8rem;margin-top:.25rem;">${links.map((l, idx) => `<a href="${escHtml(l)}" target="_blank" rel="noopener noreferrer">🔗 Link ${idx + 1}</a>`).join(' · ')}</div>`
                          : '';
                      })()}
                    </td>
                    <td>${statusBadge(a.estado || a.status)}</td>
                    <td style="text-align:center">${a.score != null ? a.score + '%' : '—'}</td>
                    <td style="text-align:center">${a.intentos ?? a.attempts ?? 0}</td>
                    <td>
                      ${(a.estado || a.status) === 'COMPLETADA'
                        ? '<span style="color:#10b981;font-weight:600;">✅ Aprobado</span>'
                        : (a.estado || a.status) === 'BLOQUEADA'
                          ? '<span style="color:#ef4444;font-weight:600;">⛔ Bloqueada</span>'
                        : a.questionsJson
                          ? `<button type="button" class="btn btn-primary btn-sm quiz-start-btn" data-aid="${a.id}">📝 Ver material y rendir</button>`
                          : '<span style="color:var(--text-muted);font-size:.85rem;">Sin quiz</span>'
                      }
                    </td>
                  </tr>`).join('')}
              </tbody>
            </table>
          </div>
        </div>
      </div>`;

    // Bind quiz buttons
    main.querySelectorAll('.quiz-start-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const assignment = myAssignments.find(a => a.id === Number(btn.dataset.aid));
        if (assignment) openQuizModal(assignment);
      });
    });
  }

  // ─── QUIZ MODAL (Estudiante) ──────────────────────────
  function openQuizModal(assignment) {
    let questions;
    try { questions = JSON.parse(assignment.questionsJson); } catch {
      Toast.error('Error', 'No se pudieron cargar las preguntas'); return;
    }

    const materialLinks = Array.isArray(assignment.linksTarea) && assignment.linksTarea.length
      ? assignment.linksTarea
      : [assignment.linkTarea || assignment.link].filter(Boolean);
    const materialHtml = materialLinks.length
      ? materialLinks.map((l, i) => `<li style="margin-bottom:.35rem;"><a href="${escHtml(l)}" target="_blank" rel="noopener noreferrer">🔗 Link ${i + 1}</a></li>`).join('')
      : '<li style="color:var(--text-muted)">No hay links cargados para esta tarea.</li>';

    const formHtml = questions.map((q, qi) => `
      <div style="margin-bottom:1.25rem;padding:1rem;background:var(--bg-secondary);border-radius:8px;">
        <p style="font-weight:600;margin-bottom:.5rem;">${qi + 1}. ${escHtml(q.question)}</p>
        ${q.options.map((opt, oi) => `
          <label style="display:flex;align-items:center;gap:.5rem;cursor:pointer;padding:.3rem 0;font-size:.95rem;">
            <input type="radio" name="quiz_${qi}" value="${oi}" style="width:16px;height:16px;" />
            ${escHtml(opt)}
          </label>
        `).join('')}
      </div>
    `).join('');

    Modal.open(`📝 Quiz — ${escHtml(assignment.taskTitle || assignment.tituloTarea || 'Tarea')}`, `
      <div style="max-height:60vh;overflow-y:auto;padding-right:.5rem;">
        <div id="quizStepMaterial">
          <p style="color:var(--text-muted);margin-bottom:1rem;font-size:.9rem;">
            Antes de responder el quiz, revisá el material de apoyo.
          </p>
          <ul style="padding-left:1.2rem;margin-bottom:1rem;">
            ${materialHtml}
          </ul>
          <label style="display:flex;align-items:flex-start;gap:.5rem;cursor:pointer;margin-bottom:1rem;">
            <input type="checkbox" id="quizConfirmVideos" style="margin-top:3px;width:16px;height:16px;" />
            <span style="font-size:.9rem;">Confirmo que revisé los videos/material de apoyo.</span>
          </label>
          <div class="form-actions">
            <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
            <button type="button" class="btn btn-primary" id="goToQuizBtn">Continuar al quiz</button>
          </div>
        </div>

        <form id="quizForm" style="display:none;">
          <p style="color:var(--text-muted);margin-bottom:1rem;font-size:.9rem;">
            Respondé todas las preguntas. Necesitás <strong>70% o más</strong> para aprobar.
            ${(assignment.intentos ?? assignment.attempts ?? 0) > 0 ? `<br>Intentos anteriores: <strong>${assignment.intentos ?? assignment.attempts ?? 0}</strong> | Último score: <strong>${assignment.score != null ? assignment.score + '%' : '—'}</strong>` : ''}
          </p>
          ${formHtml}
          <div class="form-actions">
            <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
            <button type="submit" class="btn btn-primary" id="submitQuizBtn">📩 Enviar respuestas</button>
          </div>
        </form>
      </div>
    `, { persistent: true });

    document.getElementById('goToQuizBtn')?.addEventListener('click', () => {
      const confirmed = document.getElementById('quizConfirmVideos')?.checked;
      if (!confirmed) {
        Toast.warning('Confirmación requerida', 'Marcá que revisaste los videos/material antes de continuar.');
        return;
      }
      document.getElementById('quizStepMaterial').style.display = 'none';
      document.getElementById('quizForm').style.display = '';
    });

    document.getElementById('quizForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const btn = document.getElementById('submitQuizBtn');

      // Collect answers
      const answers = [];
      for (let qi = 0; qi < questions.length; qi++) {
        const sel = document.querySelector(`input[name="quiz_${qi}"]:checked`);
        if (!sel) { Toast.warning('Completá todo', `Falta responder la pregunta ${qi + 1}`); return; }
        answers.push(Number(sel.value));
      }

      btn.disabled = true; btn.textContent = 'Evaluando...';
      try {
        const result = await Api.post(`/task-assignments/${assignment.id}/submit`, { answers });
        Modal.close();

        if (result.estado === 'COMPLETADA' || result.status === 'COMPLETADA') {
          Toast.success('🎉 ¡Quiz aprobado!', `Obtuviste ${result.score}% — Asistencia recuperada`);
        } else {
          Toast.warning('No aprobado', `Obtuviste ${result.score}%. Necesitás 70% o más. Podés reintentar.`);
        }

        // Refresh
        myAssignments = await Api.get('/tasks/my');
        renderMyAssignments(document.getElementById('tasksMain'));
      } catch(err) {
        Toast.error('Error', err.message);
        btn.disabled = false; btn.textContent = '📩 Enviar respuestas';
      }
    });
  }

  return { render };
})();
