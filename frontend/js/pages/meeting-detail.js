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
      const [meeting, attendances] = await Promise.all([
        Api.get(`/meetings/${meetingId}`),
        AuthService.isStaff() ? Api.get(`/attendances/meeting/${meetingId}`) : Promise.resolve([]),
      ]);
      currentMeeting = meeting;
      renderDetail(container, meeting, attendances);
    } catch (err) {
      Toast.error('Error', err.message);
      container.innerHTML = `<div class="empty-state"><h3>Error al cargar reunión</h3><p>${escHtml(err.message)}</p></div>`;
    }
  }

  function renderDetail(container, meeting, attendances) {
    const isActiva    = meeting.status === 'ACTIVA';
    const canManage   = AuthService.isStaff();
    const isAdminOrProf = AuthService.isAdmin() || AuthService.isProfesor();
    const canAttend   = isActiva;
    const arr = Array.isArray(attendances) ? attendances : [];

    container.innerHTML = `
      <!-- Back + Header -->
      <div class="page-header">
        <div style="display:flex;align-items:center;gap:1rem;">
          <button class="btn btn-secondary btn-sm" data-href="/meetings">← Volver</button>
          <div>
            <h2>${escHtml(meeting.title)}</h2>
            <p>${meeting.scheduledAt ? formatDate(meeting.scheduledAt) : ''} &nbsp;${statusBadge(meeting.status)}</p>
          </div>
        </div>
        <div style="display:flex;gap:.5rem;flex-wrap:wrap;align-items:center;">
          ${canAttend ? `<button class="btn btn-success" id="btnAttend">✅ Registrar mi asistencia</button>` : ''}
          ${isAdminOrProf ? `<button class="btn btn-secondary" id="btnExcel">📥 Exportar Excel</button>` : ''}
          ${canManage && AuthService.isStaff() ? `<button class="btn btn-primary btn-sm" id="btnCreateTask">➕ Crear tarea</button>` : ''}
        </div>
      </div>

      <!-- Info card -->
      <div class="card" style="margin-bottom:1.5rem;">
        <div class="card-body">
          <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:1rem;">
            <div><div class="text-xs" style="color:var(--text-muted);margin-bottom:.25rem;">LINK DE REUNIÓN</div>${meeting.accessCode ? `<a href="${escHtml(meeting.accessCode)}" target="_blank" rel="noopener" style="color:var(--primary);text-decoration:underline;">${escHtml(meeting.accessCode)}</a>` : '<span>—</span>'}</div>
            <div><div class="text-xs" style="color:var(--text-muted);margin-bottom:.25rem;">CREADO POR</div><strong>${escHtml(meeting.createdBy || '—')}</strong></div>
            <div><div class="text-xs" style="color:var(--text-muted);margin-bottom:.25rem;">DESCRIPCIÓN</div>${escHtml(meeting.description || 'Sin descripción')}</div>
            ${canManage ? `<div><div class="text-xs" style="color:var(--text-muted);margin-bottom:.25rem;">ASISTENTES</div><strong>${arr.length}</strong></div>` : ''}
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
            <div style="display:flex;align-items:center;gap:.75rem;padding:.75rem;background:var(--bg-secondary);border-radius:8px;">
              <span style="font-size:1.3rem;">🎥</span>
              <div style="flex:1;">
                <div style="font-weight:600;margin-bottom:.15rem;">Grabación de la reunión</div>
                ${meeting.recordingLink ? `<a href="${escHtml(meeting.recordingLink)}" target="_blank" rel="noopener" style="color:var(--primary);text-decoration:underline;word-break:break-all;font-size:.9rem;">${escHtml(meeting.recordingLink)}</a>` : '<span style="color:var(--text-muted);font-size:.9rem;">Sin link aún</span>'}
              </div>
            </div>
            <div style="display:flex;align-items:center;gap:.75rem;padding:.75rem;background:var(--bg-secondary);border-radius:8px;">
              <span style="font-size:1.3rem;">📰</span>
              <div style="flex:1;">
                <div style="font-weight:600;margin-bottom:.15rem;">Noticias</div>
                ${meeting.newsLink ? `<a href="${escHtml(meeting.newsLink)}" target="_blank" rel="noopener" style="color:var(--primary);text-decoration:underline;word-break:break-all;font-size:.9rem;">${escHtml(meeting.newsLink)}</a>` : '<span style="color:var(--text-muted);font-size:.9rem;">Sin link aún</span>'}
              </div>
            </div>
            <div style="display:flex;align-items:center;gap:.75rem;padding:.75rem;background:var(--bg-secondary);border-radius:8px;">
              <span style="font-size:1.3rem;">📝</span>
              <div style="flex:1;">
                <div style="font-weight:600;margin-bottom:.15rem;">Link de actividad de la reunión</div>
                ${meeting.activityLink ? `<a href="${escHtml(meeting.activityLink)}" target="_blank" rel="noopener" style="color:var(--primary);text-decoration:underline;word-break:break-all;font-size:.9rem;">${escHtml(meeting.activityLink)}</a>` : '<span style="color:var(--text-muted);font-size:.9rem;">Sin link aún</span>'}
              </div>
            </div>
          </div>
        </div>
      </div>


      <!-- Attendances table (staff only) -->
      ${canManage ? `
      <div class="card">
        <div class="card-header">
          <span class="card-title">👥 Lista de asistencias</span>
          <span class="text-sm text-muted">${arr.length} asistente(s)</span>
        </div>
        <div id="attendancesTable" class="card-body">
          ${renderAttendancesTable(arr)}
        </div>
      </div>` : `
      <div class="card">
        <div class="card-body">
          <p style="color:var(--text-muted)">Para ver la lista de asistentes necesitás rol de PROFESOR, AYUDANTE o ADMIN.</p>
        </div>
      </div>`}
    `;

    // Bind attend button — open form modal
    document.getElementById('btnAttend')?.addEventListener('click', () => {
      openAttendanceFormModal(meeting, canManage, container);
    });


    // Bind Excel export
    document.getElementById('btnExcel')?.addEventListener('click', async () => {
      const btn = document.getElementById('btnExcel');
      btn.disabled = true;
      try {
        const blob = await Api.get(`/attendances/meeting/${meeting.id}/excel`, { binary: true });
        downloadBlob(blob, `asistencias_${meeting.id}_${meeting.title.replace(/\s+/g,'_')}.xlsx`);
        Toast.success('Excel descargado', '');
      } catch (err) {
        Toast.error('Error al exportar', err.message);
      } finally {
        btn.disabled = false;
      }
    });

    // Bind create task
    document.getElementById('btnCreateTask')?.addEventListener('click', () => {
      openCreateTaskModal(meeting.id, meeting.title);
    });
  }

  function renderAttendancesTable(arr) {
    if (!arr.length) return '<div class="empty-state"><p>Sin asistencias registradas aún</p></div>';
    return `
      <div class="table-wrapper">
        <table>
          <thead><tr><th>#</th><th>Usuario</th><th>Nombre</th><th>Apellido</th><th>Correo</th><th>Legajo</th><th>Carrera</th><th>Hora de registro</th></tr></thead>
          <tbody>
            ${arr.map((a, i) => `
              <tr>
                <td>${i + 1}</td>
                <td><strong>${escHtml(a.username || a.user?.username || '—')}</strong></td>
                <td>${escHtml(a.firstName || a.user?.firstName || '—')}</td>
                <td>${escHtml(a.lastName  || a.user?.lastName  || '—')}</td>
                <td>${escHtml(a.email || '—')}</td>
                <td>${escHtml(a.legajo || '—')}</td>
                <td>${escHtml(a.carrera || '—')}</td>
                <td>${formatDate(a.registeredAt)}</td>
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

    Modal.open(`Crear Quiz — ${meetingTitle}`, `
      <p class="text-sm" style="color:var(--text-muted);margin-bottom:.75rem;">
        Se asignará automáticamente a los estudiantes que <strong>no asistieron</strong>.
      </p>
      <div class="form-group">
        <label class="form-label">Título *</label>
        <input class="form-control" id="tTitle" placeholder="Ej: Quiz de la clase" required maxlength="200" />
      </div>
      <div class="form-group">
        <label class="form-label">Descripción (opcional)</label>
        <textarea class="form-control" id="tDesc" placeholder="Instrucciones..." rows="2"></textarea>
      </div>
      <div style="margin:.75rem 0 .35rem;"><strong style="font-size:.95rem;">📝 Preguntas (5-20)</strong></div>
      <div id="mdQuizBuilder" style="max-height:40vh;overflow-y:auto;"></div>
      <button type="button" class="btn btn-secondary btn-sm" id="mdAddQ" style="margin-top:.5rem;">➕ Agregar pregunta</button>
      <div class="form-actions" style="margin-top:.75rem;">
        <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
        <button type="button" class="btn btn-primary" id="saveTaskBtn">✅ Crear quiz</button>
      </div>
    `);

    renderMdQuiz();

    document.getElementById('mdAddQ').addEventListener('click', () => {
      if (mdQuestions.length >= 20) { Toast.warning('Máximo 20 preguntas',''); return; }
      mdQuestions.push({ question: '', options: ['', '', ''], correct: 0 });
      renderMdQuiz();
    });

    document.getElementById('saveTaskBtn').addEventListener('click', async () => {
      const btn = document.getElementById('saveTaskBtn');
      const title = document.getElementById('tTitle').value.trim();
      const desc = document.getElementById('tDesc').value.trim();
      if (!title) { Toast.warning('El título es obligatorio',''); return; }
      // Validate quiz
      if (mdQuestions.length < 5) { Toast.warning('Mínimo 5 preguntas', `Tenés ${mdQuestions.length}`); return; }
      for (let i = 0; i < mdQuestions.length; i++) {
        const q = mdQuestions[i];
        if (!q.question.trim()) { Toast.warning(`Pregunta ${i+1}`, 'Falta el enunciado'); return; }
        if (q.options.length < 3) { Toast.warning(`Pregunta ${i+1}`, 'Mínimo 3 opciones'); return; }
        for (let j = 0; j < q.options.length; j++) {
          if (!q.options[j].trim()) { Toast.warning(`Pregunta ${i+1}`, `Opción ${j+1} vacía`); return; }
        }
      }
      btn.disabled = true; btn.textContent = 'Creando...';
      try {
        const result = await Api.post(`/tasks/meeting/${meetingId}`, {
          title, description: desc || null, link: null,
          questionsJson: JSON.stringify(mdQuestions),
        });
        Modal.close();
        const count = Array.isArray(result) ? result.length : 1;
        Toast.success('Quiz creado', `Asignado a ${count} estudiante(s) ausente(s)`);
      } catch (err) {
        Toast.error('Error', err.message);
        btn.disabled = false; btn.textContent = '✅ Crear quiz';
      }
    });
  }

  // ── Attendance form modal ──
  function openAttendanceFormModal(meeting, canManage, container) {
    const user = AuthService.getUser();
    const CARRERAS_SIGLO21 = [
      'Abogacía',
      'Contador Público',
      'Licenciatura en Administración',
      'Licenciatura en Comercio Internacional',
      'Licenciatura en Comunicación',
      'Licenciatura en Diseño Gráfico',
      'Licenciatura en Educación',
      'Licenciatura en Gestión Ambiental',
      'Licenciatura en Gestión de Recursos Humanos',
      'Licenciatura en Gestión Turística',
      'Licenciatura en Informática',
      'Licenciatura en Marketing',
      'Licenciatura en Psicología',
      'Licenciatura en Publicidad',
      'Licenciatura en Relaciones Internacionales',
      'Licenciatura en Relaciones Públicas',
      'Ingeniería en Software',
      'Ingeniería Industrial',
      'Tecnicatura en Programación',
      'Tecnicatura en Desarrollo Web',
      'Tecnicatura en Marketing Digital',
    ];

    Modal.open(`Registrar asistencia \u2014 ${meeting.title}`, `
      <form id="attendDetailForm">
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;">
          <div class="form-group">
            <label class="form-label">Nombre</label>
            <input class="form-control" id="adNombre" value="${escHtml(user?.firstName || '')}" readonly />
          </div>
          <div class="form-group">
            <label class="form-label">Apellido</label>
            <input class="form-control" id="adApellido" value="${escHtml(user?.lastName || '')}" readonly />
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">Correo institucional</label>
          <input class="form-control" id="adEmail" value="${escHtml(user?.email || '')}" readonly />
        </div>
        <div class="form-group">
          <label class="form-label">Legajo *</label>
          <input class="form-control" id="adLegajo" placeholder="Ej: 12345" required maxlength="20" />
        </div>

        <!-- Tipo de usuario -->
        <div class="form-group">
          <label class="form-label">Tipo de usuario *</label>
          <div style="display:flex;gap:1.5rem;margin-top:.35rem;">
            <label style="display:flex;align-items:center;gap:.4rem;cursor:pointer;font-size:.95rem;">
              <input type="radio" name="tipoUsuario" value="Alumno" id="adTipoAlumno" required /> Alumno
            </label>
            <label style="display:flex;align-items:center;gap:.4rem;cursor:pointer;font-size:.95rem;">
              <input type="radio" name="tipoUsuario" value="Egresado" id="adTipoEgresado" /> Egresado
            </label>
          </div>
        </div>

        <!-- Carrera (solo para alumnos) -->
        <div class="form-group" id="adCarreraGroup" style="display:none;">
          <label class="form-label">Carrera *</label>
          <select class="form-control" id="adCarreraSelect">
            <option value="">Seleccion\u00e1 tu carrera</option>
            ${CARRERAS_SIGLO21.map(c => `<option value="${escHtml(c)}">${escHtml(c)}</option>`).join('')}
            <option value="__otra__">Otra</option>
          </select>
        </div>
        <div class="form-group" id="adCarreraOtraGroup" style="display:none;">
          <label class="form-label">Especificar carrera *</label>
          <input class="form-control" id="adCarreraOtra" placeholder="Escrib\u00ed tu carrera" maxlength="150" />
        </div>

        <div class="form-actions">
          <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
          <button type="submit" class="btn btn-primary" id="adSubmitBtn">\u2705 Registrar</button>
        </div>
      </form>`);

    // Toggle carrera visibility based on tipo
    const tipoRadios = document.querySelectorAll('input[name="tipoUsuario"]');
    const carreraGroup = document.getElementById('adCarreraGroup');
    const carreraOtraGroup = document.getElementById('adCarreraOtraGroup');
    const carreraSelect = document.getElementById('adCarreraSelect');
    const carreraOtra = document.getElementById('adCarreraOtra');

    tipoRadios.forEach(r => r.addEventListener('change', () => {
      const isAlumno = document.getElementById('adTipoAlumno').checked;
      carreraGroup.style.display = isAlumno ? '' : 'none';
      if (!isAlumno) {
        carreraOtraGroup.style.display = 'none';
        carreraSelect.value = '';
        carreraOtra.value = '';
      }
    }));

    carreraSelect.addEventListener('change', () => {
      carreraOtraGroup.style.display = carreraSelect.value === '__otra__' ? '' : 'none';
      if (carreraSelect.value !== '__otra__') carreraOtra.value = '';
    });

    document.getElementById('attendDetailForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const btn = document.getElementById('adSubmitBtn');
      const tipoUsuario = document.querySelector('input[name="tipoUsuario"]:checked')?.value;
      if (!tipoUsuario) { Toast.error('Error', 'Seleccion\u00e1 si sos Alumno o Egresado'); return; }

      let carrera = null;
      if (tipoUsuario === 'Alumno') {
        const sel = carreraSelect.value;
        if (!sel) { Toast.error('Error', 'Seleccion\u00e1 tu carrera'); return; }
        carrera = sel === '__otra__' ? carreraOtra.value.trim() : sel;
        if (!carrera) { Toast.error('Error', 'Escrib\u00ed tu carrera'); return; }
      }

      btn.disabled = true;
      btn.textContent = 'Registrando...';
      try {
        await Api.post(`/attendances/meeting/${meeting.id}/self`, {
          nombre:              document.getElementById('adNombre').value.trim(),
          apellido:            document.getElementById('adApellido').value.trim(),
          correoInstitucional: document.getElementById('adEmail').value.trim(),
          legajo:              document.getElementById('adLegajo').value.trim(),
          carrera:             carrera,
          tipoUsuario:         tipoUsuario,
        });
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
