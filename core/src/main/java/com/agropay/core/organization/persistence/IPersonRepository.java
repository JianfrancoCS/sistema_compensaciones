package com.agropay.core.organization.persistence;

import com.agropay.core.organization.domain.PersonEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface IPersonRepository extends ISoftRepository<PersonEntity, String>, JpaSpecificationExecutor<PersonEntity> {

    /**
     * Cuenta los dependientes (hijos) de una persona/empleado
     */
    Long countByPersonParentDocumentNumber(String parentDocumentNumber);
}
