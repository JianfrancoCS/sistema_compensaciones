package com.agropay.core.assignment.model.tareo;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields({"publicId", "createdAt", "createdBy", "labor.name", "lote.name", "subsidiary.name"})
public class TareoPageableRequest extends BasePageableRequest {
    private UUID laborPublicId;
    private UUID subsidiaryPublicId;
    private String createdBy;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private Boolean isProcessed;
}