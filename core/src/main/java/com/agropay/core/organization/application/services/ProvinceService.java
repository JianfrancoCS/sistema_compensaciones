package com.agropay.core.organization.application.services;

import com.agropay.core.organization.application.usecase.IProvinceUseCase;
import com.agropay.core.organization.domain.ProvinceEntity;
import com.agropay.core.organization.persistence.IProvinceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProvinceService implements IProvinceUseCase {
    private final IProvinceRepository provinceRepository;


    @Override
    public List<ProvinceEntity> getAllByIdentifier(UUID identifier) {
        List<ProvinceEntity> allByIdentifier = provinceRepository.getAllByIdentifier(identifier);
        return allByIdentifier;
    }
}
