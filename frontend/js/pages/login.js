/* ═══════════════════════════════════════════════════════
   PIC21 — Login Page (con registro público)
═══════════════════════════════════════════════════════ */

const LoginPage = (() => {
  let showingRegister = false;

  function render(container) {
    showingRegister = false;
    renderLogin(container);
  }

  // ── LOGIN ─────────────────────────────────────────────
  function renderLogin(container) {
    container.innerHTML = `
      <div class="auth-card">
        <div class="auth-logo">
          <div class="auth-logo-icon">P</div>
          <h1>PIC21</h1>
          <p>Sistema de Gestión de Reuniones y Asistencias</p>
        </div>
        <form class="auth-form" id="loginForm" autocomplete="on">
          <div class="form-group">
            <label class="form-label" for="username">Usuario</label>
            <input class="form-control" id="username" name="username"
                   type="text" placeholder="Ingresá tu usuario" autocomplete="username" required />
          </div>
          <div class="form-group">
            <label class="form-label" for="password">Contraseña</label>
            <input class="form-control" id="password" name="password"
                   type="password" placeholder="Ingresá tu contraseña" autocomplete="current-password" required />
          </div>
          <div id="loginError" class="auth-error hidden">
            <span>⚠️</span>
            <span id="loginErrorMsg">Error al iniciar sesión</span>
          </div>
          <button class="auth-submit" type="submit" id="loginBtn">
            <span id="loginBtnText">Iniciar sesión</span>
          </button>
        </form>
        <div style="text-align:center;margin-top:1rem;">
          <button class="btn btn-secondary" id="btnShowRegister" style="width:100%;padding:.65rem;">
            📝 Registrarse como nuevo miembro
          </button>
        </div>
        <div class="auth-footer">
          Sistema PIC21 &mdash; Acceso restringido al personal autorizado
        </div>
      </div>`;

    document.getElementById('loginForm')?.addEventListener('submit', handleLogin);
    document.getElementById('btnShowRegister')?.addEventListener('click', () => renderRegister(container));
    document.getElementById('username')?.focus();
  }

  async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('username')?.value.trim();
    const password = document.getElementById('password')?.value;
    const btn      = document.getElementById('loginBtn');
    const errEl    = document.getElementById('loginError');
    const errMsg   = document.getElementById('loginErrorMsg');

    if (!username || !password) return;

    btn.disabled = true;
    setHTML('#loginBtnText', '<span class="spinner" style="width:18px;height:18px;border-width:2px;display:inline-block;"></span> Ingresando...');
    errEl.classList.add('hidden');

    try {
      await AuthService.login(username, password);
      Toast.success('¡Bienvenido!', `Hola, ${AuthService.getDisplayName()}`);
      Router.navigate(Router.getDefaultRoute());
    } catch (err) {
      errMsg.textContent = err.message || 'Credenciales inválidas';
      errEl.classList.remove('hidden');
      document.getElementById('password').value = '';
      document.getElementById('password').focus();
    } finally {
      btn.disabled = false;
      setHTML('#loginBtnText', 'Iniciar sesión');
    }
  }

  // ── REGISTRO PÚBLICO ───────────────────────────────────
  function renderRegister(container) {
    container.innerHTML = `
      <div class="auth-card">
        <div class="auth-logo">
          <div class="auth-logo-icon">P</div>
          <h1>PIC21</h1>
          <p>Registrar nueva cuenta</p>
        </div>
        <form class="auth-form" id="registerForm" autocomplete="off">
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:.75rem;">
            <div class="form-group">
              <label class="form-label">Nombre *</label>
              <input class="form-control" id="regFirstName" placeholder="Nombre" required />
            </div>
            <div class="form-group">
              <label class="form-label">Apellido *</label>
              <input class="form-control" id="regLastName" placeholder="Apellido" required />
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">Usuario *</label>
            <input class="form-control" id="regUsername" placeholder="Nombre de usuario (mín. 3 caracteres)" required minlength="3" maxlength="50" />
          </div>
          <div class="form-group">
            <label class="form-label">Email institucional *</label>
            <input class="form-control" id="regEmail" type="email" placeholder="correo@universidad.edu" required />
          </div>
          <div class="form-group">
            <label class="form-label">Contraseña *</label>
            <input class="form-control" id="regPassword" type="password" placeholder="Mínimo 6 caracteres" required minlength="6" />
          </div>
          <div id="registerError" class="auth-error hidden">
            <span>⚠️</span>
            <span id="registerErrorMsg">Error</span>
          </div>
          <button class="auth-submit" type="submit" id="registerBtn">
            <span id="registerBtnText">Crear cuenta</span>
          </button>
        </form>
        <div style="text-align:center;margin-top:1rem;">
          <button class="btn btn-secondary" id="btnBackLogin" style="width:100%;padding:.65rem;">
            ← Volver a iniciar sesión
          </button>
        </div>
        <div class="auth-footer">
          Se creará una cuenta con rol <strong>Estudiante</strong>
        </div>
      </div>`;

    document.getElementById('registerForm')?.addEventListener('submit', handleRegister);
    document.getElementById('btnBackLogin')?.addEventListener('click', () => renderLogin(container));
    document.getElementById('regFirstName')?.focus();
  }

  async function handleRegister(e) {
    e.preventDefault();
    const btn    = document.getElementById('registerBtn');
    const errEl  = document.getElementById('registerError');
    const errMsg = document.getElementById('registerErrorMsg');

    btn.disabled = true;
    setHTML('#registerBtnText', '<span class="spinner" style="width:18px;height:18px;border-width:2px;display:inline-block;"></span> Registrando...');
    errEl.classList.add('hidden');

    const body = {
      firstName: document.getElementById('regFirstName').value.trim(),
      lastName:  document.getElementById('regLastName').value.trim(),
      username:  document.getElementById('regUsername').value.trim(),
      email:     document.getElementById('regEmail').value.trim(),
      password:  document.getElementById('regPassword').value,
    };

    try {
      const resp = await fetch(`${PIC21_CONFIG.API_URL}/auth/register-public`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
      const data = await resp.json();
      if (!resp.ok) {
        throw new Error(data.message || data.error || 'Error al registrar');
      }
      Toast.success('✅ Cuenta creada', `Usuario "${data.username}" registrado. Ya podés iniciar sesión.`);
      renderLogin(container);
    } catch (err) {
      errMsg.textContent = err.message;
      errEl.classList.remove('hidden');
    } finally {
      btn.disabled = false;
      setHTML('#registerBtnText', 'Crear cuenta');
    }
  }

  return { render };
})();
