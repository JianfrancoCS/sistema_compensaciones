package com.agropay.core.attendance.model.attendance;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@ValidSortFields({ "personDocumentNumber", "createdAt", "updatedAt"})
@Schema(description = "Request for today's attendance list by subsidiary")
public class TodayAttendancePageableRequest extends BasePageableRequest {

    @Schema(description = "Número de documento para filtrar", example = "74542137")
    private String personDocumentNumber;

    @Schema(description = "Filtrar solo empleados", example = "true")
    private Boolean isEmployee;
}