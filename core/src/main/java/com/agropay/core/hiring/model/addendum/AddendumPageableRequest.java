package com.agropay.core.hiring.model.addendum;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields({ "addendumNumber", "effectiveDate", "createdAt", "updatedAt"})
public class AddendumPageableRequest extends BasePageableRequest {
    private String addendumNumber;
    private UUID contractPublicId;
}
