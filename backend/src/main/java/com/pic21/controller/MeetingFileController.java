package com.pic21.controller;

import com.pic21.domain.ArchivoReunion;
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
 * Controlador para gestión de archivos PDF en reuniones (UML v8).
 */
@RestController
@RequiredArgsConstructor
public class MeetingFileController {

    private final MeetingFileService fileService;

    @PostMapping("/api/meetings/{meetingId}/files")
    @PreAuthorize("hasAnyRole('R04_ADMIN','R01_PROFESOR')")
    public ResponseEntity<List<MeetingFileResponse>> upload(
            @PathVariable Long meetingId,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<MeetingFileResponse> result = fileService.uploadFiles(meetingId, files, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/api/meetings/{meetingId}/files")
    public ResponseEntity<List<MeetingFileResponse>> listByMeeting(@PathVariable Long meetingId) {
        return ResponseEntity.ok(fileService.listByMeeting(meetingId));
    }

    @GetMapping("/api/files/{fileId}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long fileId) {
        ArchivoReunion file = fileService.getFileForDownload(fileId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", file.getFileName());
        headers.setContentLength(file.getFileData().length);
        return new ResponseEntity<>(file.getFileData(), headers, HttpStatus.OK);
    }

    @DeleteMapping("/api/files/{fileId}")
    @PreAuthorize("hasRole('R04_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/files")
    @PreAuthorize("hasRole('R04_ADMIN')")
    public ResponseEntity<List<MeetingFileResponse>> listAll() {
        return ResponseEntity.ok(fileService.listAll());
    }
}
