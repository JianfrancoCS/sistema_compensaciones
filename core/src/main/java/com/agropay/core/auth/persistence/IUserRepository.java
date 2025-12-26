package com.agropay.core.auth.persistence;

import com.agropay.core.auth.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<UserEntity, Short>, JpaSpecificationExecutor<UserEntity> {
    /**
     * Busca un usuario por username, asegurándose de que solo devuelva usuarios activos (no eliminados).
     */
    @Query("SELECT u FROM UserEntity u WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<UserEntity> findByUsername(@Param("username") String username);
    
    /**
     * Busca un usuario por public_id, asegurándose de que solo devuelva usuarios activos (no eliminados).
     * Si hay duplicados, devuelve el primero encontrado.
     */
    @Query("SELECT u FROM UserEntity u WHERE u.publicId = :publicId AND u.deletedAt IS NULL")
    Optional<UserEntity> findByPublicId(@Param("publicId") java.util.UUID publicId);
    
    /**
     * Verifica si existe un usuario con el username dado (solo usuarios activos).
     */
    @Query("SELECT COUNT(u) > 0 FROM UserEntity u WHERE u.username = :username AND u.deletedAt IS NULL")
    boolean existsByUsername(@Param("username") String username);
    
    /**
     * Busca un usuario por employeeId, asegurándose de que solo devuelva usuarios activos (no eliminados).
     */
    @Query("SELECT u FROM UserEntity u WHERE u.employeeId = :employeeId AND u.deletedAt IS NULL")
    Optional<UserEntity> findByEmployeeId(@Param("employeeId") String employeeId);
}

