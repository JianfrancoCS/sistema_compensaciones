package com.agropay.core.organization.model.person;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields({"id", "publicId", "firstName", "lastName", "email", "createdAt", "updatedAt"})
public class PersonPageableRequest extends BasePageableRequest {
    private String query;
}
