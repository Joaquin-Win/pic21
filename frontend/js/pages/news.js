/* ═══════════════════════════════════════════════════════
   PIC21 — News Page (noticias con reacciones y admin CRUD)
═══════════════════════════════════════════════════════ */

const NewsPage = (() => {
  let allNews = [];

  /* ── Render principal ─────────────────────────────── */
  function render(container) {
    const isAdmin = AuthService.isAdmin();

    container.innerHTML = `
      <div class="page-header">
        <div>
          <h2>Noticias</h2>
          <p>Novedades y noticias del PIC UES-SIGLO21</p>
        </div>
        ${isAdmin ? `
        <button class="btn btn-primary" id="btnNewNews">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          Nueva noticia
        </button>` : ''}
      </div>

      <div id="newsContainer">
        <div class="loading"><div class="spinner"></div></div>
      </div>`;

    if (isAdmin) {
      document.getElementById('btnNewNews')?.addEventListener('click', () => openFormModal(null));
    }

    loadNews();
  }

  /* ── Cargar noticias ──────────────────────────────── */
  async function loadNews() {
    try {
      allNews = await Api.get('/news');
      if (!Array.isArray(allNews)) allNews = [];
      renderGrid();
    } catch (err) {
      showEmpty('#newsContainer', 'Error al cargar noticias', err.message);
      Toast.error('Error', err.message);
    }
  }

  /* ── Grid de cards ────────────────────────────────── */
  function renderGrid() {
    const container = document.getElementById('newsContainer');
    if (!container) return;

    if (!allNews.length) {
      showEmpty(container, 'Sin noticias', 'No hay noticias publicadas todavía.');
      return;
    }

    const isAdmin = AuthService.isAdmin();
    container.innerHTML = `<div class="news-grid">${allNews.map(n => newsCard(n, isAdmin)).join('')}</div>`;

    // Eventos de reacción
    container.querySelectorAll('[data-react]').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const id     = btn.dataset.id;
        const action = btn.dataset.react;
        handleReaction(id, action);
      });
    });

    // Eventos admin
    if (isAdmin) {
      container.querySelectorAll('[data-news-action]').forEach(btn => {
        btn.addEventListener('click', (e) => {
          e.stopPropagation();
          const id      = btn.dataset.id;
          const action  = btn.dataset.newsAction;
          const item    = allNews.find(n => String(n.id) === String(id));
          if (action === 'edit')   openFormModal(item);
          if (action === 'delete') confirmDelete(id, item?.title);
        });
      });
    }

    // Click en card → abrir URL fuente
    container.querySelectorAll('.news-card[data-url]').forEach(card => {
      card.addEventListener('click', () => {
        const url = card.dataset.url;
        if (url) window.open(url, '_blank', 'noopener');
      });
    });
  }

  /* ── Card individual ──────────────────────────────── */
  function newsCard(n, isAdmin) {
    const hasImg   = n.imageUrl && n.imageUrl.trim();
    const pubDate  = n.publishedAt ? fmtDate(n.publishedAt) : fmtDate(n.createdAt);
    const reaction = n.userReaction; // 'LIKE', 'DISLIKE', null

    const likeCls    = reaction === 'LIKE'    ? 'reaction-btn active-like'    : 'reaction-btn';
    const dislikeCls = reaction === 'DISLIKE' ? 'reaction-btn active-dislike' : 'reaction-btn';

    const adminActions = isAdmin ? `
      <div class="news-admin-actions">
        <button class="btn btn-sm btn-secondary" data-news-action="edit"   data-id="${n.id}" title="Editar">✏️</button>
        <button class="btn btn-sm btn-danger"    data-news-action="delete" data-id="${n.id}" title="Eliminar">🗑️</button>
      </div>` : '';

    return `
      <article class="news-card${n.sourceUrl ? '' : ''}" data-url="${escHtml(n.sourceUrl || '')}" data-id="${n.id}">
        ${hasImg ? `
        <div class="news-img-wrap">
          <img src="${escHtml(n.imageUrl)}" alt="${escHtml(n.title)}" class="news-img" onerror="this.closest('.news-img-wrap').style.display='none'" loading="lazy" />
        </div>` : `
        <div class="news-img-wrap news-img-placeholder">
          <span style="font-size:2.5rem">📰</span>
        </div>`}

        <div class="news-body">
          <div class="news-meta">
            <span class="news-date">📅 ${pubDate}</span>
            ${adminActions}
          </div>

          <h3 class="news-title">${escHtml(n.title)}</h3>

          ${n.description ? `<p class="news-desc">${escHtml(n.description)}</p>` : ''}

          ${n.sourceUrl ? `
          <div class="news-source">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/><polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/></svg>
            Leer más
          </div>` : ''}

          <div class="news-reactions">
            <button class="${likeCls}" data-react="${reaction === 'LIKE' ? 'remove' : 'like'}" data-id="${n.id}" title="Me gusta">
              👍 <span>${n.likes}</span>
            </button>
            <button class="${dislikeCls}" data-react="${reaction === 'DISLIKE' ? 'remove' : 'dislike'}" data-id="${n.id}" title="No me gusta">
              👎 <span>${n.dislikes}</span>
            </button>
          </div>
        </div>
      </article>`;
  }

  /* ── Reacciones ───────────────────────────────────── */
  async function handleReaction(id, action) {
    try {
      let updated;
      if (action === 'like')    updated = await Api.post(`/news/${id}/like`);
      if (action === 'dislike') updated = await Api.post(`/news/${id}/dislike`);
      if (action === 'remove')  updated = await Api.delete(`/news/${id}/reaction`);
      if (!updated) return;

      // Actualizar en el array local y re-renderizar
      const idx = allNews.findIndex(n => String(n.id) === String(id));
      if (idx !== -1) allNews[idx] = updated;
      renderGrid();
    } catch (err) {
      Toast.error('Error', err.message);
    }
  }

  /* ── Modal crear/editar ───────────────────────────── */
  function openFormModal(item) {
    const isEdit = !!item;
    Modal.open(isEdit ? 'Editar noticia' : 'Nueva noticia', `
      <form id="newsForm" class="form-grid" style="gap:1rem">

        <div class="form-group" style="grid-column:1/-1">
          <label class="form-label">URL de la noticia *</label>
          <div style="display:flex;gap:.5rem">
            <input type="url" id="newsSourceUrl" class="form-control" placeholder="https://..." value="${escHtml(item?.sourceUrl || '')}" required style="flex:1" />
            <button type="button" class="btn btn-secondary" id="btnPreviewUrl" title="Auto-completar con OpenGraph">🔍 Preview</button>
          </div>
          <small style="color:var(--text-muted)">Pegá la URL y hacé click en 🔍 para auto-completar título e imagen</small>
        </div>

        <div class="form-group" style="grid-column:1/-1">
          <label class="form-label">Título *</label>
          <input type="text" id="newsTitle" class="form-control" placeholder="Título de la noticia" maxlength="500" value="${escHtml(item?.title || '')}" required />
        </div>

        <div class="form-group" style="grid-column:1/-1">
          <label class="form-label">Descripción</label>
          <textarea id="newsDesc" class="form-control" rows="3" maxlength="2000" placeholder="Resumen o descripción...">${escHtml(item?.description || '')}</textarea>
        </div>

        <div class="form-group" style="grid-column:1/-1">
          <label class="form-label">URL de imagen</label>
          <input type="url" id="newsImageUrl" class="form-control" placeholder="https://..." value="${escHtml(item?.imageUrl || '')}" />
          <div id="imgPreview" style="margin-top:.5rem"></div>
        </div>

        <div class="form-group">
          <label class="form-label">Fecha de publicación</label>
          <input type="datetime-local" id="newsPublishedAt" class="form-control" value="${item?.publishedAt ? item.publishedAt.slice(0,16) : ''}" />
        </div>

        <div style="grid-column:1/-1;display:flex;gap:.75rem;justify-content:flex-end;margin-top:.5rem">
          <button type="button" class="btn btn-secondary" id="btnCancelNews">Cancelar</button>
          <button type="submit" class="btn btn-primary" id="btnSaveNews">${isEdit ? 'Guardar cambios' : 'Publicar noticia'}</button>
        </div>
      </form>`
    );

    // Preview URL
    document.getElementById('btnPreviewUrl')?.addEventListener('click', previewUrl);

    // Preview imagen en tiempo real
    document.getElementById('newsImageUrl')?.addEventListener('input', (e) => {
      const prev = document.getElementById('imgPreview');
      if (prev) prev.innerHTML = e.target.value
        ? `<img src="${escHtml(e.target.value)}" style="max-height:100px;border-radius:6px;object-fit:cover;" onerror="this.style.display='none'" />`
        : '';
    });

    document.getElementById('btnCancelNews')?.addEventListener('click', Modal.close);

    document.getElementById('newsForm')?.addEventListener('submit', async (e) => {
      e.preventDefault();
      await saveNews(item?.id);
    });
  }

  /* ── Preview OpenGraph ────────────────────────────── */
  async function previewUrl() {
    const url = document.getElementById('newsSourceUrl')?.value?.trim();
    if (!url) { Toast.warn('Atención', 'Ingresá una URL primero'); return; }

    const btn = document.getElementById('btnPreviewUrl');
    if (btn) { btn.disabled = true; btn.textContent = '⏳'; }

    try {
      const data = await Api.post('/news/preview', { url });
      if (data?.title)    { const t = document.getElementById('newsTitle');    if (t && !t.value) t.value = data.title; }
      if (data?.description) { const d = document.getElementById('newsDesc'); if (d && !d.value) d.value = data.description; }
      if (data?.image)    {
        const img = document.getElementById('newsImageUrl');
        if (img && !img.value) {
          img.value = data.image;
          const prev = document.getElementById('imgPreview');
          if (prev) prev.innerHTML = `<img src="${escHtml(data.image)}" style="max-height:100px;border-radius:6px;object-fit:cover;" onerror="this.style.display='none'" />`;
        }
      }
      Toast.success('Éxito', 'Datos cargados desde la URL');
    } catch (err) {
      Toast.warn('Sin preview', 'No se pudo obtener datos de la URL. Completá manualmente.');
    } finally {
      if (btn) { btn.disabled = false; btn.innerHTML = '🔍 Preview'; }
    }
  }

  /* ── Guardar noticia ──────────────────────────────── */
  async function saveNews(id) {
    const btn = document.getElementById('btnSaveNews');
    if (btn) { btn.disabled = true; btn.textContent = 'Guardando…'; }

    const payload = {
      title:       document.getElementById('newsTitle')?.value?.trim(),
      description: document.getElementById('newsDesc')?.value?.trim() || null,
      imageUrl:    document.getElementById('newsImageUrl')?.value?.trim() || null,
      sourceUrl:   document.getElementById('newsSourceUrl')?.value?.trim(),
      publishedAt: document.getElementById('newsPublishedAt')?.value || null,
    };

    if (!payload.title)     { Toast.error('Error', 'El título es obligatorio'); if (btn) { btn.disabled = false; btn.textContent = id ? 'Guardar cambios' : 'Publicar noticia'; } return; }
    if (!payload.sourceUrl) { Toast.error('Error', 'La URL de origen es obligatoria'); if (btn) { btn.disabled = false; btn.textContent = id ? 'Guardar cambios' : 'Publicar noticia'; } return; }

    try {
      const saved = id
        ? await Api.put(`/news/${id}`, payload)
        : await Api.post('/news', payload);

      if (id) {
        const idx = allNews.findIndex(n => String(n.id) === String(id));
        if (idx !== -1) allNews[idx] = saved;
      } else {
        allNews.unshift(saved);
      }

      Modal.close();
      renderGrid();
      Toast.success('Éxito', id ? 'Noticia actualizada' : 'Noticia publicada');
    } catch (err) {
      Toast.error('Error', err.message);
      if (btn) { btn.disabled = false; btn.textContent = id ? 'Guardar cambios' : 'Publicar noticia'; }
    }
  }

  /* ── Eliminar noticia ─────────────────────────────── */
  function confirmDelete(id, title) {
    Modal.open('Eliminar noticia', `
      <div style="text-align:center;padding:1rem 0">
        <div style="font-size:3rem;margin-bottom:1rem">🗑️</div>
        <p>¿Eliminar la noticia <strong>${escHtml(title || '')}</strong>?</p>
        <p style="color:var(--text-muted);font-size:.9rem">Esta acción no se puede deshacer.</p>
        <div style="display:flex;gap:.75rem;justify-content:center;margin-top:1.5rem">
          <button class="btn btn-secondary" id="btnCancelDel">Cancelar</button>
          <button class="btn btn-danger"    id="btnConfirmDel">Sí, eliminar</button>
        </div>
      </div>`
    );
    document.getElementById('btnCancelDel')?.addEventListener('click', Modal.close);
    document.getElementById('btnConfirmDel')?.addEventListener('click', () => deleteNews(id));
  }

  async function deleteNews(id) {
    try {
      await Api.delete(`/news/${id}`);
      allNews = allNews.filter(n => String(n.id) !== String(id));
      Modal.close();
      renderGrid();
      Toast.success('Éxito', 'Noticia eliminada');
    } catch (err) {
      Toast.error('Error', err.message);
    }
  }

  /* ── Helpers ──────────────────────────────────────── */
  function fmtDate(dt) {
    if (!dt) return '';
    try {
      return new Date(dt).toLocaleDateString('es-AR', {
        day: '2-digit', month: 'short', year: 'numeric'
      });
    } catch { return dt; }
  }

  return { render };
})();
