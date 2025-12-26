package com.agropay.core.organization.model.area;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields({ "publicId", "name", "createdAt", "updatedAt"})
public class AreaPageableRequest extends BasePageableRequest {
    private String name;
}
