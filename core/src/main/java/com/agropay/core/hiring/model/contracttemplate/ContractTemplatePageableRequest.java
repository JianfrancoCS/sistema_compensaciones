package com.agropay.core.hiring.model.contracttemplate;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields({ "publicId", "code", "name", "createdAt", "updatedAt"})
public class ContractTemplatePageableRequest extends BasePageableRequest {
    private String code;
    private String name;
    private UUID contractTypePublicId;
    private UUID statePublicId;
}
