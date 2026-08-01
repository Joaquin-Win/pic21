/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.pic21.controller.DashboardController
 *  com.pic21.dto.response.DashboardResponse
 *  com.pic21.service.DashboardService
 *  org.springframework.http.ResponseEntity
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package com.pic21.controller;

import com.pic21.dto.response.DashboardResponse;
import com.pic21.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/dashboard"})
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(this.dashboardService.getDashboard());
    }

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
}

