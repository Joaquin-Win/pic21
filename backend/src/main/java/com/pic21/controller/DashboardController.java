package com.pic21.controller;

import com.pic21.dto.response.DashboardResponse;
import com.pic21.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador del dashboard de estadísticas.
 *
 * Endpoints:
 *   GET /api/dashboard  → Estadísticas globales + desglose por reunión
 *                         Acceso: ADMIN, PROFESOR, AYUDANTE
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Retorna las estadísticas del sistema:
     * <ul>
     *   <li>Total de reuniones</li>
     *   <li>Total de asistencias registradas</li>
     *   <li>Porcentaje global de asistencia</li>
     *   <li>Desglose por reunión (asistentes, %, estado)</li>
     * </ul>
     *
     * @return 200 OK con {@link DashboardResponse}
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }
}
