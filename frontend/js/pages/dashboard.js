/* ═══════════════════════════════════════════════════════
   PIC21 — Dashboard Page (Chart.js + métricas)
═══════════════════════════════════════════════════════ */

const DashboardPage = (() => {
  let barChart = null;
  let donutChart = null;

  function render(container) {
    container.innerHTML = `
      <div class="page-header">
        <div>
          <h2>Dashboard</h2>
          <p>Estadísticas del sistema PIC21</p>
        </div>
        <button class="btn btn-secondary btn-sm" onclick="DashboardPage.refresh()">
          🔄 Actualizar
        </button>
      </div>

      <!-- Stat cards -->
      <div class="stat-grid" id="statGrid">
        <div class="loading"><div class="spinner"></div></div>
      </div>

      <!-- Charts -->
      <div class="charts-grid">
        <div class="card">
          <div class="card-header"><span class="card-title">🎯 Asistencia por reunión (%)</span></div>
          <div class="card-body"><div class="chart-container"><canvas id="barChart"></canvas></div></div>
        </div>
        <div class="card">
          <div class="card-header"><span class="card-title">📊 Distribución de estados</span></div>
          <div class="card-body"><div class="chart-container"><canvas id="donutChart"></canvas></div></div>
        </div>
      </div>

      <!-- Meeting stats table -->
      <div class="card">
        <div class="card-header">
          <span class="card-title">📋 Detalle por reunión</span>
        </div>
        <div id="statsTable" class="card-body">
          <div class="loading"><div class="spinner"></div></div>
        </div>
      </div>`;

    loadData();
  }

  async function loadData() {
    try {
      const data = await Api.get('/dashboard');
      renderStats(data);
      renderCharts(data);
      renderTable(data);
    } catch (err) {
      Toast.error('Error al cargar dashboard', err.message);
      setHTML('#statGrid', `<p class="text-danger">Error: ${escHtml(err.message)}</p>`);
    }
  }

  function renderStats(d) {
    const rate = typeof d.globalAttendanceRate === 'number'
      ? d.globalAttendanceRate.toFixed(1) : '—';
    setHTML('#statGrid', `
      <div class="stat-card">
        <div class="stat-icon green">📅</div>
        <div><div class="stat-value">${d.totalMeetings ?? 0}</div><div class="stat-label">Reuniones totales</div></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon teal">👥</div>
        <div><div class="stat-value">${d.totalAttendances ?? 0}</div><div class="stat-label">Asistencias registradas</div></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon dark">📈</div>
        <div><div class="stat-value">${rate}%</div><div class="stat-label">Tasa global de asistencia</div></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon muted">🗂️</div>
        <div><div class="stat-value">${(d.meetingStats || []).filter(m => m.meetingStatus === 'ACTIVA').length}</div><div class="stat-label">Reuniones activas ahora</div></div>
      </div>
    `);
  }

  function renderCharts(d) {
    if (!window.Chart) return;
    const stats  = d.meetingStats || [];
    const labels = stats.map(s => truncate(s.meetingTitle, 20));
    const pcts   = stats.map(s => +(s.attendancePercentage || 0).toFixed(1));

    // Status counts for donut
    const statusCount = { NO_INICIADA: 0, ACTIVA: 0, BLOQUEADA: 0 };
    stats.forEach(s => { if (statusCount[s.meetingStatus] !== undefined) statusCount[s.meetingStatus]++; });

    // Destroy old charts
    barChart?.destroy();
    donutChart?.destroy();

    const isDark   = document.documentElement.getAttribute('data-theme') === 'dark';
    const gridColor = isDark ? 'rgba(255,255,255,.08)' : 'rgba(0,0,0,.06)';
    const textColor = isDark ? '#8ba89f' : '#6f7073';
    Chart.defaults.color = textColor;

    // Bar chart
    const barCtx = document.getElementById('barChart')?.getContext('2d');
    if (barCtx) {
      barChart = new Chart(barCtx, {
        type: 'bar',
        data: {
          labels,
          datasets: [{
            label: '% Asistencia',
            data: pcts,
            backgroundColor: pcts.map(p => p >= 70 ? 'rgba(1,169,143,.75)' : p >= 40 ? 'rgba(83,194,177,.75)' : 'rgba(239,68,68,.5)'),
            borderRadius: 6,
          }],
        },
        options: {
          responsive: true, maintainAspectRatio: false,
          plugins: { legend: { display: false } },
          scales: {
            y: { min: 0, max: 100, grid: { color: gridColor }, ticks: { callback: v => v + '%' } },
            x: { grid: { display: false } },
          },
        },
      });
    }

    // Donut chart
    const donutCtx = document.getElementById('donutChart')?.getContext('2d');
    if (donutCtx) {
      donutChart = new Chart(donutCtx, {
        type: 'doughnut',
        data: {
          labels: ['No iniciadas', 'Activas', 'Bloqueadas'],
          datasets: [{
            data: [statusCount.NO_INICIADA, statusCount.ACTIVA, statusCount.BLOQUEADA],
            backgroundColor: ['rgba(111,112,115,.6)', 'rgba(1,169,143,.75)', 'rgba(239,68,68,.65)'],
            borderWidth: 2,
            borderColor: isDark ? '#1a2e28' : '#fff',
          }],
        },
        options: {
          responsive: true, maintainAspectRatio: false,
          plugins: { legend: { position: 'bottom', labels: { padding: 16, boxWidth: 12 } } },
          cutout: '68%',
        },
      });
    }
  }

  function renderTable(d) {
    const stats = d.meetingStats || [];
    if (!stats.length) {
      showEmpty('#statsTable', 'Sin reuniones registradas', 'Creá una reunión para ver estadísticas');
      return;
    }
    setHTML('#statsTable', `
      <div class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Reunión</th><th>Estado</th><th>Asistentes</th><th>Total est.</th><th>% Asistencia</th>
            </tr>
          </thead>
          <tbody>
            ${stats.map(s => `
              <tr>
                <td><strong>${escHtml(s.meetingTitle)}</strong></td>
                <td>${statusBadge(s.meetingStatus)}</td>
                <td>${s.totalAttendances}</td>
                <td>${s.totalStudents}</td>
                <td>
                  <div style="display:flex;align-items:center;gap:.5rem;">
                    <div style="flex:1;height:6px;background:var(--border);border-radius:3px;overflow:hidden;">
                      <div style="width:${Math.min(s.attendancePercentage,100)}%;height:100%;background:var(--primary);border-radius:3px;"></div>
                    </div>
                    <span style="font-size:.8rem;font-weight:600;white-space:nowrap;">${(s.attendancePercentage||0).toFixed(1)}%</span>
                  </div>
                </td>
              </tr>`).join('')}
          </tbody>
        </table>
      </div>`);
  }

  function truncate(str, n) {
    return str && str.length > n ? str.slice(0, n) + '…' : str;
  }

  function refresh() {
    const container = document.getElementById('page-content');
    if (container) render(container);
  }

  return { render, refresh };
})();
