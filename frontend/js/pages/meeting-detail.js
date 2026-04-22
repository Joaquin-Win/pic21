/* ═══════════════════════════════════════════════════════
   PIC21 — Meeting Detail Page (asistencias, exportar)
═══════════════════════════════════════════════════════ */

const MeetingDetailPage = (() => {
  let currentMeeting = null;

  function render(container, params = {}) {
    const meetingId = params.id;
    container.innerHTML = `<div class="loading"><div class="spinner"></div></div>`;
    loadDetail(container, meetingId);
  }

  async function loadDetail(container, meetingId) {
    try {
      const isAdminOrDirector = AuthService.isAdmin() || AuthService.isDirector();
      const [meeting, attendances, myAssignments] = await Promise.all([
        Api.get(`/meetings/${meetingId}`),
        isAdminOrDirector ? Api.get(`/attendances/meeting/${meetingId}`) : Promise.resolve([]),
        !isAdminOrDirector ? Api.get('/tasks/my') : Promise.resolve([]),
      ]);
      currentMeeting = meeting;
      // Filter assignments for this meeting
      const meetingAssignment = !isAdminOrDirector
        ? (myAssignments || []).find(a => String(a.meetingId) === String(meetingId))
        : null;
      renderDetail(container, meeting, attendances, meetingAssignment);
    } catch (err) {
      Toast.error('Error', err.message);
      container.innerHTML = `<div class="empty-state"><h3>Error al cargar reunión</h3><p>${escHtml(err.message)}</p></div>`;
    }
  }

  function renderDetail(container, meeting, attendances, meetingAssignment) {
    const isEnCurso  = meeting.estado === 'EN_CURSO';
    const isAdminOrDirector = AuthService.isAdmin() || AuthService.isDirector();
    const canCreateTask = AuthService.isAdmin() || AuthService.isDirector();
    const canAttend = isEnCurso
      && !AuthService.isAdmin()
      && !AuthService.isDirector()
      && !AuthService.isProfesor()
      && (AuthService.isEstudiante() || AuthService.isAyudante() || AuthService.isEgresado());
    const arr = Array.isArray(attendances) ? attendances : [];

    container.innerHTML = `
      <!-- Back + Header -->
      <div class="page-header">
        <div style="display:flex;align-items:center;gap:1rem;">
          <button class="btn btn-secondary btn-sm" data-href="/meetings">← Volver</button>
          <div>
            <h2>${escHtml(meeting.titulo)}</h2>
            <p>${meeting.fechaInicio ? formatDate(meeting.fechaInicio) : ''} &nbsp;${statusBadge(meeting.estado)}</p>
          </div>
        </div>
        <div style="display:flex;gap:.5rem;flex-wrap:wrap;align-items:center;">
          ${canAttend ? `<button class="btn btn-success" id="btnAttend">✅ Registrar mi asistencia</button>` : ''}
          ${isAdminOrDirector ? `<button class="btn btn-secondary" id="btnExcel">📥 Exportar Excel</button>` : ''}
          ${canCreateTask ? `<button class="btn btn-primary btn-sm" id="btnCreateTask">➕ Crear tarea</button>` : ''}
        </div>
      </div>

      <!-- Info card -->
      <div class="card" style="margin-bottom:1.5rem;">
        <div class="card-body">
          <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:1rem;">
            <div><div class="text-xs" style="color:var(--text-muted);margin-bottom:.25rem;">LINK DE REUNIÓN</div>${meeting.accessCode ? `<a href="${escHtml(meeting.accessCode)}" target="_blank" rel="noopener" style="color:var(--primary);text-decoration:underline;">${escHtml(meeting.accessCode)}</a>` : '<span>—</span>'}</div>
            <div><div class="text-xs" style="color:var(--text-muted);margin-bottom:.25rem;">CREADO POR</div><strong>${escHtml(meeting.creadoPorUsername || '—')}</strong></div>
            <div><div class="text-xs" style="color:var(--text-muted);margin-bottom:.25rem;">DESCRIPCIÓN</div>${escHtml(meeting.descripcion || 'Sin descripción')}</div>
            ${isAdminOrDirector ? `<div><div class="text-xs" style="color:var(--text-muted);margin-bottom:.25rem;">ASISTENTES</div><strong>${arr.length}</strong></div>` : ''}
          </div>
        </div>
      </div>

      <!-- Links de la reunión -->
      <div class="card" style="margin-bottom:1.5rem;">
        <div class="card-header">
          <span class="card-title">🔗 Links de la reunión</span>
        </div>
        <div class="card-body">
          <div style="display:grid;gap:1rem;">
            ${meeting.recordingLink ? `
            <div style="display:flex;align-items:flex-start;gap:.75rem;padding:.75rem;background:var(--bg-secondary);border-radius:8px;">
              <span style="font-size:1.3rem;flex-shrink:0;">🎥</span>
              <div style="min-width:0;flex:1;">
                <div style="font-weight:600;margin-bottom:.15rem;">Grabación</div>
                <a href="${escHtml(meeting.recordingLink)}" target="_blank" rel="noopener" style="color:var(--primary);text-decoration:underline;word-break:break-all;overflow-wrap:anywhere;font-size:.9rem;display:block;">${escHtml(meeting.recordingLink)}</a>
              </div>
            </div>` : ''}
            ${meeting.presentacionLink ? `
            <div style="display:flex;align-items:flex-start;gap:.75rem;padding:.75rem;background:var(--bg-secondary);border-radius:8px;">
              <span style="font-size:1.3rem;flex-shrink:0;">📊</span>
              <div style="min-width:0;flex:1;">
                <div style="font-weight:600;margin-bottom:.15rem;">Presentación</div>
                <a href="${escHtml(meeting.presentacionLink)}" target="_blank" rel="noopener" style="color:var(--primary);text-decoration:underline;word-break:break-all;overflow-wrap:anywhere;font-size:.9rem;display:block;">${escHtml(meeting.presentacionLink)}</a>
              </div>
            </div>` : ''}
            ${meeting.newsLink ? `
            <div style="display:flex;align-items:flex-start;gap:.75rem;padding:.75rem;background:var(--bg-secondary);border-radius:8px;">
              <span style="font-size:1.3rem;flex-shrink:0;">📰</span>
              <div style="min-width:0;flex:1;">
                <div style="font-weight:600;margin-bottom:.15rem;">Noticias</div>
                <a href="${escHtml(meeting.newsLink)}" target="_blank" rel="noopener" style="color:var(--primary);text-decoration:underline;word-break:break-all;overflow-wrap:anywhere;font-size:.9rem;display:block;">${escHtml(meeting.newsLink)}</a>
              </div>
            </div>` : ''}
            ${(Array.isArray(meeting.newsLinksExtra) && meeting.newsLinksExtra.length) ? meeting.newsLinksExtra.map((l, i) => `
            <div style="display:flex;align-items:flex-start;gap:.75rem;padding:.75rem;background:var(--bg-secondary);border-radius:8px;">
              <span style="font-size:1.3rem;flex-shrink:0;">📰</span>
              <div style="min-width:0;flex:1;">
                <div style="font-weight:600;margin-bottom:.15rem;">Noticia ${i + 1}</div>
                <a href="${escHtml(l)}" target="_blank" rel="noopener" style="color:var(--primary);text-decoration:underline;word-break:break-all;overflow-wrap:anywhere;font-size:.9rem;display:block;">${escHtml(l)}</a>
              </div>
            </div>`).join('') : ''}
            ${meeting.activityLink ? `
            <div style="display:flex;align-items:flex-start;gap:.75rem;padding:.75rem;background:var(--bg-secondary);border-radius:8px;">
              <span style="font-size:1.3rem;flex-shrink:0;">📝</span>
              <div style="min-width:0;flex:1;">
                <div style="font-weight:600;margin-bottom:.15rem;">Actividad</div>
                <a href="${escHtml(meeting.activityLink)}" target="_blank" rel="noopener" style="color:var(--primary);text-decoration:underline;word-break:break-all;overflow-wrap:anywhere;font-size:.9rem;display:block;">${escHtml(meeting.activityLink)}</a>
              </div>
            </div>` : ''}
            ${(Array.isArray(meeting.linksExtra) && meeting.linksExtra.length) ? meeting.linksExtra.map((l, i) => `
            <div style="display:flex;align-items:flex-start;gap:.75rem;padding:.75rem;background:var(--bg-secondary);border-radius:8px;">
              <span style="font-size:1.3rem;flex-shrink:0;">🔗</span>
              <div style="min-width:0;flex:1;">
                <div style="font-weight:600;margin-bottom:.15rem;">Link extra ${i + 1}</div>
                <a href="${escHtml(l)}" target="_blank" rel="noopener" style="color:var(--primary);text-decoration:underline;word-break:break-all;overflow-wrap:anywhere;font-size:.9rem;display:block;">${escHtml(l)}</a>
              </div>
            </div>`).join('') : ''}
            ${!meeting.recordingLink && !meeting.presentacionLink && !meeting.newsLink && !meeting.activityLink && !(meeting.linksExtra?.length) && !(meeting.newsLinksExtra?.length) ? '<p style="color:var(--text-muted);font-size:.9rem;">Sin links disponibles aún.</p>' : ''}
          </div>
        </div>
      </div>


      <!-- Recuperar asistencia (student/egresado) -->
      <div id="recoverySection"></div>

      <!-- Attendances table (admin/profesor only) -->
      ${isAdminOrDirector ? `
      <div class="card">
        <div class="card-header">
          <span class="card-title">👥 Lista de asistencias</span>
          <span class="text-sm text-muted">${arr.length} asistente(s)</span>
        </div>
        <div id="attendancesTable" class="card-body">
          ${renderAttendancesTable(arr)}
        </div>
      </div>` : ''}
    `;

    // Bind attend button — open form modal
    document.getElementById('btnAttend')?.addEventListener('click', () => {
      openAttendanceFormModal(meeting, isAdminOrDirector, container);
    });


    // Bind Excel export
    document.getElementById('btnExcel')?.addEventListener('click', async () => {
      const btn = document.getElementById('btnExcel');
      btn.disabled = true;
      try {
        const blob = await Api.get(`/attendances/meeting/${meeting.id}/excel`, { binary: true });
        downloadBlob(blob, `asistencias_${meeting.id}_${(meeting.titulo||'reunion').replace(/\s+/g,'_')}.xlsx`);
        Toast.success('Excel descargado', '');
      } catch (err) {
        Toast.error('Error al exportar', err.message);
      } finally {
        btn.disabled = false;
      }
    });

    // Bind create task
    document.getElementById('btnCreateTask')?.addEventListener('click', () => {
      openCreateTaskModal(meeting.id, meeting.titulo);
    });

    // Render recovery section for students/egresados
    renderRecoverySection(meetingAssignment, container, meeting);
  }

  // ── Recovery section for students/egresados ──────────
  function renderRecoverySection(assignment, container, meeting) {
    const section = document.getElementById('recoverySection');
    if (!section || !assignment) return;

    if (assignment.estado === 'COMPLETADA' || assignment.status === 'COMPLETADA') {
      section.innerHTML = `
        <div class="card" style="margin-bottom:1.5rem;border-left:4px solid #10b981;">
          <div class="card-body" style="display:flex;align-items:center;gap:1rem;">
            <span style="font-size:1.5rem;">✅</span>
            <div>
              <div style="font-weight:600;color:#10b981;">Asistencia recuperada</div>
              <div style="color:var(--text-muted);font-size:.85rem;">Aprobaste el quiz con ${assignment.score}% — Tu asistencia fue registrada.</div>
            </div>
          </div>
        </div>`;
      return;
    }

    if (assignment.estado === 'BLOQUEADA' || assignment.status === 'BLOQUEADA') {
      section.innerHTML = `
        <div class="card" style="margin-bottom:1.5rem;border-left:4px solid #ef4444;">
          <div class="card-body" style="display:flex;align-items:center;gap:1rem;">
            <span style="font-size:1.5rem;">⛔</span>
            <div>
              <div style="font-weight:600;color:#ef4444;">Recuperación cerrada</div>
              <div style="color:var(--text-muted);font-size:.85rem;">Esta tarea fue bloqueada. Ya no podés rendir el quiz.</div>
            </div>
          </div>
        </div>`;
      return;
    }

    if (!assignment.questionsJson) return;

    section.innerHTML = `
      <div class="card" style="margin-bottom:1.5rem;border-left:4px solid var(--primary);">
        <div class="card-body" style="display:flex;align-items:center;gap:1rem;justify-content:space-between;flex-wrap:wrap;">
          <div style="display:flex;align-items:center;gap:1rem;">
            <span style="font-size:1.5rem;">📝</span>
            <div>
              <div style="font-weight:600;">Recuperar asistencia</div>
              <div style="color:var(--text-muted);font-size:.85rem;">
                Realizá el quiz para recuperar tu asistencia a esta reunión.
                ${(assignment.intentos ?? assignment.attempts ?? 0) > 0 ? `Intentos: <strong>${assignment.intentos ?? assignment.attempts ?? 0}</strong> | Último score: <strong>${assignment.score != null ? assignment.score + '%' : '—'}</strong>` : 'Necesitás 70% o más para aprobar.'}
              </div>
            </div>
          </div>
          <button class="btn btn-primary" id="btnRecoverAttendance">📝 Rendir quiz</button>
        </div>
      </div>`;

    document.getElementById('btnRecoverAttendance')?.addEventListener('click', () => {
      openRecoveryQuizModal(assignment, container, meeting);
    });
  }

  // ── Quiz modal (reutiliza lógica de tasks.js) ────────
  function openRecoveryQuizModal(assignment, container, meeting) {
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
        <div id="recoveryQuizStepMaterial">
          <p style="color:var(--text-muted);margin-bottom:1rem;font-size:.9rem;">
            Antes de responder el quiz, revisá el material de apoyo.
          </p>
          <ul style="padding-left:1.2rem;margin-bottom:1rem;">
            ${materialHtml}
          </ul>
          <label style="display:flex;align-items:flex-start;gap:.5rem;cursor:pointer;margin-bottom:1rem;">
            <input type="checkbox" id="recoveryQuizConfirmVideos" style="margin-top:3px;width:16px;height:16px;" />
            <span style="font-size:.9rem;">Confirmo que revisé los videos/material de apoyo.</span>
          </label>
          <div class="form-actions">
            <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
            <button type="button" class="btn btn-primary" id="recoveryGoToQuizBtn">Continuar al quiz</button>
          </div>
        </div>
        <form id="recoveryQuizForm" style="display:none;">
          <p style="color:var(--text-muted);margin-bottom:1rem;font-size:.9rem;">
            Respondé todas las preguntas. Necesitás <strong>70% o más</strong> para aprobar y recuperar tu asistencia.
            ${(assignment.intentos ?? assignment.attempts ?? 0) > 0 ? `<br>Intentos anteriores: <strong>${assignment.intentos ?? assignment.attempts ?? 0}</strong> | Último score: <strong>${assignment.score != null ? assignment.score + '%' : '—'}</strong>` : ''}
          </p>
          ${formHtml}
          <div class="form-actions">
            <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
            <button type="submit" class="btn btn-primary" id="submitRecoveryBtn">📩 Enviar respuestas</button>
          </div>
        </form>
      </div>
    `, { persistent: true });

    document.getElementById('recoveryGoToQuizBtn')?.addEventListener('click', () => {
      if (!document.getElementById('recoveryQuizConfirmVideos')?.checked) {
        Toast.warning('Confirmación requerida', 'Marcá que revisaste los videos/material antes de continuar.');
        return;
      }
      document.getElementById('recoveryQuizStepMaterial').style.display = 'none';
      document.getElementById('recoveryQuizForm').style.display = '';
    });

    document.getElementById('recoveryQuizForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const btn = document.getElementById('submitRecoveryBtn');

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

        loadDetail(container, meeting.id);
      } catch(err) {
        Toast.error('Error', err.message);
        btn.disabled = false; btn.textContent = '📩 Enviar respuestas';
      }
    });
  }

  function renderAttendancesTable(arr) {
    if (!arr.length) return '<div class="empty-state"><p>Sin asistencias registradas aún</p></div>';
    return `
      <div class="table-wrapper">
        <table>
          <thead><tr><th>#</th><th>Nombre</th><th>Apellido</th><th>Correo</th><th>Hora de registro</th></tr></thead>
          <tbody>
            ${arr.map((a, i) => `
              <tr>
                <td>${i + 1}</td>
                <td>${escHtml(a.nombre   || a.user?.nombre   || '—')}</td>
                <td>${escHtml(a.apellido || a.user?.apellido || '—')}</td>
                <td>${escHtml(a.email || a.credencial?.email || '—')}</td>
                <td>${formatDate(a.fechaRegistro || a.registeredAt)}</td>
              </tr>`).join('')}
          </tbody>
        </table>
      </div>`;
  }

  // ── Cargar y mostrar lista de PDFs ─────────────────────
  async function loadPdfs(meetingId, canDelete) {
    const body  = document.getElementById('pdfListBody');
    const count = document.getElementById('pdfCount');
    if (!body) return;
    try {
      const files = await Api.get(`/meetings/${meetingId}/files`);
      const arr = Array.isArray(files) ? files : [];
      if (count) count.textContent = arr.length + ' archivo(s)';
      if (!arr.length) {
        body.innerHTML = '<p style="color:var(--text-muted)">Sin archivos adjuntos aún.</p>';
        return;
      }
      const fmtSize = b => b < 1048576 ? (b/1024).toFixed(1)+' KB' : (b/1048576).toFixed(1)+' MB';
      body.innerHTML = `<div class="table-wrapper"><table>
        <thead><tr><th>Nombre</th><th>Subido por</th><th>Fecha</th><th>Tamaño</th><th style="text-align:center">Acciones</th></tr></thead>
        <tbody>${arr.map(f => `<tr>
          <td>📄 ${escHtml(f.fileName)}</td>
          <td style="font-size:.85rem">${escHtml(f.uploadedByUsername||'—')}</td>
          <td style="font-size:.85rem">${formatDate(f.uploadedAt)}</td>
          <td style="font-size:.85rem">${f.fileSize ? fmtSize(f.fileSize) : '—'}</td>
          <td style="text-align:center;white-space:nowrap">
            <button class="btn btn-secondary btn-sm pdf-dl" data-fid="${f.id}" data-fn="${escHtml(f.fileName)}">⬇ Descargar</button>
            ${canDelete ? `<button class="btn btn-secondary btn-sm pdf-del" data-fid="${f.id}" data-fn="${escHtml(f.fileName)}" style="color:#ef4444;margin-left:.25rem">🗑</button>` : ''}
          </td></tr>`).join('')}</tbody>
      </table></div>`;
      body.querySelectorAll('.pdf-dl').forEach(btn => btn.addEventListener('click', async () => {
        btn.disabled = true;
        try { const blob = await Api.get(`/files/${btn.dataset.fid}/download`, { binary: true }); downloadBlob(blob, btn.dataset.fn); Toast.success('Descarga completada', btn.dataset.fn); }
        catch(err) { Toast.error('Error', err.message); } finally { btn.disabled = false; }
      }));
      body.querySelectorAll('.pdf-del').forEach(btn => btn.addEventListener('click', async () => {
        if (!confirm('\u00bfEliminar "' + btn.dataset.fn + '"?')) return;
        try { await Api.delete(`/files/${btn.dataset.fid}`); Toast.success('Eliminado', btn.dataset.fn); loadPdfs(meetingId, canDelete); }
        catch(err) { Toast.error('Error', err.message); }
      }));
    } catch (err) {
      if (body) body.innerHTML = '<p style="color:#ef4444">Error al cargar archivos.</p>';
    }
  }

  function openCreateTaskModal(meetingId, meetingTitle) {
    let mdQuestions = [];
    for (let i = 0; i < 5; i++) mdQuestions.push({ question: '', options: ['', '', ''], correct: 0 });
    let createLinks = [''];

    function renderMdTaskLinks() {
      const c = document.getElementById('mdTaskLinksContainer');
      if (!c) return;
      c.innerHTML = createLinks.map((link, i) => `
        <div style="display:flex;gap:.5rem;margin-bottom:.35rem;">
          <input class="form-control md-task-link" data-li="${i}" value="${escHtml(link)}" type="url" maxlength="500" placeholder="https://..." />
          <button type="button" class="btn btn-secondary btn-sm md-task-link-rm" data-li="${i}" style="color:#ef4444;">✕</button>
        </div>
      `).join('');
      c.querySelectorAll('.md-task-link').forEach(input => {
        input.addEventListener('input', () => { createLinks[Number(input.dataset.li)] = input.value; });
      });
      c.querySelectorAll('.md-task-link-rm').forEach(btn => {
        btn.addEventListener('click', () => {
          createLinks.splice(Number(btn.dataset.li), 1);
          if (!createLinks.length) createLinks.push('');
          renderMdTaskLinks();
        });
      });
    }

    function renderMdQuiz() {
      const c = document.getElementById('mdQuizBuilder');
      if (!c) return;
      c.innerHTML = mdQuestions.map((q, qi) => `
        <div style="border:1px solid var(--border-color);border-radius:8px;padding:.75rem;margin-bottom:.5rem;">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:.4rem;">
            <strong style="font-size:.9rem;">Pregunta ${qi + 1}</strong>
            ${mdQuestions.length > 5 ? `<button type="button" class="btn btn-secondary btn-sm mdq-remove" data-qi="${qi}" style="color:#ef4444;font-size:.7rem;">🗑</button>` : ''}
          </div>
          <input class="form-control mdq-text" data-qi="${qi}" value="${escHtml(q.question)}" placeholder="Enunciado" style="margin-bottom:.4rem;" />
          <div style="font-size:.8rem;color:var(--text-muted);margin-bottom:.25rem;">Opciones (mín 3) — marcá la correcta:</div>
          ${q.options.map((o, oi) => `
            <div style="display:flex;align-items:center;gap:.4rem;margin-bottom:.25rem;">
              <input type="radio" name="mdc_${qi}" value="${oi}" ${q.correct===oi?'checked':''} class="mdq-correct" data-qi="${qi}" data-oi="${oi}" style="width:16px;height:16px;" />
              <input class="form-control mdq-opt" data-qi="${qi}" data-oi="${oi}" value="${escHtml(o)}" placeholder="Opción ${oi+1}" style="flex:1;font-size:.9rem;" />
              ${q.options.length>3 ? `<button type="button" class="btn btn-secondary btn-sm mdq-rmopt" data-qi="${qi}" data-oi="${oi}" style="color:#ef4444;padding:0 .3rem;font-size:.7rem;">✕</button>` : ''}
            </div>
          `).join('')}
          <button type="button" class="btn btn-secondary btn-sm mdq-addopt" data-qi="${qi}" style="font-size:.7rem;margin-top:.15rem;">+ Opción</button>
        </div>
      `).join('');

      c.querySelectorAll('.mdq-text').forEach(e => e.addEventListener('input', () => { mdQuestions[+e.dataset.qi].question = e.value; }));
      c.querySelectorAll('.mdq-opt').forEach(e => e.addEventListener('input', () => { mdQuestions[+e.dataset.qi].options[+e.dataset.oi] = e.value; }));
      c.querySelectorAll('.mdq-correct').forEach(e => e.addEventListener('change', () => { mdQuestions[+e.dataset.qi].correct = +e.dataset.oi; }));
      c.querySelectorAll('.mdq-remove').forEach(e => e.addEventListener('click', () => { mdQuestions.splice(+e.dataset.qi, 1); renderMdQuiz(); }));
      c.querySelectorAll('.mdq-rmopt').forEach(e => e.addEventListener('click', () => {
        const qi=+e.dataset.qi, oi=+e.dataset.oi;
        mdQuestions[qi].options.splice(oi, 1);
        if (mdQuestions[qi].correct >= mdQuestions[qi].options.length) mdQuestions[qi].correct = 0;
        renderMdQuiz();
      }));
      c.querySelectorAll('.mdq-addopt').forEach(e => e.addEventListener('click', () => { mdQuestions[+e.dataset.qi].options.push(''); renderMdQuiz(); }));
    }

    Modal.open(`Crear tarea con Quiz — ${escHtml(meetingTitle)}`, `
      <p class="text-sm" style="color:var(--text-muted);margin-bottom:.75rem;">
        Se asignará automáticamente a quienes <strong>no asistieron</strong> y tengan rol recuperable (estudiante, ayudante, egresado).
      </p>
      <div class="form-group">
        <label class="form-label">Título *</label>
        <input class="form-control" id="tTitle" placeholder="Ej: Quiz de la clase" required maxlength="200" />
      </div>
      <div class="form-group">
        <label class="form-label">Descripción (opcional)</label>
        <textarea class="form-control" id="tDesc" placeholder="Instrucciones..." rows="2"></textarea>
      </div>
      <div class="form-group">
        <label class="form-label">Links de apoyo (opcionales)</label>
        <div id="mdTaskLinksContainer"></div>
        <button type="button" class="btn btn-secondary btn-sm" id="mdAddTaskLink" style="margin-top:.35rem;font-size:.8rem;">+ Agregar link</button>
      </div>
      <div style="margin:.75rem 0 .35rem;"><strong style="font-size:.95rem;">📝 Preguntas del quiz (mín. 5 — máx. 20)</strong></div>
      <div id="mdQuizBuilder" style="max-height:40vh;overflow-y:auto;"></div>
      <button type="button" class="btn btn-secondary btn-sm" id="mdAddQ" style="margin-top:.5rem;">➕ Agregar pregunta</button>
      <div class="form-actions" style="margin-top:.75rem;">
        <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
        <button type="button" class="btn btn-primary" id="saveTaskBtn">✅ Crear y asignar</button>
      </div>
    `);

    renderMdTaskLinks();
    renderMdQuiz();

    document.getElementById('mdAddTaskLink').addEventListener('click', () => {
      createLinks.push('');
      renderMdTaskLinks();
    });

    document.getElementById('mdAddQ').addEventListener('click', () => {
      if (mdQuestions.length >= 20) { Toast.warning('Máximo 20 preguntas',''); return; }
      mdQuestions.push({ question: '', options: ['', '', ''], correct: 0 });
      renderMdQuiz();
    });

    document.getElementById('saveTaskBtn').addEventListener('click', async () => {
      const btn = document.getElementById('saveTaskBtn');
      const title = document.getElementById('tTitle').value.trim();
      const desc = document.getElementById('tDesc').value.trim();
      const links = createLinks.map(l => (l || '').trim()).filter(Boolean);
      if (!title) { Toast.warning('El título es obligatorio',''); return; }
      if (mdQuestions.length < 5) { Toast.warning('Mínimo 5 preguntas', `Tenés ${mdQuestions.length}`); return; }
      if (mdQuestions.length > 20) { Toast.warning('Máximo 20 preguntas', ''); return; }
      for (let i = 0; i < mdQuestions.length; i++) {
        const q = mdQuestions[i];
        if (!q.question.trim()) { Toast.warning(`Pregunta ${i+1}`, 'Falta el enunciado'); return; }
        if (q.options.length < 3) { Toast.warning(`Pregunta ${i+1}`, 'Mínimo 3 opciones'); return; }
        for (let j = 0; j < q.options.length; j++) {
          if (!q.options[j].trim()) { Toast.warning(`Pregunta ${i+1}`, `Opción ${j+1} vacía`); return; }
        }
        if (q.correct < 0 || q.correct >= q.options.length) { Toast.warning(`Pregunta ${i+1}`, 'Seleccioná la respuesta correcta'); return; }
      }
      btn.disabled = true; btn.textContent = 'Creando...';
      try {
        const result = await Api.post(`/tasks/meeting/${meetingId}`, {
          title,
          description: desc || null,
          link: links[0] || null,
          links,
          questionsJson: JSON.stringify(mdQuestions),
        });
        Modal.close();
        const count = Array.isArray(result) ? result.length : 1;
        Toast.success('Tarea creada', `Quiz asignado a ${count} usuario(s) ausente(s)`);
      } catch (err) {
        Toast.error('Error', err.message);
        btn.disabled = false; btn.textContent = '✅ Crear y asignar';
      }
    });
  }

  // ── Attendance form modal ──
  function openAttendanceFormModal(meeting, canManage, container) {
    const user = AuthService.getUser();

    Modal.open(`Registrar asistencia \u2014 ${meeting.titulo}`, `
      <form id="attendDetailForm">
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;">
          <div class="form-group">
            <label class="form-label">Nombre</label>
            <input class="form-control" id="adNombre" value="${escHtml(user?.nombre || '')}" readonly />
          </div>
          <div class="form-group">
            <label class="form-label">Apellido</label>
            <input class="form-control" id="adApellido" value="${escHtml(user?.apellido || '')}" readonly />
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">Correo electr\u00f3nico</label>
          <input class="form-control" id="adEmail" value="${escHtml(user?.email || '')}" readonly />
        </div>
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
          <button type="submit" class="btn btn-primary" id="adSubmitBtn">\u2705 Registrar</button>
        </div>
      </form>`);

    document.getElementById('attendDetailForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const btn = document.getElementById('adSubmitBtn');
      btn.disabled = true;
      btn.textContent = 'Registrando...';
      try {
        await Api.post(`/attendances/meeting/${meeting.id}/self`, { presente: true });
        Modal.close();
        Toast.success('\u2705 Asistencia registrada', meeting.title);
        const [fresh, freshAtt] = await Promise.all([
          Api.get(`/meetings/${meeting.id}`),
          canManage ? Api.get(`/attendances/meeting/${meeting.id}`) : Promise.resolve([]),
        ]);
        currentMeeting = fresh;
        setHTML('#attendancesTable', renderAttendancesTable(Array.isArray(freshAtt) ? freshAtt : []));
      } catch (err) {
        Toast.error('No se pudo registrar', err.message);
        btn.disabled = false;
        btn.textContent = '\u2705 Registrar';
      }
    });
  }

  return { render };
})();
