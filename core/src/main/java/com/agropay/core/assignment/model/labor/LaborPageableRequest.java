package com.agropay.core.assignment.model.labor;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields({"publicId", "name", "isPiecework", "basePrice", "createdAt", "updatedAt"})
public class LaborPageableRequest extends BasePageableRequest {
    private String name;
    private Boolean isPiecework;
    private UUID laborUnitPublicId;
}