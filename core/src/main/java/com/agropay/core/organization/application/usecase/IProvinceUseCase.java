package com.agropay.core.organization.application.usecase;

import com.agropay.core.organization.domain.ProvinceEntity;
import com.agropay.core.shared.generic.usecase.IGetAllByIdentifier;

import java.util.UUID;

public interface IProvinceUseCase extends IGetAllByIdentifier<ProvinceEntity, UUID> {
}
