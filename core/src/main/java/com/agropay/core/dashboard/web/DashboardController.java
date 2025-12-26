package com.agropay.core.dashboard.web;

import com.agropay.core.dashboard.model.*;
import com.agropay.core.dashboard.service.DashboardService;
import com.agropay.core.shared.utils.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints para obtener estadísticas y métricas del dashboard")
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener estadísticas generales del dashboard",
               description = "Retorna estadísticas generales como total de empleados, planillas, montos, etc.")
    public ResponseEntity<ApiResult<DashboardStatsDTO>> getStats(
            @RequestParam(required = false) UUID subsidiaryPublicId,
            @RequestParam(required = false) UUID periodPublicId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        log.debug("GET /v1/dashboard/stats - subsidiary: {}, period: {}, dateFrom: {}, dateTo: {}",
                subsidiaryPublicId, periodPublicId, dateFrom, dateTo);
        
        DashboardStatsDTO stats = dashboardService.getStats(subsidiaryPublicId, periodPublicId, dateFrom, dateTo);
        return ResponseEntity.ok(ApiResult.success(stats));
    }

    @GetMapping("/payrolls-by-status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener planillas agrupadas por estado",
               description = "Retorna la cantidad y monto total de planillas agrupadas por estado")
    public ResponseEntity<ApiResult<List<PayrollsByStatusDTO>>> getPayrollsByStatus(
            @RequestParam(required = false) UUID subsidiaryPublicId,
            @RequestParam(required = false) UUID periodPublicId
    ) {
        log.debug("GET /v1/dashboard/payrolls-by-status - subsidiary: {}, period: {}",
                subsidiaryPublicId, periodPublicId);
        
        List<PayrollsByStatusDTO> data = dashboardService.getPayrollsByStatus(subsidiaryPublicId, periodPublicId);
        return ResponseEntity.ok(ApiResult.success(data));
    }

    @GetMapping("/employees-by-subsidiary")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener empleados agrupados por subsidiaria",
               description = "Retorna la cantidad de empleados agrupados por subsidiaria")
    public ResponseEntity<ApiResult<List<EmployeesBySubsidiaryDTO>>> getEmployeesBySubsidiary(
            @RequestParam(required = false) UUID subsidiaryPublicId
    ) {
        log.debug("GET /v1/dashboard/employees-by-subsidiary - subsidiary: {}", subsidiaryPublicId);
        
        List<EmployeesBySubsidiaryDTO> data = dashboardService.getEmployeesBySubsidiary(subsidiaryPublicId);
        return ResponseEntity.ok(ApiResult.success(data));
    }

    @GetMapping("/payrolls-by-period")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener planillas agrupadas por período",
               description = "Retorna la cantidad y monto total de planillas agrupadas por período (año-mes)")
    public ResponseEntity<ApiResult<List<PayrollsByPeriodDTO>>> getPayrollsByPeriod(
            @RequestParam(required = false) UUID subsidiaryPublicId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        log.debug("GET /v1/dashboard/payrolls-by-period - subsidiary: {}, dateFrom: {}, dateTo: {}",
                subsidiaryPublicId, dateFrom, dateTo);
        
        List<PayrollsByPeriodDTO> data = dashboardService.getPayrollsByPeriod(subsidiaryPublicId, dateFrom, dateTo);
        return ResponseEntity.ok(ApiResult.success(data));
    }

    @GetMapping("/tareos-by-labor")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener tareos agrupados por labor",
               description = "Retorna la cantidad de tareos y empleados agrupados por labor")
    public ResponseEntity<ApiResult<List<TareosByLaborDTO>>> getTareosByLabor(
            @RequestParam(required = false) UUID subsidiaryPublicId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        log.debug("GET /v1/dashboard/tareos-by-labor - subsidiary: {}, dateFrom: {}, dateTo: {}",
                subsidiaryPublicId, dateFrom, dateTo);
        
        List<TareosByLaborDTO> data = dashboardService.getTareosByLabor(subsidiaryPublicId, dateFrom, dateTo);
        return ResponseEntity.ok(ApiResult.success(data));
    }

    @GetMapping("/attendance-trend")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener tendencia de asistencia",
               description = "Retorna la cantidad de entradas y salidas por día")
    public ResponseEntity<ApiResult<List<AttendanceTrendDTO>>> getAttendanceTrend(
            @RequestParam(required = false) UUID subsidiaryPublicId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        log.debug("GET /v1/dashboard/attendance-trend - subsidiary: {}, dateFrom: {}, dateTo: {}",
                subsidiaryPublicId, dateFrom, dateTo);
        
        List<AttendanceTrendDTO> data = dashboardService.getAttendanceTrend(subsidiaryPublicId, dateFrom, dateTo);
        return ResponseEntity.ok(ApiResult.success(data));
    }
}

