package com.agropay.core.assignment.application.services;

import com.agropay.core.assignment.application.usecase.ILoteUseCase;
import com.agropay.core.assignment.domain.LoteEntity;
import com.agropay.core.assignment.mapper.ILoteMapper;
import com.agropay.core.assignment.model.lote.*;
import com.agropay.core.assignment.persistence.ILoteRepository;
import com.agropay.core.assignment.persistence.LoteSpecification;
import com.agropay.core.organization.application.usecase.ISubsidiaryUseCase;
import com.agropay.core.organization.domain.SubsidiaryEntity;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.exceptions.UniqueValidationException;
import com.agropay.core.shared.utils.PagedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class LoteService implements ILoteUseCase {

    private final ILoteRepository loteRepository;
    private final ILoteMapper loteMapper;
    private final ISubsidiaryUseCase subsidiaryUseCase;

    @Override
    @Transactional
    public CommandLoteResponse create(CreateLoteRequest request) {
        log.info("Attempting to create a new lote with name: {}", request.name());

        // Validar que subsidiary existe
        SubsidiaryEntity subsidiary = subsidiaryUseCase.findByPublicId(request.subsidiaryPublicId());

        // Validar unicidad usando publicId directamente
        if (loteRepository.existsByNameAndSubsidiaryPublicId(request.name(), request.subsidiaryPublicId())) {
            log.warn("Attempted to create a lote with a name that already exists in subsidiary: {}", request.name());
            throw new UniqueValidationException("exception.assignment.lote.name-already-exists-in-subsidiary", request.name());
        }

        LoteEntity newLote = loteMapper.toEntity(request);
        newLote.setSubsidiary(subsidiary);

        LoteEntity savedLote = loteRepository.save(newLote);
        log.info("Successfully created lote with public ID: {}", savedLote.getPublicId());
        return loteMapper.toResponse(savedLote);
    }

    @Override
    @Transactional
    public CommandLoteResponse update(UUID publicId, UpdateLoteRequest request) {
        log.info("Attempting to update lote with public ID: {}", publicId);
        LoteEntity existingLote = findByPublicId(publicId);

        // Validar que subsidiary existe
        SubsidiaryEntity subsidiary = subsidiaryUseCase.findByPublicId(request.subsidiaryPublicId());

        // Validar unicidad usando publicId directamente
        if (loteRepository.existsByNameAndSubsidiaryPublicIdAndPublicIdNot(request.name(), request.subsidiaryPublicId(), publicId)) {
            log.warn("Attempted to update lote {} with a name that already exists in subsidiary: {}", publicId, request.name());
            throw new UniqueValidationException("exception.assignment.lote.name-already-exists-in-subsidiary", request.name());
        }

        loteMapper.updateEntityFromRequest(request, existingLote);
        existingLote.setSubsidiary(subsidiary);

        LoteEntity updatedLote = loteRepository.save(existingLote);
        log.info("Successfully updated lote with public ID: {}", updatedLote.getPublicId());
        return loteMapper.toResponse(updatedLote);
    }

    @Override
    @Transactional
    public void delete(UUID publicId) {
        log.info("Attempting to delete lote with public ID: {}", publicId);
        LoteEntity loteToDelete = findByPublicId(publicId);

        loteRepository.delete(loteToDelete);
        log.info("Successfully deleted lote with public ID: {}", publicId);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<LoteListDTO> findAllPaged(String name, UUID subsidiaryPublicId, Pageable pageable) {
        log.info("Fetching paged list of lotes with filters - name: '{}', subsidiaryPublicId: {}, page: {}, size: {}",
                name, subsidiaryPublicId, pageable.getPageNumber(), pageable.getPageSize());

        Specification<LoteEntity> spec = LoteSpecification.filterBy(name, subsidiaryPublicId);
        Page<LoteEntity> lotePage = loteRepository.findAll(spec, pageable);
        return loteMapper.toPagedDTO(lotePage);
    }

    @Override
    @Transactional(readOnly = true)
    public CommandLoteResponse findById(UUID publicId) {
        log.debug("Attempting to find lote with public ID: {}", publicId);
        LoteEntity lote = findByPublicId(publicId);
        return loteMapper.toResponse(lote);
    }

    @Override
    public LoteEntity findByPublicId(UUID publicId) {
        log.debug("Attempting to find lote entity with public ID: {}", publicId);
        return loteRepository.findByPublicId(publicId)
                .orElseThrow(() -> {
                    log.warn("Lote with public ID {} not found.", publicId);
                    return new IdentifierNotFoundException("exception.assignment.lote.not-found", publicId);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoteSyncResponse> getAllForSync() {
        log.info("Fetching all active lotes for mobile sync");
        List<LoteEntity> lotes = loteRepository.findAll();
        log.info("Found {} active lotes for sync", lotes.size());
        return loteMapper.toSyncResponses(lotes);
    }
}