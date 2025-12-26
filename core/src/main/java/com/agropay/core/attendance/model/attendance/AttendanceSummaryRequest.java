package com.agropay.core.attendance.model.attendance;

import com.agropay.core.shared.annotations.BasePageableRequest;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class AttendanceSummaryRequest extends BasePageableRequest {

    @Parameter(description = "Fecha para el resumen de asistencia (formato YYYY-MM-DD)",
               required = true,
               example = "2025-09-16")
    @NotNull(message = "La fecha es requerida")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @Parameter(description = "Filtrar solo externos (true) o solo empleados (false). Si no se especifica, trae ambos",
               required = false)
    private Boolean isExternal;

    public AttendanceSummaryRequest() {
    }

    public AttendanceSummaryRequest(LocalDate date, Boolean isExternal) {
        this.date = date;
        this.isExternal = isExternal;
    }
}