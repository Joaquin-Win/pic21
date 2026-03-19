$BASE = 'http://localhost:8080'
$results = [System.Collections.ArrayList]@()

function Add-Result($name, $exp, $got, $body) {
    $s = if ($got -eq $exp) { 'PASS' } else { 'FAIL' }
    $icon = if ($s -eq 'PASS') { '[OK]' } else { '[XX]' }
    Write-Host "$icon $name => Exp:$exp Got:$got"
    if ($s -eq 'FAIL') { Write-Host "     Detail: $($body -replace '\r?\n',' ' | Select-Object -First 1)" }
    [void]$results.Add([PSCustomObject]@{Test=$name; Exp=$exp; Got=$got; Status=$s})
}

function Req($method, $url, $body, $hdrs) {
    $p = @{ Uri = "$BASE$url"; Method = $method; UseBasicParsing = $true; TimeoutSec = 8 }
    if ($body) { $p.Body = $body; $p.ContentType = 'application/json' }
    if ($hdrs) { $p.Headers = $hdrs }
    try {
        $r = Invoke-WebRequest @p
        return @{ Code = [int]$r.StatusCode; Body = $r.Content }
    } catch {
        $code = 0
        if ($_.Exception.Response) { $code = [int]$_.Exception.Response.StatusCode.value__ }
        $bd = ''
        try { $bd = (New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())).ReadToEnd() } catch {}
        return @{ Code = $code; Body = $bd }
    }
}

function T($name, $method, $url, $body, $hdrs, $exp) {
    $r = Req $method $url $body $hdrs
    Add-Result $name $exp $r.Code $r.Body
}

# ─────────────────────────────────────────────────────────────────
Write-Host ''
Write-Host '=== FASE 1: AUTENTICACION ==='

T 'Login correcto admin'       POST '/api/auth/login' '{"username":"admin","password":"admin123"}' $null 200
T 'Login pass incorrecto'      POST '/api/auth/login' '{"username":"admin","password":"WRONG"}'    $null 401
T 'Sin token (GET meetings)'   GET  '/api/meetings'   $null $null 401
T 'Token invalido'             GET  '/api/meetings'   $null @{Authorization='Bearer faketoken.abc'} 401
T 'Login username vacio'       POST '/api/auth/login' '{"username":"","password":"x"}'             $null 400

# Obtener token admin
$r = Req POST '/api/auth/login' '{"username":"admin","password":"admin123"}' $null
$token_admin = ($r.Body | ConvertFrom-Json -ErrorAction SilentlyContinue).token
$aH = @{Authorization = "Bearer $token_admin"}
Write-Host "Token admin: $(if ($token_admin) { 'OK (' + $token_admin.Substring(0,20) + '...)' } else { 'FALLO' })"

# ─────────────────────────────────────────────────────────────────
Write-Host ''
Write-Host '=== REGISTRANDO USUARIOS DE PRUEBA ==='

$r = Req POST '/api/auth/register' '{"username":"profesor1","email":"prof1@test.com","password":"prof123","firstName":"Juan","lastName":"Perez","role":"PROFESOR"}' $aH
Write-Host "Registro profesor: $($r.Code)"

$r = Req POST '/api/auth/register' '{"username":"ayudante1","email":"ay1@test.com","password":"ay12345","firstName":"Ana","lastName":"Lopez","role":"AYUDANTE"}' $aH
Write-Host "Registro ayudante: $($r.Code)"

$r = Req POST '/api/auth/register' '{"username":"est1","email":"est1@test.com","password":"est12345","firstName":"Maria","lastName":"Garcia","role":"ESTUDIANTE"}' $aH
Write-Host "Registro estudiante 1: $($r.Code)"

$r = Req POST '/api/auth/register' '{"username":"est2","email":"est2@test.com","password":"est12345","firstName":"Pedro","lastName":"Silva","role":"ESTUDIANTE"}' $aH
Write-Host "Registro estudiante 2: $($r.Code)"

$r = Req POST '/api/auth/login' '{"username":"profesor1","password":"prof123"}' $null
$token_prof = ($r.Body | ConvertFrom-Json -ErrorAction SilentlyContinue).token
$pH = @{Authorization = "Bearer $token_prof"}
Write-Host "Token profesor: $(if ($token_prof) { 'OK' } else { 'FALLO' })"

$r = Req POST '/api/auth/login' '{"username":"ayudante1","password":"ay12345"}' $null
$token_ay = ($r.Body | ConvertFrom-Json -ErrorAction SilentlyContinue).token
$ayH = @{Authorization = "Bearer $token_ay"}
Write-Host "Token ayudante: $(if ($token_ay) { 'OK' } else { 'FALLO' })"

$r = Req POST '/api/auth/login' '{"username":"est1","password":"est12345"}' $null
$token_est = ($r.Body | ConvertFrom-Json -ErrorAction SilentlyContinue).token
$eH = @{Authorization = "Bearer $token_est"}
Write-Host "Token estudiante: $(if ($token_est) { 'OK' } else { 'FALLO' })"

# ─────────────────────────────────────────────────────────────────
Write-Host ''
Write-Host '=== FASE 2: ROLES Y SEGURIDAD ==='

T 'ADMIN crea reunion (permitido 201)'     POST '/api/meetings' '{"title":"Prueba Roles","scheduledAt":"2026-12-01T10:00:00","accessCode":"X1"}' $aH 201
T 'PROFESOR no puede crear reunion (403)'  POST '/api/meetings' '{"title":"t","scheduledAt":"2026-12-01T10:00:00"}' $pH 403
T 'AYUDANTE no puede crear reunion (403)'  POST '/api/meetings' '{"title":"t","scheduledAt":"2026-12-01T10:00:00"}' $ayH 403
T 'ESTUDIANTE no puede crear reunion (403)' POST '/api/meetings' '{"title":"hack","scheduledAt":"2026-12-01T10:00:00"}' $eH 403
T 'ESTUDIANTE puede VER reuniones (200)'   GET '/api/meetings' $null $eH 200
T 'ESTUDIANTE no puede exportar Excel (403)' GET '/api/attendances/excel' $null $eH 403
T 'ESTUDIANTE no puede ver Dashboard (403)' GET '/api/dashboard' $null $eH 403
T 'PROFESOR puede ver Dashboard (200)'     GET '/api/dashboard' $null $pH 200
T 'ADMIN puede ver Dashboard (200)'        GET '/api/dashboard' $null $aH 200

# ─────────────────────────────────────────────────────────────────
Write-Host ''
Write-Host '=== FASE 3: CRUD REUNIONES ==='

# Crear reunion principal
$rr = Req POST '/api/meetings' '{"title":"Reunion Principal QA","description":"Testing completo","scheduledAt":"2026-11-15T09:00:00","accessCode":"QA2026"}' $aH
$mid = ($rr.Body | ConvertFrom-Json -ErrorAction SilentlyContinue).id
Add-Result 'ADMIN crea reunion principal' 201 $rr.Code $rr.Body
Write-Host "Meeting ID: $mid"

T 'GET /meetings lista paginada'       GET  '/api/meetings'          $null $aH 200
T "GET /meetings/$mid detalle"         GET  "/api/meetings/$mid"      $null $aH 200
T 'GET /meetings/99999 (no existe)'    GET  '/api/meetings/99999'     $null $aH 404
T "PUT /meetings/$mid (editar activo)" PUT  "/api/meetings/$mid" '{"title":"Reunion QA Editada","description":"desc","scheduledAt":"2026-11-15T09:00:00"}' $aH 200

T "PATCH status NO_INICIADA->ACTIVA"   PATCH "/api/meetings/$mid/status" '{"status":"ACTIVA"}' $aH 200
T "PATCH status ACTIVA->NO_INICIADA (invalido)" PATCH "/api/meetings/$mid/status" '{"status":"NO_INICIADA"}' $aH 409
T "PATCH status ACTIVA->BLOQUEADA->luego check" PATCH "/api/meetings/$mid/status" '{"status":"BLOQUEADA"}' $aH 200
T 'Editar reunion BLOQUEADA (409)'     PUT  "/api/meetings/$mid" '{"title":"Hack","scheduledAt":"2026-11-15T09:00:00"}' $aH 409

# Volver a crear una nueva reunión ACTIVA para tests de asistencia
$rr2 = Req POST '/api/meetings' '{"title":"Reunion Asistencias","scheduledAt":"2026-11-20T10:00:00"}' $aH
$mid2 = ($rr2.Body | ConvertFrom-Json -ErrorAction SilentlyContinue).id
Write-Host "Meeting 2 ID: $mid2"
$rActivate = Req PATCH "/api/meetings/$mid2/status" '{"status":"ACTIVA"}' $aH
Write-Host "Meeting 2 activada: $($rActivate.Code)"

# ─────────────────────────────────────────────────────────────────
Write-Host ''
Write-Host '=== FASE 4: ASISTENCIAS ==='

T 'Admin asistencia en ACTIVA (201)'     POST "/api/attendances/meeting/$mid2/self" $null $aH 201
T 'Admin asistencia duplicada (409)'     POST "/api/attendances/meeting/$mid2/self" $null $aH 409
T 'Estudiante asistencia en ACTIVA (201)' POST "/api/attendances/meeting/$mid2/self" $null $eH 201
T 'Ver asistencias reunion ADMIN (200)'  GET  "/api/attendances/meeting/$mid2"      $null $aH 200
T 'Estudiante NO ve asistencias (403)'   GET  "/api/attendances/meeting/$mid2"       $null $eH 403
T 'Asistencia reunion inexistente (404)' POST "/api/attendances/meeting/99999/self"  $null $aH 404

# Test NO_INICIADA: crear reunion nueva y no activarla
$rr3 = Req POST '/api/meetings' '{"title":"Reunion Sin Activar","scheduledAt":"2026-12-25T10:00:00"}' $aH
$mid3 = ($rr3.Body | ConvertFrom-Json -ErrorAction SilentlyContinue).id
T 'Asistencia en NO_INICIADA (409)' POST "/api/attendances/meeting/$mid3/self" $null $aH 409

# Bloquear mid2 y probar asistencia
Req PATCH "/api/meetings/$mid2/status" '{"status":"BLOQUEADA"}' $aH | Out-Null
T 'Asistencia en BLOQUEADA (409)' POST "/api/attendances/meeting/$mid2/self" $null $aH 409

# ─────────────────────────────────────────────────────────────────
Write-Host ''
Write-Host '=== FASE 5: TAREAS ==='

# est2 no asistio a mid2 -> debe recibir tarea
$rt = Req POST "/api/tasks/meeting/$mid2" '{"title":"Tarea QA compensatoria","description":"Resumen de la clase","link":"http://drive.google.com/example"}' $pH
$createdTasks = ($rt.Body | ConvertFrom-Json -ErrorAction SilentlyContinue)
Add-Result 'PROFESOR crea tareas para ausentes (201)' 201 $rt.Code $rt.Body
if ($createdTasks -is [array]) { Write-Host "  -> $($createdTasks.Count) tarea(s) creada(s)" }

T 'AYUDANTE crea tarea (201/409)'      POST "/api/tasks/meeting/$mid2" '{"title":"Otra tarea","description":"Otra desc"}' $ayH 201
T 'ESTUDIANTE no puede crear tarea (403)' POST "/api/tasks/meeting/$mid2" '{"title":"Hack","description":"d"}' $eH 403
T 'ADMIN ver mis tareas (200)'         GET  '/api/tasks/my' $null $aH 200
T 'ESTUDIANTE ver sus tareas (200)'    GET  '/api/tasks/my' $null $eH 200
T 'Tarea reunion inexistente (404)'    POST '/api/tasks/meeting/99999' '{"title":"t","description":"d"}' $pH 404
T 'Tarea sin titulo (400)'             POST "/api/tasks/meeting/$mid2" '{"title":"","description":"d"}' $pH 400

# ─────────────────────────────────────────────────────────────────
Write-Host ''
Write-Host '=== FASE 6: EXPORTACION EXCEL ==='

$rxls = Req GET "/api/attendances/meeting/$mid2/excel" $null $aH
Add-Result 'Export Excel por reunion (200)' 200 $rxls.Code ''
if ($rxls.Code -eq 200) {
    $ct = 'unknown'
    try { $resp2 = Invoke-WebRequest -Uri "$BASE/api/attendances/meeting/$mid2/excel" -Headers $aH -UseBasicParsing -TimeoutSec 10; $ct = $resp2.Headers.'Content-Type' } catch {}
    Write-Host "  Content-Type: $ct"
    Write-Host "  Body bytes: $($rxls.Body.Length)"
}

$rxls2 = Req GET '/api/attendances/excel' $null $aH
Add-Result 'Export Excel global (200)' 200 $rxls2.Code ''
Write-Host "  Excel global bytes: $($rxls2.Body.Length)"

# ─────────────────────────────────────────────────────────────────
Write-Host ''
Write-Host '=== FASE 7: DASHBOARD ==='

$rd = Req GET '/api/dashboard' $null $aH
Add-Result 'Dashboard ADMIN (200)' 200 $rd.Code $rd.Body
if ($rd.Code -eq 200) {
    $dj = $rd.Body | ConvertFrom-Json -ErrorAction SilentlyContinue
    Write-Host "  totalMeetings: $($dj.totalMeetings)"
    Write-Host "  totalAttendances: $($dj.totalAttendances)"
    Write-Host "  globalAttendanceRate: $($dj.globalAttendanceRate)%"
    Write-Host "  meetingStats count: $($dj.meetingStats.Count)"
}

# ─────────────────────────────────────────────────────────────────
Write-Host ''
Write-Host '=== FASE 8: ERRORES Y BORDES ==='

T 'JSON malformado login (400)'          POST '/api/auth/login'  '{invalid json}'  $null 400
T 'Login usuario inexistente (401)'      POST '/api/auth/login'  '{"username":"noexiste","password":"x"}' $null 401
T 'Meeting title vacio (400)'            POST '/api/meetings'    '{"title":"","scheduledAt":"2026-12-01T10:00:00"}' $aH 400
T 'Meeting sin scheduledAt (400)'        POST '/api/meetings'    '{"title":"T"}' $aH 400
T 'Task title vacio (400)'              POST "/api/tasks/meeting/$mid2" '{"title":""}' $aH 400
T 'DELETE reunion inexistente (404)'     DELETE '/api/meetings/99999' $null $aH 404

# ─────────────────────────────────────────────────────────────────
Write-Host ''
Write-Host '======================================='
Write-Host '         RESUMEN FINAL QA - PIC21'
Write-Host '======================================='
$pass = ($results | Where-Object { $_.Status -eq 'PASS' }).Count
$fail = ($results | Where-Object { $_.Status -eq 'FAIL' }).Count
Write-Host "Total tests: $($results.Count) | PASS: $pass | FAIL: $fail"
Write-Host ''
$results | Format-Table Test, Exp, Got, Status -AutoSize

$failedTests = $results | Where-Object { $_.Status -eq 'FAIL' }
if ($failedTests.Count -gt 0) {
    Write-Host '--- TESTS FALLIDOS ---'
    $failedTests | Format-List Test, Exp, Got
}

$results | ConvertTo-Json -Depth 3 | Out-File 'C:\Users\winck\Desktop\asistencia\backend\qa_results.json' -Encoding UTF8
Write-Host 'Resultados guardados en qa_results.json'
