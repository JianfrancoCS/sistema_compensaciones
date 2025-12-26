package com.agropay.core.attendance.web;

import com.agropay.core.attendance.application.usecase.IMarkingReasonUseCase;
import com.agropay.core.attendance.model.markingreason.*;
import com.agropay.core.shared.utils.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(MarkingReasonController.BASE_URL)
@RequiredArgsConstructor
@Validated
@Tag(name = "Gestión de asistencia", description = "API para razones de marcado")
public class MarkingReasonController {
    public static final String BASE_URL = "/v1/attendance/marking-reasons";

    private final IMarkingReasonUseCase markingReasonUseCase;

    @GetMapping("/select-options")
    @Operation(
        summary = "Obtener opciones de selección de razones de marcado",
        description = "Retorna las razones de marcado disponibles. " +
                     "Con isInternal=true obtiene razones para empleados (TRABAJO, TRAMITE). " +
                     "Con isInternal=false obtiene razones para externos (VISITA, SUPERVISION, etc.)."
    )
    public ResponseEntity<ApiResult<List<MarkingReasonSelectOptionDTO>>> getSelectOptions(
        @Parameter(description = "Filtrar por tipo: true=empleados internos, false=personas externas", required = true, example = "true")
        @RequestParam Boolean isInternal
    ) {
        List<MarkingReasonSelectOptionDTO> response;
        if (isInternal) {
            response = markingReasonUseCase.getInternalMarkingReasons();
        } else {
            response = markingReasonUseCase.getExternalMarkingReasons();
        }
        return ResponseEntity.ok(ApiResult.success(response));
    }
}