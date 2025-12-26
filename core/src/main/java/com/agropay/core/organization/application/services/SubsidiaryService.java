package com.agropay.core.organization.application.services;

import com.agropay.core.organization.api.ISubsidiaryAPI;
import com.agropay.core.organization.application.usecase.ICompanyUseCase;
import com.agropay.core.organization.application.usecase.IDistrictUseCase;
import com.agropay.core.organization.application.usecase.ISubsidiaryUseCase;
import com.agropay.core.organization.domain.CompanyEntity;
import com.agropay.core.organization.domain.DistrictEntity;
import com.agropay.core.organization.domain.SubsidiaryEntity;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.exceptions.UniqueValidationException;
import com.agropay.core.organization.mapper.ISubsidiaryMapper;
import com.agropay.core.organization.model.subsidiary.*;
import com.agropay.core.organization.persistence.IEmployeeRepository;
import com.agropay.core.organization.persistence.ISubsidiaryRepository;
import com.agropay.core.shared.exceptions.ReferentialIntegrityException;
import com.agropay.core.shared.utils.PagedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubsidiaryService implements ISubsidiaryUseCase, ISubsidiaryAPI {

    private final ISubsidiaryRepository subsidiaryRepository;
    private final ISubsidiaryMapper subsidiaryMapper;
    private final ICompanyUseCase companyUseCase;
    private final IDistrictUseCase districtUseCase;
    private final IEmployeeRepository employeeRepository;

    @Override
    @Transactional
    public CommandSubsidiaryResponse create(CreateSubsidiaryRequest request) {
        log.info("Attempting to create a new subsidiary with name: {} in district: {}", request.name(), request.districtPublicId());
        subsidiaryRepository.findByNameIgnoreCase(request.name()).ifPresent(s -> {
            log.warn("Attempted to create a subsidiary with a name that already exists: {}", request.name());
            throw new UniqueValidationException("exception.organization.subsidiary.name-already-exists", request.name());
        });

        CompanyEntity primaryCompany = companyUseCase.getPrimaryCompanyEntity();
        DistrictEntity district = districtUseCase.findByPublicId(request.districtPublicId());

        SubsidiaryEntity newSubsidiary = subsidiaryMapper.toEntity(request);
        newSubsidiary.setCompany(primaryCompany);
        newSubsidiary.setDistrict(district);

        SubsidiaryEntity savedSubsidiary = subsidiaryRepository.save(newSubsidiary);
        log.info("Successfully created subsidiary with public ID: {} in district: {}", savedSubsidiary.getPublicId(), district.getName());
        return subsidiaryMapper.toResponse(savedSubsidiary);
    }

    @Override
    @Transactional
    public CommandSubsidiaryResponse update(UUID publicId, UpdateSubsidiaryRequest request) {
        log.info("Attempting to update subsidiary with public ID: {} with name: {} and district: {}", publicId, request.name(), request.districtPublicId());
        SubsidiaryEntity existingSubsidiary = findByPublicId(publicId);
        subsidiaryRepository.findByNameIgnoreCase(request.name()).ifPresent(s -> {
            if (!Objects.equals(s.getPublicId(), publicId)) {
                log.warn("Attempted to update subsidiary {} with a name that already exists: {}", publicId, request.name());
                throw new UniqueValidationException("exception.organization.subsidiary.name-already-exists", request.name());
            }
        });

        DistrictEntity district = districtUseCase.findByPublicId(request.districtPublicId());
        subsidiaryMapper.updateEntityFromRequest(request, existingSubsidiary);
        existingSubsidiary.setDistrict(district);

        SubsidiaryEntity updatedSubsidiary = subsidiaryRepository.save(existingSubsidiary);
        log.info("Successfully updated subsidiary with public ID: {} in district: {}", updatedSubsidiary.getPublicId(), district.getName());
        return subsidiaryMapper.toResponse(updatedSubsidiary);
    }

    @Override
    @Transactional
    public void delete(UUID publicId) {
        log.info("Attempting to delete subsidiary with public ID: {}", publicId);
        SubsidiaryEntity subsidiaryToDelete = findByPublicId(publicId);
        long employeeCount = employeeRepository.countBySubsidiaryId(subsidiaryToDelete.getId());
        if (employeeCount > 0) {
            log.warn("Attempted to delete subsidiary {} which still has {} associated employees.", publicId, employeeCount);
            throw new ReferentialIntegrityException("exception.organization.subsidiary.cannot-delete-has-employees");
        }
        subsidiaryRepository.delete(subsidiaryToDelete);
        log.info("Successfully deleted subsidiary with public ID: {}", publicId);
    }

    @Override
    @Transactional(readOnly = true)
    public SubsidiaryDetailsDTO getByPublicId(UUID publicId) {
        log.info("Fetching details for subsidiary with public ID: {}", publicId);
        return subsidiaryRepository.findDetailsByPublicId(publicId)
                .orElseThrow(() -> new IdentifierNotFoundException("exception.organization.subsidiary.not-found", publicId));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<SubsidiaryListDTO> findAllPaged(String name, Pageable pageable) {
        log.info("Fetching paged list of subsidiaries with name: \'{}\', page: {}, size: {}", name, pageable.getPageNumber(), pageable.getPageSize());
        
        Specification<SubsidiaryEntity> spec = (root, q, cb) -> {
            if (StringUtils.hasText(name)) {
                return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
            }
            return cb.conjunction();
        };

        Page<SubsidiaryEntity> subsidiaryPage = subsidiaryRepository.findAll(spec, pageable);
        return subsidiaryMapper.toPagedDTO(subsidiaryPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubsidiarySelectOptionDTO> getSelectOptions() {
        log.info("Fetching all subsidiaries for select options.");
        return subsidiaryMapper.toSelectOptionDTOs(subsidiaryRepository.findAll());
    }

    @Override
    public SubsidiaryEntity findByPublicId(UUID publicId) {
        log.debug("Attempting to find subsidiary entity with public ID: {}", publicId);
        return subsidiaryRepository.findByPublicId(publicId)
                .orElseThrow(() -> {
                    log.warn("Subsidiary with public ID {} not found.", publicId);
                    return new IdentifierNotFoundException("exception.organization.subsidiary.not-found", publicId);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubsidiarySyncResponse> findAllForSync() {
        log.info("Fetching all subsidiaries for mobile sync.");
        return subsidiaryRepository.findAll().stream()
                .map(subsidiary -> new SubsidiarySyncResponse(
                        subsidiary.getPublicId(),
                        subsidiary.getName()
                ))
                .toList();
    }

}
