package com.pic21.controller;

import com.pic21.domain.Meeting;
import com.pic21.domain.MeetingStatus;
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
 * Controlador REST para la gestión de reuniones.
 *
 * IMPORTANTE: @PreAuthorize está AQUÍ (no en el service) para evitar el bug
 * de Spring AOP donde @PreAuthorize + @Transactional apilados en el mismo
 * método envuelven BusinessException en IllegalArgumentException → HTTP 500.
 */
@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    // -----------------------------------------------------------------------
    // GET /api/meetings  — listar paginado (cualquier usuario autenticado)
    // -----------------------------------------------------------------------
    @GetMapping
    public ResponseEntity<Page<MeetingResponse>> getAll(
            @PageableDefault(size = 50, sort = "scheduledAt") Pageable pageable) {
        return ResponseEntity.ok(meetingService.findAll(pageable));
    }

    // -----------------------------------------------------------------------
    // GET /api/meetings/{id}  — detalle (cualquier usuario autenticado)
    // -----------------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<MeetingResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(meetingService.findById(id));
    }

    // -----------------------------------------------------------------------
    // POST /api/meetings  — crear (solo ADMIN y PROFESOR)
    // -----------------------------------------------------------------------
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    public ResponseEntity<MeetingResponse> create(
            @Valid @RequestBody MeetingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        MeetingResponse response = meetingService.create(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // -----------------------------------------------------------------------
    // PUT /api/meetings/{id}  — editar (solo ADMIN y PROFESOR; no BLOQUEADA)
    // -----------------------------------------------------------------------
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    public ResponseEntity<MeetingResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody MeetingRequest request) {
        return ResponseEntity.ok(meetingService.update(id, request));
    }

    // -----------------------------------------------------------------------
    // DELETE /api/meetings/{id}  — eliminar (solo ADMIN)
    // -----------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        meetingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -----------------------------------------------------------------------
    // PATCH /api/meetings/{id}/status  — cambiar estado
    // Activar (NO_INICIADA→ACTIVA): ADMIN, PROFESOR, AYUDANTE
    // Bloquear (ACTIVA→BLOQUEADA): ADMIN, PROFESOR, AYUDANTE
    // Desbloquear (BLOQUEADA→ACTIVA): solo ADMIN
    // -----------------------------------------------------------------------
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR','AYUDANTE')")
    public ResponseEntity<MeetingResponse> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody MeetingStatusRequest request,
            @AuthenticationPrincipal UserDetails me) {

        MeetingStatus newStatus = request.getStatus();
        boolean isAdmin = me.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Bloquear: solo ADMIN y PROFESOR
        if (newStatus == MeetingStatus.BLOQUEADA
                && !me.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                                    || a.getAuthority().equals("ROLE_PROFESOR"))) {
            throw new BusinessException("Solo ADMIN o PROFESOR puede bloquear reuniones.");
        }

        return ResponseEntity.ok(meetingService.changeStatus(id, newStatus, isAdmin));
    }

    // -----------------------------------------------------------------------
    // POST /api/meetings/{id}/pdf  — subir PDF (ADMIN y PROFESOR)
    // -----------------------------------------------------------------------
    @PostMapping(value = "/{id}/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    public ResponseEntity<MeetingResponse> uploadPdf(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(meetingService.uploadPdf(id, file));
    }

    // -----------------------------------------------------------------------
    // GET /api/meetings/{id}/pdf  — descargar PDF (cualquier autenticado)
    // -----------------------------------------------------------------------
    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        Meeting meeting = meetingService.getMeetingWithPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + meeting.getPdfFileName() + "\"")
                .body(meeting.getPdfFileData());
    }
}
