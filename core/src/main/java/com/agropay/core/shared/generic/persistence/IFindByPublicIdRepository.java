package com.agropay.core.shared.generic.persistence;

import java.util.Optional;
import java.util.UUID;

public interface IFindByPublicIdRepository<M> {
    Optional<M> findByPublicId(UUID publicId);
}
