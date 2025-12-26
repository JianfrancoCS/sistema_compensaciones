package com.agropay.core.attendance.model.markingreason;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields({"publicId", "code", "name", "createdAt", "updatedAt"})
public class MarkingReasonPageableRequest extends BasePageableRequest {
    private String code;
    private String name;
}