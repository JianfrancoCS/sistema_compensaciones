package com.agropay.core.auth.persistence;

import com.agropay.core.auth.domain.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUserProfileRepository extends JpaRepository<UserProfileEntity, Short> {

    /**
     * Encuentra todos los perfiles activos asignados a un usuario
     */
    @Query("SELECT up FROM UserProfileEntity up " +
           "WHERE up.user.id = :userId " +
           "AND up.isActive = true " +
           "AND up.deletedAt IS NULL")
    List<UserProfileEntity> findActiveProfilesByUserId(@Param("userId") Short userId);

    /**
     * Encuentra todos los perfiles (activos e inactivos) asignados a un usuario
     */
    @Query("SELECT up FROM UserProfileEntity up " +
           "WHERE up.user.id = :userId " +
           "AND up.deletedAt IS NULL")
    List<UserProfileEntity> findAllProfilesByUserId(@Param("userId") Short userId);

    /**
     * Encuentra un perfil específico asignado a un usuario
     */
    @Query("SELECT up FROM UserProfileEntity up " +
           "WHERE up.user.id = :userId " +
           "AND up.profile.id = :profileId " +
           "AND up.deletedAt IS NULL")
    Optional<UserProfileEntity> findByUserIdAndProfileId(
            @Param("userId") Short userId,
            @Param("profileId") Short profileId);

    /**
     * Encuentra por public ID
     */
    Optional<UserProfileEntity> findByPublicId(UUID publicId);

    /**
     * Verifica si un usuario tiene un perfil específico activo
     */
    @Query("SELECT COUNT(up) > 0 FROM UserProfileEntity up " +
           "WHERE up.user.id = :userId " +
           "AND up.profile.id = :profileId " +
           "AND up.isActive = true " +
           "AND up.deletedAt IS NULL")
    boolean existsActiveByUserIdAndProfileId(
            @Param("userId") Short userId,
            @Param("profileId") Short profileId);
}

