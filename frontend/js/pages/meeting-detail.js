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
          ${isAdminOrProf ? `
            <input type="file" id="pdfFileInput" accept="application/pdf" multiple style="display:none;" />
            <button class="btn btn-secondary" id="btnUploadPdf">⬆ Subir PDF(s)</button>
          ` : ''}
          ${canAttend ? `<button class="btn btn-success" id="btnAttend">✅ Registrar mi asistencia</button>` : ''}
          ${isAdminOrProf ? `<button class="btn btn-secondary" id="btnExcel">📥 Exportar Excel</button>` : ''}
          ${canManage && AuthService.isStaff() ? `<button class="btn btn-primary btn-sm" id="btnCreateTask">➕ Crear tarea</button>` : ''}
        </div>
      </div>

      <!-- Info card -->
      <div class="card" style="margin-bottom:1.5rem;">
        <div class="card-body">
          <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:1rem;">
            <div><div class="text-xs" style="color:var(--text-muted);margin-bottom:.25rem;">CÓDIGO DE ACCESO</div><strong>${escHtml(meeting.accessCode || '—')}</strong></div>
            <div><div class="text-xs" style="color:var(--text-muted);margin-bottom:.25rem;">CREADO POR</div><strong>${escHtml(meeting.createdBy || '—')}</strong></div>
            <div><div class="text-xs" style="color:var(--text-muted);margin-bottom:.25rem;">DESCRIPCIÓN</div>${escHtml(meeting.description || 'Sin descripción')}</div>
            ${canManage ? `<div><div class="text-xs" style="color:var(--text-muted);margin-bottom:.25rem;">ASISTENTES</div><strong>${arr.length}</strong></div>` : ''}
          </div>
        </div>
      </div>

      <!-- PDFs adjuntos -->
      <div class="card" style="margin-bottom:1.5rem;" id="pdfSection">
        <div class="card-header">
          <span class="card-title">📄 Archivos PDF</span>
          <span id="pdfCount" class="text-sm" style="color:var(--text-muted)">Cargando...</span>
        </div>
        <div class="card-body" id="pdfListBody">
          <div class="loading"><div class="spinner"></div></div>
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

    // Bind PDF upload (multi-file)
    document.getElementById('btnUploadPdf')?.addEventListener('click', () => {
      document.getElementById('pdfFileInput').click();
    });

    document.getElementById('pdfFileInput')?.addEventListener('change', async (e) => {
      const files = Array.from(e.target.files);
      if (!files.length) return;
      const btn = document.getElementById('btnUploadPdf');
      btn.disabled = true;
      btn.textContent = 'Subiendo...';
      const formData = new FormData();
      files.forEach(f => formData.append('files', f));
      try {
        await Api.post(`/meetings/${meeting.id}/files`, formData);
        Toast.success(`${files.length} PDF(s) subidos`, files.map(f=>f.name).join(', '));
        loadPdfs(meeting.id, isAdminOrProf);
      } catch (err) {
        Toast.error('Error al subir PDF(s)', err.message);
      } finally {
        btn.disabled = false;
        btn.textContent = '⬆ Subir PDF(s)';
        e.target.value = '';
      }
    });

    // Load PDFs section
    loadPdfs(meeting.id, isAdminOrProf);

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
    Modal.open(`Crear tarea — ${meetingTitle}`, `
      <p class="text-sm" style="color:var(--text-muted);margin-bottom:1rem;">
        La tarea se asignará automáticamente a los estudiantes que <strong>no asistieron</strong> a esta reunión.
      </p>
      <form id="taskForm">
        <div class="form-group">
          <label class="form-label">Título *</label>
          <input class="form-control" id="tTitle" placeholder="Ej: Resumen de clase" required maxlength="200" />
        </div>
        <div class="form-group">
          <label class="form-label">Descripción *</label>
          <textarea class="form-control" id="tDesc" placeholder="Descripción detallada de la tarea" rows="3" required></textarea>
        </div>
        <div class="form-group">
          <label class="form-label">Link (URL)</label>
          <input class="form-control" id="tLink" type="url" placeholder="https://drive.google.com/..." />
        </div>
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
          <button type="submit" class="btn btn-primary" id="saveTaskBtn">Crear tarea</button>
        </div>
      </form>`);

    document.getElementById('taskForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const btn = document.getElementById('saveTaskBtn');
      btn.disabled = true;
      btn.textContent = 'Creando...';
      try {
        const result = await Api.post(`/tasks/meeting/${meetingId}`, {
          title:       document.getElementById('tTitle').value.trim(),
          description: document.getElementById('tDesc').value.trim(),
          link:        document.getElementById('tLink').value.trim() || null,
        });
        Modal.close();
        const count = Array.isArray(result) ? result.length : 1;
        Toast.success('Tarea creada', `Asignada a ${count} estudiante(s) ausente(s)`);
      } catch (err) {
        Toast.error('Error', err.message);
        btn.disabled = false;
        btn.textContent = 'Crear tarea';
      }
    });
  }

  // ── Attendance form modal (reutiliza misma lógica que meetings.js) ──
  function openAttendanceFormModal(meeting, canManage, container) {
    const user = AuthService.getUser();
    Modal.open(`Registrar asistencia — ${meeting.title}`, `
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
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;">
          <div class="form-group">
            <label class="form-label">Legajo *</label>
            <input class="form-control" id="adLegajo" placeholder="Ej: 12345" required maxlength="20" />
          </div>
          <div class="form-group">
            <label class="form-label">Carrera que cursás *</label>
            <input class="form-control" id="adCarrera" placeholder="Ej: Ing. en Sistemas" required maxlength="150" />
          </div>
        </div>
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" onclick="Modal.close()">Cancelar</button>
          <button type="submit" class="btn btn-primary" id="adSubmitBtn">✅ Registrar</button>
        </div>
      </form>`);

    document.getElementById('attendDetailForm').addEventListener('submit', async (e) => {
      e.preventDefault();
      const btn = document.getElementById('adSubmitBtn');
      btn.disabled = true;
      btn.textContent = 'Registrando...';
      try {
        await Api.post(`/attendances/meeting/${meeting.id}/self`, {
          nombre:              document.getElementById('adNombre').value.trim(),
          apellido:            document.getElementById('adApellido').value.trim(),
          correoInstitucional: document.getElementById('adEmail').value.trim(),
          legajo:              document.getElementById('adLegajo').value.trim(),
          carrera:             document.getElementById('adCarrera').value.trim(),
        });
        Modal.close();
        Toast.success('✅ Asistencia registrada', meeting.title);
        // Reload attendance list
        const [fresh, freshAtt] = await Promise.all([
          Api.get(`/meetings/${meeting.id}`),
          canManage ? Api.get(`/attendances/meeting/${meeting.id}`) : Promise.resolve([]),
        ]);
        currentMeeting = fresh;
        setHTML('#attendancesTable', renderAttendancesTable(Array.isArray(freshAtt) ? freshAtt : []));
      } catch (err) {
        Toast.error('No se pudo registrar', err.message);
        btn.disabled = false;
        btn.textContent = '✅ Registrar';
      }
    });
  }

  return { render };
})();
