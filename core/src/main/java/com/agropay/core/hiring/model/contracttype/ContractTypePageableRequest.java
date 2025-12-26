package com.agropay.core.hiring.model.contracttype;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields(value = {"code", "name", "createdAt", "updatedAt"})
public class ContractTypePageableRequest extends BasePageableRequest {
    private String code;
    private String name;
}
