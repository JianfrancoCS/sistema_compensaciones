package com.agropay.core.organization.model.position;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields(value = {"code", "name", "createdAt", "updatedAt"})
public class PositionPageableRequest extends BasePageableRequest {
    private String code;
    private String name;
    private UUID areaPublicId;
}
