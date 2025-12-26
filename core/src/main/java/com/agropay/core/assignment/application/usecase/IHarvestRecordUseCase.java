package com.agropay.core.assignment.application.usecase;

import com.agropay.core.assignment.model.harvest.*;
import com.agropay.core.shared.batch.BatchResponse;

public interface IHarvestRecordUseCase {

    BatchResponse<BatchHarvestResultData> batchSync(BatchHarvestSyncRequest request);
}