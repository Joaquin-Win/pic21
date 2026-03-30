package com.pic21.service;

import com.pic21.domain.Meeting;
import com.pic21.domain.Role.RoleName;
import com.pic21.dto.response.DashboardResponse;
import com.pic21.dto.response.DashboardResponse.MeetingStats;
import com.pic21.repository.AttendanceRepository;
import com.pic21.repository.MeetingRepository;
import com.pic21.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio del dashboard de estadísticas PIC21.
 *
 * Calcula:
 *   - Total de reuniones
 *   - Total de asistencias registradas
 *   - Porcentaje global de asistencia
 *   - Desglose por reunión (total asistentes, % de asistencia vs. total estudiantes)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MeetingRepository meetingRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    /**
     * Retorna las estadísticas del dashboard.
     * Acceso: ADMIN, PROFESOR, AYUDANTE.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN','PROFESOR','AYUDANTE')")
    public DashboardResponse getDashboard() {
        // Total de estudiantes y egresados registrados en el sistema
        long totalStudents = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream()
                        .anyMatch(r -> r.getName() == RoleName.ESTUDIANTE || r.getName() == RoleName.EGRESADO))
                .count();

        // Total de reuniones
        long totalMeetings = meetingRepository.count();

        // Total de asistencias
        long totalAttendances = attendanceRepository.count();

        // Stats por reunión
        List<Meeting> meetings = meetingRepository.findAll();
        List<MeetingStats> meetingStatsList = meetings.stream()
                .map(meeting -> {
                    int attended = attendanceRepository.findByMeetingWithDetails(meeting).size();
                    double percentage = totalStudents > 0
                            ? Math.round((attended * 100.0 / totalStudents) * 10.0) / 10.0
                            : 0.0;

                    return MeetingStats.builder()
                            .meetingId(meeting.getId())
                            .meetingTitle(meeting.getTitle())
                            .meetingStatus(meeting.getStatus().name())
                            .totalAttendances(attended)
                            .totalStudents((int) totalStudents)
                            .attendancePercentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());

        // Porcentaje global (promedio de porcentajes por reunión)
        double globalRate = 0.0;
        if (totalMeetings > 0 && totalStudents > 0) {
            globalRate = meetingStatsList.stream()
                    .mapToDouble(MeetingStats::getAttendancePercentage)
                    .average()
                    .orElse(0.0);
            globalRate = Math.round(globalRate * 10.0) / 10.0;
        }

        log.debug("Dashboard calculado: {} reuniones, {} asistencias totales, {}% global",
                totalMeetings, totalAttendances, globalRate);

        return DashboardResponse.builder()
                .totalMeetings(totalMeetings)
                .totalAttendances(totalAttendances)
                .globalAttendanceRate(globalRate)
                .meetingStats(meetingStatsList)
                .build();
    }
}
