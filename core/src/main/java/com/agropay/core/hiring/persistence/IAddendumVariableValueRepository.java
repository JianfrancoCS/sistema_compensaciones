package com.agropay.core.hiring.persistence;

import com.agropay.core.hiring.domain.AddendumVariableValueEntity;
import com.agropay.core.hiring.domain.AddendumVariableValueId;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IAddendumVariableValueRepository extends ISoftRepository<AddendumVariableValueEntity, AddendumVariableValueId> {
    
    @Query("SELECT avv FROM AddendumVariableValueEntity avv WHERE avv.addendum.publicId = :addendumPublicId")
    List<AddendumVariableValueEntity> findByAddendumPublicId(@Param("addendumPublicId") UUID addendumPublicId);
    
    @Query("DELETE FROM AddendumVariableValueEntity avv WHERE avv.addendum.publicId = :addendumPublicId")
    void deleteByAddendumPublicId(@Param("addendumPublicId") UUID addendumPublicId);
}