/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.dto.response.DashboardResponse
 *  com.pic21.dto.response.DashboardResponse$DashboardResponseBuilder
 *  com.pic21.dto.response.DashboardResponse$MeetingStats
 */
package com.pic21.dto.response;
import lombok.Builder;

import com.pic21.dto.response.DashboardResponse;
import java.util.List;

public class DashboardResponse {
    private long totalMeetings;
    private long totalAttendances;
    private double globalAttendanceRate;
    private List<MeetingStats> meetingStats;

    @Builder
    DashboardResponse(long totalMeetings, long totalAttendances, double globalAttendanceRate, List<MeetingStats> meetingStats) {
        this.totalMeetings = totalMeetings;
        this.totalAttendances = totalAttendances;
        this.globalAttendanceRate = globalAttendanceRate;
        this.meetingStats = meetingStats;
    }
    public long getTotalMeetings() {
        return this.totalMeetings;
    }

    public long getTotalAttendances() {
        return this.totalAttendances;
    }

    public double getGlobalAttendanceRate() {
        return this.globalAttendanceRate;
    }

    public List<MeetingStats> getMeetingStats() {
        return this.meetingStats;
    }
    // â”€â”€ EstadÃ­sticas por reuniÃ³n (inner class) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    @Builder
    @Getter
    public static class MeetingStats {
        private Long   meetingId;
        private String meetingTitle;
        private String meetingStatus;
        private int    totalAttendances;
        private int    totalStudents;
        private double attendancePercentage;
    }

}