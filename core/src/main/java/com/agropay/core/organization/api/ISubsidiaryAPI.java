package com.agropay.core.organization.api;

import com.agropay.core.organization.domain.SubsidiaryEntity;

import java.util.UUID;

public interface ISubsidiaryAPI {
    SubsidiaryEntity findByPublicId(UUID publicId);
}
