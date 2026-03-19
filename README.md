# PIC21 — Sistema de Gestión de Reuniones y Asistencias

Sistema web para gestionar reuniones académicas, registrar asistencias, asignar tareas a estudiantes ausentes y exportar reportes en Excel.

---

## 🚀 Tecnologías

| Capa       | Tecnología                         |
|------------|------------------------------------|
| Backend    | Spring Boot 3.2 · Java 17          |
| Seguridad  | Spring Security · JWT (JJWT 0.12)  |
| Base de datos | H2 (dev) · PostgreSQL (prod)    |
| ORM        | Spring Data JPA · Hibernate        |
| Reportes   | Apache POI (Excel .xlsx)           |
| Frontend   | Vanilla JS · CSS · Chart.js        |

---

## 🔐 Roles y permisos

| Rol         | Crear reunión | Cambiar estado | Registrar asistencia | Crear tareas | Exportar Excel | Subir PDF |
|-------------|:---:|:---:|:---:|:---:|:---:|:---:|
| **ADMIN**   | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **PROFESOR**| ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **AYUDANTE**| ❌ | Activar solo | ✅ | ✅ | ✅ | ❌ |
| **ESTUDIANTE** | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |

---

## 💻 Arranque local (desarrollo)

### Requisitos
- Java 17+
- Maven 3.8+ (o usar tu IDE)

### 1. Clonar el repositorio
```bash
git clone https://github.com/TU_USUARIO/pic21.git
cd pic21
```

### 2. Levantar el backend
```bash
cd backend
mvn spring-boot:run
```
El backend queda disponible en `http://localhost:8080`

### 3. Abrir el frontend
Abrir `frontend/index.html` en un servidor local (Live Server, Python, etc.):
```bash
cd frontend
python -m http.server 3000
```
Luego navegar a `http://localhost:3000`

### 4. Usuarios de prueba (creados automáticamente)

| Rol         | Usuario     | Contraseña |
|-------------|-------------|------------|
| ADMIN       | `admin`     | `admin123` |
| PROFESOR    | `profesor`  | `prof123`  |
| AYUDANTE    | `ayudante`  | `ayu123`   |
| ESTUDIANTE  | `estudiante`| `est123`   |

> ⚠️ **Cambiar estas contraseñas antes de desplegar en producción.**

---

## 🐘 Despliegue en producción (PostgreSQL)

### Variables de entorno requeridas

| Variable              | Descripción                                    |
|-----------------------|------------------------------------------------|
| `SPRING_PROFILES_ACTIVE` | Fijar en `prod`                            |
| `DB_URL`              | JDBC URL de PostgreSQL (ej: `jdbc:postgresql://host:5432/pic21`) |
| `DB_USERNAME`         | Usuario de la base de datos                    |
| `DB_PASSWORD`         | Contraseña de la base de datos                 |
| `JWT_SECRET`          | Clave secreta JWT (mínimo 256 bits, aleatoria) |
| `PORT`                | Puerto del servidor (default: `8080`)          |

### Ejemplo con variables de entorno (Railway / Render / Fly.io)
```bash
SPRING_PROFILES_ACTIVE=prod \
DB_URL=jdbc:postgresql://db.example.com:5432/pic21 \
DB_USERNAME=pic21user \
DB_PASSWORD=supersecret \
JWT_SECRET=$(openssl rand -hex 64) \
java -jar backend/target/pic21-backend-0.0.1-SNAPSHOT.jar
```

### Primera vez en producción
1. En `application-prod.properties`, cambiar `ddl-auto=update` → esto crea las tablas automáticamente
2. Arrancar la aplicación → las tablas y usuarios por defecto se crean solos vía `DataInitializer`
3. Opcionalmente cambiar a `ddl-auto=validate` una vez creadas las tablas

---

## 📁 Estructura del proyecto

```
asistencia/
├── backend/                    # Spring Boot API
│   ├── src/main/java/com/pic21/
│   │   ├── config/             # DataInitializer, SecurityConfig, CorsConfig
│   │   ├── controller/         # REST controllers
│   │   ├── domain/             # Entidades JPA
│   │   ├── dto/                # Request/Response DTOs
│   │   ├── exception/          # GlobalExceptionHandler + custom exceptions
│   │   ├── repository/         # Spring Data repositories
│   │   ├── security/           # JWT filter y provider
│   │   └── service/            # Lógica de negocio
│   └── src/main/resources/
│       ├── application.properties       # Perfil dev (H2)
│       └── application-prod.properties  # Perfil producción (PostgreSQL)
└── frontend/                   # Vanilla JS SPA
    ├── index.html
    ├── css/                    # Estilos
    └── js/
        ├── config.js           # Configuración global (auto-detecta entorno)
        ├── api.js              # Fetch wrapper + JWT interceptor
        ├── auth.js             # AuthService
        ├── router.js           # Hash-based SPA router
        ├── components/         # Sidebar, Topbar, Modal
        └── pages/              # Login, Dashboard, Meetings, Tasks, etc.
```

---

## 🔒 Seguridad

- JWT con expiración de 8 horas
- BCrypt con factor 12 para contraseñas
- CORS configurado para aceptar solo orígenes localhost en dev
- Stacktrace nunca expuesto al cliente
- Roles verificados en cada endpoint vía `@PreAuthorize`
