package com.pic21.service;

import com.pic21.domain.Rol;
import com.pic21.domain.Reunion;
import com.pic21.dto.response.DashboardResponse;
import com.pic21.dto.response.DashboardResponse.MeetingStats;
import com.pic21.repository.AsistenciaRepository;
import com.pic21.repository.ReunionRepository;
import com.pic21.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio del dashboard de estadísticas PIC21 (UML v8).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ReunionRepository reunionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
    public DashboardResponse getDashboard() {
        // Total de usuarios con roles estudiantiles
        long totalStudents = usuarioRepository.findAll().stream()
                .filter(u -> u.getRoles().contains(Rol.R02_ESTUDIANTE)
                        || u.getRoles().contains(Rol.R03_EGRESADO)
                        || u.getRoles().contains(Rol.R06_AYUDANTE))
                .count();

        long totalMeetings   = reunionRepository.count();
        long totalAttendances = asistenciaRepository.count();

        List<Reunion> reuniones = reunionRepository.findAll();
        List<MeetingStats> meetingStatsList = reuniones.stream()
                .map(reunion -> {
                    int attended = asistenciaRepository.findByReunionWithDetails(reunion).size();
                    double percentage = totalStudents > 0
                            ? Math.round((attended * 100.0 / totalStudents) * 10.0) / 10.0
                            : 0.0;
                    return MeetingStats.builder()
                            .meetingId(reunion.getId())
                            .meetingTitle(reunion.getTitulo())
                            .meetingStatus(reunion.getEstado().name())
                            .totalAttendances(attended)
                            .totalStudents((int) totalStudents)
                            .attendancePercentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());

        double globalRate = 0.0;
        if (totalMeetings > 0 && totalStudents > 0) {
            globalRate = meetingStatsList.stream()
                    .mapToDouble(MeetingStats::getAttendancePercentage)
                    .average().orElse(0.0);
            globalRate = Math.round(globalRate * 10.0) / 10.0;
        }

        log.debug("Dashboard: {} reuniones, {} asistencias, {}% global", totalMeetings, totalAttendances, globalRate);

        return DashboardResponse.builder()
                .totalMeetings(totalMeetings)
                .totalAttendances(totalAttendances)
                .globalAttendanceRate(globalRate)
                .meetingStats(meetingStatsList)
                .build();
    }
}
