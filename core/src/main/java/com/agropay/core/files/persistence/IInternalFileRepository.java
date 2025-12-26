package com.agropay.core.files.persistence;

import com.agropay.core.files.domain.InternalFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IInternalFileRepository extends JpaRepository<InternalFileEntity, Long> {

    Optional<InternalFileEntity> findByPublicIdAndDeletedAtIsNull(UUID publicId);

    List<InternalFileEntity> findByFileableIdAndFileableTypeAndDeletedAtIsNull(
            String fileableId,
            String fileableType
    );

    @Query("SELECT f FROM InternalFileEntity f WHERE f.fileableId = :fileableId " +
           "AND f.fileableType = :fileableType AND f.category = :category AND f.deletedAt IS NULL")
    List<InternalFileEntity> findByFileableAndCategory(
            @Param("fileableId") String fileableId,
            @Param("fileableType") String fileableType,
            @Param("category") String category
    );

    @Modifying
    @Query("UPDATE InternalFileEntity f SET f.deletedAt = CURRENT_TIMESTAMP, f.deletedBy = :deletedBy " +
           "WHERE f.fileableId = :fileableId AND f.fileableType = :fileableType AND f.deletedAt IS NULL")
    void softDeleteByFileable(
            @Param("fileableId") String fileableId,
            @Param("fileableType") String fileableType,
            @Param("deletedBy") String deletedBy
    );

    @Modifying
    @Query("UPDATE InternalFileEntity f SET f.deletedAt = CURRENT_TIMESTAMP, f.deletedBy = :deletedBy " +
           "WHERE f.fileableId = :fileableId AND f.fileableType = :fileableType AND f.category = :category AND f.deletedAt IS NULL")
    void softDeleteByFileableAndCategory(
            @Param("fileableId") String fileableId,
            @Param("fileableType") String fileableType,
            @Param("category") String category,
            @Param("deletedBy") String deletedBy
    );

    @Modifying
    @Query("UPDATE InternalFileEntity f SET f.deletedAt = CURRENT_TIMESTAMP, f.deletedBy = :deletedBy " +
           "WHERE f.publicId IN :publicIds AND f.deletedAt IS NULL")
    void softDeleteByPublicIds(
            @Param("publicIds") List<UUID> publicIds,
            @Param("deletedBy") String deletedBy
    );
}

