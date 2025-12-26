package com.agropay.core.images.persistence;

import com.agropay.core.images.domain.ImageEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ImageRepository extends ISoftRepository<ImageEntity, Long> {
    @Query("SELECT i FROM ImageEntity i WHERE i.imageableId = :imageableId AND i.imageableType = :imageableType AND i.deletedAt IS NULL")
    List<ImageEntity> findByImageableIdAndImageableType(
            @Param("imageableId") String imageableId, 
            @Param("imageableType") String imageableType);
    
    Optional<ImageEntity> findByPublicId(UUID publicId);
}
