# PIC21 — Checklist de Desarrollo

## Fase 0: Diseño
- [x] Arquitectura inicial del sistema
- [x] Ajustes de diseño (v1.1): roles, estados, accessCode, validación asistencia

## Fase 1: Setup Inicial del Backend
- [x] pom.xml con dependencias (Web, JPA, Security, H2, Lombok, Validation, JWT)
- [x] Clase principal [Pic21Application.java](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/Pic21Application.java)
- [x] [application.properties](file:///b:/java/asistencias%20siglo%2021/backend/src/main/resources/application.properties) configurado para H2
- [x] [SecurityConfig.java](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/config/SecurityConfig.java) básica (sin lógica JWT completa)
- [x] [CorsConfig.java](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/config/CorsConfig.java)
- [x] [GlobalExceptionHandler.java](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/exception/GlobalExceptionHandler.java) básico
- [x] Estructura de paquetes completa (domain, repository, service, controller, dto, security, exception)
- [x] [data.sql](file:///b:/java/asistencias%20siglo%2021/backend/src/main/resources/data.sql) con roles iniciales y usuario admin
- [x] ✅ `mvn compile` — BUILD SUCCESS

## Fase 2: Dominio y Persistencia
- [x] Entidades JPA: [Role](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/domain/Role.java#11-45), [User](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/domain/User.java#14-58)
- [x] Repositorios: [RoleRepository](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/repository/RoleRepository.java#9-16), [UserRepository](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/repository/UserRepository.java#9-20)
- [x] [DataInitializer.java](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/config/DataInitializer.java) — crea roles + admin vía `ApplicationRunner`

## Fase 3: Autenticación JWT
- [x] [JwtTokenProvider.java](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/security/JwtTokenProvider.java) (jjwt 0.12.x, HS256)
- [x] [JwtAuthenticationFilter.java](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/security/JwtAuthenticationFilter.java)
- [x] [UserDetailsServiceImpl.java](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/security/UserDetailsServiceImpl.java)
- [x] [AuthService.java](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/service/AuthService.java) (login + register con validación)
- [x] [AuthController.java](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/controller/AuthController.java) — POST /login público, POST /register ADMIN
- [x] DTOs: [LoginRequest](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/dto/request/LoginRequest.java#10-20), [RegisterRequest](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/dto/request/RegisterRequest.java#14-39), [AuthResponse](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/dto/response/AuthResponse.java#12-25), [UserResponse](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/dto/response/UserResponse.java#12-25)
- [x] [SecurityConfig.java](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/config/SecurityConfig.java) actualizada con filtro JWT + @Qualifier fix
- [x] ✅ Login real verificado — token JWT generado: `eyJhbGciOiJIU...`
- [x] ✅ Credenciales inválidas rechazadas correctamente por Spring Security

## Fase 4: CRUD de Reuniones
- [x] [MeetingService.java](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/service/MeetingService.java) con transición de estados (NO_INICIADA→ACTIVA→BLOQUEADA)
- [x] [MeetingController.java](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/controller/MeetingController.java) — GET/POST/PUT/DELETE/PATCH
- [x] DTOs de reunión
- [x] Validación de estados y edición de reuniones BLOQUEADAS
- [x] ✅ Tests verificados: 409/404/401 todos correctos
- [x] Bug AOP fix: [GlobalExceptionHandler](file:///b:/java/asistencias%20siglo%2021/backend/src/main/java/com/pic21/exception/GlobalExceptionHandler.java#24-157) con cause chain walking

## Fase 5: Gestión de Asistencias
- [/] Entidad `Attendance` con restricción única (meeting_id, user_id)
- [/] `AttendanceRepository.java`
- [/] DTOs de asistencia
- [/] `AttendanceService.java` con reglas de negocio
- [/] `AttendanceController.java` — POST /self, GET /meeting/{id}

## Fase 6: Gestión de Tareas
- [x] `TaskStatus.java` (enum: PENDING, IN_PROGRESS, DONE, CANCELLED)
- [x] `Task.java` (entidad: meeting, title, description, link, assignedTo, createdBy, status)
- [x] `TaskRepository.java` (findByMeetingId, findByAssignedToId con JOIN FETCH, existsByMeeting...)
- [x] DTOs: `TaskRequest.java` (título, descripción, link) + `TaskResponse.java`
- [x] `TaskService.java` — createForAbsent (asigna a ausentes, valida estado reunión, evita duplicados)
- [x] `TaskController.java` — POST /meeting/{meetingId}, GET /my

## Fase 7: Frontend
- [ ] Estructura HTML/CSS/JS + Bootstrap
- [ ] Login y autenticación por JWT
- [ ] Panel por rol (ADMIN, PROFESOR/AYUDANTE, ESTUDIANTE)
- [ ] Vista de reuniones
- [ ] Vista de asistencias

## Fase 7.5: Exportación Excel y Dashboard (Backend)
- [x] Apache POI 5.2.5 agregado a `pom.xml`
- [x] `ExcelExportService.java` — exportByMeeting (1 hoja) + exportAll (multihoja con resumen)
- [x] `DashboardResponse.java` — totalMeetings, totalAttendances, globalRate, MeetingStats[]
- [x] `DashboardService.java` — calcula stats por reunión y % global
- [x] `DashboardController.java` — GET /api/dashboard
- [x] `AttendanceController.java` actualizado — GET /meeting/{id}/excel + GET /excel

## Fase 8: Verificación
- [ ] Test de endpoints con H2
- [ ] Validar reglas de negocio (unicidad, estado ACTIVA, auto-registro)
- [ ] Revisión de seguridad

