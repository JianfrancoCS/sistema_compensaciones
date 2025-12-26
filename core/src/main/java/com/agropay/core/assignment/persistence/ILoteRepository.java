package com.agropay.core.assignment.persistence;

import com.agropay.core.assignment.domain.LoteEntity;
import com.agropay.core.shared.generic.persistence.IFindByPublicIdRepository;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ILoteRepository extends ISoftRepository<LoteEntity, Integer>,
        IFindByPublicIdRepository<LoteEntity>,
        JpaSpecificationExecutor<LoteEntity> {

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM LoteEntity l " +
           "WHERE LOWER(l.name) = LOWER(:name) " +
           "AND l.subsidiary.publicId = :subsidiaryPublicId " +
           "AND l.deletedAt IS NULL")
    boolean existsByNameAndSubsidiaryPublicId(@Param("name") String name, @Param("subsidiaryPublicId") UUID subsidiaryPublicId);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM LoteEntity l " +
           "WHERE LOWER(l.name) = LOWER(:name) " +
           "AND l.subsidiary.publicId = :subsidiaryPublicId " +
           "AND l.publicId <> :publicId " +
           "AND l.deletedAt IS NULL")
    boolean existsByNameAndSubsidiaryPublicIdAndPublicIdNot(@Param("name") String name,
                                                              @Param("subsidiaryPublicId") UUID subsidiaryPublicId,
                                                              @Param("publicId") UUID publicId);
}