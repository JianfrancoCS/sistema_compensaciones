package com.agropay.core.hiring.application.service;

import com.agropay.core.hiring.application.usecase.IAddendumTypeUseCase;
import com.agropay.core.hiring.domain.AddendumTypeEntity;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.hiring.mapper.IAddendumTypeMapper;
import com.agropay.core.hiring.model.addendumtype.AddendumTypeSelectOptionDTO;
import com.agropay.core.hiring.persistence.IAddendumTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddendumTypeServiceImpl implements IAddendumTypeUseCase {

    private final IAddendumTypeRepository addendumTypeRepository;
    private final IAddendumTypeMapper addendumTypeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AddendumTypeSelectOptionDTO> getSelectOptions() {
        log.info("Fetching select options for addendum types");
        List<AddendumTypeEntity> addendumTypes = addendumTypeRepository.findAll();
        return addendumTypeMapper.toSelectOptionDTOs(addendumTypes);
    }

    @Override
    @Transactional(readOnly = true)
    public AddendumTypeEntity findByPublicId(UUID publicId) {
        return addendumTypeRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IdentifierNotFoundException("exception.hiring.addendum-type.not-found", publicId));
    }
}