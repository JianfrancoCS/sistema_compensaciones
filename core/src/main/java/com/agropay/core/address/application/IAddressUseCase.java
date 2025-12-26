package com.agropay.core.address.application;

import com.agropay.core.address.domain.AddressEntity;
import com.agropay.core.shared.generic.usecase.IDeleteUseCase;
import com.agropay.core.shared.generic.usecase.IGetByIdentifier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAddressUseCase extends IGetByIdentifier<AddressEntity, UUID>, IDeleteUseCase<Long> {

    AddressEntity saveForEntity(IAddressableUseCase<?> entity, AddressEntity address);
    Optional<AddressEntity> findPrimaryByEntity(IAddressableUseCase<?> entity);
    List<AddressEntity> findByEntity(IAddressableUseCase<?> entity);

    AddressEntity updateAddress(UUID publicId, AddressEntity updatedAddress);
}