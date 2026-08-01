/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.controller.AttendanceController
 *  com.pic21.dto.request.AttendanceRequest
 *  com.pic21.dto.response.AttendanceResponse
 *  com.pic21.service.AttendanceService
 *  com.pic21.service.ExcelExportService
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.MediaType
 *  org.springframework.http.ResponseEntity
 *  org.springframework.http.ResponseEntity$BodyBuilder
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.security.core.annotation.AuthenticationPrincipal
 *  org.springframework.security.core.userdetails.UserDetails
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package com.pic21.controller;

import com.pic21.dto.request.AttendanceRequest;
import com.pic21.dto.response.AttendanceResponse;
import com.pic21.service.AttendanceService;
import com.pic21.service.ExcelExportService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/attendances"})
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final ExcelExportService excelExportService;

    @PostMapping(value={"/meeting/{meetingId}/self"})
    @PreAuthorize(value="!hasAnyRole('R04_ADMIN','R05_DIRECTOR','R01_PROFESOR') and hasAnyRole('R02_ESTUDIANTE','R03_EGRESADO','R06_AYUDANTE','R07_ESTUDIANTE_POSGRADO')")
    public ResponseEntity<AttendanceResponse> registerSelf(@PathVariable Long meetingId, @RequestBody(required=false) AttendanceRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        AttendanceResponse response = this.attendanceService.registerSelf(meetingId, userDetails.getUsername(), request);
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CREATED).body((Object)response);
    }

    @GetMapping(value={"/meeting/{meetingId}"})
    @PreAuthorize(value="hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
    public ResponseEntity<List<AttendanceResponse>> findByMeeting(@PathVariable Long meetingId) {
        return ResponseEntity.ok((Object)this.attendanceService.findByReunion(meetingId));
    }

    @GetMapping(value={"/meeting/{meetingId}/excel"})
    @PreAuthorize(value="hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
    public ResponseEntity<byte[]> exportMeetingAttendancesToExcel(@PathVariable Long meetingId) {
        byte[] excelBytes = this.excelExportService.exportAttendanceByMeeting(meetingId);
        String filename = "asistencias_reunion_" + meetingId + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".xlsx";
        return ((ResponseEntity.BodyBuilder)ResponseEntity.ok().header("Content-Disposition", new String[]{"attachment; filename=\"" + filename + "\""})).contentType(MediaType.parseMediaType((String)"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).body((Object)excelBytes);
    }

    @GetMapping(value={"/excel"})
    @PreAuthorize(value="hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
    public ResponseEntity<byte[]> exportAllAttendancesToExcel() {
        byte[] excelBytes = this.excelExportService.exportAllAttendances();
        String filename = "asistencias_global_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".xlsx";
        return ((ResponseEntity.BodyBuilder)ResponseEntity.ok().header("Content-Disposition", new String[]{"attachment; filename=\"" + filename + "\""})).contentType(MediaType.parseMediaType((String)"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).body((Object)excelBytes);
    }

    public AttendanceController(AttendanceService attendanceService, ExcelExportService excelExportService) {
        this.attendanceService = attendanceService;
        this.excelExportService = excelExportService;
    }
}

