package com.agropay.core.assignment.web;

import com.agropay.core.assignment.application.usecase.IHarvestRecordUseCase;
import com.agropay.core.assignment.model.harvest.*;
import com.agropay.core.shared.batch.BatchResponse;
import com.agropay.core.shared.utils.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(HarvestRecordController.BASE_URL)
@RequiredArgsConstructor
@Tag(name = "Harvest Records", description = "Endpoints para registrar cosechas offline-first")
public class HarvestRecordController {

    public static final String BASE_URL = "/v1/harvest-records";

    private final IHarvestRecordUseCase harvestRecordUseCase;

    @PostMapping("/batch-sync")
    @Operation(summary = "Sincronización batch de registros de cosecha (offline-first)")
    public ResponseEntity<ApiResult<BatchResponse<BatchHarvestResultData>>> batchSync(
            @Valid @RequestBody BatchHarvestSyncRequest request) {
        BatchResponse<BatchHarvestResultData> response = harvestRecordUseCase.batchSync(request);
        return ResponseEntity.ok(ApiResult.success(response));
    }
}
