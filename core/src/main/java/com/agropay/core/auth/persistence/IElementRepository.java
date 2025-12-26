package com.agropay.core.auth.persistence;

import com.agropay.core.auth.domain.ElementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface IElementRepository extends JpaRepository<ElementEntity, Short>, JpaSpecificationExecutor<ElementEntity> {
    Optional<ElementEntity> findByPublicId(UUID publicId);
    Optional<ElementEntity> findByName(String name);
    
    @Query("SELECT e FROM ElementEntity e " +
           "JOIN ProfileElementEntity pe ON pe.element.id = e.id " +
           "WHERE pe.profile.id = :profileId " +
           "AND e.isActive = true " +
           "AND e.deletedAt IS NULL " +
           "AND pe.deletedAt IS NULL " +
           "ORDER BY e.orderIndex")
    List<ElementEntity> findElementsByProfileId(@Param("profileId") Short profileId);
    
    @Query("SELECT e FROM ElementEntity e " +
           "JOIN ProfileElementEntity pe ON pe.element.id = e.id " +
           "WHERE pe.profile.id = :profileId " +
           "AND ((:containerId IS NULL AND e.container IS NULL) OR (:containerId IS NOT NULL AND e.container.id = :containerId)) " +
           "AND e.isActive = true " +
           "AND e.deletedAt IS NULL " +
           "AND pe.deletedAt IS NULL " +
           "ORDER BY e.orderIndex")
    List<ElementEntity> findElementsByProfileIdAndContainerId(
            @Param("profileId") Short profileId,
            @Param("containerId") Short containerId);
    
    @Query("SELECT e FROM ElementEntity e " +
           "JOIN ProfileElementEntity pe ON pe.element.id = e.id " +
           "WHERE pe.profile.id = :profileId " +
           "AND ((:containerId IS NULL AND e.container IS NULL) OR (:containerId IS NOT NULL AND e.container.id = :containerId)) " +
           "AND e.isActive = true " +
           "AND e.deletedAt IS NULL " +
           "AND pe.deletedAt IS NULL " +
           "AND ((:platform = 'WEB' AND e.isWeb = true) OR (:platform = 'MOBILE' AND e.isMobile = true) OR (:platform = 'DESKTOP' AND e.isDesktop = true)) " +
           "ORDER BY e.orderIndex")
    List<ElementEntity> findElementsByProfileIdAndContainerIdAndPlatform(
            @Param("profileId") Short profileId,
            @Param("containerId") Short containerId,
            @Param("platform") String platform);
    
    @Query("SELECT e FROM ElementEntity e " +
           "WHERE e.isActive = true " +
           "AND e.deletedAt IS NULL " +
           "ORDER BY e.orderIndex")
    List<ElementEntity> findAllActive();
}

