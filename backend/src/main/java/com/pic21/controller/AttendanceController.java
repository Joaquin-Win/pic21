package com.pic21.controller;

import com.pic21.dto.response.AttendanceResponse;
import com.pic21.service.AttendanceService;
import com.pic21.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador de asistencias.
 *
 * Endpoints:
 *   POST /api/attendances/meeting/{meetingId}/self       → cualquier usuario autenticado
 *   GET  /api/attendances/meeting/{meetingId}            → ADMIN, PROFESOR, AYUDANTE
 *   GET  /api/attendances/meeting/{meetingId}/excel      → ADMIN, PROFESOR, AYUDANTE (descarga Excel)
 *   GET  /api/attendances/excel                          → ADMIN, PROFESOR, AYUDANTE (descarga Excel global)
 */
@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final ExcelExportService excelExportService;

    // -----------------------------------------------------------------------
    // POST /api/attendances/meeting/{meetingId}/self
    // Auto-registro del usuario autenticado
    // -----------------------------------------------------------------------

    /**
     * Registra la asistencia del usuario autenticado (auto-registro).
     * <p>
     * Reglas validadas en el servicio:
     * - La reunión debe estar ACTIVA.
     * - El usuario no puede registrarse dos veces en la misma reunión.
     * </p>
     *
     * @param meetingId   ID de la reunión
     * @param userDetails usuario autenticado inyectado por Spring Security
     * @return 201 Created con los datos de la asistencia registrada
     */
    @PostMapping("/meeting/{meetingId}/self")
    public ResponseEntity<AttendanceResponse> registerSelf(
            @PathVariable Long meetingId,
            @RequestBody(required = false) com.pic21.dto.request.AttendanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        AttendanceResponse response = attendanceService.registerSelf(
                meetingId, userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // -----------------------------------------------------------------------
    // GET /api/attendances/meeting/{meetingId}
    // Ver asistencias de una reunión — solo ADMIN, PROFESOR, AYUDANTE
    // -----------------------------------------------------------------------

    /**
     * Lista todas las asistencias registradas en una reunión.
     *
     * @param meetingId ID de la reunión
     * @return lista de asistencias ordenadas por fecha de registro
     */
    @GetMapping("/meeting/{meetingId}")
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR','AYUDANTE')")
    public ResponseEntity<List<AttendanceResponse>> findByMeeting(
            @PathVariable Long meetingId) {

        return ResponseEntity.ok(attendanceService.findByMeeting(meetingId));
    }

    // -----------------------------------------------------------------------
    // GET /api/attendances/meeting/{meetingId}/excel
    // Exportar asistencias de una reunión como Excel
    // -----------------------------------------------------------------------

    /**
     * Descarga un archivo Excel (.xlsx) con las asistencias de la reunión indicada.
     * El nombre del archivo incluye el ID de la reunión y la fecha de exportación.
     *
     * @param meetingId ID de la reunión
     * @return archivo .xlsx como descarga
     */
    @GetMapping("/meeting/{meetingId}/excel")
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR','AYUDANTE')")
    public ResponseEntity<byte[]> exportMeetingAttendancesToExcel(
            @PathVariable Long meetingId) {

        byte[] excelBytes = excelExportService.exportAttendanceByMeeting(meetingId);
        String filename = "asistencias_reunion_" + meetingId + "_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
                + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    // -----------------------------------------------------------------------
    // GET /api/attendances/excel
    // Exportar todas las asistencias (global) como Excel
    // -----------------------------------------------------------------------

    /**
     * Descarga un archivo Excel (.xlsx) con todas las asistencias del sistema,
     * organizadas en un libro con hoja de resumen y una hoja por reunión.
     *
     * @return archivo .xlsx como descarga
     */
    @GetMapping("/excel")
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR','AYUDANTE')")
    public ResponseEntity<byte[]> exportAllAttendancesToExcel() {

        byte[] excelBytes = excelExportService.exportAllAttendances();
        String filename = "asistencias_global_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
                + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
