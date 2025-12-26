package com.agropay.core.assignment.persistence;

import com.agropay.core.assignment.domain.LaborUnitEntity;
import com.agropay.core.shared.generic.persistence.IFindByPublicIdRepository;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ILaborUnitRepository extends ISoftRepository<LaborUnitEntity, Short>,
        IFindByPublicIdRepository<LaborUnitEntity>,
        JpaSpecificationExecutor<LaborUnitEntity> {

    @Query("SELECT CASE WHEN COUNT(lu) > 0 THEN true ELSE false END FROM LaborUnitEntity lu " +
           "WHERE LOWER(lu.name) = LOWER(:name) AND lu.deletedAt IS NULL")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(lu) > 0 THEN true ELSE false END FROM LaborUnitEntity lu " +
           "WHERE LOWER(lu.abbreviation) = LOWER(:abbreviation) AND lu.deletedAt IS NULL")
    boolean existsByAbbreviationIgnoreCase(@Param("abbreviation") String abbreviation);

    @Query("SELECT CASE WHEN COUNT(lu) > 0 THEN true ELSE false END FROM LaborUnitEntity lu " +
           "WHERE LOWER(lu.name) = LOWER(:name) AND lu.publicId <> :publicId AND lu.deletedAt IS NULL")
    boolean existsByNameIgnoreCaseAndPublicIdNot(@Param("name") String name, @Param("publicId") UUID publicId);

    @Query("SELECT CASE WHEN COUNT(lu) > 0 THEN true ELSE false END FROM LaborUnitEntity lu " +
           "WHERE LOWER(lu.abbreviation) = LOWER(:abbreviation) AND lu.publicId <> :publicId AND lu.deletedAt IS NULL")
    boolean existsByAbbreviationIgnoreCaseAndPublicIdNot(@Param("abbreviation") String abbreviation, @Param("publicId") UUID publicId);

    @Query("SELECT COUNT(l) FROM LaborEntity l WHERE l.laborUnit.id = :laborUnitId AND l.deletedAt IS NULL")
    long countLaborsByLaborUnitId(@Param("laborUnitId") Short laborUnitId);
}