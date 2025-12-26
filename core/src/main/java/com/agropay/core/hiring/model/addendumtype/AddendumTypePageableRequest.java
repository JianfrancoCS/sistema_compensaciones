package com.agropay.core.hiring.model.addendumtype;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields(value = {"code", "name", "createdAt", "updatedAt"})
public class AddendumTypePageableRequest extends BasePageableRequest {
    private String code;
    private String name;
}
