package com.pic21.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DTO de respuesta del dashboard de estadísticas PIC21.
 */
@Getter
@Builder
public class DashboardResponse {

    /** Total de reuniones en el sistema */
    private long totalMeetings;

    /** Total de asistencias registradas en todo el sistema */
    private long totalAttendances;

    /** Promedio global de asistencia (porcentaje) */
    private double globalAttendanceRate;

    /** Estadísticas por reunión */
    private List<MeetingStats> meetingStats;

    /**
     * Estadísticas de asistencia para una reunión individual.
     */
    @Getter
    @Builder
    public static class MeetingStats {
        private Long meetingId;
        private String meetingTitle;
        private String meetingStatus;
        private int totalAttendances;
        private int totalStudents;
        private double attendancePercentage;
    }
}
