package com.pic21.controller;

import com.pic21.domain.MeetingFile;
import com.pic21.dto.response.MeetingFileResponse;
import com.pic21.service.MeetingFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador para gestión de archivos PDF en reuniones.
 *
 * POST   /api/meetings/{id}/files        → Subir uno o varios PDFs (ADMIN, PROFESOR)
 * GET    /api/meetings/{id}/files        → Listar archivos de una reunión (autenticado)
 * GET    /api/files/{fileId}/download    → Descargar archivo (autenticado)
 * DELETE /api/files/{fileId}             → Eliminar archivo (ADMIN)
 * GET    /api/files                      → Listar TODOS los archivos (ADMIN)
 */
@RestController
@RequiredArgsConstructor
public class MeetingFileController {

    private final MeetingFileService fileService;

    // ── Subir PDFs a una reunión ───────────────────────────
    @PostMapping("/api/meetings/{meetingId}/files")
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR')")
    public ResponseEntity<List<MeetingFileResponse>> upload(
            @PathVariable Long meetingId,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<MeetingFileResponse> result = fileService.uploadFiles(meetingId, files, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // ── Listar archivos de una reunión ─────────────────────
    @GetMapping("/api/meetings/{meetingId}/files")
    public ResponseEntity<List<MeetingFileResponse>> listByMeeting(@PathVariable Long meetingId) {
        return ResponseEntity.ok(fileService.listByMeeting(meetingId));
    }

    // ── Descargar un archivo ───────────────────────────────
    @GetMapping("/api/files/{fileId}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long fileId) {
        MeetingFile file = fileService.getFileForDownload(fileId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", file.getFileName());
        headers.setContentLength(file.getFileData().length);

        return new ResponseEntity<>(file.getFileData(), headers, HttpStatus.OK);
    }

    // ── Eliminar un archivo ────────────────────────────────
    @DeleteMapping("/api/files/{fileId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    // ── Listar TODOS los archivos (vista ADMIN) ───────────
    @GetMapping("/api/files")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MeetingFileResponse>> listAll() {
        return ResponseEntity.ok(fileService.listAll());
    }
}
