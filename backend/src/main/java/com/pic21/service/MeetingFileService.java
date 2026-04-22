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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de archivos PDF adjuntos a reuniones (UML v8).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingFileService {

    private final ArchivoReunionRepository archivoRepository;
    private final ReunionRepository reunionRepository;
    private final UsuarioRepository usuarioRepository;

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB
    private static final String ALLOWED_TYPE = "application/pdf";

    @Transactional
    public List<MeetingFileResponse> uploadFiles(Long reunionId, List<MultipartFile> files, String uploaderUsername) {
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new ResourceNotFoundException("Reunión", reunionId));
        Usuario uploader = usuarioRepository.findByUsernameIgnoreCase(uploaderUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + uploaderUsername));

        if (files == null || files.isEmpty()) {
            throw new BusinessException("Debe enviar al menos un archivo.");
        }

        List<ArchivoReunion> saved = files.stream().map(file -> {
            validateFile(file);
            try {
                return ArchivoReunion.builder()
                        .fileName(sanitizeFileName(file.getOriginalFilename()))
                        .fileType(ALLOWED_TYPE)
                        .fileData(file.getBytes())
                        .reunion(reunion)
                        .subidoPor(uploader)
                        .build();
            } catch (IOException e) {
                throw new BusinessException("Error al leer el archivo: " + file.getOriginalFilename());
            }
        }).collect(Collectors.toList());

        List<ArchivoReunion> result = archivoRepository.saveAll(saved);
        log.info("Subidos {} archivos a reunión id={} por '{}'", result.size(), reunionId, uploaderUsername);
        return result.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MeetingFileResponse> listByMeeting(Long reunionId) {
        if (!reunionRepository.existsById(reunionId)) {
            throw new ResourceNotFoundException("Reunión", reunionId);
        }
        return archivoRepository.findByReunionIdOrderByUploadedAtDesc(reunionId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ArchivoReunion getFileForDownload(Long fileId) {
        return archivoRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo", fileId));
    }

    @Transactional
    public void deleteFile(Long fileId) {
        ArchivoReunion file = archivoRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo", fileId));
        archivoRepository.delete(file);
        log.info("Archivo id={} '{}' eliminado", fileId, file.getFileName());
    }

    @Transactional(readOnly = true)
    public List<MeetingFileResponse> listAll() {
        return archivoRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) throw new BusinessException("El archivo está vacío.");
        if (file.getSize() > MAX_FILE_SIZE) throw new BusinessException(
                "El archivo supera el tamaño máximo de 10 MB: " + file.getOriginalFilename());
        String ct = file.getContentType();
        if (!ALLOWED_TYPE.equals(ct)) throw new BusinessException(
                "Solo se permiten archivos PDF. Tipo recibido: " + ct);
    }

    private String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) return "archivo.pdf";
        return name.replaceAll("[^a-zA-Z0-9._\\-() ]", "_");
    }

    private MeetingFileResponse mapToResponse(ArchivoReunion f) {
        return MeetingFileResponse.builder()
                .id(f.getId())
                .fileName(f.getFileName())
                .fileType(f.getFileType())
                .meetingId(f.getReunion().getId())
                .meetingTitle(f.getReunion().getTitulo())
                .uploadedByUsername(f.getSubidoPor().getUsername())
                .uploadedAt(f.getUploadedAt())
                .fileSize(f.getFileData() != null ? (long) f.getFileData().length : 0L)
                .build();
    }
}
