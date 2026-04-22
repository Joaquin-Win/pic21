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
 * Controlador de asistencias (UML v8).
 */
@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final ExcelExportService excelExportService;

    @PostMapping("/meeting/{meetingId}/self")
    @PreAuthorize("!hasAnyRole('R04_ADMIN','R05_DIRECTOR','R01_PROFESOR') and hasAnyRole('R02_ESTUDIANTE','R03_EGRESADO','R06_AYUDANTE')")
    public ResponseEntity<AttendanceResponse> registerSelf(
            @PathVariable Long meetingId,
            @RequestBody(required = false) com.pic21.dto.request.AttendanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        AttendanceResponse response = attendanceService.registerSelf(
                meetingId, userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/meeting/{meetingId}")
    @PreAuthorize("hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
    public ResponseEntity<List<AttendanceResponse>> findByMeeting(@PathVariable Long meetingId) {
        return ResponseEntity.ok(attendanceService.findByReunion(meetingId));
    }

    @GetMapping("/meeting/{meetingId}/excel")
    @PreAuthorize("hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
    public ResponseEntity<byte[]> exportMeetingAttendancesToExcel(@PathVariable Long meetingId) {
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

    @GetMapping("/excel")
    @PreAuthorize("hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
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
