/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.controller.MeetingController
 *  com.pic21.domain.EstadoReunion
 *  com.pic21.domain.Reunion
 *  com.pic21.dto.request.MeetingRequest
 *  com.pic21.dto.request.MeetingStatusRequest
 *  com.pic21.dto.response.MeetingResponse
 *  com.pic21.exception.BusinessException
 *  com.pic21.service.MeetingService
 *  jakarta.validation.Valid
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.web.PageableDefault
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.ResponseEntity
 *  org.springframework.http.ResponseEntity$BodyBuilder
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.security.core.annotation.AuthenticationPrincipal
 *  org.springframework.security.core.userdetails.UserDetails
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PatchMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.PutMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 *  org.springframework.web.multipart.MultipartFile
 */
package com.pic21.controller;

import com.pic21.domain.EstadoReunion;
import com.pic21.domain.Reunion;
import com.pic21.dto.request.MeetingRequest;
import com.pic21.dto.request.MeetingStatusRequest;
import com.pic21.dto.response.MeetingResponse;
import com.pic21.exception.BusinessException;
import com.pic21.service.MeetingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value={"/api/meetings"})
public class MeetingController {
    private final MeetingService meetingService;

    @GetMapping
    public ResponseEntity<Page<MeetingResponse>> getAll(@PageableDefault(size=50, sort={"fechaInicio"}) Pageable pageable) {
        return ResponseEntity.ok((Object)this.meetingService.findAll(pageable));
    }

    @GetMapping(value={"/{id}"})
    public ResponseEntity<MeetingResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok((Object)this.meetingService.findById(id));
    }

    @PostMapping
    @PreAuthorize(value="hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
    public ResponseEntity<MeetingResponse> create(@Valid @RequestBody MeetingRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        MeetingResponse response = this.meetingService.create(request, userDetails.getUsername());
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CREATED).body((Object)response);
    }

    @PutMapping(value={"/{id}"})
    @PreAuthorize(value="hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
    public ResponseEntity<MeetingResponse> update(@PathVariable Long id, @Valid @RequestBody MeetingRequest request, @AuthenticationPrincipal UserDetails me) {
        boolean isAdmin = me.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_R04_ADMIN"));
        return ResponseEntity.ok((Object)this.meetingService.update(id, request, isAdmin));
    }

    @DeleteMapping(value={"/{id}"})
    @PreAuthorize(value="hasRole('R04_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        this.meetingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value={"/{id}/status"})
    @PreAuthorize(value="hasRole('R04_ADMIN')")
    public ResponseEntity<MeetingResponse> changeStatus(@PathVariable Long id, @Valid @RequestBody MeetingStatusRequest request, @AuthenticationPrincipal UserDetails me) {
        EstadoReunion newEstado = request.getEstado();
        boolean isAdmin = me.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_R04_ADMIN"));
        if (newEstado == EstadoReunion.BLOQUEADA && !me.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_R04_ADMIN"))) {
            throw new BusinessException("Solo ADMIN puede bloquear reuniones.");
        }
        return ResponseEntity.ok((Object)this.meetingService.changeStatus(id, newEstado, isAdmin));
    }

    @PostMapping(value={"/{id}/pdf"}, consumes={"multipart/form-data"})
    @PreAuthorize(value="hasRole('R04_ADMIN')")
    public ResponseEntity<MeetingResponse> uploadPdf(@PathVariable Long id, @RequestParam(value="file") MultipartFile file) {
        return ResponseEntity.ok((Object)this.meetingService.uploadPdf(id, file));
    }

    @GetMapping(value={"/{id}/pdf"}, produces={"application/pdf"})
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        Reunion reunion = this.meetingService.getReunionWithPdf(id);
        return ((ResponseEntity.BodyBuilder)ResponseEntity.ok().header("Content-Disposition", new String[]{"attachment; filename=\"" + reunion.getPdfFileName() + "\""})).body((Object)reunion.getPdfFileData());
    }

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }
}

