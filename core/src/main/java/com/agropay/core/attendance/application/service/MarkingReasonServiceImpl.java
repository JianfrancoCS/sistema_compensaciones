package com.agropay.core.attendance.application.service;

import com.agropay.core.attendance.application.usecase.IMarkingReasonUseCase;
import com.agropay.core.attendance.constant.MarkingReasonEnum;
import com.agropay.core.attendance.constant.PersonTypeEnum;
import com.agropay.core.attendance.domain.MarkingReasonEntity;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.attendance.mapper.IMarkingReasonMapper;
import com.agropay.core.attendance.model.markingreason.*;
import com.agropay.core.attendance.persistence.IMarkingReasonRepository;
import com.agropay.core.attendance.persistence.MarkingReasonSpecification;
import com.agropay.core.shared.utils.PagedResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MarkingReasonServiceImpl implements IMarkingReasonUseCase {

    private final IMarkingReasonRepository repository;
    private final IMarkingReasonMapper mapper;


    @Override
    @Transactional(readOnly = true)
    public PagedResult<MarkingReasonListDTO> findAllPaged(MarkingReasonPageableRequest request) {
        Specification<MarkingReasonEntity> spec = MarkingReasonSpecification.filterBy(
            request.getCode(),
            request.getName()
        );

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<MarkingReasonEntity> page = repository.findAll(spec, pageable);

        List<MarkingReasonListDTO> content = page.getContent().stream()
            .map(mapper::toListDTO)
            .toList();

        return new PagedResult<>(
            content,
            page.getTotalElements(),
            page.getNumber(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.hasNext(),
            page.hasPrevious()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarkingReasonSelectOptionDTO> getSelectOptions() {
        List<MarkingReasonEntity> entities = repository.findAll(Sort.by("name"));
        return entities.stream()
            .map(mapper::toSelectOptionDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarkingReasonSelectOptionDTO> getExternalSelectOptions() {
        // Obtener solo las razones permitidas para personas externas
        List<String> externalCodes = java.util.Arrays.stream(MarkingReasonEnum.values())
            .filter(reason -> reason.getAllowedPersonType() == PersonTypeEnum.EXTERNAL)
            .map(MarkingReasonEnum::getCode)
            .toList();

        List<MarkingReasonEntity> entities = repository.findAll(Sort.by("name"))
            .stream()
            .filter(entity -> externalCodes.contains(entity.getCode()))
            .toList();

        return entities.stream()
            .map(mapper::toSelectOptionDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarkingReasonSelectOptionDTO> getInternalMarkingReasons() {
        List<MarkingReasonEntity> entities = repository.findByIsInternalAndDeletedAtIsNull(true, Sort.by("name"));
        return entities.stream()
            .map(mapper::toSelectOptionDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarkingReasonSelectOptionDTO> getExternalMarkingReasons() {
        List<MarkingReasonEntity> entities = repository.findByIsInternalAndDeletedAtIsNull(false, Sort.by("name"));
        return entities.stream()
            .map(mapper::toSelectOptionDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MarkingReasonEntity findByPublicId(UUID publicId) {
        return findEntityByPublicId(publicId);
    }

    @Override
    @Transactional(readOnly = true)
    public MarkingReasonEntity findByCode(String code) {
        return repository.findByCodeAndDeletedAtIsNull(code)
            .orElseThrow(() -> new IdentifierNotFoundException("exception.attendance.marking-reason.not-found", code));
    }

    private MarkingReasonEntity findEntityByPublicId(UUID publicId) {
        return repository.findByPublicId(publicId)
            .orElseThrow(() -> new IdentifierNotFoundException("exception.attendance.marking-reason.not-found", publicId));
    }

}