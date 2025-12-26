package com.agropay.core.organization.model.employee;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields({ "publicId", "documentNumber", "subsidiaryName", "positionName", "names", "paternalLastname", "maternalLastname", "createdAt", "updatedAt"})
public class EmployeePageableRequest extends BasePageableRequest {
    private String documentNumber;
    private UUID subsidiaryPublicId;
    private UUID positionPublicId;
    private Boolean isNational; // Opcional: true=peruanos, false=extranjeros, null=todos

    @Override
    public Pageable toPageable() {
        // Mapear campos especiales que no existen directamente en EmployeeEntity
        String sortByField = getSortBy();
        String mappedSortBy = sortByField;
        
        // Mapear documentNumber a personDocumentNumber para que Spring Data pueda validarlo
        if ("documentNumber".equals(sortByField)) {
            mappedSortBy = "personDocumentNumber";
        }
        
        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                mappedSortBy
        );
        return PageRequest.of(getPage(), getSize(), sort);
    }
}
