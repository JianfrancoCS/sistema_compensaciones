package com.agropay.core.assignment.persistence;

import com.agropay.core.assignment.domain.LaborEntity;
import com.agropay.core.shared.generic.persistence.IFindByPublicIdRepository;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ILaborRepository extends ISoftRepository<LaborEntity, Short>,
        IFindByPublicIdRepository<LaborEntity>,
        JpaSpecificationExecutor<LaborEntity> {

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM LaborEntity l " +
           "WHERE LOWER(l.name) = LOWER(:name) " +
           "AND l.deletedAt IS NULL")
    boolean existsByName(@Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM LaborEntity l " +
           "WHERE LOWER(l.name) = LOWER(:name) " +
           "AND l.publicId <> :publicId " +
           "AND l.deletedAt IS NULL")
    boolean existsByNameAndPublicIdNot(@Param("name") String name, @Param("publicId") UUID publicId);

    @Query("SELECT COUNT(t) FROM TareoEntity t WHERE t.labor.id = :laborId AND t.deletedAt IS NULL")
    long countTareosByLaborId(@Param("laborId") Short laborId);
}