/* ═══════════════════════════════════════════════════════
   PIC UES-SIGLO21 — Login Page (con registro público)
   CAMBIOS:
     · Texto subtítulo actualizado a "Sistema de encuentro pic"
     · Botón "Registrarse" muestra pantalla intermedia de selección de tipo
     · Formulario de registro ajustado según tipo elegido
     · Estudiante: valida email @soysiglo.21.edu.ar
     · Egresado/Docente: campo DNI (8 dígitos), sin legajo
     · Contraseña: mínimo 1 mayúscula, 1 número, 1 símbolo
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
          <!-- [CAMBIADO] Subtítulo actualizado -->
          <p>Sistema de encuentro pic</p>
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
          <!-- [CAMBIADO] Al hacer clic muestra la pantalla intermedia de selección de tipo -->
          <button class="btn btn-secondary" id="btnShowRegister" style="width:100%;padding:.65rem;">
            📝 Registrarse como nuevo miembro
          </button>
        </div>
        <div class="auth-footer">
          Sistema PIC UES-SIGLO21 &mdash; Acceso restringido al personal autorizado
        </div>
      </div>`;

    document.getElementById('loginForm')?.addEventListener('submit', handleLogin);
    // [CAMBIADO] El botón de registro ahora va a la selección de tipo de usuario
    document.getElementById('btnShowRegister')?.addEventListener('click', () => renderUserTypeSelection());
    document.getElementById('loginEmail')?.focus();
  }

  async function handleLogin(e) {
    e.preventDefault();
    const email    = document.getElementById('loginEmail')?.value.toLowerCase().trim();
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

  // ── SELECCIÓN DE TIPO DE USUARIO (pantalla intermedia) ─────
  // [NUEVO] Esta función se muestra antes del formulario de registro.
  function renderUserTypeSelection() {
    _container.innerHTML = `
      <div class="auth-card">
        <div class="auth-logo">
          <img src="/img/logos21.png" alt="PIC UES-SIGLO21" class="auth-logo-img" style="width:72px;height:72px;border-radius:16px;object-fit:contain;" onerror="this.style.display='none'" />
          <h1>PIC UES-SIGLO21</h1>
          <p>Crear nueva cuenta</p>
        </div>

        <div style="margin-bottom:1.5rem;text-align:center;">
          <p style="font-size:1.05rem;font-weight:600;color:var(--text-primary);margin-bottom:1.25rem;">
            ¿Sos estudiante o egresado/docente?
          </p>

          <!-- Botón Estudiante -->
          <button id="btnTipoEstudiante"
            style="width:100%;padding:1rem 1.25rem;margin-bottom:.75rem;
                   background:linear-gradient(135deg,#01a98f,#016655);
                   color:#fff;border:none;border-radius:12px;font-size:1rem;
                   font-weight:600;cursor:pointer;transition:all .2s;
                   display:flex;align-items:center;gap:.75rem;">
            🎓&nbsp; Estudiante
          </button>

          <!-- Botón Egresado/Docente -->
          <button id="btnTipoEgresado"
            style="width:100%;padding:1rem 1.25rem;
                   background:linear-gradient(135deg,#3b82f6,#1d4ed8);
                   color:#fff;border:none;border-radius:12px;font-size:1rem;
                   font-weight:600;cursor:pointer;transition:all .2s;
                   display:flex;align-items:center;gap:.75rem;">
            🏛️&nbsp; Egresado / Docente
          </button>
        </div>

        <div style="text-align:center;margin-top:1rem;">
          <button class="btn btn-secondary" id="btnBackFromTypeSelection" style="width:100%;padding:.65rem;">
            ← Volver a iniciar sesión
          </button>
        </div>
        <div class="auth-footer">
          Seleccioná tu tipo de usuario para registrarte
        </div>
      </div>`;

    document.getElementById('btnTipoEstudiante')?.addEventListener('click', () => renderRegister('Estudiante'));
    document.getElementById('btnTipoEgresado')?.addEventListener('click', () => renderRegister('Egresado'));
    document.getElementById('btnBackFromTypeSelection')?.addEventListener('click', () => renderLogin());

    // Efectos hover en los botones de tipo
    ['btnTipoEstudiante', 'btnTipoEgresado'].forEach(id => {
      const btn = document.getElementById(id);
      if (!btn) return;
      btn.addEventListener('mouseenter', () => { btn.style.transform = 'translateY(-2px)'; btn.style.boxShadow = '0 6px 20px rgba(0,0,0,.2)'; });
      btn.addEventListener('mouseleave', () => { btn.style.transform = ''; btn.style.boxShadow = ''; });
    });
  }

  // ── REGISTRO PÚBLICO ───────────────────────────────────
  const CARRERAS_SIGLO21 = [
    'Abogacía', 'Contador Público', 'Licenciatura en Administración',
    'Licenciatura en Comercio Internacional', 'Licenciatura en Comunicación',
    'Licenciatura en Diseño Gráfico', 'Licenciatura en Educación',
    'Licenciatura en Gestión Ambiental', 'Licenciatura en Gestión de Recursos Humanos',
    'Licenciatura en Gestión Turística', 'Licenciatura en Informática',
    'Licenciatura en Marketing', 'Licenciatura en Psicología',
    'Licenciatura en Publicidad', 'Licenciatura en Relaciones Internacionales',
    'Licenciatura en Relaciones Públicas', 'Ingeniería en Software',
    'Ingeniería Industrial', 'Tecnicatura en Programación',
    'Tecnicatura en Desarrollo Web', 'Tecnicatura en Marketing Digital',
  ];

  /**
   * renderRegister recibe el tipo de usuario como parámetro.
   * Formulario adaptado según tipo: Estudiante o Egresado.
   * @param {string} tipoUsuario - 'Estudiante' o 'Egresado'
   */
  function renderRegister(tipoUsuario) {
    const esEstudiante = tipoUsuario === 'Estudiante';
    const labelTipo    = esEstudiante ? 'Estudiante' : 'Egresado/Docente';

    _container.innerHTML = `
      <div class="auth-card">
        <div class="auth-logo">
          <img src="/img/logos21.png" alt="PIC UES-SIGLO21" class="auth-logo-img" style="width:72px;height:72px;border-radius:16px;object-fit:contain;" onerror="this.style.display='none'" />
          <h1>PIC UES-SIGLO21</h1>
          <p>Registrar nueva cuenta &mdash; <strong>${labelTipo}</strong></p>
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

          <!-- Email: Estudiante requiere @soysiglo.21.edu.ar -->
          <div class="form-group">
            <label class="form-label">
              ${esEstudiante
                ? 'Correo SoySiglo * <small style="font-weight:400;color:#888;">(@soysiglo.21.edu.ar)</small>'
                : 'Correo electrónico *'}
            </label>
            <input class="form-control" id="regEmail" type="email"
                   placeholder="${esEstudiante ? 'usuario@soysiglo.21.edu.ar' : 'correo@ejemplo.com'}"
                   required />
          </div>

          <div class="form-group">
            <label class="form-label">Contraseña *</label>
            <input class="form-control" id="regPassword" type="password" placeholder="Mínimo 6 chars, 1 MAYÚSCULA, 1 número, 1 símbolo" required minlength="6" autocomplete="new-password" />
            <small style="color:#888;font-size:.78rem;">Debe incluir: 1 mayúscula, 1 número y 1 símbolo (@#$!. etc)</small>
          </div>
          <div class="form-group">
            <label class="form-label">Tipo de usuario *</label>
            <select class="form-control" id="regTipoUsuario" required>
              <option value="">Seleccioná...</option>
              <option value="Estudiante">Estudiante</option>
              <option value="Egresado">Egresado</option>
            </select>
          </div>

          <!-- Egresado / Profesor: elegir solo uno -->
          ${!esEstudiante ? `
          <div class="form-group">
            <label class="form-label">Sos egresado o profesor? * <small style="font-weight:400;color:#888;">(solo una opción)</small></label>
            <div style="display:flex;flex-direction:column;gap:.5rem;margin-top:.25rem;">
              <label style="display:flex;align-items:center;gap:.5rem;cursor:pointer;font-size:.95rem;">
                <input type="radio" name="regRolDocenteEgresado" value="R03_EGRESADO" checked style="width:18px;height:18px;" />
                Egresado
              </label>
              <label style="display:flex;align-items:center;gap:.5rem;cursor:pointer;font-size:.95rem;">
                <input type="radio" name="regRolDocenteEgresado" value="R01_PROFESOR" style="width:18px;height:18px;" />
                Profesor
              </label>
            </div>
          </div>` : ''}

          <!-- DNI solo para Egresado/Docente | Legajo solo para Estudiante -->
          ${esEstudiante ? `
          <div class="form-group">
            <label class="form-label">Legajo *</label>
            <input class="form-control" id="regLegajo" placeholder="Ej: 12345" required maxlength="20" />
          </div>` : `
          <div class="form-group">
            <label class="form-label">DNI * <small style="font-weight:400;color:#888;">(8 dígitos, sin puntos ni espacios)</small></label>
            <input class="form-control" id="regDni" placeholder="Ej: 12345678"
                   required maxlength="8" inputmode="numeric"
                   pattern="[0-9]{8}" title="El DNI debe tener exactamente 8 dígitos numéricos" />
          </div>`}

          <!-- Campo carrera solo para Estudiantes -->
          ${esEstudiante ? `
          <div class="form-group" id="regCarreraGroup">
            <label class="form-label">Carrera *</label>
            <select class="form-control" id="regCarrera">
              <option value="">Seleccioná tu carrera</option>
              ${CARRERAS_SIGLO21.map(c => `<option value="${c}">${c}</option>`).join('')}
              <option value="__otra__">Otra carrera...</option>
            </select>
          </div>
          <div class="form-group" id="regCarreraOtraGroup" style="display:none;">
            <label class="form-label">Especificar carrera *</label>
            <input class="form-control" id="regCarreraOtra" placeholder="Escribí tu carrera" maxlength="150" />
          </div>` : ''}

          <!-- Campo oculto con el tipo de usuario preseleccionado -->
          <input type="hidden" id="regTipoUsuario" value="${tipoUsuario}" />

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
          <!-- "Volver" regresa a la selección de tipo -->
          <button class="btn btn-secondary" id="btnBackToTypeSelection" style="width:100%;padding:.65rem;">
            ← Volver a selección de tipo
          </button>
        </div>
        <div class="auth-footer">
          Se creará una cuenta de tipo <strong>${labelTipo}</strong>
        </div>
      </div>`;

    document.getElementById('registerForm')?.addEventListener('submit', (e) => handleRegister(e, tipoUsuario));
    document.getElementById('btnBackToTypeSelection')?.addEventListener('click', () => renderUserTypeSelection());
    document.getElementById('openTerms')?.addEventListener('click', (e) => { e.preventDefault(); openTermsModal(); });
    document.getElementById('regFirstName')?.focus();

    // Toggle carrera "otra" (solo para Estudiante)
    if (esEstudiante) {
      const carreraSel       = document.getElementById('regCarrera');
      const carreraOtraGroup = document.getElementById('regCarreraOtraGroup');
      carreraSel?.addEventListener('change', () => {
        carreraOtraGroup.style.display = carreraSel.value === '__otra__' ? '' : 'none';
      });
    }
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

  /**
   * handleRegister recibe el tipo de usuario como parámetro.
   * @param {Event}  e           - Evento submit del formulario
   * @param {string} tipoUsuario - 'Estudiante' o 'Egresado'
   */
  async function handleRegister(e, tipoUsuario) {
    e.preventDefault();
    const btn    = document.getElementById('registerBtn');
    const errEl  = document.getElementById('registerError');
    const errMsg = document.getElementById('registerErrorMsg');
    const terms  = document.getElementById('regTerms');

    // Helper para mostrar error
    const showError = (msg) => {
      errMsg.textContent = msg;
      errEl.classList.remove('hidden');
    };

    // Validar checkbox de T&C
    if (!terms.checked) {
      showError('Debés aceptar los términos y condiciones para registrarte.');
      return;
    }

    const email = document.getElementById('regEmail').value.trim();
    const esEstudiante = tipoUsuario === 'Estudiante';

    // ── Validación email por tipo ─────────────────
    if (!email) {
      showError('El correo electrónico es obligatorio.');
      return;
    }
    if (esEstudiante && !email.endsWith('@soysiglo.21.edu.ar')) {
      showError('El correo debe ser de la institución (@soysiglo.21.edu.ar).');
      return;
    }

    // ── Validación de contraseña (ambos tipos) ──────────────
    const pwd = document.getElementById('regPassword').value;
    const pwd2 = document.getElementById('regPassword2')?.value || '';
    if (pwd !== pwd2) {
      showError('Las contraseñas no coinciden. Verificá que las hayas escrito igual las dos veces.');
      return;
    }
    if (!/[A-Z]/.test(pwd)) {
      showError('La contraseña debe incluir al menos 1 letra mayúscula.');
      return;
    }
    if (!/[0-9]/.test(pwd)) {
      showError('La contraseña debe incluir al menos 1 número.');
      return;
    }
    if (!/[@#$%^&+=!_.\-]/.test(pwd)) {
      showError('La contraseña debe incluir al menos 1 símbolo (@#$!. etc).');
      return;
    }

    // ── Validación DNI / Legajo ─────────────────────────────
    let legajoValue = '';
    if (esEstudiante) {
      legajoValue = document.getElementById('regLegajo')?.value.trim() || '';
      if (!legajoValue) {
        showError('El legajo es obligatorio.');
        return;
      }
    } else {
      const dniRaw = document.getElementById('regDni')?.value.trim() || '';
      if (!dniRaw) {
        showError('El DNI es obligatorio.');
        return;
      }
      if (!/^\d{8}$/.test(dniRaw)) {
        showError('El DNI debe tener exactamente 8 dígitos numéricos, sin puntos, comas ni espacios.');
        return;
      }
      // Guardamos el DNI en el campo legajo para compatibilidad con la BD
      legajoValue = dniRaw;
    }

    // Validar carrera si es estudiante
    let carrera = null;
    if (esEstudiante) {
      const carreraSel = document.getElementById('regCarrera')?.value;
      if (!carreraSel) {
        showError('Seleccioná tu carrera.');
        return;
      }
      carrera = carreraSel === '__otra__'
        ? document.getElementById('regCarreraOtra')?.value.trim()
        : carreraSel;
      if (!carrera) {
        showError('Escribí tu carrera.');
        return;
      }
    }

    btn.disabled = true;
    setHTML('#registerBtnText', '<span class="spinner" style="width:18px;height:18px;border-width:2px;display:inline-block;"></span> Registrando...');
    errEl.classList.add('hidden');

    let rol = 'R02_ESTUDIANTE';
    if (!esEstudiante) {
      const sel = document.querySelector('input[name="regRolDocenteEgresado"]:checked');
      rol = sel?.value || 'R03_EGRESADO';
    }

    const body = {
      nombre:      document.getElementById('regFirstName').value.trim(),
      apellido:    document.getElementById('regLastName').value.trim(),
      username:    email,
      email:       email,
      password:    document.getElementById('regPassword').value,
      rol:         rol,
      legajo:      legajoValue,
      carrera:     carrera,
      dni:         esEstudiante ? null : legajoValue,
    };

    try {
      await Api.post('/auth/register-public', body);
      Toast.success('✅ Cuenta creada', 'Usuario registrado exitosamente. Ya podés iniciar sesión.');
      renderLogin();
    } catch (err) {
      showError(err.message || 'Error al registrar');
    } finally {
      btn.disabled = false;
      setHTML('#registerBtnText', 'Crear cuenta');
    }
  }

  return { render };
})();
