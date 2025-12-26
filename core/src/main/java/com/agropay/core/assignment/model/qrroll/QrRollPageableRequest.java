package com.agropay.core.assignment.model.qrroll;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields({"id", "publicId", "createdAt", "updatedAt"})
public class QrRollPageableRequest extends BasePageableRequest {
    private Boolean hasUnprintedCodes;
}
