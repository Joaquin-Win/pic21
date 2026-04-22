package com.pic21.service;

import com.pic21.domain.Meeting;
import com.pic21.domain.MeetingFile;
import com.pic21.domain.User;
import com.pic21.dto.response.MeetingFileResponse;
import com.pic21.exception.BusinessException;
import com.pic21.exception.ResourceNotFoundException;
import com.pic21.repository.MeetingFileRepository;
import com.pic21.repository.MeetingRepository;
import com.pic21.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de archivos PDF adjuntos a reuniones.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingFileService {

    private final MeetingFileRepository fileRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB
    private static final String ALLOWED_TYPE = "application/pdf";

    // ── Subir uno o varios archivos ────────────────────────
    @Transactional
    public List<MeetingFileResponse> uploadFiles(Long meetingId, List<MultipartFile> files, String uploaderUsername) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Reunión", meetingId));
        User uploader = userRepository.findByUsernameIgnoreCase(uploaderUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + uploaderUsername));

        if (files == null || files.isEmpty()) {
            throw new BusinessException("Debe enviar al menos un archivo.");
        }

        List<MeetingFile> saved = files.stream().map(file -> {
            validateFile(file);
            try {
                return MeetingFile.builder()
                        .fileName(sanitizeFileName(file.getOriginalFilename()))
                        .fileType(ALLOWED_TYPE)
                        .fileData(file.getBytes())
                        .meeting(meeting)
                        .uploadedBy(uploader)
                        .build();
            } catch (IOException e) {
                throw new BusinessException("Error al leer el archivo: " + file.getOriginalFilename());
            }
        }).collect(Collectors.toList());

        List<MeetingFile> result = fileRepository.saveAll(saved);
        log.info("Subidos {} archivos a reunión id={} por '{}'", result.size(), meetingId, uploaderUsername);
        return result.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── Listar archivos de una reunión ─────────────────────
    @Transactional(readOnly = true)
    public List<MeetingFileResponse> listByMeeting(Long meetingId) {
        if (!meetingRepository.existsById(meetingId)) {
            throw new ResourceNotFoundException("Reunión", meetingId);
        }
        return fileRepository.findByMeetingIdOrderByUploadedAtDesc(meetingId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── Descargar archivo ──────────────────────────────────
    @Transactional(readOnly = true)
    public MeetingFile getFileForDownload(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo", fileId));
    }

    // ── Eliminar archivo ───────────────────────────────────
    @Transactional
    public void deleteFile(Long fileId) {
        MeetingFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo", fileId));
        fileRepository.delete(file);
        log.info("Archivo id={} '{}' eliminado", fileId, file.getFileName());
    }

    // ── Listar TODOS los archivos (admin) ──────────────────
    @Transactional(readOnly = true)
    public List<MeetingFileResponse> listAll() {
        return fileRepository.findAllWithDetails()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── Helpers ────────────────────────────────────────────
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

    private MeetingFileResponse mapToResponse(MeetingFile f) {
        return MeetingFileResponse.builder()
                .id(f.getId())
                .fileName(f.getFileName())
                .fileType(f.getFileType())
                .meetingId(f.getMeeting().getId())
                .meetingTitle(f.getMeeting().getTitle())
                .uploadedByUsername(f.getUploadedBy().getUsername())
                .uploadedAt(f.getUploadedAt())
                .fileSize(f.getFileData() != null ? (long) f.getFileData().length : 0L)
                .build();
    }
}
