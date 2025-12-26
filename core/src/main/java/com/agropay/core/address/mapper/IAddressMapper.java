package com.agropay.core.address.mapper;

import com.agropay.core.address.domain.AddressEntity;
import com.agropay.core.address.models.AddressDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IAddressMapper {

    AddressEntity toEntity (AddressDTO addressDTO);
}
