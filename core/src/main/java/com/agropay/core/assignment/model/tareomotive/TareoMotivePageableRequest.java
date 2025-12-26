package com.agropay.core.assignment.model.tareomotive;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields({"publicId", "name", "description","isPaid", "createdAt", "updatedAt"})
public class TareoMotivePageableRequest extends BasePageableRequest {
    private String name;
    private Boolean isPaid;
}