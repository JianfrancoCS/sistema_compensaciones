package com.agropay.core.organization.api;

import com.agropay.core.organization.domain.PositionEntity;

import java.util.UUID;

public interface IPositionAPI {
    PositionEntity findByPublicId(UUID publicId);
}
