package com.agropay.core.hiring.model.contract;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields({"id", "publicId", "contractNumber", "personDocumentNumber", "createdAt", "updatedAt"})
public class ContractPageableRequest extends BasePageableRequest {
    private String contractNumber;
    private String documentNumber;
    private UUID contractTypePublicId;
    private UUID statePublicId;
}
