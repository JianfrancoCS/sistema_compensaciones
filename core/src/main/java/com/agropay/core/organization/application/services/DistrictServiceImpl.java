package com.agropay.core.organization.application.services;

import com.agropay.core.organization.application.usecase.IDistrictUseCase;
import com.agropay.core.organization.domain.DistrictEntity;
import com.agropay.core.organization.mapper.ILocationMapper;
import com.agropay.core.organization.model.location.DistrictDetailResponseDTO;
import com.agropay.core.organization.persistence.IDistrictRepository;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistrictServiceImpl implements IDistrictUseCase {

    private final IDistrictRepository districtRepository;
    private final ILocationMapper locationMapper;

    @Override
    public List<DistrictEntity> getAllByIdentifier(UUID identifier) {
        return districtRepository.getAllByIdentifier(identifier);
    }

    /**
     * @deprecated Use findByPublicId for clarity and consistency.
     */
    @Override
    @Deprecated(since = "1.0.0", forRemoval = true)
    public DistrictEntity getByIdentifier(UUID identifier) {
        Optional<DistrictEntity> byPublicId = districtRepository.findByPublicId(identifier);
        if (byPublicId.isEmpty()){
            throw new IdentifierNotFoundException("Identifier of district "+identifier+" not found");
        }
        return byPublicId.get();
    }

    @Override
    public DistrictEntity findByPublicId(UUID publicId) {
        log.debug("Attempting to find district entity with public ID: {}", publicId);
        return districtRepository.findByPublicId(publicId)
                .orElseThrow(() -> {
                    log.warn("District with public ID {} not found.", publicId);
                    return new IdentifierNotFoundException("exception.shared.identifier-not-found", publicId);
                });
    }

    @Override
    public DistrictDetailResponseDTO getDetailByPublicId(UUID id) {
        return locationMapper.toDistrictDetailResponse(findByPublicId(id));
    }
}
