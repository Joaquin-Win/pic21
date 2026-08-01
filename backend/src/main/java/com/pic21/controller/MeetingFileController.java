/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.controller.MeetingFileController
 *  com.pic21.domain.ArchivoReunion
 *  com.pic21.dto.response.MeetingFileResponse
 *  com.pic21.service.MeetingFileService
 *  org.springframework.http.HttpHeaders
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.MediaType
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.security.core.annotation.AuthenticationPrincipal
 *  org.springframework.security.core.userdetails.UserDetails
 *  org.springframework.util.MultiValueMap
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 *  org.springframework.web.multipart.MultipartFile
 */
package com.pic21.controller;

import com.pic21.domain.ArchivoReunion;
import com.pic21.dto.response.MeetingFileResponse;
import com.pic21.service.MeetingFileService;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class MeetingFileController {
    private final MeetingFileService fileService;

    @PostMapping(value={"/api/meetings/{meetingId}/files"})
    @PreAuthorize(value="hasAnyRole('R04_ADMIN','R01_PROFESOR')")
    public ResponseEntity<List<MeetingFileResponse>> upload(@PathVariable Long meetingId, @RequestParam(value="files") List<MultipartFile> files, @AuthenticationPrincipal UserDetails userDetails) {
        List result = this.fileService.uploadFiles(meetingId, files, userDetails.getUsername());
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CREATED).body(result);
    }

    @GetMapping(value={"/api/meetings/{meetingId}/files"})
    public ResponseEntity<List<MeetingFileResponse>> listByMeeting(@PathVariable Long meetingId) {
        return ResponseEntity.ok(this.fileService.listByMeeting(meetingId));
    }

    @GetMapping(value={"/api/files/{fileId}/download"})
    public ResponseEntity<byte[]> download(@PathVariable Long fileId) {
        ArchivoReunion file = this.fileService.getFileForDownload(fileId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", file.getFileName());
        headers.setContentLength((long)file.getFileData().length);
        return new ResponseEntity(file.getFileData(), (MultiValueMap)headers, (HttpStatusCode)HttpStatus.OK);
    }

    @DeleteMapping(value={"/api/files/{fileId}"})
    @PreAuthorize(value="hasRole('R04_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long fileId) {
        this.fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value={"/api/files"})
    @PreAuthorize(value="hasRole('R04_ADMIN')")
    public ResponseEntity<List<MeetingFileResponse>> listAll() {
        return ResponseEntity.ok(this.fileService.listAll());
    }

    public MeetingFileController(MeetingFileService fileService) {
        this.fileService = fileService;
    }
}

