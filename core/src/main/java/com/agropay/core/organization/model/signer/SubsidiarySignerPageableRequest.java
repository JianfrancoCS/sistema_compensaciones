package com.agropay.core.organization.model.signer;

import com.agropay.core.shared.annotations.BasePageableRequest;
import com.agropay.core.shared.annotations.ValidSortFields;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@EqualsAndHashCode(callSuper = true)
@Data
@ValidSortFields({ "subsidiaryName", "responsibleEmployeeName", "responsiblePosition", "createdAt", "updatedAt" })
public class SubsidiarySignerPageableRequest extends BasePageableRequest {
    private String subsidiaryName;
    private String responsibleEmployeeName;

    @Override
    public Pageable toPageable() {
        // Mapear campos especiales que no existen directamente en SubsidiaryEntity
        String sortByField = getSortBy();
        String mappedSortBy = sortByField;
        
        // Mapear subsidiaryName a name para que Spring Data pueda ordenar correctamente
        if ("subsidiaryName".equals(sortByField)) {
            mappedSortBy = "name";
        }
        // Los otros campos (responsibleEmployeeName, responsiblePosition) no se pueden ordenar
        // directamente en la entidad SubsidiaryEntity, así que los ignoramos o usamos un valor por defecto
        else if ("responsibleEmployeeName".equals(sortByField) || "responsiblePosition".equals(sortByField)) {
            // Estos campos no existen en SubsidiaryEntity, usar ordenamiento por defecto
            mappedSortBy = "name";
        }
        
        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                mappedSortBy
        );
        return PageRequest.of(getPage(), getSize(), sort);
    }
}

