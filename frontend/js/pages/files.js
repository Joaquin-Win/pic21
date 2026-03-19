/* ═══════════════════════════════════════════════════════
   PIC21 — Files Management Page (solo ADMIN)
   Lista todos los PDFs del sistema con opción de eliminar
═══════════════════════════════════════════════════════ */

const FilesPage = (() => {
  let allFiles = [];

  function render(container) {
    container.innerHTML = `
      <div class="page-header">
        <div>
          <h2>📁 Gestión de Archivos</h2>
          <p>Todos los PDFs adjuntos a reuniones del sistema</p>
        </div>
        <button class="btn btn-secondary" id="btnRefreshFiles">🔄 Actualizar</button>
      </div>
      <div id="filesContainer">
        <div class="loading"><div class="spinner"></div></div>
      </div>`;

    document.getElementById('btnRefreshFiles')?.addEventListener('click', loadFiles);
    loadFiles();
  }

  async function loadFiles() {
    const container = document.getElementById('filesContainer');
    if (!container) return;
    showLoading(container);
    try {
      allFiles = await Api.get('/files');
      renderTable(container);
    } catch (err) {
      Toast.error('Error al cargar archivos', err.message);
      showEmpty(container, 'Error', err.message);
    }
  }

  function renderTable(container) {
    if (!allFiles.length) {
      showEmpty(container, 'Sin archivos', 'Todavía no hay PDFs subidos al sistema.');
      return;
    }

    container.innerHTML = `
      <div class="card">
        <div class="card-header">
          <span class="card-title">📄 Archivos PDF</span>
          <span class="text-sm" style="color:var(--text-muted)">${allFiles.length} archivo(s)</span>
        </div>
        <div class="card-body" style="padding:0">
          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Archivo</th>
                  <th>Reunión</th>
                  <th>Subido por</th>
                  <th>Fecha</th>
                  <th>Tamaño</th>
                  <th style="text-align:center">Acciones</th>
                </tr>
              </thead>
              <tbody>
                ${allFiles.map((f, i) => fileRow(f, i + 1)).join('')}
              </tbody>
            </table>
          </div>
        </div>
      </div>`;

    container.querySelectorAll('[data-file-action]').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = Number(btn.dataset.id);
        const file = allFiles.find(f => f.id === id);
        if (btn.dataset.fileAction === 'download') downloadFile(file);
        if (btn.dataset.fileAction === 'delete')   deleteFile(id, file?.fileName);
      });
    });
  }

  function fileRow(f, idx) {
    const size = f.fileSize ? formatFileSize(f.fileSize) : '—';
    return `
      <tr>
        <td>${idx}</td>
        <td>📄 <strong>${escHtml(f.fileName)}</strong></td>
        <td>${escHtml(f.meetingTitle || '—')}</td>
        <td>${escHtml(f.uploadedByUsername || '—')}</td>
        <td style="font-size:.85rem">${formatDate(f.uploadedAt)}</td>
        <td style="font-size:.85rem">${size}</td>
        <td style="text-align:center;white-space:nowrap">
          <button class="btn btn-secondary btn-sm" data-file-action="download" data-id="${f.id}">
            ⬇ Descargar
          </button>
          <button class="btn btn-secondary btn-sm" data-file-action="delete" data-id="${f.id}"
            style="color:#ef4444;margin-left:.25rem">
            🗑 Eliminar
          </button>
        </td>
      </tr>`;
  }

  async function downloadFile(file) {
    if (!file) return;
    try {
      const blob = await Api.get(`/files/${file.id}/download`, { binary: true });
      downloadBlob(blob, file.fileName);
      Toast.success('Descarga completada', file.fileName);
    } catch (err) {
      Toast.error('Error al descargar', err.message);
    }
  }

  async function deleteFile(id, fileName) {
    if (!confirm(`¿Eliminar "${fileName}"? Esta acción no se puede deshacer.`)) return;
    try {
      await Api.delete(`/files/${id}`);
      Toast.success('Archivo eliminado', fileName);
      await loadFiles();
    } catch (err) {
      Toast.error('Error al eliminar', err.message);
    }
  }

  function formatFileSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  return { render };
})();
