package com.agropay.core.auth.persistence;

import com.agropay.core.auth.domain.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IProfileRepository extends JpaRepository<ProfileEntity, Short>, JpaSpecificationExecutor<ProfileEntity> {
    Optional<ProfileEntity> findByPublicId(UUID publicId);
    Optional<ProfileEntity> findByName(String name);
    boolean existsByName(String name);
    
    /**
     * Encuentra todos los perfiles activos ordenados por nombre
     */
    List<ProfileEntity> findByIsActiveTrueAndDeletedAtIsNullOrderByName();
}

