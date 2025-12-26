package com.agropay.core.assignment.model.lote;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields({"publicId", "name", "createdAt", "updatedAt"})
public class LotePageableRequest extends BasePageableRequest {
    private String name;
    private UUID subsidiaryPublicId;
}