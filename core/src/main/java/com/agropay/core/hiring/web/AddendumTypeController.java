package com.agropay.core.hiring.web;

import com.agropay.core.hiring.application.usecase.IAddendumTypeUseCase;
import com.agropay.core.hiring.model.addendumtype.AddendumTypeSelectOptionDTO;
import com.agropay.core.shared.utils.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(AddendumTypeController.BASE_URL)
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Tipos de Adenda", description = "API para la gestión de tipos de adenda")
public class AddendumTypeController {

    public static final String BASE_URL = "/v1/hiring/addendum-types";

    private final IAddendumTypeUseCase addendumTypeUseCase;

    @Operation(summary = "Obtener opciones de selección para tipos de adenda")
    @GetMapping("/select-options")
    public ResponseEntity<ApiResult<List<AddendumTypeSelectOptionDTO>>> getSelectOptions() {
        log.info("REST request to get select options for AddendumTypes");
        List<AddendumTypeSelectOptionDTO> options = addendumTypeUseCase.getSelectOptions();
        return ResponseEntity.ok(ApiResult.success(options));
    }
}
