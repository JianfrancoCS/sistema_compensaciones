package com.agropay.core.address.application;

import com.agropay.core.states.application.AddressService;
import com.agropay.core.address.domain.AddressEntity;

import java.util.List;
import java.util.Optional;

public interface IAddressableUseCase<T> {
    T getId();
    String getAddressableType();

    default List<AddressEntity> getAddresses(AddressService addressService){
        return addressService.findByEntity(this);
    }
    default Optional<AddressEntity> getPrimaryAddress(AddressService addressService) {
        return addressService.findPrimaryByEntity(this);
    }
}
