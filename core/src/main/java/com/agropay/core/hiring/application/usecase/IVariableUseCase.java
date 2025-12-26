package com.agropay.core.hiring.application.usecase;

import com.agropay.core.hiring.model.variable.CommandVariableResponse;
import com.agropay.core.hiring.model.variable.CreateVariableRequest;
import com.agropay.core.hiring.model.variable.UpdateVariableRequest;
import com.agropay.core.hiring.model.variable.CreateVariableWithValidationRequest;
import com.agropay.core.hiring.model.variable.UpdateVariableWithValidationRequest;
import com.agropay.core.hiring.model.variable.VariableListDTO;
import com.agropay.core.hiring.model.variable.VariableSelectOptionDTO;
import com.agropay.core.hiring.model.variable.AssociateMethodsRequest;
import com.agropay.core.hiring.model.variable.VariableWithValidationDTO;
import com.agropay.core.shared.utils.PagedResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IVariableUseCase {
    CommandVariableResponse create(CreateVariableRequest request);

    CommandVariableResponse update(UUID publicId, UpdateVariableRequest request);

    void deleteByPublicId(UUID publicId);

    List<VariableSelectOptionDTO> getSelectOptions();

    List<VariableSelectOptionDTO> getSelectOptions(String name);

    PagedResult<VariableListDTO> findAllPaged(String code, String name, Pageable pageable);

    CommandVariableResponse associateMethods(UUID variablePublicId, AssociateMethodsRequest request);

    CommandVariableResponse updateMethods(UUID variablePublicId, AssociateMethodsRequest request);

    void disassociateMethods(UUID variablePublicId);

    VariableWithValidationDTO getVariableWithValidation(UUID variablePublicId);

    CommandVariableResponse createWithValidation(CreateVariableWithValidationRequest request);

    CommandVariableResponse updateWithValidation(UUID publicId, UpdateVariableWithValidationRequest request);
}
