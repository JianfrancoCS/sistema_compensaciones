package com.agropay.core.auth.persistence;

import com.agropay.core.auth.domain.ProfileElementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;


@Repository
public interface IProfileElementRepository extends JpaRepository<ProfileElementEntity, Short> {
    @Query("SELECT pe FROM ProfileElementEntity pe " +
           "WHERE pe.profile.publicId = :profilePublicId " +
           "AND pe.deletedAt IS NULL")
    List<ProfileElementEntity> findByProfilePublicId(@Param("profilePublicId") UUID profilePublicId);
    
    @Query("SELECT pe FROM ProfileElementEntity pe " +
           "WHERE pe.profile.id = :profileId " +
           "AND pe.deletedAt IS NULL")
    List<ProfileElementEntity> findByProfileId(@Param("profileId") Short profileId);
    
    @Modifying
    @Query("UPDATE ProfileElementEntity pe SET pe.deletedAt = CURRENT_TIMESTAMP " +
           "WHERE pe.profile.id = :profileId")
    void softDeleteByProfileId(@Param("profileId") Short profileId);
    
    @Query("SELECT pe FROM ProfileElementEntity pe " +
           "WHERE pe.profile.id = :profileId " +
           "AND pe.element.id = :elementId " +
           "AND pe.deletedAt IS NULL")
    Optional<ProfileElementEntity> findByProfileIdAndElementId(
            @Param("profileId") Short profileId,
            @Param("elementId") Short elementId);
}

