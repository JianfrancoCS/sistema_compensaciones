package com.agropay.core.auth.persistence;

import com.agropay.core.auth.domain.ContainerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IContainerRepository extends JpaRepository<ContainerEntity, Short>, JpaSpecificationExecutor<ContainerEntity> {
    Optional<ContainerEntity> findByPublicId(UUID publicId);
    Optional<ContainerEntity> findByName(String name);
    
    @Query("SELECT DISTINCT c FROM ContainerEntity c " +
           "JOIN ElementEntity e ON e.container.id = c.id " +
           "JOIN ProfileElementEntity pe ON pe.element.id = e.id " +
           "WHERE pe.profile.id = :profileId " +
           "AND c.isActive = true " +
           "AND c.deletedAt IS NULL " +
           "AND e.isActive = true " +
           "AND e.deletedAt IS NULL " +
           "AND pe.deletedAt IS NULL " +
           "ORDER BY c.orderIndex")
    List<ContainerEntity> findContainersByProfileId(@Param("profileId") Short profileId);
    
    @Query("SELECT DISTINCT c FROM ContainerEntity c " +
           "JOIN ElementEntity e ON e.container.id = c.id " +
           "JOIN ProfileElementEntity pe ON pe.element.id = e.id " +
           "WHERE pe.profile.id = :profileId " +
           "AND c.isActive = true " +
           "AND c.deletedAt IS NULL " +
           "AND e.isActive = true " +
           "AND e.deletedAt IS NULL " +
           "AND pe.deletedAt IS NULL " +
           "AND ((:platform = 'WEB' AND c.isWeb = true) OR (:platform = 'MOBILE' AND c.isMobile = true) OR (:platform = 'DESKTOP' AND c.isDesktop = true)) " +
           "ORDER BY c.orderIndex")
    List<ContainerEntity> findContainersByProfileIdAndPlatform(
            @Param("profileId") Short profileId,
            @Param("platform") String platform);
    
    List<ContainerEntity> findByIsActiveTrueAndDeletedAtIsNullOrderByOrderIndex();
}

