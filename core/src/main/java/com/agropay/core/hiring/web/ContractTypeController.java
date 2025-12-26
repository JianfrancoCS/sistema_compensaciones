package com.agropay.core.hiring.web;

import com.agropay.core.hiring.application.usecase.IContractTypeUseCase;
import com.agropay.core.hiring.model.contracttype.ContractTypeSelectOptionDTO;
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
@RequestMapping(ContractTypeController.BASE_URL)
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Tipos de Contrato", description = "API para la gestión de tipos de contrato")
public class ContractTypeController {

    public static final String BASE_URL = "/v1/hiring/contract-types";

    private final IContractTypeUseCase contractTypeUseCase;

    @Operation(summary = "Obtener opciones de selección de tipo de contrato")
    @GetMapping("/select-options")
    public ResponseEntity<ApiResult<List<ContractTypeSelectOptionDTO>>> getContractTypeSelectOptions() {
        log.info("Solicitud REST para obtener opciones de selección de Tipo de Contrato");
        List<ContractTypeSelectOptionDTO> options = contractTypeUseCase.getSelectOptions();
        return ResponseEntity.ok(ApiResult.success(options));
    }
}
