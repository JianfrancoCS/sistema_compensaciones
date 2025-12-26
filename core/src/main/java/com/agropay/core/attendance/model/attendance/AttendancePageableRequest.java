package com.agropay.core.attendance.model.attendance;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@ValidSortFields({"markingDate", "personDocumentNumber", "createdAt", "updatedAt"})
@Schema(description = "Request for pageable attendance list")
public class AttendancePageableRequest extends  BasePageableRequest{

    @NotNull(message = "La fecha es requerida")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha de marcación", example = "2024-09-15", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate markingDate;

    @Schema(description = "ID público de sucursal para filtrar", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID subsidiaryPublicId;

    @Schema(description = "Número de documento para filtrar", example = "12345678")
    private String personDocumentNumber;

    @Schema(description = "Filtrar solo empleados", example = "true")
    private Boolean isEmployee;




}