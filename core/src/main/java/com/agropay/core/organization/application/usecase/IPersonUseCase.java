package com.agropay.core.organization.application.usecase;

import com.agropay.core.organization.api.IPersonAPI;
import com.agropay.core.organization.domain.PersonEntity;
import com.agropay.core.organization.model.person.CommandPersonResponse;
import com.agropay.core.organization.model.person.CreatePersonManualRequest;
import com.agropay.core.organization.model.person.UpdateAddressPersonRequest;
import com.agropay.core.organization.model.person.UpdatePersonManualRequest;
import com.agropay.core.organization.model.documenttype.DocumentTypeSelectOptionDTO;

import java.util.List;


public interface IPersonUseCase extends IPersonAPI {
//    PersonEntity create(CreatePersonRequest request);
    CommandPersonResponse updateAddressPerson(UpdateAddressPersonRequest request, String documentNumber);
//    PersonEntity getByIdentifier(String documentNumber);

//    PersonExternalInfo fetchExternalPerson(String documentNumber);

    // Métodos para registro manual de personas extranjeras
    CommandPersonResponse createPersonManual(CreatePersonManualRequest request);
    CommandPersonResponse updatePersonManual(String documentNumber, UpdatePersonManualRequest request);
    List<DocumentTypeSelectOptionDTO> getDocumentTypesForSelect();

    // Método de búsqueda con filtro opcional de tipo de documento
    PersonEntity findPersonByDocumentNumber(String documentNumber, Boolean isNational);
}
