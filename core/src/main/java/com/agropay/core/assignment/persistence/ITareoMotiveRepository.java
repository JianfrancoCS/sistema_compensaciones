package com.agropay.core.assignment.persistence;

import com.agropay.core.assignment.domain.TareoMotiveEntity;
import com.agropay.core.shared.generic.persistence.IFindByPublicIdRepository;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ITareoMotiveRepository extends ISoftRepository<TareoMotiveEntity, Short>,
        IFindByPublicIdRepository<TareoMotiveEntity>,
        JpaSpecificationExecutor<TareoMotiveEntity> {

    @Query("SELECT CASE WHEN COUNT(tm) > 0 THEN true ELSE false END FROM TareoMotiveEntity tm " +
           "WHERE LOWER(tm.name) = LOWER(:name) AND tm.deletedAt IS NULL")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(tm) > 0 THEN true ELSE false END FROM TareoMotiveEntity tm " +
           "WHERE LOWER(tm.name) = LOWER(:name) AND tm.publicId <> :publicId AND tm.deletedAt IS NULL")
    boolean existsByNameIgnoreCaseAndPublicIdNot(@Param("name") String name, @Param("publicId") UUID publicId);

    @Query("SELECT COUNT(tem) FROM TareoEmployeeMotiveEntity tem " +
           "WHERE tem.motive.id = :motiveId")
    long countTareoEmployeesByMotiveId(@Param("motiveId") Short motiveId);
}