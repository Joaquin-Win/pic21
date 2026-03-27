/* ═══════════════════════════════════════════════════════
   PIC UES-SIGLO21 — Login Page (con registro público)
═══════════════════════════════════════════════════════ */

const LoginPage = (() => {
  // Variable de módulo para evitar "container is not defined"
  let _container = null;

  function render(container) {
    _container = container;
    renderLogin();
  }

  // ── LOGIN ─────────────────────────────────────────────
  function renderLogin() {
    _container.innerHTML = `
      <div class="auth-card">
        <div class="auth-logo">
          <img src="/img/logos21.png" alt="PIC UES-SIGLO21" class="auth-logo-img" style="width:72px;height:72px;border-radius:16px;object-fit:contain;" onerror="this.style.display='none'" />
          <h1>PIC UES-SIGLO21</h1>
          <p>Sistema de Gestión de Reuniones y Asistencias</p>
        </div>
        <form class="auth-form" id="loginForm" autocomplete="on">
          <div class="form-group">
            <label class="form-label" for="loginEmail">Correo SoySiglo / Correo electrónico</label>
            <input class="form-control" id="loginEmail" name="email"
                   type="email" placeholder="Ingresá tu correo electrónico" autocomplete="email" required />
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
          Sistema PIC UES-SIGLO21 &mdash; Acceso restringido al personal autorizado
        </div>
      </div>`;

    document.getElementById('loginForm')?.addEventListener('submit', handleLogin);
    document.getElementById('btnShowRegister')?.addEventListener('click', () => renderRegister());
    document.getElementById('loginEmail')?.focus();
  }

  async function handleLogin(e) {
    e.preventDefault();
    const email    = document.getElementById('loginEmail')?.value.trim();
    const password = document.getElementById('password')?.value;
    const btn      = document.getElementById('loginBtn');
    const errEl    = document.getElementById('loginError');
    const errMsg   = document.getElementById('loginErrorMsg');

    if (!email || !password) return;

    btn.disabled = true;
    setHTML('#loginBtnText', '<span class="spinner" style="width:18px;height:18px;border-width:2px;display:inline-block;"></span> Ingresando...');
    errEl.classList.add('hidden');

    try {
      await AuthService.login(email, password);
      Toast.success('¡Bienvenido!', `Hola, ${AuthService.getDisplayName()}`);
      Router.navigate(Router.getDefaultRoute());
    } catch (err) {
      errMsg.textContent = 'Correo o contraseña incorrecta';
      errEl.classList.remove('hidden');
      document.getElementById('password').value = '';
      document.getElementById('password').focus();
    } finally {
      btn.disabled = false;
      setHTML('#loginBtnText', 'Iniciar sesión');
    }
  }

  // ── REGISTRO PÚBLICO ───────────────────────────────────
  function renderRegister() {
    _container.innerHTML = `
      <div class="auth-card">
        <div class="auth-logo">
          <img src="/img/logos21.png" alt="PIC UES-SIGLO21" class="auth-logo-img" style="width:72px;height:72px;border-radius:16px;object-fit:contain;" onerror="this.style.display='none'" />
          <h1>PIC UES-SIGLO21</h1>
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
            <label class="form-label">Correo SoySiglo / Correo electrónico *</label>
            <input class="form-control" id="regEmail" type="email" placeholder="correo@universidad.edu" required />
          </div>
          <div class="form-group">
            <label class="form-label">Contraseña *</label>
            <input class="form-control" id="regPassword" type="password" placeholder="Mínimo 6 caracteres" required minlength="6" />
          </div>

          <!-- Checkbox Términos y Condiciones -->
          <div class="form-group" style="display:flex;align-items:flex-start;gap:.5rem;margin-top:.25rem;">
            <input type="checkbox" id="regTerms" style="margin-top:3px;width:18px;height:18px;cursor:pointer;" />
            <label for="regTerms" style="font-size:.85rem;cursor:pointer;line-height:1.4;">
              Acepto los <a href="#" id="openTerms" style="color:var(--primary);text-decoration:underline;font-weight:500;">términos y condiciones</a>
            </label>
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
    document.getElementById('btnBackLogin')?.addEventListener('click', () => renderLogin());
    document.getElementById('openTerms')?.addEventListener('click', (e) => { e.preventDefault(); openTermsModal(); });
    document.getElementById('regFirstName')?.focus();
  }

  // ── MODAL TÉRMINOS Y CONDICIONES ───────────────────────
  function openTermsModal() {
    Modal.open('Términos y Condiciones', `
      <div style="max-height:400px;overflow-y:auto;font-size:.9rem;line-height:1.6;padding-right:.5rem;">
        <h4 style="margin-bottom:.5rem;">1. Uso del Sistema</h4>
        <p>El sistema PIC UES-SIGLO21 es una herramienta de gestión académica destinada exclusivamente al registro de asistencias, reuniones y tareas vinculadas a las actividades del programa. Su uso está limitado a fines educativos y organizativos.</p>

        <h4 style="margin-top:1rem;margin-bottom:.5rem;">2. Privacidad de Datos</h4>
        <p>Los datos personales proporcionados (nombre, apellido, correo electrónico, legajo y carrera) serán utilizados únicamente para la gestión interna del sistema. No se compartirán con terceros sin consentimiento previo del usuario, salvo cuando sea requerido por la institución para fines académicos.</p>

        <h4 style="margin-top:1rem;margin-bottom:.5rem;">3. Responsabilidad del Usuario</h4>
        <p>Cada usuario es responsable de la veracidad de la información que ingresa en el sistema. El uso indebido, la suplantación de identidad o el registro de asistencias falsas podrá conllevar sanciones académicas conforme al reglamento institucional.</p>

        <h4 style="margin-top:1rem;margin-bottom:.5rem;">4. Seguridad de Credenciales</h4>
        <p>El usuario se compromete a mantener la confidencialidad de sus credenciales de acceso (usuario y contraseña). Se recomienda enfáticamente no utilizar la misma contraseña que la cuenta institucional de Universidad Siglo 21. El sistema no se hace responsable por accesos no autorizados derivados de contraseñas compartidas o débiles.</p>

        <h4 style="margin-top:1rem;margin-bottom:.5rem;">5. Uso Académico</h4>
        <p>Este sistema ha sido desarrollado con fines estrictamente académicos en el marco del Proyecto Integrador Comunitario (PIC) de la Universidad Empresarial Siglo 21. Los datos almacenados se utilizan para garantizar la trazabilidad y transparencia de las actividades del programa.</p>

        <h4 style="margin-top:1rem;margin-bottom:.5rem;">6. Modificaciones</h4>
        <p>Los administradores del sistema se reservan el derecho de modificar estos términos y condiciones en cualquier momento. Los cambios serán comunicados a los usuarios a través del sistema.</p>

        <h4 style="margin-top:1rem;margin-bottom:.5rem;">7. Descargo de Responsabilidad</h4>
        <p style="font-weight:500;">Los creadores y administradores del sistema no se hacen responsables por filtraciones de datos, accesos indebidos o uso incorrecto de la información por parte de los usuarios. Cada usuario es responsable de proteger sus credenciales y de utilizar el sistema de manera adecuada conforme a estos términos.</p>
      </div>
      <div class="form-actions" style="margin-top:1rem;">
        <button type="button" class="btn btn-primary" onclick="Modal.close()" style="width:100%;">Entendido</button>
      </div>`);
  }

  async function handleRegister(e) {
    e.preventDefault();
    const btn    = document.getElementById('registerBtn');
    const errEl  = document.getElementById('registerError');
    const errMsg = document.getElementById('registerErrorMsg');
    const terms  = document.getElementById('regTerms');

    // Validar checkbox de T&C
    if (!terms.checked) {
      errMsg.textContent = 'Debés aceptar los términos y condiciones para registrarte.';
      errEl.classList.remove('hidden');
      return;
    }

    btn.disabled = true;
    setHTML('#registerBtnText', '<span class="spinner" style="width:18px;height:18px;border-width:2px;display:inline-block;"></span> Registrando...');
    errEl.classList.add('hidden');

    const email = document.getElementById('regEmail').value.trim();

    const body = {
      firstName: document.getElementById('regFirstName').value.trim(),
      lastName:  document.getElementById('regLastName').value.trim(),
      username:  email,  // auto-generar username desde el email
      email:     email,
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
      Toast.success('✅ Cuenta creada', 'Usuario registrado exitosamente. Ya podés iniciar sesión.');
      renderLogin();  // usa _container (variable de módulo), NO "container"
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
