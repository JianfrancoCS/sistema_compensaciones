package com.agropay.core.attendance.persistence;

import com.agropay.core.attendance.domain.MarkingDetailEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IMarkingDetailRepository extends ISoftRepository<MarkingDetailEntity, Long>, JpaSpecificationExecutor<MarkingDetailEntity> {

    Optional<MarkingDetailEntity> findByPublicId(UUID publicId);

    @Query("SELECT md FROM MarkingDetailEntity md " +
           "WHERE md.personDocumentNumber = :documentNumber " +
           "AND CAST(md.markedAt AS DATE) = :markingDate " +
           "AND md.deletedAt IS NULL " +
           "ORDER BY md.markedAt DESC")
    List<MarkingDetailEntity> findByPersonAndDate(
        @Param("documentNumber") String documentNumber,
        @Param("markingDate") LocalDate markingDate
    );

    @Query("SELECT md FROM MarkingDetailEntity md " +
           "WHERE md.personDocumentNumber = :documentNumber " +
           "AND CAST(md.markedAt AS DATE) = :markingDate " +
           "AND md.isEntry = :isEntry " +
           "AND md.deletedAt IS NULL")
    Optional<MarkingDetailEntity> findByPersonDateAndType(
        @Param("documentNumber") String documentNumber,
        @Param("markingDate") LocalDate markingDate,
        @Param("isEntry") Boolean isEntry
    );

    @Query("SELECT md FROM MarkingDetailEntity md " +
           "WHERE md.personDocumentNumber = :documentNumber " +
           "AND CAST(md.markedAt AS DATE) = :markingDate " +
           "AND md.isEmployee = :isEmployee " +
           "AND md.deletedAt IS NULL " +
           "ORDER BY md.markedAt ASC")
    List<MarkingDetailEntity> findByPersonAndDateAndIsEmployee(
        @Param("documentNumber") String documentNumber,
        @Param("markingDate") LocalDate markingDate,
        @Param("isEmployee") Boolean isEmployee
    );

    // Queries para conteo de asistencias por subsidiaria
    @Query("SELECT COUNT(DISTINCT md.personDocumentNumber) FROM MarkingDetailEntity md " +
           "JOIN md.marking m " +
           "WHERE m.subsidiary.publicId = :subsidiaryPublicId " +
           "AND m.markingDate = :date " +
           "AND md.isEmployee = :isEmployee " +
           "AND md.isEntry = :isEntry " +
           "AND md.deletedAt IS NULL")
    Long countBySubsidiaryAndDateAndIsEmployeeAndIsEntry(
        @Param("subsidiaryPublicId") UUID subsidiaryPublicId,
        @Param("date") LocalDate date,
        @Param("isEmployee") Boolean isEmployee,
        @Param("isEntry") Boolean isEntry
    );

    // Query flexible para conteo con filtro opcional de tipo de persona
    @Query("SELECT COUNT(DISTINCT md.personDocumentNumber) FROM MarkingDetailEntity md " +
           "JOIN md.marking m " +
           "WHERE m.subsidiary.publicId = :subsidiaryPublicId " +
           "AND m.markingDate = :date " +
           "AND md.isEntry = :isEntry " +
           "AND (:isEmployee IS NULL OR md.isEmployee = :isEmployee) " +
           "AND md.deletedAt IS NULL")
    Long countFlexibleBySubsidiaryAndDateAndIsEntry(
        @Param("subsidiaryPublicId") UUID subsidiaryPublicId,
        @Param("date") LocalDate date,
        @Param("isEntry") Boolean isEntry,
        @Param("isEmployee") Boolean isEmployee
    );

    @Query("SELECT CASE WHEN COUNT(md) > 0 THEN true ELSE false END FROM MarkingDetailEntity md " +
           "JOIN md.marking m " +
           "WHERE md.personDocumentNumber = :documentNumber " +
           "AND m.markingDate = :date " +
           "AND md.isEntry = true " +
           "AND md.isEmployee = true " +
           "AND md.deletedAt IS NULL")
    boolean hasEmployeeMarkedEntryOnDate(
        @Param("documentNumber") String documentNumber,
        @Param("date") LocalDate date
    );
}