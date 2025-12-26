package com.agropay.core.assignment.application.services;

import com.agropay.core.assignment.application.usecase.ITareoMotiveUseCase;
import com.agropay.core.assignment.domain.TareoMotiveEntity;
import com.agropay.core.assignment.mapper.ITareoMotiveMapper;
import com.agropay.core.assignment.model.tareomotive.*;
import com.agropay.core.assignment.persistence.ITareoMotiveRepository;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.exceptions.ReferentialIntegrityException;
import com.agropay.core.shared.exceptions.UniqueValidationException;
import com.agropay.core.shared.utils.PagedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.agropay.core.assignment.persistence.TareoMotiveSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TareoMotiveService implements ITareoMotiveUseCase {

    private final ITareoMotiveRepository tareoMotiveRepository;
    private final ITareoMotiveMapper tareoMotiveMapper;

    @Override
    @Transactional
    public CommandTareoMotiveResponse create(CreateTareoMotiveRequest request) {
        log.info("Attempting to create a new tareo motive with name: {}", request.name());

        if (tareoMotiveRepository.existsByNameIgnoreCase(request.name())) {
            log.warn("Attempted to create a tareo motive with a name that already exists: {}", request.name());
            throw new UniqueValidationException("exception.assignment.tareo-motive.name-already-exists", request.name());
        }

        TareoMotiveEntity newTareoMotive = tareoMotiveMapper.toEntity(request);
        TareoMotiveEntity savedTareoMotive = tareoMotiveRepository.save(newTareoMotive);
        log.info("Successfully created tareo motive with public ID: {}", savedTareoMotive.getPublicId());
        return tareoMotiveMapper.toResponse(savedTareoMotive);
    }

    @Override
    @Transactional
    public CommandTareoMotiveResponse update(UUID publicId, UpdateTareoMotiveRequest request) {
        log.info("Attempting to update tareo motive with public ID: {}", publicId);
        TareoMotiveEntity existingTareoMotive = findByPublicId(publicId);

        if (tareoMotiveRepository.existsByNameIgnoreCaseAndPublicIdNot(request.name(), publicId)) {
            log.warn("Attempted to update tareo motive {} with a name that already exists: {}", publicId, request.name());
            throw new UniqueValidationException("exception.assignment.tareo-motive.name-already-exists", request.name());
        }

        tareoMotiveMapper.updateEntityFromRequest(request, existingTareoMotive);
        TareoMotiveEntity updatedTareoMotive = tareoMotiveRepository.save(existingTareoMotive);
        log.info("Successfully updated tareo motive with public ID: {}", updatedTareoMotive.getPublicId());
        return tareoMotiveMapper.toResponse(updatedTareoMotive);
    }

    @Override
    @Transactional
    public void delete(UUID publicId) {
        log.info("Attempting to delete tareo motive with public ID: {}", publicId);
        TareoMotiveEntity tareoMotiveToDelete = findByPublicId(publicId);

        long tareoEmployeeCount = tareoMotiveRepository.countTareoEmployeesByMotiveId(tareoMotiveToDelete.getId());
        if (tareoEmployeeCount > 0) {
            log.warn("Attempted to delete tareo motive {} which still has {} associated tareo employee record(s).", publicId, tareoEmployeeCount);
            throw new ReferentialIntegrityException("exception.assignment.tareo-motive.cannot-delete-has-tareo-employees", tareoEmployeeCount);
        }

        tareoMotiveRepository.delete(tareoMotiveToDelete);
        log.info("Successfully deleted tareo motive with public ID: {}", publicId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TareoMotiveSelectOptionDTO> getSelectOptions() {
        log.info("Fetching all tareo motives for select options.");
        return tareoMotiveMapper.toSelectOptionDTOs(tareoMotiveRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<TareoMotiveListDTO> findAllPaged(String name, Boolean isPaid, Pageable pageable) {
        log.info("Fetching paged list of tareo motives with filters - name: '{}', isPaid: {}, page: {}, size: {}",
                name, isPaid, pageable.getPageNumber(), pageable.getPageSize());

        Specification<TareoMotiveEntity> spec = TareoMotiveSpecification.filterBy(name, isPaid);
        Page<TareoMotiveEntity> tareoMotivePage = tareoMotiveRepository.findAll(spec, pageable);
        return tareoMotiveMapper.toPagedDTO(tareoMotivePage);
    }

    @Override
    public TareoMotiveEntity findByPublicId(UUID publicId) {
        log.info("Attempting to find tareo motive entity with public ID: {}", publicId);
        try {
            TareoMotiveEntity motive = tareoMotiveRepository.findByPublicId(publicId)
                    .orElseThrow(() -> {
                        log.warn("Tareo motive with public ID {} not found.", publicId);
                        return new IdentifierNotFoundException("exception.assignment.tareo-motive.not-found", publicId);
                    });
            log.info("Successfully found tareo motive: {} (ID: {})", motive.getName(), motive.getId());
            return motive;
        } catch (Exception e) {
            log.error("Error finding tareo motive with public ID {}: {}", publicId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TareoMotiveSyncReponse> findAllForSync() {
        log.info("Fetching all tareo motives for mobile sync.");
        return tareoMotiveRepository.findAll().stream()
                .map(motive -> new TareoMotiveSyncReponse(
                        motive.getPublicId(),
                        motive.getName(),
                        motive.getDescription(),
                        motive.getIsPaid()
                ))
                .toList();
    }
}