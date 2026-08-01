/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.ArchivoReunion
 *  com.pic21.domain.Reunion
 *  com.pic21.domain.Usuario
 *  com.pic21.dto.response.MeetingFileResponse
 *  com.pic21.exception.BusinessException
 *  com.pic21.exception.ResourceNotFoundException
 *  com.pic21.repository.ArchivoReunionRepository
 *  com.pic21.repository.ReunionRepository
 *  com.pic21.repository.UsuarioRepository
 *  com.pic21.service.MeetingFileService
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 *  org.springframework.web.multipart.MultipartFile
 */
package com.pic21.service;

import com.pic21.domain.ArchivoReunion;
import com.pic21.domain.Reunion;
import com.pic21.domain.Usuario;
import com.pic21.dto.response.MeetingFileResponse;
import com.pic21.exception.BusinessException;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.ArchivoReunionRepository;
import com.pic21.repository.ReunionRepository;
import com.pic21.repository.UsuarioRepository;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MeetingFileService {
    private static final Logger log = LoggerFactory.getLogger(MeetingFileService.class);
    private final ArchivoReunionRepository archivoRepository;
    private final ReunionRepository reunionRepository;
    private final UsuarioRepository usuarioRepository;
    private static final long MAX_FILE_SIZE = 0xA00000L;
    private static final String ALLOWED_TYPE = "application/pdf";

    @Transactional
    public List<MeetingFileResponse> uploadFiles(Long reunionId, List<MultipartFile> files, String uploaderUsername) {
        Reunion reunion = (Reunion)this.reunionRepository.findById((Object)reunionId).orElseThrow(() -> new ResourceNotFoundException("Reuni\u00f3n", reunionId));
        Usuario uploader = (Usuario)this.usuarioRepository.findByUsernameIgnoreCase(uploaderUsername).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + uploaderUsername));
        if (files == null || files.isEmpty()) {
            throw new BusinessException("Debe enviar al menos un archivo.");
        }
        List saved = files.stream().map(file -> {
            this.validateFile(file);
            try {
                return ArchivoReunion.builder().fileName(this.sanitizeFileName(file.getOriginalFilename())).fileType(ALLOWED_TYPE).fileData(file.getBytes()).reunion(reunion).subidoPor(uploader).build();
            }
            catch (IOException e) {
                throw new BusinessException("Error al leer el archivo: " + file.getOriginalFilename());
            }
        }).collect(Collectors.toList());
        List result = this.archivoRepository.saveAll(saved);
        log.info("Subidos {} archivos a reuni\u00f3n id={} por '{}'", new Object[]{result.size(), reunionId, uploaderUsername});
        return result.stream().map(arg_0 -> this.mapToResponse(arg_0)).collect(Collectors.toList());
    }

    @Transactional(readOnly=true)
    public List<MeetingFileResponse> listByMeeting(Long reunionId) {
        if (!this.reunionRepository.existsById((Object)reunionId)) {
            throw new ResourceNotFoundException("Reuni\u00f3n", reunionId);
        }
        return this.archivoRepository.findByReunionIdOrderByUploadedAtDesc(reunionId).stream().map(arg_0 -> this.mapToResponse(arg_0)).collect(Collectors.toList());
    }

    @Transactional(readOnly=true)
    public ArchivoReunion getFileForDownload(Long fileId) {
        return (ArchivoReunion)this.archivoRepository.findById((Object)fileId).orElseThrow(() -> new ResourceNotFoundException("Archivo", fileId));
    }

    @Transactional
    public void deleteFile(Long fileId) {
        ArchivoReunion file = (ArchivoReunion)this.archivoRepository.findById((Object)fileId).orElseThrow(() -> new ResourceNotFoundException("Archivo", fileId));
        this.archivoRepository.delete((Object)file);
        log.info("Archivo id={} '{}' eliminado", (Object)fileId, (Object)file.getFileName());
    }

    @Transactional(readOnly=true)
    public List<MeetingFileResponse> listAll() {
        return this.archivoRepository.findAll().stream().map(arg_0 -> this.mapToResponse(arg_0)).collect(Collectors.toList());
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("El archivo est\u00e1 vac\u00edo.");
        }
        if (file.getSize() > 0xA00000L) {
            throw new BusinessException("El archivo supera el tama\u00f1o m\u00e1ximo de 10 MB: " + file.getOriginalFilename());
        }
        String ct = file.getContentType();
        if (!ALLOWED_TYPE.equals(ct)) {
            throw new BusinessException("Solo se permiten archivos PDF. Tipo recibido: " + ct);
        }
    }

    private String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) {
            return "archivo.pdf";
        }
        return name.replaceAll("[^a-zA-Z0-9._\\-() ]", "_");
    }

    private MeetingFileResponse mapToResponse(ArchivoReunion f) {
        return MeetingFileResponse.builder().id(f.getId()).fileName(f.getFileName()).fileType(f.getFileType()).meetingId(f.getReunion().getId()).meetingTitle(f.getReunion().getTitulo()).uploadedByUsername(f.getSubidoPor().getUsername()).uploadedAt(f.getUploadedAt()).fileSize(Long.valueOf(f.getFileData() != null ? (long)f.getFileData().length : 0L)).build();
    }

    public MeetingFileService(ArchivoReunionRepository archivoRepository, ReunionRepository reunionRepository, UsuarioRepository usuarioRepository) {
        this.archivoRepository = archivoRepository;
        this.reunionRepository = reunionRepository;
        this.usuarioRepository = usuarioRepository;
    }
}

