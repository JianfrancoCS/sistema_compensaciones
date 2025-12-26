package com.agropay.core.organization.model.subsidiary;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields(value = {"name", "createdAt", "updatedAt"})
public class SubsidiaryPageableRequest extends BasePageableRequest {
    private String name;
}
