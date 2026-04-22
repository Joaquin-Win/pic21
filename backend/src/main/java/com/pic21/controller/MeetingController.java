package com.pic21.controller;

import com.pic21.domain.EstadoReunion;
import com.pic21.domain.Reunion;
import com.pic21.dto.request.MeetingRequest;
import com.pic21.dto.request.MeetingStatusRequest;
import com.pic21.dto.response.MeetingResponse;
import com.pic21.exception.BusinessException;
import com.pic21.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador REST para reuniones (UML v8).
 *
 * Roles UML → Spring Security:
 *   R04_ADMIN    → ROLE_R04_ADMIN
 *   R01_PROFESOR → ROLE_R01_PROFESOR
 *   R06_AYUDANTE → ROLE_R06_AYUDANTE
 */
@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @GetMapping
    public ResponseEntity<Page<MeetingResponse>> getAll(
            @PageableDefault(size = 50, sort = "fechaInicio") Pageable pageable) {
        return ResponseEntity.ok(meetingService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeetingResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(meetingService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
    public ResponseEntity<MeetingResponse> create(
            @Valid @RequestBody MeetingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        MeetingResponse response = meetingService.create(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
    public ResponseEntity<MeetingResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody MeetingRequest request) {
        return ResponseEntity.ok(meetingService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('R04_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        meetingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('R04_ADMIN')")
    public ResponseEntity<MeetingResponse> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody MeetingStatusRequest request,
            @AuthenticationPrincipal UserDetails me) {

        EstadoReunion newEstado = request.getEstado();
        boolean isAdmin = me.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_R04_ADMIN"));

        if (newEstado == EstadoReunion.BLOQUEADA
                && !me.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_R04_ADMIN"))) {
            throw new BusinessException("Solo ADMIN puede bloquear reuniones.");
        }

        return ResponseEntity.ok(meetingService.changeStatus(id, newEstado, isAdmin));
    }

    @PostMapping(value = "/{id}/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('R04_ADMIN')")
    public ResponseEntity<MeetingResponse> uploadPdf(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(meetingService.uploadPdf(id, file));
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        Reunion reunion = meetingService.getReunionWithPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + reunion.getPdfFileName() + "\"")
                .body(reunion.getPdfFileData());
    }
}
