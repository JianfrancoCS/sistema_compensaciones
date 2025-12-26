package com.agropay.core.assignment.model.laborunit;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields({"publicId", "name", "description","abbreviation", "createdAt", "updatedAt"})
public class LaborUnitPageableRequest extends BasePageableRequest {
    private String name;
    private String abbreviation;
}