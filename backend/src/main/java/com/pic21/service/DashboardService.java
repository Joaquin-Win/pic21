/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.domain.Rol
 *  com.pic21.dto.response.DashboardResponse
 *  com.pic21.dto.response.DashboardResponse$MeetingStats
 *  com.pic21.repository.AsistenciaRepository
 *  com.pic21.repository.ReunionRepository
 *  com.pic21.repository.UsuarioRepository
 *  com.pic21.service.DashboardService
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package com.pic21.service;

import com.pic21.domain.Rol;
import com.pic21.domain.Reunion;
import com.pic21.dto.response.DashboardResponse;
import com.pic21.repository.AsistenciaRepository;
import com.pic21.repository.ReunionRepository;
import com.pic21.repository.UsuarioRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
    private final ReunionRepository reunionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly=true)
    @PreAuthorize(value="hasAnyRole('R04_ADMIN','R05_DIRECTOR')")
    public DashboardResponse getDashboard() {
        long totalStudents = this.usuarioRepository.findAll().stream().filter(u -> u.getRoles().contains(Rol.R02_ESTUDIANTE) || u.getRoles().contains(Rol.R03_EGRESADO) || u.getRoles().contains(Rol.R06_AYUDANTE) || u.getRoles().contains(Rol.R07_ESTUDIANTE_POSGRADO)).count();
        long totalMeetings = this.reunionRepository.count();
        long totalAttendances = this.asistenciaRepository.count();
        List<Reunion> reuniones = this.reunionRepository.findAll();
        List<DashboardResponse.MeetingStats> meetingStatsList = reuniones.stream().map(reunion -> {
            int attended = this.asistenciaRepository.findByReunionWithDetails(reunion).size();
            double percentage = totalStudents > 0L ? (double)Math.round((double)attended * 100.0 / (double)totalStudents * 10.0) / 10.0 : 0.0;
            return DashboardResponse.MeetingStats.builder().meetingId(reunion.getId()).meetingTitle(reunion.getTitulo()).meetingStatus(reunion.getEstado().name()).totalAttendances(attended).totalStudents((int)totalStudents).attendancePercentage(percentage).build();
        }).collect(Collectors.toList());
        double globalRate = 0.0;
        if (totalMeetings > 0L && totalStudents > 0L) {
            globalRate = meetingStatsList.stream().mapToDouble(DashboardResponse.MeetingStats::getAttendancePercentage).average().orElse(0.0);
            globalRate = (double)Math.round(globalRate * 10.0) / 10.0;
        }
        log.debug("Dashboard: {} reuniones, {} asistencias, {}% global", new Object[]{totalMeetings, totalAttendances, globalRate});
        return DashboardResponse.builder().totalMeetings(totalMeetings).totalAttendances(totalAttendances).globalAttendanceRate(globalRate).meetingStats(meetingStatsList).build();
    }

    public DashboardService(ReunionRepository reunionRepository, AsistenciaRepository asistenciaRepository, UsuarioRepository usuarioRepository) {
        this.reunionRepository = reunionRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.usuarioRepository = usuarioRepository;
    }
}

